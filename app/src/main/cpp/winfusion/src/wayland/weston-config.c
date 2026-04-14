#include <string.h>
#include <jni.h>
#include <common/log.h>
#include <wayland/weston.h>

#define epsilon 0.00001f
#define GetConfig(code) \
    do { WestonConfig *config = getWestonConfigFromHandle(env, handle); if (config) { code }} while (0)

static inline WestonConfig * getWestonConfigFromHandle(JNIEnv *, jlong);
static void copyStringFromJavaString(JNIEnv *, char *, size_t, jstring);
static void updateEnvVars(WestonConfig *config);

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonConfig_nativeSetRendererType(JNIEnv *env, jobject thiz,
                                                                   jlong handle,
                                                                   jint renderer_type) {

    GetConfig(config->rendererType = renderer_type;);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonConfig_nativeSetRendererRefreshRate(JNIEnv *env, jobject thiz,
                                                                          jlong handle,
                                                                          jint render_refresh_rate) {

    GetConfig(config->rendererRefreshRate = render_refresh_rate;);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonConfig_nativeSetSocketPath(JNIEnv *env, jobject thiz,
                                                                 jlong handle,
                                                                 jstring socket_path) {

    GetConfig(copyStringFromJavaString(env, config->socketPath, sizeof(config->socketPath),
                                       socket_path););
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonConfig_nativeSetXkbConfigRootPath(JNIEnv *env, jobject thiz,
                                                                        jlong handle,
                                                                        jstring xkb_config_root_path) {

    GetConfig(
            copyStringFromJavaString(env, config->xkbConfigRootPath, sizeof(config->xkbConfigRootPath),
                             xkb_config_root_path);
            updateEnvVars(config);
    );
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonConfig_nativeSetXdgRuntimePath(JNIEnv *env, jobject thiz,
                                                                     jlong handle,
                                                                     jstring xdg_runtime_path) {

    GetConfig(
            copyStringFromJavaString(env, config->xdgRuntimePath, sizeof(config->xdgRuntimePath),
                                 xdg_runtime_path);
            updateEnvVars(config);
    );
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonConfig_nativeSetXkbRule(JNIEnv *env, jobject thiz,
                                                              jlong handle, jstring xkb_rule) {

    GetConfig(copyStringFromJavaString(env, config->xkbRule, sizeof(config->xkbRule), xkb_rule););
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonConfig_nativeSetXkbModel(JNIEnv *env, jobject thiz,
                                                               jlong handle, jstring xkb_model) {

    GetConfig(copyStringFromJavaString(env, config->xkbModel, sizeof(config->xkbModel), xkb_model););
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonConfig_nativeSetXkbLayout(JNIEnv *env, jobject thiz,
                                                                jlong handle, jstring xkb_layout) {

    GetConfig(copyStringFromJavaString(env, config->xkbLayout, sizeof(config->xkbLayout),
                                       xkb_layout););
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonConfig_nativeSetScreenWidth(JNIEnv *env, jobject thiz,
                                                                  jlong handle, jint width) {

    GetConfig(config->screenWidth = width;);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonConfig_nativeSetScreenHeight(JNIEnv *env, jobject thiz,
                                                                   jlong handle, jint height) {

    GetConfig(config->screenHeight = height;);
}

static inline WestonConfig * getWestonConfigFromHandle(JNIEnv *env, jlong handle) {
    if (handle == 0) {
        throwJavaException(env, WaylandRuntimeExceptionClassName, "handle must not be null");
        return NULL;
    }

    return &(((WestonHandle*) handle)->westonConfig);
}

static void copyStringFromJavaString(JNIEnv *env, char *dest, size_t len, jstring jString) {
    const char* nString = (*env)->GetStringUTFChars(env, jString, 0);
    size_t nLen = strlen(nString);

    if (nLen + 1 > len)
        goto free;

    strncpy(dest, nString, nLen + 1);

free:
    (*env)->ReleaseStringUTFChars(env, jString, nString);
}

static void updateEnvVars(WestonConfig *config) {
    setenv("XKB_CONFIG_ROOT", config->xkbConfigRootPath, 1);
    setenv("XDG_RUNTIME_DIR", config->xdgRuntimePath, 1);
}
