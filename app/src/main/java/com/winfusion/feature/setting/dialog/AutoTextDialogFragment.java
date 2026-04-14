package com.winfusion.feature.setting.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.R;
import com.winfusion.databinding.DialogAutoTextBinding;
import com.winfusion.feature.setting.model.AutoTextModel;
import com.winfusion.feature.setting.value.ConfigElement;
import com.winfusion.feature.setting.value.ConfigPrimitive;
import com.winfusion.utils.NoFilterArrayAdapter;
import com.winfusion.utils.SimpleTextWatcher;
import com.winfusion.utils.TextChecker;
import com.winfusion.utils.TextUtils;

public class AutoTextDialogFragment extends BaseDialogFragment {

    private AutoTextModel model;
    private TextChecker textChecker;
    private DialogAutoTextBinding binding;
    private String[] autoValues;

    public AutoTextDialogFragment() {
        super();
    }

    public AutoTextDialogFragment(@NonNull AutoTextModel model,
                                  @Nullable Runnable onDismissCallback) {

        super(onDismissCallback);
        this.model = model;
        textChecker = model.getTextChecker();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogAutoTextBinding.inflate(getLayoutInflater());
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(model.getTitleId())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    Editable text = binding.autoText.getText();
                    if (text != null) {
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
        String s;
        ConfigPrimitive primitive = model.getValue().getAsConfigPrimitive();
        if (primitive.isInt())
            s = String.valueOf(primitive.getAsInt());
        else if (primitive.isFloat())
            s = String.valueOf(primitive.getAsFloat());
        else if (primitive.isString())
            s = String.valueOf(primitive.getAsString());
        else
            throw new IllegalArgumentException("Unsupported value: " + primitive);
        binding.autoText.setText(s);

        binding.autoText.addTextChangedListener(new SimpleTextWatcher() {

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
                TextUtils.copyTextToClipboard(requireContext(), binding.autoText.getText()));

        // FIXME: 使用AutoTextView.setListSelection设置下拉菜单的位置

        ArrayAdapter<String> adapter = new NoFilterArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                getResources().getStringArray(model.getAutoNamesId())
        );
        binding.autoText.setAdapter(adapter);
        binding.autoText.setThreshold(0);

        autoValues = getResources().getStringArray(model.getAutoValuesId());
        binding.autoText.setOnItemClickListener((parent, view, position, id) ->
                binding.autoText.setText(autoValues[position]));

        binding.autoText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.autoText.showDropDown();
                binding.autoText.setListSelection(
                        TextUtils.tryGetStringPosInArrayRes(
                                requireContext(),
                                model.getAutoValuesId(),
                                binding.autoText.getText().toString()
                        )
                );
            } else {
                binding.autoText.dismissDropDown();
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
