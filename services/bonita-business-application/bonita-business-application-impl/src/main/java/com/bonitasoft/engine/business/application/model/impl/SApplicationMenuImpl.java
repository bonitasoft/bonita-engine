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

}
