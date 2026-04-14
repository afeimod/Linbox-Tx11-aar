package com.winfusion.feature.input.overlay.widget;

import android.graphics.Paint;

import androidx.annotation.NonNull;

import com.winfusion.feature.input.interfaces.InputInterface;
import com.winfusion.feature.input.overlay.WidgetStateProvider;

/**
 * 控件提供器接口，用于提供全局状态和控件参数等信息。
 */
public interface WidgetProvider extends WidgetStateProvider {

    /**
     * 全局状态枚举。
     */
    enum Status {
        Edit,
        Control,
        Preview
    }

    /**
     * 获取基准宽度。
     *
     * @return 基准宽度
     */
    int getBaseWidth();

    /**
     * 获取基准高度。
     *
     * @return 基准高度
     */
    int getBaseHeight();

    /**
     * 获取基准大小。
     *
     * @return 基准大小
     */
    int getBaseSize();

    /**
     * 获取当前状态。
     *
     * @return 状态
     */
    @NonNull
    Status getStatus();

    /**
     * 获取边框画笔。
     *
     * @return 画笔对象
     */
    @NonNull
    Paint getBoundsPaint();

    /**
     * 获取当前输入接口。
     *
     * @return 输入接口对象
     */
    @NonNull
    InputInterface getInputInterface();

    /**
     * 设定控件发生了变更。
     */
    void setWidgetChanged();
}
