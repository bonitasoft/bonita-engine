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
package org.bonitasoft.engine.execution.work;

import java.util.Map;

import org.bonitasoft.engine.api.impl.transaction.event.CreateEventInstance;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.ConnectorService;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
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
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInterruptedException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEndEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.archive.ProcessArchiver;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.work.WorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class ExecuteConnectorOfActivity extends ExecuteConnectorWork {

    private final SFlowNodeInstance flowNodeInstance;

    private final ActivityInstanceService activityInstanceService;

    private final FlowNodeStateManager flowNodeStateManager;

    private final ProcessInstanceService processInstanceService;

    private final ArchiveService archiveService;

    private final DataInstanceService dataInstanceService;

    private final SDataInstanceBuilders dataInstanceBuilders;

    private final ContainerRegistry containerRegistry;

    private final EventInstanceService eventInstanceService;

    private final WorkService workService;

    //charles comment
    public ExecuteConnectorOfActivity(final ContainerRegistry containerRegistry, final TransactionExecutor transactionExecutor,
            final ProcessInstanceService processInstanceService, final ArchiveService archiveService, final BPMInstanceBuilders bpmInstanceBuilders,
            final DataInstanceService dataInstanceService, final SDataInstanceBuilders dataInstanceBuilders,
            final ActivityInstanceService activityInstanceService, final FlowNodeStateManager flowNodeStateManager,
            final ClassLoaderService classLoaderService, final ConnectorService connectorService, final ConnectorInstanceService connectorInstanceService,
            final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance, final SConnectorInstance connector,
            final SConnectorDefinition sConnectorDefinition, final Map<String, Object> inputParameters, final EventsHandler eventsHandler,
            final BPMInstancesCreator bpmInstancesCreator, final BPMDefinitionBuilders bpmDefinitionBuilders, final EventInstanceService eventInstanceService,
            final WorkService workService) {
        super(processDefinition, classLoaderService, transactionExecutor, connector, sConnectorDefinition, connectorService, connectorInstanceService,
                inputParameters, eventsHandler, bpmInstanceBuilders, bpmInstancesCreator, bpmDefinitionBuilders);
        this.containerRegistry = containerRegistry;
        this.processInstanceService = processInstanceService;
        this.archiveService = archiveService;
        this.dataInstanceService = dataInstanceService;
        this.dataInstanceBuilders = dataInstanceBuilders;
        this.activityInstanceService = activityInstanceService;
        this.flowNodeStateManager = flowNodeStateManager;
        this.flowNodeInstance = flowNodeInstance;
        this.eventInstanceService = eventInstanceService;
        this.workService = workService;
    }

    @Override
    protected void evaluateOutput(final ConnectorResult result) throws STransactionException, SBonitaException {
        evaluateOutput(result, flowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name());
    }

    @Override
    protected void continueFlow() throws SActivityReadException, SFlowNodeExecutionException, SActivityReadException, SActivityInterruptedException,
            WorkRegisterException {
        String containerType = SFlowElementsContainerType.PROCESS.name();
        if (flowNodeInstance.getLogicalGroup(2) > 0) {
            containerType = SFlowElementsContainerType.FLOWNODE.name();
        }
        // no need to set the classloader: done in the flowNodeExecutor.gotoNextStableState
        containerRegistry.executeFlowNodeInSameThread(flowNodeInstance.getId(), null, null, containerType,
                flowNodeInstance.getLogicalGroup(bpmInstanceBuilders.getSUserTaskInstanceBuilder().getParentProcessInstanceIndex()));
    }

    @Override
    protected void setContainerInFail() throws SBonitaException {
        final SFlowNodeInstance intTxflowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstance.getId());
        ProcessArchiver.archiveFlowNodeInstance(intTxflowNodeInstance, false, processInstanceService, archiveService, bpmInstanceBuilders, dataInstanceService,
                dataInstanceBuilders, processDefinition, activityInstanceService, connectorInstanceService);
        activityInstanceService.setState(intTxflowNodeInstance, flowNodeStateManager.getFailedState());
    }

    @Override
    protected SThrowEventInstance createThrowErrorEventInstance(final SEndEventDefinition eventDefinition) throws SBonitaException {
        final SEndEventInstanceBuilder endEventInstanceBuilder = bpmInstanceBuilders.getSEndEventInstanceBuilder();
        final SEndEventInstanceBuilder builder = endEventInstanceBuilder.createNewEndEventInstance(eventDefinition.getName(), eventDefinition.getId(),
                flowNodeInstance.getRootContainerId(), flowNodeInstance.getParentContainerId(), processDefinition.getId(),
                flowNodeInstance.getRootContainerId(), flowNodeInstance.getParentContainerId());
        builder.setParentActivityInstanceId(flowNodeInstance.getId());
        final SThrowEventInstance done = (SThrowEventInstance) builder.done();
        new CreateEventInstance(done, eventInstanceService).execute();
        return done;
    }

    @Override
    protected void errorEventOnFail() throws SBonitaException {
        setConnectorOnlyToFailed();
        // FIXME see comment below
        /*
         * we do this in a work because the boundary might not be in waiting state and have registered the waiting error now...
         * we should find an other way to "wait" the waiting error to be registered
         * maybe the waiting error should be registered synchronously when creating the task and not by the boundary event?
         */
        workService.registerWork(new HandleErrorEventOnFail(this));
    }

    void handleErrorEventOnFail() throws SBonitaException {
        // create a fake definition
        final SThrowErrorEventTriggerDefinitionBuilder errorEventTriggerDefinitionBuilder = bpmDefinitionBuilders.getThrowErrorEventTriggerDefinitionBuilder();
        final SThrowErrorEventTriggerDefinition errorEventTriggerDefinition = errorEventTriggerDefinitionBuilder.createNewInstance(
                sConnectorDefinition.getErrorCode()).done();
        final SEndEventDefinitionBuilder sEndEventDefinitionBuilder = bpmDefinitionBuilders.getSEndEventDefinitionBuilder();
        // event definition as the error code as name, this way we don't need to find the connector that throw this error
        final SEndEventDefinition eventDefinition = sEndEventDefinitionBuilder.createNewInstance(sConnectorDefinition.getErrorCode())
                .addErrorEventTriggerDefinition(errorEventTriggerDefinition).done();
        // create an instance using this definition
        final SThrowEventInstance throwEventInstance = createThrowErrorEventInstance(eventDefinition);
        final boolean hasActionToExecute = eventsHandler.getHandler(SEventTriggerType.ERROR).handlePostThrowEvent(processDefinition, eventDefinition,
                throwEventInstance, errorEventTriggerDefinition, flowNodeInstance);
        if (!hasActionToExecute) {
            setConnectorAndContainerToFailed();
        }
    }
}
