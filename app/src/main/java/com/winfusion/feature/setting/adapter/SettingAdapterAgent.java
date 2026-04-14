package com.winfusion.feature.setting.adapter;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.NavDirections;

import java.util.Deque;

/**
 * 设置项适配器的代理接口。
 */
public interface SettingAdapterAgent {

    /**
     * 获取返回栈。
     *
     * @return 返回栈
     */
    @NonNull
    Deque<SettingAdapter.BackModel> getBackStack();

    /**
     * 获取视图位置。
     *
     * @return 视图位置
     */
    @NonNull
    ViewPosition getViewPosition();

    /**
     * 设置视图位置。
     *
     * @param position 视图位置
     */
    void setViewPosition(@NonNull ViewPosition position);

    /**
     * 显示对话框。
     *
     * @param dialog 对话框对象
     */
    void showDialogFragment(@NonNull DialogFragment dialog);

    /**
     * 启动活动。
     *
     * @param activityClass   活动类
     * @param bundle          参数包对象
     * @param adapterPosition Item 位置，用于更新子 View
     */
    void toActivity(@NonNull Class<? extends Activity> activityClass, @NonNull Bundle bundle,
                    int adapterPosition);

    /**
     * 导航到导航目标。
     *
     * @param directions      导航目标
     * @param adapterPosition Item 位置，用于更新子 View
     */
    void toDirection(@NonNull NavDirections directions, int adapterPosition);

    /**
     * 导航到导航目标。
     *
     * @param fragmentId      Fragment 的 id
     * @param bundle          参数包对象
     * @param adapterPosition Item 位置，用于更新子 View
     */
    void toFragment(int fragmentId, @Nullable Bundle bundle, int adapterPosition);

    void updateTitle(@StringRes int titleId);

    /**
     * 视图位置类。
     * 用于确定 {@link androidx.recyclerview.widget.RecyclerView} 的视图位置。
     *
     * @param position 当前显示的子 View 的索引
     * @param offset   当前显示的子 View 相对于容器顶部的偏移
     */
    record ViewPosition(int position, int offset) {

    }
}
