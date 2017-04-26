/*
 * Copyright (C) 2017 Bonitasoft S.A.
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
 */
package org.bonitasoft.engine.profile.xml;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {})
public class ProfileMappingNode {

    @XmlElementWrapper(name = "users")
    @XmlElement(name = "user")
    private List<String> users;
    @XmlElementWrapper(name = "groups")
    @XmlElement(name = "group")
    private List<String> groups;
    @XmlElementWrapper(name = "memberships")
    @XmlElement(name = "membership")
    private List<MembershipNode> memberships;
    @XmlElementWrapper(name = "roles")
    @XmlElement(name = "role")
    private List<String> roles;

    public ProfileMappingNode() {
        users = new ArrayList<>();
        groups = new ArrayList<>();
        roles = new ArrayList<>();
        memberships = new ArrayList<>();
    }

    public List<String> getUsers() {
        return users == null ? Collections.emptyList() : users;
    }

    public void setUsers(final List<String> users) {
        this.users = users;
    }

    public List<String> getGroups() {
        return groups == null ? Collections.emptyList() : groups;
    }

    public void setGroups(final List<String> groups) {
        this.groups = groups;
    }

    public List<String> getRoles() {
        return roles == null ? Collections.emptyList() : roles;
    }

    public void setRoles(final List<String> roles) {
        this.roles = roles;
    }

    public List<MembershipNode> getMemberships() {
        return memberships == null ? Collections.emptyList() : memberships;
    }

    public void setMemberships(final List<MembershipNode> memberships) {
        this.memberships = memberships;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProfileMappingNode that = (ProfileMappingNode) o;
        return Objects.equals(users, that.users) &&
                Objects.equals(groups, that.groups) &&
                Objects.equals(roles, that.roles) &&
                Objects.equals(memberships, that.memberships);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users, groups, roles, memberships);
    }

    @Override
    public String toString() {
        return "ProfileMappingNode{" +
                "users=" + users +
                ", groups=" + groups +
                ", roles=" + roles +
                ", memberships=" + memberships +
                '}';
    }
}
