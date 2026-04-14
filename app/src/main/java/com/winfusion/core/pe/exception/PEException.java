package com.winfusion.core.pe.exception;

public class PEException extends Exception {

    public PEException() {
        super();
    }

    public PEException(String message) {
        super(message);
    }

    public PEException(String message, Throwable cause) {
        super(message, cause);
    }

    public PEException(Throwable cause) {
        super(cause);
    }
}
