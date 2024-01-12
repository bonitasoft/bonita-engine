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
package org.bonitasoft.web.rest.server.datastore.filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.web.rest.server.datastore.converter.StringValueConverter;

/**
 * @author Vincent Elcrin
 */
public class Filters {

    private final List<Filter<?>> filters = new ArrayList<>();

    public Filters(final Map<String, String> filters, final FilterCreator filterCreator) {
        if (filters != null) {
            addFilters(filters, filterCreator);
        }
    }

    /**
     * Constructor that directly uses Engine filter keys, instead of having to transcode Web filter keys to Engine
     * filter keys.
     *
     * @param filters the filter key, value map
     */
    public Filters(final Map<String, String> filters) {
        if (filters != null) {
            final Iterator<Entry<String, String>> it = filters.entrySet().iterator();
            while (it.hasNext()) {
                final Entry<String, String> filterEntry = it.next();
                final Field field = new Field(filterEntry.getKey());
                final Value<String> fieldValue = new Value<>(filterEntry.getValue(), new StringValueConverter());
                final Filter<String> filter = new Filter<>(field, fieldValue);

                this.filters.add(filter);
            }
        }
    }

    private void addFilters(final Map<String, String> filters, final FilterCreator filterCreator) {
        final Iterator<Entry<String, String>> it = filters.entrySet().iterator();
        while (it.hasNext()) {
            addEntry(it.next(), filterCreator);
        }
    }

    private void addEntry(final Entry<String, String> entry, final FilterCreator filterCreator) {
        filters.add(createFilter(filterCreator, entry));
    }

    private Filter<?> createFilter(final FilterCreator filterCreator, final Entry<String, String> entry) {
        return filterCreator.create(entry.getKey(), entry.getValue());
    }

    public List<Filter<?>> asList() {
        return filters;
    }

}
