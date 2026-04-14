package com.winfusion.feature.input.overlay.widget;

import android.graphics.PointF;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.overlay.bind.Binding;

public abstract class BaseDirWidget<T extends BaseDirWidget.Config> extends BaseWidget<T> {

    public static final int BindingCount = 4;

    public BaseDirWidget(@NonNull WidgetProvider widgetProvider, @NonNull T config) {
        super(widgetProvider, config);
    }

    protected enum Dir {
        UP(DirState.Up),
        RIGHT(DirState.Right),
        DOWN(DirState.Down),
        LEFT(DirState.Left),
        UP_RIGHT((byte) (DirState.Up | DirState.Right)),
        UP_LEFT((byte) (DirState.Up | DirState.Left)),
        DOWN_RIGHT((byte) (DirState.Down | DirState.Right)),
        DOWN_LEFT((byte) (DirState.Down | DirState.Left)),
        NONE((byte) 0);

        public final byte mask;

        Dir(byte mask) {
            this.mask = mask;
        }
    }

    protected static class DirState {

        public static final byte Up = 1;
        public static final byte Down = 1 << 1;
        public static final byte Left = 1 << 2;
        public static final byte Right = 1 << 3;
        public static final byte[] All = {Up, Down, Left, Right};
    }

    protected Dir get4WayDir(@NonNull PointF center, @NonNull PointF p) {
        double angle = calculateAngle(center, p);
        Dir dir;
        if (angle > 315 || angle <= 45)
            dir = Dir.RIGHT;
        else if (angle <= 135)
            dir = Dir.UP;
        else if (angle <= 225)
            dir = Dir.LEFT;
        else // angle <= 315
            dir = Dir.DOWN;
        return dir;
    }

    protected Dir get8WayDir(@NonNull PointF center, @NonNull PointF p) {
        double angle = calculateAngle(center, p);
        Dir dir;
        if (angle > 337.5 || angle <= 22.5f)
            dir = Dir.RIGHT;
        else if (angle <= 67.5f)
            dir = Dir.UP_RIGHT;
        else if (angle <= 112.5f)
            dir = Dir.UP;
        else if (angle <= 157.5f)
            dir = Dir.UP_LEFT;
        else if (angle <= 202.5f)
            dir = Dir.LEFT;
        else if (angle <= 247.5f)
            dir = Dir.DOWN_LEFT;
        else if (angle <= 292.5f)
            dir = Dir.DOWN;
        else // angle <= 337.5
            dir = Dir.DOWN_RIGHT;
        return dir;
    }

    private double calculateAngle(PointF p1, PointF p2) {
        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;
        double angle = Math.toDegrees(Math.atan2(-dy, dx));

        if (angle < 0)
            angle += 360;

        return angle;
    }

    protected int getBindingIndex(byte dirState) {
        return switch (dirState) {
            case DirState.Up -> 0;
            case DirState.Down -> 1;
            case DirState.Left -> 2;
            case DirState.Right -> 3;
            default -> throw new IllegalArgumentException("Unsupported state: " + dirState);
        };
    }

    public static abstract class Config extends BaseWidget.Config {

        public final Binding[] bindings = new Binding[BindingCount];
        public boolean enable8Way = false;

        public Config(float defaultSize) {
            super(defaultSize);
        }
    }
}
