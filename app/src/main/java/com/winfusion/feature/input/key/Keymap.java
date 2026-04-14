package com.winfusion.feature.input.key;

import androidx.annotation.NonNull;

public interface Keymap<T> {

    @NonNull
    T toKey(int code);

    int toCode(@NonNull T key);
}
