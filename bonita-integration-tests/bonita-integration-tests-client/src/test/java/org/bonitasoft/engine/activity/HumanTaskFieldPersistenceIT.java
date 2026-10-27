/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.Date;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.junit.Test;

/**
 * BPA-167 — guards the two <b>single-transaction</b> hazards that come with replacing the whole-row
 * {@code recordUpdate} in {@code FlowNodeInstancesServiceImpl.setExpectedEndDate} by an atomic named query
 * followed by {@code getPersistenceService().refresh(flowNodeInstance)}.
 * <p>
 * Nothing here is concurrent. Everything each test exercises happens inside <b>one</b> engine transaction:
 * the INITIALIZING state execution of a user task. A user task is driven by
 * {@code InitializingActivityWithBoundaryEventsState} (wired in {@code UserTaskStates}), whose
 * {@code afterConnectors} runs, in this order:
 *
 * <pre>
 * stateBehaviors.updateDisplayNameAndDescription(...)
 * stateBehaviors.updateExpectedDuration(...)
 * </pre>
 *
 * and the same pair exists in {@code InitializingActivityState} for manual tasks. The state execution then
 * returns {@code DONE}, so {@code FlowNodeExecutorImpl.executeStateAndReturnNextState} calls
 * {@code getNextNormalState}, which calls {@code activityInstanceService.setState(...)} — still the same
 * transaction, still the same managed entity.
 * <h2>What each test detects</h2>
 * <ul>
 * <li><b>{@link #setExpectedEndDate_should_survive_the_state_transition_that_follows_it_in_the_same_transaction()}</b>
 * — the <i>self-revert</i>. An atomic {@code UPDATE ... WHERE id = :id} never touches
 * the managed entity, so the entity still holds the pre-update {@code expectedEndDate}. When {@code setState}
 * re-dirties that entity a few frames later, the commit flush writes the whole row back from the stale snapshot
 * and the due date is silently lost. The {@code refresh} in {@code setExpectedEndDate} is what prevents this,
 * so this test goes <b>red if that refresh is missing or placed after a subsequent read/mutation</b>.</li>
 * <li><b>{@link #setExpectedEndDate_should_not_discard_the_display_name_written_earlier_in_the_same_transaction()}</b>
 * — the <i>inverse</i> hazard. {@code Session.refresh()} also discards <i>prior un-flushed</i> mutations on
 * the entity, and {@code updateDisplayNameAndDescription} performs exactly one such mutation on the line
 * above: it goes through {@code recordUpdate}, which only reflects the new values onto the managed entity
 * (see {@code HibernatePersistenceService.update}) and issues <b>no SQL</b>. So this test goes
 * <b>red if the refresh reloads the row before that pending display-name update has been flushed</b>.</li>
 * </ul>
 * <h2>Expected colour</h2>
 * These are <b>regression guards, not reproduction tests</b>: they are expected green on the unfixed code
 * (where dirty-checking writes every column at commit) and green after a correct fix. They only turn red if
 * the fix's refresh is wrong — a failure mode that no pre-existing test covers, because no pre-existing test
 * puts a display name and an expected duration on the same task.
 *
 * @see org.bonitasoft.engine.process.ProcessExecutionIT#should_update_and_sort_due_date_with_null_values()
 * @see org.bonitasoft.engine.filter.user.UserFilterIT#filterTaskWithAutoAssign()
 */
public class HumanTaskFieldPersistenceIT extends TestWithUser {

    private static final String TASK_NAME = "step1";

    private static final long EXPECTED_DURATION_IN_MILLIS = 3_600_000L;

    private static final String DISPLAY_NAME = "Review the expense report";

    private static final String DISPLAY_DESCRIPTION = "Check the receipts, then approve or reject";

    /**
     * The engine-path self-revert of {@code setExpectedEndDate}: its atomic update must survive the state
     * transition that follows it in the same transaction.
     */
    @Test
    public void setExpectedEndDate_should_survive_the_state_transition_that_follows_it_in_the_same_transaction()
            throws Exception {
        // given: a user task carrying an expected duration, so that StateBehaviors.updateExpectedDuration
        // fires inside the INITIALIZING state execution
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder()
                .createNewInstance("BPA167_expectedEndDateSurvivesStateTransition", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask(TASK_NAME, ACTOR_NAME).addExpectedDuration(
                new ExpressionBuilder().createConstantLongExpression(EXPECTED_DURATION_IN_MILLIS));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);

        // when: the task reaches READY, which means the whole INITIALIZING transaction has committed
        // (the READY event is fired from WaitForEventSynchronization.afterCompletion, STATUS_COMMITTED only),
        // so updateExpectedDuration AND the setState that follows it in FlowNodeExecutorImpl are both behind us
        final long beforeStart = System.currentTimeMillis();
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance task = waitForUserTaskAndGetIt(processInstance, TASK_NAME);
        final long afterReady = System.currentTimeMillis();

        // then
        assertThat(task.getExpectedEndDate())
                .as("BPA-167: the expected end date written by FlowNodeInstancesServiceImpl.setExpectedEndDate "
                        + "must still be in the database after the commit. A null here means the managed entity was "
                        + "never refreshed after the atomic UPDATE, so it still held the pre-update value when "
                        + "the setState in FlowNodeExecutorImpl re-dirtied it, and the commit flush wrote the "
                        + "whole row back from the stale snapshot. Check that setExpectedEndDate calls "
                        + "getPersistenceService().refresh(flowNodeInstance) right after recordUpdateWithQuery")
                .isNotNull()
                .isBetween(new Date(beforeStart + EXPECTED_DURATION_IN_MILLIS),
                        new Date(afterReady + EXPECTED_DURATION_IN_MILLIS + 1));

        disableAndDeleteProcess(processDefinition);
    }

    /**
     * The inverse hazard of the refresh: {@code Session.refresh()} discards prior un-flushed mutations, and
     * {@code updateDisplayNameAndDescription} leaves exactly one just before {@code updateExpectedDuration}.
     */
    @Test
    public void setExpectedEndDate_should_not_discard_the_display_name_written_earlier_in_the_same_transaction()
            throws Exception {
        // given: ONE user task carrying BOTH a display name/description AND an expected duration, so that
        // updateDisplayNameAndDescription dirties the managed entity without issuing SQL and
        // updateExpectedDuration then runs the atomic update + refresh on that very same entity
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder()
                .createNewInstance("BPA167_displayNameSurvivesExpectedEndDate", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        final UserTaskDefinitionBuilder taskBuilder = builder.addUserTask(TASK_NAME, ACTOR_NAME);
        taskBuilder.addExpectedDuration(
                new ExpressionBuilder().createConstantLongExpression(EXPECTED_DURATION_IN_MILLIS));
        taskBuilder.addDisplayName(new ExpressionBuilder().createConstantStringExpression(DISPLAY_NAME));
        taskBuilder.addDisplayDescription(new ExpressionBuilder().createConstantStringExpression(DISPLAY_DESCRIPTION));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);

        // when
        final long beforeStart = System.currentTimeMillis();
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance task = waitForUserTaskAndGetIt(processInstance, TASK_NAME);
        final long afterReady = System.currentTimeMillis();

        // then: all three writes of the single INITIALIZING transaction must have reached the database.
        // Soft assertions on purpose — if the refresh is mis-ordered the build log should show which of the
        // three values was lost, not just the first one.
        assertSoftly(soft -> {
            soft.assertThat(task.getDisplayName())
                    .as("BPA-167: the display name set by StateBehaviors.updateDisplayNameAndDescription "
                            + "just before updateExpectedDuration must survive. That call goes through "
                            + "recordUpdate, which only mutates the managed entity and issues no SQL — the column is "
                            + "still null in database at that point. A null here therefore means the refresh() added "
                            + "to FlowNodeInstancesServiceImpl.setExpectedEndDate reloaded the row while that "
                            + "mutation was still pending, and discarded it")
                    .isEqualTo(DISPLAY_NAME);
            soft.assertThat(task.getDisplayDescription())
                    .as("BPA-167: the display description set by "
                            + "StateBehaviors.updateDisplayNameAndDescription, same pending-mutation mechanism "
                            + "as the display name above")
                    .isEqualTo(DISPLAY_DESCRIPTION);
            soft.assertThat(task.getExpectedEndDate())
                    .as("BPA-167: the expected end date must survive the commit even when a display "
                            + "name/description update is pending on the same entity — same mechanism as the "
                            + "self-revert test in this class")
                    .isNotNull()
                    .isBetween(new Date(beforeStart + EXPECTED_DURATION_IN_MILLIS),
                            new Date(afterReady + EXPECTED_DURATION_IN_MILLIS + 1));
        });

        disableAndDeleteProcess(processDefinition);
    }

}
