package com.winfusion.feature.manager;

import static com.winfusion.feature.manager.Constants.Cache;
import static com.winfusion.feature.manager.Constants.Contents;
import static com.winfusion.feature.manager.Constants.Soundfont;
import static com.winfusion.feature.manager.Constants.Wfp;
import static com.winfusion.feature.manager.Constants.WfpProfile;
import static com.winfusion.feature.manager.Constants.contentDirs;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.application.WinfusionApplication;
import com.winfusion.core.compression.ArchiveType;
import com.winfusion.core.compression.ArchiveTypeDetector;
import com.winfusion.core.compression.TarCompressor;
import com.winfusion.core.compression.exception.CompressorException;
import com.winfusion.core.soundfont.SoundFontInfo;
import com.winfusion.core.soundfont.SoundFontParser;
import com.winfusion.core.wfp.Wfp;
import com.winfusion.core.wfp.WfpParser;
import com.winfusion.feature.content.model.BaseContentModel;
import com.winfusion.feature.content.model.SoundfontModel;
import com.winfusion.feature.content.model.WfpModel;
import com.winfusion.utils.FileUtils;
import com.winfusion.utils.UriUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ContentManager {

    private static final String TAG = "ContentsManager";
    private static ContentManager instance;

    private final Path contentsPath;

    private ContentManager() {
        contentsPath = Paths.get(WinfusionApplication.getInstance().getFilesDir().getAbsolutePath(), Contents);
    }

    @NonNull
    public static ContentManager getInstance() {
        if (instance == null)
            instance = new ContentManager();

        return instance;
    }

    @NonNull
    public List<BaseContentModel> generateModels() {
        createContentDirs();
        ArrayList<BaseContentModel> models = new ArrayList<>();
        generateSoundfontModels(models);
        generateWfpModels(models);
        return models;
    }

    @Nullable
    public SoundfontInstaller installSoundfont(@NonNull Uri source) {
        try (InputStream stream = WinfusionApplication.getInstance().getContentResolver().openInputStream(source)) {
            SoundFontParser parser = new SoundFontParser();
            if (stream == null)
                return null;

            parser.load(stream);

            SoundFontInfo info = parser.getInfo();
            if (info == null)
                return null;

            String fileName = UriUtils.getFileNameFromUri(WinfusionApplication.getInstance(), source);
            Path path = contentsPath.resolve(Soundfont).resolve(fileName);

            return new SoundfontInstaller(info, source, path);
        } catch (IOException | SoundFontParser.SoundFont2ParserException e) {
            return null;
        }
    }

    public void uninstallSoundfont(@NonNull String fileName) {
        try {
            Files.delete(contentsPath.resolve(Soundfont).resolve(fileName));
        } catch (IOException e) {
            Log.d(TAG, "Failed to uninstall soundfont: " + e.getMessage());
        }
    }

    @NonNull
    public WfpInstaller installWfp(@NonNull Uri source) throws IOException, CompressorException,
            WfpParser.BadWfpFormatException, WfpAlreadyExistsException {

        com.winfusion.core.wfp.Wfp wfp;
        ArchiveType type = ArchiveTypeDetector.detect(WinfusionApplication.getInstance(), source);
        if (type == null)
            throw new CompressorException("Unknown archive type.");

        try (InputStream inputStream = TarCompressor.extractFile(type, source, Paths.get(WfpProfile),
                WinfusionApplication.getInstance())) {

            wfp = WfpParser.parse(inputStream);
        }

        if (!checkWfpUnique(wfp))
            throw new WfpAlreadyExistsException(wfp);

        Path cachePath = contentsPath.resolve(Cache).resolve(UUID.randomUUID().toString());
        Files.createDirectories(cachePath);
        wfp.setWfpHome(cachePath.toAbsolutePath().toString());

        TarCompressor.extract(type, source, cachePath, null, WinfusionApplication.getInstance());

        return new WfpInstaller(wfp, contentsPath);
    }

    public void installBuiltinWfp(@NonNull String path, @NonNull Context context) throws IOException,
            CompressorException, WfpParser.BadWfpFormatException {

        com.winfusion.core.wfp.Wfp wfp;
        ArchiveType type;

        try (InputStream inputStream = context.getAssets().open(path)) {
            type = ArchiveTypeDetector.detect(inputStream);
            if (type == null)
                throw new CompressorException("Unknown archive type.");
        }

        try (InputStream inputStream = context.getAssets().open(path);
             InputStream inputStream2 = TarCompressor.extractFile(type, inputStream, Paths.get(WfpProfile))) {

            wfp = WfpParser.parse(inputStream2);
        }

        Path wfpHome = getWfpHomeById(wfp.toIdentifier());
        Files.createDirectories(wfpHome);

        try (InputStream inputStream = context.getAssets().open(path)) {
            TarCompressor.extract(type, inputStream, wfpHome, null);
        }
    }

    public void uninstallWfp(@NonNull String wfpIdentifier) throws IOException {
        Path wfpHome = getWfpHomeById(wfpIdentifier);
        if (Files.isDirectory(wfpHome))
            FileUtils.deleteDirectories(wfpHome);
    }

    public boolean checkWfpUnique(@NonNull Wfp wfp) {
        return !Files.isRegularFile(getWfpHomeById(wfp.toIdentifier()).resolve(WfpProfile));
    }

    public void clearInstallCache() {
        try {
            Path path = contentsPath.resolve(Cache);
            FileUtils.deleteDirectories(path);
            Files.createDirectories(path);
        } catch (IOException e) {
            Log.d(TAG, "Failed to clear install cache: " + e.getMessage());
        }
    }

    @NonNull
    private Path getWfpDir() {
        return contentsPath.resolve(Wfp);
    }

    @NonNull
    private Path getWfpHomeById(@NonNull String id) {
        return getWfpDir().resolve(id);
    }

    private void createContentDirs() {
        for (String dir : contentDirs) {
            Path path = contentsPath.resolve(dir);
            if (!Files.isDirectory(path)) {
                try {
                    if (Files.exists(path))
                        Files.delete(path);
                    Files.createDirectories(path);
                } catch (IOException e) {
                    Log.d(TAG, "Failed to create content dirs.", e);
                }
            }
        }
    }

    private void generateSoundfontModels(@NonNull List<BaseContentModel> list) {
        Path soundfontDir = contentsPath.resolve(Soundfont);

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(soundfontDir)) {
            for (Path path : paths) {
                if (!Files.isRegularFile(path) || (!path.getFileName().toString().endsWith(".sf3") &&
                        !path.getFileName().toString().endsWith(".sf2")))
                    continue;

                SoundfontModel soundfontModel = buildSoundfontModelFromFile(path);
                if (soundfontModel == null)
                    continue;

                list.add(soundfontModel);
            }
        } catch (IOException e) {
            Log.d(TAG, "Failed to read soundfont dir.");
        }
    }

    @Nullable
    private SoundfontModel buildSoundfontModelFromFile(@NonNull Path filePath) {
        SoundFontParser parser = new SoundFontParser();

        try (FileInputStream stream = new FileInputStream(filePath.toFile())) {
            parser.load(stream);
        } catch (IOException | SoundFontParser.SoundFont2ParserException e) {
            return null;
        }

        SoundFontInfo info = parser.getInfo();
        if (info == null)
            return null;

        String fileName = filePath.getFileName().toString();
        SoundfontModel model = new SoundfontModel(fileName, info);
        model.setTitle(fileName);

        return model;
    }

    private void generateWfpModels(@NonNull List<BaseContentModel> list) {
        Path wfpDir = getWfpDir();

        try (DirectoryStream<Path> paths = Files.newDirectoryStream(wfpDir)) {
            for (Path path : paths) {
                if (!Files.isDirectory(path))
                    continue;

                Path profile = path.resolve(WfpProfile);
                if (!Files.isRegularFile(profile))
                    continue;

                BaseContentModel model = generateWfpModelFromFile(profile);
                if (model == null)
                    continue;

                list.add(model);
            }
        } catch (IOException e) {
            Log.d(TAG, "Failed to read wfp dir.");
        }
    }

    @Nullable
    private BaseContentModel generateWfpModelFromFile(@NonNull Path profile) {
        com.winfusion.core.wfp.Wfp wfp;

        try {
            wfp = WfpParser.parse(profile);
        } catch (IOException | WfpParser.BadWfpFormatException e) {
            Log.d(TAG, "Failed to load wfp profile: " + e.getMessage());
            return null;
        }

        WfpModel model = new WfpModel(wfp);
        model.setTitle(wfp.getName());

        return model;
    }

    public static class SoundfontInstaller implements Installable {

        private final SoundFontInfo info;
        private final Uri source;
        private final Path path;

        public SoundfontInstaller(@NonNull SoundFontInfo info, @NonNull Uri source, @NonNull Path path) {
            this.info = info;
            this.source = source;
            this.path = path;
        }

        @Override
        public void install() throws InstallFailedException {
            try {
                UriUtils.copyFileFromUri(WinfusionApplication.getInstance(), source, path);
            } catch (IOException e) {
                throw new InstallFailedException(e);
            }
        }

        @NonNull
        public SoundFontInfo getInfo() {
            return info;
        }
    }

    public static class WfpInstaller implements Installable {

        private final com.winfusion.core.wfp.Wfp wfp;
        private final Path contentsPath;

        public WfpInstaller(@NonNull Wfp wfp, @NonNull Path contentsPath) {
            this.wfp = wfp;
            this.contentsPath = contentsPath;
        }

        @Override
        public void install() throws InstallFailedException {
            try {
                Path target = contentsPath.resolve(Wfp).resolve(wfp.toIdentifier());
                Files.move(Paths.get(wfp.getWfpHome()), target);
            } catch (IOException e) {
                throw new InstallFailedException(e);
            }
        }

        @NonNull
        public Wfp getWfp() {
            return wfp;
        }
    }

    public static class WfpAlreadyExistsException extends Exception {

        public WfpAlreadyExistsException(@NonNull Wfp wfp) {
            super("Wfp already exists: " + wfp.toIdentifier());
        }
    }
}
