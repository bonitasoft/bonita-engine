/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import java.util.Collections;

import org.bonitasoft.console.common.server.i18n.I18n;
import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.IconDescriptor;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.identity.RoleCreator;
import org.bonitasoft.engine.identity.RoleUpdater;
import org.bonitasoft.engine.identity.impl.RoleImpl;
import org.bonitasoft.web.rest.model.ModelFactory;
import org.bonitasoft.web.rest.model.identity.RoleItem;
import org.bonitasoft.web.toolkit.client.ItemDefinitionFactory;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Item;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta
 */
@RunWith(MockitoJUnitRunner.class)
public class RoleDatastoreTest {

    @InjectMocks
    @Spy
    private RoleDatastore roleDatastore;
    @Mock
    private IdentityAPI identityAPI;
    @Mock
    private BonitaHomeFolderAccessor bonitaHomeFolderAccessor;
    @Captor
    private ArgumentCaptor<RoleUpdater> roleUpdaterArgumentCaptor;
    @Captor
    private ArgumentCaptor<RoleCreator> roleCreatorArgumentCaptor;

    @Before
    public void before() throws Exception {
        ItemDefinitionFactory.setDefaultFactory(new ModelFactory());
        I18n.getInstance();
        Item.setApplyInputModifiersByDefault(false);
        Item.setApplyValidatorsByDefault(false);
        Item.setApplyOutputModifiersByDefault(false);
        Item.setApplyValidatorMandatoryByDefault(false);
        doReturn(identityAPI).when(roleDatastore).getIdentityAPI();
        doReturn(bonitaHomeFolderAccessor).when(roleDatastore).getBonitaHomeFolderAccessor();
    }

    @Test
    public void should_get_retrieve_role_with_icon() throws Exception {
        //given
        RoleImpl myRole = new RoleImpl(12L, "myRole");
        myRole.setIconId(2134L);
        doReturn(myRole).when(identityAPI).getRole(12L);
        //when
        RoleItem roleItem = roleDatastore.get(APIID.makeAPIID(12L));
        //then
        assertThat(roleItem.getIcon()).isEqualTo("../API/avatars/2134");
    }

    @Test
    public void should_get_retrieve_role_without_icon() throws Exception {
        //given
        RoleImpl myRole = new RoleImpl(12L, "myRole");
        doReturn(myRole).when(identityAPI).getRole(12L);
        //when
        RoleItem roleItem = roleDatastore.get(APIID.makeAPIID(12L));
        //then
        assertThat(roleItem.getIcon()).isEmpty();
    }

    @Test
    public void should_retrieve_role_from_engine() throws Exception {
        //given
        doReturn(new RoleImpl(123, "myRole")).when(identityAPI).getRole(123L);
        //when
        RoleItem roleItem = roleDatastore.get(APIID.makeAPIID(123L));
        //then
        assertThat(roleItem.getName()).isEqualTo("myRole");
    }

    @Test
    public void should_update_role_in_the_engine() throws Exception {
        RoleUpdater roleUpdater = new RoleUpdater().setName("newName");
        doReturn(new RoleImpl(123, "newName")).when(identityAPI).updateRole(eq(123L), eq(roleUpdater));
        //when
        roleDatastore.update(APIID.makeAPIID(123L), Collections.singletonMap("name", "newName"));
        //then
        verify(identityAPI).updateRole(eq(123L), eq(roleUpdater));
    }

    @Test
    public void should_update_icon_of_role_give_content_to_engine() throws Exception {
        doReturn(new RoleImpl(123, "newName")).when(identityAPI).updateRole(anyLong(), any(RoleUpdater.class));
        IconDescriptor iconDescriptor = new IconDescriptor("iconName", "content".getBytes());
        doReturn(iconDescriptor).when(bonitaHomeFolderAccessor).getIconFromFileSystem(eq("temp_icon_on_fs"));
        //when
        roleDatastore.update(APIID.makeAPIID(123L), Collections.singletonMap("icon", "temp_icon_on_fs"));
        //then
        verify(identityAPI).updateRole(eq(123L), roleUpdaterArgumentCaptor.capture());
        RoleUpdater roleUpdater = roleUpdaterArgumentCaptor.getValue();
        assertThat(roleUpdater.getFields().get(RoleUpdater.RoleField.ICON_FILENAME)).isEqualTo("iconName");
        assertThat(roleUpdater.getFields().get(RoleUpdater.RoleField.ICON_CONTENT)).isEqualTo("content".getBytes());
    }

    @Test
    public void should_update_role_with_empty_icon() throws Exception {
        //given
        doReturn(new RoleImpl(123, "newName")).when(identityAPI).updateRole(anyLong(), any(RoleUpdater.class));

        //when
        roleDatastore.update(APIID.makeAPIID(123L), Collections.singletonMap("icon", ""));

        //then
        verify(identityAPI).updateRole(eq(123L), roleUpdaterArgumentCaptor.capture());
        RoleUpdater roleUpdater = roleUpdaterArgumentCaptor.getValue();
        assertThat(roleUpdater.getFields().get(RoleUpdater.RoleField.ICON_FILENAME)).isNull();
        assertThat(roleUpdater.getFields().get(RoleUpdater.RoleField.ICON_CONTENT)).isNull();

        verify(bonitaHomeFolderAccessor, never()).getIconFromFileSystem(anyString());
    }

    @Test
    public void should_add_role_with_icon_give_content_to_engine() throws Exception {
        doReturn(new RoleImpl(123, "newName")).when(identityAPI).createRole(any(RoleCreator.class));
        IconDescriptor iconDescriptor = new IconDescriptor("iconName", "content".getBytes());
        doReturn(iconDescriptor).when(bonitaHomeFolderAccessor).getIconFromFileSystem(eq("temp_icon_on_fs"));
        RoleItem roleItem = new RoleItem();
        roleItem.setIcon("temp_icon_on_fs");
        roleItem.setName("name");
        //when
        roleDatastore.add(roleItem);
        //then
        verify(identityAPI).createRole(roleCreatorArgumentCaptor.capture());
        RoleCreator roleUpdater = roleCreatorArgumentCaptor.getValue();
        assertThat(roleUpdater.getFields().get(RoleCreator.RoleField.ICON_FILENAME)).isEqualTo("iconName");
        assertThat(roleUpdater.getFields().get(RoleCreator.RoleField.ICON_CONTENT)).isEqualTo("content".getBytes());
        assertThat(roleUpdater.getFields().get(RoleCreator.RoleField.NAME)).isEqualTo("name");
    }

    @Test
    public void add_role_without_icon_should_create_role_without_retrieving_icon_from_filesystem() throws Exception {
        doReturn(new RoleImpl(123, "name")).when(identityAPI).createRole(any(RoleCreator.class));
        RoleItem roleItem = new RoleItem();
        roleItem.setIcon("");
        roleItem.setName("name");
        //when
        roleDatastore.add(roleItem);
        //then
        verify(bonitaHomeFolderAccessor, never()).getIconFromFileSystem(anyString());
        verify(identityAPI).createRole(any(RoleCreator.class));
    }

}
