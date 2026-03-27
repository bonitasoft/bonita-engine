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
package org.bonitasoft.engine.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bonitasoft.engine.io.IOUtil;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class IOUtilTest {

    private static String lineSeparator = System.getProperty("line.separator");

    @Test
    public void testGetResources() throws Exception {
        final Map<String, byte[]> resources = IOUtil.getResources(IOUtilTest.class, IOUtil.class);

        assertNotNull(resources.get(IOUtil.class.getName().replace('.', '/') + ".class"));
    }

    @Test
    public void testGetClassData() throws Exception {
        assertNotNull(IOUtil.getClassData(this.getClass()));
    }

    @Test
    public void testGetAllContentFromInputStream() throws Exception {
        final byte[] bytes = "theContent\nVeryGreatContent".getBytes();
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        final byte[] read = IOUtil.getAllContentFrom(inputStream);
        inputStream.close();

        assertArrayEquals(bytes, read);
    }

    @Test
    public void testGetAllContentFromFile() throws Exception {
        final File file = File.createTempFile("test", "test");
        IOUtil.writeContentToFile("theContent\nVeryGreatContent", file);

        assertArrayEquals("theContent\nVeryGreatContent".getBytes(), IOUtil.getAllContentFrom(file));

        file.delete();
    }

    @Test
    public void testGetAllContentFromURL() throws Exception {

        final File file = File.createTempFile("test", "test");
        IOUtil.writeContentToFile("theContent\nVeryGreatContent", file);

        assertArrayEquals("theContent\nVeryGreatContent".getBytes(), IOUtil.getAllContentFrom(file.toURI().toURL()));

        file.delete();
    }

    @Test
    public void testDeleteDirFile() throws Exception {
        final File folder = File.createTempFile("folder", "test");
        folder.delete();
        folder.mkdir();
        final File file = new File(folder, "aFile");
        IOUtil.writeContentToFile("content", file);

        assertTrue(IOUtil.deleteDir(folder));

        assertFalse(file.exists());
        assertFalse(folder.exists());
    }

    @Test
    public void testDeleteDirFileWithRetry() throws Exception {
        final File folder = File.createTempFile("folder", "test");
        folder.delete();
        folder.mkdir();
        final File file = new File(folder, "aFile");
        IOUtil.writeContentToFile("content", file);

        assertTrue(IOUtil.deleteDir(folder, 5, 1));

        assertFalse(file.exists());
        assertFalse(folder.exists());
    }

    @Test
    public void testDeleteFile() {
        final File file = mock(File.class);
        when(file.delete()).thenReturn(true);
        final boolean deleteFile = IOUtil.deleteFile(file, 2, 1);

        assertTrue(deleteFile);
    }

    @Test
    public void testDeleteFileNotDeleted() {
        final File file = mock(File.class);
        when(file.delete()).thenReturn(false);
        final boolean deleteFile = IOUtil.deleteFile(file, 2, 1);

        assertFalse(deleteFile);
    }

    @Test
    public void testDeleteFiledeletedAfterFewTry() {
        final File file = mock(File.class);
        when(file.delete()).thenReturn(false, false, true);
        final boolean deleteFile = IOUtil.deleteFile(file, 6, 1);

        assertTrue(deleteFile);
    }

    @Test
    public void testZip() throws Exception {
        final HashMap<String, byte[]> hashMap = new HashMap<String, byte[]>(2);
        hashMap.put("file1.txt", "content1".getBytes());
        hashMap.put("file2.txt", "content2\ncontent2".getBytes());
        final byte[] zip = IOUtil.zip(hashMap);
        assertNotNull(zip);
    }

    @Test
    public void testWriteFileString() throws Exception {
        final File file = File.createTempFile("test", "test");

        IOUtil.writeContentToFile("theContent\ncontent", file);

        assertEquals("theContent\ncontent", new String(IOUtil.getAllContentFrom(file)));
        file.delete();
    }

    @Test
    public void unzipToFolder() throws Exception {
        final HashMap<String, byte[]> hashMap = new HashMap<String, byte[]>(2);
        hashMap.put("file1.txt", "content1".getBytes());
        hashMap.put("file2.txt", "content2\ncontent2".getBytes());
        final byte[] zip = IOUtil.zip(hashMap);
        final File folder = File.createTempFile("folder", "tmp");
        folder.delete();
        folder.mkdirs();

        IOUtil.unzipToFolder(new ByteArrayInputStream(zip), folder);

        final String[] files = folder.list();
        assertEquals(2, files.length);
        assertEquals("content1", new String(IOUtil.getAllContentFrom(new File(folder, "file1.txt"))));
        assertEquals("content2\ncontent2", new String(IOUtil.getAllContentFrom(new File(folder, "file2.txt"))));

        IOUtil.deleteDir(folder);
    }

    @Test
    public void unzipToFolder_should_reject_zip_slip_file_attack() throws Exception {
        final byte[] maliciousZip = createZipWithEntry("../../evil.txt", "malicious content");
        final File folder = createTempFolder();

        try {
            assertThatThrownBy(() -> IOUtil.unzipToFolder(new ByteArrayInputStream(maliciousZip), folder))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("outside of the target directory");
        } finally {
            IOUtil.deleteDir(folder);
        }
    }

    @Test
    public void unzipToFolder_should_reject_zip_slip_directory_attack() throws Exception {
        final byte[] maliciousZip = createZipWithDirectoryEntry("../../evil_dir/");
        final File folder = createTempFolder();

        try {
            assertThatThrownBy(() -> IOUtil.unzipToFolder(new ByteArrayInputStream(maliciousZip), folder))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("outside of the target directory");
        } finally {
            IOUtil.deleteDir(folder);
        }
    }

    @Test
    public void unzipToFolder_should_allow_legitimate_nested_paths() throws Exception {
        final HashMap<String, byte[]> hashMap = new HashMap<>(1);
        hashMap.put("subdir/nested/file.txt", "nested content".getBytes());
        final byte[] zip = IOUtil.zip(hashMap);
        final File folder = createTempFolder();

        try {
            IOUtil.unzipToFolder(new ByteArrayInputStream(zip), folder);

            final File nestedFile = new File(folder, "subdir/nested/file.txt");
            assertThat(nestedFile).exists();
            assertThat(new String(IOUtil.getAllContentFrom(nestedFile))).isEqualTo("nested content");
        } finally {
            IOUtil.deleteDir(folder);
        }
    }

    private static File createTempFolder() throws IOException {
        return Files.createTempDirectory("folder").toFile();
    }

    private static byte[] createZipWithEntry(String entryName, String content) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry(entryName));
            zos.write(content.getBytes());
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

    private static byte[] createZipWithDirectoryEntry(String dirName) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry(dirName));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
}
