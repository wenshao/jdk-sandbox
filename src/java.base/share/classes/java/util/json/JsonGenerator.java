/*
 * Copyright (c) 2025, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2025, Alibaba Group Holding Limited. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.util.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import jdk.internal.access.JavaLangAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.util.DecimalDigits;

/**
 * Generator for JSON text.
 */
public class JsonGenerator implements AutoCloseable {
    /**
     * JLA
     */
    private static final JavaLangAccess JLA = SharedSecrets.getJavaLangAccess();

    static final long ESCAPED_MASK;
    static final byte PRETTY_NON = 0, PRETTY_TAB = 1, PRETTY_2_SPACE = 2, PRETTY_4_SPACE = 4;
    static final char[] DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    static final short[] ESCAPED_CHARS;
    static {
        long mask = 0;
        char[] escaped_chars = {'"', '\n', '\r', '\f', '\b', '\t'};
        for (char ch : escaped_chars) {
            mask |= (1L << ch);
        }
        ESCAPED_MASK = mask;

        char slash = '\\';
        short[] shorts = new short[128];
        shorts['\\'] = (short) (slash | ('\\' << 8));
        shorts['"'] = (short) (slash | ('"' << 8));
        shorts['\n'] = (short) (slash | ('n' << 8));
        shorts['\r'] = (short) (slash | ('r' << 8));
        shorts['\f'] = (short) (slash | ('f' << 8));
        shorts['\b'] = (short) (slash | ('b' << 8));
        shorts['\t'] = (short) (slash | ('t' << 8));
        ESCAPED_CHARS = shorts;
    }

    /**
     * The default maximum level.
     */
    static final int DEFAULT_MAX_LEVEL = 1024;

    /**
     * The default maximum buffer size.
     */
    static final int DEFAULT_MAX_BUF_SIZE = 1024 * 1024 * 64;
    /**
     * Whether the object is currently in the Start state and no KeyValue is written.
     */
    protected boolean startObject;
    /**
     * The buffer used in the generation process
     */
    byte[] bytes;
    /**
     * The current offset in the buffer.
     */
    protected int off;
    /**
     * The current level of nesting.
     */
    protected int level;
    /**
     * The maxLevel of the generator.
     */
    protected int maxLevel = DEFAULT_MAX_LEVEL;
    /**
     * Whether to format the output
     */
    protected byte pretty;
    /**
     * The features of the generator.
     */
    protected long features;
    /**
     * The maximum size of the array.
     */
    final int maxArraySize;

    /**
     * Constructs a JsonGenerator with the specified buffer size.
     * @param bufSize
     */
    JsonGenerator(int bufSize) {
        this.maxArraySize = DEFAULT_MAX_BUF_SIZE;
        this.bytes = allocate(bufSize);
    }

    /**
     *  Allocate a new byte array.
     * @param buffSize the buffer size
     * @return the new byte array
     */
    protected byte[] allocate(int buffSize) {
        return new byte[buffSize];
    }

    /**
     * Put a character into the buffer.
     */
    void putChar(byte[] str, int off, int ch) {
        str[off] = (byte) ch;
    }

    /**
     * getChars
     * @param i the int value
     * @param index the index
     * @param buf the buf
     */
    void getChars(int i, int index, byte[] buf) {
        DecimalDigits.getCharsLatin1(i, index, buf);
    }

    /**
     * getChars
     * @param i the long value
     * @param index the index
     * @param buf the buf
     */
    void getChars(long i, int index, byte[] buf) {
        DecimalDigits.getCharsLatin1(i, index, buf);
    }

    /**
     * write indent
     * @param str the string bytes
     * @param off the offset
     * @return
     */
    final int indent(byte[] str, int off) {
        putChar(str, off, '\n');
        int toIndex = off + 1 + pretty * level;
        int space = pretty == PRETTY_TAB ? '\t' : ' ';
        for (int i = off + 1; i < toIndex; i++) {
            putChar(str, i, space);
        }
        return toIndex;
    }

    /**
     * start an object
     */
    public final void startObject() {
        if (++level > maxLevel) {
            overflowLevel();
        }

        startObject = true;

        int off = this.off;
        int minCapacity = off + 3 + pretty * level;
        byte[] bytes = ensureCapacity(minCapacity);
        putChar(bytes, off++,  '{');

        if (pretty != PRETTY_NON) {
            off = indent(bytes, off);
        }
        this.off = off;
    }

    /**
     * end an object
     */
    public final void endObject() {
        level--;
        int off = this.off;
        int minCapacity = off + 1 + (pretty == 0 ? 0 : pretty * level + 1);
        byte[] bytes = ensureCapacity(minCapacity);
        if (pretty != PRETTY_NON) {
            off = indent(bytes, off);
        }

        putChar(bytes, off,  '}');
        this.off = off + 1;
        startObject = false;
    }

    /**
     * start an array
     */
    public final void startArray() {
        if (++level > maxLevel) {
            overflowLevel();
        }

        int off = this.off;
        int minCapacity = off + 3 + pretty * level;
        byte[] bytes = ensureCapacity(minCapacity);
        putChar(bytes, off++,  '[');
        if (pretty != PRETTY_NON) {
            off = indent(bytes, off);
        }
        this.off = off;
    }

    /**
     * write a comma
     */
    public final void writeComma() {
        startObject = false;
        int off = this.off;
        int minCapacity = off + 2 + pretty * level;
        byte[] bytes = ensureCapacity(minCapacity);
        putChar(bytes, off++,  ',');
        if (pretty != PRETTY_NON) {
            off = indent(bytes, off);
        }
        this.off = off;
    }

    /**
     * write a name
     * @param name the name
     */
    public final void writeName(String name) {
        if (startObject) {
            startObject = false;
        } else {
            writeComma();
        }
        writeString(name);
        writeColon();
    }

    /**
     * get the string length of the byte array
     * @param bytes the string bytes
     * @return the string length of the byte array
     */
    int length(byte[] bytes) {
        return bytes.length;
    }

    /**
     * Returns the value that ensures the capacity
     * @param minCapacity the minimum capacity
     * @return the value that ensures the capacity
     */
    protected byte[] ensureCapacity(int minCapacity) {
        byte[] bytes = this.bytes;
        if (minCapacity > length(bytes)) {
            bytes = grow(minCapacity);
        }
        return bytes;
    }

    /**
     * writeColon
     */
    public final void writeColon() {
        int off = this.off;
        byte[] bytes = ensureCapacity(off + pretty != PRETTY_NON ? 1 : 3);
        if (pretty != PRETTY_NON) {
            putChar(bytes, off, ' ');
            putChar(bytes, off + 1, ':');
            putChar(bytes, off + 2, ' ');
            off += 3;
        } else {
            putChar(bytes, off++,  ':');
        }
        this.off = off;
    }

    /**
     * endArray
     */
    public final void endArray() {
        level--;
        int off = this.off;
        byte[] bytes = ensureCapacity(off + 1 + (pretty == 0 ? 0 : pretty * level + 1));
        if (pretty != PRETTY_NON) {
            off = indent(bytes, off);
        }
        putChar(bytes, off,  ']');
        this.off = off + 1;
        startObject = false;
    }

    /**
     * writeRaw
     * @param c0 the first character
     * @param c1 the second character
     */
    final void writeRaw(char c0, char c1) {
        if (c0 > 128 || c1 > 128) {
            throw new JsonGenerateException("not support " + c0 + ", " + c1);
        }
        int off = this.off;
        byte[] bytes = ensureCapacity(off + 2);
        putChar(bytes, off,  c0);
        putChar(bytes, off + 1,  c1);
        this.off = off + 2;
    }

    /**
     * writeNull
     */
    public final void writeNull() {
        int off = this.off;
        byte[] bytes = ensureCapacity(off + 4);
        putChar(bytes, off,  'n');
        putChar(bytes, off + 1,  'u');
        putChar(bytes, off + 2,  'l');
        putChar(bytes, off + 3,  'l');
        this.off = off + 4;
    }

    /**
     * writeInt
     * @param i the value
     */
    public final void writeInt(int i) {
        int stringSize = DecimalDigits.stringSize(i);
        boolean writeAsString = isEnabled(Feature.WriteNonStringValueAsString);

        int off = this.off;
        int minCapacity = off + stringSize;
        if (writeAsString) {
            minCapacity += 2;
        }
        byte[] bytes = ensureCapacity(minCapacity);

        if (writeAsString) {
            putChar(bytes, off++,  '"');
        }
        off += stringSize;
        getChars(i, off, bytes);
        if (writeAsString) {
            putChar(bytes, off++,  '"');
        }
        this.off = off;
    }

    /**
     *  writeLong
     * @param i the value
     */
    public final void writeLong(long i) {
        int off = this.off;
        int stringSize = DecimalDigits.stringSize(i);
        boolean writeAsString = isEnabled(Feature.WriteNonStringValueAsString);
        int minCapacity = off + stringSize;
        if (writeAsString) {
            minCapacity += 2;
        }
        byte[] bytes = ensureCapacity(minCapacity);
        if (writeAsString) {
            putChar(bytes, off++,  '"');
        }
        off += stringSize;
        getChars(i, off, bytes);
        if (writeAsString) {
            putChar(bytes, off++,  '"');
        }
        this.off = off;
    }

    /**
     * writeDecimal
     * @param value the value
     */
    public final void writeDecimal(BigDecimal value) {
        if (value == null) {
            writeNull();
            return;
        }
        writeRaw(
                isEnabled(Feature.WriteNonStringValueAsString)
                        ? value.toPlainString()
                        : value.toString(),
                isEnabled(Feature.WriteNonStringValueAsString));
    }

    /**
     * writeBigInteger
     * @param value the value
     */
    public final void writeBigInteger(BigInteger value) {
        if (value == null) {
            writeNull();
            return;
        }
        writeRaw(value.toString(), isEnabled(Feature.WriteNonStringValueAsString));
    }

    /**
     * writeFloat
     * @param value the value
     */
    public final void writeFloat(float value) {
        writeRaw(Float.toString(value), isEnabled(Feature.WriteNonStringValueAsString));
    }

    /**
     * writeDouble
     * @param value the value
     */
    public final void writeDouble(double value) {
        writeRaw(Double.toString(value), isEnabled(Feature.WriteNonStringValueAsString));
    }

    /**
     * write raw string value
     * @param str the string value
     * @param quote whether to quote the string value
     */
    private void writeRaw(String str, boolean quote) {
        int off = this.off;
        int strlen = str.length();
        int minCapacity = off + strlen;
        if (quote) {
            minCapacity += 2;
        }
        byte[] bytes = ensureCapacity(minCapacity);
        if (quote) {
            putChar(bytes, off++,  '"');
        }
        writeRaw(bytes, off, str);
        off += strlen;
        if (quote) {
            putChar(bytes, off++,  '"');
        }
        this.off = off;
    }

    /**
     *  write string value to bytes
     * @param bytes the bytes write to
     * @param off the offset
     * @param str the string
     */
    @SuppressWarnings("deprecation")
    void writeRaw(byte[] bytes, int off, String str) {
        str.getBytes(0, str.length(), bytes, off);
    }

    /**
     * write string value
     * @param value the value
     */
    public void writeString(String value) {
        int count = JLA.countPositives(value);
        int str_len = value.length();
        if (count != 0) {
            writeString0(value, 0, count);
        }
        if (count != str_len) {
            writeString1(value, count, str_len - count);
        }
    }

    /**
     * write string value
     * @param value the value
     * @param coff the offset
     * @param str_len the length
     */
    protected final void writeString0(String value, int coff, int str_len) {
        int off = this.off;
        byte[] bytes = ensureCapacity(off + str_len * 6 + 2);
        putChar(bytes, off++,  '"');
        while (coff < str_len) {
            char c = value.charAt(coff++);
            if (isEscaped(c)) {
                writeEscapedChar(bytes, off, c);
                off += 2;
            } else if (c < 32) {
                writeU4Hex2(bytes, off, c);
                off += 6;
            } else {
                putChar(bytes, off++, c);
            }
        }

        putChar(bytes, off, '"');
        this.off = off + 1;
    }

    /**
     * write latin1 string value
     * @param value the value
     * @param coff the offset
     * @param str_len the length
     */
    protected final void writeString1(String value, int coff, int str_len) {
        int off = this.off;
        byte[] bytes = ensureCapacity(off + str_len * 6 + 2);
        putChar(bytes, off++,  '"');
        while (coff < str_len) {
            char c = value.charAt(coff++);
            if (c < 0x80) {
                if (isEscaped(c)) {
                    writeEscapedChar(bytes, off, c);
                    off += 2;
                } else if (c < 32) {
                    writeU4Hex2(bytes, off, c);
                    off += 6;
                } else {
                    putChar(bytes, off++, c);
                }
            } else {
                if (c < 0x800) {
                    // 2 bytes, 11 bits
                    putChar(bytes, off, (byte) (0xc0 | (c >> 6)));
                    putChar(bytes, off + 1, (byte) (0x80 | (c & 0x3f)));
                    off += 2;
                } else if (Character.isSurrogate(c)) {
                    final int uc;
                    if (Character.isHighSurrogate(c)) {
                        if (coff + 1 > str_len) {
                            uc = -1;
                        } else {
                            char d = value.charAt(coff);
                            if (Character.isLowSurrogate(d)) {
                                coff++;
                                uc = Character.toCodePoint(c, d);
                            } else {
                                bytes[off++] = (byte) '?';
                                continue;
                            }
                        }
                    } else {
                        putChar(bytes, off++, '?');
                        continue;
                    }

                    if (uc < 0) {
                        bytes[off++] = (byte) '?';
                    } else {
                        putChar(bytes, off, (byte) (0xf0 | ((uc >> 18))));
                        putChar(bytes, off + 1, (byte) (0x80 | ((uc >> 12) & 0x3f)));
                        putChar(bytes, off + 2, (byte) (0x80 | ((uc >> 6) & 0x3f)));
                        putChar(bytes, off + 3, (byte) (0x80 | (uc & 0x3f)));
                        off += 4;
                    }
                } else {
                    // 3 bytes, 16 bits
                    putChar(bytes, off, (byte) (0xe0 | ((c >> 12))));
                    putChar(bytes, off + 1, (byte) (0x80 | ((c >> 6) & 0x3f)));
                    putChar(bytes, off + 2, (byte) (0x80 | (c & 0x3f)));
                    off += 3;
                }
            }
        }

        putChar(bytes, off, '"');
        this.off = off + 1;
    }

    /**
     * write escaped char
     * @param bytes the bytes
     * @param offset the offset
     * @param ch the char
     */
    final void writeEscapedChar(byte[] bytes, int offset, int ch) {
        int packed = ESCAPED_CHARS[ch & 0x7f];
        putChar(bytes, offset    , (byte) (packed & 0xFF));
        putChar(bytes, offset + 1, (byte) (packed >> 8));
    }

    final void writeU4Hex2(byte[] bytes, int off, int ch) {
        putChar(bytes, off    , '\\');
        putChar(bytes, off + 1, 'u');
        putChar(bytes, off + 2, '0');
        putChar(bytes, off + 3, '0');
        putChar(bytes, off + 4, DIGITS[(ch >>> 4) & 15]);
        putChar(bytes, off + 5, DIGITS[ch & 15]);
    }

    final byte[] grow(int minCapacity) {
        grow0(minCapacity);
        return bytes;
    }

    void grow0(int minCapacity) {
        // minCapacity is usually close to size, so this is a win:
        bytes = Arrays.copyOf(bytes, newCapacity(minCapacity, length(bytes)));
    }

    final int newCapacity(int minCapacity, int oldCapacity) {
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        if (newCapacity > maxArraySize) {
            if (minCapacity < maxArraySize) {
                newCapacity = maxArraySize;
            } else {
                throw new OutOfMemoryError("try enabling LargeObject feature instead");
            }
        }
        return newCapacity;
    }

    /**
     *  write boolean value
     * @param value the value
     */
    public final void writeBoolean(boolean value) {
        int off = this.off;

        long features = this.features;
        boolean writeAsNumber = Feature.WriteBooleanAsNumber.isEnabled(features);
        if (writeAsNumber) {
            byte[] bytes = ensureCapacity(off + 1);
            putChar(bytes, off++,  value ? '1' : '0');
        } else {
            byte[] bytes = ensureCapacity(off + (value ? 4 : 5));
            if (value) {
                putChar(bytes, off    ,  't');
                putChar(bytes, off + 1,  'r');
                putChar(bytes, off + 2,  'u');
                putChar(bytes, off + 3,  'e');
                off += 4;
            } else {
                putChar(bytes, off    ,  'f');
                putChar(bytes, off + 1,  'a');
                putChar(bytes, off + 2,  'l');
                putChar(bytes, off + 3,  's');
                putChar(bytes, off + 4,  'e');
                off += 5;
            }
        }
        this.off = off;
    }

    /**
     * write any value
     * @param value the value
     */
    @SuppressWarnings("rawtypes")
    public final void writeAny(Object value) {
        switch (value) {
            case null -> writeNull();
            case String v -> writeString(v);
            case Byte v -> writeInt(v);
            case Short v -> writeInt(v);
            case Integer v -> writeInt(v);
            case Long v -> writeLong(v);
            case Boolean v -> writeBoolean(v);
            case Character v -> writeString(v.toString());
            case Map v -> writeObject(v);
            case Collection v -> writeArray(v);
            case BigDecimal v -> writeDecimal(v);
            case BigInteger v -> writeBigInteger(v);
            case Float v -> writeFloat(v);
            case Double v -> writeDouble(v);
            default -> throw new JsonGenerateException("not support type " + value.getClass());
        }
    }

    /**
     * write array
     * @param collection the value
     */
    @SuppressWarnings("rawtypes")
    public final void writeArray(Collection collection) {
        if (collection == null) {
            writeNull();
            return;
        }

        if (collection.isEmpty()) {
            writeRaw('[', ']');
            return;
        }

        startArray();
        boolean first = true;
        for (Object item : collection) {
            if (!first) {
                writeComma();
            }
            writeAny(item);
            first = false;
        }
        endArray();
    }

    /**
     * write object
     * @param map the object value
     */
    @SuppressWarnings("rawtypes")
    public final void writeObject(Map<?, ?> map) {
        if (map == null) {
            writeNull();
            return;
        }

        if (map.isEmpty()) {
            writeRaw('{', '}');
            return;
        }

        startObject();

        boolean writeMapNullValue = isEnabled(Feature.WriteMapNullValue);
        for (Map.Entry entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value == null && writeMapNullValue) {
                continue;
            }
            Object key = entry.getKey();
            if (key instanceof String strKey) {
                writeName(strKey);
            } else {
                writeNonStringKey(key);
            }
            writeAny(value);
        }

        endObject();
    }

    /**
     * write non-string key
     * @param key the key
     */
    protected void writeNonStringKey(Object key) {
        throw new JsonGenerateException("not support non-string key" + key);
    }

    /**
     * create a JsonGenerator
     * @return a JsonGenerator
     */
    public static JsonGenerator of() {
        return new JsonGenerator(1024);
    }

    /**
     * create a JsonGenerator with UTF16 charset
     * @param features the features
     * @return a JsonGenerator
     */
    public static JsonGenerator ofUTF16(Feature... features) {
        JsonGeneratorUTF16 generator = new JsonGeneratorUTF16(1024);
        generator.config(features);
        return generator;
    }

    /**
     * close the generator
     */
    @Override
    public void close() {
    }

    /**
     * get the bytes
     * @return the json string bytes
     */
    public byte[] getBytes() {
        return Arrays.copyOf(bytes, off);
    }

    /**
     * get the bytes
     * @param charset the charset
     * @return the json string bytes
     */
    public byte[] getBytes(Charset charset) {
        if (charset == StandardCharsets.UTF_8) {
            return getBytes();
        }
        return toString().getBytes(charset);
    }

    /**
     * get the json string
     * @return the json string
     */
    public String toString() {
        return new String(bytes, 0, off, StandardCharsets.UTF_8);
    }

    /**
     * check the feature is enabled
     * @param feature the feature
     * @return true if enabled
     */
    public final boolean isEnabled(Feature feature) {
        return feature.isEnabled(features);
    }

    /**
     * config the feature
     * @param features the features
     */
    public final void config(Feature... features) {
        for (Feature feature : features) {
            config(feature, true);
        }
    }

    /**
     * config the feature
     * @param feature the value
     * @param state the state
     */
    public final void config(Feature feature, boolean state) {
        long features = this.features;
        if (state) {
            features |= feature.mask;
        } else {
            features &= ~feature.mask;
        }
        if ((features & Feature.PrettyFormatWith4Space.mask) != 0) {
            pretty = PRETTY_4_SPACE;
        } else if ((features & Feature.PrettyFormatWith2Space.mask) != 0) {
            pretty = PRETTY_2_SPACE;
        } else if ((features & Feature.PrettyFormat.mask) != 0) {
            pretty = PRETTY_TAB;
        } else {
            pretty = PRETTY_NON;
        }
        this.features = features;
    }

    /**
     * generator feature
     */
    public enum Feature {
        /**
         * write null value
         */
        WriteMapNullValue(1),
        /**
         * write non-string value as string
         */
        WriteNonStringValueAsString(1 << 1),
        /**
         * write big decimal as plain
         */
        WriteBigDecimalAsPlain(1 << 2),
        /**
         * write boolean as number
         */
        WriteBooleanAsNumber(1 << 3),
        /**
         * pretty format
         */
        PrettyFormat(1 << 4),
        /**
         * pretty format with 2 space
         */
        PrettyFormatWith2Space(1 << 5),
        /**
         * pretty format with 4 space
         */
        PrettyFormatWith4Space(1 << 6);
        private final long mask;
        Feature(long mask) {
            this.mask = mask;
        }

        /**
         * check the feature is enabled
         * @param features the features
         * @return true if enabled
         */
        public final boolean isEnabled(long features) {
            return (features & mask) != 0;
        }
    }

    /**
     * throw exception if level too large
     */
    protected final void overflowLevel() {
        throw new JsonGenerateException("level too large : " + level);
    }

    /**
     * check the char is escaped
     * @param c the char
     * @return true if escaped
     */
    static boolean isEscaped(char c) {
        return (c == '\\') | (c < 64) & (((1L << c) & ESCAPED_MASK) != 0);
    }
}
