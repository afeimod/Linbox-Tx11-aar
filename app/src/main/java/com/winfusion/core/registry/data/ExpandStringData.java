package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;

/**
 * 注册表的可扩展字符串数据类，对应 {@link DataType#REG_EXPAND_SZ} 类型。
 */
public final class ExpandStringData extends BaseStringData {

    public ExpandStringData(@NonNull String value) {
        super(value, DataType.REG_EXPAND_SZ);
    }
}
