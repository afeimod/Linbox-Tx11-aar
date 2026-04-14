package com.winfusion.core.registry.exporter;

import static com.winfusion.core.registry.Constants.WINE_REGISTRY_2_HEADER;

import androidx.annotation.NonNull;

import com.winfusion.core.registry.Registry;
import com.winfusion.core.registry.RegistryExporter;
import com.winfusion.core.registry.RegistryKey;
import com.winfusion.core.registry.RegistryKeys;
import com.winfusion.core.registry.SID;
import com.winfusion.core.registry.data.DataType;
import com.winfusion.core.registry.data.RegistryData;
import com.winfusion.core.registry.exception.RegistryExporterException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class WineRegExporter extends RegistryExporter {

    private static final String REGISTRY_RELATIVE_HEADER = ";; All keys relative to REGISTRY\\\\";
    private static final String RELATIVE_TO_MACHINE = "Machine";
    private static final String RELATIVE_TO_USER_DEFAULT = "User\\\\.Default";
    private static final String RELATIVE_TO_USER = "User\\\\";

    private final char[] hexBuf = new char[8];

    /**
     * Wine注册表类型枚举
     */
    public enum RegType {
        /**
         * system.reg
         */
        System,
        /**
         * user.reg
         */
        User,
        /**
         * userdef.reg
         */
        UserDef
    }

    public WineRegExporter(@NonNull Registry registry) {
        super(registry);
    }

    /**
     * 关联 {@link #export(Writer, RegType)}
     *
     * @param path    文件路径
     * @param regType 注册表类型
     * @throws IOException               如果发生读写错误
     * @throws RegistryExporterException 如果遇到不支持的注册表数据类型。
     * @see #export(Writer, RegType)
     */
    public void export(@NonNull Path path, @NonNull RegType regType)
            throws IOException, RegistryExporterException {

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            export(writer, regType);
        }
    }

    /**
     * 导出注册表到文件。
     * 将会根据导出类型导出包含的注册表项到文件，使用 UTF-8 字符编码。
     * 传入的 Writer 对象应该由调用方负责关闭，该方法不会关闭它。
     *
     * @param writer  字符流对象
     * @param regType 注册表类型
     * @throws IOException               如果发生读写错误。
     * @throws RegistryExporterException 如果遇到不支持的注册表数据类型，
     *                                   或者导出类型为 {@link RegType#User} 但是注册表元数据 {@link Registry.MetaData} 缺少 SID 信息。
     */
    public void export(@NonNull Writer writer, @NonNull RegType regType)
            throws IOException, RegistryExporterException {

        BufferedWriter w;
        if (writer instanceof BufferedWriter)
            w = (BufferedWriter) writer;
        else
            w = new BufferedWriter(writer);

        RegistryKey root = switch (regType) {
            case System -> RegistryKeys.getHkeyLocalMachine();
            case User -> RegistryKeys.getHkeyCurrentUser();
            case UserDef -> RegistryKeys.getHkeyUsersDefault();
        };

        nodes.clear();
        findNodes(root);

        // write header
        w.write(WINE_REGISTRY_2_HEADER);
        w.newLine();

        // write registry relative index
        w.write(REGISTRY_RELATIVE_HEADER);
        if (regType == RegType.System) {
            w.write(RELATIVE_TO_MACHINE);
        } else if (regType == RegType.User) {
            SID sid = registry.getMetaData().sid;
            if (sid == null)
                throw new RegistryExporterException("Registry must have sid in meta to write user.reg");
            w.append(RELATIVE_TO_USER).append(sid.toString());
        } else {
            w.write(RELATIVE_TO_USER_DEFAULT);
        }
        w.newLine();
        w.newLine();

        // write arch
        String arch = registry.getMetaData().arch;
        if (arch == null)
            throw new RegistryExporterException("Registry must have arch in meta to write wine reg");
        w.append("#arch=").append(arch);
        w.newLine();
        w.newLine();

        // write registry items
        for (Map.Entry<RegistryKey, Registry.RegistryNode> nodeEntry : nodes.entrySet()) {
            RegistryKey key = nodeEntry.getKey().relativeTo(root);
            Registry.RegistryNode node = nodeEntry.getValue();

            // write key
            w.write('[');
            writeEscapedString(key.toString(), w);
            w.write(']');

            // write default value if exists
            if (node.defaultValue != null) {
                w.newLine();
                w.write("@=");
                writeRegistryData(node.defaultValue, w);
            }

            // write named values
            for (Map.Entry<String, RegistryData> valueEntry : node.namedValues.entrySet()) {
                w.newLine();
                w.write('"');
                writeEscapedString(valueEntry.getKey(), w);
                w.write("\"=");
                writeRegistryData(valueEntry.getValue(), w);
            }

            w.newLine();
            w.newLine();
        }

        w.flush();
    }

    private void writeRegistryData(@NonNull RegistryData data, @NonNull BufferedWriter writer)
            throws IOException, RegistryExporterException {

        DataType type = data.getDataType();
        if (type == DataType.REG_SZ) {
            writer.write('"');
            writeEscapedString(data.getAsString().getString(), writer);
            writer.write('"');
        } else if (type == DataType.REG_EXPAND_SZ) {
            writer.write("str(2):\"");
            writeEscapedString(data.getAsExpandString().getString(), writer);
            writer.write('"');
        } else if (type == DataType.REG_BINARY) {
            writer.append("hex:");
            writeBytesAsHex(data.getAsBinary().toBytes(), writer);
        } else if (type == DataType.REG_DWORD) {
            HexUtils.intToHexChars(data.getAsDword().toSignedInt(), hexBuf);
            writer.write("dword:");
            writer.write(hexBuf);
        } else if (type == DataType.REG_DWORD_BIG_ENDIAN ||
                type == DataType.REG_LINK ||
                type == DataType.REG_RESOURCE_LIST ||
                type == DataType.REG_FULL_RESOURCE_DESCRIPTOR ||
                type == DataType.REG_RESOURCE_REQUIREMENTS_LIST ||
                type == DataType.REG_QWORD) {
            int len = HexUtils.intToHexCharsN(type.getId(), hexBuf);
            writer.write("hex(");
            writer.write(hexBuf, 0, len);
            writer.write("):");
            writeBytesAsHex(data.toBytes(), writer);
        } else if (type == DataType.REG_MULTI_SZ) {
            writer.write("str(7):\"");
            String[] array = data.getAsMultiString().getStringArray();
            if (array.length == 0) {
                writer.write("\\0");
            } else {
                for (String s : array) {
                    writeEscapedString(s, writer);
                    writer.write("\\0");
                }
            }
            writer.write('"');
        } else if (type == DataType.REG_RAW) {
            int len = HexUtils.intToHexCharsN(data.getAsRaw().getId(), hexBuf);
            writer.write("hex(");
            writer.write(hexBuf, 0, len);
            writer.write("):");
            writeBytesAsHex(data.getAsRaw().toBytes(), writer);
        } else {
            throw new RegistryExporterException("Unsupported data: " + data);
        }
    }

    private void writeEscapedString(@NonNull String str, @NonNull BufferedWriter writer)
            throws IOException {

        char c;
        for (int i = 0; i < str.length(); i++) {
            c = str.charAt(i);
            if (c == '\\') {
                writer.write("\\\\");
            } else if (c == '"') {
                writer.write("\\\"");
            } else if (c <= 127) {
                writer.write(c);
            } else {
                HexUtils.charToHexChars(c, hexBuf);
                writer.write("\\x");
                writer.write(hexBuf, 0, 4);
            }
        }
    }

    private void writeBytesAsHex(@NonNull byte[] bytes, @NonNull BufferedWriter writer)
            throws IOException {

        for (int i = 0; i < bytes.length; i++) {
            HexUtils.byteToHexChars(bytes[i], hexBuf);
            writer.write(hexBuf, 0, 2);
            if (i != bytes.length - 1)
                writer.write(',');
        }
    }
}
