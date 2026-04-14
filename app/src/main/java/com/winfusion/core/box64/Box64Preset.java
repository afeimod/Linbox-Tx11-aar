package com.winfusion.core.box64;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public class Box64Preset {

    private String name = "";
    private boolean builtin;
    private final TreeSet<EnvItem> envItemSet = new TreeSet<>();

    public Box64Preset() {

    }

    public Box64Preset(@NonNull Box64Preset target) {
        name = target.name;
        builtin = target.builtin;
        for (EnvItem item : target.envItemSet)
            envItemSet.add(new EnvItem(item));
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setBuiltin(boolean builtin) {
        this.builtin = builtin;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public boolean isBuiltin() {
        return builtin;
    }

    public boolean addEnvItem(@NonNull EnvItem envItem) {
        return envItemSet.add(envItem);
    }

    public boolean removeEnvItem(@NonNull String key) {
        for (EnvItem item : envItemSet) {
            if (Objects.equals(item.getKey(), key))
                return envItemSet.remove(item);
        }
        return false;
    }

    public void clear() {
        envItemSet.clear();
    }

    @NonNull
    public List<EnvItem> getEnvItems() {
        return new ArrayList<>(envItemSet);
    }
}
