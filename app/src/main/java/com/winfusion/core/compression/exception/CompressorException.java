package com.winfusion.core.compression.exception;

public class CompressorException extends Exception {

    public CompressorException() {
        super();
    }

    public CompressorException(String message) {
        super(message);
    }

    public CompressorException(String message, Throwable cause) {
        super(message, cause);
    }

    public CompressorException(Throwable cause) {
        super(cause);
    }
}
