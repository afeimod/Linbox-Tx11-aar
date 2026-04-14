package com.winfusion.core.registry;

import androidx.annotation.NonNull;

/**
 * Windows SID 类，用于表示一个 Windows SID 标识符。
 */
public class SID {

    private final int revision;
    private final long identifierAuthority;
    private final long[] subAuthorities;

    /**
     * 构造函数
     *
     * @param revision            修订版本
     * @param identifierAuthority 顶级颁发机构
     * @param subAuthorities      子颁发机构的数组
     */
    public SID(int revision, long identifierAuthority, @NonNull long[] subAuthorities) {
        if (revision < 0 || identifierAuthority < 0) {
            throw new IllegalArgumentException("Invalid SID components.");
        }
        this.revision = revision;
        this.identifierAuthority = identifierAuthority;
        this.subAuthorities = subAuthorities.clone();
    }

    /**
     * 从字符串解析并返回一个 SID 对象。
     *
     * @param sidString SID 字符串
     * @return SID 对象
     * @throws IllegalArgumentException 如果 SID 字符串不合法
     */
    @NonNull
    public static SID parse(@NonNull String sidString) throws SIDParserException {
        if (!sidString.startsWith("S-"))
            throw new SIDParserException("Invalid SID format.");

        String[] parts = sidString.split("-");
        if (parts.length < 3)
            throw new SIDParserException("SID must have at least 3 parts.");

        try {
            int revision = Integer.parseInt(parts[1]);
            long identifierAuthority = Long.parseLong(parts[2]);

            long[] subAuthorities = new long[parts.length - 3];
            for (int i = 3; i < parts.length; i++)
                subAuthorities[i - 3] = Long.parseLong(parts[i]);

            return new SID(revision, identifierAuthority, subAuthorities);
        } catch (NumberFormatException e) {
            throw new SIDParserException("Invalid SID part.");
        }
    }

    /**
     * 获取 SID 的修订版本。
     *
     * @return 修订版本
     */
    public int getRevision() {
        return revision;
    }

    /**
     * 获取 SID 的顶级颁发机构。
     *
     * @return 顶级颁发机构
     */
    public long getIdentifierAuthority() {
        return identifierAuthority;
    }

    /**
     * 获取 SID 的子颁发机构。
     *
     * @return 子颁发机构的数组
     */
    @NonNull
    public long[] getSubAuthorities() {
        return subAuthorities.clone();
    }

    /**
     * 获取 SID 的字符串表示。
     * 例如 {@code "S-1-5-21-0-0-0-1000"}
     *
     * @return 字符串表示
     */
    @NonNull
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("S-").append(revision).append("-").append(identifierAuthority);
        for (long subAuth : subAuthorities)
            builder.append("-").append(subAuth);
        return builder.toString();
    }

    public static class SIDParserException extends Exception {

        public SIDParserException(String message) {
            super(message);
        }
    }
}
