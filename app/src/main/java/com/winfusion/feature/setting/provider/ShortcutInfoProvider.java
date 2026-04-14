package com.winfusion.feature.setting.provider;

import static com.winfusion.feature.setting.model.Constants.FLAG_NON_FOLLOWABLE;
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

public class ShortcutInfoProvider implements SettingsProvider {

    private static final int FLAG_NORMAL = FLAG_SHOW_ICON | FLAG_SHOW_TITLE | FLAG_SHOW_DESCRIPTION |
            FLAG_SHOW_DETAILS;
    private static final int FLAG_TEXT =  FLAG_NORMAL | FLAG_NON_RESETTABLE | FLAG_NON_FOLLOWABLE;

    private final HashMap<String, BaseModel> modelMap = new HashMap<>();
    private final Config config;
    private GroupModel root;

    public ShortcutInfoProvider(@NonNull Config config) {
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
        root = new GroupModel.Builder(SettingKeys.ShortcutInfo.key())
                .setTitle(R.string.info)
                .setChildrenKeys(
                        SettingKeys.ShortcutInfoName.key(),
                        SettingKeys.ShortcutInfoExecArgs.key(),
                        SettingKeys.ContainerInfoRegion.key(),
                        SettingKeys.ShortcutInfoCreatedTime.key(),
                        SettingKeys.ShortcutInfoLnkName.key(),
                        SettingKeys.ShortcutInfoTarget.key(),
                        SettingKeys.ShortcutInfoUUID.key()
                )
                .create();
        add(root);

        // info -> name
        add(
                new TextModel.Builder(SettingKeys.ShortcutInfoName.key(), config)
                        .setTitle(R.string.shortcut_name)
                        .setFlags(FLAG_TEXT)
                        .setEditable(true)
                        .setDefaultSource(Config.SourceType.Local)
                        .create()
        );

        // info -> exec args
        add(
                new TextModel.Builder(SettingKeys.ShortcutInfoExecArgs.key(), config)
                        .setTitle(R.string.exec_args)
                        .setFlags(FLAG_TEXT)
                        .setEditable(true)
                        .setDefaultSource(Config.SourceType.Local)
                        .create()
        );

        // info -> region
        add(
                new SingleChoiceModel.Builder(SettingKeys.ContainerInfoRegion.key(), config)
                        .setTitle(R.string.region)
                        .setFlags(FLAG_NORMAL)
                        .setChoiceNamesId(R.array.infoRegionNamesAndValues)
                        .setChoiceValuesId(R.array.infoRegionNamesAndValues)
                        .setDefaultSource(Config.SourceType.Local)
                        .create()
        );

        // info -> created time
        add(
                new TextModel.Builder(SettingKeys.ShortcutInfoCreatedTime.key(), config)
                        .setTitle(R.string.creation_date)
                        .setFlags(FLAG_TEXT)
                        .setEditable(false)
                        .setDefaultSource(Config.SourceType.Local)
                        .create()
        );

        // info -> lnk name
        add(
                new TextModel.Builder(SettingKeys.ShortcutInfoLnkName.key(), config)
                        .setTitle(R.string.lnk_name)
                        .setFlags(FLAG_TEXT)
                        .setEditable(false)
                        .setDefaultSource(Config.SourceType.Local)
                        .create()
        );

        // info -> target
        add(
                new TextModel.Builder(SettingKeys.ShortcutInfoTarget.key(), config)
                        .setTitle(R.string.target_file_path)
                        .setFlags(FLAG_TEXT)
                        .setEditable(false)
                        .setDefaultSource(Config.SourceType.Local)
                        .create()
        );

        // info -> uuid
        add(
                new TextModel.Builder(SettingKeys.ShortcutInfoUUID.key(), config)
                        .setTitle(R.string.uuid)
                        .setFlags(FLAG_TEXT)
                        .setEditable(false)
                        .setDefaultSource(Config.SourceType.Local)
                        .create()
        );
    }

    private void add(@NonNull BaseModel model) {
        if (modelMap.put(model.getLabelKey(), model) != null)
            throw new IllegalArgumentException("Model has already exists: " + model.getLabelKey());
    }
}
