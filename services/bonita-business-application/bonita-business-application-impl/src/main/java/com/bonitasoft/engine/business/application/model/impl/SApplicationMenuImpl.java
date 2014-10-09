/**
 * Copyright (C) 2014 BonitaSoft S.A.
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
package com.bonitasoft.engine.business.application.model.impl;

import org.bonitasoft.engine.persistence.PersistentObjectId;

import com.bonitasoft.engine.business.application.model.SApplicationMenu;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationMenuImpl extends PersistentObjectId implements SApplicationMenu {

    private static final long serialVersionUID = 5080525289831930498L;
    private String displayName;
    private long applicationPageId;
    private Long parentId;
    private int index;

    //used by Hibernate
    public SApplicationMenuImpl() {
    }

    public SApplicationMenuImpl(final String displayName, final long applicationPageId, final int index) {
        this.displayName = displayName;
        this.applicationPageId = applicationPageId;
        this.index = index;
    }

    @Override
    public String getDiscriminator() {
        return SApplicationMenu.class.getName();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public long getApplicationPageId() {
        return applicationPageId;
    }

    @Override
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(final Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (int) (applicationPageId ^ applicationPageId >>> 32);
        result = prime * result + (displayName == null ? 0 : displayName.hashCode());
        result = prime * result + index;
        result = prime * result + (parentId == null ? 0 : parentId.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SApplicationMenuImpl other = (SApplicationMenuImpl) obj;
        if (applicationPageId != other.applicationPageId) {
            return false;
        }
        if (displayName == null) {
            if (other.displayName != null) {
                return false;
            }
        } else if (!displayName.equals(other.displayName)) {
            return false;
        }
        if (index != other.index) {
            return false;
        }
        if (parentId == null) {
            if (other.parentId != null) {
                return false;
            }
        } else if (!parentId.equals(other.parentId)) {
            return false;
        }
        return true;
    }

}
