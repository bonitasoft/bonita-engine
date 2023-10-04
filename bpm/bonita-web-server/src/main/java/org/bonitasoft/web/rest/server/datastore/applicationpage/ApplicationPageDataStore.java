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
package org.bonitasoft.web.rest.server.datastore.applicationpage;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.ApplicationPageNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.applicationpage.ApplicationPageDefinition;
import org.bonitasoft.web.rest.model.applicationpage.ApplicationPageItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasDelete;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Julien Mege
 */
public class ApplicationPageDataStore extends CommonDatastore<ApplicationPageItem, ApplicationPage>
        implements DatastoreHasAdd<ApplicationPageItem>,
        DatastoreHasGet<ApplicationPageItem>, DatastoreHasSearch<ApplicationPageItem>, DatastoreHasDelete {

    private final ApplicationAPI applicationAPI;
    private final ApplicationPageItemConverter converter;

    public ApplicationPageDataStore(final APISession engineSession, final ApplicationAPI applicationAPI,
            final ApplicationPageItemConverter converter) {
        super(engineSession);
        this.applicationAPI = applicationAPI;
        this.converter = converter;
    }

    /**
     * @deprecated as of 9.0.0, Application page should be created at startup.
     */
    @Override
    @Deprecated(since = "9.0.0")
    public ApplicationPageItem add(final ApplicationPageItem item) {
        try {
            final ApplicationPage applicationPage = applicationAPI.createApplicationPage(
                    item.getApplicationId().toLong(), item.getPageId().toLong(),
                    item.getToken());
            return converter.toApplicationPageItem(applicationPage);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    public ApplicationPageItem get(final APIID id) {
        try {
            final ApplicationPage applicationPage = applicationAPI.getApplicationPage(id.toLong());
            return converter.toApplicationPageItem(applicationPage);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    public void delete(final List<APIID> ids) {
        try {
            for (final APIID id : ids) {
                applicationAPI.deleteApplicationPage(id.toLong());
            }
        } catch (final BonitaException e) {
            if (e.getCause() instanceof ApplicationPageNotFoundException) {
                throw new APIItemNotFoundException(ApplicationPageDefinition.TOKEN);
            } else {
                throw new APIException(e);
            }
        }
    }

    protected ApplicationPageSearchDescriptorConverter getSearchDescriptorConverter() {
        return new ApplicationPageSearchDescriptorConverter();
    }

    @Override
    protected ApplicationPageItem convertEngineToConsoleItem(final ApplicationPage item) {
        return converter.toApplicationPageItem(item);
    }

    @Override
    public ItemSearchResult<ApplicationPageItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        final SearchOptionsCreator creator = makeSearchOptionCreator(page, resultsByPage, search, orders, filters);
        try {
            final SearchResult<ApplicationPage> searchResult = runSearch(creator);
            final List<ApplicationPageItem> appPageItems = convertEngineToConsoleItemsList(searchResult.getResult());
            return new ItemSearchResult<>(page, resultsByPage, searchResult.getCount(),
                    appPageItems);
        } catch (final SearchException e) {
            throw new APIException(e);
        }
    }

    protected SearchOptionsCreator makeSearchOptionCreator(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        return new SearchOptionsCreator(page, resultsByPage, search, new Sorts(orders, getSearchDescriptorConverter()),
                new Filters(filters,
                        new ApplicationPageFilterCreator(getSearchDescriptorConverter())));
    }

    protected SearchResult<ApplicationPage> runSearch(final SearchOptionsCreator creator) throws SearchException {
        return applicationAPI.searchApplicationPages(creator.create());
    }
}
