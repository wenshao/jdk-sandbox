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

import java.io.Serial;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A JSON object.
 */
public interface JsonObject
        extends Map<String, Object> {
    /**
     * converter
     * @return type converter
     */
    default JsonTypeConverter converter() {
        return JsonTypeConverter.DEFAULT;
    }

    /**
     * getJsonArray
     * @param key the key whose associated value is to be returned
     * @return the JsonArray to which the specified key is mapped,
     */
    default JsonArray getJsonArray(String key) {
        return (JsonArray) this.get(key);
    }

    /**
     * getJsonObject
     * @param key the key whose associated value is to be returned
     * @return the JsonObject to which the specified key is mapped,
     */
    default JsonObject getJsonObject(String key) {
        return (JsonObject) this.get(key);
    }

    /**
     * getBigDecimal
     * @param key the key whose associated value is to be returned
     * @return the BigDecimal value to which the specified key is mapped,
     */
    default BigDecimal getBigDecimal(String key) {
        return converter()
                .toBigDecimal(
                        get(key));
    }

    /**
     * getBigInteger
     * @param key the key whose associated value is to be returned
     * @return the BigInteger to which the specified key is mapped,
     */
    default BigInteger getBigInteger(String key) {
        return converter()
                .toBigInteger(
                        get(key));
    }

    /**
     * getFloat
     * @param key the key whose associated value is to be returned
     * @return the Float value to which the specified key is mapped,
     */
    default Float getFloat(String key) {
        return converter().toFloat(
                get(key)
        );
    }

    /**
     * getFloatValue
     * @param key the key whose associated value is to be returned
     * @return the float value to which the specified key is mapped,
     */
    default float getFloatValue(String key) {
        return converter()
                .toFloatValue(
                        get(key), 0f);
    }

    /**
     * getDouble
     * @param key the key whose associated value is to be returned
     * @return the Double to which the specified key is mapped,
     */
    default Double getDouble(String key) {
        return converter()
                .toDouble(
                        get(key));
    }

    /**
     * getDoubleValue
     * @param key the key whose associated value is to be returned
     * @return the double value to which the specified key is mapped,
     */
    default double getDoubleValue(String key) {
        return converter()
                .toDoubleValue(
                        get(key),
                        0d);
    }

    /**
     * getString
     * @param key the key whose associated value is to be returned
     * @return the String to which the specified key is mapped,
     */
    default String getString(String key) {
        return converter()
                .toString(
                        get(key));
    }

    /**
     * getInteger
     * @param key the key whose associated value is to be returned
     * @return the Integer to which the specified key is mapped,
     */
    default Integer getInteger(String key) {
        return converter()
                .toInteger(
                        get(key));
    }

    /**
     * getIntValue
     * @param key the key whose associated value is to be returned
     * @return the int value to which the specified key is mapped,
     */
    default int getIntValue(String key) {
        return getIntValueOrDefault(key, 0);
    }

    /**
     * getIntValueOrDefault
     * @param key the key whose associated value is to be returned
     * @param defaultValue if the element of List is null
     * @return the int value to which the specified key is mapped,
     */
    default int getIntValueOrDefault(String key, int defaultValue) {
        return converter()
                .toIntValue(
                        get(key), defaultValue);
    }

    /**
     * getLong
     * @param key the key whose associated value is to be returned
     * @return the Long to which the specified key is mapped,
     */
    default Long getLong(String key) {
        return converter()
                .toLong(
                        get(key));
    }

    /**
     * getLongValue
     * @param key the key whose associated value is to be returned
     * @return the Long to which the specified key is mapped,
     */
    default long getLongValue(String key) {
        return getLongValueOrDefault(key, 0L);
    }

    /**
     * getLongValueOrDefault
     * @param key the key whose associated value is to be returned
     * @param defaultValue if the element of List is null
     * @return the Long to which the specified key is mapped,
     */
    default long getLongValueOrDefault(String key, long defaultValue) {
        return converter()
                .toLongValue(
                        get(key), defaultValue);
    }

    /**
     * getBoolean
     * @param key the key whose associated value is to be returned
     * @return the Boolean to which the specified key is mapped,
     */
    default Boolean getBoolean(String key) {
        return converter()
                .toBoolean(
                        get(key));
    }

    /**
     * getBooleanValue
     * @param key the key whose associated value is to be returned
     * @return the Boolean to which the specified key is mapped,
     */
    default boolean getBooleanValue(String key) {
        return getBooleanValueOrDefault(key, false);
    }

    /**
     * getBooleanValueOrDefault
     * @param key the key whose associated value is to be returned
     * @param defaultValue if the element of List is null
     * @return the Boolean to which the specified key is mapped,
     */
    default boolean getBooleanValueOrDefault(String key, boolean defaultValue) {
        return converter()
                .toBooleanValue(
                        get(key));
    }

    /**
     * Returns an JsonObject containing zero mappings.
     * @param ordered The flag that the elements of JsonObject are stored in order
     * @return an JsonObject containing zero mappings.
     */
    static JsonObject create(boolean ordered) {
        class JsonObject0 extends LinkedHashMap<String, Object> implements JsonObject {
            @Serial
            private static final long serialVersionUID = 1L;
            public String toString() {
                return Json.toJsonString(this);
            }
        }
        class JsonObject1 extends HashMap<String, Object> implements JsonObject {
            @Serial
            private static final long serialVersionUID = 1L;
            public String toString() {
                return Json.toJsonString(this);
            }
        }
        return ordered ?  new JsonObject0() : new JsonObject1();
    }

    /**
     * Returns an JsonObject containing zero mappings.
     * @return an JsonObject containing zero mappings.
     */
    static JsonObject of() {
        return create(true);
    }

    /**
     *  Returns an JsonObject containing a single mapping.
     * @param key the mapping's key
     * @param value the mapping's value
     * @return an JsonObject containing a single mapping.
     */
    static JsonObject of(String key, Object value) {
        JsonObject object = create(true);
        object.put(key, value);
        return object;
    }
}
