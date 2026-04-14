package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * 注册表的位置数据类，对应 {@link DataType#REG_RAW} 类型。
 */
public final class RawData extends BaseHexData {

    private final int id;

    /**
     * 构造函数，接受一个类型标识符和一个字节数组。
     *
     * @param id    类型标识符
     * @param value 字节数组
     */
    public RawData(int id, @NonNull byte[] value) {
        super(value, DataType.REG_RAW);
        this.id = id;
    }

    /**
     * 获取数据的类型标识符。
     *
     * @return 类型标识符
     */
    public int getId() {
        return id;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj) && id == ((RawData) obj).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
