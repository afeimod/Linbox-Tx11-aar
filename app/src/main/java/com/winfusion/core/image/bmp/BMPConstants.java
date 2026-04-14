package com.winfusion.core.image.bmp;

public final class BMPConstants {

    private BMPConstants() {

    }

    public static final byte[] BMPFileHeader = new byte[]{'B', 'M'};
    public static final int BMPFileHeaderSize = 14;
    public static final int DIBHeaderSize = 40;
    public static final int ColorTableSize = 4;
    public static final int BI_RGB = 0;
}
