package com.winfusion.feature.input.event;

import androidx.annotation.NonNull;

public class MouseScrollEvent extends AbstractInputEvent {

    public enum Dir {
        Vertical,
        Horizontal
    }

    private final Dir dir;
    private final float value;

    public MouseScrollEvent(@NonNull Dir dir, float value) {
        this.dir = dir;
        this.value = value;
    }

    @NonNull
    public Dir getDir() {
        return dir;
    }

    public float getValue() {
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return "[MouseEvent] dir:" + dir.name() + " value:" + value;
    }
}
