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
package org.bonitasoft.engine.api.impl.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.business.application.ApplicationMenu;
import org.bonitasoft.engine.business.application.ApplicationMenuCreator;
import org.bonitasoft.engine.business.application.ApplicationMenuUpdater;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuFields;
import org.bonitasoft.engine.business.application.model.impl.SApplicationMenuImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationMenuModelConverterTest {


    private ApplicationMenuModelConverter convertor;

    @Before
    public void setUp() throws Exception {
        convertor = new ApplicationMenuModelConverter();
    }

    @Test
    public void buildSApplicationMenu_should_map_all_creator_fields_and_add_index() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(4L, "main", 20L);
        creator.setParentId(11);

        //when
        final SApplicationMenu menu = convertor.buildSApplicationMenu(creator, 1);

        //then
        assertThat(menu).isNotNull();
        assertThat(menu.getDisplayName()).isEqualTo("main");
        assertThat(menu.getApplicationId()).isEqualTo(4);
        assertThat(menu.getApplicationPageId()).isEqualTo(20);
        assertThat(menu.getIndex()).isEqualTo(1);
        assertThat(menu.getParentId()).isEqualTo(11);
    }

    @Test
    public void buildSApplicationMenu_should_have_null_parentId_when_not_set_on_creator() throws Exception {
        //given
        final ApplicationMenuCreator creator = new ApplicationMenuCreator(4L, "main", 20L);

        //when
        final SApplicationMenu menu = convertor.buildSApplicationMenu(creator, 1);

        //then
        assertThat(menu).isNotNull();
        assertThat(menu.getParentId()).isNull();
    }

    @Test
    public void toApplicationMenu_should_map_all_server_object_fields_and_set_application_id() throws Exception {
        //given
        final long appPageId = 15;
        final long applicationId = 18;
        final SApplicationMenuImpl sMenu = new SApplicationMenuImpl("main", applicationId, appPageId, 1);
        sMenu.setId(3);
        sMenu.setParentId(21L);

        //when
        final ApplicationMenu menu = convertor.toApplicationMenu(sMenu);

        //then
        assertThat(menu).isNotNull();
        assertThat(menu.getId()).isEqualTo(3);
        assertThat(menu.getDisplayName()).isEqualTo("main");
        assertThat(menu.getApplicationPageId()).isEqualTo(15);
        assertThat(menu.getIndex()).isEqualTo(1);
        assertThat(menu.getParentId()).isEqualTo(21);
        assertThat(menu.getApplicationId()).isEqualTo(applicationId);
    }

    @Test
    public void toApplicationMenu_list_should_call_toApplitionMenu_for_each_element_in_the_list_and_return_the_list_of_converted_values() throws Exception {
        //given
        final SApplicationMenu sMenu1 = mock(SApplicationMenu.class);
        final SApplicationMenu sMenu2 = mock(SApplicationMenu.class);
        final ApplicationMenu menu1 = mock(ApplicationMenu.class);
        final ApplicationMenu menu2 = mock(ApplicationMenu.class);
        final ApplicationMenuModelConverter convertorMock = spy(convertor);
        doReturn(menu1).when(convertorMock).toApplicationMenu(sMenu1);
        doReturn(menu2).when(convertorMock).toApplicationMenu(sMenu2);

        //when
        final List<ApplicationMenu> applicationMenus = convertorMock.toApplicationMenu(Arrays.asList(sMenu1, sMenu2));

        //then
        assertThat(applicationMenus).containsExactly(menu1, menu2);
    }

    @Test
    public void toApplicationMenuUpdateDescriptor_should_map_all_fields() throws Exception {
        ApplicationMenuUpdater updater = new ApplicationMenuUpdater();
        updater.setApplicationPageId(1L);
        updater.setDisplayName("disp");
        updater.setIndex(2);
        updater.setParentId(3L);

        EntityUpdateDescriptor updateDescriptor = convertor.toApplicationMenuUpdateDescriptor(updater);
        assertThat(updateDescriptor).isNotNull();
        assertThat(updateDescriptor.getFields()).hasSize(4);
        assertThat(updateDescriptor.getFields().get(SApplicationMenuFields.APPLICATION_PAGE_ID)).isEqualTo(1L);
        assertThat(updateDescriptor.getFields().get(SApplicationMenuFields.DISPLAY_NAME)).isEqualTo("disp");
        assertThat(updateDescriptor.getFields().get(SApplicationMenuFields.INDEX)).isEqualTo(2);
        assertThat(updateDescriptor.getFields().get(SApplicationMenuFields.PARENT_ID)).isEqualTo(3L);

    }
}
