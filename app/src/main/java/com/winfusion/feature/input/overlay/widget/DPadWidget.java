package com.winfusion.feature.input.overlay.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.overlay.bind.Binding;
import com.winfusion.feature.input.overlay.bind.BindingHandler;
import com.winfusion.feature.input.overlay.gesture.Finger;

public class DPadWidget extends BaseDirWidget<DPadWidget.Config> {

    public static final int BindingCount = 4;
    private static final int BorderWidth = 5; // px
    private final static int DefaultColor = Color.rgb(255, 255, 255);
    private final static float NormalizedMarginSize = 0.05f; // margin = marginSize * width
    private final static float NormalizedDeadZoneSize = 0.2f; // deadZone = deadZoneSize * width
    private final static float NormalizedOffset = 0.135f;

    private final Path path = new Path();
    private final Paint paint = new Paint();
    private int handledFingerId = -1;
    private final PointF center = new PointF();
    private byte lastState = Dir.NONE.mask;
    private final BindingHandler[] bindingHandlers = new BindingHandler[BindingCount];
    private float margin;
    private float deadZone;
    private float offset;

    public DPadWidget(@NonNull WidgetProvider widgetProvider, @NonNull Config widgetConfig) {
        super(widgetProvider, widgetConfig);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        float cx, cy;

        // up
        cx = center.x;
        cy = center.y - margin;
        path.reset();
        path.moveTo(cx, cy);
        path.lineTo(cx - offset, cy - offset);
        path.lineTo(cx - offset, bounding.top + BorderWidth);
        path.lineTo(cx + offset, bounding.top + BorderWidth);
        path.lineTo(cx + offset, cy - offset);
        path.close();
        updatePaintStyle(DirState.Up);
        canvas.drawPath(path, paint);

        // down
        cx = center.x;
        cy = center.y + margin;
        path.reset();
        path.moveTo(cx, cy);
        path.lineTo(cx - offset, cy + offset);
        path.lineTo(cx - offset, bounding.bottom - BorderWidth);
        path.lineTo(cx + offset, bounding.bottom - BorderWidth);
        path.lineTo(cx + offset, cy + offset);
        path.close();
        updatePaintStyle(DirState.Down);
        canvas.drawPath(path, paint);

        // left
        cx = center.x - margin;
        cy = center.y;
        path.reset();
        path.moveTo(cx, cy);
        path.lineTo(cx - offset, cy + offset);
        path.lineTo(bounding.left + BorderWidth, cy + offset);
        path.lineTo(bounding.left + BorderWidth, cy - offset);
        path.lineTo(cx - offset, cy - offset);
        path.close();
        updatePaintStyle(DirState.Left);
        canvas.drawPath(path, paint);

        // right
        cx = center.x + margin;
        cy = center.y;
        path.reset();
        path.moveTo(cx, cy);
        path.lineTo(cx + offset, cy + offset);
        path.lineTo(bounding.right - BorderWidth, cy + offset);
        path.lineTo(bounding.right - BorderWidth, cy - offset);
        path.lineTo(cx + offset, cy - offset);
        path.close();
        updatePaintStyle(DirState.Right);
        canvas.drawPath(path, paint);
    }

    @Override
    protected void onFinger(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Down) {
            if (handledFingerId != -1 || isDeadZone(finger.getPoint()))
                return;
            handledFingerId = finger.getId();
            updateState(getDir(finger));
        } else if (finger.getAction() == Finger.Action.Move) {
            if (handledFingerId != finger.getId() || isDeadZone(finger.getPoint()))
                return;
            updateState(getDir(finger));
        } else if (finger.getAction() == Finger.Action.Up) {
            if (handledFingerId != finger.getId())
                return;
            updateState(Dir.NONE);
            handledFingerId = -1;
        }
    }

    @Override
    protected void onConfigUpdated() {
        updatePaint();
    }

    @Override
    protected void onBoundingUpdated() {
        super.onBoundingUpdated();
        updateCache();
    }

    private void updateCache() {
        center.set(bounding.centerX(), bounding.centerY());
        float width = bounding.width();
        margin = NormalizedMarginSize * width;
        deadZone = NormalizedDeadZoneSize * width;
        offset = NormalizedOffset * width;
    }

    private boolean isDeadZone(@NonNull PointF point) {
        return Math.abs(point.x - center.x) <= deadZone && Math.abs(point.y - center.y) <= deadZone;
    }

    private void updatePaint() {
        paint.setStrokeWidth(BorderWidth);
        paint.setAntiAlias(true);
        paint.setColor(DefaultColor);
        paint.setAlpha(getOpacity());
    }

    private void updatePaintStyle(byte dpadState) {
        paint.setStyle((lastState & dpadState) == dpadState ? Paint.Style.FILL_AND_STROKE :
                Paint.Style.STROKE);
    }

    private void updateState(@NonNull Dir dir) {
        byte currentState = dir.mask;
        for (byte state : DirState.All) {
            performBindingByStateChange(state, (lastState & state) == state,
                    (currentState & state) == state);
        }
        lastState = currentState;
    }

    private void performBindingByStateChange(byte dpadState, boolean last, boolean current) {
        Binding binding = config.bindings[getBindingIndex(dpadState)];
        if (binding == null)
            return;
        if (last && !current)
            getBindingHandlerByState(dpadState).stop();
        else if (!last && current)
            getBindingHandlerByState(dpadState).start(binding);
    }

    private Dir getDir(@NonNull Finger finger) {
        return config.enable8Way ? get8WayDir(center, finger.getPoint()) :
                get4WayDir(center, finger.getPoint());
    }

    @NonNull
    private BindingHandler getBindingHandlerByState(byte dpadState) {
        int index = getBindingIndex(dpadState);
        if (bindingHandlers[index] == null)
            bindingHandlers[index] = new BindingHandler(inputInterface);
        return bindingHandlers[index];
    }

    public static class Config extends BaseDirWidget.Config {
        private static final float DefaultSize = 0.3f;

        public Config() {
            super(DefaultSize);
        }
    }
}
