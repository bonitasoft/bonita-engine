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

import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationAPIIT extends CommonAPISPTest {

    private ApplicationAPI applicationAPI;

    private static User user;

    @Override
    protected void setAPIs() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        super.setAPIs();
        applicationAPI = TenantAPIAccessor.getApplicationAPI(getSession());
    }

    @Before
    public void setUp() throws Exception {
        user = BPMTestSPUtil.createUserOnDefaultTenant("john", "bpm");
        loginOnDefaultTenantWith("john", "bpm");
    }

    @After
    public void tearDown() throws Exception {
        logoutOnTenant();
        BPMTestSPUtil.deleteUserOnDefaultTenant(user);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9199", keywords = { "Application", "create" })
    @Test
    public void createApplication_returns_application_based_on_ApplicationCreator_information() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My Application", "1.0", "/myApplication");

        //when
        final Application application = applicationAPI.createApplication(creator);

        //then
        assertThat(application).isNotNull();
        assertThat(application.getName()).isEqualTo("My Application");
        assertThat(application.getVersion()).isEqualTo("1.0");
        assertThat(application.getPath()).isEqualTo("/myApplication");
        assertThat(application.getId()).isGreaterThan(0);

        applicationAPI.deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9199", keywords = { "Application", "get" })
    @Test
    public void getApplication_returns_application_with_the_given_id() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My Application", "1.0", "/myApplication");
        final Application createdApp = applicationAPI.createApplication(creator);
        assertThat(createdApp).isNotNull();

        //when
        final Application retrivedApp = applicationAPI.getApplication(createdApp.getId());

        //then
        assertThat(retrivedApp).isEqualTo(createdApp);
        applicationAPI.deleteApplication(createdApp.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9199", keywords = { "Application", "get" })
    @Test
    public void deleteApplication_should_delete_application_with_the_given_id() throws Exception {
        //given
        final ApplicationCreator creator = new ApplicationCreator("My Application", "1.0", "/myApplication");
        final Application createdApp = applicationAPI.createApplication(creator);
        assertThat(createdApp).isNotNull();

        //when
        applicationAPI.deleteApplication(createdApp.getId());

        //then
        try {
            applicationAPI.getApplication(createdApp.getId());
            fail("Not found exception");
        } catch (final NotFoundException e) {
            //ok
        }
    }

}
