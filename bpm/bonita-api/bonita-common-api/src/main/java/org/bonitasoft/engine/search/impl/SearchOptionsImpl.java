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
package org.bonitasoft.engine.search.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.exception.IncorrectParameterException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchFilterOperation;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.Sort;

/**
 * @author Matthieu Chaffotte
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class SearchOptionsImpl implements SearchOptions {

    private static final long serialVersionUID = 1967932608495889373L;

    private List<SearchFilter> filters;

    private String searchTerm;

    private final int startIndex;

    private final int numberOfResults;

    private List<Sort> sorts;

    public SearchOptionsImpl(final int startIndex, final int numberOfResults) {
        filters = new ArrayList<SearchFilter>(5);
        sorts = new ArrayList<Sort>(2);
        this.startIndex = startIndex;
        this.numberOfResults = numberOfResults;
    }

    public void setFilters(final List<SearchFilter> filters) {
        this.filters = filters;
    }

    @Override
    public List<SearchFilter> getFilters() {
        return filters;
    }

    @Override
    public String getSearchTerm() {
        return searchTerm;
    }

    @Override
    public int getStartIndex() {
        return startIndex;
    }

    @Override
    public int getMaxResults() {
        return numberOfResults;
    }

    @Override
    public List<Sort> getSorts() {
        return sorts;
    }

    public void setSearchTerm(final String value) {
        searchTerm = value;
    }

    public void addGreaterThanFilter(final String field, final Serializable value) {
        filters.add(new SearchFilter(field, SearchFilterOperation.GREATER_THAN, value));
    }

    public void addGreaterOrEqualsFilter(final String field, final Serializable value) {
        filters.add(new SearchFilter(field, SearchFilterOperation.GREATER_OR_EQUAL, value));
    }

    public void addLessThanFilter(final String field, final Serializable value) {
        filters.add(new SearchFilter(field, SearchFilterOperation.LESS_THAN, value));
    }

    public void addLessOrEqualsFilter(final String field, final Serializable value) {
        filters.add(new SearchFilter(field, SearchFilterOperation.LESS_OR_EQUAL, value));
    }

    public void addBetweenFilter(final String field, final Serializable from, final Serializable to) {
        filters.add(new SearchFilter(field, from, to));
    }

    public void addDifferentFromFilter(final String field, final Serializable value) {
        filters.add(new SearchFilter(field, SearchFilterOperation.DIFFERENT, value));
    }

    public void addFilter(final String field, final Serializable value) {
        filters.add(new SearchFilter(field, SearchFilterOperation.EQUALS, value));
    }

    public final void addOrFilter() {
        try {
            filters.add(new SearchFilter(SearchFilterOperation.OR));
        } catch (final IncorrectParameterException e) {
            // Cannot happen as we force a correct value
        }
    }

    public final void addAndFilter() {
        try {
            filters.add(new SearchFilter(SearchFilterOperation.AND));
        } catch (final IncorrectParameterException e) {
            // Cannot happen as we force a correct value
        }
    }

    public final void addLeftParenthesis() {
        try {
            filters.add(new SearchFilter(SearchFilterOperation.L_PARENTHESIS));
        } catch (final IncorrectParameterException e) {
            // Cannot happen as we force a correct value
        }
    }

    public final void addRightParenthesis() {
        try {
            filters.add(new SearchFilter(SearchFilterOperation.R_PARENTHESIS));
        } catch (final IncorrectParameterException e) {
            // Cannot happen as we force a correct value
        }
    }

    public void addSort(final String field, final Order order) {
        sorts.add(new Sort(order, field));
    }

    public void setSorts(final List<Sort> sorts) {
        this.sorts = sorts;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (filters == null ? 0 : filters.hashCode());
        result = prime * result + numberOfResults;
        result = prime * result + (searchTerm == null ? 0 : searchTerm.hashCode());
        result = prime * result + (sorts == null ? 0 : sorts.hashCode());
        result = prime * result + startIndex;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SearchOptionsImpl other = (SearchOptionsImpl) obj;
        if (filters == null) {
            if (other.filters != null) {
                return false;
            }
        } else if (!filters.equals(other.filters)) {
            return false;
        }
        if (numberOfResults != other.numberOfResults) {
            return false;
        }
        if (searchTerm == null) {
            if (other.searchTerm != null) {
                return false;
            }
        } else if (!searchTerm.equals(other.searchTerm)) {
            return false;
        }
        if (sorts == null) {
            if (other.sorts != null) {
                return false;
            }
        } else if (!sorts.equals(other.sorts)) {
            return false;
        }
        if (startIndex != other.startIndex) {
            return false;
        }
        return true;
    }

}
