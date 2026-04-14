package com.winfusion.core.box64;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.winfusion.core.box64.exception.Box64ProfileBadFormatException;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Box64PresetParser {

    private static final String Name = "name";
    private static final String Items = "items";
    private static final String Key = "key";
    private static final String Value = "value";

    private Box64PresetParser() {

    }

    @NonNull
    public static List<Box64Preset> parse(@NonNull Path path) throws IOException,
            Box64ProfileBadFormatException {

        try (InputStream inputStream = Files.newInputStream(path)) {
            return parse(inputStream);
        }
    }

    @NonNull
    public static List<Box64Preset> parse(@NonNull Uri uri, @NonNull Context context)
            throws IOException, Box64ProfileBadFormatException {

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null)
                throw new FileNotFoundException("Failed to open uri: " + uri);
            return parse(inputStream);
        }
    }

    @NonNull
    public static List<Box64Preset> parseFromAssets(@NonNull String path, @NonNull Context context)
            throws IOException, Box64ProfileBadFormatException {

        try (InputStream inputStream = context.getAssets().open(path)) {
            return parse(inputStream);
        }
    }

    @NonNull
    public static List<Box64Preset> parse(@NonNull InputStream inputStream) throws IOException,
            Box64ProfileBadFormatException {

        JsonElement json;
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            json = JsonParser.parseReader(reader);
            return buildPresets(json);
        } catch (RuntimeException e) {
            throw new Box64ProfileBadFormatException(e);
        }
    }

    public static void save(@NonNull Path path, @NonNull List<Box64Preset> box64Presets)
            throws IOException {

        JsonArray presetArray = new JsonArray();
        for (Box64Preset box64Preset : box64Presets)
            presetArray.add(buildJsonFromPreset(box64Preset));
        save(path, presetArray);
    }

    public static void save(@NonNull Path path, @NonNull Box64Preset box64Preset) throws IOException {
        save(path, buildJsonFromPreset(box64Preset));
    }

    private static void save(@NonNull Path path, @NonNull JsonElement json) throws IOException {
        try (FileWriter writer = new FileWriter(path.toFile())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(json, writer);
        }
    }

    @NonNull
    private static List<Box64Preset> buildPresets(@NonNull JsonElement json)
            throws Box64ProfileBadFormatException {

        ArrayList<Box64Preset> presets = new ArrayList<>();

        if (json.isJsonArray()) {
            JsonArray array = json.getAsJsonArray();
            for (JsonElement element : array) {
                try {
                    presets.add(buildPreset(element.getAsJsonObject()));
                } catch (RuntimeException e) {
                    throw new Box64ProfileBadFormatException(e);
                }
            }
        } else if (json.isJsonObject()) {
            presets.add(buildPreset(json.getAsJsonObject()));
        } else {
            throw new Box64ProfileBadFormatException("json element is not an array nor object.");
        }

        return presets;
    }

    @NonNull
    private static Box64Preset buildPreset(@NonNull JsonObject json) {

        Box64Preset preset = new Box64Preset();
        JsonArray itemArray = json.get(Items).getAsJsonArray();

        preset.setName(json.get(Name).getAsString());
        for (JsonElement itemElement : itemArray) {
            JsonObject itemObject = itemElement.getAsJsonObject();
            EnvItem item = new EnvItem(itemObject.get(Key).getAsString(),
                    itemObject.get(Value).getAsString());
            preset.addEnvItem(item);
        }

        return preset;
    }

    @NonNull
    private static JsonObject buildJsonFromPreset(@NonNull Box64Preset box64Preset) {
        JsonObject json = new JsonObject();
        JsonArray itemArray = new JsonArray();

        json.add(Name, new JsonPrimitive(box64Preset.getName()));
        List<EnvItem> items = box64Preset.getEnvItems();
        for (EnvItem item : items) {
            JsonObject itemObject = new JsonObject();
            itemObject.add(Key, new JsonPrimitive(item.getKey()));
            itemObject.add(Value, new JsonPrimitive(item.getValue()));
            itemArray.add(itemObject);
        }
        json.add(Items, itemArray);

        return json;
    }
}
