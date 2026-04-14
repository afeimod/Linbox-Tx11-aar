package com.winfusion.feature.setting.model;

import static com.winfusion.feature.setting.model.Constants.INVALID_RES_ID;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.model.common.DetailsFormatter;
import com.winfusion.feature.setting.model.common.ElementCreator;

/**
 * 匿名de子设置项模型类。
 * 仅提供数据关联的相关属性，一般作为 {@link ActionModel#getSubModel()} 使用。
 */
public class SubModel extends SavableModel {

    protected SubModel(@NonNull String labelKey, @NonNull Config config, @NonNull String dataKey,
                       @NonNull Config.SourceType defaultSourceType,
                       @NonNull DetailsFormatter detailsFormatter,
                       @NonNull ElementCreator elementCreator) {

        super(labelKey, INVALID_RES_ID, INVALID_RES_ID, INVALID_RES_ID, INVALID_RES_ID, config,
                dataKey, defaultSourceType, detailsFormatter, elementCreator);
    }

    public static class Builder extends SavableModel.Builder<SubModel, Builder> {

        /**
         * @see SavableModel.Builder#Builder(String, String, Config)
         */
        public Builder(@NonNull String labelKey, @NonNull String dataKey, @NonNull Config config) {
            super(labelKey, dataKey, config);
        }

        /**
         * @see SavableModel.Builder#Builder(String, Config)
         */
        public Builder(@NonNull String key, @NonNull Config config) {
            super(key, config);
        }

        @NonNull
        @Override
        public SubModel create() {
            return new SubModel(labelKey, config, dataKey, defaultSourceType, detailsFormatter,
                    elementCreator);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
