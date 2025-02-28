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
package org.bonitasoft.platform.configuration.impl;

import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.bonitasoft.platform.configuration.model.FullBonitaConfiguration;
import org.bonitasoft.platform.configuration.type.ConfigurationType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class ConfigurationServiceImplTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @InjectMocks
    @Spy
    ConfigurationServiceImpl configurationService;

    @Test
    public void getPlatformPortalConf_should_call_query_for_PLATFORM_PORTAL_type() {
        doReturn(Collections.EMPTY_LIST).when(configurationService)
                .getBonitaConfigurations(ArgumentMatchers.any(ConfigurationType.class));

        configurationService.getPlatformPortalConf();

        verify(configurationService).getBonitaConfigurations(ConfigurationType.PLATFORM_PORTAL);
    }

    @Test
    public void getPlatformEngineConf_should_call_query_for_PLATFORM_ENGINE_type() {
        doReturn(Collections.EMPTY_LIST).when(configurationService)
                .getBonitaConfigurations(ArgumentMatchers.any(ConfigurationType.class));

        configurationService.getPlatformEngineConf();

        verify(configurationService).getBonitaConfigurations(ConfigurationType.PLATFORM_ENGINE);
    }

    @Test
    public void getLicenses_should_call_query_for_LICENSES_type() {
        doReturn(Collections.EMPTY_LIST).when(configurationService)
                .getBonitaConfigurations(ArgumentMatchers.any(ConfigurationType.class));

        configurationService.getLicenses();

        verify(configurationService).getBonitaConfigurations(ConfigurationType.LICENSES);
    }

    @Test
    public void should_write_file_within_sub_folder() throws Exception {
        //given
        final File configFolder = temporaryFolder.newFolder("conf");
        final File licFolder = temporaryFolder.newFolder("lic");
        List<FullBonitaConfiguration> confs = new ArrayList<>();
        confs.add(new FullBonitaConfiguration("conf1.properties", "content 1".getBytes(), "PLATFORM_TYPE"));
        confs.add(new FullBonitaConfiguration("conf2.properties", "content 2".getBytes(), "TENANT_TYPE"));

        doReturn(confs).when(configurationService).getAllConfiguration();
        doCallRealMethod().when(configurationService).writeAllConfigurationToFolder(configFolder, licFolder);

        //when
        configurationService.writeAllConfigurationToFolder(configFolder, licFolder);

        // then
        Assertions.assertThat(configFolder.toPath().resolve("platform_type").resolve("conf1.properties").toFile())
                .as("should lowercase configuration type").exists();
        Assertions
                .assertThat(configFolder.toPath().resolve("tenant_type").resolve("conf2.properties").toFile())
                .as("should create 'tenant_type' sub folder").exists();

    }

}
