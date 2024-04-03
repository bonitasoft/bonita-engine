/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.digest;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.bonitasoft.engine.api.Internal;

@Internal
public class DigestUtils {

    public static String encodeBase64AsUtf8String(byte[] bytes) {
        return new String(Base64.getEncoder().encode(bytes), UTF_8);
    }

    public static byte[] md5(String source) {
        return digest("MD5", source);
    }

    public static byte[] sha1(String source) {
        return digest("SHA1", source);
    }

    private static byte[] digest(String algorithm, String source) {
        return getDigest(algorithm).digest(utf8Bytes(source));
    }

    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static byte[] utf8Bytes(String string) {
        if (string == null) {
            return null;
        }
        return string.getBytes(UTF_8);
    }

}
