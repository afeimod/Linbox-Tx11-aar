package com.winfusion.feature.input.overlay.popupwindow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.winfusion.R;
import com.winfusion.databinding.LayoutInputOverlayToolbarBinding;
import com.winfusion.feature.input.overlay.widget.WidgetProvider;

public class OverlayToolbarPopupWindow extends FloatHeaderPopupWindow {

    private static final int DefaultAlpha = (int) (255 * 0.8f);

    private LayoutInputOverlayToolbarBinding binding;
    private String profileName;
    private Callback callback;
    private boolean editMode = true;

    public OverlayToolbarPopupWindow(@NonNull Context context) {
        super(context);
        setExpandBtnEnabled(true);
        setCloseBtnEnabled(false);
        setOutsideTouchable(false);
        setTitle(R.string.toolbar);
        setAlpha(DefaultAlpha);
    }

    public void setProfileName(@NonNull String profileName) {
        this.profileName = profileName;
        if (binding != null) {
            binding.textProfileName.setText(profileName);
            update();
        }
    }

    public void setToolbarCallback(@NonNull Callback callback) {
        this.callback = callback;
    }

    @NonNull
    @Override
    protected View onCreateView() {
        binding = LayoutInputOverlayToolbarBinding.inflate(LayoutInflater.from(getContext()));
        binding.textProfileName.setText(profileName == null ? "" : profileName);
        binding.buttonAdd.setOnClickListener(v -> {
            if (callback != null)
                callback.onCreatingWidget();
        });
        binding.buttonDelete.setOnClickListener(v -> {
            if (callback != null)
                callback.onDeletingSelectedWidget();
        });
        binding.buttonToggleStatus.setOnClickListener(v -> {
            WidgetProvider.Status status;
            editMode = !editMode;
            if (editMode) {
                status = WidgetProvider.Status.Edit;
                binding.buttonToggleStatus.setIconResource(R.drawable.ic_fullscreen);
                binding.buttonToggleStatus.setText(R.string.switch_to_preview);
            } else {
                status = WidgetProvider.Status.Preview;
                binding.buttonToggleStatus.setIconResource(R.drawable.ic_edit);
                binding.buttonToggleStatus.setText(R.string.switch_to_edit);
            }
            if (callback != null)
                callback.onStatusUpdate(status);
        });
        return binding.getRoot();
    }

    @Override
    protected void onDestroyView() {
        binding = null;
    }

    public interface Callback {

        void onCreatingWidget();

        void onDeletingSelectedWidget();

        void onStatusUpdate(@NonNull WidgetProvider.Status status);
    }
}
