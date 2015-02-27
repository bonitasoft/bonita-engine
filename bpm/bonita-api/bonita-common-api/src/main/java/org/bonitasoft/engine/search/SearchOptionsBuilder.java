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
package org.bonitasoft.engine.search;

import java.io.Serializable;
import java.util.List;

import org.bonitasoft.engine.search.impl.SearchFilter;
import org.bonitasoft.engine.search.impl.SearchOptionsImpl;

/**
 * Builder for SearchOptions objects. Defines 'pagination'.
 * When several filters are added, implicit AND operators are used if not specified.
 * See {@link SearchOptions} for deeper details on search mechanism options.
 *
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @see SearchOptions
 */
public class SearchOptionsBuilder {

    private final SearchOptionsImpl options;

    /**
     * Builds a new <code>SearchOptions</code> with results limited to startIndex and maxResults
     *
     * @param startIndex the first result to return
     * @param maxResults the maximum results to return. The actual number can be smaller, if the end of the list has been reached.
     */
    public SearchOptionsBuilder(final int startIndex, final int maxResults) {
        options = new SearchOptionsImpl(startIndex, maxResults);
    }

    /**
     * Creates a new <code>SearchOptionsBuilder</code> from another instance by
     *
     * @param searchOptions
     */
    public SearchOptionsBuilder(final SearchOptions searchOptions) {
        options = new SearchOptionsImpl(searchOptions.getStartIndex(), searchOptions.getMaxResults());
        options.setFilters(searchOptions.getFilters());
        options.setSorts(searchOptions.getSorts());
        options.setSearchTerm(searchOptions.getSearchTerm());
    }

    /**
     * Filter the results to the specific value for the specific field (equality)
     *
     * @param field
     *        The name of the field to filter on. Depending on the search parameter, specify the field by accessing the relevant xxxSearchDescriptor classes.
     *        For example, <code>HumanTaskInstanceSearchDescriptor.NAME</code> and <code>HumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID</code>.
     * @param value
     *        the single value to filter on that field name
     * @return this builder itself
     * @since 6.0
     */
    public SearchOptionsBuilder filter(final String field, final Serializable value) {
        options.addFilter(field, value);
        return this;
    }

    /**
     * Filters search results with a greaterThan comparison operation.
     *
     * @param field
     *        the field name to compare to.
     * @param value
     *        the value to compare.
     * @return this builder itself
     * @see SearchOptionsBuilder#filter(String, java.io.Serializable) for field values
     */
    public SearchOptionsBuilder greaterThan(final String field, final Serializable value) {
        options.addGreaterThanFilter(field, value);
        return this;
    }

    /**
     * @param field
     * @param value
     * @return this builder itself
     * @see SearchOptionsBuilder#filter(String, java.io.Serializable) for field values
     */
    public SearchOptionsBuilder greaterOrEquals(final String field, final Serializable value) {
        options.addGreaterOrEqualsFilter(field, value);
        return this;
    }

    /**
     * @param field the field that should be less than
     * @param value
     * @return this builder itself
     * @see SearchOptionsBuilder#filter(String, java.io.Serializable) for field values
     */
    public SearchOptionsBuilder lessThan(final String field, final Serializable value) {
        options.addLessThanFilter(field, value);
        return this;
    }

    /**
     * @param field the field that should be less or equals
     * @param value the value
     * @return this builder itself
     * @see SearchOptionsBuilder#filter(String, java.io.Serializable) for field values
     */
    public SearchOptionsBuilder lessOrEquals(final String field, final Serializable value) {
        options.addLessOrEqualsFilter(field, value);
        return this;
    }

    /**
     * @param field the field that should be between
     * @param from  from this value
     * @param to    to this value
     * @return this builder itself
     * @see SearchOptionsBuilder#filter(String, java.io.Serializable) for field values
     */
    public SearchOptionsBuilder between(final String field, final Serializable from, final Serializable to) {
        options.addBetweenFilter(field, from, to);
        return this;
    }

    /**
     * @param field
     * @param value
     * @return this builder itself
     * @see SearchOptionsBuilder#filter(String, java.io.Serializable) for field values
     */
    public SearchOptionsBuilder differentFrom(final String field, final Serializable value) {
        options.addDifferentFromFilter(field, value);
        return this;
    }

    /**
     * @return this builder itself
     */
    public SearchOptionsBuilder or() {
        if (options.getFilters().size() == 0) {
            throw new IllegalArgumentException("OR operator cannot be the first filter in the list.");
        }
        options.addOrFilter();
        return this;
    }

    /**
     * @return this builder itself
     */
    public SearchOptionsBuilder and() {
        if (options.getFilters().size() == 0) {
            throw new IllegalArgumentException("AND operator cannot be the first filter in the list.");
        }
        options.addAndFilter();
        return this;
    }

    public SearchOptionsBuilder leftParenthesis() {
        options.addLeftParenthesis();
        return this;
    }

    public SearchOptionsBuilder rightParenthesis() {
        options.addRightParenthesis();
        return this;
    }

    /**
     * @param value the search term
     * @return this builder itself
     */
    public SearchOptionsBuilder searchTerm(final String value) {
        options.setSearchTerm(value);
        return this;
    }

    /**
     * Adds a sort order option to the list of sort options
     *
     * @param field
     *        the field name to sort by
     * @param order
     *        the order of the sort (ASCENDING, DESCENDING)
     * @return the current SearchOptionsBuilder
     */
    public SearchOptionsBuilder sort(final String field, final Order order) {
        options.addSort(field, order);
        return this;
    }

    /**
     * @param filters the filters to set
     * @return this builder itself
     */
    public SearchOptionsBuilder setFilters(final List<SearchFilter> filters) {
        options.setFilters(filters);
        return this;
    }

    /**
     * @param sorts the sorts to set
     * @return this builder itself
     */
    public SearchOptionsBuilder setSort(final List<Sort> sorts) {
        options.setSorts(sorts);
        return this;
    }

    /**
     * @return the <code>SearchOptions</code> finally built using this builder.
     */
    public SearchOptions done() {
        return options;
    }
}
