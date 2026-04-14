package com.winfusion.core.box64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;

/**
 * 环境变量组类，表示一个包含多个环境变量的组。
 */
public class EnvGroup implements Comparable<EnvGroup> {

    private String name = "";
    private String comment = "";
    private final TreeSet<EnvItem> envItemSet = new TreeSet<>();

    /**
     * 构造函数，创建一个空的组。
     */
    public EnvGroup() {

    }

    /**
     * 构造函数，克隆对象。
     *
     * @param target 目标对象
     */
    public EnvGroup(@NonNull EnvGroup target) {
        name = target.name;
        comment = target.comment;
        for (EnvItem item : target.envItemSet)
            envItemSet.add(new EnvItem(item));
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setComment(@Nullable String comment) {
        this.comment = comment == null ? "" : comment;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getComment() {
        return comment;
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

    /**
     * 返回该组的所有环境变量的列表。
     *
     * @return 环境变量列表
     */
    @NonNull
    public List<EnvItem> getEnvItems() {
        return new ArrayList<>(envItemSet);
    }

    @Override
    public int compareTo(EnvGroup o) {
        return name.compareTo(o.name);
    }
}
