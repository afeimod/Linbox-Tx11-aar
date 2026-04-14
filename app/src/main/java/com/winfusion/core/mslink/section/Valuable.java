package com.winfusion.core.mslink.section;

/**
 * Represents a generic interface for objects that can provide a value of type {@code T}.
 *
 * @param <T> The type of value that the implementing class provides.
 */
public interface Valuable<T> {

    /**
     * Converts the implementing object to its corresponding value of type {@code T}.
     *
     * @return The value representation of the object.
     */
    T toValue();
}
