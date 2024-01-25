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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.data.item.IItem;

/**
 * @author Julien Mege
 */
public abstract class CommonDatastore<C extends IItem, E extends Serializable> extends Datastore {

    private APISession engineSession;

    /**
     * Default Constructor.
     *
     * @param engineSession
     *        The session that will allow to access the engine SDK
     */
    public CommonDatastore(final APISession engineSession) {
        this.engineSession = engineSession;
    }

    /**
     * @return the engineSession
     */
    protected final APISession getEngineSession() {
        return this.engineSession;
    }

    protected void addStringFilterToSearchBuilder(final Map<String, String> filters, final SearchOptionsBuilder builder,
            final String filterName,
            final String engineAttributeName) {
        if (filters != null && filters.containsKey(filterName)) {
            final String filterValue = filters.get(filterName);
            if (filterValue != null) {
                if (filterValue.startsWith(">")) {
                    builder.greaterThan(engineAttributeName, getFilterValueWithoutFirstCharacter(filterValue));
                } else if (filterValue.startsWith("<")) {
                    builder.lessThan(engineAttributeName, getFilterValueWithoutFirstCharacter(filterValue));
                } else {
                    builder.filter(engineAttributeName, filterValue);
                }
            }
        }
    }

    private String getFilterValueWithoutFirstCharacter(final String filterValue) {
        return filterValue.substring(1).trim();
    }

    protected void addLongFilterToSearchBuilder(final Map<String, String> filters, final SearchOptionsBuilder builder,
            final String filterName,
            final String engineAttributeName) {
        if (filters != null && filters.containsKey(filterName)) {
            final String filterValue = filters.get(filterName);
            if (filterValue != null) {
                if (filterValue.startsWith(">")) {
                    builder.greaterThan(engineAttributeName,
                            Long.valueOf(getFilterValueWithoutFirstCharacter(filterValue)));
                } else if (filterValue.startsWith("<")) {
                    builder.lessThan(engineAttributeName,
                            Long.valueOf(getFilterValueWithoutFirstCharacter(filterValue)));
                } else {
                    builder.filter(engineAttributeName, Long.valueOf(filterValue));
                }
            }
        }
    }

    protected abstract C convertEngineToConsoleItem(E item);

    protected ItemSearchResult<C> convertEngineToConsoleSearch(final int page, final int resultsByPage,
            final SearchResult<E> engineSearchResults) {
        return new ItemSearchResult<>(
                page,
                resultsByPage,
                engineSearchResults.getCount(),
                convertEngineToConsoleItemsList(engineSearchResults.getResult()));
    }

    protected List<C> convertEngineToConsoleItemsList(final List<E> engineSearchResults) {

        final List<C> consoleSearchResults = new ArrayList<>();

        for (final E engineItem : engineSearchResults) {
            consoleSearchResults.add(convertEngineToConsoleItem(engineItem));
        }
        return consoleSearchResults;
    }

}
