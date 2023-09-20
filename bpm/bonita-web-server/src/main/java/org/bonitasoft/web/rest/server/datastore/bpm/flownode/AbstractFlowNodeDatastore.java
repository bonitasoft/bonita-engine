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
package org.bonitasoft.web.rest.server.datastore.bpm.flownode;

import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.NotFoundException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.web.rest.model.bpm.flownode.FlowNodeItem;
import org.bonitasoft.web.rest.model.bpm.flownode.TaskItem;
import org.bonitasoft.web.rest.server.datastore.CommonDatastore;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasSearch;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasUpdate;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.rest.server.framework.utils.SearchOptionsBuilderUtil;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIMethodNotAllowedException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 * @author Celine Souchet
 */
public class AbstractFlowNodeDatastore<CONSOLE_ITEM extends FlowNodeItem, ENGINE_ITEM extends FlowNodeInstance>
        extends CommonDatastore<CONSOLE_ITEM, ENGINE_ITEM>
        implements DatastoreHasSearch<CONSOLE_ITEM>,
        DatastoreHasGet<CONSOLE_ITEM>,
        DatastoreHasUpdate<CONSOLE_ITEM> {

    private DatastoreHasUpdate<FlowNodeItem> updateHelper;

    public AbstractFlowNodeDatastore(final APISession engineSession) {
        super(engineSession);
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
    protected static FlowNodeItem fillConsoleItem(final FlowNodeItem result, final FlowNodeInstance item) {
        result.setId(item.getId());
        result.setName(item.getName());
        result.setDisplayName(item.getDisplayName());
        result.setDescription(item.getDescription());
        result.setDisplayDescription(item.getDisplayDescription());
        result.setExecutedByUserId(item.getExecutedBy());
        result.setRootCaseId(item.getRootContainerId());
        result.setParentCaseId(item.getParentProcessInstanceId());
        result.setProcessId(item.getProcessDefinitionId());
        result.setState(item.getState());
        result.setType(item.getType().name());
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

    @SuppressWarnings("unchecked")
    @Override
    protected CONSOLE_ITEM convertEngineToConsoleItem(final ENGINE_ITEM item) {
        return (CONSOLE_ITEM) FlowNodeConverter.convertEngineToConsoleItem(item);
    }

    public long count(final String search, final String orders, final Map<String, String> filters) {
        return search(0, 0, search, orders, filters).getTotal();
    }

    @Override
    public CONSOLE_ITEM get(final APIID id) {
        try {
            @SuppressWarnings("unchecked")
            final ENGINE_ITEM flowNodeInstance = (ENGINE_ITEM) getProcessAPI().getFlowNodeInstance(id.toLong());
            return convertEngineToConsoleItem(flowNodeInstance);
        } catch (final NotFoundException e) {
            throw new APIItemNotFoundException(FlowNodeDefinition.TOKEN, id);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    public ItemSearchResult<CONSOLE_ITEM> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        final SearchOptionsBuilder builder = makeSearchOptionBuilder(page, resultsByPage, search, orders, filters);
        final SearchResult<ENGINE_ITEM> results = runSearch(builder, filters);

        return new ItemSearchResult<>(
                page,
                resultsByPage,
                results.getCount(),
                convertEngineToConsoleItemsList(results.getResult()));
    }

    @SuppressWarnings("unchecked")
    protected SearchResult<ENGINE_ITEM> runSearch(final SearchOptionsBuilder builder,
            final Map<String, String> filters) {
        try {
            return (SearchResult<ENGINE_ITEM>) getProcessAPI().searchFlowNodeInstances(builder.done());
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    protected SearchOptionsBuilder makeSearchOptionBuilder(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        final SearchOptionsBuilder builder = SearchOptionsBuilderUtil.buildSearchOptions(page, resultsByPage, orders,
                search);
        addStringFilterToSearchBuilder(filters, builder, FlowNodeItem.ATTRIBUTE_CASE_ID,
                FlowNodeInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID);
        addStringFilterToSearchBuilder(filters, builder, FlowNodeItem.ATTRIBUTE_ROOT_CASE_ID,
                FlowNodeInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID);
        addStringFilterToSearchBuilder(filters, builder, FlowNodeItem.ATTRIBUTE_PARENT_CASE_ID,
                FlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID);
        addStringFilterToSearchBuilder(filters, builder, FlowNodeItem.ATTRIBUTE_PARENT_ACTIVITY_INSTANCE_ID,
                FlowNodeInstanceSearchDescriptor.PARENT_ACTIVITY_INSTANCE_ID);
        addStringFilterToSearchBuilder(filters, builder, FlowNodeItem.ATTRIBUTE_PROCESS_ID,
                FlowNodeInstanceSearchDescriptor.PROCESS_DEFINITION_ID);
        addStringFilterToSearchBuilder(filters, builder, TaskItem.ATTRIBUTE_LAST_UPDATE_DATE,
                FlowNodeInstanceSearchDescriptor.LAST_UPDATE_DATE);
        addStringFilterToSearchBuilder(filters, builder, TaskItem.ATTRIBUTE_NAME,
                FlowNodeInstanceSearchDescriptor.NAME);

        if (filters.containsKey(FlowNodeInstanceSearchDescriptor.STATE_NAME)
                && "pending".equalsIgnoreCase(filters.get(FlowNodeInstanceSearchDescriptor.STATE_NAME))) {
            builder.leftParenthesis().filter(FlowNodeInstanceSearchDescriptor.STATE_NAME, "ready")
                    .or().filter(FlowNodeInstanceSearchDescriptor.STATE_NAME, "waiting")
                    .rightParenthesis();
        } else if (filters.containsKey(FlowNodeInstanceSearchDescriptor.STATE_NAME)
                && "ongoing".equalsIgnoreCase(filters.get(FlowNodeInstanceSearchDescriptor.STATE_NAME))) {
            builder.leftParenthesis().filter(FlowNodeInstanceSearchDescriptor.STATE_NAME, "executing")
                    .or().filter(FlowNodeInstanceSearchDescriptor.STATE_NAME, "completing")
                    .or().filter(FlowNodeInstanceSearchDescriptor.STATE_NAME, "initializing")
                    .rightParenthesis();
        } else {
            addStringFilterToSearchBuilder(filters, builder, FlowNodeItem.ATTRIBUTE_STATE,
                    FlowNodeInstanceSearchDescriptor.STATE_NAME);
        }
        builder.differentFrom(FlowNodeInstanceSearchDescriptor.STATE_NAME, "aborted");
        builder.differentFrom(FlowNodeInstanceSearchDescriptor.STATE_NAME, "cancelled");
        builder.differentFrom(FlowNodeInstanceSearchDescriptor.STATE_NAME, "completed");
        return builder;
    }

    public AbstractFlowNodeDatastore<CONSOLE_ITEM, ENGINE_ITEM> setUpdateHelper(
            final DatastoreHasUpdate<FlowNodeItem> updateHelper) {
        this.updateHelper = updateHelper;
        return this;
    }

    @Override
    public CONSOLE_ITEM update(final APIID id, final Map<String, String> attributes) {
        if (updateHelper != null) {
            /*
             * Generics are useless in this class.
             * It only result in casting issues.
             * It needs to be badly removed.
             */
            return (CONSOLE_ITEM) updateHelper.update(id, attributes);
        }
        throw new APIMethodNotAllowedException("PUT method not allowed");
    }

}
