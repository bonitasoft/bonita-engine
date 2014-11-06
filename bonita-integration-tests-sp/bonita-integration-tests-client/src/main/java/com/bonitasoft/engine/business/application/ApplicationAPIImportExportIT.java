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

import com.bonitasoft.engine.BPMTestSPUtil;
import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.ApplicationAPI;
import com.bonitasoft.engine.api.TenantAPIAccessor;
import com.bonitasoft.engine.page.Page;
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.xml.XmlStringPrettyFormatter;
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

        applicationAPI.createApplication(engineeringCreator);
        final Application marketing = applicationAPI.createApplication(marketingCreator);

        //when
        final byte[] exportedBytes = applicationAPI.exportApplications(hr.getId(), marketing.getId());
        final String xmlPrettyFormatExported = XmlStringPrettyFormatter.xmlPrettyFormat(new String(exportedBytes));

        //then
        assertThatXmlHaveNoDifferences(xmlPrettyFormatExpected, xmlPrettyFormatExported);

        applicationAPI.deleteApplicationPage(appPage.getId());
        getPageAPI().deletePage(myPage.getId());
        applicationAPI.deleteApplication(hr.getId());
        getProfileAPI().deleteProfile(profile.getId());
    }

    @Cover(classes = { ApplicationAPI.class }, concept = Cover.BPMNConcept.APPLICATION, jira = "BS-9215", keywords = { "Application", "import" })
    @Test
    public void importApplications_should_create_all_applications_contained_by_xml_file_and_return_status_ok_() throws Exception {
        //given
        final Profile profile = getProfileAPI().createProfile("ApplicationProfile", "Profile for applications");

        final byte[] applicationsByteArray = IOUtils.toByteArray(ApplicationAPIApplicationIT.class
                .getResourceAsStream("applications.xml"));

        //when
        List<ImportStatus> importStatus = applicationAPI.importApplications(applicationsByteArray, ApplicationImportPolicy.FAIL_ON_DUPLICATES);

        //then
        assertThat(importStatus).hasSize(2);
        assertThat(importStatus.get(0).getName()).isEqualTo("HR-dashboard");
        assertThat(importStatus.get(0).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.get(0).getErrors()).isEmpty();

        assertThat(importStatus.get(1).getName()).isEqualTo("My");
        assertThat(importStatus.get(1).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatus.get(1).getErrors()).isEmpty();

        // check applications ware created
        final SearchResult<Application> searchResult = applicationAPI.searchApplications(buildSearchOptions(0, 10));
        assertThat(searchResult.getCount()).isEqualTo(2);
        final Application app1 = searchResult.getResult().get(0);
        assertThat(app1.getToken()).isEqualTo("HR-dashboard");
        assertThat(app1.getVersion()).isEqualTo("2.0");
        assertThat(app1.getDisplayName()).isEqualTo("My HR dashboard");
        assertThat(app1.getDescription()).isEqualTo("This is the HR dashboard.");
        assertThat(app1.getIconPath()).isEqualTo("/icon.jpg");
        assertThat(app1.getState()).isEqualTo("ACTIVATED");
        assertThat(app1.getProfileId()).isEqualTo(profile.getId());

        final Application app2 = searchResult.getResult().get(1);
        assertThat(app2.getToken()).isEqualTo("My");
        assertThat(app2.getVersion()).isEqualTo("2.0");
        assertThat(app2.getDisplayName()).isEqualTo("Marketing");
        assertThat(app2.getDescription()).isNull();
        assertThat(app2.getIconPath()).isNull();
        assertThat(app2.getState()).isEqualTo("ACTIVATED");
        assertThat(app2.getProfileId()).isNull();

        applicationAPI.deleteApplication(app1.getId());
        getProfileAPI().deleteProfile(profile.getId());

    }

}
