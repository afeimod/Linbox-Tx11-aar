/*
 * Copyright © 2011 Kristian Høgsberg
 * Copyright © 2011 Collabora, Ltd.
 * Copyright © 2025 Junyu Long
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice (including the next
 * paragraph) shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

#include "config.h"

#include <stdio.h>
#include <sys/wait.h>
#include <wayland-client.h>

#include "shared/xalloc.h"
#include "shared/cairo-util.h"
#include "shared/file-util.h"
#include "window.h"
#include "weston-desktop-shell-client-protocol.h"

#define BACKGROUND_COLOR 0xFF3A6EA5

struct desktop {
    struct display *display;
    struct weston_desktop_shell *shell;
    struct wl_list outputs;

    struct window *grab_window;
    struct widget *grab_widget;

    enum cursor_type grab_cursor;

    int painted;
};

struct surface {
    void (*configure)(void *data,
                      struct weston_desktop_shell *desktop_shell,
                      uint32_t edges, struct window *window,
                      int32_t width, int32_t height);
};

struct output;

struct background {
    struct surface base;

    struct output *owner;

    struct window *window;
    struct widget *widget;
    int painted;

    uint32_t color;
};

struct output {
    struct wl_output *output;
    uint32_t server_output_id;
    struct wl_list link;

    int x;
    int y;
    struct background *background;
};

static void
sigchild_handler(int s)
{
    int status;
    pid_t pid;

    while (pid = waitpid(-1, &status, WNOHANG), pid > 0)
        fprintf(stderr, "child %d exited\n", pid);
}

static int
is_desktop_painted(struct desktop *desktop)
{
    struct output *output;

    wl_list_for_each(output, &desktop->outputs, link) {
        if (output->background && !output->background->painted)
            return 0;
    }

    return 1;
}

static void
check_desktop_ready(struct window *window)
{
    struct display *display;
    struct desktop *desktop;

    display = window_get_display(window);
    desktop = display_get_user_data(display);

    if (!desktop->painted && is_desktop_painted(desktop)) {
        desktop->painted = 1;

        weston_desktop_shell_desktop_ready(desktop->shell);
    }
}

static void
set_hex_color(cairo_t *cr, uint32_t color)
{
    cairo_set_source_rgba(cr,
                          ((color >> 16) & 0xff) / 255.0,
                          ((color >>  8) & 0xff) / 255.0,
                          ((color >>  0) & 0xff) / 255.0,
                          ((color >> 24) & 0xff) / 255.0);
}

static void
background_draw(struct widget *widget, void *data)
{
    struct background *background = data;
    cairo_surface_t *surface;
    cairo_t *cr;
    struct rectangle allocation;

    surface = window_get_surface(background->window);

    cr = widget_cairo_create(background->widget);
    cairo_set_operator(cr, CAIRO_OPERATOR_SOURCE);
    if (background->color == 0)
        cairo_set_source_rgba(cr, 0.0, 0.0, 0.2, 1.0);
    else
        set_hex_color(cr, background->color);
    cairo_paint(cr);

    widget_get_allocation(widget, &allocation);

    cairo_destroy(cr);
    cairo_surface_destroy(surface);

    background->painted = 1;
    check_desktop_ready(background->window);
}

static void
background_destroy(struct background *background);

static void
background_configure(void *data,
                     struct weston_desktop_shell *desktop_shell,
                     uint32_t edges, struct window *window,
                     int32_t width, int32_t height)
{
    struct output *owner;
    struct background *background =
            (struct background *) window_get_user_data(window);

    if (width < 1 || height < 1) {
        /* Shell plugin configures 0x0 for redundant background. */
        owner = background->owner;
        background_destroy(background);
        owner->background = NULL;
        return;
    }

    if (background->color) {
        widget_set_viewport_destination(background->widget, width, height);
        width = 1;
        height = 1;
    }

    widget_schedule_resize(background->widget, width, height);
}

static void
desktop_shell_configure(void *data,
                        struct weston_desktop_shell *desktop_shell,
                        uint32_t edges,
                        struct wl_surface *surface,
                        int32_t width, int32_t height)
{
    struct window *window = wl_surface_get_user_data(surface);
    struct surface *s = window_get_user_data(window);

    s->configure(data, desktop_shell, edges, window, width, height);
}

static void
desktop_shell_prepare_lock_surface(void *data,
                                   struct weston_desktop_shell *desktop_shell)
{

}

static void
desktop_shell_grab_cursor(void *data,
                          struct weston_desktop_shell *desktop_shell,
                          uint32_t cursor)
{
    struct desktop *desktop = data;

    switch (cursor) {
        case WESTON_DESKTOP_SHELL_CURSOR_NONE:
            desktop->grab_cursor = CURSOR_BLANK;
            break;
        case WESTON_DESKTOP_SHELL_CURSOR_BUSY:
            desktop->grab_cursor = CURSOR_WATCH;
            break;
        case WESTON_DESKTOP_SHELL_CURSOR_MOVE:
            desktop->grab_cursor = CURSOR_DRAGGING;
            break;
        case WESTON_DESKTOP_SHELL_CURSOR_RESIZE_TOP:
            desktop->grab_cursor = CURSOR_TOP;
            break;
        case WESTON_DESKTOP_SHELL_CURSOR_RESIZE_BOTTOM:
            desktop->grab_cursor = CURSOR_BOTTOM;
            break;
        case WESTON_DESKTOP_SHELL_CURSOR_RESIZE_LEFT:
            desktop->grab_cursor = CURSOR_LEFT;
            break;
        case WESTON_DESKTOP_SHELL_CURSOR_RESIZE_RIGHT:
            desktop->grab_cursor = CURSOR_RIGHT;
            break;
        case WESTON_DESKTOP_SHELL_CURSOR_RESIZE_TOP_LEFT:
            desktop->grab_cursor = CURSOR_TOP_LEFT;
            break;
        case WESTON_DESKTOP_SHELL_CURSOR_RESIZE_TOP_RIGHT:
            desktop->grab_cursor = CURSOR_TOP_RIGHT;
            break;
        case WESTON_DESKTOP_SHELL_CURSOR_RESIZE_BOTTOM_LEFT:
            desktop->grab_cursor = CURSOR_BOTTOM_LEFT;
            break;
        case WESTON_DESKTOP_SHELL_CURSOR_RESIZE_BOTTOM_RIGHT:
            desktop->grab_cursor = CURSOR_BOTTOM_RIGHT;
            break;
        case WESTON_DESKTOP_SHELL_CURSOR_ARROW:
        default:
            desktop->grab_cursor = CURSOR_LEFT_PTR;
    }
}

static const struct weston_desktop_shell_listener listener = {
        desktop_shell_configure,
        desktop_shell_prepare_lock_surface,
        desktop_shell_grab_cursor
};

static void
background_destroy(struct background *background)
{
    widget_destroy(background->widget);
    window_destroy(background->window);

    free(background);
}

static struct background *
background_create(struct desktop *desktop, struct output *output)
{
    struct background *background;

    background = xzalloc(sizeof *background);
    background->owner = output;
    background->base.configure = background_configure;
    background->window = window_create_custom(desktop->display);
    background->widget = window_add_widget(background->window, background);

    window_set_user_data(background->window, background);
    widget_set_redraw_handler(background->widget, background_draw);
    widget_set_transparent(background->widget, 0);

    background->color = BACKGROUND_COLOR;

    return background;
}

static int
grab_surface_enter_handler(struct widget *widget, struct input *input,
                           float x, float y, void *data)
{
    struct desktop *desktop = data;

    return desktop->grab_cursor;
}

static void
grab_surface_destroy(struct desktop *desktop)
{
    widget_destroy(desktop->grab_widget);
    window_destroy(desktop->grab_window);
}

static void
grab_surface_create(struct desktop *desktop)
{
    struct wl_surface *s;

    desktop->grab_window = window_create_custom(desktop->display);
    window_set_user_data(desktop->grab_window, desktop);

    s = window_get_wl_surface(desktop->grab_window);
    weston_desktop_shell_set_grab_surface(desktop->shell, s);

    desktop->grab_widget =
            window_add_widget(desktop->grab_window, desktop);
    /* We set the allocation to 1x1 at 0,0 so the fake enter event
     * at 0,0 will go to this widget. */
    widget_set_allocation(desktop->grab_widget, 0, 0, 1, 1);

    widget_set_enter_handler(desktop->grab_widget,
                             grab_surface_enter_handler);
}

static void
output_destroy(struct output *output)
{
    if (output->background)
        background_destroy(output->background);
    wl_output_destroy(output->output);
    wl_list_remove(&output->link);

    free(output);
}

static void
desktop_destroy_outputs(struct desktop *desktop)
{
    struct output *tmp;
    struct output *output;

    wl_list_for_each_safe(output, tmp, &desktop->outputs, link)
    output_destroy(output);
}

static void
output_handle_geometry(void *data,
                       struct wl_output *wl_output,
                       int x, int y,
                       int physical_width,
                       int physical_height,
                       int subpixel,
                       const char *make,
                       const char *model,
                       int transform)
{
    struct output *output = data;

    output->x = x;
    output->y = y;

    if (output->background)
        window_set_buffer_transform(output->background->window, transform);
}

static void
output_handle_mode(void *data,
                   struct wl_output *wl_output,
                   uint32_t flags,
                   int width,
                   int height,
                   int refresh)
{

}

static void
output_handle_done(void *data,
                   struct wl_output *wl_output)
{

}

static void
output_handle_scale(void *data,
                    struct wl_output *wl_output,
                    int32_t scale)
{
    struct output *output = data;

    if (output->background)
        window_set_buffer_scale(output->background->window, scale);
}

static const struct wl_output_listener output_listener = {
        output_handle_geometry,
        output_handle_mode,
        output_handle_done,
        output_handle_scale
};

static void
output_init(struct output *output, struct desktop *desktop)
{
    struct wl_surface *surface;

    output->background = background_create(desktop, output);
    surface = window_get_wl_surface(output->background->window);
    weston_desktop_shell_set_background(desktop->shell,
                                        output->output, surface);
}

static void
create_output(struct desktop *desktop, uint32_t id)
{
    struct output *output;

    output = zalloc(sizeof *output);
    if (!output)
        return;

    output->output =
            display_bind(desktop->display, id, &wl_output_interface, 2);
    output->server_output_id = id;

    wl_output_add_listener(output->output, &output_listener, output);

    wl_list_insert(&desktop->outputs, &output->link);

    /* On start up we may process an output global before the shell global
     * in which case we can't create the panel and background just yet */
    if (desktop->shell)
        output_init(output, desktop);
}

static void
output_remove(struct desktop *desktop, struct output *output)
{
    struct output *cur;
    struct output *rep = NULL;

    if (!output->background) {
        output_destroy(output);
        return;
    }

    /* Find a wl_output that is a clone of the removed wl_output.
     * We don't want to leave the clone without a background or panel. */
    wl_list_for_each(cur, &desktop->outputs, link) {
        if (cur == output)
            continue;

        /* XXX: Assumes size matches. */
        if (cur->x == output->x && cur->y == output->y) {
            rep = cur;
            break;
        }
    }

    if (rep) {
        /* If found and it does not already have a background or panel,
         * hand over the background and panel so they don't get
         * destroyed.
         *
         * We never create multiple backgrounds or panels for clones,
         * but if the compositor moves outputs, a pair of wl_outputs
         * might become "clones". This may happen temporarily when
         * an output is about to be removed and the rest are reflowed.
         * In this case it is correct to let the background/panel be
         * destroyed.
         */

        if (!rep->background) {
            rep->background = output->background;
            output->background = NULL;
            rep->background->owner = rep;
        }
    }

    output_destroy(output);
}

static void
global_handler(struct display *display, uint32_t id,
               const char *interface, uint32_t version, void *data)
{
    struct desktop *desktop = data;

    if (!strcmp(interface, "weston_desktop_shell")) {
        desktop->shell = display_bind(desktop->display,
                                      id,
                                      &weston_desktop_shell_interface,
                                      1);
        weston_desktop_shell_add_listener(desktop->shell,
                                          &listener,
                                          desktop);
    } else if (!strcmp(interface, "wl_output")) {
        create_output(desktop, id);
    }
}

static void
global_handler_remove(struct display *display, uint32_t id,
                      const char *interface, uint32_t version, void *data)
{
    struct desktop *desktop = data;
    struct output *output;

    if (!strcmp(interface, "wl_output")) {
        wl_list_for_each(output, &desktop->outputs, link) {
            if (output->server_output_id == id) {
                output_remove(desktop, output);
                break;
            }
        }
    }
}

int main(int argc, char *argv[])
{
    struct desktop desktop = { 0 };

    wl_list_init(&desktop.outputs);
    desktop.display = display_create(&argc, argv);
    if (desktop.display == NULL) {
        fprintf(stderr, "failed to create display: %s\n",
                strerror(errno));
        return -1;
    }

    display_set_user_data(desktop.display, &desktop);
    display_set_global_handler(desktop.display, global_handler);
    display_set_global_handler_remove(desktop.display, global_handler_remove);
    grab_surface_create(&desktop);
    signal(SIGCHLD, sigchild_handler);

    display_run(desktop.display);

    /* Cleanup */
    grab_surface_destroy(&desktop);
    desktop_destroy_outputs(&desktop);
    weston_desktop_shell_destroy(desktop.shell);
    display_destroy(desktop.display);

    return 0;
}
