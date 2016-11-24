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
package org.bonitasoft.engine.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.exception.BonitaRuntimeException;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.impl.ServiceAccessorFactory;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Test;

public class LocalInterruptingTimerBoundaryEventIT extends AbstractEventIT {

    private static final String TIMER_EVENT_PREFIX = "Timer_Ev_";

    protected PlatformServiceAccessor getPlatformAccessor() {
        try {
            return ServiceAccessorFactory.getInstance().createPlatformServiceAccessor();
        } catch (final Exception e) {
            throw new BonitaRuntimeException(e);
        }
    }

    protected static void setSessionInfo(final APISession session) throws Exception {
        final SessionAccessor sessionAccessor = ServiceAccessorFactory.getInstance().createSessionAccessor();
        sessionAccessor.setSessionInfo(session.getId(), session.getTenantId());
    }

    private boolean containsTimerJob(final String jobName) throws Exception {
        setSessionInfo(getSession());
        final SchedulerService schedulerService = getPlatformAccessor().getSchedulerService();
        final TransactionService transactionService = getPlatformAccessor().getTransactionService();
        transactionService.begin();
        try {
            final List<String> jobs = schedulerService.getJobs();
            for (final String serverJobName : jobs) {
                if (serverJobName.contains(jobName)) {
                    return true;
                }
            }
        } finally {
            transactionService.complete();
        }
        return false;
    }

    private String getJobName(final long eventInstanceId) {
        return TIMER_EVENT_PREFIX + eventInstanceId;
    }

    @Test
    // when the boundary event is not triggered we will have the same behavior for interrupting and non-interrupting events; only interrupting will be tested
    public void timerBoundaryEventNotTriggered() throws Exception {
        // given
        final long timerDuration = 20000;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryTimerEvent(timerDuration, true, "step1", "exceptionStep", "step2");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final FlowNodeInstance timer = waitForFlowNodeInWaitingState(processInstance, "timer", false);
        final Long boundaryId = timer.getId();
        assertThat(containsTimerJob(getJobName(boundaryId))).isTrue();

        // when
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);

        // then
        waitForFlowNodeInState(processInstance, "timer", TestStates.ABORTED, false);
        assertThat(containsTimerJob(getJobName(boundaryId))).isFalse();

        waitForUserTaskAndExecuteIt(processInstance, "step2", user);
        waitForProcessToFinish(processInstance);

        checkFlowNodeWasntExecuted(processInstance.getId(), "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    // when the boundary event is not triggered we will have the same behavior for interrupting and non-interrupting events; only interrupting will be tested
    public void timerBoundaryEventNotTriggeredOnCallActivity() throws Exception {
        // given
        final long timerDuration = 20000;
        final String simpleProcessName = "targetProcess";
        final String simpleTaskName = "stepCA";

        // deploy a simple process p1
        final ProcessDefinition targetProcessDefinition = deployAndEnableSimpleProcess(simpleProcessName, simpleTaskName);

        // deploy a process, p2, with a call activity calling p1. The call activity has an interrupting timer boundary event
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryTimerEventOnCallActivity(timerDuration, true, simpleProcessName);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final FlowNodeInstance timer = waitForFlowNodeInWaitingState(processInstance, "timer", false);
        final Long boundaryId = timer.getId();
        assertThat(containsTimerJob(getJobName(boundaryId))).isTrue();

        // when
        waitForUserTaskAndExecuteIt(processInstance, "stepCA", user);

        // then
        waitForFlowNodeInState(processInstance, "timer", TestStates.ABORTED, false);
        assertThat(containsTimerJob(getJobName(boundaryId))).isFalse();

        waitForUserTaskAndExecuteIt(processInstance, PARENT_PROCESS_USER_TASK_NAME, user);
        waitForProcessToFinish(processInstance);
        checkFlowNodeWasntExecuted(processInstance.getId(), EXCEPTION_STEP);

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(targetProcessDefinition);
    }

    @Test
    // when the boundary event is not triggered we will have the same behavior for interrupting and non-interrupting events; only interrupting will be tested
    public void timerBoundaryEventNotTriggeredOnSequentialMultiInstance() throws Exception {
        // given
        final long timerDuration = 20000;
        final int loopCardinality = 1;
        final boolean isSequential = true;
        final ProcessDefinition processDefinition = deployAndEnableProcessMultiInstanceWithBoundaryEvent(timerDuration, true, "step1", loopCardinality,
                isSequential, "step2", "exceptionStep");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final FlowNodeInstance timer = waitForFlowNodeInWaitingState(processInstance, "timer", false);
        final Long boundaryId = timer.getId();
        assertThat(containsTimerJob(getJobName(boundaryId))).isTrue();

        // when
        waitForUserTasksAndExecuteIt("step1", processInstance, loopCardinality);

        // then
        waitForFlowNodeInState(processInstance, "timer", TestStates.ABORTED, false);
        assertThat(containsTimerJob(getJobName(boundaryId))).isFalse();

        waitForUserTaskAndExecuteIt(processInstance, "step2", user);
        waitForProcessToFinish(processInstance);

        checkFlowNodeWasntExecuted(processInstance.getId(), "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    // when the boundary event is not triggered we will have the same behavior for interrupting and non-interrupting events; only interrupting will be tested
    public void timerBoundaryEventNotTriggeredOnParallelMultiInstance() throws Exception {
        // given
        final long timerDuration = 20000;
        final int loopCardinality = 2;
        final boolean isSequential = false;
        final ProcessDefinition processDefinition = deployAndEnableProcessMultiInstanceWithBoundaryEvent(timerDuration, true, "step1", loopCardinality,
                isSequential, "step2", "exceptionStep");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final FlowNodeInstance timer = waitForFlowNodeInWaitingState(processInstance, "timer", false);
        final Long boundaryId = timer.getId();
        assertThat(containsTimerJob(getJobName(boundaryId))).isTrue();

        // when
        waitForUserTasksAndExecuteIt("step1", processInstance, loopCardinality);

        // then
        waitForFlowNodeInState(processInstance, "timer", TestStates.ABORTED, false);
        assertThat(containsTimerJob(getJobName(boundaryId))).isFalse();

        waitForUserTaskAndExecuteIt(processInstance, "step2", user);
        waitForProcessToFinish(processInstance);
        checkFlowNodeWasntExecuted(processInstance.getId(), "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    // when the boundary event is not triggered we will have the same behavior for interrupting and non-interrupting events; only interrupting will be tested
    public void timerBoundaryEventNotTriggeredOnLoopActivity() throws Exception {
        // given
        final long timerDuration = 20000;
        final int loopMax = 1;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryTimerEventOnLoopActivity(timerDuration, true, loopMax, "step1", "step2",
                "exceptionStep");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final FlowNodeInstance timer = waitForFlowNodeInWaitingState(processInstance, "timer", false);
        final Long boundaryId = timer.getId();
        assertThat(containsTimerJob(getJobName(boundaryId))).isTrue();

        // when
        waitForUserTasksAndExecuteIt("step1", processInstance, loopMax);

        // then
        waitForFlowNodeInState(processInstance, "timer", TestStates.ABORTED, false);
        assertThat(containsTimerJob(getJobName(boundaryId))).isFalse();

        waitForUserTaskAndExecuteIt(processInstance, "step2", user);
        waitForProcessToFinish(processInstance);
        checkFlowNodeWasntExecuted(processInstance.getId(), "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

}
