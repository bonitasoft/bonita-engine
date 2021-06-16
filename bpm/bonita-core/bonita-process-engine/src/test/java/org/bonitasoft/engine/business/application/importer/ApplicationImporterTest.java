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
import org.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeBuilder;
import org.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.ImportException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationImporterTest {

    private static final String UNUSED_PAGE = "";

    private static String ICON_MIME_TYPE = "iconMimeType";

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

    @InjectMocks
    private ApplicationImporter applicationImporter;

    @Before
    public void before() {
    }

    @Test
    public void importApplication_should_create_application_import_pages_and_menus_and_return_status()
            throws Exception {
        //given
        long createdBy = 5L;
        SApplicationWithIcon app = mock(SApplicationWithIcon.class);

        ImportResult importResult = mock(ImportResult.class);
        given(importResult.getApplication()).willReturn(app);
        ImportStatus importStatus = mock(ImportStatus.class);
        given(importResult.getImportStatus()).willReturn(importStatus);

        ApplicationPageNode pageNode1 = mock(ApplicationPageNode.class);
        ApplicationPageNode pageNode2 = mock(ApplicationPageNode.class);

        ApplicationMenuNode menu1 = ApplicationNodeBuilder.newMenu("menu1", UNUSED_PAGE).create();
        ApplicationMenuNode menu2 = ApplicationNodeBuilder.newMenu("menu2", UNUSED_PAGE).create();

        ApplicationNode applicationNode = new ApplicationNode();
        applicationNode.addApplicationPage(pageNode1);
        applicationNode.addApplicationPage(pageNode2);
        applicationNode.addApplicationMenu(menu1);
        applicationNode.addApplicationMenu(menu2);
        applicationNode.setHomePage("home");
        applicationNode.setToken("app");
        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy))
                .willReturn(importResult);

        long homePageId = 222L;
        SApplicationPage applicationPage = mock(SApplicationPage.class);
        given(applicationPage.getId()).willReturn(homePageId);
        given(applicationService.getApplicationPage("app", "home")).willReturn(applicationPage);

        ImportError errorPage = mock(ImportError.class);
        List<ImportError> errorsMenu = Arrays.asList(mock(ImportError.class), mock(ImportError.class));
        given(applicationPageImporter.importApplicationPage(pageNode1, app)).willReturn(errorPage);
        given(applicationMenuImporter.importApplicationMenu(menu1, app, null)).willReturn(errorsMenu);

        given(applicationService.createApplication(app)).willReturn(app);

        //when
        ImportStatus retrievedStatus = applicationImporter.importApplication(applicationNode, ICON_CONTENT,
                ICON_MIME_TYPE, createdBy, strategy);

        //then
        //create application
        assertThat(retrievedStatus).isEqualTo(importResult.getImportStatus());
        verify(applicationService, times(1)).createApplication(app);
        verifyZeroInteractions(strategy);

        //add pages
        verify(applicationPageImporter, times(1)).importApplicationPage(pageNode1, app);
        verify(applicationPageImporter, times(1)).importApplicationPage(pageNode2, app);
        verify(importStatus, times(1)).addError(errorPage);

        //add menus
        verify(applicationMenuImporter, times(1)).importApplicationMenu(menu1, app, null);
        verify(applicationMenuImporter, times(1)).importApplicationMenu(menu2, app, null);
        verify(importStatus, times(1)).addError(errorsMenu.get(0));
        verify(importStatus, times(1)).addError(errorsMenu.get(1));

        //set home page
        ArgumentCaptor<EntityUpdateDescriptor> updateCaptor = ArgumentCaptor.forClass(EntityUpdateDescriptor.class);
        verify(applicationService, times(1)).updateApplication(eq(app), updateCaptor.capture());
        EntityUpdateDescriptor updateDescriptor = updateCaptor.getValue();
        assertThat(updateDescriptor.getFields().get(AbstractSApplication.HOME_PAGE_ID)).isEqualTo(homePageId);
    }

    @Test
    public void importApplicationSetEditable_should_create_application_import_pages_and_menus_and_return_status()
            throws Exception {
        //given
        long createdBy = 408L;
        SApplicationWithIcon application = mock(SApplicationWithIcon.class);
        ImportResult importResult = mock(ImportResult.class);
        ImportStatus importStatus = mock(ImportStatus.class);
        given(importResult.getImportStatus()).willReturn(importStatus);
        given(importResult.getApplication()).willReturn(application);

        ApplicationNode applicationNode = new ApplicationNode();
        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy))
                .willReturn(importResult);

        //when
        applicationImporter.importApplicationSetEditable(applicationNode, ICON_CONTENT,
                ICON_MIME_TYPE, createdBy, strategy, false);

        //then
        verify(applicationService).createApplication(argThat(app -> !app.isEditable()));
    }

    @Test
    public void importApplication_should_not_add_error_when_error_already_exists() throws Exception {
        //given
        long createdBy = 5L;
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
        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy))
                .willReturn(importResult);

        given(applicationPageImporter.importApplicationPage(pageNode1, app)).willReturn(errorPage);

        given(applicationService.createApplication(app)).willReturn(app);

        //when
        ImportStatus retrievedStatus = applicationImporter.importApplication(applicationNode, ICON_CONTENT,
                ICON_MIME_TYPE, createdBy, strategy);

        //then
        assertThat(retrievedStatus).isEqualTo(importResult.getImportStatus());
        assertThat(retrievedStatus.getErrors()).containsExactly(errorPage);

    }

    @Test
    public void importApplication_should_not_set_home_page_when_applicationNode_does_not_have_home_page()
            throws Exception {
        //given
        long createdBy = 5L;
        SApplicationWithIcon app = mock(SApplicationWithIcon.class);

        ImportResult importResult = mock(ImportResult.class);
        given(importResult.getApplication()).willReturn(app);
        ImportStatus importStatus = mock(ImportStatus.class);
        given(importResult.getImportStatus()).willReturn(importStatus);

        ApplicationNode applicationNode = new ApplicationNode();
        applicationNode.setToken("app");
        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy))
                .willReturn(importResult);
        given(applicationService.createApplication(app)).willReturn(app);

        //when
        applicationImporter.importApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy, strategy);

        //then
        //set home page
        verify(applicationService, never()).updateApplication(any(SApplicationWithIcon.class),
                any(EntityUpdateDescriptor.class));
    }

    @Test
    public void importApplication_should_add_error_when_home_page_is_not_found() throws Exception {
        //given
        long createdBy = 5L;
        SApplicationWithIcon app = mock(SApplicationWithIcon.class);

        ImportResult importResult = mock(ImportResult.class);
        given(importResult.getApplication()).willReturn(app);
        ImportStatus importStatus = mock(ImportStatus.class);
        given(importResult.getImportStatus()).willReturn(importStatus);

        ApplicationNode applicationNode = new ApplicationNode();
        applicationNode.setToken("app");
        applicationNode.setHomePage("home");

        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy))
                .willReturn(importResult);
        given(applicationService.createApplication(app)).willReturn(app);

        given(applicationService.getApplicationPage("app", "home")).willThrow(new SObjectNotFoundException(""));

        //when
        applicationImporter.importApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy, strategy);

        //then
        //set home page
        verify(applicationService, never()).updateApplication(any(SApplicationWithIcon.class),
                any(EntityUpdateDescriptor.class));
        verify(importStatus, times(1)).addError(new ImportError("home", ImportError.Type.APPLICATION_PAGE));
    }

    @Test
    public void importApplication_should_call_importStrategy_when_application_already_exists() throws Exception {
        //given
        long createdBy = 5L;
        SApplicationWithIcon appToBeImported = mock(SApplicationWithIcon.class);
        given(appToBeImported.getToken()).willReturn("application");

        ImportResult importResult = new ImportResult(appToBeImported, new ImportStatus("name"));

        SApplication appInConflict = mock(SApplication.class);

        ApplicationNode applicationNode = mock(ApplicationNode.class);
        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy))
                .willReturn(importResult);
        given(applicationService.getApplicationByToken("application")).willReturn(appInConflict);

        //when
        applicationImporter.importApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy, strategy);

        //then
        verify(applicationService, times(1)).createApplication(appToBeImported);
        verify(strategy, times(1)).whenApplicationExists(appInConflict, appToBeImported);
    }

    @Test(expected = AlreadyExistsException.class)
    public void importApplication_should_throw_alreadyExistsException_when_stratege_throws_AlreadyExistsException()
            throws Exception {
        //given
        long createdBy = 5L;
        SApplicationWithIcon appToBeImported = mock(SApplicationWithIcon.class);
        given(appToBeImported.getToken()).willReturn("application");

        ImportResult importResult = mock(ImportResult.class);
        given(importResult.getApplication()).willReturn(appToBeImported);

        SApplication appInConflict = mock(SApplication.class);

        ApplicationNode applicationNode = mock(ApplicationNode.class);
        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy))
                .willReturn(importResult);
        given(applicationService.getApplicationByToken("application")).willReturn(appInConflict);
        doThrow(new AlreadyExistsException("")).when(strategy).whenApplicationExists(appInConflict,
                appToBeImported);

        //when
        applicationImporter.importApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy, strategy);

        //then exception
    }

    @Test(expected = ImportException.class)
    public void importApplication_should_throw_ExecutionException_when_application_service_throws_exception()
            throws Exception {
        //given
        long createdBy = 5L;
        SApplicationWithIcon app1 = mock(SApplicationWithIcon.class);

        ImportResult importResult = mock(ImportResult.class);
        given(importResult.getApplication()).willReturn(app1);

        ApplicationNode applicationNode = mock(ApplicationNode.class);
        given(nodeToApplicationConverter.toSApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy))
                .willReturn(importResult);

        given(applicationService.createApplication(app1)).willThrow(new SObjectCreationException(""));

        //when
        applicationImporter.importApplication(applicationNode, ICON_CONTENT, ICON_MIME_TYPE, createdBy, strategy);

        //then exception
    }

    @Test
    public void should_import_default_applications_on_init_and_correctly_set_final_fields() throws Exception {
        //given
        SApplicationWithIcon app1 = new SApplicationWithIcon();
        app1.setId(1);
        SApplicationWithIcon app2 = new SApplicationWithIcon();
        app2.setId(2);
        SApplicationWithIcon app3 = new SApplicationWithIcon();
        app2.setId(3);
        when(nodeToApplicationConverter.toSApplication(argThat(a -> a != null && "default_app_1".equals(a.getToken())),
                any(), anyString(), anyLong()))
                        .thenReturn(new ImportResult(app1, new ImportStatus("ok")));
        when(nodeToApplicationConverter.toSApplication(argThat(a -> a != null && "default_app_2".equals(a.getToken())),
                any(), anyString(), anyLong()))
                        .thenReturn(new ImportResult(app2, new ImportStatus("ok")));
        when(nodeToApplicationConverter.toSApplication(argThat(a -> a != null && "default_app_3".equals(a.getToken())),
                any(), anyString(), anyLong()))
                        .thenReturn(new ImportResult(app3, new ImportStatus("ok")));

        //when
        applicationImporter.init();

        //then

        verify(applicationService, times(2)).createApplication(argThat(app -> !app.isEditable()));
        verify(applicationService, times(1)).createApplication(argThat(AbstractSApplication::isEditable));
    }

    //TODO: create test that validate the default import strategy when we have decided what strategy it should be

}
