package com.winfusion.core.registry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.registry.data.RegistryData;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * 注册表类，用于维护实际的注册表数据。
 */
public class Registry {

    private final RegistryNode root;
    private final MetaData metaData;

    public Registry() {
        metaData = new MetaData();
        root = new RegistryNode();
        root.selfKey = new RegistryKey("/");
    }

    /**
     * 判断注册表中是否存在该键。
     *
     * @param key 键
     * @return 存在则返回 true，否则返回 false
     */
    public boolean hasItem(@NonNull RegistryKey key) {
        return getNode(key) != null;
    }

    /**
     * 获取该键对应的注册表项上下文。
     *
     * @param key 键
     * @return 如果该键存在，则创建并返回上下文对象，否则返回 null
     */
    @Nullable
    public RegistryItemContext getItemContext(@NonNull RegistryKey key) {
        RegistryNode node = getNode(key);
        return node == null ? null : new RegistryItemContext(key, node);
    }

    /**
     * 创建一个注册表项，并返回这个项的上下文。
     *
     * @param key 键
     * @return 创建成功则返回上下文
     * @throws IllegalArgumentException 如果键不是绝对键
     * @throws IllegalStateException    如果项已经存在
     */
    @NonNull
    public RegistryItemContext createItem(@NonNull RegistryKey key) {
        return new RegistryItemContext(key, createNode(key));
    }

    /**
     * 删除一个注册表项。
     *
     * @param key 键
     * @throws IllegalArgumentException 如果删除的是根项或键不存在
     */
    public void deleteItem(@NonNull RegistryKey key) {
        deleteNode(key);
    }

    /**
     * 将源注册表合并到当前注册表。
     * 会覆盖已经存在的项的值和数据。
     * 不会将项的默认值设为 null。
     *
     * @param source 源注册表
     */
    public void merge(@NonNull Registry source) {
        if (this == source)
            return;
        mergeNode(source.root, root);
        mergeMetaData(source.metaData);
    }

    /**
     * 获取注册表元数据，返回的对象可以直接修改。
     * 主要用于存放解析器产生的数据，以便回编译。
     *
     * @return 元数据对象
     */
    @NonNull
    public MetaData getMetaData() {
        return metaData;
    }

    @Nullable
    protected RegistryNode getNode(@NonNull RegistryKey key) {
        if (!key.isAbsolute())
            return null;
        if (key.isRoot())
            return root;

        Iterator<RegistryKey> iterator = key.iterator();
        // key.isAbsolute() has made sure that the first element is root.selfKey, so just skip it
        iterator.next();
        RegistryNode node = root;

        while (iterator.hasNext()) {
            RegistryKey part = iterator.next();
            node = node.children.get(part);
            if (node == null)
                return null;
        }

        return node;
    }

    @NonNull
    private RegistryNode createNode(@NonNull RegistryKey key) {
        if (!key.isAbsolute())
            throw new IllegalArgumentException("Key must be absolute: " + key);

        Iterator<RegistryKey> iterator = key.iterator();
        iterator.next(); // skip root
        RegistryNode node = root;
        boolean createdNew = false;

        while (iterator.hasNext()) {
            RegistryKey part = iterator.next();
            RegistryNode child = node.children.get(part);
            if (child == null) {
                child = new RegistryNode();
                child.selfKey = part;
                node.children.put(part, child);
                createdNew = true;
            }
            node = child;
        }

        if (!createdNew) {
            throw new IllegalStateException("Node already exists: " + key);
        }

        return node;
    }

    private void deleteNode(@NonNull RegistryKey key) {
        if (key.isRoot())
            throw new IllegalArgumentException("Root node cannot be deleted");
        RegistryNode parent = getNode(key.getParent());
        if (parent == null)
            throw new IllegalArgumentException("Key not found: " + key);
        if (parent.children.remove(key.getSelf()) == null)
            throw new IllegalArgumentException("Key not found: " + key);
    }

    private void mergeNode(@NonNull RegistryNode source, @NonNull RegistryNode target) {
        if (source.defaultValue != null)
            target.defaultValue = source.defaultValue;
        target.namedValues.putAll(source.namedValues);
        for (Map.Entry<RegistryKey, RegistryNode> entry : source.children.entrySet()) {
            RegistryNode node = target.children.get(entry.getKey());
            if (node == null) {
                node = new RegistryNode(entry.getValue());
                target.children.put(entry.getKey(), node);
            } else {
                mergeNode(entry.getValue(), node);
            }
        }
    }

    private void mergeMetaData(@NonNull MetaData metaData) {
        if (this.metaData.sid == null)
            this.metaData.sid = metaData.sid;
        if (this.metaData.arch == null)
            this.metaData.arch = metaData.arch;
    }

    /**
     * 注册表项节点类。
     */
    public class RegistryNode {

        public RegistryKey selfKey;
        public RegistryData defaultValue;
        public final TreeMap<String, RegistryData> namedValues = new TreeMap<>();
        public final TreeMap<RegistryKey, RegistryNode> children = new TreeMap<>();

        private RegistryNode() {

        }

        private RegistryNode(@NonNull RegistryNode node) {
            selfKey = node.selfKey;
            defaultValue = node.defaultValue;
            namedValues.putAll(node.namedValues);

            for (Map.Entry<RegistryKey, RegistryNode> entry : node.children.entrySet())
                children.put(entry.getKey(), new RegistryNode(entry.getValue()));
        }

        @NonNull
        public Registry getRegistry() {
            return Registry.this;
        }
    }

    /**
     * 注册表元数据类，被解析器使用。
     */
    public static class MetaData {

        public SID sid;
        public String arch;
    }
}
