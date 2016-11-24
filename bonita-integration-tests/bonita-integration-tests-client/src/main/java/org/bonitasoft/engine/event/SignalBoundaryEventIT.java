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

import static org.junit.Assert.assertFalse;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.wait.WaitForStep;
import org.junit.Test;

public class SignalBoundaryEventIT extends AbstractEventIT {

    @Test
    public void signalBoundaryEventTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEvent("MySignal");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");
        getProcessAPI().sendSignal("MySignal");

        waitForUserTaskAndExecuteIt(processInstance, EXCEPTION_STEP, user);

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(step1Id, TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void signalBoundaryEventNotTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEvent("MySignal1");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        final long step2 = waitForUserTask(processInstance, "step2");

        getProcessAPI().sendSignal("MySignal1");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, EXCEPTION_STEP, processInstance.getId(), TestStates.READY,
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(step2, user);

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, EXCEPTION_STEP);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void signalBoundaryEventOnCallActivityTriggered() throws Exception {
        final String signalName = "MySignal";
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnCallActivity(signalName);
        final ProcessDefinition calledProcessDefinition = deployAndEnableSimpleProcess("calledProcess", "calledStep");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance calledStep = waitForUserTaskAndGetIt(processInstance, "calledStep");
        final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep.getParentProcessInstanceId());

        getProcessAPI().sendSignal("MySignal");
        waitForUserTaskAndExecuteIt(processInstance, EXCEPTION_STEP, user);

        waitForProcessToBeInState(calledProcessInstance, ProcessInstanceState.ABORTED);
        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(calledStep.getId(), TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(calledProcessDefinition);
    }

    @Test
    public void signalBoundaryEventOnCallActivityNotTriggered() throws Exception {
        final String signalName = "MySignal";
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnCallActivity(signalName);
        final ProcessDefinition calledProcessDefinition = deployAndEnableSimpleProcess("calledProcess", "calledStep");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance calledStep = waitForUserTaskAndExecuteAndGetIt(processInstance, "calledStep", user);
        final ProcessInstance calledProcessInstance = getProcessAPI().getProcessInstance(calledStep.getParentProcessInstanceId());

        final long step2Id = waitForUserTask(processInstance, "step2");
        waitForProcessToFinish(calledProcessInstance);

        getProcessAPI().sendSignal("MySignal");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, EXCEPTION_STEP, processInstance.getId(), TestStates.READY,
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(step2Id, user);
        waitForProcessToFinish(processInstance);

        checkWasntExecuted(processInstance, EXCEPTION_STEP);

        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(calledProcessDefinition);
    }

    @Test
    public void signalBoundaryEventTriggeredOnSequentialMultiInstance() throws Exception {
        final int loopCardinality = 4;
        final boolean isSequential = true;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final long step1Id = waitForUserTask(processInstance, "step1");
        getProcessAPI().sendSignal("MySignal");
        waitForUserTaskAndExecuteIt(processInstance, EXCEPTION_STEP, user);

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(step1Id, TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void signalBoundaryEventNotTriggeredOnSequentialMultiInstance() throws Exception {
        final int loopCardinality = 3;
        final boolean isSequential = true;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopCardinality; i++) {
            waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        }
        final long step2Id = waitForUserTask(processInstance, "step2");

        getProcessAPI().sendSignal("MySignal1");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, EXCEPTION_STEP, processInstance.getId(), TestStates.READY,
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(step2Id, user);

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, EXCEPTION_STEP);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void signalBoundaryEventTriggeredOnParallelMultiInstance() throws Exception {
        final int loopCardinality = 4;
        final boolean isSequential = false;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final long step1Id = waitForUserTask(processInstance, "step1");
        getProcessAPI().sendSignal("MySignal");
        waitForUserTaskAndExecuteIt(processInstance, EXCEPTION_STEP, user);

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(step1Id, TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void signalBoundaryEventNotTriggeredOnParallelMultiInstance() throws Exception {
        final int loopCardinality = 3;
        final boolean isSequential = false;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnMultiInstance(loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopCardinality; i++) {
            waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        }
        final long step2 = waitForUserTask(processInstance, "step2");

        getProcessAPI().sendSignal("MySignal1");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, EXCEPTION_STEP, processInstance.getId(), TestStates.READY,
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(step2, user);

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, EXCEPTION_STEP);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void signalBoundaryEventTriggeredOnLoopActivity() throws Exception {
        final int loopMax = 3;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnLoopActivity(loopMax);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final long step1Id = waitForUserTask(processInstance, "step1");
        getProcessAPI().sendSignal("MySignal");
        waitForUserTaskAndExecuteIt(processInstance, EXCEPTION_STEP, user);

        waitForProcessToFinish(processInstance);
        waitForArchivedActivity(step1Id, TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void signalBoundaryEventNotTriggeredOnLoopActivity() throws Exception {
        final int loopMax = 2;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundarySignalEventOnLoopActivity(loopMax);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopMax; i++) {
            waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        }
        final long step2Id = waitForUserTask(processInstance, "step2");

        getProcessAPI().sendSignal("MySignal1");

        final WaitForStep waitForExceptionStep = new WaitForStep(50, 1000, EXCEPTION_STEP, processInstance.getId(), TestStates.READY,
                getProcessAPI());
        assertFalse(waitForExceptionStep.waitUntil());

        assignAndExecuteStep(step2Id, user);

        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, EXCEPTION_STEP);

        disableAndDeleteProcess(processDefinition);
    }

}
