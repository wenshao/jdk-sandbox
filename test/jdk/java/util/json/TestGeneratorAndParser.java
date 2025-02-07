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

/*
 * @test
 * @enablePreview
 * @run junit TestGeneratorAndParser
 */

import java.util.json.Json;
import java.util.json.JsonArray;
import java.util.json.JsonGenerator;
import java.util.json.JsonObject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestGeneratorAndParser {
    @Test
    public void test() {
        String json = "{\"name\":\"Jason\",\"age\":18,\"hobby\":[\"football\",\"basketball\"],\"address\":{\"province\":\"Guangdong\",\"city\":\"Guangzhou\"}}";
        String pretty =
                """
                {
                	"name" : "Jason",
                	"age" : 18,
                	"hobby" : [
                		"football",
                		"basketball"
                	],
                	"address" : {
                		"province" : "Guangdong",
                		"city" : "Guangzhou"
                	}
                }""";

        String pretty2Space = """
                {
                  "name" : "Jason",
                  "age" : 18,
                  "hobby" : [
                    "football",
                    "basketball"
                  ],
                  "address" : {
                    "province" : "Guangdong",
                    "city" : "Guangzhou"
                  }
                }""";

        String pretty4Space = """
                {
                    "name" : "Jason",
                    "age" : 18,
                    "hobby" : [
                        "football",
                        "basketball"
                    ],
                    "address" : {
                        "province" : "Guangdong",
                        "city" : "Guangzhou"
                    }
                }""";

        {
            JsonObject jsonObject = Json.parseObject(json);
            assertEquals("Jason", jsonObject.getString("name"));
            assertEquals(18, jsonObject.getIntValue("age"));
            assertEquals("football", jsonObject.getJsonArray("hobby").getString(0));

            assertEquals(json,
                    Json.toJsonString(jsonObject));
            assertEquals(pretty,
                    Json.toJsonString(jsonObject, JsonGenerator.Feature.PrettyFormat));
            assertEquals(pretty2Space,
                    Json.toJsonString(jsonObject, JsonGenerator.Feature.PrettyFormatWith2Space));
            assertEquals(pretty4Space,
                    Json.toJsonString(jsonObject, JsonGenerator.Feature.PrettyFormatWith4Space));
        }
        {
            JsonObject jsonObject = Json.parseObject(json.getBytes());
            assertEquals("Jason", jsonObject.getString("name"));
            assertEquals(18, jsonObject.getIntValue("age"));
            assertEquals("football", jsonObject.getJsonArray("hobby").getString(0));
            assertEquals(json,
                    new String(
                            Json.toJsonBytes(jsonObject),
                            StandardCharsets.UTF_8));
        }
    }

    @Test
    public void testObjectBasicTypes() {
        JsonObject jsonObject = JsonObject.of();
        jsonObject.put("decimal", new BigDecimal("123456789012345678901234567890.0123456789"));
        jsonObject.put("bigInt", new BigInteger("123456789012345678901234567890"));
        jsonObject.put("float", 123.45f);
        jsonObject.put("double", 123.0d);
        jsonObject.put("byte", (byte)123);
        jsonObject.put("short", (short)12345);
        jsonObject.put("int", 123456789);
        jsonObject.put("long", 1234567890123456789L);
        jsonObject.put("ascii", "abc");
        jsonObject.put("utf16", "\u4e2d");
        jsonObject.put("escaped", "\\\"\\\\\\/\\b\\f\\n\\r\\t");
        jsonObject.put("latin1", "©®¢£¥¼½¾");
        jsonObject.put("emoj", "\uD83D\uDE00");
        jsonObject.put("true", true);
        jsonObject.put("false", false);

        {
            JsonArray array = JsonArray.of(jsonObject);
            String jsonString = Json.toJsonString(array);
            JsonArray parsedArray = Json.parseArray(jsonString);

            assertEquals(array.size(), parsedArray.size());

            JsonObject parsedObject0 = parsedArray.getJsonObject(0);
            assertEquals(jsonObject.size(), parsedObject0.size());

            assertEquals(jsonObject.get("decimal"), parsedObject0.getBigDecimal("decimal"));
            assertEquals(jsonObject.get("bigInt"), parsedObject0.getBigInteger("bigInt"));
            assertEquals(jsonObject.get("float"), parsedObject0.getFloat("float"));
            assertEquals(jsonObject.get("float"), parsedObject0.getFloatValue("float"));
            assertEquals(jsonObject.get("double"), parsedObject0.getDouble("double"));
            assertEquals(jsonObject.get("double"), parsedObject0.getDoubleValue("double"));
            assertEquals(jsonObject.get("byte"), (byte) parsedObject0.getIntValue("byte"));
            assertEquals(jsonObject.get("short"), (short) parsedObject0.getIntValue("short"));
            assertEquals(jsonObject.get("int"), parsedObject0.getIntValue("int"));
            assertEquals(jsonObject.get("int"), parsedObject0.getIntValueOrDefault("int", 0));
            assertEquals(jsonObject.get("int"), parsedObject0.getInteger("int"));
            assertEquals(jsonObject.get("long"), parsedObject0.getLongValue("long"));
            assertEquals(jsonObject.get("long"), parsedObject0.getLongValueOrDefault("long", 0L));
            assertEquals(jsonObject.get("long"), parsedObject0.getLong("long"));
            assertEquals(jsonObject.get("ascii"), parsedObject0.getString("ascii"));
            assertEquals(jsonObject.get("utf16"), parsedObject0.getString("utf16"));
            assertEquals(jsonObject.get("escaped"), parsedObject0.getString("escaped"));
            assertEquals(jsonObject.get("latin1"), parsedObject0.getString("latin1"));
            assertEquals(jsonObject.get("emoj"), parsedObject0.getString("emoj"));
            assertEquals(jsonObject.get("true"), parsedObject0.getBooleanValue("true"));
            assertEquals(jsonObject.get("false"), parsedObject0.getBooleanValue("false"));
        }
        {
            JsonArray array = JsonArray.of(
                    jsonObject.values());
            byte[] jsonBytes = Json.toJsonBytes(array);

            JsonArray parsedArray = Json.parseArray(jsonBytes);

            assertEquals(array.size(), parsedArray.size());

            assertEquals(array.get(0), parsedArray.getBigDecimal(0));
            assertEquals(array.get(1), parsedArray.getBigInteger(1));
            assertEquals(array.get(2), parsedArray.getFloat(2));
            assertEquals(array.get(2), parsedArray.getFloatValue(2));
            assertEquals(array.get(3), parsedArray.getDouble(3));
            assertEquals(array.get(3), parsedArray.getDoubleValue(3));
            assertEquals(array.get(4), (byte) parsedArray.getIntValue(4));
            assertEquals(array.get(5), (short) parsedArray.getIntValue(5));
            assertEquals(array.get(6), parsedArray.getInteger(6));
            assertEquals(array.get(6), parsedArray.getIntValue(6));
            assertEquals(array.get(6), parsedArray.getIntValueOrDefault(6, 0));
            assertEquals(array.get(7), parsedArray.getLong(7));
            assertEquals(array.get(7), parsedArray.getLongValue(7));
            assertEquals(array.get(7), parsedArray.getLongValueOrDefault(7, 0));
            assertEquals(array.get(8), parsedArray.getString(8));
            assertEquals(array.get(9), parsedArray.getString(9));
            assertEquals(array.get(10), parsedArray.getString(10));
            assertEquals(array.get(11), parsedArray.getString(11));
            assertEquals(array.get(12), parsedArray.getString(12));
            assertEquals(array.get(13), parsedArray.getBoolean(13));
            assertEquals(array.get(13), parsedArray.getBooleanValue(13));
            assertEquals(array.get(13), parsedArray.getBooleanValueOrDefault(13, false));
        }
    }

    @Test
    public void testObjectComplexTypes() {
        JsonObject jsonObject = JsonObject
                .of("root", JsonObject.of(
                        "child", JsonArray.of(
                                JsonArray.of(),
                                JsonObject.of()))
                );
        String string = jsonObject.toString();
        assertEquals("{\"root\":{\"child\":[[],{}]}}", string);
        JsonObject parsedObject = Json.parseObject(string);
        assertTrue(
                parsedObject
                        .getJsonObject("root")
                        .getJsonArray("child")
                        .getJsonArray(0)
                        .isEmpty());
        assertTrue(
                parsedObject
                        .getJsonObject("root")
                        .getJsonArray("child")
                        .getJsonObject(1)
                        .isEmpty());
    }

    @Test
    public void testParse() {
        String value = "abc";
        String jsonString = Json.toJsonString(value);
        byte[] jsonBytes = jsonString.getBytes(StandardCharsets.UTF_8);
        assertEquals(value, Json.parse(jsonString));
        assertEquals(value, Json.parse(jsonBytes));
        assertEquals(value, Json.parse(jsonBytes, 0, jsonBytes.length, StandardCharsets.UTF_8));
    }
}
