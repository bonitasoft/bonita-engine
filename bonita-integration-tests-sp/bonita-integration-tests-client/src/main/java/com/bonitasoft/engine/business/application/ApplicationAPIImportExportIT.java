/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.assertj.core.util.xml.XmlStringPrettyFormatter;
import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.page.Page;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationAPIImportExportIT extends CommonAPISPTest {

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
        final SearchResult<Application> searchResult = applicationAPI.searchApplications(new SearchOptionsBuilder(0, 1000).done());
        for (final Application app : searchResult.getResult()) {
            applicationAPI.deleteApplication(app.getId());
        }
        logoutOnTenant();
        BPMTestSPUtil.deleteUserOnDefaultTenant(user);
    }

    private SearchOptionsBuilder getDefaultBuilder(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(startIndex, maxResults);
        builder.sort(ApplicationSearchDescriptor.TOKEN, Order.ASC);
        return builder;
    }

    private SearchOptions buildSearchOptions(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = getDefaultBuilder(startIndex, maxResults);
        final SearchOptions options = builder.done();
        return options;
    }

    @Cover(classes = { ApplicationAPI.class }, concept = Cover.BPMNConcept.APPLICATION, jira = "BS-9215", keywords = { "Application", "export" })
    @Test
    public void exportApplications_should_return_the_byte_content_of_xml_file_containing_selected_applications() throws Exception {
        //given
        final Profile profile = getProfileAPI().createProfile("ApplicationProfile", "Profile for applications");

        final byte[] applicationsByteArray = IOUtils.toByteArray(ApplicationAPIApplicationIT.class
                .getResourceAsStream("applications.xml"));
        final String xmlPrettyFormatExpected = XmlStringPrettyFormatter.xmlPrettyFormat(new String(applicationsByteArray));

        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "My HR dashboard", "2.0");
        hrCreator.setDescription("This is the HR dashboard.");
        hrCreator.setIconPath("/icon.jpg");
        hrCreator.setProfileId(profile.getId());

        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard", "Engineering dashboard", "1.0");
        final ApplicationCreator marketingCreator = new ApplicationCreator("My", "Marketing", "2.0");
        final Application hr = applicationAPI.createApplication(hrCreator);

        // Associate a new page to application hr (real page name is defined in zip/page.properties):
        final Page myPage = getPageAPI().createPage("not_used",
                IOUtils.toByteArray(ApplicationAPIApplicationIT.class.getResourceAsStream("dummy-bizapp-page.zip")));
        final ApplicationPage appPage = applicationAPI.createApplicationPage(hr.getId(), myPage.getId(), "my-new-custom-page");

        // Add menus:
        final ApplicationMenu menu = applicationAPI.createApplicationMenu(new ApplicationMenuCreator(hr.getId(), "HR follow-up"));
        final ApplicationMenuCreator subMenuCreator = new ApplicationMenuCreator(hr.getId(), "Daily HR follow-up", appPage.getId());
        subMenuCreator.setParentId(menu.getId());
        applicationAPI.createApplicationMenu(subMenuCreator);

        // Add home page:
        applicationAPI.setApplicationHomePage(hr.getId(), appPage.getId());

        applicationAPI.createApplication(engineeringCreator);
        final Application marketing = applicationAPI.createApplication(marketingCreator);

        //when
        final byte[] exportedBytes = applicationAPI.exportApplications(hr.getId(), marketing.getId());
        final String xmlPrettyFormatExported = XmlStringPrettyFormatter.xmlPrettyFormat(new String(exportedBytes));

        //then
        assertThatXmlHaveNoDifferences(xmlPrettyFormatExpected, xmlPrettyFormatExported);

        applicationAPI.deleteApplication(hr.getId());
        getPageAPI().deletePage(myPage.getId());
        getProfileAPI().deleteProfile(profile.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = Cover.BPMNConcept.APPLICATION, jira = "BS-9215", keywords = { "Application", "import" })
    @Test
    public void importApplications_should_create_all_applications_contained_by_xml_file_and_return_status_ok_() throws Exception {
        //given
        final Profile profile = getProfileAPI().createProfile("ApplicationProfile", "Profile for applications");

        // create page necessary to import application hr (real page name is defined in zip/page.properties):
        final Page myPage = getPageAPI().createPage("not_used",
                IOUtils.toByteArray(ApplicationAPIApplicationIT.class.getResourceAsStream("dummy-bizapp-page.zip")));

        final byte[] applicationsByteArray = IOUtils.toByteArray(ApplicationAPIApplicationIT.class
                .getResourceAsStream("applications.xml"));

        //when
        final List<ImportStatus> importStatus = applicationAPI.importApplications(applicationsByteArray, ApplicationImportPolicy.FAIL_ON_DUPLICATES);

        //then
        assertThat(importStatus).hasSize(2);
        assertIsAddOkStatus(importStatus.get(0), "HR-dashboard");
        assertIsAddOkStatus(importStatus.get(1), "My");

        // check applications ware created
        final SearchResult<Application> searchResult = applicationAPI.searchApplications(buildSearchOptions(0, 10));
        assertThat(searchResult.getCount()).isEqualTo(2);
        Application hrApp = searchResult.getResult().get(0);
        assertIsHRApplication(profile, hrApp);
        assertIsMarketingApplication(searchResult.getResult().get(1));

        //check pages were created
        SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.APPLICATION_ID, hrApp.getId());
        SearchResult<ApplicationPage> pageSearchResult = applicationAPI.searchApplicationPages(builder.done());
        assertThat(pageSearchResult.getCount()).isEqualTo(1);
        assertIsMyNewCustomPage(myPage, hrApp, pageSearchResult.getResult().get(0));

        applicationAPI.deleteApplication(hrApp.getId());
        getProfileAPI().deleteProfile(profile.getId());
        getPageAPI().deletePage(myPage.getId());

    }

    private void assertIsMyNewCustomPage(final Page myPage, final Application hrApp, final ApplicationPage applicationPage) {
        assertThat(applicationPage.getApplicationId()).isEqualTo(hrApp.getId());
        assertThat(applicationPage.getToken()).isEqualTo("my-new-custom-page");
        assertThat(applicationPage.getPageId()).isEqualTo(myPage.getId());
    }

    private void assertIsMarketingApplication(final Application app) {
        assertThat(app.getToken()).isEqualTo("My");
        assertThat(app.getVersion()).isEqualTo("2.0");
        assertThat(app.getDisplayName()).isEqualTo("Marketing");
        assertThat(app.getDescription()).isNull();
        assertThat(app.getIconPath()).isNull();
        assertThat(app.getState()).isEqualTo("ACTIVATED");
        assertThat(app.getProfileId()).isNull();
    }

    private void assertIsHRApplication(final Profile profile, final Application app) {
        assertThat(app.getToken()).isEqualTo("HR-dashboard");
        assertThat(app.getVersion()).isEqualTo("2.0");
        assertThat(app.getDisplayName()).isEqualTo("My HR dashboard");
        assertThat(app.getDescription()).isEqualTo("This is the HR dashboard.");
        assertThat(app.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(app.getState()).isEqualTo("ACTIVATED");
        assertThat(app.getProfileId()).isEqualTo(profile.getId());
    }

    private void assertIsAddOkStatus(final ImportStatus importStatus, String expectedToken) {
        assertThat(importStatus.getName()).isEqualTo(expectedToken);
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.getErrors()).isEmpty();
    }

    @Cover(classes = { ApplicationAPI.class }, concept = Cover.BPMNConcept.APPLICATION, jira = "BS-9215", keywords = { "Application", "import",
            "profile does not exist", "custom page does not exists" })
    @Test
    public void importApplications_should_create_applications_contained_by_xml_file_and_return_error_in_status_when_profile_does_not_exist() throws Exception {
        //given
        final byte[] applicationsByteArray = IOUtils.toByteArray(ApplicationAPIApplicationIT.class
                .getResourceAsStream("applicationWithUnavailableInfo.xml"));

        //when
        final List<ImportStatus> importStatus = applicationAPI.importApplications(applicationsByteArray, ApplicationImportPolicy.FAIL_ON_DUPLICATES);

        //then
        assertThat(importStatus).hasSize(1);
        assertThat(importStatus.get(0).getName()).isEqualTo("HR-dashboard");
        assertThat(importStatus.get(0).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        ImportError profileError = new ImportError("ThisProfileNotExists", ImportError.Type.PROFILE);
        ImportError customPageError = new ImportError("custompage_mynewcustompage", ImportError.Type.PAGE);
        assertThat(importStatus.get(0).getErrors()).containsExactly(profileError, customPageError);

        // check applications ware created
        final SearchResult<Application> searchResult = applicationAPI.searchApplications(buildSearchOptions(0, 10));
        assertThat(searchResult.getCount()).isEqualTo(1);
        final Application app1 = searchResult.getResult().get(0);
        assertThat(app1.getToken()).isEqualTo("HR-dashboard");
        assertThat(app1.getVersion()).isEqualTo("2.0");
        assertThat(app1.getDisplayName()).isEqualTo("My HR dashboard");
        assertThat(app1.getDescription()).isEqualTo("This is the HR dashboard.");
        assertThat(app1.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(app1.getState()).isEqualTo("ACTIVATED");
        assertThat(app1.getProfileId()).isNull();

    }

}
