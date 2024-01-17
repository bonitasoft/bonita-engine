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
import org.bonitasoft.engine.business.application.converter.NodeToApplicationMenuConverter;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationWithIcon;
import org.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNodeBuilder;
import org.bonitasoft.engine.commons.exceptions.SObjectCreationException;
import org.bonitasoft.engine.exception.ImportException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMenuImporterTest {

    @Mock
    ApplicationService applicationService;

    @Mock
    NodeToApplicationMenuConverter converter;

    @InjectMocks
    ApplicationMenuImporter importer;

    @Test
    public void importApplicationMenu_should_create_applicationMenu_and_sub_menus_when_no_error() throws Exception {
        //given
        SApplicationWithIcon application = mock(SApplicationWithIcon.class);
        ApplicationMenuNode subMenuNode = ApplicationNodeBuilder.newMenu("subMenuNode", "").create();

        ApplicationMenuNode menuNode = ApplicationNodeBuilder.newMenu("menuNode", "").create();
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
        SApplicationWithIcon application = mock(SApplicationWithIcon.class);
        ApplicationMenuNode menuNode = new ApplicationMenuNode();

        ImportError error = new ImportError("page", ImportError.Type.APPLICATION_PAGE);
        ApplicationMenuImportResult importResult = mock(ApplicationMenuImportResult.class);
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
        SApplicationWithIcon application = mock(SApplicationWithIcon.class);
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
        SApplicationWithIcon application = mock(SApplicationWithIcon.class);
        ApplicationMenuNode subMenuNode1 = ApplicationNodeBuilder.newMenu("subMenuNode1", "").create();
        ApplicationMenuNode subMenuNode2 = ApplicationNodeBuilder.newMenu("subMenuNode2", "").create();

        ApplicationMenuNode menuNode = new ApplicationMenuNode();
        menuNode.addApplicationMenu(subMenuNode1);
        menuNode.addApplicationMenu(subMenuNode2);

        SApplicationMenu mainMenu = mock(SApplicationMenu.class);
        ApplicationMenuImportResult mainMenuImportResult = mock(ApplicationMenuImportResult.class);
        given(mainMenuImportResult.getApplicationMenu()).willReturn(mainMenu);

        ImportError error1 = new ImportError("page1", ImportError.Type.APPLICATION_PAGE);
        SApplicationMenu subMenu1 = mock(SApplicationMenu.class);
        ApplicationMenuImportResult subMenuImportResult1 = mock(ApplicationMenuImportResult.class);
        given(subMenuImportResult1.getError()).willReturn(error1);

        ImportError error2 = new ImportError("page2", ImportError.Type.APPLICATION_PAGE);
        SApplicationMenu subMenu2 = mock(SApplicationMenu.class);
        ApplicationMenuImportResult subMenuImportResult2 = mock(ApplicationMenuImportResult.class);
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

    @Test
    public void importApplicationMenus_should_invoke_application_menu() throws Exception {

        SApplicationWithIcon application = mock(SApplicationWithIcon.class);
        ApplicationMenuNode subMenuNode1 = ApplicationNodeBuilder.newMenu("subMenuNode1", "").create();
        ApplicationMenuNode subMenuNode2 = ApplicationNodeBuilder.newMenu("subMenuNode2", "").create();

        ApplicationMenuNode subMenuNode3 = ApplicationNodeBuilder.newMenu("subMenuNode3", "").create();
        ApplicationMenuNode subMenuNode4 = ApplicationNodeBuilder.newMenu("subMenuNode4", "").create();

        ApplicationMenuNode menuNode = new ApplicationMenuNode();
        menuNode.addApplicationMenu(subMenuNode1);
        menuNode.addApplicationMenu(subMenuNode2);

        ImportError error1 = new ImportError("page1", ImportError.Type.APPLICATION_PAGE);
        ImportError error2 = new ImportError("page2", ImportError.Type.APPLICATION_PAGE);

        ApplicationMenuNode menuNode2 = new ApplicationMenuNode();
        menuNode2.addApplicationMenu(subMenuNode3);
        menuNode2.addApplicationMenu(subMenuNode4);

        importer = spy(new ApplicationMenuImporter(applicationService, converter));

        doReturn(Arrays.asList(error1, error1)).when(importer).importApplicationMenu(eq(menuNode), any(), any());
        doReturn(Arrays.asList(error2, error2)).when(importer).importApplicationMenu(eq(menuNode2), any(), any());

        //when
        List<ImportError> importErrors = importer.importApplicationMenus(Arrays.asList(menuNode, menuNode2),
                application);

        //then
        verify(importer, times(1)).importApplicationMenu(eq(menuNode), any(), eq(null));
        verify(importer, times(1)).importApplicationMenu(eq(menuNode2), any(), eq(null));
        assertThat(importErrors).containsExactlyInAnyOrder(error1, error1, error2, error2);
    }

}
