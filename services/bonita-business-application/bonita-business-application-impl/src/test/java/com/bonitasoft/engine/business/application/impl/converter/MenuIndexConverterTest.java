/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.bonitasoft.engine.business.application.impl.MenuIndex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuUpdateBuilderImpl;
import com.bonitasoft.engine.business.application.model.impl.SApplicationMenuImpl;

@RunWith(MockitoJUnitRunner.class)
public class MenuIndexConverterTest {

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private MenuIndexConverter convertor;

    @Test
    public void toMenuIndex_should_return_a_MenuIndex_based_on_ApplicationMenu_and_set_lastUsedIndex() throws Exception {
        //given
        SApplicationMenuImpl appMenu = new SApplicationMenuImpl("my menu", 1, null, 2);
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
    public void toMenuIndex_with_updateDescriptor_should_reuse_app_menu_parentId_when_parent_doesnt_change() throws Exception {
        //given
        SApplicationMenuImpl appMenu = new SApplicationMenuImpl("my menu", 1, null, 2);
        appMenu.setParentId(20L);

        given(applicationService.getLastUsedIndex(appMenu.getParentId())).willReturn(11);

        SApplicationMenuUpdateBuilderImpl builder = new SApplicationMenuUpdateBuilderImpl();
        builder.updateIndex(5);

        //when
        MenuIndex menuIndex = convertor.toMenuIndex(appMenu, builder.done());

        //then
        assertThat(menuIndex).isNotNull();
        assertThat(menuIndex.getValue()).isEqualTo(5);
        assertThat(menuIndex.getParentId()).isEqualTo(20L);
        assertThat(menuIndex.getLastUsedIndex()).isEqualTo(11);
    }

    @Test
    public void toMenuIndex_with_updateDescriptor_should_use_new_parent_when_parent_changes() throws Exception {
        //given
        SApplicationMenuImpl appMenu = new SApplicationMenuImpl("my menu", 1, null, 2);
        appMenu.setParentId(20L);

        SApplicationMenuUpdateBuilderImpl builder = new SApplicationMenuUpdateBuilderImpl();
        builder.updateIndex(5);
        builder.updateParentId(7L);

        given(applicationService.getLastUsedIndex(7L)).willReturn(11);

        //when
        MenuIndex menuIndex = convertor.toMenuIndex(appMenu, builder.done());

        //then
        assertThat(menuIndex).isNotNull();
        assertThat(menuIndex.getValue()).isEqualTo(5);
        assertThat(menuIndex.getParentId()).isEqualTo(7L);
        assertThat(menuIndex.getLastUsedIndex()).isEqualTo(11);
    }

    @Test
    public void toMenuIndex_with_updateDescriptor_should_reuse_app_menu_index_when_index_and_parent_dont_change() throws Exception {
        //given
        SApplicationMenuImpl appMenu = new SApplicationMenuImpl("my menu", 1, null, 2);
        appMenu.setParentId(20L);

        SApplicationMenuUpdateBuilderImpl builder = new SApplicationMenuUpdateBuilderImpl();
        builder.updateParentId(4L);

        given(applicationService.getLastUsedIndex(4L)).willReturn(11);

        //when
        MenuIndex menuIndex = convertor.toMenuIndex(appMenu, builder.done());

        //then
        assertThat(menuIndex).isNotNull();
        assertThat(menuIndex.getValue()).isEqualTo(2);
        assertThat(menuIndex.getParentId()).isEqualTo(4L);
        assertThat(menuIndex.getLastUsedIndex()).isEqualTo(11);
    }

}
