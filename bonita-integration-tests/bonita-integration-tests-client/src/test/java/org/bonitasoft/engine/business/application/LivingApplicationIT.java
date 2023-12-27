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
package org.bonitasoft.engine.business.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class LivingApplicationIT extends TestWithLivingApplication {

    @Test
    public void createApplication_returns_application_based_on_ApplicationCreator_information() throws Exception {
        //given
        final Profile profile = getProfileUser();
        Page defaultLayout = getPageAPI().getPageByName(DEFAULT_LAYOUT_NAME);
        Page defaultTheme = getPageAPI().getPageByName(DEFAULT_THEME_NAME);
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name",
                "1.0");
        creator.setDescription("This is my application");
        creator.setIconPath("/icon.jpg");
        creator.setProfileId(profile.getId());

        //when
        final Application application = getLivingApplicationAPI().createApplication(creator);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getToken()).isEqualTo("My-Application");
        assertThat(application.getDisplayName()).isEqualTo("My application display name");
        assertThat(application.getVersion()).isEqualTo("1.0");
        assertThat(application.getId()).isGreaterThan(0);
        assertThat(application.getDescription()).isEqualTo("This is my application");
        assertThat(application.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(application.getCreatedBy()).isEqualTo(getUser().getId());
        assertThat(application.getUpdatedBy()).isEqualTo(getUser().getId());
        assertThat(application.getHomePageId()).isNull();
        assertThat(application.getProfileId()).isEqualTo(profile.getId());
        assertThat(application.getLayoutId()).isEqualTo(defaultLayout.getId());
        assertThat(application.getThemeId()).isEqualTo(defaultTheme.getId());

        getLivingApplicationAPI().deleteApplication(application.getId());
    }

    @Test
    public void createApplication_without_profile_should_have_null_profileId() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name",
                "1.0");

        //when
        final Application application = getLivingApplicationAPI().createApplication(creator);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getProfileId()).isNull();
    }

    @Test
    public void updateApplication_should_return_application_up_to_date() throws Exception {
        //given
        final Profile profile = getProfileUser();
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name",
                "1.0");
        final Application application = getLivingApplicationAPI().createApplication(creator);

        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setToken("My-updated-app");
        updater.setDisplayName("Updated display name");
        updater.setVersion("1.1");
        updater.setDescription("Up description");
        updater.setIconPath("/newIcon.jpg");
        updater.setProfileId(profile.getId());
        updater.setState(ApplicationState.ACTIVATED.name());

        //when
        final Application updatedApplication = getLivingApplicationAPI().updateApplication(application.getId(),
                updater);

        //then
        assertThat(updatedApplication).isNotNull();
        assertThat(updatedApplication.getToken()).isEqualTo("My-updated-app");
        assertThat(updatedApplication.getDisplayName()).isEqualTo("Updated display name");
        assertThat(updatedApplication.getVersion()).isEqualTo("1.1");
        assertThat(updatedApplication.getDescription()).isEqualTo("Up description");
        assertThat(updatedApplication.getIconPath()).isEqualTo("/newIcon.jpg");
        assertThat(updatedApplication.getProfileId()).isEqualTo(profile.getId());
        assertThat(updatedApplication.getState()).isEqualTo(ApplicationState.ACTIVATED.name());
        assertThat(updatedApplication).isEqualTo(getLivingApplicationAPI().getApplication(application.getId()));

        getLivingApplicationAPI().deleteApplication(application.getId());
    }

    @Test
    public void getApplication_returns_application_with_the_given_id() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name",
                "1.0");
        final Application createdApp = getLivingApplicationAPI().createApplication(creator);
        assertThat(createdApp).isNotNull();

        //when
        final Application retrievedApp = getLivingApplicationAPI().getApplication(createdApp.getId());

        //then
        assertThat(retrievedApp).isEqualTo(createdApp);
    }

    @Test
    public void getApplicationByToken_returns_application_with_the_given_token() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name",
                "1.0");
        final Application createdApp = getLivingApplicationAPI().createApplication(creator);
        assertThat(createdApp).isNotNull();

        //when
        final Application retrievedApp = getLivingApplicationAPI().getApplicationByToken(createdApp.getToken());

        //then
        assertThat(retrievedApp).isEqualTo(createdApp);
    }

    @Test
    public void deleteApplication_should_delete_application_with_the_given_id() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name",
                "1.0");
        final Application createdApp = getLivingApplicationAPI().createApplication(creator);
        assertThat(createdApp).isNotNull();

        //when
        getLivingApplicationAPI().deleteApplication(createdApp.getId());

        //then
        try {
            getLivingApplicationAPI().getApplication(createdApp.getId());
            fail("Not found exception");
        } catch (final NotFoundException e) {
            //ok
        }
    }

    @Test
    public void searchApplications_without_filter_return_all_elements_based_on_pagination() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("AAA_HR-dashboard", "HR dash board", "1.0");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("AAA_Engineering-dashboard",
                "Engineering dashboard", "1.0");
        final ApplicationCreator marketingCreator = new ApplicationCreator("AAA_Marketing-dashboard",
                "Marketing dashboard",
                "1.0");

        final Application hr = getLivingApplicationAPI().createApplication(hrCreator);
        final Application engineering = getLivingApplicationAPI().createApplication(engineeringCreator);
        final Application marketing = getLivingApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchResult<Application> firstPage = getLivingApplicationAPI()
                .searchApplications(buildSearchOptions("AAA", 0, 2));

        //then
        assertThat(firstPage).isNotNull();
        assertThat(firstPage.getCount()).isEqualTo(3);
        assertThat(firstPage.getResult()).containsExactly(engineering, hr);

        //when
        final SearchResult<Application> secondPage = getLivingApplicationAPI()
                .searchApplications(buildSearchOptions("AAA", 2, 2));

        //then
        assertThat(secondPage).isNotNull();
        assertThat(secondPage.getCount()).isEqualTo(3);
        assertThat(secondPage.getResult()).containsExactly(marketing);
    }

    @Test
    public void searchApplications_can_filter_on_name() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dash board", "1.0");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard",
                "Engineering dashboard", "1.0");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard",
                "1.0");

        getLivingApplicationAPI().createApplication(hrCreator);
        final Application engineering = getLivingApplicationAPI().createApplication(engineeringCreator);
        getLivingApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getAppSearchBuilderOrderByToken(0, 10);
        builder.filter(ApplicationSearchDescriptor.TOKEN, "Engineering-dashboard");

        final SearchResult<Application> applications = getLivingApplicationAPI().searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(1);
        assertThat(applications.getResult()).containsExactly(engineering);
    }

    @Test
    public void searchApplications_can_filter_on_display_name() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dashboard", "1.0");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard",
                "Engineering dashboard", "1.0");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard",
                "1.0");

        final Application hr = getLivingApplicationAPI().createApplication(hrCreator);
        getLivingApplicationAPI().createApplication(engineeringCreator);
        getLivingApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getAppSearchBuilderOrderByToken(0, 10);
        builder.filter(ApplicationSearchDescriptor.DISPLAY_NAME, "HR dashboard");

        final SearchResult<Application> applications = getLivingApplicationAPI().searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(1);
        assertThat(applications.getResult()).containsExactly(hr);
    }

    @Test
    public void searchApplications_can_filter_on_version() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dash board", "2.0");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard",
                "Engineering dashboard", "1.0");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard",
                "2.0");

        final Application hr = getLivingApplicationAPI().createApplication(hrCreator);
        getLivingApplicationAPI().createApplication(engineeringCreator);
        final Application marketing = getLivingApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getAppSearchBuilderOrderByToken(0, 10);
        builder.filter(ApplicationSearchDescriptor.VERSION, "2.0");

        final SearchResult<Application> applications = getLivingApplicationAPI().searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(2);
        assertThat(applications.getResult()).containsExactly(hr, marketing);

    }

    @Test
    public void searchApplications_can_filter_on_profileId() throws Exception {
        //given
        final Profile profile = getProfileUser();
        final SearchOptionsBuilder builder = getAppSearchBuilderOrderByToken(0, 10);
        long initialCount = getLivingApplicationAPI().searchApplications(builder.done()).getCount();
        builder.filter(ApplicationSearchDescriptor.PROFILE_ID, profile.getId());
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dash board", "1.0");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard",
                "Engineering dashboard", "1.0");
        engineeringCreator.setProfileId(profile.getId());
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard",
                "1.0");

        final Application hr = getLivingApplicationAPI().createApplication(hrCreator);
        final Application engineering = getLivingApplicationAPI().createApplication(engineeringCreator);
        final Application marketing = getLivingApplicationAPI().createApplication(marketingCreator);

        //when

        final SearchResult<Application> applications = getLivingApplicationAPI().searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(initialCount + 1);
        assertThat(applications.getResult()).contains(engineering);

        getLivingApplicationAPI().deleteApplication(hr.getId());
        getLivingApplicationAPI().deleteApplication(engineering.getId());
        getLivingApplicationAPI().deleteApplication(marketing.getId());
    }

    @Test
    public void searchApplications_can_use_search_term() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "My HR dashboard", "2.0");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard",
                "Engineering dashboard", "1.0");
        final ApplicationCreator marketingCreator = new ApplicationCreator("My", "Marketing", "2.0");

        final Application hr = getLivingApplicationAPI().createApplication(hrCreator);
        getLivingApplicationAPI().createApplication(engineeringCreator);
        final Application marketing = getLivingApplicationAPI().createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getAppSearchBuilderOrderByToken(0, 10);
        builder.searchTerm("My");

        final SearchResult<Application> applications = getLivingApplicationAPI().searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(2);
        assertThat(applications.getResult()).containsExactly(hr, marketing);
    }

    @Test
    public void should_create_update_replace_icon_of_application() throws Exception {
        //Create application with icon
        ApplicationCreator creator = new ApplicationCreator("appWithIcon", "An application having an icon", "1.0");
        creator.setIcon("icon.png", "PNG\n\t some png content".getBytes());

        Application applicationCreated = getLivingApplicationAPI().createApplication(creator);
        assertThat(applicationCreated.hasIcon()).isTrue();
        assertThat(getLivingApplicationAPI().getIconOfApplication(applicationCreated.getId())).satisfies(icon -> {
            assertThat(new String(icon.getContent())).isEqualTo("PNG\n\t some png content");
            assertThat(icon.getMimeType()).isEqualTo("image/png");
        });

        //replace icon of application
        Thread.sleep(10);
        Application applicationUpdated = getLivingApplicationAPI().updateApplication(applicationCreated.getId(),
                new ApplicationUpdater().setIcon("toto.jpg", "jpeg content".getBytes()));

        assertThat(applicationUpdated.hasIcon()).isTrue();
        assertThat(applicationUpdated.getLastUpdateDate()).isAfter(applicationCreated.getLastUpdateDate());
        assertThat(getLivingApplicationAPI().getIconOfApplication(applicationUpdated.getId())).satisfies(icon -> {
            assertThat(new String(icon.getContent())).isEqualTo("jpeg content");
            assertThat(icon.getMimeType()).isEqualTo("image/jpeg");
        });

        // remove icon
        Thread.sleep(10);
        Application applicationWithIconRemoved = getLivingApplicationAPI().updateApplication(applicationCreated.getId(),
                new ApplicationUpdater().setIcon(null, null));

        assertThat(applicationWithIconRemoved.hasIcon()).isFalse();
        assertThat(applicationWithIconRemoved.getLastUpdateDate()).isAfter(applicationUpdated.getLastUpdateDate());
        assertThat(getLivingApplicationAPI().getIconOfApplication(applicationWithIconRemoved.getId())).isNull();
    }

    @Test
    public void should_throw_not_found_when_getting_icon_of_unknown_application() {
        assertThatThrownBy(() -> getLivingApplicationAPI().getIconOfApplication(543256432L))
                .isInstanceOf(ApplicationNotFoundException.class);
    }

    private SearchOptions buildSearchOptions(String prefix, final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = getAppSearchBuilderOrderByToken(startIndex, maxResults);
        builder.searchTerm(prefix);
        return builder.done();
    }

}
