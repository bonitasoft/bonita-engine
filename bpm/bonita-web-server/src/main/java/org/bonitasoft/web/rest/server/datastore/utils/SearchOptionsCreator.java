/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.datastore.utils;

import static org.bonitasoft.web.rest.server.framework.utils.SearchOptionsBuilderUtil.computeIndex;

import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.web.rest.server.datastore.filter.Filter;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.toolkit.client.common.util.StringUtil;

/**
 * @author Vincent Elcrin
 */
public class SearchOptionsCreator {

    private final SearchOptionsBuilder builder;

    public SearchOptionsCreator(int page, int resultsByPage, String search, Sorts sorts, Filters filters) {
        builder = new SearchOptionsBuilder(computeIndex(page, resultsByPage), resultsByPage);
        builder.searchTerm(search);
        addSorts(builder, sorts);
        addFilters(builder, filters);
    }

    private void addSorts(SearchOptionsBuilder builder, Sorts sorts) {
        for (Sort sort : sorts.asList()) {
            builder.sort(sort.getField(), sort.getOrder());
        }
    }

    private void addFilters(SearchOptionsBuilder builder, Filters filters) {
        for (Filter<?> filter : filters.asList()) {
            addFilter(builder, filter);
        }
    }

    private void addFilter(SearchOptionsBuilder builder, Filter<?> filter) {
        if (!StringUtil.isBlank(filter.getField())) {
            if (filter.getOperator() == Filter.Operator.DIFFERENT_FROM) {
                builder.differentFrom(filter.getField(), filter.getValue());
            } else {
                builder.filter(filter.getField(), filter.getValue());
            }

        }
    }

    public SearchOptions create() {
        return builder.done();
    }

    public SearchOptionsBuilder getBuilder() {
        return builder;
    }
}
