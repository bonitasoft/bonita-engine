/**
 * Copyright (C) 2023 Bonitasoft S.A.
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
package org.bonitasoft.engine.application.installer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.Optional;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.impl.application.installer.ApplicationArchive;
import org.bonitasoft.engine.api.impl.application.installer.ApplicationArchiveReader;
import org.bonitasoft.engine.api.impl.application.installer.ApplicationInstaller;
import org.bonitasoft.engine.api.impl.application.installer.detector.ArtifactTypeDetector;
import org.bonitasoft.engine.api.impl.application.installer.detector.BdmDetector;
import org.bonitasoft.engine.api.impl.application.installer.detector.CustomPageDetector;
import org.bonitasoft.engine.api.impl.application.installer.detector.LayoutDetector;
import org.bonitasoft.engine.api.impl.application.installer.detector.LivingApplicationDetector;
import org.bonitasoft.engine.api.impl.application.installer.detector.OrganizationDetector;
import org.bonitasoft.engine.api.impl.application.installer.detector.PageAndFormDetector;
import org.bonitasoft.engine.api.impl.application.installer.detector.ProcessDetector;
import org.bonitasoft.engine.api.impl.application.installer.detector.ThemeDetector;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.business.application.ApplicationNotFoundException;
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.service.ServiceAccessorSingleton;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class ApplicationInstallerIT extends CommonAPIIT {

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @After
    public void after() throws Exception {
        logoutOnTenant();
    }

    @Test
    public void custom_application_should_be_deployed_entirely() throws Exception {
        // ensure application did not exist initially:
        assertThatExceptionOfType(ApplicationNotFoundException.class)
                .isThrownBy(() -> getApplicationAPI().getApplicationByToken("appsManagerBonita"));

        // given:
        ApplicationInstaller applicationInstallerImpl = ServiceAccessorSingleton.getInstance()
                .lookup(ApplicationInstaller.class);
        final ApplicationArchiveReader applicationArchiveReader = new ApplicationArchiveReader(
                new ArtifactTypeDetector(new BdmDetector(),
                        new LivingApplicationDetector(), new OrganizationDetector(), new CustomPageDetector(),
                        new ProcessDetector(), new ThemeDetector(), new PageAndFormDetector(), new LayoutDetector()));

        // when:
        try (var applicationAsStream = ApplicationInstallerIT.class.getResourceAsStream("/customer-application.zip")) {
            var applicationArchive = applicationArchiveReader.read(applicationAsStream);
            applicationInstallerImpl.install(applicationArchive, "1.0.0");
        }

        // then:

        // Organization has been installed:
        final User captainBonita = getIdentityAPI().getUserByUserName("captainBonita");
        assertThat(captainBonita).isNotNull();
        assertThat(getIdentityAPI().getRoleByName("appsManager")).isNotNull();
        assertThat(getIdentityAPI().getGroupByPath("/appsManagement")).isNotNull();

        assertThat(getApplicationAPI().getApplicationByToken("appsManagerBonita").getDisplayName())
                .isEqualTo("Application manager");
        final long processDefinitionId = getProcessAPI().getProcessDefinitionId("CallHealthCheck", "1.0");
        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinitionId);
        assertThat(deploymentInfo.getConfigurationState()).isEqualTo(ConfigurationState.RESOLVED);
        assertThat(deploymentInfo.getActivationState()).isEqualTo(ActivationState.ENABLED);

        // Rest API Extension is there:
        assertThat(getPageAPI().getPageByName("custompage_processStarter")).isNotNull();

        // Pages are there:
        assertThat(getPageAPI().getPageByName("custompage_HealthPage")).isNotNull();

        // Layouts are there:
        assertThat(getPageAPI().getPageByName("custompage_pmLayout")).isNotNull();
    }

    @Test
    public void custom_application_should_be_installed_with_configuration() throws Exception {
        // given:
        ApplicationInstaller applicationInstallerImpl = ServiceAccessorSingleton.getInstance()
                .lookup(ApplicationInstaller.class);
        final ApplicationArchiveReader applicationArchiveReader = new ApplicationArchiveReader(
                new ArtifactTypeDetector(new BdmDetector(),
                        new LivingApplicationDetector(), new OrganizationDetector(), new CustomPageDetector(),
                        new ProcessDetector(), new ThemeDetector(), new PageAndFormDetector(), new LayoutDetector()));

        // when:
        try (var applicationAsStream = ApplicationInstallerIT.class
                .getResourceAsStream("/simple-app-1.0.0-SNAPSHOT-local.zip");
                var confStream = ApplicationInstallerIT.class
                        .getResourceAsStream("/simple-app-1.0.0-SNAPSHOT-local.bconf");) {
            var applicationArchive = applicationArchiveReader.read(applicationAsStream);
            applicationArchive.setConfigurationFile(Optional.of(confStream));
            applicationInstallerImpl.install(applicationArchive, "1.0.0-SNAPSHOT");
        }

        final long processDefinitionId = getProcessAPI().getProcessDefinitionId("Pool", "1.0");
        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinitionId);
        assertThat(deploymentInfo.getConfigurationState()).isEqualTo(ConfigurationState.RESOLVED);
        assertThat(deploymentInfo.getActivationState()).isEqualTo(ActivationState.ENABLED);
        var paramInstance = getProcessAPI().getParameterInstance(processDefinitionId, "hello");
        assertThat(paramInstance.getValue()).isEqualTo("world_post_install");
    }

    @Test
    public void empty_custom_application_should_throw_an_exception() throws Exception {
        // given:
        ApplicationInstaller applicationInstaller = ServiceAccessorSingleton.getInstance()
                .lookup(ApplicationInstaller.class);
        final ApplicationArchiveReader applicationArchiveReader = new ApplicationArchiveReader(
                new ArtifactTypeDetector(new BdmDetector(),
                        new LivingApplicationDetector(), new OrganizationDetector(), new CustomPageDetector(),
                        new ProcessDetector(), new ThemeDetector(), new PageAndFormDetector(),
                        new LayoutDetector()));

        try (var applicationAsStream = ApplicationInstallerIT.class
                .getResourceAsStream("/empty-customer-application.zip")) {
            final ApplicationArchive applicationArchive = applicationArchiveReader.read(applicationAsStream);

            // then:
            assertThatExceptionOfType(ApplicationInstallationException.class)
                    .isThrownBy(() -> applicationInstaller.install(applicationArchive, "1.0.0"))
                    .withMessage("The Application Archive contains no valid artifact to install");
        }
    }
}
