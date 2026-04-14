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
import com.winfusion.utils.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Box64RunControlFileParser {

    private static final String Name = "name";
    private static final String Builtin = "builtin";
    private static final String Groups = "groups";
    private static final String Items = "items";
    private static final String Comment = "comment";
    private static final String Key = "key";
    private static final String Value = "value";

    private Box64RunControlFileParser() {

    }

    @NonNull
    public static Box64RunControlFile parse(@NonNull Path path)
            throws IOException, Box64ProfileBadFormatException {

        try (InputStream inputStream = Files.newInputStream(path)) {
            return parse(inputStream);
        }
    }

    @NonNull
    public static Box64RunControlFile parse(@NonNull Uri uri, @NonNull Context context)
            throws IOException, Box64ProfileBadFormatException {

        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null)
                throw new FileNotFoundException("Failed to open uri: " + uri);
            return parse(inputStream);
        }
    }

    @NonNull
    public static Box64RunControlFile parseFromAssets(@NonNull String path, @NonNull Context context)
        throws IOException, Box64ProfileBadFormatException {

        try (InputStream inputStream = context.getAssets().open(path)) {
            return parse(inputStream);
        }
    }

    @NonNull
    public static Box64RunControlFile parse(@NonNull InputStream inputStream)
            throws IOException, Box64ProfileBadFormatException {

        String content = new String(FileUtils.readAllBytes(inputStream), StandardCharsets.UTF_8);
        try {
            JsonElement jsonElement = JsonParser.parseString(content);
            return parseAsJson(jsonElement.getAsJsonObject());
        } catch (RuntimeException e) {
            return parseAsIni(content);
        }
    }

    @NonNull
    public static Box64RunControlFile parseAsJson(@NonNull JsonObject json)
            throws Box64ProfileBadFormatException {

        try {
            return buildBox64RCFile(json);
        } catch (RuntimeException e) {
            throw new Box64ProfileBadFormatException(e);
        }
    }

    @NonNull
    public static Box64RunControlFile parseAsIni(@NonNull String content)
            throws IOException, Box64ProfileBadFormatException {

        try(BufferedReader reader = new BufferedReader(new StringReader(content))) {
            Box64RunControlFile rcfile = new Box64RunControlFile();
            EnvGroup currentGroup = null;
            // A group section, like {[program.exe] # comment}
            Pattern groupPattern = Pattern.compile("^\\[(.+)]\\s*(?:#\\s*(.*))?$");
            // A item section, like {KEY=VALUE # comment}
            Pattern itemPattern = Pattern.compile("^([^=#\\s]+)=([^=#\\s]+)\\s*(?:#\\s*(.*))?$");
            String line;
            rcfile.setName("box64rc");

            while ((line = reader.readLine()) != null) {
                Matcher matcher;
                line = line.trim();

                if (line.startsWith("#"))
                    continue;

                matcher = groupPattern.matcher(line);
                if (matcher.matches()) {
                    if (currentGroup != null)
                        rcfile.addEnvGroup(currentGroup);

                    currentGroup = new EnvGroup();
                    currentGroup.setName(Objects.requireNonNull(matcher.group(1)));
                    currentGroup.setComment(matcher.group(2));

                    continue;
                }

                matcher = itemPattern.matcher(line);
                if (matcher.matches()) {
                    if (currentGroup == null)
                        throw new Box64ProfileBadFormatException("expect a group but get a item: " + line);

                    EnvItem item = new EnvItem(Objects.requireNonNull(matcher.group(1)),
                            Objects.requireNonNull(matcher.group(2)));
                    item.setComment(matcher.group(3));
                    currentGroup.addEnvItem(item);

                    continue;
                }

                throw new Box64ProfileBadFormatException("bad format: " + line);
            }

            if (currentGroup != null)
                rcfile.addEnvGroup(currentGroup);

            return rcfile;
        }

    }

    public static void saveAsJson(@NonNull Path path, @NonNull Box64RunControlFile rcfile)
            throws IOException {

        try (FileWriter writer = new FileWriter(path.toFile())){
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(buildJsonFromBox64RCFile(rcfile), writer);
        }
    }

    public static void saveAsIni(@NonNull Path path, @NonNull Box64RunControlFile rcfile)
            throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()))) {
            writer.write("# This file is auto generated by WinFusion, any modification will be overwritten.");
            writer.newLine();
            writer.newLine();
            for (EnvGroup group : rcfile.getEnvGroups()) {
                writer.write("[" + group.getName() + "]");
                if (!group.getComment().isBlank())
                    writer.write(" # " + group.getComment());
                writer.newLine();

                for (EnvItem item : group.getEnvItems()) {
                    writer.write(item.getKey() + "=" + item.getValue());
                    if (!item.getComment().isBlank())
                        writer.write(" # " + item.getComment());
                    writer.newLine();
                }
            }
        }
    }

    @NonNull
    private static Box64RunControlFile buildBox64RCFile(@NonNull JsonObject json) {
        Box64RunControlFile rcfile = new Box64RunControlFile();
        JsonArray groupArray = json.get(Groups).getAsJsonArray();

        rcfile.setName(json.get(Name).getAsString());
        for (JsonElement groupElement : groupArray)
            rcfile.addEnvGroup(buildGroup(groupElement.getAsJsonObject()));

        return rcfile;
    }

    @NonNull
    private static EnvGroup buildGroup(@NonNull JsonObject json) {
        EnvGroup group = new EnvGroup();
        JsonArray itemArray = json.get(Items).getAsJsonArray();

        group.setName(json.get(Name).getAsString());
        if (json.has(Comment))
            group.setComment(json.get(Comment).getAsString());
        for (JsonElement itemElement : itemArray) {
            JsonObject itemObject = itemElement.getAsJsonObject();
            EnvItem item = new EnvItem(itemObject.get(Name).getAsString(),
                    itemObject.get(Value).getAsString());
            group.addEnvItem(item);
        }

        return group;
    }

    @NonNull
    private static JsonObject buildJsonFromBox64RCFile(@NonNull Box64RunControlFile rcfile) {
        JsonObject json = new JsonObject();
        JsonArray groupArray = new JsonArray();

        json.add(Name, new JsonPrimitive(rcfile.getName()));
        for (EnvGroup group : rcfile.getEnvGroups()) {
            JsonObject groupObject = new JsonObject();
            JsonArray itemArray = new JsonArray();

            groupObject.add(Name, new JsonPrimitive(group.getName()));
            groupObject.add(Comment, new JsonPrimitive(group.getComment()));
            for (EnvItem item : group.getEnvItems()) {
                JsonObject itemObject = new JsonObject();
                itemObject.add(Key, new JsonPrimitive(item.getKey()));
                itemObject.add(Value, new JsonPrimitive(item.getValue()));
                if (!item.getComment().isBlank())
                    itemObject.add(Comment, new JsonPrimitive(item.getComment()));
                itemArray.add(itemObject);
            }
            groupObject.add(Items, itemArray);
        }
        json.add(Groups, groupArray);

        return json;
    }
}
