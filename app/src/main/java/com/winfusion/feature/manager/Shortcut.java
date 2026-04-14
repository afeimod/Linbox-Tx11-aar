package com.winfusion.feature.manager;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.JsonConfig;
import com.winfusion.feature.setting.exception.BadConfigFileFormatException;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 快照类，用于表示一个快照实例。
 */
public class Shortcut {

    private final Container container;
    private final String uuid;
    private final Path profile;
    private Config config;
    private IconLoader iconLoader;

    Shortcut(@NonNull Container container, @NonNull String uuid, @NonNull Path profile) {
        this.container = container;
        this.uuid = uuid;
        this.profile = profile;
    }

    /**
     * 获取快照所属的容器的对象。
     *
     * @return 容器对象
     */
    @NonNull
    public Container getContainer() {
        return container;
    }

    /**
     * 获取快照的唯一标识符。
     *
     * @return 唯一标识符
     */
    @NonNull
    public String getUUID() {
        return uuid;
    }

    /**
     * 获取快照的配置文件路径。
     *
     * @return 配置文件路径
     */
    @NonNull
    public Path getProfilePath() {
        return profile;
    }

    /**
     * 判断当前快照对象是否有效。
     *
     * @return 如果快照的 target 指向的文件存在，则返回 true，否则返回 false
     */
    public boolean isValid() {
        // TODO: 实现获取 target 的文件的 unix 路径，并判断其是否存在
        return true;
    }

    /**
     * 获取快照的图标加载器对象。
     * 如果图标加载器未创建则先创建，否则将直接返回缓存的加载器对象。
     *
     * @return 图标加载器对象
     */
    @NonNull
    public synchronized IconLoader getIconLoader() {
        if (iconLoader == null)
            iconLoader = new IconLoader(this);
        return iconLoader;
    }

    /**
     * 获取快照的配置文件。
     * 如果快照的配置文件未加载，则会先加载配置，
     *
     * @return 配置文件对象
     * @throws IllegalStateException 如果加载配置文件时遇到 {@link IOException}
     */
    @NonNull
    public synchronized Config getConfig() {
        if (config == null) {
            config = new JsonConfig();
            // TODO: 加载Default配置
            try {
                config.getLocal().load(profile);
            } catch (IOException | BadConfigFileFormatException e) {
                throw new IllegalStateException("Failed to load config: " + uuid, e);
            }
            config.setGlobal(container.getConfig().getGlobal());
            config.setDefault(container.getConfig().getDefault());
        }
        return config;
    }

    /**
     * 保存快照的配置到文件。
     * 如果配置未打开，则不会保存。
     * 该方法不会保存快照所属的容器的配置。
     *
     * @throws IllegalStateException 如果保存配置文件时遇到 {@link IOException}
     */
    public synchronized void saveConfig() {
        if (config == null)
            return;
        try {
            config.getLocal().save(profile);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save config: " + uuid, e);
        }
    }
}
