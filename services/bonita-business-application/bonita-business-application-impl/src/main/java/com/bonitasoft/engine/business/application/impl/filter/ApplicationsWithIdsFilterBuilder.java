/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.impl.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.bonitasoft.engine.business.application.model.SApplication;
import com.bonitasoft.engine.business.application.model.builder.impl.SApplicationBuilderFactoryImpl;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationsWithIdsFilterBuilder implements FilterBuilder{

    private Long[] applicationIds;

    public ApplicationsWithIdsFilterBuilder(Long ... applicationIds) {
        this.applicationIds = applicationIds;
    }


    @Override
    public QueryOptions buildQueryOptions() {
        SApplicationBuilderFactoryImpl factory = new SApplicationBuilderFactoryImpl();
        List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SApplication.class, factory.getIdKey(), OrderByType.ASC));

        FilterOption filterOption = new FilterOption(SApplication.class, factory.getIdKey());
        filterOption.in(applicationIds);

        List<FilterOption> filters = Collections.singletonList(filterOption);

        return new QueryOptions(0, applicationIds.length, orderByOptions, filters, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationsWithIdsFilterBuilder)) return false;

        ApplicationsWithIdsFilterBuilder that = (ApplicationsWithIdsFilterBuilder) o;

        if (!Arrays.equals(applicationIds, that.applicationIds)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return applicationIds != null ? Arrays.hashCode(applicationIds) : 0;
    }
}
