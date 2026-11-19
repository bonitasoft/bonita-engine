/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server;

import java.util.List;

import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.filter.FormMappingTypeCreator;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;

public class APIPaginationUtils {

    /**
     * Build search options from query parameters.
     *
     * @param page the page number (0-indexed)
     * @param count the number of results per page
     * @param search the search term
     * @param order the sort order
     * @param filters the filters
     * @return the search options
     */
    public static SearchOptions buildSearchOptions(int page, int count, String search, String order,
            List<String> filters) {
        Filters parsedFilters = new Filters(QueryParameterUtils.parseFilters(filters), new FormMappingTypeCreator());
        Sorts sorts = new Sorts(order);
        return new SearchOptionsCreator(page, count, search, sorts, parsedFilters).create();
    }

    /**
     * Build Content-Range header value.
     *
     * @param page the page number
     * @param countOnCurrentPage the page size
     * @param total the total count
     * @return the Content-Range header value
     */
    public static String buildContentRange(int page, int countOnCurrentPage, long total) {
        return page + "-" + countOnCurrentPage + "/" + total;
    }
}
