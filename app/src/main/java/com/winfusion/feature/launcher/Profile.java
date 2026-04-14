package com.winfusion.feature.launcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.wfp.Wfp;
import com.winfusion.feature.content.model.BaseContentModel;
import com.winfusion.feature.content.model.SoundfontModel;
import com.winfusion.feature.content.model.WfpModel;
import com.winfusion.feature.manager.Container;
import com.winfusion.feature.manager.ContainerManager;
import com.winfusion.feature.manager.ContentManager;
import com.winfusion.feature.manager.Rootfs;
import com.winfusion.feature.manager.Shortcut;
import com.winfusion.feature.manager.ShortcutManager;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.utils.LaunchMode;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 启动器的配置类，用于存储启动器在配置阶段的信息。
 */
public class Profile {

    private final LaunchMode mode;
    private final HashMap<String, String> env = new HashMap<>();
    private final HashMap<String, Wfp> wfpMap = new HashMap<>();
    private final HashSet<String> sfSet = new HashSet<>();
    private final SettingWrapper settingWrapper;
    private final Rootfs rootfs = Rootfs.INSTANCE;
    private Container container;
    private Shortcut shortcut;
    private Path wineBinaryPath;
    private Path box64BinaryPath;
    private Path soundfontPath;

    public Profile(@NonNull LaunchMode mode, @NonNull String uuid, @NonNull Path filesDir) {

        this.mode = mode;
        switch (mode) {
            case Container -> {
                container = ContainerManager.getInstance().getContainerByUUID(uuid);
                if (container == null)
                    throw new IllegalArgumentException("Invalid container: " + uuid);
                settingWrapper = new SettingWrapper(container.getConfig());
            }
            case Shortcut -> {
                shortcut = ShortcutManager.getInstance().getShortcutByUUID(uuid);
                if (shortcut == null)
                    throw new IllegalArgumentException("Invalid shortcut: " + uuid);
                settingWrapper = new SettingWrapper(shortcut.getConfig());
            }
            default -> {
                settingWrapper = null;
                throw new IllegalArgumentException("Unsupported launch mode: " + mode.name());
            }
        }
        refreshWfpContents();
    }

    /**
     * 获取启动器模式。
     * 可能是从容器启动或从快照启动。
     *
     * @return 启动器模式
     */
    @NonNull
    public LaunchMode getMode() {
        return mode;
    }

    /**
     * 获取容器对象。
     *
     * @return 如果启动模式为容器，则返回容器对象，否则返回快照对象持有的容器对象
     */
    @NonNull
    public Container getContainer() {
        return switch (mode) {
            case Container -> container;
            case Shortcut -> shortcut.getContainer();
            case Standalone -> throw new IllegalArgumentException("Not support standalone.");
        };
    }

    /**
     * 获取快照对象。
     *
     * @return 如果启动模式为快照模式，则返回快照对象，否则返回 null
     */
    @Nullable
    public Shortcut getShortcut() {
        return shortcut;
    }

    /**
     * 获取根文件系统对象。
     *
     * @return 根文件系统对象
     */
    @NonNull
    public Rootfs getRootfs() {
        return rootfs;
    }

    /**
     * 获取当前的环境变量。
     *
     * @return 环境变量
     */
    @NonNull
    public Map<String, String> getEnv() {
        return env;
    }

    /**
     * 获取 files 文件夹路径。
     *
     * @return files 路径
     */
    @NonNull
    public Path getFilesDir() {
        return Rootfs.INSTANCE.getFilesDir();
    }

    /**
     * 获取一个 wfp 对象。
     *
     * @param id wfp 对象的 id
     * @return 如果 id 对应的 wfp 对象存在则返回对象，否则返回 null
     */
    @Nullable
    public Wfp getWfpContent(@NonNull String id) {
        return wfpMap.get(id);
    }

    /**
     * 判断音乐字体文件是否可用。
     *
     * @param filename 音乐字体文件名
     * @return 如果可用则返回 true，否则返回 false
     */
    public boolean isSoundfontAvailable(@NonNull String filename) {
        return sfSet.contains(filename);
    }

    /**
     * 设置 Wine 可执行文件路径。
     *
     * @param path Wine 可执行文件路径
     */
    public void setWineBinaryPath(@NonNull Path path) {
        wineBinaryPath = path;
    }

    /**
     * 获取 Wine 可执行文件路径。
     *
     * @return Wine 可执行文件路径
     */
    @Nullable
    public Path getWineBinaryPath() {
        return wineBinaryPath;
    }

    /**
     * 设置 Box64 可执行文件路径
     *
     * @param path Box64 可执行文件路径
     */
    public void setBox64BinaryPath(@NonNull Path path) {
        box64BinaryPath = path;
    }

    /**
     * 获取 Box64 的可执行文件路径。
     *
     * @return Box64 的可执行文件路径
     */
    @Nullable
    public Path getBox64BinaryPath() {
        return box64BinaryPath;
    }

    /**
     * 设置音乐字体文件的路径。
     *
     * @param path 音乐字体文件路径
     */
    public void setSoundfontPath(@NonNull Path path) {
        soundfontPath = path;
    }

    /**
     * 获取音乐字体文件的路径。
     *
     * @return 音乐字体路径
     */
    @Nullable
    public Path getSoundfontPath() {
        return soundfontPath;
    }

    /**
     * 刷新全部的 Wfp 缓存。
     */
    public void refreshWfpContents() {
        wfpMap.clear();
        List<BaseContentModel> models = ContentManager.getInstance().generateModels();
        for (BaseContentModel model : models) {
            if (model instanceof WfpModel wfpModel) {
                Wfp wfp = wfpModel.getWfp();
                wfpMap.put(wfp.toIdentifier(), wfp);
            } else if (model instanceof SoundfontModel soundfontModel) {
                sfSet.add(soundfontModel.getFileName());
            }
        }
    }

    /**
     * 获取设置包装器。
     *
     * @return 设置包装器
     */
    @NonNull
    public SettingWrapper getSettingWrapper() {
        return settingWrapper;
    }

    /**
     * 保存配置到文件。
     */
    public void saveConfig() {
        switch (mode) {
            case Container -> container.saveConfig();
            case Shortcut -> shortcut.saveConfig();
            case Standalone -> throw new IllegalArgumentException("Not support standalone.");
        }
    }
}
