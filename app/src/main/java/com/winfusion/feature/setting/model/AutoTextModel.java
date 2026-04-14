package com.winfusion.feature.setting.model;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;

import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.model.common.DetailsFormatter;
import com.winfusion.feature.setting.model.common.ElementCreator;
import com.winfusion.utils.TextChecker;

/**
 * 自动文本设置项模型类。
 */
public class AutoTextModel extends BaseTextModel {

    protected final int autoNamesId;
    protected final int autoValuesId;

    protected AutoTextModel(@NonNull String labelKey, int iconId, int titleId, int descriptionId,
                            int flags, @NonNull Config config, @NonNull String dataKey,
                            @NonNull Config.SourceType defaultSourceType,
                            @NonNull DetailsFormatter detailsFormatter,
                            @NonNull ElementCreator elementCreator, boolean editable,
                            @NonNull TextChecker textChecker, @ArrayRes int autoNamesId,
                            @ArrayRes int autoValuesId) {

        super(labelKey, iconId, titleId, descriptionId, flags, config, dataKey, defaultSourceType,
                detailsFormatter, elementCreator, editable, textChecker);
        this.autoNamesId = autoNamesId;
        this.autoValuesId = autoValuesId;
    }

    /**
     * 获取自动文本的名称资源 id。
     * 一定是 array 资源。
     *
     * @return 资源 id
     */
    @ArrayRes
    public int getAutoNamesId() {
        return autoNamesId;
    }

    /**
     * 获取自动文本的值资源 id。
     * 一定是 array 资源。
     *
     * @return 资源 id
     */
    @ArrayRes
    public int getAutoValuesId() {
        return autoValuesId;
    }

    public static class Builder extends BaseTextModel.Builder<AutoTextModel, Builder> {

        private int autoNamesId;
        private int autoValuesId;

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

        /**
         * 设置该项的自动文本名称的资源 id。
         * 必须是 array 资源。
         *
         * @param autoNamesId 资源 id
         * @return 构造器自身
         */
        @NonNull
        public Builder setAutoNamesId(@ArrayRes int autoNamesId) {
            this.autoNamesId = autoNamesId;
            return self();
        }

        /**
         * 设置该项的自动文本值的资源 id。
         * 必须是 array 资源。
         *
         * @param autoValuesId 资源 id
         * @return 构造器自身
         */
        @NonNull
        public Builder setAutoValuesId(@ArrayRes int autoValuesId) {
            this.autoValuesId = autoValuesId;
            return self();
        }

        @NonNull
        @Override
        public AutoTextModel create() {
            return new AutoTextModel(labelKey, iconId, titleId, descriptionId, flags, config, dataKey,
                    defaultSourceType, detailsFormatter, elementCreator, editable, textChecker,
                    autoNamesId, autoValuesId);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
