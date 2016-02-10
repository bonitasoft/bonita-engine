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

import org.bonitasoft.engine.profile.impl.ExportedParentProfileEntry;
import org.bonitasoft.engine.profile.impl.ExportedProfile;
import org.bonitasoft.engine.profile.impl.ExportedProfileMapping;

/**
 * Import / export version of the client profile model
 * 
 * @author Celine Souchet
 */
public class ExportedProfileBuilder {

    private final ExportedProfile profile;

    public ExportedProfileBuilder(final String profileName, final boolean isDefault) {
        profile = new ExportedProfile(profileName, isDefault);
    }

    public ExportedProfileBuilder setDescription(final String description) {
        profile.setDescription(description);
        return this;
    }

    public ExportedProfileBuilder setParentProfileEntries(final List<ExportedParentProfileEntry> parentProfileEntries) {
        profile.setParentProfileEntries(parentProfileEntries);
        return this;
    }

    public ExportedProfileBuilder setProfileMapping(final ExportedProfileMapping profileMapping) {
        profile.setProfileMapping(profileMapping);
        return this;
    }

    public ExportedProfile done() {
        return profile;
    }

}
