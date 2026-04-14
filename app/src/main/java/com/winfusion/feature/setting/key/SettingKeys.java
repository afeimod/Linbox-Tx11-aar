package com.winfusion.feature.setting.key;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.feature.launcher.Constants;
import com.winfusion.feature.setting.Key;
import com.winfusion.feature.setting.value.ConfigElement;
import com.winfusion.feature.setting.value.ConfigPrimitive;
import com.winfusion.utils.SystemUtils;

/**
 * 一些设置项键的枚举，以及键对应的默认值。
 */
public enum SettingKeys implements Key {

    // container root
    Container("Container"),

    // general
    ContainerInfo(Container, "Info"),
    ContainerInfoName(ContainerInfo, "Name"),
    ContainerInfoRegion(ContainerInfo, "Region", v(SystemUtils.getRegion())),
    ContainerInfoCreatedTime(ContainerInfo, "CreatedTime"),
    ContainerInfoWineVersionAtCreation(ContainerInfo, "WineVersionAtCreation"),
    ContainerInfoUUID(ContainerInfo, "UUID"),

    // display
    ContainerDisplay(Container, "Display"),
    ContainerDisplayResolution(ContainerDisplay, "Resolution", v("1280x720")),
    ContainerDisplayRefreshRate(ContainerDisplay, "RefreshRate", v(60)),
    ContainerDisplayOrientation(ContainerDisplay, "Orientation",
            v(SettingOptions.DisplayOrientation.Landscape.option())),
    ContainerDisplayScalingMode(ContainerDisplay, "ScalingMode",
            v(SettingOptions.DisplayScalingMode.Fit.option())),
    ContainerDisplayRendererBackend(ContainerDisplay, "RendererBackend",
            v(SettingOptions.DisplayRendererBackend.PixmanRenderer.option())),

    // audio
    ContainerAudio(Container, "Audio"),
    ContainerAudioDriver(ContainerAudio, "Driver",
            v(SettingOptions.AudioDriver.ALSA.option())),
    ContainerAudioBackend(ContainerAudio, "Backend",
            v(SettingOptions.AudioBackend.AAudio.option())),
    ContainerAudioVolumeGain(ContainerAudio, "VolumeGain", v(0)),
    ContainerAudioBackgroundPlayback(ContainerAudio, "BackgroundPlayback", v(true)),
    ContainerAudioMIDIVolumeGain(ContainerAudio, "MIDIVolumeGain", v(0)),
    ContainerAudioMIDISoundFont(ContainerAudio, "MIDISoundFont", v("builtin")),

    // graphics
    ContainerGraphics(Container, "Graphics"),
    ContainerGraphicsDriver(ContainerGraphics, "Driver"),
    ContainerGraphicsDriverVulkan(ContainerGraphicsDriver, "Vulkan", v("builtin")),
    ContainerGraphicsDriverOpenGL(ContainerGraphicsDriver, "OpenGL", v("builtin")),
    ContainerGraphicsDirectXWrapper(ContainerGraphics, "DirectXWrapper"),
    ContainerGraphicsDirectXWrapper8_11(ContainerGraphicsDirectXWrapper, "8_11", v("builtin")),
    ContainerGraphicsDirectXWrapper12(ContainerGraphicsDirectXWrapper, "12", v("builtin")),

    // wine
    ContainerWine(Container, "Wine"), // group
    ContainerWineVersion(ContainerWine, "Version", v(Constants.BUILTIN_WINE_VERSION)),

    // box64
    ContainerBox64(Container, "Box64"), // group
    ContainerBox64Version(ContainerBox64, "Version", v(Constants.BUILTIN_BOX64_VERSION)),
    ContainerBox64Preset(ContainerBox64, "Preset", v("builtin")),
    ContainerBox64RunControlFile(ContainerBox64, "RunControlFile", v("builtin")),

    // control overlay
    ContainerControlOverlay(Container, "ControlOverlay"),
    ContainerControlOverlayProfile(ContainerControlOverlay, "Profile", v("default-1.json")),

    // installed
    ContainerInstalled(Container, "Installed"),
    ContainerInstalledWine(ContainerInstalled, "Wine"),
    ContainerInstalledDirectXWrapper8_11(ContainerInstalled, "DirectXWrapper8_11"),
    ContainerInstalledDirectXWrapper12(ContainerInstalled, "DirectXWrapper12"),

    // shortcut
    Shortcut("Shortcut"),
    ShortcutInfo(Shortcut, "Info"),
    ShortcutInfoName(ShortcutInfo, "Name"),
    ShortcutInfoExecArgs(ShortcutInfo, "ExecArgs"),
    ShortcutInfoCreatedTime(ShortcutInfo, "CreatedTime"),
    ShortcutInfoUUID(ShortcutInfo, "UUID"),
    ShortcutInfoTarget(ShortcutInfo, "Target"),
    ShortcutInfoLnkName(ShortcutInfo, "LnkName"),

    // label
    Label("Label"),
    LabelMIDI(Label, "MIDI"),
    LabelMIDISoundfont(LabelMIDI, "Soundfont"),
    LabelVulkan(Label, "Vulkan"),
    LabelVulkanChoose(LabelVulkan, "Choose"),
    LabelVulkanConfig(LabelVulkan, "Config"),
    LabelOpenGL(Label, "OpenGL"),
    LabelOpenGLChoose(LabelOpenGL, "Choose"),
    LabelOpenGLConfig(LabelOpenGL, "Config"),
    LabelDirectXWrapper(Label, "DirectXWrapper"),
    LabelDirectXWrapper8_11(LabelDirectXWrapper, "8_11"),
    LabelDirectXWrapper8_11Choose(LabelDirectXWrapper8_11, "Choose"),
    LabelDirectXWrapper12(LabelDirectXWrapper, "12"),
    LabelDirectXWrapper12Choose(LabelDirectXWrapper12, "Choose"),
    LabelDirectXWrapperConfig(LabelDirectXWrapper, "Config");

    private final String key;
    private final ConfigElement defaultValue;

    SettingKeys(@NonNull String key, @Nullable ConfigElement defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    SettingKeys(@NonNull String key) {
        this(key, null);
    }

    SettingKeys(@NonNull Key parent, @NonNull String selfKey) {
        this(parent, selfKey, null);
    }

    SettingKeys(@NonNull Key parent, @NonNull String selfKey, @Nullable ConfigElement defaultValue) {
        this(parent.key() + "/" + selfKey, defaultValue);
    }

    @NonNull
    @Override
    public String key() {
        return key;
    }

    @Nullable
    public ConfigElement getDefaultValue() {
        return defaultValue;
    }

    @NonNull
    private static ConfigElement v(int value) {
        return new ConfigPrimitive(value);
    }

    @NonNull
    private static ConfigElement v(float value) {
        return new ConfigPrimitive(value);
    }

    @NonNull
    private static ConfigElement v(boolean value) {
        return new ConfigPrimitive(value);
    }

    @NonNull
    private static ConfigElement v(String value) {
        return new ConfigPrimitive(value);
    }
}
