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
package org.bonitasoft.engine.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.bonitasoft.engine.util.AssertionsUtils.assertNoErrorAfterXAttemps;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.test.TestStates;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageBoundaryEventIT extends AbstractEventIT {

    private static final Logger logger = LoggerFactory.getLogger(MessageBoundaryEventIT.class);

    @Test
    public void messageBoundaryEventTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEvent("MyMessage");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance.getId(), "step1");

        // Check that boundary event has been created:
        long otherBoundaries = getProcessAPI()
                .searchFlowNodeInstances(new SearchOptionsBuilder(0, 10)
                        .filter(FlowNodeInstanceSearchDescriptor.NAME, "otherBoundaryNotTriggered").done())
                .getCount();
        assertThat(otherBoundaries).isEqualTo(1);

        getProcessAPI().sendMessage("MyMessage",
                new ExpressionBuilder().createConstantStringExpression("pMessageBoundary"),
                new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);
        waitForUserTaskAndExecuteIt(processInstance, EXCEPTION_STEP, user);

        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(step1Id, TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        // Boundary events are not archived, so there is no way to check if the second boundary has been properly aborted (otherBoundaryNotTriggered).
        // For that matter, only check that boundary is deleted:
        otherBoundaries = getProcessAPI()
                .searchFlowNodeInstances(new SearchOptionsBuilder(0, 10)
                        .filter(FlowNodeInstanceSearchDescriptor.NAME, "otherBoundaryNotTriggered").done())
                .getCount();
        assertThat(otherBoundaries).isEqualTo(0);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void messageBoundaryEventNotTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEvent("MyMessage1");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        final long step2Id = waitForUserTask(processInstance.getId(), "step2");

        getProcessAPI().sendMessage("MyMessage1",
                new ExpressionBuilder().createConstantStringExpression("pMessageBoundary"),
                new ExpressionBuilder().createConstantStringExpression("step1"), null);

        assignAndExecuteStep(step2Id, user);
        checkWasntExecuted(processInstance, "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void messageBoundaryEventOnCallActivityTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnCallActivity();
        final ProcessDefinition calledProcessDefinition = deployAndEnableSimpleProcess("calledProcess", "calledTask");

        try {
            final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
            final ActivityInstance calledStep = waitForUserTaskAndGetIt(processInstance.getId(), "calledTask");
            final ProcessInstance calledProcessInstance = getProcessAPI()
                    .getProcessInstance(calledStep.getParentProcessInstanceId());

            getProcessAPI().sendMessage("MyMessage",
                    new ExpressionBuilder().createConstantStringExpression("pMessageBoundary"),
                    new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);

            waitForUserTaskAndExecuteIt(processInstance, "exceptionStep", user);
            waitForProcessToBeInState(calledProcessInstance, ProcessInstanceState.ABORTED);
            waitForProcessToFinish(processInstance);

            waitForArchivedActivity(calledStep.getId(), TestStates.ABORTED);

            checkWasntExecuted(processInstance, "step2");
        } finally {
            disableAndDeleteProcess(processDefinition, calledProcessDefinition);
        }
    }

    @Test
    public void messageBoundaryEventOnCallActivityNotTriggered() throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnCallActivity();
        final ProcessDefinition calledProcessDefinition = deployAndEnableSimpleProcess("calledProcess", "calledTask");

        try {
            final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
            final ActivityInstance calledStep = waitForUserTaskAndGetIt(processInstance.getId(), "calledTask");
            final ProcessInstance calledProcessInstance = getProcessAPI()
                    .getProcessInstance(calledStep.getParentProcessInstanceId());
            assignAndExecuteStep(calledStep, user.getId());

            final long step2Id = waitForUserTask(processInstance.getId(), "step2");

            getProcessAPI().sendMessage("MyMessage",
                    new ExpressionBuilder().createConstantStringExpression("pMessageBoundary"),
                    new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);

            waitForProcessToFinish(calledProcessInstance);
            assignAndExecuteStep(step2Id, user);
            waitForProcessToFinish(processInstance);

            checkWasntExecuted(processInstance, "exceptionStep");
        } finally {
            disableAndDeleteProcess(processDefinition, calledProcessDefinition);
        }
    }

    @Test
    public void messageBoundaryEventTriggeredOnSequentialMultiInstance() throws Exception {
        final int loopCardinality = 4;
        final boolean isSequential = true;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnMultiInstance(
                loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance.getId(), "step1");

        getProcessAPI().sendMessage("MyMessage",
                new ExpressionBuilder()
                        .createConstantStringExpression("processWithBoundaryMessageEventAndMultiInstance"),
                new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);
        waitForUserTaskAndExecuteIt(processInstance, EXCEPTION_STEP, user);

        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(step1Id, TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void messageBoundaryEventNotTriggeredOnSequentialMultiInstance() throws Exception {
        final int loopCardinality = 3;
        final boolean isSequential = true;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnMultiInstance(
                loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopCardinality; i++) {
            waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        }
        final long step2Id = waitForUserTask(processInstance.getId(), "step2");

        getProcessAPI().sendMessage("MyMessage1",
                new ExpressionBuilder().createConstantStringExpression("pMessageBoundary"),
                new ExpressionBuilder().createConstantStringExpression("step1"), null);

        assignAndExecuteStep(step2Id, user);
        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void messageBoundaryEventTriggeredOnParallelMultiInstance() throws Exception {
        final int loopCardinality = 4;
        final boolean isSequential = false;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnMultiInstance(
                loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final List<HumanTaskInstance> pendingTasks = waitForPendingTasks(user, loopCardinality);
        for (final HumanTaskInstance humanTaskInstance : pendingTasks) {
            assertEquals("step1", humanTaskInstance.getName());
        }

        getProcessAPI().sendMessage("MyMessage",
                new ExpressionBuilder()
                        .createConstantStringExpression("processWithBoundaryMessageEventAndMultiInstance"),
                new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);
        waitForUserTaskAndExecuteIt(processInstance, EXCEPTION_STEP, user);

        waitForProcessToFinish(processInstance);

        for (final HumanTaskInstance humanTaskInstance : pendingTasks) {
            waitForArchivedActivity(humanTaskInstance.getId(), TestStates.ABORTED);
        }

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void messageBoundaryEventNotTriggeredOnParallelMultiInstance() throws Exception {
        final int loopCardinality = 3;
        final boolean isSequential = false;

        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnMultiInstance(
                loopCardinality, isSequential);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopCardinality; i++) {
            waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        }
        final long step2Id = waitForUserTask(processInstance.getId(), "step2");

        getProcessAPI().sendMessage("MyMessage1",
                new ExpressionBuilder().createConstantStringExpression("processWithMultiInstanceAndBoundaryEvent"),
                new ExpressionBuilder().createConstantStringExpression("step1"), null);

        assignAndExecuteStep(step2Id, user);
        checkWasntExecuted(processInstance, "exceptionStep");

        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void messageBoundaryEventTriggeredOnLoopActivity() throws Exception {
        final int loopMax = 3;

        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnLoopActivity(
                loopMax);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance.getId(), "step1");

        getProcessAPI().sendMessage("MyMessage",
                new ExpressionBuilder().createConstantStringExpression("processWithLoopActivityAndBoundaryEvent"),
                new ExpressionBuilder().createConstantStringExpression(BOUNDARY_NAME), null);
        waitForUserTaskAndExecuteIt(processInstance, EXCEPTION_STEP, user);

        waitForProcessToFinish(processInstance);

        waitForArchivedActivity(step1Id, TestStates.ABORTED);

        checkWasntExecuted(processInstance, "step2");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void messageBoundaryEventNotTriggeredOnLoopActivity() throws Exception {
        final int loopMax = 2;
        final ProcessDefinition processDefinition = deployAndEnableProcessWithBoundaryMessageEventOnLoopActivity(
                loopMax);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        for (int i = 0; i < loopMax; i++) {
            waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        }
        final long step2Id = waitForUserTask(processInstance.getId(), "step2");

        getProcessAPI().sendMessage("MyMessage1",
                new ExpressionBuilder().createConstantStringExpression("processWithLoopActivityAndBoundaryEvent"),
                new ExpressionBuilder().createConstantStringExpression("step1"), null);

        assignAndExecuteStep(step2Id, user);
        waitForProcessToFinish(processInstance);
        checkWasntExecuted(processInstance, "exceptionStep");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void process_with_call_activity_aborted_by_boundary_event_should_complete_along_with_its_target_process()
            throws Exception {
        // given:
        final DesignProcessDefinition subProcess = new ProcessDefinitionBuilder()
                .createNewInstance("SubProcessWith2AutomaticTasks", "2.7")
                .addAutomaticTask("sub1").addAutomaticTask("sub2")
                .addTransition("sub1", "sub2")
                .getProcess();
        final DesignProcessDefinition parentProcess = new ProcessDefinitionBuilder()
                .createNewInstance("ProcessWithCallActivityAborted", "7.3")
                .addCallActivity("call",
                        new ExpressionBuilder().createConstantStringExpression(subProcess.getName()),
                        new ExpressionBuilder().createConstantStringExpression(subProcess.getVersion()))
                .addBoundaryEvent("boundary", true).addMessageEventTrigger("abortCallActivity")
                .addEndEvent("end")
                .addTransition("boundary", "end")
                .getProcess();
        getProcessAPI().deployAndEnableProcess(subProcess);
        ProcessDefinition parentProcessDefinition = getProcessAPI().deployAndEnableProcess(parentProcess);
        assertNoErrorAfterXAttemps(5, () -> {
            getProcessAPI().startProcess(parentProcessDefinition.getId());
            getProcessAPI().sendMessage("abortCallActivity",
                    new ExpressionBuilder().createConstantStringExpression("ProcessWithCallActivityAborted"),
                    new ExpressionBuilder().createConstantStringExpression("boundary"), Collections.emptyMap());
            await().until(
                    () -> getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.LAST_UPDATE_ASC),
                    hasSize(0));
        }, e -> logAllProcesses());
    }

    @Test
    public void process_with_call_activity_aborted_by_terminate_end_event_should_complete_along_with_its_target_process()
            throws Exception {
        // given:
        final DesignProcessDefinition subProcess = new ProcessDefinitionBuilder()
                .createNewInstance("SubProcessWith2AutomaticTasks", "2.7")
                .addAutomaticTask("sub1").addAutomaticTask("sub2")
                .addTransition("sub1", "sub2")
                .getProcess();
        final DesignProcessDefinition parentProcess = new ProcessDefinitionBuilder()
                .createNewInstance("ProcessWithCallActivityAborted", "7.3")
                .addStartEvent("start").addCallActivity("call",
                        new ExpressionBuilder().createConstantStringExpression(subProcess.getName()),
                        new ExpressionBuilder().createConstantStringExpression(subProcess.getVersion()))
                // will loop forever until the terminate and event aborts it:
                .addLoop(false, new ExpressionBuilder().createConstantBooleanExpression(true)).addAutomaticTask("auto1")
                .addAutomaticTask("auto2").addEndEvent("end").addTerminateEventTrigger()
                .addTransition("start", "call")
                .addTransition("start", "auto1")
                .addTransition("auto1", "auto2")
                .addTransition("auto2", "end").getProcess();
        getProcessAPI().deployAndEnableProcess(subProcess);
        ProcessDefinition parentProcessDefinition = getProcessAPI().deployAndEnableProcess(parentProcess);

        assertNoErrorAfterXAttemps(5, () -> {
            getProcessAPI().startProcess(parentProcessDefinition.getId());
            await().until(
                    () -> getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.LAST_UPDATE_ASC),
                    hasSize(0));
        }, e -> logAllProcesses());
    }

    protected void logAllProcesses() throws SearchException {
        logger.error("Found processes instances");
        for (ProcessInstance p : getProcessAPI().getProcessInstances(0, 10,
                ProcessInstanceCriterion.LAST_UPDATE_ASC)) {
            logger.error("  * " + p);
        }
        logger.error("Found flow nodes");
        for (FlowNodeInstance p : getProcessAPI()
                .searchFlowNodeInstances(new SearchOptionsBuilder(0, 100).done()).getResult()) {
            logger.error("  * " + p);
        }
    }

}
