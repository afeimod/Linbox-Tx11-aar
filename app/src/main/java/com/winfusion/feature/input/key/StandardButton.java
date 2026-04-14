package com.winfusion.feature.input.key;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.winfusion.R;

public enum StandardButton implements StandardItem {

    BtnLeft("Mouse Left", R.string.mouse_button_left),
    BtnRight("Mouse Right", R.string.mouse_button_right),
    BtnMiddle("Mouse Middle", R.string.mouse_button_middle),
    BtnA("A"),
    BtnB("B"),
    BtnX("X"),
    BtnY("Y"),
    BtnL1("L1"),
    BtnR1("R1"),
    BtnL2("L2"),
    BtnR2("R2"),
    BtnL3("L3"),
    BtnR3("R3"),
    BtnDPadUp("↑", R.string.dpad_up),
    BtnDPadDown("↓", R.string.dpad_down),
    BtnDPadLeft("←", R.string.dpad_left),
    BtnDPadRight("→", R.string.dpad_right),
    BtnSelect("Select"),
    BtnStart("Start"),

    None;

    private final String symbol;
    private final int resId;

    StandardButton() {
        symbol = name();
        resId = 0;
    }

    StandardButton(@NonNull String symbol) {
        this.symbol = symbol;
        resId = 0;
    }

    StandardButton(@NonNull String symbol, @StringRes int resId) {
        this.symbol = symbol;
        this.resId = resId;
    }

    @NonNull
    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    @StringRes
    public int getResId() {
        return resId;
    }
}
