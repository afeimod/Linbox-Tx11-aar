package com.winfusion.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.feature.setting.SettingsFragment;
import com.winfusion.feature.setting.provider.ContainerInfoProvider;
import com.winfusion.feature.setting.provider.SettingsProvider;
import com.winfusion.utils.LaunchMode;

public class ContainerInfoFragment extends SettingsFragment {

    private ContainerInfoFragmentArgs args;
    private SettingsProvider provider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        args = ContainerInfoFragmentArgs.fromBundle(getArguments());
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    protected SettingsProvider getProvider() {
        if (provider == null)
            provider = new ContainerInfoProvider(getConfig());
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
        return LaunchMode.Container;
    }

    @Override
    protected boolean isSaveOnDetached() {
        return args.getSaveConfigOnDetach();
    }
}
