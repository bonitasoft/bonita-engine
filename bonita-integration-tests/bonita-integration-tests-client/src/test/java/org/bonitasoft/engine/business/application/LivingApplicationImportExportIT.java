/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.List;

import org.apache.commons.io.IOUtils;
import org.assertj.core.util.xml.XmlStringPrettyFormatter;
import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.profile.Profile;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class LivingApplicationImportExportIT extends TestWithLivingApplication {

    private SearchOptions buildSearchOptions(final int startIndex, final int maxResults) {
        return getAppSearchBuilderOrderById(startIndex, maxResults).done();
    }

    @Test
    public void exportApplications_should_return_the_byte_content_of_xml_file_containing_selected_applications()
            throws Exception {
        //given
        Profile userProfile = getProfileUser();

        final byte[] applicationsByteArray = IOUtils
                .toByteArray(LivingApplicationIT.class.getResourceAsStream("applications.xml"));
        final String xmlPrettyFormatExpected = XmlStringPrettyFormatter
                .xmlPrettyFormat(new String(applicationsByteArray));

        final ApplicationCreator hrCreator = new ApplicationCreator("HR-dashboard", "My HR dashboard", "2.0");
        hrCreator.setDescription("This is the HR dashboard.");
        hrCreator.setIconPath("/icon.jpg");
        hrCreator.setProfileId(userProfile.getId());

        final ApplicationCreator engineeringCreator = new ApplicationCreator("Engineering-dashboard",
                "Engineering dashboard", "1.0");
        final ApplicationCreator marketingCreator = new ApplicationCreator("My", "Marketing", "2.0");
        final Application hr = getLivingApplicationAPI().createApplication(hrCreator);

        // Associate a new page to application hr (real page name is defined in zip/page.properties):
        final Page myPage = getPageAPI().createPage("not_used",
                IOUtils.toByteArray(LivingApplicationIT.class.getResourceAsStream("dummy-bizapp-page.zip")));
        final ApplicationPage appPage = getLivingApplicationAPI().createApplicationPage(hr.getId(), myPage.getId(),
                "my-new-custom-page");

        // Add menus:
        final ApplicationMenu menu = getLivingApplicationAPI()
                .createApplicationMenu(new ApplicationMenuCreator(hr.getId(), "HR follow-up"));
        final ApplicationMenuCreator subMenuCreator = new ApplicationMenuCreator(hr.getId(), "Daily HR follow-up",
                appPage.getId());
        subMenuCreator.setParentId(menu.getId());
        getLivingApplicationAPI().createApplicationMenu(subMenuCreator);

        getLivingApplicationAPI().createApplicationMenu(new ApplicationMenuCreator(hr.getId(), "Empty menu"));

        // Add home page:
        getLivingApplicationAPI().setApplicationHomePage(hr.getId(), appPage.getId());

        getLivingApplicationAPI().createApplication(engineeringCreator);
        final Application marketing = getLivingApplicationAPI().createApplication(marketingCreator);

        //when
        final byte[] exportedBytes = getLivingApplicationAPI().exportApplications(hr.getId(), marketing.getId());
        final String xmlPrettyFormatExported = XmlStringPrettyFormatter.xmlPrettyFormat(new String(exportedBytes));

        //then
        assertThatXmlHaveNoDifferences(xmlPrettyFormatExpected, xmlPrettyFormatExported);

        getLivingApplicationAPI().deleteApplication(hr.getId());
        getPageAPI().deletePage(myPage.getId());
    }

    @Test
    public void importApplications_should_create_all_applications_contained_by_xml_file_and_return_status_ok_()
            throws Exception {
        //given
        final Profile profile = getProfileUser();

        // create page necessary to import application hr (real page name is defined in zip/page.properties):
        final Page myPage = getPageAPI().createPage("not_used",
                IOUtils.toByteArray(LivingApplicationIT.class.getResourceAsStream("dummy-bizapp-page.zip")));
        final Page defaultLayout = getPageAPI().getPageByName(DEFAULT_LAYOUT_NAME);
        final Page defaultTheme = getPageAPI().getPageByName(DEFAULT_THEME_NAME);

        final byte[] applicationsByteArray = IOUtils
                .toByteArray(LivingApplicationIT.class.getResourceAsStream("applications.xml"));

        //when
        final List<ImportStatus> importStatus = getLivingApplicationAPI().importApplications(applicationsByteArray,
                ApplicationImportPolicy.FAIL_ON_DUPLICATES);

        //then
        assertThat(importStatus).hasSize(2);
        assertIsAddOkStatus(importStatus.get(0), "HR-dashboard");
        assertIsAddOkStatus(importStatus.get(1), "My");

        // check applications ware created
        final SearchResult<Application> searchResult = getLivingApplicationAPI()
                .searchApplications(buildSearchOptions(0, 10));
        assertThat(searchResult.getCount()).isEqualTo(2);
        Application hrApp = searchResult.getResult().get(0);
        assertIsHRApplication(profile, defaultLayout, defaultTheme, hrApp);
        assertIsMarketingApplication(searchResult.getResult().get(1));

        //check pages were created
        SearchOptionsBuilder builder = getAppSearchBuilderOrderById(0, 10);
        builder.filter(ApplicationPageSearchDescriptor.APPLICATION_ID, hrApp.getId());
        SearchResult<ApplicationPage> pageSearchResult = getLivingApplicationAPI()
                .searchApplicationPages(builder.done());
        assertThat(pageSearchResult.getCount()).isEqualTo(1);
        ApplicationPage myNewCustomPage = pageSearchResult.getResult().get(0);
        assertIsMyNewCustomPage(myPage, hrApp, myNewCustomPage);

        //check home page
        assertThat(hrApp.getHomePageId()).isEqualTo(myNewCustomPage.getId());

        //check menu is created
        builder = getAppSearchBuilderOrderById(0, 10);
        builder.filter(ApplicationMenuSearchDescriptor.APPLICATION_ID, hrApp.getId());
        SearchResult<ApplicationMenu> menuSearchResult = getLivingApplicationAPI()
                .searchApplicationMenus(builder.done());
        assertThat(menuSearchResult.getCount()).isEqualTo(3);
        ApplicationMenu hrFollowUpMenu = menuSearchResult.getResult().get(0);
        assertIsHrFollowUpMenu(hrFollowUpMenu);
        assertIsDailyHrFollowUpMenu(menuSearchResult.getResult().get(1), hrFollowUpMenu, myNewCustomPage);
        assertIsEmptyMenu(menuSearchResult.getResult().get(2));

        getLivingApplicationAPI().deleteApplication(hrApp.getId());
        getPageAPI().deletePage(myPage.getId());

    }

    @Test
    public void importApplications_should_create_applications_contained_by_xml_file_and_return_error_if_there_is_unavailable_info()
            throws Exception {
        //given
        final byte[] applicationsByteArray = IOUtils.toByteArray(LivingApplicationIT.class
                .getResourceAsStream("applicationWithUnavailableInfo.xml"));

        // create page necessary to import application hr (real page name is defined in zip/page.properties):
        final Page myPage = getPageAPI().createPage("not_used",
                IOUtils.toByteArray(LivingApplicationIT.class.getResourceAsStream("dummy-bizapp-page.zip")));

        //when
        final List<ImportStatus> importStatus = getLivingApplicationAPI().importApplications(applicationsByteArray,
                ApplicationImportPolicy.FAIL_ON_DUPLICATES);

        //then
        assertThat(importStatus).hasSize(1);
        assertThat(importStatus.get(0).getName()).isEqualTo("HR-dashboard");
        assertThat(importStatus.get(0).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        ImportError profileError = new ImportError("ThisProfileDoesNotExist", ImportError.Type.PROFILE);
        ImportError customPageError = new ImportError("custompage_notexists", ImportError.Type.PAGE);
        ImportError appPageError1 = new ImportError("will-not-be-imported", ImportError.Type.APPLICATION_PAGE);
        ImportError appPageError2 = new ImportError("never-existed", ImportError.Type.APPLICATION_PAGE);
        assertThat(importStatus.get(0).getErrors()).containsExactly(profileError, customPageError, appPageError1,
                appPageError2);

        // check applications ware created
        final SearchResult<Application> searchResult = getLivingApplicationAPI()
                .searchApplications(buildSearchOptions(0, 10));
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
        SearchResult<ApplicationPage> pageSearchResult = getLivingApplicationAPI()
                .searchApplicationPages(builder.done());
        assertThat(pageSearchResult.getCount()).isEqualTo(1);
        assertThat(pageSearchResult.getResult().get(0).getToken()).isEqualTo("my-new-custom-page");

        builder = getAppSearchBuilderOrderById(0, 10);
        builder.filter(ApplicationMenuSearchDescriptor.APPLICATION_ID, app1.getId());

        //check three menus were created
        SearchResult<ApplicationMenu> menuSearchResult = getLivingApplicationAPI()
                .searchApplicationMenus(builder.done());
        assertThat(menuSearchResult.getCount()).isEqualTo(3);
        assertThat(menuSearchResult.getResult().get(0).getDisplayName()).isEqualTo("HR follow-up");
        assertThat(menuSearchResult.getResult().get(0).getIndex()).isEqualTo(1);
        assertThat(menuSearchResult.getResult().get(1).getDisplayName()).isEqualTo("Daily HR follow-up");
        assertThat(menuSearchResult.getResult().get(1).getIndex()).isEqualTo(1);
        assertThat(menuSearchResult.getResult().get(2).getDisplayName()).isEqualTo("Empty menu");
        assertThat(menuSearchResult.getResult().get(1).getIndex()).isEqualTo(1);

        getLivingApplicationAPI().deleteApplication(app1.getId());
        getPageAPI().deletePage(myPage.getId());

    }

    @Test
    public void export_after_import_should_return_the_same_xml_file() throws Exception {
        //given
        // create page necessary to import application hr (real page name is defined in zip/page.properties):
        final Page myPage = getPageAPI().createPage("not_used",
                IOUtils.toByteArray(LivingApplicationIT.class.getResourceAsStream("dummy-bizapp-page.zip")));

        final byte[] importedByteArray = IOUtils.toByteArray(LivingApplicationIT.class
                .getResourceAsStream("applications.xml"));

        getLivingApplicationAPI().importApplications(importedByteArray, ApplicationImportPolicy.FAIL_ON_DUPLICATES);
        final SearchResult<Application> searchResult = getLivingApplicationAPI()
                .searchApplications(buildSearchOptions(0, 10));
        assertThat(searchResult.getCount()).isEqualTo(2);

        //when
        Application hrApplication = searchResult.getResult().get(0);
        byte[] exportedByteArray = getLivingApplicationAPI().exportApplications(hrApplication.getId(),
                searchResult.getResult().get(1).getId());

        //then
        final String xmlPrettyFormatExpected = XmlStringPrettyFormatter.xmlPrettyFormat(new String(importedByteArray));
        final String xmlPrettyFormatActual = XmlStringPrettyFormatter.xmlPrettyFormat(new String(exportedByteArray));
        assertThatXmlHaveNoDifferences(xmlPrettyFormatExpected, xmlPrettyFormatActual);

        getLivingApplicationAPI().deleteApplication(hrApplication.getId());
        getPageAPI().deletePage(myPage.getId());

    }

}
