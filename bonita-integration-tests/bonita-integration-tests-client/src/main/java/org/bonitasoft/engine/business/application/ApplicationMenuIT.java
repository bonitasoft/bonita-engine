/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
package org.bonitasoft.engine.business.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationMenuIT extends TestWithCustomPage {

    private Application application;

    private ApplicationPage appPage;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        application = getApplicationAPI().createApplication(new ApplicationCreator("app", "My app", "1.0"));
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

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu", "create",
            "no application page" })
    @Test
    public void createApplicationMenu_ApplicationPage_should_return_applicationMenu_based_on_creator_and_should_manage_indexes() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(application.getId(), "Main");

        //when
        final ApplicationMenu createdAppMenu = getApplicationAPI().createApplicationMenu(creator);

        //then
        assertThat(createdAppMenu).isNotNull();
        assertThat(createdAppMenu.getDisplayName()).isEqualTo("Main");
        assertThat(createdAppMenu.getApplicationId()).isEqualTo(application.getId());
        assertThat(createdAppMenu.getApplicationPageId()).isNull();
        assertThat(createdAppMenu.getIndex()).isEqualTo(1);
        assertThat(createdAppMenu.getParentId()).isNull();
        assertThat(createdAppMenu.getId()).isGreaterThan(0);

        //when
        //create a second menu
        ApplicationMenu index2Menu = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "second menu"));

        //then
        // should have a index incremented
        assertThat(index2Menu.getIndex()).isEqualTo(2);

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu", "create",
            "related application page" })
    @Test
    public void createApplicationMenu_with_applicationPage_should_return_ApplicationMenu_with_applicationPageId() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(application.getId(), "Main", appPage.getId());

        //when
        final ApplicationMenu createdAppMenu = getApplicationAPI().createApplicationMenu(creator);

        //then
        assertThat(createdAppMenu).isNotNull();
        assertThat(createdAppMenu.getApplicationPageId()).isEqualTo(appPage.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu", "create",
            "parent menu" })
    @Test
    public void createApplicationMenu_with_parent_menu_should_return_ApplicationMenu_with_parentId_and_should_manage_indexes() throws Exception {
        //given
        final ApplicationMenuCreator mainCreator = new ApplicationMenuCreator(application.getId(), "Main");
        final ApplicationMenu mainMenu = getApplicationAPI().createApplicationMenu(mainCreator);

        final ApplicationMenuCreator childCreator = new ApplicationMenuCreator(application.getId(), "Child", appPage.getId());
        childCreator.setParentId(mainMenu.getId());

        //when
        final ApplicationMenu createdAppMenu = getApplicationAPI().createApplicationMenu(childCreator);

        //then
        assertThat(createdAppMenu).isNotNull();
        assertThat(createdAppMenu.getParentId()).isEqualTo(mainMenu.getId());
        assertThat(createdAppMenu.getIndex()).isEqualTo(1);

        //when
        //create a child menu second menu
        ApplicationMenuCreator secondChildCreator = new ApplicationMenuCreator(application.getId(), "second child", appPage.getId());
        secondChildCreator.setParentId(mainMenu.getId());
        ApplicationMenu secondChild = getApplicationAPI().createApplicationMenu(secondChildCreator);

        //then
        //should have incremented index
        assertThat(secondChild.getIndex()).isEqualTo(2);

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu", "update" })
    @Test
    public void updateApplicationMenu_should_update_application_menu_based_on_updater() throws Exception {
        //given
        final ApplicationMenuCreator parentCreator = new ApplicationMenuCreator(application.getId(), "Main");
        final ApplicationMenuCreator childCreator = new ApplicationMenuCreator(application.getId(), "Child");
        final ApplicationMenu parentAppMenu = getApplicationAPI().createApplicationMenu(parentCreator);
        final ApplicationMenu childCreatedAppMenu = getApplicationAPI().createApplicationMenu(childCreator);

        ApplicationMenuUpdater updater = new ApplicationMenuUpdater();
        updater.setApplicationPageId(appPage.getId());
        updater.setParentId(parentAppMenu.getId());
        updater.setDisplayName("Updated child");

        //when
        ApplicationMenu updatedChildMenu = getApplicationAPI().updateApplicationMenu(childCreatedAppMenu.getId(), updater);

        //then
        assertThat(updatedChildMenu).isNotNull();
        //updated:
        assertThat(updatedChildMenu.getDisplayName()).isEqualTo("Updated child");
        assertThat(updatedChildMenu.getApplicationPageId()).isEqualTo(appPage.getId());
        assertThat(updatedChildMenu.getParentId()).isEqualTo(parentAppMenu.getId());
        assertThat(updatedChildMenu.getIndex()).isEqualTo(1); //because parent changed
        //not changed:
        assertThat(updatedChildMenu.getApplicationId()).isEqualTo(application.getId());

        //given
        //check it's possible to clean parent and application page
        updater = new ApplicationMenuUpdater();
        updater.setApplicationPageId(null);
        updater.setParentId(null);

        //when
        updatedChildMenu = getApplicationAPI().updateApplicationMenu(childCreatedAppMenu.getId(), updater);

        //then
        assertThat(updatedChildMenu).isNotNull();
        assertThat(updatedChildMenu).isEqualTo(getApplicationAPI().getApplicationMenu(updatedChildMenu.getId()));
        // updated:
        assertThat(updatedChildMenu.getApplicationPageId()).isNull();
        assertThat(updatedChildMenu.getParentId()).isNull();
        assertThat(updatedChildMenu.getIndex()).isEqualTo(2); //because parent changed
        //not changed:
        assertThat(updatedChildMenu.getDisplayName()).isEqualTo("Updated child");
        assertThat(updatedChildMenu.getApplicationId()).isEqualTo(application.getId());

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu", "update", "index" })
    @Test
    public void updateApplicationMenu_index_should_organize_indexes_for_elements_having_the_same_parent() throws Exception {
        //given
        final ApplicationMenu parentAppMenu1 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "Main1"));
        final ApplicationMenu parentAppMenu2 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "Main2"));

        final ApplicationMenuCreator childCreator1 = new ApplicationMenuCreator(application.getId(), "Child");
        childCreator1.setParentId(parentAppMenu1.getId());
        final ApplicationMenuCreator childCreator2 = new ApplicationMenuCreator(application.getId(), "Child");
        childCreator2.setParentId(parentAppMenu1.getId());
        final ApplicationMenuCreator childCreator3 = new ApplicationMenuCreator(application.getId(), "Child");
        childCreator3.setParentId(parentAppMenu1.getId());

        final ApplicationMenu childCreatedAppMenu1 = getApplicationAPI().createApplicationMenu(childCreator1);
        final ApplicationMenu childCreatedAppMenu2 = getApplicationAPI().createApplicationMenu(childCreator2);
        final ApplicationMenu childCreatedAppMenu3 = getApplicationAPI().createApplicationMenu(childCreator3);

        //when move up
        ApplicationMenu updatedChildMenu = getApplicationAPI().updateApplicationMenu(childCreatedAppMenu3.getId(), new ApplicationMenuUpdater().setIndex(1));

        //then
        assertThat(updatedChildMenu.getIndex()).isEqualTo(1);
        assertThat(getApplicationAPI().getApplicationMenu(childCreatedAppMenu1.getId()).getIndex()).isEqualTo(2);
        assertThat(getApplicationAPI().getApplicationMenu(childCreatedAppMenu2.getId()).getIndex()).isEqualTo(3);

        //when move down
        updatedChildMenu = getApplicationAPI().updateApplicationMenu(childCreatedAppMenu3.getId(), new ApplicationMenuUpdater().setIndex(2));

        //then
        assertThat(updatedChildMenu.getIndex()).isEqualTo(2);
        assertThat(getApplicationAPI().getApplicationMenu(childCreatedAppMenu1.getId()).getIndex()).isEqualTo(1);
        assertThat(getApplicationAPI().getApplicationMenu(childCreatedAppMenu2.getId()).getIndex()).isEqualTo(3);

        //when change parent
        updatedChildMenu = getApplicationAPI().updateApplicationMenu(childCreatedAppMenu3.getId(),
                new ApplicationMenuUpdater().setParentId(parentAppMenu2.getId()));

        //then
        assertThat(updatedChildMenu.getIndex()).isEqualTo(1);
        assertThat(getApplicationAPI().getApplicationMenu(childCreatedAppMenu1.getId()).getIndex()).isEqualTo(1);
        assertThat(getApplicationAPI().getApplicationMenu(childCreatedAppMenu2.getId()).getIndex()).isEqualTo(2);

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu", "get by id" })
    @Test
    public void getApplicationMenu_should_return_the_applicationMenu_identified_by_the_given_id() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(application.getId(), "Main", appPage.getId());
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
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(application.getId(), "Main", appPage.getId());
        final ApplicationMenu createdAppMenu = getApplicationAPI().createApplicationMenu(creator);

        //when
        getApplicationAPI().deleteApplicationMenu(createdAppMenu.getId());

        //then
        getApplicationAPI().getApplicationMenu(createdAppMenu.getId()); //throws exception
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application, Application menu",
            "delete cascade" })
    @Test
    public void deleteApplication_also_deletes_application_pages_and_applicationMenu() throws Exception {
        //given
        final Application application = getApplicationAPI().createApplication(new ApplicationCreator("app2", "My secpond app", "1.0"));
        final ApplicationPage appPage = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "myPage");
        final ApplicationMenu mainMenu = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "Main"));
        ApplicationMenuCreator subMenuCreator = new ApplicationMenuCreator(application.getId(), "Main", appPage.getId());
        subMenuCreator.setParentId(mainMenu.getId());
        final ApplicationMenu subMenu = getApplicationAPI().createApplicationMenu(subMenuCreator);

        //when
        getApplicationAPI().deleteApplication(application.getId());

        //then
        verifyNotExists(mainMenu);
        verifyNotExists(subMenu);
        verifyNotExists(appPage);

    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application page, Application menu",
            "delete cascade" })
    @Test
    public void deleteApplicationPage_also_deletes_related_applicationMenu() throws Exception {
        //given
        final ApplicationPage pageToDelete = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "pageToDelete");
        final ApplicationPage pageToKeep = getApplicationAPI().createApplicationPage(application.getId(), getPage().getId(), "pageToKeep");
        final ApplicationMenu menuToDelete = getApplicationAPI().createApplicationMenu(
                new ApplicationMenuCreator(application.getId(), "Main", pageToDelete.getId()));
        final ApplicationMenu menuToKeep = getApplicationAPI().createApplicationMenu(
                new ApplicationMenuCreator(application.getId(), "Main", pageToKeep.getId()));
        final ApplicationMenu containerMenu = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "Main"));

        //when
        getApplicationAPI().deleteApplicationPage(pageToDelete.getId());

        //then
        // container menu and menu related to another page are keep
        verifyExists(containerMenu);
        verifyExists(menuToKeep);

        // menu related application is deleted
        verifyNotExists(menuToDelete);
    }

    private void verifyExists(ApplicationMenu applicationMenu) throws ApplicationMenuNotFoundException {
        ApplicationMenu retrievedMenu = getApplicationAPI().getApplicationMenu(applicationMenu.getId());
        assertThat(retrievedMenu).isNotNull();
    }

    private void verifyNotExists(ApplicationMenu applicationMenu) {
        try {
            getApplicationAPI().getApplicationMenu(applicationMenu.getId()); //throws exception
            fail("exception expected");
        } catch (ApplicationMenuNotFoundException e) {
            //OK
        }
    }

    private void verifyNotExists(ApplicationPage applicationPage) {
        try {
            getApplicationAPI().getApplicationPage(applicationPage.getId()); //throws exception
            fail("exception expected");
        } catch (ApplicationPageNotFoundException e) {
            //OK
        }
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu",
            "search", "all" })
    @Test
    public void searchApplicationMenus_without_filters_without_search_term_should_return_all_applicationMenues_pagged() throws Exception {
        //given
        final ApplicationMenu menu1 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "first", appPage.getId()));
        final ApplicationMenu menu2 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "second", appPage.getId()));
        final ApplicationMenu menu3 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "third", appPage.getId()));

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
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "first", appPage.getId()));
        final ApplicationMenu menu2 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "second", appPage.getId()));
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "third", appPage.getId()));

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
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "first", appPage.getId()));
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "second", appPage.getId()));
        final ApplicationMenu menu3 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "third", appPage.getId()));

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
        final ApplicationMenu menu1 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "first", appPage.getId()));
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "second", appPage2.getId()));
        final ApplicationMenu menu3 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "third", appPage.getId()));

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
        final Application application2 = getApplicationAPI().createApplication(new ApplicationCreator("app2", "My second app", "1.0"));
        final ApplicationPage appPage2 = getApplicationAPI().createApplicationPage(application2.getId(), getPage().getId(), "mySecondPage");

        final ApplicationMenu menu1 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application2.getId(), "first", appPage2.getId()));
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "second", appPage.getId()));
        final ApplicationMenu menu3 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application2.getId(), "third", appPage2.getId()));

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
            "search", "filter on parent id" })
    @Test
    public void searchApplicationMenus_can_filter_on_parentId() throws Exception {
        //given
        final ApplicationMenu parentMenu = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "parent"));

        final ApplicationMenuCreator creator = new ApplicationMenuCreator(application.getId(), "child", appPage.getId());
        creator.setParentId(parentMenu.getId());
        final ApplicationMenu childMenu = getApplicationAPI().createApplicationMenu(creator);

        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "third", appPage.getId()));

        //when
        final SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationMenuSearchDescriptor.PARENT_ID, parentMenu.getId());
        final SearchResult<ApplicationMenu> searchResult = getApplicationAPI().searchApplicationMenus(builder.done());

        //then
        assertThat(searchResult).isNotNull();
        assertThat(searchResult.getCount()).isEqualTo(1);
        assertThat(searchResult.getResult()).containsExactly(childMenu);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9216", keywords = { "Application menu",
            "search", "search term" })
    @Test
    public void searchApplicationMenus_can_use_searchTerm() throws Exception {
        //given
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "first", appPage.getId()));
        final ApplicationMenu menu2 = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "second", appPage.getId()));
        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(application.getId(), "third", appPage.getId()));

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
