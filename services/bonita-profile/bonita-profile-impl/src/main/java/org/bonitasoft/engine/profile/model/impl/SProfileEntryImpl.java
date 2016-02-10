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
package org.bonitasoft.engine.profile.model.impl;

import org.bonitasoft.engine.profile.model.SProfileEntry;

/**
 * @author Matthieu Chaffotte
 */
public class SProfileEntryImpl implements SProfileEntry {

    private static final long serialVersionUID = -6338293070515058067L;

    private long id;

    private long tenantId;

    private long profileId;

    private long parentId;

    private String name;

    private String description;

    private long index;

    private String type;

    private String page;

    private boolean custom;

    public SProfileEntryImpl() {
        super();
    }

    public SProfileEntryImpl(final String name, final long profileId) {
        this.name = name;
        this.profileId = profileId;
    }

    public SProfileEntryImpl(final SProfileEntry profileEntry) {
        super();
        id = profileEntry.getId();
        name = profileEntry.getName();
        description = profileEntry.getDescription();
        profileId = profileEntry.getProfileId();
        parentId = profileEntry.getParentId();
        index = profileEntry.getIndex();
        page = profileEntry.getPage();
        type = profileEntry.getType();
        custom = profileEntry.isCustom();
    }

    public long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(final long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(final long id) {
        this.id = id;
    }

    @Override
    public String getDiscriminator() {
        return SProfileEntryImpl.class.getName();
    }

    @Override
    public long getParentId() {
        return parentId;
    }

    public void setParentId(final long parentId) {
        this.parentId = parentId;
    }

    @Override
    public long getProfileId() {
        return profileId;
    }

    public void setProfileId(final long profileId) {
        this.profileId = profileId;
    }

    public void setCustom(final boolean custom) {
        this.custom = custom;
    }

    @Override
    public boolean isCustom() {
        return custom;
    }

    @Override
    public long getIndex() {
        return index;
    }

    public void setIndex(final long index) {
        this.index = index;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    @Override
    public String getPage() {
        return page;
    }

    public void setPage(final String page) {
        this.page = page;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (custom ? 1231 : 1237);
        result = prime * result + (description == null ? 0 : description.hashCode());
        result = prime * result + (int) (id ^ id >>> 32);
        result = prime * result + (int) (index ^ index >>> 32);
        result = prime * result + (name == null ? 0 : name.hashCode());
        result = prime * result + (page == null ? 0 : page.hashCode());
        result = prime * result + (int) (parentId ^ parentId >>> 32);
        result = prime * result + (int) (profileId ^ profileId >>> 32);
        result = prime * result + (int) (tenantId ^ tenantId >>> 32);
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
        SProfileEntryImpl other = (SProfileEntryImpl) obj;
        if (custom != other.custom) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (id != other.id) {
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
        if (parentId != other.parentId) {
            return false;
        }
        if (profileId != other.profileId) {
            return false;
        }
        if (tenantId != other.tenantId) {
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

    @Override
    public String toString() {
        return "SProfileEntryImpl [id=" + id + ", tenantId=" + tenantId + ", profileId=" + profileId + ", parentId=" + parentId + ", name=" + name
                + ", description=" + description + ", index=" + index + ", type=" + type + ", page=" + page + ", custom=" + custom + "]";
    }

}
