package com.winfusion.feature.input.overlay;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.R;
import com.winfusion.databinding.ActivityControlsOverlayEditorBinding;
import com.winfusion.feature.input.overlay.widget.WidgetProvider;
import com.winfusion.feature.manager.Overlay;
import com.winfusion.feature.manager.OverlayManager;
import com.winfusion.utils.UiUtils;

public class OverlayEditorActivity extends AppCompatActivity {

    private ActivityControlsOverlayEditorBinding binding;
    private OverlayController overlayController;
    private boolean profileLoaded = false;
    private OverlayEditorActivityArgs args;
    private Overlay overlay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = OverlayEditorActivityArgs.fromBundle(getIntent().getExtras());
        binding = ActivityControlsOverlayEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        UiUtils.setActivityFullscreen(this);
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!profileLoaded || !overlayController.isWidgetChanged()) {
                    finish();
                    return;
                }

                new MaterialAlertDialogBuilder(OverlayEditorActivity.this)
                        .setTitle(R.string.save)
                        .setMessage(R.string.save_overlay_warning_description)
                        .setPositiveButton(R.string.save, (dialog, which) -> {
                            if (overlay != null) {
                                OverlayManager.getInstance().saveOverlay(overlayController.toProfile(),
                                        overlay.getFileName());
                            }
                            finish();
                        })
                        .setNegativeButton(R.string.quit, (dialog, which) -> finish())
                        .setNeutralButton(android.R.string.cancel, null)
                        .create()
                        .show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (overlayController == null)
            overlayController = binding.overlay.getController();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!profileLoaded)
            setupOverlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overlayController.destroy();
        binding = null;
    }

    private void setupOverlay() {
        binding.overlay.post(() -> {
            overlayController.updateScreenSize(binding.overlay.getWidth(), binding.overlay.getHeight());
            overlayController.updateStatus(WidgetProvider.Status.Edit);
            overlay = OverlayManager.getInstance().getOverlayByFileName(args.getFileName());
            if (overlay != null)
                overlayController.setProfile(overlay.getProfile());
            binding.overlay.addOnLayoutChangeListener((v, left, top, right,
                                                       bottom, oldLeft, oldTop, oldRight,
                                                       oldBottom) ->
                    overlayController.updateScreenSize(right - left, bottom - top));
            profileLoaded = true;
        });
    }
}
