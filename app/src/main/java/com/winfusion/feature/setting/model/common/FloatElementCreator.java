package com.winfusion.feature.setting.model.common;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.value.ConfigElement;
import com.winfusion.feature.setting.value.ConfigPrimitive;

/**
 * 浮点型值构造器。
 */
public class FloatElementCreator implements ElementCreator {

    public static final ElementCreator INSTANCE = new FloatElementCreator();

    /**
     * 根据输入值构造浮点型值对象。
     *
     * @param value 输入值
     * @return 输出值
     */
    @NonNull
    @Override
    public ConfigElement create(@NonNull Object value) {
        float f;
        if (value instanceof Number number) {
            f = number.floatValue();
        } else {
            try {
                f = Float.parseFloat(value.toString());
            } catch (NumberFormatException e) {
                f = 0f;
            }
        }
        return new ConfigPrimitive(f);
    }
}
