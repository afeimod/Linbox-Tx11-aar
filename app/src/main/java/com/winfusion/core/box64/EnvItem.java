package com.winfusion.core.box64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 环境变量类。
 * 包含键，值和注释等成员变量。
 */
public class EnvItem implements Comparable<EnvItem> {

    private String key;
    private String value;
    private String comment = "";

    /**
     * 构造函数。
     *
     * @param key   键
     * @param value 值
     */
    public EnvItem(@NonNull String key, @NonNull String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 构造函数，克隆对象。
     *
     * @param target 目标对象
     */
    public EnvItem(@NonNull EnvItem target) {
        key = target.key;
        value = target.value;
        comment = target.comment;
    }

    public void setKey(@NonNull String key) {
        this.key = key;
    }

    public void setValue(@NonNull String value) {
        this.value = value;
    }

    public void setComment(@Nullable String comment) {
        this.comment = comment == null ? "" : comment;
    }

    @NonNull
    public String getKey() {
        return key;
    }

    @NonNull
    public String getValue() {
        return value;
    }

    @NonNull
    public String getComment() {
        return comment;
    }

    @Override
    public int compareTo(EnvItem o) {
        return key.compareTo(o.key);
    }
}
