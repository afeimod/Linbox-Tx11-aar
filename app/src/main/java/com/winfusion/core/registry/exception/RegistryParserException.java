package com.winfusion.core.registry.exception;

/**
 * 表示解析注册表时遇到和解析相关的错误。
 */
public class RegistryParserException extends RegistryException {

    public RegistryParserException(String message) {
        super(message);
    }

    public RegistryParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public RegistryParserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RegistryParserException(Throwable cause) {
        super(cause);
    }
}
