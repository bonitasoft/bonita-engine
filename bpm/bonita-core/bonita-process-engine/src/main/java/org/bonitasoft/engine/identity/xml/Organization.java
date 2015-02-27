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

import org.bonitasoft.engine.identity.ExportedUser;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;

/**
 * @author Elias Ricken de Medeiros
 */
public class Organization {

    private final List<SCustomUserInfoDefinition> customUserInfoDefinitions;
    
    private final List<ExportedUser> users;

    private final List<Role> roles;

    private final List<Group> groups;

    private final List<UserMembership> memberships;

    public Organization(final List<ExportedUser> users, final List<Role> roles, final List<Group> groups, final List<UserMembership> memberships,
            List<SCustomUserInfoDefinition> customUserInfoDefinitions) {
        this.users = users;
        this.roles = roles;
        this.groups = groups;
        this.memberships = memberships;
        this.customUserInfoDefinitions = customUserInfoDefinitions;
    }
    
    public List<SCustomUserInfoDefinition> getCustomUserInfoDefinitions() {
        return customUserInfoDefinitions;
    }

    public List<ExportedUser> getUsers() {
        return users;
    }

    public List<Role> getRole() {
        return roles;
    }

    public List<Group> getGroup() {
        return groups;
    }

    public List<UserMembership> getMemberships() {
        return memberships;
    }

}
