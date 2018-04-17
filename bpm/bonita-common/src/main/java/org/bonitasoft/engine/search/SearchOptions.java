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

/**
 * A <code>SearchOptions</code> object helps define the generic options of the search mechanism.
 * A <code>SearchOptions</code> has a 'start index' field and a 'max results' field that define where to start and where to stop to return results that match
 * the provided search criteria.
 * It is composed of a list of <code>SearchFilter</code> objects defining the restrictive criteria that a result must match to fulfill the search.
 * It is also composed of a 'search term', which is a free text that can be search for in a certain amount of fields, depending on what object is the search
 * applied on.
 * Finally, a search can define a list of {@link Sort} options to define the order in which the matching results will be returned.
 * Use {@link SearchOptionsBuilder} to build a SearchOptions object.
 * 
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 * @see SearchOptionsBuilder
 * @see SearchResult
 */
public interface SearchOptions extends Serializable {

    /**
     * Gets the list of <code>SearchFilter</code> objects defining the restrictive criteria that a result must match to fulfill the search.
     * 
     * @return the list of <code>SearchFilter</code> objects
     */
    List<SearchFilter> getFilters();

    /**
     * Gets the search term (free text that can be search for in a certain amount of properties, depending on what object is the search applied on)
     * 
     * @return the search term (as a String) that will be searched for.
     */
    String getSearchTerm();

    /**
     * The result start index, that is the first result that matches the search criteria that we want to retrieve.
     * 
     * @return the defined start index
     */
    int getStartIndex();

    /**
     * The maximum results to return. The actual number can be smaller, if the end of the list has been reached.
     * 
     * @return The maximum number of results
     */
    int getMaxResults();

    /**
     * Gets the list of sort criteria
     * 
     * @return the list of <code>Sort</code> to order the results
     */
    List<Sort> getSorts();

}
