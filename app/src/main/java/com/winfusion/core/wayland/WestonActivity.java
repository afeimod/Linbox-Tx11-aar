package com.winfusion.core.wayland;

import static com.winfusion.core.wayland.Constants.WESTON_RENDERER_PIXMAN;
import static com.winfusion.core.wayland.Constants.WL_TOUCH_CANCEL;
import static com.winfusion.core.wayland.Constants.WL_TOUCH_DOWN;
import static com.winfusion.core.wayland.Constants.WL_TOUCH_MOTION;
import static com.winfusion.core.wayland.Constants.WL_TOUCH_UP;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.winfusion.application.WinfusionApplication;
import com.winfusion.core.wayland.exception.WaylandException;
import com.winfusion.core.wayland.exception.WaylandRuntimeException;
import com.winfusion.databinding.ActivityWestonBinding;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WestonActivity extends AppCompatActivity {

    private static final String TAG = "WestonActivity";

    private Weston weston;
    private WestonSimpleClient damageClient;
    private WestonSimpleClient eglClient;
    private WestonSimpleClient shmClient;
    private WestonSimpleClient touchClient;
    private ActivityWestonBinding binding;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWestonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupWindow();
        setupWeston();
        setupClients();
        setupButtons();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        damageClient.stop();
        eglClient.stop();
        shmClient.stop();
        weston.destroy();
        binding = null;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupWeston() {
        try {
            weston = new Weston();
        } catch (WaylandException e) {
            throw new RuntimeException(e);
        }

        WestonConfig config = weston.getConfig();
        Path filesDir = Paths.get(getFilesDir().getAbsolutePath());
        Path tmpDir = Paths.get(getCacheDir().getAbsolutePath()).resolve("tmp");

        try {
            if (!Files.isDirectory(tmpDir))
                Files.createDirectories(tmpDir);
        } catch (IOException e) {
            throw new WaylandRuntimeException(e);
        }

        config.setSocketPath(tmpDir.resolve("wayland-0").toString());
        config.setXkbConfigRootPath(filesDir.resolve("xkb").toString());
        config.setXdgRuntimePath(tmpDir.toString());
        config.setRenderRefreshRate(60);
        config.setRendererType(WESTON_RENDERER_PIXMAN);
        config.setScreenWidth(1920);
        config.setScreenHeight(1080);

        binding.surfaceWeston.setOnTouchListener((v, event) -> {
            int touchType = switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> WL_TOUCH_DOWN;
                case MotionEvent.ACTION_MOVE -> WL_TOUCH_MOTION;
                case MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> WL_TOUCH_UP;
                case MotionEvent.ACTION_CANCEL -> WL_TOUCH_CANCEL;
                default -> -1;
            };

            if (touchType == -1)
                return false;

            if (touchType == WL_TOUCH_MOTION) {
                int pointerCount = event.getPointerCount();
                for (int i = 0; i < pointerCount; i++) {
                    weston.getInput().performTouch(event.getPointerId(i), touchType,
                            event.getX(i), event.getY(i));
                }
            } else {
                weston.getInput().performTouch(event.getPointerId(event.getActionIndex()), touchType,
                        event.getX(event.getActionIndex()), event.getY(event.getActionIndex()));
            }

            return true;
        });

        try {
            weston.start(WinfusionApplication.getInstance().getExecutor());
        } catch (WaylandException e) {
            throw new RuntimeException(e);
        }

        weston.setWestonGLSurfaceView(binding.surfaceWeston);
    }

    private void setupClients() {
        String nativeLibraryDir = getApplicationInfo().nativeLibraryDir;
        String xdgRuntimeDir = weston.getConfig().getXdgRuntimePath();

        damageClient = new WestonSimpleClient(nativeLibraryDir, WestonSimpleClient.Name.WESTON_SIMPLE_DAMAGE);
        damageClient.setXdgRuntimePath(xdgRuntimeDir);

        eglClient = new WestonSimpleClient(nativeLibraryDir, WestonSimpleClient.Name.WESTON_SIMPLE_EGL);
        eglClient.setXdgRuntimePath(xdgRuntimeDir);

        shmClient = new WestonSimpleClient(nativeLibraryDir, WestonSimpleClient.Name.WESTON_SIMPLE_SHM);
        shmClient.setXdgRuntimePath(xdgRuntimeDir);

        touchClient = new WestonSimpleClient(nativeLibraryDir, WestonSimpleClient.Name.WESTON_SIMPLE_TOUCH);
        touchClient.setXdgRuntimePath(xdgRuntimeDir);
    }

    private void setupButtons() {
        setupButton(binding.buttonDamage, damageClient);
        setupButton(binding.buttonEgl, eglClient);
        setupButton(binding.buttonShm, shmClient);
        setupButton(binding.buttonTouch, touchClient);
    }

    @SuppressLint("SetTextI18n")
    private void setupButton(@NonNull Button button, @NonNull WestonSimpleClient client) {

        String clientName = client.getName().name();
        button.setText("Run " + clientName);
        button.setOnClickListener(v -> {
            if (client.isRunning()) {
                client.stop();
                return;
            }

            try {
                button.setText("Kill " + clientName);
                client.onStdOut(s -> Log.d(clientName, s));
                client.onStdErr(s -> Log.d(clientName, s));
                client.onExit(i -> {
                    Log.d(clientName, "exit with: " + i);
                    runOnUiThread(() -> {
                        button.setEnabled(true);
                        button.setText("Run " + clientName);
                    });
                });
                client.start();
            } catch (IOException e) {
                button.setText("Run failed");
                Log.e(TAG, "Failed to run " + clientName, e);
            }
        });
    }

    private void setupWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller;
            controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
}
