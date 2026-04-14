package com.winfusion.core.desktop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Linux 的 .desktop 文件类。
 */
public class Desktop {

    private final String name;
    private final String exec;
    private final String iconName;

    public Desktop(@NonNull String name, @NonNull String exec, @Nullable String iconName) {
        this.name = name;
        this.exec = exec;
        this.iconName = iconName;
    }

    /**
     * 获取 .desktop 文件的名称，对应 Name 部分。
     *
     * @return 名称
     */
    @NonNull
    public String getName() {
        return name;
    }

    /**
     * 获取 .desktop 文件的执行命令，对应 Exec 部分。
     *
     * @return 执行命令
     */
    @NonNull
    public String getExec() {
        return exec;
    }

    /**
     * 获取 .desktop 文件的图标名称，对应 Icon 部分。
     *
     * @return 如果存在则返回图标名称，否则返回 null
     */
    @Nullable
    public String getIconName() {
        return iconName;
    }
}
