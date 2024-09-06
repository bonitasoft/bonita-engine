/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.platform.setup;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Emmanuel Duchastenier
 */
public final class SimpleEncryptor {

    private static final byte[] SALT = new byte[] { 0x47, 0x6f, 0x6f, 0x64, 0x4a, 0x6f, 0x62, 0x21 };
    private static final char[] PASSPHRASE = new String(new byte[] { 0x48, 0x34, 0x76, 0x33, 0x46, 0x75, 0x6e,
            0x57, 0x31, 0x37, 0x68, 0x38, 0x30, 0x6e, 0x31, 0x37, 0x34 }).toCharArray();

    private static final SecretKey SECRET_KEY = SimpleEncryptor.generateKey();

    private SimpleEncryptor() {
    }

    public static SecretKey generateKey() {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(PASSPHRASE, SALT, 1000, 256);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String encrypt(byte[] data) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY);
        byte[] encryptedBytes = cipher.doFinal(data);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static byte[] decrypt(String encryptedData) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
        return cipher.doFinal(decodedBytes);
    }
}
