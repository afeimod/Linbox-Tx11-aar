package com.winfusion.feature.input.key;

import androidx.annotation.NonNull;

public class WaylandButtonKeymap implements Keymap<StandardButton> {

    @NonNull
    @Override
    public StandardButton toKey(int code) {
        return switch (code) {
            case EvdevKeycode.BTN_LEFT -> StandardButton.BtnLeft;
            case EvdevKeycode.BTN_RIGHT -> StandardButton.BtnRight;
            case EvdevKeycode.BTN_MIDDLE -> StandardButton.BtnMiddle;
            default -> StandardButton.None;
        };
    }

    @Override
    public int toCode(@NonNull StandardButton key) {
        return switch (key) {
            case BtnLeft -> EvdevKeycode.BTN_LEFT;
            case BtnRight -> EvdevKeycode.BTN_RIGHT;
            case BtnMiddle -> EvdevKeycode.BTN_MIDDLE;
            default -> EvdevKeycode.KEY_RESERVED;
        };
    }
}
