package com.winfusion.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.Objects;

/**
 * UI 相关的工具类。
 */
public final class UiUtils {

    private UiUtils() {

    }

    /**
     * 获取当前上下文下 dp 对应的 px 值。
     *
     * @param context 上下文对象
     * @param dp      设备无关像素值
     * @return 像素值
     */
    public static int dpToPx(@NonNull Context context, float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    /**
     * 找到 value 对应 valuesId 数组的索引位置，并返回 entriesId 数组对应位置的字符串元素。
     *
     * @param context   上下文对象
     * @param value     要匹配的值
     * @param entriesId 入口数组的资源 id
     * @param valuesId  值数组的资源 id
     * @return 对应的 entry
     * @throws IllegalStateException 如果 value 不对应于 valuesId 中的任何值
     */
    @NonNull
    public static String getEntryByValue(@NonNull Context context, @NonNull String value,
                                         @ArrayRes int entriesId, @ArrayRes int valuesId) {

        String[] entries = context.getResources().getStringArray(entriesId);
        String[] values = context.getResources().getStringArray(valuesId);
        for (int i = 0; i < values.length; i++) {
            if (Objects.equals(values[i], value))
                return entries[i];
        }
        throw new IllegalStateException("Value not found: " + value);
    }

    /**
     * 设置 Activity 全屏，即隐藏状态栏和导航栏。
     *
     * @param activity 活动对象
     */
    public static void setActivityFullscreen(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller;
            controller = activity.getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    /**
     * 设置 Activity 不全屏，即显示状态栏和导航栏。
     *
     * @param activity 活动对象
     */
    public static void setActivityNotFullscreen(@NonNull Activity activity) {
        Window window = activity.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        window.setStatusBarColor(ContextCompat.getColor(activity, android.R.color.transparent));
        window.setNavigationBarColor(ContextCompat.getColor(activity, android.R.color.transparent));
    }

    /**
     * 显示短消息。
     *
     * @param context 上下文对象
     * @param msg     短消息内容
     */
    public static void showShortToast(@NonNull Context context, @NonNull String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示短消息。
     *
     * @param context 上下文对象
     * @param msgId   短消息的资源 id
     */
    public static void showShortToast(@NonNull Context context, @StringRes int msgId) {
        Toast.makeText(context, msgId, Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置列表滚动时隐藏悬浮按钮，停下时显示悬浮按钮。
     *
     * @param recyclerView 列表对象
     * @param buttons      按钮数组
     */
    public static void hideExtendedFloatingActionButtonOnScroll(@NonNull RecyclerView recyclerView,
                                                                @NonNull ExtendedFloatingActionButton... buttons) {

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private boolean show = true;

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    for (ExtendedFloatingActionButton btn : buttons)
                        btn.show();
                    show = true;
                } else {
                    if (show) {
                        for (ExtendedFloatingActionButton btn : buttons)
                            btn.hide();
                        show = false;
                    }
                }
            }
        });
    }
}
