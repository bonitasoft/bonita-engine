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
import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.page.Page;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationAPIImportExportIT extends TestWithApplication {

    private SearchOptionsBuilder getDefaultBuilder(final int startIndex, final int maxResults) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(startIndex, maxResults);
        builder.sort(ApplicationSearchDescriptor.ID, Order.ASC);
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
        final Application hr = getApplicationAPI().createApplication(hrCreator);

        // Associate a new page to application hr (real page name is defined in zip/page.properties):
        final Page myPage = getPageAPI().createPage("not_used",
                IOUtils.toByteArray(ApplicationAPIApplicationIT.class.getResourceAsStream("dummy-bizapp-page.zip")));
        final ApplicationPage appPage = getApplicationAPI().createApplicationPage(hr.getId(), myPage.getId(), "my-new-custom-page");

        // Add menus:
        final ApplicationMenu menu = getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(hr.getId(), "HR follow-up"));
        final ApplicationMenuCreator subMenuCreator = new ApplicationMenuCreator(hr.getId(), "Daily HR follow-up", appPage.getId());
        subMenuCreator.setParentId(menu.getId());
        getApplicationAPI().createApplicationMenu(subMenuCreator);

        getApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(hr.getId(), "Empty menu"));

        // Add home page:
        getApplicationAPI().setApplicationHomePage(hr.getId(), appPage.getId());

        getApplicationAPI().createApplication(engineeringCreator);
        final Application marketing = getApplicationAPI().createApplication(marketingCreator);

        //when
        final byte[] exportedBytes = getApplicationAPI().exportApplications(hr.getId(), marketing.getId());
        final String xmlPrettyFormatExported = XmlStringPrettyFormatter.xmlPrettyFormat(new String(exportedBytes));

        //then
        assertThatXmlHaveNoDifferences(xmlPrettyFormatExpected, xmlPrettyFormatExported);

        getApplicationAPI().deleteApplication(hr.getId());
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
        final List<ImportStatus> importStatus = getApplicationAPI().importApplications(applicationsByteArray, ApplicationImportPolicy.FAIL_ON_DUPLICATES);

        //then
        assertThat(importStatus).hasSize(2);
        assertIsAddOkStatus(importStatus.get(0), "HR-dashboard");
        assertIsAddOkStatus(importStatus.get(1), "My");

        // check applications ware created
        final SearchResult<Application> searchResult = getApplicationAPI().searchApplications(buildSearchOptions(0, 10));
        assertThat(searchResult.getCount()).isEqualTo(2);
        Application hrApp = searchResult.getResult().get(0);
        assertIsHRApplication(profile, hrApp);
        assertIsMarketingApplication(searchResult.getResult().get(1));

        //check pages were created
        SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.APPLICATION_ID, hrApp.getId());
        SearchResult<ApplicationPage> pageSearchResult = getApplicationAPI().searchApplicationPages(builder.done());
        assertThat(pageSearchResult.getCount()).isEqualTo(1);
        ApplicationPage myNewCustomPage = pageSearchResult.getResult().get(0);
        assertIsMyNewCustomPage(myPage, hrApp, myNewCustomPage);

        //check home page
        assertThat(hrApp.getHomePageId()).isEqualTo(myNewCustomPage.getId());

        //check menu is created
        builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationMenuSearchDescriptor.APPLICATION_ID, hrApp.getId());
        SearchResult<ApplicationMenu> menuSearchResult = getApplicationAPI().searchApplicationMenus(builder.done());
        assertThat(menuSearchResult.getCount()).isEqualTo(3);
        ApplicationMenu hrFollowUpMenu = menuSearchResult.getResult().get(0);
        assertIsHrFollowUpMenu(hrFollowUpMenu);
        assertIsDailyHrFollowUpMenu(menuSearchResult.getResult().get(1), hrFollowUpMenu, myNewCustomPage);
        assertIsEmptyMenu(menuSearchResult.getResult().get(2));


        getApplicationAPI().deleteApplication(hrApp.getId());
        getProfileAPI().deleteProfile(profile.getId());
        getPageAPI().deletePage(myPage.getId());

    }

    private void assertIsHrFollowUpMenu(final ApplicationMenu applicationMenu) {
        assertThat(applicationMenu.getIndex()).isEqualTo(1);
        assertThat(applicationMenu.getParentId()).isNull();
        assertThat(applicationMenu.getDisplayName()).isEqualTo("HR follow-up");
        assertThat(applicationMenu.getApplicationPageId()).isNull();
    }

    private void assertIsDailyHrFollowUpMenu(final ApplicationMenu applicationMenu, ApplicationMenu hrFollowUpMenu, ApplicationPage myNewCustomPage) {
        assertThat(applicationMenu.getIndex()).isEqualTo(1);
        assertThat(applicationMenu.getParentId()).isEqualTo(hrFollowUpMenu.getId());
        assertThat(applicationMenu.getDisplayName()).isEqualTo("Daily HR follow-up");
        assertThat(applicationMenu.getApplicationPageId()).isEqualTo(myNewCustomPage.getId());
    }

    private void assertIsEmptyMenu(final ApplicationMenu applicationMenu) {
        assertThat(applicationMenu.getIndex()).isEqualTo(2);
        assertThat(applicationMenu.getParentId()).isNull();
        assertThat(applicationMenu.getDisplayName()).isEqualTo("Empty menu");
        assertThat(applicationMenu.getApplicationPageId()).isNull();
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
    public void importApplications_should_create_applications_contained_by_xml_file_and_return_error_in_there_is_unavailable_info() throws Exception {
        //given
        final byte[] applicationsByteArray = IOUtils.toByteArray(ApplicationAPIApplicationIT.class
                .getResourceAsStream("applicationWithUnavailableInfo.xml"));

        // create page necessary to import application hr (real page name is defined in zip/page.properties):
        final Page myPage = getPageAPI().createPage("not_used",
                IOUtils.toByteArray(ApplicationAPIApplicationIT.class.getResourceAsStream("dummy-bizapp-page.zip")));

        //when
        final List<ImportStatus> importStatus = getApplicationAPI().importApplications(applicationsByteArray, ApplicationImportPolicy.FAIL_ON_DUPLICATES);

        //then
        assertThat(importStatus).hasSize(1);
        assertThat(importStatus.get(0).getName()).isEqualTo("HR-dashboard");
        assertThat(importStatus.get(0).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        ImportError profileError = new ImportError("ThisProfileNotExists", ImportError.Type.PROFILE);
        ImportError customPageError = new ImportError("custompage_notexists", ImportError.Type.PAGE);
        ImportError appPageError1 = new ImportError("will-not-be-imported", ImportError.Type.APPLICATION_PAGE);
        ImportError appPageError2 = new ImportError("never-existed", ImportError.Type.APPLICATION_PAGE);
        assertThat(importStatus.get(0).getErrors()).containsExactly(profileError, customPageError, appPageError1, appPageError2);

        // check applications ware created
        final SearchResult<Application> searchResult = getApplicationAPI().searchApplications(buildSearchOptions(0, 10));
        assertThat(searchResult.getCount()).isEqualTo(1);
        final Application app1 = searchResult.getResult().get(0);
        assertThat(app1.getToken()).isEqualTo("HR-dashboard");
        assertThat(app1.getVersion()).isEqualTo("2.0");
        assertThat(app1.getDisplayName()).isEqualTo("My HR dashboard");
        assertThat(app1.getDescription()).isEqualTo("This is the HR dashboard.");
        assertThat(app1.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(app1.getState()).isEqualTo("ACTIVATED");
        assertThat(app1.getProfileId()).isNull();

        //check only one application page was created
        SearchOptionsBuilder builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.APPLICATION_ID, app1.getId());
        SearchResult<ApplicationPage> pageSearchResult = getApplicationAPI().searchApplicationPages(builder.done());
        assertThat(pageSearchResult.getCount()).isEqualTo(1);
        assertThat(pageSearchResult.getResult().get(0).getToken()).isEqualTo("my-new-custom-page");

        builder = getDefaultBuilder(0, 10);
        builder.filter(ApplicationMenuSearchDescriptor.APPLICATION_ID, app1.getId());

        //check three menus were created
        SearchResult<ApplicationMenu> menuSearchResult = getApplicationAPI().searchApplicationMenus(builder.done());
        assertThat(menuSearchResult.getCount()).isEqualTo(3);
        assertThat(menuSearchResult.getResult().get(0).getDisplayName()).isEqualTo("HR follow-up");
        assertThat(menuSearchResult.getResult().get(0).getIndex()).isEqualTo(1);
        assertThat(menuSearchResult.getResult().get(1).getDisplayName()).isEqualTo("Daily HR follow-up");
        assertThat(menuSearchResult.getResult().get(1).getIndex()).isEqualTo(1);
        assertThat(menuSearchResult.getResult().get(2).getDisplayName()).isEqualTo("Empty menu");
        assertThat(menuSearchResult.getResult().get(1).getIndex()).isEqualTo(1);

        getApplicationAPI().deleteApplication(app1.getId());
        getPageAPI().deletePage(myPage.getId());

    }

    @Cover(classes = { ApplicationAPI.class }, concept = Cover.BPMNConcept.APPLICATION, jira = "BS-9215", keywords = { "Application", "import" })
    @Test
    public void export_after_import_should_return_the_same_xml_file() throws Exception {
        //given
        final Profile profile = getProfileAPI().createProfile("ApplicationProfile", "Profile for applications");

        // create page necessary to import application hr (real page name is defined in zip/page.properties):
        final Page myPage = getPageAPI().createPage("not_used",
                IOUtils.toByteArray(ApplicationAPIApplicationIT.class.getResourceAsStream("dummy-bizapp-page.zip")));

        final byte[] importedByteArray = IOUtils.toByteArray(ApplicationAPIApplicationIT.class
                .getResourceAsStream("applications.xml"));

        getApplicationAPI().importApplications(importedByteArray, ApplicationImportPolicy.FAIL_ON_DUPLICATES);
        final SearchResult<Application> searchResult = getApplicationAPI().searchApplications(buildSearchOptions(0, 10));
        assertThat(searchResult.getCount()).isEqualTo(2);

        //when
        Application hrApplication = searchResult.getResult().get(0);
        byte[] exportedByteArray = getApplicationAPI().exportApplications(hrApplication.getId(), searchResult.getResult().get(1).getId());

        //then
        final String xmlPrettyFormatExpected = XmlStringPrettyFormatter.xmlPrettyFormat(new String(importedByteArray));
        final String xmlPrettyFormatActual = XmlStringPrettyFormatter.xmlPrettyFormat(new String(exportedByteArray));
        assertThatXmlHaveNoDifferences(xmlPrettyFormatExpected, xmlPrettyFormatActual);

        getApplicationAPI().deleteApplication(hrApplication.getId());
        getProfileAPI().deleteProfile(profile.getId());
        getPageAPI().deletePage(myPage.getId());

    }


}
