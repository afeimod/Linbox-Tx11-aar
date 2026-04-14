package com.winfusion.feature.input.overlay.utils;

import static com.winfusion.feature.input.overlay.utils.Constants.*;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.winfusion.feature.input.overlay.OverlayProfile;
import com.winfusion.feature.input.overlay.bind.Binding;
import com.winfusion.feature.input.overlay.widget.BaseWidget;
import com.winfusion.feature.input.overlay.widget.ButtonWidget;
import com.winfusion.feature.input.overlay.widget.DPadWidget;
import com.winfusion.feature.input.overlay.widget.ThumbStickWidget;
import com.winfusion.feature.input.overlay.widget.WidgetType;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

/**
 * 覆盖层导出器，用于输出配置到文件。
 */
public final class OverlayExporter {

    private OverlayExporter() {

    }

    /**
     * 保存到文件。
     *
     * @param profile 配置文件对象
     * @param path    文件路径
     * @throws IOException 如果遇到读写错误
     */
    public static void save(@NonNull OverlayProfile profile, @NonNull Path path) throws IOException {
        try (FileWriter writer = new FileWriter(path.toFile())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject json = profileToJson(profile);
            gson.toJson(json, writer);
        }
    }

    @NonNull
    private static JsonObject profileToJson(@NonNull OverlayProfile profile) {
        JsonObject json = new JsonObject();
        json.add(SchemaVersion, new JsonPrimitive(OverlayProfile.VERSION_1));
        json.add(Name, new JsonPrimitive(profile.getName()));

        Collection<BaseWidget.Config> configs = profile.getConfigs();
        JsonArray configArray = new JsonArray();
        for (BaseWidget.Config config : configs)
            configArray.add(configToJson(config));
        json.add(Widgets, configArray);
        return json;
    }

    @NonNull
    private static JsonObject configToJson(@NonNull BaseWidget.Config config) {
        if (config instanceof ButtonWidget.Config buttonConfig)
            return buttonToJson(buttonConfig);
        else if (config instanceof DPadWidget.Config dpadConfig)
            return dpadToJson(dpadConfig);
        else if (config instanceof ThumbStickWidget.Config thumbStickConfig)
            return thumbStickToJson(thumbStickConfig);
        else
            throw new IllegalArgumentException("Unsupported widget: " + config.getClass());
    }

    @NonNull
    private static JsonObject buttonToJson(@NonNull ButtonWidget.Config config) {
        JsonObject json = new JsonObject();
        json.add(Type, new JsonPrimitive(WidgetType.Button.name()));
        baseToJson(config, json);
        json.add(Shape, new JsonPrimitive(config.shape.toString()));
        json.add(Text, new JsonPrimitive(config.text));
        json.add(ToggleSwitch, new JsonPrimitive(config.toggleSwitch));
        json.add(Bindings, bindingToJson(config.bindings));
        return json;
    }

    @NonNull
    private static JsonObject dpadToJson(@NonNull DPadWidget.Config config) {
        JsonObject json = new JsonObject();
        json.add(Type, new JsonPrimitive(WidgetType.DPad.name()));
        baseToJson(config, json);
        json.add(Enable8Way, new JsonPrimitive(config.enable8Way));
        json.add(Bindings, bindingToJson(config.bindings));
        return json;
    }

    @NonNull
    private static JsonObject thumbStickToJson(@NonNull ThumbStickWidget.Config config) {
        JsonObject json = new JsonObject();
        json.add(Type, new JsonPrimitive(WidgetType.ThumbStick.name()));
        baseToJson(config, json);
        json.add(Mode, new JsonPrimitive(config.mode.name()));
        if (config.mode == ThumbStickWidget.Mode.LeftThumbStick ||
                config.mode == ThumbStickWidget.Mode.RightThumbStick ||
                config.mode == ThumbStickWidget.Mode.Mouse) {
            json.add(InvertX, new JsonPrimitive(config.invertX));
            json.add(InvertY, new JsonPrimitive(config.invertY));
        } else if (config.mode == ThumbStickWidget.Mode.Mapping) {
            json.add(Enable8Way, new JsonPrimitive(config.enable8Way));
            json.add(Bindings, bindingToJson(config.bindings));
        }
        return json;
    }

    private static void baseToJson(@NonNull BaseWidget.Config config, @NonNull JsonObject json) {
        json.add(X, new JsonPrimitive(config.normalizedX));
        json.add(Y, new JsonPrimitive(config.normalizedY));
        json.add(Scale, new JsonPrimitive(config.scale));
        json.add(Opacity, new JsonPrimitive(config.opacity));
        json.add(Hide, new JsonPrimitive(config.hide));
    }

    @NonNull
    private static JsonElement bindingToJson(@NonNull Binding[] bindings) {
        JsonArray array = new JsonArray();
        for (Binding binding : bindings) {
            JsonObject bindingJson = new JsonObject();
            if (binding != null) {
                Binding.Type type = binding.getType();
                bindingJson.add(Type, new JsonPrimitive(type.name()));
                bindingJson.add(Item, new JsonPrimitive(binding.getItem().name()));
                if (type == Binding.Type.MouseAction || type == Binding.Type.ControllerAction)
                    bindingJson.add(Value, new JsonPrimitive(binding.getValue()));
            }
            array.add(bindingJson);
        }
        return array;
    }
}
