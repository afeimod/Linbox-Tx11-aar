package com.winfusion.feature.setting.model.common;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.value.ConfigElement;
import com.winfusion.feature.setting.value.ConfigPrimitive;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 布尔型值构造器。
 */
public class BooleanElementCreator implements ElementCreator {

    public static final ElementCreator INSTANCE = new BooleanElementCreator();

    /**
     * 根据输入值构造布尔型的值对象。
     * 如果输入值不是合法的
     *
     * @param value 输入值
     * @return 输出值
     */
    @NonNull
    @Override
    public ConfigElement create(@NonNull Object value) {
        boolean bool;
        if (value instanceof Number number)
            bool = !isZero(number);
        else if (value instanceof Boolean b)
            bool = b;
        else
            bool = Boolean.parseBoolean(value.toString());
        return new ConfigPrimitive(bool);
    }

    private boolean isZero(@NonNull Number number) {
        if (number instanceof Byte || number instanceof Short || number instanceof Integer ||
                number instanceof Long)
            return number.longValue() == 0;
        else if (number instanceof Float || number instanceof Double)
            return number.doubleValue() == 0f;
        else if (number instanceof BigDecimal bigDecimal)
            return bigDecimal.compareTo(BigDecimal.ZERO) == 0;
        else if (number instanceof BigInteger bigInteger)
            return bigInteger.compareTo(BigInteger.ZERO) == 0;
        else
            return false;
    }
}
