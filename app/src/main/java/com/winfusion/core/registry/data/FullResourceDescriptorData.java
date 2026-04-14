package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;

/**
 * 注册表的完整资源描述符数据类，对应 {@link DataType#REG_FULL_RESOURCE_DESCRIPTOR} 类型。
 */
public final class FullResourceDescriptorData extends BaseHexData {

    public FullResourceDescriptorData(@NonNull byte[] value) {
        super(value, DataType.REG_FULL_RESOURCE_DESCRIPTOR);
    }
}
