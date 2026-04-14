package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 注册表的 DWORD 数据类，对应 {@link DataType#REG_DWORD} 类型。
 */
public final class DoubleWordData extends RegistryData {

    /**
     * DWORD 数据类型的最大值
     */
    public static final long DWORD_MAX = 0xFFFFFFFFL;

    private final int value;

    /**
     * 构造函数，接受一个 long 参数，但是上限不能超过 DWORD 的上限 {@link #DWORD_MAX}。
     *
     * @param value DWORD 数据
     */
    public DoubleWordData(long value) {
        super(DataType.REG_DWORD);
        if (value > DWORD_MAX)
            throw new IllegalArgumentException("value is larger than dword max: " + value + " > " + DWORD_MAX);
        this.value = (int) value;
    }

    /**
     * 构造函数，接受一个 int 参数。
     *
     * @param value DWORD 数据
     */
    public DoubleWordData(int value) {
        super(DataType.REG_DWORD);
        this.value = value;
    }

    /**
     * 返回 DWORD 数据的字节数组表示，大小固定为 {@link Integer#BYTES}，采用小端序。
     *
     * @return DWORD 数据的字节数组
     */
    @NonNull
    @Override
    public byte[] toBytes() {
        return ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array();
    }

    /**
     * 以 int 类型返回 DWORD 数据。
     * 在数据表示上是有符号的，不能完全表示无符号 DWORD。
     *
     * @return 对应的 int
     */
    public int toSignedInt() {
        return value;
    }

    /**
     * 以 long 类型返回 DWORD 数据。
     * 在数据表示上无符号，可以完全表示 DWORD。
     *
     * @return 对应的 long
     */
    public long toUnsignedLong() {
        return Integer.toUnsignedLong(value);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj) && value == ((DoubleWordData) obj).value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
