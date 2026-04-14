package com.winfusion.core.wayland;

import static com.winfusion.core.wayland.Constants.WESTON_RENDERER_PIXMAN;

import androidx.annotation.NonNull;

public class WestonConfig {

    private long jniHandle;

    public static final int DefaultRenderRefreshRate = 60;
    public static final int DefaultRendererType = WESTON_RENDERER_PIXMAN;
    public static final String DefaultXkbRule = "evdev";
    public static final String DefaultXkbModel = "pc105";
    public static final String DefaultXkbLayout = "us";

    protected int rendererType;
    protected int renderRefreshRate;
    protected String socketPath;
    protected String xkbConfigRootPath;
    protected String xdgRuntimePath;
    protected String xkbRule;
    protected String xkbModel;
    protected String xkbLayout;
    protected int screenWidth;
    protected int screenHeight;

    protected WestonConfig(long jniHandle) {
        this.jniHandle = jniHandle;
        setRendererType(DefaultRendererType);
        setRenderRefreshRate(DefaultRenderRefreshRate);
        setXkbRule(DefaultXkbRule);
        setXkbModel(DefaultXkbModel);
        setXkbLayout(DefaultXkbLayout);
    }

    protected void setJniHandle(long jniHandle) {
        this.jniHandle = jniHandle;
    }

    public void setRendererType(int rendererType) {
        this.rendererType = rendererType;
        nativeSetRendererType(jniHandle, rendererType);
    }

    public void setRenderRefreshRate(int renderRefreshRate) {
        this.renderRefreshRate = renderRefreshRate;
        nativeSetRendererRefreshRate(jniHandle, renderRefreshRate);
    }

    public void setSocketPath(@NonNull String socketPath) {
        this.socketPath = socketPath;
        nativeSetSocketPath(jniHandle, socketPath);
    }

    public void setXkbConfigRootPath(@NonNull String xkbConfigRootPath) {
        this.xkbConfigRootPath = xkbConfigRootPath;
        nativeSetXkbConfigRootPath(jniHandle, xkbConfigRootPath);
    }

    public void setXdgRuntimePath(@NonNull String xdgRuntimePath) {
        this.xdgRuntimePath = xdgRuntimePath;
        nativeSetXdgRuntimePath(jniHandle, xdgRuntimePath);
    }

    public void setXkbRule(@NonNull String xkbRule) {
        this.xkbRule = xkbRule;
        nativeSetXkbRule(jniHandle, xkbRule);
    }

    public void setXkbModel(@NonNull String xkbModel) {
        this.xkbModel = xkbModel;
        nativeSetXkbModel(jniHandle, xkbModel);
    }

    public void setXkbLayout(@NonNull String xkbLayout) {
        this.xkbLayout = xkbLayout;
        nativeSetXkbLayout(jniHandle, xkbLayout);
    }

    public void setScreenWidth(int width) {
        screenWidth = width;
        nativeSetScreenWidth(jniHandle, width);
    }

    public void setScreenHeight(int height) {
        screenHeight = height;
        nativeSetScreenHeight(jniHandle, height);
    }

    public int getRendererType() {
        return rendererType;
    }

    public int getRenderRefreshRate() {
        return renderRefreshRate;
    }

    public String getSocketPath() {
        return socketPath;
    }

    public String getXkbConfigRootPath() {
        return xkbConfigRootPath;
    }

    public String getXdgRuntimePath() {
        return xdgRuntimePath;
    }

    public String getXkbRule() {
        return xkbRule;
    }

    public String getXkbModel() {
        return xkbModel;
    }

    public String getXkbLayout() {
        return xkbLayout;
    }

    private native void nativeSetRendererType(long handle, int rendererType);

    private native void nativeSetRendererRefreshRate(long handle, int renderRefreshRate);

    private native void nativeSetSocketPath(long handle, @NonNull String socketPath);

    private native void nativeSetXkbConfigRootPath(long handle, @NonNull String xkbConfigRootPath);

    private native void nativeSetXdgRuntimePath(long handle, @NonNull String xdgRuntimePath);

    private native void nativeSetXkbRule(long handle, @NonNull String xdgRule);

    private native void nativeSetXkbModel(long handle, @NonNull String xdgModel);

    private native void nativeSetXkbLayout(long handle, @NonNull String xdgLayout);

    private native void nativeSetScreenWidth(long handle, int width);

    private native void nativeSetScreenHeight(long handle, int height);
}
