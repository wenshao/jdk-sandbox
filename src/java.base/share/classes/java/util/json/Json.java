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

/**
 * Factory and utility methods for Json
 */
public final class Json {
    /**
     * Constructor
     */
    private Json() {
        // no instances
    }

    /**
     * Parse value from a JsonParser
     * @param parser the parser
     * @return a value
     */
    private static Object parse(JsonParser parser) {
        try {
            return parser.parseAny();
        } finally {
            parser.close();
        }
    }

    /**
     * Parse from a String
     * @param str json string
     * @return a value
     */
    public static Object parse(String str) {
        try (JsonParser parser = JsonParser.of(str)) {
            return parse(parser);
        }
    }

    /**
     * Parse from a utf8 bytes
     * @param str utf8 bytes
     * @return a value
     */
    public static Object parse(byte[] str) {
        return parse(str, 0, str.length, StandardCharsets.UTF_8);
    }

    /**
     * Parse from a String
     * @param bytes json string bytes
     * @param offset the offset
     * @param length the length
     * @param charset the charset
     * @return a value
     */
    public static Object parse(byte[] bytes, int offset, int length, Charset charset) {
        try (JsonParser parser = JsonParser.of(bytes, offset, length, charset)) {
            return parse(parser);
        }
    }

    /**
     * Parse a JsonObject from a JsonParser
     * @param parser the parser
     * @return a JsonObject
     */
    private static JsonObject parseObject(JsonParser parser) {
        try {
            JsonObject object = parser.createJsonObject();
            parser.parseObject(object);
            return object;
        } finally {
            parser.close();
        }
    }

    /**
     * Parse a JsonObject from a byte array
     * @param json the json string utf8 bytes
     * @return a JsonObject
     */
    public static JsonObject parseObject(byte[] json) {
        return parseObject(json, 0, json.length, StandardCharsets.UTF_8);
    }

    /**
     * Parse a JsonObject from a byte array
     * @param json json string bytes
     * @param off the offset
     * @param len the length
     * @param charset the charset
     * @return a JsonObject
     */
    public static JsonObject parseObject(byte[] json, int off, int len, Charset charset) {
        return parseObject(JsonParser.of(json, off, len, charset));
    }

    /**
     * Parse a JsonObject from a String
     * @param json json string
     * @return a JsonObject
     */
    public static JsonObject parseObject(String json) {
        return parseObject(JsonParser.of(json));
    }

    /**
     * Parse a JsonArray from a String
     * @param json json string
     * @return a JsonArray
     */
    public static JsonArray parseArray(String json) {
        try (JsonParser parser = JsonParser.of(json)) {
            JsonArray array = JsonArray.of();
            parser.parseArray(array);
            return array;
        }
    }

    /**
     * Parse a JsonArray from a byte array
     * @param json json string utf8 bytes
     * @return a JsonArray
     */
    public static JsonArray parseArray(byte[] json) {
        return parseArray(json, 0, json.length, StandardCharsets.UTF_8);
    }

    /**
     * Parse a JsonArray from a byte array
     * @param json json string bytes
     * @param offset the offset
     * @param length the length
     * @param charset the charset
     * @return a JsonArray
     */
    public static JsonArray parseArray(byte[] json, int offset, int length, Charset charset) {
        try (JsonParser parser = JsonParser.of(json, offset, length, charset)) {
            JsonArray array = JsonArray.of();
            parser.parseArray(array);
            return array;
        }
    }

    /**
     * Convert a value to json string
     * @param value the value to convert
     * @param features generator features
     * @return json string
     */
    public static String toJsonString(Object value, JsonGenerator.Feature... features) {
        try (JsonGenerator generator = JsonGenerator.ofUTF16(features)) {
            generator.writeAny(value);
            return generator.toString();
        }
    }

    /**
     * Convert a value to json string utf8 bytes
     * @param value the value to convert
     * @return json string utf8 bytes
     */
    public static byte[] toJsonBytes(Object value) {
        try (JsonGenerator generator = JsonGenerator.of()) {
            generator.writeAny(value);
            return generator.getBytes();
        }
    }
}
