package com.winfusion.feature.input.key;

import static com.winfusion.feature.input.key.EvdevKeycode.*;
import static com.winfusion.feature.input.key.StandardKey.*;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public final class WaylandKeymap implements Keymap<StandardKey> {

    private final HashMap<StandardKey, Integer> originalMap = new HashMap<>();
    private final HashMap<Integer, StandardKey> reversedMap = new HashMap<>();

    public WaylandKeymap() {
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
        return keycode == null ? KEY_RESERVED : keycode;
    }

    private void initOriginalMap() {
        // letter
        put(A, KEY_A);
        put(B, KEY_B);
        put(C, KEY_C);
        put(D, KEY_D);
        put(E, KEY_E);
        put(F, KEY_F);
        put(G, KEY_G);
        put(H, KEY_H);
        put(I, KEY_I);
        put(J, KEY_J);
        put(K, KEY_K);
        put(L, KEY_L);
        put(M, KEY_M);
        put(N, KEY_N);
        put(O, KEY_O);
        put(P, KEY_P);
        put(Q, KEY_Q);
        put(R, KEY_R);
        put(S, KEY_S);
        put(T, KEY_T);
        put(U, KEY_U);
        put(V, KEY_V);
        put(W, KEY_W);
        put(X, KEY_X);
        put(Y, KEY_Y);
        put(Z, KEY_Z);

        // num
        put(_0, KEY_0);
        put(_1, KEY_1);
        put(_2, KEY_2);
        put(_3, KEY_3);
        put(_4, KEY_4);
        put(_5, KEY_5);
        put(_6, KEY_6);
        put(_7, KEY_7);
        put(_8, KEY_8);
        put(_9, KEY_9);

        // numeric keypad
        put(Numpad0, KEY_KP0);
        put(Numpad1, KEY_KP1);
        put(Numpad2, KEY_KP2);
        put(Numpad3, KEY_KP3);
        put(Numpad4, KEY_KP4);
        put(Numpad5, KEY_KP5);
        put(Numpad6, KEY_KP6);
        put(Numpad7, KEY_KP7);
        put(Numpad8, KEY_KP8);
        put(Numpad9, KEY_KP9);
        put(NumpadDivide, KEY_KPSLASH);
        put(NumpadMultiply, KEY_KPASTERISK);
        put(NumpadSubtract, KEY_KPMINUS);
        put(NumpadAdd, KEY_KPPLUS);
        put(NumpadEnter, KEY_KPENTER);
        put(NumpadDot, KEY_KPDOT);
        put(NumLock, KEY_NUMLOCK);

        // function
        put(F1, KEY_F1);
        put(F2, KEY_F2);
        put(F3, KEY_F3);
        put(F4, KEY_F4);
        put(F5, KEY_F5);
        put(F6, KEY_F6);
        put(F7, KEY_F7);
        put(F8, KEY_F8);
        put(F9, KEY_F9);
        put(F10, KEY_F10);
        put(F11, KEY_F11);
        put(F12, KEY_F12);

        // control
        put(LShift, KEY_LEFTSHIFT);
        put(RShift, KEY_RIGHTSHIFT);
        put(LCtrl, KEY_LEFTCTRL);
        put(RCtrl, KEY_RIGHTCTRL);
        put(LAlt, KEY_LEFTALT);
        put(RAlt, KEY_RIGHTALT);
        put(LWin, KEY_LEFTMETA);
        put(RWin, KEY_RIGHTMETA);
        put(Apps, KEY_MENU);

        // special control
        put(Esc, KEY_ESC);
        put(Tab, KEY_TAB);
        put(CapsLock, KEY_CAPSLOCK);
        put(Space, KEY_SPACE);
        put(Enter, KEY_ENTER);
        put(Backspace, KEY_BACKSPACE);
        put(Insert, KEY_INSERT);
        put(Delete, KEY_DELETE);
        put(Home, KEY_HOME);
        put(End, KEY_END);
        put(PageUp, KEY_PAGEUP);
        put(PageDown, KEY_PAGEDOWN);
        put(ScrollLock, KEY_SCROLLLOCK);
        put(PrintScreen, KEY_SYSRQ);
        put(Pause, KEY_PAUSE);

        // navigation
        put(Up, KEY_UP);
        put(Down, KEY_DOWN);
        put(Left, KEY_LEFT);
        put(Right, KEY_RIGHT);

        // symbol
        put(Grave, KEY_GRAVE);
        put(Minus, KEY_MINUS);
        put(Equal, KEY_EQUAL);
        put(BracketLeft, KEY_LEFTBRACE);
        put(BracketRight, KEY_RIGHTBRACE);
        put(Backslash, KEY_BACKSLASH);
        put(Semicolon, KEY_SEMICOLON);
        put(Apostrophe, KEY_APOSTROPHE);
        put(Comma, KEY_COMMA);
        put(Period, KEY_DOT);
        put(Slash, KEY_SLASH);
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
