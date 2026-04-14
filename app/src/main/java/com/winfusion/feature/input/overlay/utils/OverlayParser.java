package com.winfusion.feature.input.overlay.utils;

import static com.winfusion.core.wfp.Wfp.VERSION_1;
import static com.winfusion.feature.input.overlay.utils.Constants.*;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.winfusion.feature.input.key.StandardAction;
import com.winfusion.feature.input.key.StandardButton;
import com.winfusion.feature.input.key.StandardKey;
import com.winfusion.feature.input.overlay.OverlayProfile;
import com.winfusion.feature.input.overlay.bind.Binding;
import com.winfusion.feature.input.overlay.widget.BaseWidget;
import com.winfusion.feature.input.overlay.widget.ButtonWidget;
import com.winfusion.feature.input.overlay.widget.DPadWidget;
import com.winfusion.feature.input.overlay.widget.ThumbStickWidget;
import com.winfusion.feature.input.overlay.widget.WidgetType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 覆盖层解析器。
 */
public final class OverlayParser {

    private OverlayParser() {

    }

    /**
     * 从 Uri 解析覆盖层对象。
     *
     * @param uri     Uri 对象
     * @param context 上下文对象
     * @return 覆盖层对象
     * @throws IOException                      如果遇到读写错误
     * @throws BadOverlayProfileFormatException 如果遇到格式错误
     */
    @NonNull
    public static OverlayProfile parse(@NonNull Uri uri, @NonNull Context context)
            throws IOException, BadOverlayProfileFormatException {

        try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
            if (stream == null)
                throw new FileNotFoundException("Failed to open uri: " + uri);
            return parse(stream);
        }
    }

    /**
     * 从文件解析覆盖层对象。
     *
     * @param profilePath 文件路径
     * @return 覆盖层对象
     * @throws IOException                      如果遇到读写错误
     * @throws BadOverlayProfileFormatException 如果遇到格式错误
     */
    @NonNull
    public static OverlayProfile parse(@NonNull Path profilePath)
            throws IOException, BadOverlayProfileFormatException {

        try (InputStream stream = Files.newInputStream(profilePath)) {
            return parse(stream);
        }
    }

    /**
     * 从输入流解析覆盖层对象。
     *
     * @param inputStream 输入流
     * @return 覆盖层对象
     * @throws IOException                      如果遇到读写错误
     * @throws BadOverlayProfileFormatException 如果遇到格式错误
     */
    @NonNull
    public static OverlayProfile parse(@NonNull InputStream inputStream)
            throws IOException, BadOverlayProfileFormatException {

        JsonObject json;

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            json = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (RuntimeException e) {
            throw new BadOverlayProfileFormatException(e);
        }

        return buildOverlayProfile(json);
    }

    @NonNull
    private static OverlayProfile buildOverlayProfile(@NonNull JsonObject json)
            throws BadOverlayProfileFormatException {

        int schemaVersion = json.get(SchemaVersion).getAsInt();

        try {
            if (schemaVersion == VERSION_1)
                return buildProfileVersion1(json);
        } catch (RuntimeException e) {
            throw new BadOverlayProfileFormatException(e);
        }

        throw new BadOverlayProfileFormatException("Unsupported schemaVersion: " + schemaVersion);
    }

    @NonNull
    private static OverlayProfile buildProfileVersion1(@NonNull JsonObject json) {
        String name = json.get(Name).getAsString();
        List<BaseWidget.Config> configs = new ArrayList<>();
        JsonArray configArray = json.get(Widgets).getAsJsonArray();

        for (JsonElement configJson : configArray)
            configs.add(parseConfig(configJson.getAsJsonObject()));

        OverlayProfile profile = new OverlayProfile();
        profile.setName(name);
        profile.setConfigs(configs);

        return profile;
    }

    @NonNull
    private static BaseWidget.Config parseConfig(@NonNull JsonObject configJson) {
        WidgetType type = WidgetType.valueOf(configJson.get(Type).getAsString());
        return switch (type) {
            case Button -> parseButtonConfig(configJson);
            case DPad -> parseDPadConfig(configJson);
            case ThumbStick -> parseThumbStickConfig(configJson);
            default -> throw new IllegalArgumentException("Unsupported widget: " + type.name());
        };
    }

    private static void parseBaseConfig(@NonNull JsonObject configJson,
                                        @NonNull BaseWidget.Config config) {

        config.normalizedX = configJson.get(X).getAsFloat();
        config.normalizedY = configJson.get(Y).getAsFloat();
        config.scale = configJson.get(Scale).getAsFloat();
        config.opacity = configJson.get(Opacity).getAsFloat();
        config.hide = configJson.get(Hide).getAsBoolean();
    }

    @Nullable
    private static Binding parseBindings(@NonNull JsonObject bindingJson) {
        if (bindingJson.isEmpty())
            return null;

        Binding.Type type = Binding.Type.valueOf(bindingJson.get(Type).getAsString());
        String item = bindingJson.get(Item).getAsString();
        return switch (type) {
            case Keyboard -> new Binding(type, StandardKey.valueOf(item));
            case MouseButton, ControllerButton -> new Binding(type, StandardButton.valueOf(item));
            case MouseAction, ControllerAction -> new Binding(type, StandardAction.valueOf(item),
                    bindingJson.get(Value).getAsFloat());
            default -> throw new IllegalArgumentException("Unsupported binding: " + type.name());
        };
    }

    @NonNull
    private static ButtonWidget.Config parseButtonConfig(@NonNull JsonObject configJson) {
        ButtonWidget.Config config = new ButtonWidget.Config();
        config.shape = ButtonWidget.Shape.valueOf(configJson.get(Shape).getAsString());
        config.text = configJson.get(Text).getAsString();
        config.toggleSwitch = configJson.get(ToggleSwitch).getAsBoolean();
        JsonArray bindingArray = configJson.get(Bindings).getAsJsonArray();
        for (int i = 0; i < bindingArray.size(); i++)
            config.bindings[i] = parseBindings(bindingArray.get(i).getAsJsonObject());
        parseBaseConfig(configJson, config);
        return config;
    }

    @NonNull
    private static DPadWidget.Config parseDPadConfig(@NonNull JsonObject configJson) {
        DPadWidget.Config config = new DPadWidget.Config();
        config.enable8Way = configJson.get(Enable8Way).getAsBoolean();
        JsonArray bindingArray = configJson.get(Bindings).getAsJsonArray();
        for (int i = 0; i < bindingArray.size(); i++)
            config.bindings[i] = parseBindings(bindingArray.get(i).getAsJsonObject());
        parseBaseConfig(configJson, config);
        return config;
    }

    @NonNull
    private static ThumbStickWidget.Config parseThumbStickConfig(@NonNull JsonObject configJson) {
        ThumbStickWidget.Config config = new ThumbStickWidget.Config();
        config.mode = ThumbStickWidget.Mode.valueOf(configJson.get(Mode).getAsString());
        if (config.mode == ThumbStickWidget.Mode.LeftThumbStick ||
                config.mode == ThumbStickWidget.Mode.RightThumbStick ||
                config.mode == ThumbStickWidget.Mode.Mouse) {
            config.invertX = configJson.get(InvertX).getAsBoolean();
            config.invertY = configJson.get(InvertY).getAsBoolean();
        } else if (config.mode == ThumbStickWidget.Mode.Mapping) {
            config.enable8Way = configJson.get(Enable8Way).getAsBoolean();
            JsonArray bindingArray = configJson.get(Bindings).getAsJsonArray();
            for (int i = 0; i < bindingArray.size(); i++)
                config.bindings[i] = parseBindings(bindingArray.get(i).getAsJsonObject());
        }
        parseBaseConfig(configJson, config);
        return config;
    }

    public static class BadOverlayProfileFormatException extends Exception {

        public BadOverlayProfileFormatException(String message) {
            super(message);
        }

        public BadOverlayProfileFormatException(Throwable cause) {
            super(cause);
        }
    }
}
