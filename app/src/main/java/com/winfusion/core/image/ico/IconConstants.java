package com.winfusion.core.image.ico;

public final class IconConstants {

    private IconConstants() {

    }

    public static final int IconHeaderSize = 6;
    public static final int IconDirEntrySize = 16;
    public static final int IconType = 1;
    public static final byte[] PNGHeaderData =
            new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
}
