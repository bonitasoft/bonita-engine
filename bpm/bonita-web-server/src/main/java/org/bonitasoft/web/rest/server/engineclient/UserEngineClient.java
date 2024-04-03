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

import java.util.List;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.identity.UserCreator.UserField;
import org.bonitasoft.engine.identity.UserNotFoundException;
import org.bonitasoft.engine.identity.UserUpdater;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APINotFoundException;
import org.bonitasoft.web.toolkit.client.common.i18n.T_;
import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;

public class UserEngineClient {

    private final IdentityAPI identityAPI;

    public UserEngineClient(IdentityAPI identityAPI) {
        this.identityAPI = identityAPI;
    }

    public User update(long userId, UserUpdater userUpdater) {
        try {
            return identityAPI.updateUser(userId, userUpdater);
        } catch (UserNotFoundException e) {
            throw new APINotFoundException(new T_("Can't update user. User not found"), e);
        } catch (UpdateException e) {
            throw new APIException(new T_("Error when updating user"), e);
        }
    }

    public User create(UserCreator creator) {
        try {
            return identityAPI.createUser(creator);
        } catch (AlreadyExistsException e) {
            throw new APIForbiddenException(new T_("Can't create user. User '%userName%' already exists",
                    new Arg("userName", creator.getFields().get(UserField.NAME))), e);
        } catch (CreationException e) {
            throw new APIException(new T_("Error when creating user"), e);
        }
    }

    public User get(long userId) {
        try {
            return identityAPI.getUser(userId);
        } catch (UserNotFoundException e) {
            throw new APINotFoundException(new T_("User not found"), e);
        }
    }

    public void delete(List<Long> userIds) {
        try {
            identityAPI.deleteUsers(userIds);
        } catch (DeletionException e) {
            throw new APIException(new T_("Error when deleting users"), e);
        }
    }

    public SearchResult<User> search(SearchOptions searchOptions) {
        try {
            return identityAPI.searchUsers(searchOptions);
        } catch (SearchException e) {
            throw new APIException(new T_("Error when searching users"), e);
        }
    }

}
