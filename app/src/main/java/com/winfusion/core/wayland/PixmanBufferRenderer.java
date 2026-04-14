package com.winfusion.core.wayland;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PixmanBufferRenderer implements GLSurfaceView.Renderer {

    private ByteBuffer pixmanBuffer;
    private int pixmanWidth;
    private int pixmanHeight;
    private int textureId = -1;
    private int program;
    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;
    private Runnable pixmanRenderDone;

    private static final String vertexShaderCode =
            "attribute vec4 aPosition;" +
                    "attribute vec2 aTexCoord;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "  gl_Position = aPosition;" +
                    "  vTexCoord = aTexCoord;" +
                    "}";

    private static final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D uTexture;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(uTexture, vTexCoord);" +
                    "}";

//    long lastTime;

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, pixmanWidth, pixmanHeight,
                0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixmanBuffer);

        drawFullScreenQuad();
        if (pixmanRenderDone != null)
            pixmanRenderDone.run();

//        long currentTime = System.currentTimeMillis();
//        Log.e("test", "time: " + (currentTime - lastTime) / 1000f + "s");
//        lastTime = currentTime;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        textureId = createTexture();
        initShaders();
        initQuad();
        GLES20.glClearColor(0f, 0f, 0f, 1f);
    }

    public void updatePixmanBuffer(@NonNull ByteBuffer buffer, int width, int height,
                                   @NonNull Runnable pixmanRenderDone) {

        pixmanBuffer = buffer;
        pixmanWidth = width;
        pixmanHeight = height;
        this.pixmanRenderDone = pixmanRenderDone;
    }

    private int createTexture() {
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        int textureId = textureIds[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        return textureId;
    }

    private void initShaders() {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
    }

    private int loadShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        return shader;
    }

    private void initQuad() {
        float[] vertices = {
                -1.0f, 1.0f,    // left-top
                -1.0f, -1.0f,   // left-bottom
                1.0f, 1.0f,     // right-top
                1.0f, -1.0f     // right-bottom
        };

        float[] texCoords = {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f
        };

        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(vertices).position(0);

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        texCoordBuffer.put(texCoords).position(0);
    }

    private void drawFullScreenQuad() {
        GLES20.glUseProgram(program);

        int aPosition = GLES20.glGetAttribLocation(program, "aPosition");
        int aTexCoord = GLES20.glGetAttribLocation(program, "aTexCoord");
        int uTexture = GLES20.glGetUniformLocation(program, "uTexture");

        GLES20.glEnableVertexAttribArray(aPosition);
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        GLES20.glEnableVertexAttribArray(aTexCoord);
        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(uTexture, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(aPosition);
        GLES20.glDisableVertexAttribArray(aTexCoord);
    }
}
