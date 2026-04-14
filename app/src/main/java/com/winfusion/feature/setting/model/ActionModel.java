package com.winfusion.feature.setting.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.feature.setting.model.action.BaseAction;
import com.winfusion.feature.setting.model.common.DetailsFormatter;

import java.util.Arrays;

/**
 * 包含 {@link BaseAction} 的设置项模型类。
 */
public class ActionModel extends BaseModel {

    private final BaseAction[] actions;
    private final SavableModel subModel;
    private final DetailsFormatter detailsFormatter;

    protected ActionModel(@NonNull String labelKey, int iconId, int titleId, int descriptionId,
                          int flags, @Nullable SavableModel subModel, @NonNull BaseAction[] actions,
                          @NonNull DetailsFormatter detailsFormatter) {

        super(labelKey, iconId, titleId, descriptionId, flags);
        this.actions = actions;
        this.subModel = subModel;
        this.detailsFormatter = detailsFormatter;
    }

    /**
     * 获取设置项的 action 对象。
     *
     * @return action 对象数组
     */
    @NonNull
    public BaseAction[] getActions() {
        return Arrays.copyOf(actions, actions.length);
    }

    /**
     * 获取设置项的子项模型对象。
     *
     * @return 子项模型对象
     */
    @Nullable
    public SavableModel getSubModel() {
        return subModel;
    }

    /**
     * 获取设置项的格式化器对象。
     *
     * @return 格式化器
     */
    @NonNull
    public DetailsFormatter getDetailsFormatter() {
        return detailsFormatter;
    }

    public static class Builder extends BaseModel.Builder<ActionModel, Builder> {

        private BaseAction[] actions = new BaseAction[0];
        private SavableModel subModel;
        private DetailsFormatter detailsFormatter = Object::toString;

        public Builder(@NonNull String labelKey) {
            super(labelKey);
        }

        /**
         * 设置该项的 action 对象。
         *
         * @param actions action 对象数组
         * @return 构造器自身
         */
        @NonNull
        public Builder setActions(@NonNull BaseAction... actions) {
            this.actions = actions;
            return self();
        }

        /**
         * 设置该项的子项模型对象。
         *
         * @param subModel 子项模型
         * @return 构造器自身
         */
        @NonNull
        public Builder setSubModel(@NonNull SavableModel subModel) {
            this.subModel = subModel;
            return self();
        }

        /**
         * 设置该项的数据格式化器。
         *
         * @param detailsFormatter 格式化器
         * @return 构造器自身
         */
        @NonNull
        public Builder setDetailsFormatter(@NonNull DetailsFormatter detailsFormatter) {
            this.detailsFormatter = detailsFormatter;
            return self();
        }

        @NonNull
        @Override
        public ActionModel create() {
            return new ActionModel(labelKey, iconId, titleId, descriptionId, flags, subModel, actions,
                    detailsFormatter);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
