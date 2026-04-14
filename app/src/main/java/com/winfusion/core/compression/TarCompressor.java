package com.winfusion.core.compression;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.compression.exception.CompressorException;
import com.winfusion.core.compression.exception.TarEntryNotFoundException;
import com.winfusion.utils.FileUtils;
import com.winfusion.utils.StreamUtils;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.tar.TarConstants;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipParameters;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Objects;

public final class TarCompressor {

    private static final String X0755 = "rwxr-xr-x";

    private TarCompressor() {

    }

    public static void compress(@NonNull final ArchiveType type, final int level,
                                @Nullable final CompressCallback callback,
                                @Nullable final String basePath,
                                @NonNull final Path dest, @NonNull final Path... source)
            throws IOException, CompressorException {

        long totalSize = 0;
        ProgressRecorder recorder;

        for (Path f : source)
            totalSize += FileUtils.calculateFileSize(f);

        recorder = new ProgressRecorder(null, dest.toAbsolutePath().toString(), totalSize,
                0, callback);

        try (OutputStream cos = internalGetCompressorOutputStream(
                type, level, dest);
             TarArchiveOutputStream tos = new TarArchiveOutputStream(cos)) {
            tos.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);

            for (Path path : source) {
                String selfBasePath = path.getParent() == null ? "" :
                        path.getParent().toAbsolutePath().toString();
                internalAddFilesToTar(tos, path, recorder, basePath == null ? selfBasePath : basePath);
            }
        }
    }

    public static void extract(@NonNull ArchiveType type, @NonNull Uri source, @NonNull Path dest,
                               @Nullable CompressCallback callback, @NonNull Context context)
            throws IOException, CompressorException {

        ProgressRecorder recorder = new ProgressRecorder(
                source.getPath(), dest.toAbsolutePath().toString(), -1, 0, callback);

        try (InputStream in = context.getContentResolver().openInputStream(source)) {
            if (in == null)
                throw new FileNotFoundException("Failed to read uri: " + source);
            extract(type, in, dest, recorder);
        }
    }

    public static void extract(@NonNull ArchiveType type, @NonNull Path source, @NonNull Path dest,
                               @Nullable CompressCallback callback)
            throws IOException, CompressorException {

        ProgressRecorder recorder = new ProgressRecorder(
                source.toAbsolutePath().toString(),
                dest.toAbsolutePath().toString(),
                FileUtils.calculateFileSize(source), 0, callback
        );

        try (InputStream in = internalGetBufferedFileInputStream(source)) {
            extract(type, in, dest, recorder);
        }
    }

    public static void extract(@NonNull ArchiveType type, @NonNull InputStream inputStream,
                               @NonNull Path dest, @Nullable CompressCallback callback)
            throws IOException, CompressorException {

        ProgressRecorder recorder = new ProgressRecorder(
                "null", dest.toAbsolutePath().toString(), -1, 0, callback);

        extract(type, inputStream, dest, recorder);
    }

    @NonNull
    public static InputStream extractFile(@NonNull ArchiveType type, @NonNull Path source,
                                          @NonNull Path target)
            throws IOException, TarEntryNotFoundException {

        try (InputStream in = internalGetBufferedFileInputStream(source)) {
            return extractFile(type, in, target);
        }
    }

    @NonNull
    public static InputStream extractFile(@NonNull ArchiveType type, @NonNull Uri source,
                                          @NonNull Path target, @NonNull Context context)
            throws IOException, CompressorException {

        try (InputStream in = context.getContentResolver().openInputStream(source)) {
            if (in == null)
                throw new FileNotFoundException("Failed to read uri: " + source);
            return extractFile(type, in, target);
        }
    }

    private static void extract(@NonNull ArchiveType type, @NonNull InputStream source,
                                @NonNull Path dest, @NonNull ProgressRecorder recorder)
            throws IOException {

        Files.createDirectories(dest);

        try (ArchiveInputStream<TarArchiveEntry> ais = new TarArchiveInputStream(
                internalGetCompressorInputStream(type, source))) {

            TarArchiveEntry entry;
            while ((entry = ais.getNextEntry()) != null) {
                if (!ais.canReadEntryData(entry))
                    continue;

                Path path = dest.resolve(entry.getName());

                recorder.dest = path.toAbsolutePath().toString();
                recorder.currentRead = ais.getBytesRead();
                recorder.post();

                if (entry.isSymbolicLink()) {
                    Path target = Paths.get(entry.getLinkName());
                    Files.createSymbolicLink(path, target);
                } else if (entry.isDirectory()) {
                    Files.createDirectories(path);
                } else {
                    Path parent = path.getParent();
                    Files.createDirectories(parent);
                    try (OutputStream os = internalGetBufferedFileOutputStream(path)) {
                        StreamUtils.copyStream(ais, os);
                    }
                }

                recorder.currentRead = ais.getBytesRead();
                recorder.post();

                if (!entry.isSymbolicLink())
                    Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(X0755));
            }
        }
    }

    @NonNull
    public static InputStream extractFile(@NonNull ArchiveType type, @NonNull InputStream source,
                                          @NonNull Path target)
            throws IOException, TarEntryNotFoundException {

        try (ArchiveInputStream<TarArchiveEntry> ais = new TarArchiveInputStream(
                internalGetCompressorInputStream(type, source))) {

            TarArchiveEntry entry;
            while ((entry = ais.getNextEntry()) != null) {
                if (!ais.canReadEntryData(entry))
                    continue;

                if (!entry.isFile())
                    continue;

                if (!Objects.equals(Paths.get(entry.getName()).normalize(), target.normalize()))
                    continue;

                ByteArrayOutputStream os = new ByteArrayOutputStream();
                StreamUtils.copyStream(ais, os);
                os.close();

                return new ByteArrayInputStream(os.toByteArray());
            }

            throw new TarEntryNotFoundException(target.toString());
        }
    }

    private static void internalAddFilesToTar(@NonNull TarArchiveOutputStream tos, @NonNull Path path,
                                              @NonNull ProgressRecorder recorder,
                                              @NonNull String basePath)
            throws IOException, CompressorException {

        if (Files.isSymbolicLink(path))
            internalAddSymbolLinkToTar(tos, path, recorder, basePath);
        else if (Files.isDirectory(path))
            internalAddDirToTar(tos, path, recorder, basePath);
        else if (Files.isRegularFile(path))
            internalAddFileToTar(tos, path, recorder, basePath);
        else
            throw new CompressorException("Unsupported file: " + path.toAbsolutePath().toString());
    }

    private static void internalAddFileToTar(@NonNull TarArchiveOutputStream tos, @NonNull Path path,
                                             @NonNull ProgressRecorder recorder,
                                             @NonNull String basePath) throws IOException {

        recorder.source = path.toAbsolutePath().toString();
        recorder.post();

        String entryName = internalGetEntryName(basePath, path);
        tos.putArchiveEntry(tos.createArchiveEntry(path, entryName));

        try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(path))) {

            StreamUtils.copyStream(in, tos);
        }

        tos.closeArchiveEntry();

        recorder.currentRead += FileUtils.calculateFileSize(path);
        recorder.post();
    }

    private static void internalAddDirToTar(@NonNull TarArchiveOutputStream tos, @NonNull Path path,
                                            @NonNull ProgressRecorder recorder,
                                            @NonNull String basePath)
            throws IOException, CompressorException {

        recorder.source = path.toAbsolutePath().toString();
        recorder.post();

        String newBasePath = internalGetEntryName(basePath, path);
        String entryName = newBasePath + "/";

        tos.putArchiveEntry(tos.createArchiveEntry(path, entryName));
        tos.closeArchiveEntry();

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(path)) {
            for (Path p : paths)
                internalAddFilesToTar(tos, p, recorder, newBasePath);
        }
    }

    private static void internalAddSymbolLinkToTar(@NonNull TarArchiveOutputStream tos,
                                                   @NonNull Path path,
                                                   @NonNull ProgressRecorder recorder,
                                                   @NonNull String basePath) throws IOException {

        recorder.source = path.toAbsolutePath().toString();
        recorder.post();

        String entryName = internalGetEntryName(basePath, path);
        TarArchiveEntry entry = new TarArchiveEntry(entryName, TarConstants.LF_SYMLINK);
        entry.setLinkName(Files.readSymbolicLink(path).toString());
        tos.putArchiveEntry(entry);
        tos.closeArchiveEntry();
    }

    @NonNull
    private static String internalGetEntryName(@NonNull String basePath, @NonNull Path path) {
        return Paths.get(basePath, path.getFileName().toString()).toAbsolutePath().toString();
    }

    @NonNull
    private static OutputStream internalGetCompressorOutputStream(@NonNull ArchiveType type, int level,
                                                                  @NonNull Path dest)
            throws IOException {

        if (type == ArchiveType.TAR_XZ) {
            return new XZCompressorOutputStream(internalGetBufferedFileOutputStream(dest), level);
        } else if (type == ArchiveType.TAR_ZST) {
            return new ZstdCompressorOutputStream(internalGetBufferedFileOutputStream(dest), level);
        } else if (type == ArchiveType.TAR_GZ) {
            GzipParameters param = new GzipParameters();
            param.setCompressionLevel(level);
            return new GzipCompressorOutputStream(internalGetBufferedFileOutputStream(dest), param);
        } else {
            throw new IllegalArgumentException("Unsupported compress output type: " + type.name());
        }
    }

    @NonNull
    private static InputStream internalGetCompressorInputStream(@NonNull ArchiveType type,
                                                                @NonNull InputStream source)
            throws IOException {

        if (type == ArchiveType.TAR_XZ)
            return new XZCompressorInputStream(source);
        else if (type == ArchiveType.TAR_ZST)
            return new ZstdCompressorInputStream(source);
        else if (type == ArchiveType.TAR_GZ)
            return new GzipCompressorInputStream(source);
        else
            throw new IllegalArgumentException("Unsupported compress input type: " + type.name());
    }

    private static OutputStream internalGetBufferedFileOutputStream(@NonNull Path path)
            throws IOException {

        return new BufferedOutputStream(Files.newOutputStream(path));
    }

    private static InputStream internalGetBufferedFileInputStream(@NonNull Path path)
            throws IOException {

        return new BufferedInputStream(Files.newInputStream(path));
    }

    private static class ProgressRecorder {

        public long totalSize;
        public long currentRead;
        public String source;
        public String dest;
        public CompressCallback callback;

        public ProgressRecorder(@Nullable String source, @Nullable String dest,
                                long totalSize, long currentRead,
                                @Nullable CompressCallback callback) {

            this.source = source;
            this.dest = dest;
            this.totalSize = totalSize;
            this.currentRead = currentRead;
            this.callback = callback;
        }

        public void post() {
            if (callback != null)
                callback.onProgress(source, dest, totalSize, currentRead);
        }
    }
}
