package com.winfusion.core.registry.exception;

/**
 * 表示解析注册表时遇到语法错误。
 */
public class RegistrySyntaxException extends RegistryParserException {

    public RegistrySyntaxException(String message) {
        super(message);
    }

    public RegistrySyntaxException(Throwable cause) {
        super(cause);
    }
}
