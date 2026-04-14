package com.winfusion.feature.manager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.feature.input.overlay.OverlayProfile;
import com.winfusion.feature.input.overlay.utils.OverlayExporter;
import com.winfusion.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 覆盖层类，持有覆盖层配置文件的对象，用于管理配置文件。
 */
public final class Overlay {

    private final OverlayProfile profile;
    private final String fileName;
    private final boolean builtin;

    /**
     * 构造函数。
     * 如果 builtin 为 false，则 fileName 应该是 overlay-x.json，否则应该是 default-x.json。
     *
     * @param profile  配置文件
     * @param fileName 文件名称
     * @param builtin  是否是内置的
     */
    Overlay(@NonNull OverlayProfile profile, @NonNull String fileName, boolean builtin) {
        this.profile = profile;
        this.fileName = fileName;
        this.builtin = builtin;
    }

    /**
     * 获取覆盖层配置文件对象。
     *
     * @return 配置文件对象。
     */
    @NonNull
    public OverlayProfile getProfile() {
        return profile;
    }

    /**
     * 获取覆盖层配置文件名称。
     *
     * @return 配置文件名称
     */
    @NonNull
    public String getFileName() {
        return fileName;
    }

    /**
     * 判断覆盖层是否是内置的，
     *
     * @return 如果是内置的则返回 true，否则返回 false
     */
    public boolean isBuiltin() {
        return builtin;
    }

    /**
     * 导出配置文件到外部存储。
     *
     * @return 导出的文件路径
     */
    @Nullable
    public Path export() {
        Path target = FileUtils.getStoragePath("overlay", "json");
        try {
            OverlayExporter.save(profile, target);
            return target;
        } catch (IOException e) {
            return null;
        }
    }
}
