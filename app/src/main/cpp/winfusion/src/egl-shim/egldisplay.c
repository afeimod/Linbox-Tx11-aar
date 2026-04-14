#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <dlfcn.h>

static EGLDisplay (*real_eglGetPlatformDisplay)(EGLenum, void *, const EGLAttrib *) = NULL;

EGLDisplay eglGetPlatformDisplay (EGLenum platform, void *native_display, const EGLAttrib *attrib_list) {
    if (!real_eglGetPlatformDisplay) {
        real_eglGetPlatformDisplay = dlsym(RTLD_NEXT, "eglGetPlatformDisplay");
        if (!real_eglGetPlatformDisplay)
            return EGL_NO_DISPLAY;
    }

    if (platform == EGL_PLATFORM_WAYLAND_KHR)
        return real_eglGetPlatformDisplay(EGL_PLATFORM_ANDROID_KHR, EGL_DEFAULT_DISPLAY, attrib_list);

    return real_eglGetPlatformDisplay(platform, native_display, attrib_list);
}
