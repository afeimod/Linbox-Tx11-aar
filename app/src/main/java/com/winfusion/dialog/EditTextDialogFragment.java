package com.winfusion.dialog;

import static android.view.View.GONE;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.databinding.DialogEditTextBinding;
import com.winfusion.utils.TextChecker;

import java.util.function.Consumer;

public class EditTextDialogFragment extends DialogFragment {

    private DialogEditTextBinding binding;
    private EditTextDialogFragmentArgs args;
    private final Consumer<String> resultCallback;
    private final TextChecker textChecker;

    public EditTextDialogFragment() {
        resultCallback = null;
        textChecker = null;
    }

    public EditTextDialogFragment(@NonNull Consumer<String> resultCallback,
                                  @Nullable TextChecker textChecker) {

        this.resultCallback = resultCallback;
        this.textChecker = textChecker;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        args = EditTextDialogFragmentArgs.fromBundle(getArguments());

        if (resultCallback == null)
            dismissAllowingStateLoss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogEditTextBinding.inflate(getLayoutInflater());

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(args.getTitleId())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    CharSequence text = binding.editText.getText();
                    if (text == null)
                        return;

                    String str = text.toString();
                    if (textChecker != null && !textChecker.check(str))
                        return;

                    if (resultCallback != null)
                        resultCallback.accept(str);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setView(binding.getRoot())
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        if (textChecker != null) {
            binding.editText.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (textChecker.check(s.toString())) {
                        binding.textLayout.setErrorEnabled(false);
                    } else {
                        binding.textLayout.setErrorEnabled(true);
                        int tipsId = textChecker.getTipsId();
                        binding.textLayout.setError(getResources().getText(tipsId));
                    }
                }
            });
        }

        binding.editText.setText(args.getDefaultText());
        binding.buttonCopy.setVisibility(GONE);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
