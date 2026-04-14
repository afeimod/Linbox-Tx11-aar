package com.winfusion.feature.launcher.configure;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.winfusion.R;
import com.winfusion.core.compression.ArchiveType;
import com.winfusion.core.compression.TarCompressor;
import com.winfusion.core.compression.exception.CompressorException;
import com.winfusion.feature.launcher.Constants;
import com.winfusion.feature.launcher.LauncherException;
import com.winfusion.feature.launcher.Profile;
import com.winfusion.feature.manager.Rootfs;
import com.winfusion.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;

public class RootfsConfigure extends Configure {

    @Override
    public void configure(@NonNull Profile profile, @NonNull Context context) throws LauncherException {
        installRootfsIfNeeded(profile, context);
        setupEnv(profile);
    }

    @Override
    public int stageResId() {
        return R.string.rootfs;
    }

    private void installRootfsIfNeeded(@NonNull Profile profile, @NonNull Context context)
            throws LauncherException {

        SharedPreferences sp = context.getSharedPreferences(Constants.CONFIG_NAME, MODE_PRIVATE);
        Rootfs rootfs = profile.getRootfs();
        if (!Objects.equals(sp.getString(Constants.KEY_BUILTIN_ROOTFS_VERSION, null),
                Constants.BUILTIN_ROOTFS_VERSION) ||
                !Files.isDirectory(rootfs.getRootfsDir())) {
            try (InputStream inStream = context.getAssets().open(Constants.ROOTFS_PACKAGE_NAME)) {
                if (Files.isDirectory(rootfs.getRootfsDir()))
                    FileUtils.deleteDirectories(rootfs.getRootfsDir());
                TarCompressor.extract(ArchiveType.TAR_XZ, inStream, profile.getFilesDir(), null);
            } catch (Exception e) {
                throw new LauncherException(e);
            }

            SharedPreferences.Editor editor = sp.edit();
            editor.putString(Constants.KEY_BUILTIN_ROOTFS_VERSION, Constants.BUILTIN_ROOTFS_VERSION);
            editor.apply();
        }
    }

    private void setupEnv(@NonNull Profile profile) {
        Map<String, String> env = profile.getEnv();
        Rootfs rootfs = profile.getRootfs();

        env.put("HOME", profile.getContainer().getUserHomeDir().toString());
        env.put("USER", Constants.USER_NAME);
        env.put("LD_LIBRARY_PATH", rootfs.getRootfsDir().resolve("usr/lib").toString());
        // for fontconfig
        env.put("FONTCONFIG_PATH", rootfs.getRootfsDir().resolve("usr/etc/fonts").toString());
        env.put("FONTCONFIG_FILE", rootfs.getRootfsDir().resolve("usr/etc/fonts/fonts.conf").toString());
        env.put("FONTCONFIG_CACHE", rootfs.getRootfsDir().resolve("usr/var/cache/fontconfig").toString());
    }
}
