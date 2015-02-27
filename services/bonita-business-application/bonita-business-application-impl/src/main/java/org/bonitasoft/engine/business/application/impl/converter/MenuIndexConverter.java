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
package org.bonitasoft.engine.business.application.impl.converter;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.impl.MenuIndex;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuFields;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 */
public class MenuIndexConverter {

    private ApplicationService applicationService;

    public MenuIndexConverter(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    public MenuIndex toMenuIndex(SApplicationMenu appMenu) throws SBonitaReadException {
        int lastUsedIndex = applicationService.getLastUsedIndex(appMenu.getParentId());
        return new MenuIndex(appMenu.getParentId(), appMenu.getIndex(), lastUsedIndex);
    }

    public MenuIndex toMenuIndex(SApplicationMenu oldAppMenu, EntityUpdateDescriptor updateDescriptor) throws SBonitaReadException {
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
