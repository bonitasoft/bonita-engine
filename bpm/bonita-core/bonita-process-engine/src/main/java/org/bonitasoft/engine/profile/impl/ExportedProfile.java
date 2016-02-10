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

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class ExportedProfile {

    private final String name;

    private boolean isDefault;

    private String description;

    private List<ExportedParentProfileEntry> parentProfileEntries;

    private ExportedProfileMapping profileMapping;

    public ExportedProfile(final String name, final boolean isDefault) {
        this.name = name;
        this.isDefault = isDefault;
        parentProfileEntries = new ArrayList<ExportedParentProfileEntry>();
        profileMapping = new ExportedProfileMapping();
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(final boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<ExportedParentProfileEntry> getParentProfileEntries() {
        return parentProfileEntries;
    }

    public void setParentProfileEntries(final List<ExportedParentProfileEntry> parentProfileEntries) {
        this.parentProfileEntries = parentProfileEntries;
    }

    public ExportedProfileMapping getProfileMapping() {
        return profileMapping;
    }

    public void setProfileMapping(final ExportedProfileMapping profileMapping) {
        this.profileMapping = profileMapping;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (isDefault ? 1231 : 1237);
        result = prime * result + (description == null ? 0 : description.hashCode());
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
        final ExportedProfile other = (ExportedProfile) obj;
        if (isDefault != other.isDefault) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    public boolean hasCustomPages() {
        if (parentProfileEntries != null) {
            for (final ExportedParentProfileEntry parentProfileEntry : parentProfileEntries) {
                if (parentProfileEntry.hasCustomPages()) {
                    return true;
                }
            }
        }
        return false;
    }

}
