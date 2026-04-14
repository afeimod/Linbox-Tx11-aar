package com.winfusion.feature.launcher;

import android.content.Context;

import androidx.annotation.NonNull;

import com.winfusion.core.compression.exception.CompressorException;
import com.winfusion.core.wfp.WfpParser;
import com.winfusion.feature.manager.ContentManager;

import java.io.IOException;

public final class Utils {

    private Utils() {

    }

    public static void installBuiltinWfp(@NonNull String name, @NonNull Context context)
            throws LauncherException {

        try {
            ContentManager.getInstance().installBuiltinWfp(name, context);
        } catch (IOException | CompressorException | WfpParser.BadWfpFormatException e) {
            throw new LauncherException(e);
        }
    }
}
