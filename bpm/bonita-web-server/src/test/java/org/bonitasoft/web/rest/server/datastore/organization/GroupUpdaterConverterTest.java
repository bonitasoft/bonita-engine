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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.Serializable;
import java.util.HashMap;

import org.bonitasoft.console.common.server.utils.BonitaHomeFolderAccessor;
import org.bonitasoft.console.common.server.utils.IconDescriptor;
import org.bonitasoft.engine.identity.GroupUpdater;
import org.bonitasoft.engine.identity.GroupUpdater.GroupField;
import org.bonitasoft.web.rest.model.identity.GroupItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.engineclient.GroupEngineClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Colin PUY
 */
@RunWith(MockitoJUnitRunner.class)
public class GroupUpdaterConverterTest extends APITestWithMock {

    @Mock
    private GroupEngineClient groupEngineClient;
    @Mock
    private BonitaHomeFolderAccessor bonitaHomeFolderAccessor;
    @InjectMocks
    @Spy
    private GroupUpdaterConverter groupUpdaterConverter;

    @Before
    public void init() {
        doReturn(bonitaHomeFolderAccessor).when(groupUpdaterConverter).getBonitaHomeFolderAccessor();
    }

    private Serializable getFieldValue(GroupUpdater groupUpdater, GroupField field) {
        return groupUpdater.getFields().get(field);
    }

    private HashMap<String, String> buildSimpleAttribute(String attributeName, String attributeValue) {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put(attributeName, attributeValue);
        return attributes;
    }

    @Test
    public void convert_return_a_groupUpdater_with_required_fields() throws Exception {
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put(GroupItem.ATTRIBUTE_DESCRIPTION, "aNewDescription");
        attributes.put(GroupItem.ATTRIBUTE_ICON, "aNewIcon");
        attributes.put(GroupItem.ATTRIBUTE_NAME, "aNewName");
        attributes.put(GroupItem.ATTRIBUTE_DISPLAY_NAME, "aNewDisplayName");
        byte[] content = { 1, 2, 3 };
        doReturn(new IconDescriptor("aNewIcon.png", content)).when(bonitaHomeFolderAccessor)
                .getIconFromFileSystem("aNewIcon");

        GroupUpdater updater = groupUpdaterConverter.convert(attributes);

        assertThat(getFieldValue(updater, GroupField.DESCRIPTION)).isEqualTo("aNewDescription");
        assertThat(getFieldValue(updater, GroupField.ICON_FILENAME)).isEqualTo("aNewIcon.png");
        assertThat(getFieldValue(updater, GroupField.ICON_CONTENT)).isEqualTo(content);
        assertThat(getFieldValue(updater, GroupField.NAME)).isEqualTo("aNewName");
        assertThat(getFieldValue(updater, GroupField.DISPLAY_NAME)).isEqualTo("aNewDisplayName");
    }

    @Test
    public void convert_should_skip_empty_icon() throws Exception {
        //given
        HashMap<String, String> attributes = new HashMap<>();
        attributes.put(GroupItem.ATTRIBUTE_ICON, "");
        attributes.put(GroupItem.ATTRIBUTE_NAME, "aNewName");

        //when
        GroupUpdater updater = groupUpdaterConverter.convert(attributes);

        //then
        assertThat(getFieldValue(updater, GroupField.ICON_FILENAME)).isNull();
        assertThat(getFieldValue(updater, GroupField.ICON_CONTENT)).isNull();
        assertThat(getFieldValue(updater, GroupField.NAME)).isEqualTo("aNewName");

        verify(bonitaHomeFolderAccessor, never()).getIconFromFileSystem(anyString());

    }

    @Test
    public void convert_dont_update_name_if_this_is_a_blank_value() throws Exception {
        String unexpectedName = " ";
        HashMap<String, String> attribute = buildSimpleAttribute(GroupItem.ATTRIBUTE_NAME, unexpectedName);

        GroupUpdater updater = groupUpdaterConverter.convert(attribute);

        assertThat(getFieldValue(updater, GroupField.NAME)).isNull();
    }

    @Test
    public void convert_update_parent_path_if_a_parent_group_id_is_specified() throws Exception {
        HashMap<String, String> attributes = buildSimpleAttribute(GroupItem.ATTRIBUTE_PARENT_GROUP_ID, "101");
        when(groupEngineClient.getPath("101")).thenReturn("/Expected/Parent/Path");

        GroupUpdater updater = groupUpdaterConverter.convert(attributes);

        assertThat(getFieldValue(updater, GroupField.PARENT_PATH)).isEqualTo("/Expected/Parent/Path");
    }

    @Test
    public void convert_set_parent_path_to_empty_if_parentGroupId_is_an_empty_string() throws Exception {
        HashMap<String, String> attributes = buildSimpleAttribute(GroupItem.ATTRIBUTE_PARENT_GROUP_ID, "");

        GroupUpdater updater = groupUpdaterConverter.convert(attributes);

        assertThat(getFieldValue(updater, GroupField.PARENT_PATH)).isEqualTo("");
    }

}
