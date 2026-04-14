package com.winfusion.feature.launcher;

import static com.winfusion.core.wayland.Constants.*;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.winfusion.core.shell.ShellExecutor;
import com.winfusion.core.shm.SHMServer;
import com.winfusion.core.shm.SHMServerCallback;
import com.winfusion.core.shm.driver.BaseSHMDriver;
import com.winfusion.core.shm.exception.SHMServerException;
import com.winfusion.core.wayland.Weston;
import com.winfusion.core.wayland.WestonConfig;
import com.winfusion.core.wayland.WestonSurfaceView;
import com.winfusion.core.wayland.WestonInput;
import com.winfusion.core.wayland.exception.WaylandException;
import com.winfusion.feature.input.event.ControllerAxisEvent;
import com.winfusion.feature.input.event.ControllerButtonEvent;
import com.winfusion.feature.input.event.KeyboardEvent;
import com.winfusion.feature.input.event.MouseButtonEvent;
import com.winfusion.feature.input.event.MousePointerEvent;
import com.winfusion.feature.input.event.MouseScrollEvent;
import com.winfusion.feature.input.interfaces.InputInterface;
import com.winfusion.feature.input.key.WaylandButtonKeymap;
import com.winfusion.feature.input.key.WaylandKeymap;
import com.winfusion.feature.input.overlay.OverlayController;
import com.winfusion.feature.input.overlay.OverlayView;
import com.winfusion.feature.input.overlay.widget.WidgetProvider;
import com.winfusion.feature.launcher.configure.Box64Configure;
import com.winfusion.feature.launcher.configure.Configure;
import com.winfusion.feature.launcher.configure.DirectXWrapperConfigure;
import com.winfusion.feature.launcher.configure.OpenglDriverConfigure;
import com.winfusion.feature.launcher.configure.RootfsConfigure;
import com.winfusion.feature.launcher.configure.VulkanDriverConfigure;
import com.winfusion.feature.launcher.configure.WineConfigure;
import com.winfusion.feature.launcher.configure.XkbConfigure;
import com.winfusion.feature.manager.Overlay;
import com.winfusion.feature.manager.OverlayManager;
import com.winfusion.feature.manager.Rootfs;
import com.winfusion.feature.setting.key.SettingWrapper;
import com.winfusion.utils.FileUtils;
import com.winfusion.utils.LaunchMode;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Launcher {

    private static final String TAG = "Launcher";
    public static final String SHM_KEY = "WINFUSION_SHM";
    public static final String SHM_SOCKET_NAME = "shm";
    public static final int SHM_SOCKET_ID = 0;
    public static final String WAYLAND_SOCKET_NAME = "wayland";
    public static final int WAYLAND_SOCKET_ID = 0;

    private final Profile profile;
    private final WaylandKeymap waylandKeymap;
    private final WaylandButtonKeymap waylandButtonKeymap;
    private final Context context;
    private ThreadPoolExecutor executor;
    private Callback callback;
    private SHMServer shmServer;
    private Weston weston;
    private WestonInput westonInput;
    private WestonSurfaceView westonSurfaceView;
    private OverlayController overlayController;
    private OverlayView overlayView;
    private ShellExecutor shellExecutor;
    private boolean launched = false;

    public Launcher(@NonNull Context context, @NonNull LaunchMode mode, @NonNull String uuid) {
        this.context = context;
        profile = new Profile(mode, uuid, Paths.get(context.getFilesDir().getAbsolutePath()));
        waylandKeymap = new WaylandKeymap();
        waylandButtonKeymap = new WaylandButtonKeymap();
    }

    public void launch(@NonNull Callback callback, @NonNull WestonSurfaceView westonSurfaceView,
                       @NonNull OverlayView overlayView) {

        if (launched)
            return;
        else
            launched = true;

        executor = new ThreadPoolExecutor(16, 16, 30,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadPoolExecutor.AbortPolicy());
        this.callback = callback;
        this.westonSurfaceView = westonSurfaceView;
        this.overlayView = overlayView;
        checkDirs();

        executor.submit(() -> {
            Configure[] configures = new Configure[]{
                    new RootfsConfigure(),
                    new XkbConfigure(),
                    new Box64Configure(),
                    new WineConfigure(),
                    new VulkanDriverConfigure(),
                    new OpenglDriverConfigure(),
                    new DirectXWrapperConfigure()
            };

            boolean configureFinished = true;
            for (Configure configure : configures) {
                callback.onStageChanged(false, configure.stageResId());
                try {
                    configure.configure(profile, context);
                } catch (LauncherException e) {
                    callback.onLaunchFailed(e.getMessage());
                    configureFinished = false;
                    break;
                }
            }

            if (!configureFinished)
                return;

            callback.onStageChanged(true, 0);

            try {
                startSHMServer();
                startWeston();
                startControlsOverlay();

                // sleep for 1s to wait for shm and weston to start.
                // FIXME: consider using a callback to detect when shm and weston have actually started.
                Thread.sleep(1000);
                startShell();
            } catch (Exception e) {
                callback.onLaunchFailed(e.getMessage());
                clean();
            }
        });
    }

    public void terminator() {
        if (!launched)
            return;
        else
            launched = false;
        clean();
    }

    @NonNull
    public InputInterface getInputInterface() {
        return inputInterface;
    }

    private void startSHMServer() throws SHMServerException {
        String socketPath = profile.getRootfs().getTmpDir()
                .resolve(SHM_SOCKET_NAME + "-" + SHM_SOCKET_ID).toString();
        profile.getEnv().put(SHM_KEY, socketPath);
        shmServer = new SHMServer(socketPath, new SHMServerCallback() {
            @Override
            public void onRuntimeFatal(SHMServerException e) {
                callback.onRunningFatal(e.toString());
            }

            @Nullable
            @Override
            public BaseSHMDriver onRequireDriver(int clientType) {
                // TODO: 提供驱动
                return null;
            }

            @Override
            public void onDriverAttached(@NonNull BaseSHMDriver driver) {
                Log.d(TAG, "Driver attached: " + driver.name() + " fd: " + driver.getSharedMemoryFd());
            }

            @Override
            public void onDriverDetached(@NonNull BaseSHMDriver driver) {
                Log.d(TAG, "Driver detached: " + driver.name());
            }
        });
        shmServer.start(executor);
    }

    private void checkDirs() {
        Rootfs rootfs = profile.getRootfs();
        try {
            FileUtils.checkDirectory(rootfs.getRootfsDir());
            FileUtils.checkDirectory(rootfs.getTmpDir());
            FileUtils.checkDirectory(rootfs.getXkbDir());
        } catch (IOException e) {
            Log.e(TAG, "Failed to check dir.", e);
        }
    }

    private void startWeston() throws WaylandException {
        Rootfs rootfs = profile.getRootfs();
        String waylandDisplay = WAYLAND_SOCKET_NAME + "-" + WAYLAND_SOCKET_ID;
        profile.getEnv().put("XDG_RUNTIME_DIR", rootfs.getTmpDir().toString());
        profile.getEnv().put("WAYLAND_DISPLAY", waylandDisplay);

        weston = new Weston();
        westonInput = weston.getInput();

        SettingWrapper wrapper = profile.getSettingWrapper();
        WestonConfig westonConfig = weston.getConfig();
        westonConfig.setSocketPath(rootfs.getTmpDir().resolve(waylandDisplay).toString());
        westonConfig.setXkbConfigRootPath(rootfs.getXkbDir().toString());
        westonConfig.setXdgRuntimePath(rootfs.getTmpDir().toString());
        westonConfig.setRenderRefreshRate(wrapper.getContainerDisplayRefreshRate());
        westonConfig.setRendererType(WESTON_RENDERER_PIXMAN);
        String[] resolution = wrapper.getContainerDisplayResolution().toLowerCase().split("x");
        westonConfig.setScreenWidth(Integer.parseInt(resolution[0]));
        westonConfig.setScreenHeight(Integer.parseInt(resolution[1]));

        weston.start(executor);
        weston.setWestonGLSurfaceView(westonSurfaceView);
    }

    private void startControlsOverlay() {
        overlayView.post(() -> {
            overlayController = overlayView.getController();
            overlayController.updateScreenSize(overlayView.getWidth(), overlayView.getHeight());
            overlayController.updateStatus(WidgetProvider.Status.Control);
            Overlay overlay = OverlayManager.getInstance().getOverlayByFileName(
                    profile.getSettingWrapper().getContainerControlOverlayProfile());
            if (overlay != null)
                overlayController.setProfile(overlay.getProfile());
            overlayView.addOnLayoutChangeListener((v, left, top, right,
                                                   bottom, oldLeft, oldTop, oldRight,
                                                   oldBottom) ->
                    overlayController.updateScreenSize(right - left, bottom - top));
        });
    }

    private void startShell() throws IOException {
        profile.getEnv().put("WINFUSION_HOME", profile.getFilesDir().toString());
        shellExecutor = new ShellExecutor()
                .setCommand(buildCommand())
                .putEnv(profile.getEnv())
                .setWorkingDir(profile.getContainer().getUserHomeDir())
                .onStdOut(s -> callback.onShellOutput(s))
                .onStdErr(s -> callback.onShellOutput(s))
                .onExit(i -> callback.onShellExit(i))
                .exec();
    }

    @NonNull
    private String[] buildCommand() {
        if (profile.getBox64BinaryPath() == null || profile.getWineBinaryPath() == null)
            throw new IllegalArgumentException("Binary path is not set.");

        // 获取屏幕分辨率
        String resolution = profile.getSettingWrapper().getContainerDisplayResolution();
        
        // 构造 Wine 虚拟桌面命令
        // 格式: box64 wine explorer /desktop=wine,<分辨率> <程序>
        // 默认使用 taskmgr 作为启动程序
        String desktopArg = "explorer /desktop=wine," + resolution;
        
        return new String[]{
                profile.getBox64BinaryPath().toString(),
                profile.getWineBinaryPath().toString(),
                desktopArg,
                "taskmgr"
        };
    }

    private final InputInterface inputInterface = new InputInterface() {
        @Override
        public void onControllerAxisEvent(@NonNull ControllerAxisEvent event) {
            // TODO: 处理控制器轴事件
        }

        @Override
        public void onControllerButtonEvent(@NonNull ControllerButtonEvent event) {
            // TODO: 处理控制器按键事件
        }

        @Override
        public void onKeyboardEvent(@NonNull KeyboardEvent event) {
            if (westonInput == null)
                return;

            //Log.d(TAG, event.toString());

            switch (event.getState()) {
                case Pressed -> westonInput.performKey(waylandKeymap.toCode(event.getKey()),
                        WL_KEYBOARD_KEY_STATE_PRESSED);
                case Released -> westonInput.performKey(waylandKeymap.toCode(event.getKey()),
                        WL_KEYBOARD_KEY_STATE_RELEASED);
            }
        }

        @Override
        public void onMousePointerEvent(@NonNull MousePointerEvent event) {
            if (westonInput == null)
                return;

            //Log.d(TAG, event.toString());

            switch (event.getType()) {
                case Relative -> westonInput.performPointer(WESTON_POINTER_MOTION_REL, event.getX(),
                        event.getY());
                case Absolute -> westonInput.performPointer(WESTON_POINTER_MOTION_ABS, event.getX(),
                        event.getY());
            }
        }

        @Override
        public void onMouseButtonEvent(@NonNull MouseButtonEvent event) {
            if (westonInput == null)
                return;

            //Log.d(TAG, event.toString());

            switch (event.getState()) {
                case Pressed ->
                        westonInput.performButton(waylandButtonKeymap.toCode(event.getButton()),
                                WL_POINTER_BUTTON_STATE_PRESSED);
                case Released ->
                        westonInput.performButton(waylandButtonKeymap.toCode(event.getButton()),
                                WL_POINTER_BUTTON_STATE_RELEASED);
            }
        }

        @Override
        public void onMouseScrollEvent(@NonNull MouseScrollEvent event) {
            if (westonInput == null)
                return;

            //Log.d(TAG, event.toString());

            switch (event.getDir()) {
                case Horizontal -> westonInput.performAxis(WL_POINTER_AXIS_HORIZONTAL_SCROLL,
                        event.getValue(), false, 0);
                case Vertical -> westonInput.performAxis(WL_POINTER_AXIS_VERTICAL_SCROLL,
                        event.getValue(), false, 0);
            }
        }
    };

    private void clean() {
        if (shmServer != null)
            shmServer.stop();
        if (weston != null) {
            weston.stop();
            weston.destroy();
        }
        if (shellExecutor != null)
            shellExecutor.stop(true);
        if (executor != null)
            executor.shutdown();

        shmServer = null;
        weston = null;
        westonInput = null;
        shellExecutor = null;
        executor = null;
    }

    public interface Callback {

        void onStageChanged(boolean finished, @StringRes int resId);

        void onLaunchFailed(@Nullable String reason);

        void onRunningFatal(@NonNull String reason);

        void onShellOutput(@NonNull String out);

        void onShellExit(int exitCode);
    }
}
