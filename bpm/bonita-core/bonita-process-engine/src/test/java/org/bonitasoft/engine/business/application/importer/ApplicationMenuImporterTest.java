/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.converter.ApplicationMenuNodeConverter;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.exception.ImportException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMenuImporterTest {

    @Mock
    ApplicationService applicationService;

    @Mock
    ApplicationMenuNodeConverter converter;

    @InjectMocks
    ApplicationMenuImporter importer;

    @Test
    public void importApplicationMenu_should_create_applicationMenu_and_sub_menus_when_no_error() throws Exception {
        //given
        SApplication application = mock(SApplication.class);
        ApplicationMenuNode subMenuNode = new ApplicationMenuNode();

        ApplicationMenuNode menuNode = new ApplicationMenuNode();
        menuNode.addApplicationMenu(subMenuNode);

        SApplicationMenu mainMenu = mock(SApplicationMenu.class);
        ApplicationMenuImportResult mainMenuImportResult = mock(ApplicationMenuImportResult.class);
        given(mainMenuImportResult.getApplicationMenu()).willReturn(mainMenu);

        SApplicationMenu subMenu = mock(SApplicationMenu.class);
        ApplicationMenuImportResult subMenuImportResult = mock(ApplicationMenuImportResult.class);
        given(subMenuImportResult.getApplicationMenu()).willReturn(subMenu);

        given(converter.toSApplicationMenu(menuNode, application, null)).willReturn(mainMenuImportResult);
        given(converter.toSApplicationMenu(subMenuNode, application, null)).willReturn(subMenuImportResult);

        //when
        List<ImportError> importErrors = importer.importApplicationMenu(menuNode, application, null);

        //then
        assertThat(importErrors).isEmpty();
        verify(applicationService).createApplicationMenu(mainMenu);
        verify(applicationService).createApplicationMenu(subMenu);

    }

    @Test
    public void importApplicationMenu_should_not_create_applicationMenu_when_there_is_error() throws Exception {
        //given
        SApplication application = mock(SApplication.class);
        ApplicationMenuNode menuNode = new ApplicationMenuNode();

        SApplicationMenu applicationMenu = mock(SApplicationMenu.class);
        ImportError error = new ImportError("page", ImportError.Type.APPLICATION_PAGE);
        ApplicationMenuImportResult importResult = mock(ApplicationMenuImportResult.class);
        given(importResult.getApplicationMenu()).willReturn(applicationMenu);
        given(importResult.getError()).willReturn(error);

        given(converter.toSApplicationMenu(menuNode, application, null)).willReturn(importResult);

        //when
        List<ImportError> importErrors = importer.importApplicationMenu(menuNode, application, null);

        //then
        assertThat(importErrors).containsExactly(error);
        verify(applicationService, never()).createApplicationMenu(any(SApplicationMenu.class));

    }

    @Test(expected = ImportException.class)
    public void importApplicationMenu_should_throw_Exception_when_menu_creation_throws_exception() throws Exception {
        //given
        SApplication application = mock(SApplication.class);
        ApplicationMenuNode menuNode = new ApplicationMenuNode();

        SApplicationMenu mainMenu = mock(SApplicationMenu.class);
        ApplicationMenuImportResult mainMenuImportResult = mock(ApplicationMenuImportResult.class);
        given(mainMenuImportResult.getApplicationMenu()).willReturn(mainMenu);

        given(converter.toSApplicationMenu(menuNode, application, null)).willReturn(mainMenuImportResult);
        given(applicationService.createApplicationMenu(mainMenu)).willThrow(new SObjectCreationException());

        //when
        importer.importApplicationMenu(menuNode, application, null);

        //then exception

    }

    @Test
    public void importApplicationMenu_should_return_errors_for_sub_menus() throws Exception {
        //given
        SApplication application = mock(SApplication.class);
        ApplicationMenuNode subMenuNode1 = new ApplicationMenuNode();
        ApplicationMenuNode subMenuNode2 = new ApplicationMenuNode();

        ApplicationMenuNode menuNode = new ApplicationMenuNode();
        menuNode.addApplicationMenu(subMenuNode1);
        menuNode.addApplicationMenu(subMenuNode2);

        SApplicationMenu mainMenu = mock(SApplicationMenu.class);
        ApplicationMenuImportResult mainMenuImportResult = mock(ApplicationMenuImportResult.class);
        given(mainMenuImportResult.getApplicationMenu()).willReturn(mainMenu);

        ImportError error1 = new ImportError("page1", ImportError.Type.APPLICATION_PAGE);
        SApplicationMenu subMenu1 = mock(SApplicationMenu.class);
        ApplicationMenuImportResult subMenuImportResult1 = mock(ApplicationMenuImportResult.class);
        given(subMenuImportResult1.getApplicationMenu()).willReturn(subMenu1);
        given(subMenuImportResult1.getError()).willReturn(error1);

        ImportError error2 = new ImportError("page2", ImportError.Type.APPLICATION_PAGE);
        SApplicationMenu subMenu2 = mock(SApplicationMenu.class);
        ApplicationMenuImportResult subMenuImportResult2 = mock(ApplicationMenuImportResult.class);
        given(subMenuImportResult2.getApplicationMenu()).willReturn(subMenu2);
        given(subMenuImportResult2.getError()).willReturn(error2);

        given(converter.toSApplicationMenu(menuNode, application, null)).willReturn(mainMenuImportResult);
        given(converter.toSApplicationMenu(subMenuNode1, application, null)).willReturn(subMenuImportResult1);
        given(converter.toSApplicationMenu(subMenuNode2, application, null)).willReturn(subMenuImportResult2);

        //when
        List<ImportError> importErrors = importer.importApplicationMenu(menuNode, application, null);

        //then
        assertThat(importErrors).containsExactly(error1, error2);
        verify(applicationService).createApplicationMenu(mainMenu);
        verify(applicationService, never()).createApplicationMenu(subMenu1);
        verify(applicationService, never()).createApplicationMenu(subMenu2);

    }

}
