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
package org.bonitasoft.engine.profile.impl;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.commons.Pair;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class ExportedProfileMapping {

    private List<String> users;

    private List<String> groups;

    private List<String> roles;

    private List<Pair<String, String>> memberships;

    public ExportedProfileMapping() {
        users = new ArrayList<String>();
        groups = new ArrayList<String>();
        roles = new ArrayList<String>();
        memberships = new ArrayList<Pair<String, String>>();
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(final List<String> users) {
        this.users = users;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(final List<String> groups) {
        this.groups = groups;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(final List<String> roles) {
        this.roles = roles;
    }

    public List<Pair<String, String>> getMemberships() {
        return memberships;
    }

    public void setMemberships(final List<Pair<String, String>> memberships) {
        this.memberships = memberships;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (groups == null ? 0 : groups.hashCode());
        result = prime * result + (memberships == null ? 0 : memberships.hashCode());
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
        final ExportedProfileMapping other = (ExportedProfileMapping) obj;
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
