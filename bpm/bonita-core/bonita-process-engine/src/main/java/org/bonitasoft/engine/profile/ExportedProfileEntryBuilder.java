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

import org.bonitasoft.engine.profile.impl.ExportedProfileEntry;

/**
 * Import / export version of the client profile entry model
 * 
 * @author Celine Souchet
 */
public class ExportedProfileEntryBuilder {

    private final ExportedProfileEntry profileEntry;

    public ExportedProfileEntryBuilder(final String name) {
        profileEntry = new ExportedProfileEntry(name);
    }

    public ExportedProfileEntryBuilder setDescription(final String description) {
        profileEntry.setDescription(description);
        return this;
    }

    public ExportedProfileEntryBuilder setIndex(final long index) {
        profileEntry.setIndex(index);
        return this;
    }

    public ExportedProfileEntryBuilder setPage(final String page) {
        profileEntry.setPage(page);
        return this;
    }

    public ExportedProfileEntryBuilder setParentName(final String parentName) {
        profileEntry.setParentName(parentName);
        return this;
    }

    public ExportedProfileEntryBuilder setType(final String type) {
        profileEntry.setType(type);
        return this;
    }

    public ExportedProfileEntry done() {
        return profileEntry;
    }

    public ExportedProfileEntryBuilder setCustom(final boolean custom) {
        profileEntry.setCustom(custom);
        return this;
    }

}
