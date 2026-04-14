package com.winfusion.core.registry;

import androidx.annotation.NonNull;

import java.util.Iterator;

/**
 * 注册表键层级迭代器，从最顶层迭代到最底层。
 */
public class RegistryLevelIterator implements Iterator<RegistryKey> {

    private final String[] rawKeys;
    private final int length;
    private int currentIndex;

    protected RegistryLevelIterator(@NonNull RegistryKey key) {
        rawKeys = key.toString().split("\\\\", -1);
        if (rawKeys[0].isEmpty())
            rawKeys[0] = RegistryKey.KEY_SPLIT;
        length = rawKeys.length;
        currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < length;
    }

    @Override
    public RegistryKey next() {
        return new RegistryKey(rawKeys[currentIndex++]);
    }
}
