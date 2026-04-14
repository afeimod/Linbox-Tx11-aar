package com.winfusion.utils;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

import android.content.Context;
import android.os.Environment;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 文件操作相关的工具类。
 */
public final class FileUtils {

    private static final String TAG = "FileUtils";
    private static final int BUFFER_SIZE = 8192;

    private FileUtils() {

    }

    /**
     * 将字节缓冲区从当前位置到尾部的内容写入文件。
     *
     * @param buffer 字节缓冲区
     * @param path   要写入的文件路径
     * @throws IOException 如果遇到 IO 错误
     */
    public static void writeByteBufferToFile(@NonNull final ByteBuffer buffer, @NonNull Path path)
            throws IOException {

        if (Files.isRegularFile(path))
            throw new FileAlreadyExistsException(path + " already exists.");

        try (FileOutputStream fos = new FileOutputStream(path.toFile());
             FileChannel channel = fos.getChannel()) {
            channel.write(buffer);
        }
    }

    /**
     * 将字节数组写入文件。
     *
     * @param bytes 字节数组
     * @param path  要写入的文件路径
     * @throws IOException 如果遇到 IO 错误
     */
    public static void writeBytesToFile(@NonNull final byte[] bytes, @NonNull Path path)
            throws IOException {

        writeByteBufferToFile(ByteBuffer.wrap(bytes), path);
    }

    /**
     * 将流从当前位置到尾部的内容读入字节数组中。
     *
     * @param inputStream 输入流
     * @return 字节数组
     * @throws IOException 如果遇到 IO 错误
     */
    @NonNull
    public static byte[] readAllBytes(@NonNull InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] temp = new byte[4096];
        int read;
        while ((read = inputStream.read(temp)) != -1) {
            buffer.write(temp, 0, read);
        }
        return buffer.toByteArray();
    }

    /**
     * 创建软连接。
     *
     * @param target 目标路径
     * @param link   链接路径
     * @return 如果成功则返回 true，否则返回 false
     */
    public static boolean createSymbolLink(@NonNull Path target, @NonNull Path link) {
        return createSymbolLink(target.toAbsolutePath().toString(), link.toAbsolutePath().toString());
    }

    /**
     * 创建软连接。
     *
     * @param target   目标路径
     * @param linkPath 链接路径
     * @return 如果成功则返回 true，否则返回 false
     */
    public static boolean createSymbolLink(@NonNull String target, @NonNull String linkPath) {
        try {
            Os.symlink(target, linkPath);
        } catch (ErrnoException e) {
            return false;
        }

        return true;
    }

    /**
     * 更改文件或文件夹的权限。
     *
     * @param path 文件或文件夹路径
     * @param mode 权限值
     * @return 如果成功则返回 true，否则返回 false
     */
    public static boolean chmod(@NonNull String path, int mode) {
        try {
            Os.chmod(path, mode);
        } catch (ErrnoException e) {
            return false;
        }

        return true;
    }

    /**
     * 更改文件或文件夹权限。
     *
     * @param path 文件或文件夹路径。
     * @param mode 权限值
     * @return 如果成功则返回 true，否则返回 false
     */
    public static boolean chmod(@NonNull Path path, int mode) {
        return chmod(path.toAbsolutePath().toString(), mode);
    }

    /**
     * 获取软链接的目标路径。
     *
     * @param path 软链接路径
     * @return 软链接指向的目标路径
     * @throws IOException 如果遇到 IO 错误
     */
    public static String readSymbolLink(@NonNull String path) throws IOException {
        return Files.readSymbolicLink(Paths.get(path)).toString();
    }

    /**
     * 获取软链接的目标路径。
     *
     * @param path 软链接路径
     * @return 软链接指向的目标路径
     * @throws IOException 如果遇到 IO 错误
     */
    public static String readSymbolLink(@NonNull Path path) throws IOException {
        return readSymbolLink(path.toAbsolutePath().toString());
    }

    /**
     * 递归的删除文件夹。
     * 这将会删除整个文件夹，无论它是否是空的。
     *
     * @param path 文件夹路径
     * @throws IOException              如果遇到 IO 错误
     * @throws IllegalArgumentException 如果要删除的目标不是文件夹
     */
    public static void deleteDirectories(@NonNull Path path) throws IOException {
        if (!Files.isDirectory(path))
            throw new IllegalArgumentException("Not a directory: " + path);

        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 如果文件存在的话，删除这个文件。
     *
     * @param path 文件的路径
     * @throws IOException 如果遇到IO错误
     */
    public static void deleteFileIfExist(@NonNull Path path) throws IOException {
        if (!Files.isRegularFile(path))
            throw new IllegalArgumentException("Not a file: " + path);
        Files.delete(path);
    }

    /**
     * 计算文件或文件夹的大小。
     *
     * @param path 文件或文件夹路径
     * @return 大小，单位是字节
     * @throws IOException 如果遇到 IO 错误
     */
    public static long calculateFileSize(@NonNull Path path) throws IOException {
        AtomicLong size = new AtomicLong();

        if (Files.isSymbolicLink(path)) {
            size.set(0);
        } else if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
                for (Path p : paths)
                    size.addAndGet(calculateFileSize(p));
            }
        } else if (Files.isRegularFile(path)) {
            size.set(Files.size(path));
        }

        return size.get();
    }

    /**
     * 获取一个可用的手机内部存储的文件路径。
     * 这个方法通常用于需要导出文件到手机存储时，生成一个包含时间戳的文件路径。
     * 例如 {@code "/sdcard/downloads/winfusion/prefix-20250101_120000.suffix"}。
     *
     * @param prefix 文件名前缀
     * @param suffix 文件名后缀
     * @return 可用的文件路径
     */
    public static Path getStoragePath(@Nullable String prefix, @Nullable String suffix) {
        Path downloadDir = Paths.get(
                Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getAbsolutePath(),
                "winfusion");
        if (!Files.isDirectory(downloadDir)) {
            try {
                Files.createDirectories(downloadDir);
            } catch (IOException e) {
                Log.e(TAG, "Failed to create downloads storage", e);
            }
        }
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        return downloadDir.resolve((prefix == null ? "" : prefix + "-") + timestamp +
                (suffix == null ? "" : "." + suffix));
    }

    /**
     * 判断一个文件夹是否是空的。
     *
     * @param directory 文件夹路径
     * @return 如果文件夹存在且是空的则返回 true，如果文件夹存在且不是空的则返回 false
     * @throws IllegalStateException 如果文件夹不存在或遇到 {@link IOException}
     */
    public static boolean isDirectoryEmpty(@NonNull Path directory) {
        try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory)) {
            return !paths.iterator().hasNext();
        } catch (IOException e) {
            throw new IllegalStateException("Directory not found.", e);
        }
    }

    /**
     * 检查一个文件夹是否存在，如果不存在，则递归的创建该文件夹。
     *
     * @param directory 文件夹路径
     * @throws IOException 如果路径不是一个文件夹或发生读写错误s
     */
    public static void checkDirectory(@NonNull Path directory) throws IOException {
        if (!Files.isDirectory(directory))
            Files.createDirectories(directory);
    }

    /**
     * 遍历文件夹的子文件，并以子文件路径为参数执行回调。
     *
     * @param dir      文件夹路径
     * @param consumer 回调
     * @throws IOException 如果遇到 IO 错误
     */
    public static void listPaths(@NonNull Path dir, @NonNull Consumer<Path> consumer)
            throws IOException {

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(dir)) {
            for (Path child : paths)
                consumer.accept(child);
        }
    }

    /**
     * 获取一个目录中下一个可用的名称带编号的子文件路径。
     * 例如 {@code "xxx/prefix-2.suffix"}。
     *
     * @param parentDir 父文件夹
     * @param prefix    子文件名前缀
     * @param suffix    子文件名后缀, null 表示没有后缀
     * @return 子文件路径
     * @throws IllegalStateException 如果父文件夹不存在或者无法访问，或者尝试次数达到1024次。
     */
    @NonNull
    public static Path getNextAvailableChildPathWithIndex(@NonNull Path parentDir,
                                                          @NonNull String prefix,
                                                          @Nullable String suffix) {

        if (!Files.isDirectory(parentDir) || !Files.isReadable(parentDir) ||
                !Files.isExecutable(parentDir))
            throw new IllegalStateException("Directory is not exists or not accessible: " + parentDir);

        int i = 1;
        String baseName = prefix + "-";
        String childName;
        Path childPath;
        String s = suffix == null ? "" : "." + suffix;

        while (i <= 1024) {
            childName = baseName + i + s;
            childPath = parentDir.resolve(childName);
            if (!Files.exists(childPath))
                return childPath;
            i++;
        }

        throw new IllegalStateException("Tried 1024 times but no available name found in: " + parentDir);
    }

    /**
     * 将 Assets 中的文件复制到目标路径。
     *
     * @param context    上下文对象
     * @param assetsPath Assets 路径
     * @param target     目标路径
     * @throws IOException 如果发生读写错误
     */
    public static void copyFileFromAssets(@NonNull Context context, @NonNull String assetsPath,
                                          @NonNull Path target) throws IOException {

        try (InputStream is = context.getAssets().open(assetsPath);
             OutputStream os = Files.newOutputStream(target)) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = is.read(buffer)) != -1)
                os.write(buffer, 0, len);
        }
    }
}
