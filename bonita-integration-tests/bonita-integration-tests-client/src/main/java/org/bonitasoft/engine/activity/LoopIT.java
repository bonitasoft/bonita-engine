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
package org.bonitasoft.engine.activity;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessManagementAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedLoopActivityInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperationBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Test;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Matthieu Chaffotte
 */
public class LoopIT extends TestWithUser {

    @After
    public void afterTest() {
        VariableStorage.clearAll();
    }

    @Test
    public void executeAStandardLoopUserTaskWhichDoesNotLoop() throws Exception {
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(false);

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAStandardLoopUserTaskWhichDoesNotLoop", "1.0");
        builder.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        builder.addUserTask("step1", ACTOR_NAME).addLoop(true, condition);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(instance);
        final List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(instance.getId(), 0, 100,
                ActivityInstanceCriterion.NAME_ASC);
        assertEquals(2, archivedActivityInstances.size());
        for (final ArchivedActivityInstance archivedActivityInstance : archivedActivityInstances) {
            assertTrue(ArchivedLoopActivityInstance.class.isInstance(archivedActivityInstance));
        }

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeAStandardLoopUserTask() throws Exception {
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAStandardLoopUserTask", "1.0");
        builder.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        builder.addUserTask("step1", ACTOR_NAME).addLoop(false, condition);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        waitForUserTask("step1");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.EXPRESSIONS, keywords = { "expression context", "flow node container hierarchy" }, jira = "ENGINE-1848")
    public void evaluateExpressionsOnLoopUserTask() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("evaluateExpressionsOnLoopUserTask", "1.0");
        builder.addActor(ACTOR_NAME).addDescription("For Golf players only");
        final String activityName = "launch";
        builder.addStartEvent("dummy");
        builder.addUserTask(activityName, ACTOR_NAME).addLoop(false, new ExpressionBuilder().createConstantBooleanExpression(true));
        builder.addTransition("dummy", activityName);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        try {
            final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
            final long userTaskId = waitForUserTask(processInstance, activityName);
            final Map<Expression, Map<String, Serializable>> expressions = new HashMap<Expression, Map<String, Serializable>>();
            expressions.put(new ExpressionBuilder().createConstantBooleanExpression(true), new HashMap<String, Serializable>(0));
            getProcessAPI().evaluateExpressionsOnActivityInstance(userTaskId, expressions);
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Test
    public void executeAStandardLoopWithMaxIteration() throws Exception {
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);
        final int loopMax = 3;

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAStandardLoopUserTask", "1.0");
        builder.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        builder.addIntegerData("loopMax", new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        builder.addUserTask("step1", ACTOR_NAME).addLoop(false, condition, new ExpressionBuilder().createDataExpression("loopMax", Integer.class.getName()));
        builder.addUserTask("step2", ACTOR_NAME).addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        for (int i = 0; i < loopMax; i++) {
            final long step1Id = waitForUserTaskAndcheckPendingHumanTaskInstances("step1", processInstance);
            assignAndExecuteStep(step1Id, user);
        }
        waitForUserTaskAndcheckPendingHumanTaskInstances("step2", processInstance);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeAStandardLoopWithConditionUsingLoopCounter() throws Exception {
        final Expression condition = new ExpressionBuilder().createGroovyScriptExpression("executeAStandardLoopWithConditionUsingLoopCounter",
                "loopCounter < 3", Boolean.class.getName(), Arrays.asList(new ExpressionBuilder().createEngineConstant(ExpressionConstants.LOOP_COUNTER)));

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAStandardLoopUserTask", "1.0");
        builder.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        builder.addUserTask("step1", ACTOR_NAME).addLoop(false, condition);
        builder.addUserTask("step2", ACTOR_NAME).addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        for (int i = 0; i < 3; i++) {
            final long step1Id = waitForUserTaskAndcheckPendingHumanTaskInstances("step1", processInstance);
            assignAndExecuteStep(step1Id, user);
        }
        waitForUserTaskAndcheckPendingHumanTaskInstances("step2", processInstance);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeAStandardLoopWithConditionUsingDataUsingLoopCounter() throws Exception {
        final Expression condition = new ExpressionBuilder().createGroovyScriptExpression("executeAStandardLoopWithConditionUsingLoopCounter",
                "pData + loopCounter < 6", Boolean.class.getName(), Arrays.asList(new ExpressionBuilder().createDataExpression("pData",Integer.class.getName()),new ExpressionBuilder().createEngineConstant(ExpressionConstants.LOOP_COUNTER)));

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAStandardLoopUserTask", "1.0");
        builder.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        builder.addData("pData", Integer.class.getName(), null);
        UserTaskDefinitionBuilder step1 = builder.addUserTask("step1", ACTOR_NAME);
        step1.addLoop(false, condition);
        step1.addData("theData", Integer.class.getName(), new ExpressionBuilder().createEngineConstant(ExpressionConstants.LOOP_COUNTER));
        step1.addOperation(new OperationBuilder().createSetDataOperation("pData",new ExpressionBuilder().createEngineConstant(ExpressionConstants.LOOP_COUNTER)));
        builder.addUserTask("step2", ACTOR_NAME).addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        for (int i = 0; i < 3; i++) {
            final long step1Id = waitForUserTaskAndcheckPendingHumanTaskInstances("step1", processInstance);
            assignAndExecuteStep(step1Id, user);
        }
        waitForUserTaskAndcheckPendingHumanTaskInstances("step2", processInstance);

        disableAndDeleteProcess(processDefinition);
    }


    private long waitForUserTaskAndcheckPendingHumanTaskInstances(final String userTaskName, final ProcessInstance processInstance)
            throws Exception {
        final long pendingTaskId = waitForUserTask(processInstance, userTaskName);
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        return pendingTaskId;
    }

    @Test
    public void executeAStandardLoopWithConditionUsingData() throws Exception {
        final Expression condition = new ExpressionBuilder().createGroovyScriptExpression("executeAStandardLoopWithConditionUsingData1", "myData < 3",
                Boolean.class.getName(), Arrays.asList(new ExpressionBuilder().createDataExpression("myData", Integer.class.getName())));

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAStandardLoopUserTaskWithData", "1.0");
        builder.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        builder.addData("myData", Integer.class.getName(), new ExpressionBuilder().createConstantIntegerExpression(0));
        builder.addUserTask("step1", ACTOR_NAME)
                .addLoop(false, condition)
                .addOperation(
                        new LeftOperandBuilder().createNewInstance("myData").done(),
                        OperatorType.ASSIGNMENT,
                        "=",
                        null,
                        new ExpressionBuilder().createGroovyScriptExpression("executeAStandardLoopWithConditionUsingData1", "myData + 1",
                                Integer.class.getName(), Arrays.asList(new ExpressionBuilder().createDataExpression("myData", Integer.class.getName()))));
        builder.addUserTask("step2", ACTOR_NAME).addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        for (int i = 0; i < 3; i++) {
            final long step1Id = waitForUserTaskAndcheckPendingHumanTaskInstances("step1", processInstance);
            assignAndExecuteStep(step1Id, user);
        }
        waitForUserTaskAndcheckPendingHumanTaskInstances("step2", processInstance);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void abortProcessWithActiveLoopActivity() throws Exception {
        // given
        final String loopName = "step1";
        final String userTaskName = "step2";
        final ProcessDefinition processDefinition = deployAndEnableProcessWithLoopAndUserTaskInPararallelAndTerminateEvent(loopName, userTaskName);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        waitForUserTask(processInstance.getId(), loopName);
        // when
        waitForUserTaskAndExecuteIt(processInstance, userTaskName, user);

        // then
        // executing the user task will terminate the process: the loop activity must be aborted
        waitForFlowNodeInState(processInstance, loopName, TestStates.ABORTED, true);
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployAndEnableProcessWithLoopAndUserTaskInPararallelAndTerminateEvent(final String loopName, final String parallelTaskName)
            throws InvalidExpressionException, BonitaException, InvalidProcessDefinitionException {
        final Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("My proc", "1.0");
        builder.addActor(ACTOR_NAME).addDescription("ACTOR_NAME all day and night long");
        builder.addStartEvent("start");
        builder.addUserTask(loopName, ACTOR_NAME).addLoop(false, condition);
        builder.addUserTask(parallelTaskName, ACTOR_NAME);
        builder.addGateway("gateway", GatewayType.PARALLEL);
        builder.addEndEvent("terminate").addTerminateEventTrigger();
        builder.addTransition("start", "gateway");
        builder.addTransition("gateway", loopName);
        builder.addTransition("gateway", parallelTaskName);
        builder.addTransition(loopName, "terminate");
        builder.addTransition(parallelTaskName, "terminate");

        return deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

}
