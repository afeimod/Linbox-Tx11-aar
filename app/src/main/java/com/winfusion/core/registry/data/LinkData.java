package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;

/**
 * 注册表的链接数据类，对应 {@link DataType#REG_LINK} 类型。
 */
public final class LinkData extends BaseHexData {

    public LinkData(@NonNull byte[] value) {
        super(value, DataType.REG_LINK);
    }
}
