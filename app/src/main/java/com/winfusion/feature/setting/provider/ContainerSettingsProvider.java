package com.winfusion.feature.setting.provider;

import static com.winfusion.feature.setting.model.Constants.FLAG_SHOW_DESCRIPTION;
import static com.winfusion.feature.setting.model.Constants.FLAG_SHOW_DETAILS;
import static com.winfusion.feature.setting.model.Constants.FLAG_SHOW_ICON;
import static com.winfusion.feature.setting.model.Constants.FLAG_SHOW_TITLE;

import androidx.annotation.NonNull;

import com.winfusion.R;
import com.winfusion.feature.content.ContentManagerActivity;
import com.winfusion.feature.content.ContentManagerActivityArgs;
import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.key.SettingKeys;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.feature.setting.model.ActionModel;
import com.winfusion.feature.setting.model.AutoTextModel;
import com.winfusion.feature.setting.model.BaseModel;
import com.winfusion.feature.setting.model.GroupModel;
import com.winfusion.feature.setting.model.LabelModel;
import com.winfusion.feature.setting.model.SingleChoiceModel;
import com.winfusion.feature.setting.model.SliderModel;
import com.winfusion.feature.setting.model.SubModel;
import com.winfusion.feature.setting.model.action.ActivityAction;
import com.winfusion.feature.setting.model.common.DetailsFormatter;
import com.winfusion.utils.LaunchMode;
import com.winfusion.utils.TextChecker;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 容器设置项提供器，用于提供专用于容器和快照的设置项。
 */
public class ContainerSettingsProvider implements SettingsProvider {

    private static final int FLAG_NORMAL = FLAG_SHOW_ICON | FLAG_SHOW_TITLE | FLAG_SHOW_DESCRIPTION;
    private static final int FLAG_DETAIL = FLAG_NORMAL | FLAG_SHOW_DETAILS;

    private GroupModel root;
    private final Config config;
    private final HashMap<String, BaseModel> modelMap = new HashMap<>();
    private final LaunchMode buildType;
    private final String uuid;
    private final Config.SourceType sourceType;

    /**
     * 构造函数。
     *
     * @param config    配置对象
     * @param buildType 构造目标
     * @throws IllegalArgumentException 如果 buildType 是 {@link LaunchMode#Standalone}
     */
    public ContainerSettingsProvider(@NonNull Config config, @NonNull LaunchMode buildType) {
        this.config = config;
        this.buildType = buildType;
        SettingWrapper wrapper = new SettingWrapper(config);
        switch (buildType) {
            case Container -> {
                uuid = wrapper.getContainerInfoUUID();
                sourceType = Config.SourceType.Global;
            }
            case Shortcut -> {
                uuid = wrapper.getShortcutInfoUUID();
                sourceType = Config.SourceType.Local;
            }
            default -> throw new IllegalArgumentException("Unsupported type: " + buildType);
        };
        build();
    }

    @NonNull
    @Override
    public GroupModel root() {
        return root;
    }

    @NonNull
    @Override
    public Map<String, BaseModel> models() {
        return modelMap;
    }

    private void build() {
        // root
        root = new GroupModel.Builder(SettingKeys.Container.key())
                .setTitle(R.string.container_settings)
                .setChildrenKeys(
                        SettingKeys.ContainerDisplay.key(),
                        SettingKeys.ContainerAudio.key(),
                        SettingKeys.ContainerGraphics.key()
                )
                .create();
        add(root);

        // container -> display
        add(
                new GroupModel.Builder(SettingKeys.ContainerDisplay.key())
                        .setIcon(R.drawable.ic_monitor)
                        .setTitle(R.string.display)
                        .setDescription(R.string.container_display_description)
                        .setFlags(FLAG_NORMAL)
                        .setChildrenKeys(
                                SettingKeys.ContainerDisplayResolution.key(),
                                SettingKeys.ContainerDisplayRefreshRate.key(),
                                SettingKeys.ContainerDisplayScalingMode.key(),
                                SettingKeys.ContainerDisplayOrientation.key(),
                                SettingKeys.ContainerDisplayRendererBackend.key()
                        )
                        .create()
        );

        // container -> display -> resolution
        add(
                new AutoTextModel.Builder(SettingKeys.ContainerDisplayResolution.key(), config)
                        .setTitle(R.string.resolution)
                        .setDescription(R.string.display_resolution_description)
                        .setFlags(FLAG_DETAIL)
                        .setTextChecker(resolutionTextChecker)
                        .setAutoNamesId(R.array.displayResolutionNames)
                        .setAutoValuesId(R.array.displayResolutionValues)
                        .setDefaultSource(sourceType)
                        .create()
        );

        // container -> display -> refresh rate
        add(
                new AutoTextModel.Builder(SettingKeys.ContainerDisplayRefreshRate.key(), config)
                        .setTitle(R.string.refresh_rate)
                        .setDescription(R.string.display_refresh_rate_description)
                        .setFlags(FLAG_DETAIL)
                        .setTextChecker(refreshRateTextChecker)
                        .setAutoNamesId(R.array.displayRefreshRateNames)
                        .setAutoValuesId(R.array.displayRefreshRateValues)
                        .setDefaultSource(sourceType)
                        .create()
        );

        // container -> display -> scaling mode
        add(
                new SingleChoiceModel.Builder(SettingKeys.ContainerDisplayScalingMode.key(), config)
                        .setTitle(R.string.scaling_mode)
                        .setDescription(R.string.display_scaling_mode_description)
                        .setFlags(FLAG_DETAIL)
                        .setChoiceNamesId(R.array.displayScalingModeNames)
                        .setChoiceValuesId(R.array.displayScalingModeValues)
                        .setDefaultSource(sourceType)
                        .create()
        );

        // container -> display -> display orientation
        add(
                new SingleChoiceModel.Builder(SettingKeys.ContainerDisplayOrientation.key(), config)
                        .setTitle(R.string.screen_orientation)
                        .setFlags(FLAG_DETAIL)
                        .setChoiceNamesId(R.array.displayOrientationNames)
                        .setChoiceValuesId(R.array.displayOrientationValues)
                        .setDefaultSource(sourceType)
                        .create()
        );

        // container -> display -> display renderer backend
        add(
                new SingleChoiceModel.Builder(SettingKeys.ContainerDisplayRendererBackend.key(), config)
                        .setTitle(R.string.renderer_backend)
                        .setFlags(FLAG_DETAIL)
                        .setChoiceNamesId(R.array.displayRendererBackendNames)
                        .setChoiceValuesId(R.array.displayRendererBackendValues)
                        .setDefaultSource(sourceType)
                        .create()
        );

        // container -> audio
        add(
                new GroupModel.Builder(SettingKeys.ContainerAudio.key())
                        .setIcon(R.drawable.ic_tune)
                        .setTitle(R.string.audio)
                        .setDescription(R.string.container_audio_description)
                        .setFlags(FLAG_NORMAL)
                        .setChildrenKeys(
                                SettingKeys.ContainerAudioDriver.key(),
                                SettingKeys.ContainerAudioVolumeGain.key(),
                                SettingKeys.LabelMIDI.key(),
                                SettingKeys.LabelMIDISoundfont.key(),
                                SettingKeys.ContainerAudioMIDIVolumeGain.key()
                        )
                        .create()
        );

        // container -> audio -> driver
        add(
                new SingleChoiceModel.Builder(SettingKeys.ContainerAudioDriver.key(), config)
                        .setTitle(R.string.audio_driver)
                        .setFlags(FLAG_DETAIL)
                        .setChoiceNamesId(R.array.audioDriverNames)
                        .setChoiceValuesId(R.array.audioDriverValues)
                        .setDefaultSource(sourceType)
                        .create()
        );

        // container -> audio -> backend
        add(
                new SingleChoiceModel.Builder(SettingKeys.ContainerAudioBackend.key(), config)
                        .setTitle(R.string.audio_backend)
                        .setFlags(FLAG_DETAIL)
                        .setChoiceNamesId(R.array.audioBackendNames)
                        .setChoiceValuesId(R.array.audioBackendValues)
                        .setDefaultSource(sourceType)
                        .create()
        );

        // container -> audio -> volume gain
        add(
                new SliderModel.Builder(SettingKeys.ContainerAudioVolumeGain.key(), config)
                        .setTitle(R.string.audio_volume_gain)
                        .setDescription(R.string.audio_volume_gain_description)
                        .setFlags(FLAG_DETAIL)
                        .setValueFrom(-50)
                        .setValueTo(50)
                        .setValueStep(1)
                        .setDetailsFormatter(percentageFormatter)
                        .setDefaultSource(sourceType)
                        .create()
        );

        // container -> audio -> label midi
        add(
                new LabelModel.Builder(SettingKeys.LabelMIDI.key(), R.string.midi)
                        .create()
        );

        // container -> audio -> choose midi soundfont
        add(
                new ActionModel.Builder(SettingKeys.LabelMIDISoundfont.key())
                        .setTitle(R.string.choose_soundfont)
                        .setDescription(R.string.audio_midi_soundfont_description)
                        .setFlags(FLAG_DETAIL)
                        .setSubModel(
                                new SubModel.Builder(SettingKeys.ContainerAudioMIDISoundFont.key(), config)
                                        .setDefaultSource(sourceType)
                                        .create()
                        )
                        .setActions(
                                new ActivityAction(
                                        new ContentManagerActivityArgs.Builder(
                                                getContentMode().name(),
                                                uuid,
                                                ContentManagerActivity.Filter.Soundfont.name()
                                        ).build().toBundle(),
                                        ContentManagerActivity.class
                                )
                        )
                        .create()
        );

        // container -> audio -> midi volume gain
        add(
                new SliderModel.Builder(SettingKeys.ContainerAudioMIDIVolumeGain.key(), config)
                        .setTitle(R.string.midi_volume_gain)
                        .setDescription(R.string.audio_midi_volume_gain_description)
                        .setFlags(FLAG_DETAIL)
                        .setValueFrom(-50)
                        .setValueTo(50)
                        .setValueStep(1)
                        .setDetailsFormatter(percentageFormatter)
                        .setDefaultSource(sourceType)
                        .create()
        );

        // container -> graphics
        add(
                new GroupModel.Builder(SettingKeys.ContainerGraphics.key())
                        .setIcon(R.drawable.ic_developer_board)
                        .setTitle(R.string.graphics)
                        .setDescription(R.string.container_graphics_description)
                        .setFlags(FLAG_NORMAL)
                        .setChildrenKeys(
                                SettingKeys.LabelOpenGL.key(),
                                SettingKeys.LabelOpenGLChoose.key(),
                                SettingKeys.LabelOpenGLConfig.key(),
                                SettingKeys.LabelVulkan.key(),
                                SettingKeys.LabelVulkanChoose.key(),
                                SettingKeys.LabelVulkanConfig.key(),
                                SettingKeys.LabelDirectXWrapper.key(),
                                SettingKeys.LabelDirectXWrapper8_11Choose.key(),
                                SettingKeys.LabelDirectXWrapper12Choose.key(),
                                SettingKeys.LabelDirectXWrapperConfig.key()
                        )
                        .create()
        );

        // container -> graphics -> label opengl
        add(
                new LabelModel.Builder(SettingKeys.LabelOpenGL.key(), R.string.opengl)
                        .create()
        );

        // container -> graphics -> choose opengl
        add(
                new ActionModel.Builder(SettingKeys.LabelOpenGLChoose.key())
                        .setTitle(R.string.choose_opengl_driver)
                        .setDescription(R.string.graphics_opengl_driver_description)
                        .setFlags(FLAG_DETAIL)
                        .setSubModel(
                                new SubModel.Builder(SettingKeys.ContainerGraphicsDriverOpenGL.key(), config)
                                        .setDefaultSource(sourceType)
                                        .create()
                        )
                        .setActions(
                                new ActivityAction(
                                        new ContentManagerActivityArgs.Builder(
                                                getContentMode().name(),
                                                uuid,
                                                ContentManagerActivity.Filter.OpenGL_Driver.name()
                                        ).build().toBundle(),
                                        ContentManagerActivity.class
                                )
                        )
                        .create()
        );

        // container -> graphics -> config opengl
        add(
                new ActionModel.Builder(SettingKeys.LabelOpenGLConfig.key())
                        .setTitle(R.string.config_opengl_driver)
                        .setFlags(FLAG_NORMAL)
                        .setActions(
                                // TODO: 实现 activity
                                new ActivityAction(
                                        null,
                                        null
                                )
                        )
                        .create()
        );

        // container -> graphics -> label vulkan
        add(
                new LabelModel.Builder(SettingKeys.LabelVulkan.key(), R.string.vulkan)
                        .create()
        );

        // container -> graphics -> choose vulkan
        add(
                new ActionModel.Builder(SettingKeys.LabelVulkanChoose.key())
                        .setTitle(R.string.choose_vulkan_driver)
                        .setDescription(R.string.graphics_vulkan_driver_description)
                        .setFlags(FLAG_DETAIL)
                        .setSubModel(
                                new SubModel.Builder(SettingKeys.ContainerGraphicsDriverVulkan.key(), config)
                                        .setDefaultSource(sourceType)
                                        .create()
                        )
                        .setActions(
                                new ActivityAction(
                                        new ContentManagerActivityArgs.Builder(
                                                getContentMode().name(),
                                                uuid,
                                                ContentManagerActivity.Filter.Vulkan_Driver.name()
                                        ).build().toBundle(),
                                        ContentManagerActivity.class
                                )
                        )
                        .create()
        );

        // container -> graphics -> config vulkan
        add(
                new ActionModel.Builder(SettingKeys.LabelVulkanConfig.key())
                        .setTitle(R.string.config_vulkan_driver)
                        .setFlags(FLAG_NORMAL)
                        .setActions(
                                // TODO: 实现 activity
                                new ActivityAction(
                                        null,
                                        null
                                )
                        )
                        .create()
        );

        // container -> graphics -> label directx wrapper
        add(
                new LabelModel.Builder(SettingKeys.LabelDirectXWrapper.key(), R.string.directx_wrapper)
                        .create()
        );

        // container -> graphics -> choose directx8_11 wrapper
        add(
                new ActionModel.Builder(SettingKeys.LabelDirectXWrapper8_11Choose.key())
                        .setTitle(R.string.choose_directx_8_11_wrapper)
                        .setDescription(R.string.graphics_directx8_11_description)
                        .setFlags(FLAG_DETAIL)
                        .setSubModel(
                                new SubModel.Builder(SettingKeys.ContainerGraphicsDirectXWrapper8_11.key(), config)
                                        .setDefaultSource(sourceType)
                                        .create()
                        )
                        .setActions(
                                new ActivityAction(
                                        new ContentManagerActivityArgs.Builder(
                                                getContentMode().name(),
                                                uuid,
                                                ContentManagerActivity.Filter.DirectX_8_to_11.name()
                                        ).build().toBundle(),
                                        ContentManagerActivity.class
                                )
                        )
                        .create()
        );

        // container -> graphics -> choose directx12 wrapper
        add(
                new ActionModel.Builder(SettingKeys.LabelDirectXWrapper12Choose.key())
                        .setTitle(R.string.choose_directx_12_wrapper)
                        .setDescription(R.string.graphics_directx12_description)
                        .setFlags(FLAG_DETAIL)
                        .setSubModel(
                                new SubModel.Builder(SettingKeys.ContainerGraphicsDirectXWrapper12.key(), config)
                                        .setDefaultSource(sourceType)
                                        .create()
                        )
                        .setActions(
                                new ActivityAction(
                                        new ContentManagerActivityArgs.Builder(
                                                getContentMode().name(),
                                                uuid,
                                                ContentManagerActivity.Filter.DirectX_12.name()
                                        ).build().toBundle(),
                                        ContentManagerActivity.class
                                )
                        )
                        .create()
        );

        // container -> graphics -> config directx wrapper
        add(
                new ActionModel.Builder(SettingKeys.LabelDirectXWrapperConfig.key())
                        .setTitle(R.string.config_directx_wrapper)
                        .setFlags(FLAG_NORMAL)
                        .setActions(
                                // TODO: 实现 activity
                                new ActivityAction(
                                        null,
                                        null
                                )
                        )
                        .create()
        );
    }

    private void add(@NonNull BaseModel model) {
        if (modelMap.put(model.getLabelKey(), model) != null)
            throw new IllegalArgumentException("Model has already exists: " + model.getLabelKey());
    }

    private final TextChecker resolutionTextChecker = new TextChecker() {

        @Override
        public boolean check(@NonNull String textStr) {
            String regex = "^\\d+x\\d+$";
            return Pattern.matches(regex, textStr);
        }

        @Override
        public int getTipsId() {
            // TODO: 改为正确提示
            return R.string.resolution;
        }
    };

    private final TextChecker refreshRateTextChecker = new TextChecker() {

        @Override
        public boolean check(@NonNull String textStr) {
            int rate;

            try {
                rate = Integer.parseInt(textStr);
            } catch (NumberFormatException e) {
                return false;
            }

            return rate >= 30 && rate <= 1000;
        }

        @Override
        public int getTipsId() {
            return R.string.refresh_rate_warning_description;
        }
    };

    private final DetailsFormatter percentageFormatter = element -> element.getAsInt() + "%";

    @NonNull
    private LaunchMode getContentMode() {
        return switch (buildType) {
            case Container -> LaunchMode.Container;
            case Shortcut -> LaunchMode.Shortcut;
            case Standalone -> throw new IllegalArgumentException("Not support standalone.");
        };
    }
}
