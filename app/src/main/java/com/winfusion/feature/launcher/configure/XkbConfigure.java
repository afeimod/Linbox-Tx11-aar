package com.winfusion.feature.launcher.configure;

import android.content.Context;

import androidx.annotation.NonNull;

import com.winfusion.R;
import com.winfusion.core.compression.ArchiveType;
import com.winfusion.core.compression.TarCompressor;
import com.winfusion.core.compression.exception.CompressorException;
import com.winfusion.feature.launcher.Constants;
import com.winfusion.feature.launcher.LauncherException;
import com.winfusion.feature.launcher.Profile;
import com.winfusion.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class XkbConfigure extends Configure {

    @Override
    public void configure(@NonNull Profile profile, @NonNull Context context) throws LauncherException {
        installXkbIfNeeded(profile, context);
        setupEnv(profile);
    }

    @Override
    public int stageResId() {
        return R.string.xkb;
    }

    private void installXkbIfNeeded(@NonNull Profile profile, @NonNull Context context)
            throws LauncherException {

        Path xkbDir = profile.getRootfs().getXkbDir();
        if (Files.isDirectory(xkbDir) && !FileUtils.isDirectoryEmpty(xkbDir))
            return;

        try (InputStream inputStream = context.getAssets().open(Constants.XKB_PACKAGE_NAME)) {
            TarCompressor.extract(ArchiveType.TAR_XZ, inputStream, profile.getFilesDir(), null);
        } catch (IOException | CompressorException e) {
            throw new LauncherException(e);
        }
    }

    private void setupEnv(@NonNull Profile profile) {
        profile.getEnv().put("XDG_CONFIG_ROOT", profile.getRootfs().getXkbDir().toString());
    }
}
