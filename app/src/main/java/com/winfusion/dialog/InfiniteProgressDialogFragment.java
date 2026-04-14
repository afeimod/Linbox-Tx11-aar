package com.winfusion.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.databinding.DialogProcessingBinding;

public class InfiniteProgressDialogFragment extends DialogFragment {

    private DialogProcessingBinding binding;
    private InfiniteProgressDialogFragmentArgs args;

    public InfiniteProgressDialogFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        args = InfiniteProgressDialogFragmentArgs.fromBundle(getArguments());

        if (savedInstanceState != null)
            dismissAllowingStateLoss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogProcessingBinding.inflate(getLayoutInflater());
        setCancelable(false);
        return new MaterialAlertDialogBuilder(requireContext())
                .setCancelable(false)
                .setView(binding.getRoot())
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding.textMessage.setText(args.getMsgId());
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
