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

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportError.Type;

/**
 * @author Zhao Na
 * @author Celine Souchet
 */
public class ExportedParentProfileEntry extends ExportedProfileEntry {

    private List<ExportedProfileEntry> childProfileEntries;

    public List<ExportedProfileEntry> getChildProfileEntries() {
        return childProfileEntries;
    }

    public void setChildProfileEntries(final List<ExportedProfileEntry> childProfileEntries) {
        this.childProfileEntries = childProfileEntries;
    }

    public ExportedParentProfileEntry(final String name) {
        super(name);
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
        if (!compareExportedProfileEntry(obj)) {
            return false;
        }
        final ExportedParentProfileEntry other = (ExportedParentProfileEntry) obj;
        if (getChildProfileEntries() == null) {
            if (other.getChildProfileEntries() != null) {
                return false;
            }
        } else if (!getChildProfileEntries().equals(other.getChildProfileEntries())) {
            return false;
        }

        return true;
    }

    public List<ImportError> getErrors() {
        final List<ImportError> errors = new ArrayList<ImportError>();
        if (hasError()) {
            errors.add(getError());
        }
        final List<ExportedProfileEntry> childProfileEntries = getChildProfileEntries();
        if (childProfileEntries != null) {
            for (final ExportedProfileEntry childEntry : childProfileEntries) {
                if (childEntry.hasError()) {
                    errors.add(childEntry.getError());
                }
            }
        }
        if (errors.isEmpty()) {
            return null;
        }
        return errors;
    }

    public boolean hasErrors() {
        if (getErrors() == null) {
            return false;
        }
        return true;
    }

    @Override
    public ImportError getError() {
        if (getName() == null) {
            return new ImportError(getName(), Type.PAGE);
        }
        return null;
    }

    public boolean hasCustomPages() {
        if (isCustom()) {
            return true;
        }
        if (getChildProfileEntries() != null) {
            for (final ExportedProfileEntry exportedProfileEntry : getChildProfileEntries()) {
                if (exportedProfileEntry.isCustom()) {
                    return true;
                }
            }
        }
        return false;
    }
}
