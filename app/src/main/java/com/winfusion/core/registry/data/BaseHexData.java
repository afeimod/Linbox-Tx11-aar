package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

/**
 * 所有以字节存储的注册表数据的基类。
 */
public abstract class BaseHexData extends RegistryData {

    protected final byte[] value;

    /**
     * 构造函数，接受一个字节数组和数据类型。
     *
     * @param value    数据的字节数组
     * @param dataType 数据类型
     */
    public BaseHexData(@NonNull byte[] value, @NonNull DataType dataType) {
        super(dataType);
        this.value = value;
    }

    /**
     * 返回当前对象的字节数组的拷贝。
     *
     * @return 数据的字节数组
     */
    @NonNull
    @Override
    public byte[] toBytes() {
        return value.clone();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj) && Arrays.equals(value, ((BaseHexData) obj).value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
