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
package org.bonitasoft.engine.execution;

import org.bonitasoft.engine.SArchivingException;
import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.classloader.ClassLoaderService;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.connector.ConnectorInstanceService;
import org.bonitasoft.engine.core.contract.data.ContractDataService;
import org.bonitasoft.engine.core.contract.data.SContractDataDeletionException;
import org.bonitasoft.engine.core.operation.OperationService;
import org.bonitasoft.engine.core.process.comment.api.SCommentAddException;
import org.bonitasoft.engine.core.process.comment.api.SCommentService;
import org.bonitasoft.engine.core.process.comment.api.SystemCommentType;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.definition.exception.SProcessDefinitionNotFoundException;
import org.bonitasoft.engine.core.process.definition.model.SProcessDefinition;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
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
import org.bonitasoft.engine.core.process.instance.model.SStateCategory;
import org.bonitasoft.engine.core.process.instance.model.archive.builder.SAAutomaticTaskInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.SUserTaskInstanceBuilderFactory;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.execution.archive.ProcessArchiver;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.work.SWorkRegisterException;
import org.bonitasoft.engine.work.WorkService;

/**
 * @author Baptiste Mesta
 * @author Yanyan Liu
 * @author Elias Ricken de Medeiros
 * @author Emmanuel Duchastenier
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class FlowNodeExecutorImpl implements FlowNodeExecutor {

    private final FlowNodeStateManager flowNodeStateManager;

    private final ActivityInstanceService activityInstanceService;

    private final EventInstanceService eventInstanceService;

    private final OperationService operationService;

    private final ArchiveService archiveService;

    private final DataInstanceService dataInstanceService;

    private final ContainerRegistry containerRegistry;

    private final ProcessDefinitionService processDefinitionService;

    private final SCommentService commentService;

    private final ProcessInstanceService processInstanceService;

    private final ConnectorInstanceService connectorInstanceService;

    private final ClassLoaderService classLoaderService;

    private final WorkService workService;
    private final BPMWorkFactory workFactory;

    private final ContractDataService contractDataService;

    public FlowNodeExecutorImpl(final FlowNodeStateManager flowNodeStateManager, final ActivityInstanceService activityInstanceManager,
            final OperationService operationService, final ArchiveService archiveService,
            final DataInstanceService dataInstanceService,
            final ContainerRegistry containerRegistry, final ProcessDefinitionService processDefinitionService,
            final SCommentService commentService,
            final ProcessInstanceService processInstanceService,
            final ConnectorInstanceService connectorInstanceService,
            final ClassLoaderService classLoaderService, final WorkService workService, BPMWorkFactory workFactory,
            final ContractDataService contractDataService, final EventInstanceService eventInstanceService) {
        super();
        this.flowNodeStateManager = flowNodeStateManager;
        activityInstanceService = activityInstanceManager;
        this.operationService = operationService;
        this.archiveService = archiveService;
        this.dataInstanceService = dataInstanceService;
        this.containerRegistry = containerRegistry;
        this.processInstanceService = processInstanceService;
        this.connectorInstanceService = connectorInstanceService;
        this.classLoaderService = classLoaderService;
        this.workService = workService;
        this.workFactory = workFactory;
        containerRegistry.addContainerExecutor(this);
        this.processDefinitionService = processDefinitionService;
        this.commentService = commentService;
        this.contractDataService = contractDataService;
        this.eventInstanceService = eventInstanceService;
    }

    @Override
    public StateCode executeState(final SProcessDefinition processDefinition, final SFlowNodeInstance flowNodeInstance, final FlowNodeState state)
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

    private void addSystemComment(final SFlowNodeInstance flowNodeInstance, final FlowNodeState state) throws SCommentAddException {
        if (commentService.isCommentEnabled(SystemCommentType.STATE_CHANGE) && state.mustAddSystemComment(flowNodeInstance)) {
            commentService.addSystemComment(flowNodeInstance.getRootContainerId(), state.getSystemComment(flowNodeInstance));
        }
    }

    @Override
    public FlowNodeState stepForward(final long flowNodeInstanceId,
            final Long executerId, final Long executerSubstituteId) throws SFlowNodeExecutionException {
        // retrieve the activity and execute its state
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            final SFlowNodeInstance sFlowNodeInstance = activityInstanceService.getFlowNodeInstance(flowNodeInstanceId);
            final long processDefinitionId = sFlowNodeInstance.getLogicalGroup(BuilderFactory.get(SUserTaskInstanceBuilderFactory.class)
                    .getProcessDefinitionIndex());
            final ClassLoader localClassLoader = classLoaderService.getLocalClassLoader(ScopeType.PROCESS.name(), processDefinitionId);
            Thread.currentThread().setContextClassLoader(localClassLoader);

            if (!sFlowNodeInstance.isStateExecuting()) {
                archiveFlowNodeInstance(sFlowNodeInstance, false, processDefinitionId);
                setExecutedBy(executerId, sFlowNodeInstance);
                setExecutedBySubstitute(executerSubstituteId, sFlowNodeInstance);
            }

            final SProcessDefinition processDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
            final FlowNodeState state = updateState(sFlowNodeInstance, processDefinition);
            if (!sFlowNodeInstance.isStateExecuting() && state != null) {
                registerWork(state, sFlowNodeInstance);
            }
            return state;
        } catch (final SFlowNodeExecutionException e) {
            throw e;
        } catch (final SBonitaException e) {
            throw new SFlowNodeExecutionException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private FlowNodeState updateState(final SFlowNodeInstance sFlowNodeInstance, final SProcessDefinition processDefinition)
            throws SActivityStateExecutionException, SActivityExecutionException, SFlowNodeModificationException {
        final StateCode stateCode = executeState(processDefinition, sFlowNodeInstance, flowNodeStateManager.getState(sFlowNodeInstance.getStateId()));
        if (StateCode.DONE.equals(stateCode)) {
            return getNextNormalState(sFlowNodeInstance, processDefinition);
        } else if (StateCode.EXECUTING.equals(stateCode)) {
            // the state is still executing set the executing flag
            activityInstanceService.setExecuting(sFlowNodeInstance);
        }
        return null;
    }

    private FlowNodeState getNextNormalState(final SFlowNodeInstance sFlowNodeInstance, final SProcessDefinition processDefinition)
            throws SActivityExecutionException,
            SFlowNodeModificationException {
        final FlowNodeState state = flowNodeStateManager.getNextNormalState(processDefinition, sFlowNodeInstance, sFlowNodeInstance.getStateId());
        if (sFlowNodeInstance.getStateId() != state.getId()) {
            // this also unset the executing flag
            activityInstanceService.setState(sFlowNodeInstance, state);
        }
        return state;
    }

    private void registerWork(FlowNodeState state, SFlowNodeInstance sFlowNodeInstance) throws SWorkRegisterException {
        if (!state.isStable() && !state.isInterrupting()) {
            registerExecuteFlowNodeWork(sFlowNodeInstance);
        } else if (state.isTerminal()) {
            registerNotifyFinishWork(sFlowNodeInstance);
        }
    }

    private void registerExecuteFlowNodeWork(SFlowNodeInstance sFlowNodeInstance) throws SWorkRegisterException {
        workService.registerWork(workFactory.createExecuteFlowNodeWorkDescriptor(sFlowNodeInstance.getProcessDefinitionId(),
                sFlowNodeInstance.getParentProcessInstanceId(), sFlowNodeInstance.getId()));
    }

    private void interruptSubActivities(SFlowNodeInstance flowNodeInstance) throws SWorkRegisterException {
        try {
            for (SActivityInstance child : activityInstanceService.searchActivityInstances(SActivityInstance.class,
                    activityInstanceService.buildQueryOptionsForSubActivitiesInNormalStateAndNotTerminal(flowNodeInstance.getId(),
                            Integer.MAX_VALUE))) {
                abort(child);
            }
        } catch (SFlowNodeModificationException | SBonitaReadException e) {
            throw new SWorkRegisterException("unable interrupt sub activities", e);
        }

    }

    private void abort(SFlowNodeInstance flowNodeInstance) throws SFlowNodeModificationException, SWorkRegisterException {
        activityInstanceService.setStateCategory(flowNodeInstance, SStateCategory.ABORTING);
        if (flowNodeInstance.isStable()) {//if not stable the flow node will be executed automatically
            registerExecuteFlowNodeWork(flowNodeInstance);
        }
    }

    private void setExecutedBySubstitute(final Long executerSubstituteId, final SFlowNodeInstance sFlowNodeInstance) throws SFlowNodeModificationException {
        if (isNotNullOrEmptyAndDifferentOf(sFlowNodeInstance.getExecutedBySubstitute(), executerSubstituteId)) {
            activityInstanceService.setExecutedBySubstitute(sFlowNodeInstance, executerSubstituteId);
        }
    }

    private void setExecutedBy(final Long executerId, final SFlowNodeInstance sFlowNodeInstance) throws SFlowNodeModificationException {
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
            archiveFlowNodeInstance(sFlowNodeInstance, false, sFlowNodeInstance.getProcessDefinitionId());
            activityInstanceService.setState(sFlowNodeInstance, state);
            if (state.isTerminal()) {
                if (hasChildren(sFlowNodeInstance)) {
                    interruptSubActivities(sFlowNodeInstance);
                } else {
                    registerNotifyFinishWork(sFlowNodeInstance);
                }
                if (sFlowNodeInstance instanceof SActivityInstance) {
                    flowNodeStateManager.getStateBehaviors().interruptAttachedBoundaryEvent(processDefinitionService.getProcessDefinition(sFlowNodeInstance.getProcessDefinitionId()),
                            ((SActivityInstance) sFlowNodeInstance), SStateCategory.ABORTING);
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
                workFactory.createNotifyChildFinishedWorkDescriptor(sFlowNodeInstance.getProcessDefinitionId(),
                        sFlowNodeInstance.getParentProcessInstanceId(),
                        sFlowNodeInstance.getId(), sFlowNodeInstance.getParentContainerId(), sFlowNodeInstance.getParentContainerType().name()));
    }

    @Override
    public void childFinished(long processDefinitionId, long parentId, SFlowNodeInstance sFlowNodeInstanceChild) throws SFlowNodeNotFoundException,
            SFlowNodeReadException, SProcessDefinitionNotFoundException, SBonitaReadException, SArchivingException, SFlowNodeModificationException,
            SFlowNodeExecutionException, SContractDataDeletionException, SWorkRegisterException {
        // TODO check deletion here
        archiveFlowNodeInstance(sFlowNodeInstanceChild, true, processDefinitionId);

        final SActivityInstance activityInstanceParent = (SActivityInstance) activityInstanceService.getFlowNodeInstance(parentId);
        decrementToken(activityInstanceParent);
        final SProcessDefinition sProcessDefinition = processDefinitionService.getProcessDefinition(processDefinitionId);
        final boolean hit = flowNodeStateManager.getState(activityInstanceParent.getStateId()).hit(sProcessDefinition, activityInstanceParent,
                sFlowNodeInstanceChild);
        if (hit) {// we continue parent if hit of the parent return true
            if (activityInstanceParent.isTerminal()) {
                registerNotifyFinishWork(activityInstanceParent);
            } else {
                stepForward(parentId, null, null);
            }
        }
    }

    private void decrementToken(final SActivityInstance sActivityInstance) throws SFlowNodeModificationException {
        final int tokenCount = sActivityInstance.getTokenCount() - 1;
        activityInstanceService.setTokenCount(sActivityInstance, tokenCount);
    }

    @Override
    public void childReachedState(final SProcessInstance childProcInst, final ProcessInstanceState childState, final boolean hasActionsToExecute)
            throws SBonitaException {
        final long callerId = childProcInst.getCallerId();
        if (isTerminalState(childState) && callerId > 0) {
            final SActivityInstance callActivityInstance = activityInstanceService.getActivityInstance(childProcInst.getCallerId());
            decrementToken(callActivityInstance);
            executeFlowNodeIfHasActionsToExecute(hasActionsToExecute, callActivityInstance);
        }

    }

    private void executeFlowNodeIfHasActionsToExecute(final boolean hasActionsToExecute, final SActivityInstance callActivityInstance)
            throws SWorkRegisterException {
        if (!hasActionsToExecute) {
            containerRegistry.executeFlowNode(callActivityInstance.getProcessDefinitionId(),
                    callActivityInstance.getLogicalGroup(BuilderFactory.get(SAAutomaticTaskInstanceBuilderFactory.class).getParentProcessInstanceIndex()),
                    callActivityInstance.getId());
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
    public FlowNodeState executeFlowNode(final long flowNodeInstanceId,
            final Long executerId, final Long executerSubstituteId) throws SFlowNodeExecutionException {
        return stepForward(flowNodeInstanceId, executerId, executerSubstituteId);
    }

    @Override
    public void archiveFlowNodeInstance(final SFlowNodeInstance flowNodeInstance, final boolean deleteAfterArchive, final long processDefinitionId)
            throws SArchivingException {
        new ProcessArchiver().archiveFlowNodeInstance(flowNodeInstance, deleteAfterArchive, processDefinitionId, processInstanceService,
                processDefinitionService, archiveService, dataInstanceService, activityInstanceService, connectorInstanceService, contractDataService);
    }

}
