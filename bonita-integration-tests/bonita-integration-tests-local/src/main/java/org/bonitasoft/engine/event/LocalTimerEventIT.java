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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.LocalServerTestsInitializer;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.test.CommonAPILocalIT;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.runner.BonitaSuiteRunner;
import org.bonitasoft.engine.test.runner.BonitaTestRunner;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(BonitaTestRunner.class)
@BonitaSuiteRunner.Initializer(LocalServerTestsInitializer.class)
public class LocalTimerEventIT extends CommonAPILocalIT {

    private static final String TIMER_EVENT_PREFIX = "Timer_Ev_";

    @Before
    public void setUp() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @After
    public void tearDown() throws BonitaException {
        logoutOnTenant();
    }

    @Test
    public void timerJobsAreDeleteOnDisable() throws Exception {
        final ProcessDefinitionBuilder definitionBuilder = new ProcessDefinitionBuilder().createNewInstance("proc", "1.0");
        final String timerEventName = "startTimer";
        definitionBuilder.addStartEvent(timerEventName).addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createConstantLongExpression(2000));
        definitionBuilder.addAutomaticTask("auto");
        definitionBuilder.addTransition(timerEventName, "auto");

        final ProcessDefinition processDefinition = deployAndEnableProcess(definitionBuilder.done());
        final String jobName = getJobName(processDefinition, timerEventName);
        assertTrue(containsTimerJob(jobName));

        getProcessAPI().disableProcess(processDefinition.getId());
        assertFalse(containsTimerJob(jobName));

        getProcessAPI().enableProcess(processDefinition.getId());
        assertTrue(containsTimerJob(jobName));

        disableAndDeleteProcess(processDefinition.getId());
        assertFalse(containsTimerJob(jobName));

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

    private String getJobName(final ProcessDefinition processDefinition, final String timerEventName) {
        return TIMER_EVENT_PREFIX + processDefinition.getId() + timerEventName;
    }

    private String getJobName(final long eventInstanceId) {
        return TIMER_EVENT_PREFIX + eventInstanceId;
    }

    private ProcessDefinition deployProcessWithTimerIntermediateCatchEvent(final TimerType timerType, final Expression timerValue, final String stepName)
            throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My Process with start event", "1.0");
        processDefinitionBuilder.addIntermediateCatchEvent("intermediateCatchEvent").addTimerEventTriggerDefinition(timerType, timerValue);
        processDefinitionBuilder.addAutomaticTask(stepName);
        processDefinitionBuilder.addEndEvent("end");
        processDefinitionBuilder.addTransition("intermediateCatchEvent", stepName);
        processDefinitionBuilder.addTransition(stepName, "end");

        final ProcessDefinition definition = deployAndEnableProcess(processDefinitionBuilder.getProcess());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        return definition;
    }

    @Test
    public void cancelProcessInstanceWithTimerIntermediateCatchEvent() throws Exception {
        // given
        final int timerTrigger = 20000; // the timer intermediate catch event will wait 20 seconds
        final Expression timerExpression = new ExpressionBuilder().createConstantLongExpression(timerTrigger);
        final ProcessDefinition definition = deployProcessWithTimerIntermediateCatchEvent(TimerType.DURATION, timerExpression, "step");

        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        final FlowNodeInstance intermediateCatchEvent = waitForFlowNodeInWaitingState(processInstance, "intermediateCatchEvent", false);
        final Long floNodeInstanceId = intermediateCatchEvent.getId();
        final String jobName = getJobName(floNodeInstanceId);
        assertThat(containsTimerJob(jobName)).isTrue();

        // when
        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // then
        waitForFlowNodeInState(processInstance, "intermediateCatchEvent", TestStates.CANCELLED, false);
        assertThat(containsTimerJob(jobName)).isFalse();

        waitForProcessToBeInState(processInstance, ProcessInstanceState.CANCELLED);
        checkWasntExecuted(processInstance, "step");

        disableAndDeleteProcess(definition);
    }

}
