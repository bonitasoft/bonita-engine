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

import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.page.Page;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationAPIApplicationPageIT extends TestWithCustomPage {


    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page", "create" })
    @Test
    public void createApplicationPage_returns_applicationPage_based_on_the_given_parameters() throws Exception {
        //given
        final Application application = getApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0", "/app"));

        //when
        final ApplicationPage appPage = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");

        //then
        assertThat(appPage.getId()).isGreaterThan(0);
        assertThat(appPage.getApplicationId()).isEqualTo(application.getId());
        assertThat(appPage.getPageId()).isEqualTo(getPage().getId());
        assertThat(appPage.getToken()).isEqualTo("firstPage");

        getApplicationAPI().deleteApplication(application.getId());

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application", "Application page",
    "set home page" })
    @Test
    public void setApplicationHomePage_should_update_the_application_homePage() throws Exception {
        //given
        final Application application = getApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0", "/app"));
        final ApplicationPage appPage = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");

        //when
        getApplicationAPI().setApplicationHomePage(application.getId(), appPage.getId());

        //then
        final Application upToDateApp = getApplicationAPI().getApplication(application.getId());
        assertThat(upToDateApp.getHomePageId()).isEqualTo(appPage.getId());

        getApplicationAPI().deleteApplication(application.getId());

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
    "get by name and application name" })
    @Test
    public void getApplicationPage_byNameAndAppName_returns_the_applicationPage_corresponding_to_the_given_parameters() throws Exception {
        //given
        final Application application = getApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0", "/app"));
        final ApplicationPage appPage = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");

        //when
        final ApplicationPage retrievedAppPage = getApplicationAPI().getApplicationPage(application.getName(), appPage.getToken());

        //then
        assertThat(retrievedAppPage).isEqualTo(appPage);
        getApplicationAPI().deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
    "get by id" })
    @Test
    public void getApplicationPage_byId_returns_the_applicationPage_corresponding_to_the_given_Id() throws Exception {
        //given
        final Application application = getApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0", "/app"));
        final ApplicationPage appPage = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");

        //when
        final ApplicationPage retrievedAppPage = getApplicationAPI().getApplicationPage(appPage.getId());

        //then
        assertThat(retrievedAppPage).isEqualTo(appPage);
        getApplicationAPI().deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application", "Application page",
    "delete" })
    @Test
    public void deleteApplication_should_also_delete_related_applicationPage() throws Exception {
        //given
        final Application application = getApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0", "/app"));
        final ApplicationPage appPage = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");

        //when
        getApplicationAPI().deleteApplication(application.getId());

        //then
        try {
            getApplicationAPI().getApplicationPage(appPage.getId());
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
        final Application application = getApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0", "/app"));
        final ApplicationPage appPage = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");

        //when
        getApplicationAPI().deleteApplicationPage(appPage.getId());

        //then
        try {
            getApplicationAPI().getApplicationPage(appPage.getId());
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
        final Application application = getApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0", "/app"));
        final ApplicationPage appPage = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        getApplicationAPI().setApplicationHomePage(application.getId(), appPage.getId());

        //when
        final ApplicationPage homePage = getApplicationAPI().getApplicationHomePage(application.getId());

        //then
        assertThat(homePage).isEqualTo(appPage);

        getApplicationAPI().deleteApplication(application.getId());

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "no filters", "no search term" })
    @Test
    public void searchApplicationPages_without_filters_and_search_term_should_return_all_applicationPages_pagged() throws Exception {
        //given
        final Application application = getApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0", "/app"));
        final ApplicationPage appPage1 = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        final ApplicationPage appPage2 = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "secondPage");
        final ApplicationPage appPage3 = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "thirdPage");

        //when
        final SearchResult<ApplicationPage> searchResultPage1 = getApplicationAPI().searchApplicationPages(buildSearchOptions(0, 2));
        final SearchResult<ApplicationPage> searchResultPage2 = getApplicationAPI().searchApplicationPages(buildSearchOptions(2, 2));

        //then
        assertThat(searchResultPage1).isNotNull();
        assertThat(searchResultPage1.getCount()).isEqualTo(3);
        assertThat(searchResultPage1.getResult()).containsExactly(appPage1, appPage2);

        assertThat(searchResultPage2).isNotNull();
        assertThat(searchResultPage2.getCount()).isEqualTo(3);
        assertThat(searchResultPage2.getResult()).containsExactly(appPage3);

        getApplicationAPI().deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "filter on name" })
    @Test
    public void searchApplicationPages_can_filter_on_name() throws Exception {
        //given
        final Application application = getApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0", "/app"));
        getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        final ApplicationPage appPage2 = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "secondPage");
        getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.TOKEN, "secondPage");
        final SearchResult<ApplicationPage> searchResult = getApplicationAPI().searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(1);
        assertThat(searchResult.getResult()).containsExactly(appPage2);

        getApplicationAPI().deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "filter on application id" })
    @Test
    public void searchApplicationPages_can_filter_on_applicationId() throws Exception {
        //given
        final Application application1 = getApplicationAPI().createApplication(new ApplicationCreator("app1", "My app 1", "1.0", "/app1"));
        final Application application2 = getApplicationAPI().createApplication(new ApplicationCreator("app2", "My app 2", "1.0", "/app2"));
        final ApplicationPage appPage1 = getApplicationAPI().createApplicationPage(application1.getId(), getPage().getId(), "firstPage");
        getApplicationAPI().createApplicationPage(application2.getId(), getPage().getId(), "secondPage");
        final ApplicationPage appPage3 = getApplicationAPI().createApplicationPage(application1.getId(), getPage().getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.APPLICATION_ID, application1.getId());
        final SearchResult<ApplicationPage> searchResult = getApplicationAPI().searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(2);
        assertThat(searchResult.getResult()).containsExactly(appPage1, appPage3);

        getApplicationAPI().deleteApplication(application1.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "filter on page id" })
    @Test
    public void searchApplicationPages_can_filter_on_pageId() throws Exception {
        //given
        final Page page2 = createPage("custompage_MyPage2");
        final Application application = getApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0", "/app"));
        getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        final ApplicationPage appPage2 = getApplicationAPI().createApplicationPage(application.getId(), page2.getId(), "secondPage");
        final ApplicationPage appPage3 = getApplicationAPI().createApplicationPage(application.getId(), page2.getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.PAGE_ID, page2.getId());
        final SearchResult<ApplicationPage> searchResult = getApplicationAPI().searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(2);
        assertThat(searchResult.getResult()).containsExactly(appPage2, appPage3);

        getApplicationAPI().deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "filter on id" })
    @Test
    public void searchApplicationPages_can_filter_on_id() throws Exception {
        //given
        final Application application = getApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0", "/app"));
        getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        final ApplicationPage appPage2 = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "secondPage");
        getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.ID, appPage2.getId());
        final SearchResult<ApplicationPage> searchResult = getApplicationAPI().searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(1);
        assertThat(searchResult.getResult()).containsExactly(appPage2);

        getApplicationAPI().deleteApplication(application.getId());
    }

    private SearchOptions buildSearchOptions(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = getDefaultBuilder(startIndex, maxResults);
        final SearchOptions options = builder.done();
        return options;
    }

    private SearchOptionsBuilder getDefaultBuilder(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(startIndex, maxResults);
        builder.sort(ApplicationPageSearchDescriptor.TOKEN, Order.ASC);
        return builder;
    }

}
