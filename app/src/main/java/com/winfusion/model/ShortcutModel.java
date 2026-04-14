package com.winfusion.model;

import androidx.annotation.NonNull;

import com.winfusion.feature.manager.Shortcut;
import com.winfusion.feature.setting.key.SettingWrapper;

public class ShortcutModel {

    private final String shortcutName;
    private final String containerName;
    private final Shortcut shortcut;

    public ShortcutModel(@NonNull Shortcut shortcut) {
        SettingWrapper wrapper = new SettingWrapper(shortcut.getConfig());
        shortcutName = wrapper.getShortcutInfoName();
        containerName = wrapper.getContainerInfoName();
        this.shortcut = shortcut;
    }

    @NonNull
    public Shortcut getShortcut() {
        return shortcut;
    }

    @NonNull
    public String getShortcutName() {
        return shortcutName;
    }

    @NonNull
    public String getContainerName() {
        return containerName;
    }
}
