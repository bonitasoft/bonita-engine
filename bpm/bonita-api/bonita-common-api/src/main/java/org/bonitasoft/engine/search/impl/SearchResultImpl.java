/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
import java.util.List;

import org.bonitasoft.engine.search.SearchResult;

/**
 * @author Emmanuel Duchastenier
 */
public class SearchResultImpl<T extends Serializable> implements SearchResult<T> {

    private static final long serialVersionUID = -685595668360293014L;

    private final long count;

    private final List<T> list;

    public SearchResultImpl(final long count, final List<T> list) {
        super();
        this.count = count;
        this.list = list;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public List<T> getResult() {
        return list;
    }

}
