#include <jni.h>
#include <wayland/weston.h>

static inline struct weston_winfusion_backend_api * getBackendApi(jlong);

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonInput_performTouch(JNIEnv *env, jobject thiz, jlong handle,
                                                         jint touch_id, jint touch_type, jfloat x,
                                                         jfloat y) {

    struct weston_winfusion_backend_api *backendApi = getBackendApi(handle);

    if (!backendApi || !backendApi->input_touch)
        return;

    backendApi->input_touch(backendApi->backend, touch_id, touch_type, x, y);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonInput_performKey(JNIEnv *env, jobject thiz, jlong handle,
                                                       jint key, jint key_state) {

    struct weston_winfusion_backend_api *backendApi = getBackendApi(handle);

    if (!backendApi || !backendApi->input_keyboard)
        return;

    backendApi->input_keyboard(backendApi->backend, key, key_state);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonInput_performPointer(JNIEnv *env, jobject thiz, jlong handle,
                                                           jint pointer_type, jfloat x, jfloat y) {

    struct weston_winfusion_backend_api *backendApi = getBackendApi(handle);

    if (!backendApi || !backendApi->input_pointer)
        return;

    backendApi->input_pointer(backendApi->backend, pointer_type, x, y);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonInput_performButton(JNIEnv *env, jobject thiz, jlong handle,
                                                          jint button, jint button_state) {

    struct weston_winfusion_backend_api *backendApi = getBackendApi(handle);

    if (!backendApi || !backendApi->input_button)
        return;

    backendApi->input_button(backendApi->backend, button, button_state);
}

JNIEXPORT void JNICALL
Java_com_winfusion_core_wayland_WestonInput_performAxis(JNIEnv *env, jobject thiz, jlong handle,
                                                        jint axis_type, jfloat value,
                                                        jboolean has_discrete, jint discrete) {

    struct weston_winfusion_backend_api *backendApi = getBackendApi(handle);

    if (!backendApi || !backendApi->input_axis)
        return;

    backendApi->input_axis(backendApi->backend, axis_type, value, has_discrete, discrete);
}

static inline struct weston_winfusion_backend_api * getBackendApi(jlong handle) {
    if (!handle)
        return NULL;

    return &((WestonHandle *) handle)->backendApi;
}
