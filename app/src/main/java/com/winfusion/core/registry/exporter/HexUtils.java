package com.winfusion.core.registry.exporter;

import androidx.annotation.NonNull;

/**
 * 十六进制操作相关的工具类。
 */
public final class HexUtils {

    private static final char[] HEX_ARRAY =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private HexUtils() {

    }

    /**
     * 将字节类型转换 2 个 16 进制字符形式并保存在传入的数组中。
     *
     * @param b   要转换的字节
     * @param buf 字符数组，大小不能小于 2
     */
    public static void byteToHexChars(byte b, @NonNull char[] buf) {
        int i = b & 0xFF;
        buf[0] = HEX_ARRAY[i >>> 4];
        buf[1] = HEX_ARRAY[i & 0xF];
    }

    /**
     * 将字符类型转换为 4 个 16 进制字符形式并保存在传入的数组中。
     *
     * @param c   要转换的字符
     * @param buf 字符数组，大小不能小于 4
     */
    public static void charToHexChars(char c, @NonNull char[] buf) {
        buf[0] = HEX_ARRAY[(c >>> 12) & 0xF];
        buf[1] = HEX_ARRAY[(c >>> 8) & 0xF];
        buf[2] = HEX_ARRAY[(c >>> 4) & 0xF];
        buf[3] = HEX_ARRAY[c & 0xF];
    }

    /**
     * 将 int 整型转换为 8 个 16 进制字符形式并保存在传入的数组中。
     * 例如 0x12345678 会得到 ['1', '2', '3', '4', '5', '6', '7', '8']
     * 例如 0x1234 会得到 ['0', '0', '0', '0', '1', '2', '3', '4']
     *
     * @param value 要转换的 int
     * @param buf   字符数组，大小不能小于 8
     */
    public static void intToHexChars(int value, @NonNull char[] buf) {
        for (int i = 7; i >= 0; i--) {
            buf[i] = HEX_ARRAY[value & 0xF];
            value >>>= 4;
        }
    }

    /**
     * 将 int 整型转换为最多 8 个，最少 1 个 16 进制字符形式并保存在传入的数组中。
     * 功能和 {@link #intToHexChars(int, char[])} 相似，但是不输出前导 0。
     *
     * @param value 要转换的 int
     * @param buf   字符数组，大小不能小于 8
     * @return 转换后的字符数
     */
    public static int intToHexCharsN(int value, @NonNull char[] buf) {
        int i = 7;
        do {
            buf[i--] = HEX_ARRAY[value & 0xF];
            value >>>= 4;
        } while (value != 0 && i >= 0);

        int length = 7 - i;
        System.arraycopy(buf, i + 1, buf, 0, length);
        return length;
    }
}
