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

/**
 * Represents the result of a Search. For details on 'Search mechanism', see {@link SearchOptionsBuilder} and {@link SearchOptions}.
 * A <code>SearchResult</code> is composed of a result list {@link #getResult()} that is the paginated list of results matching the provided criteria, and a
 * result count {@link #getCount()} that is the total number of results matching the provided criteria.
 * 
 * @param <T>
 *            the type of the objects being returned by the search.
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 * @author Elias Ricken de Medeiros
 * @see SearchOptions
 * @see SearchOptionsBuilder
 */
public interface SearchResult<T extends Serializable> extends Serializable {

    /**
     * Get the total number of matching result in the data base. This number can be greater than the number of elements retrieved in the search depending on
     * paging criterion.
     * 
     * @return The total number of matching result in the data base.
     * @since 6.0
     */
    long getCount();

    /**
     * Get the list of elements retrieved by the search.
     * 
     * @return The list of elements retrieved by the search.
     * @since 6.0
     */
    List<T> getResult();

}
