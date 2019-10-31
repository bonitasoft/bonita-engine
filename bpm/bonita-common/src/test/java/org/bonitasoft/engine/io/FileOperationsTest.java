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

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.io.FileAndContentUtils.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author Baptiste Mesta
 */
public class FileOperationsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void should_extract_file_from_zip() throws Exception {
        File zipFile = aZipWithFiles();

        byte[] contentOfFile2 = FileOperations.getFileFromZip(zipFile, "/file2");

        assertThat(new String(contentOfFile2)).isEqualTo("the content of file 2");
    }

    private File aZipWithFiles() throws IOException {
        File zipFile;
        byte[] zip = zip(file("/file1", "the content of file 1"),
                file("/file2", "the content of file 2"));
        zipFile = temporaryFolder.newFile();
        Files.write(zipFile.toPath(), zip);
        return zipFile;
    }

    @Test(expected = FileNotFoundException.class)
    public void should_throw_file_not_found_when_zip_do_not_contains_file() throws Exception {
        byte[] zip = zip(file("/file1", "the content of file 1"));
        File zipFile = temporaryFolder.newFile();
        Files.write(zipFile.toPath(), zip);

        FileOperations.getFileFromZip(zipFile, "/file2");
    }

    @Test(expected = IOException.class)
    public void should_throw_IOException_when_file_is_not_a_zip() throws Exception {
        File zipFile = temporaryFolder.newFile();
        Files.write(zipFile.toPath(), new byte[] { 1, 2, 3 });

        FileOperations.getFileFromZip(zipFile, "toto");
    }

    @Test
    public void should_getContent_of_file_in_sub_folder() throws Exception {
        byte[] zip = zip(file("/file1", "the content of file 1"),
                directory("/sub/"),
                file("/sub/file2", "the content of file 2"));
        File zipFile = temporaryFolder.newFile();
        Files.write(zipFile.toPath(), zip);

        byte[] contentOfFile2 = FileOperations.getFileFromZip(zipFile, "/sub/file2");

        assertThat(new String(contentOfFile2)).isEqualTo("the content of file 2");
    }

}
