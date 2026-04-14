package com.winfusion.feature.input.key;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public interface StandardItem {

    @NonNull
    String getSymbol();

    @StringRes
    int getResId();

    @NonNull
    String name();
}
