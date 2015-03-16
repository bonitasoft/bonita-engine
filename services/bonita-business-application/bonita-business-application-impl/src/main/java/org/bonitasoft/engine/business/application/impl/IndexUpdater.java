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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.business.application.ApplicationService;
import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuBuilderFactoryImpl;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuUpdateBuilderImpl;
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

    public void incrementIndexes(Long parentId, int from, int to) throws SBonitaReadException, SObjectModificationException {
        updateIndexes(parentId, from, to, 1);
    }

    public void decrementIndexes(Long parentId, int from, int to) throws SBonitaReadException, SObjectModificationException {
        updateIndexes(parentId, from, to, -1);
    }

    private void updateIndexes(Long parentId, int from, int to, int offSet) throws SObjectModificationException, SBonitaReadException {
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

    private List<SApplicationMenu> getCurrentPage(Long parentId, int from, int to, int firstResult) throws SBonitaReadException {
        SApplicationMenuBuilderFactoryImpl appMenuFactory = new SApplicationMenuBuilderFactoryImpl();
        List<OrderByOption> orderBy = Collections.singletonList(new OrderByOption(SApplicationMenu.class, appMenuFactory.getIndexKey(), OrderByType.ASC));
        List<FilterOption> filters = Arrays.asList(new FilterOption(SApplicationMenu.class, appMenuFactory.getIndexKey(), from, to), new FilterOption(
                SApplicationMenu.class, appMenuFactory.getParentIdKey(), parentId));
        QueryOptions options = new QueryOptions(firstResult, maxResults, orderBy, filters, null);
        return applicationService.searchApplicationMenus(options);
    }

    private void updateIndexes(List<SApplicationMenu> menusToUpdate, int offSet) throws SObjectModificationException {
        for (SApplicationMenu menuToUpdate : menusToUpdate) {
            SApplicationMenuUpdateBuilderImpl builder = new SApplicationMenuUpdateBuilderImpl();
            builder.updateIndex(menuToUpdate.getIndex() + offSet);
            applicationService.updateApplicationMenu(menuToUpdate, builder.done(), false);
        }
    }

}
