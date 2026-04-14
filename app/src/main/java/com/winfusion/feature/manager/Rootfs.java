package com.winfusion.feature.manager;

import androidx.annotation.NonNull;

import com.winfusion.application.WinfusionApplication;

import java.nio.file.Path;

/**
 * 根文件系统类，表示一个根文件系统。
 */
public class Rootfs {

    public static final Rootfs INSTANCE = new Rootfs(WinfusionApplication.getInstance().getFilesDir().toPath());

    private final Path filesDir;

    public Rootfs(@NonNull Path filesDir) {
        this.filesDir = filesDir;
    }

    /**
     * 获取 App 的 files 目录。
     *
     * @return files 目录路径
     */
    public Path getFilesDir() {
        return filesDir;
    }

    /**
     * 获取 rootfs 目录。
     *
     * @return rootfs 目录路径
     */
    public Path getRootfsDir() {
        return filesDir.resolve("rootfs");
    }

    /**
     * 获取 tmp 目录。
     *
     * @return tmp 目录路径
     */
    public Path getTmpDir() {
        return filesDir.resolve("tmp");
    }

    /**
     * 获取 xkb 目录。
     *
     * @return xkb 目录路径
     */
    public Path getXkbDir() {
        return filesDir.resolve("xkb");
    }

    /**
     * 获取用户名。
     *
     * @return 用户名
     */
    public String getUserName() {
        return "xuser";
    }
}
