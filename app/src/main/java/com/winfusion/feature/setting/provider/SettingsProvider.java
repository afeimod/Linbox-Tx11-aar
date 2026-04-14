package com.winfusion.feature.setting.provider;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.model.BaseModel;
import com.winfusion.feature.setting.model.GroupModel;

import java.util.Map;

public interface SettingsProvider {

    /**
     * 获取设置项的根模型。
     * 必须是 {@link GroupModel} 或它的子类。
     *
     * @return 根模型
     */
    @NonNull
    GroupModel root();

    /**
     * 获取全部设置项模型的映射集合。
     *
     * @return 全部设置项模型
     */
    @NonNull
    Map<String, BaseModel> models();
}
