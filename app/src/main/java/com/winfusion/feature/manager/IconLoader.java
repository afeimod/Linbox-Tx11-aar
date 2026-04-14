package com.winfusion.feature.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.desktop.Desktop;
import com.winfusion.core.desktop.DesktopParser;
import com.winfusion.core.desktop.exception.BadDesktopFormatException;
import com.winfusion.core.image.ico.IconDecoder;
import com.winfusion.core.image.ico.exception.BadIconFormatException;
import com.winfusion.core.pe.IconParser;
import com.winfusion.core.pe.exception.BadPEFormatException;
import com.winfusion.core.pe.exception.IconNotFoundException;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.utils.BitmapUtils;
import com.winfusion.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 快照的图标加载器类，用于管理以及按需加载快照的图标。
 */
public final class IconLoader {

    private static final int[] ICON_RESOLUTIONS = {256, 64, 48, 32, 24, 16};
    private static final String ICON_SUFFIX = ".png";
    private static final String TAG = "IconLoader";


    private final Shortcut shortcut;
    private Bitmap bitmap;

    IconLoader(@NonNull Shortcut shortcut) {
        this.shortcut = shortcut;
    }

    /**
     * 获取图标的点阵图对象。
     * 优先返回已缓存的点阵图对象，如果没有缓存，则会尝试加载。
     *
     * @return 点阵图对象
     */
    @Nullable
    public synchronized Bitmap getIcon() {
        return bitmap == null ? loadIcon() : bitmap;
    }

    /**
     * 加载并返回图标的点阵图对象。
     * 该方法将会强制刷新缓存的点阵图对象。
     *
     * @return 点阵图对象
     */
    @Nullable
    public synchronized Bitmap loadIcon() {
        bitmap = loadIconFromCache();
        if (bitmap == null) {
            bitmap = loadIconFromDesktop();
            if (bitmap == null)
                bitmap = loadIconFromTarget();
        }
        return bitmap;
    }

    /**
     * 设置图标缓存文件。
     *
     * @param uri     图标的 uri
     * @param context 上下文对象
     * @return 如果图标文件加载成功并且成功设置为缓存文件则返回 true，否则返回 false
     */
    public synchronized boolean setIcon(@NonNull Uri uri, @NonNull Context context) {
        Bitmap bitmap;
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)){
            bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null)
                return false;
        } catch (IOException e) {
            Log.e(TAG, "Failed to read icon from uri.", e);
            return false;
        }

        if (!saveBitmapToCacheFile(bitmap))
            return false;

        this.bitmap = bitmap;
        return true;
    }

    /**
     * 导出图标到 {@code /sdcard/downloads/winfusion/xxx.png} 文件。
     *
     * @return 如果缓存文件存在且导出成功，则返回目标文件路径，否则返回 null
     */
    @Nullable
    public Path exportIcon() {
        Path imagePath = getIconCacheFile();
        if (!Files.isRegularFile(imagePath))
            return null;

        Path targetPath = FileUtils.getStoragePath("icon", ICON_SUFFIX);
        try {
            Files.copy(imagePath, targetPath);
        } catch (IOException e) {
            return null;
        }

        return targetPath;
    }

    /**
     * 删除该快照的图标缓存文件。
     */
    public void deleteIcon() {
        Path imagePath = getIconCacheFile();
        bitmap = null;
        if (!Files.isRegularFile(imagePath))
            return;
        try {
            Files.delete(imagePath);
        } catch (IOException e) {
            Log.e(TAG, "Failed to delete cached icon file.", e);
        }
    }

    /**
     * 获取快照的图标缓存文件的路径。
     *
     * @return 图标缓存文件的路径。
     */
    @NonNull
    public Path getIconCacheFile() {
        return shortcut.getContainer().getIconDir()
                .resolve(shortcut.getUUID() + ICON_SUFFIX);
    }

    /**
     * 从缓存加载图标文件。
     *
     * @return 如果缓存存在并且解析成功，则返回点阵图对象，否则返回 null
     */
    @Nullable
    private Bitmap loadIconFromCache() {
        Path imagePath = shortcut.getContainer().getIconDir()
                .resolve(shortcut.getUUID() + ICON_SUFFIX);
        if (!Files.isRegularFile(imagePath))
            return null;
        return BitmapFactory.decodeFile(imagePath.toString());
    }

    /**
     * 从 Linux Desktop 加载图标，并创建缓存文件。
     *
     * @return 如果 Desktop 图标存在并且解析成功，则返回点阵图对象，否则返回 null
     */
    @Nullable
    private Bitmap loadIconFromDesktop() {
        SettingWrapper wrapper = new SettingWrapper(shortcut.getConfig());
        String lnkName = wrapper.getShortcutInfoLnkName();
        if (lnkName.isEmpty())
            return null;

        Desktop desktop;
        try {
            desktop = DesktopParser.parse(shortcut.getContainer().getDesktopDir().resolve(lnkName));
        } catch (IOException | BadDesktopFormatException e) {
            return null;
        }

        String iconName = desktop.getIconName();
        if (iconName == null || iconName.isEmpty())
            return null;

        for (int resolution : ICON_RESOLUTIONS) {
            Path imagePath = shortcut.getContainer().getLocalIconDir(resolution)
                    .resolve(iconName + ICON_SUFFIX);
            if (!Files.isRegularFile(imagePath))
                continue;

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath.toString());
            if (bitmap == null)
                continue;

            Path iconPath = shortcut.getContainer().getIconDir()
                    .resolve(shortcut.getUUID() + ICON_SUFFIX);
            try {
                Files.copy(imagePath, iconPath);
            } catch (IOException e) {
                continue;
            }

            return bitmap;
        }

        return null;
    }

    /**
     * 从 EXE 可执行文件加载图标，并创建缓存文件。
     *
     * @return 如果成功从 EXE 中提取出图标，则返回点阵图对象，否则返回 null
     */
    @Nullable
    private Bitmap loadIconFromTarget() {
        // FIXME: 获取真实的 exe 可执行文件路径。
        Path exePath = shortcut.getContainer().getHomeDir().resolve("xxx");
        if (!Files.isRegularFile(exePath))
            return null;

        ByteBuffer buffer;
        try {
            buffer = IconParser.parseMainIconFromPE(exePath);
        } catch (IconNotFoundException | BadPEFormatException e) {
            return null;
        }

        List<Bitmap> bitmaps;
        try {
            bitmaps = IconDecoder.parseBitmapsFromIcon(buffer);
        } catch (BadIconFormatException e) {
            return null;
        }

        Bitmap bitmap = BitmapUtils.getBestBitmap(bitmaps);
        if (bitmap == null)
            return null;

        if (!saveBitmapToCacheFile(bitmap))
            return null;

        return bitmap;
    }

    private boolean saveBitmapToCacheFile(@NonNull Bitmap bitmap) {
        try (OutputStream outputStream = Files.newOutputStream(getIconCacheFile())) {
            return bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (IOException e) {
            Log.e(TAG, "Failed to save icon cache.", e);
            return false;
        }
    }
}
