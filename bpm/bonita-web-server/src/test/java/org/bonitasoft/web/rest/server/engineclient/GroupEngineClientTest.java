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
package org.bonitasoft.web.rest.server.engineclient;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import org.bonitasoft.engine.api.GroupAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.GroupNotFoundException;
import org.bonitasoft.engine.identity.GroupUpdater;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Colin PUY
 */
public class GroupEngineClientTest extends APITestWithMock {

    @Mock
    private GroupAPI groupAPI;

    private GroupEngineClient groupEngineClient;

    @Before
    public void init() {
        initMocks(this);
        groupEngineClient = new GroupEngineClient(groupAPI);
    }

    @Test
    public void get_get_a_group_in_engine_repository() throws Exception {

        groupEngineClient.get(1L);

        verify(groupAPI).getGroup(1L);
    }

    @Test(expected = APIException.class)
    public void get_throw_APIException_if_group_is_not_found_in_engine_repository() throws Exception {
        when(groupAPI.getGroup(1L)).thenThrow(new GroupNotFoundException(new Exception()));

        groupEngineClient.get(1L);
    }

    @Test
    public void delete_delete_groups_in_engine_repository() throws Exception {
        List<Long> groupIds = asList(1L, 2L);

        groupEngineClient.delete(groupIds);

        verify(groupAPI).deleteGroups(groupIds);
    }

    @Test(expected = APIException.class)
    public void delete_throw_APIException_if_an_error_occurs_when_deleting_groups_in_engine_repository()
            throws Exception {
        List<Long> groupIds = asList(1L, 2L);
        doThrow(new DeletionException("error")).when(groupAPI).deleteGroups(groupIds);

        groupEngineClient.delete(groupIds);
    }

    @Test
    public void update_update_a_group_in_engine_repository() throws Exception {
        GroupUpdater groupUpdater = new GroupUpdater();

        groupEngineClient.update(1L, groupUpdater);

        verify(groupAPI).updateGroup(1L, groupUpdater);
    }

    @Test(expected = APIException.class)
    public void update_throw_APIException_if_group_not_exists_in_engine_repository() throws Exception {
        when(groupAPI.updateGroup(eq(1L), any(GroupUpdater.class)))
                .thenThrow(new GroupNotFoundException(new Exception()));

        groupEngineClient.update(1L, new GroupUpdater());
    }

    @Test(expected = APIException.class)
    public void update_throw_APIException_if_an_exception_occurs_when_updating_in_engine_repository() throws Exception {
        when(groupAPI.updateGroup(eq(1L), any(GroupUpdater.class))).thenThrow(new UpdateException(""));

        groupEngineClient.update(1L, new GroupUpdater());
    }

    @Test(expected = APIException.class)
    public void update_throw_APIException_if_an_exception_occurs_when_updating_with_name_alreayExist()
            throws Exception {
        when(groupAPI.updateGroup(eq(1L), any(GroupUpdater.class))).thenThrow(new AlreadyExistsException(""));

        groupEngineClient.update(1L, new GroupUpdater());
    }

    @Test(expected = APIException.class)
    public void getPath_throw_APIexception_if_groupId_is_not_a_number() throws Exception {
        groupEngineClient.getPath("notANumber");
    }

    @Test
    public void getPath_return_the_group_path_for_the_specified_group_id() throws Exception {
        Group group = mock(Group.class);
        when(group.getPath()).thenReturn("/expected/group/path");
        when(groupAPI.getGroup(1L)).thenReturn(group);

        String groupPath = groupEngineClient.getPath("1");

        assertThat(groupPath, is("/expected/group/path"));
    }

    @Test
    public void create_create_a_group_in_engine_repository() throws Exception {
        GroupCreator creator = new GroupCreator("aName");

        groupEngineClient.create(creator);

        verify(groupAPI).createGroup(creator);
    }
}
