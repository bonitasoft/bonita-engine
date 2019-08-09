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
package org.bonitasoft.engine.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.bonitasoft.engine.io.FileOperations.resource;

import java.io.IOException;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.page.PageSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.tenant.TenantResourceState;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class ProjectIT extends TestWithTechnicalUser {

    @Test
    public void should_deploy_all_project_artifacts() throws Exception {
        getApiClient().getProjectAPI().deployProject(createCompleteApp());

        String organization = getIdentityAPI().exportOrganization();
        assertThat(organization).contains("daniela.angelo");
        assertThat(organization).contains("april.sanchez");
        assertThat(organization).contains("helen.kelly");

        assertThat(getPageAPI()
                .searchPages(new SearchOptionsBuilder(0, 10).filter(PageSearchDescriptor.PROVIDED, false)
                        .sort(PageSearchDescriptor.NAME, Order.ASC).done())
                .getResult()).extracting("name").containsExactly("custompage_loanindex", "custompage_newForm");

        assertThat(getTenantAdministrationAPI().getBusinessDataModelResource().getState())
                .isEqualTo(TenantResourceState.INSTALLED);

        final long processDefinitionId = getProcessAPI().getProcessDefinitionId("myProcess", "1.0");
        assertThat(processDefinitionId).isGreaterThan(0L);

        // For now, the process is not resolved, as the organization is not deployed, so actor mapping cannot complete:
        //        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinitionId);
        //        assertThat(deploymentInfo.getConfigurationState()).isEqualTo(ConfigurationState.RESOLVED);
        //        assertThat(deploymentInfo.getActivationState()).isEqualTo(ActivationState.ENABLED);

        getIdentityAPI().deleteOrganization();
    }

    private byte[] createCompleteApp() throws IOException {
        return zip(
                //    file("deploy.json", resource("/complete_app/deploy.json")),
                file("organization/ACME.xml", resource("/complete_app/Organization_Data.xml")),
                //    file("applications/Application_Data.xml", resource("/complete_app/Application_Data.xml")),
                //    file("profiles/profile1.xml", resource("/complete_app/profile1.xml")),
                file("pages/page1.zip", resource("/complete_app/page1.zip")),
                file("processes/myProcess--1.0.bar", resource("/complete_app/myProcess--1.0.bar")),
                //    file("extensions/resourceNameRestAPI-1.0.0.zip",
                //            resource("/complete_app/resourceNameRestAPI-1.0.0.zip")),
                file("bdm/bdm.zip", resource("/complete_app/bdm.zip"))
        //    file("bdm/bdm_access_control.xml", resource("/complete_app/bdm_access_control.xml")),
        //    file("layouts/layout.zip", resource("/complete_app/layout.zip")),
        //    file("themes/custom-theme.zip", resource("/complete_app/custom-theme.zip"))
        );
    }

}
