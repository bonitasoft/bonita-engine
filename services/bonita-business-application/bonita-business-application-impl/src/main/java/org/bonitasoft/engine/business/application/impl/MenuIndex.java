/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

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
