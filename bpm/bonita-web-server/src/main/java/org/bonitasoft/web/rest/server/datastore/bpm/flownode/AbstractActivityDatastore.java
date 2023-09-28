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

import static org.bonitasoft.web.toolkit.client.common.util.StringUtil.isBlank;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.model.bpm.flownode.ActivityDefinition;
import org.bonitasoft.web.rest.model.bpm.flownode.ActivityItem;
import org.bonitasoft.web.rest.model.bpm.flownode.FlowNodeItem;
import org.bonitasoft.web.rest.model.bpm.flownode.HumanTaskItem;
import org.bonitasoft.web.rest.server.datastore.converter.ActivityAttributeConverter;
import org.bonitasoft.web.rest.server.datastore.filter.ActivityFilterCreator;
import org.bonitasoft.web.rest.server.datastore.filter.Filters;
import org.bonitasoft.web.rest.server.datastore.utils.SearchOptionsCreator;
import org.bonitasoft.web.rest.server.datastore.utils.Sorts;
import org.bonitasoft.web.rest.server.datastore.utils.VariableMapper;
import org.bonitasoft.web.rest.server.datastore.utils.VariablesMapper;
import org.bonitasoft.web.rest.server.engineclient.ActivityEngineClient;
import org.bonitasoft.web.rest.server.engineclient.EngineAPIAccessor;
import org.bonitasoft.web.rest.server.engineclient.EngineClientFactory;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasGet;
import org.bonitasoft.web.rest.server.framework.api.DatastoreHasUpdate;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIItemNotFoundException;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * @author Séverin Moussel
 */
public class AbstractActivityDatastore<CONSOLE_ITEM extends ActivityItem, ENGINE_ITEM extends ActivityInstance> extends
        AbstractFlowNodeDatastore<CONSOLE_ITEM, ENGINE_ITEM>
        implements DatastoreHasGet<CONSOLE_ITEM>, DatastoreHasUpdate<CONSOLE_ITEM> {

    public AbstractActivityDatastore(final APISession engineSession) {
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
    protected static ActivityItem fillConsoleItem(final ActivityItem result, final ActivityInstance item) {
        FlowNodeDatastore.fillConsoleItem(result, item);

        result.setReachStateDate(item.getReachedStateDate());
        result.setLastUpdateDate(item.getLastUpdateDate());

        return result;
    }

    @Override
    public CONSOLE_ITEM get(final APIID id) {
        try {
            @SuppressWarnings("unchecked")
            final ENGINE_ITEM activityInstance = (ENGINE_ITEM) getProcessAPI().getActivityInstance(id.toLong());
            return convertEngineToConsoleItem(activityInstance);
        } catch (final ActivityInstanceNotFoundException e) {
            throw new APIItemNotFoundException(ActivityDefinition.TOKEN, id);
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    protected SearchResult<ENGINE_ITEM> runSearch(final SearchOptionsBuilder builder,
            final Map<String, String> filters) {
        try {
            @SuppressWarnings("unchecked")
            final SearchResult<ENGINE_ITEM> results = (SearchResult<ENGINE_ITEM>) getProcessAPI()
                    .searchActivities(builder.done());
            return results;
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

    @Override
    protected SearchOptionsBuilder makeSearchOptionBuilder(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {

        return new SearchOptionsCreator(page, resultsByPage, search,
                new Sorts(orders, new ActivityAttributeConverter()), new Filters(filters,
                        new ActivityFilterCreator())).getBuilder();
    }

    @Override
    public CONSOLE_ITEM update(final APIID id, final Map<String, String> attributes) {
        final String jsonVariables = MapUtil.getValue(attributes, ActivityItem.ATTRIBUTE_VARIABLES, null);
        if (!isBlank(jsonVariables)) {
            updateActivityVariables(id.toLong(), jsonVariables);
        }
        update(get(id), attributes);
        return null;
    }

    private void updateActivityVariables(final long activityId, final String jsonValue) {
        final ActivityEngineClient activityEngineclient = getActivityEngineClient();
        final HashMap<String, Serializable> variables = buildVariablesMap(activityId, jsonValue, activityEngineclient);
        activityEngineclient.updateVariables(activityId, variables);
    }

    private ActivityEngineClient getActivityEngineClient() {
        return new EngineClientFactory(new EngineAPIAccessor(getEngineSession())).createActivityEngineClient();
    }

    private HashMap<String, Serializable> buildVariablesMap(final long activityId, final String jsonValue,
            final ActivityEngineClient client) {
        final HashMap<String, Serializable> map = new HashMap<>();
        for (final VariableMapper var : VariablesMapper.fromJson(jsonValue).getVariables()) {
            final DataInstance data = client.getDataInstance(var.getName(), activityId);
            map.put(var.getName(), var.getSerializableValue(data.getClassName()));
        }
        return map;
    }

    protected void update(final CONSOLE_ITEM item, final Map<String, String> attributes) {
        updateState(item, MapUtil.getValue(attributes, FlowNodeItem.ATTRIBUTE_STATE, null),
                MapUtil.getValue(attributes, FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID, null));
    }

    /**
     * @param item
     *        The item to update
     * @param state
     *        The state to set
     */
    protected void updateState(final CONSOLE_ITEM item, final String state, String userExecuteById) {
        try {
            if (state == null) {
                return;
            }
            if (HumanTaskItem.VALUE_STATE_SKIPPED.equals(state) && item instanceof FlowNodeItem) {
                getProcessAPI().setActivityStateByName(item.getId().toLong(), ActivityStates.SKIPPED_STATE);
            } else if (HumanTaskItem.VALUE_STATE_COMPLETED.equals(state) && item instanceof ActivityItem) {
                if (userExecuteById != null) {
                    getProcessAPI().executeFlowNode(Long.valueOf(userExecuteById), item.getId().toLong());
                } else {
                    getProcessAPI().executeFlowNode(item.getId().toLong());
                }
            } else {
                throw new APIException("Can't update " + item.getClass().getName() + " state to \"" + state + "\"");
            }
        } catch (final BonitaException e) {
            throw new APIException(e);
        }
    }

}
