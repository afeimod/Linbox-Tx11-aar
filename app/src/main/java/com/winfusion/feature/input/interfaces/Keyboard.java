package com.winfusion.feature.input.interfaces;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.event.KeyboardEvent;

public interface Keyboard {

    void onKeyboardEvent(@NonNull KeyboardEvent event);
}
