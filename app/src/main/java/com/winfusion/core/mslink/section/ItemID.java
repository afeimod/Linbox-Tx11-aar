package com.winfusion.core.mslink.section;

import androidx.annotation.NonNull;

/**
 * Represents an Item ID within the Link Target ID List of a Shell Link file.
 * Each Item ID contains binary data that describes a component of the target path,
 * such as a file, directory, or special folder.
 * <p>
 * This class wraps the raw binary data using the {@link ByteData} class for
 * structured access and manipulation.
 */
public class ItemID {

    /**
     * The binary data representing this Item ID.
     */
    private ByteData data;

    public ItemID() {
        data = new ByteData();
    }

    @NonNull
    public ByteData getData() {
        return data;
    }

    public void setData(@NonNull ByteData data) {
        this.data = data;
    }
}
