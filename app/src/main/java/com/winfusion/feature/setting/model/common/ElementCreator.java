package com.winfusion.feature.setting.model.common;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.value.ConfigElement;

/**
 * 值构造器接口。
 * 用于根据输入构建 {@link ConfigElement} 对象。
 * 这个接口主要用于从不同来源来构建确定或不确定类型的 {@link ConfigElement},
 * 例如: 字符串 "1"，整型值 1 作为输入，而得到值为真的布尔型 {@link com.winfusion.feature.setting.value.ConfigPrimitive} 对象。
 */
public interface ElementCreator {

    /**
     * 构建并返回值对象。
     *
     * @param value 输入值
     * @return 输出值
     */
    @NonNull
    ConfigElement create(@NonNull Object value);
}
