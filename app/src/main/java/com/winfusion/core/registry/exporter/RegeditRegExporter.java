package com.winfusion.core.registry.exporter;

import static com.winfusion.core.registry.Constants.REGEDIT_VERSION_5_00_HEADER;

import androidx.annotation.NonNull;

import com.winfusion.core.registry.Registry;
import com.winfusion.core.registry.RegistryExporter;
import com.winfusion.core.registry.RegistryKey;
import com.winfusion.core.registry.RegistryKeys;
import com.winfusion.core.registry.data.DataType;
import com.winfusion.core.registry.data.RegistryData;
import com.winfusion.core.registry.exception.RegistryExporterException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

public class RegeditRegExporter extends RegistryExporter {

    private final char[] hexBuf = new char[8];

    public RegeditRegExporter(@NonNull Registry registry) {
        super(registry);
    }

    /**
     * 关联 {@link #export(Writer, RegistryKey)}
     *
     * @param path   文件路径
     * @param target 目标键
     * @throws IOException               如果发生读写错误
     * @throws RegistryExporterException 如果遇到不支持的注册表数据类型。
     * @see #export(Writer, RegistryKey)
     */
    public void export(@NonNull Path path, @NonNull RegistryKey target)
            throws IOException, RegistryExporterException {

        try (OutputStream out = Files.newOutputStream(path);
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(out, StandardCharsets.UTF_16LE))) {

            // write BOM
            out.write(0xFF);
            out.write(0xFE);

            export(writer, target);
        }
    }

    /**
     * 导出注册表到文件。
     * 将会导出目标键的注册表项以及它的全部子项，使用 UTF-16LE BOM 字符编码。
     * 传入的 Writer 对象应该由调用方负责关闭，该方法不会关闭它。
     *
     * @param writer 字符流对象
     * @param target 目标键
     * @throws IOException               如果发生读写错误
     * @throws RegistryExporterException 如果遇到不支持的注册表数据类型
     */
    public void export(@NonNull Writer writer, @NonNull RegistryKey target)
            throws IOException, RegistryExporterException {

        BufferedWriter w;
        if (writer instanceof BufferedWriter)
            w = (BufferedWriter) writer;
        else
            w = new BufferedWriter(writer);

        if (!Objects.equals(target, RegistryKeys.getHkeyLocalMachine()) &&
                !target.isChildOf(RegistryKeys.getHkeyLocalMachine()) &&
                !Objects.equals(target, RegistryKeys.getHkeyCurrentUser()) &&
                !target.isChildOf(RegistryKeys.getHkeyCurrentUser()) &&
                !Objects.equals(target, RegistryKeys.getHkeyUsersDefault()) &&
                !target.isChildOf(RegistryKeys.getHkeyUsersDefault()))
            throw new RegistryExporterException("Target must be child of a hkey.");

        nodes.clear();
        findNodes(target.isAbsolute() ? target : RegistryKeys.root().resolve(target));

        // write header
        w.write(REGEDIT_VERSION_5_00_HEADER);
        w.newLine();
        w.newLine();

        // write registry items
        for (Map.Entry<RegistryKey, Registry.RegistryNode> nodeEntry : nodes.entrySet()) {
            RegistryKey key = nodeEntry.getKey().relativeTo(RegistryKeys.root());
            Registry.RegistryNode node = nodeEntry.getValue();

            // write key
            w.append('[').append(key.toString()).append(']');

            // write default value if exists
            if (node.defaultValue != null) {
                w.newLine();
                w.write("@=");
                writeRegistryData(node.defaultValue, w);
            }

            // write named values
            for (Map.Entry<String, RegistryData> valueEntry : node.namedValues.entrySet()) {
                w.newLine();
                w.append('"').append(valueEntry.getKey()).append("\"=");
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
            writer.append('"').append(data.getAsString().getString()).append('"');
        } else if (type == DataType.REG_BINARY) {
            writer.append("hex:");
            writeBytesAsHex(data.getAsBinary().toBytes(), writer);
        } else if (type == DataType.REG_DWORD) {
            HexUtils.intToHexChars(data.getAsDword().toSignedInt(), hexBuf);
            writer.write("dword:");
            writer.write(hexBuf);
        } else if (type == DataType.REG_EXPAND_SZ ||
                type == DataType.REG_DWORD_BIG_ENDIAN ||
                type == DataType.REG_LINK ||
                type == DataType.REG_MULTI_SZ ||
                type == DataType.REG_RESOURCE_LIST ||
                type == DataType.REG_FULL_RESOURCE_DESCRIPTOR ||
                type == DataType.REG_RESOURCE_REQUIREMENTS_LIST ||
                type == DataType.REG_QWORD) {
            int len = HexUtils.intToHexCharsN(type.getId(), hexBuf);
            writer.write("hex(");
            writer.write(hexBuf, 0, len);
            writer.write("):");
            writeBytesAsHex(data.toBytes(), writer);
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
