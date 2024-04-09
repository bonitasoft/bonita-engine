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
package org.bonitasoft.web.rest.server.datastore.converter;

import java.io.Serializable;

import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.data.item.IItem;

/**
 * @author Vincent Elcrin
 */
public class ItemSearchResultConverter<I extends IItem, E extends Serializable> {

    private final int page;
    private final SearchResult<E> result;
    private final ItemConverter<I, E> converter;
    private final long total;
    private final int nbResultsByPage;

    public ItemSearchResultConverter(int page, int nbResultsByPage, SearchResult<E> result,
            ItemConverter<I, E> converter) {
        this(page, nbResultsByPage, result, result.getCount(), converter);
    }

    public ItemSearchResultConverter(int page, int nbResultsByPage, SearchResult<E> result, long total,
            ItemConverter<I, E> converter) {
        this.page = page;
        this.nbResultsByPage = nbResultsByPage;
        this.result = result;
        this.total = total;
        this.converter = converter;
    }

    public ItemSearchResult<I> toItemSearchResult() {
        return new ItemSearchResult<>(page, nbResultsByPage, total, converter.convert(result.getResult()));
    }
}
