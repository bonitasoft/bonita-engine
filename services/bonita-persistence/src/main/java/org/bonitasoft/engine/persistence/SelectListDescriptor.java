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
package org.bonitasoft.engine.persistence;

import java.util.Map;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 */
public class SelectListDescriptor<T> extends AbstractSelectWithParametersDescriptor<T> {

    private final QueryOptions queryOptions;

    public SelectListDescriptor(final String queryName, final Map<String, Object> inputParameters, final Class<? extends PersistentObject> entityType,
            final QueryOptions queryOptions) {
        super(queryName, inputParameters, entityType, (Class<T>) entityType);
        if (queryOptions != null) {
            this.queryOptions = queryOptions;
        } else {
            throw new IllegalArgumentException("Need to have a query option to paginate and order the results.");
        }
    }

    public SelectListDescriptor(final String queryName, final Map<String, Object> inputParameters, final Class<? extends PersistentObject> entityType,
            final Class<T> returnType, final QueryOptions queryOptions) {
        super(queryName, inputParameters, entityType, returnType);
        if (queryOptions != null) {
            this.queryOptions = queryOptions;
        } else {
            throw new IllegalArgumentException("Need to have a query option to paginate and order the results.");
        }
    }

    public QueryOptions getQueryOptions() {
        return queryOptions;
    }

    public boolean hasOrderByParameters() {
        return queryOptions.hasOrderByOptions();
    }

    public int getStartIndex() {
        return queryOptions.getFromIndex();
    }

    public int getPageSize() {
        return queryOptions.getNumberOfResults();
    }

    public boolean hasAFilter() {
        return queryOptions.hasAFilter();
    }

}
