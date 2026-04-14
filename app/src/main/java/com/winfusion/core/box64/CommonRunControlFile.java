package com.winfusion.core.box64;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

public abstract class CommonRunControlFile implements Comparable<CommonRunControlFile> {

    private String name = "";
    private boolean builtin;
    private final TreeSet<EnvGroup> envGroupSet = new TreeSet<>();

    protected CommonRunControlFile() {

    }

    protected CommonRunControlFile(@NonNull CommonRunControlFile target) {
        name = target.name;
        builtin = target.builtin;
        for (EnvGroup group : target.envGroupSet)
            envGroupSet.add(new EnvGroup(group));
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

    public boolean addEnvGroup(@NonNull EnvGroup envGroup) {
        return envGroupSet.add(envGroup);
    }

    public boolean removeEnvGroup(@NonNull String groupName) {
        for (EnvGroup group : envGroupSet) {
            if (Objects.equals(group.getName(), groupName))
                return envGroupSet.remove(group);
        }
        return false;
    }

    @NonNull
    public List<EnvGroup> getEnvGroups() {
        return new ArrayList<>(envGroupSet);
    }

    @Override
    public int compareTo(CommonRunControlFile o) {
        return name.compareTo(o.name);
    }
}
