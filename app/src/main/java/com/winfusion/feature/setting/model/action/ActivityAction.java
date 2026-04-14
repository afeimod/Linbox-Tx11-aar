package com.winfusion.feature.setting.model.action;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;

/**
 * 适用于目标为 {@link Activity} 的动作类。
 * 用于为打开一个活动提供必要的参数。
 */
public class ActivityAction extends BaseAction {

    private final Bundle bundle;
    private final Class<? extends Activity> activityClass;

    /**
     * 构造函数。
     *
     * @param bundle        数据包对象
     * @param activityClass 活动类
     */
    public ActivityAction(@NonNull Bundle bundle, @NonNull Class<? extends Activity> activityClass) {
        this.activityClass = activityClass;
        this.bundle = bundle;
    }

    /**
     * 获取数据包对象。
     *
     * @return 数据包对象
     */
    @NonNull
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * 获取活动类。
     *
     * @return 活动类
     */
    @NonNull
    public Class<? extends Activity> getActivityClass() {
        return activityClass;
    }
}
