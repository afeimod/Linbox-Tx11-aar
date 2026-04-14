package com.winfusion.core.desktop.exception;

public class DesktopException extends Exception {

    public DesktopException() {
        super();
    }

    public DesktopException(String message) {
        super(message);
    }

    public DesktopException(String message, Throwable cause) {
        super(message, cause);
    }

    public DesktopException(Throwable cause) {
        super(cause);
    }
}
