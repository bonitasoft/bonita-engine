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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.exception.ImportException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.converter.ApplicationPageNodeConverter;
import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.SApplicationPage;
import com.bonitasoft.engine.business.application.xml.ApplicationPageNode;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationPageImporterTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ApplicationPageNodeConverter converter;

    @InjectMocks
    private ApplicationPageImporter importer;

    @Test
    public void importApplicationPage_should_create_applicationPage_when_there_is_no_error() throws Exception {
        //given
        SApplication application = mock(SApplication.class);
        ApplicationPageNode applicationPageNode = mock(ApplicationPageNode.class);
        SApplicationPage applicationPage = mock(SApplicationPage.class);
        ApplicationPageImportResult importResult = new ApplicationPageImportResult(applicationPage, null);

        given(converter.toSApplicationPage(applicationPageNode, application)).willReturn(importResult);

        //when
        ImportError importError = importer.importApplicationPage(applicationPageNode, application);

        //then
        verify(applicationService, times(1)).createApplicationPage(applicationPage);
        assertThat(importError).isNull();
    }

    @Test
    public void importApplicationPage_should_not_create_applicationPage_when_there_is_an_error() throws Exception {
        //given
        SApplication application = mock(SApplication.class);
        ApplicationPageNode applicationPageNode = mock(ApplicationPageNode.class);
        SApplicationPage applicationPage = mock(SApplicationPage.class);
        ImportError error = mock(ImportError.class);
        ApplicationPageImportResult importResult = new ApplicationPageImportResult(applicationPage, error);

        given(converter.toSApplicationPage(applicationPageNode, application)).willReturn(importResult);

        //when
        ImportError importError = importer.importApplicationPage(applicationPageNode, application);

        //then
        verify(applicationService, never()).createApplicationPage(any(SApplicationPage.class));
        assertThat(importError).isEqualTo(error);
    }

    @Test(expected = ImportException.class)
    public void importApplicationPage_should_throw_Exception_when_create_applicationPage_throws_exception() throws Exception {
        //given
        SApplication application = mock(SApplication.class);
        ApplicationPageNode applicationPageNode = mock(ApplicationPageNode.class);
        SApplicationPage applicationPage = mock(SApplicationPage.class);
        ApplicationPageImportResult importResult = new ApplicationPageImportResult(applicationPage, null);

        given(converter.toSApplicationPage(applicationPageNode, application)).willReturn(importResult);
        given(applicationService.createApplicationPage(applicationPage)).willThrow(new SObjectCreationException(""));

        //when
        importer.importApplicationPage(applicationPageNode, application);

        //then exception
    }

}
