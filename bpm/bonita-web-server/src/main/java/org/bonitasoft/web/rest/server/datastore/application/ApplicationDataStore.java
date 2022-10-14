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
package org.bonitasoft.web.rest.server.datastore.application;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ApplicationAPI;
import org.bonitasoft.engine.api.PageAPI;
import org.bonitasoft.engine.business.application.Application;
import org.bonitasoft.engine.business.application.ApplicationCreator;
import org.bonitasoft.engine.business.application.ApplicationNotFoundException;
import org.bonitasoft.engine.business.application.ApplicationPage;
import org.bonitasoft.engine.business.application.ApplicationUpdater;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.page.Page;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.application.ApplicationDefinition;
import org.bonitasoft.web.rest.model.application.ApplicationItem;
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
 * @author Elias Ricken de Medeiros
 */
public class ApplicationDataStore extends CommonDatastore<ApplicationItem, Application>
        implements DatastoreHasAdd<ApplicationItem>,
        DatastoreHasUpdate<ApplicationItem>,
        DatastoreHasGet<ApplicationItem>, DatastoreHasSearch<ApplicationItem>, DatastoreHasDelete {

    private final ApplicationAPI applicationAPI;
    private final ApplicationItemConverter converter;
    private final PageAPI pageAPI;
    private static final String CUSTOMPAGE_HOME = "custompage_home";

    public ApplicationDataStore(final APISession engineSession, final ApplicationAPI applicationAPI,
            final PageAPI pageAPI, final ApplicationItemConverter converter) {
        super(engineSession);
        this.applicationAPI = applicationAPI;
        this.pageAPI = pageAPI;
        this.converter = converter;
    }

    @Override
    public void delete(final List<APIID> ids) {
        try {
            for (final APIID id : ids) {
                applicationAPI.deleteApplication(id.toLong());
            }
        } catch (final Exception e) {
            if (e.getCause() instanceof ApplicationNotFoundException) {
                throw new APIItemNotFoundException(ApplicationDefinition.TOKEN);
            } else {
                throw new APIException(e);
            }
        }
    }

    @Override
    public ApplicationItem get(final APIID id) {
        try {
            final Application application = applicationAPI.getApplication(id.toLong());
            return converter.toApplicationItem(application);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public ApplicationItem add(final ApplicationItem item) {

        try {
            final Page homePageDef = pageAPI.getPageByName(CUSTOMPAGE_HOME);

            final ApplicationCreator creator = converter.toApplicationCreator(item);

            final Application application = applicationAPI.createApplication(creator);
            final ApplicationPage appHomePage = applicationAPI.createApplicationPage(application.getId(),
                    homePageDef.getId(), "home");
            applicationAPI.setApplicationHomePage(application.getId(), appHomePage.getId());
            return converter.toApplicationItem(application);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public ApplicationItem update(final APIID id, final Map<String, String> attributes) {
        try {
            final ApplicationUpdater applicationUpdater = converter.toApplicationUpdater(attributes);
            final Application application = applicationAPI.updateApplication(id.toLong(), applicationUpdater);
            return converter.toApplicationItem(application);
        } catch (final Exception e) {
            throw new APIException(e);
        }
    }

    @Override
    public ItemSearchResult<ApplicationItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        // Build search
        final SearchOptionsCreator creator = makeSearchOptionCreator(page, resultsByPage, search, orders, filters);

        // Run search depending on filters passed
        SearchResult<Application> searchResult;
        try {
            searchResult = runSearch(creator);

            // Convert to ConsoleItems
            return new ItemSearchResult<>(page, resultsByPage, searchResult.getCount(),
                    convertEngineToConsoleItemsList(searchResult.getResult()));
        } catch (final SearchException e) {
            throw new APIException(e);
        }

    }

    protected SearchOptionsCreator makeSearchOptionCreator(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        return new SearchOptionsCreator(page, resultsByPage, search, new Sorts(orders, getSearchDescriptorConverter()),
                new Filters(filters,
                        new ApplicationFilterCreator(getSearchDescriptorConverter())));
    }

    protected SearchResult<Application> runSearch(final SearchOptionsCreator creator) throws SearchException {
        return applicationAPI.searchApplications(creator.create());
    }

    protected ApplicationSearchDescriptorConverter getSearchDescriptorConverter() {
        return new ApplicationSearchDescriptorConverter();
    }

    @Override
    protected ApplicationItem convertEngineToConsoleItem(final Application item) {
        return new ApplicationItemConverter().toApplicationItem(item);
    }

}
