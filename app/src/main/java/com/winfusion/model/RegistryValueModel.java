package com.winfusion.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.registry.data.RegistryData;

public class RegistryValueModel {

    private final String valueName;
    private final RegistryData valueData;
    private final String flatData;

    public RegistryValueModel(@NonNull String valueName, @Nullable RegistryData valueData) {

        this.valueName = valueName;
        this.valueData = valueData;
        this.flatData = null;
    }

    public RegistryValueModel(@NonNull String valueName, @NonNull String flatData) {
        this.valueName = valueName;
        this.valueData = null;
        this.flatData = flatData;
    }

    @NonNull
    public String getValueName() {
        return valueName;
    }

    @Nullable
    public RegistryData getValueData() {
        return valueData;
    }

    @Nullable
    public String getFlatData() {
        return flatData;
    }
}
