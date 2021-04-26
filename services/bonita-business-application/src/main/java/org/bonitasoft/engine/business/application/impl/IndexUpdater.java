/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.builder.SApplicationMenuUpdateBuilder;
import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;

/**
 * @author Elias Ricken de Medeiros
 */
public class IndexUpdater {

    private ApplicationService applicationService;
    private int maxResults;

    public IndexUpdater(ApplicationService applicationService, int maxResults) {
        this.applicationService = applicationService;
        this.maxResults = maxResults;
    }

    public void incrementIndexes(Long parentId, int from, int to)
            throws SBonitaReadException, SObjectModificationException {
        updateIndexes(parentId, from, to, 1);
    }

    public void decrementIndexes(Long parentId, int from, int to)
            throws SBonitaReadException, SObjectModificationException {
        updateIndexes(parentId, from, to, -1);
    }

    private void updateIndexes(Long parentId, int from, int to, int offSet)
            throws SObjectModificationException, SBonitaReadException {
        if (to >= from) {
            List<SApplicationMenu> menusToUpdate = null;
            int firstResult = 0;
            do {
                menusToUpdate = getCurrentPage(parentId, from, to, firstResult);
                firstResult += maxResults;
                updateIndexes(menusToUpdate, offSet);
            } while (menusToUpdate.size() == maxResults);
        }
    }

    private List<SApplicationMenu> getCurrentPage(Long parentId, int from, int to, int firstResult)
            throws SBonitaReadException {
        List<OrderByOption> orderBy = Collections
                .singletonList(new OrderByOption(SApplicationMenu.class, SApplicationMenu.INDEX, OrderByType.ASC));
        List<FilterOption> filters = Arrays
                .asList(new FilterOption(SApplicationMenu.class, SApplicationMenu.INDEX, from, to), new FilterOption(
                        SApplicationMenu.class, SApplicationMenu.PARENT_ID, parentId));
        QueryOptions options = new QueryOptions(firstResult, maxResults, orderBy, filters, null);
        return applicationService.searchApplicationMenus(options);
    }

    private void updateIndexes(List<SApplicationMenu> menusToUpdate, int offSet) throws SObjectModificationException {
        for (SApplicationMenu menuToUpdate : menusToUpdate) {
            applicationService.updateApplicationMenu(menuToUpdate,
                    new SApplicationMenuUpdateBuilder().updateIndex(menuToUpdate.getIndex() + offSet).done(), false);
        }
    }

}
