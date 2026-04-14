package com.winfusion.feature.input.event;

import androidx.annotation.NonNull;

public class MousePointerEvent extends AbstractInputEvent {

    public enum Type {
        Relative,
        Absolute
    }

    private final Type type;
    private final float x;
    private final float y;

    public MousePointerEvent(@NonNull Type type, float x, float y) {
        this.type = type;
        this.x = x;
        this.y = y;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @NonNull
    @Override
    public String toString() {
        return "[MousePointerEvent] type:" + type.name() + " x:" + x + " y:" + y;
    }
}
