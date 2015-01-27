/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package org.bonitasoft.engine.business.application.impl.filter;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.business.application.model.SApplicationMenu;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationMenuBuilderFactoryImpl;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;

/**
 * @author Elias Ricken de Medeiros
 */
public class ChildrenMenusFilterBuilder implements FilterBuilder {

    private SelectRange range;
    private long parentId;

    public ChildrenMenusFilterBuilder(SelectRange range, long parentId) {
        this.range = range;
        this.parentId = parentId;
    }

    @Override
    public QueryOptions buildQueryOptions() {
        SApplicationMenuBuilderFactoryImpl factory = new SApplicationMenuBuilderFactoryImpl();
        List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SApplicationMenu.class, factory.getIdKey(), OrderByType.ASC));
        List<FilterOption> filters = Collections.singletonList(new FilterOption(SApplicationMenu.class, factory.getParentIdKey(), parentId));
        return new QueryOptions(range.getStartIndex(), range.getMaxResults(), orderByOptions, filters, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChildrenMenusFilterBuilder)) return false;

        ChildrenMenusFilterBuilder that = (ChildrenMenusFilterBuilder) o;

        if (parentId != that.parentId) return false;
        if (range != null ? !range.equals(that.range) : that.range != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = range != null ? range.hashCode() : 0;
        result = 31 * result + (int) (parentId ^ (parentId >>> 32));
        return result;
    }

}
