package com.winfusion.feature.input.overlay.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.interfaces.InputInterface;
import com.winfusion.feature.input.overlay.gesture.Finger;

public abstract class BaseWidget<T extends BaseWidget.Config> {

    public static final float DefaultOpacity = 0.6f;
    protected final WidgetProvider widgetProvider;
    protected final T config;
    protected final Rect bounding;
    protected InputInterface inputInterface;
    private boolean configUpdated;
    private boolean selected = false;
    private PointF lastDragPoint;
    private boolean firstUpdate = true;

    public BaseWidget(@NonNull WidgetProvider widgetProvider,
                      @NonNull T config) {

        this.widgetProvider = widgetProvider;
        this.config = config;
        this.configUpdated = true;
        bounding = new Rect();
    }

    public void onDrawView(@NonNull Canvas canvas) {
        if (configUpdated) {
            onBoundingUpdated();
            onConfigUpdated();
            inputInterface = widgetProvider.getInputInterface();
            if (firstUpdate) {
                firstUpdate = false;
            } else
                widgetProvider.setWidgetChanged();
            configUpdated = false;
        }

        WidgetProvider.Status status = widgetProvider.getStatus();
        if (status == WidgetProvider.Status.Control) {
            if (config.hide)
                return;
            onDraw(canvas);
        } else if (status == WidgetProvider.Status.Edit) {
            onDraw(canvas);
            if (config.hide)
                drawHidingBox(canvas);
            if (selected)
                drawBoundingBox(canvas);
        } else if (status == WidgetProvider.Status.Preview) {
            if (config.hide)
                return;
            onDraw(canvas);
            if (selected)
                drawBoundingBox(canvas);
        }
    }

    public boolean onFingerTouch(@NonNull Finger finger) {
        WidgetProvider.Status status = widgetProvider.getStatus();
        if (status == WidgetProvider.Status.Edit) {
            dragByFinger(finger);
        } else if (status == WidgetProvider.Status.Control || status == WidgetProvider.Status.Preview) {
            if (config.hide)
                return false;
            onFinger(finger);
        }
        return true;
    }

    @NonNull
    public Rect getBounding() {
        return bounding;
    }

    @NonNull
    public T getConfig() {
        return config;
    }

    public void notifyConfigUpdated() {
        this.configUpdated = true;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void drawBoundingBox(@NonNull Canvas canvas) {
        canvas.drawRect(bounding, widgetProvider.getBoundsPaint());
    }

    public void drawHidingBox(@NonNull Canvas canvas) {
        Paint paint = widgetProvider.getBoundsPaint();
        canvas.drawLine(bounding.left, bounding.top, bounding.right, bounding.bottom, paint);
        canvas.drawLine(bounding.left, bounding.bottom, bounding.right, bounding.top, paint);
    }

    protected abstract void onDraw(@NonNull Canvas canvas);

    protected abstract void onFinger(@NonNull Finger finger);

    protected abstract void onConfigUpdated();

    protected void onBoundingUpdated() {
        int sizePx = (int) (config.getNormalizedSize() * widgetProvider.getBaseSize());
        int xPx = (int) (config.normalizedX * widgetProvider.getBaseWidth());
        int yPx = (int) (config.normalizedY * widgetProvider.getBaseHeight());

        // 特殊处理 TouchpadWidget：让它覆盖整个屏幕区域
        if (this instanceof com.winfusion.feature.input.overlay.widget.TouchpadWidget) {
            bounding.left = 0;
            bounding.top = 0;
            bounding.right = widgetProvider.getBaseWidth();
            bounding.bottom = widgetProvider.getBaseHeight();
        } else {
            bounding.left = xPx - sizePx / 2;
            bounding.right = bounding.left + sizePx;
            bounding.top = yPx - sizePx / 2;
            bounding.bottom = bounding.top + sizePx;
        }
    }

    protected int getOpacity() {
        return (int) (0.1f * 255 + config.opacity * 0.9f * 255);
    }

    private void dragByFinger(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Down) {
            lastDragPoint = finger.getPoint();
        } else if (finger.getAction() == Finger.Action.Move) {
            PointF point = finger.getPoint();

            config.normalizedX += (point.x - lastDragPoint.x) / widgetProvider.getBaseWidth();
            config.normalizedY += (point.y - lastDragPoint.y) / widgetProvider.getBaseHeight();

            onBoundingUpdated();
            widgetProvider.setWidgetChanged();
            lastDragPoint = point;
        } else if (finger.getAction() == Finger.Action.Up) {
            lastDragPoint = null;
        }
    }

    public abstract static class Config {

        public float normalizedX = 0f;
        public float normalizedY = 0f;
        public float scale = 1f;
        public float opacity = DefaultOpacity;
        public boolean hide = false;
        private final float defaultSize;

        public Config(float defaultSize) {
            this.defaultSize = defaultSize;
        }

        public void set(@NonNull Config config) {
            normalizedX = config.normalizedX;
            normalizedY = config.normalizedY;
            scale = config.scale;
            opacity = config.opacity;
        }

        protected float getNormalizedSize() {
            return defaultSize * scale;
        }
    }
}
