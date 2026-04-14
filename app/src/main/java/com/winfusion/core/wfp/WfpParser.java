package com.winfusion.core.wfp;

import static com.winfusion.core.wfp.Wfp.VERSION_1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public final class WfpParser {

    private static final String Type = "type";
    private static final String SchemaVersion = "schemaVersion";
    private static final String Name = "name";
    private static final String Author = "author";
    private static final String Comment = "comment";
    private static final String PackageCopyright = "packageCopyright";
    private static final String PackageLicense = "packageLicense";
    private static final String LibraryCopyright = "libraryCopyright";
    private static final String LibraryLicense = "libraryLicense";
    private static final String Details = "details";
    private static final String Property = "property";

    @NonNull
    public static Wfp parse(@NonNull Path profilePath) throws IOException, BadWfpFormatException {
        try (InputStream stream = Files.newInputStream(profilePath)) {
            return parse(profilePath, stream);
        }
    }

    @NonNull
    public static Wfp parse(@NonNull InputStream inputStream) throws IOException, BadWfpFormatException {
        return parse(null, inputStream);
    }

    @NonNull
    private static Wfp parse(@Nullable Path profilePath, @NonNull InputStream inputStream)
            throws IOException, BadWfpFormatException {

        JsonObject json;
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (RuntimeException e) {
            throw new BadWfpFormatException(e);
        }

        return buildWfp(profilePath, json);
    }

    @NonNull
    private static Wfp buildWfp(@Nullable Path path, @NonNull JsonObject json)
            throws BadWfpFormatException {

        int schemaVersion = json.get(SchemaVersion).getAsInt();

        try {
            if (schemaVersion == VERSION_1)
                return buildWfpVersion1(path, json);
        } catch (RuntimeException e) {
            throw new BadWfpFormatException(e);
        }

        throw new BadWfpFormatException("Unsupported schemaVersion: " + schemaVersion);
    }

    @NonNull
    private static Wfp buildWfpVersion1(@Nullable Path path, @NonNull JsonObject json) {
        Wfp wfp = new Wfp();
        wfp.setWfpHome(path == null ? null : path.getParent().toAbsolutePath().toString());
        wfp.setWfpType(WfpType.fromId(json.get(Type).getAsString()));
        wfp.setSchemaVersion(VERSION_1);
        wfp.setName(json.get(Name).getAsString());
        wfp.setAuthor(json.get(Author).getAsString());
        wfp.setPackageCopyright(json.get(PackageCopyright).getAsString());
        wfp.setPackageLicense(json.get(PackageLicense).getAsString());
        wfp.setLibraryCopyright(json.get(LibraryCopyright).getAsString());
        wfp.setLibraryLicense(json.get(LibraryLicense).getAsString());
        wfp.setDetails(json.get(Details).getAsString());

        String commentStr;
        JsonElement commentElement = json.get(Comment);
        if (commentElement.isJsonPrimitive()) {
            commentStr = commentElement.getAsString();
        } else {
            Locale locale = Locale.getDefault();
            // FIXME: implement for more locales
            if (Objects.equals("zh", locale.getLanguage()))
                commentStr = commentElement.getAsJsonObject().get("zh_cn").getAsString();
            else
                commentStr = commentElement.getAsJsonObject().get("en_us").getAsString();
        }
        wfp.setComment(commentStr);

        TreeMap<String, String> map = new TreeMap<>();
        Map<String, JsonElement> jsonMap = json.get(Property).getAsJsonObject().asMap();
        for (Map.Entry<String, JsonElement> entry : jsonMap.entrySet())
            map.put(entry.getKey(), entry.getValue().getAsString());
        wfp.setProperty(map);

        return wfp;
    }

    public static class BadWfpFormatException extends Exception {

        public BadWfpFormatException(String message) {
            super(message);
        }

        public BadWfpFormatException(Throwable cause) {
            super(cause);
        }
    }
}
