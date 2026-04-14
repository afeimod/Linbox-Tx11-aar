package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;

/**
 * 注册表的二进制数据类，对应 {@link DataType#REG_BINARY} 类型。
 */
public final class BinaryData extends BaseHexData {

    public BinaryData(@NonNull byte[] value) {
        super(value, DataType.REG_BINARY);
    }
}
