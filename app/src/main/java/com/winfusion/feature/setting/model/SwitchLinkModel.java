package com.winfusion.feature.setting.model;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.model.common.BooleanElementCreator;
import com.winfusion.feature.setting.model.common.DetailsFormatter;
import com.winfusion.feature.setting.model.common.ElementCreator;

/**
 * 条件开关设置项模型类。
 */
public class SwitchLinkModel extends BaseSwitchModel {

    protected final String[] hideWhenOnKeys;
    protected final String[] hideWhenOffKeys;

    protected SwitchLinkModel(@NonNull String labelKey, int iconId, int titleId, int descriptionId,
                              int flags, @NonNull Config config, @NonNull String dataKey,
                              @NonNull Config.SourceType defaultSourceType,
                              @NonNull DetailsFormatter detailsFormatter,
                              @NonNull ElementCreator elementCreator,
                              @NonNull String[] hideWhenOnKeys, @NonNull String[] hideWhenOffKeys) {

        super(labelKey, iconId, titleId, descriptionId, flags, config, dataKey, defaultSourceType,
                detailsFormatter, elementCreator);
        this.hideWhenOnKeys = hideWhenOnKeys;
        this.hideWhenOffKeys = hideWhenOffKeys;
    }

    /**
     * 获取设置项启用时要隐藏的键。
     *
     * @return 键数组
     */
    @NonNull
    public String[] getHideWhenOnKeys() {
        return hideWhenOnKeys;
    }

    /**
     * 获取设置项禁用时要隐藏的键。
     *
     * @return 键数组
     */
    @NonNull
    public String[] getHideWhenOffKeys() {
        return hideWhenOffKeys;
    }

    public static class Builder extends BaseSwitchModel.Builder<SwitchLinkModel, Builder> {

        private String[] hideWhenOnKeys = new String[0];
        private String[] hideWhenOffKeys = new String[0];

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

        /**
         * 设置该项启用时要隐藏的项的键。
         *
         * @param hideWhenOnKeys 键数组
         * @return 构造器自身
         */
        @NonNull
        public Builder setHideWhenOnKeys(@NonNull String[] hideWhenOnKeys) {
            this.hideWhenOnKeys = hideWhenOnKeys;
            return self();
        }

        /**
         * 设置该项禁用时要隐藏的项的键。
         *
         * @param hideWhenOffKeys 键数组
         * @return 构造器自身
         */
        @NonNull
        public Builder setHideWhenOffKeys(@NonNull String[] hideWhenOffKeys) {
            this.hideWhenOffKeys = hideWhenOffKeys;
            return self();
        }

        @NonNull
        @Override
        public SwitchLinkModel create() {
            return new SwitchLinkModel(labelKey, iconId, titleId, descriptionId, flags, config,
                    dataKey, defaultSourceType, detailsFormatter, elementCreator, hideWhenOnKeys,
                    hideWhenOffKeys);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
