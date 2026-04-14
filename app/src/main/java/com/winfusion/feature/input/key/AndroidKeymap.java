package com.winfusion.feature.input.key;

import static android.view.KeyEvent.*;
import static com.winfusion.feature.input.key.StandardKey.*;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class AndroidKeymap implements Keymap<StandardKey> {

    private final HashMap<StandardKey, Integer> originalMap = new HashMap<>();
    private final HashMap<Integer, StandardKey> reversedMap = new HashMap<>();

    public AndroidKeymap() {
        initOriginalMap();
        initReversedMap();
    }

    @NonNull
    @Override
    public StandardKey toKey(int keycode) {
        StandardKey key = reversedMap.get(keycode);
        return key == null ? StandardKey.None : key;
    }

    @Override
    public int toCode(@NonNull StandardKey standardKey) {
        Integer keycode = originalMap.get(standardKey);
        return keycode == null ? KEYCODE_UNKNOWN : keycode;
    }

    private void initOriginalMap() {
        // letter
        put(A, KEYCODE_A);
        put(B, KEYCODE_B);
        put(C, KEYCODE_C);
        put(D, KEYCODE_D);
        put(E, KEYCODE_E);
        put(F, KEYCODE_F);
        put(G, KEYCODE_G);
        put(H, KEYCODE_H);
        put(I, KEYCODE_I);
        put(J, KEYCODE_J);
        put(K, KEYCODE_K);
        put(L, KEYCODE_L);
        put(M, KEYCODE_M);
        put(N, KEYCODE_N);
        put(O, KEYCODE_O);
        put(P, KEYCODE_P);
        put(Q, KEYCODE_Q);
        put(R, KEYCODE_R);
        put(S, KEYCODE_S);
        put(T, KEYCODE_T);
        put(U, KEYCODE_U);
        put(V, KEYCODE_V);
        put(W, KEYCODE_W);
        put(X, KEYCODE_X);
        put(Y, KEYCODE_Y);
        put(Z, KEYCODE_Z);

        // num
        put(_0, KEYCODE_0);
        put(_1, KEYCODE_1);
        put(_2, KEYCODE_2);
        put(_3, KEYCODE_3);
        put(_4, KEYCODE_4);
        put(_5, KEYCODE_5);
        put(_6, KEYCODE_6);
        put(_7, KEYCODE_7);
        put(_8, KEYCODE_8);
        put(_9, KEYCODE_9);

        // numeric keypad
        put(Numpad0, KEYCODE_NUMPAD_0);
        put(Numpad1, KEYCODE_NUMPAD_1);
        put(Numpad2, KEYCODE_NUMPAD_2);
        put(Numpad3, KEYCODE_NUMPAD_3);
        put(Numpad4, KEYCODE_NUMPAD_4);
        put(Numpad5, KEYCODE_NUMPAD_5);
        put(Numpad6, KEYCODE_NUMPAD_6);
        put(Numpad7, KEYCODE_NUMPAD_7);
        put(Numpad8, KEYCODE_NUMPAD_8);
        put(Numpad9, KEYCODE_NUMPAD_9);
        put(NumpadDivide, KEYCODE_NUMPAD_DIVIDE);
        put(NumpadMultiply, KEYCODE_NUMPAD_MULTIPLY);
        put(NumpadSubtract, KEYCODE_NUMPAD_SUBTRACT);
        put(NumpadAdd, KEYCODE_NUMPAD_ADD);
        put(NumpadEnter, KEYCODE_NUMPAD_ENTER);
        put(NumpadDot, KEYCODE_NUMPAD_DOT);
        put(NumLock, KEYCODE_NUM_LOCK);

        // function
        put(F1, KEYCODE_F1);
        put(F2, KEYCODE_F2);
        put(F3, KEYCODE_F3);
        put(F4, KEYCODE_F4);
        put(F5, KEYCODE_F5);
        put(F6, KEYCODE_F6);
        put(F7, KEYCODE_F7);
        put(F8, KEYCODE_F8);
        put(F9, KEYCODE_F9);
        put(F10, KEYCODE_F10);
        put(F11, KEYCODE_F11);
        put(F12, KEYCODE_F12);

        // control
        put(LShift, KEYCODE_SHIFT_LEFT);
        put(RShift, KEYCODE_SHIFT_RIGHT);
        put(LCtrl, KEYCODE_CTRL_LEFT);
        put(RCtrl, KEYCODE_CTRL_RIGHT);
        put(LAlt, KEYCODE_ALT_LEFT);
        put(RAlt, KEYCODE_ALT_RIGHT);
        put(LWin, KEYCODE_META_LEFT);
        put(RWin, KEYCODE_META_RIGHT);
        put(Apps, KEYCODE_MENU);

        // special control
        put(Esc, KEYCODE_ESCAPE);
        put(Tab, KEYCODE_TAB);
        put(CapsLock, KEYCODE_CAPS_LOCK);
        put(Space, KEYCODE_SPACE);
        put(Enter, KEYCODE_ENTER);
        put(Backspace, KEYCODE_DEL);
        put(Insert, KEYCODE_INSERT);
        put(Delete, KEYCODE_FORWARD_DEL);
        put(Home, KEYCODE_MOVE_HOME);
        put(End, KEYCODE_MOVE_END);
        put(PageUp, KEYCODE_PAGE_UP);
        put(PageDown, KEYCODE_PAGE_DOWN);
        put(ScrollLock, KEYCODE_SCROLL_LOCK);
        put(PrintScreen, KEYCODE_SYSRQ);
        put(Pause, KEYCODE_BREAK);

        // navigation
        put(Up, KEYCODE_DPAD_UP);
        put(Down, KEYCODE_DPAD_DOWN);
        put(Left, KEYCODE_DPAD_LEFT);
        put(Right, KEYCODE_DPAD_RIGHT);

        // symbol
        put(Grave, KEYCODE_GRAVE);
        put(Minus, KEYCODE_MINUS);
        put(Equal, KEYCODE_EQUALS);
        put(BracketLeft, KEYCODE_LEFT_BRACKET);
        put(BracketRight, KEYCODE_RIGHT_BRACKET);
        put(Backslash, KEYCODE_BACKSLASH);
        put(Semicolon, KEYCODE_SEMICOLON);
        put(Apostrophe, KEYCODE_APOSTROPHE);
        put(Comma, KEYCODE_COMMA);
        put(Period, KEYCODE_PERIOD);
        put(Slash, KEYCODE_SLASH);
    }

    private void initReversedMap() {
        for (Map.Entry<StandardKey, Integer> entry : originalMap.entrySet()) {
            if (reversedMap.put(entry.getValue(), entry.getKey()) != null)
                throw new IllegalStateException("Key will be overwrite: " + entry.getKey().name() + " " + entry.getValue());
        }
    }

    private void put(@NonNull StandardKey key, int keycode) {
        if (originalMap.put(key, keycode) != null)
            throw new IllegalStateException("Key will be overwrite: " + key.name() + " " + keycode);
    }
}
