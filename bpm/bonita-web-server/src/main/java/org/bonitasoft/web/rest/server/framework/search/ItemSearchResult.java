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
package org.bonitasoft.web.rest.server.framework.search;

import java.util.List;

import org.bonitasoft.web.toolkit.client.common.exception.api.APISearchIndexOutOfRange;
import org.bonitasoft.web.toolkit.client.data.item.IItem;

/**
 * @author Séverin Moussel
 */
public class ItemSearchResult<T extends IItem> {

    private final int page;

    private final int length;

    private final long total;

    private final List<T> results;

    public ItemSearchResult(final int page, final int length, final long total, final List<T> results) {
        this.page = page;
        this.length = length;
        this.total = total;
        this.results = results;

        if (page < 0 || page > total) {
            throw new APISearchIndexOutOfRange(page);
        }
    }

    /**
     * @return the page
     */
    public int getPage() {
        return this.page;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return this.length;
    }

    /**
     * @return the total
     */
    public long getTotal() {
        return this.total;
    }

    /**
     * @return the results
     */
    public List<T> getResults() {
        return this.results;
    }

}
