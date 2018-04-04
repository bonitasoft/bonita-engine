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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.page.PageCreator;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.profile.ProfileSearchDescriptor;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.After;
import org.junit.Before;

/**
 * @author Elias Ricken de Medeiros
 */
public class TestWithApplication extends CommonAPIIT {

    public static final String DEFAULT_LAYOUT_NAME = "custompage_defaultlayout";
    public static final String DEFAULT_THEME_NAME = "custompage_bootstrapdefaulttheme";
    private User user;

    @Before
    public void setUp() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        user = createUser("john", "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith("john", "bpm");
    }

    @After
    public void tearDown() throws Exception {
        final SearchResult<Application> searchResult = getApplicationAPI().searchApplications(new SearchOptionsBuilder(0, 1000).done());
        for (final Application app : searchResult.getResult()) {
            getApplicationAPI().deleteApplication(app.getId());
        }
        logoutThenlogin();
        deleteUser(user);
        logoutOnTenant();
    }

    protected Profile getProfileUser() throws SearchException {
        return getProfile("User");
    }

    protected Profile getProfileAdmin() throws SearchException {
        return getProfile("Administrator");
    }

    protected Profile getProfile(String profileName) throws SearchException {
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 1);
        builder.filter(ProfileSearchDescriptor.NAME, profileName);
        SearchResult<Profile> profileSearchResult = getProfileAPI().searchProfiles(builder.done());
        assertThat(profileSearchResult.getCount()).isEqualTo(1);
        Profile profile = profileSearchResult.getResult().get(0);
        return profile;
    }

    public User getUser() {
        return user;
    }

    protected Page createPage(final String pageName) throws Exception {
        return getPageAPI().createPage(new PageCreator(pageName, "content.zip").setDisplayName(pageName), createPageContent(pageName));
    }

    private byte[] createPageContent(final String pageName)
            throws BonitaException {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ZipOutputStream zos = new ZipOutputStream(baos);
            zos.putNextEntry(new ZipEntry("Index.groovy"));
            zos.write("return \"\";".getBytes());

            zos.putNextEntry(new ZipEntry("page.properties"));
            String s = "name=" + pageName + "\n"
                    + "displayName=no display name\n"
                    + "description=empty desc\n";
            zos.write(s.getBytes("UTF-8"));

            zos.closeEntry();
            return baos.toByteArray();
        } catch (final IOException e) {
            throw new BonitaException(e);
        }
    }

    protected SearchOptionsBuilder getAppSearchBuilderOrderByToken(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(startIndex, maxResults);
        builder.sort(ApplicationSearchDescriptor.TOKEN, Order.ASC);
        return builder;
    }

    protected void assertIsAddOkStatus(final ImportStatus importStatus, String expectedToken) {
        assertThat(importStatus.getName()).isEqualTo(expectedToken);
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).isEmpty();
    }

    protected SearchOptionsBuilder getAppSearchBuilderOrderById(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(startIndex, maxResults);
        builder.sort(ApplicationSearchDescriptor.ID, Order.ASC);
        return builder;
    }

    protected void assertIsMarketingApplication(final Application app) {
        assertThat(app.getToken()).isEqualTo("My");
        assertThat(app.getVersion()).isEqualTo("2.0");
        assertThat(app.getDisplayName()).isEqualTo("Marketing");
        assertThat(app.getDescription()).isNull();
        assertThat(app.getIconPath()).isNull();
        assertThat(app.getState()).isEqualTo("ACTIVATED");
        assertThat(app.getProfileId()).isNull();
    }

    protected void assertIsHRApplication(final Profile profile, final Page layout, final Page theme, final Application app) {
        assertThat(app.getToken()).isEqualTo("HR-dashboard");
        assertThat(app.getVersion()).isEqualTo("2.0");
        assertThat(app.getDisplayName()).isEqualTo("My HR dashboard");
        assertThat(app.getDescription()).isEqualTo("This is the HR dashboard.");
        assertThat(app.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(app.getState()).isEqualTo("ACTIVATED");
        assertThat(app.getProfileId()).isEqualTo(profile.getId());
        assertThat(app.getLayoutId()).isEqualTo(layout.getId());
        assertThat(app.getThemeId()).isEqualTo(theme.getId());
    }

    protected void assertIsMyNewCustomPage(final Page myPage, final Application hrApp, final ApplicationPage applicationPage) {
        assertThat(applicationPage.getApplicationId()).isEqualTo(hrApp.getId());
        assertThat(applicationPage.getToken()).isEqualTo("my-new-custom-page");
        assertThat(applicationPage.getPageId()).isEqualTo(myPage.getId());
    }

    protected void assertIsHrFollowUpMenu(final ApplicationMenu applicationMenu) {
        assertThat(applicationMenu.getIndex()).isEqualTo(1);
        assertThat(applicationMenu.getParentId()).isNull();
        assertThat(applicationMenu.getDisplayName()).isEqualTo("HR follow-up");
        assertThat(applicationMenu.getApplicationPageId()).isNull();
    }

    protected void assertIsDailyHrFollowUpMenu(final ApplicationMenu applicationMenu, ApplicationMenu hrFollowUpMenu, ApplicationPage myNewCustomPage) {
        assertThat(applicationMenu.getIndex()).isEqualTo(1);
        assertThat(applicationMenu.getParentId()).isEqualTo(hrFollowUpMenu.getId());
        assertThat(applicationMenu.getDisplayName()).isEqualTo("Daily HR follow-up");
        assertThat(applicationMenu.getApplicationPageId()).isEqualTo(myNewCustomPage.getId());
    }

    protected void assertIsEmptyMenu(final ApplicationMenu applicationMenu) {
        assertThat(applicationMenu.getIndex()).isEqualTo(2);
        assertThat(applicationMenu.getParentId()).isNull();
        assertThat(applicationMenu.getDisplayName()).isEqualTo("Empty menu");
        assertThat(applicationMenu.getApplicationPageId()).isNull();
    }
}
