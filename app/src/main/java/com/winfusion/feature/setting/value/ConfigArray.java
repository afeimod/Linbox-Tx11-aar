package com.winfusion.feature.setting.value;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringJoiner;

/**
 * 数组元素类。
 */
public class ConfigArray extends ConfigElement implements Iterable<ConfigElement> {

    private final ArrayList<ConfigElement> elements = new ArrayList<>();

    /**
     * 向数组添加一个布尔值。
     *
     * @param value 布尔值
     */
    public void add(boolean value) {
        elements.add(new ConfigPrimitive(value));
    }

    /**
     * 向数组添加一个 float 浮点数。
     *
     * @param value float 浮点数
     */
    public void add(float value) {
        elements.add(new ConfigPrimitive(value));
    }

    /**
     * 向数组添加一个 int 整型数。
     *
     * @param value int 整型数。
     */
    public void add(int value) {
        elements.add(new ConfigPrimitive(value));
    }

    /**
     * 向数组添加一个字符串。
     *
     * @param value 字符串
     */
    public void add(@NonNull String value) {
        elements.add(new ConfigPrimitive(value));
    }

    /**
     * 向数组添加一个值元素。
     *
     * @param element 值元素
     */
    public void add(@NonNull ConfigElement element) {
        elements.add(element);
    }

    /**
     * 向数组添加一个空值。
     */
    public void addNull() {
        elements.add(ConfigNull.INSTANCE);
    }

    /**
     * 判断数组是否包含目标元素。
     *
     * @param element 目标元素
     * @return 包含则返回true，否则返回 false
     */
    public boolean contains(@NonNull ConfigElement element) {
        return elements.contains(element);
    }

    /**
     * 获取数组的大小。
     *
     * @return 数组的大小
     */
    public int size() {
        return elements.size();
    }

    /**
     * 判断数组是否是空的。
     *
     * @return 是空的则返回 true，否则返回 false
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * 获取索引位置的值。
     *
     * @param index 索引
     * @return 索引位置的值
     * @throws IndexOutOfBoundsException 如果索引超出范围（{@code index < 0 || index >= size}）
     */
    @NonNull
    public ConfigElement get(int index) {
        return elements.get(index);
    }

    @NonNull
    @Override
    public Iterator<ConfigElement> iterator() {
        return elements.iterator();
    }

    @NonNull
    @Override
    public String toString() {
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        for (Object element : elements)
            joiner.add(element.toString());
        return joiner.toString();
    }
}
