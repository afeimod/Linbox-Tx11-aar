package com.winfusion.core.registry.parser;

import static com.winfusion.core.registry.Constants.REGEDIT_VERSION_5_00_HEADER;

import androidx.annotation.NonNull;

import com.winfusion.core.registry.Registry;
import com.winfusion.core.registry.RegistryKey;
import com.winfusion.core.registry.RegistryKeys;
import com.winfusion.core.registry.data.BinaryData;
import com.winfusion.core.registry.data.DataType;
import com.winfusion.core.registry.data.DoubleWordData;
import com.winfusion.core.registry.data.ExpandStringData;
import com.winfusion.core.registry.data.FullResourceDescriptorData;
import com.winfusion.core.registry.data.LinkData;
import com.winfusion.core.registry.data.MultiStringData;
import com.winfusion.core.registry.data.NoneData;
import com.winfusion.core.registry.data.QuadWordData;
import com.winfusion.core.registry.data.RawData;
import com.winfusion.core.registry.data.RegistryData;
import com.winfusion.core.registry.data.ResourceListData;
import com.winfusion.core.registry.data.ResourceRequirementsListData;
import com.winfusion.core.registry.data.StringData;
import com.winfusion.core.registry.exception.RegistrySyntaxException;
import com.winfusion.core.registry.exception.UnsupportedRegistryException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 注册表解析类，特定用于解析 Windows Registry Editor 生成的 reg 文件。
 */
public class RegeditRegParser extends BaseRegParser {

    private final Pattern valuePattern = Pattern.compile("^(?:\"(.+)\"|@)=(?:\"(.*)\"|(.+?):(.+))$");
    private final Pattern hexTypePattern = Pattern.compile("^hex\\(([0-9A-Fa-f]+)\\)$");

    @NonNull
    @Override
    public Registry parse(@NonNull BufferedReader reader)
            throws IOException, UnsupportedRegistryException, RegistrySyntaxException {

        String line;
        context = new ParserContext(reader);

        line = context.forceNextLine("Expect registry version but get EOF.");
        // remove BOM first
        if (!Objects.equals(line.replace("\uFEFF", ""), REGEDIT_VERSION_5_00_HEADER))
            throw new UnsupportedRegistryException("Unsupported registry version: " + line);

        try {
            while ((line = context.getNextLine()) != null)
                parseLine(line);
        } catch (RuntimeException e) {
            throw new RegistrySyntaxException(e);
        }

        return context.registry;
    }

    private void parseLine(@NonNull String line) throws IOException, RegistrySyntaxException {
        if (line.startsWith("[")) {
            if (!line.endsWith("]"))
                throw newSyntaxException("Expect \"]\" as end, but got other one: " + line);
            parseLineAsItemKey(line);
        } else if (line.startsWith("\"") || line.startsWith("@")) {
            if (context.currentItemContext == null)
                throw newSyntaxException("Expect item key before a value: " + line);
            parseLineAsValue(context.getFullLine(line));
        } else if (!line.startsWith(";") && !line.isEmpty()) {
            throw newSyntaxException("Unrecognized syntax: " + line);
        }
    }

    private void parseLineAsItemKey(@NonNull String line) throws RegistrySyntaxException {
        String rawKey = line.substring(1, line.length() - 1);
        if (rawKey.isEmpty())
            throw newSyntaxException("Registry key must not be empty: " + line);

        RegistryKey key = RegistryKeys.get(rawKey);
        if (key.isAbsolute())
            throw newSyntaxException("Registry key created by regedit must not be absolute: " + line);
        context.currentItemContext = context.registry.createItem(RegistryKeys.root().resolve(key));
    }

    private void parseLineAsValue(@NonNull String line) throws RegistrySyntaxException {
        Matcher matcher = valuePattern.matcher(line);
        if (!matcher.matches())
            throw newSyntaxException("Unrecognized syntax: " + line);
        FlatData flatData = new FlatData();
        String v = matcher.group(2);
        if (v == null) {
            flatData.id = matcher.group(3);
            flatData.data = matcher.group(4);
        } else {
            flatData.isString = true;
            flatData.data = v;
        }
        RegistryData data = parseValue(flatData);
        String name = matcher.group(1);
        if (name == null)
            context.currentItemContext.setDefaultValue(data);
        else if (!name.isEmpty())
            context.currentItemContext.addValue(name, data);
        else
            throw newSyntaxException("Invalid value name: " + line);
    }

    @NonNull
    private RegistryData parseValue(@NonNull FlatData flatData) throws RegistrySyntaxException {
        Matcher matcher;
        if (flatData.isString)
            return new StringData(unescapeString(flatData.data));
        else if (flatData.id.equals("hex"))
            return new BinaryData(getBytesFromString(flatData.data));
        else if (flatData.id.equals("dword"))
            return parseDwordValue(flatData);
        else if ((matcher = hexTypePattern.matcher(flatData.id)).matches())
            return parseHexValue(flatData, matcher.group(1));
        else
            throw newSyntaxException("Invalid value type: " + flatData.id);
    }

    @NonNull
    private RegistryData parseHexValue(@NonNull FlatData flatData, String id)
            throws RegistrySyntaxException {

        long numId;
        try {
            numId = Long.parseLong(id, 16);
        } catch (NumberFormatException e) {
            throw newSyntaxException("Invalid id: " + id);
        }
        if (numId > DataType.REG_ID_MAX)
            throw newSyntaxException("Unexpected id overflow: " + id);

        DataType dataType = DataType.fromId((int) numId);
        byte[] bytes = getBytesFromString(flatData.data);
        return switch (dataType) {
            case REG_NONE -> new NoneData(bytes);
            case REG_SZ -> new StringData(getStringFromBytes(bytes));
            case REG_EXPAND_SZ -> new ExpandStringData(getStringFromBytes(bytes));
            case REG_BINARY -> new BinaryData(bytes);
            case REG_DWORD -> new DoubleWordData(getIntFromBytes(bytes, ByteOrder.LITTLE_ENDIAN));
            case REG_DWORD_BIG_ENDIAN ->
                    new DoubleWordData(getIntFromBytes(bytes, ByteOrder.BIG_ENDIAN));
            case REG_LINK -> new LinkData(bytes);
            case REG_MULTI_SZ -> new MultiStringData(getMultiStringFromBytes(bytes));
            case REG_RESOURCE_LIST -> new ResourceListData(bytes);
            case REG_FULL_RESOURCE_DESCRIPTOR -> new FullResourceDescriptorData(bytes);
            case REG_RESOURCE_REQUIREMENTS_LIST -> new ResourceRequirementsListData(bytes);
            case REG_QWORD -> new QuadWordData(getLongFromBytes(bytes));
            case REG_RAW -> new RawData((int) numId, bytes);
        };
    }

    @NonNull
    private String unescapeString(@NonNull String str) throws RegistrySyntaxException {
        StringBuilder builder = new StringBuilder();
        boolean escape = false;
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (escape) {
                if (c == '"') {
                    builder.append('"');
                    escape = false;
                } else if (c == '\\') {
                    builder.append('\\');
                    escape = false;
                } else {
                    throw newSyntaxException("Invalid escape symbol: " + c);
                }
            } else {
                if (c == '\\')
                    escape = true;
                else
                    builder.append(c);
            }
        }
        if (escape)
            throw newSyntaxException("Leak a escape symbol.");
        return builder.toString();
    }
}
