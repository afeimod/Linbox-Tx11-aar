package com.winfusion.feature.setting.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.R;
import com.winfusion.feature.setting.model.BaseModel;
import com.winfusion.feature.setting.model.common.Resettable;

public class RestoreDefaultDialogFragment extends BaseDialogFragment {

    private BaseModel model;

    public RestoreDefaultDialogFragment() {
        super();
    }

    public RestoreDefaultDialogFragment(@NonNull BaseModel model,
                                        @Nullable Runnable onDismissCallback) {

        super(onDismissCallback);
        this.model = model;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.reset)
                .setMessage(R.string.reset_setting_to_default)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (model instanceof Resettable resettable)
                        resettable.resetToDefault();
                    else
                        throw new UnsupportedOperationException("Model is not resettable: " + model);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}
