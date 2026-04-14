package com.winfusion.feature.input.event;

import androidx.annotation.NonNull;

public class ControllerAxisEvent extends AbstractControllerEvent {

    private final ControllerState state;
    private final float x;
    private final float y;
    private final float value;

    public ControllerAxisEvent(@NonNull ControllerState state, float x, float y) {
        this.state = state;
        this.x = x;
        this.y = y;
        value = 0;
    }

    public ControllerAxisEvent(int id, @NonNull ControllerState state, float x, float y) {
        super(id);
        this.state = state;
        this.x = x;
        this.y = y;
        value = 0;
    }

    public ControllerAxisEvent(@NonNull ControllerState state, float value) {
        this.state = state;
        this.value = value;
        x = 0;
        y = 0;
    }

    public ControllerAxisEvent(int id, @NonNull ControllerState state, float value) {
        super(id);
        this.state = state;
        this.value = value;
        x = 0;
        y = 0;
    }

    @NonNull
    public ControllerState getState() {
        return state;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getValue() {
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return "[ControllerAxisEvent] id:" + id + " state:" + state.name() + " x:" + x + " y:" + y +
                " value:" + value;
    }
}
