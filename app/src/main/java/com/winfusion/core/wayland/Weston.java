package com.winfusion.core.wayland;

import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.wayland.exception.WaylandException;
import com.winfusion.core.wayland.exception.WaylandRuntimeException;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Weston {

    static {
        System.loadLibrary("winfusion");
    }

    private static final String TAG = "Weston";
    private static final long NullPtr = 0;

    private long jniHandle;
    private Future<?> displayFuture;
    private final WestonConfig westonConfig;
    private final WestonInput westonInput;
    private WestonCallback westonCallback;
    private WestonSurfaceView westonSurfaceView;

    private final Runnable pixmanRenderDone = new Runnable() {
        @Override
        public void run() {
            if (!isDestroyed())
                onPixmanRenderFinished(jniHandle);
        }
    };

    public Weston() throws WaylandException {
        jniHandle = create();

        this.westonConfig = new WestonConfig(jniHandle);
        this.westonInput = new WestonInput(jniHandle);

        if (jniHandle == NullPtr)
            throw new WaylandException("Failed to create jni handle.");
    }

    public void setWestonGLSurfaceView(@NonNull WestonSurfaceView westonSurfaceView) {
        this.westonSurfaceView = westonSurfaceView;
        Surface surface = westonSurfaceView.getHolder().getSurface();
        if (westonSurfaceView.isSurfaceReady())
            enableOutput(jniHandle, surface);
        westonSurfaceView.getHolder().addCallback(surfaceHolderCallback);
    }

    public void setCallback(@Nullable WestonCallback westonCallback) {
        this.westonCallback = westonCallback;
    }

    public void start(@NonNull ExecutorService executorService)
            throws WaylandException {

        if (jniHandle == NullPtr)
            throw new WaylandRuntimeException("Weston handle has not been created or has been destroyed.");

        if (isDisplayRunning(jniHandle))
            throw new WaylandRuntimeException("Weston display is running already.");

        initialize(jniHandle);
        displayFuture = executorService.submit(() -> {
            startDisplay(jniHandle);
            if (westonCallback != null)
                westonCallback.onDisplayTerminated();
        });
    }

    public void stop() {
        if (jniHandle == NullPtr)
            return;

        if (isDisplayRunning(jniHandle)) {
            stopDisplay(jniHandle);
            internalWaitDisplayStop();
        }
    }

    public void destroy() {
        if (jniHandle == NullPtr)
            return;

        destroy(jniHandle);
        internalWaitDisplayStop();
        jniHandle = NullPtr;
        westonConfig.setJniHandle(NullPtr);
        westonInput.setJniHandle(NullPtr);
    }

    public boolean isDisplayRunning() {
        if (jniHandle == NullPtr)
            return false;
        return isDisplayRunning(jniHandle);
    }

    public boolean isDestroyed() {
        return jniHandle == NullPtr;
    }

    @NonNull
    public WestonConfig getConfig() {
        return westonConfig;
    }

    @NonNull
    public WestonInput getInput() {
        return westonInput;
    }

    private final SurfaceHolder.Callback surfaceHolderCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            enableOutput(jniHandle, holder.getSurface());
        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

        }
    };

    private void internalWaitDisplayStop() {
        if (displayFuture != null) {
            try {
                displayFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                Log.d(TAG, "Display loop interrupted.", e);
            }

            displayFuture = null;
        }
    }

    @SuppressWarnings("unused")
    private void JNINotifyPixmanBufferUpdate(@NonNull ByteBuffer buffer, int width, int height) {
        if (westonSurfaceView != null)
            westonSurfaceView.notifyPixmanBufferUpdate(buffer, width, height, pixmanRenderDone);
    }

    private native long create();

    private native void initialize(long handle) throws WaylandException;

    private native void destroy(long handle);

    private native void startDisplay(long handle);

    private native void stopDisplay(long handle);

    private native boolean isDisplayRunning(long handle);

    private native void disableOutput(long handle);

    private native void enableOutput(long handle, @NonNull Surface surface);

    private native void onPixmanRenderFinished(long handle);
}
