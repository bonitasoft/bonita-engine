/**
 * Copyright (C) 2015 BonitaSoft S.A.
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
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.exception.SConnectorDefinitionNotFoundException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionReadException;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SEndEventDefinitionBuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowErrorEventTriggerDefinitionBuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.execution.Filter;
import org.bonitasoft.engine.execution.FlowNodeSelector;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.StartFlowNodeFilter;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Baptiste Mesta
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class ExecuteConnectorOfProcess extends ExecuteConnectorWork {

    private static final long serialVersionUID = 4993288070721748951L;

    private final long processInstanceId;

    private final long rootProcessInstanceId;

    private final ConnectorEvent activationEvent;

    private final Filter<SFlowNodeDefinition> filter;

    private long subProcessDefinitionId;

    ExecuteConnectorOfProcess(final long processDefinitionId, final long connectorInstanceId, final String connectorDefinitionName,
                              final long processInstanceId, final long rootProcessInstanceId, final ConnectorEvent activationEvent,
                              final FlowNodeSelector flowNodeSelector) {
        super(processDefinitionId, connectorInstanceId, connectorDefinitionName, new SExpressionContext(processInstanceId,
                DataInstanceContainer.PROCESS_INSTANCE.name(), processDefinitionId));
        this.processInstanceId = processInstanceId;
        this.rootProcessInstanceId = rootProcessInstanceId;
        this.activationEvent = activationEvent;
        if(flowNodeSelector != null){
            this.filter = flowNodeSelector.getSelector();
            this.subProcessDefinitionId = flowNodeSelector.getSubProcessDefinitionId();
        }else{
            this.filter = null;
            this.subProcessDefinitionId = -1;
        }
    }

    @Override
    protected void evaluateOutput(final Map<String, Object> context, final ConnectorResult result, final SConnectorDefinition sConnectorDefinition)
            throws SBonitaException {
        evaluateOutput(context, result, sConnectorDefinition, processInstanceId, DataInstanceContainer.PROCESS_INSTANCE.name());
    }

    @Override
    protected void continueFlow(final Map<String, Object> context) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final ProcessInstanceService processInstanceService = tenantAccessor.getProcessInstanceService();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ProcessExecutor processExecutor = tenantAccessor.getProcessExecutor();

        final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        final SProcessInstance intTxProcessInstance = processInstanceService.getProcessInstance(processInstanceId);
        Filter<SFlowNodeDefinition> filterToUse = filter != null ? filter : new StartFlowNodeFilter();
        FlowNodeSelector flowNodeSelector = new FlowNodeSelector(sProcessDefinition, filterToUse, subProcessDefinitionId);
        final boolean connectorTriggered = processExecutor.executeConnectors(sProcessDefinition, intTxProcessInstance, activationEvent,
                flowNodeSelector);
        if (!connectorTriggered) {
            if (activationEvent == ConnectorEvent.ON_ENTER) {
                processExecutor.startElements(intTxProcessInstance, flowNodeSelector);
            } else {
                processExecutor.handleProcessCompletion(sProcessDefinition, intTxProcessInstance, false);
            }
        }
    }

    @Override
    protected void setContainerInFail(final Map<String, Object> context) throws SBonitaException {
        final ProcessInstanceService processInstanceService = getTenantAccessor(context).getProcessInstanceService();
        final SProcessInstance intTxProcessInstance = processInstanceService.getProcessInstance(processInstanceId);
        processInstanceService.setState(intTxProcessInstance, ProcessInstanceState.ERROR);
    }

    @Override
    protected SThrowEventInstance createThrowErrorEventInstance(final Map<String, Object> context, final SEndEventDefinition eventDefinition)
            throws SBonitaException {
        final BPMInstancesCreator bpmInstancesCreator = getTenantAccessor(context).getBPMInstancesCreator();
        final SFlowNodeInstance createFlowNodeInstance = bpmInstancesCreator.createFlowNodeInstance(processDefinitionId, rootProcessInstanceId,
                processInstanceId, SFlowElementsContainerType.PROCESS, eventDefinition, rootProcessInstanceId, processInstanceId, false, -1,
                SStateCategory.NORMAL, -1);
        return (SThrowEventInstance) createFlowNodeInstance;
    }

    @Override
    protected void errorEventOnFail(final Map<String, Object> context, final SConnectorDefinition sConnectorDefinition, final Exception exception)
            throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final EventsHandler eventsHandler = tenantAccessor.getEventsHandler();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();

        setConnectorOnlyToFailed(context, exception);
        // create a fake definition
        final String errorCode = sConnectorDefinition.getErrorCode();
        final SThrowErrorEventTriggerDefinition errorEventTriggerDefinition = BuilderFactory.get(SThrowErrorEventTriggerDefinitionBuilderFactory.class)
                .createNewInstance(errorCode).done();
        // event definition as the error code as name, this way we don't need to find the connector that throw this error
        final SEndEventDefinition eventDefinition = BuilderFactory.get(SEndEventDefinitionBuilderFactory.class).createNewInstance(errorCode)
                .addErrorEventTriggerDefinition(errorEventTriggerDefinition).done();
        // create an instance using this definition
        final SThrowEventInstance throwEventInstance = createThrowErrorEventInstance(context, eventDefinition);

        final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        final boolean hasActionToExecute = eventsHandler.getHandler(SEventTriggerType.ERROR).handlePostThrowEvent(sProcessDefinition, eventDefinition,
                throwEventInstance, errorEventTriggerDefinition, throwEventInstance);

        tenantAccessor.getFlowNodeExecutor().archiveFlowNodeInstance(throwEventInstance, true, sProcessDefinition.getId());
        if (!hasActionToExecute) {
            setConnectorAndContainerToFailed(context, exception);
        }
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": processInstanceId = " + processInstanceId + ", connectorDefinitionName = " + connectorDefinitionName;
    }

    @Override
    protected SConnectorDefinition getSConnectorDefinition(final ProcessDefinitionService processDefinitionService)
            throws SProcessDefinitionNotFoundException, SProcessDefinitionReadException, SConnectorDefinitionNotFoundException {
        final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
        // final SConnectorDefinition sConnectorDefinition = processContainer.getConnectorDefinition(connectorDefinitionId);// FIXME: Uncomment when generate id
        final SConnectorDefinition sConnectorDefinition = processContainer.getConnectorDefinition(connectorDefinitionName);
        if (sConnectorDefinition == null) {
            throw new SConnectorDefinitionNotFoundException(connectorDefinitionName);
        }
        return sConnectorDefinition;
    }

}
