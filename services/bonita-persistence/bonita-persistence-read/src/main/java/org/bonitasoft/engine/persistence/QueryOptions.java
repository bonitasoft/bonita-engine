/**
 * Copyright (C) 2011-2012 BonitaSoft S.A.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Charles Souillard
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 */
public class QueryOptions implements Serializable {

    private static final long serialVersionUID = 8923754215920928153L;

    private final int fromIndex;

    private final int numberOfResults;

    private final List<FilterOption> filters;

    private final SearchFields multipleFilter;

    private final List<OrderByOption> orderByOptions;

    public static final int UNLIMITED_NUMBER_OF_RESULTS = Integer.MAX_VALUE;

    public static final int DEFAULT_NUMBER_OF_RESULTS = 20;

    private static final QueryOptions DEFAULT_QUERY_OPTIONS = new QueryOptions(0, DEFAULT_NUMBER_OF_RESULTS);

    private static final QueryOptions ALL_RESULTS_QUERY_OPTIONS = new QueryOptions(0, UNLIMITED_NUMBER_OF_RESULTS);

    public QueryOptions(final QueryOptions queryOptions) {
        super();
        fromIndex = queryOptions.getFromIndex();
        numberOfResults = queryOptions.getNumberOfResults();
        orderByOptions = queryOptions.getOrderByOptions();
        filters = queryOptions.getFilters();
        multipleFilter = queryOptions.getMultipleFilter();
    }

    @Deprecated
    public QueryOptions(final int fromIndex, final int numberOfResults) {
        super();
        this.fromIndex = fromIndex;
        this.numberOfResults = numberOfResults;
        orderByOptions = Collections.emptyList();
        filters = Collections.emptyList();
        multipleFilter = null;
    }

    public QueryOptions(final int fromIndex, final int numberOfResults, final List<OrderByOption> orderByOptions) {
        super();
        this.fromIndex = fromIndex;
        this.numberOfResults = numberOfResults;
        this.orderByOptions = orderByOptions;
        filters = Collections.emptyList();
        multipleFilter = null;
    }

    public QueryOptions(final int fromIndex, final int numberOfResults, final List<OrderByOption> orderByOptions, final List<FilterOption> filters,
            final SearchFields multipleFilter) {
        super();
        this.fromIndex = fromIndex;
        this.numberOfResults = numberOfResults;
        this.orderByOptions = orderByOptions;
        this.filters = filters;
        this.multipleFilter = multipleFilter;
    }

    @Deprecated
    public QueryOptions(final List<FilterOption> filters, final SearchFields multipleFilter) {
        this(0, DEFAULT_NUMBER_OF_RESULTS, Collections.<OrderByOption> emptyList(), filters, multipleFilter);
    }

    public QueryOptions(final List<OrderByOption> orderByOptions, final List<FilterOption> filters, final SearchFields multipleFilter) {
        this(0, DEFAULT_NUMBER_OF_RESULTS, orderByOptions, filters, multipleFilter);
    }

    public QueryOptions(final int fromIndex, final int numberOfResults, final Class<? extends PersistentObject> clazz, final String fieldName,
            final OrderByType orderByType) {
        super();
        this.fromIndex = fromIndex;
        this.numberOfResults = numberOfResults;
        if (fieldName == null || orderByType == null) {
            orderByOptions = Collections.emptyList();
        } else {
            orderByOptions = new ArrayList<OrderByOption>();
            orderByOptions.add(new OrderByOption(clazz, fieldName, orderByType));
        }
        filters = Collections.emptyList();
        multipleFilter = null;
    }

    public QueryOptions(final List<OrderByOption> orderByOptions) {
        super();
        fromIndex = 0;
        numberOfResults = DEFAULT_NUMBER_OF_RESULTS;
        this.orderByOptions = orderByOptions;
        filters = Collections.emptyList();
        multipleFilter = null;
    }

    public QueryOptions(final Class<? extends PersistentObject> clazz, final String fieldName, final OrderByType orderByType) {
        super();
        fromIndex = 0;
        numberOfResults = DEFAULT_NUMBER_OF_RESULTS;
        orderByOptions = new ArrayList<OrderByOption>();
        orderByOptions.add(new OrderByOption(clazz, fieldName, orderByType));
        filters = Collections.emptyList();
        multipleFilter = null;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public int getNumberOfResults() {
        return numberOfResults;
    }

    public List<FilterOption> getFilters() {
        return filters;
    }

    public SearchFields getMultipleFilter() {
        return multipleFilter;
    }

    public List<OrderByOption> getOrderByOptions() {
        return orderByOptions;
    }

    public boolean hasOrderByOptions() {
        return orderByOptions != null && !orderByOptions.isEmpty();
    }

    public static QueryOptions defaultQueryOptions() {
        return DEFAULT_QUERY_OPTIONS;
    }

    public static QueryOptions allResultsQueryOptions() {
        return ALL_RESULTS_QUERY_OPTIONS;
    }

    public static QueryOptions getNextPage(final QueryOptions queryOptions) {
        return new QueryOptions(queryOptions.getFromIndex() + queryOptions.getNumberOfResults(), queryOptions.getNumberOfResults(),
                queryOptions.getOrderByOptions());
    }

    @Override
    public String toString() {
        return "QueryOptions [fromIndex=" + fromIndex + ", numberOfResults=" + numberOfResults + ", orderByOptions=" + orderByOptions + "]";
    }

    public boolean hasAFilter() {
        return filters != null && !filters.isEmpty() || multipleFilter != null;
    }

}
