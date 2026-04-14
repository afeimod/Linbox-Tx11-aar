package com.winfusion.feature.setting.value;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.internal.LinkedTreeMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * 映射元素类。
 */
public class ConfigMapping extends ConfigElement implements Map<String, ConfigElement> {

    private final LinkedTreeMap<String, ConfigElement> members = new LinkedTreeMap<>(false);

    public void add(@NonNull String key, @NonNull ConfigElement value) {
        members.put(key, value);
    }

    public void add(@NonNull String key, boolean value) {
        members.put(key, new ConfigPrimitive(value));
    }

    public void add(@NonNull String key, float value) {
        members.put(key, new ConfigPrimitive(value));
    }

    public void add(@NonNull String key, int value) {
        members.put(key, new ConfigPrimitive(value));
    }

    public void add(@NonNull String key, String value) {
        members.put(key, new ConfigPrimitive(value));
    }

    public void addNull(@NonNull String key) {
        members.put(key, ConfigNull.INSTANCE);
    }

    @Override
    public void clear() {
        members.clear();
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return members.containsKey(key);
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return members.containsValue(value);
    }

    @NonNull
    @Override
    public Set<Entry<String, ConfigElement>> entrySet() {
        return members.entrySet();
    }

    @Nullable
    @Override
    public ConfigElement get(@Nullable Object key) {
        return members.get(key);
    }

    @Override
    public boolean isEmpty() {
        return members.isEmpty();
    }

    @NonNull
    @Override
    public Set<String> keySet() {
        return members.keySet();
    }

    @Nullable
    @Override
    public ConfigElement put(String key, ConfigElement value) {
        return members.put(key, value);
    }

    @Override
    public void putAll(@NonNull Map<? extends String, ? extends ConfigElement> m) {
        members.putAll(m);
    }

    @Nullable
    @Override
    public ConfigElement remove(@Nullable Object key) {
        return members.remove(key);
    }

    @Override
    public int size() {
        return members.size();
    }

    @NonNull
    @Override
    public Collection<ConfigElement> values() {
        return members.values();
    }

    @NonNull
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        for (Map.Entry<String, ConfigElement> entry : members.entrySet())
            joiner.add(entry.getKey() + "=" + entry.getValue());
        return joiner.toString();
    }
}
