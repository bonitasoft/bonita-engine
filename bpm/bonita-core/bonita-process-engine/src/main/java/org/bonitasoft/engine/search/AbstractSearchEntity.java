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
package org.bonitasoft.engine.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SearchFields;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;
import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.search.impl.SearchResultImpl;

/**
 * Abstract class to allow to search server object and convert them to client object
 *
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @param <C>
 *        The client object
 * @param <S>
 *        The server object
 */
public abstract class AbstractSearchEntity<C extends Serializable, S extends PersistentObject>
        implements TransactionContentWithResult<SearchResult<C>> {

    private final SearchOptions options;

    private final SearchEntityDescriptor searchDescriptor;

    private long count;

    private List<C> clientObjects;

    /**
     * @param searchDescriptor
     *        The search descriptor of the searched entity
     * @param options
     *        The options of the search
     */
    public AbstractSearchEntity(final SearchEntityDescriptor searchDescriptor, final SearchOptions options) {
        this.searchDescriptor = searchDescriptor;
        this.options = options;
    }

    protected void validateQuery(SearchOptions options) throws SBonitaException {
        /*
         * Used to validate the query before execution,
         * in order to throw exceptions with meaningful message.
         */
    }

    @Override
    public void execute() throws SBonitaException {
        validateQuery(options);
        List<S> serverObjects;
        if (options == null) {
            throw new SBonitaReadException("SearchOptions cannot be null");
        }
        final int numberOfResults = options.getMaxResults();
        final int fromIndex = options.getStartIndex();
        final List<SearchFilter> filters = options.getFilters();
        final List<FilterOption> filterOptions = new ArrayList<>(filters.size());
        for (final SearchFilter filter : filters) {
            final FilterOption option = searchDescriptor.getEntityFilter(filter);
            if (option != null) {// in case of a unknown filter on state
                filterOptions.add(option);
            }
        }
        final String searchTerm = options.getSearchTerm();
        SearchFields userSearchTerm = null;
        if (searchTerm != null) {
            userSearchTerm = searchDescriptor.getEntitySearchTerm(searchTerm);
        }
        final List<OrderByOption> orderOptions = new ArrayList<>();
        final List<Sort> sorts = options.getSorts();
        for (final Sort sort : sorts) {
            final OrderByOption order = searchDescriptor.getEntityOrder(sort);
            orderOptions.add(order);
        }
        final QueryOptions countOptions = new QueryOptions(0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS, null,
                filterOptions, userSearchTerm);
        count = executeCount(countOptions);
        if (count > 0 && numberOfResults != 0) {
            final QueryOptions searchOptions = new QueryOptions(fromIndex, numberOfResults, orderOptions, filterOptions,
                    userSearchTerm);
            serverObjects = executeSearch(searchOptions);
        } else {
            serverObjects = Collections.emptyList();
        }
        clientObjects = convertToClientObjects(serverObjects);
    }

    /**
     * execute this search and return the result
     *
     * @return the result of the search
     */
    public SearchResult<C> search() throws SearchException {
        try {
            execute();
        } catch (SBonitaException e) {
            throw new SearchException(e);
        }
        return getResult();
    }

    /**
     * Execute the count here
     *
     * @param queryOptions
     *        The query options to execute the count with
     * @return The number of result on the server
     * @throws SBonitaReadException when the search failed to retrieve the count number
     */
    public abstract long executeCount(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Execute the search here
     *
     * @param queryOptions
     *        The query options to execute the search with
     * @return The list of searched server objects
     * @throws SBonitaReadException when the search failed to retrieve the results
     */
    public abstract List<S> executeSearch(QueryOptions queryOptions) throws SBonitaReadException;

    /**
     * Must convert server objects in client objects here
     *
     * @param serverObjects
     *        The server object to convert
     * @return The list of the client objects corresponding to the server objects
     */
    public abstract List<C> convertToClientObjects(List<S> serverObjects) throws SBonitaException;

    @Override
    public SearchResult<C> getResult() {
        return new SearchResultImpl<>(count, clientObjects);
    }

    protected SearchFilter getSearchFilter(final SearchOptions searchOptions, final String searchedKey) {
        return searchOptions.getFilters().stream().filter(searchFilter -> searchedKey.equals(searchFilter.getField()))
                .findFirst().orElse(null);
    }

    public static <C extends Serializable, S extends PersistentObject> SearchResult<C> search(
            SearchEntityDescriptor searchDescriptor,
            SearchOptions options,
            BonitaReadFunction<List<S>, List<C>> converter,
            BonitaReadFunction<QueryOptions, Long> count,
            BonitaReadFunction<QueryOptions, List<S>> search) throws SearchException {
        return new AbstractSearchEntity<C, S>(searchDescriptor, options) {

            @Override
            public long executeCount(QueryOptions queryOptions) throws SBonitaReadException {
                return count.apply(queryOptions);
            }

            @Override
            public List<S> executeSearch(QueryOptions queryOptions) throws SBonitaReadException {
                return search.apply(queryOptions);
            }

            @Override
            public List<C> convertToClientObjects(List<S> serverObjects) throws SBonitaException {
                return converter.apply(serverObjects);
            }
        }.search();
    }

}
