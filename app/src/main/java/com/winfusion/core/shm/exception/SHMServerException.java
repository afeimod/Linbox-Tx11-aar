package com.winfusion.core.shm.exception;

public class SHMServerException extends Exception {

    public SHMServerException() {
        super();
    }

    public SHMServerException(String message) {
        super(message);
    }

    public SHMServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public SHMServerException(Throwable cause) {
        super(cause);
    }
}
