package com.winfusion.core.wayland;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

public class WestonSurfaceView extends GLSurfaceView {

    private PixmanBufferRenderer pixmanRenderer;
    private boolean surfaceReady = false;

    public WestonSurfaceView(Context context) {
        super(context);
        init();
    }

    public WestonSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void notifyPixmanBufferUpdate(@NonNull ByteBuffer pixmanBuffer, int width, int height,
                                         @NonNull Runnable pixmanRenderDone) {

        pixmanRenderer.updatePixmanBuffer(pixmanBuffer, width, height, pixmanRenderDone);
        requestRender();
    }

    public boolean isSurfaceReady() {
        return surfaceReady;
    }

    private void init() {
        pixmanRenderer = new PixmanBufferRenderer();
        setEGLContextClientVersion(2);
        setRenderer(pixmanRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                surfaceReady = true;
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                surfaceReady = false;
            }
        });
    }
}
