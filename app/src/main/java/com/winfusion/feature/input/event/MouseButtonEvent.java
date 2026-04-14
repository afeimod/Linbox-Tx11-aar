package com.winfusion.feature.input.event;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.key.StandardButton;

public class MouseButtonEvent extends AbstractInputEvent {

    private final KeyState state;
    private final StandardButton button;

    public MouseButtonEvent(@NonNull KeyState state, @NonNull StandardButton button) {
        this.state = state;
        this.button = button;
    }

    @NonNull
    public KeyState getState() {
        return state;
    }

    @NonNull
    public StandardButton getButton() {
        return button;
    }

    @NonNull
    @Override
    public String toString() {
        return "[MouseButtonEvent] state:" + state.name() + " button:" + button.name();
    }
}
