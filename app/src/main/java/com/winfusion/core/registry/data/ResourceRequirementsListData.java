package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;

/**
 * 注册表的资源请求列表数据类，对应 {@link DataType#REG_RESOURCE_REQUIREMENTS_LIST} 类型。
 */
public final class ResourceRequirementsListData extends BaseHexData {

    public ResourceRequirementsListData(@NonNull byte[] value) {
        super(value, DataType.REG_RESOURCE_REQUIREMENTS_LIST);
    }
}
