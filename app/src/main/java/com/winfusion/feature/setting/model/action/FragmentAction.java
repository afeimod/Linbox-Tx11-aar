package com.winfusion.feature.setting.model.action;

import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 * 适用于目标为 {@link androidx.fragment.app.Fragment} 的动作类。
 * 用于打开一个 Fragment。
 */
public class FragmentAction extends BaseAction {

    private final Bundle bundle;
    private final int id;

    /**
     * 构造函数。
     *
     * @param id     Fragment 的 id
     * @param bundle 参数包对象
     */
    public FragmentAction(int id, @Nullable Bundle bundle) {
        this.bundle = bundle;
        this.id = id;
    }

    /**
     * 获取参数包对象。
     *
     * @return 参数包对象
     */
    @Nullable
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * 获取 Fragment 的 id。
     *
     * @return id
     */
    public int getId() {
        return id;
    }
}
