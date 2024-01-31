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
package org.bonitasoft.web.rest.server.datastore.bpm.cases;

import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.cases.CaseDefinition;
import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.engineclient.EngineAPIAccessor;
import org.bonitasoft.web.rest.server.engineclient.EngineClientFactory;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasAdd;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasDelete;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.rest.server.framework.utils.SearchOptionsBuilderUtil;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 * @author Celine Souchet
 */
public class CaseDatastore extends CommonDatastore<CaseItem, ProcessInstance>
        implements DatastoreHasGet<CaseItem>, DatastoreHasSearch<CaseItem>,
        DatastoreHasDelete, DatastoreHasAdd<CaseItem> {

    public CaseDatastore(final APISession engineSession) {
        super(engineSession);
    }

    @Override
    protected CaseItem convertEngineToConsoleItem(final ProcessInstance item) {
        return new CaseItemConverter().convert(item);
    }

    public long count(final String search, final String orders, final Map<String, String> filters) {
        return search(0, 0, search, orders, filters).getTotal();
    }

    /**
     * convenience for stubbing during unit test
     *
     * @see org.bonitasoft.web.rest.server.datastore.CommonDatastore#convertEngineToConsoleSearch(int, int,
     *      org.bonitasoft.engine.search.SearchResult)
     */
    @Override
    protected ItemSearchResult<CaseItem> convertEngineToConsoleSearch(final int page, final int resultsByPage,
            final SearchResult<ProcessInstance> engineSearchResults) {
        return super.convertEngineToConsoleSearch(page, resultsByPage, engineSearchResults);
    }

    @Override
    public ItemSearchResult<CaseItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        try {
            final SearchOptionsBuilder builder = buildSearchOptions(page, resultsByPage, search, orders, filters);
            final SearchResult<ProcessInstance> searchResult = searchProcessInstances(filters, builder.done());
            return convertEngineToConsoleSearch(page, resultsByPage, searchResult);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    protected SearchOptionsBuilder buildSearchOptions(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        // Build search
        final SearchOptionsBuilder builder = SearchOptionsBuilderUtil.buildSearchOptions(page, resultsByPage, orders,
                search);
        addLongFilterToSearchBuilder(filters, builder, CaseItem.ATTRIBUTE_START_DATE,
                ProcessInstanceSearchDescriptor.START_DATE);
        addLongFilterToSearchBuilder(filters, builder, CaseItem.ATTRIBUTE_END_DATE,
                ProcessInstanceSearchDescriptor.END_DATE);
        addLongFilterToSearchBuilder(filters, builder, CaseItem.ATTRIBUTE_LAST_UPDATE_DATE,
                ProcessInstanceSearchDescriptor.LAST_UPDATE);
        addLongFilterToSearchBuilder(filters, builder, CaseItem.ATTRIBUTE_PROCESS_ID,
                ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID);
        addStringFilterToSearchBuilder(filters, builder, CaseItem.ATTRIBUTE_PROCESS_NAME,
                ProcessInstanceSearchDescriptor.NAME);
        addLongFilterToSearchBuilder(filters, builder, CaseItem.ATTRIBUTE_STARTED_BY_USER_ID,
                ProcessInstanceSearchDescriptor.STARTED_BY);
        addCallerFilterToSearchBuilderIfNecessary(filters, builder);
        builder.differentFrom(ProcessInstanceSearchDescriptor.STATE_ID, ProcessInstanceState.COMPLETED.getId());
        builder.differentFrom(ProcessInstanceSearchDescriptor.STATE_ID, ProcessInstanceState.CANCELLED.getId());
        builder.differentFrom(ProcessInstanceSearchDescriptor.STATE_ID, ProcessInstanceState.ABORTED.getId());
        return builder;
    }

    void addCallerFilterToSearchBuilderIfNecessary(final Map<String, String> filters,
            final SearchOptionsBuilder builder) {
        /*
         * By default we add a caller filter of -1 to avoid having sub processes.
         * If caller is forced to any then we don't need to add the filter.
         */
        if (!filters.containsKey(CaseItem.FILTER_CALLER)) {
            builder.filter(ProcessInstanceSearchDescriptor.CALLER_ID, -1);
        } else if (!"any".equalsIgnoreCase(filters.get(CaseItem.FILTER_CALLER))) {
            builder.filter(ProcessInstanceSearchDescriptor.CALLER_ID,
                    MapUtil.getValueAsLong(filters, CaseItem.FILTER_CALLER));
        }
    }

    private SearchResult<ProcessInstance> searchProcessInstances(final Map<String, String> filters,
            final SearchOptions searchOptions) throws BonitaException {
        final ProcessAPI processAPI = getProcessAPI();

        if (filters.containsKey(CaseItem.FILTER_USER_ID)) {
            return processAPI.searchOpenProcessInstancesInvolvingUser(
                    MapUtil.getValueAsLong(filters, CaseItem.FILTER_USER_ID), searchOptions);
        }
        if (filters.containsKey(CaseItem.FILTER_SUPERVISOR_ID)) {
            if (filters.containsKey(CaseItem.FILTER_STATE)
                    && ("failed".equalsIgnoreCase(filters.get(CaseItem.FILTER_STATE))
                            || "error".equalsIgnoreCase(filters.get(CaseItem.FILTER_STATE)))) {
                return processAPI.searchFailedProcessInstancesSupervisedBy(
                        MapUtil.getValueAsLong(filters, CaseItem.FILTER_SUPERVISOR_ID), searchOptions);
            } else {
                return processAPI.searchOpenProcessInstancesSupervisedBy(
                        MapUtil.getValueAsLong(filters, CaseItem.FILTER_SUPERVISOR_ID), searchOptions);
            }
        }
        if (filters.containsKey(CaseItem.FILTER_STATE)
                && ("failed".equalsIgnoreCase(filters.get(CaseItem.FILTER_STATE))
                        || "error".equalsIgnoreCase(filters.get(CaseItem.FILTER_STATE)))) {
            return processAPI.searchFailedProcessInstances(searchOptions);
        }

        return processAPI.searchProcessInstances(searchOptions);
    }

    @Override
    public CaseItem get(final APIID id) {
        try {
            return convertEngineToConsoleItem(getProcessAPI().getProcessInstance(id.toLong()));
        } catch (final ProcessInstanceNotFoundException e) {
            return null;
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    public void delete(final List<APIID> ids) {
        try {
            final ProcessAPI processApi = getProcessAPI();
            for (final APIID id : ids) {
                processApi.deleteProcessInstance(id.toLong());
                processApi.deleteArchivedProcessInstancesInAllStates(id.toLong());
            }
        } catch (final BonitaException e) {
            if (e.getCause() instanceof ProcessInstanceNotFoundException) {
                throw new APIItemNotFoundException(CaseDefinition.TOKEN);
            } else {
                throw new APIException(e);
            }
        }
    }

    @Override
    public CaseItem add(final CaseItem caseItem) {
        final EngineClientFactory factory = new EngineClientFactory(new EngineAPIAccessor(getEngineSession()));
        return new CaseSarter(caseItem, factory.createCaseEngineClient(), factory.createProcessEngineClient()).start();
    }

    public ProcessAPI getProcessAPI() throws BonitaException {
        return TenantAPIAccessor.getProcessAPI(getEngineSession());
    }

}
