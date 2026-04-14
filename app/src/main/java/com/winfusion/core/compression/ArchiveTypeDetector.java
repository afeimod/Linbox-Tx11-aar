package com.winfusion.core.compression;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public final class ArchiveTypeDetector {

    private ArchiveTypeDetector() {

    }

    @Nullable
    public static ArchiveType detect(@NonNull Context context, @NonNull Uri source) {
        try (InputStream stream = context.getContentResolver().openInputStream(source)) {
            if (stream == null)
                return null;

            return detect(stream);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static ArchiveType detect(@NonNull Path source) {
        try (InputStream stream = new FileInputStream(source.toFile())) {
            return detect(stream);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static ArchiveType detect(@NonNull InputStream inputStream) {
        try (BufferedInputStream in = new BufferedInputStream(inputStream)) {
            String flat = CompressorStreamFactory.detect(in);
            return switch (flat) {
                case CompressorStreamFactory.XZ -> ArchiveType.TAR_XZ;
                case CompressorStreamFactory.ZSTANDARD -> ArchiveType.TAR_ZST;
                case CompressorStreamFactory.GZIP -> ArchiveType.TAR_GZ;
                default -> null;
            };
        } catch (CompressorException | IOException e) {
            return null;
        }
    }
}
