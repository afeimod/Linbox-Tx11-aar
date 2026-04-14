package com.winfusion.feature.setting.key;

import androidx.annotation.NonNull;

/**
 * 设置项接口。
 *
 * @param <T> 设置项的默认值类型
 */
public interface Option<T> {

    /**
     * 获取设置项的默认值。
     *
     * @return 默认值
     */
    @NonNull
    T option();
}
