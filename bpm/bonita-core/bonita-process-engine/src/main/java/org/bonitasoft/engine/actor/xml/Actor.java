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
package org.bonitasoft.engine.actor.xml;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.bpm.bar.xml.XMLProcessDefinition.BEntry;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class Actor {

    private final String name;

    private String description;

    private Set<String> users;

    private Set<String> groups;

    private Set<String> roles;

    private final Set<BEntry<String, String>> memberships;

    public Actor(final String name) {
        this.name = name;
        users = new HashSet<String>();
        groups = new HashSet<String>();
        roles = new HashSet<String>();
        memberships = new HashSet<BEntry<String, String>>();
    }

    public String getName() {
        return name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void addGroup(final String group) {
        groups.add(group);
    }

    public void addGroups(final List<String> groups) {
        this.groups = new HashSet<String>(groups);
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void addRole(final String role) {
        roles.add(role);
    }

    public void addRoles(final List<String> roles) {
        this.roles = new HashSet<String>(roles);
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void addUser(final String user) {
        users.add(user);
    }

    public void addUsers(final List<String> userNames) {
        users = new HashSet<String>(userNames);
    }

    public Set<String> getUsers() {
        return users;
    }

    public void addMembership(final String groupName, final String roleName) {
        memberships.add(new BEntry<String, String>(groupName, roleName));
    }

    public Set<BEntry<String, String>> getMemberships() {
        return memberships;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (groups == null ? 0 : groups.hashCode());
        result = prime * result + (memberships == null ? 0 : memberships.hashCode());
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (roles == null ? 0 : roles.hashCode());
        result = prime * result + (users == null ? 0 : users.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Actor other = (Actor) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (groups == null) {
            if (other.groups != null) {
                return false;
            }
        } else if (!groups.equals(other.groups)) {
            return false;
        }
        if (memberships == null) {
            if (other.memberships != null) {
                return false;
            }
        } else if (!memberships.equals(other.memberships)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (roles == null) {
            if (other.roles != null) {
                return false;
            }
        } else if (!roles.equals(other.roles)) {
            return false;
        }
        if (users == null) {
            if (other.users != null) {
                return false;
            }
        } else if (!users.equals(other.users)) {
            return false;
        }
        return true;
    }

}
