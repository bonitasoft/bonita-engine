/**
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package com.bonitasoft.engine.business.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.User;
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
import com.bonitasoft.engine.page.Page;
import com.bonitasoft.engine.page.PageSearchDescriptor;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationAPIIT extends CommonAPISPTest {

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
        final ApplicationCreator creator = new ApplicationCreator("My Application", "1.0", "/myApplication");
        creator.setDescription("This is my application");
        creator.setIconPath("/icon.jpg");

        //when
        final Application application = applicationAPI.createApplication(creator);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getName()).isEqualTo("My Application");
        assertThat(application.getVersion()).isEqualTo("1.0");
        assertThat(application.getPath()).isEqualTo("/myApplication");
        assertThat(application.getId()).isGreaterThan(0);
        assertThat(application.getDescription()).isEqualTo("This is my application");
        assertThat(application.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(application.getCreatedBy()).isEqualTo(user.getId());
        assertThat(application.getUpdatedBy()).isEqualTo(user.getId());

        applicationAPI.deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9199", keywords = { "Application", "get" })
    @Test
    public void getApplication_returns_application_with_the_given_id() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My Application", "1.0", "/myApplication");
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
        final ApplicationCreator creator = new ApplicationCreator("My Application", "1.0", "/myApplication");
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
        final ApplicationCreator hrCreator = new ApplicationCreator("HR dashboard", "1.0", "/hr");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering dashboard", "1.0", "/engineering");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing dashboard", "1.0", "/marketing");

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
        final ApplicationCreator hrCreator = new ApplicationCreator("HR dashboard", "1.0", "/hr");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering dashboard", "1.0", "/engineering");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing dashboard", "1.0", "/marketing");

        final Application hr = applicationAPI.createApplication(hrCreator);
        final Application engineering = applicationAPI.createApplication(engineeringCreator);
        final Application marketing = applicationAPI.createApplication(marketingCreator);

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationSearchDescriptor.NAME, "Engineering dashboard");

        final SearchResult<Application> applications = applicationAPI.searchApplications(builder.done());
        assertThat(applications).isNotNull();
        assertThat(applications.getCount()).isEqualTo(1);
        assertThat(applications.getResult()).containsExactly(engineering);

        applicationAPI.deleteApplication(hr.getId());
        applicationAPI.deleteApplication(engineering.getId());
        applicationAPI.deleteApplication(marketing.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9290", keywords = { "Application", "search", "filter on path",
    "no search term" })
    @Test
    public void searchApplications_can_filter_on_path() throws Exception {
        //given
        final ApplicationCreator hrCreator = new ApplicationCreator("HR dashboard", "1.0", "/hr");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering dashboard", "1.0", "/engineering");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing dashboard", "1.0", "/marketing");

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
        final ApplicationCreator hrCreator = new ApplicationCreator("HR dashboard", "2.0", "/hr");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering dashboard", "1.0", "/engineering");
        final ApplicationCreator marketingCreator = new ApplicationCreator("Marketing dashboard", "2.0", "/marketing");

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
        final ApplicationCreator hrCreator = new ApplicationCreator("My HR dashboard", "2.0", "/hr");
        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering dashboard", "1.0", "/engineering");
        final ApplicationCreator marketingCreator = new ApplicationCreator("My Marketing dashboard", "2.0", "/marketing");

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

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page", "create" })
    @Test
    public void createApplicationPage_returns_applicationPage_based_on_the_given_parameters() throws Exception {
        //given
        final Page page = getAProvidedPage();

        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));

        //when
        final ApplicationPage appPage = applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");

        //then
        assertThat(appPage.getId()).isGreaterThan(0);
        assertThat(appPage.getApplicationId()).isEqualTo(application.getId());
        assertThat(appPage.getPageId()).isEqualTo(page.getId());
        assertThat(appPage.getName()).isEqualTo("firstPage");
        applicationAPI.deleteApplication(application.getId());
    }

    private Page getAProvidedPage() throws SearchException {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1);
        builder.filter(PageSearchDescriptor.PROVIDED, true);
        final SearchResult<Page> pagesResult = getPageAPI().searchPages(builder.done());
        final Page page = pagesResult.getResult().get(0);
        return page;
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
    "get by name and application name" })
    @Test
    public void getApplicationPage_byNameAndAppName_returns_the_applicationPage_corresponding_to_the_given_parameters() throws Exception {
        //given
        final Page page = getAProvidedPage();
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        final ApplicationPage appPage = applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");

        //when
        final ApplicationPage retrievedAppPage = applicationAPI.getApplicationPage(application.getName(), appPage.getName());

        //then
        assertThat(retrievedAppPage).isEqualTo(appPage);
        applicationAPI.deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
    "get by id" })
    @Test
    public void getApplicationPage_byId_returns_the_applicationPage_corresponding_to_the_given_Id() throws Exception {
        //given
        final Page page = getAProvidedPage();
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        final ApplicationPage appPage = applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");

        //when
        final ApplicationPage retrievedAppPage = applicationAPI.getApplicationPage(appPage.getId());

        //then
        assertThat(retrievedAppPage).isEqualTo(appPage);
        applicationAPI.deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application", "Application page",
    "delete" })
    @Test
    public void deleteApplication_should_also_delete_related_applicationPage() throws Exception {
        //given
        final Page page = getAProvidedPage();
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        final ApplicationPage appPage = applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");

        //when
        applicationAPI.deleteApplication(application.getId());

        //then
        try {
            applicationAPI.getApplicationPage(appPage.getId());
            fail("Not found expected");
        } catch (final ApplicationPageNotFoundException e) {
            //OK
        }
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "delete" })
    @Test
    public void deleteApplicationPage_should_delete_applicationPage_with_the_given_id() throws Exception {
        //given
        final Page page = getAProvidedPage();
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        final ApplicationPage appPage = applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");

        //when
        applicationAPI.deleteApplicationPage(appPage.getId());

        //then
        try {
            applicationAPI.getApplicationPage(appPage.getId());
            fail("Not found expected");
        } catch (final ApplicationPageNotFoundException e) {
            //OK
        }
    }

}
