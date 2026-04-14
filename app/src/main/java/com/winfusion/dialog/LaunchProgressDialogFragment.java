package com.winfusion.dialog;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.databinding.DialogLaunchProgressBinding;

public class LaunchProgressDialogFragment extends DialogFragment {

    private DialogLaunchProgressBinding binding;
    private final MutableLiveData<String> titleData = new MutableLiveData<>();
    private final MutableLiveData<String> stageData = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> iconData = new MutableLiveData<>();

    public LaunchProgressDialogFragment() {
        super();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogLaunchProgressBinding.inflate(getLayoutInflater());
        return new MaterialAlertDialogBuilder(requireContext())
                .setView(binding.getRoot())
                .setCancelable(false)
                .create();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        setupObservers();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void setTitle(@NonNull String title) {
        titleData.postValue(title);
    }

    public void setStage(@NonNull String stage) {
        stageData.postValue(stage);
    }

    public void setIcon(@NonNull Bitmap bitmap) {
        iconData.postValue(bitmap);
    }

    private void setupObservers() {
        titleData.observe(this, s -> {
            if (s != null && binding != null)
                binding.textTitle.setText(s);
        });

        stageData.observe(this, s -> {
            if (s != null && binding != null)
                binding.textStage.setText(s);
        });

        iconData.observe(this, b -> {
            if (b != null && binding != null)
                binding.imageIcon.setImageBitmap(b);
        });
    }
}
