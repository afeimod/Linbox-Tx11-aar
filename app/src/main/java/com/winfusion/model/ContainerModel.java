package com.winfusion.model;

import androidx.annotation.NonNull;

import com.winfusion.feature.manager.Container;
import com.winfusion.feature.setting.key.SettingWrapper;

public class ContainerModel implements Comparable<ContainerModel> {

    private final String name;
    private final Container container;

    public ContainerModel(@NonNull Container container) {
        name =  new SettingWrapper(container.getConfig()).getContainerInfoName();
        this.container = container;
    }

    @NonNull
    public Container getContainer() {
        return container;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Override
    public int compareTo(ContainerModel o) {
        return name.compareTo(o.name);
    }
}
