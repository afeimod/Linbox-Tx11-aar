package com.winfusion.core.wayland.exception;

public class WaylandRuntimeException extends RuntimeException {

    public WaylandRuntimeException() {
        super();
    }

    public WaylandRuntimeException(String message) {
        super(message);
    }

    public WaylandRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public WaylandRuntimeException(Throwable cause) {
        super(cause);
    }
}
