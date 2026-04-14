package com.winfusion.feature.input.overlay.gesture;

import android.graphics.PointF;

import androidx.annotation.NonNull;

public class Finger {

    public enum Action {
        Down,
        Move,
        Up
    }

    private final int id;
    private final Action action;
    private final long downTime;
    private final long actionTime;
    private final PointF point;

    public Finger(final int id, @NonNull Action action, long downTime, long actionTime,
                  @NonNull PointF point) {

        this.id = id;
        this.action = action;
        this.downTime = downTime;
        this.actionTime = actionTime;
        this.point = point;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public Action getAction() {
        return action;
    }

    public long getDownTime() {
        return downTime;
    }

    public long getActionTime() {
        return actionTime;
    }

    @NonNull
    public PointF getPoint() {
        return point;
    }
}
