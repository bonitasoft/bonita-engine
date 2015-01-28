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
package org.bonitasoft.engine.business.application.impl;

/**
 * @author Elias Ricken de Medeiros
 */
public class MenuIndex {

    private int value;

    private Long parentId;

    private int lastUsedIndex;

    public MenuIndex(Long parentId, int value, int lastUsedIndex) {
        this.value = value;
        this.parentId = parentId;
        this.lastUsedIndex = lastUsedIndex;
    }

    public int getValue() {
        return value;
    }

    public Long getParentId() {
        return parentId;
    }

    public int getLastUsedIndex() {
        return lastUsedIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuIndex)) return false;

        MenuIndex menuIndex = (MenuIndex) o;

        if (lastUsedIndex != menuIndex.lastUsedIndex) return false;
        if (value != menuIndex.value) return false;
        if (parentId != null ? !parentId.equals(menuIndex.parentId) : menuIndex.parentId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value;
        result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
        result = 31 * result + lastUsedIndex;
        return result;
    }

}
