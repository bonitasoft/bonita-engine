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
import org.bonitasoft.engine.profile.impl.ExportedProfileEntry;

/**
 * Import / export version of the client profile entry model
 * 
 * @author Celine Souchet
 */
public class ExportedParentProfileEntryBuilder {

    private final ExportedParentProfileEntry profileEntry;

    public ExportedParentProfileEntryBuilder(final String name) {
        profileEntry = new ExportedParentProfileEntry(name);
    }

    public ExportedParentProfileEntryBuilder setDescription(final String description) {
        profileEntry.setDescription(description);
        return this;
    }

    public ExportedParentProfileEntryBuilder setIndex(final long index) {
        profileEntry.setIndex(index);
        return this;
    }

    public ExportedParentProfileEntryBuilder setPage(final String page) {
        profileEntry.setPage(page);
        return this;
    }

    public ExportedParentProfileEntryBuilder setParentName(final String parentName) {
        profileEntry.setParentName(parentName);
        return this;
    }

    public ExportedParentProfileEntryBuilder setType(final String type) {
        profileEntry.setType(type);
        return this;
    }

    public ExportedParentProfileEntryBuilder setChildProfileEntries(final List<ExportedProfileEntry> childProfileEntries) {
        profileEntry.setChildProfileEntries(childProfileEntries);
        return this;
    }

    public ExportedParentProfileEntry done() {
        return profileEntry;
    }

    public ExportedParentProfileEntryBuilder setCustom(final boolean custom) {
        profileEntry.setCustom(custom);
        return this;
    }

}
