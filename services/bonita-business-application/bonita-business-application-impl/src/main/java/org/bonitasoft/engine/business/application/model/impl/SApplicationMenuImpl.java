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
package org.bonitasoft.engine.business.application.model.impl;

import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.persistence.PersistentObjectId;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class SApplicationMenuImpl extends PersistentObjectId implements SApplicationMenu {

    private static final long serialVersionUID = 5080525289831930498L;
    private String displayName;
    private long applicationId;
    private Long applicationPageId;
    private Long parentId;
    private int index;

    //used by Hibernate
    public SApplicationMenuImpl() {
    }

    public SApplicationMenuImpl(final String displayName, long applicationId, final Long applicationPageId, final int index) {
        this.displayName = displayName;
        this.applicationId = applicationId;
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

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public long getApplicationId() {
        return applicationId;
    }

    @Override
    public Long getApplicationPageId() {
        return applicationPageId;
    }

    public void setApplicationPageId(Long applicationPageId) {
        this.applicationPageId = applicationPageId;
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

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SApplicationMenuImpl)) return false;
        if (!super.equals(o)) return false;

        SApplicationMenuImpl that = (SApplicationMenuImpl) o;

        if (applicationId != that.applicationId) return false;
        if (index != that.index) return false;
        if (applicationPageId != null ? !applicationPageId.equals(that.applicationPageId) : that.applicationPageId != null)
            return false;
        if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) return false;
        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (int) (applicationId ^ (applicationId >>> 32));
        result = 31 * result + (applicationPageId != null ? applicationPageId.hashCode() : 0);
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + index;
        return result;
    }
}
