package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;

/**
 * 注册表的字符串数据类，对应 {@link DataType#REG_SZ} 类型。
 */
public final class StringData extends BaseStringData {

    public StringData(@NonNull String value) {
        super(value, DataType.REG_SZ);
    }
}
