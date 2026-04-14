package com.winfusion.core.registry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Iterator;
import java.util.Objects;

/**
 * 表示注册表中的键，例如 {@code HKEY_LOCAL_MACHINE\Software\Microsoft}。
 * 键对大小写是不敏感的，例如键名为 {@code AA\BB\CC} 和 {@code aa\bb\cc} 的两个键被认为是相等的。
 * 区分相对键和绝对键，在绝对键中，根键的键名为 {@link #KEY_SPLIT}，相对键则没有根键。
 */
public final class RegistryKey implements Comparable<RegistryKey>, Iterable<RegistryKey> {

    /**
     * 注册表键层级的分隔符。
     */
    public static final String KEY_SPLIT = "\\";
    /**
     * 注册表键的最大长度，参考<a href="https://learn.microsoft.com/en-us/windows/win32/sysinfo/registry-element-size-limits">注册表元素大小限制</a>。
     */
    public static final int KEY_MAX_LENGTH = 255;
    /**
     * 值名称的最大长度，参考<a href="https://learn.microsoft.com/en-us/windows/win32/sysinfo/registry-element-size-limits">注册表元素大小限制</a>。
     */
    public static final int VALUE_NAME_MAX_LENGTH = 16383;

    private final String rawKey;
    private final String normalizedKey;

    /**
     * 构造函数，接受一个字符串参数作为原始键名。
     *
     * @param rawKey 原始键名
     * @throws IllegalArgumentException 如果键名的长度太长，或者键名包含了不合法的分隔符
     */
    RegistryKey(@NonNull String rawKey) {
        if (rawKey.contains("\\\\"))
            throw new IllegalArgumentException("Key must not contain \"\\\\\": " + rawKey);
        int len = rawKey.length();
        if (len > KEY_MAX_LENGTH)
            throw new IllegalArgumentException("Key length is too long: " + len + " > " + KEY_MAX_LENGTH);
        if (len != 1 && rawKey.endsWith(KEY_SPLIT))
            rawKey = rawKey.substring(0, len - 1);
        this.rawKey = rawKey;
        this.normalizedKey = this.rawKey.toLowerCase();
    }

    /**
     * 判断该键是否为目标键的子键。
     *
     * @param key 目标键
     * @return 是子键则返回 true，否则返回 false
     */
    public boolean isChildOf(@NonNull RegistryKey key) {
        return !equals(key) && normalizedKey.startsWith(key.normalizedKey);
    }

    /**
     * 判断该键是否为目标键的父键。
     *
     * @param key 目标键
     * @return 是父键则返回 true，否则返回 false
     */
    public boolean isParentOf(@NonNull RegistryKey key) {
        return key.isChildOf(this);
    }

    /**
     * 判断该键是否是目标键的直接子键。
     *
     * @param key 目标键
     * @return 是直接子键则返回 true，否则返回 false
     */
    public boolean isDirectChildOf(@NonNull RegistryKey key) {
        return isChildOf(key) && normalizedKey.substring(key.normalizedKey.length() + 1)
                .contains(KEY_SPLIT);
    }

    /**
     * 判断该键是否是目标键的直接父键。
     *
     * @param key 目标键
     * @return 是直接父键则返回 true，否则返回 false
     */
    public boolean isDirectParentOf(@NonNull RegistryKey key) {
        return key.isDirectChildOf(this);
    }

    /**
     * 判断该键是否是根键。
     *
     * @return 是根键则返回 true，否则返回 false
     */
    public boolean isRoot() {
        return Objects.equals(rawKey, KEY_SPLIT);
    }

    /**
     * 判断该键是否是绝对键。
     *
     * @return 是绝对键则返回 true，否则返回 false
     */
    public boolean isAbsolute() {
        return rawKey.startsWith(KEY_SPLIT);
    }

    /**
     * 返货该键与目标键拼接后的键。
     * 例如 {@code new RegistryKey("\\AA").resolve(new RegistryKey("\\BB"))} 得到 {@code \AA\BB}。
     *
     * @param key 要拼接的键
     * @return 拼接后的键
     */
    @NonNull
    public RegistryKey resolve(@NonNull RegistryKey key) {
        String newRawKey = (rawKey + KEY_SPLIT + key.rawKey).replace("\\\\", KEY_SPLIT);
        return new RegistryKey(newRawKey);
    }

    /**
     * 从原始键名构建目标键，并返回该键与目标键拼接后的键。
     *
     * @param rawKey 原始键名
     * @return 拼接后的键
     * @throws IllegalArgumentException 如果原始键名不合法
     */
    @NonNull
    public RegistryKey resolve(@NonNull String rawKey) {
        return resolve(new RegistryKey(rawKey));
    }

    /**
     * 获取键名为该键最底层级名称的键。
     * 例如 {@code \AA\BB\CC} 的最底层级为 {@code CC}。
     *
     * @return 该键的最底层级键
     */
    @NonNull
    public RegistryKey getSelf() {
        if (getLevel() == 1)
            return this;
        int i = rawKey.lastIndexOf(KEY_SPLIT);
        return new RegistryKey(rawKey.substring(i + 1));
    }

    /**
     * 获取该键的父键，如果层级为 1，则返回该键自身。
     *
     * @return 父键
     */
    @NonNull
    public RegistryKey getParent() {
        if (getLevel() == 1)
            return this;
        if (getLevel() == 2 && isAbsolute())
            return new RegistryKey(KEY_SPLIT);
        int i = rawKey.lastIndexOf(KEY_SPLIT);
        return new RegistryKey(rawKey.substring(0, i));
    }

    /**
     * 获取该键的层级数量。
     *
     * @return 层级数量
     */
    public int getLevel() {
        if (isRoot())
            return 1;
        char split = KEY_SPLIT.charAt(0);
        int splitCount = (int) rawKey.chars().filter(c -> c == split).count();
        return splitCount + 1;
    }

    /**
     * 获取该键和目标键的相对部分的键。
     * 目标键必须是该键本身或者父键。
     *
     * @param parent 目标键
     * @return 相对键
     * @throws IllegalArgumentException 如果目标键不是该键本身或该键的父键
     */
    @NonNull
    public RegistryKey relativeTo(@NonNull RegistryKey parent) {
        if (Objects.equals(this, parent))
            return new RegistryKey("");
        if (!isChildOf(parent))
            throw new IllegalArgumentException("Key must be child of target to get relative key.");
        return new RegistryKey(rawKey.substring(parent.rawKey.length() + (parent.isRoot() ? 0 : 1)));
    }

    /**
     * 获得归一化的键名。
     *
     * @return 归一化的键名
     */
    @NonNull
    public String toNormalize() {
        return normalizedKey;
    }

    /**
     * 获得原始键名。
     *
     * @return 原始键名。
     */
    @NonNull
    @Override
    public String toString() {
        return rawKey;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return Objects.equals(normalizedKey, ((RegistryKey) obj).normalizedKey);
    }

    @Override
    public int hashCode() {
        return normalizedKey.hashCode();
    }

    @Override
    public int compareTo(RegistryKey o) {
        return normalizedKey.compareTo(o.normalizedKey);
    }

    /**
     * 获得键的层级迭代器，顺序是从该键的第 1 层（最顶层）到最底层，详见 {@link RegistryLevelIterator}。
     * 这个不是累积迭代器，累积迭代可以通过 {@link #getLevel()} 和 {@link #getParent()} 组合实现。
     *
     * @return 层级迭代器
     */
    @NonNull
    @Override
    public Iterator<RegistryKey> iterator() {
        return new RegistryLevelIterator(this);
    }
}
