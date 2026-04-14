package com.winfusion.feature.manager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.desktop.Desktop;
import com.winfusion.core.desktop.DesktopParser;
import com.winfusion.core.desktop.exception.BadDesktopFormatException;
import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.JsonConfig;
import com.winfusion.feature.setting.exception.BadConfigFileFormatException;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 快照管理类，用于提供快照的管理功能，是静态单例的。
 */
public final class ShortcutManager {

    private static final String LNK_SUFFIX = ".lnk";
    private static final String DESKTOP_SUFFIX = ".desktop";
    private static final String SHORTCUT_BASENAME = "shortcut";
    private static final String TAG = "ShortcutManager";
    private static ShortcutManager INSTANCE;

    private final HashMap<String, Shortcut> shortcutMap = new HashMap<>();
    private final HashMap<String, Set<String>> lnkMap = new HashMap<>();

    private ShortcutManager() {

    }

    /**
     * 获取 {@link ShortcutManager} 的实例。
     *
     * @return 实例
     */
    public static ShortcutManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ShortcutManager();
        return INSTANCE;
    }

    /**
     * 刷新缓存的快照对象。
     * 这个方法同时执行了 {@link ContainerManager#refreshContainers()} 用于刷新缓存的容器对象。
     */
    public void refreshShortcuts() {
        shortcutMap.clear();
        lnkMap.clear();
        ContainerManager.getInstance().refreshContainers();
        for (Container container : ContainerManager.getInstance().getContainers()) {
            refreshByProfiles(container);
            refreshByDesktops(container);
        }
    }

    /**
     * 获取 UUID 对应的快照对象。
     * 使用该方法前请至少执行一次 {@link #refreshShortcuts()} 来创建对象缓存。
     *
     * @param uuid 唯一标识符
     * @return 如果存在该唯一标识符，则返回对应的快照对象，否则返回 null
     */
    @Nullable
    public Shortcut getShortcutByUUID(@NonNull String uuid) {
        return shortcutMap.get(uuid);
    }

    /**
     * 获取全部的快照对象的集合。
     * 使用该方法前请至少执行一次 {@link #refreshShortcuts()} ()} 来创建对象缓存。
     *
     * @return 对象集合
     */
    @NonNull
    public Collection<Shortcut> getShortcuts() {
        return shortcutMap.values();
    }

    /**
     * 删除一个快照，包括它的 profile 、lnk、 Linux Desktop 文件以及图标缓存文件。
     *
     * @param shortcut 快照对象
     */
    public void deleteShortcut(@NonNull Shortcut shortcut) {
        SettingWrapper wrapper = new SettingWrapper(shortcut.getConfig());
        String lnkName = wrapper.getShortcutInfoLnkName();

        try {
            if (!lnkName.isEmpty()) {
                FileUtils.deleteFileIfExist(shortcut.getContainer().getDesktopDir()
                        .resolve(lnkName.replace(DESKTOP_SUFFIX, LNK_SUFFIX)));
                FileUtils.deleteFileIfExist(shortcut.getContainer().getDesktopDir().resolve(lnkName));
            }
            FileUtils.deleteFileIfExist(shortcut.getProfilePath());
            FileUtils.deleteFileIfExist(shortcut.getIconLoader().getIconCacheFile());
        } catch (IOException e) {
            Log.e(TAG, "Failed to delete shortcut.", e);
        }
    }

    /**
     * 从容器的 shortcut 文件夹刷新快照对象。
     * 用于处理在 app 中创建的快捷方式。
     *
     * @param container 容器对象。
     */
    private void refreshByProfiles(@NonNull Container container) {
        HashSet<String> lnkSet = new HashSet<>();
        Path shortcutDir = container.getShortcutDir();
        try {
            FileUtils.listPaths(shortcutDir, path -> {
                if (!Files.isRegularFile(path))
                    return;
                Shortcut shortcut = buildShortcutByProfile(path, container);
                if (shortcut == null)
                    return;
                shortcutMap.put(shortcut.getUUID(), shortcut);
                lnkSet.add(new SettingWrapper(shortcut.getConfig()).getShortcutInfoLnkName());
            });
        } catch (IOException e) {
            Log.e(TAG, "Failed to refresh by profile.", e);
        }
        lnkMap.put(container.getUUID(), lnkSet);
    }

    /**
     * 从容器的 desktop 文件夹刷新快照对象。
     * 用于处理在 wine 中创建的快捷方式。
     *
     * @param container 容器对象
     */
    private void refreshByDesktops(@NonNull Container container) {
        Path desktopDir = container.getDesktopDir();
        if (!Files.isDirectory(desktopDir))
            return;
        try {
            FileUtils.listPaths(desktopDir, path -> {
                String lnkName = path.getFileName().toString();
                Set<String> lnkSet = lnkMap.get(container.getUUID());
                if (!Files.isRegularFile(path) || !lnkName.endsWith(".desktop") || lnkSet == null ||
                        lnkSet.contains(lnkName)) {
                    return;
                }
                Shortcut shortcut = buildShortcutByDesktopLnk(path, container);
                if (shortcut == null)
                    return;
                shortcutMap.put(shortcut.getUUID(), shortcut);
            });
        } catch (IOException e) {
            Log.e(TAG, "Failed to refresh by desktop.", e);
        }
    }

    /**
     * 从配置文件构建快照对象。
     *
     * @param profile   配置文件路径
     * @param container 快照所属的容器的对象
     * @return 如果成功则返回快照对象，否则返回 null
     */
    @Nullable
    private Shortcut buildShortcutByProfile(@NonNull Path profile, @NonNull Container container) {
        Config config = new JsonConfig();
        try {
            config.getLocal().load(profile);
        } catch (IOException | BadConfigFileFormatException e) {
            Log.e(TAG, "Failed to load config.", e);
            return null;
        }
        SettingWrapper wrapper = new SettingWrapper(config);
        try {
            return new Shortcut(container, wrapper.getShortcutInfoUUID(), profile);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to read uuid from config.", e);
            return null;
        }
    }

    /**
     * 从 desktop 文件构建快照对象，并创建 profile 配置文件。
     *
     * @param lnk       desktop 文件路径
     * @param container 快照所属的容器的对象
     * @return 如果成功则返回快照对象，否则返回 null
     */
    @Nullable
    private Shortcut buildShortcutByDesktopLnk(@NonNull Path lnk, @NonNull Container container) {
        Desktop desktop;
        try {
            desktop = DesktopParser.parse(lnk);
        } catch (IOException | BadDesktopFormatException e) {
            Log.e(TAG, "Failed to parse desktop file.", e);
            return null;
        }

        String target = DesktopParser.getWineTargetFromExec(desktop.getExec());
        if (target == null) {
            Log.e(TAG, "Invalid target in desktop: " + lnk);
            return null;
        }

        Config config = new JsonConfig();
        config.getLocal().loadEmpty();
        SettingWrapper wrapper = new SettingWrapper(config);
        String uuid = UUID.randomUUID().toString();

        wrapper.setShortcutInfoName(desktop.getName());
        // TODO: 解析额外的执行参数
        wrapper.setShortcutInfoExecArgs("");
        wrapper.setShortcutInfoLnkName(lnk.getFileName().toString());
        wrapper.setShortcutInfoTarget(target);
        wrapper.setShortcutInfoCreatedTime(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern(ContainerManager.DATA_FORMAT)));
        wrapper.setShortcutInfoUUID(uuid);

        Path profile = FileUtils.getNextAvailableChildPathWithIndex(container.getShortcutDir(),
                SHORTCUT_BASENAME, "json");
        try {
            config.getLocal().save(profile);
        } catch (IOException e) {
            Log.e(TAG, "Failed to save profile.", e);
            return null;
        }

        return new Shortcut(container, uuid, profile);
    }
}
