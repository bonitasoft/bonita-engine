/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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

    public String getName() {
        return name;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (int) (index ^ index >>> 32);
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (page == null ? 0 : page.hashCode());
        result = prime * result + (parentName == null ? 0 : parentName.hashCode());
        result = prime * result + (type == null ? 0 : type.hashCode());
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
        final ExportedProfileEntry other = (ExportedProfileEntry) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (index != other.index) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (page == null) {
            if (other.page != null) {
                return false;
            }
        } else if (!page.equals(other.page)) {
            return false;
        }
        if (parentName == null) {
            if (other.parentName != null) {
                return false;
            }
        } else if (!parentName.equals(other.parentName)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(final String parentName) {
        this.parentName = parentName;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(final long index) {
        this.index = index;
    }

    public String getPage() {
        return page;
    }

    public void setPage(final String page) {
        this.page = page;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public void setCustom(final boolean isCustom) {
        this.isCustom = isCustom;
    }

}
