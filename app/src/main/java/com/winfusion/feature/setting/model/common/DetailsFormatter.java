package com.winfusion.feature.setting.model.common;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.value.ConfigElement;

/**
 * 值的格式化类。
 * 用于将值对象转换为字符串类型，常用在数据显示方面。
 */
public interface DetailsFormatter {

    /**
     * 获取格式化后的值。
     *
     * @param element 输入值
     * @return 格式化后的值
     */
    @NonNull
    String getFormattedValue(@NonNull ConfigElement element);
}
