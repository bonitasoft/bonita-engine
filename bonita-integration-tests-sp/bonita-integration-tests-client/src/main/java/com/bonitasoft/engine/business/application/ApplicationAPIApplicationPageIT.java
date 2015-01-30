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

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.profile.Profile;
import org.assertj.core.api.Assertions;
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
 * @author Baptiste Mesta
 *
 */
public class ApplicationAPIApplicationPageIT extends TestWithCustomPage {

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page", "create" })
    @Test
    public void createApplicationPage_returns_applicationPage_based_on_the_given_parameters() throws Exception {
        //given
        final Application application = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0"));

        //when
        final ApplicationPage appPage = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");

        //then
        assertThat(appPage.getId()).isGreaterThan(0);
        assertThat(appPage.getApplicationId()).isEqualTo(application.getId());
        assertThat(appPage.getPageId()).isEqualTo(getPage().getId());
        assertThat(appPage.getToken()).isEqualTo("firstPage");

        getSubscriptionApplicationAPI().deleteApplication(application.getId());

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application", "Application page",
            "set home page" })
    @Test
    public void setApplicationHomePage_should_update_the_application_homePage() throws Exception {
        //given
        final Application application = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage appPage = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");

        //when
        getSubscriptionApplicationAPI().setApplicationHomePage(application.getId(), appPage.getId());

        //then
        final Application upToDateApp = getSubscriptionApplicationAPI().getApplication(application.getId());
        assertThat(upToDateApp.getHomePageId()).isEqualTo(appPage.getId());

        getSubscriptionApplicationAPI().deleteApplication(application.getId());

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application", "update", "home page" })
    @Test
    public void updateApplication_should_update_home_page() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name", "1.0");
        final Application application = getSubscriptionApplicationAPI().createApplication(creator);
        final ApplicationPage appPage = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");

        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setHomePageId(appPage.getId());

        //when
        final Application updatedApplication = getSubscriptionApplicationAPI().updateApplication(application.getId(), updater);

        //then
        assertThat(updatedApplication).isNotNull();
        assertThat(updatedApplication.getHomePageId()).isEqualTo(appPage.getId());

        getSubscriptionApplicationAPI().deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "get by name and application name" })
    @Test
    public void getApplicationPage_byNameAndAppName_returns_the_applicationPage_corresponding_to_the_given_parameters() throws Exception {
        //given
        final Application application = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage appPage = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");

        //when
        final ApplicationPage retrievedAppPage = getSubscriptionApplicationAPI().getApplicationPage(application.getToken(), appPage.getToken());

        //then
        assertThat(retrievedAppPage).isEqualTo(appPage);
        getSubscriptionApplicationAPI().deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "get by id" })
    @Test
    public void getApplicationPage_byId_returns_the_applicationPage_corresponding_to_the_given_Id() throws Exception {
        //given
        final Application application = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage appPage = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");

        //when
        final ApplicationPage retrievedAppPage = getSubscriptionApplicationAPI().getApplicationPage(appPage.getId());

        //then
        assertThat(retrievedAppPage).isEqualTo(appPage);
        getSubscriptionApplicationAPI().deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application", "Application page",
            "delete" })
    @Test
    public void deleteApplication_should_also_delete_related_applicationPage() throws Exception {
        //given
        final Application application = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage homePage = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        getSubscriptionApplicationAPI().setApplicationHomePage(application.getId(), homePage.getId());
        final ApplicationPage aAppPage = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "secondPage");

        //when
        getSubscriptionApplicationAPI().deleteApplication(application.getId());

        //then
        verifyNotExists(homePage);
        verifyNotExists(aAppPage);
    }

    private void verifyNotExists(ApplicationPage applicationPage) {
        try {
            getSubscriptionApplicationAPI().getApplicationPage(applicationPage.getId()); //throws exception
            Assertions.fail("exception expected");
        } catch (ApplicationPageNotFoundException e) {
            //OK
        }
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "delete" })
    @Test
    public void deleteApplicationPage_should_delete_applicationPage_with_the_given_id() throws Exception {
        //given
        final Application application = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage appPage = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");

        //when
        getSubscriptionApplicationAPI().deleteApplicationPage(appPage.getId());

        //then
        try {
            getSubscriptionApplicationAPI().getApplicationPage(appPage.getId());
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
        final Application application = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage appPage = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        getSubscriptionApplicationAPI().setApplicationHomePage(application.getId(), appPage.getId());

        //when
        final ApplicationPage homePage = getSubscriptionApplicationAPI().getApplicationHomePage(application.getId());

        //then
        assertThat(homePage).isEqualTo(appPage);

        getSubscriptionApplicationAPI().deleteApplication(application.getId());

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "no filters", "no search term" })
    @Test
    public void searchApplicationPages_without_filters_and_search_term_should_return_all_applicationPages_pagged() throws Exception {
        //given
        final Application application = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage appPage1 = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        final ApplicationPage appPage2 = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "secondPage");
        final ApplicationPage appPage3 = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "thirdPage");

        //when
        final SearchResult<ApplicationPage> searchResultPage1 = getSubscriptionApplicationAPI().searchApplicationPages(buildSearchOptions(0, 2));
        final SearchResult<ApplicationPage> searchResultPage2 = getSubscriptionApplicationAPI().searchApplicationPages(buildSearchOptions(2, 2));

        //then
        assertThat(searchResultPage1).isNotNull();
        assertThat(searchResultPage1.getCount()).isEqualTo(3);
        assertThat(searchResultPage1.getResult()).containsExactly(appPage1, appPage2);

        assertThat(searchResultPage2).isNotNull();
        assertThat(searchResultPage2.getCount()).isEqualTo(3);
        assertThat(searchResultPage2.getResult()).containsExactly(appPage3);

        getSubscriptionApplicationAPI().deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "filter on name" })
    @Test
    public void searchApplicationPages_can_filter_on_name() throws Exception {
        //given
        final Application application = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0"));
        getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        final ApplicationPage appPage2 = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "secondPage");
        getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.TOKEN, "secondPage");
        final SearchResult<ApplicationPage> searchResult = getSubscriptionApplicationAPI().searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(1);
        assertThat(searchResult.getResult()).containsExactly(appPage2);

        getSubscriptionApplicationAPI().deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "filter on application id" })
    @Test
    public void searchApplicationPages_can_filter_on_applicationId() throws Exception {
        //given
        final Application application1 = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app1", "My app 1", "1.0"));
        final Application application2 = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app2", "My app 2", "1.0"));
        final ApplicationPage appPage1 = getSubscriptionApplicationAPI().createApplicationPage(application1.getId(), getPage().getId(), "firstPage");
        getSubscriptionApplicationAPI().createApplicationPage(application2.getId(), getPage().getId(), "secondPage");
        final ApplicationPage appPage3 = getSubscriptionApplicationAPI().createApplicationPage(application1.getId(), getPage().getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.APPLICATION_ID, application1.getId());
        final SearchResult<ApplicationPage> searchResult = getSubscriptionApplicationAPI().searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(2);
        assertThat(searchResult.getResult()).containsExactly(appPage1, appPage3);

        getSubscriptionApplicationAPI().deleteApplication(application1.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "filter on page id" })
    @Test
    public void searchApplicationPages_can_filter_on_pageId() throws Exception {
        //given
        final Page page2 = createPage("custompage_MyPage2");
        final Application application = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0"));
        getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        final ApplicationPage appPage2 = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), page2.getId(), "secondPage");
        final ApplicationPage appPage3 = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), page2.getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.PAGE_ID, page2.getId());
        final SearchResult<ApplicationPage> searchResult = getSubscriptionApplicationAPI().searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(2);
        assertThat(searchResult.getResult()).containsExactly(appPage2, appPage3);

        getSubscriptionApplicationAPI().deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
            "search", "filter on id" })
    @Test
    public void searchApplicationPages_can_filter_on_id() throws Exception {
        //given
        final Application application = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0"));
        getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        final ApplicationPage appPage2 = getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "secondPage");
        getSubscriptionApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.ID, appPage2.getId());
        final SearchResult<ApplicationPage> searchResult = getSubscriptionApplicationAPI().searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(1);
        assertThat(searchResult.getResult()).containsExactly(appPage2);

        getSubscriptionApplicationAPI().deleteApplication(application.getId());
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


    @Test
    public void getAllAccessiblePageForAProfile() throws Exception {
        //given
        //profile1
        Profile profile1 = getProfileAPI().createProfile("profile1", "My profile1");
        //app1
        final Application app1 = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app1", "My app1", "1.0").setProfileId(profile1.getId()));
        final Page page1 = createPage("custompage_page1");
        getSubscriptionApplicationAPI().createApplicationPage(app1.getId(), page1.getId(), "appPage1");
        final Page page2 = createPage("custompage_page2");
        getSubscriptionApplicationAPI().createApplicationPage(app1.getId(), page2.getId(), "appPage2");
        //app2
        final Application app2 = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app2", "My app2", "1.0").setProfileId(profile1.getId()));
        final Page page3 = createPage("custompage_page3");
        getSubscriptionApplicationAPI().createApplicationPage(app2.getId(), page3.getId(), "appPage1");

        //profile2
        Profile profile2 = getProfileAPI().createProfile("profile2", "My profile2");
        //app3
        final Application app3 = getSubscriptionApplicationAPI().createApplication(new ApplicationCreator("app3", "My app3", "1.0").setProfileId(profile2.getId()));
        final Page page4 = createPage("custompage_page4");
        getSubscriptionApplicationAPI().createApplicationPage(app3.getId(), page4.getId(), "appPage1");


        //when
        List<String> allPagesForProfile1 = getSubscriptionApplicationAPI().getAllPagesForProfile(profile1.getId());
        List<String> allPagesForProfile2 = getSubscriptionApplicationAPI().getAllPagesForProfile(profile2.getId());

        //then
        assertThat(allPagesForProfile1).isEqualTo(Arrays.asList("custompage_page1","custompage_page2","custompage_page3"));
        assertThat(allPagesForProfile2).isEqualTo(Arrays.asList("custompage_page4"));

        getSubscriptionApplicationAPI().deleteApplication(app1.getId());
        getSubscriptionApplicationAPI().deleteApplication(app2.getId());
        getSubscriptionApplicationAPI().deleteApplication(app3.getId());
    }

}
