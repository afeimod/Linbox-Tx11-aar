package com.winfusion.feature.setting.model;

/**
 * 常量值类。
 */
public final class Constants {

    private Constants() {

    }

    public static final int FLAG_SHOW_ICON = 1;
    public static final int FLAG_SHOW_TITLE = 1 << 1;
    public static final int FLAG_SHOW_DESCRIPTION = 1 << 2;
    public static final int FLAG_SHOW_DETAILS = 1 << 3;
    public static final int FLAG_NON_RESETTABLE = 1 << 4;
    public static final int FLAG_NON_FOLLOWABLE = 1 << 5;
    public static final int INVALID_RES_ID = 0;
}
