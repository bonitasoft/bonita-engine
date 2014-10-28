/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SObjectModificationException;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;

import com.bonitasoft.engine.business.application.ApplicationService;
import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuBuilderFactoryImpl;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuUpdateBuilderImpl;

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

    public void incrementIndexes(Long parentId, int from, int to) throws SBonitaSearchException, SObjectModificationException {
        updateIndexes(parentId, from, to, 1);
    }

    public void decrementIndexes(Long parentId, int from, int to) throws SBonitaSearchException, SObjectModificationException {
        updateIndexes(parentId, from, to, -1);
    }

    private void updateIndexes(Long parentId, int from, int to, int offSet) throws SBonitaSearchException, SObjectModificationException {
        if(to >= from) {
            List<SApplicationMenu> menusToUpdate = null;
            int firstResult = 0;
            do {
                menusToUpdate = getCurrentPage(parentId, from, to, firstResult);
                firstResult += maxResults;
                updateIndexes(menusToUpdate, offSet);
            } while (menusToUpdate.size() == maxResults);
        }
    }

    private List<SApplicationMenu> getCurrentPage(Long parentId, int from, int to, int firstResult) throws SBonitaSearchException {
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
