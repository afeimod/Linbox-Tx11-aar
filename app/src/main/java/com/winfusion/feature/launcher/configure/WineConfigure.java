package com.winfusion.feature.launcher.configure;

import static com.winfusion.feature.launcher.Constants.BUILTIN_WINE_VERSION;

import android.content.Context;

import androidx.annotation.NonNull;

import com.winfusion.R;
import com.winfusion.core.shell.ShellExecutor;
import com.winfusion.core.wfp.Wfp;
import com.winfusion.feature.launcher.Constants;
import com.winfusion.feature.launcher.LauncherException;
import com.winfusion.feature.launcher.Profile;
import com.winfusion.feature.launcher.Utils;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class WineConfigure extends Configure {

    @Override
    public void configure(@NonNull Profile profile, @NonNull Context context) throws LauncherException {
        setupWineBinaryPath(profile, context);
        setupEnv(profile);
        setupWinePrefixIfNeeded(profile);
    }

    @Override
    public int stageResId() {
        return R.string.wine;
    }

    private void setupWineBinaryPath(@NonNull Profile profile, @NonNull Context context)
            throws LauncherException {

        String wineVersion = profile.getSettingWrapper().getContainerWineVersion();
        boolean fallbackBuiltin = false;
        boolean useBuiltin = false;
        if (wineVersion.isEmpty()) {
            fallbackBuiltin = true;
            wineVersion = BUILTIN_WINE_VERSION;
        } else if (Objects.equals(wineVersion, BUILTIN_WINE_VERSION)) {
            useBuiltin = true;
        }

        Wfp wfp = profile.getWfpContent(wineVersion);
        if (wfp == null) {
            if (fallbackBuiltin || useBuiltin) {
                Utils.installBuiltinWfp(Constants.WINE_PACKAGE_NAME, context);
                profile.refreshWfpContents();
                wfp = Objects.requireNonNull(profile.getWfpContent(wineVersion));
            } else {
                throw new LauncherException("Wine is not found: " + wineVersion);
            }
        }

        if (fallbackBuiltin)
            profile.getSettingWrapper().setContainerWineVersion(BUILTIN_WINE_VERSION);

        Path wineBinaryPath = Paths.get(wfp.getWfpHome(), "bin/wine");
        if (!Files.isRegularFile(wineBinaryPath)) {
            wineBinaryPath = Paths.get(wfp.getWfpHome(), "lib/wine/x86_64-unix/wine");
            if (!Files.isRegularFile(wineBinaryPath))
                throw new LauncherException("Wine binary is not found: " + wineVersion);
        }
        profile.setWineBinaryPath(wineBinaryPath);

        profile.getSettingWrapper().setContainerInstalledWine(wineVersion);
        profile.saveConfig();
    }

    private void setupEnv(@NonNull Profile profile) {
        profile.getEnv().put("WINEPREFIX", profile.getContainer().getWinePrefixDir().toString());
    }

    private void setupWinePrefixIfNeeded(@NonNull Profile profile) throws LauncherException {
        Path prefixPath = profile.getContainer().getWinePrefixDir();
        if (Files.exists(prefixPath) && !FileUtils.isDirectoryEmpty(prefixPath))
            return;

        if (profile.getBox64BinaryPath() == null)
            throw new LauncherException("Box64 must be configured before wine.");

        try {
            new ShellExecutor()
                    .putEnv(profile.getEnv())
                    .setCommand(
                            profile.getBox64BinaryPath().toString(),
                            Objects.requireNonNull(profile.getWineBinaryPath()).toString(),
                            "wineboot"
                    )
                    .exec()
                    .waitFor();
        } catch (IOException | InterruptedException e) {
            throw new LauncherException(e);
        }

        SettingWrapper wrapper = profile.getSettingWrapper();
        wrapper.setContainerInfoWineVersionAtCreation(wrapper.getContainerWineVersion());
        profile.saveConfig();
    }
}
