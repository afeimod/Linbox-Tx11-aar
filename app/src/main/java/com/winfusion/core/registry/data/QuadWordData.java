package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * 注册表的 QWORD 数据类，对应 {@link DataType#REG_QWORD} 类型。
 */
public final class QuadWordData extends RegistryData {

    private final long value;

    /**
     * 构造函数，接受一个 long 参数。
     *
     * @param value QWORD 数据
     */
    public QuadWordData(long value) {
        super(DataType.REG_QWORD);
        this.value = value;
    }

    /**
     * 构造函数，接受一个长度为 8 的字节数组，并且是大端序编码。
     *
     * @param value QWORD 数据
     * @throws IllegalArgumentException 如果数组长度不为 8
     */
    public QuadWordData(@NonNull byte[] value) {
        this(fromBytes(value));
    }

    /**
     * 构造函数，接受一个符号为正且不超过 64 位的 {@link BigInteger} 参数。
     *
     * @param value QWORD 数据
     * @throws IllegalArgumentException 如果符号为负或超过 64 位
     */
    public QuadWordData(@NonNull BigInteger value) {
        this(fromBigInteger(value));
    }

    /**
     * 返回 QWORD 数据的字节数组表示，大小固定为 {@link Long#BYTES} 采用小端序。
     *
     * @return QWORD 数据的字节数组表示
     */
    @NonNull
    @Override
    public byte[] toBytes() {
        return ByteBuffer.allocate(Long.BYTES).order(ByteOrder.LITTLE_ENDIAN).putLong(value).array();
    }

    /**
     * 以 long 类型返回 QWORD 数据。
     * 在数据表示上是有符号的，不能完全表示无符号 QWORD。
     *
     * @return 对应的 long
     */
    public long toSignedLong() {
        return value;
    }

    /**
     * 以 {@link BigInteger} 类型返回 QWORD 数据。
     * 在数据表示上是无符号的，可以完全表示 QWORD，采用大端序。
     *
     * @return 对应的 BigInteger
     */
    @NonNull
    public BigInteger toBigInteger() {
        return new BigInteger(1,
                ByteBuffer.allocate(Long.BYTES).order(ByteOrder.BIG_ENDIAN).putLong(value).array());
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj) && value == ((QuadWordData) obj).value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }

    private static long fromBytes(@NonNull byte[] bytes) {
        if (bytes.length != 8)
            throw new IllegalArgumentException("qword must have 8 bytes");
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    private static long fromBigInteger(@NonNull BigInteger bigInteger) {
        if (bigInteger.signum() < 0 || bigInteger.bitLength() > 64)
            throw new IllegalArgumentException("qword must be unsigned 64-bit integer");
        return bigInteger.longValue();
    }
}
