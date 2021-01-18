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
package org.bonitasoft.engine.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.io.FileAndContentUtils.file;
import static org.bonitasoft.engine.io.FileAndContentUtils.zip;
import static org.bonitasoft.engine.io.FileOperations.resource;

import java.io.IOException;
import java.util.List;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.result.ExecutionResult;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationCreator;
import org.bonitasoft.engine.business.application.ApplicationSearchDescriptor;
import org.bonitasoft.engine.page.ContentType;
import org.bonitasoft.engine.page.PageSearchDescriptor;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Emmanuel Duchastenier
 */
public class ApplicationIT extends TestWithTechnicalUser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationIT.class);

    @Test
    @Ignore("API not released")
    public void should_deploy_all_artifacts_from_application() throws Exception {
        // given:
        final byte[] completeApplicationZip = createCompleteApplication();

        // when:
        ExecutionResult result = null;
        //getApiClient().getApplicationAPI().deployApplication(completeApplicationZip);

        // then:
        assertThat(result.hasErrors()).isFalse();

        result.getAllStatus().forEach(s -> LOGGER.info(s.toString()));

        assertThat(getPageAPI()
                .searchPages(new SearchOptionsBuilder(0, 10)
                        .filter(PageSearchDescriptor.PROVIDED, false)
                        .and()
                        .leftParenthesis()
                        .filter(PageSearchDescriptor.CONTENT_TYPE, ContentType.PAGE)
                        .or()
                        .filter(PageSearchDescriptor.CONTENT_TYPE, ContentType.FORM)
                        .rightParenthesis()
                        .sort(PageSearchDescriptor.NAME, Order.ASC).done())
                .getResult()).extracting("name").containsExactly("custompage_loanindex", "custompage_newForm");

        assertThat(getPageAPI()
                .searchPages(new SearchOptionsBuilder(0, 10)
                        .filter(PageSearchDescriptor.PROVIDED, false)
                        .filter(PageSearchDescriptor.CONTENT_TYPE, ContentType.THEME)
                        .done())
                .getResult()).extracting("name").containsExactly("custompage_customtheme");

        assertThat(getPageAPI()
                .searchPages(new SearchOptionsBuilder(0, 10)
                        .filter(PageSearchDescriptor.PROVIDED, false)
                        .filter(PageSearchDescriptor.CONTENT_TYPE, ContentType.LAYOUT)
                        .done())
                .getResult()).extracting("name").containsExactly("custompage_customlayout");

        assertThat(getPageAPI()
                .searchPages(new SearchOptionsBuilder(0, 10)
                        .filter(PageSearchDescriptor.PROVIDED, false)
                        .filter(PageSearchDescriptor.CONTENT_TYPE, ContentType.API_EXTENSION)
                        .done())
                .getResult()).extracting("name").containsExactly("custompage_resourceNameRestAPI");

        final long processDefinitionId = getProcessAPI().getProcessDefinitionId("myProcess", "1.0");
        assertThat(processDefinitionId).isGreaterThan(0L);

        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinitionId);
        assertThat(deploymentInfo.getConfigurationState()).isEqualTo(ConfigurationState.UNRESOLVED);
        assertThat(deploymentInfo.getActivationState()).isEqualTo(ActivationState.DISABLED);

        List<Application> insertedApplicationList = getLivingApplicationAPI()
                .searchApplications(getApplicationSearchOptionsOrderByToken(0, 10).done()).getResult();
        assertThat(insertedApplicationList).hasSize(2);
        assertThat(insertedApplicationList.get(0).getDisplayName()).isEqualToIgnoringCase("Loan request"); // token LoanApp
        assertThat(insertedApplicationList.get(1).getDisplayName()).isEqualToIgnoringCase("Leave request application"); // token Tahiti

        // We must be able to redeploy the same application:
        //        result = getApiClient().getApplicationAPI().deployApplication(completeApplicationZip);

        // then:
        assertThat(result.hasErrors()).isFalse();

        result.getAllStatus().forEach(s -> LOGGER.info(s.toString()));

    }

    @Test
    public void searchApplications_can_filter_by_profile_ids() throws Exception {
        //given
        final List<Profile> profiles = getProfileAPI().searchProfiles(new SearchOptionsBuilder(0, 10).done())
                .getResult();

        final Application hr = getApplicationAPI()
                .createApplication(new ApplicationCreator("HR-dashboard", "HR dashboard", "1.0")
                        .setProfileId(profiles.get(0).getId()));
        final Application engineering = getApplicationAPI()
                .createApplication(new ApplicationCreator("Engineering-dashboard",
                        "Engineering dashboard", "1.0").setProfileId(profiles.get(0).getId()));
        final Application marketing = getApplicationAPI()
                .createApplication(new ApplicationCreator("Marketing-dashboard", "Marketing dashboard",
                        "1.0").setProfileId(profiles.get(1).getId()));
        getApplicationAPI().createApplication(new ApplicationCreator("AppNotConnected", "App not connected",
                "1.0"));

        //when
        final SearchOptionsBuilder builderProfile1 = new SearchOptionsBuilder(0, 10);
        builderProfile1.filter(ApplicationSearchDescriptor.PROFILE_ID, profiles.get(0).getId());
        builderProfile1.sort(ApplicationSearchDescriptor.DISPLAY_NAME, Order.ASC);
        final SearchResult<Application> applicationsProfile1 = getApplicationAPI()
                .searchApplications(builderProfile1.done());
        assertThat(applicationsProfile1).isNotNull();
        assertThat(applicationsProfile1.getCount()).isEqualTo(2);
        assertThat(applicationsProfile1.getResult()).containsExactly(engineering, hr);

        final SearchOptionsBuilder builderProfile2 = new SearchOptionsBuilder(0, 10);
        builderProfile2.filter(ApplicationSearchDescriptor.PROFILE_ID, profiles.get(1).getId());
        builderProfile2.sort(ApplicationSearchDescriptor.DISPLAY_NAME, Order.ASC);
        final SearchResult<Application> applicationsProfile2 = getApplicationAPI()
                .searchApplications(builderProfile2.done());
        assertThat(applicationsProfile2).isNotNull();
        assertThat(applicationsProfile2.getCount()).isEqualTo(1);
        assertThat(applicationsProfile2.getResult()).containsExactly(marketing);

        final SearchOptionsBuilder builderProfile3 = new SearchOptionsBuilder(0, 10);
        builderProfile3.filter(ApplicationSearchDescriptor.PROFILE_ID,
                profiles.get(0).getId() + "," + profiles.get(1).getId() + "," + "500");
        builderProfile3.sort(ApplicationSearchDescriptor.DISPLAY_NAME, Order.ASC);
        final SearchResult<Application> applicationsProfile3 = getApplicationAPI()
                .searchApplications(builderProfile3.done());
        assertThat(applicationsProfile3).isNotNull();
        assertThat(applicationsProfile3.getCount()).isEqualTo(3);
        assertThat(applicationsProfile3.getResult()).containsExactly(engineering, hr, marketing);

        final SearchOptionsBuilder builderProfile4 = new SearchOptionsBuilder(0, 10);
        builderProfile4.filter(ApplicationSearchDescriptor.PROFILE_ID, "500");
        builderProfile4.sort(ApplicationSearchDescriptor.DISPLAY_NAME, Order.ASC);
        final SearchResult<Application> applicationsProfile4 = getApplicationAPI()
                .searchApplications(builderProfile4.done());
        assertThat(applicationsProfile4.getResult()).isEmpty();
    }

    @Test
    public void searchApplications_can_filter_by_profile_ids_and_profile_display_name() throws Exception {
        //given
        final List<Profile> profiles = getProfileAPI().searchProfiles(new SearchOptionsBuilder(0, 10).done())
                .getResult();

        getApplicationAPI().createApplication(new ApplicationCreator("HR-dashboard", "HR dashboard", "1.0")
                .setProfileId(profiles.get(0).getId()));
        final Application engineering = getApplicationAPI()
                .createApplication(new ApplicationCreator("Engineering-dashboard",
                        "Engineering dashboard", "1.0").setProfileId(profiles.get(0).getId()));
        getApplicationAPI().createApplication(new ApplicationCreator("Marketing-dashboard", "Marketing dashboard",
                "1.0").setProfileId(profiles.get(1).getId()));
        getApplicationAPI().createApplication(new ApplicationCreator("AppNotConnected", "App not connected",
                "1.0"));

        //when
        final SearchOptionsBuilder builderProfile1 = new SearchOptionsBuilder(0, 10);
        builderProfile1.filter(ApplicationSearchDescriptor.PROFILE_ID,
                profiles.get(0).getId() + "," + profiles.get(1).getId() + "," + "500");
        builderProfile1.filter(ApplicationSearchDescriptor.DISPLAY_NAME, "Engineering dashboard");
        final SearchResult<Application> applicationsProfile1 = getApplicationAPI()
                .searchApplications(builderProfile1.done());
        assertThat(applicationsProfile1).isNotNull();
        assertThat(applicationsProfile1.getCount()).isEqualTo(1);
        assertThat(applicationsProfile1.getResult()).containsExactly(engineering);
    }

    private byte[] createCompleteApplication() throws IOException {
        return zip(
                file("applications/Application_Data.xml", resource("/complete_app/Application_Data.xml")),
                file("pages/page1.zip", resource("/complete_app/page1.zip")),
                file("processes/myProcess--1.0.bar", resource("/complete_app/myProcess--1.0.bar")),
                file("extensions/resourceNameRestAPI-1.0.0.zip",
                        resource("/complete_app/resourceNameRestAPI-1.0.0.zip")),
                file("layouts/layout.zip", resource("/complete_app/layout.zip")),
                file("themes/custom-theme.zip", resource("/complete_app/custom-theme.zip")));
    }

    private SearchOptionsBuilder getApplicationSearchOptionsOrderByToken(final int startIndex, final int maxResults) {
        return new SearchOptionsBuilder(startIndex, maxResults).sort(ApplicationSearchDescriptor.TOKEN, Order.ASC);
    }

}
