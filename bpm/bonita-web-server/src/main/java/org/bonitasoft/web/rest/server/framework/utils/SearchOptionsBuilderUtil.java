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
package org.bonitasoft.web.rest.server.framework.utils;

import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;

/**
 * @author Haojie Yuan
 */
public class SearchOptionsBuilderUtil {

    /**
     * build SearchOptionsBuilder
     *
     * @deprecated use org.bonitasoft.web.rest.server.credentials.utils.SearchOptionsCreator
     */
    @Deprecated
    public static SearchOptionsBuilder buildSearchOptions(final int pageIndex, final int numberOfResults,
            final String sort, final String search) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(computeIndex(pageIndex, numberOfResults),
                numberOfResults);
        if (sort != null) {
            final String[] order = sort.split(" ");
            if (order.length == 2) {
                builder.sort(order[0], Order.valueOf(order[1].toUpperCase()));
            }
        }
        if (search != null && !search.isEmpty()) {
            builder.searchTerm(search);
        }
        return builder;
    }

    public static int computeIndex(int page, int resultsByPage) {
        return page * resultsByPage;
    }
}
