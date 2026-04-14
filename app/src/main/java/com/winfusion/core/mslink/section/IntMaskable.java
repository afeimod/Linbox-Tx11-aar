package com.winfusion.core.mslink.section;

/**
 * Represents an entity that can be converted to an integer bitmask.
 * <p>
 * This interface is typically implemented by enumerations or classes
 * that need to represent a set of flags or options as bitmasks.
 * The {@link #toMask()} method allows obtaining the integer representation of the mask.
 * </p>
 */
public interface IntMaskable {

    /**
     * Converts the implementing object to its corresponding bitmask.
     *
     * @return An integer representing the bitmask.
     */
    int toMask();
}
