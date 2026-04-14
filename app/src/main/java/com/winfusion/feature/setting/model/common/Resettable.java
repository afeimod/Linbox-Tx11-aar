package com.winfusion.feature.setting.model.common;

/**
 * 表示对象可以恢复到默认值的接口。
 */
public interface Resettable {

    /**
     * 恢复到默认值。
     */
    void resetToDefault();
}
