/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.document.api.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;

/**
 * @author Yongtao Guo
 */
public class DocumentUtil {

    public static byte[] getArrayByte(final InputStream input, final int estimatedSize) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream(estimatedSize);
        try {
            final byte[] buf = new byte[8192];
            int len;

            while ((len = input.read(buf)) >= 0) {
                output.write(buf, 0, len);
            }
        } finally {
            output.close();
        }
        return output.toByteArray();
    }

    public static byte[] getArrayByteFromFile(final File f) throws IOException {
        final long length = f.length();
        if (length > Integer.MAX_VALUE) { // more than 2 GB
            throw new IOException("File too big");
        }

        final FileInputStream input = new FileInputStream(f);
        try {
            return getArrayByte(input, (int) length);
        } finally {
            input.close();
        }
    }

    public static String getFileNameFromUrl(final String file) {
        String fileName = null;
        if (file != null) {
            final int index = file.lastIndexOf("/");
            if (index != -1 && index != file.length() - 1) {
                final String queryName = file.substring(index + 1);
                final int queryIndex = queryName.indexOf("?");
                if (queryIndex != -1) {
                    fileName = queryName.substring(0, queryIndex);
                } else {
                    fileName = queryName;
                }
            }
        }

        return fileName;
    }

    public static String getMimeTypeFromUrl(final String fileUrl) {
        String type = null;
        final FileNameMap fileNameMap = URLConnection.getFileNameMap();
        type = fileNameMap.getContentTypeFor(fileUrl);

        return type;
    }
}
