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
import static org.junit.Assert.fail;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 * @author Baptiste Mesta
 */
public class LivingApplicationPageIT extends TestWithCustomPage {

    @Test
    public void createApplicationPage_returns_applicationPage_based_on_the_given_parameters() throws Exception {
        //given
        final Application application = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app", "My app", "1.0"));

        //when
        final ApplicationPage appPage = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "firstPage");

        //then
        assertThat(appPage.getId()).isGreaterThan(0);
        assertThat(appPage.getApplicationId()).isEqualTo(application.getId());
        assertThat(appPage.getPageId()).isEqualTo(getPage().getId());
        assertThat(appPage.getToken()).isEqualTo("firstPage");

        getLivingApplicationAPI().deleteApplication(application.getId());

    }

    @Test
    public void setApplicationHomePage_should_update_the_application_homePage() throws Exception {
        //given
        final Application application = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage appPage = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "firstPage");

        //when
        getLivingApplicationAPI().setApplicationHomePage(application.getId(), appPage.getId());

        //then
        final Application upToDateApp = getLivingApplicationAPI().getApplication(application.getId());
        assertThat(upToDateApp.getHomePageId()).isEqualTo(appPage.getId());

        getLivingApplicationAPI().deleteApplication(application.getId());

    }

    @Test
    public void updateApplication_should_update_home_page() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My-Application", "My application display name",
                "1.0");
        final Application application = getLivingApplicationAPI().createApplication(creator);
        final ApplicationPage appPage = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "firstPage");

        final ApplicationUpdater updater = new ApplicationUpdater();
        updater.setHomePageId(appPage.getId());

        //when
        final Application updatedApplication = getLivingApplicationAPI().updateApplication(application.getId(),
                updater);

        //then
        assertThat(updatedApplication).isNotNull();
        assertThat(updatedApplication.getHomePageId()).isEqualTo(appPage.getId());

        getLivingApplicationAPI().deleteApplication(application.getId());
    }

    @Test
    public void getApplicationPage_byNameAndAppName_returns_the_applicationPage_corresponding_to_the_given_parameters()
            throws Exception {
        //given
        final Application application = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage appPage = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "firstPage");

        //when
        final ApplicationPage retrievedAppPage = getLivingApplicationAPI().getApplicationPage(application.getToken(),
                appPage.getToken());

        //then
        assertThat(retrievedAppPage).isEqualTo(appPage);
        getLivingApplicationAPI().deleteApplication(application.getId());
    }

    @Test
    public void getApplicationPage_byId_returns_the_applicationPage_corresponding_to_the_given_Id() throws Exception {
        //given
        final Application application = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage appPage = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "firstPage");

        //when
        final ApplicationPage retrievedAppPage = getLivingApplicationAPI().getApplicationPage(appPage.getId());

        //then
        assertThat(retrievedAppPage).isEqualTo(appPage);
        getLivingApplicationAPI().deleteApplication(application.getId());
    }

    @Test
    public void deleteApplication_should_also_delete_related_applicationPage() throws Exception {
        //given
        final Application application = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage homePage = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "firstPage");
        getLivingApplicationAPI().setApplicationHomePage(application.getId(), homePage.getId());
        final ApplicationPage aAppPage = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "secondPage");

        //when
        getLivingApplicationAPI().deleteApplication(application.getId());

        //then
        verifyNotExists(homePage);
        verifyNotExists(aAppPage);
    }

    private void verifyNotExists(ApplicationPage applicationPage) {
        try {
            getLivingApplicationAPI().getApplicationPage(applicationPage.getId()); //throws exception
            Assertions.fail("exception expected");
        } catch (ApplicationPageNotFoundException e) {
            //OK
        }
    }

    @Test
    public void deleteApplicationPage_should_delete_applicationPage_with_the_given_id() throws Exception {
        //given
        final Application application = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage appPage = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "firstPage");

        //when
        getLivingApplicationAPI().deleteApplicationPage(appPage.getId());

        //then
        try {
            getLivingApplicationAPI().getApplicationPage(appPage.getId());
            fail("Not found expected");
        } catch (final ApplicationPageNotFoundException e) {
            //OK
        }
    }

    @Test
    public void getApplicationHomePage_should_return_application_homePage() throws Exception {
        //given
        final Application application = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage appPage = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "firstPage");
        getLivingApplicationAPI().setApplicationHomePage(application.getId(), appPage.getId());

        //when
        final ApplicationPage homePage = getLivingApplicationAPI().getApplicationHomePage(application.getId());

        //then
        assertThat(homePage).isEqualTo(appPage);

        getLivingApplicationAPI().deleteApplication(application.getId());

    }

    @Test
    public void searchApplicationPages_without_filters_and_search_term_should_return_all_applicationPages_pagged()
            throws Exception {
        //given
        final Application application = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app", "My app", "1.0"));
        final ApplicationPage appPage1 = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "firstPage");
        final ApplicationPage appPage2 = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "secondPage");
        final ApplicationPage appPage3 = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "thirdPage");

        //when
        final SearchResult<ApplicationPage> searchResultPage1 = getLivingApplicationAPI()
                .searchApplicationPages(buildSearchOptions(0, 2));
        final SearchResult<ApplicationPage> searchResultPage2 = getLivingApplicationAPI()
                .searchApplicationPages(buildSearchOptions(2, 2));

        //then
        assertThat(searchResultPage1).isNotNull();
        assertThat(searchResultPage1.getCount()).isEqualTo(3);
        assertThat(searchResultPage1.getResult()).containsExactly(appPage1, appPage2);

        assertThat(searchResultPage2).isNotNull();
        assertThat(searchResultPage2.getCount()).isEqualTo(3);
        assertThat(searchResultPage2.getResult()).containsExactly(appPage3);

        getLivingApplicationAPI().deleteApplication(application.getId());
    }

    @Test
    public void searchApplicationPages_can_filter_on_name() throws Exception {
        //given
        final Application application = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app", "My app", "1.0"));
        getLivingApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        final ApplicationPage appPage2 = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "secondPage");
        getLivingApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getAppSearchBuilderOrderByToken(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.TOKEN, "secondPage");
        final SearchResult<ApplicationPage> searchResult = getLivingApplicationAPI()
                .searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(1);
        assertThat(searchResult.getResult()).containsExactly(appPage2);

        getLivingApplicationAPI().deleteApplication(application.getId());
    }

    @Test
    public void searchApplicationPages_can_filter_on_applicationId() throws Exception {
        //given
        final Application application1 = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app1", "My app 1", "1.0"));
        final Application application2 = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app2", "My app 2", "1.0"));
        final ApplicationPage appPage1 = getLivingApplicationAPI().createApplicationPage(application1.getId(),
                getPage().getId(), "firstPage");
        getLivingApplicationAPI().createApplicationPage(application2.getId(), getPage().getId(), "secondPage");
        final ApplicationPage appPage3 = getLivingApplicationAPI().createApplicationPage(application1.getId(),
                getPage().getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getAppSearchBuilderOrderByToken(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.APPLICATION_ID, application1.getId());
        final SearchResult<ApplicationPage> searchResult = getLivingApplicationAPI()
                .searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(2);
        assertThat(searchResult.getResult()).containsExactly(appPage1, appPage3);

        getLivingApplicationAPI().deleteApplication(application1.getId());
    }

    @Test
    public void searchApplicationPages_can_filter_on_pageId() throws Exception {
        //given
        final Page page2 = createPage("custompage_MyPage2");
        final Application application = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app", "My app", "1.0"));
        getLivingApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        final ApplicationPage appPage2 = getLivingApplicationAPI().createApplicationPage(application.getId(),
                page2.getId(), "secondPage");
        final ApplicationPage appPage3 = getLivingApplicationAPI().createApplicationPage(application.getId(),
                page2.getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getAppSearchBuilderOrderByToken(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.PAGE_ID, page2.getId());
        final SearchResult<ApplicationPage> searchResult = getLivingApplicationAPI()
                .searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(2);
        assertThat(searchResult.getResult()).containsExactly(appPage2, appPage3);

        //clean
        getLivingApplicationAPI().deleteApplication(application.getId());
        getPageAPI().deletePage(page2.getId());
    }

    @Test
    public void searchApplicationPages_can_filter_on_id() throws Exception {
        //given
        final Application application = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app", "My app", "1.0"));
        getLivingApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "firstPage");
        final ApplicationPage appPage2 = getLivingApplicationAPI().createApplicationPage(application.getId(),
                getPage().getId(), "secondPage");
        getLivingApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "thirdPage");

        //when
        final SearchOptionsBuilder builder = getAppSearchBuilderOrderByToken(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.ID, appPage2.getId());
        final SearchResult<ApplicationPage> searchResult = getLivingApplicationAPI()
                .searchApplicationPages(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(1);
        assertThat(searchResult.getResult()).containsExactly(appPage2);

        getLivingApplicationAPI().deleteApplication(application.getId());
    }

    private SearchOptions buildSearchOptions(final int startIndex, final int maxResults) {
        return getAppSearchBuilderOrderByToken(startIndex, maxResults).done();
    }

    @Test
    public void getAllAccessiblePageForAProfile() throws Exception {
        //given
        //profile1
        Profile profile1 = getProfileUser();
        //app1
        final Application app1 = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app1", "My app1", "1.0").setProfileId(profile1.getId()));
        final Page page1 = createPage("custompage_page1");
        getLivingApplicationAPI().createApplicationPage(app1.getId(), page1.getId(), "appPage1");

        final Page page2 = createPage("custompage_page2");
        getLivingApplicationAPI().createApplicationPage(app1.getId(), page2.getId(), "appPage2");

        //app2
        final Application app2 = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app2", "My app2", "1.0").setProfileId(profile1.getId()));
        final Page page3 = createPage("custompage_page3");
        getLivingApplicationAPI().createApplicationPage(app2.getId(), page3.getId(), "appPage1");

        //profile2
        Profile profile2 = getProfileAdmin();
        //app3
        final Application app3 = getLivingApplicationAPI()
                .createApplication(new ApplicationCreator("app3", "My app3", "1.0").setProfileId(profile2.getId()));
        final Page page4 = createPage("custompage_page4");
        getLivingApplicationAPI().createApplicationPage(app3.getId(), page4.getId(), "appPage1");

        //when
        List<String> allPagesForProfile1 = getLivingApplicationAPI().getAllPagesForProfile(profile1.getId());
        List<String> allPagesForProfile2 = getLivingApplicationAPI().getAllPagesForProfile(profile2.getId());

        //then
        assertThat(allPagesForProfile1).containsExactly("custompage_bootstrapdefaulttheme", "custompage_layoutBonita",
                "custompage_page1", "custompage_page2", "custompage_page3");
        assertThat(getLivingApplicationAPI().getAllPagesForProfile(profile1.getName())).containsExactly(
                "custompage_bootstrapdefaulttheme", "custompage_layoutBonita", "custompage_page1", "custompage_page2",
                "custompage_page3");
        assertThat(allPagesForProfile2).containsExactly("custompage_bootstrapdefaulttheme", "custompage_layoutBonita",
                "custompage_page4");
        assertThat(getLivingApplicationAPI().getAllPagesForProfile(profile2.getName()))
                .containsExactly("custompage_bootstrapdefaulttheme", "custompage_layoutBonita", "custompage_page4");

        //clean
        getLivingApplicationAPI().deleteApplication(app1.getId());
        getLivingApplicationAPI().deleteApplication(app2.getId());
        getLivingApplicationAPI().deleteApplication(app3.getId());
        getPageAPI().deletePage(page1.getId());
        getPageAPI().deletePage(page2.getId());
        getPageAPI().deletePage(page3.getId());
        getPageAPI().deletePage(page4.getId());

    }

}
