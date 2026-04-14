package com.winfusion.feature.manager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.registry.exporter.WineRegExporter;
import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.JsonConfig;
import com.winfusion.feature.setting.exception.BadConfigFileFormatException;
import com.winfusion.feature.setting.key.SettingKeys;
import com.winfusion.feature.setting.value.ConfigElement;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * 容器类，用于表示一个容器实例。
 */
public class Container {

    public static final String USER_HOME_DIR = "home";
    public static final String WINE_PREFIX_DIR = "prefix";
    public static final String SHORTCUT_DIR = "shortcuts";
    public static final String ICON_DIR = "icons";
    public static final String PROFILE_JSON = "profile.json";
    public static final String REG_SYSTEM = "system.reg";
    public static final String REG_USER = "user.reg";
    public static final String REG_USER_DEF = "userdef.reg";
    static Config.Source defaultSource;

    private final String uuid;
    private final Path home;
    private final Path profile;
    private Config config;

    Container(@NonNull String uuid, @NonNull Path home) {
        this.uuid = uuid;
        this.home = home;
        profile = home.resolve(PROFILE_JSON);
    }

    /**
     * 获取容器的唯一标识符。
     *
     * @return 唯一标识符
     */
    @NonNull
    public String getUUID() {
        return uuid;
    }

    /**
     * 获取容器的主目录。
     *
     * @return 主目录路径
     */
    @NonNull
    public Path getHomeDir() {
        return home;
    }

    /**
     * 获取容器的用户主目录。
     *
     * @return 用户主目录路径
     */
    @NonNull
    public Path getUserHomeDir() {
        return home.resolve(USER_HOME_DIR);
    }

    /**
     * 获取容器的 Wine 前缀目录。
     *
     * @return Wine 前缀路径
     */
    @NonNull
    public Path getWinePrefixDir() {
        return home.resolve(WINE_PREFIX_DIR);
    }

    /**
     * 获取容器的快捷方式目录。
     *
     * @return 快捷方式目录路径
     */
    @NonNull
    public Path getShortcutDir() {
        return home.resolve(SHORTCUT_DIR);
    }

    /**
     * 获取容器的图标目录。
     *
     * @return 图标目录路径
     */
    @NonNull
    public Path getIconDir() {
        return home.resolve(ICON_DIR);
    }

    /**
     * 获取容器的配置文件路径。
     *
     * @return 配置文件路径
     */
    @NonNull
    public Path getProfilePath() {
        return profile;
    }

    /**
     * 获取快照的 wine 前缀中的 desktop 文件夹路径。
     *
     * @return desktop 文件夹路径
     */
    @NonNull
    public Path getDesktopDir() {
        return getWinePrefixDir().resolve("drive_c/users")
                .resolve(Rootfs.INSTANCE.getUserName())
                .resolve("Desktop");
    }

    /**
     * 获取容器 Wine 的注册表文件路径。
     *
     * @param type 注册表类型
     * @return 注册表文件路径
     */
    @NonNull
    public Path getRegistryPath(@NonNull WineRegExporter.RegType type) {
        return getWinePrefixDir().resolve(switch (type) {
            case System -> REG_SYSTEM;
            case User -> REG_USER;
            case UserDef -> REG_USER_DEF;
        });
    }

    /**
     * 获取本地图标缓存目录。
     *
     * @param resolution 图标分辨率
     * @return 图标缓存目录
     */
    @NonNull
    public Path getLocalIconDir(int resolution) {
        return getUserHomeDir().resolve(".local/share/icons/hicolor")
                .resolve(resolution + "x" + resolution)
                .resolve("apps");
    }

    /**
     * 获取容器的配置。
     * 如果容器的配置未加载，则会先加载配置。
     *
     * @return 配置文件对象
     * @throws IllegalStateException 如果加载配置文件时遇到 {@link IOException} 或者 {@link BadConfigFileFormatException}
     */
    @NonNull
    public synchronized Config getConfig() {
        if (config == null) {
            config = new JsonConfig();
            // TODO: 加载Default配置
            try {
                config.getGlobal().load(home.resolve(PROFILE_JSON));
            } catch (IOException | BadConfigFileFormatException e) {
                throw new IllegalStateException("Failed to load config: " + uuid, e);
            }
            if (defaultSource == null)
                defaultSource = new DefaultSource();
            config.setDefault(defaultSource);
        }
        return config;
    }

    /**
     * 保存容器配置到文件。
     * 如果配置未打开，则不会保存。
     *
     * @throws IllegalStateException 如果保存配置文件时遇到 {@link IOException}
     */
    public synchronized void saveConfig() {
        if (config == null)
            return;
        try {
            config.getGlobal().save(home.resolve(PROFILE_JSON));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save config: " + uuid, e);
        }
    }

    private static class DefaultSource implements Config.Source {

        private final HashMap<String, ConfigElement> configMap = new HashMap<>();

        public DefaultSource() {
            for (SettingKeys key : SettingKeys.values()) {
                ConfigElement element = key.getDefaultValue();
                if (element == null)
                    continue;
                configMap.put(key.key(), element);
            }
        }

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public void loadEmpty() {
            throw new UnsupportedOperationException("Not support load empty.");
        }

        @Override
        public void load(@NonNull Path path) {
            throw new UnsupportedOperationException("Not support load.");
        }

        @Override
        public void save(@NonNull Path path) {
            throw new UnsupportedOperationException("Not support save.");
        }

        @Override
        public boolean has(@NonNull String key) {
            return configMap.containsKey(key);
        }

        @Nullable
        @Override
        public ConfigElement get(@NonNull String key) {
            return configMap.get(key);
        }

        @Override
        public void set(@NonNull String key, @NonNull ConfigElement element) {
            throw new UnsupportedOperationException("Not support set.");
        }

        @Override
        public void remove(@NonNull String key) {
            throw new UnsupportedOperationException("Not support remove.");
        }
    }
}
