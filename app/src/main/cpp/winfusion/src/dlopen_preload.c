#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <dlfcn.h>
#include <sys/stat.h>
#include <unistd.h>

typedef void *(*dlopen_method)(const char *, int);

static void *dlopen_symlink(const char *filename, int flags, dlopen_method real_dlopen) {
    void *handle = NULL;
    const char *ld_env = getenv("LD_LIBRARY_PATH");
    if (!ld_env)
        return NULL;

    char *copy_env = strdup(ld_env);
    char *token_save;
    char *token = strtok_r(copy_env, ":", &token_save);
    char sym_path[PATH_MAX];
    char real_path[PATH_MAX];
    while (token) {
        char *end = token + strlen(token) - 1;
        if (*end == '/')
            *end = '\0';

        size_t len = strlen(token) + strlen(filename) + 2;
        if (len > PATH_MAX)
            goto next_token;

        if (snprintf(sym_path, len, "%s/%s", token, filename) < 0)
            goto next_token;

        if (!realpath(sym_path, real_path))
            goto next_token;

        if ((handle = real_dlopen(real_path, flags))) {
            fprintf(stdout, "dlopen_symlink original: %s, found at: %s\n", filename, real_path);
            break;
        }

        next_token:
            token = strtok_r(NULL, ":", &token_save);
    }

    free(copy_env);
    return handle;
}

void *dlopen(const char *filename, int flags) {
    static dlopen_method real_dlopen = NULL;

    if (!real_dlopen) {
        real_dlopen = dlsym(RTLD_NEXT, "dlopen");
        if (!real_dlopen) {
            fprintf(stderr, "dlsym failed to find real dlopen\n");
            return NULL;
        }
    }

    void *handle = real_dlopen(filename, flags);
    if (!handle && *filename != '/')
        handle = dlopen_symlink(filename, flags, real_dlopen);

    return handle;
}
