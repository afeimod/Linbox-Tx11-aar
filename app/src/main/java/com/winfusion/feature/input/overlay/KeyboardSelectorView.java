package com.winfusion.feature.input.overlay;

import static com.winfusion.feature.input.key.StandardKey.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.winfusion.feature.input.key.StandardKey;

import java.util.LinkedHashMap;
import java.util.Map;

public class KeyboardSelectorView extends View {

    private static final float AspectRatio = 23 / 7f;
    private static final float KeySizeRatio = 1 / 23f; // keySize : width
    private static final float KeyRegionMarginRatio = KeySizeRatio * 0.25f;
    private static final float KeyElevationRatio = KeySizeRatio * 0.33f; // for custom
    private static final float KeyRadiusRatio = KeySizeRatio * 0.11f; // for custom
    private static final float EyeDistance = 0.3f; // for custom
    private static final int DefaultWidth = 960; // px
    private static final int DefaultColorSurface = Color.parseColor("#5C6870"); // for custom
    private static final int DefaultColorOnSurface = Color.WHITE; // for custom
    private static final int DefaultPressedColorSurface = Color.parseColor("#3C4850"); // for custom
    private static final int DefaultSelectedColorSurface = Color.parseColor("#5567b4"); // for custom

    private final LinkedHashMap<StandardKey, KeyItem> items = new LinkedHashMap<>();
    private final Paint backgroundPaint = new Paint();
    private final Paint surfacePaint = new Paint();
    private final Paint sideSurfacePaint = new Paint();
    private final Paint textPaint = new Paint();
    private final ColorGroup colors = new ColorGroup();
    private final ColorGroup colorsWhenPressed = new ColorGroup();
    private final ColorGroup colorsWhenSelected = new ColorGroup();
    private float normalKeySize;
    private float normalMarginSize;
    private float normalElevationSize;
    private float normalRadius;
    private float textSizeLarge;
    private KeyItem selectedKeyItem;
    private KeyItem pressedKeyItem;
    private float textSizeMedium;
    private float textLargetOffset;
    private float textMediumOffset;

    public KeyboardSelectorView(Context context) {
        super(context);
        buildItems();
        setupColors();
        setupPaint();
    }

    public KeyboardSelectorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        buildItems();
        setupColors();
        setupPaint();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        for (Map.Entry<StandardKey, KeyItem> entry : items.entrySet()) {
            KeyItem item = entry.getValue();
            if (item.bounding.contains(event.getX(), event.getY())) {
                processTouch(item, event);
                invalidate();
                return true;
            }
        }
        setPressedKeyItem(null);
        invalidate();
        return false;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        for (Map.Entry<StandardKey, KeyItem> entry : items.entrySet()) {
            entry.getValue().onDraw(canvas);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width, height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
            height = (int) (width / AspectRatio);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(DefaultWidth, widthSize);
            height = (int) (width / AspectRatio);
        } else if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
            width = (int) (height * AspectRatio);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min((int) (DefaultWidth / AspectRatio), heightSize);
            width = (int) (height * AspectRatio);
        } else {
            width = DefaultWidth;
            height = (int) (width / AspectRatio);
        }

        setMeasuredDimension(width, height);

        normalKeySize = width * KeySizeRatio;
        normalMarginSize = width * KeyRegionMarginRatio;
        normalElevationSize = width * KeyElevationRatio;
        normalRadius = width * KeyRadiusRatio;
        for (Map.Entry<StandardKey, KeyItem> entry : items.entrySet())
            entry.getValue().onUpdate();

        setupTextSize();
    }

    private void processTouch(@NonNull KeyItem keyItem, @NonNull MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            if (keyItem.pressed)
                return;
            setPressedKeyItem(keyItem);
        } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
            setPressedKeyItem(null);
            setSelectedKey(keyItem.selected ? null : keyItem.key);
        }
    }

    public void setSelectedKey(@Nullable StandardKey selectedKey) {
        if (selectedKeyItem != null) {
            selectedKeyItem.selected = false;
            selectedKeyItem.onUpdate();
        }

        KeyItem item = items.get(selectedKey);
        if (item == null) {
            selectedKeyItem = null;
            return;
        }

        item.selected = true;
        item.onUpdate();
        selectedKeyItem = item;
    }

    private void setPressedKeyItem(@Nullable KeyItem keyItem) {
        if (pressedKeyItem != null) {
            pressedKeyItem.pressed = false;
            pressedKeyItem.onUpdate();
        }

        pressedKeyItem = keyItem;
        if (pressedKeyItem == null)
            return;

        pressedKeyItem.pressed = true;
        keyItem.onUpdate();
    }

    @Nullable
    public StandardKey getSelectedKey() {
        if (selectedKeyItem != null)
            return selectedKeyItem.key;
        return null;
    }

    private void setupColors() {
        setupColors(colors, DefaultColorSurface, DefaultColorOnSurface);
        setupColors(colorsWhenPressed, DefaultPressedColorSurface, DefaultColorOnSurface);
        setupColors(colorsWhenSelected, DefaultSelectedColorSurface, DefaultColorOnSurface);
    }

    @SuppressWarnings("SameParameterValue")
    private void setupColors(@NonNull ColorGroup colors, @ColorInt int surfaceColor,
                             @ColorInt int onSurfaceColor) {

        colors.colorSurface = surfaceColor;
        colors.colorOnSurface = onSurfaceColor;
        colors.colorBackground = setColorLightness(surfaceColor, 0.2f);
        colors.colorEdgeTop = setColorLightness(surfaceColor, 0.6f);
        colors.colorEdgeLeft = setColorLightness(surfaceColor, 0.6f);
        colors.colorEdgeRight = setColorLightness(surfaceColor, 0.85f);
        colors.colorEdgeBottom = setColorLightness(surfaceColor, 0.85f);
    }

    private void setupTextSize() {
        float maxTextSize = normalKeySize - normalElevationSize;
        float minTextSize = 0;
        float textSize;
        Rect bounds = new Rect();

        for (textSize = maxTextSize; textSize > minTextSize; textSize -= 0.5f) {
            textPaint.setTextSize(textSize);
            textPaint.getTextBounds("A", 0, 1, bounds);
            if (bounds.width() < maxTextSize * 0.3f)
                break;
        }

        textSizeLarge = textSize;
        textLargetOffset = bounds.width() * 0.25f;

        for (textSize = maxTextSize; textSize > minTextSize; textSize -= 0.5f) {
            textPaint.setTextSize(textSize);
            textPaint.getTextBounds("A", 0, 1, bounds);
            if (bounds.width() < maxTextSize / 6f)
                break;
        }
        textSizeMedium = textSize;
        textMediumOffset = bounds.width() * 0.5f;
    }

    @ColorInt
    private int setColorLightness(@ColorInt int color, float lightness) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        hsl[2] = hsl[2] * lightness;
        return ColorUtils.HSLToColor(hsl);
    }

    private void setupPaint() {
        surfacePaint.setStyle(Paint.Style.FILL);
        surfacePaint.setAntiAlias(true);

        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);

        sideSurfacePaint.setStyle(Paint.Style.FILL);
        sideSurfacePaint.setAntiAlias(true);

        textPaint.setTypeface(Typeface.MONOSPACE);
        textPaint.setAntiAlias(true);
    }

    private void buildItems() {
        // shit
        builder().sK(Esc).sTa(new Align(Align.AlignDirection.Top, 0, true)).sLa(new Align(Align.AlignDirection.Left, 0, true)).sPs("Esc").sS(SymbolStyle.S2).add();
        builder().sK(Grave).sTa(new Align(Align.AlignDirection.Bottom, Esc, true)).sLa(left(Esc)).sPs("`").sSs("~").add();
        builder().sK(_1).sTa(top(Grave)).sLa(right(Grave)).sPs("1").sSs("!").add();
        builder().sK(_2).sTa(top(Grave)).sLa(right(_1)).sPs("2").sSs("@").add();
        builder().sK(_3).sTa(top(Grave)).sLa(right(_2)).sPs("3").sSs("#").add();
        builder().sK(_4).sTa(top(Grave)).sLa(right(_3)).sPs("4").sSs("$").add();
        builder().sK(_5).sTa(top(Grave)).sLa(right(_4)).sPs("5").sSs("%").add();
        builder().sK(_6).sTa(top(Grave)).sLa(right(_5)).sPs("6").sSs("^").add();
        builder().sK(_7).sTa(top(Grave)).sLa(right(_6)).sPs("7").sSs("&").add();
        builder().sK(_8).sTa(top(Grave)).sLa(right(_7)).sPs("8").sSs("*").add();
        builder().sK(_9).sTa(top(Grave)).sLa(right(_8)).sPs("9").sSs("(").add();
        builder().sK(_0).sTa(top(Grave)).sLa(right(_9)).sPs("0").sSs(")").add();
        builder().sK(Minus).sTa(top(Grave)).sLa(right(_0)).sPs("-").sSs("_").add();
        builder().sK(Equal).sTa(top(Grave)).sLa(right(Minus)).sPs("=").sSs("+").add();
        builder().sK(Backspace).sTa(top(Grave)).sLa(right(Equal)).sPs("Backspace").sWr(2f).sS(SymbolStyle.S2).add();
        builder().sK(Tab).sTa(bottom(Grave)).sLa(left(Grave)).sPs("Tab").sWr(1.5f).sS(SymbolStyle.S2).add();
        builder().sK(Q).sTa(top(Tab)).sLa(right(Tab)).sPs("Q").add();
        builder().sK(W).sTa(top(Tab)).sLa(right(Q)).sPs("W").add();
        builder().sK(E).sTa(top(Tab)).sLa(right(W)).sPs("E").add();
        builder().sK(R).sTa(top(Tab)).sLa(right(E)).sPs("R").add();
        builder().sK(T).sTa(top(Tab)).sLa(right(R)).sPs("T").add();
        builder().sK(StandardKey.Y).sTa(top(Tab)).sLa(right(T)).sPs("Y").add();
        builder().sK(U).sTa(top(Tab)).sLa(right(StandardKey.Y)).sPs("U").add();
        builder().sK(I).sTa(top(Tab)).sLa(right(U)).sPs("I").add();
        builder().sK(O).sTa(top(Tab)).sLa(right(I)).sPs("O").add();
        builder().sK(P).sTa(top(Tab)).sLa(right(O)).sPs("P").add();
        builder().sK(BracketLeft).sTa(top(Tab)).sLa(right(P)).sPs("[").sSs("{").add();
        builder().sK(BracketRight).sTa(top(Tab)).sLa(right(BracketLeft)).sPs("]").sSs("}").add();
        builder().sK(Backslash).sTa(top(Tab)).sLa(right(BracketRight)).sPs("\\").sSs("|").sWr(1.5f).add();
        builder().sK(CapsLock).sTa(bottom(Tab)).sLa(left(Tab)).sPs("CapsLock").sWr(1.75f).sS(SymbolStyle.S2).add();
        builder().sK(A).sTa(top(CapsLock)).sLa(right(CapsLock)).sPs("A").add();
        builder().sK(S).sTa(top(CapsLock)).sLa(right(A)).sPs("S").add();
        builder().sK(D).sTa(top(CapsLock)).sLa(right(S)).sPs("D").add();
        builder().sK(F).sTa(top(CapsLock)).sLa(right(D)).sPs("F").add();
        builder().sK(G).sTa(top(CapsLock)).sLa(right(F)).sPs("G").add();
        builder().sK(H).sTa(top(CapsLock)).sLa(right(G)).sPs("H").add();
        builder().sK(J).sTa(top(CapsLock)).sLa(right(H)).sPs("J").add();
        builder().sK(K).sTa(top(CapsLock)).sLa(right(J)).sPs("K").add();
        builder().sK(L).sTa(top(CapsLock)).sLa(right(K)).sPs("L").add();
        builder().sK(Semicolon).sTa(top(CapsLock)).sLa(right(L)).sPs(";").sSs(":").add();
        builder().sK(Apostrophe).sTa(top(CapsLock)).sLa(right(Semicolon)).sPs("'").sSs("\"").add();
        builder().sK(Enter).sTa(top(CapsLock)).sLa(right(Apostrophe)).sPs("Enter").sWr(2.25f).sS(SymbolStyle.S2).add();
        builder().sK(LShift).sTa(bottom(CapsLock)).sLa(left(CapsLock)).sPs("Shift").sWr(2.25f).sS(SymbolStyle.S2).add();
        builder().sK(StandardKey.Z).sTa(top(LShift)).sLa(right(LShift)).sPs("Z").add();
        builder().sK(StandardKey.X).sTa(top(LShift)).sLa(right(StandardKey.Z)).sPs("X").add();
        builder().sK(C).sTa(top(LShift)).sLa(right(StandardKey.X)).sPs("C").add();
        builder().sK(V).sTa(top(LShift)).sLa(right(C)).sPs("V").add();
        builder().sK(B).sTa(top(LShift)).sLa(right(V)).sPs("B").add();
        builder().sK(N).sTa(top(LShift)).sLa(right(B)).sPs("N").add();
        builder().sK(M).sTa(top(LShift)).sLa(right(N)).sPs("M").add();
        builder().sK(Comma).sTa(top(LShift)).sLa(right(M)).sPs(",").sSs("<").add();
        builder().sK(Period).sTa(top(LShift)).sLa(right(Comma)).sPs(".").sSs(">").add();
        builder().sK(Slash).sTa(top(LShift)).sLa(right(Period)).sPs("/").sSs("?").add();
        builder().sK(RShift).sTa(top(LShift)).sLa(right(Slash)).sPs("Shift").sWr(2.75f).sS(SymbolStyle.S2).add();
        builder().sK(LCtrl).sTa(bottom(LShift)).sLa(left(LShift)).sPs("Ctrl").sWr(1.25f).sS(SymbolStyle.S2).add();
        builder().sK(LWin).sTa(top(LCtrl)).sLa(right(LCtrl)).sPs("Win").sWr(1.25f).sS(SymbolStyle.S2).add();
        builder().sK(LAlt).sTa(top(LCtrl)).sLa(right(LWin)).sPs("Alt").sWr(1.25f).sS(SymbolStyle.S2).add();
        builder().sK(Space).sTa(top(LCtrl)).sLa(right(LAlt)).sWr(6.25f).add();
        builder().sK(RAlt).sTa(top(LCtrl)).sLa(right(Space)).sPs("Alt").sWr(1.25f).sS(SymbolStyle.S2).add();
        builder().sK(RWin).sTa(top(LCtrl)).sLa(right(RAlt)).sPs("Fn").sWr(1.25f).sS(SymbolStyle.S2).add();
        builder().sK(Apps).sTa(top(LCtrl)).sLa(right(RWin)).sPs("Apps").sWr(1.25f).sS(SymbolStyle.S2).add();
        builder().sK(RCtrl).sTa(top(LCtrl)).sLa(right(Apps)).sPs("Ctrl").sWr(1.25f).sS(SymbolStyle.S2).add();
        builder().sK(F1).sTa(top(Esc)).sLa(left(_2)).sPs("F1").sS(SymbolStyle.S2).add();
        builder().sK(F2).sTa(top(Esc)).sLa(right(F1)).sPs("F2").sS(SymbolStyle.S2).add();
        builder().sK(F3).sTa(top(Esc)).sLa(right(F2)).sPs("F3").sS(SymbolStyle.S2).add();
        builder().sK(F4).sTa(top(Esc)).sLa(right(F3)).sPs("F4").sS(SymbolStyle.S2).add();
        builder().sK(F5).sTa(top(Esc)).sLa(left(StandardKey.Y)).sPs("F5").sS(SymbolStyle.S2).add();
        builder().sK(F6).sTa(top(Esc)).sLa(right(F5)).sPs("F6").sS(SymbolStyle.S2).add();
        builder().sK(F7).sTa(top(Esc)).sLa(right(F6)).sPs("F7").sS(SymbolStyle.S2).add();
        builder().sK(F8).sTa(top(Esc)).sLa(right(F7)).sPs("F8").sS(SymbolStyle.S2).add();
        builder().sK(F9).sTa(top(Esc)).sLa(left(Minus)).sPs("F9").sS(SymbolStyle.S2).add();
        builder().sK(F10).sTa(top(Esc)).sLa(right(F9)).sPs("F10").sS(SymbolStyle.S2).add();
        builder().sK(F11).sTa(top(Esc)).sLa(right(F10)).sPs("F11").sS(SymbolStyle.S2).add();
        builder().sK(F12).sTa(top(Esc)).sLa(right(F11)).sPs("F12").sS(SymbolStyle.S2).add();
        builder().sK(PrintScreen).sTa(top(Esc)).sLa(rightM(F12)).sPs("PrtSc").sS(SymbolStyle.S2).add();
        builder().sK(ScrollLock).sTa(top(Esc)).sLa(right(PrintScreen)).sPs("SrcLk").sS(SymbolStyle.S2).add();
        builder().sK(Pause).sTa(top(Esc)).sLa(right(ScrollLock)).sPs("Pause").sS(SymbolStyle.S2).add();
        builder().sK(Insert).sTa(top(Grave)).sLa(left(PrintScreen)).sPs("Ins").sS(SymbolStyle.S2).add();
        builder().sK(Home).sTa(top(Grave)).sLa(right(Insert)).sPs("Home").sS(SymbolStyle.S2).add();
        builder().sK(PageUp).sTa(top(Grave)).sLa(right(Home)).sPs("PgUp").sS(SymbolStyle.S2).add();
        builder().sK(Delete).sTa(top(Tab)).sLa(left(Insert)).sPs("Del").sS(SymbolStyle.S2).add();
        builder().sK(End).sTa(top(Tab)).sLa(right(Delete)).sPs("End").sS(SymbolStyle.S2).add();
        builder().sK(PageDown).sTa(top(Tab)).sLa(right(End)).sPs("PgDn").sS(SymbolStyle.S2).add();
        builder().sK(Up).sTa(top(LShift)).sLa(left(End)).sPs("↑").add();
        builder().sK(Left).sTa(top(LCtrl)).sLa(left(Delete)).sPs("←").add();
        builder().sK(Down).sTa(top(LCtrl)).sLa(right(Left)).sPs("↓").add();
        builder().sK(Right).sTa(top(LCtrl)).sLa(right(Down)).sPs("→").add();
        builder().sK(NumLock).sTa(top(PageUp)).sLa(rightM(PageUp)).sPs("Num").sS(SymbolStyle.S2).add();
        builder().sK(NumpadDivide).sTa(top(NumLock)).sLa(right(NumLock)).sPs("/").sS(SymbolStyle.S2).add();
        builder().sK(NumpadMultiply).sTa(top(NumLock)).sLa(right(NumpadDivide)).sPs("*").sS(SymbolStyle.S2).add();
        builder().sK(NumpadSubtract).sTa(top(NumLock)).sLa(right(NumpadMultiply)).sPs("-").sS(SymbolStyle.S2).add();
        builder().sK(Numpad7).sTa(bottom(NumLock)).sLa(left(NumLock)).sPs("7").add();
        builder().sK(Numpad8).sTa(top(Numpad7)).sLa(right(Numpad7)).sPs("8").add();
        builder().sK(Numpad9).sTa(top(Numpad7)).sLa(right(Numpad8)).sPs("9").add();
        builder().sK(NumpadAdd).sTa(top(Numpad7)).sLa(right(Numpad9)).sPs("+").sHr(2).sS(SymbolStyle.S3).add();
        builder().sK(Numpad4).sTa(bottom(Numpad7)).sLa(left(Numpad7)).sPs("4").add();
        builder().sK(Numpad5).sTa(top(Numpad4)).sLa(right(Numpad4)).sPs("5").add();
        builder().sK(Numpad6).sTa(top(Numpad4)).sLa(right(Numpad5)).sPs("6").add();
        builder().sK(Numpad1).sTa(bottom(Numpad4)).sLa(left(Numpad4)).sPs("1").add();
        builder().sK(Numpad2).sTa(top(Numpad1)).sLa(right(Numpad1)).sPs("2").add();
        builder().sK(Numpad3).sTa(top(Numpad1)).sLa(right(Numpad2)).sPs("3").add();
        builder().sK(NumpadEnter).sTa(top(Numpad1)).sLa(right(Numpad3)).sPs("Enter").sHr(2).sS(SymbolStyle.S2).add();
        builder().sK(Numpad0).sTa(bottom(Numpad1)).sLa(left(Numpad1)).sPs("0").sWr(2).add();
        builder().sK(NumpadDot).sTa(top(Numpad0)).sLa(right(Numpad0)).sPs(".").add();
    }

    @NonNull
    private KeyItemBuilder builder() {
        return new KeyItemBuilder();
    }

    @NonNull
    private Align left(@NonNull StandardKey key) {
        return new Align(Align.AlignDirection.Left, key, false);
    }

    @NonNull
    private Align right(@NonNull StandardKey key) {
        return new Align(Align.AlignDirection.Right, key, false);
    }

    @NonNull
    private Align rightM(@NonNull StandardKey key) {
        return new Align(Align.AlignDirection.Right, key, true);
    }

    @NonNull
    private Align top(@NonNull StandardKey key) {
        return new Align(Align.AlignDirection.Top, key, false);
    }

    @NonNull
    private Align bottom(@NonNull StandardKey key) {
        return new Align(Align.AlignDirection.Bottom, key, false);
    }

    /*
        -----------   -----------
        |primary  |   |         |
        |secondary|   | primary |
        ----------   -----------
    */
    private enum SymbolStyle {
        S1,
        S2,
        S3 // for NumpadAdd
    }

    private static class ColorGroup {

        public int colorSurface;
        public int colorOnSurface;
        public int colorBackground;
        public int colorEdgeTop;
        public int colorEdgeBottom;
        public int colorEdgeLeft;
        public int colorEdgeRight;
    }

    private static class Align {

        private enum AlignType {
            Direct,
            Relative
        }

        private enum AlignDirection {
            Top,
            Bottom,
            Left,
            Right
        }

        public final AlignType type;
        public final AlignDirection dir;
        public final float value;
        public final StandardKey target;
        public final boolean hasMargin;

        private Align(@NonNull AlignType type, @NonNull AlignDirection dir, float value,
                      @Nullable StandardKey target, boolean hasMargin) {

            this.type = type;
            this.dir = dir;
            this.value = value;
            this.target = target;
            this.hasMargin = hasMargin;
        }

        public Align(@NonNull AlignDirection dir, float value, boolean hasMargin) {
            this(AlignType.Direct, dir, value, null, hasMargin);
        }

        public Align(@NonNull AlignDirection dir, @NonNull StandardKey target, boolean hasMargin) {
            this(AlignType.Relative, dir, -1, target, hasMargin);
        }
    }

    private class KeyItem {

        public StandardKey key;
        public final RectF bounding = new RectF();
        private final RectF surfaceBounding = new RectF();
        private final RectF drawBounding = new RectF();
        private final Path frontSurfacePath = new Path();
        private final Path behindSurfacePath = new Path();
        private final Path leftSurfacePath = new Path();
        private final Path rightSurfacePath = new Path();
        private LinearGradient frontSurfaceShader;
        private LinearGradient behindSurfaceShader;
        private LinearGradient leftSurfaceShader;
        private LinearGradient rightSurfaceShader;
        public String primarySymbol;
        public String secondarySymbol;
        public Align topAlign;
        public Align leftAlign;
        public float widthRatio;
        public float heightRatio;
        public SymbolStyle symbolStyle;
        public boolean pressed;
        public boolean selected;
        private ColorGroup colors;
        private float padding;

        public void onDraw(@NonNull Canvas canvas) {
            drawSurface(canvas);
            drawText(canvas);
        }

        public void onUpdate() {
            padding = (pressed || selected) ? normalKeySize * 0.015f : 0;
            selectColor();
            updateBounding();
            updateSurfaceBounding();
            updateSurfacePaths();
            updateShaders();
        }

        private void drawSurface(@NonNull Canvas canvas) {
            backgroundPaint.setColor(colors.colorBackground);
            canvas.drawRoundRect(drawBounding, normalRadius, normalRadius, backgroundPaint);
            sideSurfacePaint.setShader(frontSurfaceShader);
            canvas.drawPath(frontSurfacePath, sideSurfacePaint);
            sideSurfacePaint.setShader(behindSurfaceShader);
            canvas.drawPath(behindSurfacePath, sideSurfacePaint);
            sideSurfacePaint.setShader(leftSurfaceShader);
            canvas.drawPath(leftSurfacePath, sideSurfacePaint);
            sideSurfacePaint.setShader(rightSurfaceShader);
            canvas.drawPath(rightSurfacePath, sideSurfacePaint);
            surfacePaint.setColor(colors.colorSurface);
            canvas.drawRoundRect(surfaceBounding, normalRadius, normalRadius, surfacePaint);
        }

        private void drawText(@NonNull Canvas canvas) {
            if (primarySymbol == null)
                return;

            textPaint.setColor(colors.colorOnSurface);
            if (symbolStyle == SymbolStyle.S1) {
                if (secondarySymbol == null) {
                    textPaint.setTextSize(textSizeLarge);
                    canvas.drawText(primarySymbol, surfaceBounding.left + textLargetOffset,
                            surfaceBounding.top - textPaint.ascent(), textPaint);
                } else {
                    textPaint.setTextSize(textSizeMedium);
                    canvas.drawText(secondarySymbol, surfaceBounding.left + textMediumOffset,
                            surfaceBounding.top - textPaint.ascent() + textMediumOffset, textPaint);
                    canvas.drawText(primarySymbol, surfaceBounding.left + textMediumOffset,
                            surfaceBounding.bottom - textPaint.descent() - textMediumOffset, textPaint);
                }
            } else if (symbolStyle == SymbolStyle.S2) {
                textPaint.setTextSize(textSizeMedium);
                canvas.drawText(primarySymbol, surfaceBounding.left + textMediumOffset,
                        surfaceBounding.centerY() + textPaint.descent(), textPaint);
            } else if (symbolStyle == SymbolStyle.S3) {
                textPaint.setTextSize(textSizeLarge);
                canvas.drawText(primarySymbol, surfaceBounding.left + textLargetOffset,
                        surfaceBounding.centerY() + textPaint.descent(), textPaint);
            }
        }

        private void updateBounding() {
            float top, left;

            if (topAlign == null || leftAlign == null)
                throw new IllegalStateException("Top align and left align must not be null.");

            if (topAlign.type == Align.AlignType.Direct) {
                top = topAlign.value + (topAlign.hasMargin ? normalMarginSize : 0);
            } else if (topAlign.type == Align.AlignType.Relative) {
                KeyItem target = items.get(topAlign.target);
                float margin = topAlign.hasMargin ? normalMarginSize : 0;

                if (target == null)
                    throw new IllegalStateException("Target must be created before using.");
                if (topAlign.dir == Align.AlignDirection.Top)
                    top = target.bounding.top + margin;
                else if (topAlign.dir == Align.AlignDirection.Bottom)
                    top = target.bounding.bottom + margin;
                else
                    throw new IllegalArgumentException("Dir of top align must be [Top] or [Bottom].");
            } else {
                throw new IllegalArgumentException("Unsupported align: " + topAlign.type.name());
            }

            if (leftAlign.type == Align.AlignType.Direct) {
                left = leftAlign.value + (leftAlign.hasMargin ? normalMarginSize : 0);
            } else if (leftAlign.type == Align.AlignType.Relative) {
                KeyItem target = items.get(leftAlign.target);
                float margin = leftAlign.hasMargin ? normalMarginSize : 0;

                if (target == null)
                    throw new IllegalStateException("Target must be created before using.");
                if (leftAlign.dir == Align.AlignDirection.Left)
                    left = target.bounding.left + margin;
                else if (leftAlign.dir == Align.AlignDirection.Right)
                    left = target.bounding.right + margin;
                else
                    throw new IllegalArgumentException("Dir of top align must be [Left] or [Right].");

            } else {
                throw new IllegalArgumentException("Unsupported align: " + leftAlign.type.name());
            }

            bounding.set(left, top, left + normalKeySize * widthRatio,
                    top + normalKeySize * heightRatio);
            drawBounding.set(bounding.left + padding, bounding.top + padding,
                    bounding.right - padding, bounding.bottom - padding);
        }

        private void updateSurfaceBounding() {
            float leftElevation = getLeftElevation();
            float rightElevation = normalElevationSize - leftElevation;
            float topElevation = getTopElevation();
            float bottomElevation = normalElevationSize - topElevation;

            surfaceBounding.set(
                    drawBounding.left + leftElevation,
                    drawBounding.top + topElevation,
                    drawBounding.right - rightElevation,
                    drawBounding.bottom - bottomElevation
            );
        }

        private void updateSurfacePaths() {
            float radiusMargin = normalRadius / 2f;
            float bLeft = drawBounding.left + radiusMargin;
            float bRight = drawBounding.right - radiusMargin;
            float bTop = drawBounding.top + radiusMargin;
            float bBottom = drawBounding.bottom - radiusMargin;
            float sLeft = surfaceBounding.left + radiusMargin;
            float sRight = surfaceBounding.right - radiusMargin;
            float sTop = surfaceBounding.top + radiusMargin;
            float sBottom = surfaceBounding.bottom - radiusMargin;

            frontSurfacePath.reset();
            frontSurfacePath.moveTo(sRight, sTop);
            frontSurfacePath.lineTo(bRight, bTop);
            frontSurfacePath.lineTo(bLeft, bTop);
            frontSurfacePath.lineTo(sLeft, sTop);
            frontSurfacePath.close();

            behindSurfacePath.reset();
            behindSurfacePath.moveTo(sRight, sBottom);
            behindSurfacePath.lineTo(bRight, bBottom);
            behindSurfacePath.lineTo(bLeft, bBottom);
            behindSurfacePath.lineTo(sLeft, sBottom);
            behindSurfacePath.close();

            leftSurfacePath.reset();
            leftSurfacePath.moveTo(sLeft, sTop);
            leftSurfacePath.lineTo(bLeft, bTop);
            leftSurfacePath.lineTo(bLeft, bBottom);
            leftSurfacePath.lineTo(sLeft, sBottom);
            leftSurfacePath.close();

            rightSurfacePath.reset();
            rightSurfacePath.moveTo(sRight, sTop);
            rightSurfacePath.lineTo(bRight, bTop);
            rightSurfacePath.lineTo(bRight, bBottom);
            rightSurfacePath.lineTo(sRight, sBottom);
            rightSurfacePath.close();
        }

        private void selectColor() {
            if (pressed)
                colors = colorsWhenPressed;
            else if (selected)
                colors = colorsWhenSelected;
            else
                colors = KeyboardSelectorView.this.colors;
        }

        private void updateShaders() {
            frontSurfaceShader = new LinearGradient(0, surfaceBounding.top, 0, drawBounding.top,
                    colors.colorEdgeTop, colors.colorBackground, Shader.TileMode.CLAMP);
            behindSurfaceShader = new LinearGradient(0, surfaceBounding.bottom, 0, drawBounding.bottom,
                    colors.colorEdgeBottom, colors.colorBackground, Shader.TileMode.CLAMP);
            leftSurfaceShader = new LinearGradient(surfaceBounding.left, 0, drawBounding.left, 0,
                    colors.colorEdgeLeft, colors.colorBackground, Shader.TileMode.CLAMP);
            rightSurfaceShader = new LinearGradient(surfaceBounding.right, 0, drawBounding.right, 0,
                    colors.colorEdgeRight, colors.colorBackground, Shader.TileMode.CLAMP);
        }

        private float getLeftElevation() {
            float width = getMeasuredWidth();
            float centerX = bounding.centerX();
            float offset = EyeDistance - EyeDistance * centerX / width * 2;
            return (centerX / width + offset) * normalElevationSize;
        }

        private float getTopElevation() {
            float height = getMeasuredHeight();
            float centerY = bounding.centerY();
            float offset = EyeDistance - EyeDistance * centerY / height * 2;
            return (centerY / height + offset) * normalElevationSize;
        }
    }

    private class KeyItemBuilder {

        private StandardKey key = None;
        private String primarySymbol;
        private String secondarySymbol;
        private Align topAlign;
        private Align leftAlign;
        private float widthRatio = 1;
        private float heightRatio = 1;
        private SymbolStyle symbolStyle = SymbolStyle.S1;

        @NonNull
        public KeyItemBuilder sK(@NonNull StandardKey key) {
            this.key = key;
            return this;
        }

        @NonNull
        public KeyItemBuilder sPs(@Nullable String primarySymbol) {
            this.primarySymbol = primarySymbol;
            return this;
        }

        @NonNull
        public KeyItemBuilder sSs(@NonNull String secondarySymbol) {
            this.secondarySymbol = secondarySymbol;
            return this;
        }

        @NonNull
        public KeyItemBuilder sTa(@NonNull Align topAlign) {
            this.topAlign = topAlign;
            return this;
        }

        @NonNull
        public KeyItemBuilder sLa(@NonNull Align leftAlign) {
            this.leftAlign = leftAlign;
            return this;
        }

        @NonNull
        public KeyItemBuilder sWr(float widthRatio) {
            this.widthRatio = widthRatio;
            return this;
        }

        @NonNull
        public KeyItemBuilder sHr(float heightRatio) {
            this.heightRatio = heightRatio;
            return this;
        }

        @NonNull
        public KeyItemBuilder sS(@NonNull SymbolStyle symbolStyle) {
            this.symbolStyle = symbolStyle;
            return this;
        }

        public void add() {
            KeyItem item = new KeyItem();
            item.key = key;
            item.primarySymbol = primarySymbol;
            item.secondarySymbol = secondarySymbol;
            item.topAlign = topAlign;
            item.leftAlign = leftAlign;
            item.widthRatio = widthRatio;
            item.heightRatio = heightRatio;
            item.symbolStyle = symbolStyle;
            if (items.put(key, item) != null)
                throw new IllegalStateException("KeyItem exists: " + key.name());
        }
    }
}
