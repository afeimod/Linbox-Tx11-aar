package com.winfusion.feature.setting.model;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.model.common.BooleanElementCreator;
import com.winfusion.feature.setting.model.common.DetailsFormatter;
import com.winfusion.feature.setting.model.common.ElementCreator;

/**
 * 开关设置项模型类。
 */
public class SwitchModel extends BaseSwitchModel {
    protected SwitchModel(@NonNull String labelKey, int iconId, int titleId, int descriptionId,
                          int flags, @NonNull Config config, @NonNull String dataKey,
                          @NonNull Config.SourceType defaultSourceType,
                          @NonNull DetailsFormatter detailsFormatter,
                          @NonNull ElementCreator elementCreator) {

        super(labelKey, iconId, titleId, descriptionId, flags, config, dataKey, defaultSourceType,
                detailsFormatter, elementCreator);
    }

    public static class Builder extends BaseSwitchModel.Builder<SwitchModel, Builder> {

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

        @NonNull
        @Override
        public SwitchModel create() {
            return new SwitchModel(labelKey, iconId, titleId, descriptionId, flags, config, dataKey,
                    defaultSourceType, detailsFormatter, elementCreator);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
