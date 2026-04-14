package com.winfusion.core.registry;

import androidx.annotation.NonNull;

import com.winfusion.core.registry.exception.RegistryExporterException;

import java.util.Map;
import java.util.TreeMap;

/**
 * 注册表导出器的基类，提供了一些导出器需要的方法。
 */
public abstract class RegistryExporter {

    protected final Registry registry;
    protected final TreeMap<RegistryKey, Registry.RegistryNode> nodes = new TreeMap<>();

    public RegistryExporter(@NonNull Registry registry) {
        this.registry = registry;
    }

    /**
     * 找到某一个注册表项节点对象，并将该节点以及该节点的所有子节点放入 {@link #nodes} 中。
     *
     * @param key 注册表项的键
     * @throws RegistryExporterException 如果注册表项不存在
     */
    protected void findNodes(@NonNull RegistryKey key) throws RegistryExporterException {
        Registry.RegistryNode node = registry.getNode(key);
        if (node == null)
            throw new RegistryExporterException("Key not found: " + key);
        findAllNodes(node, key);
    }

    /**
     * 递归的查找某一个注册表项节点的所有子节点，按照深度优先的方式，并将结果放入 {@link #nodes} 中。
     *
     * @param node    注册表项节点
     * @param nodeKey 注册表项的键
     */
    private void findAllNodes(@NonNull Registry.RegistryNode node, @NonNull RegistryKey nodeKey) {
        if (!node.namedValues.isEmpty() || node.defaultValue != null || node.children.isEmpty())
            nodes.put(nodeKey, node);
        for (Map.Entry<RegistryKey, Registry.RegistryNode> entry : node.children.entrySet()) {
            RegistryKey newKey = nodeKey.resolve(entry.getKey());
            findAllNodes(entry.getValue(), newKey);
        }
    }
}
