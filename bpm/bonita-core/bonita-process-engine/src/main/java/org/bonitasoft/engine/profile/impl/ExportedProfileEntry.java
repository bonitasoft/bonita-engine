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

import org.bonitasoft.engine.api.ImportError;
import org.bonitasoft.engine.api.ImportError.Type;

/**
 * @author Zhao Na
 * @author Matthieu Chaffotte
 */
public class ExportedProfileEntry {

    private final String name;

    private boolean isCustom;

    private String description;

    private String type;

    private String parentName;

    private long index;

    private String page;

    public ExportedProfileEntry(final String name) {
        this.name = name;
    }

    public final boolean isCustom() {
        return isCustom;
    }

    public final void setCustom(final boolean isCustom) {
        this.isCustom = isCustom;
    }

    public final String getDescription() {
        return description;
    }

    public final void setDescription(final String description) {
        this.description = description;
    }

    public final String getType() {
        return type;
    }

    public final void setType(final String type) {
        this.type = type;
    }

    public final String getParentName() {
        return parentName;
    }

    public final void setParentName(final String parentName) {
        this.parentName = parentName;
    }

    public final long getIndex() {
        return index;
    }

    public final void setIndex(final long index) {
        this.index = index;
    }

    public final String getPage() {
        return page;
    }

    public final void setPage(final String page) {
        this.page = page;
    }

    public final String getName() {
        return name;
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (getDescription() == null ? 0 : getDescription().hashCode());
        result = prime * result + (getName() == null ? 0 : getName().hashCode());
        result = prime * result + (getType() == null ? 0 : getType().hashCode());
        result = prime * result + (getPage() == null ? 0 : getPage().hashCode());
        result = prime * result + (!isCustom() ? 0 : getPage().hashCode());
        result = prime * result + (getParentName() == null ? 0 : getParentName().hashCode());
        result = prime * result + (int) (getIndex() ^ (getIndex() >>> 32));
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        return compareExportedProfileEntry(obj);
    }

    protected boolean compareExportedProfileEntry(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExportedProfileEntry other = (ExportedProfileEntry) obj;
        if (getName() == null) {
            if (other.getName() != null) {
                return false;
            }
        } else if (!getName().equals(other.getName())) {
            return false;
        }

        if (getDescription() == null) {
            if (other.getDescription() != null) {
                return false;
            }
        } else if (!getDescription().equals(other.getDescription())) {
            return false;
        }
        if (getType() == null) {
            if (other.getType() != null) {
                return false;
            }
        } else if (!getType().equals(other.getType())) {
            return false;
        }
        if (getPage() == null) {
            if (other.getPage() != null) {
                return false;
            }
        } else if (!getPage().equals(other.getPage())) {
            return false;
        }
        if (isCustom() != other.isCustom()) {
            return false;
        }
        if (getIndex() != other.getIndex()) {
            return false;
        }
        if (getParentName() == null) {
            if (other.getParentName() != null) {
                return false;
            }
        } else if (!getParentName().equals(other.getParentName())) {
            return false;
        }
        return true;
    }

    public boolean hasError() {
        if (getError() == null) {
            return false;
        }
        return true;
    }

    public ImportError getError() {
        if (getName() == null) {
            return new ImportError(getName(), Type.PAGE);
        }
        if (getPage() == null || getPage().isEmpty()) {
            return new ImportError(getName(), Type.PAGE);
        }
        return null;
    }

}
