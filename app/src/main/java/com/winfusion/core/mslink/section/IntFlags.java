package com.winfusion.core.mslink.section;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * A utility class for managing a set of integer-based flags.
 *
 * @param <T> The type of flags, which must implement the {@link IntMaskable} interface.
 */
public final class IntFlags<T extends IntMaskable> implements Cloneable {

    private int intFlags;

    /**
     * Constructs an {@code IntFlags} instance with no flags set.
     */
    public IntFlags() {
        this(0);
    }

    /**
     * Constructs an {@code IntFlags} instance with the specified initial flags.
     *
     * @param flags The initial flags to set.
     */
    public IntFlags(int flags) {
        this.intFlags = flags;
    }

    /**
     * Adds one or more flags to the current set of flags.
     *
     * @param flags The flags to add.
     */
    @SafeVarargs
    public final void addFlags(@NonNull T... flags) {
        for (T flag : flags)
            intFlags |= flag.toMask();
    }

    /**
     * Adds raw integer flags to the current set of flags.
     *
     * @param flags The raw integer flags to add.
     */
    public void addFlags(int flags) {
        intFlags |= flags;
    }

    /**
     * Removes one or more flags from the current set of flags.
     *
     * @param flags The flags to remove.
     */
    @SafeVarargs
    public final void removeFlags(@NonNull T... flags) {
        for (T flag : flags)
            intFlags &= ~flag.toMask();
    }

    /**
     * Removes raw integer flags from the current set of flags.
     *
     * @param flags The raw integer flags to remove.
     */
    public void removeFlags(int flags) {
        intFlags &= ~flags;
    }

    /**
     * Replaces the current set of flags with the specified integer flags.
     *
     * @param flags The new flags to set.
     */
    public void setIntFlags(int flags) {
        intFlags = flags;
    }

    /**
     * Retrieves the current set of flags as an integer.
     *
     * @return The current flags.
     */
    public int getIntFlags() {
        return intFlags;
    }

    /**
     * Checks whether the specified flag is set.
     *
     * @param flag The flag to check.
     * @return {@code true} if the flag is set, {@code false} otherwise.
     */
    public boolean hasFlag(@NonNull T flag) {
        return (intFlags & flag.toMask()) != 0;
    }

    /**
     * Checks whether the specified raw flag is set.
     *
     * @param flag The raw flag to check.
     * @return {@code true} if the flag is set, {@code false} otherwise.
     */
    public boolean hasFlag(int flag) {
        return (intFlags & flag) != 0;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(intFlags);
    }

    @Override
    public boolean equals(@Nullable Object flags) {
        if (flags == null)
            return false;
        if (flags instanceof IntFlags<?>)
            return intFlags == ((IntFlags<?>) flags).intFlags;
        return false;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        IntFlags<T> flags = (IntFlags<T>) super.clone();
        flags.intFlags = intFlags;
        return flags;
    }

    @NonNull
    @Override
    public String toString() {
        return Integer.toBinaryString(intFlags);
    }
}
