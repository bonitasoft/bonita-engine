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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.page.Page;
import com.bonitasoft.engine.page.PageCreator;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class ApplicationAPIApplicationPageIT extends CommonAPISPTest {

    private ApplicationAPI applicationAPI;

    private static User user;

    private Page page;

    @Override
    protected void setAPIs() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        super.setAPIs();
        applicationAPI = TenantAPIAccessor.getApplicationAPI(getSession());
    }

    @Before
    public void setUp() throws Exception {
        user = BPMTestSPUtil.createUserOnDefaultTenant("john", "bpm");
        loginOnDefaultTenantWith("john", "bpm");
        try {
            page = createPage("custompage_MyPage");
        } catch (final AlreadyExistsException e) {
            throw e;
        }
    }

    @After
    public void tearDown() throws Exception {
        final SearchResult<Application> searchResult = applicationAPI.searchApplications(new SearchOptionsBuilder(0, 1000).done());
        for (final Application app : searchResult.getResult()) {
            applicationAPI.deleteApplication(app.getId());
        }
        if (page != null) {
            getPageAPI().deletePage(page.getId());
        }
        logoutOnTenant();
        BPMTestSPUtil.deleteUserOnDefaultTenant(user);
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page", "create" })
    @Test
    public void createApplicationPage_returns_applicationPage_based_on_the_given_parameters() throws Exception {
        //given
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));

        //when
        final ApplicationPage appPage = applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");

        //then
        assertThat(appPage.getId()).isGreaterThan(0);
        assertThat(appPage.getApplicationId()).isEqualTo(application.getId());
        assertThat(appPage.getPageId()).isEqualTo(page.getId());
        assertThat(appPage.getName()).isEqualTo("firstPage");

        applicationAPI.deleteApplication(application.getId());

    }

    private byte[] createPageContent(final String pageName)
            throws BonitaException {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            zos.putNextEntry(new ZipEntry("Index.groovy"));
            zos.write("return \"\";".getBytes());

            zos.putNextEntry(new ZipEntry("page.properties"));
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("name=");
            stringBuilder.append(pageName);
            stringBuilder.append("\n");
            stringBuilder.append("displayName=");
            stringBuilder.append("no display name");
            stringBuilder.append("\n");
            stringBuilder.append("description=");
            stringBuilder.append("empty desc");
            stringBuilder.append("\n");
            zos.write(stringBuilder.toString().getBytes());

            zos.closeEntry();
            return baos.toByteArray();
        } catch (final IOException e) {
            throw new BonitaException(e);
        }
    }

    private Page createPage(final String pageName) throws Exception {
        final Page page = getPageAPI().createPage(new PageCreator(pageName, "content.zip").setDisplayName(pageName), createPageContent(pageName));
        return page;
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
    "get by name and application name" })
    @Test
    public void getApplicationPage_byNameAndAppName_returns_the_applicationPage_corresponding_to_the_given_parameters() throws Exception {
        //given
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        final ApplicationPage appPage = applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");

        //when
        final ApplicationPage retrievedAppPage = applicationAPI.getApplicationPage(application.getName(), appPage.getName());

        //then
        assertThat(retrievedAppPage).isEqualTo(appPage);
        applicationAPI.deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application page",
    "get by id" })
    @Test
    public void getApplicationPage_byId_returns_the_applicationPage_corresponding_to_the_given_Id() throws Exception {
        //given
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        final ApplicationPage appPage = applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");

        //when
        final ApplicationPage retrievedAppPage = applicationAPI.getApplicationPage(appPage.getId());

        //then
        assertThat(retrievedAppPage).isEqualTo(appPage);
        applicationAPI.deleteApplication(application.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = BPMNConcept.APPLICATION, jira = "BS-9212", keywords = { "Application", "Application page",
    "delete" })
    @Test
    public void deleteApplication_should_also_delete_related_applicationPage() throws Exception {
        //given
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        final ApplicationPage appPage = applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");

        //when
        applicationAPI.deleteApplication(application.getId());

        //then
        try {
            applicationAPI.getApplicationPage(appPage.getId());
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
        final Application application = applicationAPI.createApplication(new ApplicationCreator("app", "1.0", "/app"));
        final ApplicationPage appPage = applicationAPI.createApplicationPage(application.getId(), page.getId(), "firstPage");

        //when
        applicationAPI.deleteApplicationPage(appPage.getId());

        //then
        try {
            applicationAPI.getApplicationPage(appPage.getId());
            fail("Not found expected");
        } catch (final ApplicationPageNotFoundException e) {
            //OK
        }
    }

}
