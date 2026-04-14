package com.winfusion.core.pe;

import androidx.annotation.NonNull;

import com.winfusion.core.pe.exception.BadPEFormatException;
import com.winfusion.core.pe.exception.IconNotFoundException;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * PE 文件图标解析器。
 * 使用 icoutils 进行图标解析。
 */
public final class IconParser {

    static {
        System.loadLibrary("icoutils");
    }

    private IconParser() {

    }

    /**
     * 从 PE 文件解析全部的 .ico 图标。
     *
     * @param filePath PE 文件路径
     * @return .ico 图标字节对象的列表
     * @throws BadPEFormatException 如果 PE 文件不存在或格式不正确
     */
    @NonNull
    public static List<ByteBuffer> parseAllIconFromPE(@NonNull Path filePath) throws BadPEFormatException {
        return internalParseIconFromPE(filePath, false);
    }

    /**
     * 从 PE 文件解析主要 .ico 图标。
     *
     * @param filePath PE 文件路径
     * @return .ico 图标字节对象
     * @throws IconNotFoundException 如果该 PE 文件没有主要图标
     * @throws BadPEFormatException  如果 PE 文件不存在或者格式不正确
     */
    @NonNull
    public static ByteBuffer parseMainIconFromPE(@NonNull Path filePath)
            throws IconNotFoundException, BadPEFormatException {

        List<ByteBuffer> list = internalParseIconFromPE(filePath, true);
        if (list.size() != 1)
            throw new IconNotFoundException("Failed to get main icon.");
        return list.get(0);
    }

    @NonNull
    private static List<ByteBuffer> internalParseIconFromPE(@NonNull Path path, boolean logoOnly)
            throws BadPEFormatException {

        ArrayList<ByteBuffer> list = new ArrayList<>();
        try {
            extractIconFromPE(path.toAbsolutePath().toString(), list, logoOnly);
        } catch (Exception e) {
            throw new BadPEFormatException("Failed to parse PE.", e);
        }

        // copy direct buffer to heap, for security
        ArrayList<ByteBuffer> heapBufferList = new ArrayList<>();
        for (ByteBuffer buffer : list) {
            if (buffer.isDirect()) {
                ByteBuffer heapBuffer = ByteBuffer.allocate(buffer.capacity());
                buffer.position(0);
                heapBuffer.put(buffer);
                heapBuffer.position(0);
                heapBufferList.add(heapBuffer);
                releaseMemory(buffer);
            } else {
                // never reach here
                heapBufferList.add(buffer);
            }
        }
        return heapBufferList;
    }

    /**
     * 该方法只能被 jni 端调用。
     * 用于将 jni 端的结果存放到列表中。
     *
     * @param buffer ico图标字节对象
     * @param list   列表
     */
    @SuppressWarnings("unused")
    private static void JNIAddByteBufferToList(@NonNull ByteBuffer buffer,
                                               @NonNull List<ByteBuffer> list) {
        list.add(buffer);
    }

    private static native void extractIconFromPE(String path, List<ByteBuffer> buffers, boolean logoOnly);

    private static native void releaseMemory(@NonNull ByteBuffer buffer);
}
