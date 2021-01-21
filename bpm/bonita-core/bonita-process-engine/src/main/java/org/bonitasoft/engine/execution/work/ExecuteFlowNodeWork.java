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

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SFlowNodeReadException;
import org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance;
import org.bonitasoft.engine.execution.WaitingEventsInterrupter;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.SWorkPreconditionException;

/**
 * Work that is responsible of executing a flow node.
 * If the execution fails it will put the flow node in failed state
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ExecuteFlowNodeWork extends TenantAwareBonitaWork {

    private final long flowNodeInstanceId;
    private final Integer stateId;
    private final Boolean executing;
    private final Boolean aborting;
    private final Boolean canceling;

    ExecuteFlowNodeWork(final long flowNodeInstanceId, Integer stateId, Boolean executing, Boolean aborting,
            Boolean canceling) {
        this.flowNodeInstanceId = flowNodeInstanceId;
        this.stateId = stateId;
        this.executing = executing;
        this.aborting = aborting;
        this.canceling = canceling;
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": flowNodeInstanceId: " + flowNodeInstanceId + " (" + stateId + ", "
                + executing + ", " + aborting + ", " + canceling + ")";
    }

    @Override
    public CompletableFuture<Void> work(final Map<String, Object> context) throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);

        SFlowNodeInstance flowNodeInstance = retrieveAndVerifyFlowNodeInstance(tenantAccessor);
        tenantAccessor.getFlowNodeExecutor().executeFlowNode(flowNodeInstance, null, null);
        return CompletableFuture.completedFuture(null);
    }

    private SFlowNodeInstance retrieveAndVerifyFlowNodeInstance(TenantServiceAccessor tenantAccessor)
            throws SFlowNodeReadException, SWorkPreconditionException {
        SFlowNodeInstance flowNodeInstance;
        try {
            flowNodeInstance = tenantAccessor.getActivityInstanceService().getFlowNodeInstance(flowNodeInstanceId);
        } catch (SFlowNodeNotFoundException e) {
            throw new SWorkPreconditionException(String.format("Flow node %d does not exists", flowNodeInstanceId), e);
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
                            flowNodeInstance.getId(), stateId, executing, aborting, canceling,
                            flowNodeInstance.getStateId(),
                            flowNodeInstance.isStateExecuting(), flowNodeInstance.isAborting(),
                            flowNodeInstance.isCanceling()));
        }
        return flowNodeInstance;
    }

    @Override
    public void handleFailure(final Throwable e, final Map<String, Object> context) throws Exception {
        TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final UserTransactionService userTransactionService = tenantAccessor.getUserTransactionService();
        TechnicalLoggerService loggerService = tenantAccessor.getTechnicalLoggerService();
        WaitingEventsInterrupter waitingEventsInterrupter = new WaitingEventsInterrupter(
                tenantAccessor.getEventInstanceService(),
                tenantAccessor.getSchedulerService(), loggerService);
        FailedStateSetter failedStateSetter = new FailedStateSetter(waitingEventsInterrupter,
                tenantAccessor.getActivityInstanceService(),
                tenantAccessor.getFlowNodeStateManager(), loggerService);
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
