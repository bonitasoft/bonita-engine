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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Baptiste Mesta
 * @author Frederic Bouquet
 * @author Matthieu Chaffotte
 */

@XmlRootElement(name = "Organization")
@XmlAccessorType(XmlAccessType.FIELD)
public class Organization {

    @XmlElementWrapper(name = "customUserInfoDefinitions")
    @XmlElement(name = "customUserInfoDefinition")
    private final List<ExportedCustomUserInfoDefinition> customUserInfoDefinition;

    @XmlElementWrapper(name = "users")
    @XmlElement(name = "user")
    private final List<ExportedUser> users;

    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "role")
    private final List<ExportedRole> roles;

    @XmlElementWrapper(name = "groups")
    @XmlElement(name = "group")
    private final List<ExportedGroup> groups;

    @XmlElementWrapper(name = "memberships")
    @XmlElement(name = "membership")
    private final List<ExportedUserMembership> memberships;

    public Organization() {
        this.users = new ArrayList<>();
        this.roles = new ArrayList<>();
        this.groups = new ArrayList<>();
        this.memberships = new ArrayList<>();
        this.customUserInfoDefinition = new ArrayList<>();
    }

    public Organization(final List<ExportedUser> users, final List<ExportedRole> roles, final List<ExportedGroup> groups,
            final List<ExportedUserMembership> memberships,
            List<ExportedCustomUserInfoDefinition> customUserInfoDefinition) {
        this.users = users;
        this.roles = roles;
        this.groups = groups;
        this.memberships = memberships;
        this.customUserInfoDefinition = customUserInfoDefinition;
    }

    public List<ExportedCustomUserInfoDefinition> getCustomUserInfoDefinition() {
        return customUserInfoDefinition;
    }

    public List<ExportedUser> getUsers() {
        return users;
    }

    public List<ExportedRole> getRoles() {
        return roles;
    }

    public List<ExportedGroup> getGroups() {
        return groups;
    }

    public List<ExportedUserMembership> getMemberships() {
        return memberships;
    }

}
