package com.winfusion.feature.setting.provider;

import static com.winfusion.feature.setting.model.Constants.FLAG_NON_RESETTABLE;
import static com.winfusion.feature.setting.model.Constants.FLAG_SHOW_DESCRIPTION;
import static com.winfusion.feature.setting.model.Constants.FLAG_SHOW_DETAILS;
import static com.winfusion.feature.setting.model.Constants.FLAG_SHOW_ICON;
import static com.winfusion.feature.setting.model.Constants.FLAG_SHOW_TITLE;

import androidx.annotation.NonNull;

import com.winfusion.R;
import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.key.SettingKeys;
import com.winfusion.feature.setting.model.BaseModel;
import com.winfusion.feature.setting.model.GroupModel;
import com.winfusion.feature.setting.model.SingleChoiceModel;
import com.winfusion.feature.setting.model.TextModel;

import java.util.HashMap;
import java.util.Map;

/**
 * 容器信息项提供器。
 */
public class ContainerInfoProvider implements SettingsProvider {

    private static final int FLAG_NORMAL = FLAG_SHOW_ICON | FLAG_SHOW_TITLE | FLAG_SHOW_DESCRIPTION |
            FLAG_SHOW_DETAILS;
    private static final int FLAG_TEXT =  FLAG_NORMAL | FLAG_NON_RESETTABLE;

    private final HashMap<String, BaseModel> modelMap = new HashMap<>();
    private final Config config;
    private GroupModel root;

    public ContainerInfoProvider(@NonNull Config config) {
        this.config = config;
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
        root = new GroupModel.Builder(SettingKeys.ContainerInfo.key())
                .setTitle(R.string.info)
                .setChildrenKeys(
                        SettingKeys.ContainerInfoName.key(),
                        SettingKeys.ContainerInfoRegion.key(),
                        SettingKeys.ContainerInfoCreatedTime.key(),
                        SettingKeys.ContainerInfoWineVersionAtCreation.key(),
                        SettingKeys.ContainerInfoUUID.key()
                )
                .create();
        add(root);

        // info -> name
        add(
                new TextModel.Builder(SettingKeys.ContainerInfoName.key(), config)
                        .setTitle(R.string.container_name)
                        .setDescription(R.string.container_name_description)
                        .setFlags(FLAG_TEXT)
                        .setEditable(true)
                        .create()
        );

        // info -> region
        add(
                new SingleChoiceModel.Builder(SettingKeys.ContainerInfoRegion.key(), config)
                        .setTitle(R.string.region)
                        .setFlags(FLAG_NORMAL)
                        .setChoiceNamesId(R.array.infoRegionNamesAndValues)
                        .setChoiceValuesId(R.array.infoRegionNamesAndValues)
                        .create()
        );

        // info -> created time
        add(
                new TextModel.Builder(SettingKeys.ContainerInfoCreatedTime.key(), config)
                        .setTitle(R.string.creation_date)
                        .setDescription(R.string.container_creation_time_description)
                        .setFlags(FLAG_TEXT)
                        .setEditable(false)
                        .create()
        );

        // info -> wine version at creation
        add(
                new TextModel.Builder(SettingKeys.ContainerInfoWineVersionAtCreation.key(), config)
                        .setTitle(R.string.wine_version_at_creation)
                        .setDescription(R.string.container_wine_version_at_creation_description)
                        .setFlags(FLAG_TEXT)
                        .setEditable(false)
                        .create()
        );

        // info -> uuid
        add(
                new TextModel.Builder(SettingKeys.ContainerInfoUUID.key(), config)
                        .setTitle(R.string.uuid)
                        .setDescription(R.string.container_uuid_description)
                        .setFlags(FLAG_TEXT)
                        .setEditable(false)
                        .create()
        );
    }

    private void add(@NonNull BaseModel model) {
        if (modelMap.put(model.getLabelKey(), model) != null)
            throw new IllegalArgumentException("Model has already exists: " + model.getLabelKey());
    }
}
