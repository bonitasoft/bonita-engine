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
package org.bonitasoft.engine.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Baptiste Mesta.
 */
public class FileAndContentUtils {

    public static byte[] zip(FileAndContent... files) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (FileAndContent file : files) {
                ZipEntry e = new ZipEntry(file.getFileName());
                zos.putNextEntry(e);
                if (!e.isDirectory()) {
                    zos.write(file.getContent());
                }
                zos.closeEntry();
            }
            zos.finish();
            return baos.toByteArray();
        }
    }

    public static FileAndContent file(String fileName, String content) {
        return new FileAndContent(fileName, content.getBytes());
    }

    public static FileAndContent file(String fileName, byte[] content) {
        return new FileAndContent(fileName, content);
    }

    public static FileAndContent directory(String fileName) {
        return new FileAndContent(fileName, null);
    }

    public static FileAndContent file(String fileName, InputStream content) throws IOException {
        return new FileAndContent(fileName, content.readAllBytes());
    }

    public static class FileAndContent {

        private String fileName;
        private byte[] content;

        public FileAndContent(String fileName, byte[] content) {
            this.fileName = fileName;
            this.content = content;
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getContent() {
            return content;
        }
    }
}
