package com.winfusion.feature.setting.exception;

public class BadConfigFileFormatException extends SettingException {

    public BadConfigFileFormatException(String message) {
        super(message);
    }

    public BadConfigFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
