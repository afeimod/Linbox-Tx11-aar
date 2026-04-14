package com.winfusion.feature.setting.model;

import androidx.annotation.NonNull;

/**
 * 组设置项模型类。
 */
public class GroupModel extends BaseModel {

    protected final String[] childrenKeys;

    protected GroupModel(@NonNull String labelKey, int iconId, int titleId, int descriptionId,
                         int flags, @NonNull String[] childKeys) {

        super(labelKey, iconId, titleId, descriptionId, flags);
        this.childrenKeys = childKeys;
    }

    /**
     * 获取所有子项的标签键。
     *
     * @return 键数组
     */
    @NonNull
    public String[] getChildrenKeys() {
        return childrenKeys;
    }

    public static class Builder extends BaseModel.Builder<GroupModel, Builder> {

        private String[] childrenKeys = new String[0];

        public Builder(@NonNull String labelKey) {
            super(labelKey);
        }

        /**
         * 设置该项的子项标签键。
         *
         * @param childrenKeys 键数组
         * @return 构造器自身
         */
        @NonNull
        public Builder setChildrenKeys(@NonNull String... childrenKeys) {
            this.childrenKeys = childrenKeys;
            return self();
        }

        @NonNull
        @Override
        public GroupModel create() {
            return new GroupModel(labelKey, iconId, titleId, descriptionId, flags, childrenKeys);
        }

        @NonNull
        @Override
        protected Builder self() {
            return this;
        }
    }
}
