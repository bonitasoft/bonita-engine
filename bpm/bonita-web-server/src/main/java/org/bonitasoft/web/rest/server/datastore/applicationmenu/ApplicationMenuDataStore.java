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
package org.bonitasoft.web.rest.server.datastore.applicationmenu;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.business.application.ApplicationMenu;
import org.bonitasoft.engine.business.application.ApplicationMenuNotFoundException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.applicationmenu.ApplicationMenuDefinition;
import org.bonitasoft.web.rest.model.applicationmenu.ApplicationMenuItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasDelete;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasUpdate;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Julien Mege
 */
public class ApplicationMenuDataStore extends CommonDatastore<ApplicationMenuItem, ApplicationMenu>
        implements DatastoreHasAdd<ApplicationMenuItem>,
        DatastoreHasUpdate<ApplicationMenuItem>,
        DatastoreHasGet<ApplicationMenuItem>, DatastoreHasSearch<ApplicationMenuItem>, DatastoreHasDelete {

    private final ApplicationAPI applicationAPI;
    private final ApplicationMenuItemConverter converter;

    public ApplicationMenuDataStore(final APISession engineSession, final ApplicationAPI applicationAPI,
            final ApplicationMenuItemConverter converter) {
        super(engineSession);
        this.applicationAPI = applicationAPI;
        this.converter = converter;
    }

    @Override
    public ApplicationMenuItem add(final ApplicationMenuItem item) {
        try {
            final ApplicationMenu applicationMenu = applicationAPI
                    .createApplicationMenu(converter.toApplicationMenuCreator(item));
            return converter.toApplicationMenuItem(applicationMenu);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public ApplicationMenuItem update(final APIID id, final Map<String, String> attributes) {
        try {
            final ApplicationMenu applicationMenu = applicationAPI.updateApplicationMenu(id.toLong(),
                    converter.toApplicationMenuUpdater(attributes));
            return converter.toApplicationMenuItem(applicationMenu);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public ApplicationMenuItem get(final APIID id) {
        try {
            final ApplicationMenu applicationMenu = applicationAPI.getApplicationMenu(id.toLong());
            return converter.toApplicationMenuItem(applicationMenu);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public void delete(final List<APIID> ids) {
        try {
            for (final APIID id : ids) {
                applicationAPI.deleteApplicationMenu(id.toLong());
            }
        } catch (final Exception e) {
            if (e.getCause() instanceof ApplicationMenuNotFoundException) {
                throw new APIItemNotFoundException(ApplicationMenuDefinition.TOKEN);
            } else {
                throw new APIException(e);
            }
        }
    }

    protected ApplicationMenuSearchDescriptorConverter getSearchDescriptorConverter() {
        return new ApplicationMenuSearchDescriptorConverter();
    }

    @Override
    protected ApplicationMenuItem convertEngineToConsoleItem(final ApplicationMenu item) {
        return new ApplicationMenuItemConverter().toApplicationMenuItem(item);
    }

    @Override
    public ItemSearchResult<ApplicationMenuItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        final SearchOptionsCreator creator = makeSearchOptionCreator(page, resultsByPage, search, orders, filters);
        try {
            final SearchResult<ApplicationMenu> searchResult = runSearch(creator);
            final List<ApplicationMenuItem> appMenuItems = convertEngineToConsoleItemsList(searchResult.getResult());
            return new ItemSearchResult<>(page, resultsByPage, searchResult.getCount(),
                    appMenuItems);
        } catch (final SearchException e) {
            throw new APIException(e);
        }
    }

    protected SearchOptionsCreator makeSearchOptionCreator(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        return new SearchOptionsCreator(page, resultsByPage, search, new Sorts(orders, getSearchDescriptorConverter()),
                new Filters(filters,
                        new ApplicationMenuFilterCreator(getSearchDescriptorConverter())));
    }

    protected SearchResult<ApplicationMenu> runSearch(final SearchOptionsCreator creator) throws SearchException {
        return applicationAPI.searchApplicationMenus(creator.create());
    }

}
