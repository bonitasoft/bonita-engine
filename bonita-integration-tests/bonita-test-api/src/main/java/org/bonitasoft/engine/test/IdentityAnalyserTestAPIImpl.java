/*******************************************************************************
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
 ******************************************************************************/

package org.bonitasoft.engine.test;

import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.identity.UserMembership;

/**
 * @author mazourd
 */

public class IdentityAnalyserTestAPIImpl implements IdentityAnalyserTestAPI {

    public IdentityAnalyserTestAPIImpl(IdentityAPI identityAPI) {
        this.identityAPI = identityAPI;
    }

    private IdentityAPI getIdentityAPI() {
        return identityAPI;
    }

    private IdentityAPI identityAPI;

    @Override
    public User createUser(final String userName, final String password, final long managerId) throws BonitaException {
        final UserCreator creator = new UserCreator(userName, password);
        creator.setManagerUserId(managerId);
        return getIdentityAPI().createUser(creator);
    }

    @Override
    public void deleteUsers(final User... users) throws BonitaException {
        if (users != null) {
            for (final User user : users) {
                getIdentityAPI().deleteUser(user.getId());
            }
        }
    }

    @Override
    public void deleteRoles(final Role... roles) throws BonitaException {
        if (roles != null) {
            for (final Role role : roles) {
                getIdentityAPI().deleteRole(role.getId());
            }
        }
    }

    @Override
    public void deleteGroups(final Group... groups) throws BonitaException {
        if (groups != null) {
            for (final Group group : groups) {
                getIdentityAPI().deleteGroup(group.getId());
            }
        }
    }

    @Override
    public UserMembership createUserMembership(final String userName, final String roleName, final String groupName) throws BonitaException {
        return getIdentityAPI().addUserMembership(getIdentityAPI().getUserByUserName(userName).getId(), getIdentityAPI().getGroupByPath(groupName).getId(),
                getIdentityAPI().getRoleByName(roleName).getId());
    }

}
