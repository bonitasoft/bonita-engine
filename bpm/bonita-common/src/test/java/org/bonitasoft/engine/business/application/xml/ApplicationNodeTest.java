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
package org.bonitasoft.engine.business.application.xml;

import static org.bonitasoft.engine.business.application.xml.ApplicationNodeAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ApplicationNodeTest {

    @Test
    public void simple_setters_and_getters_are_working() throws Exception {
        //given
        final String displayName = "My application";
        final String description = "This is my main application";
        final String homePage = "home";
        final String iconPath = "/icon.jpg";
        final String layout = "leftMenu";
        final String theme = "defaultTheme";
        final String profile = "User";
        final String token = "myapp";
        final String version = "1.0";
        final String state = "ACTIVATED";

        //when
        final ApplicationNode app = new ApplicationNode();
        app.setDisplayName(displayName);
        app.setDescription(description);
        app.setHomePage(homePage);
        app.setIconPath(iconPath);
        app.setLayout(layout);
        app.setTheme(theme);
        app.setProfile(profile);
        app.setState(state);
        app.setToken(token);
        app.setVersion(version);

        //then
        assertThat(app).hasDisplayName(displayName).hasDescription(description).hasHomePage(homePage).hasIconPath(iconPath)
                .hasLayout(layout)
                .hasTheme(theme).hasProfile(profile).hasState(state).hasToken(token).hasVersion(version);
    }

    @Test
    public void getApplicationPages_should_return_empty_list_when_no_elements_were_added() throws Exception {
        //given
        final ApplicationNode applicationNode = new ApplicationNode();

        //then
        assertThat(applicationNode).hasNoApplicationPages();
    }

    @Test
    public void getApplicationMenus_should_return_empty_list_when_no_elements_were_added() throws Exception {
        //given
        final ApplicationNode applicationNode = new ApplicationNode();

        //then
        assertThat(applicationNode).hasNoApplicationMenus();
    }

    @Test
    public void addApplicationPage_should_add_a_new_element_in_the_current_page_list() throws Exception {
        //given
        final ApplicationNode applicationNode = new ApplicationNode();
        final ApplicationPageNode applicationPage1 = new ApplicationPageNode();
        applicationPage1.setToken("page1");

        final ApplicationPageNode applicationPage2 = new ApplicationPageNode();
        applicationNode.setToken("page2");

        //when
        applicationNode.addApplicationPage(applicationPage1);
        applicationNode.addApplicationPage(applicationPage2);

        //then
        assertThat(applicationNode).hasApplicationPages(applicationPage1, applicationPage2);
    }

    @Test
    public void addApplicationMenu_should_add_a_new_element_in_the_current_menu_list() throws Exception {
        //given
        final ApplicationNode applicationNode = new ApplicationNode();
        final ApplicationMenuNode menu1 = new ApplicationMenuNode();
        menu1.setDisplayName("entry1");

        final ApplicationMenuNode menu2 = new ApplicationMenuNode();
        menu2.setDisplayName("entry2");

        //when
        applicationNode.addApplicationMenu(menu1);
        applicationNode.addApplicationMenu(menu2);

        //then
        assertThat(applicationNode).hasApplicationMenus(menu1, menu2);
    }

    @Test
    public void equals_should_return_true_on_different_applications_with_same_content() {
        ApplicationNode applicationNode1 = ApplicationNodeBuilder.newApplication("appToken", "appName", "1.0")
                .havingApplicationPages(
                        ApplicationNodeBuilder.newApplicationPage("page1", "tokenPage1"),
                        ApplicationNodeBuilder.newApplicationPage("page2", "tokenPage2"))
                .havingApplicationMenus(
                        ApplicationNodeBuilder.newMenu("topMenu", "").havingMenu(
                                ApplicationNodeBuilder.newMenu("menu1", "page1"),
                                ApplicationNodeBuilder.newMenu("menu2", "page2")))
                .create();
        ApplicationNode applicationNode2 = ApplicationNodeBuilder.newApplication("appToken", "appName", "1.0")
                .havingApplicationPages(
                        ApplicationNodeBuilder.newApplicationPage("page1", "tokenPage1"),
                        ApplicationNodeBuilder.newApplicationPage("page2", "tokenPage2"))
                .havingApplicationMenus(
                        ApplicationNodeBuilder.newMenu("topMenu", "").havingMenu(
                                ApplicationNodeBuilder.newMenu("menu1", "page1"),
                                ApplicationNodeBuilder.newMenu("menu2", "page2")))
                .create();
        assertTrue(applicationNode1.equals(applicationNode2));
    }

    @Test
    public void equals_should_return_false_on_different_applications_with_different_content() {
        ApplicationNode applicationNode1 = ApplicationNodeBuilder.newApplication("appToken", "appName", "1.0")
                .havingApplicationPages(
                        ApplicationNodeBuilder.newApplicationPage("page1", "tokenPage1"),
                        ApplicationNodeBuilder.newApplicationPage("page2_diff", "tokenPage2"))
                .havingApplicationMenus(
                        ApplicationNodeBuilder.newMenu("topMenu", "").havingMenu(
                                ApplicationNodeBuilder.newMenu("menu1", "page1"),
                                ApplicationNodeBuilder.newMenu("menu2", "page2_diff")))
                .create();
        ApplicationNode applicationNode2 = ApplicationNodeBuilder.newApplication("appToken", "appName", "1.0")
                .havingApplicationPages(
                        ApplicationNodeBuilder.newApplicationPage("page1", "tokenPage1"),
                        ApplicationNodeBuilder.newApplicationPage("page2", "tokenPage2"))
                .havingApplicationMenus(
                        ApplicationNodeBuilder.newMenu("topMenu", "").havingMenu(
                                ApplicationNodeBuilder.newMenu("menu1", "page1"),
                                ApplicationNodeBuilder.newMenu("menu2", "page2")))
                .create();
        assertFalse(applicationNode1.equals(applicationNode2));
    }

}
