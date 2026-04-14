package com.winfusion.core.registry;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.registry.exception.RegistryException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 注册表解析器的基类。
 */
public abstract class RegistryParser {

    /**
     * 从 Uri 解析注册表，并返回注册表对象。
     *
     * @param uri     Uri
     * @param context 上下文
     * @param cs      字符编码
     * @return 注册表对象
     * @throws IOException       如果发生读写错误
     * @throws RegistryException 如果发生解析错误
     */
    @NonNull
    public Registry prase(@NonNull Uri uri, @NonNull Context context, @Nullable Charset cs)
            throws IOException, RegistryException {

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null)
                throw new IOException("Failed to open uri: " + uri);
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, getCharset(cs)))) {
                return parse(reader);
            }
        }
    }

    /**
     * 从字符串解析注册表，并返回注册表对象。
     *
     * @param content 字符串
     * @return 注册表对象
     * @throws IOException       如果发生读写错误
     * @throws RegistryException 如果发生解析错误
     */
    @NonNull
    public Registry parse(@NonNull String content) throws IOException, RegistryException {
        try (BufferedReader reader = new BufferedReader(new StringReader(content))) {
            return parse(reader);
        }
    }

    /**
     * 从文件解析注册表，并返回注册表对象。
     *
     * @param path 文件路径
     * @param cs   字符编码，为 null 时将默认采用 UTF-8 编码
     * @return 注册表对象
     * @throws IOException       如果发生读写错误
     * @throws RegistryException 如果发生解析错误
     */
    @NonNull
    public Registry parse(@NonNull Path path, @Nullable Charset cs)
            throws IOException, RegistryException {

        try (BufferedReader reader = Files.newBufferedReader(path, getCharset(cs))) {
            return parse(reader);
        }
    }

    /**
     * 从 Assets 中解析注册表，并返回注册表对象。
     *
     * @param path    注册表文件路径
     * @param context 上下文
     * @param cs      字符编码
     * @return 注册表
     * @throws IOException       如果发生读写错误。
     * @throws RegistryException 如果发生解析错误
     */
    @NonNull
    public Registry parseFromAssets(@NonNull String path, @NonNull Context context,
                                    @Nullable Charset cs)
            throws IOException, RegistryException {

        try (InputStreamReader reader1 = new InputStreamReader(context.getAssets().open(path), getCharset(cs));
             BufferedReader reader2 = new BufferedReader(reader1)) {

            return parse(reader2);
        }
    }

    /**
     * 从输入流解析注册表，并返回注册表对象。
     *
     * @param reader 输入流
     * @return 注册表对象
     * @throws IOException       如果发生读写错误
     * @throws RegistryException 如果发生解析错误
     */
    @NonNull
    public abstract Registry parse(@NonNull BufferedReader reader)
            throws IOException, RegistryException;

    @NonNull
    private Charset getCharset(@Nullable Charset defaultCharset) {
        return defaultCharset == null ? StandardCharsets.UTF_8 : defaultCharset;
    }
}
