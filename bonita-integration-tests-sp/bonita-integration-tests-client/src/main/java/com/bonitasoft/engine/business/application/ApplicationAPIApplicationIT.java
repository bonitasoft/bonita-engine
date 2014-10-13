/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.business.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationAPIApplicationIT extends CommonAPISPTest {

    private ApplicationAPI applicationAPI;

    private static User user;

    @Override
    protected void setAPIs() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        super.setAPIs();
        applicationAPI = TenantAPIAccessor.getApplicationAPI(getSession());
    }

    @Before
    public void setUp() throws Exception {
        user = BPMTestSPUtil.createUserOnDefaultTenant("john", "bpm");
        loginOnDefaultTenantWith("john", "bpm");
    }

    @After
    public void tearDown() throws Exception {
        final SearchResult<Application> searchResult = applicationAPI.searchApplications(new SearchOptionsBuilder(0, 1000).done());
        for (final Application app : searchResult.getResult()) {
            applicationAPI.deleteApplication(app.getId());
        }
        logoutOnTenant();
        BPMTestSPUtil.deleteUserOnDefaultTenant(user);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9199", keywords = { "Application", "create" })
    @Test
    public void createApplication_returns_application_based_on_ApplicationCreator_information() throws Exception {
        //given
        final Profile profile = getProfileAPI().createProfile("ProfileForApp", "Profile used by the application");
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name", "1.0", "/myApplication");
        creator.setDescription("This is my application");
        creator.setIconPath("/icon.jpg");
        creator.setProfileId(profile.getId());

        //when
        final Application application = applicationAPI.createApplication(creator);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getName()).isEqualTo("My-Application");
        assertThat(application.getDisplayName()).isEqualTo("My application display name");
        assertThat(application.getVersion()).isEqualTo("1.0");
        assertThat(application.getPath()).isEqualTo("/myApplication");
        assertThat(application.getId()).isGreaterThan(0);
        assertThat(application.getDescription()).isEqualTo("This is my application");
        assertThat(application.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(application.getCreatedBy()).isEqualTo(user.getId());
        assertThat(application.getUpdatedBy()).isEqualTo(user.getId());
        assertThat(application.getHomePageId()).isEqualTo(0);
        assertThat(application.getProfileId()).isEqualTo(profile.getId());

        applicationAPI.deleteApplication(application.getId());
        getProfileAPI().deleteProfile(profile.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9199", keywords = { "Application", "create", "no profile" })
    @Test
    public void createApplication_without_profile_should_have_null_profileId() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name", "1.0", "/myApplication");

        //when
        final Application application = applicationAPI.createApplication(creator);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getProfileId()).isNull();

        applicationAPI.deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application", "update" })
    @Test
    public void updateApplication_should_return_application_up_to_date() throws Exception {
        //given
        final Profile profile = getProfileAPI().createProfile("ProfileForApp", "Profile used by the application");
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name", "1.0", "/myApplication");
        final Application application = applicationAPI.createApplication(creator);

        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setName("My-updated-app");
        updater.setDisplayName("Updated display name");
        updater.setVersion("1.1");
        updater.setPath("/myUpdatedApp");
        updater.setDescription("Up description");
        updater.setIconPath("/newIcon.jpg");
        updater.setProfileId(profile.getId());
        updater.setState(ApplicationState.ACTIVATED.name());

        //when
        final Application updatedApplication = applicationAPI.updateApplication(application.getId(), updater);

        //then
        assertThat(updatedApplication).isNotNull();
        assertThat(updatedApplication.getName()).isEqualTo("My-updated-app");
        assertThat(updatedApplication.getDisplayName()).isEqualTo("Updated display name");
        assertThat(updatedApplication.getVersion()).isEqualTo("1.1");
        assertThat(updatedApplication.getPath()).isEqualTo("/myUpdatedApp");
        assertThat(updatedApplication.getDescription()).isEqualTo("Up description");
        assertThat(updatedApplication.getIconPath()).isEqualTo("/newIcon.jpg");
        assertThat(updatedApplication.getProfileId()).isEqualTo(profile.getId());
        assertThat(updatedApplication.getState()).isEqualTo(ApplicationState.ACTIVATED.name());
        assertThat(updatedApplication).isEqualTo(applicationAPI.getApplication(application.getId()));

        applicationAPI.deleteApplication(application.getId());
        getProfileAPI().deleteProfile(profile.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9199", keywords = { "Application", "get" })
    @Test
    public void getApplication_returns_application_with_the_given_id() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name", "1.0", "/myApplication");
        final Application createdApp = applicationAPI.createApplication(creator);
        assertThat(createdApp).isNotNull();

        //when
        final Application retrivedApp = applicationAPI.getApplication(createdApp.getId());

        //then
        assertThat(retrivedApp).isEqualTo(createdApp);
        applicationAPI.deleteApplication(createdApp.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9199", keywords = { "Application", "get" })
    @Test
    public void deleteApplication_should_delete_application_with_the_given_id() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name", "1.0", "/myApplication");
        final Application createdApp = applicationAPI.createApplication(creator);
        assertThat(createdApp).isNotNull();

        //when
        applicationAPI.deleteApplication(createdApp.getId());

        //then
        try {
            applicationAPI.getApplication(createdApp.getId());
            fail("Not found exception");
        } catch (final NotFoundException e) {
            //ok
        }
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9290", keywords = { "Application", "search", "no filter",
    "no search term" })
    @Test
    public void searchApplications_without_filter_return_all_elements_based_on_pagination() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dash board", "1.0", "/hr");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard", "Engineering dashboard", "1.0", "/engineering");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard", "1.0", "/marketing");

        final Application hr = applicationAPI.createApplication(hrCreator);
        final Application engineering = applicationAPI.createApplication(engineeringCreator);
        final Application marketing = applicationAPI.createApplication(marketingCreator);

        //when
        final SearchResult<Application> firstPage = applicationAPI.searchApplications(buildSearchOptions(0, 2));

        //then
        assertThat(firstPage).isNotNull();
        assertThat(firstPage.getCount()).isEqualTo(3);
        assertThat(firstPage.getResult()).containsExactly(engineering, hr);

        //when
        final SearchResult<Application> secondPage = applicationAPI.searchApplications(buildSearchOptions(2, 2));

        //then
        assertThat(secondPage).isNotNull();
        assertThat(secondPage.getCount()).isEqualTo(3);
        assertThat(secondPage.getResult()).containsExactly(marketing);

        applicationAPI.deleteApplication(hr.getId());
        applicationAPI.deleteApplication(engineering.getId());
        applicationAPI.deleteApplication(marketing.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9290", keywords = { "Application", "search", "filter on name",
    "no search term" })
    @Test
    public void searchApplications_can_filter_on_name() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dash board", "1.0", "/hr");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard", "Engineering dashboard", "1.0", "/engineering");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard", "1.0", "/marketing");

        final Application hr = applicationAPI.createApplication(hrCreator);
        final Application engineering = applicationAPI.createApplication(engineeringCreator);
        final Application marketing = applicationAPI.createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationSearchDescriptor.NAME, "Engineering-dashboard");

        final SearchResult<Application> applications = applicationAPI.searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(1);
        assertThat(applications.getResult()).containsExactly(engineering);

        applicationAPI.deleteApplication(hr.getId());
        applicationAPI.deleteApplication(engineering.getId());
        applicationAPI.deleteApplication(marketing.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9290", keywords = { "Application", "search",
            "filter on display name",
    "no search term" })
    @Test
    public void searchApplications_can_filter_on_display_name() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dashboard", "1.0", "/hr");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard", "Engineering dashboard", "1.0", "/engineering");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard", "1.0", "/marketing");

        final Application hr = applicationAPI.createApplication(hrCreator);
        final Application engineering = applicationAPI.createApplication(engineeringCreator);
        final Application marketing = applicationAPI.createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationSearchDescriptor.DISPLAY_NAME, "HR dashboard");

        final SearchResult<Application> applications = applicationAPI.searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(1);
        assertThat(applications.getResult()).containsExactly(hr);

        applicationAPI.deleteApplication(hr.getId());
        applicationAPI.deleteApplication(engineering.getId());
        applicationAPI.deleteApplication(marketing.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9290", keywords = { "Application", "search", "filter on path",
    "no search term" })
    @Test
    public void searchApplications_can_filter_on_path() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dash board", "1.0", "/hr");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard", "Engineering dashboard", "1.0", "/engineering");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard", "1.0", "/marketing");

        final Application hr = applicationAPI.createApplication(hrCreator);
        final Application engineering = applicationAPI.createApplication(engineeringCreator);
        final Application marketing = applicationAPI.createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationSearchDescriptor.PATH, "/hr");

        final SearchResult<Application> applications = applicationAPI.searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(1);
        assertThat(applications.getResult()).containsExactly(hr);

        applicationAPI.deleteApplication(hr.getId());
        applicationAPI.deleteApplication(engineering.getId());
        applicationAPI.deleteApplication(marketing.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9290", keywords = { "Application", "search", "filter on version",
    "no search term" })
    @Test
    public void searchApplications_can_filter_on_version() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "HR dash board", "2.0", "/hr");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard", "Engineering dashboard", "1.0", "/engineering");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing-dashboard", "Marketing dashboard", "2.0", "/marketing");

        final Application hr = applicationAPI.createApplication(hrCreator);
        final Application engineering = applicationAPI.createApplication(engineeringCreator);
        final Application marketing = applicationAPI.createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationSearchDescriptor.VERSION, "2.0");

        final SearchResult<Application> applications = applicationAPI.searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(2);
        assertThat(applications.getResult()).containsExactly(hr, marketing);

        applicationAPI.deleteApplication(hr.getId());
        applicationAPI.deleteApplication(engineering.getId());
        applicationAPI.deleteApplication(marketing.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9290", keywords = { "Application", "search", "no filter",
    "search term" })
    @Test
    public void searchApplications_can_use_search_term() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "My HR dashboard", "2.0", "/hr");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard", "Engineering dashboard", "1.0", "/engineering");
        final ApplicationCreator marketingCreator = new ApplicationCreator("My", "Marketing", "2.0", "/marketing");

        final Application hr = applicationAPI.createApplication(hrCreator);
        final Application engineering = applicationAPI.createApplication(engineeringCreator);
        final Application marketing = applicationAPI.createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.searchTerm("My");

        final SearchResult<Application> applications = applicationAPI.searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(2);
        assertThat(applications.getResult()).containsExactly(hr, marketing);

        applicationAPI.deleteApplication(hr.getId());
        applicationAPI.deleteApplication(engineering.getId());
        applicationAPI.deleteApplication(marketing.getId());
    }

    private SearchOptions buildSearchOptions(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = getDefaultBuilder(startIndex, maxResults);
        final SearchOptions options = builder.done();
        return options;
    }

    private SearchOptionsBuilder getDefaultBuilder(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(startIndex, maxResults);
        builder.sort(ApplicationSearchDescriptor.NAME, Order.ASC);
        return builder;
    }

}
