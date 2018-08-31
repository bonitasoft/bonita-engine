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
import java.util.concurrent.CompletableFuture;

import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeExecutionException;
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.execution.WaitingEventsInterrupter;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;

/**
 * Work that is responsible of executing a flow node.
 * If the execution fails it will put the flow node in failed state
 * 
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ExecuteFlowNodeWork extends TenantAwareBonitaWork {

    private static final long serialVersionUID = -5873526992671300038L;

    public enum Type {
        PROCESS, FLOWNODE
    }

    private final long flowNodeInstanceId;

    private boolean isReadyHumanTask = false;

    ExecuteFlowNodeWork(final long flowNodeInstanceId) {
        this.flowNodeInstanceId = flowNodeInstanceId;
    }

    public void setReadyHumanTask(boolean readyHumanTask) {
        isReadyHumanTask = readyHumanTask;
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": flowNodeInstanceId: " + flowNodeInstanceId;
    }

    @Override
    public String getRecoveryProcedure() {
        return "call processApi.executeFlowNode(" + flowNodeInstanceId + ")";
    }

    @Override
    public CompletableFuture<Void> work(final Map<String, Object> context) throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        if (isReadyHumanTask) {
            SHumanTaskInstance humanTaskInstance = tenantAccessor.getActivityInstanceService().getHumanTaskInstance(flowNodeInstanceId);
            /*
             * the stateExecuting flag must be set to true by the API
             * however this do not completely avoid concurrency issue:
             * if user a and user b call execute at the same time on a flow node with no contract input
             * it can happen that both transactions are committed successfully so 2 works are registered
             * the first work will find the task in state 4 with flag executing
             * and the second will find it in the next state (so it is ok) unless there is an on-finish connector.
             * In this last case it will try to execute that and may execute twice the same connector (not verified)
             */
            if (humanTaskInstance.getStateId() != 4 || !humanTaskInstance.isStateExecuting()) {
                throw new SFlowNodeExecutionException(
                        "Unable to execute flow node " + humanTaskInstance.getId()
                                + " because it is in an incompatible state ("
                                + (humanTaskInstance.isStateExecuting() ? "transitioning from state " : "on state ")
                                + humanTaskInstance.getStateName() + "). Someone probably already called execute on it.");
            }
        }
        tenantAccessor.getFlowNodeExecutor().executeFlowNode(flowNodeInstanceId, null, null);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void handleFailure(final Throwable e, final Map<String, Object> context) throws Exception {
        TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final UserTransactionService userTransactionService = tenantAccessor.getUserTransactionService();
        TechnicalLoggerService loggerService = tenantAccessor.getTechnicalLoggerService();
        WaitingEventsInterrupter waitingEventsInterrupter = new WaitingEventsInterrupter(tenantAccessor.getEventInstanceService(),
                tenantAccessor.getSchedulerService(), loggerService);
        FailedStateSetter failedStateSetter = new FailedStateSetter(waitingEventsInterrupter, tenantAccessor.getActivityInstanceService(),
                tenantAccessor.getFlowNodeStateManager(), loggerService);
        userTransactionService.executeInTransaction(new SetInFailCallable(failedStateSetter, flowNodeInstanceId));
    }

    @Override
    public String toString() {
        return "Work[" + getDescription() + "]";
    }

}
