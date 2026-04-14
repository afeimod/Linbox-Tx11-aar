package com.winfusion.feature.setting.dialog;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class BaseDialogFragment extends DialogFragment {

    private static final Runnable DEFAULT_RUNNABLE = () -> {
        // do nothing
    };

    protected Runnable onDismissCallback;

    public BaseDialogFragment() {
        super();
    }

    public BaseDialogFragment(@Nullable Runnable onDismissCallback) {
        this.onDismissCallback = onDismissCallback == null ? DEFAULT_RUNNABLE : onDismissCallback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (onDismissCallback == null)
            dismissAllowingStateLoss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        onDismissCallback.run();
    }
}
