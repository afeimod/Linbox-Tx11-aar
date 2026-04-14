package com.winfusion.feature.setting.key;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * 预设的设置项类，包含一些有预设值的设置项枚举。
 */
public final class SettingOptions {

    private SettingOptions() {

    }

    /**
     * 显示方向设置项枚举。
     */
    public enum DisplayOrientation implements Option<String> {
        Landscape("landscape"),
        Portrait("portrait");

        private final String v;

        DisplayOrientation(@NonNull String v) {
            this.v = v;
        }

        @NonNull
        @Override
        public String option() {
            return v;
        }

        @NonNull
        public static DisplayOrientation from(@NonNull String v) {
            for (DisplayOrientation o : DisplayOrientation.values())
                if (Objects.equals(o.v, v))
                    return o;
            throw new IllegalArgumentException("Not enum of DisplayOrientation: " + v);
        }
    }

    /**
     * 显示缩放模式设置项枚举。
     */
    public enum DisplayScalingMode implements Option<String> {
        Fit("fit"),
        Original("original"),
        Stretch("stretch");

        private final String v;

        DisplayScalingMode(@NonNull String v) {
            this.v = v;
        }

        @NonNull
        @Override
        public String option() {
            return v;
        }

        @NonNull
        public static DisplayScalingMode from(@NonNull String v) {
            for (DisplayScalingMode o : DisplayScalingMode.values())
                if (Objects.equals(o.v, v))
                    return o;
            throw new IllegalArgumentException("Not enum of DisplayScalingMode: " + v);
        }
    }

    /**
     * 显示渲染后端设置项枚举。
     */
    public enum DisplayRendererBackend implements Option<String> {
        PixmanRenderer("pixman"),
        OpenGLESRenderer("gles");

        private final String v;

        DisplayRendererBackend(@NonNull String v) {
            this.v = v;
        }

        @NonNull
        @Override
        public String option() {
            return v;
        }

        @NonNull
        public static DisplayRendererBackend from(@NonNull String v) {
            for (DisplayRendererBackend o : DisplayRendererBackend.values())
                if (Objects.equals(o.v, v))
                    return o;
            throw new IllegalArgumentException("Not enum of DisplayRendererBackend: " + v);
        }
    }

    /**
     * 音频驱动设置项枚举。
     */
    public enum AudioDriver implements Option<String> {
        ALSA("alsa"),
        PulseAudio("pulse");

        private final String v;

        AudioDriver(@NonNull String v) {
            this.v = v;
        }

        @NonNull
        @Override
        public String option() {
            return v;
        }

        @NonNull
        public static AudioDriver from(@NonNull String v) {
            for (AudioDriver o : AudioDriver.values())
                if (Objects.equals(o.v, v))
                    return o;
            throw new IllegalArgumentException("Not enum of AudioDriver: " + v);
        }
    }

    /**
     * 音频后端设置项枚举。
     */
    public enum AudioBackend implements Option<String> {
        OpenSL("opensl"),
        AAudio("aaudio");

        private final String v;

        AudioBackend(@NonNull String v) {
            this.v = v;
        }

        @NonNull
        @Override
        public String option() {
            return v;
        }

        @NonNull
        public static AudioBackend from(@NonNull String v) {
            for (AudioBackend o : AudioBackend.values())
                if (Objects.equals(o.v, v))
                    return o;
            throw new IllegalArgumentException("Not enum of AudioBackend: " + v);
        }
    }
}
