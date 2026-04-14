package com.winfusion.feature.setting.key;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.Key;
import com.winfusion.feature.setting.value.ConfigElement;
import com.winfusion.feature.setting.value.ConfigPrimitive;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置项包装类，用于为设置项提供方便的获取和设定方法并隐藏 {@link Config} 的使用细节。
 * 使用这个类的前提是设置项类型以及值是该类和 {@link SettingKeys} 约定的类型与值。
 */
public class SettingWrapper {

    private final Config config;

    public SettingWrapper(@NonNull Config config) {
        this.config = config;
    }

    /**
     * 获取容器的名称。
     * 关联 {@link SettingKeys#ContainerInfoName}
     *
     * @return 容器的名称
     */
    @NonNull
    public String getContainerInfoName() {
        ConfigElement element = getFromGlobalCertain(SettingKeys.ContainerInfoName);
        return element == null ? "" : element.getAsString();
    }

    /**
     * 设定容器的名称。
     * 关联 {@link SettingKeys#ContainerInfoName}
     *
     * @param name 容器的名称
     */
    public void setContainerInfoName(@NonNull String name) {
        setForGlobalCertain(SettingKeys.ContainerInfoName, new ConfigPrimitive(name));
    }

    /**
     * 获取容器的区域。
     * 关联 {@link SettingKeys#ContainerInfoRegion}
     *
     * @return 容器的区域
     */
    @NonNull
    public String getContainerInfoRegion() {
        return getFromLocalPriority(SettingKeys.ContainerInfoRegion).getAsString();
    }

    /**
     * 设定容器的区域。
     * 关联 {@link SettingKeys#ContainerInfoRegion}
     *
     * @param region 容器的区域
     */
    public void setContainerInfoRegion(@NonNull String region) {
        setForLocalPriority(SettingKeys.ContainerInfoRegion, new ConfigPrimitive(region));
    }

    /**
     * 获取容器的创建时间。
     * 关联 {@link SettingKeys#ContainerInfoCreatedTime}
     *
     * @return 容器的创建时间
     */
    @NonNull
    public String getContainerInfoCreatedTime() {
        ConfigElement element = getFromGlobalCertain(SettingKeys.ContainerInfoCreatedTime);
        return element == null ? "" : element.getAsString();
    }

    /**
     * 设定容器的创建时间。
     * 关联 {@link SettingKeys#ContainerInfoCreatedTime}
     *
     * @param time 容器的创建时间
     */
    public void setContainerInfoCreatedTime(@NonNull String time) {
        setForGlobalCertain(SettingKeys.ContainerInfoCreatedTime, new ConfigPrimitive(time));
    }

    /**
     * 获取容器在首次启动时使用的 Wine 版本。
     * 关联 {@link SettingKeys#ContainerInfoWineVersionAtCreation}
     *
     * @return Wine 版本
     */
    @NonNull
    public String getContainerInfoWineVersionAtCreation() {
        ConfigElement element = getFromGlobalCertain(SettingKeys.ContainerInfoWineVersionAtCreation);
        return element == null ? "" : element.getAsString();
    }

    /**
     * 设定容器首次启动时的 Wine 版本。
     * 关联 {@link SettingKeys#ContainerInfoWineVersionAtCreation}
     *
     * @param version 首次启动时的 Wine 版本
     */
    public void setContainerInfoWineVersionAtCreation(@NonNull String version) {
        setForGlobalCertain(SettingKeys.ContainerInfoWineVersionAtCreation,
                new ConfigPrimitive(version));
    }

    /**
     * 获取容器的唯一标识符。
     * 关联 {@link SettingKeys#ContainerInfoUUID}
     *
     * @return 容器的唯一标识符
     */
    @NonNull
    public String getContainerInfoUUID() {
        ConfigElement element = getFromGlobalCertain(SettingKeys.ContainerInfoUUID);
        return element == null ? "" : element.getAsString();
    }

    /**
     * 设定容器的唯一标识符。
     * 关联 {@link SettingKeys#ContainerInfoUUID}
     *
     * @param uuid 唯一标识符
     */
    public void setContainerInfoUUID(@NonNull String uuid) {
        setForGlobalCertain(SettingKeys.ContainerInfoUUID, new ConfigPrimitive(uuid));
    }

    /**
     * 获取容器的显示分辨率。
     * 关联 {@link SettingKeys#ContainerDisplayResolution}
     *
     * @return 容器的显示分辨率
     */
    @NonNull
    public String getContainerDisplayResolution() {
        return getFromLocalPriority(SettingKeys.ContainerDisplayResolution).getAsString();
    }

    /**
     * 设定容器的显示分辨率。
     * 关联 {@link SettingKeys#ContainerDisplayResolution}
     *
     * @param resolution 容器的显示分辨率
     */
    public void setContainerDisplayResolution(@NonNull String resolution) {
        setForLocalPriority(SettingKeys.ContainerDisplayResolution, new ConfigPrimitive(resolution));
    }

    /**
     * 获取容器的显示刷新率。
     * 关联 {@link SettingKeys#ContainerDisplayRefreshRate}
     *
     * @return 容器的显示刷新率
     */
    public int getContainerDisplayRefreshRate() {
        return getFromLocalPriority(SettingKeys.ContainerDisplayRefreshRate).getAsInt();
    }

    /**
     * 设定容器的显示刷新率。
     * 关联 {@link SettingKeys#ContainerDisplayRefreshRate}
     *
     * @param rate 容器的显示刷新率
     */
    public void setContainerDisplayRefreshRate(int rate) {
        setForLocalPriority(SettingKeys.ContainerDisplayRefreshRate, new ConfigPrimitive(rate));
    }

    /**
     * 获取容器的显示方向。
     * 关联 {@link SettingKeys#ContainerDisplayOrientation}
     *
     * @return 容器的显示方向
     */
    @NonNull
    public SettingOptions.DisplayOrientation getContainerDisplayOrientation() {
        return SettingOptions.DisplayOrientation.from(
                getFromLocalPriority(SettingKeys.ContainerDisplayOrientation).getAsString());
    }

    /**
     * 设定容器的显示方向。
     * 关联 {@link SettingKeys#ContainerDisplayOrientation}
     *
     * @param orientation 容器的显示方向
     */
    public void setContainerDisplayOrientation(@NonNull SettingOptions.DisplayOrientation orientation) {
        setForLocalPriority(SettingKeys.ContainerDisplayOrientation,
                new ConfigPrimitive(orientation.option()));
    }

    /**
     * 获取容器的显示缩放模式。
     * 关联 {@link SettingKeys#ContainerDisplayScalingMode}
     *
     * @return 容器的显示缩放模式
     */
    @NonNull
    public SettingOptions.DisplayScalingMode getContainerDisplayScalingMode() {
        return SettingOptions.DisplayScalingMode.from(
                getFromLocalPriority(SettingKeys.ContainerDisplayScalingMode).getAsString());
    }

    public void setContainerDisplayScalingMode(@NonNull SettingOptions.DisplayScalingMode mode) {
        setForLocalPriority(SettingKeys.ContainerDisplayScalingMode,
                new ConfigPrimitive(mode.option()));
    }

    /**
     * 获取容器的渲染后端。
     * 关联 {@link SettingKeys#ContainerDisplayRendererBackend}
     *
     * @return 容器的渲染后端
     */
    @NonNull
    public SettingOptions.DisplayRendererBackend getContainerDisplayRendererBackend() {
        return SettingOptions.DisplayRendererBackend.from(
                getFromLocalPriority(SettingKeys.ContainerDisplayRendererBackend).getAsString());
    }

    /**
     * 设定容器的渲染后端。
     * 关联 {@link SettingKeys#ContainerDisplayRendererBackend}
     *
     * @param backend 容器的渲染后端
     */
    public void setContainerDisplayRendererBackend(@NonNull SettingOptions.DisplayRendererBackend backend) {
        setForLocalPriority(SettingKeys.ContainerDisplayRendererBackend,
                new ConfigPrimitive(backend.option()));
    }

    /**
     * 获取容器的音频驱动。
     * 关联 {@link SettingKeys#ContainerAudioDriver}
     *
     * @return 容器的音频驱动
     */
    @NonNull
    public SettingOptions.AudioDriver getContainerAudioDriver() {
        return SettingOptions.AudioDriver.from(
                getFromLocalPriority(SettingKeys.ContainerAudioDriver).getAsString());
    }

    /**
     * 设定容器的音频驱动。
     * 关联 {@link SettingKeys#ContainerAudioDriver}
     *
     * @param driver 容器的音频驱动
     */
    public void setContainerAudioDriver(@NonNull SettingOptions.AudioDriver driver) {
        setForLocalPriority(SettingKeys.ContainerAudioDriver, new ConfigPrimitive(driver.option()));
    }

    /**
     * 获取容器的音频后端。
     * 关联 {@link SettingKeys#ContainerAudioBackend}
     *
     * @return 容器的音频后端
     */
    @NonNull
    public SettingOptions.AudioBackend getContainerAudioBackend() {
        return SettingOptions.AudioBackend.from(
                getFromLocalPriority(SettingKeys.ContainerAudioBackend).getAsString());
    }

    /**
     * 设定容器的音频后端。
     * 关联 {@link SettingKeys#ContainerAudioBackend}
     *
     * @param backend 容器的音频后端
     */
    public void setContainerAudioBackend(@NonNull SettingOptions.AudioBackend backend) {
        setForLocalPriority(SettingKeys.ContainerAudioBackend,
                new ConfigPrimitive(backend.option()));
    }

    /**
     * 获取容器的音量增益。
     * 关联 {@link SettingKeys#ContainerAudioVolumeGain}
     *
     * @return 容器的音量增益
     */
    public int getContainerAudioVolumeGain() {
        return getFromLocalPriority(SettingKeys.ContainerAudioVolumeGain).getAsInt();
    }

    /**
     * 设定容器的音量增益。
     * 关联 {@link SettingKeys#ContainerAudioVolumeGain}
     *
     * @param gain 容器的音量增益
     */
    public void setContainerAudioVolumeGain(int gain) {
        setForLocalPriority(SettingKeys.ContainerAudioVolumeGain, new ConfigPrimitive(gain));
    }

    /**
     * 获取容器是否开启音频后台播放。
     * 关联 {@link SettingKeys#ContainerAudioBackgroundPlayback}
     *
     * @return 开启则返回 true，否则返回 false
     */
    public boolean getContainerAudioBackgroundPlayback() {
        return getFromLocalPriority(SettingKeys.ContainerAudioBackgroundPlayback).getAsBool();
    }

    /**
     * 设定容器是否开启音频后台播放。
     * 关联 {@link SettingKeys#ContainerAudioBackgroundPlayback}
     *
     * @param playback 是否开启音频后台播放
     */
    public void setContainerAudioBackgroundPlayback(boolean playback) {
        setForLocalPriority(SettingKeys.ContainerAudioBackgroundPlayback,
                new ConfigPrimitive(playback));
    }

    /**
     * 获取容器的 MIDI 音量增益。
     * 关联 {@link SettingKeys#ContainerAudioMIDIVolumeGain}
     *
     * @return 容器的 MIDI 音量增益
     */
    public int getContainerAudioMIDIVolumeGain() {
        return getFromLocalPriority(SettingKeys.ContainerAudioMIDIVolumeGain).getAsInt();
    }

    /**
     * 设定容器的 MIDI 音量增益。
     * 关联 {@link SettingKeys#ContainerAudioMIDIVolumeGain}
     *
     * @param gain 容器的 MIDI 音量增益
     */
    public void setContainerAudioMIDIVolumeGain(int gain) {
        setForLocalPriority(SettingKeys.ContainerAudioMIDIVolumeGain, new ConfigPrimitive(gain));
    }

    /**
     * 获取容器的 MIDI 音乐字体文件名。
     * 关联 {@link SettingKeys#ContainerAudioMIDISoundFont}
     *
     * @return 容器的 MIDI 音乐字体文件名
     */
    @NonNull
    public String getContainerAudioMIDISoundFont() {
        return getFromLocalPriority(SettingKeys.ContainerAudioMIDISoundFont).getAsString();
    }

    /**
     * 设定容器的 MIDI 音乐字体文件名。
     * 关联 {@link SettingKeys#ContainerAudioMIDISoundFont}
     *
     * @param soundfont 容器的 MIDI 音乐字体文件名
     */
    public void setContainerAudioMIDISoundFont(@NonNull String soundfont) {
        setForLocalPriority(SettingKeys.ContainerAudioMIDISoundFont, new ConfigPrimitive(soundfont));
    }

    /**
     * 获取容器的 Vulkan 驱动名。
     * 关联 {@link SettingKeys#ContainerGraphicsDriverVulkan}
     *
     * @return 容器的 Vulkan 驱动名
     */
    @NonNull
    public String getContainerGraphicsDriverVulkan() {
        return getFromLocalPriority(SettingKeys.ContainerGraphicsDriverVulkan).getAsString();
    }

    /**
     * 设定容器的 Vulkan 驱动名。
     * 关联 {@link SettingKeys#ContainerGraphicsDriverVulkan}
     *
     * @param driver 容器的 Vulkan 驱动名
     */
    public void setContainerGraphicsDriverVulkan(@NonNull String driver) {
        setForLocalPriority(SettingKeys.ContainerGraphicsDriverVulkan, new ConfigPrimitive(driver));
    }

    /**
     * 获取容器的 OpenGL 驱动名。
     * 关联 {@link SettingKeys#ContainerGraphicsDriverOpenGL}
     *
     * @return 容器的 OpenGL 驱动名
     */
    @NonNull
    public String getContainerGraphicsDriverOpenGL() {
        return getFromLocalPriority(SettingKeys.ContainerGraphicsDriverOpenGL).getAsString();
    }

    /**
     * 设定容器的 OpenGL 驱动名。
     * 关联 {@link SettingKeys#ContainerGraphicsDriverOpenGL}
     *
     * @param driver 容器的 OpenGL 驱动名
     */
    public void setContainerGraphicsDriverOpenGL(@NonNull String driver) {
        setForLocalPriority(SettingKeys.ContainerGraphicsDriverOpenGL, new ConfigPrimitive(driver));
    }

    /**
     * 获取容器的 DX8_11 包装器名。
     * 关联 {@link SettingKeys#ContainerGraphicsDirectXWrapper8_11}
     *
     * @return 容器的 DX8_11 包装器名
     */
    @NonNull
    public String getContainerGraphicsDirectXWrapper8_11() {
        return getFromLocalPriority(SettingKeys.ContainerGraphicsDirectXWrapper8_11).getAsString();
    }

    /**
     * 设定容器的 DX8_11 包装器名。
     * 关联 {@link SettingKeys#ContainerGraphicsDirectXWrapper8_11}
     *
     * @param wrapper 容器的 DX8_11 包装器名
     */
    public void setContainerGraphicsDirectXWrapper8_11(@NonNull String wrapper) {
        setForLocalPriority(SettingKeys.ContainerGraphicsDirectXWrapper8_11,
                new ConfigPrimitive(wrapper));
    }

    /**
     * 获取容器的 DX12 包装器名。
     * 关联 {@link SettingKeys#ContainerGraphicsDirectXWrapper12}
     *
     * @return 容器的 DX12 包装器名
     */
    @NonNull
    public String getContainerGraphicsDirectXWrapper12() {
        return getFromLocalPriority(SettingKeys.ContainerGraphicsDirectXWrapper12).getAsString();
    }

    /**
     * 设定容器的 DX12 包装器名。
     * 关联 {@link SettingKeys#ContainerGraphicsDirectXWrapper12}
     *
     * @param wrapper 容器的 DX12 包装器名
     */
    public void setContainerGraphicsDirectXWrapper12(@NonNull String wrapper) {
        setForLocalPriority(SettingKeys.ContainerGraphicsDirectXWrapper12,
                new ConfigPrimitive(wrapper));
    }

    /**
     * 获取容器的 Wine 版本。
     * 关联 {@link SettingKeys#ContainerWineVersion}
     *
     * @return 容器的 Wine 版本
     */
    @NonNull
    public String getContainerWineVersion() {
        return getFromGlobalPriority(SettingKeys.ContainerWineVersion).getAsString();
    }

    /**
     * 设定容器的 Wine 版本。
     * 关联 {@link SettingKeys#ContainerWineVersion}
     *
     * @param version 容器的 Wine 版本
     */
    public void setContainerWineVersion(@NonNull String version) {
        setForGlobalCertain(SettingKeys.ContainerWineVersion, new ConfigPrimitive(version));
    }

    /**
     * 获取容器的 Box64 版本。
     * 关联 {@link SettingKeys#ContainerBox64Version}
     *
     * @return 容器的 Box64 版本
     */
    @NonNull
    public String getContainerBox64Version() {
        return getFromLocalPriority(SettingKeys.ContainerBox64Version).getAsString();
    }

    /**
     * 设定容器的 Box64 版本。
     * 关联 {@link SettingKeys#ContainerBox64Version}
     *
     * @param version 容器的 Box64 版本
     */
    public void setContainerBox64Version(@NonNull String version) {
        setForLocalPriority(SettingKeys.ContainerBox64Version, new ConfigPrimitive(version));
    }

    /**
     * 获取容器的 Box64 预设。
     * 关联 {@link SettingKeys#ContainerBox64Preset}
     *
     * @return 容器的 Box64 预设
     */
    @NonNull
    public String getContainerBox64Preset() {
        return getFromLocalPriority(SettingKeys.ContainerBox64Preset).getAsString();
    }

    /**
     * 设定容器的 Box64 预设。
     * 关联 {@link SettingKeys#ContainerBox64Preset}
     *
     * @param preset 容器的 Box64 预设
     */
    public void setContainerBox64Preset(@NonNull String preset) {
        setForLocalPriority(SettingKeys.ContainerBox64Preset, new ConfigPrimitive(preset));
    }

    /**
     * 获取容器的 Box64 运行控制文件。
     * 关联 {@link SettingKeys#ContainerBox64RunControlFile}
     *
     * @return 容器的 Box64 运行控制文件
     */
    @NonNull
    public String getContainerBox64RunControlFile() {
        return getFromLocalPriority(SettingKeys.ContainerBox64RunControlFile).getAsString();
    }

    /**
     * 设定容器的 Box64 运行控制文件。
     * 关联 {@link SettingKeys#ContainerBox64RunControlFile}
     *
     * @param rc 容器的 Box64 运行控制文件
     */
    public void setContainerBox64RunControlFile(@NonNull String rc) {
        setForLocalPriority(SettingKeys.ContainerBox64RunControlFile, new ConfigPrimitive(rc));
    }

    /**
     * 获取容器的控制覆盖层。
     * 关联 {@link SettingKeys#ContainerControlOverlayProfile}
     *
     * @return 容器的控制覆盖层
     */
    @NonNull
    public String getContainerControlOverlayProfile() {
        return getFromLocalPriority(SettingKeys.ContainerControlOverlayProfile).getAsString();
    }

    /**
     * 设定容器的控制覆盖层。
     * 关联 {@link SettingKeys#ContainerControlOverlayProfile}
     *
     * @param overlay 容器的控制覆盖层
     */
    public void setContainerControlOverlayProfile(@NonNull String overlay) {
        setForLocalPriority(SettingKeys.ContainerControlOverlayProfile,
                new ConfigPrimitive(overlay));
    }

    /**
     * 获取容器当前安装的 Wine 版本。
     * 关联 {@link SettingKeys#ContainerInstalledWine}
     *
     * @return 容器当前安装的 Wine 版本
     */
    @NonNull
    public String getContainerInstalledWine() {
        ConfigElement element = getFromGlobalCertain(SettingKeys.ContainerInstalledWine);
        return element == null ? "" : element.getAsString();
    }

    /**
     * 设定容器当前安装的 Wine 版本。
     * 关联 {@link SettingKeys#ContainerInstalledWine}
     *
     * @param wine 当前安装的 Wine 版本
     */
    public void setContainerInstalledWine(@NonNull String wine) {
        setForGlobalCertain(SettingKeys.ContainerInstalledWine, new ConfigPrimitive(wine));
    }

    /**
     * 获取容器当前安装的 DX8_11 包装器版本。
     * 关联 {@link SettingKeys#ContainerInstalledDirectXWrapper8_11}
     *
     * @return 容器当前安装的 DX8_11 包装器版本
     */
    @NonNull
    public String getContainerInstalledDirectXWrapper8_11() {
        ConfigElement element = getFromGlobalCertain(SettingKeys.ContainerInstalledDirectXWrapper8_11);
        return element == null ? "" : element.getAsString();
    }

    /**
     * 设定容器当前安装的 DX8_11 包装器版本。
     * 关联 {@link SettingKeys#ContainerInstalledDirectXWrapper8_11}
     *
     * @param wrapper 当前安装的 DX8_11 包装器版本
     */
    public void setContainerInstalledDirectXWrapper8_11(@NonNull String wrapper) {
        setForGlobalCertain(SettingKeys.ContainerInstalledDirectXWrapper8_11,
                new ConfigPrimitive(wrapper));
    }

    /**
     * 获取容器当前安装的 DX12 包装器版本。
     * 关联 {@link SettingKeys#ContainerInstalledDirectXWrapper12}
     *
     * @return 容器当前安装的 DX12 包装器版本
     */
    @NonNull
    public String getContainerInstallDirectXWrapper12() {
        ConfigElement element = getFromGlobalCertain(SettingKeys.ContainerInstalledDirectXWrapper12);
        return element == null ? "" : element.getAsString();
    }

    /**
     * 设定容器当前安装的 DX12 包装器版本。
     * 关联 {@link SettingKeys#ContainerInstalledDirectXWrapper12}
     *
     * @param wrapper 当前安装的 DX12 包装器版本
     */
    public void setContainerInstallDirectXWrapper12(@NonNull String wrapper) {
        setForGlobalCertain(SettingKeys.ContainerInstalledDirectXWrapper12,
                new ConfigPrimitive(wrapper));
    }

    /**
     * 获取快照的名称。
     * 关联 {@link SettingKeys#ShortcutInfoName}
     *
     * @return 快照的名称
     */
    @NonNull
    public String getShortcutInfoName() {
        ConfigElement element = getFromLocalCertain(SettingKeys.ShortcutInfoName);
        return element == null ? "" : element.getAsString();
    }

    /**
     * 设定快照的名称。
     * 关联 {@link SettingKeys#ShortcutInfoName}
     *
     * @param name 快照的名称
     */
    public void setShortcutInfoName(@NonNull String name) {
        setForLocalCertain(SettingKeys.ShortcutInfoName, new ConfigPrimitive(name));
    }

    /**
     * 获取快照的执行参数。
     * 关联 {@link SettingKeys#ShortcutInfoExecArgs}
     *
     * @return 快照的执行参数
     */
    @NonNull
    public String getShortcutInfoExecArgs() {
        ConfigElement element = getFromLocalCertain(SettingKeys.ShortcutInfoExecArgs);
        return element == null ? "" : element.getAsString();
    }

    /**
     * 设定快照的执行参数。
     * 关联 {@link SettingKeys#ShortcutInfoExecArgs}
     *
     * @param args 快照的执行参数
     */
    public void setShortcutInfoExecArgs(@NonNull String args) {
        setForLocalCertain(SettingKeys.ShortcutInfoExecArgs, new ConfigPrimitive(args));
    }

    /**
     * 获取快照的创建时间。
     * 关联 {@link SettingKeys#ShortcutInfoCreatedTime}
     *
     * @return 快照的创建时间
     */
    @NonNull
    public String getShortcutInfoCreatedTime() {
        ConfigElement element = getFromLocalCertain(SettingKeys.ShortcutInfoCreatedTime);
        return element == null ? "" : element.getAsString();
    }

    /**
     * 设定快照的创建时间。
     * 关联 {@link SettingKeys#ShortcutInfoCreatedTime}
     *
     * @param time 快照的创建时间
     */
    public void setShortcutInfoCreatedTime(@NonNull String time) {
        setForLocalCertain(SettingKeys.ShortcutInfoCreatedTime, new ConfigPrimitive(time));
    }

    /**
     * 获取快照的唯一标识符。
     * 关联 {@link SettingKeys#ShortcutInfoUUID}
     *
     * @return 快照的唯一标识符
     */
    @NonNull
    public String getShortcutInfoUUID() {
        ConfigElement element = getFromLocalCertain(SettingKeys.ShortcutInfoUUID);
        return element == null ? "" : element.getAsString();
    }

    /**
     * 设定快照的唯一标识符。
     * 关联 {@link SettingKeys#ShortcutInfoUUID}
     *
     * @param uuid 唯一标识符
     */
    public void setShortcutInfoUUID(@NonNull String uuid) {
        setForLocalCertain(SettingKeys.ShortcutInfoUUID, new ConfigPrimitive(uuid));
    }

    /**
     * 获取快照的目标路径。
     * 关联 {@link SettingKeys#ShortcutInfoTarget}
     *
     * @return 快照的目标路径
     */
    @NonNull
    public String getShortcutInfoTarget() {
        ConfigElement element = getFromLocalCertain(SettingKeys.ShortcutInfoTarget);
        return element == null ? "" : element.getAsString();
    }

    /**
     * 设定快照的目标路径。
     * 关联 {@link SettingKeys#ShortcutInfoTarget}
     *
     * @param target 目标路径
     */
    public void setShortcutInfoTarget(@NonNull String target) {
        setForLocalCertain(SettingKeys.ShortcutInfoTarget, new ConfigPrimitive(target));
    }

    /**
     * 获取快照的快捷方式名称。
     * 关联 {@link SettingKeys#ShortcutInfoLnkName}
     *
     * @return 快照的快捷方式名称
     */
    @NonNull
    public String getShortcutInfoLnkName() {
        ConfigElement element = getFromLocalCertain(SettingKeys.ShortcutInfoLnkName);
        return element == null ? "" : element.getAsString();
    }

    /**
     * 设定快照的快捷方式名称。
     * 关联 {@link SettingKeys#ShortcutInfoLnkName}
     *
     * @param lnkName 快捷方式名称
     */
    public void setShortcutInfoLnkName(@NonNull String lnkName) {
        setForLocalCertain(SettingKeys.ShortcutInfoLnkName, new ConfigPrimitive(lnkName));
    }

    /**
     * 从本地配置获取确定存在的值。
     *
     * @param key 键
     * @return 值
     */
    @Nullable
    private ConfigElement getFromLocalCertain(@NonNull Key key) {
        return getCertain(key, Config.SourceType.Local);
    }

    /**
     * 从全局配置获取确定存在的值。
     *
     * @param key 键
     * @return 值
     */
    @Nullable
    private ConfigElement getFromGlobalCertain(@NonNull Key key) {
        return getCertain(key, Config.SourceType.Global);
    }

    /**
     * 从配置获取确定存在的值。
     *
     * @param key        键
     * @param sourceType 来源类型
     * @return 如果存在则返回值，否则返回 null
     */
    @Nullable
    private ConfigElement getCertain(@NonNull Key key, @NonNull Config.SourceType sourceType) {
        String k = key.key();
        Config.Source source = config.get(sourceType);
        if (!source.isLoaded())
            throw new IllegalStateException("Source must be loaded before getting: " + k + " " + sourceType);
        return source.get(k);
    }

    /**
     * 从配置获取动态值。
     * 优先从本地配置获取，如果失败则从全局配置获取，如果仍然失败则从默认配置获取。
     *
     * @param key 键
     * @return 值
     * @throws IllegalStateException 如果配置的三个层级均不存在该键
     */
    @NonNull
    private ConfigElement getFromLocalPriority(@NonNull Key key) {
        ArrayList<Config.Source> sources = new ArrayList<>();
        Config.Source source = config.getLocal();
        if (source.isLoaded())
            sources.add(source);
        source = config.getGlobal();
        if (source.isLoaded())
            sources.add(source);
        source = config.getDefault();
        if (source.isLoaded())
            sources.add(source);
        ConfigElement element = getByPriority(key, sources);
        if (element == null)
            throw new IllegalStateException("Failed to get value from local, global and default: " + key.key());
        return element;
    }

    /**
     * 从配置获取动态值。
     * 优先从全局配置获取，如果失败则从默认配置获取。
     *
     * @param key 键
     * @return 值
     * @throws IllegalStateException 如果配置的两个层级均不存在该键
     */
    @NonNull
    private ConfigElement getFromGlobalPriority(@NonNull Key key) {
        ArrayList<Config.Source> sources = new ArrayList<>();
        Config.Source source = config.getGlobal();
        if (source.isLoaded())
            sources.add(source);
        source = config.getDefault();
        if (source.isLoaded())
            sources.add(source);
        ConfigElement element = getByPriority(key, sources);
        if (element == null)
            throw new IllegalStateException("Failed to get value from global and default: " + key.key());
        return element;
    }

    /**
     * 按顺序尝试从 source 中查找值。
     *
     * @param key     键
     * @param sources 有序的 source 列表
     * @return 如果找到值则则立即返回值对象，否则返回 null
     */
    @Nullable
    private ConfigElement getByPriority(@NonNull Key key, @NonNull List<Config.Source> sources) {
        ConfigElement element;
        String k = key.key();
        for (Config.Source source : sources) {
            element = source.get(k);
            if (element != null)
                return element;
        }
        return null;
    }

    /**
     * 设置本地配置的值。
     *
     * @param key     键
     * @param element 值
     */
    private void setForLocalCertain(@NonNull Key key, @NonNull ConfigElement element) {
        setCertain(key, element, Config.SourceType.Local);
    }

    /**
     * 设置全局配置的值。
     *
     * @param key     键
     * @param element 值
     */
    private void setForGlobalCertain(@NonNull Key key, @NonNull ConfigElement element) {
        setCertain(key, element, Config.SourceType.Global);
    }

    /**
     * 设置值到确定的配置来源类型。
     *
     * @param key        键
     * @param element    值
     * @param sourceType 来源类型
     * @throws IllegalStateException 如果配置未加载。
     */
    private void setCertain(@NonNull Key key, @NonNull ConfigElement element,
                            @NonNull Config.SourceType sourceType) {
        Config.Source source = config.get(sourceType);
        if (!source.isLoaded())
            throw new IllegalStateException("Source must be loaded before setting: " + key + " " + sourceType);
        source.set(key.key(), element);
    }

    /**
     * 设置配置的值。
     * 优先设置本地配置，如果失败则设置全局配置。
     *
     * @param key     键
     * @param element 值
     * @throws IllegalStateException 如果本地配置和全局配置均未加载
     */
    private void setForLocalPriority(@NonNull Key key, @NonNull ConfigElement element) {
        Config.Source local = config.getLocal();
        Config.Source global = config.getGlobal();
        Config.Source source;
        if (local.isLoaded())
            source = local;
        else if (global.isLoaded())
            source = global;
        else
            throw new IllegalStateException("Local or global must be loaded before setting");
        source.set(key.key(), element);
    }
}
