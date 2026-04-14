package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;

/**
 * 注册表的无类型数据类，对应 {@link DataType#REG_NONE} 类型。
 */
public final class NoneData extends BaseHexData {

    public NoneData(@NonNull byte[] value) {
        super(value, DataType.REG_NONE);
    }
}
