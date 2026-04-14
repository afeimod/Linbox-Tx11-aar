package com.winfusion.feature.manager;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.application.WinfusionApplication;
import com.winfusion.feature.input.overlay.OverlayProfile;
import com.winfusion.feature.input.overlay.utils.OverlayParser;
import com.winfusion.feature.input.overlay.utils.OverlayExporter;
import com.winfusion.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * 叠加层管理器类，用于提供叠加层管理功能，是静态单例的。
 */
public final class OverlayManager {

    public static final String OVERLAYS_DIR = "overlays";
    public static final String ASSETS_OVERLAY_DIR = "overlay";
    public static final String OVERLAY_BASENAME = "overlay";
    private static final String TAG = "OverlayManager";
    private static OverlayManager INSTANCE;

    private final Path overlayDir;

    private OverlayManager() {
        overlayDir = WinfusionApplication.getInstance().getFilesDir().toPath().resolve(OVERLAYS_DIR);
        try {
            FileUtils.checkDirectory(overlayDir);
        } catch (IOException e) {
            Log.e(TAG, "Failed to create overlays dir.", e);
        }
    }

    public static OverlayManager getInstance() {
        if (INSTANCE == null)
            INSTANCE = new OverlayManager();
        return INSTANCE;
    }

    /**
     * 安装全部的内置覆盖层。
     */
    public void installBuiltinOverlay() {
        Pattern pattern = Pattern.compile("^default-[0-9]+.json$");
        Context context = WinfusionApplication.getInstance();
        try {
            String[] files = context.getAssets().list(ASSETS_OVERLAY_DIR);
            if (files == null)
                return;
            for (String f : files) {
                if (!pattern.matcher(f).matches())
                    continue;
                Path target = overlayDir.resolve(f);
                if (Files.isRegularFile(target))
                    continue;
                String assetsPath = ASSETS_OVERLAY_DIR + "/" + f;
                FileUtils.copyFileFromAssets(context, assetsPath, target);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 恢复覆盖层配置文件为内置。
     *
     * @param fileName 文件名称
     */
    public void resetBuiltinOverlay(@NonNull String fileName) {
        try {
            Path target = overlayDir.resolve(fileName);
            String assetsPath = ASSETS_OVERLAY_DIR + "/" + fileName;
            FileUtils.deleteFileIfExist(target);
            FileUtils.copyFileFromAssets(WinfusionApplication.getInstance(), assetsPath, target);
        } catch (IOException e) {
            Log.e(TAG, "Failed to reset overlay.", e);
        }
    }

    /**
     * 创建一个覆盖层。
     *
     * @param name 覆盖层名称
     * @return 覆盖层对象
     */
    @NonNull
    public Overlay createOverlay(@NonNull String name) {
        Path path = FileUtils.getNextAvailableChildPathWithIndex(overlayDir, OVERLAY_BASENAME,
                "json");
        OverlayProfile profile = new OverlayProfile();
        profile.setName(name);
        try {
            OverlayExporter.save(profile, path);
        } catch (IOException e) {
            Log.e(TAG, "Failed to create new overlay file.", e);
        }
        return new Overlay(profile, path.getFileName().toString(), false);
    }

    /**
     * 删除覆盖层配置文件。
     *
     * @param overlay 覆盖层对象
     */
    public void deleteOverlay(@NonNull Overlay overlay) {
        try {
            FileUtils.deleteFileIfExist(overlayDir.resolve(overlay.getFileName()));
        } catch (IOException e) {
            Log.e(TAG, "Failed to delete overlay.", e);
        }
    }

    /**
     * 保存配置文件对象。
     *
     * @param profile  配置文件对象
     * @param fileName 文件名称
     */
    public void saveOverlay(@NonNull OverlayProfile profile, @NonNull String fileName) {
        try {
            Path target = overlayDir.resolve(fileName);
            OverlayExporter.save(profile, target);
        } catch (IOException e) {
            Log.e(TAG, "Failed to save overlay.", e);
        }
    }

    /**
     * 保存配置文件对象。
     *
     * @param overlay 覆盖层对象
     */
    public void saveOverlay(@NonNull Overlay overlay) {
        saveOverlay(overlay.getProfile(), overlay.getFileName());
    }

    /**
     * 重命名配置文件。
     *
     * @param overlay 覆盖层对象
     * @param newName 新名称
     */
    public void renameOverlay(@NonNull Overlay overlay, @NonNull String newName) {
        OverlayProfile profile = new OverlayProfile();
        profile.setName(newName);
        profile.setConfigs(overlay.getProfile().getConfigs());
        saveOverlay(profile, overlay.getFileName());
    }

    /**
     * 克隆覆盖层，并保存配置文件。
     *
     * @param overlay 覆盖层对象
     * @return 新的覆盖层对象
     */
    @NonNull
    public Overlay duplicateOverlay(@NonNull Overlay overlay) {
        Path target = FileUtils.getNextAvailableChildPathWithIndex(overlayDir, OVERLAY_BASENAME,
                "json");
        OverlayProfile profile = new OverlayProfile();
        profile.setName(overlay.getProfile().getName() + "-1");
        profile.setConfigs(overlay.getProfile().getConfigs());
        try {
            OverlayExporter.save(profile, target);
        } catch (IOException e) {
            Log.e(TAG, "Failed to duplicate overlay.", e);
        }
        return new Overlay(profile, target.getFileName().toString(), false);
    }

    /**
     * 从 Uri 导入覆盖层，并保存配置文件。
     *
     * @param uri uri 对象
     * @return 覆盖层对象
     */
    @Nullable
    public Overlay importOverlay(@NonNull Uri uri) {
        try {
            OverlayProfile profile = OverlayParser.parse(uri, WinfusionApplication.getInstance());
            Path target = FileUtils.getNextAvailableChildPathWithIndex(overlayDir, OVERLAY_BASENAME,
                    "json");
            OverlayExporter.save(profile, target);
            return new Overlay(profile, target.getFileName().toString(), false);
        } catch (IOException | OverlayParser.BadOverlayProfileFormatException e) {
            Log.e(TAG, "Failed to import overlay.", e);
            return null;
        }
    }

    /**
     * 通过文件名获取覆盖层对象。
     *
     * @return 如果文件存在，则解析并返回覆盖层对象，否则返回 null
     */
    @Nullable
    public Overlay getOverlayByFileName(@NonNull String fileName) {
        try {
            Path path = overlayDir.resolve(fileName);
            OverlayProfile profile = OverlayParser.parse(path);
            return new Overlay(profile, fileName, false);
        } catch (IOException | OverlayParser.BadOverlayProfileFormatException e) {
            return null;
        }
    }

    /**
     * 扫描并返回所有的覆盖层。
     *
     * @return 覆盖层集合
     */
    @NonNull
    public Collection<Overlay> getOverlays() {
        ArrayList<Overlay> list = new ArrayList<>();
        try {
            FileUtils.listPaths(overlayDir, path -> {
                if (!Files.isRegularFile(path))
                    return;
                OverlayProfile profile;
                try {
                    profile = OverlayParser.parse(path);
                } catch (IOException | OverlayParser.BadOverlayProfileFormatException e) {
                    return;
                }
                String fileName = path.getFileName().toString();
                list.add(new Overlay(profile, fileName, fileName.startsWith("default")));
            });
        } catch (IOException e) {
            Log.e(TAG, "Failed to get overlays.", e);
        }
        return list;
    }
}
