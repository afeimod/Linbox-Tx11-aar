package com.winfusion.core.registry.parser;

import static com.winfusion.core.registry.Constants.WINE_REGISTRY_2_HEADER;

import androidx.annotation.NonNull;

import com.winfusion.core.registry.Registry;
import com.winfusion.core.registry.RegistryKey;
import com.winfusion.core.registry.RegistryKeys;
import com.winfusion.core.registry.SID;
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
 * 注册表解析类，特定用于解析 Wine 生成的 reg 文件。
 */
public class WineRegParser extends BaseRegParser {

    private final Pattern relativePattern = Pattern.compile("^;; All keys relative to REGISTRY\\\\\\\\(.+)$");
    private final Pattern machinePattern = Pattern.compile("^Machine$");
    private final Pattern userPattern = Pattern.compile("^User\\\\(S(?:-\\d+)+)$");
    private final Pattern userdefPattern = Pattern.compile("^User\\\\.Default$");
    private final Pattern keyPattern = Pattern.compile("^\\[(.*)](?: \\d+)?$");
    private final Pattern archPattern = Pattern.compile("^#arch=(.+)$");
    private final Pattern valuePattern = Pattern.compile("^(?:\"(.+)\"|@)=(?:\"(.*)\"|(.+?):(.*))$");
    private final Pattern typePattern = Pattern.compile("^(hex|str)\\(([0-9A-Fa-f]+)\\)$");
    private RegistryKey rootKey;
    private SID sid;
    private String arch;

    @NonNull
    @Override
    public Registry parse(@NonNull BufferedReader reader)
            throws IOException, UnsupportedRegistryException, RegistrySyntaxException {

        context = new ParserContext(reader);
        sid = null;
        arch = null;

        checkHeader();
        setupRootKey();

        try {
            String line;
            while ((line = context.getNextLine()) != null)
                parseLine(line);
        } catch (RuntimeException e) {
            throw new RegistrySyntaxException(e);
        }

        Registry.MetaData metaData = context.registry.getMetaData();
        metaData.sid = sid;
        metaData.arch = arch;
        return context.registry;
    }

    private void parseLine(@NonNull String line) throws IOException, RegistrySyntaxException {
        if (line.startsWith("[")) {
            parseLineAsItemKey(unescapeString(line));
        } else if (line.startsWith("\"") || line.startsWith("@")) {
            if (context.currentItemContext == null)
                throw newSyntaxException("Expect item key before a value: " + line);
            parseLineAsValue(unescapeString(context.getFullLine(line)));
        } else if (line.startsWith("#")) {
            if (arch == null) {
                Matcher matcher = archPattern.matcher(line);
                if (matcher.matches())
                    arch = matcher.group(1);
            }
        } else if (!line.startsWith(";") && !line.isEmpty()) {
            throw newSyntaxException("Unrecognized syntax: " + line);
        }
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
        if (flatData.isString) {
            return new StringData(flatData.data);
        } else if (flatData.id.equals("hex")) {
            return new BinaryData(getBytesFromString(flatData.data));
        } else if (flatData.id.equals("dword")) {
            return parseDwordValue(flatData);
        } else if ((matcher = typePattern.matcher(flatData.id)).matches()) {
            String g2 = matcher.group(2);
            if (g2 == null)
                throw newSyntaxException("Failed to get value id");
            long numId;
            try {
                numId = Long.parseLong(g2, 16);
            } catch (NumberFormatException e) {
                throw newSyntaxException("Invalid id: " + g2);
            }
            if (numId > DataType.REG_ID_MAX)
                throw newSyntaxException("Unexpected id overflow: " + g2);

            String g1 = matcher.group(1);
            if (Objects.equals(g1, "str"))
                return parseStrValue(flatData, (int) numId);
            else if (Objects.equals(g1, "hex"))
                return parseHexValue(flatData, (int) numId);
            else
                throw newSyntaxException("Unsupported id: " + g2);
        } else {
            throw newSyntaxException("Invalid value type: " + flatData.id);
        }
    }

    private void parseLineAsItemKey(@NonNull String line) throws RegistrySyntaxException {
        Matcher matcher = keyPattern.matcher(line);
        if (!matcher.matches())
            throw newSyntaxException("Failed to get: " + line);
        String rawKey = matcher.group(1);
        if (rawKey == null)
            throw newSyntaxException("Invalid key: " + line);
        RegistryKey key;
        if (rawKey.isEmpty()) {
            key = rootKey;
        } else {
            RegistryKey k = RegistryKeys.get(rawKey);
            if (k.isAbsolute())
                throw newSyntaxException("Registry key created by wine must not be absolute: " + line);
            key = rootKey.resolve(k);
        }
        context.currentItemContext = context.registry.createItem(key);
    }

    @NonNull
    private RegistryData parseHexValue(@NonNull FlatData flatData, int numId)
            throws RegistrySyntaxException {

        DataType dataType = DataType.fromId(numId);
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
            case REG_RAW -> new RawData(numId, bytes);
        };
    }

    @NonNull
    private RegistryData parseStrValue(@NonNull FlatData flatData, int numId)
            throws RegistrySyntaxException {

        if (!flatData.data.startsWith("\"") || !flatData.data.endsWith("\""))
            throw newSyntaxException("Invalid string value: " + flatData.data);
        flatData.data = flatData.data.substring(1, flatData.data.length() - 1);
        DataType dataType = DataType.fromId(numId);
        return switch (dataType) {
            case REG_SZ -> new StringData(flatData.data);
            case REG_EXPAND_SZ -> new ExpandStringData(flatData.data);
            case REG_MULTI_SZ -> new MultiStringData(flatData.data.split("\\\\0"));
            default ->
                    throw new RegistrySyntaxException("Unsupported data as string: " + dataType);
        };
    }

    private void checkHeader()
            throws UnsupportedRegistryException, RegistrySyntaxException, IOException {

        String line = context.forceNextLine("Expect registry version but get EOF.");
        if (!Objects.equals(line, WINE_REGISTRY_2_HEADER))
            throw new UnsupportedRegistryException("Unsupported registry version: " + line);
    }

    private void setupRootKey() throws RegistrySyntaxException, IOException {
        String line = context.forceNextLine("Expect root key but get EOF.");
        Matcher matcher = relativePattern.matcher(line);
        if (!matcher.matches())
            throw newSyntaxException("Root key not found: " + line);

        String root = matcher.group(1);
        if (root == null)
            throw newSyntaxException("Failed to get root key: " + line);
        root = unescapeString(root);

        if (machinePattern.matcher(root).matches()) {
            rootKey = RegistryKeys.getHkeyLocalMachine();
        } else if ((matcher = userPattern.matcher(root)).matches()) {
            String sidStr = matcher.group(1);
            if (sidStr == null)
                throw newSyntaxException("Failed to get sid: " + root);
            try {
                sid = SID.parse(sidStr);
            } catch (SID.SIDParserException e) {
                throw newSyntaxException("Invalid sid: " + sidStr);
            }
            rootKey = RegistryKeys.getHkeyCurrentUser();
        } else if (userdefPattern.matcher(root).matches()) {
            rootKey = RegistryKeys.getHkeyUsersDefault();
        } else {
            throw newSyntaxException("Unsupported root key: " + root);
        }
    }

    @NonNull
    private String unescapeString(@NonNull String str) throws RegistrySyntaxException {
        StringBuilder builder = new StringBuilder();
        boolean escape = false;
        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (escape) {
                if (c == 'x') {
                    int len;
                    if (i + 4 < str.length())
                        len = 4;
                    else if (i + 2 < str.length())
                        len = 2;
                    else
                        throw new RegistrySyntaxException("Invalid unicode symbol: " + str);

                    String hex = str.substring(i + 1, i + len + 1);
                    char unicode;
                    try {
                        unicode = (char) Integer.parseInt(hex, 16);
                    } catch (NumberFormatException e) {
                        throw newSyntaxException("Failed to parse unicode: " + hex);
                    }
                    builder.append(unicode);
                    i = i + len;
                    escape = false;
                } else if (c == '\\') {
                    builder.append('\\');
                    escape = false;
                } else if (c == '"') {
                    builder.append('"');
                    escape = false;
                } else if (c == '0') {
                    builder.append("\\0");
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
