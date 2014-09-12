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

import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.api.ApplicationAPI;

/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationAPIApplicationMenuIT extends TestWithCustomPage {

    private Application application;

    private ApplicationPage appPage;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        application = getApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0", "/app"));
        appPage = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "myPage");
    }

    @Override
    @After
    public void tearDown() throws Exception {
        getApplicationAPI().deleteApplication(application.getId());
        application = null;
        appPage = null;
        super.tearDown();
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu", "create" })
    @Test
    public void createApplicationMenu_should_return_applicationMenu_based_on_creator() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator("Main", appPage.getId(), 1);

        //when
        final ApplicationMenu createdAppMenu = getApplicationAPI().createApplicationMenu(creator);

        //then
        assertThat(createdAppMenu).isNotNull();
        assertThat(createdAppMenu.getDisplayName()).isEqualTo("Main");
        assertThat(createdAppMenu.getApplicationPageId()).isEqualTo(appPage.getId());
        assertThat(createdAppMenu.getIndex()).isEqualTo(1);
        assertThat(createdAppMenu.getParentId()).isNull();
        assertThat(createdAppMenu.getId()).isGreaterThan(0);

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu", "get by id" })
    @Test
    public void getApplicationMenu_should_return_the_applicationMenu_identified_by_the_given_id() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator("Main", appPage.getId(), 1);
        final ApplicationMenu createdAppMenu = getApplicationAPI().createApplicationMenu(creator);

        //when
        final ApplicationMenu retrievedMenu = getApplicationAPI().getApplicationMenu(createdAppMenu.getId());

        //then
        assertThat(retrievedMenu).isNotNull();
        assertThat(retrievedMenu).isEqualTo(createdAppMenu);

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu", "delete" })
    @Test(expected = ApplicationMenuNotFoundException.class)
    public void deleteApplicationMenu_should_remove_the_applicationMenu_identified_by_the_given_id() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator("Main", appPage.getId(), 1);
        final ApplicationMenu createdAppMenu = getApplicationAPI().createApplicationMenu(creator);

        //when
        getApplicationAPI().deleteApplicationMenu(createdAppMenu.getId());

        //then
        getApplicationAPI().getApplicationMenu(createdAppMenu.getId()); //throws exception
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application, Application menu",
    "delete cascade" })
    @Test(expected = ApplicationMenuNotFoundException.class)
    public void deleteApplication_also_deletes_applicationMenu() throws Exception {
        //given
        final Application application = getApplicationAPI().createApplication(new ApplicationCreator("app2", "My secpond app", "1.0", "/app2"));
        final ApplicationPage appPage = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "myPage");
        final ApplicationMenuCreator creator = new ApplicationMenuCreator("Main", appPage.getId(), 1);
        final ApplicationMenu createdAppMenu = getApplicationAPI().createApplicationMenu(creator);

        //when
        getApplicationAPI().deleteApplication(application.getId());

        //then
        getApplicationAPI().getApplicationMenu(createdAppMenu.getId()); //throws exception
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu",
            "search", "all" })
    @Test
    public void searchApplicationMenus_without_filters_without_search_term_should_return_all_applicationMenues_pagged() throws Exception {
        //given
        final ApplicationMenu menu1 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("first", appPage.getId(), 1));
        final ApplicationMenu menu2 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("second", appPage.getId(), 2));
        final ApplicationMenu menu3 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("third", appPage.getId(), 3));

        //when
        final SearchResult<ApplicationMenu> firstPage = getApplicationAPI().searchApplicationMenus(buildSearchOptions(0, 2));
        final SearchResult<ApplicationMenu> secondPage = getApplicationAPI().searchApplicationMenus(buildSearchOptions(2, 2));

        //then
        assertThat(firstPage).isNotNull();
        assertThat(firstPage.getCount()).isEqualTo(3);
        assertThat(firstPage.getResult()).containsExactly(menu1, menu2);
        assertThat(secondPage).isNotNull();
        assertThat(secondPage.getCount()).isEqualTo(3);
        assertThat(secondPage.getResult()).containsExactly(menu3);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu",
            "search", "filter on display name" })
    @Test
    public void searchApplicationMenus_can_filter_on_displayname() throws Exception {
        //given
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("first", appPage.getId(), 1));
        final ApplicationMenu menu2 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("second", appPage.getId(), 2));
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("third", appPage.getId(), 3));

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationMenuSearchDescriptor.DISPLAY_NAME, "second");
        final SearchResult<ApplicationMenu> searchResult = getApplicationAPI().searchApplicationMenus(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(1);
        assertThat(searchResult.getResult()).containsExactly(menu2);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu",
            "search", "filter on index" })
    @Test
    public void searchApplicationMenus_can_filter_on_index() throws Exception {
        //given
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("first", appPage.getId(), 1));
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("second", appPage.getId(), 2));
        final ApplicationMenu menu3 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("third", appPage.getId(), 3));

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationMenuSearchDescriptor.INDEX, 3);
        final SearchResult<ApplicationMenu> searchResult = getApplicationAPI().searchApplicationMenus(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(1);
        assertThat(searchResult.getResult()).containsExactly(menu3);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu",
            "search", "filter on application page id" })
    @Test
    public void searchApplicationMenus_can_filter_on_applicationPageId() throws Exception {
        //given
        final ApplicationPage appPage2 = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "mySecondPage");
        final ApplicationMenu menu1 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("first", appPage.getId(), 1));
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("second", appPage2.getId(), 2));
        final ApplicationMenu menu3 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("third", appPage.getId(), 3));

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationMenuSearchDescriptor.APPLICATION_PAGE_ID, appPage.getId());
        final SearchResult<ApplicationMenu> searchResult = getApplicationAPI().searchApplicationMenus(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(2);
        assertThat(searchResult.getResult()).containsExactly(menu1, menu3);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu",
            "search", "filter on application id" })
    @Test
    public void searchApplicationMenus_can_filter_on_applicationId() throws Exception {
        //given
        final Application application2 = getApplicationAPI().createApplication(new ApplicationCreator("app2", "My second app", "1.0", "/app"));
        final ApplicationPage appPage2 = getApplicationAPI().createApplicationPage(application2.getId(), getPage().getId(), "mySecondPage");

        final ApplicationMenu menu1 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("first", appPage2.getId(), 1));
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("second", appPage.getId(), 2));
        final ApplicationMenu menu3 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("third", appPage2.getId(), 3));

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationMenuSearchDescriptor.APPLICATION_ID, application2.getId());
        final SearchResult<ApplicationMenu> searchResult = getApplicationAPI().searchApplicationMenus(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(2);
        assertThat(searchResult.getResult()).containsExactly(menu1, menu3);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu",
            "search", "search term" })
    @Test
    public void searchApplicationMenus_can_use_searchTerm() throws Exception {
        //given
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("first", appPage.getId(), 1));
        final ApplicationMenu menu2 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("second", appPage.getId(), 2));
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator("third", appPage.getId(), 3));

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.searchTerm("second");
        final SearchResult<ApplicationMenu> searchResult = getApplicationAPI().searchApplicationMenus(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(1);
        assertThat(searchResult.getResult()).containsExactly(menu2);
    }

    private SearchOptions buildSearchOptions(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = getDefaultBuilder(startIndex, maxResults);
        final SearchOptions options = builder.done();
        return options;
    }

    private SearchOptionsBuilder getDefaultBuilder(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(startIndex, maxResults);
        builder.sort(ApplicationMenuSearchDescriptor.INDEX, Order.ASC);
        return builder;
    }

}
