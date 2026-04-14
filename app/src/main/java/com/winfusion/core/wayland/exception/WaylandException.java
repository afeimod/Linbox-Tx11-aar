package com.winfusion.core.wayland.exception;

public class WaylandException extends Exception {

    public WaylandException() {
        super();
    }

    public WaylandException(String message) {
        super(message);
    }

    public WaylandException(String message, Throwable cause) {
        super(message, cause);
    }

    public WaylandException(Throwable cause) {
        super(cause);
    }
}
