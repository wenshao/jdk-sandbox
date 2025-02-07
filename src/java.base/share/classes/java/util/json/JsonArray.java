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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * JSON array.
 */
public interface JsonArray extends List<Object> {
    /**
     * converter
     * @return type converter
     */
    default JsonTypeConverter converter() {
        return JsonTypeConverter.DEFAULT;
    }

    /**
     * getJsonArray
     * @param index index of the element to return
     * @return the JsonArray at the specified position in this list
     */
    default JsonArray getJsonArray(int index) {
        return (JsonArray)this.get(index);
    }

    /**
     * getJsonObject
     * @param index index of the element to return
     * @return the JsonObject at the specified position in this list
     */
    default JsonObject getJsonObject(int index) {
        return (JsonObject)this.get(index);
    }

    /**
     * getBigDecimal
     * @param index index of the element to return
     * @return the BigDecimal value at the specified position in this list
     */
    default BigDecimal getBigDecimal(int index) {
        return converter()
                .toBigDecimal(
                        get(index));
    }

    /**
     * getBigInteger
     * @param index index of the element to return
     * @return the BigInteger value at the specified position in this list
     */
    default BigInteger getBigInteger(int index) {
        return converter()
                .toBigInteger(
                        get(index));
    }

    /**
     * getString
     * @param index index of the element to return
     * @return the String value at the specified position in this list
     */
    default String getString(int index) {
        return converter()
                .toString(
                        get(index));
    }

    /**
     * getFloat
     * @param index index of the element to return
     * @return the Float value at the specified position in this list
     */
    default Float getFloat(int index) {
        return converter().toFloat(
                get(index)
        );
    }

    /**
     * getFloatValue
     * @param index index of the element to return
     * @return the float value at the specified position in this list
     */
    default float getFloatValue(int index) {
        return converter()
                .toFloatValue(
                        get(index), 0f);
    }

    /**
     * getDouble
     * @param index index of the element to return
     * @return the Double value at the specified position in this list
     */
    default Double getDouble(int index) {
        return converter()
                .toDouble(
                        get(index));
    }

    /**
     * getDoubleValue
     * @param index index of the element to return
     * @return the double value at the specified position in this list
     */
    default double getDoubleValue(int index) {
        return converter()
                .toDoubleValue(
                        get(index),
                        0d);
    }

    /**
     * getInteger
     * @param index index of the element to return
     * @return the Integer value at the specified position in this list
     */
    default Integer getInteger(int index) {
        return converter()
                .toInteger(
                        get(index));
    }

    /**
     * getIntValue
     * @param index index of the element to return
     * @return the int value at the specified position in this list
     */
    default int getIntValue(int index) {
        return getIntValueOrDefault(index, 0);
    }

    /**
     * getIntValueOrDefault
     * @param index index of the element to return
     * @param defaultValue if the element of List is null
     * @return the int value at the specified position in this list
     */
    default int getIntValueOrDefault(int index, int defaultValue) {
        return converter()
                .toIntValue(
                        get(index), defaultValue);
    }

    /**
     * getLong
     * @param index index of the element to return
     * @return the Long value at the specified position in this list
     */
    default Long getLong(int index) {
        return converter()
                .toLong(
                        get(index));
    }

    /**
     * getLongValue
     * @param index index of the element to return
     * @return the long value at the specified position in this list
     */
    default long getLongValue(int index) {
        return getLongValueOrDefault(index, 0L);
    }

    /**
     * getLongValueOrDefault
     * @param index index of the element to return
     * @param defaultValue if the element of List is null
     * @return the long value at the specified position in this list
     */
    default long getLongValueOrDefault(int index, long defaultValue) {
        return converter()
                .toLongValue(
                        get(index), defaultValue);
    }

    /**
     * getBoolean
     * @param index index of the element to return
     * @return the Boolean value at the specified position in this list
     */
    default Boolean getBoolean(int index) {
        return converter()
                .toBoolean(
                        get(index));
    }

    /**
     * getBooleanValue
     * @param index index of the element to return
     * @return the boolean value at the specified position in this list
     */
    default boolean getBooleanValue(int index) {
        return getBooleanValueOrDefault(index, false);
    }

    /**
     * getBooleanValueOrDefault
     * @param index index of the element to return
     * @param defaultValue if the element of List is null
     * @return the boolean value at the specified position in this list
     */
    default boolean getBooleanValueOrDefault(int index, boolean defaultValue) {
        return converter()
                .toBooleanValue(
                        get(index));
    }

    /**
     * Returns an JsonArray containing zero elements.
     * @return an JsonArray containing zero elements.
     */
    static JsonArray of() {
        final class JsonArrayImpl extends ArrayList<Object> implements JsonArray {
            @Serial
            private static final long serialVersionUID = 1L;
            public String toString() {
                return Json.toJsonString(this);
            }
        }
        return new JsonArrayImpl();
    }

    /**
     * Returns an JsonArray containing an arbitrary number of values.
     * @param values  the values to be contained in the list
     * @return an JsonArray containing an arbitrary number of values.
     */
    static JsonArray of(Collection<?> values) {
        JsonArray array = of();
        array.addAll(values);
        return array;
    }

    /**
     * Returns an JsonArray containing an arbitrary number of values.
     * @param values  the values to be contained in the list
     * @return an JsonArray containing an arbitrary number of values.
     */
    static JsonArray of(Object... values) {
        JsonArray array = of();
        for (Object value : values) {
            array.add(value);
        }
        return array;
    }
}
