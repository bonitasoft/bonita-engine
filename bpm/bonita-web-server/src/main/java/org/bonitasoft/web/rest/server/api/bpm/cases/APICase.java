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
package org.bonitasoft.web.rest.server.api.bpm.cases;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.web.rest.model.bpm.cases.CaseDefinition;
import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;
import org.bonitasoft.web.rest.model.bpm.flownode.FlowNodeItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.FlowNodeDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasAdd;
import org.bonitasoft.web.rest.server.framework.api.APIHasDelete;
import org.bonitasoft.web.rest.server.framework.api.APIHasGet;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.bonitasoft.web.toolkit.client.data.item.Definitions;
import org.bonitasoft.web.toolkit.client.data.item.ItemDefinition;

/**
 * @author Séverin Moussel
 * @author Celine Souchet
 */
public class APICase extends ConsoleAPI<CaseItem>
        implements APIHasGet<CaseItem>, APIHasAdd<CaseItem>, APIHasSearch<CaseItem>, APIHasDelete {

    @Override
    protected ItemDefinition defineItemDefinition() {
        return Definitions.get(CaseDefinition.TOKEN);
    }

    @Override
    public CaseItem add(final CaseItem caseItem) {
        return getCaseDatastore().add(caseItem);
    }

    @Override
    public CaseItem get(final APIID id) {
        return getCaseDatastore().get(id);
    }

    @Override
    public ItemSearchResult<CaseItem> search(final int page, final int resultsByPage, final String search,
            final String orders,
            final Map<String, String> filters) {
        // Check that team manager and supervisor filters are not used together
        if (filters.containsKey(CaseItem.FILTER_TEAM_MANAGER_ID)
                && filters.containsKey(CaseItem.FILTER_SUPERVISOR_ID)) {
            throw new APIException(
                    "Can't set those filters at the same time : " + CaseItem.FILTER_TEAM_MANAGER_ID + " and "
                            + CaseItem.FILTER_SUPERVISOR_ID);
        }

        return getCaseDatastore().search(page, resultsByPage, search, orders, filters);
    }

    @Override
    public String defineDefaultSearchOrder() {
        return ProcessInstanceCriterion.CREATION_DATE_DESC.name();
    }

    @Override
    protected void fillDeploys(final CaseItem item, final List<String> deploys) {
        fillStartedBy(item, deploys);
        fillStartedBySubstitute(item, deploys);
        fillProcess(item, deploys);
    }

    private void fillStartedBy(final CaseItem item, final List<String> deploys) {
        if (isDeployable(CaseItem.ATTRIBUTE_STARTED_BY_USER_ID, deploys, item)) {
            item.setDeploy(
                    CaseItem.ATTRIBUTE_STARTED_BY_USER_ID,
                    getUserDatastore().get(item.getStartedByUserId()));
        }
    }

    private void fillStartedBySubstitute(final CaseItem item, final List<String> deploys) {
        if (isDeployable(CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID, deploys, item)) {
            item.setDeploy(
                    CaseItem.ATTRIBUTE_STARTED_BY_SUBSTITUTE_USER_ID,
                    getUserDatastore().get(item.getStartedBySubstituteUserId()));
        }
    }

    private void fillProcess(final CaseItem item, final List<String> deploys) {
        if (isDeployable(CaseItem.ATTRIBUTE_PROCESS_ID, deploys, item)) {
            item.setDeploy(
                    CaseItem.ATTRIBUTE_PROCESS_ID,
                    getProcessDatastore().get(item.getProcessId()));
        }
    }

    private void fillNumberOfFailedFlowNodesIfFailedCounterExists(final CaseItem item, final List<String> counters) {
        if (counters.contains(CaseItem.COUNTER_FAILED_FLOW_NODES)) {
            final FlowNodeDatastore flowNodeDatastore = getFlowNodeDatastore();
            final Map<String, String> filters = new HashMap<>();
            filters.put(FlowNodeItem.ATTRIBUTE_STATE, FlowNodeItem.VALUE_STATE_FAILED);
            filters.put(FlowNodeItem.ATTRIBUTE_PARENT_CASE_ID, String.valueOf(item.getId().toLong()));
            item.setAttribute(CaseItem.COUNTER_FAILED_FLOW_NODES, flowNodeDatastore.count(null, null, filters));
        }
    }

    private void fillNumberOfPendingFlowNodesIfActiveCounterExists(final CaseItem item, final List<String> counters) {
        if (counters.contains(CaseItem.COUNTER_ACTIVE_FLOW_NODES)) {
            final FlowNodeDatastore flowNodeDatastore = getFlowNodeDatastore();
            final Map<String, String> filters = new HashMap<>();
            filters.put(FlowNodeItem.ATTRIBUTE_PARENT_CASE_ID, String.valueOf(item.getId().toLong()));
            item.setAttribute(CaseItem.COUNTER_ACTIVE_FLOW_NODES, flowNodeDatastore.count(null, null, filters));
        }
    }

    @Override
    public void delete(final List<APIID> ids) {
        getCaseDatastore().delete(ids);
    }

    @Override
    protected void fillCounters(final CaseItem item, final List<String> counters) {
        fillNumberOfFailedFlowNodesIfFailedCounterExists(item, counters);
        fillNumberOfPendingFlowNodesIfActiveCounterExists(item, counters);
    }

    UserDatastore getUserDatastore() {
        return new UserDatastore(getEngineSession());
    }

    ProcessDatastore getProcessDatastore() {
        return new ProcessDatastore(getEngineSession());
    }

    FlowNodeDatastore getFlowNodeDatastore() {
        return new FlowNodeDatastore(getEngineSession());
    }

    protected CaseDatastore getCaseDatastore() {
        return new CaseDatastore(getEngineSession());
    }

}
