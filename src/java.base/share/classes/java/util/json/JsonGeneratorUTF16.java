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

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import jdk.internal.access.JavaLangAccess;
import jdk.internal.access.SharedSecrets;
import jdk.internal.util.DecimalDigits;

final class JsonGeneratorUTF16 extends JsonGenerator {
    /**
     * JLA
     */
    private static final JavaLangAccess JLA = SharedSecrets.getJavaLangAccess();

    static final Charset CHARSET = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN ? StandardCharsets.UTF_16BE : StandardCharsets.UTF_16LE;
    JsonGeneratorUTF16(int bufSize) {
        super(bufSize);
    }

    @Override
    void getChars(int i, int index, byte[] bytes) {
        DecimalDigits.getCharsUTF16(i, index, bytes);
    }

    @Override
    protected byte[] allocate(int buffSize) {
        return new byte[buffSize << 1];
    }

    @Override
    void getChars(long i, int index, byte[] bytes) {
        DecimalDigits.getCharsUTF16(i, index, bytes);
    }

    @Override
    void putChar(byte[] str, int off, int ch) {
        JLA.putCharUTF16(str, off, ch);
    }

    @Override
    void writeRaw(byte[] bytes, int off, String str) {
        for (int i = 0; i < str.length(); i++) {
            putChar(bytes, off + i, str.charAt(i));
        }
    }

    void grow0(int minCapacity) {
        // minCapacity is usually close to size, so this is a win:
        bytes = Arrays.copyOf(bytes, newCapacity(minCapacity, length(bytes)) << 1);
    }

    @Override
    public byte[] getBytes() {
        return Arrays.copyOf(bytes, off << 1);
    }

    @Override
    public byte[] getBytes(Charset charset) {
        if (charset == CHARSET) {
            return getBytes();
        }
        return toString().getBytes(charset);
    }

    @Override
    public String toString() {
        return new String(bytes, 0, off << 1, CHARSET);
    }

    @Override
    int length(byte[] bytes) {
        return bytes.length >> 1;
    }

    public void writeString(String value) {
        writeString0(value, 0, value.length());
    }
}
