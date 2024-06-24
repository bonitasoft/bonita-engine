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
package org.bonitasoft.engine.business.application.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.converter.NodeToApplicationConverter;
import org.bonitasoft.engine.business.application.model.AbstractSApplication;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.business.application.xml.ApplicationLinkNode;
import org.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeBuilder;
import org.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.session.SessionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationImporterTest {

    private static final String UNUSED_PAGE = "";

    private static final String ICON_MIME_TYPE = "iconMimeType";

    private static final byte[] ICON_CONTENT = "iconContent".getBytes(StandardCharsets.UTF_8);

    @Mock
    private ApplicationService applicationService;
    @Mock
    private ApplicationImportStrategy strategy;
    @Mock
    private NodeToApplicationConverter nodeToApplicationConverter;
    @Mock
    private ApplicationPageImporter applicationPageImporter;
    @Mock
    private ApplicationMenuImporter applicationMenuImporter;
    @Mock
    private ImportResult importResult;

    private ApplicationImporter applicationImporter;

    @Before
    public void before() {
        applicationImporter = spy(new ApplicationImporter(applicationService, nodeToApplicationConverter,
                applicationPageImporter, applicationMenuImporter));
    }

    @Test
    public void importApplication_should_create_application_import_pages_and_menus_and_return_status_when_no_application_existing_and_add_if_missing_is_true()
            throws Exception {
        //given
        long createdBy = 5L;
        boolean editable = false;
        SApplicationWithIcon app = new SApplicationWithIcon("app", "app", "1.0", 1L, createdBy, "state", editable);
        app.setId(1);
        given(importResult.getApplication()).willReturn(app);
        given(importResult.getImportStatus()).willReturn(new ImportStatus(app.getToken()));

        ApplicationPageNode homePage = ApplicationNodeBuilder.newApplicationPage("home", "home").create();
        ApplicationPageNode pageNode2 = ApplicationNodeBuilder.newApplicationPage("page", "page").create();

        ApplicationMenuNode menu1 = ApplicationNodeBuilder.newMenu("menu1", UNUSED_PAGE).create();
        ApplicationMenuNode menu2 = ApplicationNodeBuilder.newMenu("menu2", UNUSED_PAGE).create();

        ApplicationNode applicationNode = new ApplicationNode();
        applicationNode.addApplicationPage(homePage);
        applicationNode.addApplicationPage(pageNode2);
        applicationNode.addApplicationMenu(menu1);
        applicationNode.addApplicationMenu(menu2);
        applicationNode.setHomePage(homePage.getToken());
        applicationNode.setToken("app");

        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy,
                editable))
                        .willReturn(importResult);

        long homePageId = 222L;
        SApplicationPage applicationPage = new SApplicationPage(app.getId(), homePageId, "home");
        applicationPage.setId(homePageId);

        given(applicationService.getApplicationPage("app", "home")).willReturn(applicationPage);

        ImportError errorMenu = new ImportError("errorMenu", ImportError.Type.APPLICATION_PAGE);
        ImportError errorPage = new ImportError("errorPage", ImportError.Type.APPLICATION_PAGE);

        given(applicationPageImporter.importApplicationPages(any(), eq(app)))
                .willReturn(List.of(errorPage));

        given(applicationMenuImporter.importApplicationMenus(any(), eq(app)))
                .willReturn(Arrays.asList(errorMenu, errorMenu));

        given(applicationService.createApplication(app)).willReturn(app);

        //when
        ImportStatus retrievedStatus = applicationImporter.importApplication(applicationNode, editable, createdBy,
                ICON_CONTENT, ICON_MIME_TYPE, true, strategy);

        //then
        //create application
        assertThat(retrievedStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        verify(applicationService).createApplication(app);
        assertThat(app.isEditable()).isFalse();
        verifyNoInteractions(strategy);

        //add pages
        verify(applicationPageImporter).importApplicationPages(any(), eq(app));
        assertThat(retrievedStatus.getErrors()).containsExactlyInAnyOrder(errorPage, errorMenu);
        //add menus
        verify(applicationMenuImporter).importApplicationMenus(any(), eq(app));

        //set home page
        verify(applicationService).updateApplication(eq(app),
                argThat(desc -> desc.getFields().get(AbstractSApplication.HOME_PAGE_ID).equals(homePageId)));
    }

    @Test
    public void importApplication_should_not_create_application_when_no_application_existing_and_add_if_missing_is_false()
            throws Exception {
        //given
        long createdBy = 5L;
        boolean editable = false;
        SApplicationWithIcon app = new SApplicationWithIcon("app", "app", "1.0", 1L, createdBy, "state", editable);
        app.setId(1);
        given(importResult.getApplication()).willReturn(app);
        given(importResult.getImportStatus()).willReturn(new ImportStatus(app.getToken()));

        ApplicationPageNode homePage = ApplicationNodeBuilder.newApplicationPage("home", "home").create();
        ApplicationPageNode pageNode2 = ApplicationNodeBuilder.newApplicationPage("page", "page").create();

        ApplicationMenuNode menu1 = ApplicationNodeBuilder.newMenu("menu1", UNUSED_PAGE).create();
        ApplicationMenuNode menu2 = ApplicationNodeBuilder.newMenu("menu2", UNUSED_PAGE).create();

        ApplicationNode applicationNode = new ApplicationNode();
        applicationNode.addApplicationPage(homePage);
        applicationNode.addApplicationPage(pageNode2);
        applicationNode.addApplicationMenu(menu1);
        applicationNode.addApplicationMenu(menu2);
        applicationNode.setHomePage(homePage.getToken());
        applicationNode.setToken("app");

        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy,
                editable))
                        .willReturn(importResult);

        long homePageId = 222L;

        //when
        ImportStatus retrievedStatus = applicationImporter.importApplication(applicationNode, editable, createdBy,
                ICON_CONTENT, ICON_MIME_TYPE, false, strategy);

        //then
        //create application
        assertThat(retrievedStatus.getStatus()).isEqualTo(ImportStatus.Status.SKIPPED);
        verify(applicationService, never()).createApplication(app);
        assertThat(app.isEditable()).isFalse();
        verifyNoInteractions(strategy);

        //add pages
        verify(applicationPageImporter, never()).importApplicationPages(any(), eq(app));
        //add menus
        verify(applicationMenuImporter, never()).importApplicationMenus(any(), eq(app));

        //set home page
        verify(applicationService, never()).updateApplication(eq(app),
                argThat(desc -> desc.getFields().get(AbstractSApplication.HOME_PAGE_ID).equals(homePageId)));
    }

    @Test
    public void importApplication_should_not_add_error_when_error_already_exists() throws Exception {
        //given
        long createdBy = 5L;
        boolean editable = true;
        SApplicationWithIcon app = mock(SApplicationWithIcon.class);

        ImportResult importResult = mock(ImportResult.class);
        given(importResult.getApplication()).willReturn(app);
        ImportStatus importStatus = new ImportStatus("app");
        ImportError errorPage = new ImportError("home", ImportError.Type.PAGE);
        importStatus.addError(errorPage);

        given(importResult.getImportStatus()).willReturn(importStatus);

        ApplicationPageNode pageNode1 = mock(ApplicationPageNode.class);

        ApplicationNode applicationNode = new ApplicationNode();
        applicationNode.addApplicationPage(pageNode1);
        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy,
                editable))
                        .willReturn(importResult);

        given(applicationService.createApplication(app)).willReturn(app);

        //when
        ImportStatus retrievedStatus = applicationImporter.importApplication(applicationNode, editable, createdBy,
                ICON_CONTENT, ICON_MIME_TYPE, true, strategy);

        //then
        assertThat(retrievedStatus).isEqualTo(importResult.getImportStatus());
        assertThat(retrievedStatus.getErrors()).containsExactly(errorPage);
    }

    @Test
    public void importApplication_should_not_set_home_page_when_applicationNode_does_not_have_home_page()
            throws Exception {
        //given
        long createdBy = 5L;
        boolean editable = true;
        SApplicationWithIcon app = mock(SApplicationWithIcon.class);

        ImportResult importResult = mock(ImportResult.class);
        given(importResult.getApplication()).willReturn(app);
        ImportStatus importStatus = mock(ImportStatus.class);
        given(importResult.getImportStatus()).willReturn(importStatus);

        ApplicationNode applicationNode = new ApplicationNode();
        applicationNode.setToken("app");
        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy,
                editable))
                        .willReturn(importResult);
        given(applicationService.createApplication(app)).willReturn(app);

        //when
        applicationImporter.importApplication(applicationNode, editable, createdBy, ICON_CONTENT,
                ICON_MIME_TYPE, true, strategy);

        //then
        //set home page
        verify(applicationService, never()).updateApplication(any(SApplicationWithIcon.class),
                any(EntityUpdateDescriptor.class));
    }

    @Test
    public void importApplication_should_add_error_when_home_page_is_not_found() throws Exception {
        //given
        long createdBy = 5L;
        boolean editable = true;
        SApplicationWithIcon app = mock(SApplicationWithIcon.class);

        ImportResult importResult = mock(ImportResult.class);
        given(importResult.getApplication()).willReturn(app);
        ImportStatus importStatus = mock(ImportStatus.class);
        given(importResult.getImportStatus()).willReturn(importStatus);

        ApplicationNode applicationNode = new ApplicationNode();
        applicationNode.setToken("app");
        applicationNode.setHomePage("home");

        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy,
                editable))
                        .willReturn(importResult);
        given(applicationService.createApplication(app)).willReturn(app);

        given(applicationService.getApplicationPage("app", "home")).willThrow(new SObjectNotFoundException(""));

        //when
        applicationImporter.importApplication(applicationNode, editable, createdBy, ICON_CONTENT,
                ICON_MIME_TYPE, true, strategy);

        //then
        //set home page
        verify(applicationService, never()).updateApplication(any(SApplicationWithIcon.class),
                any(EntityUpdateDescriptor.class));
        verify(importStatus)
                .addErrorsIfNotExists(List.of(new ImportError("home", ImportError.Type.APPLICATION_PAGE)));
    }

    @Test
    public void importApplication_should_skip_when_strategy_return_skip() throws Exception {
        //given
        long createdBy = 5L;
        boolean editable = true;
        SApplicationWithIcon appToBeImported = mock(SApplicationWithIcon.class);
        given(appToBeImported.getToken()).willReturn("application");

        ImportResult importResult = new ImportResult(appToBeImported, new ImportStatus("name"));

        SApplication appInConflict = mock(SApplication.class);

        ApplicationNode applicationNode = mock(ApplicationNode.class);
        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy,
                editable))
                        .willReturn(importResult);
        given(applicationService.getApplicationByToken("application")).willReturn(appInConflict);
        given(strategy.whenApplicationExists(any(), any())).willReturn(ApplicationImportStrategy.ImportStrategy.SKIP);
        //when
        ImportStatus importStatus = applicationImporter.importApplication(applicationNode, editable, createdBy,
                ICON_CONTENT, ICON_MIME_TYPE, true, strategy);

        //then
        verify(applicationService, never()).forceDeleteApplication(any());
        verify(applicationService, never()).createApplication(any());
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.SKIPPED);
    }

    @Test
    public void importApplication_replace_application_when_strategy_is_replace_duplicate() throws Exception {
        //given
        long createdBy = 5L;
        boolean editable = true;
        SApplicationWithIcon appToBeImported = mock(SApplicationWithIcon.class);
        given(appToBeImported.getToken()).willReturn("application");

        ImportResult importResult = new ImportResult(appToBeImported, new ImportStatus("name"));

        SApplication appInConflict = mock(SApplication.class);

        ApplicationNode applicationNode = mock(ApplicationNode.class);
        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy,
                editable))
                        .willReturn(importResult);
        given(applicationService.getApplicationByToken("application")).willReturn(appInConflict);

        //when
        ImportStatus importStatus = applicationImporter.importApplication(applicationNode, editable, createdBy,
                ICON_CONTENT, ICON_MIME_TYPE, true, new ReplaceDuplicateApplicationImportStrategy());

        //then
        verify(applicationService).forceDeleteApplication(appInConflict);
        verify(applicationService).createApplication(appToBeImported);
        verify(applicationService).createApplication(appToBeImported);
        verify(applicationService).createApplication(appToBeImported);
        assertThat(importStatus.getStatus()).isEqualTo(ImportStatus.Status.REPLACED);
    }

    @Test
    public void importApplication_should_throw_alreadyExistsException_when_strategy_FailOnDuplicateApplicationImportStrategy()
            throws Exception {

        //given
        long createdBy = 5L;
        boolean editable = true;
        SApplicationWithIcon appToBeImported = mock(SApplicationWithIcon.class);
        given(appToBeImported.getToken()).willReturn("application");

        ImportResult importResult = mock(ImportResult.class);
        given(importResult.getApplication()).willReturn(appToBeImported);
        ImportStatus importStatus = mock(ImportStatus.class);
        given(importResult.getImportStatus()).willReturn(importStatus);

        SApplication appInConflict = mock(SApplication.class);

        ApplicationNode applicationNode = mock(ApplicationNode.class);
        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy,
                editable))
                        .willReturn(importResult);
        given(applicationService.getApplicationByToken("application")).willReturn(appInConflict);

        //when - then exception
        assertThatExceptionOfType(AlreadyExistsException.class)
                .isThrownBy(() -> applicationImporter
                        .importApplication(applicationNode, editable, createdBy, ICON_CONTENT, ICON_MIME_TYPE, true,
                                new FailOnDuplicateApplicationImportStrategy()));
    }

    @Test
    public void importApplication_should_throw_ExecutionException_when_application_service_throws_exception()
            throws Exception {
        //given
        SApplicationWithIcon app1 = mock(SApplicationWithIcon.class);
        ImportResult importResult = mock(ImportResult.class);
        given(importResult.getApplication()).willReturn(app1);
        ImportStatus importStatus = mock(ImportStatus.class);
        given(importResult.getImportStatus()).willReturn(importStatus);

        long createdBy = 5L;
        boolean editable = true;

        given(importResult.getApplication()).willReturn(app1);

        ApplicationNode applicationNode = mock(ApplicationNode.class);
        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy,
                editable))
                        .willReturn(importResult);

        given(applicationService.createApplication(app1)).willThrow(new SObjectCreationException(""));

        //when - then exception
        assertThatExceptionOfType(ImportException.class).isThrownBy(() -> applicationImporter
                .importApplication(applicationNode, editable, createdBy, ICON_CONTENT, ICON_MIME_TYPE, true, strategy));
    }

    @Test
    public void importApplicationLink_should_create_application_link_with_no_menus_and_no_pages_and_no_home_page()
            throws Exception {
        //given
        long createdBy = SessionService.SYSTEM_ID;
        boolean editable = true;
        SApplicationWithIcon app = new SApplicationWithIcon("app", "app", "1.0", 1L, createdBy, "state", editable);
        app.setId(1);
        app.setLink(true);
        given(importResult.getApplication()).willReturn(app);
        given(importResult.getImportStatus()).willReturn(new ImportStatus(app.getToken()));

        ApplicationLinkNode applicationLinkNode = new ApplicationLinkNode();
        applicationLinkNode.setToken("app");

        given(nodeToApplicationConverter.toSApplication(applicationLinkNode, null, null, createdBy, editable))
                .willReturn(importResult);

        given(applicationService.createApplication(app)).willReturn(app);

        //when
        ImportStatus retrievedStatus = applicationImporter.importApplication(applicationLinkNode, editable,
                createdBy, null, null, true, strategy);

        //then
        //create application
        assertThat(retrievedStatus.getStatus()).isEqualTo(ImportStatus.Status.ADDED);
        assertThat(retrievedStatus.getErrors()).isEmpty();
        verify(applicationService).createApplication(app);
        verifyNoInteractions(strategy);

        //no pages added
        verify(applicationPageImporter, never()).importApplicationPages(any(), any());
        //no menus added
        verify(applicationMenuImporter, never()).importApplicationMenus(any(), any());
        //no home page set
        verify(applicationService, never()).updateApplication(any(), any());
    }
}
