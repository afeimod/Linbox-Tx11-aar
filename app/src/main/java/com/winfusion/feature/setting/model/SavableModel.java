package com.winfusion.feature.setting.model;

import static com.winfusion.feature.setting.model.Constants.FLAG_NON_FOLLOWABLE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.model.common.DetailsFormatter;
import com.winfusion.feature.setting.model.common.ElementCreator;
import com.winfusion.feature.setting.model.common.Resettable;
import com.winfusion.feature.setting.model.common.StringElementCreator;
import com.winfusion.feature.setting.value.ConfigElement;

/**
 * 可持久化的设置项模型类。
 * 持久化方面配合 {@link Config} 实现。
 */
public abstract class SavableModel extends BaseModel implements Resettable {

    protected final Config config;
    protected final String dataKey;
    protected final Config.SourceType defaultSourceType;
    protected final DetailsFormatter detailsFormatter;
    protected final ElementCreator elementCreator;
    protected Config.SourceType currentSourceType;

    protected SavableModel(@NonNull String labelKey, int iconId, int titleId, int descriptionId,
                           int flags, @NonNull Config config, @NonNull String dataKey,
                           @NonNull Config.SourceType defaultSourceType,
                           @NonNull DetailsFormatter detailsFormatter,
                           @NonNull ElementCreator elementCreator) {

        super(labelKey, iconId, titleId, descriptionId, flags);
        this.config = config;
        this.dataKey = dataKey;
        this.defaultSourceType = defaultSourceType;
        this.detailsFormatter = detailsFormatter;
        this.elementCreator = elementCreator;
    }

    /**
     * 获取设置项使用的配置对象。
     *
     * @return 配置对象
     */
    @NonNull
    public Config getConfig() {
        return config;
    }

    /**
     * 获取设置项的数据键。
     * 数据键用于表示该设置项关联的数据的键。
     *
     * @return 数据键
     */
    @NonNull
    public String getDataKey() {
        return dataKey;
    }

    /**
     * 获取设置项的默认数据来源。
     *
     * @return 默认数据来源
     */
    @NonNull
    public Config.SourceType getDefaultSourceType() {
        return defaultSourceType;
    }

    @Nullable
    public Config.SourceType getCurrentSourceType() {
        return currentSourceType;
    }

    /**
     * 获取设置项的数据值。
     *
     * @return 设置项关联的数据的值
     * @throws IllegalStateException 如果键不在配置对象中
     */
    @NonNull
    public ConfigElement getValue() {
        // try local first
        ConfigElement value = config.get(defaultSourceType).get(dataKey);
        currentSourceType = defaultSourceType;
        if (value == null) {
            if (defaultSourceType == Config.SourceType.Local) {
                // fallback to global
                value = config.getGlobal().get(dataKey);
                currentSourceType = Config.SourceType.Global;
                if (value != null)
                    return value;
            }

            // fallback to default
            value = config.getDefault().get(dataKey);
            currentSourceType = Config.SourceType.Default;
            if (value != null)
                return value;

            throw new IllegalStateException(
                    "fallback to default and still failed, value not found: " + dataKey);
        }
        return value;
    }

    /**
     * 设置该项的数据值。
     *
     * @param value 值
     */
    public void setValue(@NonNull ConfigElement value) {
        config.get(defaultSourceType).set(dataKey, value);
    }

    /**
     * 将设置项关联的数据值恢复到默认值。
     *
     * @throws IllegalStateException 如果没有默认值
     */
    @Override
    public void resetToDefault() {
        ConfigElement value = config.getDefault().get(dataKey);
        if (value == null)
            throw new IllegalStateException("no default value found: " + dataKey);

        config.get(defaultSourceType).set(dataKey, value);
    }

    /**
     * 判断当前设置项是否能够被设置跟随。
     *
     * @return 如果可以被设置则返回 true，否则返回 false
     */
    public boolean canFollowed() {
        return defaultSourceType == Config.SourceType.Local &&
                currentSourceType == defaultSourceType && !hasFlag(FLAG_NON_FOLLOWABLE);
    }

    /**
     * 获取设置项的数据显示格式化对象。
     *
     * @return 格式化对象
     */
    @NonNull
    public DetailsFormatter getDetailsFormatter() {
        return detailsFormatter;
    }

    /**
     * 获取设置项的值构造器
     *
     * @return 值构造器
     */
    @NonNull
    public ElementCreator getElementCreator() {
        return elementCreator;
    }

    protected abstract static class Builder<T extends SavableModel, V extends Builder<T, V>>
            extends BaseModel.Builder<T, V> {

        private static final DetailsFormatter DefaultDetailsFormatter = Object::toString;

        protected final Config config;
        protected final String dataKey;
        protected Config.SourceType defaultSourceType = Config.SourceType.Global;
        protected DetailsFormatter detailsFormatter = DefaultDetailsFormatter;
        protected ElementCreator elementCreator = StringElementCreator.INSTANCE;

        /**
         * 构造函数。
         *
         * @param labelKey 标签键
         * @param dataKey  数据键
         * @param config   配置对象
         */
        public Builder(@NonNull String labelKey, @NonNull String dataKey, @NonNull Config config) {
            super(labelKey);
            this.config = config;
            this.dataKey = dataKey;
        }

        /**
         * 构造函数。
         * 接收的键既作为标签键也作为数据键。
         *
         * @param key    键
         * @param config 配置对象
         */
        public Builder(@NonNull String key, @NonNull Config config) {
            this(key, key, config);
        }

        /**
         * 设置该项默认的数据来源类型。
         *
         * @param defaultSourceType 数据来源类型
         * @return 构造器本身
         */
        @NonNull
        public V setDefaultSource(@NonNull Config.SourceType defaultSourceType) {
            this.defaultSourceType = defaultSourceType;
            return self();
        }

        /**
         * 设置该项的数据显示格式化器，
         *
         * @param detailsFormatter 格式化器
         * @return 构造器本身
         */
        @NonNull
        public V setDetailsFormatter(@NonNull DetailsFormatter detailsFormatter) {
            this.detailsFormatter = detailsFormatter;
            return self();
        }

        /**
         * 设置该项的值构造器。
         *
         * @param elementCreator 值构造器
         * @return 构造器本身
         */
        @NonNull
        public V setElementCreator(@NonNull ElementCreator elementCreator) {
            this.elementCreator = elementCreator;
            return self();
        }
    }
}
