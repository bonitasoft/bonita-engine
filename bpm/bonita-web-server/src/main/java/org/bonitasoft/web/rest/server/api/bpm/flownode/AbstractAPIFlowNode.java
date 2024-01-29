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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseItem;
import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedHumanTaskItem;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedTaskDefinition;
import org.bonitasoft.web.rest.model.bpm.flownode.FlowNodeDefinition;
import org.bonitasoft.web.rest.model.bpm.flownode.FlowNodeItem;
import org.bonitasoft.web.rest.model.bpm.flownode.HumanTaskItem;
import org.bonitasoft.web.rest.model.bpm.flownode.IFlowNodeItem;
import org.bonitasoft.web.rest.server.api.ConsoleAPI;
import org.bonitasoft.web.rest.server.api.deployer.GenericDeployer;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.ArchivedCaseDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.CaseDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.FlowNodeDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.TaskDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.TaskFinder;
import org.bonitasoft.web.rest.server.datastore.bpm.flownode.archive.ArchivedTaskDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ActorDatastore;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessDatastore;
import org.bonitasoft.web.rest.server.datastore.organization.UserDatastore;
import org.bonitasoft.web.rest.server.framework.api.APIHasGet;
import org.bonitasoft.web.rest.server.framework.api.APIHasSearch;
import org.bonitasoft.web.rest.server.framework.api.APIHasUpdate;
import org.bonitasoft.web.rest.server.framework.api.Datastore;
import org.bonitasoft.web.rest.server.framework.search.ISearchDirection;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.bonitasoft.web.toolkit.client.data.APIID;

/**
 * Contains all the implementation for a APIFlowNode and inherited APIs
 *
 * @author Séverin Moussel
 */
public class AbstractAPIFlowNode<ITEM extends IFlowNodeItem> extends ConsoleAPI<ITEM> implements
        APIHasUpdate<ITEM>,
        APIHasGet<ITEM>,
        APIHasSearch<ITEM> {

    @Override
    protected FlowNodeDefinition defineItemDefinition() {
        return FlowNodeDefinition.get();
    }

    @Override
    public String defineDefaultSearchOrder() {
        return FlowNodeInstanceSearchDescriptor.DISPLAY_NAME + ISearchDirection.SORT_ORDER_ASCENDING;
    }

    @Override
    protected Datastore defineDefaultDatastore() {
        return new FlowNodeDatastore(getEngineSession());
    }

    @Override
    public ItemSearchResult<ITEM> search(final int page, final int resultsByPage, final String search,
            final String orders, final Map<String, String> filters) {
        // Check that team manager and supervisor filters are not used together
        if (filters.containsKey(HumanTaskItem.FILTER_TEAM_MANAGER_ID)
                && filters.containsKey(HumanTaskItem.FILTER_SUPERVISOR_ID)) {
            throw new APIException("Can't set those filters at the same time : "
                    + HumanTaskItem.FILTER_TEAM_MANAGER_ID
                    + " and "
                    + HumanTaskItem.FILTER_SUPERVISOR_ID);
        }

        return super.search(page, resultsByPage, search, orders, filters);
    }

    @Override
    public ITEM update(final APIID id, final Map<String, String> attributes) {
        final String state = attributes.get(FlowNodeItem.ATTRIBUTE_STATE);
        if (state != null && !isAllowedState(state)) {
            throw new APIException("Can't update a flow node state to \"" + state + "\"");
        }

        return super.update(id, attributes);
    }

    protected boolean isAllowedState(final String state) {
        return FlowNodeItem.VALUE_STATE_READY.equals(state)
                || FlowNodeItem.VALUE_STATE_SKIPPED.equals(state)
                || FlowNodeItem.VALUE_STATE_REPLAY.equals(state)
                || FlowNodeItem.VALUE_STATE_COMPLETED.equals(state);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void fillDeploys(final ITEM item, final List<String> deploys) {
        /** TODO Refactor to an oriented object (cf. WEB-1637 ) */
        if (isDeployable(FlowNodeItem.ATTRIBUTE_PROCESS_ID, deploys, item)) {
            item.setDeploy(FlowNodeItem.ATTRIBUTE_PROCESS_ID,
                    new ProcessDatastore(getEngineSession()).get(item.getProcessId()));
        }

        if (isDeployable(FlowNodeItem.ATTRIBUTE_CASE_ID, deploys, item)
                || isDeployable(FlowNodeItem.ATTRIBUTE_ROOT_CASE_ID, deploys, item)) {
            final CaseItem openedCaseItem = getCaseDatastore().get(item.getCaseId());
            if (openedCaseItem != null) {
                item.setDeploy(FlowNodeItem.ATTRIBUTE_CASE_ID, openedCaseItem);
                item.setDeploy(FlowNodeItem.ATTRIBUTE_ROOT_CASE_ID, openedCaseItem);
            } else {
                final ArchivedCaseItem archivedCaseItem = getArchivedCaseDatastore()
                        .getUsingSourceObjectId(item.getCaseId());
                item.setDeploy(FlowNodeItem.ATTRIBUTE_CASE_ID, archivedCaseItem);
                item.setDeploy(FlowNodeItem.ATTRIBUTE_ROOT_CASE_ID, archivedCaseItem);
            }
        }

        if (isDeployable(FlowNodeItem.ATTRIBUTE_PARENT_CASE_ID, deploys, item)) {
            final CaseItem openedParentCaseItem = getCaseDatastore().get(item.getParentCaseId());
            if (openedParentCaseItem != null) {
                item.setDeploy(FlowNodeItem.ATTRIBUTE_PARENT_CASE_ID,
                        openedParentCaseItem);
            } else {
                final ArchivedCaseItem archivedParentCaseItem = getArchivedCaseDatastore()
                        .getUsingSourceObjectId(item.getParentCaseId());
                item.setDeploy(FlowNodeItem.ATTRIBUTE_PARENT_CASE_ID,
                        archivedParentCaseItem);
            }
        }

        if (isDeployable(FlowNodeItem.ATTRIBUTE_ROOT_CONTAINER_ID, deploys, item)) {
            CaseItem rootContainerCase = getCaseDatastore().get(item
                    .getAttributeValueAsAPIID(HumanTaskItem.ATTRIBUTE_ROOT_CONTAINER_ID));
            if (rootContainerCase == null) {
                rootContainerCase = getArchivedCase(item.getAttributeValue(HumanTaskItem.ATTRIBUTE_ROOT_CONTAINER_ID));
            }
            if (rootContainerCase != null) {
                item.setDeploy(FlowNodeItem.ATTRIBUTE_ROOT_CONTAINER_ID,
                        new ProcessDatastore(getEngineSession()).get(rootContainerCase.getProcessId()));
            }
        }

        if (isDeployable(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID, deploys, item)) {
            item.setDeploy(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID,
                    new UserDatastore(getEngineSession()).get(item.getExecutedByUserId()));
        }

        if (isDeployable(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID, deploys, item)) {
            item.setDeploy(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_SUBSTITUTE_USER_ID,
                    new UserDatastore(getEngineSession()).get(item.getExecutedBySubstituteUserId()));
        }

        if (isDeployable(HumanTaskItem.ATTRIBUTE_ACTOR_ID, deploys, item)) {
            item.setDeploy(HumanTaskItem.ATTRIBUTE_ACTOR_ID,
                    new ActorDatastore(getEngineSession())
                            .get(item.getAttributeValueAsAPIID(HumanTaskItem.ATTRIBUTE_ACTOR_ID)));
        }

        if (isDeployable(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID, deploys, item)) {
            item.setDeploy(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID,
                    new UserDatastore(getEngineSession())
                            .get(item.getAttributeValueAsAPIID(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID)));
        }

        addDeployer(new GenericDeployer<>(id -> new TaskFinder(
                new TaskDatastore(getEngineSession()),
                new ArchivedTaskDatastore(getEngineSession(), ArchivedTaskDefinition.TOKEN)).find(id),
                HumanTaskItem.ATTRIBUTE_PARENT_TASK_ID));

        super.fillDeploys(item, deploys);
    }

    protected CaseDatastore getCaseDatastore() {
        return new CaseDatastore(getEngineSession());
    }

    private CaseItem getArchivedCase(final String id) {
        final List<ArchivedCaseItem> result = getArchivedCaseDatastore().search(
                0, 1,
                null,
                null,
                Collections.singletonMap(ArchivedHumanTaskItem.ATTRIBUTE_SOURCE_OBJECT_ID, id)).getResults();
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    protected ArchivedCaseDatastore getArchivedCaseDatastore() {
        return new ArchivedCaseDatastore(getEngineSession());
    }

    @Override
    protected List<String> defineReadOnlyAttributes() {
        final List<String> attributes = new ArrayList<>();

        attributes.add(FlowNodeItem.ATTRIBUTE_CASE_ID);
        attributes.add(FlowNodeItem.ATTRIBUTE_ROOT_CASE_ID);
        attributes.add(FlowNodeItem.ATTRIBUTE_PARENT_CASE_ID);
        attributes.add(FlowNodeItem.ATTRIBUTE_PROCESS_ID);
        attributes.add(FlowNodeItem.ATTRIBUTE_DESCRIPTION);
        attributes.add(FlowNodeItem.ATTRIBUTE_NAME);
        attributes.add(FlowNodeItem.ATTRIBUTE_EXECUTED_BY_USER_ID);
        attributes.add(FlowNodeItem.ATTRIBUTE_TYPE);
        attributes.add(FlowNodeItem.ATTRIBUTE_ID);

        return attributes;
    }

}
