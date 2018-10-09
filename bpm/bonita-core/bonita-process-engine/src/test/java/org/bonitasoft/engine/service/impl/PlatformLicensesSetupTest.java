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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.service.impl.PlatformLicensesSetup.BONITA_CLIENT_HOME;
import static org.mockito.Mockito.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
import org.mockito.junit.MockitoJUnitRunner;
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
    private ConfigurationServiceImpl configurationService;

    @Spy
    private PlatformLicensesSetup platformLicensesSetup;

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

        private int count = 0;

        @Override
        public List<BonitaConfiguration> answer(final InvocationOnMock invocation) {
            count++;
            List<BonitaConfiguration> bonitaConfigurations = new ArrayList<>();
            final String resourceName = "license" + count + ".lic";
            bonitaConfigurations
                    .add(new BonitaConfiguration(resourceName, ("license " + count + " content").getBytes()));
            return bonitaConfigurations;
        }

    }

    @Test
    public void should_setup_licenses_folder_when_bonitaHomeClient_is_not_set() throws Exception {
        //given
        doReturn(singletonList(
                new BonitaConfiguration("license1.lic", "licence content".getBytes(StandardCharsets.UTF_8))))
                        .when(configurationService).getLicenses();

        //when
        platformLicensesSetup.setupLicenses();

        //then
        assertThat(System.getProperty(BONITA_CLIENT_HOME)).as("should set system property in temp folder, depends on OS")
                .isNotNull()
                .contains("licenses");

        assertThat(new File(System.getProperty(BONITA_CLIENT_HOME)).listFiles()).extracting("name").contains("license1.lic");
    }

    @Test
    public void should_not_set_the_bonita_client_home_if_there_is_no_licenses() throws Exception {
        //given
        doReturn(emptyList()).when(configurationService).getLicenses();

        //when
        platformLicensesSetup.setupLicenses();

        //then
        assertThat(System.getProperties()).doesNotContainKey(BONITA_CLIENT_HOME);
    }

    @Test
    public void should_not_overrride_bonita_client_home_if_it_is_already_set() throws Exception {
        //given
        System.setProperty(BONITA_CLIENT_HOME, "existing_licenses_folder");
        when(configurationService.getLicenses()).thenAnswer(licenseAnswer);

        //when
        platformLicensesSetup.setupLicenses();

        //then
        assertThat(System.getProperty(BONITA_CLIENT_HOME)).as("should not override system property")
                .isEqualTo("existing_licenses_folder");
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
    public void should_not_extract_licenses_if_already_done() throws Exception {
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
    public void should_extract_license_a_second_time_when_calling_force_setup_licenses() throws Exception {
        //given
        when(configurationService.getLicenses()).thenAnswer(licenseAnswer);
        platformLicensesSetup.setupLicenses();
        assertThat(licensesFolder.listFiles()).extracting("name").as("initial extracted licenses")
                .containsOnly("license1.lic");

        //when
        platformLicensesSetup.forceSetupLicenses();

        //then: licenses were not replace by new one: it was already done
        assertThat(licensesFolder.listFiles()).extracting("name").as("licenses after reset and new retrieval")
                .containsOnly("license1.lic", "license2.lic");
    }

}
