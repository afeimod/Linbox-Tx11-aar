package com.winfusion.core.registry.exception;

/**
 * 表示解析注册表时遇到不支持的注册表版本。
 */
public class UnsupportedRegistryException extends RegistryParserException {

    public UnsupportedRegistryException(String message) {
        super(message);
    }
}
