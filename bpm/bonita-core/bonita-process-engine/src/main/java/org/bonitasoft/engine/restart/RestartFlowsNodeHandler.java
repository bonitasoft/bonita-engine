/**
 * Copyright (C) 2012 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.restart;

import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.ExecuteFlowNodeWork;
import org.bonitasoft.engine.execution.work.NotifyChildFinishedWork;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.work.WorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 */
public class RestartFlowsNodeHandler implements TenantRestartHandler {

    @Override
    public void handleRestart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor) throws RestartException {
        final ActivityInstanceService activityInstanceService = tenantServiceAccessor.getActivityInstanceService();
        QueryOptions queryOptions = QueryOptions.defaultQueryOptions();
        List<SFlowNodeInstance> flowNodes;
        final WorkService workService = platformServiceAccessor.getWorkService();
        final ProcessExecutor processExecutor = tenantServiceAccessor.getProcessExecutor();
        FlowNodeStateManager flowNodeStateManager = tenantServiceAccessor.getFlowNodeStateManager();
        ProcessDefinitionService processDefinitionService = tenantServiceAccessor.getProcessDefinitionService();
        ContainerRegistry containerRegistry = tenantServiceAccessor.getContainerRegistry();
        try {
            final BPMInstanceBuilders bpmInstanceBuilders = tenantServiceAccessor.getBPMInstanceBuilders();
            final int processInstanceIndex = bpmInstanceBuilders.getSUserTaskInstanceBuilder().getParentProcessInstanceIndex();
            do {
                flowNodes = activityInstanceService.getFlowNodeInstancesToRestart(queryOptions);
                queryOptions = QueryOptions.getNextPage(queryOptions);
                for (final SFlowNodeInstance flowNodeInstance : flowNodes) {
                    if (flowNodeInstance.isTerminal()) {
                        // if it is terminal it means the notify was not called yet
                        long processDefinitionId = flowNodeInstance.getProcessDefinitionId();
                        workService.registerWork(new NotifyChildFinishedWork(containerRegistry, processDefinitionService
                                .getProcessDefinition(processDefinitionId), flowNodeInstance, flowNodeStateManager.getState(flowNodeInstance.getStateId())));
                    } else {
                        workService.registerWork(new ExecuteFlowNodeWork(processExecutor, flowNodeInstance.getId(), null, null, flowNodeInstance
                                .getLogicalGroup(processInstanceIndex)));
                    }
                }
            } while (flowNodes.size() == queryOptions.getNumberOfResults());
        } catch (final WorkRegisterException e) {
            handleException(e, "Unable to restart flowNodes: can't register work");
        } catch (final SBonitaException e) {
            handleException(e, "Unable to restart flowNodes: can't read flow nodes");
        }

    }

    private void handleException(final Exception e, final String message) throws RestartException {
        throw new RestartException(message, e);
    }
}
