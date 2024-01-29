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
package org.bonitasoft.console.common.server.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.bonitasoft.engine.io.FileContent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BonitaHomeFolderAccessorTest {

    @Mock
    private WebBonitaConstantsUtils webBonitaConstantsUtils;

    @Mock
    private FileContent fileContent;

    @Spy
    private final BonitaHomeFolderAccessor tenantFolder = new BonitaHomeFolderAccessor();

    @Test
    public void should_authorized_a_file_in_temp_folder() throws Exception {
        given(webBonitaConstantsUtils.getTempFolder()).willReturn(new File("." + File.separator + "tempFolder"));

        final File file = new File(webBonitaConstantsUtils.getTempFolder().getAbsolutePath(),
                "" + File.separator + ".." + File.separator + "tempFolder"
                        + File.separator + "fileName.txt");

        final boolean isInTempFolder = tenantFolder.isInTempFolder(file, webBonitaConstantsUtils);

        assertTrue(isInTempFolder);
    }

    @Test
    public void should_unauthorized_a_file_not_in_temp_folder() throws Exception {
        given(webBonitaConstantsUtils.getTempFolder()).willReturn(new File("." + File.separator + "tempFolder"));

        final File file = new File(webBonitaConstantsUtils.getTempFolder().getAbsolutePath(),
                "" + File.separator + ".." + File.separator + ".."
                        + File.separator + ".." + File.separator + "fileName.txt");

        final boolean isInTempFolder = tenantFolder.isInTempFolder(file, webBonitaConstantsUtils);

        assertFalse(isInTempFolder);
    }

    @Test
    public void should_authorized_a_file_in_a_specific_folder() throws Exception {

        final File folder = new File("." + File.separator + "anyFolder");

        final File file = new File("." + File.separator + "anyFolder" + File.separator + ".." + File.separator
                + "anyFolder" + File.separator + "fileName.txt");

        final boolean isInTempFolder = tenantFolder.isInFolder(file, folder);

        assertTrue(isInTempFolder);
    }

    @Test
    public void should_unauthorized_a_file_not_in_a_specific_folder() throws Exception {

        final File folder = new File("." + File.separator + "anyFolder");

        final File file = new File("." + File.separator + "anyFolder" + File.separator + ".." + File.separator + ".."
                + File.separator + "fileName.txt");

        final boolean isInTempFolder = tenantFolder.isInFolder(file, folder);

        assertFalse(isInTempFolder);
    }

    @Test
    public void should_return_completed_temp_file() throws Exception {
        final String originalFilename = "text.txt";
        final String tempFileName = UUID.randomUUID().toString();

        Path tempFolder = Path.of("." + File.separator + "tempFolder");
        Files.createDirectories(tempFolder);

        given(tenantFolder.getBonitaTenantConstantUtil()).willReturn(webBonitaConstantsUtils);
        given(webBonitaConstantsUtils.getTempFolder()).willReturn(tempFolder.toFile());

        File newTmpFile = tenantFolder.makeUniqueFilename(originalFilename);
        doReturn(newTmpFile).when(tenantFolder).makeUniqueFilename(originalFilename);
        doReturn(InputStream.nullInputStream()).when(fileContent).getInputStream();
        doReturn(originalFilename).when(fileContent).getFileName();
        doReturn(fileContent).when(tenantFolder).retrieveUploadedTempContent(tempFileName);

        final File completedFile = tenantFolder.getTempFile(tempFileName);

        assertThat(completedFile.getCanonicalPath()).isEqualTo(
                new File("." + File.separator + "tempFolder" + File.separator + newTmpFile.getName())
                        .getCanonicalPath());
    }
}
