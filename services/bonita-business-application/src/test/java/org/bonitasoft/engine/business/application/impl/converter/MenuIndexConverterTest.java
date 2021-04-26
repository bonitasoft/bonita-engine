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
package org.bonitasoft.engine.business.application.impl.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.impl.MenuIndex;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuUpdateBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MenuIndexConverterTest {

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private MenuIndexConverter convertor;

    @Test
    public void toMenuIndex_should_return_a_MenuIndex_based_on_ApplicationMenu_and_set_lastUsedIndex()
            throws Exception {
        //given
        SApplicationMenu appMenu = new SApplicationMenu("my menu", 1, null, 2);
        appMenu.setParentId(20L);
        given(applicationService.getLastUsedIndex(appMenu.getParentId())).willReturn(11);

        //when
        MenuIndex menuIndex = convertor.toMenuIndex(appMenu);

        //then
        assertThat(menuIndex).isNotNull();
        assertThat(menuIndex.getValue()).isEqualTo(2);
        assertThat(menuIndex.getParentId()).isEqualTo(20L);
        assertThat(menuIndex.getLastUsedIndex()).isEqualTo(11);
    }

    @Test
    public void toMenuIndex_with_updateDescriptor_should_reuse_app_menu_parentId_when_parent_doesnt_change()
            throws Exception {
        //given
        SApplicationMenu appMenu = new SApplicationMenu("my menu", 1, null, 2);
        appMenu.setParentId(20L);

        given(applicationService.getLastUsedIndex(appMenu.getParentId())).willReturn(11);

        //when
        MenuIndex menuIndex = convertor.toMenuIndex(appMenu, new SApplicationMenuUpdateBuilder().updateIndex(5).done());

        //then
        assertThat(menuIndex).isNotNull();
        assertThat(menuIndex.getValue()).isEqualTo(5);
        assertThat(menuIndex.getParentId()).isEqualTo(20L);
        assertThat(menuIndex.getLastUsedIndex()).isEqualTo(11);
    }

    @Test
    public void toMenuIndex_with_updateDescriptor_should_use_new_parent_when_parent_changes() throws Exception {
        //given
        SApplicationMenu appMenu = new SApplicationMenu("my menu", 1, null, 2);
        appMenu.setParentId(20L);

        given(applicationService.getLastUsedIndex(7L)).willReturn(11);

        //when
        MenuIndex menuIndex = convertor.toMenuIndex(appMenu,
                new SApplicationMenuUpdateBuilder().updateIndex(5).updateParentId(7L).done());

        //then
        assertThat(menuIndex).isNotNull();
        assertThat(menuIndex.getValue()).isEqualTo(5);
        assertThat(menuIndex.getParentId()).isEqualTo(7L);
        assertThat(menuIndex.getLastUsedIndex()).isEqualTo(11);
    }

    @Test
    public void toMenuIndex_with_updateDescriptor_should_reuse_app_menu_index_when_index_and_parent_dont_change()
            throws Exception {
        //given
        SApplicationMenu appMenu = new SApplicationMenu("my menu", 1, null, 2);
        appMenu.setParentId(20L);

        given(applicationService.getLastUsedIndex(4L)).willReturn(11);

        //when
        MenuIndex menuIndex = convertor.toMenuIndex(appMenu,
                new SApplicationMenuUpdateBuilder().updateParentId(4L).done());

        //then
        assertThat(menuIndex).isNotNull();
        assertThat(menuIndex.getValue()).isEqualTo(2);
        assertThat(menuIndex.getParentId()).isEqualTo(4L);
        assertThat(menuIndex.getLastUsedIndex()).isEqualTo(11);
    }

}
