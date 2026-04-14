package com.winfusion.feature.setting.exception;

public class SettingException extends Exception {

    public SettingException() {
        super();
    }

    public SettingException(String message) {
        super(message);
    }

    public SettingException(String message, Throwable cause) {
        super(message, cause);
    }

    public SettingException(Throwable cause) {
        super(cause);
    }
}
