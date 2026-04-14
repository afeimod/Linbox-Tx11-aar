package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;

/**
 * 注册表的资源列表数据类，对应 {@link DataType#REG_RESOURCE_LIST} 类型。
 */
public final class ResourceListData extends BaseHexData {

    public ResourceListData(@NonNull byte[] value) {
        super(value, DataType.REG_RESOURCE_LIST);
    }
}
