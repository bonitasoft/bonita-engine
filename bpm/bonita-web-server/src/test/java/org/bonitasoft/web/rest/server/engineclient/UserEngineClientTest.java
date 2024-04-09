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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.identity.UserUpdater;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Colin PUY
 */
public class UserEngineClientTest extends APITestWithMock {

    @Mock
    private IdentityAPI identityAPI;

    private UserEngineClient userEngineClient;

    @Before
    public void init() {
        initMocks(this);
        userEngineClient = new UserEngineClient(identityAPI);
    }

    @Test
    public void update_update_a_user_in_engine_repository() throws Exception {
        UserUpdater userUpdater = new UserUpdater();

        userEngineClient.update(1L, userUpdater);

        verify(identityAPI).updateUser(1L, userUpdater);
    }

    @Test(expected = APIException.class)
    public void update_throw_APIException_if_user_is_not_found_in_engine_repository() throws Exception {
        UserUpdater userUpdater = new UserUpdater();
        when(identityAPI.updateUser(1L, userUpdater)).thenThrow(new UserNotFoundException("aMessage"));

        userEngineClient.update(1L, userUpdater);
    }

    @Test(expected = APIException.class)
    public void update_throw_APIException_if_exception_occur_when_updating_user_in_engine_repository()
            throws Exception {
        UserUpdater userUpdater = new UserUpdater();
        when(identityAPI.updateUser(1L, userUpdater)).thenThrow(new UpdateException("aMessage"));

        userEngineClient.update(1L, userUpdater);
    }

    @Test
    public void create_create_a_user_in_engine_repository() throws Exception {
        UserCreator userCreator = new UserCreator("aName", "aPassword");

        userEngineClient.create(userCreator);

        verify(identityAPI).createUser(userCreator);
    }

    @Test(expected = APIException.class)
    public void create_throw_APIException_if_exception_occur_when_creating_user_in_engine_repository()
            throws Exception {
        UserCreator userCreator = new UserCreator("aName", "aPassword");
        when(identityAPI.createUser(userCreator)).thenThrow(new CreationException("aMessage"));

        userEngineClient.create(userCreator);
    }

    @Test
    public void get_fetch_a_user_from_engine_repository() throws Exception {

        userEngineClient.get(1L);

        verify(identityAPI).getUser(1L);
    }

    @Test(expected = APIException.class)
    public void get_throw_APIException_if_user_is_not_found_in_engine_repository() throws Exception {
        when(identityAPI.getUser(1L)).thenThrow(new UserNotFoundException("aMessage"));

        userEngineClient.get(1L);
    }

    @Test
    public void delete_delete_users_in_engine_repository() throws Exception {
        List<Long> idsToBeDeleted = asList(1L, 2L);

        userEngineClient.delete(idsToBeDeleted);

        verify(identityAPI).deleteUsers(idsToBeDeleted);
    }

    @Test(expected = APIException.class)
    public void delete_throw_APIException_if_exception_occur_when_deleting_users_in_engine_repository()
            throws Exception {
        List<Long> idsToBeDeleted = asList(1L, 2L);
        doThrow(new DeletionException("aMessage")).when(identityAPI).deleteUsers(idsToBeDeleted);

        userEngineClient.delete(idsToBeDeleted);
    }
}
