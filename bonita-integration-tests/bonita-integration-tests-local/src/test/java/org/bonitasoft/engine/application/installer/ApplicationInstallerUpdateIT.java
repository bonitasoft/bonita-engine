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

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.awaitility.Awaitility;
import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.impl.application.installer.ApplicationArchive;
import org.bonitasoft.engine.api.impl.application.installer.ApplicationArchiveReader;
import org.bonitasoft.engine.api.impl.application.installer.ApplicationInstaller;
import org.bonitasoft.engine.api.impl.application.installer.detector.*;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.exception.ApplicationInstallationException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageNotFoundException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.service.ServiceAccessorSingleton;
import org.bonitasoft.engine.tenant.TenantResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Haroun El Alami
 * @author Danila Mazour
 */
public class ApplicationInstallerUpdateIT extends CommonAPIIT {

    private ApplicationInstaller applicationInstaller;
    private ApplicationArchiveReader applicationArchiveReader;

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();

        applicationInstaller = ServiceAccessorSingleton.getInstance()
                .lookup(ApplicationInstaller.class);;
        applicationArchiveReader = new ApplicationArchiveReader(
                new ArtifactTypeDetector(new BdmDetector(),
                        new LivingApplicationDetector(), new OrganizationDetector(), new CustomPageDetector(),
                        new ProcessDetector(), new ThemeDetector(), new PageAndFormDetector(), new LayoutDetector()));

        initFirstInstall();
    }

    @After
    public void after() throws Exception {
        logoutOnTenant();
    }

    private void initFirstInstall() throws Exception {
        // ensure application did not exist initially:
        assertThatExceptionOfType(ApplicationNotFoundException.class)
                .isThrownBy(() -> getApplicationAPI().getApplicationByToken("appsManagerBonita"));

        // given:
        final InputStream applicationAsStream = this.getClass().getResourceAsStream("/customer-application.zip");

        // when:
        applicationInstaller.install(applicationArchiveReader.read(applicationAsStream), "1.0.0");

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
    public void empty_custom_application_should_throw_an_exception() throws Exception {
        // given:
        final InputStream applicationAsStream = this.getClass().getResourceAsStream("/empty-customer-application.zip");

        final ApplicationArchive applicationArchive = applicationArchiveReader.read(applicationAsStream);

        // then:
        assertThatExceptionOfType(ApplicationInstallationException.class)
                .isThrownBy(() -> applicationInstaller.update(applicationArchive, "1.0.1"))
                .withMessage("The Application Archive contains no valid artifact to install");
    }

    @Test
    public void process_update_custom_application_with_same_installed_version() throws ApplicationNotFoundException,
            PageNotFoundException, ProcessDefinitionNotFoundException, IOException, ApplicationInstallationException {
        // given:
        //installed resources
        TenantResource bdm = getTenantAdministrationAPI().getBusinessDataModelResource();
        Application application = getApplicationAPI().getApplicationByToken("appsManagerBonita");
        Page processStarterAPI = getPageAPI().getPageByName("custompage_processStarter");
        Page healthPage = getPageAPI().getPageByName("custompage_HealthPage");
        Page pmLayout = getPageAPI().getPageByName("custompage_pmLayout");
        final long processDefinitionId = getProcessAPI().getProcessDefinitionId("CallHealthCheck", "1.0");
        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinitionId);

        // when:
        try (var applicationAsStream = this.getClass().getResourceAsStream("/customer-application.zip")) {
            // Avoid test failure due to instant update.
            Awaitility.await().timeout(Duration.of(1, ChronoUnit.SECONDS));
            applicationInstaller.update(applicationArchiveReader.read(applicationAsStream), "1.0.0");
        }
        // then:
        TenantResource updatedBdm = getTenantAdministrationAPI().getBusinessDataModelResource();
        Application updatedApplication = getApplicationAPI().getApplicationByToken("appsManagerBonita");
        Page updatedProcessStarterAPI = getPageAPI().getPageByName("custompage_processStarter");
        Page updatedHealthPage = getPageAPI().getPageByName("custompage_HealthPage");
        Page updatedPmLayout = getPageAPI().getPageByName("custompage_pmLayout");
        final ProcessDeploymentInfo deploymentInfoAfterUpdate = getProcessAPI()
                .getProcessDeploymentInfo(processDefinitionId);

        // check that resources has been updated
        assertThat(updatedBdm.getLastUpdateDate().toEpochSecond() > bdm.getLastUpdateDate().toEpochSecond()).isTrue();
        assertThat(updatedApplication.getLastUpdateDate().after(application.getLastUpdateDate())).isTrue();
        assertThat(
                updatedProcessStarterAPI.getLastModificationDate().after(processStarterAPI.getLastModificationDate()))
                        .isTrue();
        assertThat(updatedHealthPage.getLastModificationDate().after(healthPage.getLastModificationDate())).isTrue();
        assertThat(updatedPmLayout.getLastModificationDate().after(pmLayout.getLastModificationDate())).isTrue();

        // CallHealthCheck Process must not be updated
        assertThat(deploymentInfoAfterUpdate.getLastUpdateDate()).isEqualTo(deploymentInfo.getLastUpdateDate());
    }

    @Test
    public void process_update_custom_application_with_new_version()
            throws ApplicationNotFoundException, PageNotFoundException, ProcessDefinitionNotFoundException, IOException,
            ApplicationInstallationException, SearchException {
        // given:
        //installed resources
        TenantResource bdm = getTenantAdministrationAPI().getBusinessDataModelResource();
        Application application = getApplicationAPI().getApplicationByToken("appsManagerBonita");
        Page processStarterAPI = getPageAPI().getPageByName("custompage_processStarter");
        Page healthPage = getPageAPI().getPageByName("custompage_HealthPage");
        Page pmLayout = getPageAPI().getPageByName("custompage_pmLayout");
        final long processDefinitionId = getProcessAPI().getProcessDefinitionId("CallHealthCheck", "1.0");
        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinitionId);

        // when:
        final InputStream applicationAsStream = this.getClass().getResourceAsStream("/customer-application-v2.zip");
        applicationInstaller.update(applicationArchiveReader.read(applicationAsStream), "1.0.1");

        // then:
        TenantResource updatedBdm = getTenantAdministrationAPI().getBusinessDataModelResource();
        Application updatedApplication = getApplicationAPI().getApplicationByToken("appsManagerBonita");
        Page updatedProcessStarterAPI = getPageAPI().getPageByName("custompage_processStarter");
        Page updatedHealthPage = getPageAPI().getPageByName("custompage_HealthPage");
        Page updatedPmLayout = getPageAPI().getPageByName("custompage_pmLayout");
        final ProcessDeploymentInfo deploymentInfoAfterUpdate = getProcessAPI()
                .getProcessDeploymentInfo(processDefinitionId);

        // check that resources has been updated
        assertThat(updatedBdm.getLastUpdateDate().toEpochSecond() > bdm.getLastUpdateDate().toEpochSecond()).isTrue();
        // check installed apps
        assertThat(updatedApplication.getLastUpdateDate().after(application.getLastUpdateDate())).isTrue();
        // fetch application menus
        ApplicationPage healthzPage = getApplicationAPI().searchApplicationPages(
                new SearchOptionsBuilder(0, Integer.MAX_VALUE)
                        .filter("applicationId", (Long) updatedApplication.getId())
                        .done())
                .getResult().get(0);
        assertThat(healthzPage.getToken()).isEqualTo("healthz");

        // new app installed
        assertThat(getPageAPI().getPageByName("custompage_reportsPage")).isNotNull();

        // check updated custom pages
        assertThat(
                updatedProcessStarterAPI.getLastModificationDate().after(processStarterAPI.getLastModificationDate()))
                        .isTrue();
        assertThat(updatedProcessStarterAPI.getContentName()).isEqualTo("processStarter-1.1.zip");
        assertThat(updatedHealthPage.getLastModificationDate().after(healthPage.getLastModificationDate())).isTrue();
        assertThat(updatedHealthPage.getContentName()).isEqualTo("page_HealthPage.zip");
        assertThat(updatedPmLayout.getLastModificationDate().after(pmLayout.getLastModificationDate())).isTrue();
        assertThat(updatedPmLayout.getContentName()).isEqualTo("layout_pmLayout.zip");

        // CallHealthCheck Process must not be updated
        assertThat(deploymentInfoAfterUpdate.getLastUpdateDate().after(deploymentInfo.getLastUpdateDate())).isTrue();
        assertThat(deploymentInfoAfterUpdate.getActivationState().name()).isEqualTo("DISABLED");
    }
}
