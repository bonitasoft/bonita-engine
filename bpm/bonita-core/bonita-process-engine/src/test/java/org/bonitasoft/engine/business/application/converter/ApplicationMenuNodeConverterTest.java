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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.importer.ApplicationMenuImportResult;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.SApplicationPage;
import org.bonitasoft.engine.business.application.model.impl.SApplicationMenuImpl;
import org.bonitasoft.engine.business.application.model.impl.SApplicationPageImpl;
import org.bonitasoft.engine.business.application.xml.ApplicationMenuNode;
import org.bonitasoft.engine.business.application.xml.ApplicationNode;
import org.bonitasoft.engine.commons.exceptions.SObjectNotFoundException;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMenuNodeConverterTest {

    @Mock
    ApplicationService applicationService;

    @InjectMocks
    private ApplicationMenuNodeConverter converter;

    @Test(expected = IllegalArgumentException.class)
    public void convertNullMenuShouldThrowIllegalArgument() throws Exception {
        converter.toMenu(null);
    }

    @Test
    public void convertMenuShouldConvertAllFields() throws Exception {
        // given:
        final Long applicationPageId = 888L;
        final String displayName = "Readable menu name for display";
        final String token = "application-page-token";
        doReturn(new SApplicationPageImpl(91L, applicationPageId, token)).when(applicationService).getApplicationPage(applicationPageId);

        // when:
        final ApplicationMenuNode convertedMenu = converter.toMenu(new SApplicationMenuImpl(displayName, 147L, applicationPageId, 14));

        // then:
        assertThat(convertedMenu.getApplicationPage()).isEqualTo(token);
        assertThat(convertedMenu.getDisplayName()).isEqualTo(displayName);
    }

    @Test
    public void addMenusToApplicationNodeShouldBeRecursive() throws Exception {
        // given:
        final long applicationId = 333L;
        final Long parentMenuId = null;
        final Long applicationPageId1 = 44L;

        final String displayName1 = "HR";
        final SApplicationMenu sApplicationMenu1 = new SApplicationMenuImpl(displayName1, applicationId, applicationPageId1, 1);
        final List<SApplicationMenu> level1Menus = new ArrayList<SApplicationMenu>();
        level1Menus.add(sApplicationMenu1);

        final List<SApplicationMenu> level2Menus = new ArrayList<SApplicationMenu>();
        final String displayName11 = "Legal HR procedures";
        final SApplicationMenu sApplicationMenu11 = new SApplicationMenuImpl(displayName11, applicationId, null, 1);
        level2Menus.add(sApplicationMenu11);
        final String displayName12 = "HR collective agreement";
        final Long applicationPageId12 = 577L;
        final SApplicationMenu sApplicationMenu12 = new SApplicationMenuImpl(displayName12, applicationId, applicationPageId12, 2);
        level2Menus.add(sApplicationMenu12);

        given(applicationService.searchApplicationMenus(any(QueryOptions.class))).willReturn(level1Menus).willReturn(level2Menus)
                .willReturn(Collections.<SApplicationMenu> emptyList());

        final String token1 = "mytoken-level-1";
        given(applicationService.getApplicationPage(applicationPageId1)).willReturn(new SApplicationPageImpl(applicationId, applicationPageId1, token1));
        final String token12 = "mytoken-level-12";
        given(applicationService.getApplicationPage(applicationPageId12)).willReturn(new SApplicationPageImpl(applicationId, applicationPageId12, token12));

        // when:
        final ApplicationNode node = new ApplicationNode();
        converter.addMenusToApplicationNode(applicationId, parentMenuId, node, null);

        // then:
        assertThat(node.getApplicationMenus().size()).isEqualTo(1);
        final ApplicationMenuNode menuNode1 = node.getApplicationMenus().get(0);
        assertThat(menuNode1.getApplicationPage()).isEqualTo(token1);
        assertThat(menuNode1.getDisplayName()).isEqualTo(displayName1);
        assertThat(menuNode1.getApplicationMenus().size()).isEqualTo(2);
        final ApplicationMenuNode menuNode11 = menuNode1.getApplicationMenus().get(0);
        assertThat(menuNode11.getApplicationPage()).isNull(); // no page linked to that menu
        assertThat(menuNode11.getDisplayName()).isEqualTo(displayName11);
        final ApplicationMenuNode menuNode12 = menuNode1.getApplicationMenus().get(1);
        assertThat(menuNode12.getApplicationPage()).isEqualTo(token12);
        assertThat(menuNode12.getDisplayName()).isEqualTo(displayName12);
    }

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
        given(application.getToken()).willReturn("app");

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
