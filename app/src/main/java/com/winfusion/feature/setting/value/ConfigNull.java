package com.winfusion.feature.setting.value;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 空元素类。
 */
public class ConfigNull extends ConfigElement {

    public static final ConfigNull INSTANCE = new ConfigNull();

    @Override
    public int hashCode() {
        return ConfigNull.class.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof ConfigNull;
    }

    @NonNull
    @Override
    public String toString() {
        return "null";
    }
}
