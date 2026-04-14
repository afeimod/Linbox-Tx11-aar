package com.winfusion.utils;

import androidx.annotation.NonNull;

import java.util.Locale;

/**
 * 和系统有关的工具类。
 */
public final class SystemUtils {

    private SystemUtils() {

    }

    /**
     * 获取系统的区域代号。
     * 例如 {@code "zh_cn"}
     *
     * @return 区域代号
     */
    @NonNull
    public static String getRegion() {
        return Locale.getDefault().toString().split("_#")[0];
    }
}
