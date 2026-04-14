package com.winfusion.feature.input.key;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.winfusion.R;

public enum StandardAction implements StandardItem {

    MouseMoveUp(R.string.mouse_move_up),
    MouseMoveDown(R.string.mouse_move_down),
    MouseMoveLeft(R.string.mouse_move_left),
    MouseMoveRight(R.string.mouse_move_right),
    MouseScrollUp(R.string.mouse_scroll_up),
    MouseScrollDown(R.string.mouse_scroll_down),
    LeftThumbUp(R.string.left_thumb_up),
    LeftThumbDown(R.string.left_thumb_down),
    LeftThumbLeft(R.string.left_thumb_left),
    LeftThumbRight(R.string.left_thumb_right),
    RightThumbUp(R.string.right_thumb_up),
    RightThumbDown(R.string.right_thumb_down),
    RightThumbLeft(R.string.right_thumb_left),
    RightThumbRight(R.string.right_thumb_right);

    private final String symbol;
    private final int resId;

    StandardAction(@StringRes int resId) {
        symbol = name();
        this.resId = resId;
    }

    @NonNull
    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public int getResId() {
        return resId;
    }
}
