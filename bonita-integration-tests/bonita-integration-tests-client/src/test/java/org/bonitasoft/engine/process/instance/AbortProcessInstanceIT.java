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
package org.bonitasoft.engine.process.instance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.bpm.NamedElement;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.test.TestStates;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbortProcessInstanceIT extends AbstractProcessInstanceIT {

    private static final Logger logger = LoggerFactory.getLogger(AbortProcessInstanceIT.class);

    private ProcessDefinition deployProcessWithMultiInstanceCallActivity(final int loopCardinality,
            final String targetProcess, final String targetVersion)
            throws BonitaException {
        final Expression targetProcExpr = string(targetProcess);
        final Expression targetVersionExpr = string(targetVersion);

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder()
                .createNewInstance("RemainingInstancesAreAbortedAfterCompletionCondition", "1.0");
        builder.addStartEvent("start");
        builder.addCallActivity("callActivity", targetProcExpr, targetVersionExpr)
                .addMultiInstance(false, intExpr(loopCardinality))
                .addCompletionCondition(
                        new ExpressionBuilder()
                                .createGroovyScriptExpression("deployProcessWithMultiInstanceCallActivity",
                                        ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES.getEngineConstantName()
                                                + " == 1 ",
                                        Boolean.class.getName(),
                                        new ExpressionBuilder().createEngineConstant(
                                                ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES)));
        builder.addEndEvent("end");
        builder.addTransition("start", "callActivity");
        builder.addTransition("callActivity", "end");

        return deployAndEnableProcess(builder.done());
    }

    @Test
    public void abortProcessWithHumanTasks() throws Exception {
        final String taskName1 = "userTask1";
        final String taskName2 = "userTask2";
        final String autoTaskName = "auto1";
        final ProcessDefinition targetProcess = deployProcessWith2UserTasksAnd1AutoTask(taskName1, taskName2,
                autoTaskName);
        final int loopCardinality = 2;
        final ProcessDefinition parentProcess = deployProcessWithMultiInstanceCallActivity(loopCardinality,
                targetProcess.getName(), targetProcess.getVersion());
        final ProcessInstance parentProcessInstance = getProcessAPI().startProcess(parentProcess.getId());
        checkNbOfProcessInstances(loopCardinality + 1);

        // execute task1 of a target process instance
        final List<HumanTaskInstance> pendingHumanTaskInstances = checkNbPendingTaskOf(true, 2 * loopCardinality, user)
                .getPendingHumanTaskInstances();
        final HumanTaskInstance humanTaskInst1ToExecute = pendingHumanTaskInstances.get(0);
        assertEquals(taskName1, humanTaskInst1ToExecute.getName());
        assignAndExecuteStep(humanTaskInst1ToExecute, user);

        final HumanTaskInstance humanTaskInst1ToAbort = pendingHumanTaskInstances.get(1);
        assertEquals(taskName1, humanTaskInst1ToAbort.getName());
        final long toBeAbortedProcInstId = humanTaskInst1ToAbort.getParentProcessInstanceId();
        final ProcessInstance procInstToAbort = getProcessAPI().getProcessInstance(toBeAbortedProcInstId);

        // execute task2 of same target process instance
        HumanTaskInstance humanTaskInst2ToExecute = pendingHumanTaskInstances.get(2);
        assertEquals(taskName2, humanTaskInst2ToExecute.getName());
        HumanTaskInstance humanTaskInst2ToAbort = pendingHumanTaskInstances.get(3);
        assertEquals(taskName2, humanTaskInst2ToAbort.getName());
        if (humanTaskInst1ToExecute.getParentProcessInstanceId() != humanTaskInst2ToExecute
                .getParentProcessInstanceId()) { // ensure tasks are in the same
            humanTaskInst2ToAbort = humanTaskInst2ToExecute;
            humanTaskInst2ToExecute = pendingHumanTaskInstances.get(3);
        }
        assignAndExecuteStep(humanTaskInst2ToExecute, user.getId());

        // the target process instances that exceed the max loop must be in aborted state
        waitForProcessToBeInState(procInstToAbort, ProcessInstanceState.ABORTED);

        // task1 not executed must be in aborted state
        waitForFlowNodeInState(parentProcessInstance, humanTaskInst1ToAbort.getName(), TestStates.ABORTED, true);
        // task2 not executed must be in aborted state
        waitForFlowNodeInState(parentProcessInstance, humanTaskInst2ToAbort.getName(), TestStates.ABORTED, true);

        // check the automatic task in the aborted process instance was not created
        checkWasntExecuted(procInstToAbort, autoTaskName);

        // the parent process instance must finish in normal state
        waitForProcessToFinish(parentProcessInstance);

        disableAndDeleteProcess(parentProcess);
        disableAndDeleteProcess(targetProcess);
    }

    @Test
    public void should_abort_stable_and_non_stable_flow_nodes() throws Exception {
        executeAndVerifyCompleted(() -> new ProcessDefinitionBuilder().createNewInstance("processWithTerminate", "3.0")
                .addAutomaticTask("step1")
                .addAutomaticTask("step2")
                .addAutomaticTask("step3")
                .addAutomaticTask("step4")
                .addEndEvent("terminateEnd").addTerminateEventTrigger()
                .addAutomaticTask("step5")
                .addAutomaticTask("step6")
                .addAutomaticTask("step7")
                .addAutomaticTask("step8")
                .addAutomaticTask("step9")
                .addAutomaticTask("step10")
                .getProcess());

        shouldNotHaveFailedTasks();
        shouldAllCompleteWithAbortedOrCompleted();
        assertThat(getAllCompletedArchivedFlowNodeInstances().stream().map(NamedElement::getName))
                .containsOnly("step1", "step2", "step3", "step4", "step5", "step6", "step7", "step8", "step9",
                        "step10");
    }

    @Test
    public void should_abort_executing_loops() throws Exception {
        executeAndVerifyCompleted(() -> new ProcessDefinitionBuilder().createNewInstance("processWithTerminate", "1.0")
                .addAutomaticTask("step1").addLoop(false, bool(true))
                .addAutomaticTask("step2").addLoop(false, bool(true))
                .addAutomaticTask("step3").addLoop(false, bool(true))
                .addAutomaticTask("step4").addLoop(false, bool(true))
                .addAutomaticTask("step5").addLoop(false, bool(true))
                .addAutomaticTask("step6").addLoop(false, bool(true))
                .addAutomaticTask("step7").addLoop(false, bool(true))
                .addAutomaticTask("step8").addLoop(false, bool(true))
                .addAutomaticTask("step9").addLoop(false, bool(true))
                .addAutomaticTask("step10").addLoop(false, bool(true))
                .addAutomaticTask("step_before_abort").addEndEvent("terminateEnd").addTerminateEventTrigger()
                .addTransition("step_before_abort", "terminateEnd")
                .getProcess());

        shouldNotHaveFailedTasks();
        shouldAllCompleteWithAbortedOrCompleted();
        assertThat(getAllCompletedArchivedFlowNodeInstances().stream().map(NamedElement::getName))
                .contains("step1", "step2", "step3", "step4", "step5", "step6", "step7", "step8", "step9", "step10",
                        "step_before_abort");
    }

    private void shouldAllCompleteWithAbortedOrCompleted() throws SearchException {
        assertThat(getAllCompletedArchivedFlowNodeInstances())
                .allSatisfy(a -> assertThat(a.getState()).isIn("aborted", "completed"));
    }

    @Test
    public void should_abort_executing_multiInstances() throws Exception {
        executeAndVerifyCompleted(() -> {
            ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithTerminate",
                    "1.0");
            builder.addAutomaticTask("step1").addMultiInstance(false, intExpr(5));
            builder.addAutomaticTask("step2").addMultiInstance(false, intExpr(5));
            builder.addAutomaticTask("step3").addMultiInstance(false, intExpr(5));
            builder.addAutomaticTask("step4").addMultiInstance(false, intExpr(5));
            builder.addAutomaticTask("step5").addMultiInstance(false, intExpr(5));
            builder.addAutomaticTask("step6").addMultiInstance(false, intExpr(5));
            builder.addAutomaticTask("step7").addMultiInstance(false, intExpr(5));
            builder.addAutomaticTask("step8").addMultiInstance(false, intExpr(5));
            builder.addAutomaticTask("step9").addMultiInstance(false, intExpr(5));
            builder.addAutomaticTask("step10").addMultiInstance(false, intExpr(5));
            return builder
                    .addAutomaticTask("step_before_abort").addEndEvent("terminateEnd").addTerminateEventTrigger()
                    .addTransition("step_before_abort", "terminateEnd")
                    .getProcess();
        });

        shouldNotHaveFailedTasks();
        shouldAllCompleteWithAbortedOrCompleted();
        assertThat(getAllCompletedArchivedFlowNodeInstances().stream().map(NamedElement::getName))
                .contains("step1", "step2", "step3", "step4", "step5", "step6", "step7", "step8", "step9", "step10",
                        "step_before_abort");
    }

    @Test
    @Ignore("not working need the retry mechanism, the fix BR454 is needed  ")
    public void should_abort_call_activities_from_calledProcess_with_event_subprocess() throws Exception {
        ProcessDefinition calledProcess = getProcessAPI()
                .deployAndEnableProcess(new ProcessDefinitionBuilder().createNewInstance("calledProcess", "4.0")
                        .addAutomaticTask("sub1")
                        .addAutomaticTask("sub2")
                        .addAutomaticTask("sub3")
                        .addAutomaticTask("sub4").getProcess());
        ProcessDefinition endErrorProcess = getProcessAPI()
                .deployAndEnableProcess(new ProcessDefinitionBuilder().createNewInstance("endErrorProcess", "4.0")
                        .addAutomaticTask("sub1")
                        .addEndEvent("terminate").addErrorEventTrigger("theError")
                        .addTransition("sub1", "terminate")
                        .getProcess());
        ProcessDefinition processDefinition = executeAndVerifyAborted(() -> {

            ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithTerminate",
                    "4.0");
            builder.addCallActivity("step1", string("calledProcess"), string("4.0"));
            builder.addCallActivity("step2", string("calledProcess"), string("4.0")).addMultiInstance(false,
                    intExpr(5));
            builder.addCallActivity("step3", string("calledProcess"), string("4.0"));//.addLoop(false, bool(true));
            builder.addCallActivity("step4", string("calledProcess"), string("4.0"));
            builder.addCallActivity("step5", string("calledProcess"), string("4.0"));
            builder.addCallActivity("step6", string("calledProcess"), string("4.0"));
            builder.addCallActivity("step7", string("calledProcess"), string("4.0"));
            builder.addCallActivity("step10", string("endErrorProcess"), string("4.0"));
            builder.addCallActivity("step8", string("calledProcess"), string("4.0"));
            builder.addCallActivity("step9", string("calledProcess"), string("4.0"));
            builder.addSubProcess("errorEventSub", true).getSubProcessBuilder()
                    .addStartEvent("startError").addErrorEventTrigger("theError")
                    .addAutomaticTask("stepToHandleError")
                    .addTransition("startError", "stepToHandleError");
            return builder
                    .getProcess();
        });

        shouldNotHaveFailedTasks();
        assertThat(getAllCompletedArchivedFlowNodeInstances().stream().map(NamedElement::getName))
                .contains("stepToHandleError");
        shouldAllCompleteWithAbortedOrCompleted();
        disableAndDeleteProcess(calledProcess);
        disableAndDeleteProcess(endErrorProcess);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Ignore("not working need the retry mechanism, the fix BR454 is needed  ")
    public void should_abort_elements_from_calledProcess_with_event_subprocess() throws Exception {

        ProcessDefinition endErrorProcess = getProcessAPI()
                .deployAndEnableProcess(new ProcessDefinitionBuilder().createNewInstance("endErrorProcess", "2.0")
                        .addAutomaticTask("sub1")
                        .addEndEvent("terminate").addErrorEventTrigger("theError")
                        .addTransition("sub1", "terminate")
                        .getProcess());
        ProcessDefinition processDefinition = executeAndVerifyAborted(() -> {

            ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder()
                    .createNewInstance("processWithCalledProcess", "2.0");
            builder.addAutomaticTask("step1");
            builder.addAutomaticTask("step2").addMultiInstance(false, intExpr(5));
            builder.addAutomaticTask("step3").addLoop(false, bool(true));
            builder.addAutomaticTask("step4");
            builder.addAutomaticTask("step5");
            builder.addCallActivity("step10", string("endErrorProcess"), string("2.0"));
            builder.addAutomaticTask("step6");
            builder.addAutomaticTask("step7");
            builder.addAutomaticTask("step8");
            builder.addAutomaticTask("step9");
            builder.addSubProcess("errorEventSub", true).getSubProcessBuilder()
                    .addStartEvent("startError").addErrorEventTrigger("theError")
                    .addAutomaticTask("stepToHandleError")
                    .addTransition("startError", "stepToHandleError");
            return builder
                    .getProcess();
        });

        shouldNotHaveFailedTasks();
        assertThat(getAllCompletedArchivedFlowNodeInstances().stream().map(NamedElement::getName))
                .contains("stepToHandleError");
        shouldAllCompleteWithAbortedOrCompleted();

        disableAndDeleteProcess(endErrorProcess);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Ignore("not working need the retry mechanism, the fix BR454 is needed  ")
    public void should_abort_call_activities_from_parent_processes() throws Exception {
        ProcessDefinition calledProcess = getProcessAPI()
                .deployAndEnableProcess(new ProcessDefinitionBuilder().createNewInstance("calledProcess", "1.0")
                        .addAutomaticTask("sub1")
                        .addAutomaticTask("sub2")
                        .addAutomaticTask("sub3")
                        .addAutomaticTask("sub4").getProcess());
        executeAndVerifyCompleted(() -> {

            ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processWithTerminate",
                    "1.0");
            builder.addCallActivity("step1", string("calledProcess"), string("1.0"));
            builder.addCallActivity("step2", string("calledProcess"), string("1.0")).addMultiInstance(false,
                    intExpr(5));
            builder.addCallActivity("step3", string("calledProcess"), string("1.0")).addLoop(false, bool(true));
            builder.addCallActivity("step4", string("calledProcess"), string("1.0"));
            builder.addCallActivity("step5", string("calledProcess"), string("1.0"));
            builder.addCallActivity("step6", string("calledProcess"), string("1.0"));
            builder.addCallActivity("step7", string("calledProcess"), string("1.0"));
            builder.addCallActivity("step8", string("calledProcess"), string("1.0"));
            builder.addCallActivity("step9", string("calledProcess"), string("1.0"));
            builder.addCallActivity("step10", string("calledProcess"), string("1.0"));
            return builder
                    .addAutomaticTask("step_before_abort").addEndEvent("terminateEnd").addTerminateEventTrigger()
                    .addTransition("step_before_abort", "terminateEnd")
                    .getProcess();
        });

        shouldNotHaveFailedTasks();
        assertThat(getAllCompletedArchivedFlowNodeInstances().stream().map(NamedElement::getName))
                .contains("step1", "step2", "step3", "step4", "step5", "step6", "step7", "step8", "step9", "step10",
                        "step_before_abort");
        shouldAllCompleteWithAbortedOrCompleted();
        disableAndDeleteProcess(calledProcess);
    }

    @Test
    public void should_abort_or_cancel_all_flow_nodes_including_boundary_events_when_process_is_aborted_or_cancelled()
            throws Exception {
        // given:
        ProcessDefinition processDefinition = deployAndEnableProcessWithActor(new ProcessDefinitionBuilder()
                .createNewInstance("process to be aborted with boundary", "2.a")
                .addActor("actor", true)
                .addStartEvent("start")
                .addUserTask("toBeAborted", "actor").addBoundaryEvent("boundary").addSignalEventTrigger("theSignal")
                .addUserTask("terminateTask", "actor")
                .addEndEvent("end").addTerminateEventTrigger()
                .addTransition("start", "toBeAborted")
                .addTransition("start", "terminateTask")
                .addTransition("boundary", "end")
                .addTransition("terminateTask", "end").getProcess(), "actor", user);

        // when:
        getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("toBeAborted");
        waitForUserTaskAndExecuteIt("terminateTask", user);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "toBeAborted");
        getProcessAPI().cancelProcessInstance(processInstance.getId());

        // then:
        await().until(
                () -> getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.LAST_UPDATE_ASC),
                hasSize(0));
        assertThat(getProcessAPI().searchFlowNodeInstances(new SearchOptionsBuilder(0, 100).done()).getResult())
                .hasSize(0);
        await().until(
                () -> getProcessAPI()
                        .searchArchivedFlowNodeInstances(new SearchOptionsBuilder(0, 100)
                                .filter(ArchivedFlowNodeInstanceSearchDescriptor.NAME, "toBeAborted")
                                .filter(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME, "aborted")
                                .done())
                        .getResult(),
                hasSize(1));
        await().until(
                () -> getProcessAPI()
                        .searchArchivedFlowNodeInstances(new SearchOptionsBuilder(0, 100)
                                .filter(ArchivedFlowNodeInstanceSearchDescriptor.NAME, "toBeAborted")
                                .filter(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME, "cancelled")
                                .done())
                        .getResult(),
                hasSize(1));

        disableAndDeleteProcess(processDefinition);
    }

    private void executeAndVerifyCompleted(Callable<DesignProcessDefinition> callable) throws Exception {
        ProcessDefinition processDefinition = getProcessAPI().deployAndEnableProcess(callable.call());
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        try {
            waitForProcessToFinish(processInstance);
        } catch (Exception e) {
            logger.error("error while waiting for process to finish");
            ArchivedProcessInstance finalArchivedProcessInstance = getProcessAPI()
                    .getFinalArchivedProcessInstance(processInstance.getId());
            logger.error("final archive: state={}, endDate={}", finalArchivedProcessInstance.getState(),
                    finalArchivedProcessInstance.getEndDate());
            getProcessAPI().searchProcessInstances(new SearchOptionsBuilder(0, 100).done()).getResult()
                    .forEach(a -> logger.error("process found at the end: {}", a));
            getProcessAPI().searchFlowNodeInstances(new SearchOptionsBuilder(0, 100).done()).getResult()
                    .forEach(a -> logger.error("flow node found at the end: {}", a));
            throw e;
        }
    }

    private ProcessDefinition executeAndVerifyAborted(Callable<DesignProcessDefinition> callable) throws Exception {
        ProcessDefinition processDefinition = getProcessAPI().deployAndEnableProcess(callable.call());
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        try {
            waitForProcessToBeInState(processInstance, ProcessInstanceState.ABORTED);
        } catch (Exception e) {
            logger.error("error while waiting for process to be aborted");
            ArchivedProcessInstance finalArchivedProcessInstance = getProcessAPI()
                    .getFinalArchivedProcessInstance(processInstance.getId());
            logger.error("final archive: state={}, endDate={}", finalArchivedProcessInstance.getState(),
                    finalArchivedProcessInstance.getEndDate());
            getProcessAPI().searchProcessInstances(new SearchOptionsBuilder(0, 100).done()).getResult()
                    .forEach(a -> logger.error("process found at the end: {}", a));
            getProcessAPI().searchFlowNodeInstances(new SearchOptionsBuilder(0, 100).done()).getResult()
                    .forEach(a -> logger.error("flow node found at the end: {}", a));
            throw e;
        }
        return processDefinition;
    }

    private Expression bool(boolean b) throws InvalidExpressionException {
        return new ExpressionBuilder().createConstantBooleanExpression(b);
    }

    private Expression intExpr(int i) throws InvalidExpressionException {
        return new ExpressionBuilder().createConstantIntegerExpression(i);
    }

    private Expression string(String calledProcess) throws InvalidExpressionException {
        return new ExpressionBuilder().createConstantStringExpression(calledProcess);
    }

    private void shouldNotHaveFailedTasks() throws SearchException {
        assertThat(getProcessAPI().searchArchivedFlowNodeInstances(new SearchOptionsBuilder(0, 1000).done()).getResult()
                .stream().map(ArchivedFlowNodeInstance::getState))
                        .doesNotContain("failed");
    }

    private List<ArchivedFlowNodeInstance> getAllCompletedArchivedFlowNodeInstances() throws SearchException {
        return getProcessAPI()
                .searchArchivedFlowNodeInstances(new SearchOptionsBuilder(0, 100)
                        .filter(ArchivedFlowNodeInstanceSearchDescriptor.TERMINAL, true).done())
                .getResult();
    }

}
