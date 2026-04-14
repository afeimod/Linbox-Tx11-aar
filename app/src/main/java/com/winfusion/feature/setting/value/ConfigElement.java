package com.winfusion.feature.setting.value;

import androidx.annotation.NonNull;

/**
 * 抽象元素类。
 */
public abstract class ConfigElement {

    /**
     * 判断值是否为 {@link ConfigPrimitive} 类型。
     *
     * @return 是则返回 true，否则返回 false
     */
    public boolean isPrimitive() {
        return this instanceof ConfigPrimitive;
    }

    /**
     * 判断值是否为 {@link ConfigArray} 类型。
     *
     * @return 是则返回 true，否则返回 false
     */
    public boolean isArray() {
        return this instanceof ConfigArray;
    }

    /**
     * 判断值是否为 {@link ConfigMapping} 类型。
     *
     * @return 是则返回 true，否则返回 false
     */
    public boolean isMapping() {
        return this instanceof ConfigMapping;
    }

    /**
     * 判断值是否为 {@link ConfigNull} 类型。
     *
     * @return 是则返回 true，否则返回 false
     */
    public boolean isNull() {
        return this instanceof ConfigNull;
    }

    /**
     * 将该对象强制转为 {@link ConfigPrimitive} 类型。
     *
     * @return 转为 ConfigPrimitive 类型的对象。
     * @throws IllegalStateException 如果类型不是 {@link ConfigPrimitive}
     */
    @NonNull
    public ConfigPrimitive getAsConfigPrimitive() {
        if (isPrimitive())
            return (ConfigPrimitive) this;
        throw new IllegalStateException("Not a ConfigPrimitive: " + this);
    }

    /**
     * 将该对象强制转为 {@link ConfigArray} 类型。
     *
     * @return 转为 ConfigArray 类型的对象。
     * @throws IllegalStateException 如果类型不是 {@link ConfigArray}
     */
    @NonNull
    public ConfigArray getAsConfigArray() {
        if (isArray())
            return (ConfigArray) this;
        throw new IllegalStateException("Not a ConfigArray: " + this);
    }

    /**
     * 将该对象强制转为 {@link ConfigMapping} 类型。
     *
     * @return 转为 ConfigMapping 类型的对象。
     * @throws IllegalStateException 如果类型不是 {@link ConfigMapping}
     */
    @NonNull
    public ConfigMapping getAsConfigMapping() {
        if (isMapping())
            return (ConfigMapping) this;
        throw new IllegalStateException("Not a ConfigMapping: " + this);
    }

    /**
     * 将该对象强制转为 {@link ConfigNull} 类型。
     *
     * @return 转为 ConfigNull 类型的对象。
     * @throws IllegalStateException 如果类型不是 {@link ConfigNull}
     */
    @NonNull
    public ConfigNull getAsConfigNull() {
        if (isNull())
            return (ConfigNull) this;
        throw new IllegalStateException("Not a ConfigNull: " + this);
    }

    /**
     * 将对象强制转为 boolean 类型。
     * 该方法必须由子类覆盖，否则一定抛出 UnsupportedOperationException
     *
     * @return boolean 类型
     */
    public boolean getAsBool() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    /**
     * 将对象强制转为 float 类型。
     * 该方法必须由子类覆盖，否则一定抛出 UnsupportedOperationException
     *
     * @return float 类型
     */
    public float getAsFloat() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    /**
     * 将对象强制转为 int 类型。
     * 该方法必须由子类覆盖，否则一定抛出 UnsupportedOperationException
     *
     * @return int 类型
     */
    public int getAsInt() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }

    /**
     * 将对象强制转为 String 类型。
     * 该方法必须由子类覆盖，否则一定抛出 UnsupportedOperationException
     *
     * @return String 类型
     */
    @NonNull
    public String getAsString() {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }
}
