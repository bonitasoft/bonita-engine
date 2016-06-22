/**
 * Copyright (C) 2016 Bonitasoft S.A.
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

package org.bonitasoft.engine.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.io.IOUtil;
import org.bonitasoft.platform.configuration.impl.ConfigurationServiceImpl;
import org.bonitasoft.platform.configuration.model.BonitaConfiguration;
import org.bonitasoft.platform.exception.PlatformException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ClearSystemProperties;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class PlatformLicensesSetupTest {

    @Rule
    public final ClearSystemProperties bonitaClientHome = new ClearSystemProperties(PlatformLicensesSetup.BONITA_CLIENT_HOME);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    ConfigurationServiceImpl configurationService;

    @Spy
    PlatformLicensesSetup platformLicensesSetup;

    private LicenseAnswer licenseAnswer;

    @Before
    public void before() throws Exception {
        licenseAnswer = new LicenseAnswer();
        doReturn(configurationService).when(platformLicensesSetup).getConfigurationService();
    }

    private final class LicenseAnswer implements Answer<List<BonitaConfiguration>> {

        int i = 0;

        @Override
        public List<BonitaConfiguration> answer(final InvocationOnMock invocation) {
            i++;
            List<BonitaConfiguration> bonitaConfigurations = new ArrayList<>();
            final String resourceName = new StringBuilder().append("license").append(i).append(".lic").toString();
            bonitaConfigurations
                    .add(new BonitaConfiguration(resourceName, new StringBuilder().append("license ").append(i).append(" content").toString().getBytes()));
            return bonitaConfigurations;
        }

    }

    @Test
    public void should_setup_licenses_folder_when_bonitaHomeClient_is_not_set() throws Exception {
        //given
        doReturn(Collections.singletonList(new BonitaConfiguration("license1.lic", "licence content".getBytes()))).when(configurationService).getLicenses();

        //when
        platformLicensesSetup.setupLicenses();

        //then
        final String property = System.getProperty(PlatformLicensesSetup.BONITA_CLIENT_HOME);
        assertThat(property).as("should set system property in temp folder, depends on OS")
                .isNotNull()
                .contains("bonita_licenses");

        final File folder = new File(property);
        assertThat(folder).exists().isDirectory();
        assertThat(folder.listFiles()).extracting("name").contains("license1.lic");

    }

    @Test
    public void should_setup_licenses_folder_skip_property_when_no_licenses_are_found() throws Exception {
        //given
        doReturn(Collections.EMPTY_LIST).when(configurationService).getLicenses();

        //when
        platformLicensesSetup.setupLicenses();

        //then
        assertThat(System.getProperties()).doesNotContainKey(PlatformLicensesSetup.BONITA_CLIENT_HOME);
    }

    @Test
    public void should_extract_licenses_even_if_bonitaHomeClient_is_set() throws Exception {
        //given
        final String existing_licenses_folder = "existing_licenses_folder";
        System.setProperty(PlatformLicensesSetup.BONITA_CLIENT_HOME, existing_licenses_folder);
        doReturn(Collections.singletonList(new BonitaConfiguration("license1.lic", "license content".getBytes()))).when(configurationService).getLicenses();

        //when
        platformLicensesSetup.setupLicenses();

        //then
        final String property = System.getProperty(PlatformLicensesSetup.BONITA_CLIENT_HOME);
        assertThat(property).as("should not modify property").isEqualTo(existing_licenses_folder);

        verify(platformLicensesSetup).extractLicenses();

    }

    @Test
    public void should_throw_exception_when_unable_to_retrieve_licenses() throws Exception {
        //given
        doThrow(PlatformException.class).when(configurationService).getLicenses();

        //then
        expectedException.expect(IllegalStateException.class);

        //when
        platformLicensesSetup.setupLicenses();

    }

    @Test
    public void should_extract_licenses_clear_previous_licenses_from_temp_folder() throws Exception {
        //given
        final File licensesFolder = Paths.get(IOUtil.TMP_DIRECTORY, "bonita_licenses").toFile();
        when(configurationService.getLicenses()).thenAnswer(licenseAnswer);

        //when
        platformLicensesSetup.extractLicenses();

        //then
        assertThat(licensesFolder).isDirectory().exists();
        assertThat(System.getProperty(PlatformLicensesSetup.BONITA_CLIENT_HOME)).isEqualTo(licensesFolder.getAbsolutePath());
        assertThat(licensesFolder.listFiles()).extracting("name").as("should contains only extracted licenses").containsOnly("license1.lic");

        //when
        platformLicensesSetup.extractLicenses();

        //then
        assertThat(licensesFolder.listFiles()).extracting("name").as("should remove previous licenses").containsOnly("license2.lic");

    }

}
