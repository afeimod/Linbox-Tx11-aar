package com.winfusion.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * 文本工具类，提供一些和文本相关的方法。
 */
public final class TextUtils {

    private TextUtils() {

    }

    /**
     * 复制文本到系统剪贴板。
     *
     * @param context 上下文对象
     * @param text    要复制的文本
     */
    public static void copyTextToClipboard(@NonNull Context context, @Nullable CharSequence text) {
        if (text == null)
            return;

        ClipboardManager clipboard =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(null, text);
        clipboard.setPrimaryClip(clipData);
    }

    /**
     * 尝试获取字符串在 Res 数组资源中的位置。
     *
     * @param context    上下文对象
     * @param arrayResId Res 数组的 Id
     * @param value      要匹配的字符串
     * @return 如果字符串在 Res 数组资源中，则返回对应的位置，否则返回 -1
     */
    public static int tryGetStringPosInArrayRes(@NonNull Context context, @ArrayRes int arrayResId,
                                                @NonNull String value) {

        String[] values = context.getResources().getStringArray(arrayResId);
        int pos = -1;

        for (int i = 0; i < values.length; i++) {
            if (Objects.equals(values[i], value)) {
                pos = i;
                break;
            }
        }

        return pos;
    }

    /**
     * 尝试获取 value 对应的 name。
     *
     * @param context  上下文对象
     * @param namesId  name 的 Res 数组 Id
     * @param valuesId value 的 Res 数组 Id
     * @param value    要匹配的 value
     * @return 如果 value 在数组资源中，则返回对应位置的 name，否则直接返回 value 本身
     */
    @NonNull
    public static String tryMatchValueNameByValue(@NonNull Context context, @ArrayRes int namesId,
                                                  @ArrayRes int valuesId, @NonNull String value) {

        int pos = tryGetStringPosInArrayRes(context, valuesId, value);

        if (pos == -1)
            return value;
        else
            return context.getResources().getStringArray(namesId)[pos];
    }
}
