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

import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.BoundaryEventInstance;
import org.bonitasoft.engine.bpm.flownode.CallActivityInstance;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.LoopActivityInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class InterruptingTimerBoundaryEventIT extends AbstractEventIT {

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary", "Timer",
            "Interrupting" }, story = "Execute timer boundary event triggerd.", jira = "ENGINE-500")
    @Test
    public void timerBoundaryEventTriggered() throws Exception {
        final int timerDuration = 1000;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryTimerEvent(timerDuration, true, "step1", "exceptionStep", "step2");
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance.getId(), "step1");

        // wait timer trigger
        waitForUserTaskAndExecuteIt(processInstance, "exceptionStep", user);
        waitForFlowNodeInState(processInstance, "step1", TestStates.ABORTED, true);
        waitForProcessToFinish(processInstance);

        checkFlowNodeWasntExecuted(processInstance.getId(), "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void timerBoundaryEventWithScriptThatFail() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("pTimerBoundary", "2.0");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("start");
        final UserTaskDefinitionBuilder userTaskDefinitionBuilder = processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        userTaskDefinitionBuilder.addBoundaryEvent("timer", true).addTimerEventTriggerDefinition(TimerType.DURATION,
                new ExpressionBuilder().createGroovyScriptExpression("script", "throw new java.lang.RuntimeException()", Long.class.getName()));
        processDefinitionBuilder.addAutomaticTask("timerStep");
        processDefinitionBuilder.addTransition("timer", "timerStep");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForFlowNodeInFailedState(processInstance, "step1");

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { EventInstance.class, CallActivityInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary", "Interrupting",
            "Timer", "Call Activity" }, story = "Execute timer boundary event triggered on call activity.", jira = "ENGINE-547")
    @Test
    public void timerBoundaryEventTriggeredOnCallActivity() throws Exception {
        final int timerDuration = 2000;
        final String simpleProcessName = "targetProcess";
        final String simpleTaskName = "stepCA";

        // deploy a simple process p1
        final ProcessDefinition targetProcessDefinition = deployAndEnableSimpleProcess(simpleProcessName, simpleTaskName);

        // deploy a process, p2, with a call activity calling p1. The call activity has an interrupting timer boundary event
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryTimerEventOnCallActivity(timerDuration, true, simpleProcessName);

        // start the root process and wait for boundary event trigger
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long stepCAId = waitForUserTask(processInstance, simpleTaskName);
        // wait timer trigger
        // check that the exception flow was taken
        waitForUserTaskAndExecuteIt(processInstance, "exceptionStep", user);
        waitForProcessToFinish(processInstance);

        final ArchivedActivityInstance archActivityInst = getProcessAPI().getArchivedActivityInstance(stepCAId);
        assertEquals(TestStates.ABORTED.getStateName(), archActivityInst.getState());

        checkFlowNodeWasntExecuted(processInstance.getId(), PARENT_PROCESS_USER_TASK_NAME);

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(targetProcessDefinition);
    }

    @Cover(classes = { EventInstance.class, MultiInstanceLoopCharacteristics.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary",
            "Interrupting", "Timer", "MultiInstance", "Sequential" }, story = "Execute interrupting timer boundary event triggered on sequential multi-instance.", jira = "ENGINE-547")
    @Test
    public void timerBoundaryEventTriggeredOnSequentialMultiInstance() throws Exception {
        // deploy a process with a interrupting timer boundary event attached to a sequential multi-instance
        final int timerDuration = 1000;
        final String multiTaskName = "step1";
        final ProcessDefinition processDefinition = deployAndEnableProcessMultiInstanceWithBoundaryEvent(timerDuration, true, multiTaskName, 4, true, "step2",
                "exceptionStep");

        // start the process and wait the timer to trigger
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance.getId(), multiTaskName);
        // wait timer trigger
        // check that the exception flow was taken
        waitForUserTaskAndExecuteIt(processInstance, "exceptionStep", user);
        waitForFlowNodeInState(processInstance, multiTaskName, TestStates.ABORTED, true);
        waitForProcessToFinish(processInstance);

        checkFlowNodeWasntExecuted(processInstance.getId(), "step2");

        disableAndDeleteProcess(processDefinition.getId());
    }

    @Cover(classes = { EventInstance.class, MultiInstanceLoopCharacteristics.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary", "Timer",
            "MultiInstance", "Parallel" }, story = "Execute timer boundary event triggered on parallel multi-instance.", jira = "ENGINE-547")
    @Test
    public void timerBoundaryEventTriggeredOnParallelMultiInstance() throws Exception {
        final int timerDuration = 1000;
        final int loopCardinality = 4;
        final boolean isSequential = false;

        // deploy a process with a interrupting timer boundary event attached to a parallel multi-instance
        final ProcessDefinition processDefinition = deployAndEnableProcessMultiInstanceWithBoundaryEvent(timerDuration, true, "step1", loopCardinality,
                isSequential, "step2", "exceptionStep");

        // start the process and wait for process to be triggered
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");
        // wait timer trigger
        waitForUserTaskAndExecuteIt(processInstance, "exceptionStep", user);
        waitForProcessToFinish(processInstance);

        final ArchivedActivityInstance archActivityInst = getProcessAPI().getArchivedActivityInstance(step1Id);
        assertEquals(TestStates.ABORTED.getStateName(), archActivityInst.getState());

        checkFlowNodeWasntExecuted(processInstance.getId(), "step2");

        disableAndDeleteProcess(processDefinition.getId());
    }

    @Cover(classes = { EventInstance.class, LoopActivityInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary", "Interrupging",
            "Timer", "Loop activity" }, story = "Interrupting timer boundary event triggered on loop activity", jira = "ENGINE-547")
    @Test
    public void timerBoundaryEventTriggeredOnLoopActivity() throws Exception {
        final long timerDuration = 1000;
        final int loopMax = 2;
        final String loopActivityName = "step1";
        final String normalFlowStepName = "step2";
        final String exceptionFlowStepName = "exceptionStep";
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryTimerEventOnLoopActivity(timerDuration, true, loopMax, loopActivityName,
                normalFlowStepName, exceptionFlowStepName);

        // start the process and wait timer to trigger
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance.getId(), loopActivityName);
        Thread.sleep(timerDuration); // wait timer trigger

        // verify that the exception flow was taken
        waitForUserTaskAndExecuteIt(processInstance, exceptionFlowStepName, user);
        // verify that the normal flow was aborted
        waitForFlowNodeInState(processInstance, loopActivityName, TestStates.ABORTED, true);
        waitForProcessToFinish(processInstance);

        checkFlowNodeWasntExecuted(processInstance.getId(), normalFlowStepName);

        disableAndDeleteProcess(processDefinition.getId());
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.EVENTS, keywords = { "Timer boundary event", "Data", "Long", "On process", "Human task",
            "Exception" }, story = "Execute a process with a long data and a human task with a timer boundary event that throw a exception", jira = "ENGINE-1383")
    @Test
    public void timerBoundaryEventTriggeredAndLongData() throws Exception {
        final int timerDuration = 1000;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryTimerEventOnHumanTask(timerDuration, true);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // Wait and execute the step1 with a timer boundary event
        waitForUserTaskAndAssigneIt(processInstance, "step1", user);

        // wait timer trigger
        waitForUserTaskAndExecuteIt(processInstance, "exceptionStep", user);
        waitForFlowNodeInState(processInstance, "step1", TestStates.ABORTED, true);
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
    }

}
