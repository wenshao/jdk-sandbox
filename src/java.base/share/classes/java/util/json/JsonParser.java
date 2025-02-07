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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;

/**
 * JsonParser
 */
public abstract class JsonParser implements AutoCloseable {
    /**
     * EOI (End Of Input)
     */
    protected static final char EOI = 0x1A;
    /**
     * Whitespace mask
     */
    protected static final long SPACE
            = (1L << ' ')
            | (1L << '\n')
            | (1L << '\r')
            | (1L << '\f')
            | (1L << '\t')
            | (1L << '\b');

    /**
     * Current char
     */
    protected char ch;
    /**
     * Current offset
     */
    protected int offset;
    /**
     * Start offset
     */
    protected final int start;
    /**
     * End offset
     */
    protected final int end;
    /**
     * Current level
     */
    protected int level;
    /**
     * Max level
     */
    protected int maxLevel = 1024;
    /**
     * has comma
     */
    protected boolean comma;
    /**
     * Features
     */
    protected long features;

    /**
     * Constructor
     * @param offset the offset
     * @param end the end offset
     */
    protected JsonParser(int offset, int end) {
        this.offset = offset;
        this.start = offset;
        this.end = end;
    }

    /**
     * Get max level
     * @return the max level
     */
    protected int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Is whitespace
     * @param ch the char
     * @return true if whitespace
     */
    static boolean isWhitSpace(int ch) {
        return ch <= ' ' && ((1L << ch) & SPACE) != 0;
    }

    /**
     * Next if match
     * @param m the match charactor
     * @return true if match
     */
    protected abstract boolean nextIfMatch(char m);

    /**
     * Next Char
     */
    protected abstract void next();

    /**
     * Parse number
     * @return the number value
     */
    protected abstract Number parseNumber();

    /**
     * Parse string
     * @return the string
     */
    protected String parseString() {
        return parseString(false);
    }

    /**
     *  Parse field name
     * @return the field name
     */
    protected String parseFieldName() {
        return parseString(true);
    }

    /**
     * Parse string
     * @param isName is name
     * @return the string
     */
    protected abstract String parseString(boolean isName);

    /**
     * Parse boolean
     * @return the boolean
     */
    protected abstract boolean parseBoolean();

    /**
     * Parse null
     */
    protected abstract void parseNull();

    /**
     * Create JsonObject
     * @return the json object
     */
    protected final JsonObject createJsonObject() {
        return JsonObject.create(true);
    }

    /**
     * Create Map
     * @return the map
     */
    protected final Map<String, Object> createObject() {
        return createJsonObject();
    }

    /**
     * Create Collection
     * @return the collection
     */
    protected final Collection<Object> createArray() {
        return JsonArray.of();
    }

    /**
     * Parse array
     * @param list the list
     */
    public final void parseArray(Collection<Object> list) {
        if (!nextIfMatch('[')) {
            throw new JsonParseException("illegal input, offset " + offset + ", char " + ch, 0, 0);
        }

        level++;
        if (level >= getMaxLevel()) {
            throw new JsonParseException("level too large : " + level, 0, 0);
        }

        for (; ; ) {
            if (nextIfMatch(']')) {
                level--;
                break;
            }
            Object item = parseAny();
            list.add(item);
        }

        nextIfMatch(',');
    }

    /**
     * Parse object
     * @param object the object
     */
    public void parseObject(Map<String, Object> object) {
        nextIfMatch('{');

        level++;
        if (level >= getMaxLevel()) {
            throw new JsonParseException("level too large : " + level, 0, 0);
        }

        for (int i = 0; ; ++i) {
            if (ch == '}') {
                next();
                break;
            }

            String name = parseFieldName();
            Object val;
            val = parseAny();

            Object origin = object.put(name, val);
            if (origin != null) {
                onDuplicate(object, name, val, origin);
            }
        }

        if (comma = (ch == ',')) {
            next();
        }

        level--;
    }

    /**
     * Parse any
     * @return the value
     */
    public Object parseAny() {
        Object val;
        switch (ch) {
            case '-':
            case '+':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                val = parseNumber();
                break;
            case '[':
                Collection<Object> arrayValue = createArray();
                parseArray(arrayValue);
                val = arrayValue;
                break;
            case '{':
                Map<String, Object> objectValue =createObject();
                parseObject(objectValue);
                val = objectValue;
                break;
            case '"':
            case '\'':
                val = parseString();
                break;
            case 't':
            case 'f':
                val = parseBoolean();
                break;
            case 'n':
                parseNull();
                val = null;
                break;
            default:
                throw new JsonParseException("illegal input " + ch, 0, 0);
        }
        return val;
    }

    /**
     * On duplicate
     * @param object the object
     * @param name the name
     * @param val the value
     * @param origin the origin value
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void onDuplicate(Map object, String name, Object val, Object origin) {
        if (isEnabled(Feature.DuplicateKeyValueAsArray)) {
            if (origin instanceof Collection) {
                ((Collection) origin).add(val);
                object.put(name, origin);
            } else {
                Collection array = createArray();
                array.add(origin);
                array.add(val);
                object.put(name, array);
            }
        }
    }

    /**
     * Is enabled
     * @param feature the feature
     * @return true if enabled
     */
    public boolean isEnabled(Feature feature) {
        return feature.isEnabled(features);
    }

    /**
     * Parser Feature
     */
    public enum Feature {
        /**
         * Keep order
         */
        KeepOrder(1),
        /**
         * Trim string
         */
        TrimString(1 << 1),
        /**
         * Empty string as null
         */
        EmptyStringAsNull(1 << 2),
        /**
         * Null as empty string
         */
        DuplicateKeyValueAsArray(1 << 3),
        /**
         * Duplicate key error
         */
        DuplicateKeyError(1 << 4);
        private final long mask;
        Feature(long mask) {
            this.mask = mask;
        }

        /**
         * Is enabled
         * @param features the features
         * @return true if enabled
         */
        public final boolean isEnabled(long features) {
            return (features & mask) != 0;
        }
    }

    /**
     * DIGITS
     */
    static final int[] DIGITS = new int[]{
            +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
            +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
            +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
            +0, +1, +2, +3, +4, +5, +6, +7, +8, +9, +0, +0, +0, +0, +0, +0,
            +0, 10, 11, 12, 13, 14, 15, +0, +0, +0, +0, +0, +0, +0, +0, +0,
            +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0, +0,
            +0, 10, 11, 12, 13, 14, 15
    };

    final char char1(int c) {
        switch (c) {
            case '0':
                return '\0';
            case '1':
                return '\1';
            case '2':
                return '\2';
            case '3':
                return '\3';
            case '4':
                return '\4';
            case '5':
                return '\5';
            case '6':
                return '\6';
            case '7':
                return '\7';
            case 'b': // 8
                return '\b';
            case 't': // 9
                return '\t';
            case 'n': // 10
                return '\n';
            case 'v': // 11
                return '\u000B';
            case 'f': // 12
            case 'F':
                return '\f';
            case 'r': // 13
                return '\r';
            case '"': // 34
            case '\'': // 39
            case '/': // 47
            case '.': // 47
            case '\\': // 92
            case '#':
            case '&':
            case '[':
            case ']':
            case '@':
            case '(':
            case ')':
            case '_':
            case ',':
            case '~':
                return (char) c;
            default:
                throw new JsonParseException("unclosed.str '\\" + c, 0, 0   );
        }
    }

    /**
     * escaped char2
     * @param c1 c1
     * @param c2 c2
     * @return the char
     */
    static char char2(int c1, int c2) {
        return (char) (DIGITS[c1] * 0x10
                + DIGITS[c2]);
    }

    /**
     * escaped char4
     * @param c1 c1
     * @param c2 c2
     * @param c3 c3
     * @param c4 c4
     * @return char
     */
    static char char4(int c1, int c2, int c3, int c4) {
        return (char) (DIGITS[c1] * 0x1000
                + DIGITS[c2] * 0x100
                + DIGITS[c3] * 0x10
                + DIGITS[c4]);
    }

    /**
     * Close
     */
    @Override
    public void close() {
    }

    /**
     * create a JsonParser
     * @param json the json string
     * @return a JsonParser
     */
    public static JsonParser of(String json) {
        return new JsonParserStr(json);
    }

    /**
     * create a JsonParser
     * @param json the json string bytes
     * @param offset the offset
     * @param length the length
     * @param charset the charset
     * @return a JsonParser
     */
    public static JsonParser of(byte[] json, int offset, int length, Charset charset) {
        if (charset == StandardCharsets.UTF_8 || charset == StandardCharsets.US_ASCII) {
            return new JsonParserUTF8(json, offset, length);
        }
        return of(new String(json, offset, length, charset));
    }
}
