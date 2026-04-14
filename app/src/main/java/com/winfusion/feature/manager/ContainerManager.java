package com.winfusion.feature.manager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.application.WinfusionApplication;
import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.JsonConfig;
import com.winfusion.feature.setting.exception.BadConfigFileFormatException;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.utils.FileUtils;
import com.winfusion.utils.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

/**
 * 容器管理器类，用于提供容器的管理功能，是静态单例的。
 */
public final class ContainerManager {

    public static final String CONTAINERS_DIR = "containers";
    public static final String CONTAINER_BASENAME = "container";
    public static final String DATA_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TAG = "ContainerManager";
    private static ContainerManager INSTANCE;

    private final HashMap<String, Container> containerMap = new HashMap<>();
    private final Path containersDir;

    private ContainerManager() {
        containersDir = WinfusionApplication.getInstance().getFilesDir().toPath().resolve(CONTAINERS_DIR);
        try {
            FileUtils.checkDirectory(containersDir);
        } catch (IOException e) {
            Log.e(TAG, "Failed to create containers dir.", e);
        }
    }

    /**
     * 获取 {@link ContainerManager} 的实例。
     *
     * @return 实例
     */
    public static ContainerManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ContainerManager();
        return INSTANCE;
    }

    /**
     * 刷新缓存的容器对象。
     */
    public void refreshContainers() {
        containerMap.clear();
        try {
            FileUtils.listPaths(containersDir, path -> {
                if (!Files.isDirectory(path))
                    return;
                Container container = buildContainerByHome(path);
                if (container == null)
                    return;
                containerMap.put(container.getUUID(), container);
                checkContainerDirs(container);
            });
        } catch (IOException e) {
            Log.e(TAG, "Failed to refresh containers", e);
        }
    }

    /**
     * 获取 UUID 对应的容器对象。
     * 使用该方法前请至少执行一次 {@link #refreshContainers()} 来创建对象缓存。
     *
     * @param uuid 唯一标识符
     * @return 如果存在对应的唯一标识符，则返回对应的容器对象，否则返回 null
     */
    @Nullable
    public Container getContainerByUUID(@NonNull String uuid) {
        return containerMap.get(uuid);
    }

    /**
     * 获取全部的容器对象的集合。
     * 使用该方法前请至少执行一次 {@link #refreshContainers()} 来创建对象缓存。
     *
     * @return 容器集合
     */
    @NonNull
    public Collection<Container> getContainers() {
        return containerMap.values();
    }

    /**
     * 删除一个容器。
     *
     * @param container 容器对象
     */
    public void deleteContainer(@NonNull Container container) {
        Path home = container.getHomeDir();
        if (!Files.isDirectory(home))
            return;
        try {
            FileUtils.deleteDirectories(home);
            containerMap.remove(container.getUUID());
        } catch (IOException e) {
            Log.e(TAG, "Failed to delete container.", e);
        }
    }

    /**
     * 创建新容器，并返回容器对象。
     *
     * @param containerName 容器名称
     * @return 容器对象
     * @throws IllegalStateException 如果创建容器的文件夹或配置文件失败。
     */
    @NonNull
    public Container createContainer(@NonNull String containerName) {
        Path home = FileUtils.getNextAvailableChildPathWithIndex(containersDir, CONTAINER_BASENAME,
                null);
        JsonConfig config = new JsonConfig();
        Config.Source global = config.getGlobal();
        String uuid = UUID.randomUUID().toString();

        global.loadEmpty();
        SettingWrapper wrapper = new SettingWrapper(config);
        wrapper.setContainerInfoName(containerName);
        wrapper.setContainerInfoRegion(SystemUtils.getRegion());
        wrapper.setContainerInfoCreatedTime(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern(DATA_FORMAT)));
        wrapper.setContainerInfoUUID(uuid);
        wrapper.setContainerInfoWineVersionAtCreation("");

        try {
            Files.createDirectories(home);
            global.save(home.resolve(Container.PROFILE_JSON));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create container.", e);
        }

        return new Container(uuid, home);
    }

    /**
     * 从容器主目录构建容器对象。
     *
     * @param home 容器主目录路径
     * @return 如果成功则返回容器对象，否则返回 null
     */
    @Nullable
    private Container buildContainerByHome(@NonNull Path home) {
        Path profile = home.resolve(Container.PROFILE_JSON);
        if (!Files.isRegularFile(profile))
            return null;
        Config config = new JsonConfig();
        try {
            config.getGlobal().load(profile);
        } catch (IOException | BadConfigFileFormatException e) {
            Log.e(TAG, "Failed to load config", e);
            return null;
        }
        SettingWrapper wrapper = new SettingWrapper(config);
        try {
            return new Container(wrapper.getContainerInfoUUID(), home);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to read uuid from config", e);
            return null;
        }
    }

    /**
     * 检查一个容器的目录结构，并创建缺少的子目录。
     *
     * @param container 容器对象
     */
    private void checkContainerDirs(@NonNull Container container) {
        try {
            FileUtils.checkDirectory(container.getUserHomeDir());
            FileUtils.checkDirectory(container.getIconDir());
            FileUtils.checkDirectory(container.getShortcutDir());
            FileUtils.checkDirectory(container.getWinePrefixDir());
        } catch (IOException e) {
            Log.e(TAG, "Failed to create container dirs.", e);
        }
    }
}
