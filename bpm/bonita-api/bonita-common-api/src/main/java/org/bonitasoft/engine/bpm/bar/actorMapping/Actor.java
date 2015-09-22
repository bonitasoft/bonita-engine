/**
 * Copyright (C) 2015 Bonitasoft S.A.
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

package org.bonitasoft.engine.bpm.bar.actorMapping;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Actor implements Serializable {

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Membership implements Serializable {

        private String role;

        private String group;

        public Membership() {
        }

        public Membership(String group, String role) {
            this.group = group;
            this.role = role;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            Membership that = (Membership) o;
            return Objects.equals(role, that.role) &&
                    Objects.equals(group, that.group);
        }

        @Override
        public int hashCode() {
            return Objects.hash(role, group);
        }
    }

    @XmlAttribute
    private String name;
    @XmlAttribute
    private String description;
    @XmlElementWrapper(name = "users", required = false)
    @XmlElement(name = "user")
    private Set<String> users = new HashSet<>();;
    @XmlElementWrapper(name = "groups", required = false)
    @XmlElement(name = "group")
    private Set<String> groups = new HashSet<>();
    @XmlElementWrapper(name = "roles", required = false)
    @XmlElement(name = "role")
    private Set<String> roles = new HashSet<>();
    @XmlElementWrapper(name = "memberships", required = false)
    @XmlElement(name = "membership")
    private Set<Membership> memberships = null;

    public Actor(final String name) {
        this.name = name;
        memberships = new HashSet<>();
    }

    public Actor() {
        memberships = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public void addGroup(final String group) {
        groups.add(group);
    }

    public void addGroups(final List<String> groups) {
        this.groups = new HashSet<>(groups);
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void addRole(final String role) {
        roles.add(role);
    }

    public void addRoles(final List<String> roles) {
        this.roles = new HashSet<>(roles);
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void addUser(final String user) {
        users.add(user);
    }

    public void addUsers(final List<String> userNames) {
        users = new HashSet<>(userNames);
    }

    public Set<String> getUsers() {
        return users;
    }

    public void addMembership(final String groupName, final String roleName) {
        memberships.add(new Membership(groupName, roleName));
    }

    public Set<Membership> getMemberships() {
        return memberships;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Actor actor = (Actor) o;
        return Objects.equals(name, actor.name) &&
                Objects.equals(description, actor.description) &&
                Objects.equals(users, actor.users) &&
                Objects.equals(groups, actor.groups) &&
                Objects.equals(roles, actor.roles) &&
                Objects.equals(memberships, actor.memberships);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, users, groups, roles, memberships);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("description", description)
                .append("users", users)
                .append("groups", groups)
                .append("roles", roles)
                .append("memberships", memberships)
                .toString();
    }
}
