package com.winfusion.feature.launcher.configure;

import static com.winfusion.feature.launcher.Constants.BOX64_PACKAGE_NAME;
import static com.winfusion.feature.launcher.Constants.BUILTIN_BOX64_VERSION;

import android.content.Context;

import androidx.annotation.NonNull;

import com.winfusion.R;
import com.winfusion.core.wfp.Wfp;
import com.winfusion.feature.launcher.LauncherException;
import com.winfusion.feature.launcher.Profile;
import com.winfusion.feature.launcher.Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

public class Box64Configure extends Configure {

    @Override
    public void configure(@NonNull Profile profile, @NonNull Context context) throws LauncherException {
        setupBox64BinaryPath(profile, context);
        setupEnv(profile);
    }

    @Override
    public int stageResId() {
        return R.string.box64;
    }

    private void setupBox64BinaryPath(@NonNull Profile profile, @NonNull Context context)
            throws LauncherException {

        String box64Version = profile.getSettingWrapper().getContainerBox64Version();
        boolean fallbackBuiltin = false;
        boolean useBuiltin = false;
        if (box64Version.isEmpty()) {
            fallbackBuiltin = true;
            box64Version = BUILTIN_BOX64_VERSION;
        } else if (Objects.equals(box64Version, BUILTIN_BOX64_VERSION)) {
            useBuiltin = true;
        }

        Wfp wfp = profile.getWfpContent(box64Version);
        if (wfp == null) {
            if (fallbackBuiltin || useBuiltin) {
                Utils.installBuiltinWfp(BOX64_PACKAGE_NAME, context);
                profile.refreshWfpContents();
                wfp = Objects.requireNonNull(profile.getWfpContent(box64Version));
            } else {
                throw new LauncherException("Box64 is not found: " + box64Version);
            }
        }

        if (fallbackBuiltin) {
            profile.getSettingWrapper().setContainerBox64Version(box64Version);
            profile.saveConfig();
        }

        Path box64BinaryPath = Paths.get(wfp.getWfpHome(), "box64");
        if (!Files.isRegularFile(box64BinaryPath))
            throw new LauncherException("Box64 binary is not found: " + box64Version);

        profile.setBox64BinaryPath(box64BinaryPath);
    }

    private void setupEnv(@NonNull Profile profile) {
        Map<String, String> env = profile.getEnv();
        env.put("BOX64_LD_LIBRARY_PATH", profile.getRootfs().getRootfsDir()
                .resolve("usr/lib-x86_64").toString());
        env.put("BOX64_PREFER_EMULATED", "1");
    }
}
