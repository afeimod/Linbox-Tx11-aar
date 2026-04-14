package com.winfusion.core.wayland;

public class Constants {

    // wl_touch
    public static final int WL_TOUCH_DOWN = 0;
    public static final int WL_TOUCH_UP = 1;
    public static final int WL_TOUCH_MOTION = 2;
    public static final int WL_TOUCH_FRAME = 3;
    public static final int WL_TOUCH_CANCEL = 4;
    public static final int WL_TOUCH_SHAPE = 5;
    public static final int WL_TOUCH_ORIENTATION = 6;

    // wl_pointer_button_state
    public static final int WL_POINTER_BUTTON_STATE_RELEASED = 0;
    public static final int WL_POINTER_BUTTON_STATE_PRESSED = 1;

    // wl_pointer_axis_scroll
    public static final int WL_POINTER_AXIS_VERTICAL_SCROLL = 0;
    public static final int WL_POINTER_AXIS_HORIZONTAL_SCROLL = 1;

    // wl_keyboard_key_state
    public static final int WL_KEYBOARD_KEY_STATE_RELEASED = 0;
    public static final int WL_KEYBOARD_KEY_STATE_PRESSED = 1;

    // weston_pointer_motion
    public static final int WESTON_POINTER_MOTION_ABS = 1;
    public static final int WESTON_POINTER_MOTION_REL = 1 << 1;
    public static final int WESTON_POINTER_MOTION_REL_UNACCEL = 1 << 2;

    // weston_renderer
    public static final int WESTON_RENDERER_NOOP = 1;
    public static final int WESTON_RENDERER_PIXMAN = 2;
    public static final int WESTON_RENDERER_GL = 3;
}
