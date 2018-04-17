/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

        assertEquals(2, resources.size());
        assertNotNull(resources.get(IOUtilTest.class.getName().replace('.', '/') + ".class"));
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
    public void testReadInputStream() throws Exception {
        final String content = "theContent" + lineSeparator + "VeryGreatContent";
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());

        final String read = IOUtil.read(inputStream);
        inputStream.close();

        assertEquals(content, read);
    }

    @Test
    public void readFile() throws Exception {
        final File file = File.createTempFile("test", "test");
        final String content = "theContent" + lineSeparator + "VeryGreatContent";
        IOUtil.writeContentToFile(content, file);

        assertEquals(content, IOUtil.read(file));

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
}
