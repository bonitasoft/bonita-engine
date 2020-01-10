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
package org.bonitasoft.engine.home;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.home.TenantStorage.INCIDENTS_LOG_FILENAME;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class TenantStorageTest {

    @Rule
    public final ClearSystemProperties bonitaClientHome = new ClearSystemProperties(
            TenantStorage.INCIDENT_LOG_FOLDER_PROPERTY);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();;

    @Spy
    TenantStorage tenantStorage = new TenantStorage();

    @Test
    public void should_return_profiles_md5_file() throws Exception {

        //when
        Files.write(tenantStorage.getProfileMD5(5L).toPath(), "md5".getBytes());
        final File profileMD5 = tenantStorage.getProfileMD5(5L);

        //then
        assertThat(profileMD5).as("should retrieve file").exists().hasBinaryContent("md5".getBytes());
    }

    @Test
    public void getIncidentFileHandler_should_use_temp_folder_if_property_not_set() throws Exception {
        //given
        final String expectedIncidentFileAbsolutePath = Paths
                .get(tenantStorage.getTenantTempFolder(14L).getFile().getAbsolutePath())
                .resolve(INCIDENTS_LOG_FILENAME).toString();

        // when
        tenantStorage.getIncidentFileHandler(14L);

        verify(tenantStorage).getFileHandler(expectedIncidentFileAbsolutePath);
    }

    @Test
    public void getIncidentFileHandler_should_use_incident_property_if_set() throws Exception {
        //given
        final String newFolder = temporaryFolder.newFolder().getAbsolutePath();
        System.setProperty(TenantStorage.INCIDENT_LOG_FOLDER_PROPERTY, newFolder);
        final long tenantId = 14L;
        final String expectedIncidentFileAbsolutePath = Paths.get(newFolder).resolve("tenants").resolve("" + tenantId)
                .resolve(INCIDENTS_LOG_FILENAME)
                .toString();

        // when
        tenantStorage.getIncidentFileHandler(tenantId);

        //then:
        verify(tenantStorage).getFileHandler(expectedIncidentFileAbsolutePath);
        verify(tenantStorage, times(0)).getTenantTempFolder(anyLong());
    }
}
