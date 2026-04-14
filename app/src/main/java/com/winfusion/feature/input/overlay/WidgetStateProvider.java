package com.winfusion.feature.input.overlay;

import androidx.annotation.Nullable;

import com.winfusion.feature.input.overlay.widget.BaseWidget;

public interface WidgetStateProvider {

    void setSelectedWidget(@Nullable BaseWidget<?> widget);

    @Nullable
    BaseWidget<?> getSelectedWidget();
}
