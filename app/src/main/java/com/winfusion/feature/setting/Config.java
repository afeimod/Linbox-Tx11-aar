package com.winfusion.feature.setting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.feature.setting.exception.BadConfigFileFormatException;
import com.winfusion.feature.setting.value.ConfigElement;

import java.io.IOException;
import java.nio.file.Path;

public interface Config {

    @NonNull
    Source getDefault();

    @NonNull
    Source getGlobal();

    @NonNull
    Source getLocal();

    void setDefault(@NonNull Source source);

    void setGlobal(@NonNull Source source);

    void setLocal(@NonNull Source source);

    @NonNull
    Source get(@NonNull SourceType sourceType);

    void set(@NonNull SourceType sourceType, @NonNull Source source);

    interface Source {

        boolean isLoaded();

        void loadEmpty();

        void load(@NonNull Path path) throws IOException, BadConfigFileFormatException;

        void save(@NonNull Path path) throws IOException;

        boolean has(@NonNull String key);

        @Nullable
        ConfigElement get(@NonNull String key);

        void set(@NonNull String key, @NonNull ConfigElement element);

        void remove(@NonNull String key);
    }

    enum SourceType {
        Default,
        Global,
        Local
    }
}
