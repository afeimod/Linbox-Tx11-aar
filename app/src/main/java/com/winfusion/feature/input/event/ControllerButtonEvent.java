package com.winfusion.feature.input.event;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.key.StandardButton;

public class ControllerButtonEvent extends AbstractControllerEvent {

    private final KeyState state;
    private final StandardButton button;

    public ControllerButtonEvent(@NonNull KeyState state, @NonNull StandardButton button) {
        this.state = state;
        this.button = button;
    }

    public ControllerButtonEvent(int id, @NonNull KeyState state, @NonNull StandardButton button) {
        super(id);
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
        return "[ControllerButtonEvent] id:" + id + " state:" + state.name() + " button:" + button.name();
    }
}
