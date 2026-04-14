package com.winfusion.feature.setting.model;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.model.common.DetailsFormatter;
import com.winfusion.feature.setting.model.common.ElementCreator;
import com.winfusion.feature.setting.model.common.IntegerElementCreator;

/**
 * 滑块设置项模型类。
 */
public class SliderModel extends SavableModel {

    private final int valueFrom;
    private final int valueTo;
    private final int valueStep;

    protected SliderModel(@NonNull String labelKey, int iconId, int titleId, int descriptionId,
                          int flags, @NonNull Config config, @NonNull String dataKey,
                          @NonNull Config.SourceType defaultSourceType,
                          @NonNull DetailsFormatter detailsFormatter,
                          @NonNull ElementCreator elementCreator, int valueFrom, int valueTo,
                          int valueStep) {

        super(labelKey, iconId, titleId, descriptionId, flags, config, dataKey, defaultSourceType,
                detailsFormatter, elementCreator);
        this.valueFrom = valueFrom;
        this.valueTo = valueTo;
        this.valueStep = valueStep;
    }

    /**
     * 获取滑块起始值。
     *
     * @return 起始值
     */
    public int getValueFrom() {
        return valueFrom;
    }

    /**
     * 获取滑块最大值。
     *
     * @return 最大值
     */
    public int getValueTo() {
        return valueTo;
    }

    /**
     * 获取滑块步长。
     *
     * @return 步长
     */
    public int getValueStep() {
        return valueStep;
    }

    public static class Builder extends SavableModel.Builder<SliderModel, Builder> {

        private int valueFrom = 0;
        private int valueTo = 100;
        private int valueStep = 1;

        /**
         * @see SavableModel.Builder#Builder(String, String, Config)
         */
        public Builder(@NonNull String labelKey, @NonNull String dataKey, @NonNull Config config) {
            super(labelKey, dataKey, config);
            elementCreator = IntegerElementCreator.INSTANCE;
        }

        /**
         * @see SavableModel.Builder#Builder(String, Config)
         */
        public Builder(@NonNull String key, @NonNull Config config) {
            super(key, config);
            elementCreator = IntegerElementCreator.INSTANCE;
        }

        /**
         * 设置该项的滑块起始值。
         *
         * @param valueFrom 起始值
         * @return 构造器自身
         */
        @NonNull
        public Builder setValueFrom(int valueFrom) {
            this.valueFrom = valueFrom;
            return self();
        }

        /**
         * 设置该项的滑块最大值。
         *
         * @param valueTo 最大值
         * @return 构造器自身
         */
        @NonNull
        public Builder setValueTo(int valueTo) {
            this.valueTo = valueTo;
            return self();
        }

        /**
         * 设置该项的滑块步长。
         *
         * @param valueStep 步长
         * @return 构造器自身
         */
        @NonNull
        public Builder setValueStep(int valueStep) {
            this.valueStep = valueStep;
            return self();
        }

        @NonNull
        @Override
        public SliderModel create() {
            return new SliderModel(labelKey, iconId, titleId, descriptionId, flags, config, dataKey,
                    defaultSourceType, detailsFormatter, elementCreator, valueFrom, valueTo, valueStep);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
