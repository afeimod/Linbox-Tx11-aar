package com.winfusion.feature.setting.model;

import androidx.annotation.NonNull;

import com.winfusion.feature.setting.Config;
import com.winfusion.feature.setting.model.common.DetailsFormatter;
import com.winfusion.feature.setting.model.common.ElementCreator;
import com.winfusion.utils.TextChecker;

/**
 * 基本文本设置项模型类。
 */
public abstract class BaseTextModel extends SavableModel {

    protected final boolean editable;
    protected final TextChecker textChecker;

    protected BaseTextModel(@NonNull String labelKey, int iconId, int titleId, int descriptionId, int flags,
                            @NonNull Config config, @NonNull String dataKey,
                            @NonNull Config.SourceType defaultSourceType,
                            @NonNull DetailsFormatter detailsFormatter,
                            @NonNull ElementCreator elementCreator, boolean editable,
                            @NonNull TextChecker textChecker) {

        super(labelKey, iconId, titleId, descriptionId, flags, config, dataKey, defaultSourceType,
                detailsFormatter, elementCreator);
        this.editable = editable;
        this.textChecker = textChecker;
    }

    /**
     * 判断设置项的文本是否可以被编辑。
     *
     * @return 可以被编辑则返回 true，否则返回 false
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * 获取设置项的文本检查器对象。
     *
     * @return 文本检查器对象
     */
    @NonNull
    public TextChecker getTextChecker() {
        return textChecker;
    }

    public abstract static class Builder<T extends BaseTextModel, V extends Builder<T, V>>
            extends SavableModel.Builder<T, V> {

        protected boolean editable = true;
        protected TextChecker textChecker = DefaultTextChecker;

        private static final TextChecker DefaultTextChecker = new TextChecker() {
            @Override
            public boolean check(@NonNull String textStr) {
                return true;
            }

            @Override
            public int getTipsId() {
                return 0;
            }
        };

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
         * 设置该项的文本是否可以被编辑。
         *
         * @param editable 是否可以被编辑
         * @return 构造器本身
         */
        public V setEditable(boolean editable) {
            this.editable = editable;
            return self();
        }

        /**
         * 设置该项的文本检查器。
         *
         * @param textChecker 文本检查器对象
         * @return 构造器本身
         */
        public V setTextChecker(@NonNull TextChecker textChecker) {
            this.textChecker = textChecker;
            return self();
        }
    }
}
