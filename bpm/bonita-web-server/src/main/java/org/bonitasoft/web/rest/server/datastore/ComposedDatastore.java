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
package org.bonitasoft.web.rest.server.datastore;

import java.util.List;
import java.util.Map;

import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasDelete;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasUpdate;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIMethodNotAllowedException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.IItem;

/**
 * @author Vincent Elcrin
 */
public class ComposedDatastore<T extends IItem> extends Datastore
        implements
        DatastoreHasGet<T>,
        DatastoreHasSearch<T>,
        DatastoreHasAdd<T>,
        DatastoreHasUpdate<T>,
        DatastoreHasDelete {

    private DatastoreHasGet<T> getHelper;

    private DatastoreHasSearch<T> searchHelper;

    private DatastoreHasUpdate<T> updateHelper;

    private DatastoreHasAdd<T> addHelper;

    private DatastoreHasDelete deleteHelper;

    @Override
    public T get(APIID id) {
        if (getHelper != null) {
            return getHelper.get(id);
        }
        throw new APIMethodNotAllowedException("GET method not allowed.");
    }

    @Override
    public ItemSearchResult<T> search(int page, int resultsByPage, String search, String orders,
            Map<String, String> filters) {
        if (searchHelper != null) {
            return searchHelper.search(page, resultsByPage, search, orders, filters);
        }
        throw new APIMethodNotAllowedException("SEARCH method not allowed.");
    }

    @Override
    public T add(T item) {
        if (addHelper != null) {
            return addHelper.add(item);
        }
        throw new APIMethodNotAllowedException("ADD method not allowed.");
    }

    @Override
    public T update(APIID id, Map<String, String> attributes) {
        if (updateHelper != null) {
            return updateHelper.update(id, attributes);
        }
        throw new APIMethodNotAllowedException("UPDATE method not allowed.");
    }

    @Override
    public void delete(List<APIID> ids) {
        if (deleteHelper != null) {
            deleteHelper.delete(ids);
        } else {
            throw new APIMethodNotAllowedException("DELETE method not allowed.");
        }
    }

    public void setGetHelper(DatastoreHasGet<T> getHelper) {
        this.getHelper = getHelper;
    }

    public void setSearchHelper(DatastoreHasSearch<T> searchHelper) {
        this.searchHelper = searchHelper;
    }

    public void setAddHelper(DatastoreHasAdd<T> addHelper) {
        this.addHelper = addHelper;
    }

    public void setDeleteHelper(DatastoreHasDelete deleteHelper) {
        this.deleteHelper = deleteHelper;
    }
}
