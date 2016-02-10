/*
 * Copyright (C) 2015 Bonitasoft S.A.
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
 */
package org.bonitasoft.engine.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.filefilter.FileFileFilter;
import org.junit.Before;
import org.junit.Test;

/**
 * author Emmanuel Duchastenier
 */
public class FolderTest {

    //    @Spy
    private File f;

    //    @InjectMocks
    private Folder folder;

    @Before
    public void setUp() throws IOException {
        //        MockitoAnnotations.initMocks(this);
        f = spy(new File("/tmp/non-existent"));
        folder = spy(new Folder(f));
    }

    @Test
    public void listFilesAsResources_should_ignore_subFolders() throws Exception {
        final File file = mock(File.class);
        doReturn("file").when(file).getName();
        doReturn(true).when(file).isFile();
        final File directory = mock(File.class);
        doReturn(false).when(directory).isFile();
        File[] filesAndFolders = new File[] { file, directory };
        doReturn(filesAndFolders).when(f).listFiles(any(FileFilter.class));
        doReturn(filesAndFolders).when(f).listFiles();
        doReturn(new File[] { file }).when(f).listFiles((FileFilter) FileFileFilter.FILE);
        doReturn(true).when(f).exists();
        doReturn(true).when(f).isDirectory();
        doReturn(new byte[1]).when(folder).getFileContent(file);
        doThrow(new RuntimeException("Directories should be filtered by Folder.listFilesAsResources() method")).when(folder).getFileContent(directory);

        // should not fail:
        final Map<String, byte[]> map = folder.listFilesAsResources();

        verify(folder).getFileContent(file);
        verify(folder, times(0)).getFileContent(directory);

        assertThat(map).hasSize(1);
        assertThat(map.get("file")).isNotNull();
    }
}
