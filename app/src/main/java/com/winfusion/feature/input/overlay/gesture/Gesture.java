package com.winfusion.feature.input.overlay.gesture;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

public class Gesture {

    public enum Type {
        ONE_FINGER_CLICK,
        ONE_FINGER_DRAG,
        TWO_FINGERS_CLICK,
        TWO_FINGERS_SCROLL,
        TWO_FINGERS_DRAG
    }

    public enum Stage {
        START,
        RUNNING,
        FINISH
    }

    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        VERTICAL,
        HORIZONTAL,
        NONE
    }

    private final Type type;
    private final Stage stage;
    private final Direction direction;
    private final float x;
    private final float y;

    public Gesture(@NonNull Type type, @NonNull Stage stage, @NonNull Direction direction, float x, float y) {
        this.type = type;
        this.stage = stage;
        this.direction = direction;
        this.x = x;
        this.y = y;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    @NonNull
    public Stage getStage() {
        return stage;
    }

    @NonNull
    public Direction getDirection() {
        return direction;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    @SuppressLint("DefaultLocale")
    @NonNull
    @Override
    public String toString() {
        return String.format("Type:%s, Stage:%s, Dir:%s, x:%f, y:%f", type.name(), stage.name(),
                direction.name(), x, y);
    }
}
