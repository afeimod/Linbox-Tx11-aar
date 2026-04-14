package com.winfusion.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.Objects;

/**
 * 主题相关的工具类。
 */
public final class ThemeUtils {

    private ThemeUtils() {

    }

    /**
     * 设置 Activity 的主题模式。
     *
     * @param activity 活动对象
     */
    public static void setThemeMode(@NonNull final AppCompatActivity activity) {
        Objects.requireNonNull(activity);
        WindowInsetsControllerCompat windowController = WindowCompat.getInsetsController(
                activity.getWindow(),
                activity.getWindow().getDecorView()
        );

        if (isNightMode(activity))
            setDarkModeSystemBars(windowController);
        else
            setLightModeSystemBars(windowController);
    }

    private static boolean isNightMode(@NonNull final AppCompatActivity activity) {
        int uiMode = activity.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return uiMode == Configuration.UI_MODE_NIGHT_YES;
    }

    private static void setLightModeSystemBars(@NonNull final WindowInsetsControllerCompat windowController) {
        windowController.setAppearanceLightStatusBars(true);
        windowController.setAppearanceLightNavigationBars(true);
    }

    private static void setDarkModeSystemBars(@NonNull final WindowInsetsControllerCompat windowController) {
        windowController.setAppearanceLightStatusBars(false);
        windowController.setAppearanceLightNavigationBars(false);
    }

    /**
     * 从属性资源中获得对象。
     *
     * @param context 上下文对象
     * @param attr    属性资源的 id
     * @return 颜色值
     */
    public static int getColorFromAttr(@NonNull Context context, @AttrRes int attr) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }
}
