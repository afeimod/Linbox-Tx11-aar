package com.winfusion.feature.setting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.feature.setting.value.ConfigElement;

public class ConfigReader {

    private final Config config;

    public ConfigReader(@NonNull Config config) {
        this.config = config;
        if (!config.getGlobal().isLoaded())
            throw new IllegalArgumentException("Global source of config must be loaded before using");
    }

    @NonNull
    public Config getConfig() {
        return config;
    }

    @Nullable
    public ConfigElement get(@NonNull String key) {
        Config.Source source = config.getLocal().isLoaded() ? config.getLocal() : config.getGlobal();
        return source.get(key);
    }

    @NonNull
    public ConfigElement getOrDefault(@NonNull String key, @NonNull ConfigElement element) {
        ConfigElement ret = get(key);
        return ret == null ? element : ret;
    }

    @NonNull
    public ConfigElement getOrSetDefault(@NonNull String key, @NonNull ConfigElement element) {
        ConfigElement ret = get(key);
        if (ret == null) {
            set(key, element);
            ret = element;
        }
        return ret;
    }

    public void set(@NonNull String key, @NonNull ConfigElement element) {
        Config.Source source = config.getLocal().isLoaded() ? config.getLocal() : config.getGlobal();
        source.set(key, element);
    }
}
