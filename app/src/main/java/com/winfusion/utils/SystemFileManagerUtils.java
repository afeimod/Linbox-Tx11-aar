package com.winfusion.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.DocumentsContract;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * 系统文件管理器相关的工具类。
 */
public final class SystemFileManagerUtils {

    private static final String ANDROID_PROVIDER_ACTION_BROWSE = "android.provider.action.BROWSE";
    private static final String COM_GOOGLE_ANDROID_DOCUMENTSUI = "com.google.android.documentsui";
    private static final String COM_ANDROID_DOCUMENTSUI = "com.android.documentsui";

    private SystemFileManagerUtils() {

    }

    /**
     * 打开系统的文件管理器。
     *
     * @param context 上下文对象
     */
    public static void openFileManager(@NonNull final Context context) {
        try {
            context.startActivity(getFileManagerIntentOnDocumentProvider(context, Intent.ACTION_VIEW));
            return;
        } catch (ActivityNotFoundException e) {

        }

        try {
            context.startActivity(getFileManagerIntentOnDocumentProvider(context, ANDROID_PROVIDER_ACTION_BROWSE));
            return;
        } catch (ActivityNotFoundException e) {

        }

        try {
            context.startActivity(getFileManagerIntent(COM_GOOGLE_ANDROID_DOCUMENTSUI));
            return;
        } catch (ActivityNotFoundException e) {

        }

        try {
            context.startActivity(getFileManagerIntent(COM_ANDROID_DOCUMENTSUI));
        } catch (ActivityNotFoundException e) {

        }
    }

    private static Intent getFileManagerIntent(@NonNull final String packageName) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName(packageName, "com.android.documentsui.files.FilesActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private static Intent getFileManagerIntentOnDocumentProvider(@NonNull final Context context,
                                                                 @NonNull final String action) {
        String authority = context.getPackageName() + ".user";
        Intent intent = new Intent(action);
        Uri uri = DocumentsContract.buildRootUri(authority, DocumentsContract.Root.COLUMN_ROOT_ID);

        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(uri);
        intent.addFlags(
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION |
                        Intent.FLAG_GRANT_PREFIX_URI_PERMISSION |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        );

        return intent;
    }
}
