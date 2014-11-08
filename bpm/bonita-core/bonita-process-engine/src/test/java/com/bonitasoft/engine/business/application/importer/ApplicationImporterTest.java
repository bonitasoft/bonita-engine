/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.importer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Arrays;
import java.util.List;

import com.bonitasoft.engine.business.application.converter.ApplicationNodeConverter;
import com.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import com.bonitasoft.engine.business.application.xml.ApplicationNode;
import com.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportStatus;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.exception.ExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.model.SApplication;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationImporterTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ApplicationImportStrategy strategy;

    @Mock
    private ApplicationNodeConverter applicationNodeConverter;

    @Mock
    private ApplicationPageImporter applicationPageImporter;

    @Mock
    private ApplicationMenuImporter applicationMenuImporter;

    @InjectMocks
    private ApplicationImporter applicationImporter;

    @Test
    public void importApplication_should_create_application_import_pages_and_menus_and_return_status() throws Exception {
        //given
        long createdBy = 5L;
        SApplication app = mock(SApplication.class);

        ImportResult importResult = mock(ImportResult.class);
        given(importResult.getApplication()).willReturn(app);
        ImportStatus importStatus = mock(ImportStatus.class);
        given(importResult.getImportStatus()).willReturn(importStatus);

        ApplicationPageNode pageNode1 = mock(ApplicationPageNode.class);
        ApplicationPageNode pageNode2 = mock(ApplicationPageNode.class);

        ApplicationMenuNode menu1 = new ApplicationMenuNode();
        ApplicationMenuNode menu2 = new ApplicationMenuNode();

        ApplicationNode applicationNode = mock(ApplicationNode.class);
        given(applicationNode.getApplicationPages()).willReturn(Arrays.asList(pageNode1, pageNode2));
        given(applicationNode.getApplicationMenus()).willReturn(Arrays.asList(menu1, menu2));
        given(applicationNodeConverter.toSApplication(applicationNode, createdBy)).willReturn(importResult);

        ImportError errorPage = mock(ImportError.class);
        List<ImportError> errorsMenu = Arrays.asList(mock(ImportError.class));
        given(applicationPageImporter.importApplicationPage(pageNode1, app)).willReturn(errorPage);
        given(applicationMenuImporter.importApplicationMenu(menu1, app, null)).willReturn(errorsMenu);

        given(applicationService.createApplication(app)).willReturn(app);

        //when
        ImportStatus retrievedStatus = applicationImporter.importApplication(applicationNode, createdBy);

        //then
        assertThat(retrievedStatus).isEqualTo(importResult.getImportStatus());
        verify(applicationService, times(1)).createApplication(app);
        verifyZeroInteractions(strategy);

        verify(applicationPageImporter, times(1)).importApplicationPage(pageNode1, app);
        verify(applicationPageImporter, times(1)).importApplicationPage(pageNode2, app);
        verify(importStatus, times(1)).addError(errorPage);

        verify(applicationMenuImporter, times(1)).importApplicationMenu(menu1, app, null);
        verify(applicationMenuImporter, times(1)).importApplicationMenu(menu2, app, null);
        verify(importStatus, times(1)).addErrors(errorsMenu);
        verify(importStatus, never()).addError(null);
    }

    @Test
    public void importApplication_should_call_importStrategy_when_application_already_exists() throws Exception {
        //given
        long createdBy = 5L;
        SApplication appToBeImported = mock(SApplication.class);
        given(appToBeImported.getToken()).willReturn("application");

        ImportResult importResult = mock(ImportResult.class);
        given(importResult.getApplication()).willReturn(appToBeImported);

        SApplication appInConflict = mock(SApplication.class);

        ApplicationNode applicationNode = mock(ApplicationNode.class);
        given(applicationNodeConverter.toSApplication(applicationNode, createdBy)).willReturn(importResult);
        given(applicationService.getApplicationByToken("application")).willReturn(appInConflict);

        //when
        applicationImporter.importApplication(applicationNode, createdBy);

        //then
        verify(applicationService, times(1)).createApplication(appToBeImported);
        verify(strategy, times(1)).whenApplicationExists(appInConflict, appToBeImported);
    }

    @Test(expected = ExecutionException.class)
    public void importApplication_should_throw_ExecutionException_when_application_service_throws_exception() throws Exception {
        //given
        long createdBy = 5L;
        SApplication app1 = mock(SApplication.class);

        ImportResult importResult = mock(ImportResult.class);
        given(importResult.getApplication()).willReturn(app1);

        ApplicationNode applicationNode = mock(ApplicationNode.class);
        given(applicationNodeConverter.toSApplication(applicationNode, createdBy)).willReturn(importResult);

        given(applicationService.createApplication(app1)).willThrow(new SObjectCreationException(""));

        //when
        applicationImporter.importApplication(applicationNode, createdBy);

        //then exception
    }
}
