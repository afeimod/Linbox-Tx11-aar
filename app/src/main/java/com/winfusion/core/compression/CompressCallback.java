package com.winfusion.core.compression;

import androidx.annotation.Nullable;

public interface CompressCallback {

    void onProgress(@Nullable String source, @Nullable String dest, long totalSize, long currentRead);
}
