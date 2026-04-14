package com.winfusion.feature.input.overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.feature.input.overlay.gesture.Finger;
import com.winfusion.feature.input.overlay.widget.BaseWidget;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * 覆盖层视图类。
 */
public class OverlayView extends View {

    private final OverlayController controller;
    private final HashMap<Integer, WeakReference<BaseWidget<?>>> fingerHandleMap = new HashMap<>(); // <pointerId, widget>

    public OverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        controller = new OverlayController(this);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        Collection<BaseWidget<?>> widgets = controller.getWidgets();
        for (BaseWidget<?> widget : widgets)
            widget.onDrawView(canvas);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        List<Finger> fingers = generateFingersFromEvent(event);
        Collection<BaseWidget<?>> widgets = controller.getWidgets();

        for (Finger finger : fingers) {
            boolean fingerConsumed = false;

            if (finger.getAction() == Finger.Action.Down) {
                for (BaseWidget<?> widget : widgets) {
                    if (widget.getBounding().contains((int) finger.getPoint().x, (int) finger.getPoint().y)) {
                        if (!widget.onFingerTouch(finger))
                            continue;
                        fingerHandleMap.put(finger.getId(), new WeakReference<>(widget));
                        fingerConsumed = true;
                        controller.setSelectedWidget(widget);
                        break;
                    }
                }
                if (!fingerConsumed) {
                    controller.setSelectedWidget(null);
                    invalidate();
                }
            } else {
                WeakReference<BaseWidget<?>> widgetReference = fingerHandleMap.get(finger.getId());
                if (widgetReference != null) {
                    BaseWidget<?> widget = widgetReference.get();
                    if (widget != null) {
                        widget.onFingerTouch(finger);
                        fingerConsumed = true;
                    }
                    if (finger.getAction() == Finger.Action.Up)
                        fingerHandleMap.remove(finger.getId());
                }
            }

            if (fingerConsumed)
                invalidate();
            else
                controller.getTouchpad().onFingerTouch(finger);
        }

        return true;
    }

    @NonNull
    public OverlayController getController() {
        return controller;
    }

    @NonNull
    private List<Finger> generateFingersFromEvent(@NonNull MotionEvent event) {
        ArrayList<Finger> fingers = new ArrayList<>();
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            fingers.add(buildFinger(Finger.Action.Down, event, event.getActionIndex()));
        } else if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_POINTER_UP) {
            fingers.add(buildFinger(Finger.Action.Up, event, event.getActionIndex()));
        } else if (action == MotionEvent.ACTION_MOVE) {
            int pointerCount = event.getPointerCount();
            for (int i = 0; i < pointerCount; i++)
                fingers.add(buildFinger(Finger.Action.Move, event, i));
        }

        return fingers;
    }

    @NonNull
    private Finger buildFinger(@NonNull Finger.Action action, @NonNull MotionEvent event, int actionIndex) {
        return new Finger(
                event.getPointerId(actionIndex),
                action,
                event.getDownTime(),
                event.getEventTime(),
                new PointF(event.getX(actionIndex), event.getY(actionIndex))
        );
    }
}
