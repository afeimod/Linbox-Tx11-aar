package com.winfusion.core.wayland;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.shell.ShellExecutor;
import com.winfusion.core.wayland.exception.WaylandRuntimeException;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class WestonSimpleClient {

    public static final String TAG = "SimpleClient";

    public enum Name {
        WESTON_SIMPLE_DAMAGE("weston-simple-damage.so"),
        WESTON_SIMPLE_EGL("weston-simple-egl.so"),
        WESTON_SIMPLE_SHM("weston-simple-shm.so"),
        WESTON_SIMPLE_TOUCH("weston-simple-touch.so");

        public final String library;

        Name(@NonNull String library) {
            this.library = library;
        }
    }

    private final Name name;
    private final String[] args;
    private final String nativeLibraryPath;
    private String xdgRuntimePath;
    private final ShellExecutor shellExecutor = new ShellExecutor();

    public WestonSimpleClient(@NonNull String nativeLibraryPath, @NonNull Name name,
                              @Nullable String... args) {

        this.nativeLibraryPath = nativeLibraryPath;
        this.name = name;
        this.args = args == null ? new String[0] : args;
    }

    public void setXdgRuntimePath(@NonNull String xdgRuntimePath) {
        this.xdgRuntimePath = xdgRuntimePath;
    }

    @Nullable
    public String getXdgRuntimePath() {
        return xdgRuntimePath;
    }

    @NonNull
    public Name getName() {
        return name;
    }

    @NonNull
    public String[] getArgs() {
        return Arrays.copyOf(args, args.length);
    }

    public void start() throws IOException {
        if (shellExecutor.isRunning())
            throw new WaylandRuntimeException("Client running already.");
        if (xdgRuntimePath == null)
            throw new WaylandRuntimeException("XDG_RUNTIME has not been set.");

        List<String> command = new ArrayList<>();
        command.add(Paths.get(nativeLibraryPath, name.library).toString());
        command.addAll(Arrays.asList(args));

        shellExecutor.setCommand(command.toArray(new String[0]))
                .putEnv("WAYLAND_DEBUG", "0")
                .putEnv("XDG_RUNTIME_DIR", xdgRuntimePath)
                .putEnv("LD_LIBRARY_PATH", nativeLibraryPath)
                .exec();
    }

    public void stop() {
        shellExecutor.stop(true);
    }

    public boolean isRunning() {
        return shellExecutor.isRunning();
    }

    public void onStdOut(@Nullable Consumer<String> consumer) {
        shellExecutor.onStdOut(consumer);
    }

    public void onStdErr(@Nullable Consumer<String> consumer) {
        shellExecutor.onStdErr(consumer);
    }

    public void onExit(@NonNull Consumer<Integer> consumer) {
        shellExecutor.onExit(consumer);
    }
}
