/**
 * Copyright (C) 2024 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.application.importer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.api.impl.SessionInfos;
import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationState;
import org.bonitasoft.engine.page.PageService;
import org.bonitasoft.engine.page.SPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ApplicationImporterIT extends CommonBPMServicesTest {

    private ApplicationImporter applicationImporter;
    private ApplicationService applicationService;
    private PageService pageService;

    private static final String APP_1_TOKEN = "app1";
    private static final String APP_2_TOKEN = "app2";
    private static final String APP_3_TOKEN = "app3";
    private static final String APP_4_TOKEN = "app4";

    @Before
    public void setUp() {
        applicationImporter = getServiceAccessor().getApplicationImporter();
        applicationService = getServiceAccessor().getApplicationService();
        pageService = getServiceAccessor().getPageService();
    }

    @After
    public void tearDown() throws Exception {
        deleteApplication(APP_1_TOKEN);
        deleteApplication(APP_2_TOKEN);
        deleteApplication(APP_3_TOKEN);
        deleteApplication(APP_4_TOKEN);
        getTransactionService().executeInTransaction(() -> {
            SPage page = pageService.getPageByName("custompage_mynewcustompage");
            if (page != null) {
                pageService.deletePage(page.getId());
            }
            return page;
        });
    }

    private void deleteApplication(String appToken) throws Exception {
        getTransactionService().executeInTransaction(() -> {
            SApplication app = applicationService.getApplicationByToken(appToken);
            if (app != null) {
                applicationService.forceDeleteApplication(app);
            }
            return app;
        });
    }

    @Test
    public void one_advanced_application_should_be_imported_successfully() throws Exception {
        //given
        String xmlToImport = "/applications-importer/oneAdvancedApplication.xml";

        // ensure applications did not exist initially:
        assertAppNotExists(APP_1_TOKEN);

        //when
        List<ImportStatus> importStatuses = importApplicationsFromXml(xmlToImport);

        //then
        assertThat(importStatuses).hasSize(1);
        assertThat(importStatuses.get(0).getName()).isEqualTo(APP_1_TOKEN);
        assertThat(importStatuses.get(0).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatuses.get(0).getErrors()).isEmpty();

        assertAdvancedApp1();
    }

    @Test
    public void multiple_advanced_applications_should_be_imported_successfully() throws Exception {
        //given
        String xmlToImport = "/applications-importer/multipleAdvancedApplications.xml";

        // ensure applications did not exist initially:
        assertAppNotExists(APP_1_TOKEN);
        assertAppNotExists(APP_2_TOKEN);

        //when
        List<ImportStatus> importStatuses = importApplicationsFromXml(xmlToImport);

        //then
        assertThat(importStatuses).hasSize(2);
        assertThat(importStatuses.get(0).getName()).isEqualTo(APP_1_TOKEN);
        assertThat(importStatuses.get(0).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatuses.get(0).getErrors()).isEmpty();
        assertThat(importStatuses.get(1).getName()).isEqualTo(APP_2_TOKEN);
        assertThat(importStatuses.get(1).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatuses.get(1).getErrors()).isEmpty();

        assertAdvancedApp1();
        assertAdvancedApp2();
    }

    @Test
    public void mixed_legacy_and_advanced_applications_should_be_imported_successfully() throws Exception {
        //given
        String xmlToImport = "/applications-importer/mixedLegacyAndAdvancedApplications.xml";

        // ensure applications did not exist initially:
        assertAppNotExists(APP_1_TOKEN);
        assertAppNotExists(APP_2_TOKEN);
        assertAppNotExists(APP_3_TOKEN);
        assertAppNotExists(APP_4_TOKEN);

        // create page mandatory for app3
        createDummyPage();

        //when
        List<ImportStatus> importStatuses = importApplicationsFromXml(xmlToImport);

        //then
        assertThat(importStatuses).hasSize(4);
        assertThat(importStatuses.get(0).getName()).isEqualTo(APP_1_TOKEN);
        assertThat(importStatuses.get(0).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatuses.get(0).getErrors()).isEmpty();
        assertThat(importStatuses.get(1).getName()).isEqualTo(APP_2_TOKEN);
        assertThat(importStatuses.get(1).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatuses.get(1).getErrors()).isEmpty();
        assertThat(importStatuses.get(2).getName()).isEqualTo(APP_3_TOKEN);
        assertThat(importStatuses.get(2).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatuses.get(2).getErrors()).isEmpty();
        assertThat(importStatuses.get(3).getName()).isEqualTo(APP_4_TOKEN);
        assertThat(importStatuses.get(3).getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(importStatuses.get(3).getErrors()).isEmpty();

        assertAdvancedApp1();
        assertAdvancedApp2();
        assertAdvancedApp3();
        assertAdvancedApp4();
    }

    @Test
    public void mixed_legacy_and_advanced_applications_should_be_imported_successfully_twice() throws Exception {
        //given
        String xmlToImport = "/applications-importer/mixedLegacyAndAdvancedApplications.xml";
        ApplicationImportStrategy updateStrategy = (a1, a2) -> ApplicationImportStrategy.ImportStrategy.REPLACE;

        // create page mandatory for app3
        createDummyPage();

        //when
        List<ImportStatus> importStatuses = importApplicationsFromXml(xmlToImport);
        assertThat(importStatuses).hasSize(4);
        // and re-import
        try (var xmlAsStream = this.getClass().getResourceAsStream(xmlToImport)) {
            assertThat(xmlAsStream).isNotNull();
            importStatuses = getTransactionService()
                    .executeInTransaction(() -> applicationImporter.importApplications(xmlAsStream.readAllBytes(), null,
                            null, SessionInfos.getUserIdFromSession(), updateStrategy));
        }

        //then
        assertThat(importStatuses).hasSize(4);
        assertThat(importStatuses.get(0).getName()).isEqualTo(APP_1_TOKEN);
        assertThat(importStatuses.get(0).getStatus()).isEqualTo(ImportStatus.Status.REPLACED);
        assertThat(importStatuses.get(0).getErrors()).isEmpty();
        assertThat(importStatuses.get(1).getName()).isEqualTo(APP_2_TOKEN);
        assertThat(importStatuses.get(1).getStatus()).isEqualTo(ImportStatus.Status.REPLACED);
        assertThat(importStatuses.get(1).getErrors()).isEmpty();
        assertThat(importStatuses.get(2).getName()).isEqualTo(APP_3_TOKEN);
        assertThat(importStatuses.get(2).getStatus()).isEqualTo(ImportStatus.Status.REPLACED);
        assertThat(importStatuses.get(2).getErrors()).isEmpty();
        assertThat(importStatuses.get(3).getName()).isEqualTo(APP_4_TOKEN);
        assertThat(importStatuses.get(3).getStatus()).isEqualTo(ImportStatus.Status.REPLACED);
        assertThat(importStatuses.get(3).getErrors()).isEmpty();

        assertAdvancedApp1();
        assertAdvancedApp2();
        assertAdvancedApp3();
        assertAdvancedApp4();

    }

    private List<ImportStatus> importApplicationsFromXml(String xmlToImport) throws Exception {
        try (var xmlAsStream = this.getClass().getResourceAsStream(xmlToImport)) {
            assertThat(xmlAsStream).isNotNull();
            return getTransactionService()
                    .executeInTransaction(() -> applicationImporter.importApplications(xmlAsStream.readAllBytes(), null,
                            null, SessionInfos.getUserIdFromSession(), null));
        }
    }

    private void assertAppNotExists(String appToken) throws Exception {
        SApplication existingApp1 = getTransactionService()
                .executeInTransaction(() -> applicationService.getApplicationByToken(appToken));
        assertThat(existingApp1).isNull();
    }

    private void assertAdvancedApp1() throws Exception {
        SApplication app = getTransactionService()
                .executeInTransaction(() -> applicationService.getApplicationByToken(APP_1_TOKEN));
        assertThat(app).isNotNull();
        assertThat(app.getDisplayName()).isEqualTo("Application 1");
        assertThat(app.getDescription()).isEqualTo("Description of Application 1");
        assertThat(app.getVersion()).isEqualTo("1.0");
        assertThat(app.getIconPath()).isEqualTo("/app1.jpg");
        assertThat(app.getCreatedBy()).isEqualTo(-1L);
        assertThat(app.getState()).isEqualTo(SApplicationState.ACTIVATED.name());
        assertThat(app.getProfileId()).isEqualTo(1L);
        assertThat(app.getInternalProfile()).isNull();
        assertThat(app.getHomePageId()).isNull();
        assertThat(app.getLayoutId()).isNull();
        assertThat(app.getThemeId()).isNull();
        assertThat(app.isEditable()).isTrue();
        assertThat(app.isAdvanced()).isTrue();
    }

    private void assertAdvancedApp2() throws Exception {
        SApplication app = getTransactionService()
                .executeInTransaction(() -> applicationService.getApplicationByToken(APP_2_TOKEN));
        assertThat(app).isNotNull();
        assertThat(app.getDisplayName()).isEqualTo("Application 2");
        assertThat(app.getDescription()).isNull();
        assertThat(app.getVersion()).isEqualTo("1.1");
        assertThat(app.getIconPath()).isNull();
        assertThat(app.getCreatedBy()).isEqualTo(-1L);
        assertThat(app.getState()).isEqualTo(SApplicationState.DEACTIVATED.name());
        assertThat(app.getProfileId()).isNull();
        assertThat(app.getInternalProfile()).isNull();
        assertThat(app.getHomePageId()).isNull();
        assertThat(app.getLayoutId()).isNull();
        assertThat(app.getThemeId()).isNull();
        assertThat(app.isEditable()).isTrue();
        assertThat(app.isAdvanced()).isTrue();
    }

    private void assertAdvancedApp3() throws Exception {
        SApplication app = getTransactionService()
                .executeInTransaction(() -> applicationService.getApplicationByToken(APP_3_TOKEN));
        assertThat(app).isNotNull();
        assertThat(app.getDisplayName()).isEqualTo("Application 3");
        assertThat(app.getDescription()).isEqualTo("Description of Application 3");
        assertThat(app.getVersion()).isEqualTo("2.0");
        assertThat(app.getIconPath()).isEqualTo("/app3.jpg");
        assertThat(app.getCreatedBy()).isEqualTo(-1L);
        assertThat(app.getState()).isEqualTo(SApplicationState.ACTIVATED.name());
        assertThat(app.getHomePageId()).isNotNull();
        assertThat(app.getProfileId()).isEqualTo(1L);
        assertThat(app.getInternalProfile()).isNull();
        assertThat(app.getLayoutId()).isNotNull();
        assertThat(app.getThemeId()).isNotNull();
        assertThat(app.isEditable()).isTrue();
        assertThat(app.isAdvanced()).isFalse();
    }

    private void assertAdvancedApp4() throws Exception {
        SApplication app = getTransactionService()
                .executeInTransaction(() -> applicationService.getApplicationByToken(APP_4_TOKEN));
        assertThat(app).isNotNull();
        assertThat(app.getDisplayName()).isEqualTo("Application 4");
        assertThat(app.getDescription()).isNull();
        assertThat(app.getVersion()).isEqualTo("2.0");
        assertThat(app.getIconPath()).isNull();
        assertThat(app.getCreatedBy()).isEqualTo(-1L);
        assertThat(app.getState()).isEqualTo(SApplicationState.DEACTIVATED.name());
        assertThat(app.getProfileId()).isNull();
        assertThat(app.getInternalProfile()).isNull();
        assertThat(app.getHomePageId()).isNull();
        assertThat(app.getLayoutId()).isNotNull();
        assertThat(app.getThemeId()).isNotNull();
        assertThat(app.isEditable()).isTrue();
        assertThat(app.isAdvanced()).isFalse();
    }

    private void createDummyPage() throws Exception {
        try (var contentStream = this.getClass().getResourceAsStream("/applications-importer/dummy-bizapp-page.zip")) {
            assertThat(contentStream).isNotNull();
            getTransactionService().executeInTransaction(() -> pageService.addPage(contentStream.readAllBytes(),
                    "custompage_mynewcustompage", SessionInfos.getUserIdFromSession()));
        }
    }
}
