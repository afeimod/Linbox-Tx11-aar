package com.winfusion.core.registry.parser;

import androidx.annotation.NonNull;

import com.winfusion.core.registry.Registry;
import com.winfusion.core.registry.RegistryParser;
import com.winfusion.core.registry.exception.RegistryException;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * 注册表解析类，特定用于解析采用 json 数据结构的注册表文件。
 */
public class JsonRegParser extends RegistryParser {

    @NonNull
    @Override
    public Registry parse(@NonNull BufferedReader reader) throws IOException, RegistryException {
        // TODO: 实现从 json 解析
        return new Registry();
    }
}
