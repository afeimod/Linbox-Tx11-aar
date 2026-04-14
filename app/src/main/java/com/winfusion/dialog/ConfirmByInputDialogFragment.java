package com.winfusion.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.databinding.DialogConfirmByInputBinding;
import com.winfusion.utils.TextUtils;

import java.util.Objects;
import java.util.function.Consumer;

public class ConfirmByInputDialogFragment extends DialogFragment {

    private DialogConfirmByInputBinding binding;
    private ConfirmByInputDialogFragmentArgs args;
    private final Consumer<Boolean> resultCallback;

    public ConfirmByInputDialogFragment() {
        resultCallback = null;
    }

    public ConfirmByInputDialogFragment(@NonNull Consumer<Boolean> resultCallback) {
        this.resultCallback = resultCallback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        args = ConfirmByInputDialogFragmentArgs.fromBundle(getArguments());

        if (resultCallback == null)
            dismissAllowingStateLoss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogConfirmByInputBinding.inflate(getLayoutInflater());

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(args.getTitleId())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    Editable editable = binding.editTextInput.getText();

                    if (editable == null)
                        return;

                    if (resultCallback != null)
                        resultCallback.accept(Objects.equals(editable.toString(), args.getKey()));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setView(binding.getRoot())
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding.textMsg.setText(args.getMsgId());
        binding.textKey.setText(args.getKey());

        binding.textKey.setLongClickable(true);
        binding.textKey.setOnLongClickListener(v -> {
            TextUtils.copyTextToClipboard(requireContext(), binding.textKey.getText());
            return true;
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
