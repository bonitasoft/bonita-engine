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
import static org.bonitasoft.engine.service.impl.PlatformLicensesSetup.BONITA_CLIENT_HOME;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bonitasoft.engine.home.BonitaHomeServer;
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
    public final ClearSystemProperties bonitaClientHome = new ClearSystemProperties(BONITA_CLIENT_HOME);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Mock
    ConfigurationServiceImpl configurationService;

    @Spy
    PlatformLicensesSetup platformLicensesSetup;

    private LicenseAnswer licenseAnswer;
    private File licensesFolder;

    @Before
    public void before() throws Exception {
        licenseAnswer = new LicenseAnswer();
        doReturn(configurationService).when(platformLicensesSetup).getConfigurationService();
        licensesFolder = BonitaHomeServer.getInstance().getLicensesFolder();
        FileUtils.deleteDirectory(licensesFolder);
    }

    private final class LicenseAnswer implements Answer<List<BonitaConfiguration>> {

        int i = 0;

        @Override
        public List<BonitaConfiguration> answer(final InvocationOnMock invocation) {
            i++;
            List<BonitaConfiguration> bonitaConfigurations = new ArrayList<>();
            final String resourceName = "license" + i + ".lic";
            bonitaConfigurations
                    .add(new BonitaConfiguration(resourceName, ("license " + i + " content").getBytes()));
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
        assertThat(System.getProperty(BONITA_CLIENT_HOME)).as("should set system property in temp folder, depends on OS")
                .isNotNull()
                .contains("licenses");

        assertThat(new File(System.getProperty(BONITA_CLIENT_HOME)).listFiles()).extracting("name").contains("license1.lic");
    }

    @Test
    public void should_setup_licenses_folder_do_not_set_bonita_client_home_even_if_there_is_no_licenses() throws Exception {
        //given
        doReturn(Collections.EMPTY_LIST).when(configurationService).getLicenses();

        //when
        platformLicensesSetup.setupLicenses();

        //then
        assertThat(System.getProperties()).doesNotContainKey(BONITA_CLIENT_HOME);
    }

    @Test
    public void should_not_overwrite_bonita_client_home_system_property() throws Exception {
        //given
        System.setProperty(BONITA_CLIENT_HOME, "existing_licenses_folder");
        when(configurationService.getLicenses()).thenAnswer(licenseAnswer);

        //when
        platformLicensesSetup.setupLicenses();

        //then
        assertThat(System.getProperty(BONITA_CLIENT_HOME)).as("should not modify system property").isEqualTo("existing_licenses_folder");
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
    public void should_extract_licenses_do_not_extract_it_if_already_done() throws Exception {
        //given
        when(configurationService.getLicenses()).thenAnswer(licenseAnswer);

        //when
        platformLicensesSetup.setupLicenses();

        //then
        assertThat(licensesFolder).isDirectory().exists();
        assertThat(System.getProperty(BONITA_CLIENT_HOME)).isEqualTo(licensesFolder.getAbsolutePath());
        assertThat(licensesFolder.listFiles()).extracting("name").as("should contains only extracted licenses").containsOnly("license1.lic");

        //when
        platformLicensesSetup.setupLicenses();

        //then: licenses were not replace by new one: it was already done
        assertThat(licensesFolder.listFiles()).extracting("name").as("should remove previous licenses").containsOnly("license1.lic");
    }

    @Test
    public void should_extract_extract_a_second_time_if_folder_is_deleted() throws Exception {
        //given
        when(configurationService.getLicenses()).thenAnswer(licenseAnswer);
        platformLicensesSetup.setupLicenses();
        assertThat(licensesFolder.listFiles()).extracting("name").as("should contains only extracted licenses").containsOnly("license1.lic");
        FileUtils.deleteDirectory(licensesFolder);
        //when
        platformLicensesSetup.setupLicenses();

        //then: licenses were not replace by new one: it was already done
        assertThat(licensesFolder.listFiles()).extracting("name").as("should remove previous licenses").containsOnly("license2.lic");
    }

    @Test
    public void should_extract_extract_a_second_time_if_folder_is_empty() throws Exception {
        //given
        when(configurationService.getLicenses()).thenAnswer(licenseAnswer);
        platformLicensesSetup.setupLicenses();
        assertThat(licensesFolder.listFiles()).extracting("name").as("should contains only extracted licenses").containsOnly("license1.lic");
        for (File file : licensesFolder.listFiles()) {
            file.delete();
        }
        //when
        platformLicensesSetup.setupLicenses();

        //then: licenses were not replace by new one: it was already done
        assertThat(licensesFolder.listFiles()).extracting("name").as("should remove previous licenses").containsOnly("license2.lic");
    }

}
