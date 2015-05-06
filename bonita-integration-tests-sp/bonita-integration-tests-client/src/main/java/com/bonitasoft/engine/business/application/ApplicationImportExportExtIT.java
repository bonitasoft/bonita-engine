/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.business.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.assertj.core.util.xml.XmlStringPrettyFormatter;
import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationCreator;
import org.bonitasoft.engine.business.application.ApplicationMenu;
import org.bonitasoft.engine.business.application.ApplicationMenuCreator;
import org.bonitasoft.engine.business.application.ApplicationMenuSearchDescriptor;
import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.ApplicationPageSearchDescriptor;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationImportExportExtIT extends org.bonitasoft.engine.business.application.TestWithApplication {

    @Cover(classes = { ApplicationAPI.class }, concept = Cover.BPMNConcept.APPLICATION, jira = "BS-13007", keywords = { "Application", "export", "layout", "theme" })
    @Test
    public void exportApplications_with_specific_layout_should_export_file_with_specific_layout() throws Exception {
        //given
        Profile userProfile = getProfileUser();

        Page layout = createPage("custompage_mainLayout");
        Page theme = createPage("custompage_mainTheme");

        final byte[] applicationsByteArray = IOUtils.toByteArray(this.getClass().getResourceAsStream("applications_with_layout.xml"));
        final String xmlPrettyFormatExpected = XmlStringPrettyFormatter.xmlPrettyFormat(new String(applicationsByteArray));

        //hr application
        final ApplicationCreatorExt hrCreator = new ApplicationCreatorExt("HR-dashboard", "My HR dashboard", "2.0", layout.getId(), theme.getId());
        hrCreator.setDescription("This is the HR dashboard.");
        hrCreator.setIconPath("/icon.jpg");
        hrCreator.setProfileId(userProfile.getId());

        //enginering application
        final ApplicationCreator engineeringCreator = new org.bonitasoft.engine.business.application.ApplicationCreator("Engineering-dashboard",
                "Engineering dashboard", "1.0");
        final ApplicationCreator marketingCreator = new ApplicationCreator("My", "Marketing", "2.0");
        final Application hr = getApplicationAPI().createApplication(hrCreator);

        final Page myPage = createPage("custompage_mynewcustompage");
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
        getPageAPI().deletePage(layout.getId());
        getPageAPI().deletePage(theme.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = Cover.BPMNConcept.APPLICATION, jira = "BS-13007", keywords = { "Application", "import", "layout","theme" })
    @Test
    public void importApplications_should_import_the_layout() throws Exception {
        //given
        final Profile profile = getProfileUser();

        // create page necessary to import application hr (real page name is defined in zip/page.properties):
        final Page myPage = createPage("custompage_mynewcustompage");
        final Page myLayout = createPage("custompage_mainLayout");
        final Page myTheme = createPage("custompage_mainTheme");

        final byte[] applicationsByteArray = IOUtils.toByteArray(this.getClass().getResourceAsStream("applications_with_layout.xml"));

        //when
        final List<ImportStatus> importStatus = getApplicationAPI().importApplications(applicationsByteArray,
                org.bonitasoft.engine.business.application.ApplicationImportPolicy.FAIL_ON_DUPLICATES);

        //then
        assertThat(importStatus).hasSize(2);
        assertIsAddOkStatus(importStatus.get(0), "HR-dashboard");
        assertIsAddOkStatus(importStatus.get(1), "My");

        // check applications ware created
        final SearchResult<Application> searchResult = getApplicationAPI().searchApplications(getAppSearchBuilderOrderById(0, 10).done());
        assertThat(searchResult.getCount()).isEqualTo(2);
        Application hrApp = searchResult.getResult().get(0);
        assertIsHRApplication(profile, myLayout, myTheme, hrApp);
        assertIsMarketingApplication(searchResult.getResult().get(1));

        //check pages were created
        SearchOptionsBuilder builder = getAppSearchBuilderOrderById(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.APPLICATION_ID, hrApp.getId());
        SearchResult<ApplicationPage> pageSearchResult = getApplicationAPI().searchApplicationPages(builder.done());
        assertThat(pageSearchResult.getCount()).isEqualTo(1);
        ApplicationPage myNewCustomPage = pageSearchResult.getResult().get(0);
        assertIsMyNewCustomPage(myPage, hrApp, myNewCustomPage);

        //check home page
        assertThat(hrApp.getHomePageId()).isEqualTo(myNewCustomPage.getId());

        //check menu is created
        builder = getAppSearchBuilderOrderById(0, 10);
        builder.filter(ApplicationMenuSearchDescriptor.APPLICATION_ID, hrApp.getId());
        SearchResult<ApplicationMenu> menuSearchResult = getApplicationAPI().searchApplicationMenus(builder.done());
        assertThat(menuSearchResult.getCount()).isEqualTo(3);
        ApplicationMenu hrFollowUpMenu = menuSearchResult.getResult().get(0);
        assertIsHrFollowUpMenu(hrFollowUpMenu);
        assertIsDailyHrFollowUpMenu(menuSearchResult.getResult().get(1), hrFollowUpMenu, myNewCustomPage);
        assertIsEmptyMenu(menuSearchResult.getResult().get(2));

        getApplicationAPI().deleteApplication(hrApp.getId());
        getPageAPI().deletePage(myPage.getId());
        getPageAPI().deletePage(myLayout.getId());
        getPageAPI().deletePage(myTheme.getId());

    }

    @Cover(classes = { ApplicationAPI.class }, concept = Cover.BPMNConcept.APPLICATION, jira = "BS-13007", keywords = { "Application", "import",
            "profile does not exist", "custom page does not exists", "layout does not exist" })
    @Test
    public void importApplications_should_create_applications_contained_by_xml_file_and_return_error_if_there_is_unavailable_info() throws Exception {
        //given
        final byte[] applicationsByteArray = IOUtils.toByteArray(this.getClass().getResourceAsStream("applications_with_missing_layout.xml"));
        final Page myPage = createPage("custompage_mynewcustompage");

        //when
        final List<ImportStatus> importStatus = getApplicationAPI().importApplications(applicationsByteArray, org.bonitasoft.engine.business.application.ApplicationImportPolicy.FAIL_ON_DUPLICATES);

        //then
        assertThat(importStatus).hasSize(1);
        assertThat(importStatus.get(0).getName()).isEqualTo("HR-dashboard");
        assertThat(importStatus.get(0).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        ImportError layoutError = new ImportError("ThisLayoutDoesNotExist", ImportError.Type.PAGE);
        ImportError themeError = new ImportError("ThisThemeDoesNotExist", ImportError.Type.PAGE);
        ImportError profileError = new ImportError("ThisProfileDoesNotExist", ImportError.Type.PROFILE);
        ImportError customPageError = new ImportError("custompage_notexists", ImportError.Type.PAGE);
        ImportError appPageError1 = new ImportError("will-not-be-imported", ImportError.Type.APPLICATION_PAGE);
        ImportError appPageError2 = new ImportError("never-existed", ImportError.Type.APPLICATION_PAGE);
        assertThat(importStatus.get(0).getErrors()).containsExactly(layoutError, themeError, profileError, customPageError, appPageError1, appPageError2);

        // check applications ware created
        final SearchResult<Application> searchResult = getApplicationAPI().searchApplications(getAppSearchBuilderOrderById(0, 10).done());
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
        SearchOptionsBuilder builder = getAppSearchBuilderOrderById(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.APPLICATION_ID, app1.getId());
        SearchResult<ApplicationPage> pageSearchResult = getApplicationAPI().searchApplicationPages(builder.done());
        assertThat(pageSearchResult.getCount()).isEqualTo(1);
        assertThat(pageSearchResult.getResult().get(0).getToken()).isEqualTo("my-new-custom-page");

        builder = getAppSearchBuilderOrderById(0, 10);
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

}
