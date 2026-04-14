package com.winfusion.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.registry.RegistryKey;

public class RegistryItemModel {

    private final String itemName;
    private final RegistryKey itemKey;
    private final Action action;

    public enum Action {
        OpenItem,
        GoBack
    }

    public RegistryItemModel(@NonNull String itemName, @Nullable RegistryKey itemKey,
                             @NonNull Action action) {

        this.itemName = itemName;
        this.itemKey = itemKey;
        this.action = action;
    }

    public String getItemName() {
        return itemName;
    }

    @Nullable
    public RegistryKey getItemKey() {
        return itemKey;
    }

    @NonNull
    public Action getAction() {
        return action;
    }
}
