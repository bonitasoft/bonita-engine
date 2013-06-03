/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
 * a <code>SearchOptions</code> heps define the generic options of the search mechanism.
 * Use SearchOptionsBuilder to build a SearchOptions object.
 * 
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 * @see SearchOptionsBuilder
 */
public interface SearchOptions extends Serializable {

    List<SearchFilter> getFilters();

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

    List<Sort> getSorts();

}
