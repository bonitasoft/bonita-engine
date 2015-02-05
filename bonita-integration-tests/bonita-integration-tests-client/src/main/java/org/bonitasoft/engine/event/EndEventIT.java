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

import org.bonitasoft.engine.bpm.flownode.EndEventInstance;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.impl.internal.MultiInstanceLoopCharacteristics;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class EndEventIT extends AbstractEventIT {

    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Start event", "End event" }, story = "Execute process with start and end events.", jira = "")
    @Test
    public void executeStartAndEndEvents() throws Exception {
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0")
                .addStartEvent("startEvent").addAutomaticTask("step1").addEndEvent("endEvent").addTransition("startEvent", "step1")
                .addTransition("step1", "endEvent").getProcess();

        final ProcessDefinition definition = deployAndEnableProcess(designProcessDefinition);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(definition);
    }

    @Cover(classes = EventInstance.class, exceptions = InvalidProcessDefinitionException.class, concept = BPMNConcept.EVENTS, keywords = { "Event",
            "Start event", "Incoming transition" }, story = "Check that a start event can't have an incoming transition.", jira = "")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void startEventCannotHaveIncomingTransition() throws BonitaException {
        new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addStartEvent("startEvent").addAutomaticTask("step1")
                .addTransition("step1", "startEvent").getProcess();
    }

    @Cover(classes = EventInstance.class, exceptions = InvalidProcessDefinitionException.class, concept = BPMNConcept.EVENTS, keywords = { "Event",
            "End event", "Outgoing transition" }, story = "Check that an end event can't have an outgoing transition.", jira = "")
    @Test(expected = InvalidProcessDefinitionException.class)
    public void endEventCannotHaveOutgoingTransition() throws BonitaException {
        new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0").addAutomaticTask("step1").addEndEvent("endEvent")
                .addTransition("endEvent", "step1").getProcess();
    }

    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Terminate event", "End event" }, story = "Execute process with only a terminate end event.", jira = "")
    @Test
    public void terminateEndEventAlone() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("Proc", "1.0");
        builder.addEndEvent("stop").addTerminateEventTrigger();

        final ProcessDefinition process = deployAndEnableProcess(builder.done());

        final ProcessInstance startProcess = getProcessAPI().startProcess(process.getId());
        waitForProcessToFinish(startProcess);
        disableAndDeleteProcess(process);
    }

    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Terminate event", "Start event", "End event", "Automatic task" }, story = "Execute a process with start event, terminate end event and automatic task.", jira = "")
    @Test
    public void executeStartAndEndEventWithTask() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeStartAndEndEventWithTask", "1.0");
        builder.addStartEvent("start").addAutomaticTask("step1").addEndEvent("stop").addTerminateEventTrigger().addTransition("start", "step1")
                .addTransition("step1", "stop");
        final ProcessDefinition process = deployAndEnableProcess(builder.done());
        final ProcessInstance startProcess = getProcessAPI().startProcess(process.getId());
        waitForProcessToFinish(startProcess);
        disableAndDeleteProcess(process);
    }

    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Terminate event", "End event", "User task" }, story = "Execute a process with a terminate end event and user task.", jira = "")
    @Test
    public void terminateEndEventWithTasks() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("Proc", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME);
        builder.addEndEvent("stop").addTerminateEventTrigger();
        builder.addTransition("step1", "stop");
        final ProcessDefinition process = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(process.getId());
        waitForUserTaskAndExecuteIt(startProcess, "step1", user);
        waitForProcessToFinish(startProcess);
        disableAndDeleteProcess(process);
    }

    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Terminate event", "End event", "Branch not finished" }, story = "Execute a process with terminate end event and a branch not finished.", jira = "")
    @Test
    public void terminateEndEventWithNotFinishedBranch() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("Proc", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME);
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addEndEvent("stop").addTerminateEventTrigger();
        builder.addTransition("step1", "stop");
        builder.addTransition("step2", "stop");
        final ProcessDefinition process = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        waitForUserTask(processInstance.getId(), "step2");
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        // should finish even if we don't execute step2
        waitForFlowNodeInState(processInstance, "step2", TestStates.ABORTED, true);
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(process);
    }

    @Cover(classes = EndEventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "terminate", "branch" }, jira = "ENGINE-236", story = "terminate end event abort all active activity and does not trigger new one")
    @Test
    public void terminateEndEvendWithNotFinishedBranch2() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("Proc", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME);
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addUserTask("step3", ACTOR_NAME);
        builder.addEndEvent("stop").addTerminateEventTrigger();
        builder.addTransition("step1", "stop");
        builder.addTransition("step2", "step3");
        builder.addTransition("step3", "stop");
        final ProcessDefinition process = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());
        waitForUserTask(processInstance.getId(), "step2");
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        // should finish even if we don't execute step2
        waitForFlowNodeInState(processInstance, "step2", TestStates.ABORTED, true);
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(process);
    }

    // @Ignore("Currently ignored because it cause timeout lock on data base: need to refactor transactions and so on")
    @Cover(classes = EventInstance.class, concept = BPMNConcept.EVENTS, keywords = { "Event", "Terminate event", "End event", "Multiple branches not finished" }, story = "Execute a process with terminate end event and multiple branches not finished.", jira = "")
    @Test
    public void terminateEndEvendWithNotFinishedMultipleBranch() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("Proc", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME);
        builder.addEndEvent("stop").addTerminateEventTrigger();
        builder.addTransition("step1", "stop");
        for (int i = 2; i < 6; i++) {
            builder.addUserTask("step" + i, ACTOR_NAME);
            builder.addTransition("step" + i, "stop");
        }
        final ProcessDefinition process = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance startProcess = getProcessAPI().startProcess(process.getId());
        waitForUserTask(startProcess, "step2");
        waitForUserTask(startProcess, "step3");
        waitForUserTask(startProcess, "step4");
        waitForUserTask(startProcess, "step5");
        waitForUserTaskAndExecuteIt(startProcess, "step1", user);
        // should finish even if we don't execute step2
        waitForProcessToFinish(startProcess);
        disableAndDeleteProcess(process);
    }

    @Cover(classes = { EventInstance.class, MultiInstanceLoopCharacteristics.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Multi-instance",
            "End event", "Terminate event", "Parallel" }, story = "Execute a process with a terminate end event and parallel multi-instance.", jira = "")
    @Test
    public void terminateEventWithMultiInstanceParallel() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("terminateEventWithMultiInstance", "1.0");
        builder.addAutomaticTask("step1").addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(3));
        builder.addEndEvent("stop").addTerminateEventTrigger().addTransition("step1", "stop");

        final ProcessDefinition process = deployAndEnableProcess(builder.done());
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());

        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(process);

    }

    @Cover(classes = { EventInstance.class, MultiInstanceLoopCharacteristics.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Multi-instance",
            "End event", "Terminate event", "Sequential", "User task" }, story = "Execute a process with a terminate end event and sequential multi-instance.", jira = "")
    @Test
    public void terminateEventWithMultiInstanceSequential() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("terminateEventWithMultiInstance", "1.0");
        builder.addActor(ACTOR_NAME).addUserTask("step1", ACTOR_NAME).addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(3));
        builder.addEndEvent("stop").addTerminateEventTrigger().addTransition("step1", "stop");

        final ProcessDefinition process = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(process.getId());

        for (int i = 0; i < 3; i++) {
            waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        }

        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(process);
    }
}
