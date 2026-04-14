package com.winfusion.core.registry.parser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.winfusion.core.registry.Registry;
import com.winfusion.core.registry.RegistryItemContext;
import com.winfusion.core.registry.RegistryParser;
import com.winfusion.core.registry.data.DoubleWordData;
import com.winfusion.core.registry.data.RegistryData;
import com.winfusion.core.registry.exception.RegistrySyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

abstract class BaseRegParser extends RegistryParser {

    protected ParserContext context;

    @NonNull
    protected RegistrySyntaxException newSyntaxException(@NonNull String errorMsg) {
        return new RegistrySyntaxException("(line:" + context.lineCount + ") " + errorMsg);
    }

    @NonNull
    protected byte[] getBytesFromString(@NonNull String data) throws RegistrySyntaxException {
        if (data.isEmpty())
            return new byte[0];

        String[] strArray = data.split(",");
        byte[] bytes = new byte[strArray.length];
        for (int i = 0; i < strArray.length; i++) {
            int b;
            try {
                b = Integer.parseInt(strArray[i], 16);
            } catch (NumberFormatException e) {
                throw newSyntaxException("Invalid hex value: " + data);
            }
            if (b > 255)
                throw newSyntaxException("Unexpected byte overflow: " + data);
            bytes[i] = (byte) b;
        }
        return bytes;
    }

    @NonNull
    protected String getStringFromBytes(@NonNull byte[] bytes) throws RegistrySyntaxException {
        if (bytes.length % 2 != 0)
            throw newSyntaxException("Invalid hexadecimal sequence, length must be a multiple of 2.");

        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        StringBuilder builder = new StringBuilder();
        char c;
        while (byteBuffer.hasRemaining()) {
            c = byteBuffer.getChar();
            if (c == '\0') {
                if (byteBuffer.hasRemaining())
                    throw newSyntaxException("REG_SZ must not contain '\\0' in the middle.");
                break;
            }
            builder.append(c);
        }
        return builder.toString();
    }

    protected int getIntFromBytes(@NonNull byte[] bytes, ByteOrder order) throws RegistrySyntaxException {
        if (bytes.length > Integer.BYTES)
            throw newSyntaxException("Unexpected dword overflow, dword must be 32-bit.");
        if (bytes.length < Integer.BYTES) {
            byte[] newBytes = new byte[Integer.BYTES];
            if (order == ByteOrder.BIG_ENDIAN)
                System.arraycopy(bytes, 0, newBytes, Integer.BYTES - bytes.length, bytes.length);
            else if (order == ByteOrder.LITTLE_ENDIAN)
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            bytes = newBytes;
        }

        return ByteBuffer.wrap(bytes).order(order).getInt();
    }

    protected long getLongFromBytes(@NonNull byte[] bytes) throws RegistrySyntaxException {
        if (bytes.length > Long.BYTES)
            throw newSyntaxException("Unexpected qword overflow, qword must be 64-bit.");
        if (bytes.length < Long.BYTES) {
            byte[] newBytes = new byte[Long.BYTES];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            bytes = newBytes;
        }

        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getLong();
    }

    @NonNull
    protected String[] getMultiStringFromBytes(@NonNull byte[] bytes) throws RegistrySyntaxException {
        if (bytes.length % 2 != 0)
            throw newSyntaxException("Invalid hexadecimal sequence, length must be a multiple of 2.");
        if (bytes.length < 4)
            throw newSyntaxException("Hexadecimal sequence is too short.");
        for (int i = bytes.length - 4; i < bytes.length; i++) {
            if (bytes[i] != 0)
                throw newSyntaxException("REG_MULTI_SZ must have 4 zero bytes at the end.");
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, bytes.length - 2)
                .order(ByteOrder.LITTLE_ENDIAN);
        ArrayList<String> strArray = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        char c;
        while (byteBuffer.hasRemaining()) {
            c = byteBuffer.getChar();
            if (c == '\0') {
                if (builder.length() == 0)
                    throw newSyntaxException("REG_MULTI_SZ must not contain an empty string.");
                strArray.add(builder.toString());
                builder.setLength(0);
            } else {
                builder.append(c);
            }
        }
        return strArray.toArray(new String[0]);
    }

    @NonNull
    protected RegistryData parseDwordValue(@NonNull FlatData flatData) throws RegistrySyntaxException {
        long dword;
        try {
            dword = Long.parseLong(flatData.data, 16);
            if (dword > DoubleWordData.DWORD_MAX)
                throw newSyntaxException("Unexpected dword overflow: " + flatData.data);
        } catch (NumberFormatException e) {
            throw newSyntaxException("Invalid dword value: " + flatData.data);
        }
        return new DoubleWordData(dword);
    }

    protected static class ParserContext {

        public final BufferedReader reader;
        public final Registry registry = new Registry();
        public int lineCount = 0;
        public RegistryItemContext currentItemContext;

        public ParserContext(@NonNull BufferedReader reader) {
            this.reader = reader;
        }

        @Nullable
        public String getNextLine() throws IOException {
            String str = reader.readLine();
            if (str != null) {
                str = str.trim();
                lineCount++;
            }
            return str;
        }

        @NonNull
        public String forceNextLine(@Nullable String errorMsg)
                throws IOException, RegistrySyntaxException {

            String str = getNextLine();
            if (str == null)
                throw new RegistrySyntaxException(errorMsg == null ?
                        "Expect next line but got EOF" : errorMsg);
            return str;
        }

        @NonNull
        public String getFullLine(@NonNull String line) throws IOException, RegistrySyntaxException {
            if (!line.endsWith("\\"))
                return line;
            StringBuilder builder = new StringBuilder(line);
            String newLine;
            do {
                newLine = forceNextLine("Expect continuation line but got EOF.");
                builder.setLength(builder.length() - 1);
                builder.append(newLine);
            } while (newLine.endsWith("\\"));
            return builder.toString();
        }
    }

    protected static class FlatData {
        public String id;
        public String data;
        public boolean isString;
    }
}
