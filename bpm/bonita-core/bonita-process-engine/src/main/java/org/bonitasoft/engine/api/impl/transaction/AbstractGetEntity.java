/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContentWithResult;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.search.Sort;
import org.bonitasoft.engine.search.descriptor.SearchEntityDescriptor;

/**
 * Abstract class to allow to search server object and convert them to client object
 * first generic C is the client object
 * second generic S is the server object
 * 
 * @author Celine Souchet
 */
public abstract class AbstractGetEntity<C extends Serializable, S extends PersistentObject> implements TransactionContentWithResult<List<C>> {

    private final SearchEntityDescriptor searchDescriptor;

    private final int numberOfResults;

    private final int fromIndex;

    private final Sort sort;

    private List<C> clientObjects;

    public AbstractGetEntity(final SearchEntityDescriptor searchDescriptor, final int fromIndex, final int numberOfResults, final Sort sort) {
        this.searchDescriptor = searchDescriptor;
        this.numberOfResults = numberOfResults;
        this.fromIndex = fromIndex;
        this.sort = sort;
    }

    @Override
    public void execute() throws SBonitaException {
        if (searchDescriptor == null || sort == null) {
            throw new SBonitaReadException("Sort and SearchDescriptor cannot be null");
        }

        final OrderByOption orderByOption = searchDescriptor.getEntityOrder(sort);
        final QueryOptions searchOptions = new QueryOptions(fromIndex, numberOfResults, Collections.singletonList(orderByOption));
        List<S> serverObjects = executeGet(searchOptions);
        clientObjects = convertToClientObjects(serverObjects);
    }

    /**
     * execute the search here
     * 
     * @param queryOptions
     *            query options to execute the search with
     * @return
     *         the search result
     * @throws SBonitaException
     */
    public abstract List<S> executeGet(final QueryOptions queryOptions) throws SBonitaException;

    /**
     * Must convert server objects in client objects here
     * 
     * @param serverObjects
     *            server object to convert
     * @return
     */
    public abstract List<C> convertToClientObjects(final List<S> serverObjects);

    @Override
    public List<C> getResult() {
        return clientObjects;
    }

}
