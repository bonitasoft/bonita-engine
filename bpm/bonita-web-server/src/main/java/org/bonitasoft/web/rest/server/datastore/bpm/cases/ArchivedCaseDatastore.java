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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.process.*;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseDefinition;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseItem;
import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
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
 */
public class ArchivedCaseDatastore extends CommonDatastore<ArchivedCaseItem, ArchivedProcessInstance>
        implements DatastoreHasGet<ArchivedCaseItem>,
        DatastoreHasSearch<ArchivedCaseItem>, DatastoreHasDelete {

    public ArchivedCaseDatastore(final APISession engineSession) {
        super(engineSession);
    }

    @Override
    protected ArchivedCaseItem convertEngineToConsoleItem(final ArchivedProcessInstance item) {

        final ArchivedCaseItem result = new ArchivedCaseItem();

        result.setId(item.getId());
        result.setLastUpdateDate(item.getLastUpdate());
        result.setState(item.getState());
        result.setStartDate(item.getStartDate());
        result.setEndDate(item.getEndDate());
        result.setProcessId(item.getProcessDefinitionId());
        result.setArchivedDate(item.getArchiveDate());
        result.setSourceObjectId(item.getSourceObjectId());
        result.setRootCaseId(item.getRootProcessInstanceId());
        result.setStartedByUserId(item.getStartedBy());
        result.setStartedBySubstituteUserId(item.getStartedBySubstitute());
        result.setSearchIndex1Label(item.getStringIndexLabel(1));
        result.setSearchIndex2Label(item.getStringIndexLabel(2));
        result.setSearchIndex3Label(item.getStringIndexLabel(3));
        result.setSearchIndex4Label(item.getStringIndexLabel(4));
        result.setSearchIndex5Label(item.getStringIndexLabel(5));
        result.setSearchIndex1Value(item.getStringIndexValue(1));
        result.setSearchIndex2Value(item.getStringIndexValue(2));
        result.setSearchIndex3Value(item.getStringIndexValue(3));
        result.setSearchIndex4Value(item.getStringIndexValue(4));
        result.setSearchIndex5Value(item.getStringIndexValue(5));
        return result;
    }

    @Override
    public ItemSearchResult<ArchivedCaseItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {

        final SearchOptionsBuilder builder = buildSearchOptions(page, resultsByPage, search, orders, filters);

        // Run search depending on filters passed
        final SearchResult<ArchivedProcessInstance> searchResult = runSearch(filters, builder);

        // Convert to ConsoleItems
        return new ItemSearchResult<>(
                page,
                resultsByPage,
                searchResult.getCount(),
                convertEngineToConsoleItemsList(searchResult.getResult()));
    }

    protected SearchOptionsBuilder buildSearchOptions(final int page, final int resultsByPage, final String search,
            final String orders, final Map<String, String> filters) {
        // Build search
        final SearchOptionsBuilder builder = SearchOptionsBuilderUtil.buildSearchOptions(page, resultsByPage, orders,
                search);

        addStringFilterToSearchBuilder(filters, builder, ArchivedCaseItem.ATTRIBUTE_PROCESS_NAME,
                ArchivedProcessInstancesSearchDescriptor.NAME);
        addLongFilterToSearchBuilder(filters, builder, ArchivedCaseItem.ATTRIBUTE_PROCESS_ID,
                ArchivedProcessInstancesSearchDescriptor.PROCESS_DEFINITION_ID);
        addLongFilterToSearchBuilder(filters, builder, ArchivedCaseItem.ATTRIBUTE_STARTED_BY_USER_ID,
                ArchivedProcessInstancesSearchDescriptor.STARTED_BY);
        addLongFilterToSearchBuilder(filters, builder, ArchivedCaseItem.ATTRIBUTE_SOURCE_OBJECT_ID,
                ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID);

        addCallerFilterToSearchBuilderIfNecessary(filters, builder);
        return builder;
    }

    void addCallerFilterToSearchBuilderIfNecessary(final Map<String, String> filters,
            final SearchOptionsBuilder builder) {
        /*
         * By default we add a caller filter of -1 to avoid having sub processes.
         * If caller is forced to any then we don't need to add the filter.
         */
        if (!filters.containsKey(CaseItem.FILTER_CALLER)) {
            builder.filter(ArchivedProcessInstancesSearchDescriptor.CALLER_ID, -1);
        } else if (!"any".equalsIgnoreCase(filters.get(CaseItem.FILTER_CALLER))) {
            builder.filter(ArchivedProcessInstancesSearchDescriptor.CALLER_ID,
                    MapUtil.getValueAsLong(filters, CaseItem.FILTER_CALLER));
        }
    }

    // Overridden for testing
    protected void addLongFilterToSearchBuilder(final Map<String, String> filters, final SearchOptionsBuilder builder,
            final String filterName,
            final String engineAttributeName) {
        super.addLongFilterToSearchBuilder(filters, builder, filterName, engineAttributeName);
    }

    // protected for testing
    protected SearchResult<ArchivedProcessInstance> runSearch(final Map<String, String> filters,
            final SearchOptionsBuilder builder) {

        try {
            final ProcessAPI processAPI = getProcessApi();

            if (filters.containsKey(ArchivedCaseItem.FILTER_USER_ID)) {
                return processAPI.searchArchivedProcessInstancesInvolvingUser(
                        MapUtil.getValueAsLong(filters, ArchivedCaseItem.FILTER_USER_ID),
                        builder.done());
            }

            if (filters.containsKey(ArchivedCaseItem.FILTER_SUPERVISOR_ID)) {
                return processAPI
                        .searchArchivedProcessInstancesSupervisedBy(
                                MapUtil.getValueAsLong(filters, ArchivedCaseItem.FILTER_SUPERVISOR_ID), builder.done());
            }

            if (filters.containsKey(CaseItem.FILTER_CALLER)) {
                builder.filter(ArchivedProcessInstancesSearchDescriptor.STATE_ID,
                        ProcessInstanceState.COMPLETED.getId());
                return processAPI.searchArchivedProcessInstancesInAllStates(builder.done());
            }

            return processAPI.searchArchivedProcessInstances(builder.done());
        } catch (final BonitaException e) {
            throw new APIException(e);
        }

    }

    @Override
    public ArchivedCaseItem get(final APIID id) {
        try {
            return convertEngineToConsoleItem(getProcessApi()
                    .getArchivedProcessInstance(id.toLong()));
        } catch (ArchivedProcessInstanceNotFoundException e) {
            throw new APIItemNotFoundException(ArchivedCaseDefinition.TOKEN, id);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    public ArchivedCaseItem getUsingSourceObjectId(final APIID id) {
        try {
            return convertEngineToConsoleItem(getProcessApi()
                    .getFinalArchivedProcessInstance(id.toLong()));
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    public void delete(final List<APIID> ids) {
        try {
            final ProcessAPI processAPI = getProcessApi();
            final List<Long> toDeleteIds = new ArrayList<>();
            for (final APIID apiId : ids) {
                final ArchivedProcessInstance archivedProcessInstance = processAPI
                        .getArchivedProcessInstance(apiId.toLong());
                toDeleteIds.add(archivedProcessInstance.getSourceObjectId());
            }
            processAPI.deleteArchivedProcessInstancesInAllStates(toDeleteIds);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }

    }

    public ProcessAPI getProcessApi() throws BonitaHomeNotSetException, ServerAPIException, UnknownAPITypeException {
        return TenantAPIAccessor.getProcessAPI(getEngineSession());
    }
}
