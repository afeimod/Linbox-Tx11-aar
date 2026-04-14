package com.winfusion.core.image.bmp;

import static com.winfusion.core.image.bmp.BMPConstants.BI_RGB;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.image.bmp.exception.BadBMPFormatException;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public final class BMPDecoder {

    private BMPDecoder() {

    }

    @NonNull
    public static BitmapInfoHeader parseHeader(@NonNull ByteBuffer buffer)
            throws BadBMPFormatException {

        try {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            int iHeaderSize = buffer.getInt();
            int iWidth = buffer.getInt();
            int iHeight = buffer.getInt();
            int sColorPlanesCount = buffer.getShort();
            int sBitCount = buffer.getShort();
            int iCompression = buffer.getInt();
            int iImageSize = buffer.getInt();
            int iHorizontalResolution = buffer.getInt();
            int iVerticalResolution = buffer.getInt();
            int iColorCountInPalette = buffer.getInt();
            int iImportantColorCount = buffer.getInt();

            if (iHeaderSize != 40)
                throw new BadBMPFormatException("Unsupported header size: " + iHeaderSize);

            if (iWidth == 0 || iHeight == 0)
                throw new BadBMPFormatException("Invalid image width and height: " + iWidth + "x" + iHeight);

            if (sColorPlanesCount != 1)
                throw new BadBMPFormatException("Invalid number of color planes: " + sColorPlanesCount);

            if (sBitCount != 1 && sBitCount != 4 && sBitCount != 8 &&
                    sBitCount != 16 && sBitCount != 24 && sBitCount != 32)
                throw new BadBMPFormatException("Invalid bit count: " + sBitCount);

            if (iCompression != BI_RGB)
                throw new BadBMPFormatException("Unsupported compression method: " + iCompression);

            return new BitmapInfoHeader(
                    iHeaderSize, iWidth, iHeight, sColorPlanesCount, sBitCount, iCompression,
                    iImageSize, iHorizontalResolution, iVerticalResolution, iColorCountInPalette,
                    iImportantColorCount
            );
        } catch (BufferUnderflowException e) {
            throw new BadBMPFormatException("Unexpected EOF.", e);
        }
    }

    @NonNull
    public static Bitmap parseByHeader(@NonNull ByteBuffer buffer, @NonNull BitmapInfoHeader header)
            throws BadBMPFormatException {

        ColorEntry[] colorTable = null;

        if (header.sBitCount <= 8) {
            colorTable = internalParseColorTable(buffer, header);
            if (colorTable.length == 0)
                throw new BadBMPFormatException("Required ColorTable but it is empty.");
        }

        return parseByHeaderAndColorTable(buffer, header, colorTable);
    }

    @NonNull
    public static Bitmap parseByHeaderAndColorTable(@NonNull ByteBuffer buffer,
                                                    @NonNull BitmapInfoHeader header,
                                                    @Nullable ColorEntry[] colorTable)
            throws BadBMPFormatException {

        buffer.order(ByteOrder.LITTLE_ENDIAN);

        try {
            return switch (header.sBitCount) {
                case 1 -> internalParseBit1(buffer, header, Objects.requireNonNull(colorTable));
                case 4 -> internalParseBit4(buffer, header, Objects.requireNonNull(colorTable));
                case 8 -> internalParseBit8(buffer, header, Objects.requireNonNull(colorTable));
                case 24 -> internalParseBit24(buffer, header);
                case 32 -> internalParseBit32(buffer, header);
                default ->
                        throw new BadBMPFormatException("Unsupported bit count: " + header.sBitCount);
            };
        } catch (BufferUnderflowException e) {
            throw new BadBMPFormatException("Unexpected EOF.");
        }
    }

    @NonNull
    private static ColorEntry[] internalParseColorTable(@NonNull ByteBuffer buffer,
                                                        @NonNull BitmapInfoHeader header) {

        ColorEntry[] colorTable = new ColorEntry[header.iColorCountInPalette];
        for (int i = 0; i < colorTable.length; i++) {
            colorTable[i] = internalParseColorEntry(buffer);
        }
        return colorTable;
    }

    @NonNull
    private static ColorEntry internalParseColorEntry(@NonNull ByteBuffer buffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        int b = Byte.toUnsignedInt(buffer.get());
        int g = Byte.toUnsignedInt(buffer.get());
        int r = Byte.toUnsignedInt(buffer.get());
        int reserved = Byte.toUnsignedInt(buffer.get());
        return new ColorEntry(r, g, b);
    }

    private static void internalGetColorTable(@NonNull ColorEntry[] colorTable, @NonNull byte[] ar,
                                              @NonNull byte[] ag, @NonNull byte[] ab) {

        for (int i = 0; i < colorTable.length; i++) {
            ar[i] = (byte) colorTable[i].bRed;
            ag[i] = (byte) colorTable[i].bGreen;
            ab[i] = (byte) colorTable[i].bBlue;
        }
    }

    @NonNull
    private static Bitmap internalParseBit1(@NonNull ByteBuffer buffer, @NonNull BitmapInfoHeader header,
                                            @NonNull ColorEntry[] colorTable) {

        byte[] ar = new byte[colorTable.length];
        byte[] ag = new byte[colorTable.length];
        byte[] ab = new byte[colorTable.length];

        internalGetColorTable(colorTable, ar, ag, ab);

        Bitmap bitmap = Bitmap.createBitmap(header.iWidth, header.iHeight, Bitmap.Config.ARGB_8888);

        int bitsPerLine = header.iWidth;
        if (bitsPerLine % 32 != 0) {
            bitsPerLine = (bitsPerLine / 32 + 1) * 32;
        }

        int bytesPerLine = bitsPerLine / 8;
        int[] line = new int[bytesPerLine];

        for (int y = header.iHeight - 1; y >= 0; y--) {
            for (int i = 0; i < bytesPerLine; i++) {
                line[i] = Byte.toUnsignedInt(buffer.get());
            }

            for (int x = 0; x < header.iWidth; x++) {
                int i = x / 8;
                int v = line[i];
                int b = x % 8;
                int index = (v >> (7 - b)) & 1;

                bitmap.setPixel(x, y, Color.rgb(ar[index], ag[index], ab[index]));
            }
        }

        return bitmap;
    }

    @NonNull
    private static Bitmap internalParseBit4(@NonNull ByteBuffer buffer, @NonNull BitmapInfoHeader header,
                                            @NonNull ColorEntry[] colorTable) {

        byte[] ar = new byte[colorTable.length];
        byte[] ag = new byte[colorTable.length];
        byte[] ab = new byte[colorTable.length];

        internalGetColorTable(colorTable, ar, ag, ab);

        Bitmap bitmap = Bitmap.createBitmap(header.iWidth, header.iHeight, Bitmap.Config.ARGB_8888);

        int bitsPerLine = header.iWidth * 4;
        if (bitsPerLine % 32 != 0) {
            bitsPerLine = (bitsPerLine / 32 + 1) * 32;
        }
        int bytesPerLine = bitsPerLine / 8;

        int[] line = new int[bytesPerLine];

        for (int y = header.iHeight - 1; y >= 0; y--) {
            for (int i = 0; i < bytesPerLine; i++) {
                int b = Byte.toUnsignedInt(buffer.get());
                line[i] = b;
            }

            for (int x = 0; x < header.iWidth; x++) {
                int b = x / 2;
                int i = x % 2;
                int n = line[b];
                int index = (n >> (4 * (1 - i))) & 0xF;
                bitmap.setPixel(x, y, Color.rgb(ar[index], ag[index], ab[index]));
            }
        }

        return bitmap;
    }

    @NonNull
    private static Bitmap internalParseBit8(@NonNull ByteBuffer buffer, @NonNull BitmapInfoHeader header,
                                            @NonNull ColorEntry[] colorTable) {

        byte[] ar = new byte[colorTable.length];
        byte[] ag = new byte[colorTable.length];
        byte[] ab = new byte[colorTable.length];

        internalGetColorTable(colorTable, ar, ag, ab);

        Bitmap bitmap = Bitmap.createBitmap(header.iWidth, header.iHeight, Bitmap.Config.ARGB_8888);

        int[] c = new int[header.iColorCountInPalette];
        for (int i = 0; i < c.length; i++) {
            int r = colorTable[i].bRed;
            int g = colorTable[i].bGreen;
            int b = colorTable[i].bBlue;
            c[i] = (r << 16) | (g << 8) | (b);
        }

        int dataPerLine = header.iWidth;
        int bytesPerLine = dataPerLine;
        if (bytesPerLine % 4 != 0) {
            bytesPerLine = (bytesPerLine / 4 + 1) * 4;
        }
        int padBytesPerLine = bytesPerLine - dataPerLine;

        for (int y = header.iHeight - 1; y >= 0; y--) {
            for (int x = 0; x < header.iWidth; x++) {
                int b = Byte.toUnsignedInt(buffer.get());
                bitmap.setPixel(x, y, Color.argb(255,
                        Color.red(c[b]), Color.green(c[b]), Color.blue(c[b])));
            }

            buffer.position(buffer.position() + padBytesPerLine);
        }

        return bitmap;
    }

    @NonNull
    private static Bitmap internalParseBit24(@NonNull ByteBuffer buffer,
                                             @NonNull BitmapInfoHeader header) {

        Bitmap bitmap = Bitmap.createBitmap(header.iWidth, header.iHeight, Bitmap.Config.ARGB_8888);

        int dataPerLine = header.iWidth * 3;
        int bytesPerLine = dataPerLine;
        if (bytesPerLine % 4 != 0) {
            bytesPerLine = (bytesPerLine / 4 + 1) * 4;
        }
        int padBytesPerLine = bytesPerLine - dataPerLine;

        for (int y = header.iHeight - 1; y >= 0; y--) {
            for (int x = 0; x < header.iWidth; x++) {
                int b = Byte.toUnsignedInt(buffer.get());
                int g = Byte.toUnsignedInt(buffer.get());
                int r = Byte.toUnsignedInt(buffer.get());

                bitmap.setPixel(x, y, Color.rgb(r, g, b));
            }

            buffer.position(buffer.position() + padBytesPerLine);
        }

        return bitmap;
    }

    @NonNull
    private static Bitmap internalParseBit32(@NonNull ByteBuffer buffer,
                                             @NonNull BitmapInfoHeader header) {

        Bitmap bitmap = Bitmap.createBitmap(header.iWidth, header.iHeight, Bitmap.Config.ARGB_8888);

        for (int y = header.iHeight - 1; y >= 0; y--) {
            for (int x = 0; x < header.iWidth; x++) {
                int b = Byte.toUnsignedInt(buffer.get());
                int g = Byte.toUnsignedInt(buffer.get());
                int r = Byte.toUnsignedInt(buffer.get());
                int a = Byte.toUnsignedInt(buffer.get());
                bitmap.setPixel(x, y, Color.argb(a, r, g, b));
            }
        }

        return bitmap;
    }
}
