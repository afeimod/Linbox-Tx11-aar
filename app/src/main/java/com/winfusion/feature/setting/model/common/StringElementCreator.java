package com.winfusion.feature.setting.model.common;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.value.ConfigPrimitive;

/**
 * 字符串型值构造器。
 */
public class StringElementCreator implements ElementCreator {

    public static final ElementCreator INSTANCE = new StringElementCreator();

    /**
     * 根据输入值构造字符串型值对象。
     *
     * @param value 输入值
     * @return 输出值
     */
    @NonNull
    @Override
    public ConfigPrimitive create(@NonNull Object value) {
        return new ConfigPrimitive(value.toString());
    }
}
