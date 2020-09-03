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
package org.bonitasoft.engine.execution;

import static org.bonitasoft.engine.core.process.instance.model.SStateCategory.ABORTING;

import org.bonitasoft.engine.SArchivingException;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.comment.api.SCommentAddException;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.api.SystemCommentType;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityStateExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.api.states.StateCode;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SFlowElementsContainerType;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.execution.archive.BPMArchiverService;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class FlowNodeExecutorImpl implements FlowNodeExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(FlowNodeExecutorImpl.class);

    private final FlowNodeStateManager flowNodeStateManager;
    private final ActivityInstanceService activityInstanceService;
    private final ContainerRegistry containerRegistry;
    private final ProcessDefinitionService processDefinitionService;
    private final SCommentService commentService;
    private final ClassLoaderService classLoaderService;
    private final WorkService workService;
    private final BPMWorkFactory workFactory;
    private final ProcessInstanceInterruptor processInstanceInterruptor;
    private final BPMArchiverService bpmArchiverService;

    public FlowNodeExecutorImpl(final FlowNodeStateManager flowNodeStateManager,
            final ActivityInstanceService activityInstanceManager,
            final ContainerRegistry containerRegistry,
            final ProcessDefinitionService processDefinitionService,
            final SCommentService commentService,
            final ClassLoaderService classLoaderService, final WorkService workService, BPMWorkFactory workFactory,
            ProcessInstanceInterruptor processInstanceInterruptor,
            final BPMArchiverService bpmArchiverService) {
        this.flowNodeStateManager = flowNodeStateManager;
        activityInstanceService = activityInstanceManager;
        this.containerRegistry = containerRegistry;
        this.classLoaderService = classLoaderService;
        this.workService = workService;
        this.workFactory = workFactory;
        this.processInstanceInterruptor = processInstanceInterruptor;
        containerRegistry.addContainerExecutor(this);
        this.processDefinitionService = processDefinitionService;
        this.commentService = commentService;
        this.bpmArchiverService = bpmArchiverService;

    }

    @Override
    public StateCode executeState(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance,
            final FlowNodeState state)
            throws SActivityStateExecutionException, SActivityExecutionException {
        try {
            // if state is part of normal state and the flowNode state category is aborting or canceling it's not necessary to execute the state
            StateCode stateCode = StateCode.DONE;
            if (state.getStateCategory().equals(flowNodeInstance.getStateCategory())) {
                stateCode = state.execute(processDefinition, flowNodeInstance);
                // Add a system comment for Human task only
                addSystemComment(flowNodeInstance, state);
            }
            return stateCode;
        } catch (final SCommentAddException e) {
            throw new SActivityExecutionException(e);
        }
    }

    private void addSystemComment(final SFlowNodeInstance flowNodeInstance, final FlowNodeState state)
            throws SCommentAddException {
        if (commentService.isCommentEnabled(SystemCommentType.STATE_CHANGE)
                && state.mustAddSystemComment(flowNodeInstance)) {
            commentService.addSystemComment(flowNodeInstance.getRootContainerId(),
                    state.getSystemComment(flowNodeInstance));
        }
    }

    @Override
    public FlowNodeState stepForward(SFlowNodeInstance flowNodeInstance, Long executerId, Long executerSubstituteId)
            throws SFlowNodeExecutionException {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final long processDefinitionId = flowNodeInstance
                    .getLogicalGroup(BuilderFactory.get(SUserTaskInstanceBuilderFactory.class)
                            .getProcessDefinitionIndex());
            final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(),
                    processDefinitionId);
            Thread.currentThread().setContextClassLoader(localClassLoader);

            if (!flowNodeInstance.isStateExecuting()) {
                bpmArchiverService.archiveFlowNodeInstance(flowNodeInstance);
                setExecutedBy(executerId, flowNodeInstance);
                setExecutedBySubstitute(executerSubstituteId, flowNodeInstance);
            }

            final SProcessDefinition processDefinition = processDefinitionService
                    .getProcessDefinition(processDefinitionId);
            final FlowNodeState nextState = updateState(flowNodeInstance, processDefinition);
            registerWorkIfUnstableOrTerminal(nextState, flowNodeInstance);
            return nextState;
        } catch (final SFlowNodeExecutionException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SFlowNodeExecutionException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    /**
     * Executes the current state, and returns the next step, if applicable.
     * In a normal case, the normal next state is returned.
     * If execution of the current state returns StateCode.EXECUTING (meaning the execution of the state is not
     * finished)
     * then null is returned (meaning we stay on the same state, some background work will trigger the next state
     * later).
     */
    private FlowNodeState updateState(final SFlowNodeInstance sFlowNodeInstance,
            final SProcessDefinition processDefinition)
            throws SActivityStateExecutionException, SActivityExecutionException, SFlowNodeModificationException {
        final StateCode stateCode = executeState(processDefinition, sFlowNodeInstance,
                flowNodeStateManager.getState(sFlowNodeInstance.getStateId()));
        switch (stateCode) {
            case DONE:
                return getNextNormalState(sFlowNodeInstance, processDefinition);
            case EXECUTING:
            default:
                // the state is still executing set the executing flag
                activityInstanceService.setExecuting(sFlowNodeInstance);
                return null;
        }
    }

    private FlowNodeState getNextNormalState(final SFlowNodeInstance sFlowNodeInstance,
            final SProcessDefinition processDefinition)
            throws SActivityExecutionException,
            SFlowNodeModificationException {
        final FlowNodeState state = flowNodeStateManager.getNextState(processDefinition, sFlowNodeInstance,
                sFlowNodeInstance.getStateId());
        if (sFlowNodeInstance.getStateId() != state.getId()) {
            // this also unset the executing flag
            activityInstanceService.setState(sFlowNodeInstance, state);
        }
        return state;
    }

    private void registerWorkIfUnstableOrTerminal(FlowNodeState nextState, SFlowNodeInstance flowNodeInstance)
            throws SWorkRegisterException {
        if (flowNodeInstance.isStateExecuting() || nextState == null) {
            LOG.debug("No work to register on state {} for flowNode {}", nextState, flowNodeInstance);
            return;
        }
        if (nextState.isInterrupting()) {
            LOG.debug("No work to register on state {} for flowNode {}, should never be there!!!!", nextState,
                    flowNodeInstance);
            return;
            // Only used by InterruptedFlowNodeState that is never used
            //TODO remove me
        }
        if (!nextState.isStable()) {
            registerExecuteFlowNodeWork(flowNodeInstance);
            return;
        }
        if (nextState.isTerminal()) {
            registerNotifyFinishWork(flowNodeInstance);
            return;
        }
        // State is stable, nothing to do.
        LOG.debug("No work to register on state {} for flowNode {}", nextState, flowNodeInstance);
    }

    private void registerExecuteFlowNodeWork(SFlowNodeInstance sFlowNodeInstance) throws SWorkRegisterException {
        workService.registerWork(workFactory.createExecuteFlowNodeWorkDescriptor(sFlowNodeInstance));
    }

    private void setExecutedBySubstitute(final Long executerSubstituteId, final SFlowNodeInstance sFlowNodeInstance)
            throws SFlowNodeModificationException {
        if (isNotNullOrEmptyAndDifferentOf(sFlowNodeInstance.getExecutedBySubstitute(), executerSubstituteId)) {
            activityInstanceService.setExecutedBySubstitute(sFlowNodeInstance, executerSubstituteId);
        }
    }

    private void setExecutedBy(final Long executerId, final SFlowNodeInstance sFlowNodeInstance)
            throws SFlowNodeModificationException {
        if (isNotNullOrEmptyAndDifferentOf(sFlowNodeInstance.getExecutedBy(), executerId)) {
            activityInstanceService.setExecutedBy(sFlowNodeInstance, executerId);
        }
    }

    private boolean isNotNullOrEmptyAndDifferentOf(final long executerId, final Long newExecuterId) {
        return newExecuterId != null && newExecuterId > 0 && executerId != newExecuterId;
    }

    @Override
    public void setStateByStateId(long flowNodeInstanceId, int stateId) throws SActivityStateExecutionException {
        final FlowNodeState state = flowNodeStateManager.getState(stateId);
        try {
            final SFlowNodeInstance sFlowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
            bpmArchiverService.archiveFlowNodeInstance(sFlowNodeInstance);
            activityInstanceService.setState(sFlowNodeInstance, state);
            if (state.isTerminal()) {
                if (hasChildren(sFlowNodeInstance)) {
                    processInstanceInterruptor.interruptChildrenOfFlowNodeInstance(sFlowNodeInstance, ABORTING);
                } else {
                    registerNotifyFinishWork(sFlowNodeInstance);
                }
                if (sFlowNodeInstance instanceof SActivityInstance) {
                    flowNodeStateManager.getStateBehaviors().interruptAttachedBoundaryEvent(
                            processDefinitionService.getProcessDefinition(sFlowNodeInstance.getProcessDefinitionId()),
                            ((SActivityInstance) sFlowNodeInstance), ABORTING);
                }
            }
        } catch (final SBonitaException e) {
            throw new SActivityStateExecutionException(e);
        }
    }

    private boolean hasChildren(SFlowNodeInstance sFlowNodeInstance) {
        return sFlowNodeInstance.getTokenCount() > 0;
    }

    private void registerNotifyFinishWork(SFlowNodeInstance sFlowNodeInstance) throws SWorkRegisterException {
        workService.registerWork(
                workFactory.createNotifyChildFinishedWorkDescriptor(sFlowNodeInstance));
    }

    @Override
    public void childFinished(final long processDefinitionId, final long parentId, SFlowNodeInstance childFlowNode)
            throws SFlowNodeNotFoundException,
            SFlowNodeReadException, SProcessDefinitionNotFoundException, SBonitaReadException, SArchivingException,
            SFlowNodeModificationException,
            SFlowNodeExecutionException, SWorkRegisterException {
        bpmArchiverService.archiveAndDeleteFlowNodeInstance(childFlowNode, processDefinitionId);
        final SActivityInstance activityInstanceParent = (SActivityInstance) activityInstanceService
                .getFlowNodeInstance(parentId);
        decrementToken(activityInstanceParent);
        final SProcessDefinition sProcessDefinition = processDefinitionService
                .getProcessDefinition(processDefinitionId);
        final FlowNodeState state = flowNodeStateManager.getState(activityInstanceParent.getStateId());
        final boolean shouldContinueParent = state.hit(sProcessDefinition, activityInstanceParent, childFlowNode);
        if (shouldContinueParent) {
            // it should never happen, because the terminal state never waits children to finish
            if (activityInstanceParent.isTerminal()) {
                registerNotifyFinishWork(activityInstanceParent);
            } else {
                stepForward(activityInstanceParent, null, null);
            }
        } else {
            LOG.debug("the child flownode {} of parent flownode {} finished, but there are other children remaining",
                    childFlowNode, activityInstanceParent);
        }
    }

    private void decrementToken(final SActivityInstance sActivityInstance) throws SFlowNodeModificationException {
        final int tokenCount = sActivityInstance.getTokenCount() - 1;
        activityInstanceService.setTokenCount(sActivityInstance, tokenCount);
    }

    @Override
    public void childReachedState(final SProcessInstance childProcInst, final ProcessInstanceState childState,
            final boolean hasActionsToExecute) throws SBonitaException {
        if (isTerminalState(childState) && childProcInst.getCallerId() > 0) {
            final SActivityInstance callActivityInstance = activityInstanceService
                    .getActivityInstance(childProcInst.getCallerId());
            decrementToken(callActivityInstance);
            executeFlowNodeIfHasActionsToExecute(hasActionsToExecute, callActivityInstance);
        }

    }

    private void executeFlowNodeIfHasActionsToExecute(final boolean hasActionsToExecute,
            final SActivityInstance callActivityInstance)
            throws SWorkRegisterException {
        if (!hasActionsToExecute) {
            containerRegistry.executeFlowNode(callActivityInstance);
        }
    }

    private boolean isTerminalState(final ProcessInstanceState childState) {
        return ProcessInstanceState.COMPLETED.equals(childState) || ProcessInstanceState.CANCELLED.equals(childState)
                || ProcessInstanceState.ABORTED.equals(childState);
    }

    @Override
    public String getHandledType() {
        return SFlowElementsContainerType.FLOWNODE.name();
    }

    @Override
    public FlowNodeState executeFlowNode(SFlowNodeInstance flowNodeInstance, Long executerId, Long executerSubstituteId)
            throws SFlowNodeExecutionException {
        return stepForward(flowNodeInstance, executerId, executerSubstituteId);
    }
}
