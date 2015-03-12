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

import org.bonitasoft.engine.bpm.flownode.BoundaryEventInstance;
import org.bonitasoft.engine.bpm.flownode.CallActivityInstance;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.LoopActivityInstance;
import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Elias Ricken de Medeiros
 */
public class NonInterruptingTimerBoundaryEventIT extends AbstractEventIT {

    @Cover(classes = { EventInstance.class, BoundaryEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary event", "Timer",
            "Non-interrupting" }, story = "Execute non-interrupting timer boundary event triggered.", jira = "ENGINE-1042")
    @Test
    public void nonInterruptTimerBoundaryEventTriggered() throws Exception {
        // deploy process with non-interrupting boundary event
        final long timerDuration = 1000;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryTimerEvent(timerDuration, false, "step1", "exceptionStep", "step2");

        // start the process and wait for timer to trigger
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance.getId(), "step1");
        Thread.sleep(timerDuration); // wait timer trigger

        // check that the exception flow was taken
        final long exceptionFlowStepId = waitForUserTask(processInstance, "exceptionStep");

        // execute the task containing the boundary and verify that the normal flow continues
        assignAndExecuteStep(step1Id, user);
        final long normalFlowStepId = waitForUserTask(processInstance, "step2");

        // execute exception flow step and normal flow step and verify that the process has finished
        assignAndExecuteStep(exceptionFlowStepId, user);
        assignAndExecuteStep(normalFlowStepId, user);
        waitForProcessToFinish(processInstance);

        // clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { EventInstance.class, CallActivityInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary", "Interrupting",
            "Timer", "Call Activity" }, story = "Execute timer boundary event triggered on call activity.", jira = "ENGINE-1042")
    @Test
    public void nonInterruptTimerBoundaryEventTriggeredOnCallActivity() throws Exception {
        final long timerDuration = 2000;
        final String simpleProcessName = "targetProcess";
        final String simpleTaskName = "stepCA";

        // deploy a simple process p1
        final ProcessDefinition targetProcessDefinition = deployAndEnableSimpleProcess(simpleProcessName, simpleTaskName);

        // deploy a process, p2, with a call activity calling p1. The call activity has a non-interrupting timer boundary event
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryTimerEventOnCallActivity(timerDuration, false, simpleProcessName);

        // start the root process and wait for boundary event triggering
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long stepCAId = waitForUserTask(processInstance.getId(), simpleTaskName);
        Thread.sleep(timerDuration); // wait timer trigger

        // check that the exception flow was taken
        final long exceptionFlowStepId = waitForUserTask(processInstance.getId(), EXCEPTION_STEP);

        // execute the user task of p1 and check that the normal flow also was taken
        assignAndExecuteStep(stepCAId, user.getId());
        final long normalFlowStepId = waitForUserTask(processInstance.getId(), PARENT_PROCESS_USER_TASK_NAME);

        // execute exception flow and normal flow and verify that the process completes
        assignAndExecuteStep(exceptionFlowStepId, user);
        assignAndExecuteStep(normalFlowStepId, user);
        waitForProcessToFinish(processInstance);

        // clean up
        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(targetProcessDefinition);
    }

    @Cover(classes = { EventInstance.class, MultiInstanceLoopCharacteristics.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary",
            "Non-interrupting", "Timer", "MultiInstance", "Sequential" }, story = "Execute non-interrupting timer boundary event triggered on sequential multi-instance.", jira = "ENGINE-1042")
    @Test
    public void nonInterruptTimerBoundaryEventTriggeredOnSequentialMultiInstance() throws Exception {
        // deploy a process with a non-interrupting timer boundary event attached to a sequential multi-instance
        final long timerDuration = 1000;
        final String multiTaskName = "step1";
        final String exceptionFlowTaskName = "exceptionStep";
        final int loopCardinality = 2;
        final String normalFlowTaskName = "step2";
        final ProcessDefinition processDefinition = deployAndEnableProcessMultiInstanceWithBoundaryEvent(timerDuration, false, multiTaskName, loopCardinality,
                true,
                normalFlowTaskName, exceptionFlowTaskName);

        // start the process and wait the timer to trigger
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long multiInstanceId = waitForUserTask(processInstance, multiTaskName);
        Thread.sleep(timerDuration); // wait timer trigger

        // check that the exception flow was taken
        final long exceptionFlowStepId = waitForUserTask(processInstance, exceptionFlowTaskName);

        // execute multi-instances and verify that normal flow continues
        assignAndExecuteStep(multiInstanceId, user);
        waitForUserTasksAndExecuteIt(multiTaskName, processInstance, loopCardinality - 1);
        final long normalFlowStepId = waitForUserTask(processInstance, normalFlowTaskName);

        // execute exception flow and normal flow and verify that the process completes
        assignAndExecuteStep(exceptionFlowStepId, user);
        assignAndExecuteStep(normalFlowStepId, user);
        waitForProcessToFinish(processInstance);

        // clean up
        disableAndDeleteProcess(processDefinition.getId());
    }

    @Cover(classes = { EventInstance.class, MultiInstanceLoopCharacteristics.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary",
            "Non-interrupting", "Timer", "MultiInstance", "Parallel" }, story = "Non-interrupting timer boundary event attached to parallel multi-instance.", jira = "ENGINE-1042")
    @Test
    public void nonInterruptTimerBoundaryEventTriggeredOnParallelMultiInstance() throws Exception {
        final long timerDuration = 1000;
        final int loopCardinality = 2;
        // deploy a process with a interrupting timer boundary event attached to a parallel multi-instance
        final String multiTaskName = "step1";
        final String normalTaskName = "step2";
        final ProcessDefinition processDefinition = deployAndEnableProcessMultiInstanceWithBoundaryEvent(timerDuration, false, multiTaskName, loopCardinality,
                false, normalTaskName, "exceptionStep");

        // start the process and wait for process to be triggered
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, multiTaskName);

        // verify that the exception flow was taken
        final long exceptionFlowStepId = waitForUserTask(processInstance, "exceptionStep");

        // execute multi-instance and verify that normal flow continues
        executeRemainingParallelMultiInstances(multiTaskName, processInstance, loopCardinality);
        final long normalTaskId = waitForUserTask(processInstance, normalTaskName);

        // execute exception flow and normal flow and verify the process completes
        assignAndExecuteStep(exceptionFlowStepId, user);
        assignAndExecuteStep(normalTaskId, user);
        waitForProcessToFinish(processInstance);

        // clean up
        disableAndDeleteProcess(processDefinition.getId());
    }

    @Cover(classes = { EventInstance.class, LoopActivityInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Boundary", "Non-interrupging",
            "Timer", "Loop activity", "Exception flow" }, story = "Non-interrupting timer boundary event triggered on loop activity", jira = "ENGINE-1042")
    @Test
    public void nonInterruptTimerBoundaryEventTriggeredOnLoopActivity() throws Exception {
        final long timerDuration = 1000;
        final int loopMax = 2;
        final String loopActivityName = "step1";
        final String normalFlowStepName = "step2";
        final String exceptionFlowStepName = "exceptionStep";
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryTimerEventOnLoopActivity(timerDuration, false, loopMax, loopActivityName,
                normalFlowStepName, exceptionFlowStepName);

        // start the process and wait timer to trigger
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long loopId = waitForUserTask(processInstance, loopActivityName);
        Thread.sleep(timerDuration); // wait timer trigger

        // verify that the exception flow was taken
        final long exceptionStepId = waitForUserTask(processInstance, exceptionFlowStepName);

        // execute all loop activities and verify that the nomal flow continues
        assignAndExecuteStep(loopId, user);
        waitForUserTasksAndExecuteIt(loopActivityName, processInstance, loopMax - 1);
        final long normalFlowStepId = waitForUserTask(processInstance, normalFlowStepName);

        // execute the exception flow and the normal flow and verify that the process completes
        assignAndExecuteStep(exceptionStepId, user);
        assignAndExecuteStep(normalFlowStepId, user);
        waitForProcessToFinish(processInstance);

        // clean up
        disableAndDeleteProcess(processDefinition.getId());
    }

}
