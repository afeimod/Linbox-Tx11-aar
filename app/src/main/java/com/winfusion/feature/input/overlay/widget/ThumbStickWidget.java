package com.winfusion.feature.input.overlay.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.event.MousePointerEvent;
import com.winfusion.feature.input.event.ControllerAxisEvent;
import com.winfusion.feature.input.event.ControllerState;
import com.winfusion.feature.input.interfaces.Mouse;
import com.winfusion.feature.input.overlay.bind.Binding;
import com.winfusion.feature.input.overlay.bind.BindingHandler;
import com.winfusion.feature.input.overlay.gesture.Finger;

public class ThumbStickWidget extends BaseDirWidget<ThumbStickWidget.Config> {

    private static final int MinMouseMoveInterval = 20; // ms
    private static final int MaxMouseMoverInterval = 60; // ms
    private static final int MouseMoveStep = 1; // px
    private static final int BorderWidth = 5; // px
    private final static int DefaultColor = Color.rgb(255, 255, 255);
    private final static float NormalizedDeadZoneSize = 0.05f; // deadZone = deadZoneSize * width
    private final static float NormalizedStickRadiusSize = 0.25f; // stickRadius = stickRadiusSize * width;

    private final Paint paint = new Paint();
    private final PointF center = new PointF();
    private final PointF stickCenter = new PointF();
    private int handledFingerId = -1;
    private float stickRadius;
    private float mainRadius;
    private float deadZone;
    private float effectiveZone;
    private ControllerState controllerState;
    private final Handler mouseTaskHandler = new Handler(Looper.getMainLooper());
    private MouseMoveTask mouseXTask;
    private MouseMoveTask mouseYTask;
    private final BindingHandler[] bindingHandler = new BindingHandler[BindingCount];
    private byte lastState = Dir.NONE.mask;

    public enum Mode {
        LeftThumbStick,
        RightThumbStick,
        Mouse,
        Mapping
    }

    public ThumbStickWidget(@NonNull WidgetProvider widgetProvider,
                            @NonNull Config widgetConfig) {

        super(widgetProvider, widgetConfig);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(center.x, center.y, mainRadius, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(stickCenter.x, stickCenter.y, stickRadius, paint);
    }

    @Override
    protected void onFinger(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Down) {
            if (handledFingerId != -1)
                return;
            handledFingerId = finger.getId();
            stickCenter.set(finger.getPoint());
            performInput(finger);
        } else if (finger.getAction() == Finger.Action.Move) {
            if (handledFingerId != finger.getId())
                return;
            PointF fp = finger.getPoint();
            float x, y;
            float distance = (float) Math.hypot(fp.x - center.x, fp.y - center.y);
            if (distance > mainRadius) {
                float angle = (float) Math.atan2(fp.y - center.y, fp.x - center.x);
                float ox = (float) (Math.cos(angle) * mainRadius);
                float oy = (float) (Math.sin(angle) * mainRadius);
                x = ox + center.x;
                y = oy + center.y;
            } else {
                x = fp.x;
                y = fp.y;
            }
            stickCenter.set(x, y);
            performInput(finger);
        } else if (finger.getAction() == Finger.Action.Up) {
            if (handledFingerId != finger.getId())
                return;
            stickCenter.set(center);
            performInput(finger);
            handledFingerId = -1;
        }
    }

    @Override
    protected void onConfigUpdated() {
        updatePaint();
        if (config.mode == Mode.LeftThumbStick)
            controllerState = ControllerState.LeftThumbStick;
        else if (config.mode == Mode.RightThumbStick)
            controllerState = ControllerState.RightThumbStick;
    }

    @Override
    protected void onBoundingUpdated() {
        super.onBoundingUpdated();
        updateCache();
    }

    private void updateCache() {
        center.set(bounding.centerX(), bounding.centerY());
        stickCenter.set(center);
        float boundingWidth = bounding.width();
        stickRadius = NormalizedStickRadiusSize * boundingWidth;
        mainRadius = (boundingWidth - BorderWidth) / 2f;
        deadZone = NormalizedDeadZoneSize * boundingWidth;
        effectiveZone = (boundingWidth - deadZone) / 2;
    }

    private void performInput(@NonNull Finger finger) {
        switch (config.mode) {
            case LeftThumbStick, RightThumbStick -> performAsController(finger);
            case Mouse -> performAsMouse(finger);
            case Mapping -> performAsMapping(finger);
        }
    }

    private void performAsController(@NonNull Finger finger) {
        float x = 0, y = 0;
        if (finger.getAction() == Finger.Action.Down || finger.getAction() == Finger.Action.Move) {
            x = getNormalizedStickValue(finger.getPoint().x, center.x);
            y = getNormalizedStickValue(finger.getPoint().y, center.y);
        }
        inputInterface.onControllerAxisEvent(new ControllerAxisEvent(controllerState, x, y));
    }

    private void performAsMouse(@NonNull Finger finger) {

        if (mouseXTask == null)
            mouseXTask = new MouseMoveTask(mouseTaskHandler, inputInterface);
        if (mouseYTask == null)
            mouseYTask = new MouseMoveTask(mouseTaskHandler, inputInterface);

        float normalizedStickX = getNormalizedStickValue(finger.getPoint().x, center.x);
        float normalizedStickY = getNormalizedStickValue(finger.getPoint().y, center.y);

        mouseXTask.setX(Math.signum(normalizedStickX) * MouseMoveStep);
        mouseXTask.setInterval((long) (MaxMouseMoverInterval - normalizedStickX *
                (MaxMouseMoverInterval - MinMouseMoveInterval)));
        mouseYTask.setY(Math.signum(normalizedStickY) * MouseMoveStep);
        mouseYTask.setInterval((long) (MaxMouseMoverInterval - normalizedStickY *
                (MaxMouseMoverInterval - MinMouseMoveInterval)));

        if (finger.getAction() == Finger.Action.Down) {
            mouseXTask.start();
            mouseYTask.start();
        } else if (finger.getAction() == Finger.Action.Up) {
            mouseXTask.stop();
            mouseYTask.stop();
        }
    }

    private void performAsMapping(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Down || finger.getAction() == Finger.Action.Move) {
            updateState(config.enable8Way ? get8WayDir(center, finger.getPoint()) :
                    get4WayDir(center, finger.getPoint()));
        } else if (finger.getAction() == Finger.Action.Up) {
            updateState(Dir.NONE);
        }
    }

    private void updateState(@NonNull Dir dir) {
        byte currentState = dir.mask;
        for (byte state : DirState.All) {
            performBindingByStateChange(state, (lastState & state) == state,
                    (currentState & state) == state);
        }
        lastState = currentState;
    }

    private float getNormalizedStickValue(float fingerValue, float centerValue) {
        float offset = fingerValue - centerValue;
        return Math.signum(offset) * Math.clamp((Math.abs(offset) - deadZone) / effectiveZone, 0, 1f);
    }

    private void updatePaint() {
        paint.setStrokeWidth(BorderWidth);
        paint.setColor(DefaultColor);
        paint.setAntiAlias(true);
        paint.setAlpha(getOpacity());
    }

    private void performBindingByStateChange(byte state, boolean last, boolean current) {
        Binding binding = config.bindings[getBindingIndex(state)];
        if (binding == null)
            return;
        if (last && !current)
            getBindingHandlerByState(state).stop();
        else if (!last && current)
            getBindingHandlerByState(state).start(binding);
    }

    @NonNull
    private BindingHandler getBindingHandlerByState(byte state) {
        int index = getBindingIndex(state);
        if (bindingHandler[index] == null)
            bindingHandler[index] = new BindingHandler(inputInterface);
        return bindingHandler[index];
    }

    private static class MouseMoveTask {

        private final Handler handler;
        private long interval;
        private float x;
        private float y;
        private final Runnable runnable;

        public MouseMoveTask(@NonNull Handler handler, @NonNull Mouse mouse) {
            this.handler = handler;
            runnable = new Runnable() {
                @Override
                public void run() {
                    if (x != 0 || y != 0)
                        mouse.onMousePointerEvent(new MousePointerEvent(MousePointerEvent.Type.Relative, x, y));
                    handler.postDelayed(this, interval);
                }
            };
        }

        public void start() {
            handler.postDelayed(runnable, interval);
        }

        public void stop() {
            handler.removeCallbacks(runnable);
        }

        public void setInterval(long internal) {
            this.interval = internal;
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }
    }

    public static class Config extends BaseDirWidget.Config {

        private static final float DefaultSize = 0.3f;

        public Mode mode = Mode.LeftThumbStick;
        public boolean invertX = false; // For thumbStick and mouse mode, TODO: not implemented yet
        public boolean invertY = false; // For thumbStick and mouse mode, TODO: not implemented yet

        public Config() {
            super(DefaultSize);
        }
    }
}
