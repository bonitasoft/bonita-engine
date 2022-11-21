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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 * @author Danila Mazour
 */
public class FileOperations {

    public static byte[] getFileFromZip(File zip, String filePath) throws IOException {
        try (InputStream inputStream = new FileInputStream(zip)) {
            return getFileFromZip(inputStream, filePath);
        }
    }

    public static void updateFileContent(File zip, String filePath, InputStream newContent) throws IOException {
        Path zipFilePath = zip.toPath();
        try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {
            Path source = fs.getPath(filePath);
            Path temp = fs.getPath("./temp_" + UUID.randomUUID().toString());
            Files.write(temp, readFully(newContent), StandardOpenOption.CREATE_NEW);
            Files.move(temp, source, REPLACE_EXISTING);
        }
    }

    public static byte[] getFileFromZip(byte[] content, String filePath) throws IOException {
        return getFileFromZip(new ByteArrayInputStream(content), filePath);
    }

    public static byte[] getFileFromZip(InputStream inputStream, String filePath) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.getName().equals(filePath) && !zipEntry.isDirectory()) {
                    return readFully(zipInputStream);
                }
            }
            throw new FileNotFoundException(String.format("Entry %s not found in zip", filePath));
        }
    }

    public static byte[] readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = in.read(buf)) > 0) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }

    public static String read(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            return new String(readFully(inputStream), UTF_8);
        }
    }

    public static byte[] readFully(File file) throws IOException {
        try (InputStream inputStream = new FileInputStream(file)) {
            return readFully(inputStream);
        }
    }

    public static boolean isBarFile(String fileName) {
        return fileName.endsWith(".bar");
    }

    public static boolean isXmlFile(String fileName) {
        return fileName.endsWith(".xml");
    }

    public static boolean isZipFile(File file) {
        return file.getName().endsWith(".zip");
    }

    public static InputStream asInputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    public static InputStream asInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(UTF_8));
    }

    public static InputStream resource(String name) {
        return FileOperations.class.getResourceAsStream(name);
    }

    /**
     * Gets the contents of a classpath resource as a byte array.
     */
    public static byte[] resourceAsBytes(String name) throws IOException {
        return FileOperations.readFully(resource(name));
    }
}
