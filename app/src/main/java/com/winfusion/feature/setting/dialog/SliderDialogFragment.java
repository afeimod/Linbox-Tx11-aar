package com.winfusion.feature.setting.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.databinding.DialogSliderBinding;
import com.winfusion.feature.setting.model.SliderModel;

public class SliderDialogFragment extends BaseDialogFragment {

    private SliderModel model;
    private DialogSliderBinding binding;

    public SliderDialogFragment() {
        super();
    }

    public SliderDialogFragment(@NonNull SliderModel model,
                                @Nullable Runnable onDismissCallback) {

        super(onDismissCallback);
        this.model = model;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogSliderBinding.inflate(getLayoutInflater());

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(model.getTitleId())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    int value = (int) binding.slider.getValue();
                    model.setValue(model.getElementCreator().create(value));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setView(binding.getRoot())
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding.slider.setValueFrom(model.getValueFrom());
        binding.slider.setValueTo(model.getValueTo());
        binding.slider.setStepSize(model.getValueStep());
        binding.slider.setValue(Math.clamp(model.getValue().getAsInt(), model.getValueFrom(),
                model.getValueTo()));

        binding.buttonIncrease.setOnClickListener(v -> increaseSliderValue());
        binding.buttonDecrease.setOnClickListener(v -> decreaseSliderValue());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void increaseSliderValue() {
        int newValue = (int) (binding.slider.getValue() + binding.slider.getStepSize());
        if (newValue < binding.slider.getValueTo())
            binding.slider.setValue(newValue);
    }

    private void decreaseSliderValue() {
        int newValue = (int) (binding.slider.getValue() - binding.slider.getStepSize());
        if (newValue > binding.slider.getValueFrom())
            binding.slider.setValue(newValue);
    }
}
