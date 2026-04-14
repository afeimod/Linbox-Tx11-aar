/*
 * Copyright © 2010-2011 Intel Corporation
 * Copyright © 2008-2011 Kristian Høgsberg
 * Copyright © 2012-2018,2022 Collabora, Ltd.
 * Copyright © 2010-2011 Benjamin Franzke
 * Copyright © 2013 Jason Ekstrand
 * Copyright © 2017, 2018 General Electric Company
 * Copyright © 2025 Junyu Long
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice (including the
 * next paragraph) shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#include <dlfcn.h>
#include <sys/socket.h>
#include "libweston/libweston.h"
#include "weston.h"
#include "process-util.h"
#include "xalloc.h"

static struct wet_compositor *
to_wet_compositor(struct weston_compositor *compositor)
{
    return weston_compositor_get_user_data(compositor);
}

static void
cleanup_for_child_process() {
    sigset_t allsigs;

    /* Put the client in a new session so it won't catch signals
    * intended for the parent. Sharing a session can be
    * confusing when launching weston under gdb, as the ctrl-c
    * intended for gdb will pass to the child, and weston
    * will cleanly shut down when the child exits.
    */
    setsid();

    /* do not give our signal mask to the new process */
    sigfillset(&allsigs);
    sigprocmask(SIG_UNBLOCK, &allsigs, NULL);
}

WL_EXPORT struct weston_config *
wet_get_config(struct weston_compositor *ec)
{
    return NULL;
}

static char *get_winfusion_lib_dir(void)
{
    Dl_info dl_info;
    if (dladdr((void*) wet_get_libexec_path, &dl_info) && dl_info.dli_fname) {
        const char *dl_path = dl_info.dli_fname;
        size_t i = strlen(dl_path) - 1;

        for (; i > 0 && *(dl_path + i) != '/'; i--);
        char *path = (char*) xcalloc(i, sizeof(char));
        memcpy(path, dl_path, i);

        return path;
    } else {
        weston_log("Failed to get libexec path: dladdr() failed or returned NULL path.");
        return NULL;
    }
}

WL_EXPORT char *
wet_get_libexec_path(const char *name)
{
    char *lib_dir = get_winfusion_lib_dir();
    if (!lib_dir)
        return NULL;

    char *path = NULL;
    asprintf(&path, "%s/%s", lib_dir, name);

    free(lib_dir);
    return path;
}

WL_EXPORT struct wet_process *
wet_client_launch(struct weston_compositor *compositor,
                  struct custom_env *child_env,
                  int *no_cloexec_fds,
                  size_t num_no_cloexec_fds,
                  wet_process_cleanup_func_t cleanup,
                  void *cleanup_data)
{
    struct wet_process *proc = NULL;
    const char *fail_cloexec = "Couldn't unset CLOEXEC on child FDs";
    const char *fail_seteuid = "Couldn't call seteuid";
    char *fail_exec;
    char * const *argp;
    char * const *envp;
    pid_t pid;
    int err;
    size_t i;
    size_t written __attribute__((unused));

    argp = custom_env_get_argp(child_env);
    envp = custom_env_get_envp(child_env);

    weston_log("launching '%s'\n", argp[0]);
    str_printf(&fail_exec, "Error: Couldn't launch client '%s'\n", argp[0]);

    pid = fork();
    switch (pid) {
        case 0:
            cleanup_for_child_process();

            /* Launch clients as the user. Do not launch clients with wrong euid. */
            if (seteuid(getuid()) == -1) {
                written = write(STDERR_FILENO, fail_seteuid,
                                strlen(fail_seteuid));
                _exit(EXIT_FAILURE);
            }

            for (i = 0; i < num_no_cloexec_fds; i++) {
                err = os_fd_clear_cloexec(no_cloexec_fds[i]);
                if (err < 0) {
                    written = write(STDERR_FILENO, fail_cloexec,
                                    strlen(fail_cloexec));
                    _exit(EXIT_FAILURE);
                }
            }

            execve(argp[0], argp, envp);
            if (fail_exec)
                written = write(STDERR_FILENO, fail_exec,
                                strlen(fail_exec));
            _exit(EXIT_FAILURE);

        default:
            proc = xzalloc(sizeof(*proc));
            proc->pid = pid;
            proc->cleanup = cleanup;
            proc->cleanup_data = cleanup_data;
            proc->path = strdup(argp[0]);
            break;

        case -1:
            weston_log("weston_client_launch: "
                       "fork failed while launching '%s': %s\n", argp[0],
                       strerror(errno));
            break;
    }

    custom_env_fini(child_env);
    free(fail_exec);
    return proc;
}

WL_EXPORT struct wl_client *
wet_client_start(struct weston_compositor *compositor, const char *path)
{
    struct wet_process *proc;
    struct wl_client *client;
    struct custom_env child_env;
    struct fdstr wayland_socket = FDSTR_INIT;
    int no_cloexec_fds[1];
    size_t num_no_cloexec_fds = 0;
    char *lib_dir = get_winfusion_lib_dir();

    if (!lib_dir) {
        weston_log("failed to get lib dir\n");
        return NULL;
    }

    if (os_socketpair_cloexec(AF_UNIX, SOCK_STREAM, 0,
                              wayland_socket.fds) < 0) {
        weston_log("wet_client_start: "
                   "socketpair failed while launching '%s': %s\n",
                   path, strerror(errno));
        return NULL;
    }

    custom_env_init_from_environ(&child_env);
    custom_env_add_arg(&child_env, path);
    custom_env_set_env_var(&child_env, "LD_LIBRARY_PATH", lib_dir);
    free(lib_dir);

    fdstr_update_str1(&wayland_socket);
    no_cloexec_fds[num_no_cloexec_fds++] = wayland_socket.fds[1];
    custom_env_set_env_var(&child_env, "WAYLAND_SOCKET",
                           wayland_socket.str1);

    //assert(num_no_cloexec_fds <= ARRAY_LENGTH(no_cloexec_fds));

    proc = wet_client_launch(compositor, &child_env,
                             no_cloexec_fds, num_no_cloexec_fds,
                             NULL, NULL);
    if (!proc)
        return NULL;

    client = wl_client_create(compositor->wl_display,
                              wayland_socket.fds[0]);
    if (!client) {
        weston_log("wet_client_start: "
                   "wl_client_create failed while launching '%s'.\n",
                   path);
        /* We have no way of killing the process, so leave it hanging */
        fdstr_close_all(&wayland_socket);
        return NULL;
    }

    /* Close the child end of our socket which we no longer need */
    close(wayland_socket.fds[1]);

    /* proc is now owned by the compositor's process list */

    return client;
}
