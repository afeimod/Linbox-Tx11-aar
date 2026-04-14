package com.winfusion.core.wayland;

public class WestonInput {

    private static final long NullPtr = 0;

    private long jniHandle;

    protected WestonInput(long jniHandle) {
        this.jniHandle = jniHandle;
    }

    protected void setJniHandle(long jniHandle) {
        this.jniHandle = jniHandle;
    }

    public void performTouch(int touchId, int touchType, float x, float y) {
        performTouch(jniHandle, touchId, touchType, x, y);
    }

    public void performKey(int key, int keyState) {
        performKey(jniHandle, key, keyState);
    }

    public void performPointer(int pointerType, float x, float y) {
        performPointer(jniHandle, pointerType, x, y);
    }

    public void performButton(int button, int buttonState) {
        performButton(jniHandle, button, buttonState);
    }

    public void performAxis(int axisType, float value, boolean hasDiscrete, int discrete) {
        performAxis(jniHandle, axisType, value, hasDiscrete, discrete);
    }

    private native void performTouch(long handle, int touchId, int touchType, float x, float y);

    private native void performKey(long handle, int key, int keyState);

    private native void performPointer(long handle, int pointerType, float x, float y);

    private native void performButton(long handle, int button, int buttonState);

    private native void performAxis(long handle, int axisType, float value, boolean hasDiscrete,
                                    int discrete);
}
