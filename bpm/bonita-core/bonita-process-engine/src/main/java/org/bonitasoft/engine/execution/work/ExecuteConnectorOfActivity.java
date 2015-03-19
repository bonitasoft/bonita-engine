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

import org.bonitasoft.engine.api.impl.transaction.event.CreateEventInstance;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorResult;
import org.bonitasoft.engine.core.connector.exception.SConnectorDefinitionNotFoundException;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SConnectorDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeDefinition;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SEndEventDefinitionBuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.builder.event.trigger.SThrowErrorEventTriggerDefinitionBuilderFactory;
import org.bonitasoft.engine.core.process.definition.model.event.SEndEventDefinition;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SEventTriggerType;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.SThrowErrorEventTriggerDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEndEventInstanceBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.event.SEndEventInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.SThrowEventInstance;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.execution.WaitingEventsInterrupter;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.STransactionException;
import org.bonitasoft.engine.work.BonitaWork;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ExecuteConnectorOfActivity extends ExecuteConnectorWork {

    private static final long serialVersionUID = 6220793197069669088L;

    private final long flowNodeInstanceId;

    private final long flowNodeDefinitionId;


    ExecuteConnectorOfActivity(final long processDefinitionId, final long flowNodeDefinitionId, final long flowNodeInstanceId, final long connectorInstanceId,
            final String connectorDefinitionName) {
        super(processDefinitionId, connectorInstanceId, connectorDefinitionName, new SExpressionContext(flowNodeInstanceId,
                DataInstanceContainer.ACTIVITY_INSTANCE.name(), processDefinitionId));
        this.flowNodeDefinitionId = flowNodeDefinitionId;
        this.flowNodeInstanceId = flowNodeInstanceId;
    }

    @Override
    protected void evaluateOutput(final Map<String, Object> context, final ConnectorResult result, final SConnectorDefinition sConnectorDefinition)
            throws STransactionException, SBonitaException {
        evaluateOutput(context, result, sConnectorDefinition, flowNodeInstanceId, DataInstanceContainer.ACTIVITY_INSTANCE.name());
    }

    @Override
    protected void continueFlow(final Map<String, Object> context) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final FlowNodeInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();
        final WorkService workService = tenantAccessor.getWorkService();
        final SFlowNodeInstance sFlowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
        final long parentProcessInstanceId = sFlowNodeInstance.getParentProcessInstanceId();
        final BonitaWork executeFlowNodeWork = WorkFactory.createExecuteFlowNodeWork(sFlowNodeInstance.getProcessDefinitionId(), parentProcessInstanceId,
                flowNodeInstanceId, null, null);
        workService.registerWork(executeFlowNodeWork);
    }

    @Override
    protected void setContainerInFail(final Map<String, Object> context) throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        TechnicalLoggerService logger = tenantAccessor.getTechnicalLoggerService();
        WaitingEventsInterrupter waitingEventsInterrupter = new WaitingEventsInterrupter(tenantAccessor.getEventInstanceService(),
                tenantAccessor.getSchedulerService(), logger);
        FailedStateSetter failedStateSetter = new FailedStateSetter(waitingEventsInterrupter, tenantAccessor.getActivityInstanceService(),
                tenantAccessor.getFlowNodeStateManager(), logger);
        failedStateSetter.setAsFailed(flowNodeInstanceId);
    }

    @Override
    protected SThrowEventInstance createThrowErrorEventInstance(final Map<String, Object> context, final SEndEventDefinition eventDefinition)
            throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final EventInstanceService eventInstanceService = tenantAccessor.getEventInstanceService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();

        final SFlowNodeInstance sFlowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
        final SEndEventInstanceBuilder builder = BuilderFactory.get(SEndEventInstanceBuilderFactory.class).createNewEndEventInstance(eventDefinition.getName(),
                eventDefinition.getId(), sFlowNodeInstance.getRootContainerId(), sFlowNodeInstance.getParentContainerId(), processDefinitionId,
                sFlowNodeInstance.getRootContainerId(), sFlowNodeInstance.getParentProcessInstanceId());
        builder.setParentActivityInstanceId(flowNodeInstanceId);
        final SThrowEventInstance done = (SThrowEventInstance) builder.done();
        new CreateEventInstance(done, eventInstanceService).call();
        return done;
    }

    @Override
    protected void errorEventOnFail(final Map<String, Object> context, final SConnectorDefinition sConnectorDefinition, final Exception exception)
            throws SBonitaException {
        setConnectorOnlyToFailed(context, exception);
        handleErrorEventOnFail(context, sConnectorDefinition, exception);
    }

    private void handleErrorEventOnFail(final Map<String, Object> context, final SConnectorDefinition sConnectorDefinition, final Exception exception)
            throws SBonitaException {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final EventsHandler eventsHandler = tenantAccessor.getEventsHandler();
        final ProcessDefinitionService processDefinitionService = tenantAccessor.getProcessDefinitionService();
        final ActivityInstanceService activityInstanceService = tenantAccessor.getActivityInstanceService();

        final SFlowNodeInstance sFlowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);

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
        eventsHandler.getHandler(SEventTriggerType.ERROR).handlePostThrowEvent(sProcessDefinition, eventDefinition, throwEventInstance,
                errorEventTriggerDefinition, sFlowNodeInstance);
        tenantAccessor.getFlowNodeExecutor().archiveFlowNodeInstance(throwEventInstance, true, sProcessDefinition.getId());
        setConnectorAndContainerToFailed(context, exception);
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": flowNodeInstanceId = " + flowNodeInstanceId + ", connectorDefinitionName = " + connectorDefinitionName;
    }

    @Override
    protected SConnectorDefinition getSConnectorDefinition(final ProcessDefinitionService processDefinitionService) throws SBonitaException {
        final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        final SFlowNodeDefinition flowNodeDefinition = processDefinition.getProcessContainer().getFlowNode(flowNodeDefinitionId);

        final SConnectorDefinition sConnectorDefinition = flowNodeDefinition.getConnectorDefinition(connectorDefinitionName);
        if (sConnectorDefinition == null) {
            throw new SConnectorDefinitionNotFoundException("Coudn't find the connector definition [" + connectorDefinitionName + "]");
        }
        return sConnectorDefinition;
    }

    @Override
    public String getRecoveryProcedure() {
        return "call processApi.executeFlowNode(" + flowNodeInstanceId + ")";
    }
}
