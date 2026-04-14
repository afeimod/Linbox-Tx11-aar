package com.winfusion.core.image.ico;

import static com.winfusion.core.image.bmp.BMPConstants.BI_RGB;
import static com.winfusion.core.image.bmp.BMPConstants.BMPFileHeader;
import static com.winfusion.core.image.bmp.BMPConstants.BMPFileHeaderSize;
import static com.winfusion.core.image.bmp.BMPConstants.ColorTableSize;
import static com.winfusion.core.image.bmp.BMPConstants.DIBHeaderSize;
import static com.winfusion.core.image.ico.IconConstants.IconDirEntrySize;
import static com.winfusion.core.image.ico.IconConstants.IconHeaderSize;
import static com.winfusion.core.image.ico.IconConstants.IconType;
import static com.winfusion.core.image.ico.IconConstants.PNGHeaderData;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.winfusion.core.image.bmp.BMPDecoder;
import com.winfusion.core.image.bmp.BitmapInfoHeader;
import com.winfusion.core.image.bmp.ColorEntry;
import com.winfusion.core.image.bmp.exception.BadBMPFormatException;
import com.winfusion.core.image.ico.exception.BadIconFormatException;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public final class IconDecoder {

    private static final String TAG = "IconParser";

    private IconDecoder() {

    }

    @NonNull
    public static List<Bitmap> parseBitmapsFromIcon(@NonNull ByteBuffer buffer)
            throws BadIconFormatException {

        List<Bitmap> bitmaps;
        IconHeader header;
        List<IconDirEntry> iconDirEntries;

        try {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            header = internalCheckIconHeader(buffer);
            iconDirEntries = internalGetEntries(buffer, header);
            bitmaps = internalGetBitmapsFromEntries(buffer, iconDirEntries);
        } catch (BufferUnderflowException e) {
            throw new BadIconFormatException("Unexpected EOF.", e);
        }

        return bitmaps;
    }

    @NonNull
    private static IconHeader internalCheckIconHeader(@NonNull ByteBuffer buffer)
            throws BadIconFormatException {

        buffer.position(0);

        short reserved = buffer.getShort();
        short type = buffer.getShort();
        short counts = buffer.getShort();

        if (reserved != 0 || type != IconType || counts == 0)
            throw new BadIconFormatException("Icon header is invalid.");

        return new IconHeader(counts);
    }

    @NonNull
    private static List<IconDirEntry> internalGetEntries(@NonNull ByteBuffer buffer, @NonNull IconHeader header)
            throws BadIconFormatException {

        ArrayList<IconDirEntry> entries = new ArrayList<>();
        buffer.position(6);

        for (int i = 0; i < header.imageCount; i++) {
            short width = buffer.get();
            short height = buffer.get();
            short colorCountInPalette = buffer.get();
            short reserved = buffer.get();
            int colorPlanes = buffer.getShort();
            int bitsPreSize = buffer.getShort();
            long dataSize = buffer.getInt();
            long offset = buffer.getInt();

            if (reserved != 0 || dataSize == 0 || (int) offset < i * IconDirEntrySize + IconHeaderSize ||
                    (colorPlanes != 0 && colorPlanes != 1) || (bitsPreSize != 1 && bitsPreSize != 4
                    && bitsPreSize != 8 && bitsPreSize != 24 && bitsPreSize != 32)) {
                throw new BadIconFormatException("Icon dir entry is invalid");
            }

            if ((int) dataSize < 0)
                throw new BadIconFormatException("Image size is too large.");

            if ((int) offset < 0)
                throw new BadIconFormatException("Image offset is too large.");

            entries.add(new IconDirEntry(
                    width, height, colorCountInPalette, colorPlanes,
                    bitsPreSize, (int) dataSize, (int) offset
            ));
        }

        return entries;
    }

    @NonNull
    private static List<Bitmap> internalGetBitmapsFromEntries(@NonNull ByteBuffer buffer,
                                                              @NonNull List<IconDirEntry> entries) {

        ArrayList<Bitmap> bitmaps = new ArrayList<>();

        for (IconDirEntry entry : entries) {
            byte[] bytes = new byte[(int) entry.dataSize];
            buffer.position(entry.offset);
            buffer.get(bytes);
            Bitmap bitmap;

            try {
                if (internalCheckEncodeFormat(bytes) == IconEncodeFormat.BMP) {
                    bitmap = internalParseImageDataAsBMP(internalFixBMPHeader(bytes, entry));
                } else {
                    bitmap = internalParseImageDataAsPNG(bytes);
                }
            } catch (BadIconFormatException e) {
                Log.d(TAG, "Failed to parse a image from icon.", e);
                continue;
            }

            bitmaps.add(bitmap);
        }

        return bitmaps;
    }
    @NonNull
    private static Bitmap internalParseImageDataAsBMP(byte[] bytes) throws BadIconFormatException {

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.position(BMPFileHeaderSize);

        try {
            BitmapInfoHeader header = BMPDecoder.parseHeader(buffer);
            BitmapInfoHeader xorHeader = new BitmapInfoHeader(header);
            BitmapInfoHeader andHeader = new BitmapInfoHeader(header);

            xorHeader.iHeight /= 2;

            andHeader.iHeight /= 2;
            andHeader.sBitCount = 1;
            andHeader.iColorCountInPalette = 2;
            andHeader.iCompression = BI_RGB;

            Bitmap bitmap = BMPDecoder.parseByHeader(buffer, xorHeader);
            ColorEntry[] andColorTable = new ColorEntry[]{
                    new ColorEntry(255, 255, 255),
                    new ColorEntry(0, 0, 0)
            };

            if (header.sBitCount != 32) {
                Bitmap andBitmap = BMPDecoder.parseByHeaderAndColorTable(buffer, andHeader, andColorTable);

                for (int y = 0; y < xorHeader.iHeight; y++) {
                    for (int x = 0; x < xorHeader.iWidth; x++) {
                        int color = bitmap.getPixel(x, y);
                        bitmap.setPixel(
                                x, y,
                                Color.argb(
                                        andBitmap.getPixel(x, y),
                                        Color.red(color), Color.green(color),
                                        Color.blue(color)
                                )
                        );
                    }
                }
            }

            return bitmap;
        } catch (BadBMPFormatException e) {
            throw new BadIconFormatException("Failed to parse as bmp.", e);
        }
    }

    @NonNull
    private static Bitmap internalParseImageDataAsPNG(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    @NonNull
    private static IconEncodeFormat internalCheckEncodeFormat(@NonNull byte[] data)
            throws BadIconFormatException {

        try {
            for (int i = 0; i < PNGHeaderData.length; i++) {
                if (data[i] != PNGHeaderData[i])
                    return IconEncodeFormat.BMP;
            }
        } catch (IndexOutOfBoundsException e) {
            throw new BadIconFormatException("Image data is too short.", e);
        }

        return IconEncodeFormat.PNG;
    }

    @NonNull
    private static byte[] internalFixBMPHeader(@NonNull byte[] data, @NonNull IconDirEntry entry)
            throws BadIconFormatException {

        int fileSize = BMPFileHeaderSize + entry.dataSize;

        if (fileSize < BMPFileHeaderSize + DIBHeaderSize)
            throw new BadIconFormatException("Image is too large.");

        ByteBuffer buffer = ByteBuffer.allocate(fileSize);

        buffer.position(0);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.put(BMPFileHeader); // bfType
        buffer.putInt(fileSize); // bfSize
        buffer.putShort((short) 0); // bfReserved1
        buffer.putShort((short) 0); // bfReserved2
        buffer.putInt(BMPFileHeaderSize + DIBHeaderSize +
                (entry.bitCount < 24 ? ColorTableSize : 0)); // bfPixelsOffset
        buffer.put(data);

        return buffer.array();
    }

    private enum IconEncodeFormat {
        BMP,
        PNG
    }

    private static class IconHeader {
        public int imageCount;

        public IconHeader(int imageCounts) {
            this.imageCount = imageCounts;
        }
    }
}
