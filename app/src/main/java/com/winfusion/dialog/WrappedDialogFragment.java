package com.winfusion.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class WrappedDialogFragment extends DialogFragment {

    private Dialog dialog;

    public WrappedDialogFragment() {
        super();
    }

    public WrappedDialogFragment(@NonNull Dialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (dialog == null)
            dismissAllowingStateLoss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dialog = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return dialog;
    }
}
