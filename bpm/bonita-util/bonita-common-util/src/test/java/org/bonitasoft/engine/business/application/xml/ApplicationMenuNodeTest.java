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

import static org.bonitasoft.engine.business.application.xml.ApplicationMenuNodeAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ApplicationMenuNodeTest {

    @Test
    public void setter_and_getter_are_working() throws Exception {
        //given
        ApplicationMenuNode menuNode = new ApplicationMenuNode();
        menuNode.setApplicationPage("app");
        menuNode.setDisplayName("Main menu");

        //then
        assertThat(menuNode).hasApplicationPage("app");
        assertThat(menuNode).hasDisplayName("Main menu");
    }

    @Test
    public void getApplicationMenus_should_return_empty_list_when_no_elements_were_added() throws Exception {
        //given
        ApplicationMenuNode menuNode = new ApplicationMenuNode();

        //then
        assertThat(menuNode).hasNoApplicationMenus();
    }

    @Test
    public void addApplicationMenu_should_add_a_new_entry_in_the_current_application_menus() throws Exception {
        //given
        ApplicationMenuNode subMenu1 = new ApplicationMenuNode();
        subMenu1.setDisplayName("support");
        ApplicationMenuNode subMenu2 = new ApplicationMenuNode();
        subMenu2.setDisplayName("commerce");

        ApplicationMenuNode mainMenu = new ApplicationMenuNode();
        mainMenu.addApplicationMenu(subMenu1);
        mainMenu.addApplicationMenu(subMenu2);

        //when

        //then
        assertThat(mainMenu).hasApplicationMenus(subMenu1, subMenu2);
    }

    @Test
    public void equals_should_return_true_on_different_menus_with_same_content() {
        ApplicationMenuNode menu1 = ApplicationNodeBuilder.newMenu("Menu", "Page")
                .havingMenu(
                        ApplicationNodeBuilder.newMenu("Menu2", "Page2"),
                        ApplicationNodeBuilder.newMenu("Menu3", "Page3"))
                .create();
        ApplicationMenuNode menu2 = ApplicationNodeBuilder.newMenu("Menu", "Page")
                .havingMenu(
                        ApplicationNodeBuilder.newMenu("Menu2", "Page2"),
                        ApplicationNodeBuilder.newMenu("Menu3", "Page3"))
                .create();
        assertTrue(menu1.equals(menu2));
    }

    @Test
    public void equals_should_return_false_on_different_menus_with_different_content() {
        ApplicationMenuNode menu1 = ApplicationNodeBuilder.newMenu("Menu", "Page")
                .havingMenu(
                        ApplicationNodeBuilder.newMenu("Menu2", "Page2"),
                        ApplicationNodeBuilder.newMenu("Menu3", "Page3"))
                .create();
        ApplicationMenuNode menu2 = ApplicationNodeBuilder.newMenu("Menu", "Page")
                .havingMenu(
                        ApplicationNodeBuilder.newMenu("Menu2", "Page2"),
                        ApplicationNodeBuilder.newMenu("Menu3_diff", "Page3"))
                .create();
        assertFalse(menu1.equals(menu2));
    }
}
