package com.winfusion.core.image.bmp.exception;

public class BadBMPFormatException extends BMPException {

    public BadBMPFormatException(String message) {
        super(message);
    }

    public BadBMPFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
