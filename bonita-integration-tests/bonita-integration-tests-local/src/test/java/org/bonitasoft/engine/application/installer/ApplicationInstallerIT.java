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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import java.io.InputStream;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.impl.application.installer.ApplicationInstaller;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.business.application.ApplicationNotFoundException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.service.TenantServiceSingleton;
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
        assertThatThrownBy(() -> getApplicationAPI().getApplicationByToken("appsManagerBonita"))
                .isInstanceOf(ApplicationNotFoundException.class);
        //        logoutOnTenant();
        // given:
        // name is NOT the default one so that it is not deployed automatically for the whole test suite at startup:
        final InputStream applicationAsStream = this.getClass().getResourceAsStream("/customer-application.zip");
        ApplicationInstaller applicationInstallerImpl = TenantServiceSingleton.getInstance()
                .lookup(ApplicationInstaller.class);

        // when:
        applicationInstallerImpl.install(applicationAsStream);

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

}
