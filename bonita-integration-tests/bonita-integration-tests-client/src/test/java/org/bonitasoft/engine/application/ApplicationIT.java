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

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.permission.APICallContext;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationCreator;
import org.bonitasoft.engine.business.application.ApplicationImportPolicy;
import org.bonitasoft.engine.business.application.ApplicationSearchDescriptor;
import org.bonitasoft.engine.business.application.IApplication;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class ApplicationIT extends TestWithTechnicalUser {

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
        builderUser1.sort(ApplicationSearchDescriptor.DISPLAY_NAME, Order.ASC);
        final SearchResult<IApplication> applicationsUser1 = getApplicationAPI()
                .searchIApplications(builderUser1.done());
        assertThat(applicationsUser1).isNotNull();
        assertThat(applicationsUser1.getResult()).contains(engineering, hr).doesNotContain(marketing);

        final SearchOptionsBuilder builderUser2 = new SearchOptionsBuilder(0, 10);
        builderUser2.filter(ApplicationSearchDescriptor.USER_ID, user2.getId());
        builderUser2.sort(ApplicationSearchDescriptor.DISPLAY_NAME, Order.ASC);
        final SearchResult<IApplication> applicationsUser2 = getApplicationAPI()
                .searchIApplications(builderUser2.done());
        assertThat(applicationsUser2).isNotNull();
        assertThat(applicationsUser2.getResult()).contains(marketing).doesNotContain(engineering, hr);

        final SearchOptionsBuilder builderUser3 = new SearchOptionsBuilder(0, 10);
        builderUser3.filter(ApplicationSearchDescriptor.USER_ID, user3.getId());
        builderUser3.sort(ApplicationSearchDescriptor.DISPLAY_NAME, Order.ASC);
        final SearchResult<IApplication> applicationsUser3 = getApplicationAPI()
                .searchIApplications(builderUser3.done());
        assertThat(applicationsUser3).isNotNull();
        assertThat(applicationsUser3.getResult()).contains(engineering, hr, marketing);

        final SearchOptionsBuilder builderUser4 = new SearchOptionsBuilder(0, 10);
        builderUser4.filter(ApplicationSearchDescriptor.USER_ID, user4.getId());
        builderUser4.sort(ApplicationSearchDescriptor.DISPLAY_NAME, Order.ASC);
        final SearchResult<IApplication> applicationsUser4 = getApplicationAPI()
                .searchIApplications(builderUser4.done());
        assertThat(applicationsUser4.getResult()).isEmpty();

        // Let's check SAM (=tenant admin), has access to applications mapped to "_BONITA_INTERNAL_PROFILE_SUPER_ADMIN":
        final List<ImportStatus> importStatus = getLivingApplicationAPI().importApplications(
                IOUtils.toByteArray(ApplicationIT.class.getResourceAsStream("superAdminApp.xml")),
                ApplicationImportPolicy.FAIL_ON_DUPLICATES);
        assertThat(importStatus).isNotEmpty().allMatch(status -> status.getStatus().equals(ImportStatus.Status.ADDED));
        final SearchOptionsBuilder soSystemAdmin = new SearchOptionsBuilder(0, 10);
        soSystemAdmin.filter(ApplicationSearchDescriptor.USER_ID, "-1"); // -1 is userId for SAM (= tenant admin)
        final SearchResult<IApplication> samApplications = getApplicationAPI()
                .searchIApplications(soSystemAdmin.done());
        final List<IApplication> applications = samApplications.getResult();
        assertThat(applications).hasSize(1);
        assertThat(applications.get(0).getToken()).isEqualTo("superAdminAppBonita");

        getApplicationAPI().deleteApplication(applications.get(0).getId());
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
        final SearchResult<IApplication> applicationsUser1 = getApplicationAPI()
                .searchIApplications(builderUser1.done());
        assertThat(applicationsUser1).isNotNull();
        assertThat(applicationsUser1.getCount()).isEqualTo(1);
        assertThat(applicationsUser1.getResult()).containsExactly(engineering);
    }

    @Test
    public void should_access_identity_api_using_default_application_permissions() throws Exception {

        // Uses page-to-test-permissions.zip
        // Uses application-to-test-permissions.zip
        User user = createUser("baptiste", "bpm");
        loginOnDefaultTenantWith("baptiste", "bpm");

        assertThat(getPermissionAPI().isAuthorized(new APICallContext("GET", "identity", "user", null))).isFalse();

        getProfileAPI().createProfileMember(
                getProfileAPI().searchProfiles(new SearchOptionsBuilder(0, 1).searchTerm("User").done()).getResult()
                        .get(0).getId(),
                user.getId(),
                -1L,
                -1L);

        loginOnDefaultTenantWith("baptiste", "bpm");

        long userId = user.getId();
        assertThat(
                getPermissionAPI().isAuthorized(new APICallContext("GET", "identity", "user", String.valueOf(userId))))
                        .isTrue();
        assertThat(getPermissionAPI()
                .isAuthorized(new APICallContext("GET", "identity", "user", String.valueOf(userId + 1)))).isFalse();
        assertThat(getPermissionAPI().isAuthorized(new APICallContext("GET", "identity", "user", null))).isFalse();

        getProfileAPI().createProfileMember(
                getProfileAPI().searchProfiles(new SearchOptionsBuilder(0, 1).searchTerm("Admin").done()).getResult()
                        .get(0).getId(),
                user.getId(),
                -1L,
                -1L);

        loginOnDefaultTenantWith("baptiste", "bpm");

        assertThat(getPermissionAPI().isAuthorized(new APICallContext("GET", "identity", "user", null))).isTrue();
    }

}
