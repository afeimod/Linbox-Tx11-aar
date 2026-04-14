package com.winfusion.feature.launcher.configure;

import android.content.Context;

import androidx.annotation.NonNull;

import com.winfusion.R;
import com.winfusion.feature.launcher.LauncherException;
import com.winfusion.feature.launcher.Profile;

public class SoundfontConfigure extends Configure {

    @Override
    public void configure(@NonNull Profile profile, @NonNull Context context) throws LauncherException {

    }

    @Override
    public int stageResId() {
        return R.string.soundfont;
    }
}
