package com.winfusion.feature.input.interfaces;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.event.ControllerAxisEvent;
import com.winfusion.feature.input.event.ControllerButtonEvent;

public interface Controller {

    void onControllerAxisEvent(@NonNull ControllerAxisEvent event);

    void onControllerButtonEvent(@NonNull ControllerButtonEvent event);
}
