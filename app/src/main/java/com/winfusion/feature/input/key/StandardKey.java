package com.winfusion.feature.input.key;

import androidx.annotation.NonNull;

public enum StandardKey implements StandardItem {

    // letter
    A,
    B,
    C,
    D,
    E,
    F,
    G,
    H,
    I,
    J,
    K,
    L,
    M,
    N,
    O,
    P,
    Q,
    R,
    S,
    T,
    U,
    V,
    W,
    X,
    Y,
    Z,

    // num
    _0("0"),
    _1("1"),
    _2("2"),
    _3("3"),
    _4("4"),
    _5("5"),
    _6("6"),
    _7("7"),
    _8("8"),
    _9("9"),

    // numeric keypad
    Numpad0("NP 0"),
    Numpad1("NP 1"),
    Numpad2("NP 2"),
    Numpad3("NP 3"),
    Numpad4("NP 4"),
    Numpad5("NP 5"),
    Numpad6("NP 6"),
    Numpad7("NP 7"),
    Numpad8("NP 8"),
    Numpad9("NP 9"),
    NumpadDivide("NP /"),   // /
    NumpadMultiply("*"),    // *
    NumpadSubtract("NP -"), // -
    NumpadAdd("NP +"),      // +
    NumpadEnter("NP Enter"),
    NumpadDot("NP ."),      // .
    NumLock,

    // function
    F1,
    F2,
    F3,
    F4,
    F5,
    F6,
    F7,
    F8,
    F9,
    F10,
    F11,
    F12,

    // control
    LShift,
    RShift,
    LCtrl,
    RCtrl,
    LAlt,
    RAlt,
    LWin,
    RWin,
    Apps,

    // special control
    Esc,
    Tab,
    CapsLock,
    Space,
    Enter,
    Backspace,
    Insert,
    Delete,
    Home,
    End,
    PageUp,
    PageDown,
    ScrollLock,
    PrintScreen,
    Pause,

    // navigation
    Up("↑"),
    Down("↓"),
    Left("←"),
    Right("→"),

    // symbol
    Grave("`"),             // `
    Minus("-"),             // -
    Equal("="),             // =
    BracketLeft("["),       // [
    BracketRight("]"),      // ]
    Backslash("\\"),        // \
    Semicolon(";"),         // ;
    Apostrophe("'"),        // '
    Comma(","),             // ,
    Period("."),            // .
    Slash("/"),             // /

    None;                        // reserved for all unsupported keys

    private final String symbol;

    StandardKey() {
        this.symbol = name();
    }

    StandardKey(@NonNull String symbol) {
        this.symbol = symbol;
    }

    @NonNull
    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public int getResId() {
        return 0;
    }
}
