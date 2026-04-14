package com.winfusion.core.registry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.registry.data.RegistryData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * 注册表项上下文，用于编辑或查看某一个注册表项。
 */
public class RegistryItemContext {

    private RegistryKey key;
    private final Registry.RegistryNode node;

    protected RegistryItemContext(@NonNull RegistryKey key, @NonNull Registry.RegistryNode node) {
        this.key = key;
        this.node = node;
    }

    /**
     * 设置该项的键。
     * 接受一个只有一层的相对键，或原父键的直接子键。
     * 例如，如果原键为 {@code \A\B}，那么接受 {@code C} 或 {@code \A\C} 均可使该项键变为 {@code \A\C}
     *
     * @param newKey 新键
     * @throws IllegalArgumentException 如果新键不合法, 或新键已经存在
     */
    public void setSelfKey(@NonNull RegistryKey newKey) {
        if (Objects.equals(key, newKey) || Objects.equals(node.selfKey, newKey))
            return;

        if (key.isRoot())
            throw new IllegalArgumentException("Cannot change the key of root item");

        if (newKey.isAbsolute() || newKey.getLevel() != 1)
            throw new IllegalArgumentException("New key must be relative with 1 level: " + newKey);

        RegistryKey selfKey1 = newKey.getSelf();
        RegistryKey key1 = key.getParent().resolve(selfKey1);
        if (node.getRegistry().hasItem(key1))
            throw new IllegalArgumentException("New key is already exists: " + key1);

        Registry.RegistryNode parentNode = node.getRegistry().getNode(key.getParent());
        if (parentNode == null)
            throw new IllegalArgumentException("Parent node must not be null.");
        parentNode.children.remove(node.selfKey);
        parentNode.children.put(selfKey1, node);

        key = key1;
        node.selfKey = selfKey1;
    }

    /**
     * 设置项的默认值。
     *
     * @param data 值
     */
    public void setDefaultValue(@Nullable RegistryData data) {
        node.defaultValue = data;
    }

    /**
     * 获取项的默认值。
     *
     * @return 存在则返回默认值，否则返回 null
     */
    @Nullable
    public RegistryData getDefaultValue() {
        return node.defaultValue;
    }

    /**
     * 判断值是否存在。
     *
     * @param name 值名
     * @return 存在则返回 true， 否则返回 false
     */
    public boolean hasValue(@NonNull String name) {
        return node.namedValues.containsKey(name);
    }

    /**
     * 添加一个值。
     *
     * @param name 值名
     * @param data 值数据
     * @throws IllegalArgumentException 如果值已经存在
     */
    public void addValue(@NonNull String name, @NonNull RegistryData data) {
        if (hasValue(name))
            throw new IllegalArgumentException("Value has already exists: " + name);
        node.namedValues.put(name, data);
    }

    /**
     * 删除一个值
     *
     * @param name 值名
     * @throws IllegalArgumentException 如果值不存在
     */
    public void deleteValue(@NonNull String name) {
        if (node.namedValues.remove(name) == null)
            throw new IllegalArgumentException("Value is not found: " + name);
    }

    /**
     * 设置一个值为新值。
     *
     * @param name 值名
     * @param data 新值数据
     * @throws IllegalArgumentException 如果要设置的值不存在
     */
    public void setValue(@NonNull String name, @NonNull RegistryData data) {
        if (!hasValue(name))
            throw new IllegalArgumentException("Value is not found: " + name);
        node.namedValues.put(name, data);
    }

    /**
     * 放入或覆盖一个值。
     *
     * @param name 值名称
     * @param data 值数据
     */
    public void putValue(@NonNull String name, @NonNull RegistryData data) {
        node.namedValues.put(name, data);
    }

    /**
     * 获取该项所有直接子项的相对键。
     *
     * @return 子项键的列表
     */
    @NonNull
    public List<RegistryKey> getKeyOfChildren() {
        ArrayList<RegistryKey> list = new ArrayList<>();
        for (Map.Entry<RegistryKey, Registry.RegistryNode> entry : node.children.entrySet())
            list.add(entry.getKey());
        return list;
    }

    /**
     * 获取该项所有直接子项的完整键。
     *
     * @return 子项键的列表
     */
    @NonNull
    public List<RegistryKey> getRealKeyOfChildren() {
        ArrayList<RegistryKey> list = new ArrayList<>();
        for (Map.Entry<RegistryKey, Registry.RegistryNode> entry : node.children.entrySet())
            list.add(key.resolve(entry.getKey()));
        return list;
    }

    /**
     * 判断当前项是否存在目标子项。
     *
     * @param childKey 目标子项的键
     * @return 存在则返回 true，否则返回 false
     */
    public boolean hasChild(@NonNull RegistryKey childKey) {
        RegistryKey childSelfKey = null;
        if (childKey.isDirectChildOf(key))
            childSelfKey = childKey.getSelf();
        else if (childKey.getLevel() == 1)
            childSelfKey = childKey;
        return childSelfKey != null && node.children.containsKey(childSelfKey);
    }

    /**
     * 获取项的值。
     *
     * @param name 值名称
     * @return 存在则返回值的数据，否则返回 null
     */
    @Nullable
    public RegistryData getValue(@NonNull String name) {
        return node.namedValues.get(name);
    }

    /**
     * 创建并返回项的视图。
     *
     * @return 该项的视图。
     */
    @NonNull
    public RegistryItem toItem() {
        RegistryItem item = new RegistryItem();
        item.key = key;
        item.defaultValue = node.defaultValue;
        item.namedValues.putAll(node.namedValues);
        return item;
    }

    /**
     * 获取该项的键。
     *
     * @return 该项的键。
     */
    @NonNull
    public RegistryKey getKey() {
        return key;
    }

    /**
     * 注册表项的视图类。
     */
    public static class RegistryItem {

        public RegistryKey key;
        public RegistryData defaultValue;
        public Map<String, RegistryData> namedValues = new TreeMap<>();
    }
}
