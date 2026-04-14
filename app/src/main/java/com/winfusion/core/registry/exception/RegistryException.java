package com.winfusion.core.registry.exception;

/**
 * 表示该模块遇到的自定义错误。
 */
public class RegistryException extends Exception {

    public RegistryException(String message) {
        super(message);
    }

    public RegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    protected RegistryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RegistryException(Throwable cause) {
        super(cause);
    }
}
