package com.winfusion.core.registry;

import androidx.annotation.NonNull;

import java.util.StringJoiner;

/**
 * 用于创建 {@link RegistryKey} 对象的工具类。
 */
public final class RegistryKeys {

    private static final RegistryKey ROOT_KEY = new RegistryKey(RegistryKey.KEY_SPLIT);
    private static final RegistryKey HKEY_LOCAL_MACHINE = new RegistryKey("\\HKEY_LOCAL_MACHINE");
    private static final RegistryKey HKEY_CURRENT_USER = new RegistryKey("\\HKEY_CURRENT_USER");
    private static final RegistryKey HKEY_USERS_DEFAULT = new RegistryKey("\\HKEY_USERS\\.DEFAULT");

    private RegistryKeys() {

    }

    /**
     * 从字符串拼接注册表键。
     *
     * @param first 第一段
     * @param more  更多段
     * @return 拼接的注册表键
     * @throws IllegalArgumentException 如果键名不合法
     */
    @NonNull
    public static RegistryKey get(@NonNull String first, @NonNull String... more) {
        StringJoiner joiner = new StringJoiner(RegistryKey.KEY_SPLIT);
        int len = first.length();
        if (len != 1 && first.endsWith(RegistryKey.KEY_SPLIT))
            first = first.substring(0, len - 1);
        joiner.add(first);
        for (String key : more) {
            for (String part : key.split(RegistryKey.KEY_SPLIT)) {
                if (part.isEmpty())
                    continue;
                joiner.add(part);
            }
        }
        return new RegistryKey(joiner.toString());
    }

    /**
     * 获取根键。
     * 由于根键常用，因此提供方法获取根键，避免重复创建。
     *
     * @return 根键
     */
    @NonNull
    public static RegistryKey root() {
        return ROOT_KEY;
    }

    /**
     * 获取 HKEY_LOCAL_MACHINE 的键。
     *
     * @return HKEY_LOCAL_MACHINE
     */
    @NonNull
    public static RegistryKey getHkeyLocalMachine() {
        return HKEY_LOCAL_MACHINE;
    }

    /**
     * 获取 HKEY_CURRENT_USER 的键。
     *
     * @return HKEY_CURRENT_USER
     */
    @NonNull
    public static RegistryKey getHkeyCurrentUser() {
        return HKEY_CURRENT_USER;
    }

    /**
     * 获取 HKEY_USERS\.DEFAULT 的键。
     *
     * @return HKEY_USERS\.DEFAULT
     */
    @NonNull
    public static RegistryKey getHkeyUsersDefault() {
        return HKEY_USERS_DEFAULT;
    }
}
