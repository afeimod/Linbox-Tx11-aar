package com.winfusion.feature.setting.model;

import static com.winfusion.feature.setting.model.Constants.INVALID_RES_ID;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * 基本设置项模型类。
 */
public abstract class BaseModel {

    protected final String labelKey;
    private final int iconId;
    private final int titleId;
    private final int descriptionId;
    private int flags;
    private boolean hide = false;

    protected BaseModel(@NonNull String labelKey, int iconId, int titleId, int descriptionId,
                        int flags) {

        this.labelKey = labelKey;
        this.iconId = iconId;
        this.titleId = titleId;
        this.descriptionId = descriptionId;
        this.flags = flags;
    }

    /**
     * 设置 flag。
     *
     * @param flags 值
     */
    public void setFlags(int flags) {
        this.flags = flags;
    }

    /**
     * 设置该项是否隐藏。
     *
     * @param hide 是否隐藏
     */
    public void setHide(boolean hide) {
        this.hide = hide;
    }

    /**
     * 获取设置项的标签键。
     * 标签键用于表示唯一的模型对象。
     *
     * @return 标签键
     */
    @NonNull
    public String getLabelKey() {
        return labelKey;
    }

    /**
     * 获取设置项的图标资源 id。
     * 一定是 drawable 资源。
     *
     * @return 资源 id
     */
    @DrawableRes
    public int getIconId() {
        return iconId;
    }

    /**
     * 获取设置项标题的资源 id。
     * 一定是 string 资源。
     *
     * @return 资源 id
     */
    @StringRes
    public int getTitleId() {
        return titleId;
    }

    /**
     * 获取设置项的描述的资源 id。
     * 一定是 string 资源。
     *
     * @return 资源 id
     */
    @StringRes
    public int getDescriptionId() {
        return descriptionId;
    }

    /**
     * 获取 flag。
     *
     * @return 值
     */
    public int getFlags() {
        return flags;
    }

    /**
     * 判断设置项是否隐藏。
     *
     * @return 如果隐藏则返回 true，否则返回 false
     */
    public boolean isHide() {
        return hide;
    }

    /**
     * 判断设置项是否包含该 flag。
     *
     * @param mask 位掩码
     * @return 如果包含则返回 true，否则返回 false
     */
    public boolean hasFlag(int mask) {
        return (flags & mask) != 0;
    }

    /**
     * 基本模型的构造器类。
     *
     * @param <T> 模型类
     * @param <V> 构造器类
     */
    protected abstract static class Builder<T extends BaseModel, V extends Builder<T, V>> {

        protected final String labelKey;
        protected int iconId = INVALID_RES_ID;
        protected int titleId = INVALID_RES_ID;
        protected int descriptionId = INVALID_RES_ID;
        protected int flags = INVALID_RES_ID;

        /**
         * 构造函数
         *
         * @param labelKey 标签键
         */
        public Builder(@NonNull String labelKey) {
            this.labelKey = labelKey;
        }

        /**
         * 设置该项的图标资源 id。
         * 必须是 drawable 资源。
         *
         * @param iconId 资源 id
         * @return 构造器自身
         */
        @NonNull
        public V setIcon(@DrawableRes int iconId) {
            this.iconId = iconId;
            return self();
        }

        /**
         * 设置该项的标题资源 id。
         * 必须是 string 资源。
         *
         * @param titleId 资源 id
         * @return 构造器自身
         */
        @NonNull
        public V setTitle(@StringRes int titleId) {
            this.titleId = titleId;
            return self();
        }

        /**
         * 设置该项的描述资源 id。
         * 必须是 string 资源。
         *
         * @param descriptionId 资源 id
         * @return 构造器自身
         */
        @NonNull
        public V setDescription(@StringRes int descriptionId) {
            this.descriptionId = descriptionId;
            return self();
        }

        /**
         * 设置该项的 flag。
         *
         * @param flags 值
         * @return 构造器自身
         */
        public V setFlags(int flags) {
            this.flags = flags;
            return self();
        }

        /**
         * 创建模型对象。
         *
         * @return 模型对象
         */
        @NonNull
        public abstract T create();

        /**
         * 获取自身的引用。
         * 为自引用泛型提供类型明确的自身对象。
         *
         * @return 自身的对象
         */
        @NonNull
        protected abstract V self();
    }
}
