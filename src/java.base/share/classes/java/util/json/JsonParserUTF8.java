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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import jdk.internal.util.ByteArray;

final class JsonParserUTF8
        extends JsonParser {
    private final byte[] bytes;

    JsonParserUTF8(byte[] bytes, int off, int length) {
        super(off, length);
        this.bytes = bytes;
        next();
    }

    private void valueEnd(byte[] str, int offset) {
        int ch = offset == end ? EOI : str[offset++];
        while (isWhitSpace(ch)) {
            ch = offset == end ? EOI : str[offset++];
        }
        if (comma = ch == ',') {
            ch = offset == end ? EOI :str[offset++];
            while (isWhitSpace(ch)) {
                ch = offset == end ? EOI : str[offset++];
            }
        }
        if (ch < 0) {
            char_utf8(ch, offset);
            return;
        }
        this.ch = (char) ch;
        this.offset = offset;
    }

    public boolean nextIfMatch(char e) {
        final byte[] bytes = this.bytes;
        int offset = this.offset;
        int ch = this.ch;
        while (isWhitSpace(ch)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch != e) {
            return false;
        }

        ch = offset == end ? EOI : bytes[offset++];
        while (isWhitSpace(ch)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (ch < 0) {
            char_utf8(ch, offset);
            return true;
        }

        this.offset = offset;
        this.ch = (char) ch;
        return true;
    }

    private void char_utf8(int ch, int offset) {
        final byte[] bytes = this.bytes;
        switch ((ch & 0xFF) >> 4) {
            case 12:
            case 13: {
                /* 110x xxxx   10xx xxxx*/
                ch = char2_utf8(ch & 0xFF, bytes[offset++], offset);
                break;
            }
            case 14: {
                /* 1110 xxxx  10xx xxxx  10xx xxxx */
                ch = char2_utf8(ch & 0xFF, bytes[offset], bytes[offset + 1], offset);
                offset += 2;
                break;
            }
            default: {
                if ((ch >> 3) == -2) {
                    int c2 = bytes[offset];
                    int c3 = bytes[offset + 1];
                    int c4 = bytes[offset + 2];
                    ch = ((ch << 18) ^
                            (c2 << 12) ^
                            (c3 << 6) ^
                            (c4 ^ (((byte) 0xF0 << 18) ^
                                    ((byte) 0x80 << 12) ^
                                    ((byte) 0x80 << 6) ^
                                    ((byte) 0x80 << 0))));
                    offset += 3;
                    break;
                }
                /* 10xx xxxx,  1111 xxxx */
                throw error("malformed input around byte ", offset);
            }
        }
        this.ch = (char) ch;
        this.offset = offset;
    }

    int char2_utf8(int ch, int char2, int offset) {
        if ((char2 & 0xC0) != 0x80) {
            throw error("malformed input around byte ", offset);
        }
        return ((ch & 0x1F) << 6) | (char2 & 0x3F);
    }

    int char2_utf8(int ch, int char2, int char3, int offset) {
        if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
            throw error("malformed input around byte ", offset);
        }
        return (((ch & 0x0F) << 12) | ((char2 & 0x3F) << 6) | (char3 & 0x3F));
    }

    void char2_utf8(byte[] bytes, int offset, int c, char[] chars, int charPos) {
        if ((c >> 3) == -2) {
            int c2 = bytes[offset + 1];
            int c3 = bytes[offset + 2];
            int c4 = bytes[offset + 3];
            int uc = ((c << 18) ^
                    (c2 << 12) ^
                    (c3 << 6) ^
                    (c4 ^ (((byte) 0xF0 << 18) ^
                            ((byte) 0x80 << 12) ^
                            ((byte) 0x80 << 6) ^
                            ((byte) 0x80 << 0))));

            if (((c2 & 0xc0) != 0x80 || (c3 & 0xc0) != 0x80 || (c4 & 0xc0) != 0x80) // isMalformed4
                    ||
                    // shortest form check
                    !(uc >= 0x010000 && uc < 0X10FFFF + 1) // !Character.isSupplementaryCodePoint(uc)
            ) {
                throw error("malformed input around byte ", offset);
            } else {
                chars[charPos] = (char) ((uc >>> 10) + ('\uD800' - (0x010000 >>> 10))); // Character.highSurrogate(uc);
                chars[charPos + 1] = (char) ((uc & 0x3ff) + '\uDC00'); // Character.lowSurrogate(uc);
            }
            return;
        }

        throw error("malformed input around byte ", offset);
    }

    public void next() {
        int offset = this.offset;
        final byte[] str = this.bytes;
        int ch = offset >= end ? EOI : str[offset++];
        while (isWhitSpace(ch)) {
            ch = offset == end ? EOI : str[offset++];
        }
        if (ch < 0) {
            char_utf8(ch, offset);
            return;
        }
        this.ch = (char) ch;
        this.offset = offset;
    }

    @Override
    public Number parseNumber() {
        int ch = this.ch, first = ch;
        int offset = this.offset, start = offset, end = this.end;
        byte[] bytes = this.bytes;

        long result = isDigit(ch) ? '0' - ch : ch == '-' || ch == '+' ? 0 : 1;
        while (offset < end
                && isDigit(ch = bytes[offset])
                && Long.MIN_VALUE / 10 <= result & result <= 0) {
            result = result * 10 + '0' - ch;  // overflow from '0' - d => result > 0
            offset += 1;
        }
        if (result > 0 || isDigit(ch)) {
            result = 1;
            while (offset < end && isDigit(ch = bytes[offset])) {
                offset++;
            }
        }
        boolean small = false;
        int scale = 0;
        if (ch == '.') {
            small = true;
            if (offset != end) {
                offset++;
            }
            while (offset < end
                    && isDigit(ch = bytes[offset])
                    && Long.MIN_VALUE / 10 <= result & result <= 0) {
                result = result * 10 + '0' - ch;  // overflow from '0' - d => result > 0
                offset += 1;
                scale++;
            }
            if (result > 0 || isDigit(ch)) {
                result = 1;
                while (offset < end && isDigit(ch = bytes[offset])) {
                    offset++;
                    scale++;
                }
            }
        }
        int len = offset - (start - 1);
        if (len == 0 | (len == 1 & (first == '+' | first == '-' | first == '.')) | (len == 2 && (first == '+' | first == '-') && ch == '.')) {
            throw new JsonParseException("parse number error", 0, 0);
        }

        Number number;
        if (result <= 0) {
            if (first != '-') {
                result = -result;
            }

            if (small) {
                number = BigDecimal.valueOf(result, scale);
            } else if (result >= Integer.MIN_VALUE & result <= Integer.MAX_VALUE) {
                number = (int) result;
            } else {
                number = result;
            }
        } else {
            String strVal = new String(bytes, start - 1, len, StandardCharsets.ISO_8859_1);
            if (small) {
                number = new BigDecimal(strVal);
            } else {
                number = new BigInteger(strVal);
            }
        }

        valueEnd(bytes, offset);
        return number;
    }

    static boolean containsSlashOrQuote(long v, long quote) {
        /*
          for (int i = 0; i < 8; ++i) {
            byte c = (byte) v;
            if (c == '"' || c == '\\') {
                return true;
            }
            v >>>= 8;
          }
          return false;
         */
        long x22 = v ^ quote; // " -> 0x22
        long x5c = v ^ 0x5C5C5C5C5C5C5C5CL; // \n -> 0x0a
        x22 = (x22 - 0x0101010101010101L) & ~x22;
        x5c = (x5c - 0x0101010101010101L) & ~x5c;
        return ((x22 | x5c) & 0x8080808080808080L) != 0;
    }

    @Override
    protected String parseString(boolean acceptColon) {
        final byte[] bytes = this.bytes;
        char quote = this.ch;
        int valueLength;
        int offset = this.offset;
        final int start = offset, end = this.end;
        final long byteVectorQuote = quote == '\'' ? 0x2727_2727_2727_2727L : 0x2222_2222_2222_2222L;
        boolean ascii = true;
        boolean valueEscape = false;

        {
            int i = 0;
            int upperBound = offset + ((end - offset) & ~7);
            while (offset < upperBound) {
                long v = ByteArray.getLong(bytes, offset);
                if ((v & 0xFF00FF00FF00FF00L) != 0 || containsSlashOrQuote(v, byteVectorQuote)) {
                    break;
                }

                offset += 8;
                i += 8;
            }
            // ...

            for (; ; ++i) {
                if (offset >= end) {
                    throw error("invalid escape character EOI", offset);
                }

                int c = bytes[offset];
                if (c == '\\') {
                    valueEscape = true;
                    c = bytes[offset + 1];
                    offset += (c == 'u' ? 6 : (c == 'x' ? 4 : 2));
                    continue;
                }

                if (c >= 0) {
                    if (c == quote) {
                        valueLength = i;
                        break;
                    }
                    offset++;
                } else {
                    ascii = false;
                    switch ((c & 0xFF) >> 4) {
                        case 12:
                        case 13: {
                            /* 110x xxxx   10xx xxxx*/
                            offset += 2;
                            break;
                        }
                        case 14: {
                            offset += 3;
                            break;
                        }
                        default: {
                            /* 10xx xxxx,  1111 xxxx */
                            if ((c >> 3) == -2) {
                                offset += 4;
                                i++;
                                break;
                            }

                            throw error("malformed input around byte ", offset);
                        }
                    }
                }
            }
        }

        String strValue;
        if (valueEscape) {
            char[] chars = new char[valueLength];
            offset = start;
            for (int i = 0; ; ++i) {
                int ch = bytes[offset];
                if (ch == '\\') {
                    ch = bytes[++offset];
                    switch (ch) {
                        case 'u': {
                            ch = char4(bytes[offset + 1], bytes[offset + 2], bytes[offset + 3], bytes[offset + 4]);
                            offset += 4;
                            break;
                        }
                        case 'x': {
                            ch = char2(bytes[offset + 1], bytes[offset + 2]);
                            offset += 2;
                            break;
                        }
                        case '\\':
                        case '"':
                            break;
                        case 'b':
                            ch = '\b';
                            break;
                        case 't':
                            ch = '\t';
                            break;
                        case 'n':
                            ch = '\n';
                            break;
                        case 'f':
                            ch = '\f';
                            break;
                        case 'r':
                            ch = '\r';
                            break;
                        default:
                            ch = char1(ch);
                            break;
                    }
                    chars[i] = (char) ch;
                    offset++;
                } else if (ch == quote) {
                    break;
                } else {
                    if (ch >= 0) {
                        chars[i] = (char) ch;
                        offset++;
                    } else {
                        switch ((ch & 0xFF) >> 4) {
                            case 12:
                            case 13: {
                                /* 110x xxxx   10xx xxxx*/
                                chars[i] = (char) (((ch & 0x1F) << 6) | (bytes[offset + 1] & 0x3F));
                                offset += 2;
                                break;
                            }
                            case 14: {
                                chars[i] = (char)
                                        (((ch & 0x0F) << 12) |
                                                ((bytes[offset + 1] & 0x3F) << 6) |
                                                ((bytes[offset + 2] & 0x3F) << 0));
                                offset += 3;
                                break;
                            }
                            default: {
                                /* 10xx xxxx,  1111 xxxx */
                                char2_utf8(bytes, offset, ch, chars, i);
                                offset += 4;
                                i++;
                            }
                        }
                    }
                }
            }

            strValue = new String(chars);
        } else if (ascii) {
            strValue = new String(bytes, start, offset - start, StandardCharsets.US_ASCII);
        } else {
            strValue = new String(bytes, start, offset - start, StandardCharsets.UTF_8);
        }

        if (isEnabled(Feature.TrimString)) {
            strValue = strValue.trim();
        }
        // empty string to null
        if (strValue.isEmpty() && isEnabled(Feature.EmptyStringAsNull)) {
            strValue = null;
        }

        int ch = ++offset == end ? EOI : bytes[offset++];
        while (isWhitSpace(ch)) {
            ch = offset == end ? EOI : bytes[offset++];
        }

        if (acceptColon) {
            if (ch != ':') {
                throw error("read name error", offset);
            }

            ch = offset == end ? EOI : bytes[offset++];
            while (isWhitSpace(ch)) {
                ch = offset == end ? EOI : bytes[offset++];
            }
        } else {
            if (comma = ch == ',') {
                ch = offset == end ? EOI : bytes[offset++];
                while (isWhitSpace(ch)) {
                    ch = offset == end ? EOI : bytes[offset++];
                }
            }
        }

        if (ch < 0) {
            char_utf8(ch, offset);
        } else {
            this.ch = (char) ch;
            this.offset = offset;
        }
        return strValue;
    }

    @Override
    protected boolean parseBoolean() {
        int offset = this.offset;
        byte[] bytes = this.bytes;
        char first = this.ch;
        if (first == 'f' & offset + 3 < end & bytes[offset] == 'a' & bytes[offset + 1] == 'l' & bytes[offset + 2] == 's' & bytes[offset + 3] == 'e') {
            offset += 4;
        } else if (first == 't' & offset + 2 < end & bytes[offset] == 'r' & bytes[offset + 1] == 'u' & bytes[offset + 2] == 'e') {
            offset += 3;
        } else {
            throw error("read boolean error", offset);
        }
        valueEnd(bytes, offset);
        return first == 't';
    }

    @Override
    public void parseNull() {
        int offset = this.offset;
        byte[] str = this.bytes;
        if (offset + 3 >= this.end | ch != 'n' & str[offset] != 'u' & str[offset + 1] != 'l' & str[offset + 2] != 'l') {
            throw error("read null error", offset);
        }
        valueEnd(str, offset + 4);
    }

    static boolean isDigit(int ch) {
        return '0' <= ch & ch <= '9';
    }

    JsonParseException error(String message, int offset) {
        byte[] bytes = this.bytes;
        int line = 1;
        int column = 1;
        for (int i = this.start, end = offset; i < end; ++i) {
            int ch = bytes[i];
            if (ch == '\n') {
                column = 1;
                line++;
            } else {
                column++;
            }
        }
        if ((line | column) != 0) {
            message += ", at line " + line + ", column " + column;
        }
        return new JsonParseException(message, line, column);
    }
}
