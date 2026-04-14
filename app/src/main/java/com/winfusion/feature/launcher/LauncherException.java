package com.winfusion.feature.launcher;

public class LauncherException extends Exception{

    public LauncherException(String message) {
        super(message);
    }

    public LauncherException(Throwable cause) {
        super(cause);
    }

    public LauncherException(String message, Throwable cause) {
        super(message, cause);
    }
}
