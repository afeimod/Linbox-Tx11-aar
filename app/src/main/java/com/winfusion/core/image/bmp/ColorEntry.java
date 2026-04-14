package com.winfusion.core.image.bmp;

public class ColorEntry {

    /**
     * The red component, which should be in the range of 0..255.
     */
    public int bRed;

    /**
     * The green component, which should be in the range of 0..255.
     */
    public int bGreen;

    /**
     * The blue component, which should be in the range of 0..255.
     */
    public int bBlue;

    public ColorEntry(int r, int g, int b) {
        bRed = r;
        bGreen = g;
        bBlue = b;
    }
}
