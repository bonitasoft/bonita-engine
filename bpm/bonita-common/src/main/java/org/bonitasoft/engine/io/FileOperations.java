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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.zip.ZipFile;

/**
 * @author Baptiste Mesta
 * @author Emmanuel Duchastenier
 * @author Danila Mazour
 */
public class FileOperations {

    private FileOperations() {
        // private constructor
    }

    /**
     * Retrieve the content of a file inside a ZIP file.
     *
     * @param zip ZIP file to parse
     * @param filePath path of the file to search inside the ZIP
     * @return the bytes read from the searched file
     * @throws FileNotFoundException if the ZIP file does not contain the searched file
     * @throws java.util.zip.ZipException if a ZIP format error has occurred (e.g. the input zip file is not a ZIP)
     * @throws IOException if an I/O error has occurred
     */
    public static byte[] getFileFromZip(File zip, String filePath) throws IOException {
        try (var zipFile = new ZipFile(zip)) {
            var entry = zipFile.getEntry(filePath);
            if (entry == null) {
                throw new FileNotFoundException(String.format("'%s' not found in %s", filePath, zip.getName()));
            }
            try (var is = zipFile.getInputStream(entry)) {
                return is.readAllBytes();
            }
        }
    }

    public static void updateFileContent(File zip, String filePath, InputStream newContent) throws IOException {
        Path zipFilePath = zip.toPath();
        try (FileSystem fs = FileSystems.newFileSystem(zipFilePath, (ClassLoader) null)) {
            Path source = fs.getPath(filePath);
            Path temp = fs.getPath("./temp_" + UUID.randomUUID().toString());
            Files.write(temp, newContent.readAllBytes(), StandardOpenOption.CREATE_NEW);
            Files.move(temp, source, REPLACE_EXISTING);
        }
    }

    public static String read(File file) throws IOException {
        return Files.readString(file.toPath(), UTF_8);
    }

    public static byte[] readFully(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
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
        return resource(name).readAllBytes();
    }
}
