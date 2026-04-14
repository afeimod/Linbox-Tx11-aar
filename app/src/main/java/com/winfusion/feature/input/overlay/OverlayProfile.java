package com.winfusion.feature.input.overlay;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.overlay.widget.BaseWidget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OverlayProfile {

    public static final int VERSION_1 = 1;
    public static final int CURRENT_VERSION = VERSION_1;

    private String name = "null";
    private final List<BaseWidget.Config> configs = new ArrayList<>();

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setConfigs(@NonNull Collection<BaseWidget.Config> configs) {
        this.configs.clear();
        this.configs.addAll(configs);
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public Collection<BaseWidget.Config> getConfigs() {
        return Collections.unmodifiableList(configs);
    }
}
