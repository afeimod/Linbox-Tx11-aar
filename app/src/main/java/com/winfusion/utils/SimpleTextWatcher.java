package com.winfusion.utils;

import android.text.TextWatcher;

/**
 * 简化的 {@link TextWatcher} 类，仅需要实现 {@link #afterTextChanged} 方法。
 */
public abstract class SimpleTextWatcher implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
}
