package com.winfusion.feature.input.overlay.widget;

import android.graphics.Canvas;
import android.graphics.PointF;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.event.KeyState;
import com.winfusion.feature.input.event.MouseButtonEvent;
import com.winfusion.feature.input.event.MousePointerEvent;
import com.winfusion.feature.input.event.MouseScrollEvent;
import com.winfusion.feature.input.key.StandardButton;
import com.winfusion.feature.input.overlay.gesture.Finger;
import com.winfusion.feature.input.overlay.gesture.FingerHandler;
import com.winfusion.feature.input.overlay.gesture.Gesture;

public class TouchpadWidget extends BaseWidget<TouchpadWidget.TouchpadConfig> {

    private final FingerHandler fingerHandler;
    private final PointF lastPoint;

    public TouchpadWidget(@NonNull WidgetProvider widgetProvider,
                          @NonNull TouchpadConfig widgetConfig) {

        super(widgetProvider, widgetConfig);
        fingerHandler = new FingerHandler(this::onGesture);
        lastPoint = new PointF();
        inputInterface = widgetProvider.getInputInterface();
    }

    @Override
    public void onDrawView(@NonNull Canvas canvas) {

    }

    @Override
    public void drawBoundingBox(@NonNull Canvas canvas) {

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

    }

    @Override
    public boolean onFingerTouch(@NonNull Finger finger) {
        if (widgetProvider.getStatus() == WidgetProvider.Status.Control)
            fingerHandler.handleFinger(finger);
        return true;
    }

    @Override
    protected void onFinger(@NonNull Finger finger) {

    }

    @Override
    protected void onConfigUpdated() {

    }

    @Override
    protected void onBoundingUpdated() {

    }

    private void onGesture(@NonNull Gesture gesture) {
        switch (gesture.getType()) {
            case ONE_FINGER_CLICK -> handleOneFingerClick(gesture);
            case ONE_FINGER_DRAG -> handleOneFingerDrag(gesture);
            case TWO_FINGERS_CLICK -> handleTwoFingerClick(gesture);
            case TWO_FINGERS_DRAG -> handleTwoFingerDrag(gesture);
            case TWO_FINGERS_SCROLL -> handleTwoFingerScroll(gesture);
            default -> throw new IllegalArgumentException("Unsupported gesture: " + gesture);
        }
    }

    private void handleOneFingerClick(@NonNull Gesture gesture) {
        if (gesture.getStage() == Gesture.Stage.START)
            inputInterface.onMouseButtonEvent(new MouseButtonEvent(KeyState.Pressed, StandardButton.BtnLeft));
        else if (gesture.getStage() == Gesture.Stage.FINISH)
            inputInterface.onMouseButtonEvent(new MouseButtonEvent(KeyState.Released, StandardButton.BtnLeft));
    }

    private void handleOneFingerDrag(@NonNull Gesture gesture) {
        if (gesture.getStage() == Gesture.Stage.START)
            lastPoint.set(gesture.getX(), gesture.getY());
        else if (gesture.getStage() == Gesture.Stage.RUNNING || gesture.getStage() == Gesture.Stage.FINISH)
            moveMouseAndUpdatePointCache(gesture);
    }

    private void handleTwoFingerClick(@NonNull Gesture gesture) {
        if (gesture.getStage() == Gesture.Stage.START)
            inputInterface.onMouseButtonEvent(new MouseButtonEvent(KeyState.Pressed, StandardButton.BtnRight));
        else if (gesture.getStage() == Gesture.Stage.FINISH)
            inputInterface.onMouseButtonEvent(new MouseButtonEvent(KeyState.Released, StandardButton.BtnRight));
    }

    private void handleTwoFingerDrag(@NonNull Gesture gesture) {
        if (gesture.getStage() == Gesture.Stage.START) {
            lastPoint.set(gesture.getX(), gesture.getY());
            inputInterface.onMouseButtonEvent(new MouseButtonEvent(KeyState.Pressed, StandardButton.BtnLeft));
        } else if (gesture.getStage() == Gesture.Stage.RUNNING) {
            moveMouseAndUpdatePointCache(gesture);
        } else if (gesture.getStage() == Gesture.Stage.FINISH) {
            moveMouseAndUpdatePointCache(gesture);
            inputInterface.onMouseButtonEvent(new MouseButtonEvent(KeyState.Released, StandardButton.BtnLeft));
        }
    }

    private void handleTwoFingerScroll(@NonNull Gesture gesture) {
        float value = 0;
        MouseScrollEvent.Dir dir = null;

        if (gesture.getDirection() == Gesture.Direction.HORIZONTAL) {
            value = gesture.getX();
            dir = MouseScrollEvent.Dir.Horizontal;
        } else if (gesture.getDirection() == Gesture.Direction.VERTICAL) {
            value = gesture.getY();
            dir = MouseScrollEvent.Dir.Vertical;
        }

        if (value != 0)
            inputInterface.onMouseScrollEvent(new MouseScrollEvent(dir, value));
    }

    private void moveMouseAndUpdatePointCache(@NonNull Gesture gesture) {
        float relX = gesture.getX() - lastPoint.x;
        float relY = gesture.getY() - lastPoint.y;
        if (relX == 0 && relY == 0)
            return;
        
        // 应用灵敏度设置，并放大相对移动的值
        // 原始像素差值可能太小，需要放大才能在屏幕上移动足够距离
        float sensitivity = config.sensitivity;
        relX = relX * sensitivity * 10;  // 放大10倍作为基础增益
        relY = relY * sensitivity * 10;
        
        inputInterface.onMousePointerEvent(new MousePointerEvent(MousePointerEvent.Type.Relative,
                relX, relY));
        lastPoint.set(gesture.getX(), gesture.getY());
    }

    public static class TouchpadConfig extends Config {

        public float sensitivity = 1f;

        public TouchpadConfig() {
            super(1.0f);  // 使用 1.0f 作为默认大小，让触摸板覆盖整个屏幕
        }
    }
}
