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

import org.bonitasoft.engine.business.application.ApplicationState;
import org.junit.Test;

public class ApplicationNodeTest {

    @Test
    public void simple_setters_and_getters_are_working() throws Exception {
        //given
        String displayName = "My application";
        String description = "This is my main application";
        String homePage = "home";
        String iconPath = "/icon.jpg";
        String layout = "leftMenu";
        String profile = "User";
        String token = "myapp";
        String version = "1.0";
        String state = ApplicationState.ACTIVATED.name();

        //when
        ApplicationNode app = new ApplicationNode();
        app.setDisplayName(displayName);
        app.setDescription(description);
        app.setHomePage(homePage);
        app.setIconPath(iconPath);
        app.setLayout(layout);
        app.setProfile(profile);
        app.setState(state);
        app.setToken(token);
        app.setVersion(version);

        //then
        assertThat(app).hasDisplayName(displayName).hasDescription(description).hasHomePage(homePage).hasIconPath(iconPath).hasLayout(layout)
                .hasProfile(profile).hasState(state).hasToken(token).hasVersion(version);
    }

    @Test
    public void getApplicationPages_should_return_empty_list_when_no_elements_were_added() throws Exception {
        //given
        ApplicationNode applicationNode = new ApplicationNode();

        //then
        assertThat(applicationNode).hasNoApplicationPages();
    }

    @Test
    public void getApplicationMenus_should_return_empty_list_when_no_elements_were_added() throws Exception {
        //given
        ApplicationNode applicationNode = new ApplicationNode();

        //then
        assertThat(applicationNode).hasNoApplicationMenus();
    }

    @Test
    public void addApplicationPage_should_add_a_new_element_in_the_current_page_list() throws Exception {
        //given
        ApplicationNode applicationNode = new ApplicationNode();
        ApplicationPageNode applicationPage1 = new ApplicationPageNode();
        applicationPage1.setToken("page1");

        ApplicationPageNode applicationPage2 = new ApplicationPageNode();
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
        ApplicationNode applicationNode = new ApplicationNode();
        ApplicationMenuNode menu1 = new ApplicationMenuNode();
        menu1.setDisplayName("entry1");

        ApplicationMenuNode menu2 = new ApplicationMenuNode();
        menu2.setDisplayName("entry2");

        //when
        applicationNode.addApplicationMenu(menu1);
        applicationNode.addApplicationMenu(menu2);

        //then
        assertThat(applicationNode).hasApplicationMenus(menu1, menu2);
    }

}
