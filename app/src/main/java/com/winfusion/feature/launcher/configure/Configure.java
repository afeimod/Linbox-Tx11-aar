package com.winfusion.feature.launcher.configure;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.winfusion.feature.launcher.LauncherException;
import com.winfusion.feature.launcher.Profile;

public abstract class Configure {

    public abstract void configure(@NonNull Profile profile, @NonNull Context context)
            throws LauncherException;

    @StringRes
    public abstract int stageResId();
}
