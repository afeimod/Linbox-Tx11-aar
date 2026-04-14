package com.winfusion.feature.setting.model;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.model.common.BooleanElementCreator;
import com.winfusion.feature.setting.model.common.DetailsFormatter;
import com.winfusion.feature.setting.model.common.ElementCreator;

/**
 * 基本开关设置项模型类。
 */
public abstract class BaseSwitchModel extends SavableModel {
    protected BaseSwitchModel(@NonNull String labelKey, int iconId, int titleId, int descriptionId,
                              int flags, @NonNull Config config, @NonNull String dataKey,
                              @NonNull Config.SourceType defaultSourceType,
                              @NonNull DetailsFormatter detailsFormatter,
                              @NonNull ElementCreator elementCreator) {

        super(labelKey, iconId, titleId, descriptionId, flags, config, dataKey, defaultSourceType,
                detailsFormatter, elementCreator);
    }

    public abstract static class Builder<T extends BaseSwitchModel, V extends Builder<T, V>>
            extends SavableModel.Builder<T, V> {

        /**
         * @see SavableModel.Builder#Builder(String, String, Config)
         */
        public Builder(@NonNull String labelKey, @NonNull String dataKey, @NonNull Config config) {
            super(labelKey, dataKey, config);
            elementCreator = BooleanElementCreator.INSTANCE;
        }

        /**
         * @see SavableModel.Builder#Builder(String, Config)
         */
        public Builder(@NonNull String key, @NonNull Config config) {
            super(key, config);
            elementCreator = BooleanElementCreator.INSTANCE;
        }
    }
}
