package com.winfusion.feature.setting.value;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * 基本数据类型元素类。
 */
public class ConfigPrimitive extends ConfigElement {

    private final Object value;
    private final Type type;

    private enum Type {
        Bool,
        Float,
        Int,
        String
    }

    public ConfigPrimitive(boolean value) {
        this.value = value;
        this.type = Type.Bool;
    }

    public ConfigPrimitive(float value) {
        this.value = value;
        this.type = Type.Float;
    }

    public ConfigPrimitive(int value) {
        this.value = value;
        this.type = Type.Int;
    }

    public ConfigPrimitive(@NonNull String value) {
        this.value = value;
        this.type = Type.String;
    }

    public boolean isBool() {
        return type == Type.Bool;
    }

    public boolean isFloat() {
        return type == Type.Float;
    }

    public boolean isInt() {
        return type == Type.Int;
    }

    public boolean isString() {
        return type == Type.String;
    }

    @Override
    public boolean getAsBool() {
        if (isBool())
            return (boolean) value;
        throw new IllegalStateException("Not a bool: " + this);
    }

    @Override
    public float getAsFloat() {
        if (isFloat())
            return (float) value;
        throw new IllegalStateException("Not a float: " + this);
    }

    @Override
    public int getAsInt() {
        if (isInt())
            return (int) value;
        throw new IllegalStateException("Not a int: " + this);
    }

    @NonNull
    @Override
    public String getAsString() {
        if (isString())
            return (String) value;
        throw new IllegalStateException("Not a string: " + this);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        return Objects.equals(value, ((ConfigPrimitive) obj).value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return value.toString();
    }
}
