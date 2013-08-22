/**
 * Copyright (C) 2012-2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.execution.work;

import java.util.Map;

import org.bonitasoft.engine.api.impl.transaction.event.CreateEventInstance;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.exception.SConnectorDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.BPMDefinitionBuilders;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SEndEventDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowErrorEventTriggerDefinitionBuilder;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEndEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.archive.ProcessArchiver;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.STransactionException;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class ExecuteConnectorOfActivity extends ExecuteConnectorWork {

    private static final long serialVersionUID = 6220793197069669088L;

    private final long flowNodeInstanceId;

    private final long flowNodeDefinitionId;

    public ExecuteConnectorOfActivity(final long processDefinitionId, final long flowNodeDefinitionId, final long flowNodeInstanceId,
            final long connectorInstanceId, final String connectorDefinitionName, final Map<String, Object> inputParameters) {
        super(processDefinitionId, connectorInstanceId, connectorDefinitionName, inputParameters);
        this.flowNodeDefinitionId = flowNodeDefinitionId;
        this.flowNodeInstanceId = flowNodeInstanceId;
    }

    @Override
    protected void evaluateOutput(final ConnectorResult result) throws STransactionException, SBonitaException {
        evaluateOutput(result, flowNodeInstanceId, DataInstanceContainer.ACTIVITY_INSTANCE.name());
    }

    @Override
    protected void continueFlow() {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final ContainerRegistry containerRegistry = tenantAccessor.getContainerRegistry();
        String containerType = SFlowElementsContainerType.PROCESS.name();

        try {
            final SFlowNodeInstance sFlowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
            if (sFlowNodeInstance.getLogicalGroup(2) > 0) {
                containerType = SFlowElementsContainerType.FLOWNODE.name();
            }
            // no need to set the classloader: done in the flowNodeExecutor.gotoNextStableState
            final long parentProcessInstanceId = sFlowNodeInstance.getLogicalGroup(getTenantAccessor().getBPMInstanceBuilders().getSUserTaskInstanceBuilder()
                    .getParentProcessInstanceIndex());
            containerRegistry.executeFlowNodeInSameThread(flowNodeInstanceId, null, null, containerType, parentProcessInstanceId);
        } catch (SBonitaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void setContainerInFail() throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final ConnectorInstanceService connectorInstanceService = tenantAccessor.getConnectorInstanceService();
        final BPMInstanceBuilders bpmInstanceBuilders = tenantAccessor.getBPMInstanceBuilders();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ArchiveService archiveService = tenantAccessor.getArchiveService();
        final FlowNodeStateManager flowNodeStateManager = tenantAccessor.getFlowNodeStateManager();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();

        final SFlowNodeInstance intTxflowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
        ProcessArchiver.archiveFlowNodeInstance(intTxflowNodeInstance, false, processDefinitionId, processInstanceService, processDefinitionService,
                archiveService, bpmInstanceBuilders, dataInstanceService, activityInstanceService, connectorInstanceService);
        activityInstanceService.setState(intTxflowNodeInstance, flowNodeStateManager.getFailedState());
    }

    @Override
    protected SThrowEventInstance createThrowErrorEventInstance(final SEndEventDefinition eventDefinition) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final EventInstanceService eventInstanceService = tenantAccessor.getEventInstanceService();
        final BPMInstanceBuilders bpmInstanceBuilders = tenantAccessor.getBPMInstanceBuilders();
        final SEndEventInstanceBuilder endEventInstanceBuilder = bpmInstanceBuilders.getSEndEventInstanceBuilder();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();

        final SFlowNodeInstance sFlowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
        final SEndEventInstanceBuilder builder = endEventInstanceBuilder.createNewEndEventInstance(eventDefinition.getName(), eventDefinition.getId(),
                sFlowNodeInstance.getRootContainerId(), sFlowNodeInstance.getParentContainerId(), processDefinitionId, sFlowNodeInstance.getRootContainerId(),
                sFlowNodeInstance.getParentContainerId());
        builder.setParentActivityInstanceId(flowNodeInstanceId);
        final SThrowEventInstance done = (SThrowEventInstance) builder.done();
        new CreateEventInstance(done, eventInstanceService).call();
        return done;
    }

    @Override
    protected void errorEventOnFail() throws SBonitaException {
        setConnectorOnlyToFailed();
        handleErrorEventOnFail();
    }

    void handleErrorEventOnFail() throws SBonitaException {
        final BPMDefinitionBuilders bpmDefinitionBuilders = getBPMDefinitionBuilders();
        final SThrowErrorEventTriggerDefinitionBuilder errorEventTriggerDefinitionBuilder = bpmDefinitionBuilders.getThrowErrorEventTriggerDefinitionBuilder();
        final SEndEventDefinitionBuilder sEndEventDefinitionBuilder = bpmDefinitionBuilders.getSEndEventDefinitionBuilder();
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final EventsHandler eventsHandler = tenantAccessor.getEventsHandler();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SConnectorDefinition sConnectorDefinition = getSConnectorDefinition(tenantAccessor);
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();

        final SFlowNodeInstance sFlowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);

        // create a fake definition
        final String errorCode = sConnectorDefinition.getErrorCode();
        final SThrowErrorEventTriggerDefinition errorEventTriggerDefinition = errorEventTriggerDefinitionBuilder.createNewInstance(errorCode).done();
        // event definition as the error code as name, this way we don't need to find the connector that throw this error
        final SEndEventDefinition eventDefinition = sEndEventDefinitionBuilder.createNewInstance(errorCode)
                .addErrorEventTriggerDefinition(errorEventTriggerDefinition).done();
        // create an instance using this definition
        final SThrowEventInstance throwEventInstance = createThrowErrorEventInstance(eventDefinition);
        final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        final boolean hasActionToExecute = eventsHandler.getHandler(SEventTriggerType.ERROR).handlePostThrowEvent(sProcessDefinition, eventDefinition,
                throwEventInstance, errorEventTriggerDefinition, sFlowNodeInstance);
        if (!hasActionToExecute) {
            setConnectorAndContainerToFailed();
        }
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": flowNodeInstanceId = " + flowNodeInstanceId + ", connectorDefinitionName = " + connectorDefinitionName;
    }

    @Override
    protected SConnectorDefinition getSConnectorDefinition(final TenantServiceAccessor tenantAccessor) throws SBonitaException {
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        final SFlowNodeDefinition flowNodeDefinition = processDefinition.getProcessContainer().getFlowNode(flowNodeDefinitionId);

        final SConnectorDefinition sConnectorDefinition = flowNodeDefinition.getConnectorDefinition(connectorDefinitionName);
        if (sConnectorDefinition == null) {
            throw new SConnectorDefinitionNotFoundException(connectorDefinitionName);
        }
        return sConnectorDefinition;
    }
}
