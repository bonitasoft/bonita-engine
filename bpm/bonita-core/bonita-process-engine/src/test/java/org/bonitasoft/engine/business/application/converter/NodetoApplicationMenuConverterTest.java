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

package org.bonitasoft.engine.business.application.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.importer.ApplicationMenuImportResult;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class NodetoApplicationMenuConverterTest {

    @Mock
    ApplicationService applicationService;

    @InjectMocks
    private NodeToApplicationMenuConverter converter;


    @Test
    public void toSApplicationMenu_should_convert_all_fields() throws Exception {
        //given
        long applicationId = 3L;
        SApplication application = mock(SApplication.class);
        given(application.getId()).willReturn(applicationId);
        given(application.getToken()).willReturn("app");

        long parentMenuId = 100L;
        SApplicationMenu parentMenu = buildMenu(parentMenuId);

        ApplicationMenuNode menuNode = new ApplicationMenuNode();
        menuNode.setApplicationPage("home");
        menuNode.setDisplayName("Sub");

        long appPageId = 12L;
        SApplicationPage applicationPage = mock(SApplicationPage.class);
        given(applicationPage.getId()).willReturn(appPageId);
        given(applicationService.getApplicationPage("app", "home")).willReturn(applicationPage);

        int index = 4;
        given(applicationService.getNextAvailableIndex(parentMenuId)).willReturn(index);

        //when
        ApplicationMenuImportResult importResult = converter.toSApplicationMenu(menuNode, application, parentMenu);

        //then
        assertThat(importResult).isNotNull();
        SApplicationMenu applicationMenu = importResult.getApplicationMenu();
        assertThat(applicationMenu.getApplicationId()).isEqualTo(applicationId);
        assertThat(applicationMenu.getApplicationPageId()).isEqualTo(appPageId);
        assertThat(applicationMenu.getDisplayName()).isEqualTo("Sub");
        assertThat(applicationMenu.getIndex()).isEqualTo(index);
        assertThat(applicationMenu.getParentId()).isEqualTo(parentMenuId);
        assertThat(importResult.getError()).isNull();
    }

    @Test
    public void toSApplicationMenu_should_not_set_parent_id_when_parent_is_null() throws Exception {
        //given
        long applicationId = 3L;
        SApplication application = mock(SApplication.class);
        given(application.getId()).willReturn(applicationId);
        given(application.getToken()).willReturn("app");

        ApplicationMenuNode menuNode = new ApplicationMenuNode();
        menuNode.setApplicationPage("home");
        menuNode.setDisplayName("Sub");

        long appPageId = 12L;
        SApplicationPage applicationPage = mock(SApplicationPage.class);
        given(applicationPage.getId()).willReturn(appPageId);
        given(applicationService.getApplicationPage("app", "home")).willReturn(applicationPage);

        int index = 4;
        given(applicationService.getNextAvailableIndex(null)).willReturn(index);

        //when
        ApplicationMenuImportResult importResult = converter.toSApplicationMenu(menuNode, application, null);

        //then
        assertThat(importResult).isNotNull();
        SApplicationMenu applicationMenu = importResult.getApplicationMenu();
        assertThat(applicationMenu.getApplicationId()).isEqualTo(applicationId);
        assertThat(applicationMenu.getApplicationPageId()).isEqualTo(appPageId);
        assertThat(applicationMenu.getDisplayName()).isEqualTo("Sub");
        assertThat(applicationMenu.getIndex()).isEqualTo(index);
        assertThat(applicationMenu.getParentId()).isNull();
        assertThat(importResult.getError()).isNull();
    }

    private SApplicationMenu buildMenu(final long id) {
        SApplicationMenu parentMenu = mock(SApplicationMenu.class);
        given(parentMenu.getId()).willReturn(id);
        return parentMenu;
    }

    @Test
    public void toSApplicationMenu_should_have_null_applicationPageId_when_applicationPage_is_null_in_xml() throws Exception {
        //given
        long applicationId = 3L;
        SApplication application = mock(SApplication.class);
        given(application.getId()).willReturn(applicationId);

        ApplicationMenuNode menuNode = new ApplicationMenuNode();
        menuNode.setDisplayName("Sub");

        long parentMenuId = 100L;
        SApplicationMenu parentMenu = buildMenu(parentMenuId);

        int index = 4;
        given(applicationService.getNextAvailableIndex(parentMenuId)).willReturn(index);

        //when
        ApplicationMenuImportResult importResult = converter.toSApplicationMenu(menuNode, application, parentMenu);

        //then
        assertThat(importResult).isNotNull();
        SApplicationMenu applicationMenu = importResult.getApplicationMenu();
        assertThat(applicationMenu.getApplicationId()).isEqualTo(applicationId);
        assertThat(applicationMenu.getApplicationPageId()).isNull();
        assertThat(applicationMenu.getDisplayName()).isEqualTo("Sub");
        assertThat(applicationMenu.getIndex()).isEqualTo(index);
        assertThat(applicationMenu.getParentId()).isEqualTo(parentMenuId);
        assertThat(importResult.getError()).isNull();

        verify(applicationService, never()).getApplicationPage(anyString(), anyString());
    }

    @Test
    public void toSApplicationMenu_return_error_when_ApplicationPage_is_not_found() throws Exception {
        //given
        long applicationId = 3L;
        SApplication application = mock(SApplication.class);
        given(application.getId()).willReturn(applicationId);
        given(application.getToken()).willReturn("app");

        ApplicationMenuNode menuNode = new ApplicationMenuNode();
        menuNode.setApplicationPage("home");
        menuNode.setDisplayName("Sub");

        given(applicationService.getApplicationPage("app", "home")).willThrow(new SObjectNotFoundException());

        long parentMenuId = 100L;
        int index = 4;
        given(applicationService.getNextAvailableIndex(parentMenuId)).willReturn(index);

        SApplicationMenu parentMenu = buildMenu(parentMenuId);

        //when
        ApplicationMenuImportResult importResult = converter.toSApplicationMenu(menuNode, application, parentMenu);

        //then
        assertThat(importResult).isNotNull();
        SApplicationMenu applicationMenu = importResult.getApplicationMenu();
        assertThat(applicationMenu.getApplicationId()).isEqualTo(applicationId);
        assertThat(applicationMenu.getApplicationPageId()).isNull();
        assertThat(applicationMenu.getDisplayName()).isEqualTo("Sub");
        assertThat(applicationMenu.getIndex()).isEqualTo(index);
        assertThat(applicationMenu.getParentId()).isEqualTo(parentMenuId);
        assertThat(importResult.getError()).isEqualTo(new ImportError("home", ImportError.Type.APPLICATION_PAGE));
    }

}
