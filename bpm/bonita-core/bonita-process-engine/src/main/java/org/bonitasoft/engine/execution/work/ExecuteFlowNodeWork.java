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
import org.bonitasoft.engine.core.process.instance.model.SHumanTaskInstance;
import org.bonitasoft.engine.execution.WaitingEventsInterrupter;
import org.bonitasoft.engine.service.ServiceAccessor;
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
        final ServiceAccessor serviceAccessor = getServiceAccessor(context);
        SFlowNodeInstance flowNodeInstance = retrieveAndVerifyFlowNodeInstance(serviceAccessor);
        serviceAccessor.getFlowNodeExecutor().executeFlowNode(flowNodeInstance, null, null);
        return CompletableFuture.completedFuture(null);
    }

    private SFlowNodeInstance retrieveAndVerifyFlowNodeInstance(ServiceAccessor serviceAccessor)
            throws SFlowNodeReadException, SWorkPreconditionException {
        SFlowNodeInstance flowNodeInstance;
        try {
            flowNodeInstance = serviceAccessor.getActivityInstanceService().getFlowNodeInstance(flowNodeInstanceId);
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
                            + "%s Current state name: %s. Last writer trace: %s",
                            flowNodeInstance.getId(), stateId, executing, aborting, canceling,
                            flowNodeInstance.getStateId(),
                            flowNodeInstance.isStateExecuting(), flowNodeInstance.isAborting(),
                            flowNodeInstance.isCanceling(),
                            probableCause(flowNodeInstance), flowNodeInstance.getStateName(),
                            lastWriterTrace(flowNodeInstance)));
        }
        return flowNodeInstance;
    }

    /**
     * The probable cause of a precondition failure, as precise as the mismatch allows (BPA-167).
     * <p>
     * Only one mismatch is the signature of a lost update: same state, same aborting and canceling flags, but the
     * execution flag that this work expected to be set has been cleared. That happens when a concurrent update
     * writes the whole row back from a snapshot loaded before the execution was requested, and it may also be a
     * stale cached copy read by this work while the database row is correct. Since BPA-167, assignment, priority
     * and due-date changes write only their own columns and cannot write the flag back any more; the other writers
     * of this table still update the whole row from their own in-memory copy and can: display name and
     * description, state, executedBy, and the addition of a manual subtask ({@code ProcessAPI.addManualUserTask},
     * which writes the parent task back and increments its {@code tokenCount}). Only for this mismatch is the
     * work's loss a consequence worth stating: the database row tells whether the task is stuck. Any other mismatch
     * keeps the historical reading: another execution already moved the flow node on.
     */
    private String probableCause(final SFlowNodeInstance flowNodeInstance) {
        final boolean executionFlagClearedOnly = executing && !flowNodeInstance.isStateExecuting()
                && stateId == flowNodeInstance.getStateId()
                && aborting == flowNodeInstance.isAborting()
                && canceling == flowNodeInstance.isCanceling();
        if (executionFlagClearedOnly) {
            return " The execution flag was cleared while the state did not change (a concurrent whole-row update,"
                    + " or a stale cached read). This work is dropped and is NOT retried: if the database row shows"
                    + " stateExecuting=false, the flow node is stuck; otherwise recovery will execute it.";
        }
        return " Someone probably already called execute on it.";
    }

    /**
     * The fields that let support identify the <b>last writer</b> of this row from the first log bundle,
     * without asking the customer to reproduce with DEBUG enabled (BPA-167).
     * <p>
     * A precondition failure only says the row is not in the expected state; it does not say who changed it.
     * {@code lastUpdateDate} bounds when the last write happened. For a human task, a {@code claimedDate} equal to
     * {@code lastUpdateDate} means an assignment wrote last; since BPA-167 an assignment cannot clear the
     * execution flag, so in that case the flag was cleared by an earlier write. A release does not leave that
     * mark: it writes {@code assigneeId=0} and {@code claimedDate=0} with a fresh {@code lastUpdateDate}, which is
     * also what a task that was never assigned shows, so {@code claimedDate=0} identifies no writer. A differing
     * non-zero {@code claimedDate} points at another writer. For a human task, {@code tokenCount} is incremented
     * when a manual subtask is added to it (on loops, multi-instance and call activities it counts other things,
     * so it is not traced for them).
     * <p>
     * Metadata only: ids, timestamps and states, never business data.
     */
    private static String lastWriterTrace(final SFlowNodeInstance flowNodeInstance) {
        final StringBuilder trace = new StringBuilder();
        trace.append("lastUpdateDate=").append(flowNodeInstance.getLastUpdateDate());
        if (flowNodeInstance instanceof SHumanTaskInstance humanTask) {
            trace.append(", assigneeId=").append(humanTask.getAssigneeId())
                    .append(", claimedDate=").append(humanTask.getClaimedDate())
                    .append(", tokenCount=").append(humanTask.getTokenCount());
        }
        return trace.toString();
    }

    @Override
    public void handleFailure(final Throwable e, final Map<String, Object> context) throws Exception {
        ServiceAccessor serviceAccessor = getServiceAccessor(context);
        final UserTransactionService userTransactionService = serviceAccessor.getUserTransactionService();
        WaitingEventsInterrupter waitingEventsInterrupter = new WaitingEventsInterrupter(
                serviceAccessor.getEventInstanceService(),
                serviceAccessor.getSchedulerService());
        FailedStateSetter failedStateSetter = new FailedStateSetter(waitingEventsInterrupter,
                serviceAccessor.getActivityInstanceService(),
                serviceAccessor.getFlowNodeStateManager());
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
