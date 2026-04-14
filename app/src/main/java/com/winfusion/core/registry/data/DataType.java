package com.winfusion.core.registry.data;

import androidx.annotation.NonNull;

/**
 * 注册表数据类型枚举。
 */
public enum DataType {
    REG_NONE(0),
    REG_SZ(1),
    REG_EXPAND_SZ(2),
    REG_BINARY(3),
    REG_DWORD(4),
    REG_DWORD_BIG_ENDIAN(5),
    REG_LINK(6),
    REG_MULTI_SZ(7),
    REG_RESOURCE_LIST(8),
    REG_FULL_RESOURCE_DESCRIPTOR(9),
    REG_RESOURCE_REQUIREMENTS_LIST(10),
    REG_QWORD(11),

    /**
     * 未知类型，实际标识符不再通过 {@link DataType#getId()} 获取，而是通过 {@link RawData#getId()}。
     */
    REG_RAW(-1);

    public static final long REG_ID_MAX = 0xFFFFFFFFL;

    private final int id;

    DataType(int id) {
        this.id = id;
    }

    /**
     * 获取数据类型枚举在注册表系统中的整型标识符。
     *
     * @return 对应的标识符
     */
    public int getId() {
        return id;
    }

    /**
     * 根据标识符返回数据类型枚举。
     * 如果标识符无法识别，则返回 {@link #REG_RAW}。
     *
     * @param id 标识符
     * @return 对应的数据类型枚举
     */
    @NonNull
    public static DataType fromId(int id) {
        for (DataType type : values()) {
            if (type.id == id) {
                return type;
            }
        }
        return REG_RAW;
    }
}
