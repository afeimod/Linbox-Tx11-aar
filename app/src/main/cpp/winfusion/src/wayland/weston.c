#include <jni.h>
#include <common/log.h>
#include <wayland/weston.h>
#include <android/native_window_jni.h>
#include <libweston/windowed-output-api.h>
#include <string.h>
#include <unistd.h>

#define TAG "WinfusionWeston"
#define WINFUSION_HEAD_NAME "winfusion"
#define WESTON_DESKTOP_SHELL_LIB "desktop-shell.so"
#define WESTON_FULLSCREEN_SHELL_LIB "fullscreen-shell.so"
#define WESTON_SHELL_ENTRY "wet_shell_init"

#define ThrowWaylandException(...) \
    throwJavaException(env, WaylandExceptionClassName, __VA_ARGS__)

#define ThrowWaylandExceptionWithErrno(...) \
    throwJavaExceptionWithErrno(env, WaylandExceptionClassName, __VA_ARGS__)

#define ThrowWaylandRuntimeException(...) \
    throwJavaException(env, WaylandRuntimeExceptionClassName, __VA_ARGS__)

static inline WestonHandle * getHandle(JNIEnv *, jlong);

static int signal_sigterm_handler(int, void *);
static int signal_sigusr2_handler(int, void *);
static int signal_sigchld_handler(int, void *);

static int weston_log_handler(const char *, va_list);
static void xkb_log_handler(struct xkb_context *, enum xkb_log_level, const char *, va_list);

// winfusion backend api
static bool update_xkb_rules(const void *, const char **, const char **, const char **);
static void notify_output_create(const void *);
static void notify_output_destroy(const void *);
static void notify_output_set_size(const void *, int, int);
static void notify_output_repaint_pixman(const void *, pixman_image_t *);
static void notify_backend_destroy(const void *);

JNIEXPORT jlong JNICALL
Java_com_winfusion_core_wayland_Weston_create(JNIEnv *env, jobject thiz) {
    return (jlong) zalloc(sizeof(WestonHandle));
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_Weston_initialize(JNIEnv *env, jobject thiz, jlong handle) {
    WestonHandle *westonHandle = getHandle(env, handle);
    WestonConfig *westonConfig = NULL;
    struct weston_winfusion_backend_api *backendApi = NULL;
    struct wl_display *display = NULL;
    struct wl_event_loop *loop = NULL;
    struct weston_log_context *logCtx = NULL;
    struct weston_compositor *compositor = NULL;
    struct xkb_rule_names xkbRuleNames = {0};
    struct weston_winfusion_backend_config *backendConfig = NULL;
    struct weston_backend *backend;
    const struct weston_windowed_output_api *api = NULL;
    struct weston_head *head = NULL;
    struct weston_output *output = NULL;

    if (!westonHandle || westonHandle->compositor)
        return;

    westonConfig = &westonHandle->westonConfig;
    backendApi = &westonHandle->backendApi;

    // create wayland display
    if (!(display = wl_display_create())) {
        ThrowWaylandException("Failed to create display.");
        goto error_free;
    }

    // add signal handler to display's event loop
    loop = wl_display_get_event_loop(display);
    westonHandle->signals[0] = wl_event_loop_add_signal(loop, SIGTERM, signal_sigterm_handler,
                                          display);
    westonHandle->signals[1] = wl_event_loop_add_signal(loop, SIGUSR2, signal_sigusr2_handler,
                                          display);
    westonHandle->signals[2] = wl_event_loop_add_signal(loop, SIGCHLD, signal_sigchld_handler,
                                          display);

    // add unix socket to display
    if (wl_display_add_socket(display, westonConfig->socketPath)) {
        ThrowWaylandExceptionWithErrno("Failed to add unix socket: %s.", westonConfig->socketPath);
        goto error_free;
    }

    // create the logger and add handler with android log
    if (!(logCtx = weston_log_ctx_create())) {
        ThrowWaylandException("Failed to create logger.");
        goto error_free;
    }

    // FIXME: log_continue not implemented, fallback to log
    weston_log_set_handler(weston_log_handler, weston_log_handler);

    // create compositor for display
    if (!(compositor = weston_compositor_create(display, logCtx,
                                                &westonHandle->backendApi,NULL))) {
        ThrowWaylandException("Failed to create compositor.");
        goto error_free;
    }

    // set default xkb rule names to compositor
    if (weston_compositor_set_xkb_rule_names(compositor, &xkbRuleNames)) {
        ThrowWaylandException("Failed to set default xkb rule names to compositor.");
        goto error_free;
    }

    xkb_context_set_log_fn(compositor->xkb_context, xkb_log_handler);

    // create winfusion backend config and config it by WestonConfig
    if (!(backendConfig = zalloc(sizeof(*backendConfig)))) {
        ThrowWaylandExceptionWithErrno("Failed to create winfusion backend config.");
        goto error_free;
    }

    backendConfig->base.struct_version = WESTON_WINFUSION_BACKEND_CONFIG_VERSION;
    backendConfig->base.struct_size = sizeof(*backendConfig);
    backendConfig->refresh = westonConfig->rendererRefreshRate * 1000;

    if (westonConfig->rendererType != WESTON_RENDERER_PIXMAN &&
                westonConfig->rendererType != WESTON_RENDERER_GL &&
                westonConfig->rendererType != WESTON_RENDERER_NOOP) {

        ThrowWaylandException("Unsupported rendererType: %d.", westonConfig->rendererType);
        goto error_free;
    }

    backendConfig->renderer = westonConfig->rendererType;

    // setup winfusion side backend api
    backendApi->magic = WESTON_WINFUSION_BACKEND_API_MAGIC;
    backendApi->update_xkb_rules = update_xkb_rules;
    backendApi->notify_output_create = notify_output_create;
    backendApi->notify_output_destroy = notify_output_destroy;
    backendApi->notify_output_set_size = notify_output_set_size;
    backendApi->notify_output_repaint_pixman = notify_output_repaint_pixman;
    backendApi->notify_backend_destroy = notify_backend_destroy;
    backendApi->handle = westonHandle;

    // load winfusion backend
    if (!(backend = weston_compositor_load_backend(compositor, WESTON_BACKEND_WINFUSION,
                        (struct weston_backend_config *) backendConfig))) {

        ThrowWaylandException("Failed to load winfusion backend.");
        goto error_free;
    }

    // call backends loaded after loading winfusion backend
    if (weston_compositor_backends_loaded(compositor)) {
        ThrowWaylandException("Failed to call backends loaded.");
        goto error_free;
    }

    // create head for output and check the result
    if (!(api = weston_windowed_output_get_api(compositor, WESTON_WINDOWED_OUTPUT_WINFUSION))) {
        ThrowWaylandException("Failed to get weston windowsed output api.");
        goto error_free;
    }

    if (api->create_head(backend, WINFUSION_HEAD_NAME)) {
        ThrowWaylandException("Failed to create head for output.");
        goto error_free;
    }

    if (!(head = weston_compositor_iterate_heads(compositor, head))) {
        ThrowWaylandException("Failed to add head for output.");
        goto error_free;
    }

    if (head->compositor_link.next != &(compositor->head_list)) {
        ThrowWaylandException("There should be only one head, but got more than it.");
        goto error_free;
    }

    // create output for backend
    if (!(output = weston_compositor_create_output(compositor, head, head->name))) {
        ThrowWaylandException("Failed to create output.");
        goto error_free;
    }

    // config output
    output->pos.c = weston_coord(0, 0);
    weston_output_set_scale(output, 1);
    weston_output_set_transform(output, WL_OUTPUT_TRANSFORM_NORMAL);
    api->output_set_size(output, westonConfig->screenWidth, westonConfig->screenHeight);

    // load desktop shell
    int (*shell_init)(struct weston_compositor*, int*, char**);
    if (!(shell_init = weston_load_module(WESTON_DESKTOP_SHELL_LIB, WESTON_SHELL_ENTRY,
                                          NULL))) {

        ThrowWaylandException("Failed to load shell: %s.", WESTON_DESKTOP_SHELL_LIB);
        goto error_free;
    }

    if (shell_init(compositor, NULL, NULL)) {
        ThrowWaylandException("Failed to init shell.");
        goto error_free;
    }

    // set wayland server log handler
    wl_log_set_handler_server((wl_log_func_t) weston_log_handler);

    if ((*env)->GetJavaVM(env, &(westonHandle->java_vm))) {
        ThrowWaylandException("Failed to get java vm.");
        goto error_free;
    }

    jclass clazz = (*env)->GetObjectClass(env, thiz);
    if (!(westonHandle->notifyPixmanBufferUpdateMethodId = (*env)->GetMethodID(env, clazz,
            WestonNotifyPixmanBufferUpdateMethodName,WestonNotifyPixmanBufferUpdateMethodSign))) {
        ThrowWaylandException("Failed to get method: %s", WestonNotifyPixmanBufferUpdateMethodName);
        goto error_free;
    }

    westonHandle->westonObject = (*env)->NewGlobalRef(env, thiz);
    westonHandle->compositor = compositor;
    westonHandle->output = output;
    westonHandle->logCtx = logCtx;
    westonHandle->backendApi.backend = backend;
    sem_init(&westonHandle->pixmanBufferCanWrite, 0, 1);

    return;

error_free:
    if (logCtx)
        weston_log_ctx_destroy(logCtx);

    if (compositor)
        weston_compositor_destroy(compositor);

    for (int i = 0; i < 3; i++) {
        if (westonHandle->signals[i])
            wl_event_source_remove(westonHandle->signals[i]);
    }

    if (display)
        wl_display_destroy(display);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_Weston_destroy(JNIEnv *env, jobject thiz, jlong handle) {
    WestonHandle *westonHandle = getHandle(env, handle);
    struct weston_compositor *compositor = NULL;
    struct wl_display *display = NULL;

    if (!westonHandle || !(compositor = westonHandle->compositor))
        return;

    display = compositor->wl_display;

    if (westonHandle->displayRunning) {
        sem_post(&westonHandle->pixmanBufferCanWrite);
        wl_display_terminate(display);
    }

    // set shutting_down to pass desktop's early crash checking
    compositor->shutting_down = true;
    wl_display_destroy_clients(display);

    weston_compositor_destroy(compositor);

    for (int i = 0; i < 3; i++) {
        if (westonHandle->signals[i])
            wl_event_source_remove(westonHandle->signals[i]);
    }

    wl_display_destroy(display);

    if (westonHandle->logCtx)
        weston_log_ctx_destroy(westonHandle->logCtx);

    if (westonHandle->westonObject)
        (*env)->DeleteGlobalRef(env, westonHandle->westonObject);

    sem_destroy(&westonHandle->pixmanBufferCanWrite);

    free(westonHandle);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_Weston_startDisplay(JNIEnv *env, jobject thiz, jlong handle) {
    WestonHandle *westonHandle = getHandle(env, handle);

    if (!westonHandle || !westonHandle->compositor || westonHandle->displayRunning)
        return;

    westonHandle->displayRunning = true;

    wl_display_run(westonHandle->compositor->wl_display);

    LOGD(TAG, "Weston display terminated.");

    westonHandle->displayRunning = false;
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_Weston_stopDisplay(JNIEnv *env, jobject thiz, jlong handle) {
    WestonHandle *westonHandle = getHandle(env, handle);

    if (!westonHandle || !westonHandle->compositor || !westonHandle->displayRunning)
        return;

    wl_display_terminate(westonHandle->compositor->wl_display);

    westonHandle->displayRunning = false;
}

JNIEXPORT jboolean JNICALL
Java_com_winfusion_core_wayland_Weston_isDisplayRunning(JNIEnv *env, jobject thiz, jlong handle) {
    WestonHandle *westonHandle = getHandle(env, handle);

    return (!handle) ? JNI_FALSE : westonHandle->displayRunning;
}

static inline WestonHandle * getHandle(JNIEnv *env, jlong handle) {
    if (!handle) {
        ThrowWaylandRuntimeException("Handle can't be null.");
        return NULL;
    }

    return (WestonHandle *) handle;
}

static int signal_sigterm_handler(int signal, void *data) {
    if (data) {
        wl_display_terminate(data);
        return 1;
    }
    return 0;
}

static int signal_sigusr2_handler(int signal, void *data) {
    if (data) {
        wl_display_terminate(data);
        return 1;
    }
    return 0;
}

static int signal_sigchld_handler(int signal, void *data) {
    return -1;
}

static int weston_log_handler(const char *fmt, va_list ap) {
    return LOGD_AP(TAG, fmt, ap);
}

static void xkb_log_handler(struct xkb_context* ctx, enum xkb_log_level level, const char *fmt, va_list ap) {
    LOGD_AP(TAG, fmt, ap);
}

static bool update_xkb_rules(const void *handle, const char **xkbRule, const char **xkbModel,
                             const char **xkbLayout) {

    WestonHandle *westonHandle = (WestonHandle *) handle;

    if (!westonHandle)
        return false;

    *xkbRule = westonHandle->westonConfig.xkbRule;
    *xkbModel = westonHandle->westonConfig.xkbModel;
    *xkbLayout = westonHandle->westonConfig.xkbLayout;

    return true;
}

static void notify_output_create(const void *handle) {
    LOGD(TAG, "notify_output_create was called.");
}

static void notify_output_destroy(const void *handle) {
    LOGD(TAG, "notify_output_destroy was called.");
}

static void notify_output_set_size(const void *handle, int width, int height) {
    LOGD(TAG, "notify_output_set_size was called.");
}

static void notify_output_repaint_pixman(const void *handle, pixman_image_t *srcImg) {
    WestonHandle *westonHandle = (WestonHandle *) handle;

    if (!westonHandle || !srcImg)
        return;

    JNIEnv *env;
    if ((*westonHandle->java_vm)->AttachCurrentThread(westonHandle->java_vm, &env, NULL)) {
        LOGE(TAG, "Failed to attach to current thread");
        return;
    }

    uint32_t *data = pixman_image_get_data(srcImg);
    int height = pixman_image_get_height(srcImg);
    int width = pixman_image_get_width(srcImg);
    size_t size = pixman_image_get_stride(srcImg) * height;
    jobject buf = (*env)->NewDirectByteBuffer(env, data, (jlong) size);

    (*env)->CallVoidMethod(env, westonHandle->westonObject, westonHandle->notifyPixmanBufferUpdateMethodId,
                           buf, width, height);

    sem_wait(&westonHandle->pixmanBufferCanWrite);
}

static void notify_backend_destroy(const void *handle) {
    LOGD(TAG, "notify_backend_destroy was called.");
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_Weston_enableOutput(JNIEnv *env, jobject thiz, jlong handle,
                                                    jobject surface) {

    WestonHandle *westonHandle = getHandle(env, handle);
    ANativeWindow *window = NULL;

    if (!westonHandle)
        return;

    if (!surface) {
        westonHandle->backendApi.window = NULL;
        return;
    }

    if (!(window = ANativeWindow_fromSurface(env, surface))) {
        ThrowWaylandRuntimeException("Failed to get native window from surface.");
        return;
    }

    westonHandle->backendApi.window = window;

    if (!westonHandle->output) {
        ThrowWaylandException("Weston not initialized yet.");
        return;
    }

    if (westonHandle->output->enabled)
        return;

    if (weston_output_enable(westonHandle->output)) {
        ThrowWaylandException("Failed to enable output.");
        return;
    }
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_Weston_disableOutput(JNIEnv *env, jobject thiz, jlong handle) {
    WestonHandle *westonHandle = getHandle(env, handle);

    if (!westonHandle || !westonHandle->output)
        return;

    weston_output_disable(westonHandle->output);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_Weston_onPixmanRenderFinished(JNIEnv *env, jobject thiz, jlong handle) {
    WestonHandle *westonHandle = getHandle(env, handle);

    if (!westonHandle)
        return;

    sem_post(&westonHandle->pixmanBufferCanWrite);
}
