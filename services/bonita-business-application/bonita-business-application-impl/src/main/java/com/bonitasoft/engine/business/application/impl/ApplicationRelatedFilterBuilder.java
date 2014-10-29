/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bonitasoft.engine.business.application.model.SApplicationMenu;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuBuilderFactoryImpl;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationRelatedFilterBuilder extends AbstractFilterBuilder {

    private long applicationId;

    public ApplicationRelatedFilterBuilder(int startIndex, int maxResults, long applicationId) {
        super(startIndex, maxResults);
        this.applicationId = applicationId;
    }

    @Override
    public QueryOptions buildQueryOptions() {
        SApplicationMenuBuilderFactoryImpl factory = new SApplicationMenuBuilderFactoryImpl();
        List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SApplicationMenu.class, factory.getIdKey(), OrderByType.ASC));
        List<FilterOption> filters = new ArrayList<FilterOption>(2);
        filters.add(new FilterOption(SApplicationMenu.class, factory.getApplicationIdKey(), applicationId));
        //only too menu will be deleted as children menus will be deleted by the parent
        filters.add(new FilterOption(SApplicationMenu.class, factory.getParentIdKey(), null));
        return new QueryOptions(getStartIndex(), getMaxResults(), orderByOptions, filters, null);
    }

    public long getApplicationId() {
        return applicationId;
    }
}
