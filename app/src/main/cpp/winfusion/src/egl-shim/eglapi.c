#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <dlfcn.h>
#include <egl-shim/egl.h>
#include <wayland-egl.h>

static EGLSurface (*real_eglCreateWindowSurface)(EGLDisplay, EGLConfig, EGLNativeWindowType, const EGLint) = NULL;

EGLSurface eglCreateWindowSurface (EGLDisplay dpy, EGLConfig config, EGLNativeWindowType win, const EGLint *attrib_list) {
    if (!real_eglCreateWindowSurface) {
        real_eglCreateWindowSurface = dlsym(RTLD_NEXT, "eglCreateWindowSurface");
        if (!real_eglCreateWindowSurface)
            return EGL_NO_SURFACE;
    }
}
