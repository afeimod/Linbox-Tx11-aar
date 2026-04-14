package com.winfusion.feature.input.overlay.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.overlay.bind.Binding;
import com.winfusion.feature.input.overlay.bind.BindingHandler;
import com.winfusion.feature.input.overlay.gesture.Finger;

public class ButtonWidget extends BaseWidget<ButtonWidget.Config> {

    public static final int MaxBindingCount = 4;
    private static final int BorderWidth = 5; // px
    private static final int ReleasedBorderColor = Color.rgb(255, 255, 255);
    private static final int ReleasedTextColor = Color.rgb(255, 255, 255);
    private static final int PressedBorderColor = ReleasedBorderColor;
    private static final int PressedTextColor = Color.rgb(0, 0, 0);
    private static final int MaxTextLength = 8;
    private static final float NormalizedSquareRadiusSize = 0.2f; // radius = radiusSize * width
    private static final int MinTextSize = 10;
    private static final int TextMargin = 15;
    private static final float RectAspectRatio = 2f;

    private final Paint borderPaint = new Paint();
    private final Paint textPaint = new Paint();
    private State state = State.Released;
    private int handledFingerId = -1;
    private final PaintCache paintCache = new PaintCache();
    private final BindingHandler[] bindingHandlers = new BindingHandler[MaxBindingCount];

    public enum Shape {
        Circle,
        Square,
        Rect,
        RoundRect
    }

    private enum State {
        Pressed,
        Released
    }

    public ButtonWidget(@NonNull WidgetProvider widgetProvider,
                        @NonNull Config config) {

        super(widgetProvider, config);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        drawBorder(canvas);
        drawText(canvas);
    }

    @Override
    protected void onFinger(@NonNull Finger finger) {
        if (finger.getAction() == Finger.Action.Down) {
            if (handledFingerId != -1)
                return;
            handledFingerId = finger.getId();

            if (state != State.Pressed) {
                state = State.Pressed;
                updateBorderPaint();
                updateTextPaint();
            }

            performBindings(true);
        } else if (finger.getAction() == Finger.Action.Up) {
            if (finger.getId() != handledFingerId)
                return;
            handledFingerId = -1;

            if (state != State.Released) {
                state = State.Released;
                updateBorderPaint();
                updateTextPaint();
            }

            performBindings(false);
        }
    }

    @Override
    protected void onConfigUpdated() {
        updateBorderPaint();
        updateTextPaint();
        updatePaintCache();
    }

    @Override
    protected void onBoundingUpdated() {
        if (config.shape == Shape.Circle || config.shape == Shape.Square)
            super.onBoundingUpdated();
        else
            updateBoundingForRect();

        updatePaintCache();
    }

    private void updateBoundingForRect() {
        int widthPx = (int) ((config.getNormalizedSize() + 0.03f) * widgetProvider.getBaseSize());
        int heightPx = (int) (widthPx / RectAspectRatio);
        int xPx = (int) (config.normalizedX * widgetProvider.getBaseWidth());
        int yPx = (int) (config.normalizedY * widgetProvider.getBaseHeight());

        bounding.left = xPx - widthPx / 2;
        bounding.right = bounding.left + widthPx;
        bounding.top = yPx - heightPx / 2;
        bounding.bottom = bounding.top + heightPx;
    }

    private void drawBorder(@NonNull Canvas canvas) {
        if (config.shape == Shape.Circle) {
            canvas.drawCircle(paintCache.circleCenterX, paintCache.circleCenterY, paintCache.circleRadius,
                    borderPaint);
        } else if (config.shape == Shape.Square || config.shape == Shape.Rect ||
                config.shape == Shape.RoundRect) {
            canvas.drawRoundRect(paintCache.borderRect, paintCache.radiusSize, paintCache.radiusSize,
                    borderPaint);
        }
    }

    private void drawText(@NonNull Canvas canvas) {
        canvas.drawText(paintCache.text, paintCache.textStartX, paintCache.textCenterY, textPaint);
    }

    private void updatePaintCache() {
        // border cache
        if (config.shape == Shape.Circle) {
            paintCache.circleCenterX = bounding.centerX();
            paintCache.circleCenterY = bounding.centerY();
            paintCache.circleRadius = (bounding.width() - BorderWidth) / 2f;
        } else if (config.shape == Shape.Square) {
            updateBorderRectWithMargin();
            paintCache.radiusSize = bounding.width() * NormalizedSquareRadiusSize;
        } else if (config.shape == Shape.Rect) {
            updateBorderRectWithMargin();
            paintCache.radiusSize = 0;
        } else if (config.shape == Shape.RoundRect) {
            updateBorderRectWithMargin();
            paintCache.radiusSize = Integer.MAX_VALUE;
        }

        // text cache
        String text = config.text;
        if (text.isEmpty() && config.bindings[0] != null)
            text = config.bindings[0].getItem().getSymbol();
        if (text.length() > MaxTextLength)
            text = text.substring(0, MaxTextLength);

        float maxWidth = config.getNormalizedSize() * widgetProvider.getBaseSize() - 2 * BorderWidth -
                TextMargin;
        float textSize = maxWidth / 2f;

        textPaint.setTextSize(textSize);
        while (textPaint.measureText(text) > maxWidth && textSize > MinTextSize) {
            textSize -= 2;
            textPaint.setTextSize(textSize);
        }

        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        paintCache.text = text;
        paintCache.textCenterY = bounding.centerY() - (fontMetrics.ascent + fontMetrics.descent) / 2;
        paintCache.textStartX = bounding.centerX() - textPaint.measureText(text) / 2;
    }

    private void updateBorderRectWithMargin() {
        int margin = BorderWidth / 2;
        paintCache.borderRect.set(
                bounding.left + margin, bounding.top + margin, bounding.right - margin,
                bounding.bottom - margin);
    }

    private void performBindings(boolean start) {
        if (start) {
            for (int i = 0; i < MaxBindingCount; i++) {
                Binding binding = config.bindings[i];
                if (binding != null)
                    getBindingHandler(i).start(config.bindings[i]);
            }
        } else {
            for (int i = 0; i < MaxBindingCount; i++) {
                BindingHandler handler = bindingHandlers[i];
                if (handler != null)
                    bindingHandlers[i].stop();
            }
        }
    }

    @NonNull
    private BindingHandler getBindingHandler(int index) {
        if (bindingHandlers[index] == null)
            bindingHandlers[index] = new BindingHandler(inputInterface);
        return bindingHandlers[index];
    }

    private void updateBorderPaint() {
        if (state == State.Pressed) {
            borderPaint.setColor(PressedBorderColor);
            borderPaint.setStyle(Paint.Style.FILL);
        } else if (state == State.Released) {
            borderPaint.setColor(ReleasedBorderColor);
            borderPaint.setStyle(Paint.Style.STROKE);
        }
        borderPaint.setStrokeWidth(BorderWidth);
        borderPaint.setAntiAlias(true);
        borderPaint.setAlpha(getOpacity());
    }

    private void updateTextPaint() {
        if (state == State.Pressed)
            textPaint.setColor(PressedTextColor);
        else if (state == State.Released)
            textPaint.setColor(ReleasedTextColor);
        textPaint.setAntiAlias(true);
        textPaint.setAlpha(getOpacity());
    }

    private static class PaintCache {

        // text cache
        public String text;
        public float textStartX;
        public float textCenterY;

        // border cache
        public float circleCenterX;
        public float circleCenterY;
        public float circleRadius;
        public RectF borderRect = new RectF();
        public float radiusSize;
    }

    public static class Config extends BaseWidget.Config {

        private static final float DefaultSize = 0.12f;

        public Shape shape = Shape.Circle;
        public String text = "";
        public Binding[] bindings = new Binding[MaxBindingCount];
        public boolean toggleSwitch;

        public Config() {
            super(DefaultSize);
        }

        @Override
        protected float getNormalizedSize() {
            return DefaultSize * scale;
        }
    }
}
