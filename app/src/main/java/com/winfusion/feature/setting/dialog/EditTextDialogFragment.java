package com.winfusion.feature.setting.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.R;
import com.winfusion.databinding.DialogEditTextBinding;
import com.winfusion.feature.setting.model.TextModel;
import com.winfusion.utils.SimpleTextWatcher;
import com.winfusion.utils.TextChecker;
import com.winfusion.utils.TextUtils;

public class EditTextDialogFragment extends BaseDialogFragment {

    private TextModel model;
    private TextChecker textChecker;
    private DialogEditTextBinding binding;

    public EditTextDialogFragment() {
        super();
    }

    public EditTextDialogFragment(@NonNull TextModel model, @Nullable Runnable onDismissCallback) {
        super(onDismissCallback);
        this.model = model;
        textChecker = model.getTextChecker();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogEditTextBinding.inflate(getLayoutInflater());

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(model.getTitleId())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    Editable text = binding.editText.getText();
                    if (model.isEditable() && text != null) {
                        String textStr = text.toString();
                        if (textChecker == null || textChecker.check(text.toString()))
                            model.setValue(model.getElementCreator().create(textStr));
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setView(binding.getRoot())
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding.editText.setText(model.getValue().getAsString());
        binding.editText.setFocusable(model.isEditable());
        binding.editText.setCursorVisible(model.isEditable());
        binding.editText.addTextChangedListener(new SimpleTextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (textChecker != null) {
                    if (textChecker.check(s.toString())) {
                        binding.textLayout.setErrorEnabled(false);
                    } else {
                        binding.textLayout.setErrorEnabled(true);
                        int tipsId = textChecker == null ? R.string.invalid_input :
                                textChecker.getTipsId();
                        binding.textLayout.setError(getResources().getText(tipsId));
                    }
                }
            }
        });

        binding.buttonCopy.setOnClickListener(v ->
                TextUtils.copyTextToClipboard(requireContext(), binding.editText.getText()));

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
