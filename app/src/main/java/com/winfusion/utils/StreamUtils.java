package com.winfusion.utils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 流相关操作的工具类。
 */
public final class StreamUtils {

    public static final int BufferedStreamSize = 1024 * 16;

    private StreamUtils() {

    }

    /**
     * 按字节复制输入流到输出流。
     *
     * @param inStream  输入流
     * @param outStream 输出流
     * @return 成功复制的字节数
     * @throws IOException 如果遇到读写错误。
     */
    public static long copyStream(@NonNull InputStream inStream, @NonNull OutputStream outStream)
            throws IOException {

        byte[] buffer = new byte[BufferedStreamSize];
        long totalRead = 0;
        int amountRead;

        while ((amountRead = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, amountRead);
            totalRead += amountRead;
        }

        outStream.flush();
        return totalRead;
    }
}
