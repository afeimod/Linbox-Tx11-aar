package com.winfusion.feature.setting.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.feature.setting.model.SingleChoiceModel;
import com.winfusion.utils.TextUtils;

public class SingleChoiceDialogFragment extends BaseDialogFragment {

    private SingleChoiceModel model;

    public SingleChoiceDialogFragment() {
        super();
    }

    public SingleChoiceDialogFragment(@NonNull SingleChoiceModel model,
                                      @Nullable Runnable onDismissCallback) {

        super(onDismissCallback);
        this.model = model;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(model.getTitleId())
                .setSingleChoiceItems(
                        model.getChoiceNamesId(),
                        getCheckedItemFromModel(model),
                        (dialog, which) -> {
                            String v = getResources().getStringArray(model.getChoiceValuesId())[which];
                            model.setValue(model.getElementCreator().create(v));
                            dismiss();
                        }
                )
                .create();
    }

    private int getCheckedItemFromModel(@NonNull SingleChoiceModel model) {
        return TextUtils.tryGetStringPosInArrayRes(requireContext(), model.getChoiceValuesId(),
                model.getValue().getAsString());
    }
}
