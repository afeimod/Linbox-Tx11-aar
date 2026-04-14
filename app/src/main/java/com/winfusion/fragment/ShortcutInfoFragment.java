package com.winfusion.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.feature.setting.SettingsFragment;
import com.winfusion.feature.setting.provider.SettingsProvider;
import com.winfusion.feature.setting.provider.ShortcutInfoProvider;
import com.winfusion.utils.LaunchMode;

public class ShortcutInfoFragment extends SettingsFragment {

    private ShortcutInfoFragmentArgs args;
    private ShortcutInfoProvider provider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        args = ShortcutInfoFragmentArgs.fromBundle(getArguments());
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected SettingsProvider getProvider() {
        if (provider == null)
            provider = new ShortcutInfoProvider(getConfig());
        return provider;
    }

    @NonNull
    @Override
    protected String getUUID() {
        return args.getUuid();
    }

    @NonNull
    @Override
    protected LaunchMode getMode() {
        return LaunchMode.Shortcut;
    }

    @Override
    protected boolean isSaveOnDetached() {
        return args.getSaveConfigOnDetach();
    }
}
