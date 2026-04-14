package com.winfusion.feature.setting.model.common;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.value.ConfigElement;
import com.winfusion.feature.setting.value.ConfigPrimitive;

/**
 * 整型值构造器。
 */
public class IntegerElementCreator implements ElementCreator {

    public static final ElementCreator INSTANCE = new IntegerElementCreator();

    /**
     * 根据输入值构造整型值对象。
     *
     * @param value 输入值
     * @return 输出值
     */
    @NonNull
    @Override
    public ConfigElement create(@NonNull Object value) {
        int i;
        if (value instanceof Number number) {
            i = number.intValue();
        } else {
            try {
                i = Integer.parseInt(value.toString());
            } catch (NumberFormatException e) {
                i = 0;
            }
        }
        return new ConfigPrimitive(i);
    }
}
