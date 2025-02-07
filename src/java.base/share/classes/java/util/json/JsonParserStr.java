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

final class JsonParserStr extends JsonParser {
    private final String str;
    static final int ESCAPE_INDEX_NOT_SET = -2;
    int nextEscapeIndex = ESCAPE_INDEX_NOT_SET;

    public JsonParserStr(String str) {
        super(0, str.length());
        this.str = str;
        next();
    }

    private void valueEnd(String str, int offset) {
        char ch = offset == end ? EOI : str.charAt(offset++);
        while (isWhitSpace(ch)) {
            ch = offset == end ? EOI : str.charAt(offset++);
        }
        if (comma = ch == ',') {
            ch = offset == end ? EOI : str.charAt(offset++);
            while (isWhitSpace(ch)) {
                ch = offset == end ? EOI : str.charAt(offset++);
            }
        }
        this.ch = ch;
        this.offset = offset;
    }

    public boolean nextIfMatch(char m) {
        String str = this.str;
        int offset = this.offset;
        char ch = this.ch;
        while (isWhitSpace(ch)) {
            ch = offset == end ? EOI : str.charAt(offset++);
        }

        if (ch != m) {
            return false;
        }

        ch = offset == end ? EOI : str.charAt(offset++);
        while (isWhitSpace(ch)) {
            ch = offset == end ? EOI : str.charAt(offset++);
        }
        if (m == ',') {
            this.comma = true;
        }
        this.offset = offset;
        this.ch = ch;
        return true;
    }

    public void next() {
        int offset = this.offset;
        final String str = this.str;
        char ch = offset >= end ? EOI : str.charAt(offset++);
        while (isWhitSpace(ch)) {
            ch = offset == end ? EOI : str.charAt(offset++);
        }
        this.offset = offset;
        this.ch = ch;
    }

    @Override
    public Number parseNumber() {
        String str = this.str;
        char ch = this.ch, first = ch;
        int offset = this.offset, start = offset, end = this.end;

        long result = isDigit(ch) ? '0' - ch : ch == '-' || ch == '+' ? 0 : 1;
        while (offset < end
                && isDigit(ch = str.charAt(offset))
                && Long.MIN_VALUE / 10 <= result & result <= 0) {
            result = result * 10 + '0' - ch;  // overflow from '0' - d => result > 0
            offset += 1;
        }
        if (result > 0 || isDigit(ch)) {
            result = 1;
            while (offset < end && isDigit(ch = str.charAt(offset))) {
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
                    && isDigit(ch = str.charAt(offset))
                    && Long.MIN_VALUE / 10 <= result & result <= 0) {
                result = result * 10 + '0' - ch;  // overflow from '0' - d => result > 0
                offset += 1;
                scale++;
            }
            if (result > 0 || isDigit(ch)) {
                result = 1;
                while (offset < end && isDigit(ch = str.charAt(offset))) {
                    offset++;
                    scale++;
                }
            }
        }
        int len = offset - start;
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
            String strVal = str.substring(start - 1, offset);
            if (small) {
                number = new BigDecimal(strVal);
            } else {
                number = new BigInteger(strVal);
            }
        }

        valueEnd(str, offset);
        return number;
    }

    @Override
    protected String parseString(boolean acceptColon) {
        char ch = this.ch;
        final String str = this.str;
        final byte quote = (byte) ch;
        final byte slash = (byte) '\\';

        int offset = this.offset;
        final int start = offset, end = this.end;
        int valueLength;
        boolean valueEscape = false;

        int index = str.indexOf(quote, offset, end);
        if (index == -1) {
            throw error("invalid escape character EOI", offset);
        }
        int slashIndex = nextEscapeIndex;
        if (slashIndex == ESCAPE_INDEX_NOT_SET || (slashIndex != -1 && slashIndex < offset)) {
            nextEscapeIndex = slashIndex = str.indexOf('\\', offset, end);
        }
        if (slashIndex == -1 || slashIndex > index) {
            valueLength = index - offset;
            offset = index;
        }
        else {
            valueEscape = true;
            valueLength = slashIndex - offset;
            offset = slashIndex;

            for (; ; ) {
                if (offset >= end) {
                    throw new JsonParseException("invalid escape character EOI", 0, 0);
                }

                char c = str.charAt(offset);
                if (c == slash) {
                    valueLength++;
                    c = str.charAt(offset + 1);
                    offset += (c == 'u' ? 6 : (c == 'x' ? 4 : 2));
                    continue;
                }

                if (c == quote) {
                    break;
                }
                offset++;
                valueLength++;
            }
        }

        String strVal;
        if (valueEscape) {
            char[] buf = new char[valueLength];
            offset = readEscaped(str, start, quote, buf);
            strVal = new String(buf);
        }
        else {
            strVal = this.str.substring(start, offset);
        }

        if (isEnabled(Feature.TrimString)) {
            strVal = str.trim();
        }
        // empty string to null
        if (str.isEmpty() && isEnabled(Feature.EmptyStringAsNull)) {
            strVal = null;
        }

        ch = ++offset == end ? EOI : str.charAt(offset++);
        while (isWhitSpace(ch)) {
            ch = offset == end ? EOI : str.charAt(offset++);
        }

        if (acceptColon) {
            if (ch != ':') {
                throw error("read name error", offset);
            }

            ch = offset == end ? EOI : str.charAt(offset++);
            while (isWhitSpace(ch)) {
                ch = offset == end ? EOI : str.charAt(offset++);
            }
        } else {
            if (comma = ch == ',') {
                ch = offset == end ? EOI : str.charAt(offset++);
                while (isWhitSpace(ch)) {
                    ch = offset == end ? EOI : str.charAt(offset++);
                }
            }
        }

        this.ch = ch;
        this.offset = offset;
        return strVal;
    }

    private int readEscaped(String str, int offset, byte quote, char[] buf) {
        for (int i = 0; ; ++i) {
            char c = (char) (str.charAt(offset) & 0xff);
            if (c == '\\') {
                c = str.charAt(++offset);
                switch (c) {
                    case 'u': {
                        c = char4(str.charAt(offset + 1), str.charAt(offset + 2), str.charAt(offset + 3), str.charAt(offset + 4));
                        offset += 4;
                        break;
                    }
                    case 'x': {
                        c = char2(str.charAt(offset + 1), str.charAt(offset + 2));
                        offset += 2;
                        break;
                    }
                    case '\\':
                    case '"':
                        break;
                    case 'b':
                        c = '\b';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    case 'n':
                        c = '\n';
                        break;
                    case 'f':
                        c = '\f';
                        break;
                    case 'r':
                        c = '\r';
                        break;
                    default:
                        c = char1(c);
                        break;
                }
            } else if (c == quote) {
                break;
            }
            buf[i] = c;
            offset++;
        }
        return offset;
    }

    @Override
    protected boolean parseBoolean() {
        int offset = this.offset;
        String str = this.str;
        char first = this.ch;
        if (first == 'f' & offset + 3 < end & str.charAt(offset) == 'a' & str.charAt(offset + 1) == 'l' & str.charAt(offset + 2) == 's' & str.charAt(offset + 3) == 'e') {
            offset += 4;
        } else if (first == 't' & offset + 2 < end & str.charAt(offset) == 'r' & str.charAt(offset + 1) == 'u' & str.charAt(offset + 2) == 'e') {
            offset += 3;
        } else {
            throw error("read boolean error", offset);
        }
        valueEnd(str, offset);
        return first == 't';
    }

    @Override
    public void parseNull() {
        int offset = this.offset;
        String str = this.str;
        if (offset + 3 >= this.end | ch != 'n' & str.charAt(offset) != 'u' & str.charAt(offset + 1) != 'l' & str.charAt(offset + 2) != 'l') {
            throw error("read null error", offset);
        }
        valueEnd(str, offset + 4);
    }

    static boolean isDigit(int ch) {
        return '0' <= ch & ch <= '9';
    }

    JsonParseException error(String message, int offset) {
        String str = this.str;
        int line = 1;
        int column = 1;
        for (int i = this.start, end = offset; i < end; ++i) {
            char ch = str.charAt(i);
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
