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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
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
import com.bonitasoft.engine.page.PageCreator;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationAPIApplicationPageIT extends CommonAPISPTest {

    private ApplicationAPI applicationAPI;

    private static User user;

    private Page page;

    @Override
    protected void setAPIs() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        super.setAPIs();
        applicationAPI = TenantAPIAccessor.getApplicationAPI(getSession());
    }

    @Before
    public void setUp() throws Exception {
        user = BPMTestSPUtil.createUserOnDefaultTenant("john", "bpm");
        loginOnDefaultTenantWith("john", "bpm");
        try {
            page = createPage("custompage_MyPage");
        } catch (final AlreadyExistsException e) {
            throw e;
        }
    }

    @After
    public void tearDown() throws Exception {
        final SearchResult<Application> searchResult = applicationAPI.searchApplications(new SearchOptionsBuilder(0, 1000).done());
        for (final Application app : searchResult.getResult()) {
            applicationAPI.deleteApplication(app.getId());
        }
        if (page != null) {
            getPageAPI().deletePage(page.getId());
        }
        logoutOnTenant();
        BPMTestSPUtil.deleteUserOnDefaultTenant(user);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page", "create" })
    @Test
    public void createApplicationPage_returns_applicationPage_based_on_the_given_parameters() throws Exception {
        //given
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

    private byte[] createPageContent(final String pageName)
            throws BonitaException {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            zos.putNextEntry(new ZipEntry("Index.groovy"));
            zos.write("return \"\";".getBytes());

            zos.putNextEntry(new ZipEntry("page.properties"));
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("name=");
            stringBuilder.append(pageName);
            stringBuilder.append("\n");
            stringBuilder.append("displayName=");
            stringBuilder.append("no display name");
            stringBuilder.append("\n");
            stringBuilder.append("description=");
            stringBuilder.append("empty desc");
            stringBuilder.append("\n");
            zos.write(stringBuilder.toString().getBytes());

            zos.closeEntry();
            return baos.toByteArray();
        } catch (final IOException e) {
            throw new BonitaException(e);
        }
    }

    private Page createPage(final String pageName) throws Exception {
        final Page page = getPageAPI().createPage(new PageCreator(pageName, "content.zip").setDisplayName(pageName), createPageContent(pageName));
        return page;
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application", "Application page",
    "set home page" })
    @Test
    public void setApplicationHomePage_should_update_the_application_homePage() throws Exception {
        //given
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        final ApplicationPage appPage = applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");

        //when
        applicationAPI.setApplicationHomePage(application.getId(), appPage.getId());

        //then
        final Application upToDateApp = applicationAPI.getApplication(application.getId());
        assertThat(upToDateApp.getHomePageId()).isEqualTo(appPage.getId());

        applicationAPI.deleteApplication(application.getId());

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
    "get by name and application name" })
    @Test
    public void getApplicationPage_byNameAndAppName_returns_the_applicationPage_corresponding_to_the_given_parameters() throws Exception {
        //given
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

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application", "Application page",
    "set home page" })
    @Test
    public void getApplicationHomePage_should_return_application_homePage() throws Exception {
        //given
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        final ApplicationPage appPage = applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");
        applicationAPI.setApplicationHomePage(application.getId(), appPage.getId());

        //when
        final ApplicationPage homePage = applicationAPI.getApplicationHomePage(application.getId());

        //then
        assertThat(homePage).isEqualTo(appPage);

        applicationAPI.deleteApplication(application.getId());

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "no filters", "no search term" })
    @Test
    public void searchApplicationPages_without_filters_and_search_term_should_return_all_applicationPages_pagged() throws Exception {
        //given
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        final ApplicationPage appPage1 = applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");
        final ApplicationPage appPage2 = applicationAPI.createApplicationPage(application.getId(), page.getId(), "secondPage");
        final ApplicationPage appPage3 = applicationAPI.createApplicationPage(application.getId(), page.getId(), "thirdPage");

        //when
        final SearchResult<ApplicationPage> searchResultPage1 = applicationAPI.searchApplicationPages(buildSearchOptions(0, 2));
        final SearchResult<ApplicationPage> searchResultPage2 = applicationAPI.searchApplicationPages(buildSearchOptions(2, 2));

        //then
        assertThat(searchResultPage1).isNotNull();
        assertThat(searchResultPage1.getCount()).isEqualTo(3);
        assertThat(searchResultPage1.getResult()).containsExactly(appPage1, appPage2);

        assertThat(searchResultPage2).isNotNull();
        assertThat(searchResultPage2.getCount()).isEqualTo(3);
        assertThat(searchResultPage2.getResult()).containsExactly(appPage3);

        applicationAPI.deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "filter on name" })
    @Test
    public void searchApplicationPages_can_filter_on_name() throws Exception {
        //given
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");
        final ApplicationPage appPage2 = applicationAPI.createApplicationPage(application.getId(), page.getId(), "secondPage");
        applicationAPI.createApplicationPage(application.getId(), page.getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.NAME, "secondPage");
        final SearchResult<ApplicationPage> searchResult = applicationAPI.searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(1);
        assertThat(searchResult.getResult()).containsExactly(appPage2);

        applicationAPI.deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "filter on application id" })
    @Test
    public void searchApplicationPages_can_filter_on_applicationId() throws Exception {
        //given
        final Application application1 = applicationAPI.createApplication(new ApplicationCreator("app1", "1.0", "/app1"));
        final Application application2 = applicationAPI.createApplication(new ApplicationCreator("app2", "1.0", "/app2"));
        final ApplicationPage appPage1 = applicationAPI.createApplicationPage(application1.getId(), page.getId(), "firstPage");
        applicationAPI.createApplicationPage(application2.getId(), page.getId(), "secondPage");
        final ApplicationPage appPage3 = applicationAPI.createApplicationPage(application1.getId(), page.getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.APPLICATION_ID, application1.getId());
        final SearchResult<ApplicationPage> searchResult = applicationAPI.searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(2);
        assertThat(searchResult.getResult()).containsExactly(appPage1, appPage3);

        applicationAPI.deleteApplication(application1.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "filter on page id" })
    @Test
    public void searchApplicationPages_can_filter_on_pageId() throws Exception {
        //given
        final Page page2 = createPage("custompage_MyPage2");
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");
        final ApplicationPage appPage2 = applicationAPI.createApplicationPage(application.getId(), page2.getId(), "secondPage");
        final ApplicationPage appPage3 = applicationAPI.createApplicationPage(application.getId(), page2.getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.PAGE_ID, page2.getId());
        final SearchResult<ApplicationPage> searchResult = applicationAPI.searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(2);
        assertThat(searchResult.getResult()).containsExactly(appPage2, appPage3);

        applicationAPI.deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "filter on id" })
    @Test
    public void searchApplicationPages_can_filter_on_id() throws Exception {
        //given
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");
        final ApplicationPage appPage2 = applicationAPI.createApplicationPage(application.getId(), page.getId(), "secondPage");
        applicationAPI.createApplicationPage(application.getId(), page.getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.ID, appPage2.getId());
        final SearchResult<ApplicationPage> searchResult = applicationAPI.searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(1);
        assertThat(searchResult.getResult()).containsExactly(appPage2);

        applicationAPI.deleteApplication(application.getId());
    }

    private SearchOptions buildSearchOptions(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = getDefaultBuilder(startIndex, maxResults);
        final SearchOptions options = builder.done();
        return options;
    }

    private SearchOptionsBuilder getDefaultBuilder(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(startIndex, maxResults);
        builder.sort(ApplicationPageSearchDescriptor.NAME, Order.ASC);
        return builder;
    }

}
