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

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.model.impl.BPMInstancesCreator;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
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
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.event.EventsHandler;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 */
public class ExecuteConnectorOfProcess extends ExecuteConnectorWork {

    private final ProcessInstanceService processInstanceService;

    private final SProcessInstance processInstance;

    private final ProcessExecutor processExecutor;

    private final ConnectorEvent activationEvent;

    public ExecuteConnectorOfProcess(final TransactionExecutor transactionExecutor, final ProcessInstanceService processInstanceService,
            final ClassLoaderService classLoaderService, final ConnectorService connectorService, final ConnectorInstanceService connectorInstanceService,
            final SProcessDefinition processDefinition, final SConnectorInstance connector, final SConnectorDefinition sConnectorDefinition,
            final Map<String, Object> inputParameters, final SProcessInstance processInstance, final ProcessExecutor processExecutor,
            final ConnectorEvent activationEvent, final EventsHandler eventsHandler, final BPMInstanceBuilders bpmInstanceBuilders,
            final BPMInstancesCreator bpmInstancesCreator, final BPMDefinitionBuilders bpmDefinitionBuilders, final ContainerRegistry containerRegistry) {
        super(processDefinition, classLoaderService, transactionExecutor, connector, sConnectorDefinition, connectorService, connectorInstanceService,
                inputParameters, eventsHandler, bpmInstanceBuilders, bpmInstancesCreator, bpmDefinitionBuilders);
        this.processInstanceService = processInstanceService;
        this.processInstance = processInstance;
        this.processExecutor = processExecutor;
        this.activationEvent = activationEvent;
    }

    @Override
    protected void evaluateOutput(final ConnectorResult result) throws SBonitaException {
        evaluateOutput(result, processInstance.getId(), DataInstanceContainer.PROCESS_INSTANCE.name());
    }

    @Override
    protected void continueFlow() throws SBonitaException {
        final SProcessInstance intTxProcessInstance = processInstanceService.getProcessInstance(processInstance.getId());
        final boolean connectorTriggered = processExecutor.executeConnectors(processDefinition, intTxProcessInstance, activationEvent, connectorService);
        if (!connectorTriggered) {
            if (activationEvent == ConnectorEvent.ON_ENTER) {
                processExecutor.startElements(processDefinition, intTxProcessInstance);
            } else {
                processExecutor.handleProcessCompletion(processDefinition, intTxProcessInstance, false);
            }
        }
    }

    @Override
    protected void setContainerInFail() throws SBonitaException {
        final SProcessInstance intTxProcessInstance = processInstanceService.getProcessInstance(processInstance.getId());
        processInstanceService.setState(intTxProcessInstance, ProcessInstanceState.ERROR);
    }

    @Override
    protected SThrowEventInstance createThrowErrorEventInstance(final SEndEventDefinition eventDefinition) throws SBonitaException {
        final SFlowNodeInstance createFlowNodeInstance = bpmInstancesCreator.createFlowNodeInstance(processDefinition,
                processInstance.getRootProcessInstanceId(), processInstance.getId(), SFlowElementsContainerType.PROCESS, eventDefinition,
                processInstance.getRootProcessInstanceId(), processInstance.getId(), false, -1, SStateCategory.NORMAL, -1, null);
        return (SThrowEventInstance) createFlowNodeInstance;
    }

    @Override
    protected void errorEventOnFail() throws SBonitaException {
        setConnectorOnlyToFailed();
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
                throwEventInstance, errorEventTriggerDefinition, null);
        if (!hasActionToExecute) {
            setConnectorAndContainerToFailed();
        }
    }

    @Override
    protected String getDescription() {
        return getClass().getSimpleName() + ": processInstanceId:" + processInstance.getId();
    }

}
