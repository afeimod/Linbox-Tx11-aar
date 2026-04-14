package com.winfusion.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Uri 操作相关的工具类。
 */
public final class UriUtils {

    private UriUtils() {

    }

    /**
     * 复制 Uri 对应的文件到目的地位置。
     *
     * @param context     上下文对象
     * @param uri         Uri对象
     * @param destination 目的地位置
     * @throws IOException 如果打开 uri 时失败，或者复制过程中遇到读写错误
     */
    public static void copyFileFromUri(@NonNull Context context, @NonNull Uri uri,
                                       @NonNull Path destination) throws IOException {

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(destination.toFile())) {

            if (inputStream == null)
                throw new FileNotFoundException("Failed to open uri: " + uri);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1)
                outputStream.write(buffer, 0, bytesRead);

            outputStream.flush();
        }
    }

    /**
     * 获取 Uri 对象对应的文件名称。
     *
     * @param context 上下文对象
     * @param uri     Uri 对象
     * @return 如果 Uri 对象有 unix 路径，则返回文件名称，否则返回 null
     */
    @Nullable
    public static String getFileNameFromUri(@NonNull Context context, @NonNull Uri uri) {
        String fileName = getFileNameFromContentUri(context, uri);
        if (fileName == null)
            fileName = getFileNameFromFileUri(uri);
        return fileName;
    }

    @Nullable
    private static String getFileNameFromContentUri(@NonNull Context context, @NonNull Uri uri) {
        String fileName = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1 && cursor.moveToFirst())
                fileName = cursor.getString(nameIndex);
            cursor.close();
        }
        return fileName;
    }

    @Nullable
    private static String getFileNameFromFileUri(@NonNull Uri uri) {
        String path = uri.getPath();
        if (path != null)
            return path.substring(path.lastIndexOf("/") + 1);
        return null;
    }
}
