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
import org.bonitasoft.engine.identity.User;
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
    public void searchApplications_can_filter_by_user_id() throws Exception {
        //given
        final User user1 = createUser("walter.bates", "bpm");
        final User user2 = createUser("helen.kelly", "bpm");
        final User user3 = createUser("daniela.angelo", "bpm");
        final User user4 = createUser("jan.fisher", "bpm");

        final List<Profile> profiles = getProfileAPI().searchProfiles(new SearchOptionsBuilder(0, 10).done())
                .getResult();

        getProfileAPI().createProfileMember(profiles.get(0).getId(), user1.getId(), null, null);
        getProfileAPI().createProfileMember(profiles.get(1).getId(), user2.getId(), null, null);
        getProfileAPI().createProfileMember(profiles.get(0).getId(), user3.getId(), null, null);
        getProfileAPI().createProfileMember(profiles.get(1).getId(), user3.getId(), null, null);

        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dashboard", "1.0")
                .setProfileId(profiles.get(0).getId());
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard",
                "Engineering dashboard", "1.0").setProfileId(profiles.get(0).getId());
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard",
                "1.0").setProfileId(profiles.get(1).getId());

        final Application hr = getApplicationAPI().createApplication(hrCreator);
        final Application engineering = getApplicationAPI().createApplication(engineeringCreator);
        final Application marketing = getApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builderUser1 = new SearchOptionsBuilder(0, 10);
        builderUser1.filter(ApplicationSearchDescriptor.USER_ID, user1.getId());
        final SearchResult<Application> applicationsUser1 = getApplicationAPI().searchApplications(builderUser1.done());
        assertThat(applicationsUser1).isNotNull();
        assertThat(applicationsUser1.getCount()).isEqualTo(2);
        assertThat(applicationsUser1.getResult()).containsExactly(hr, engineering);

        final SearchOptionsBuilder builderUser2 = new SearchOptionsBuilder(0, 10);
        builderUser2.filter(ApplicationSearchDescriptor.USER_ID, user2.getId());
        final SearchResult<Application> applicationsUser2 = getApplicationAPI().searchApplications(builderUser2.done());
        assertThat(applicationsUser2).isNotNull();
        assertThat(applicationsUser2.getCount()).isEqualTo(1);
        assertThat(applicationsUser2.getResult()).containsExactly(marketing);

        final SearchOptionsBuilder builderUser3 = new SearchOptionsBuilder(0, 10);
        builderUser3.filter(ApplicationSearchDescriptor.USER_ID, user3.getId());
        final SearchResult<Application> applicationsUser3 = getApplicationAPI().searchApplications(builderUser3.done());
        assertThat(applicationsUser3).isNotNull();
        assertThat(applicationsUser3.getCount()).isEqualTo(3);
        assertThat(applicationsUser3.getResult()).containsExactly(hr, engineering, marketing);

        final SearchOptionsBuilder builderUser4 = new SearchOptionsBuilder(0, 10);
        builderUser4.filter(ApplicationSearchDescriptor.USER_ID, user4.getId());
        final SearchResult<Application> applicationsUser4 = getApplicationAPI().searchApplications(builderUser4.done());
        assertThat(applicationsUser4.getResult()).isEmpty();
    }

    @Test
    public void searchApplications_can_filter_by_user_id_and_process_display_name() throws Exception {
        //given
        final User user1 = createUser("walter.bates", "bpm");

        final List<Profile> profiles = getProfileAPI().searchProfiles(new SearchOptionsBuilder(0, 10).done())
                .getResult();

        getProfileAPI().createProfileMember(profiles.get(0).getId(), user1.getId(), null, null);

        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dashboard", "1.0")
                .setProfileId(profiles.get(0).getId());
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard",
                "Engineering dashboard", "1.0").setProfileId(profiles.get(0).getId());
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard",
                "1.0").setProfileId(profiles.get(1).getId());

        final Application hr = getApplicationAPI().createApplication(hrCreator);
        final Application engineering = getApplicationAPI().createApplication(engineeringCreator);
        final Application marketing = getApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builderUser1 = new SearchOptionsBuilder(0, 10);
        builderUser1.filter(ApplicationSearchDescriptor.USER_ID, user1.getId());
        builderUser1.filter(ApplicationSearchDescriptor.DISPLAY_NAME, "Engineering dashboard");
        final SearchResult<Application> applicationsUser1 = getApplicationAPI().searchApplications(builderUser1.done());
        assertThat(applicationsUser1).isNotNull();
        assertThat(applicationsUser1.getCount()).isEqualTo(1);
        assertThat(applicationsUser1.getResult()).containsExactly(engineering);
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
