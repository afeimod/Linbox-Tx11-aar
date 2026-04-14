package com.winfusion.core.mslink.exception;

public class BadLnkFormatException extends ShellLinkException {

    public BadLnkFormatException(String message) {
        super(message);
    }

    public BadLnkFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
