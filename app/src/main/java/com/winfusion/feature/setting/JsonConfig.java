package com.winfusion.feature.setting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.winfusion.feature.setting.exception.BadConfigFileFormatException;
import com.winfusion.feature.setting.value.ConfigArray;
import com.winfusion.feature.setting.value.ConfigElement;
import com.winfusion.feature.setting.value.ConfigMapping;
import com.winfusion.feature.setting.value.ConfigNull;
import com.winfusion.feature.setting.value.ConfigPrimitive;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class JsonConfig implements Config {

    private Source defaultSource = new DefaultSource();
    private Source globalSource = new JsonSource();
    private Source localSource = new JsonSource();

    @NonNull
    @Override
    public Source getDefault() {
        return defaultSource;
    }

    @NonNull
    @Override
    public Source getGlobal() {
        return globalSource;
    }

    @NonNull
    @Override
    public Source getLocal() {
        return localSource;
    }

    @Override
    public void setDefault(@NonNull Source source) {
        defaultSource = source;
    }

    @Override
    public void setGlobal(@NonNull Source source) {
        globalSource = source;
    }

    @Override
    public void setLocal(@NonNull Source source) {
        localSource = source;
    }

    @NonNull
    @Override
    public Source get(@NonNull SourceType sourceType) {
        return switch (sourceType) {
            case Default -> getDefault();
            case Global -> getGlobal();
            case Local -> getLocal();
        };
    }

    @Override
    public void set(@NonNull SourceType sourceType, @NonNull Source source) {
        switch (sourceType) {
            case Default -> setDefault(source);
            case Global -> setGlobal(source);
            case Local -> setLocal(source);
        }
    }

    private static class DefaultSource implements Source {

        private final TreeMap<String, ConfigElement> map = new TreeMap<>();

        @Override
        public boolean isLoaded() {
            return true;
        }

        @Override
        public void loadEmpty() {
            throw new UnsupportedOperationException("DefaultSource can't be loaded");
        }

        @Override
        public void load(@NonNull Path path) throws IOException, BadConfigFileFormatException {
            throw new UnsupportedOperationException("DefaultSource can't be loaded");
        }

        @Override
        public void save(@NonNull Path path) throws IOException {
            throw new UnsupportedOperationException("DefaultSource can't be saved");
        }

        @Override
        public boolean has(@NonNull String key) {
            return map.containsKey(key);
        }

        @Nullable
        @Override
        public ConfigElement get(@NonNull String key) {
            return map.get(key);
        }

        @Override
        public void set(@NonNull String key, @NonNull ConfigElement element) {
            map.put(key, element);
        }

        @Override
        public void remove(@NonNull String key) {
            map.remove(key);
        }
    }

    private static class JsonSource implements Source {

        private JsonObject json = new JsonObject();
        private boolean loaded = false;

        @Override
        public boolean isLoaded() {
            return loaded;
        }

        @Override
        public void loadEmpty() {
            json = new JsonObject();
            loaded = true;
        }

        @Override
        public void load(@NonNull Path path) throws IOException, BadConfigFileFormatException {
            try (FileReader reader = new FileReader(path.toFile())) {
                json = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (RuntimeException e) {
                throw new BadConfigFileFormatException("Failed to load json config: " +
                        path.toAbsolutePath().toString(), e);
            }
            loaded = true;
        }

        @Override
        public void save(@NonNull Path path) throws IOException {
            try (FileWriter writer = new FileWriter(path.toFile())) {
                new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            }
        }

        @Override
        public boolean has(@NonNull String key) {
            return json.has(key);
        }

        @Nullable
        @Override
        public ConfigElement get(@NonNull String key) {
            JsonElement je = json.get(key);
            if (je == null)
                return null;
            return buildConfigElement(je);
        }

        @Override
        public void set(@NonNull String key, @NonNull ConfigElement element) {
            json.add(key, buildJsonElement(element));
        }

        @Override
        public void remove(@NonNull String key) {
            json.remove(key);
        }

        @NonNull
        private ConfigElement buildConfigElement(@NonNull JsonElement je) {
            if (je.isJsonPrimitive())
                return buildConfigPrimitive(je.getAsJsonPrimitive());
            else if (je.isJsonArray())
                return buildConfigArray(je.getAsJsonArray());
            else if (je.isJsonObject())
                return buildConfigMapping(je.getAsJsonObject());
            else if (je.isJsonNull())
                return buildConfigNull();
            else
                throw new IllegalArgumentException("Unsupported JsonElement: " + je);
        }

        @NonNull
        private ConfigPrimitive buildConfigPrimitive(@NonNull JsonPrimitive jp) {
            if (jp.isNumber())
                // Since Gson does not distinguish the original number type,
                // and Config requires the data type to be determined at creation time,
                // we can only assume that the number is an Integer.
                // Or we may need an additional flag to indicate the actual data type.
                return new ConfigPrimitive(jp.getAsNumber().intValue());
            else if (jp.isBoolean())
                return new ConfigPrimitive(jp.getAsBoolean());
            else if (jp.isString())
                return new ConfigPrimitive(jp.getAsString());
            else
                throw new IllegalArgumentException("Unsupported JsonPrimitive: " + jp);
        }

        @NonNull
        private ConfigArray buildConfigArray(@NonNull JsonArray ja) {
            ConfigArray configArray = new ConfigArray();
            for (JsonElement element : ja)
                configArray.add(buildConfigElement(element));
            return configArray;
        }

        @NonNull
        private ConfigMapping buildConfigMapping(@NonNull JsonObject jo) {
            ConfigMapping map = new ConfigMapping();
            for (Map.Entry<String, JsonElement> entry : jo.entrySet())
                map.add(entry.getKey(), buildConfigElement(entry.getValue()));
            return map;
        }

        @NonNull
        private ConfigNull buildConfigNull() {
            return ConfigNull.INSTANCE;
        }

        @NonNull
        private JsonElement buildJsonElement(@NonNull ConfigElement ce) {
            if (ce.isPrimitive())
                return buildJsonPrimitive(ce.getAsConfigPrimitive());
            else if (ce.isArray())
                return buildJsonArray(ce.getAsConfigArray());
            else if (ce.isMapping())
                return buildJsonObject(ce.getAsConfigMapping());
            else if (ce.isNull())
                return buildJsonNull();
            else
                throw new IllegalArgumentException("Unsupported ConfigElement: " + ce);
        }

        @NonNull
        private JsonPrimitive buildJsonPrimitive(@NonNull ConfigPrimitive cp) {
            if (cp.isBool())
                return new JsonPrimitive(cp.getAsBool());
            else if (cp.isFloat())
                return new JsonPrimitive(cp.getAsFloat());
            else if (cp.isInt())
                return new JsonPrimitive(cp.getAsInt());
            else if (cp.isString())
                return new JsonPrimitive(cp.getAsString());
            else
                throw new IllegalArgumentException("Unsupported ConfigPrimitive: " + cp);
        }

        @NonNull
        private JsonArray buildJsonArray(@NonNull ConfigArray ca) {
            JsonArray jsonArray = new JsonArray();
            for (ConfigElement element : ca)
                jsonArray.add(buildJsonElement(element));
            return jsonArray;
        }

        @NonNull
        private JsonObject buildJsonObject(@NonNull ConfigMapping cm) {
            JsonObject jsonObject = new JsonObject();
            for (Map.Entry<String, ConfigElement> entry : cm.entrySet())
                jsonObject.add(entry.getKey(), buildJsonElement(entry.getValue()));
            return jsonObject;
        }

        @NonNull
        private JsonNull buildJsonNull() {
            return JsonNull.INSTANCE;
        }
    }
}
