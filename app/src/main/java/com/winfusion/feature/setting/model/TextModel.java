package com.winfusion.feature.setting.model;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.model.common.DetailsFormatter;
import com.winfusion.feature.setting.model.common.ElementCreator;
import com.winfusion.utils.TextChecker;

/**
 * 文本设置项模型类。
 */
public class TextModel extends BaseTextModel {

    protected TextModel(@NonNull String labelKey, int iconId, int titleId, int descriptionId, int flags,
                        @NonNull Config config, @NonNull String dataKey,
                        @NonNull Config.SourceType defaultSourceType,
                        @NonNull DetailsFormatter detailsFormatter,
                        @NonNull ElementCreator elementCreator, boolean editable,
                        @NonNull TextChecker textChecker) {

        super(labelKey, iconId, titleId, descriptionId, flags, config, dataKey, defaultSourceType,
                detailsFormatter, elementCreator, editable, textChecker);
    }

    public static class Builder extends BaseTextModel.Builder<TextModel, Builder> {

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
        public TextModel create() {
            return new TextModel(labelKey, iconId, titleId, descriptionId, flags, config, dataKey,
                    defaultSourceType, detailsFormatter, elementCreator, editable, textChecker);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
