package com.winfusion.utils;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;

/**
 * 点阵图工具类。
 */
public final class BitmapUtils {

    private BitmapUtils() {

    }

    /**
     * 获取一组点阵图中像素最大(质量最好)的一个。
     * 如果最大值有多个，则返回其中的第一个，
     *
     * @param bitmaps 点阵图集合
     * @return 如果传入的点阵图集合不为空，则返回其中像素最大的，否则返回 null
     */
    @Nullable
    public static Bitmap getBestBitmap(@NonNull Collection<Bitmap> bitmaps) {
        long maxPixels = 0;
        Bitmap bestBitmap = null;

        for (Bitmap bitmap : bitmaps) {
            long pixels = (long) bitmap.getHeight() * bitmap.getWidth();
            if (pixels > maxPixels) {
                maxPixels = pixels;
                bestBitmap = bitmap;
            }
        }

        return bestBitmap;
    }
}
