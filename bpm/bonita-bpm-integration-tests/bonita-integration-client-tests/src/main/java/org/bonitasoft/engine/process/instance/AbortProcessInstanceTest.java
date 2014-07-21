package org.bonitasoft.engine.process.instance;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.test.TestStates;
import org.junit.Test;

public class AbortProcessInstanceTest extends AbstractProcessInstanceTest {

    private ProcessDefinition deployProcessWithMultiInstanceCallActivity(final int loopCardinality, final String targetProcess, final String targetVersion)
            throws BonitaException {
        final Expression targetProcExpr = new ExpressionBuilder().createConstantStringExpression(targetProcess);
        final Expression targetVersionExpr = new ExpressionBuilder().createConstantStringExpression(targetVersion);

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder()
                .createNewInstance("RemainingInstancesAreAbortedAfterCompletionCondition", "1.0");
        builder.addStartEvent("start");
        builder.addCallActivity("callActivity", targetProcExpr, targetVersionExpr)
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(loopCardinality))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("deployProcessWithMultiInstanceCallActivity",
                                ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES.getEngineConstantName() + " == 1 ", Boolean.class.getName(),
                                new ExpressionBuilder().createEngineConstant(ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES)));
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
        final ProcessDefinition targetProcess = deployProcessWith2UserTasksAnd1AutoTask("delivery", taskName1, taskName2, autoTaskName);
        final int loopCardinality = 2;
        final ProcessDefinition parentProcess = deployProcessWithMultiInstanceCallActivity(loopCardinality, targetProcess.getName(), targetProcess.getVersion());
        final ProcessInstance parentProcessInstance = getProcessAPI().startProcess(parentProcess.getId());
        checkNbOfProcessInstances(loopCardinality + 1);

        // execute task1 of a target process instance
        final List<HumanTaskInstance> pendingHumanTaskInstances = checkNbPendingTaskOf(true, 2 * loopCardinality, pedro).getPendingHumanTaskInstances();
        final HumanTaskInstance humanTaskInst1ToExecute = pendingHumanTaskInstances.get(0);
        assertEquals(taskName1, humanTaskInst1ToExecute.getName());
        assignAndExecuteStep(humanTaskInst1ToExecute, pedro.getId());

        final HumanTaskInstance humanTaskInst1ToAbort = pendingHumanTaskInstances.get(1);
        assertEquals(taskName1, humanTaskInst1ToAbort.getName());
        final long toBeAbortedProcInstId = humanTaskInst1ToAbort.getParentProcessInstanceId();
        final ProcessInstance procInstToAbort = getProcessAPI().getProcessInstance(toBeAbortedProcInstId);

        // execute task2 of same target process instance
        HumanTaskInstance humanTaskInst2ToExecute = pendingHumanTaskInstances.get(2);
        assertEquals(taskName2, humanTaskInst2ToExecute.getName());
        HumanTaskInstance humanTaskInst2ToAbort = pendingHumanTaskInstances.get(3);
        assertEquals(taskName2, humanTaskInst2ToAbort.getName());
        if (humanTaskInst1ToExecute.getParentProcessInstanceId() != humanTaskInst2ToExecute.getParentProcessInstanceId()) { // ensure tasks are in the same
            humanTaskInst2ToAbort = humanTaskInst2ToExecute;
            humanTaskInst2ToExecute = pendingHumanTaskInstances.get(3);
        }
        assignAndExecuteStep(humanTaskInst2ToExecute, pedro.getId());

        // the target process instances that exceed the max loop must be in aborted state
        waitForProcessToFinish(procInstToAbort, TestStates.getAbortedState());

        // task1 not executed must be in aborted state
        waitForArchivedActivity(humanTaskInst1ToAbort.getId(), TestStates.getAbortedState());
        // task2 not executed must be in aborted state
        waitForArchivedActivity(humanTaskInst2ToAbort.getId(), TestStates.getAbortedState());

        // check the automatic task in the aborted process instance was not created
        checkWasntExecuted(procInstToAbort, autoTaskName);

        // the parent process instance must finish in normal state
        waitForProcessToFinish(parentProcessInstance);

        disableAndDeleteProcess(parentProcess);
        disableAndDeleteProcess(targetProcess);
    }

}
