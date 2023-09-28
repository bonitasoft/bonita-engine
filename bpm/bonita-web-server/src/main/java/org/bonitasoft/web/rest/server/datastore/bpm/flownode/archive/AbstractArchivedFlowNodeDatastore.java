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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode.archive;

import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceNotFoundException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedFlowNodeItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.FlowNodeConverter;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.archive.converter.ArchivedFlowNodeSearchDescriptorConverter;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public abstract class AbstractArchivedFlowNodeDatastore<CONSOLE_ITEM extends ArchivedFlowNodeItem, ENGINE_ITEM extends ArchivedFlowNodeInstance>
        extends CommonDatastore<CONSOLE_ITEM, ENGINE_ITEM> implements
        DatastoreHasGet<CONSOLE_ITEM>,
        DatastoreHasSearch<CONSOLE_ITEM> {

    protected final String token;

    public AbstractArchivedFlowNodeDatastore(final APISession engineSession, String token) {
        super(engineSession);
        this.token = token;
    }

    /**
     * Fill a console item using the engine item passed.
     *
     * @param result
     *        The console item to fill
     * @param item
     *        The engine item to use for filling
     * @return This method returns the result parameter passed.
     */
    public static ArchivedFlowNodeItem fillConsoleItem(final ArchivedFlowNodeItem result,
            final ArchivedFlowNodeInstance item) {
        result.setId(item.getId());
        result.setName(item.getName());
        result.setDisplayName(item.getDisplayName());
        result.setDescription(item.getDescription());
        result.setDisplayDescription(item.getDisplayDescription());
        result.setExecutedByUserId(item.getExecutedBy());
        result.setRootCaseId(item.getRootContainerId());
        result.setParentCaseId(item.getProcessInstanceId());
        result.setProcessId(item.getProcessDefinitionId());
        result.setState(item.getState());
        result.setType(item.getType().name());
        result.setArchivedDate(item.getArchiveDate());
        result.setSourceObjectId(item.getSourceObjectId());
        result.setRootContainerId(item.getRootContainerId());
        result.setExecutedBySubstituteUserId(item.getExecutedBySubstitute());
        return result;
    }

    protected ProcessAPI getProcessAPI() {
        try {
            return TenantAPIAccessor.getProcessAPI(getEngineSession());
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    protected CONSOLE_ITEM convertEngineToConsoleItem(final ENGINE_ITEM item) {

        @SuppressWarnings("unchecked")
        final CONSOLE_ITEM result = (CONSOLE_ITEM) FlowNodeConverter.convertEngineToConsoleItem(item);

        return result;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // C.R.U.D.S
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // GET

    @Override
    public CONSOLE_ITEM get(final APIID id) {
        final ENGINE_ITEM archivedFlowNodeInstance = runGet(id);
        return convertEngineToConsoleItem(archivedFlowNodeInstance);
    }

    @SuppressWarnings("unchecked")
    protected ENGINE_ITEM runGet(final APIID id) {
        try {
            return (ENGINE_ITEM) getProcessAPI().getArchivedFlowNodeInstance(id.toLong());
        } catch (ArchivedFlowNodeInstanceNotFoundException e) {
            throw new APIItemNotFoundException(this.token, id);
        }
    }

    @Override
    public ItemSearchResult<CONSOLE_ITEM> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        final SearchOptionsCreator creator = makeSearchOptionCreator(page, resultsByPage, search, orders, filters);

        final SearchResult<ENGINE_ITEM> results = runSearch(creator, filters);

        return new ItemSearchResult<>(
                page,
                resultsByPage,
                results.getCount(),
                convertEngineToConsoleItemsList(results.getResult()));
    }

    /**
     * Run the engine API search method
     */
    protected SearchResult<ENGINE_ITEM> runSearch(final SearchOptionsCreator creator,
            final Map<String, String> filters) {
        try {
            @SuppressWarnings("unchecked")
            final SearchResult<ENGINE_ITEM> result = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                    .searchArchivedFlowNodeInstances(
                            creator.create());

            return result;
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    protected SearchOptionsCreator makeSearchOptionCreator(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        return new SearchOptionsCreator(page,
                resultsByPage,
                search,
                new Sorts(orders, getSearchDescriptorConverter()),
                new Filters(filters, new ArchivedFlowNodeFilterCreator(getSearchDescriptorConverter())));
    }

    protected ArchivedFlowNodeSearchDescriptorConverter getSearchDescriptorConverter() {
        return new ArchivedFlowNodeSearchDescriptorConverter();
    }

}
