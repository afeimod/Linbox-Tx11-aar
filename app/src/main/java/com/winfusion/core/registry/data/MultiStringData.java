package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * 注册表的多行字符串数据类，对应 {@link DataType#REG_MULTI_SZ} 类型。
 */
public final class MultiStringData extends RegistryData {

    private final String[] value;

    /**
     * 构造函数，接受零个或多个字符串。
     *
     * @param value 字符串
     */
    public MultiStringData(@NonNull String... value) {
        super(DataType.REG_MULTI_SZ);
        this.value = value;
    }

    /**
     * 获取内部字符串数组的拷贝。
     *
     * @return 字符串数组
     */
    @NonNull
    public String[] getStringArray() {
        return value.clone();
    }

    /**
     * 返回多字符串的字节数组，使用 {@code '\0'} 作为单个字符串结束符号，使用 {@code ['\0', '\0']} 作为整体结束符号。
     * 采用 UTF-16 LE 编码。
     *
     * @return 多字符串的字节数组
     */
    @NonNull
    @Override
    public byte[] toBytes() {
        StringBuilder builder = new StringBuilder();
        if (value.length == 0) {
            builder.append('\0');
        } else {
            for (String str : value)
                builder.append(str).append('\0');
        }
        builder.append('\0');
        return builder.toString().getBytes(StandardCharsets.UTF_16LE);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != getClass())
            return false;
        return Arrays.equals(value, ((MultiStringData) obj).value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }
}
