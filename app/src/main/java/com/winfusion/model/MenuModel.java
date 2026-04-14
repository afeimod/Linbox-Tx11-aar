package com.winfusion.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public class MenuModel {

    private final int iconId;
    private final int titleId;
    private final boolean enabled;
    private final Runnable runnable;

    public MenuModel(@DrawableRes int iconId, @StringRes int titleId, boolean enabled,
                     @Nullable Runnable runnable) {

        this.iconId = iconId;
        this.titleId = titleId;
        this.enabled = enabled;
        this.runnable = runnable;
    }

    @DrawableRes
    public int getIconId() {
        return iconId;
    }

    @StringRes
    public int getTitleId() {
        return titleId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Nullable
    public Runnable getRunnable() {
        return runnable;
    }
}
