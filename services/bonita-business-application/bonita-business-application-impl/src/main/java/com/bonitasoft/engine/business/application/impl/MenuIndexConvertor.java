/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuFields;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 */
public class MenuIndexConvertor {

    private ApplicationService applicationService;

    public MenuIndexConvertor(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    MenuIndex toMenuIndex(SApplicationMenu appMenu) throws SBonitaReadException {
        int lastUsedIndex = applicationService.getLastUsedIndex(appMenu.getParentId());
        return new MenuIndex(appMenu.getParentId(), appMenu.getIndex(), lastUsedIndex);
    }

    MenuIndex toMenuIndex(SApplicationMenu oldAppMenu, EntityUpdateDescriptor updateDescriptor) throws SBonitaReadException {
        Long parentId = getParentId(oldAppMenu, updateDescriptor);
        Integer indexValue = getIndexValue(oldAppMenu, updateDescriptor);
        int lastUsedIndex = applicationService.getLastUsedIndex(parentId);
        MenuIndex menuIndex = new MenuIndex(parentId, indexValue, lastUsedIndex);
        return menuIndex;
    }

    private Integer getIndexValue(SApplicationMenu oldAppMenu, EntityUpdateDescriptor updateDescriptor) {
        Integer indexValue;
        indexValue = (Integer) updateDescriptor.getFields().get(SApplicationMenuFields.INDEX);
        if(indexValue == null) {
            indexValue = oldAppMenu.getIndex();
        }
        return indexValue;
    }

    private Long getParentId(SApplicationMenu oldAppMenu, EntityUpdateDescriptor updateDescriptor) {
        Long parentId;
        if(updateDescriptor.getFields().containsKey(SApplicationMenuFields.PARENT_ID)) {
            parentId = (Long) updateDescriptor.getFields().get(SApplicationMenuFields.PARENT_ID);
        } else {
            parentId = oldAppMenu.getParentId();
        }
        return parentId;
    }

}
