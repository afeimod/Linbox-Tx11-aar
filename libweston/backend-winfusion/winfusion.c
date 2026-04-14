/*
 * Copyright © 2010-2011 Benjamin Franzke
 * Copyright © 2012 Intel Corporation
 * Copyright © 2013 Jason Ekstrand
 * Copyright 2022 Collabora, Ltd.
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

#include "config.h"

#include <string.h>
#include <libweston/backend-winfusion.h>
#include <libweston/windowed-output-api.h>
#include <linux-explicit-synchronization.h>
#include <pixel-formats.h>
#include <pixman-renderer.h>
#include <shared/weston-drm-fourcc.h>
#include <shared/timespec-util.h>
#include <renderer-gl/gl-renderer.h>
#include <shared/weston-egl-ext.h>

#define DEFAULT_OUTPUT_REPAINT_REFRESH 60000 // (mHz) = 60Hz
#define MAX_OUTPUT_REPAINT_REFRESH 1000000 // (mHz) = 1000Hz
#define WINFUSION_HEAD_MAKE "weston"
#define WINFUSION_HEAD_MODEL "winfusion"
#define WINFUSION_SEAT_NAME "winfusion"
#define WINFUSION_TOUCH_DEVICE_PATH "winfusion-touch"

struct winfusion_backend {
    struct weston_backend base;
    struct weston_compositor *compositor;

    struct weston_seat winfusion_seat;
    struct weston_touch_device *winfusion_touch_device;

    const struct pixel_format_info **formats;
    unsigned int formats_count;

    int refresh;
};

struct winfusion_head {
    struct weston_head base;
};

struct winfusion_output {
    struct weston_output base;
    struct winfusion_backend *backend;

    struct weston_mode mode;
    struct wl_event_source *finish_frame_timer;
    struct weston_renderbuffer *render_buffer;

    EGLDisplay display;
};

static const uint32_t winfusion_pixel_formats[] = {
        DRM_FORMAT_XBGR8888
};

// backend func
static struct winfusion_backend * winfusion_backend_create(struct weston_compositor *,
        struct weston_winfusion_backend_config *);
static void winfusion_backend_destroy(struct weston_backend *);

// head func
static int winfusion_head_create(struct weston_backend *, const char *);
static void winfusion_head_destroy(struct weston_head *);

// output func
static struct weston_output *
        winfusion_output_create(struct weston_backend *, const char *);
static void winfusion_output_destroy(struct weston_output *);
static int winfusion_output_enable(struct weston_output *);
static int winfusion_output_enable_pixman(struct winfusion_output *);
static int winfusion_output_enable_gl(struct winfusion_output *);
static int winfusion_output_disable(struct weston_output *);
static void winfusion_output_disable_pixman(struct winfusion_output *);
static void winfusion_output_disable_gl(struct winfusion_output *);
static int winfusion_output_set_size(struct weston_output *, int, int);
static int winfusion_output_start_repaint_loop(struct weston_output *);
static int winfusion_output_repaint(struct weston_output *);

// input func
static int winfusion_input_create(struct winfusion_backend *);
static void winfusion_input_destroy(struct winfusion_backend *);

// backend api
void winfusion_input_touch(const struct weston_backend *, int, int, float, float);
void winfusion_input_keyboard(const struct weston_backend *, int, int);
void winfusion_input_pointer(const struct weston_backend *, int, float, float);
void winfusion_input_button(const struct weston_backend *, int, int);
void winfusion_input_axis(const struct weston_backend *, int, float, bool, int);

// common func
static int finish_frame_handler(void *data);
static void config_init_to_defaults(struct weston_winfusion_backend_config *);
static inline struct winfusion_head *
        to_winfusion_head(const struct weston_head *);
static inline struct winfusion_backend *
        to_winfusion_backend(const struct weston_backend *);
static inline struct winfusion_output *
        to_winfusion_output(const struct weston_output *);
static inline struct weston_winfusion_backend_api *
        get_winfusion_backend_api(const struct weston_compositor *);

WL_EXPORT int
weston_backend_init(struct weston_compositor *compositor,
                    struct weston_backend_config *config_base)
{
    struct winfusion_backend *backend;
    struct weston_winfusion_backend_config config = {{ 0, }};

    if (!config_base ||
        config_base->struct_version != WESTON_WINFUSION_BACKEND_CONFIG_VERSION ||
        config_base->struct_size > sizeof(struct weston_winfusion_backend_config)) {

        weston_log("winfusion backend config structure is invalid.\n");
        return -1;
    }

    if (!compositor->user_data) {
        weston_log("winfusion backend needs user_data but got null.\n");
        return -1;
    }

    if (((struct weston_winfusion_backend_api *) compositor->user_data)->magic !=
            WESTON_WINFUSION_BACKEND_API_MAGIC) {
        weston_log("user_data is not winfusion backend api.\n");
        return -1;
    }

    config_init_to_defaults(&config);
    memcpy(&config, config_base, config_base->struct_size);

    if (!(backend = winfusion_backend_create(compositor, &config)))
        return -1;

    if (winfusion_input_create(backend)) {
        weston_log("Failed to create winfusion input.\n");
        wl_list_remove(&backend->base.link);
        free(backend);
        return -1;
    }

    return 0;
}

static struct winfusion_backend *
winfusion_backend_create(struct weston_compositor *compositor,
                         struct weston_winfusion_backend_config *config)
{
    struct winfusion_backend *backend;
    static const struct weston_windowed_output_api winfusion_windowsed_output_api = {
            winfusion_output_set_size,
            winfusion_head_create,
    };
    int ret;

    if (!(backend = zalloc(sizeof(*backend))))
        return NULL;

    backend->compositor = compositor;
    wl_list_insert(&compositor->backend_list, &backend->base.link);

    backend->base.supported_presentation_clocks =
            WESTON_PRESENTATION_CLOCKS_SOFTWARE;
    backend->base.destroy = winfusion_backend_destroy;
    backend->base.create_output = winfusion_output_create;

    // TODO: support decorate here?

    backend->formats_count = ARRAY_LENGTH(winfusion_pixel_formats);
    backend->formats = pixel_format_get_array(
            winfusion_pixel_formats, backend->formats_count);

    if (config->refresh > 0)
        backend->refresh = MIN(config->refresh, MAX_OUTPUT_REPAINT_REFRESH);
    else
        backend->refresh = DEFAULT_OUTPUT_REPAINT_REFRESH;

    if (!compositor->renderer) {
        if (config->renderer == WESTON_RENDERER_GL) {
            const struct gl_renderer_display_options options = {
                    .egl_platform = EGL_PLATFORM_ANDROID_KHR,
                    .egl_native_display = EGL_DEFAULT_DISPLAY,
                    .formats = backend->formats,
                    .formats_count = backend->formats_count
            };

            ret = weston_compositor_init_renderer(
                    compositor,
                    WESTON_RENDERER_GL,
                    &options.base);
        } else if (config->renderer == WESTON_RENDERER_PIXMAN) {
            ret = weston_compositor_init_renderer(
                    compositor, WESTON_RENDERER_PIXMAN, NULL);
        } else if (config->renderer == WESTON_RENDERER_AUTO || config->renderer == WESTON_RENDERER_NOOP) {
            ret = noop_renderer_init(compositor);
        } else {
            weston_log("Error: unsupported renderer: %d.\n", config->renderer);
            ret = -1;
        }

        if (ret < 0)
            goto err_free;

        /* Support zwp_linux_explicit_synchronization_unstable_v1 to enable
         * testing. */
        if (linux_explicit_synchronization_setup(compositor) < 0)
            goto err_free;
    }

    ret = weston_plugin_api_register(
            compositor,
            WESTON_WINDOWED_OUTPUT_API_NAME_WINFUSION,
            &winfusion_windowsed_output_api,
            sizeof(winfusion_windowsed_output_api));

    if (ret < 0) {
        weston_log("Failed to registry output API.\n");
        goto err_free;
    }

    return backend;

err_free:
    wl_list_remove(&backend->base.link);
    free(backend);
    return NULL;
}

static void winfusion_backend_destroy(struct weston_backend *backend_base)
{
    struct winfusion_backend *backend = to_winfusion_backend(backend_base);
    struct weston_compositor *compositor = backend->compositor;
    struct weston_head *head_base, *head_next;

    winfusion_input_destroy(backend);

    wl_list_remove(&backend->base.link);

    wl_list_for_each_safe(head_base, head_next, &compositor->head_list,
                          compositor_link) {
        if (to_winfusion_head(head_base))
            winfusion_head_destroy(head_base);
    }

    free(backend->formats);
    free(backend);
}

static int winfusion_head_create(struct weston_backend *backend_base,
        const char *name)
{
    struct winfusion_backend *backend = to_winfusion_backend(backend_base);
    struct winfusion_head *head;

    if (!name) {
        weston_log("Head name can't be null.\n");
        return -1;
    }

    if (!(head = zalloc(sizeof(*head))))
        return -1;

    weston_head_init(&head->base, name);
    head->base.backend = &backend->base;

    weston_head_set_connection_status(&head->base, true);
    weston_head_set_supported_eotf_mask(
            &head->base,WESTON_EOTF_MODE_SDR);
    weston_head_set_supported_colorimetry_mask(
            &head->base,WESTON_COLORIMETRY_MODE_DEFAULT);

    weston_compositor_add_head(backend->compositor, &head->base);

    return 0;
}

static void winfusion_head_destroy(struct weston_head *head_base)
{
    struct winfusion_head *head = to_winfusion_head(head_base);

    weston_head_release(&head->base);
    free(head);
}

static struct weston_output *
winfusion_output_create(struct weston_backend * backend_base, const char *name)
{
    struct winfusion_backend *backend = to_winfusion_backend(backend_base);
    struct weston_compositor *compositor = backend->compositor;
    struct weston_winfusion_backend_api *api =
            get_winfusion_backend_api(compositor);
    struct winfusion_output *output;

    if (!name) {
        weston_log("Output name con't be null.\n");
        return NULL;
    }

    if (!(output = zalloc(sizeof(*output))))
        return NULL;

    weston_output_init(&output->base, compositor, name);

    output->base.destroy = winfusion_output_destroy;
    output->base.enable = winfusion_output_enable;
    output->base.disable = winfusion_output_disable;
    output->base.attach_head = NULL;
    output->base.repaint_only_on_capture = false;

    output->backend = backend;

    weston_compositor_add_pending_output(&output->base, compositor);

    if (api && api->notify_output_create)
        api->notify_output_create(api->handle);

    return &output->base;
}

static void winfusion_output_destroy(struct weston_output *output_base)
{
    struct winfusion_output *output = to_winfusion_output(output_base);
    struct weston_winfusion_backend_api *api =
            get_winfusion_backend_api(output_base->compositor);

    winfusion_output_disable(&output->base);
    weston_output_release(&output->base);

    free(output);

    if (api && api->notify_output_destroy)
        api->notify_output_destroy(api->handle);
}

static int winfusion_output_enable(struct weston_output *output_base)
{
    struct winfusion_output *output = to_winfusion_output(output_base);
    struct winfusion_backend *backend;
    struct wl_event_loop *loop;
    int ret = 0;

    backend = output->backend;
    loop = wl_display_get_event_loop(backend->compositor->wl_display);
    output->finish_frame_timer = wl_event_loop_add_timer(
            loop,finish_frame_handler, output);

    if (!output->finish_frame_timer) {
        weston_log("Failed to add finish frame timer.\n");
        return -1;
    }

    switch (backend->compositor->renderer->type) {
        case WESTON_RENDERER_GL:
            ret = winfusion_output_enable_gl(output);
            break;
        case WESTON_RENDERER_PIXMAN:
            ret = winfusion_output_enable_pixman(output);
            break;
        case WESTON_RENDERER_NOOP:
            break;
        case WESTON_RENDERER_AUTO:
            unreachable("Cannot have auto renderer at runtime");
    }

    if (ret < 0) {
        wl_event_source_remove(output->finish_frame_timer);
        return -1;
    }

    return 0;
}

static int winfusion_output_enable_pixman(struct winfusion_output *output)
{
    const struct pixman_renderer_interface *pixman;
    const struct pixman_renderer_output_options options = {
            .use_shadow = true,
            .fb_size = {
                    .width = output->base.current_mode->width,
                    .height = output->base.current_mode->height
            },
            .format = pixel_format_get_info(winfusion_pixel_formats[0])
    };

    pixman = output->base.compositor->renderer->pixman;

    if (pixman->output_create(&output->base, &options) < 0)
        return -1;

    output->render_buffer =
            pixman->create_image(&output->base, options.format,
                                 output->base.current_mode->width,
                                 output->base.current_mode->height);
    if (!output->render_buffer)
        goto err_renderer;

    return 0;

err_renderer:
    pixman->output_destroy(&output->base);

    return -1;
}

static int winfusion_output_enable_gl(struct winfusion_output *output)
{
    struct winfusion_backend *backend = output->backend;
    const struct weston_mode *mode = output->base.current_mode;
    const struct weston_renderer *renderer = output->base.compositor->renderer;
    struct weston_winfusion_backend_api *api =
            get_winfusion_backend_api(backend->compositor);

    const struct gl_renderer_output_options options = {
            .window_for_legacy = api->window,
            .window_for_platform = NULL,
            .formats = backend->formats,
            .formats_count = backend->formats_count,
            .area.x = 0,
            .area.y = 0,
            .area.width = mode->width,
            .area.height = mode->height,
            .fb_size.width = mode->width,
            .fb_size.height = mode->height,
    };

    if (renderer->gl->output_window_create(&output->base, &options) < 0) {
        weston_log("Failed to create gl window\n");
        return -1;
    }

    return 0;
}

static int winfusion_output_disable(struct weston_output *output_base)
{
    struct winfusion_output *output = to_winfusion_output(output_base);
    struct winfusion_backend *backend;

    if (!output->base.enabled)
        return 0;

    backend = output->backend;

    wl_event_source_remove(output->finish_frame_timer);

    switch (backend->compositor->renderer->type) {
        case WESTON_RENDERER_GL:
            winfusion_output_disable_gl(output);
            break;
        case WESTON_RENDERER_PIXMAN:
            winfusion_output_disable_pixman(output);
        case WESTON_RENDERER_NOOP:
            break;
        case WESTON_RENDERER_AUTO:
            unreachable("cannot have auto renderer at runtime");
    }

    return 0;
}

static void winfusion_output_disable_pixman(struct winfusion_output *output)
{
    struct weston_renderer *renderer = output->base.compositor->renderer;

    weston_renderbuffer_unref(output->render_buffer);
    output->render_buffer = NULL;
    renderer->pixman->output_destroy(&output->base);
}

static void winfusion_output_disable_gl(struct winfusion_output *output)
{
    struct weston_compositor *compositor = output->base.compositor;
    const struct weston_renderer *renderer = compositor->renderer;

    renderer->gl->output_destroy(&output->base);
}

static int
winfusion_output_set_size(struct weston_output *output_base, int width, int height)
{
    struct winfusion_output *output = to_winfusion_output(output_base);
    struct weston_winfusion_backend_api *api =
            get_winfusion_backend_api(output_base->compositor);
    struct weston_head *head_base;
    int output_width, output_height;

    if (!output)
        return -1;

    wl_list_for_each(head_base, &output->base.head_list, output_link) {
        weston_head_set_monitor_strings(head_base, WINFUSION_HEAD_MAKE,
                                        WINFUSION_HEAD_MODEL, NULL);
        weston_head_set_physical_size(
                head_base, width, height);
    }

    output_width = width * output->base.current_scale;
    output_height = height * output->base.current_scale;

    output->mode.flags = WL_OUTPUT_MODE_CURRENT | WL_OUTPUT_MODE_PREFERRED;
    output->mode.width = output_width;
    output->mode.height = output_height;
    output->mode.refresh = output->backend->refresh;
    wl_list_insert(&output->base.mode_list, &output->mode.link);

    output->base.current_mode = &output->mode;

    output->base.start_repaint_loop = winfusion_output_start_repaint_loop;
    output->base.repaint = winfusion_output_repaint;
    output->base.assign_planes = NULL;
    output->base.set_backlight = NULL;
    output->base.set_dpms = NULL;
    output->base.switch_mode = NULL;

    if (api && api->notify_output_set_size)
        api->notify_output_set_size(api->handle, width, height);

    return 0;
}

static int winfusion_output_start_repaint_loop(struct weston_output *output_base)
{
    struct timespec ts;

    weston_compositor_read_presentation_clock(output_base->compositor, &ts);
    weston_output_finish_frame(output_base, &ts,
                               WP_PRESENTATION_FEEDBACK_INVALID);

    return 0;
}

static int winfusion_output_repaint(struct weston_output *output_base)
{
    struct winfusion_output *output = to_winfusion_output(output_base);
    struct weston_winfusion_backend_api *api =
            get_winfusion_backend_api(output_base->compositor);
    struct weston_compositor *compositor;
    pixman_region32_t damage;
    pixman_image_t *image;
    int delay_msec;

    compositor = output->base.compositor;

    pixman_region32_init(&damage);

    weston_output_flush_damage_for_primary_plane(output_base, &damage);

    delay_msec = (int) millihz_to_nsec(output->mode.refresh) / 1000000;
    wl_event_source_timer_update(output->finish_frame_timer, delay_msec);

    if (compositor->renderer->type == WESTON_RENDERER_PIXMAN) {
        compositor->renderer->repaint_output(
                &output->base, &damage, output->render_buffer);

        pixman_region32_fini(&damage);

        if (api && api->notify_output_repaint_pixman) {
            image = compositor->renderer->pixman->renderbuffer_get_image(
                    output->render_buffer);
            api->notify_output_repaint_pixman(api->handle,image);
        }
    } else if (compositor->renderer->type == WESTON_RENDERER_GL) {
        compositor->renderer->repaint_output(
                &output->base, &damage, NULL);

        pixman_region32_fini(&damage);
    }

    return 0;
}

static int winfusion_input_create(struct winfusion_backend *backend)
{
    struct weston_winfusion_backend_api *api =
            get_winfusion_backend_api(backend->compositor);
    struct xkb_keymap *keymap = NULL;
    struct xkb_rule_names rules = {0};
    struct weston_seat* seat = &backend->winfusion_seat;

    // init seat for input
    weston_seat_init(seat, backend->compositor, WINFUSION_SEAT_NAME);

    // update xkb rules
    if (!api || !api->update_xkb_rules)
        goto error_free;

    if (!api->update_xkb_rules(api->handle, &rules.rules, &rules.model, &rules.layout)) {
        weston_log("Failed to update xkb rules.\n");
        goto error_free;
    }

    // init keyboard
    if (!(keymap = xkb_keymap_new_from_names(
            backend->compositor->xkb_context,
            &rules,
            XKB_KEYMAP_COMPILE_NO_FLAGS))) {
        weston_log("Faild to get keymap.\n");
        goto error_free;
    }

    if (weston_seat_init_keyboard(seat, keymap)) {
        weston_log("Failed to init keyboard for winfusion seat.\n");
        goto error_free;
    }

    xkb_keymap_unref(keymap);

    // init pointer
    if (weston_seat_init_pointer(seat)) {
        weston_log("Failed to init pointer for winfusion seat.\n");
        goto error_free;
    }

    // init touch
    if (weston_seat_init_touch(seat)) {
        weston_log("Failed to init touch for winfusion seat.\n");
        goto error_free;
    }

    if (!(backend->winfusion_touch_device = weston_touch_create_touch_device(
            seat->touch_state, WINFUSION_TOUCH_DEVICE_PATH, NULL, NULL))) {
        weston_log("Failed to create touch device.\n");
        goto error_free;
    }

    // setup backend side api
    api->input_touch = winfusion_input_touch;
    api->input_keyboard = winfusion_input_keyboard;
    api->input_pointer = winfusion_input_pointer;
    api->input_button = winfusion_input_button;
    api->input_axis = winfusion_input_axis;

    return 0;

error_free:
    if (backend->winfusion_touch_device != NULL) {
        weston_touch_device_destroy(backend->winfusion_touch_device);
        backend->winfusion_touch_device = NULL;
    }

    weston_seat_release(&backend->winfusion_seat);

    return -1;
}

static void winfusion_input_destroy(struct winfusion_backend *backend)
{
    struct weston_winfusion_backend_api *api =
            get_winfusion_backend_api(backend->compositor);

    weston_touch_device_destroy(backend->winfusion_touch_device);
    weston_seat_release(&backend->winfusion_seat);

    // detach backend side api
    if (api) {
        api->input_touch = NULL;
        api->input_keyboard = NULL;
        api->input_pointer = NULL;
        api->input_button = NULL;
        api->input_axis = NULL;
    }
}

void winfusion_input_touch(const struct weston_backend *backend_base, int touchId,
        int touchType, float x, float y)
{
    struct winfusion_backend *backend = to_winfusion_backend(backend_base);
    static struct timespec ts;
    static struct weston_coord_global pos;
    static struct weston_coord_global *pos_p;

    pos.c.x = x;
    pos.c.y = y;

    weston_compositor_get_time(&ts);

    if (touchType == WL_TOUCH_UP)
        pos_p = NULL;
    else
        pos_p = &pos;

    notify_touch(backend->winfusion_touch_device, &ts, touchId,pos_p, touchType);
    notify_touch_frame(backend->winfusion_touch_device);
}

void winfusion_input_keyboard(const struct weston_backend *backend_base,
        int keyCode, int keyState)
{
    struct winfusion_backend *backend = to_winfusion_backend(backend_base);
    static struct timespec ts;

    weston_compositor_get_time(&ts);

    notify_key(&backend->winfusion_seat, &ts, keyCode, keyState,
               STATE_UPDATE_AUTOMATIC);
}

void winfusion_input_pointer(const struct weston_backend *backend_base,
        int pointerType, float x, float y)
{
    struct winfusion_backend *backend = to_winfusion_backend(backend_base);
    static struct weston_coord_global pos;
    static struct timespec ts;
    static struct weston_pointer_motion_event motionEvent = {0};

    pos.c.x = x;
    pos.c.y = y;
    weston_compositor_get_time(&ts);

    if (pointerType == WESTON_POINTER_MOTION_ABS) {
        motionEvent.mask = WESTON_POINTER_MOTION_ABS;
        motionEvent.abs = pos;
    } else if (pointerType == WESTON_POINTER_MOTION_REL) {
        motionEvent.mask = WESTON_POINTER_MOTION_REL;
        motionEvent.rel = pos.c;
    }

    notify_motion(&backend->winfusion_seat, &ts, &motionEvent);
    notify_pointer_frame(&backend->winfusion_seat);
}

void winfusion_input_button(const struct weston_backend *backend_base,
        int buttonCode, int buttonState)
{
    struct winfusion_backend *backend = to_winfusion_backend(backend_base);
    static struct timespec ts;

    weston_compositor_get_time(&ts);

    notify_button(&backend->winfusion_seat, &ts, buttonCode, buttonState);
    notify_pointer_frame(&backend->winfusion_seat);
}

void winfusion_input_axis(const struct weston_backend *backend_base, int axisType,
        float value, bool hasDiscrete, int discrete)
{
    struct winfusion_backend *backend = to_winfusion_backend(backend_base);
    static struct weston_pointer_axis_event axisEvent= {0};
    static struct timespec ts;

    weston_compositor_get_time(&ts);

    axisEvent.axis = axisType;
    axisEvent.value = value;
    axisEvent.has_discrete = hasDiscrete;
    axisEvent.discrete = discrete;

    notify_axis(&backend->winfusion_seat, &ts, &axisEvent);
    notify_pointer_frame(&backend->winfusion_seat);
}

static void config_init_to_defaults(struct weston_winfusion_backend_config *config)
{
    config->refresh = DEFAULT_OUTPUT_REPAINT_REFRESH;
}

static int finish_frame_handler(void *data)
{
    struct winfusion_output *output = data;

    weston_output_finish_frame_from_timer(&output->base);

    return 1;
}

static inline struct winfusion_head *
to_winfusion_head(const struct weston_head *head_base)
{
    if (head_base->backend->destroy != winfusion_backend_destroy)
        return NULL;
    return container_of(head_base, struct winfusion_head, base);
}

static inline struct winfusion_backend *
to_winfusion_backend(const struct weston_backend *backend_base)
{
    return container_of(backend_base, struct winfusion_backend, base);
}

static inline struct winfusion_output *
to_winfusion_output(const struct weston_output *output_base)
{
    return container_of(output_base, struct winfusion_output, base);
}

static inline struct weston_winfusion_backend_api *
get_winfusion_backend_api(const struct weston_compositor *compositor_base)
{
    return (struct weston_winfusion_backend_api *) compositor_base->user_data;
}
