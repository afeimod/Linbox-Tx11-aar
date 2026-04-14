package com.winfusion.core.image.bmp;

public class BitmapInfoHeader {

    /**
     * The size of this header, in bytes (40)
     */
    public int iHeaderSize;

    /**
     * The bitmap width in pixels (signed integer)
     */
    public int iWidth;

    /**
     * The bitmap height in pixels (signed integer)
     */
    public int iHeight;

    /**
     * The number of color planes (must be 1)
     */
    public int sColorPlanesCount;

    /**
     * The number of bits per pixel, which is the color depth of the image. Typical values are 1, 4, 8, 16, 24 and 32.
     */
    public int sBitCount;

    /**
     * The compression method being used.
     * 0 - 	BI_RGB
     */
    public int iCompression;

    /**
     * The image size. This is the size of the raw bitmap data; a dummy 0 can be given for BI_RGB bitmaps.
     */
    public int iImageSize;

    /**
     * 	The horizontal resolution of the image. (pixel per metre, signed integer)
     */
    public int iHorizontalResolution;

    /**
     * 	The vertical resolution of the image. (pixel per metre, signed integer)
     */
    public int iVerticalResolution;

    /**
     * The number of colors in the color palette, or 0 to default to 2n
     */
    public int iColorCountInPalette;

    /**
     * The number of important colors used, or 0 when every color is important; generally ignored
     */
    public int iImportantColorCount;

    public BitmapInfoHeader(int iHeaderSize, int iWidth, int iHeight, int sColorPlanesCount, int sBitCount,
                            int iCompression, int iImageSize, int iHorizontalResolution,
                            int iVerticalResolution, int iColorCountInPalette, int iImportantColorCount) {
        this.iHeaderSize = iHeaderSize;
        this.iWidth = iWidth;
        this.iHeight = iHeight;
        this.sColorPlanesCount = sColorPlanesCount;
        this.sBitCount = sBitCount;
        this.iCompression = iCompression;
        this.iImageSize = iImageSize;
        this.iHorizontalResolution = iHorizontalResolution;
        this.iVerticalResolution = iVerticalResolution;
        this.iColorCountInPalette = iColorCountInPalette;
        this.iImportantColorCount = iImportantColorCount;
    }
    public BitmapInfoHeader(BitmapInfoHeader source) {
        this.iHeaderSize = source.iHeaderSize;
        this.iWidth = source.iWidth;
        this.iHeight = source.iHeight;
        this.sColorPlanesCount = source.sColorPlanesCount;
        this.sBitCount = source.sBitCount;
        this.iCompression = source.iCompression;
        this.iImageSize = source.iImageSize;
        this.iHorizontalResolution = source.iHorizontalResolution;
        this.iVerticalResolution = source.iVerticalResolution;
        this.iColorCountInPalette = source.iColorCountInPalette;
        this.iImportantColorCount = source.iImportantColorCount;
    }
}
