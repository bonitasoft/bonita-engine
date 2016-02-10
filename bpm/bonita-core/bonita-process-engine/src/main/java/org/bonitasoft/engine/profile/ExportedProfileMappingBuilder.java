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
package org.bonitasoft.engine.profile;

import java.util.List;

import org.bonitasoft.engine.commons.Pair;
import org.bonitasoft.engine.profile.impl.ExportedProfileMapping;

/**
 * Import / export version of the client profile mapping model
 * 
 * @author Celine Souchet
 */
public class ExportedProfileMappingBuilder {

    private final ExportedProfileMapping profileMapping;

    public ExportedProfileMappingBuilder() {
        profileMapping = new ExportedProfileMapping();
    }

    public ExportedProfileMappingBuilder setGroups(final List<String> groups) {
        profileMapping.setGroups(groups);
        return this;
    }

    public ExportedProfileMappingBuilder setUsers(final List<String> userNames) {
        profileMapping.setUsers(userNames);
        return this;
    }

    public ExportedProfileMappingBuilder setRoles(final List<String> roles) {
        profileMapping.setRoles(roles);
        return this;
    }

    public ExportedProfileMappingBuilder setMemberships(final List<Pair<String, String>> memberships) {
        profileMapping.setMemberships(memberships);
        return this;
    }

    public ExportedProfileMapping done() {
        return profileMapping;
    }

}
