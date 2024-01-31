/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.execution.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.expression.control.api.ExpressionResolverService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SCallActivityDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowElementContainerDefinition;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.StateBehaviors;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.expression.exception.SExpressionDependencyMissingException;
import org.bonitasoft.engine.expression.exception.SExpressionEvaluationException;
import org.bonitasoft.engine.expression.exception.SExpressionTypeUnknownException;
import org.bonitasoft.engine.expression.exception.SInvalidExpressionException;
import org.bonitasoft.engine.expression.model.SExpression;
import org.bonitasoft.engine.transaction.BonitaTransactionSynchronization;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InitializingActivityWithBoundaryEventsState extends OnEnterConnectorState {

    private final StateBehaviors stateBehaviors;
    private final ExpressionResolverService expressionResolverService;
    private ProcessExecutor processExecutor;
    private final ActivityInstanceService activityInstanceService;
    private final ProcessDefinitionService processDefinitionService;
    private final UserTransactionService userTransactionService;
    private final WorkService workService;
    private final BPMWorkFactory workFactory;

    public InitializingActivityWithBoundaryEventsState(final StateBehaviors stateBehaviors,
            ExpressionResolverService expressionResolverService,
            ActivityInstanceService activityInstanceService,
            ProcessDefinitionService processDefinitionService,
            UserTransactionService userTransactionService,
            WorkService workService,
            BPMWorkFactory workFactory) {
        super(stateBehaviors);
        this.stateBehaviors = stateBehaviors;
        this.expressionResolverService = expressionResolverService;
        this.activityInstanceService = activityInstanceService;
        this.processDefinitionService = processDefinitionService;
        this.userTransactionService = userTransactionService;
        this.workService = workService;
        this.workFactory = workFactory;
    }

    //FIXME There is responsibility issue the circular dependencies must be fixed next time.
    @Autowired
    public void setProcessExecutor(@Lazy @Qualifier("processExecutor") ProcessExecutor processExecutor) {
        this.processExecutor = processExecutor;
    }

    public ProcessExecutor getProcessExecutor() {
        return processExecutor;
    }

    @Override
    public int getId() {
        return 32;
    }

    @Override
    public boolean isStable() {
        return false;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public String getName() {
        return "initializing";
    }

    @Override
    public boolean shouldExecuteState(final SProcessDefinition processDefinition,
            final SFlowNodeInstance flowNodeInstance) {
        return true;
    }

    @Override
    public SStateCategory getStateCategory() {
        return SStateCategory.NORMAL;
    }

    @Override
    public boolean mustAddSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return false;
    }

    @Override
    public String getSystemComment(final SFlowNodeInstance flowNodeInstance) {
        return "";
    }

    @Override
    protected void beforeConnectors(SProcessDefinition processDefinition, SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        stateBehaviors.createData(processDefinition, flowNodeInstance);
        stateBehaviors.createAttachedBoundaryEvents(processDefinition, (SActivityInstance) flowNodeInstance);
        stateBehaviors.mapActors(flowNodeInstance, processDefinition.getProcessContainer());
    }

    @Override
    protected void afterConnectors(SProcessDefinition processDefinition, SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        stateBehaviors.updateDisplayNameAndDescription(processDefinition, flowNodeInstance);
        stateBehaviors.updateExpectedDuration(processDefinition, flowNodeInstance);
        stateBehaviors.addAssignmentSystemCommentIfTaskWasAutoAssign(flowNodeInstance);
        handleCallActivity(processDefinition, flowNodeInstance);
        stateBehaviors.registerWaitingEvent(processDefinition, flowNodeInstance);
    }

    private void handleCallActivity(SProcessDefinition processDefinition, SFlowNodeInstance flowNodeInstance)
            throws SActivityStateExecutionException {
        if (SFlowNodeType.CALL_ACTIVITY.equals(flowNodeInstance.getType())) {
            final SFlowElementContainerDefinition processContainer = processDefinition.getProcessContainer();
            try {
                final SCallActivityDefinition callActivity = (SCallActivityDefinition) processContainer
                        .getFlowNode(flowNodeInstance.getFlowNodeDefinitionId());
                if (callActivity == null) {
                    throw new SActivityStateExecutionException("unable to find call activity definition with name "
                            + flowNodeInstance.getName() + " in process definition " + processDefinition.getId());
                }

                final SExpressionContext expressionContext = new SExpressionContext(flowNodeInstance.getId(),
                        DataInstanceContainer.ACTIVITY_INSTANCE.name(), processDefinition.getId());
                final String callableElement = (String) expressionResolverService
                        .evaluate(callActivity.getCallableElement(), expressionContext);
                String callableElementVersion = null;
                if (callActivity.getCallableElementVersion() != null) {
                    callableElementVersion = (String) expressionResolverService
                            .evaluate(callActivity.getCallableElementVersion(), expressionContext);
                }

                final long targetProcessDefinitionId = getTargetProcessDefinitionId(callableElement,
                        callableElementVersion);
                SProcessInstance sProcessInstance = instantiateProcess(processDefinition, callActivity,
                        flowNodeInstance, targetProcessDefinitionId);

                final SCallActivityInstance callActivityInstance = (SCallActivityInstance) flowNodeInstance;
                // update token count
                if (sProcessInstance.getStateId() != ProcessInstanceState.COMPLETED.getId()) {
                    activityInstanceService.setTokenCount(callActivityInstance,
                            callActivityInstance.getTokenCount() + 1);
                } else {
                    log.warn("Call activity {} with id {} started the process {} that is already completed. " +
                            "This might due to an empty process or a process without 'NONE' start event.",
                            callActivityInstance.getName(),
                            callActivityInstance.getId(),
                            sProcessInstance.getId());
                    // This need to be done "later" we register a work to execute this call activity because it is in fact finish
                    // we can't do it right now because its state is not yet changed and the work would fail.
                    // FIXME: it 's not normal to handle this here , it was added to fix the bug https://bonitasoft.atlassian.net/browse/BS-11654.
                    // It's happens because the start process do too much things, we need to provide a new way that handle start process with work, like flow node handling
                    // this is covered by integration CallActivityIT should_complete_call_activity_when_target_is_empty
                    // this situation happens when the user do an error of designing process and we should almost to log a warning message to inform the user.
                    userTransactionService.registerBonitaSynchronization(new BonitaTransactionSynchronization() {

                        @Override
                        public void beforeCompletion() {
                            //the called process is finished, next step is stable so we trigger execution of this flownode
                            try {
                                workService.registerWork(
                                        workFactory.createExecuteFlowNodeWorkDescriptor(flowNodeInstance));
                            } catch (SWorkRegisterException e) {
                                throw new BonitaRuntimeException(e);
                            }
                        }

                        @Override
                        public void afterCompletion(final int txState) {
                        }
                    });
                }
            } catch (final SBonitaException e) {
                throw new SActivityStateExecutionException(e);
            }
        }
    }

    protected SProcessInstance instantiateProcess(final SProcessDefinition callerProcessDefinition,
            final SCallActivityDefinition callActivityDefinition,
            final SFlowNodeInstance callActivityInstance, final long targetProcessDefinitionId)
            throws SBonitaException {
        final long callerProcessDefinitionId = callerProcessDefinition.getId();
        final long callerId = callActivityInstance.getId();
        final SExpressionContext context = new SExpressionContext(callerId,
                DataInstanceContainer.ACTIVITY_INSTANCE.name(), callerProcessDefinitionId);

        final Map<String, Serializable> processInputs = evaluateContractInputExpression(
                callActivityDefinition.getProcessStartContractInputs(), context);

        return processExecutor
                .start(targetProcessDefinitionId, -1, 0, 0, context, callActivityDefinition.getDataInputOperations(),
                        callerId, -1, processInputs);
    }

    protected Map<String, Serializable> evaluateContractInputExpression(Map<String, SExpression> contractInputs,
            SExpressionContext context)
            throws SBonitaException {
        final Map<SExpression, Serializable> evaluationResults = evaluateExpressions(contractInputs, context);
        final Map<String, Serializable> inputExpressionsMap = new HashMap<>(contractInputs.size());
        for (Map.Entry<String, SExpression> entry : contractInputs.entrySet()) {
            inputExpressionsMap.put(entry.getKey(), evaluationResults.get(entry.getValue()));
        }
        return inputExpressionsMap;
    }

    private Map<SExpression, Serializable> evaluateExpressions(Map<String, SExpression> contractInputs,
            SExpressionContext context) throws SExpressionTypeUnknownException, SExpressionEvaluationException,
            SExpressionDependencyMissingException, SInvalidExpressionException {
        List<SExpression> expressionList = new ArrayList<>(contractInputs.values());
        final List<Object> exprResults = expressionResolverService.evaluate(expressionList, context);
        final Map<SExpression, Serializable> evaluationResults = new HashMap<>(contractInputs.size());
        for (int i = 0; i < expressionList.size(); i++) {
            evaluationResults.put(expressionList.get(i), (Serializable) exprResults.get(i));
        }
        return evaluationResults;
    }

    private long getTargetProcessDefinitionId(final String callableElement, final String callableElementVersion)
            throws SBonitaException {
        if (callableElementVersion != null) {
            return processDefinitionService.getProcessDefinitionId(callableElement, callableElementVersion);
        }
        return processDefinitionService.getLatestProcessDefinitionId(callableElement);
    }
}
