package com.winfusion.core.mslink.section;

import androidx.annotation.NonNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Represents a byte array with a specific charset encoding.
 */
public class ByteData {

    /**
     * Defined how the raw data encoded
     */
    private Charset charset;

    /**
     * Raw data in bytes
     */
    private byte[] data;

    /**
     * Default constructor using US_ASCII charset.
     */
    public ByteData() {
        this(StandardCharsets.US_ASCII);
    }

    /**
     * Constructor to initialize with specific charset and byte data.
     *
     * @param charset the charset to be used for encoding/decoding.
     * @param data the byte data.
     */
    public ByteData(@NonNull Charset charset, byte... data) {
        this.charset = charset;
        this.data = data.clone();
    }

    /**
     * Returns a cloned copy of the byte array.
     *
     * @return the byte data.
     */
    @NonNull
    public byte[] getBytes() {
        return data.clone();
    }

    /**
     * Sets the byte data directly.
     *
     * @param data the byte data.
     */
    public void setByBytes(@NonNull byte... data) {
        this.data = data.clone();
    }

    /**
     * Sets the byte data from a string using the current charset.
     *
     * @param str the string to be converted to bytes.
     */
    public void setByString(@NonNull String str) {
        data = str.getBytes(charset);
    }

    /**
     * Returns the charset used for encoding/decoding the byte data.
     *
     * @return the charset.
     */
    @NonNull
    public Charset getCharset() {
        return charset;
    }

    /**
     * Changes the charset used for encoding/decoding the byte data.
     * Converts the existing data to the new charset.
     *
     * @param charset the new charset.
     */
    public void changeCharset(@NonNull Charset charset) {
        data = new String(data, this.charset).getBytes(charset);
        this.charset = charset;
    }

    /**
     * Converts the byte data to a string using the current charset.
     *
     * @return the string representation of the byte data.
     */
    @NonNull
    @Override
    public String toString() {
        return new String(data, charset);
    }
}
