#ifndef WINFUSION_WESTON_H
#define WINFUSION_WESTON_H

#include <limits.h>
#include <stdbool.h>
#include <pixman.h>
#include <semaphore.h>
#include <libweston/backend-winfusion.h>

#define XKB_STR_MAX 32
#define WaylandExceptionClassName "com/winfusion/core/wayland/exception/WaylandException"
#define WaylandRuntimeExceptionClassName "com/winfusion/core/wayland/exception/WaylandRuntimeException"
#define WestonNotifyPixmanBufferUpdateMethodName "JNINotifyPixmanBufferUpdate"
#define WestonNotifyPixmanBufferUpdateMethodSign "(Ljava/nio/ByteBuffer;II)V"

typedef struct WestonConfig_s {

    int rendererType;
    int rendererRefreshRate;

    char socketPath[PATH_MAX];
    char xkbConfigRootPath[PATH_MAX];
    char xdgRuntimePath[PATH_MAX];

    char xkbRule[XKB_STR_MAX];
    char xkbModel[XKB_STR_MAX];
    char xkbLayout[XKB_STR_MAX];

    int screenWidth;
    int screenHeight;
} WestonConfig;

typedef struct WestonHandle_s {

    struct weston_winfusion_backend_api backendApi;
    WestonConfig westonConfig;

    struct weston_compositor *compositor;
    struct weston_output *output;
    struct weston_log_context *logCtx;
    struct wl_event_source *signals[3];

    bool displayRunning;

    JavaVM *java_vm;
    jobject westonObject;
    jmethodID notifyPixmanBufferUpdateMethodId;

    sem_t pixmanBufferCanWrite;
} WestonHandle;

#endif //WINFUSION_WESTON_H
