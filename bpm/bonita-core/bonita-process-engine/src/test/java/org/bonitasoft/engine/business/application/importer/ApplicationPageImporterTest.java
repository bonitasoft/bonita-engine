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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.converter.NodeToApplicationPageConverter;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.business.application.xml.ApplicationPageNode;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.exception.ImportException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationPageImporterTest {

    @Mock
    private ApplicationService applicationService;

    @Mock
    private NodeToApplicationPageConverter converter;

    @InjectMocks
    private ApplicationPageImporter importer;

    @Test
    public void importApplicationPage_should_create_applicationPage_when_there_is_no_error() throws Exception {
        //given
        SApplicationWithIcon application = mock(SApplicationWithIcon.class);
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
        SApplicationWithIcon application = mock(SApplicationWithIcon.class);
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
    public void importApplicationPage_should_throw_Exception_when_create_applicationPage_throws_exception()
            throws Exception {
        //given
        SApplicationWithIcon application = mock(SApplicationWithIcon.class);
        ApplicationPageNode applicationPageNode = mock(ApplicationPageNode.class);
        SApplicationPage applicationPage = mock(SApplicationPage.class);
        ApplicationPageImportResult importResult = new ApplicationPageImportResult(applicationPage, null);

        given(converter.toSApplicationPage(applicationPageNode, application)).willReturn(importResult);
        given(applicationService.createApplicationPage(applicationPage)).willThrow(new SObjectCreationException(""));

        //when
        importer.importApplicationPage(applicationPageNode, application);

        //then exception
    }

    @Test
    public void importApplicationPages_should_invoke_import_application_page() throws Exception {
        importer = spy(new ApplicationPageImporter(applicationService, converter));
        //given
        SApplicationWithIcon application = mock(SApplicationWithIcon.class);

        ApplicationPageNode applicationPageNode = new ApplicationPageNode();
        applicationPageNode.setToken("appNode1");
        ApplicationPageNode applicationPageNode1 = new ApplicationPageNode();
        applicationPageNode1.setToken("appNode2");
        ImportError error1 = new ImportError("page1", ImportError.Type.APPLICATION_PAGE);
        ImportError error2 = new ImportError("page2", ImportError.Type.APPLICATION_PAGE);

        doReturn(error1).when(importer).importApplicationPage(eq(applicationPageNode), any());
        doReturn(error2).when(importer).importApplicationPage(eq(applicationPageNode1), any());

        //when
        List<ImportError> importErrors = importer
                .importApplicationPages(Arrays.asList(applicationPageNode, applicationPageNode1), application);

        //then
        verify(importer, times(1)).importApplicationPage(eq(applicationPageNode), any());
        verify(importer, times(1)).importApplicationPage(eq(applicationPageNode1), any());
        assertThat(importErrors).containsExactlyInAnyOrder(error1, error2);
    }

}
