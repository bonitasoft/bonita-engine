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
package org.bonitasoft.engine.business.application.filter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.business.application.model.SApplication;
import org.bonitasoft.engine.business.application.model.builder.SApplicationBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;

/**
 * @author Elias Ricken de Medeiros
 */
public class ApplicationsWithIdsFilterBuilder {

    private final Long[] applicationIds;

    public ApplicationsWithIdsFilterBuilder(final Long... applicationIds) {
        this.applicationIds = applicationIds;
    }

    public QueryOptions buildQueryOptions() {
        final SApplicationBuilderFactory factory = BuilderFactory.get(SApplicationBuilderFactory.class);
        final List<OrderByOption> orderByOptions = Collections.singletonList(new OrderByOption(SApplication.class, factory.getIdKey(), OrderByType.ASC));

        final FilterOption filterOption = new FilterOption(SApplication.class, factory.getIdKey());
        filterOption.in((Object[]) applicationIds);

        final List<FilterOption> filters = Collections.singletonList(filterOption);

        return new QueryOptions(0, applicationIds.length, orderByOptions, filters, null);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApplicationsWithIdsFilterBuilder)) {
            return false;
        }

        final ApplicationsWithIdsFilterBuilder that = (ApplicationsWithIdsFilterBuilder) o;

        if (!Arrays.equals(applicationIds, that.applicationIds)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return applicationIds != null ? Arrays.hashCode(applicationIds) : 0;
    }
}
