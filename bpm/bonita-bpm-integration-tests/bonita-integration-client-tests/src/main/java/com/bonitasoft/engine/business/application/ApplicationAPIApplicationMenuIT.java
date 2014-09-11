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

}
