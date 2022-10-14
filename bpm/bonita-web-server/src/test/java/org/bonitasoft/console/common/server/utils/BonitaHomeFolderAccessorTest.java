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

import java.io.File;

import org.bonitasoft.console.common.server.preferences.constants.WebBonitaConstantsUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BonitaHomeFolderAccessorTest {

    @Mock
    private WebBonitaConstantsUtils webBonitaConstantsUtils;

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
    public void should_complete_file_path() throws Exception {
        final String fileName = "fileName.txt";

        given(tenantFolder.getBonitaTenantConstantUtil()).willReturn(webBonitaConstantsUtils);
        given(webBonitaConstantsUtils.getTempFolder()).willReturn(new File("." + File.separator + "tempFolder"));

        final String completedPath = tenantFolder.getCompleteTenantTempFilePath(fileName);

        assertThat(new File(completedPath).getCanonicalPath()).isEqualTo(
                new File("." + File.separator + "tempFolder" + File.separator + "fileName.txt").getCanonicalPath());
    }

    @Test
    public void should_verifyAuthorization_file_path() throws Exception {
        final String fileName = "c:" + File.separator + "tempFolder" + File.separator + "fileName.txt";

        given(tenantFolder.getBonitaTenantConstantUtil()).willReturn(webBonitaConstantsUtils);
        given(webBonitaConstantsUtils.getTempFolder()).willReturn(new File("c:" + File.separator + "tempFolder"));

        final String completedPath = tenantFolder.getCompleteTenantTempFilePath(fileName);

        assertThat(completedPath).isEqualTo("c:" + File.separator + "tempFolder" + File.separator + "fileName.txt");
    }

    @Test(expected = UnauthorizedFolderException.class)
    public void should_UnauthorizedFolder() throws Exception {
        final String fileName = "c:" + File.separator + "UnauthorizedFolder" + File.separator + "tempFolder"
                + File.separator + "fileName.txt";

        given(tenantFolder.getBonitaTenantConstantUtil()).willReturn(webBonitaConstantsUtils);
        given(webBonitaConstantsUtils.getTempFolder()).willReturn(new File("c:" + File.separator + "tempFolder"));

        tenantFolder.getCompleteTenantTempFilePath(fileName);
    }

    @Test
    public void should_return_completed_temp_file() throws Exception {
        final String fileName = "fileName.txt";

        given(tenantFolder.getBonitaTenantConstantUtil()).willReturn(webBonitaConstantsUtils);
        given(webBonitaConstantsUtils.getTempFolder()).willReturn(new File("." + File.separator + "tempFolder"));

        final File completedFile = tenantFolder.getTempFile(fileName);

        assertThat(completedFile.getCanonicalPath()).isEqualTo(
                new File("." + File.separator + "tempFolder" + File.separator + "fileName.txt").getCanonicalPath());
    }
}
