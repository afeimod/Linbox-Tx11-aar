package com.winfusion.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.feature.setting.SettingsFragment;
import com.winfusion.feature.setting.provider.ContainerSettingsProvider;
import com.winfusion.feature.setting.provider.SettingsProvider;
import com.winfusion.utils.LaunchMode;

public class ContainerSettingsFragment extends SettingsFragment {

    private ContainerSettingsFragmentArgs args;
    private SettingsProvider provider;
    private LaunchMode mode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        args = ContainerSettingsFragmentArgs.fromBundle(getArguments());
        mode = LaunchMode.valueOf(args.getMode());
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected SettingsProvider getProvider() {
        if (provider == null)
            provider = new ContainerSettingsProvider(getConfig(), mode);
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
        return mode;
    }

    @Override
    protected boolean isSaveOnDetached() {
        return args.getSaveConfigOnDetach();
    }
}
