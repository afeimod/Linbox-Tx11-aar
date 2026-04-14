package com.winfusion.feature.setting.model;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;

import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.model.common.DetailsFormatter;
import com.winfusion.feature.setting.model.common.ElementCreator;

/**
 * 单选设置项模型类。
 */
public class SingleChoiceModel extends SavableModel {

    private final int choiceNamesId;
    private final int choiceValuesId;

    protected SingleChoiceModel(@NonNull String labelKey, int iconId, int titleId, int descriptionId,
                                int flags, @NonNull Config config, @NonNull String dataKey,
                                @NonNull Config.SourceType defaultSourceType,
                                @NonNull DetailsFormatter detailsFormatter,
                                @NonNull ElementCreator elementCreator,
                                @ArrayRes int choiceNamesId, @ArrayRes int choiceValuesId) {

        super(labelKey, iconId, titleId, descriptionId, flags, config, dataKey, defaultSourceType,
                detailsFormatter, elementCreator);
        this.choiceNamesId = choiceNamesId;
        this.choiceValuesId = choiceValuesId;
    }

    /**
     * 获取选项名称的资源 id。
     * 一定是 array 资源。
     *
     * @return 资源 id
     */
    @ArrayRes
    public int getChoiceNamesId() {
        return choiceNamesId;
    }

    /**
     * 获取选项值的资源 id。
     * 一定是 array 资源。
     *
     * @return 资源 id
     */
    @ArrayRes
    public int getChoiceValuesId() {
        return choiceValuesId;
    }

    public static class Builder extends SavableModel.Builder<SingleChoiceModel, Builder> {

        private int choiceNamesId;
        private int choiceValuesId;

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
         * 设置该项的选项名称资源 id。
         * 必须是 array 资源。
         *
         * @param choiceNamesId 资源 id
         * @return 构造器自身
         */
        @NonNull
        public Builder setChoiceNamesId(@ArrayRes int choiceNamesId) {
            this.choiceNamesId = choiceNamesId;
            return self();
        }

        /**
         * 设置该项的选项值资源 id。
         * 必须是 array 资源。
         *
         * @param choiceValuesId 资源 id
         * @return 构造器自身
         */
        @NonNull
        public Builder setChoiceValuesId(@ArrayRes int choiceValuesId) {
            this.choiceValuesId = choiceValuesId;
            return self();
        }

        @NonNull
        @Override
        public SingleChoiceModel create() {
            return new SingleChoiceModel(labelKey, iconId, titleId, descriptionId, flags, config,
                    dataKey, defaultSourceType, detailsFormatter, elementCreator, choiceNamesId,
                    choiceValuesId);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
