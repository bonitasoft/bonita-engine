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
package org.bonitasoft.engine.identity.xml;

import java.util.List;

import org.bonitasoft.engine.identity.CustomUserInfoDefinitionCreator;
import org.bonitasoft.engine.identity.ExportedUser;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.RoleCreator;
import org.bonitasoft.engine.identity.UserMembership;

/**
 * @author Baptiste Mesta
 * @author Frederic Bouquet
 * @author Matthieu Chaffotte
 */
public class OrganizationCreator {

    private final List<CustomUserInfoDefinitionCreator> customUserInfoDefinitionCreators;
    
    private final List<ExportedUser> users;

    private final List<RoleCreator> roles;

    private final List<GroupCreator> groups;

    private final List<UserMembership> memberships;

    public OrganizationCreator(final List<ExportedUser> users, final List<RoleCreator> roles, final List<GroupCreator> groups, final List<UserMembership> memberships,
            List<CustomUserInfoDefinitionCreator> customUserInfoDefinitionCreators) {
        this.users = users;
        this.roles = roles;
        this.groups = groups;
        this.memberships = memberships;
        this.customUserInfoDefinitionCreators = customUserInfoDefinitionCreators;
    }
    
    public List<CustomUserInfoDefinitionCreator> getCustomUserInfoDefinitionCreators() {
        return customUserInfoDefinitionCreators;
    }

    public List<ExportedUser> getUsers() {
        return users;
    }

    public List<RoleCreator> getRoleCreators() {
        return roles;
    }

    public List<GroupCreator> getGroupCreators() {
        return groups;
    }

    public List<UserMembership> getMemberships() {
        return memberships;
    }

}
