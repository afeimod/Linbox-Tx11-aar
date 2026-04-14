package com.winfusion.activity;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.winfusion.R;
import com.winfusion.databinding.ActivityWineBinding;
import com.winfusion.dialog.LaunchProgressDialogFragment;
import com.winfusion.dialog.WrappedDialogFragment;
import com.winfusion.feature.launcher.Launcher;
import com.winfusion.feature.manager.Container;
import com.winfusion.feature.manager.ContainerManager;
import com.winfusion.feature.manager.Shortcut;
import com.winfusion.feature.manager.ShortcutManager;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.utils.LaunchMode;
import com.winfusion.utils.UiUtils;

import java.util.Objects;

public class WineActivity extends AppCompatActivity {

    private static final String TAG = "WineActivity";
    private static final String LAUNCH_PROGRESS_DIALOG_TAG = "LaunchProgressDialog";

    private ActivityWineBinding binding;
    private Launcher launcher;
    private LaunchMode mode;
    private String uuid;
    private String title;
    private Container container;
    private Shortcut shortcut;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWineBinding.inflate(getLayoutInflater());
        WineActivityArgs args = WineActivityArgs.fromBundle(getIntent().getExtras());
        mode = LaunchMode.valueOf(args.getMode());
        uuid = args.getUuid();

        setContentView(binding.getRoot());
        UiUtils.setActivityFullscreen(this);
        showLaunchProgressDialog();

        switch (mode) {
            case Container -> container = ContainerManager.getInstance().getContainerByUUID(args.getUuid());
            case Shortcut -> shortcut = ShortcutManager.getInstance().getShortcutByUUID(args.getUuid());
        }
        if (container == null && shortcut == null) {
            showErrorDialog(getString(R.string.invalid_container_or_shortcut));
            return;
        }

        setupTitle();
        setupDrawerLayout();
        setupBackPress();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (launcher == null)
            startLauncher();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    private void setupTitle() {
        if (container != null)
            title = new SettingWrapper(container.getConfig()).getContainerInfoName();
        else if (shortcut != null)
            title = new SettingWrapper(shortcut.getConfig()).getShortcutInfoName();
    }

    private void setupDrawerLayout() {
        // TODO: 实现侧边栏
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (launcher != null)
                    launcher.terminator();
            }
        });
    }

    private void startLauncher() {
        launcher = new Launcher(this, mode, uuid);
        getLaunchProgressDialog().setTitle(title);
        launcher.launch(new Launcher.Callback() {
            @Override
            public void onStageChanged(boolean finished, int resId) {
                if (finished) {
                    runOnUiThread(() -> getLaunchProgressDialog().dismiss());
                    return;
                }

                getLaunchProgressDialog().setStage(String.format("%s %s…", getString(R.string.configuring),
                        getString(resId)));
            }

            @Override
            public void onLaunchFailed(@Nullable String reason) {
                launcher.terminator();
                runOnUiThread(() -> showErrorDialog(reason == null ? getString(R.string.unknown_error) : reason));
                Log.e(TAG, reason == null ? "" : reason);
            }

            @Override
            public void onRunningFatal(@NonNull String reason) {
                launcher.terminator();
                runOnUiThread(() -> showErrorDialog(reason));
                Log.e(TAG, reason);
            }

            @Override
            public void onShellOutput(@NonNull String out) {
                // TODO: 实现日志后端
                Log.d(TAG, out);
            }

            @Override
            public void onShellExit(int exitCode) {
                Log.d(TAG, "Shell exit with: " + exitCode);
                launcher.terminator();
                runOnUiThread(() -> finish());
            }
        }, binding.surface, binding.overlay);
    }

    private void showLaunchProgressDialog() {
        LaunchProgressDialogFragment dialog = new LaunchProgressDialogFragment();
        dialog.show(getSupportFragmentManager(), LAUNCH_PROGRESS_DIALOG_TAG);
    }

    private void showErrorDialog(@NonNull String errorMsg) {
        new WrappedDialogFragment(
                new MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.error)
                        .setMessage(errorMsg)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                        .setCancelable(false)
                        .create()
        ).show(getSupportFragmentManager(), null);
    }

    @NonNull
    private LaunchProgressDialogFragment getLaunchProgressDialog() {
        return (LaunchProgressDialogFragment) Objects.requireNonNull(getSupportFragmentManager().
                findFragmentByTag(LAUNCH_PROGRESS_DIALOG_TAG));
    }
}
