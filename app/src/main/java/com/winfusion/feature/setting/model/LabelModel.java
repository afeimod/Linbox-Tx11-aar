package com.winfusion.feature.setting.model;

import static com.winfusion.feature.setting.model.Constants.INVALID_RES_ID;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * 标签设置项模型类。
 */
public class LabelModel extends BaseModel {

    protected final int labelId;

    protected LabelModel(@NonNull String labelKey, int flags, int labelId) {

        super(labelKey, INVALID_RES_ID, INVALID_RES_ID, INVALID_RES_ID, flags);
        this.labelId = labelId;
    }

    /**
     * 获取标签文本的资源 id。
     * 一定是 string 资源。
     *
     * @return 资源 id
     */
    @StringRes
    public int getLabelId() {
        return labelId;
    }

    public static class Builder extends BaseModel.Builder<LabelModel, Builder> {

        private final int labelId;

        /**
         * 构造函数。
         *
         * @param labelKey 标签键
         * @param labelId  标签文本的资源 id
         */
        public Builder(@NonNull String labelKey, @StringRes int labelId) {
            super(labelKey);
            this.labelId = labelId;
        }

        @NonNull
        @Override
        public LabelModel create() {
            return new LabelModel(labelKey, flags, labelId);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
