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

/**
 * JsonTypeConverter is used to convert the value to the specified type.
 */
public class JsonTypeConverter {
    /**
     * The default JsonTypeConverter
     */
    public static final JsonTypeConverter DEFAULT = new JsonTypeConverter();

    /**
     * Constructor for JsonTypeConverter.
     */
    public JsonTypeConverter() {
    }

    /**
     * Convert the value to BigDecimal.
     * @param value the value to be converted
     * @return the converted value
     */
    public BigDecimal toBigDecimal(Object value) {
        return switch (value) {
            case null -> null;
            case BigDecimal decimal -> decimal;
            case BigInteger i -> new BigDecimal(i);
            case Byte i -> BigDecimal.valueOf (i);
            case Short i -> BigDecimal.valueOf (i);
            case Integer i -> BigDecimal.valueOf (i);
            case Long i -> BigDecimal.valueOf (i);
            case Float i -> BigDecimal.valueOf (i);
            case Double i -> BigDecimal.valueOf (i);
            default -> throw new JsonParseException("can not cast to BigDecimal from " + value.getClass().getName(), 0, 0);
        };
    }

    /**
     * Convert the value to BigInteger.
     * @param value the value to be converted
     * @return the converted value
     */
    public  BigInteger toBigInteger(Object value) {
        return switch (value) {
            case null -> null;
            case BigInteger v -> v;
            case BigDecimal decimal -> decimal.toBigIntegerExact();
            case Byte i -> BigInteger.valueOf (i);
            case Short i -> BigInteger.valueOf (i);
            case Integer i -> BigInteger.valueOf (i);
            case Long i -> BigInteger.valueOf (i);
            case String str -> new BigInteger(str);
            default -> throw new JsonParseException("can not cast to BigInteger from " + value.getClass().getName(), 0, 0);
        };
    }

    /**
     * Convert the value to float.
     * @param value the value to be converted
     * @param defaultValue the default value
     * @return the converted value
     */
    public  float toFloatValue(Object value, float defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Float) {
            return (Float) value;
        }
        return toFloatValue(value);
    }

    /**
     * Convert the value to float.
     * @param value the vlaue
     * @return the converted value
     */
    public  Float toFloat(Object value) {
        if (value == null || value instanceof Float) {
            return (Float) value;
        }
        return toFloatValue(value);
    }

    /**
     * Convert the value to float.
     * @param value the value
     * @return the converted value
     */
    public  float toFloatValue(Object value) {
        return switch (value) {
            case Number number -> number.floatValue();
            case String str -> Float.parseFloat(str);
            default -> throw new JsonParseException("can not cast to float from " + value.getClass().getName(), 0, 0);
        };
    }

    /**
     * Convert the value to double.
     * @param value the value
     * @param defaultValue the default value
     * @return the converted value
     */
    public double toDoubleValue(Object value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Double) {
            return (Double) value;
        }
        return toDoubleValue(value);
    }

    /**
     * Convert the value to double.
     * @param value the value
     * @return the converted value
     */
    public  Double toDouble(Object value) {
        if (value == null || value instanceof Double) {
            return (Double) value;
        }
        return toDoubleValue(value);
    }

    /**
     * Convert the value to double.
     * @param value the vlaue
     * @return the converted value
     */
    public double toDoubleValue(Object value) {
        return switch (value) {
            case Number number -> number.doubleValue();
            case String str -> Double.parseDouble(str);
            default -> throw new JsonParseException("can not cast to double from " + value.getClass().getName(), 0, 0);
        };
    }

    /**
     * Convert the value to int.
     * @param value the value
     * @return the converted value
     */
    public  Integer toInteger(Object value) {
        if (value == null || value instanceof Integer) {
            return (Integer) value;
        }
        return toIntValue(value);
    }

    /**
     * Convert the value to int.
     * @param value the value
     * @param defaultValue the default value
     * @return the converted value
     */
    public int toIntValue(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return toIntValue(value);
    }

    /**
     * Convert the value to int.
     * @param value the value
     * @return the converted value
     */
    public int toIntValue(Object value) {
        return switch (value) {
            case Integer i -> i;
            case Number number -> number.intValue();
            case String str -> Integer.parseInt(str);
            default -> throw new JsonParseException("can not cast to int from " + value.getClass().getName(), 0, 0);
        };
    }

    /**
     * Convert the value to long.
     * @param value the value
     * @return the converted value
     */
    public  Long toLong(Object value) {
        if (value == null || value instanceof Long) {
            return (Long) value;
        }
        return toLongValue(value);
    }

    /**
     * Convert the value to long.
     * @param value the value
     * @param defaultValue the default value
     * @return the converted value
     */
    public long toLongValue(Object value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        return toLongValue(value);
    }

    /**
     * Convert the value to long.
     * @param value the value
     * @return the converted value
     */
    protected long toLongValue(Object value) {
        return switch (value) {
            case Number number -> number.longValue();
            case String str -> Long.parseLong(str);
            default -> throw new JsonParseException("can not cast to long from " + value.getClass().getName(), 0, 0);
        };
    }

    /**
     * Convert the value to boolean.
     * @param value the value
     * @param defaultValue the default value
     * @return the converted value
     */
    public boolean toBooleanValue(Object value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return toBooleanValue(value);
    }

    /**
     * Convert the value to boolean.
     * @param value the value
     * @return the converted value
     */
    public Boolean toBoolean(Object value) {
        if (value == null || value instanceof Boolean) {
            return (Boolean) value;
        }
        return toBooleanValue(value);
    }

    /**
     * Convert the value to boolean.
     * @param value the value
     * @return the converted value
     */
    protected boolean toBooleanValue(Object value) {
        return switch (value) {
            case Boolean b -> b;
            case Integer i -> i != 0;
            case String str -> str.equals("1") || (!str.equals("0") && Boolean.parseBoolean(str));
            default -> throw new JsonParseException("can not cast to long from " + value.getClass().getName(), 0, 0);
        };
    }

    /**
     * Convert the value to string.
     * @param value the value
     * @return the converted value
     */
    public String toString(Object value) {
        if (value == null || value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }
}
