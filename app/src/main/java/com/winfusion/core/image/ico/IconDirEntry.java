package com.winfusion.core.image.ico;

public class IconDirEntry {

    /**
     * Width in pixels (0 means 256 pixels)
     */
    public int width;

    /**
     * Height in pixels (0 means 256 pixels)
     */
    public int height;

    /**
     * Number of colors in the palette (0 if none)
     */
    public int colorCount;

    /**
     * Color planes
     */
    public int colorPlanes;

    /**
     * Bits per pixel
     */
    public int bitCount;

    /**
     * Size of the image data (in bytes)
     */
    public int dataSize;

    /**
     * Offset of the image data (from the file start)
     */
    public int offset;

    public IconDirEntry(int width, int height, int colorCount, int colorPlanes,
                        int bitCount, int dataSize, int offset) {
        this.width = width;
        this.height = height;
        this.colorCount = colorCount;
        this.colorPlanes = colorPlanes;
        this.bitCount = bitCount;
        this.dataSize = dataSize;
        this.offset = offset;
    }
}
