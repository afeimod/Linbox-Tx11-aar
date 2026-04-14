package com.winfusion.utils;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/**
 * 文本检查接口。
 * 用于检查输入的文本是否合法，如果不合法，则可以通过 {@link #getTipsId()} 获取错误提示。
 */
public interface TextChecker {

    /**
     * 判断文本输入是否合法。
     *
     * @param textStr 文本输入
     * @return 如果合法则返回 true，否则返回 false
     */
    boolean check(@NonNull String textStr);

    /**
     * 获取错误提示的资源 id
     *
     * @return 如果不合法，则返回对应的资源 id，否则行为未定义
     */
    @StringRes
    int getTipsId();
}
