package com.winfusion.feature.input.interfaces;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.event.MouseButtonEvent;
import com.winfusion.feature.input.event.MousePointerEvent;
import com.winfusion.feature.input.event.MouseScrollEvent;

public interface Mouse {

    void onMousePointerEvent(@NonNull MousePointerEvent event);

    void onMouseButtonEvent(@NonNull MouseButtonEvent event);

    void onMouseScrollEvent(@NonNull MouseScrollEvent event);
}
