package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * 所有以字符串存储的注册表数据的基类。
 */
public abstract class BaseStringData extends RegistryData {

    protected final String value;

    /**
     * 构造函数，接受一个字符串和一个数据类型。
     *
     * @param value    字符串
     * @param dataType 数据类型
     */
    protected BaseStringData(@NonNull String value, @NonNull DataType dataType) {
        super(dataType);
        this.value = value;
    }

    /**
     * 返回当前字符串的字节数组，以 UTF-16 LE 编码。
     *
     * @return 字符串的字节数组
     */
    @NonNull
    @Override
    public byte[] toBytes() {
        return (value + "\0").getBytes(StandardCharsets.UTF_16LE);
    }

    /**
     * 获取当前对象存储的字符串。
     *
     * @return 存储的字符串。
     */
    @NonNull
    public String getString() {
        return value;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj) && Objects.equals(value, ((BaseStringData) obj).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
