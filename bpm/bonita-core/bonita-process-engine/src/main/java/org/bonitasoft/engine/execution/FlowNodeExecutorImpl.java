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
package org.bonitasoft.engine.execution;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.impl.transaction.activity.SetActivityState;
import org.bonitasoft.engine.api.impl.transaction.flownode.GetFlowNodeInstance;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.commons.transaction.TransactionExecutor;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.expression.control.model.SExpressionContext;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.operation.model.SOperation;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.api.SystemCommentType;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.model.SFlowNodeType;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInterruptedException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.STransitionInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SFlowNodeInstanceLogBuilder;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.data.instance.model.builder.SDataInstanceBuilders;
import org.bonitasoft.engine.execution.archive.ProcessArchiver;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.lock.SLockException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLog;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType;
import org.bonitasoft.engine.queriablelogger.model.builder.SLogBuilder;
import org.bonitasoft.engine.transaction.STransactionException;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 */
public class FlowNodeExecutorImpl implements FlowNodeExecutor {

    /**
     * timeout for the thread that set flow nodes in fail
     */
    private final int setInFailThreadTimeout;

    private static final String FLOWNODE_COMPLETION_LOCK = SFlowElementsContainerType.FLOWNODE.name() + "_finish";

    private final FlowNodeStateManager flowNodeStateManager;

    private final ActivityInstanceService activityInstanceService;

    private final OperationService operationService;

    private final TransactionExecutor transactionExecutor;

    private final ArchiveService archiveService;

    private final DataInstanceService dataInstanceService;

    private final SDataInstanceBuilders dataInstanceBuilders;

    private final BPMInstanceBuilders bpmInstanceBuilders;

    private final ContainerRegistry containerRegistry;

    private final ProcessDefinitionService processDefinitionService;

    private final SCommentService commentService;

    private final ProcessInstanceService processInstanceService;

    private final LockService lockService;

    private final ConnectorInstanceService connectorInstanceService;

    private final ClassLoaderService classLoaderService;

    private final TechnicalLoggerService logger;

    public FlowNodeExecutorImpl(final FlowNodeStateManager flowNodeStateManager, final ActivityInstanceService activityInstanceManager,
            final TransactionExecutor transactionExecutor, final OperationService operationService, final ArchiveService archiveService,
            final DataInstanceService dataInstanceService, final SDataInstanceBuilders sDataInstanceBuilders, final BPMInstanceBuilders bpmInstanceBuilders,
            final TechnicalLoggerService logger, final ContainerRegistry containerRegistry, final ProcessDefinitionService processDefinitionService,
            final SCommentService commentService, final ProcessInstanceService processInstanceService, final LockService lockService,
            final EventInstanceService eventInstanceService, final ConnectorInstanceService connectorInstanceService,
            final ClassLoaderService classLoaderService) {
        super();
        this.flowNodeStateManager = flowNodeStateManager;
        activityInstanceService = activityInstanceManager;
        this.transactionExecutor = transactionExecutor;
        this.operationService = operationService;
        this.archiveService = archiveService;
        this.dataInstanceService = dataInstanceService;
        dataInstanceBuilders = sDataInstanceBuilders;
        this.bpmInstanceBuilders = bpmInstanceBuilders;
        this.containerRegistry = containerRegistry;
        this.processInstanceService = processInstanceService;
        this.lockService = lockService;
        this.connectorInstanceService = connectorInstanceService;
        this.classLoaderService = classLoaderService;
        containerRegistry.addContainerExecutor(this);
        this.processDefinitionService = processDefinitionService;
        this.commentService = commentService;
        this.logger = logger;
        setInFailThreadTimeout = 3000;
    }

    public FlowNodeExecutorImpl(final FlowNodeStateManager flowNodeStateManager, final ActivityInstanceService activityInstanceManager,
            final TransactionExecutor transactionExecutor, final OperationService operationService, final ArchiveService archiveService,
            final DataInstanceService dataInstanceService, final SDataInstanceBuilders sDataInstanceBuilders, final BPMInstanceBuilders bpmInstanceBuilders,
            final TechnicalLoggerService logger, final ContainerRegistry containerRegistry, final ProcessDefinitionService processDefinitionService,
            final SCommentService commentService, final ProcessInstanceService processInstanceService, final LockService lockService,
            final EventInstanceService eventInstanceService, final ConnectorInstanceService connectorInstanceService,
            final ClassLoaderService classLoaderService, final int setInFailThreadTimeout) {
        super();
        this.flowNodeStateManager = flowNodeStateManager;
        activityInstanceService = activityInstanceManager;
        this.transactionExecutor = transactionExecutor;
        this.operationService = operationService;
        this.archiveService = archiveService;
        this.dataInstanceService = dataInstanceService;
        dataInstanceBuilders = sDataInstanceBuilders;
        this.bpmInstanceBuilders = bpmInstanceBuilders;
        this.containerRegistry = containerRegistry;
        this.processInstanceService = processInstanceService;
        this.lockService = lockService;
        this.connectorInstanceService = connectorInstanceService;
        this.classLoaderService = classLoaderService;
        containerRegistry.addContainerExecutor(this);
        this.processDefinitionService = processDefinitionService;
        this.commentService = commentService;
        this.logger = logger;
        this.setInFailThreadTimeout = setInFailThreadTimeout;
    }

    @Override
    public StateCode executeState(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance, final FlowNodeState state)
            throws SActivityStateExecutionException, SActivityExecutionException {
        // if state is part of normal state and the flowNode state category is aborting or canceling it's not necessary to execute the state
        StateCode stateCode = StateCode.DONE;
        if (state.getStateCategory().equals(flowNodeInstance.getStateCategory())) {
            stateCode = state.execute(processDefinition, flowNodeInstance);
            // Add a system comment for Human task only
            try {
                if (commentService.isCommentEnabled(SystemCommentType.STATE_CHANGE) && state.mustAddSystemComment(flowNodeInstance)) {
                    commentService.addSystemComment(flowNodeInstance.getRootContainerId(), state.getSystemComment(flowNodeInstance));
                }
            } catch (final SBonitaException e) {
                throw new SActivityExecutionException(e);
            }
        }
        return stateCode;
    }

    @Override
    public FlowNodeState stepForward(final long flowNodeInstanceId, final SExpressionContext expressionContext, final List<SOperation> operations,
            final Long executedBy, Long processInstanceId) throws SFlowNodeExecutionException {
        boolean sharedLockCreated = false;
        final String objectType = SFlowElementsContainerType.PROCESS.name();
        boolean txOpened = false;
        // put a process instance lock if we have the process instance id already
        try {
            if (processInstanceId != null) {
                lockService.createSharedLockAccess(processInstanceId, objectType);
                sharedLockCreated = true;
            }
            txOpened = transactionExecutor.openTransaction();
        } catch (final SBonitaException e) {
            throw new SFlowNodeExecutionException(e);
        }
        FlowNodeState state = null;
        boolean failed = false;
        SBonitaException failedException = null;
        SFlowNodeInstance fFlowNodeInstance = null;
        SProcessDefinition processDefinition = null;
        // retrieve the activity and execute its state

        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {

            try {
                fFlowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
                if (processInstanceId == null) {
                    processInstanceId = fFlowNodeInstance.getLogicalGroup(bpmInstanceBuilders.getSUserTaskInstanceBuilder().getParentProcessInstanceIndex());
                }
                final long processDefinitionId = fFlowNodeInstance.getLogicalGroup(bpmInstanceBuilders.getSUserTaskInstanceBuilder()
                        .getProcessDefinitionIndex());
                final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader("process", processDefinitionId);
                Thread.currentThread().setContextClassLoader(localClassLoader);
                processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
                if (!sharedLockCreated) {
                    lockService.createSharedLockAccess(processInstanceId, objectType);
                }
                sharedLockCreated = true;
                if (!fFlowNodeInstance.isStateExecuting()) {
                    archiveFlowNodeInstance(fFlowNodeInstance, false, processDefinition);
                    if (executedBy != null && executedBy > 0 && fFlowNodeInstance.getExecutedBy() != executedBy) {
                        activityInstanceService.setExecutedBy(fFlowNodeInstance, executedBy);
                    }
                    if (operations != null) {
                        for (final SOperation operation : operations) {
                            operationService.execute(operation, fFlowNodeInstance.getId(), DataInstanceContainer.ACTIVITY_INSTANCE.name(), expressionContext);
                        }
                    }
                }
                final StateCode stateCode = executeState(processDefinition, fFlowNodeInstance, flowNodeStateManager.getState(fFlowNodeInstance.getStateId()));
                if (StateCode.DONE.equals(stateCode)) {
                    state = flowNodeStateManager.getNextNormalState(processDefinition, fFlowNodeInstance, fFlowNodeInstance.getStateId());
                    if (fFlowNodeInstance.getStateId() != state.getId()) {
                        // this also unset the executing flag
                        activityInstanceService.setState(fFlowNodeInstance, state);
                    }
                } else if (StateCode.EXECUTING.equals(stateCode)) {
                    // the state is still executing set the executing flag
                    activityInstanceService.setExecuting(fFlowNodeInstance);
                }
            } catch (final SFlowNodeNotFoundException e) {
                handleExecutionException(e);
            } catch (final SBonitaException e) {
                failed = true;
                failedException = e;
                handleExecutionException(e);
            } finally {
                try {
                    transactionExecutor.completeTransaction(txOpened);
                    releaseSharedLock(objectType, sharedLockCreated, processInstanceId);
                } catch (final STransactionException e) {
                    try {
                        releaseSharedLock(objectType, sharedLockCreated, processInstanceId);
                    } catch (final SLockException e1) {
                        throw new SFlowNodeExecutionException(e1);
                    }
                } catch (final SLockException e) {
                    throw new SFlowNodeExecutionException(e);
                }
                if (failed) {
                    setFlowNodeFailedInTransaction(fFlowNodeInstance, processDefinition, failedException);
                }
            }
            // notify the parent container the state changed
            notifyParentStateIsFinished(processDefinition, fFlowNodeInstance, state);

        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        return state;
    }

    /**
     * Rollbacks transaction and throws SFlowNodeExecutionException
     * 
     * @throws SFlowNodeExecutionException
     */
    protected void handleExecutionException(final SBonitaException e) throws SFlowNodeExecutionException {
        try {
            transactionExecutor.setTransactionRollback();
        } catch (final STransactionException e1) {
            throw new SFlowNodeExecutionException(e1);
        }
        throw new SFlowNodeExecutionException(e);
    }

    private void notifyParentStateIsFinished(final SProcessDefinition processDefinition, final SFlowNodeInstance fFlowNodeInstance, final FlowNodeState state)
            throws SFlowNodeExecutionException {
        if (!fFlowNodeInstance.isStateExecuting()) {// the state is still executing
            try {
                containerRegistry.nodeReachedState(processDefinition, fFlowNodeInstance, state, fFlowNodeInstance.getParentContainerId(), fFlowNodeInstance
                        .getParentContainerType().name());
            } catch (final SBonitaException e) {
                setFlowNodeFailedInTransaction(fFlowNodeInstance, processDefinition, e);
                throw new SFlowNodeExecutionException("Error while finishing element", e);
            }
        }
    }

    protected void releaseSharedLock(final String objectType, final boolean lockCreated, final Long processInstanceId) throws SLockException {
        if (lockCreated) {
            lockService.releaseSharedLockAccess(processInstanceId, objectType);
        }
    }

    private void setFlowNodeFailedInTransaction(final SFlowNodeInstance fFlowNodeInstance, final SProcessDefinition processDefinition,
            final SBonitaException sbe) throws SFlowNodeExecutionException {
        long flowNodeInstanceId = fFlowNodeInstance.getId();
        // we put the step in failed if the boundary attached to it fails
        if (SFlowNodeType.BOUNDARY_EVENT.equals(fFlowNodeInstance.getType())) {
            flowNodeInstanceId = fFlowNodeInstance.getParentContainerId();
        }
        // we do that is an other thread to avoid issues with nested transactions
        SetFlowNodeInFailedThread setInFailedThread;
        try {
            setInFailedThread = new SetFlowNodeInFailedThread(flowNodeInstanceId, processDefinition, this);
        } catch (final Exception e) {
            throw new SFlowNodeExecutionException("Unable to put the failed state on flow node instance with id " + fFlowNodeInstance, e);
        }
        setInFailedThread.start();
        final StringBuilder msg = new StringBuilder("Flownode with id " + fFlowNodeInstance + " named '" + setInFailedThread.getFlowNodeInstanceName()
                + "' on process instance with id " + setInFailedThread.getParentFlowNodeInstanceId() + " has failed: " + sbe.getMessage());
        logger.log(this.getClass(), TechnicalLogSeverity.ERROR, msg.toString());

        try {
            setInFailedThread.join(setInFailThreadTimeout);
        } catch (final InterruptedException e) {
            logger.log(this.getClass(), TechnicalLogSeverity.ERROR, "The thread that set in fail flownode was interrupted: " + e.getMessage());
        }

        if (!setInFailedThread.isFinished()) {
            setInFailedThread.interrupt();
            throw new SFlowNodeExecutionException("Unable to put the failed state on flownode instance with id " + fFlowNodeInstance
                    + ": execution not finished", setInFailedThread.getThrowable());
        }
        if (setInFailedThread.getThrowable() != null) {
            throw new SFlowNodeExecutionException("Unable to put the failed state on flownode instance with id " + fFlowNodeInstance + ": "
                    + setInFailedThread.getThrowable());
        }
    }

    @Override
    public FlowNodeState gotoNextStableState(final long flowNodeInstanceId, final SExpressionContext expressionContext, final List<SOperation> operations,
            final Long executedBy, final Long processInstanceId) throws SFlowNodeExecutionException, SActivityInterruptedException {
        FlowNodeState state = stepForward(flowNodeInstanceId, expressionContext, operations, executedBy, processInstanceId);
        if (state != null) {// state did not change
            handleInterruption(state);
            while (state != null && !state.isStable()) {
                state = stepForward(flowNodeInstanceId, null, null, null, processInstanceId);
                handleInterruption(state);
            }
        }
        return state;
    }

    private void handleInterruption(final FlowNodeState state) throws SActivityInterruptedException {
        if (state.isInterrupting()) {
            throw new SActivityInterruptedException();
        }
    }

    protected <T extends SLogBuilder> void initializeLogBuilder(final T logBuilder, final String message) {
        logBuilder.createNewInstance().actionStatus(SQueriableLog.STATUS_FAIL).severity(SQueriableLogSeverity.INTERNAL).rawMessage(message);
    }

    protected <T extends HasCRUDEAction> void updateLog(final ActionType actionType, final T logBuilder) {
        logBuilder.setActionType(actionType);
    }

    protected SFlowNodeInstanceLogBuilder getQueriableLog(final ActionType actionType, final String message) {
        final SFlowNodeInstanceLogBuilder logBuilder = bpmInstanceBuilders.getActivityInstanceLogBuilder();
        this.initializeLogBuilder(logBuilder, message);
        this.updateLog(actionType, logBuilder);
        return logBuilder;
    }

    @Override
    public void setStateByStateId(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance, final int stateId)
            throws SActivityStateExecutionException {
        final FlowNodeState state = flowNodeStateManager.getState(stateId);
        try {
            final TransactionContent content = new TransactionContent() {

                @Override
                public void execute() throws SBonitaException {
                    final SFlowNodeInstance fFlowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstance.getId());
                    archiveFlowNodeInstance(fFlowNodeInstance, false, processDefinition);
                }
            };
            final SetActivityState transactionContent = new SetActivityState(activityInstanceService, flowNodeInstance.getId(), state);
            final GetFlowNodeInstance getFlowNodeInstance = new GetFlowNodeInstance(activityInstanceService, flowNodeInstance.getId());
            transactionExecutor.execute(Arrays.asList(content, transactionContent, getFlowNodeInstance));
            // not sure we should finish state
            notifyParentStateIsFinished(processDefinition, getFlowNodeInstance.getResult(), state);
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException(e);
        }
    }

    @Override
    public void childReachedState(final SProcessDefinition processDefinition, final SFlowNodeInstance child, final FlowNodeState state, final long parentId)
            throws SBonitaException {
        if (state.isTerminal()) {
            boolean hit = false;
            lockService.createExclusiveLockAccess(parentId, FLOWNODE_COMPLETION_LOCK);// lock activity in order to avoid hit 2 times at the same time
            try {
                final boolean txOpened = transactionExecutor.openTransaction();
                try {
                    final SFlowNodeInstance fChild = activityInstanceService.getFlowNodeInstance(child.getId());
                    final SFlowNodeInstance fParent = activityInstanceService.getFlowNodeInstance(parentId);
                    archiveFlowNodeInstance(fChild, true, processDefinition);
                    final SActivityInstance activityInstance = (SActivityInstance) fParent;
                    final int tokenCount = activityInstance.getTokenCount() - 1;
                    activityInstanceService.setTokenCount(activityInstance, tokenCount);
                    hit = flowNodeStateManager.getState(fParent.getStateId()).hit(processDefinition, fParent, fChild);
                } catch (final SBonitaException e) {
                    transactionExecutor.setTransactionRollback();
                    throw e;
                } finally {
                    transactionExecutor.completeTransaction(txOpened);
                }
            } finally {
                lockService.releaseExclusiveLockAccess(parentId, FLOWNODE_COMPLETION_LOCK);
            }
            if (hit) {// we continue parent if hit of the parent return true
                gotoNextStableState(parentId, null, null, null,
                        child.getLogicalGroup(bpmInstanceBuilders.getSUserTaskInstanceBuilder().getProcessDefinitionIndex()));
            }
        }
    }

    @Override
    public void childReachedState(final SProcessDefinition childProcDef, final SProcessInstance childProcInst, final ProcessInstanceState childState,
            final boolean hasActionsToExecute) throws SBonitaException {
        final long callerId = childProcInst.getCallerId();
        if (isTerminalState(childState) && callerId > 0) {
            lockService.createExclusiveLockAccess(callerId, FLOWNODE_COMPLETION_LOCK);// lock activity in order to avoid hit 2 times at the same time
            try {
                final SActivityInstance activityInstance;
                activityInstance = activityInstanceService.getActivityInstance(childProcInst.getCallerId());
                final int tokenCount = activityInstance.getTokenCount() - 1;
                activityInstanceService.setTokenCount(activityInstance, tokenCount);
                if (!hasActionsToExecute) {
                    containerRegistry.executeFlowNode(activityInstance.getId(), null, null, SFlowElementsContainerType.FLOWNODE.name(),
                            activityInstance.getLogicalGroup(bpmInstanceBuilders.getSAAutomaticTaskInstanceBuilder().getParentProcessInstanceIndex()));
                }
            } finally {
                lockService.releaseExclusiveLockAccess(callerId, FLOWNODE_COMPLETION_LOCK);
            }
        }

    }

    private boolean isTerminalState(final ProcessInstanceState childState) {
        return ProcessInstanceState.COMPLETED.equals(childState) || ProcessInstanceState.CANCELLED.equals(childState)
                || ProcessInstanceState.ABORTED.equals(childState);
    }

    @Override
    public void executeTransition(final SProcessDefinition sDefinition, final STransitionInstance transitionInstance) throws SBonitaException {
        // do nothing: no transition in a flow node for now
    }

    @Override
    public String getHandledType() {
        return SFlowElementsContainerType.FLOWNODE.name();
    }

    @Override
    public void executeFlowNode(final long flowNodeInstanceId, final SExpressionContext contextDependency, final List<SOperation> operations,
            final Long processInstanceId) throws SFlowNodeExecutionException, SActivityInterruptedException, SActivityReadException {
        gotoNextStableState(flowNodeInstanceId, null, null, null, processInstanceId);
    }

    @Override
    public void archiveFlowNodeInstance(final SFlowNodeInstance flowNodeInstance, final boolean deleteAfterArchive, final SProcessDefinition processDefinition)
            throws SActivityExecutionException {
        ProcessArchiver.archiveFlowNodeInstance(flowNodeInstance, deleteAfterArchive, processInstanceService, archiveService, bpmInstanceBuilders,
                dataInstanceService, dataInstanceBuilders, processDefinition, activityInstanceService, connectorInstanceService);
    }

}
