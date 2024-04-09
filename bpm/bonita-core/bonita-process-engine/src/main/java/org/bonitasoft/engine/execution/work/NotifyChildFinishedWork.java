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
package org.bonitasoft.engine.execution.work;

import static org.bonitasoft.engine.classloader.ClassLoaderIdentifier.identifier;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.dependency.model.ScopeType;
import org.bonitasoft.engine.execution.ContainerRegistry;
import org.bonitasoft.engine.execution.WaitingEventsInterrupter;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.SWorkPreconditionException;

/**
 * Work that notify a container that a flow node is in completed state
 * e.g. when a flow node of a process finish we evaluate the outgoing transitions of this flow node.
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class NotifyChildFinishedWork extends TenantAwareBonitaWork {

    private final long processDefinitionId;
    private final long flowNodeInstanceId;
    private final Integer stateId;
    private final Boolean executing;
    private final Boolean aborting;
    private final Boolean canceling;

    NotifyChildFinishedWork(long processDefinitionId, long flowNodeInstanceId, Integer stateId, Boolean executing,
            Boolean aborting, Boolean canceling) {
        this.processDefinitionId = processDefinitionId;
        this.flowNodeInstanceId = flowNodeInstanceId;
        this.stateId = stateId;
        this.executing = executing;
        this.aborting = aborting;
        this.canceling = canceling;
    }

    protected ClassLoader getClassLoader(final Map<String, Object> context) throws SBonitaException {
        return getTenantAccessor(context).getClassLoaderService().getClassLoader(
                identifier(ScopeType.PROCESS, processDefinitionId));
    }

    @Override
    public CompletableFuture<Void> work(final Map<String, Object> context) throws Exception {
        final ClassLoader processClassloader = getClassLoader(context);
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(processClassloader);
            TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
            SFlowNodeInstance flowNodeInstance = retrieveAndVerifyFlowNodeInstance(tenantAccessor);
            final ContainerRegistry containerRegistry = tenantAccessor.getContainerRegistry();
            containerRegistry.nodeReachedState(flowNodeInstance);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
        return CompletableFuture.completedFuture(null);
    }

    private SFlowNodeInstance retrieveAndVerifyFlowNodeInstance(TenantServiceAccessor tenantAccessor)
            throws SWorkPreconditionException, SFlowNodeReadException {
        SFlowNodeInstance flowNodeInstance;
        try {
            flowNodeInstance = tenantAccessor.getActivityInstanceService().getFlowNodeInstance(flowNodeInstanceId);
        } catch (SFlowNodeNotFoundException e) {
            throw new SWorkPreconditionException(
                    "Flow node " + flowNodeInstanceId + " is already completed ( not found )");
        }
        if (stateId != flowNodeInstance.getStateId()
                || executing != flowNodeInstance.isStateExecuting()
                || aborting != flowNodeInstance.isAborting()
                || canceling != flowNodeInstance.isCanceling()) {
            throw new SWorkPreconditionException(
                    String.format("Unable to execute flow node %d because it is not in the expected state " +
                            "( expected state: %d, transitioning: %s, aborting: %s, canceling: %s, but got  state: %d, transitioning: %s, aborting: %s, canceling: %s)."
                            +
                            " Someone probably already called execute on it.",
                            flowNodeInstanceId, stateId, executing, aborting, canceling, flowNodeInstance.getStateId(),
                            flowNodeInstance.isStateExecuting(), flowNodeInstance.isAborting(),
                            flowNodeInstance.isCanceling()));
        }
        if (!flowNodeInstance.isTerminal()) {
            throw new SWorkPreconditionException("Flow node " + flowNodeInstanceId + " is not yet completed");
        }
        return flowNodeInstance;
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + " flowNodeInstanceId: " + flowNodeInstanceId;
    }

    @Override
    public void handleFailure(final Throwable e, final Map<String, Object> context) throws Exception {
        TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final UserTransactionService userTransactionService = tenantAccessor.getUserTransactionService();
        WaitingEventsInterrupter waitingEventsInterrupter = new WaitingEventsInterrupter(
                tenantAccessor.getEventInstanceService(),
                tenantAccessor.getSchedulerService());
        FailedStateSetter failedStateSetter = new FailedStateSetter(waitingEventsInterrupter,
                tenantAccessor.getActivityInstanceService(),
                tenantAccessor.getFlowNodeStateManager());
        userTransactionService.executeInTransaction(new SetInFailCallable(failedStateSetter, flowNodeInstanceId));
    }

    @Override
    public String toString() {
        return "Work[" + getDescription() + "]";
    }

    @Override
    public boolean canBeRecoveredByTheRecoveryMechanism() {
        return true;
    }
}
