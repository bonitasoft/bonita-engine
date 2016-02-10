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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedAutomaticTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedMultiInstanceActivityInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.MultiInstanceActivityInstance;
import org.bonitasoft.engine.bpm.flownode.impl.internal.FlowElementContainerDefinitionImpl;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connectors.TestConnector;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionConstants;
import org.bonitasoft.engine.filter.user.TestFilterWithAutoAssign;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class MultiInstanceIT extends TestWithTechnicalUser {

    private static final String JACK = "jack";

    private static final String JOHN = "john";

    private static final String JENNY = "jenny";

    private User john;

    private User jack;

    private User jenny;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        john = createUser(JOHN, "bpm");
        jack = createUser(JACK, "bpm");
        jenny = createUser(JENNY, "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith(JOHN, "bpm");
    }

    @Override
    @After
    public void after() throws Exception {
        deleteUser(JOHN);
        deleteUser(JACK);
        deleteUser(JENNY);
        VariableStorage.clearAll();
        super.after();
    }

    @Test
    public void executeAMultiInstanceUserTaskWhichCreate0Task() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceUserTaskWhichCreate0Task",
                PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addAutomaticTask("autostep").addUserTask("step1", ACTOR_NAME)
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(0));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(instance);
        final List<ArchivedActivityInstance> archivedActivityInstances = getProcessAPI().getArchivedActivityInstances(instance.getId(), 0, 100,
                ActivityInstanceCriterion.NAME_ASC);
        assertEquals(4, archivedActivityInstances.size());
        for (final ArchivedActivityInstance archivedActivityInstance : archivedActivityInstances) {
            assertTrue(ArchivedAutomaticTaskInstance.class.isInstance(archivedActivityInstance) && archivedActivityInstance.getName().contains("auto")
                    || ArchivedMultiInstanceActivityInstance.class.isInstance(archivedActivityInstance));
        }

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeAMultiInstanceUserTask() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceUserTask", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME).addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(2));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", john);
        waitForUserTask(processInstance, "step1");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchMultiInstance() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceUserTask", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME).addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(1));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final long step1Id = waitForUserTask(processInstance, "step1");
        final SearchResult<ActivityInstance> searchActivities = getProcessAPI().searchActivities(
                new SearchOptionsBuilder(0, 10).filter(ActivityInstanceSearchDescriptor.PROCESS_INSTANCE_ID, processInstance.getId())
                        .filter(ActivityInstanceSearchDescriptor.STATE_NAME, "executing").done());
        final MultiInstanceActivityInstance activityInstance = (MultiInstanceActivityInstance) searchActivities.getResult().get(0);
        assertEquals(1, activityInstance.getNumberOfActiveInstances());

        final SearchResult<FlowNodeInstance> searchFlowNode = getProcessAPI().searchFlowNodeInstances(
                new SearchOptionsBuilder(0, 10).filter(FlowNodeInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, processInstance.getId())
                        .filter(FlowNodeInstanceSearchDescriptor.STATE_NAME, "executing").done());
        final MultiInstanceActivityInstance flowNode = (MultiInstanceActivityInstance) searchFlowNode.getResult().get(0);
        assertEquals(1, flowNode.getNumberOfActiveInstances());

        assignAndExecuteStep(step1Id, john);
        waitForProcessToFinish(processInstance);

        final SearchResult<ArchivedActivityInstance> searchArchivedActivities = getProcessAPI().searchArchivedActivities(
                new SearchOptionsBuilder(0, 10).filter(ArchivedActivityInstanceSearchDescriptor.SOURCE_OBJECT_ID, flowNode.getId()).done());
        assertTrue(ArchivedMultiInstanceActivityInstance.class.isInstance(searchArchivedActivities.getResult().get(0)));
        assertEquals(flowNode.getId(), searchArchivedActivities.getResult().get(0).getSourceObjectId());

        final SearchResult<ArchivedFlowNodeInstance> searchArchivedFlowNode = getProcessAPI().searchArchivedFlowNodeInstances(
                new SearchOptionsBuilder(0, 10).filter(ArchivedFlowNodeInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, processInstance.getId())
                        .filter(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME, "executing").done());
        assertTrue(ArchivedMultiInstanceActivityInstance.class.isInstance(searchArchivedFlowNode.getResult().get(0)));
        assertEquals(flowNode.getId(), searchArchivedFlowNode.getResult().get(0).getSourceObjectId());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeAMultiInstanceWithMaxIteration() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceWithMaxIteration", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addIntegerData("a", new ExpressionBuilder().createConstantIntegerExpression(1));
        builder.addIntegerData("b", new ExpressionBuilder().createConstantIntegerExpression(2));
        final int loopMax = 3;
        builder.addUserTask("step1", ACTOR_NAME).addMultiInstance(
                true,
                new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceWithMaxIteration", "a + b", Integer.class.getName(),
                        new ExpressionBuilder().createDataExpression("a", Integer.class.getName()),
                        new ExpressionBuilder().createDataExpression("b", Integer.class.getName())));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        checkPendingTaskSequentially(loopMax, processInstance, true);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeAMultiInstanceWithLoopDataInputAndOutput() throws Exception {
        final List<?> outputList = executeAMultiInstanceWithLoopDataAs("[58,26,12]", "[1,2,3]");
        assertEquals(3, outputList.size());
        assertEquals(59, outputList.get(0));
        assertEquals(27, outputList.get(1));
        assertEquals(13, outputList.get(2));
    }

    private List<?> executeAMultiInstanceWithLoopDataAs(final String inputListScript, final String outputListScript) throws Exception {
        final String anotherActor = "anotherActor";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceWithMaxIteration", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addActor(anotherActor);

        final String loopDataInputName = "loopDataInput_";
        String loopDataOutputName = "loopDataOutput_";
        builder.addData(loopDataInputName, List.class.getName(),
                new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceWithLoopDataInputAndOutput1", inputListScript, List.class.getName()));
        if (outputListScript != null) {
            builder.addData(
                    loopDataOutputName,
                    List.class.getName(),
                    new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceWithLoopDataInputAndOutput2", outputListScript,
                            List.class.getName()));
        } else {
            loopDataOutputName = loopDataInputName;
        }
        final UserTaskDefinitionBuilder userTask = builder.addUserTask("step1", ACTOR_NAME);
        userTask.addData("dataInputItem_", Integer.class.getName(), new ExpressionBuilder().createConstantIntegerExpression(0));
        userTask.addData("dataOutputItem_", Integer.class.getName(), new ExpressionBuilder().createConstantIntegerExpression(0));
        userTask.addOperation(
                new LeftOperandBuilder().createNewInstance("dataOutputItem_").done(),
                OperatorType.ASSIGNMENT,
                "=",
                null,
                new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceWithLoopDataInputAndOutput3", "dataInputItem_ + 1",
                        Integer.class.getName(), new ExpressionBuilder().createDataExpression("dataInputItem_", Integer.class.getName())));
        userTask.addMultiInstance(true, loopDataInputName).addDataInputItemRef("dataInputItem_").addDataOutputItemRef("dataOutputItem_")
                .addLoopDataOutputRef(loopDataOutputName);
        builder.addUserTask("lastTask", anotherActor);
        builder.addTransition("step1", "lastTask");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), Arrays.asList(ACTOR_NAME, anotherActor),
                Arrays.asList(john, jack));
        final ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());
        final DataInstance processDataInstance2 = getProcessAPI().getProcessDataInstance(loopDataInputName, process.getId());
        final List<?> value = (List<?>) processDataInstance2.getValue();
        final int loopMax = value.size();
        checkPendingTaskSequentially(loopMax, process, false);
        final DataInstance processDataInstance = getProcessAPI().getProcessDataInstance(loopDataOutputName, process.getId());
        assertNotNull("unable to find the loop data output on the process", processDataInstance);
        final List<?> list = (List<?>) processDataInstance.getValue();
        waitForUserTask(process, "lastTask");
        disableAndDeleteProcess(processDefinition);
        return list;
    }

    @Test
    public void executeAMultiInstanceWithLoopDataOutputEmpty() throws Exception {
        final List<?> list = executeAMultiInstanceWithLoopDataAs("[58,26,12]", "[]");
        assertEquals(3, list.size());
        assertEquals(59, list.get(0));
        assertEquals(27, list.get(1));
        assertEquals(13, list.get(2));
    }

    @Test
    public void executeAMultiInstanceWithLoopDataOutputNull() throws Exception {
        final List<?> list = executeAMultiInstanceWithLoopDataAs("[58,26,12]", "null");
        assertEquals(3, list.size());
        assertEquals(59, list.get(0));
        assertEquals(27, list.get(1));
        assertEquals(13, list.get(2));
    }

    @Test
    public void executeAMultiInstanceWithLoopDataOutputTooShort() throws Exception {
        final List<?> list = executeAMultiInstanceWithLoopDataAs("[58,26,12]", "[1,2]");
        assertEquals(3, list.size());
        assertEquals(59, list.get(0));
        assertEquals(27, list.get(1));
        assertEquals(13, list.get(2));
    }

    @Test
    public void executeAMultiInstanceWithLoopDataOutputTooLong() throws Exception {
        final List<?> list = executeAMultiInstanceWithLoopDataAs("[58,26,12]", "[1,2,3,4]");
        assertEquals(4, list.size());
        assertEquals(59, list.get(0));
        assertEquals(27, list.get(1));
        assertEquals(13, list.get(2));
        assertEquals(4, list.get(3));
    }

    @Test
    public void executeAMultiInstanceWithSameLoopDataAsInputAndOutput() throws Exception {
        final List<?> list = executeAMultiInstanceWithLoopDataAs("[58,26,12]", null);
        assertEquals(3, list.size());
        assertEquals(59, list.get(0));
        assertEquals(27, list.get(1));
        assertEquals(13, list.get(2));
    }

    @Test
    public void executeAMultiInstanceParallelWithLoopCardinalityUsingGroovyAndData() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceParallelWithMaxIteration",
                PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addIntegerData("a", new ExpressionBuilder().createConstantIntegerExpression(1));
        builder.addIntegerData("b", new ExpressionBuilder().createConstantIntegerExpression(2));
        final int loopMax = 3;
        builder.addUserTask("step1", ACTOR_NAME).addMultiInstance(
                false,
                new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceParallelWithLoopCardinalityUsingGroovyAndData", "a + b",
                        Integer.class.getName(), new ExpressionBuilder().createDataExpression("a", Integer.class.getName()),
                        new ExpressionBuilder().createDataExpression("b", Integer.class.getName())));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        checkPendingTaskInParallel(loopMax, loopMax, processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeAMultiInstanceParallelWithLoopCardinalityMoreThan20() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceParallelWithMaxIteration",
                PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addIntegerData("a", new ExpressionBuilder().createConstantIntegerExpression(16));
        builder.addIntegerData("b", new ExpressionBuilder().createConstantIntegerExpression(14));
        final int loopMax = 30;
        builder.addUserTask("step1", ACTOR_NAME).addMultiInstance(
                false,
                new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceParallelWithLoopCardinalityUsingGroovyAndData", "a + b",
                        Integer.class.getName(), new ExpressionBuilder().createDataExpression("a", Integer.class.getName()),
                        new ExpressionBuilder().createDataExpression("b", Integer.class.getName())));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        checkPendingTaskInParallel(loopMax, loopMax, processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeAMultiInstanceParallelWithCompletionCondition() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceParallelWithCompletionCondition",
                PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        final int loopMax = 3;
        builder.addUserTask("step1", ACTOR_NAME)
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(3))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceParallelWithCompletionCondition",
                                ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES.getEngineConstantName() + " >= 2 ", Boolean.class.getName(),
                                new ExpressionBuilder().createEngineConstant(ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES)));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskInParallel(loopMax, 2, processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void remainingInstancesAreAbortedAfterCompletionCondition() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("remainingInstancesAreAbortedAfterCompletionCondition",
                PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        final int loopMax = 3;
        builder.addUserTask("step1", ACTOR_NAME)
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(loopMax))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("remainingInstancesAreAbortedAfterCompletionCondition",
                                ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES.getEngineConstantName() + " == 1 ", Boolean.class.getName(),
                                new ExpressionBuilder().createEngineConstant(ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES)));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskInParallel(loopMax, 1, processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Test of completion condition (Sequential multi-instance - Number of completed instances).
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeAMultiInstanceSequentialWithCompletionCondition() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceSequentialWithCompletionCondition",
                PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME)
                .addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(20))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceSequentialWithCompletionCondition",
                                ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES.getEngineConstantName() + " >= 15 ", Boolean.class.getName(),
                                new ExpressionBuilder().createEngineConstant(ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES)));

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskSequentially(15, processInstance, true);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void abortAMultiInstanceSequential() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("Start").addEndEvent("End").addTerminateEventTrigger();
        builder.addUserTask("Step1", ACTOR_NAME).addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(20))
                .addCompletionCondition(new ExpressionBuilder().createConstantBooleanExpression(false));
        builder.addGateway("Gateway", GatewayType.PARALLEL).addUserTask("Step2", ACTOR_NAME);
        builder.addTransition("Start", "Gateway").addTransition("Gateway", "Step1").addTransition("Step1", "End").addTransition("Gateway", "Step2")
                .addTransition("Step2", "End");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "Step1");
        waitForUserTaskAndExecuteIt(processInstance, "Step2", john);
        waitForProcessToFinish(processInstance);
        final ArchivedActivityInstance archivedStep1 = getProcessAPI().getArchivedActivityInstance(step1Id);
        assertEquals(ActivityStates.ABORTED_STATE, archivedStep1.getState());

        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Test with completion condition equal to true (sequential multi-instance).
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeAMultiInstanceSequentialWithConpletionConditionTrue() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceSequentialWithCompletionConditionTrue",
                PROCESS_VERSION);
        builder.addActor(ACTOR_NAME).addDescription("Deliver all day and night long");
        builder.addUserTask("step1", ACTOR_NAME)
                .addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(4))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceSequentialWithCompletionConditionTrue", "true",
                                Boolean.class.getName()));
        builder.addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addTransition("step1", "step2").addTransition("step2", "step3");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskSequentially(3, processInstance, true);
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Test with completion condition equal to true (parallel multi-instance).
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeAMultiInstanceParallelWithConpletionConditionTrue() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceSequentialWithCompletionConditionTrue",
                PROCESS_VERSION);
        builder.addActor(ACTOR_NAME).addDescription("Deliver all day and night long");
        builder.addUserTask("step1", ACTOR_NAME)
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(4))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceSequentialWithCompletionConditionTrue", "true",
                                Boolean.class.getName()));
        builder.addAutomaticTask("step2").addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step1");
        waitForUserTask(processInstance, "step1");
        waitForUserTask(processInstance, "step1");
        waitForUserTaskAndExecuteIt(processInstance, "step1", john);

        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Test of completion condition (Sequential multi-instance - Number of completed instances).
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeAMultiInstanceWithCompletionConditionNumberOfCompletedInstances() throws Exception {
        final String condition = " >= 2";
        final int loopMax = 3;
        final int numberOfExecutedActivities = loopMax - 1;

        withMultiInstanceAttribute(condition, ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES, numberOfExecutedActivities);
    }

    /**
     * Test of completion condition (Sequential multi-instance - Number of instances).
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeAMultiInstanceWithCompletionConditionNumberOfInstances() throws Exception {
        final String condition = " >= 2";
        final int loopMax = 3;
        final int numberOfExecutedActivities = loopMax - 1;

        withMultiInstanceAttribute(condition, ExpressionConstants.NUMBER_OF_INSTANCES, numberOfExecutedActivities);
    }

    /**
     * Test of completion condition (Sequential multi-instance - Number of terminated instances).
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeAMultiInstanceWithCompletionConditionNumberOfTerminatedInstances() throws Exception {
        final String condition = " >= 2";
        final int loopMax = 3;
        final int numberOfExecutedActivities = loopMax;// numberOfTerminated instance is never >= 2 here

        withMultiInstanceAttribute(condition, ExpressionConstants.NUMBER_OF_TERMINATED_INSTANCES, numberOfExecutedActivities);
    }

    /**
     * Test of completion condition (Sequential multi-instance - Number of actives instances).
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeAMultiInstanceWithCompletionConditionNumberOfActiveInstances() throws Exception {
        final String condition = " == 2";
        final int loopMax = 3;
        final int numberOfExecutedActivities = loopMax;// will never be true when is in sequence

        withMultiInstanceAttribute(condition, ExpressionConstants.NUMBER_OF_ACTIVE_INSTANCES, numberOfExecutedActivities);
    }

    /**
     * Test of completion condition (Parallel multi-instance - Number of completed instances).
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeAMultiInstanceParallelWithCompletionConditionNumberOfCompletedInstances() throws Exception {
        final String condition = " >= 2 ";
        final int loopMax = 3;
        final int numberOfExecutedActivities = loopMax;
        final int numberOfTaskToComplete = 2;

        withParallelMultiInstanceAttribute(condition, ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES, numberOfExecutedActivities, numberOfTaskToComplete);
    }

    /**
     * Test of completion condition (Parallel multi-instance - Number of instances).
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeAMultiInstanceParallelWithCompletionConditionNumberOfInstances() throws Exception {
        final String condition = " >= 2";
        final int loopMax = 3;
        final int numberOfExecutedActivities = loopMax;
        final int numberOfTaskToComplete = 1;

        withParallelMultiInstanceAttribute(condition, ExpressionConstants.NUMBER_OF_INSTANCES, numberOfExecutedActivities, numberOfTaskToComplete);
    }

    /**
     * Test of completion condition (Parallel multi-instance - Number of terminated instances).
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeAMultiInstanceParallelWithCompletionConditionNumberOfTerminatedInstances() throws Exception {
        final String condition = " >= 2";
        final int loopMax = 3;
        final int numberOfExecutedActivities = loopMax;
        final int numberOfTaskToComplete = 3;

        withParallelMultiInstanceAttribute(condition, ExpressionConstants.NUMBER_OF_TERMINATED_INSTANCES, numberOfExecutedActivities, numberOfTaskToComplete);
    }

    /**
     * Test of completion condition (Parallel multi-instance - Number of actives instances).
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeAMultiInstanceParallelWithCompletionConditionNumberOfActiveInstances() throws Exception {
        final String condition = " == 2";
        final int loopMax = 3;
        final int numberOfExecutedActivities = loopMax;
        final int numberOfTaskToComplete = 1;

        withParallelMultiInstanceAttribute(condition, ExpressionConstants.NUMBER_OF_ACTIVE_INSTANCES, numberOfExecutedActivities, numberOfTaskToComplete);
    }

    /**
     * Test of task execution after a sequential multi-instance with user tasks.
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeTaskAfterMultiInstanceSequential() throws Exception {
        final int loopMax = 3;
        final int numberOfExecutedActivities = loopMax + 1;

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeTaskAfterMultiInstanceSequential", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME).addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        builder.addAutomaticTask("step2").addUserTask("step3", ACTOR_NAME).addTransition("step1", "step2").addTransition("step2", "step3");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskSequentially(numberOfExecutedActivities, processInstance, true);
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Test of task execution after a sequential multi-instance with automatics tasks.
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeTaskAfterMultiInstanceSequentialAuto() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeTaskAfterMultiInstanceSequentialAuto",
                PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addAutomaticTask("step1").addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(3));
        builder.addAutomaticTask("step2").addUserTask("step3", ACTOR_NAME).addTransition("step1", "step2").addTransition("step2", "step3");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step3", john);

        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Test of task execution after a parallel multi-instance with user tasks.
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeTaskAfterMultiInstanceParallel() throws Exception {
        final int loopMax = 3;
        final int numberOfTaskToComplete = 3;

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeTaskAfterMultiInstanceSequential", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME).addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        builder.addAutomaticTask("step2").addAutomaticTask("step3").addTransition("step1", "step2").addTransition("step2", "step3");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskInParallel(loopMax, numberOfTaskToComplete, processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Test of task execution after a parallel multi-instance with automatics tasks.
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void executeTaskAfterMultiInstanceParallelAuto() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeTaskAfterMultiInstanceSequentialAuto",
                PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addAutomaticTask("step1").addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(3));
        builder.addAutomaticTask("step2").addUserTask("step3", ACTOR_NAME).addTransition("step1", "step2").addTransition("step2", "step3");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step3", john);

        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Test of parallel multi-instance with several users.
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void multiInstanceParallelWithSeveralUsers() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeMultiInstanceWithActors", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME).addDescription("Survey");
        builder.addUserTask("step1", ACTOR_NAME).addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(3));
        builder.addAutomaticTask("step2").addTransition("step1", "step2");

        final List<User> listUsers = new ArrayList<User>();
        final List<String> listActors = new ArrayList<String>();
        listUsers.add(john);
        listActors.add(ACTOR_NAME);
        listUsers.add(jack);
        listActors.add(ACTOR_NAME);
        listUsers.add(jenny);
        listActors.add(ACTOR_NAME);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), listActors, listUsers);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkNbPendingTaskOf(3, john);
        checkNbPendingTaskOf(3, jack);
        checkNbPendingTaskOf(3, jenny);

        // Execute task of multi-instance for John
        final List<HumanTaskInstance> pendingTasks1 = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask1 = pendingTasks1.get(0);
        assignAndExecuteStep(pendingTask1, john.getId());

        // Execute task of multi-instance for Jack
        final List<HumanTaskInstance> pendingTasks2 = getProcessAPI().getPendingHumanTaskInstances(jack.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask2 = pendingTasks2.get(0);
        assignAndExecuteStep(pendingTask2, jack.getId());

        // Execute task of multi-instance for Jenny
        final List<HumanTaskInstance> pendingTasks3 = getProcessAPI().getPendingHumanTaskInstances(jenny.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask3 = pendingTasks3.get(0);
        assignAndExecuteStep(pendingTask3, jenny.getId());

        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Test of sequential multi-instance with several users.
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void multiInstanceSequentialWithSeveralUsers() throws Exception {
        final int loopMax = 3;

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeMultiInstanceWithActors", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME).addDescription("Survey");
        builder.addUserTask("step1", ACTOR_NAME).addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        builder.addAutomaticTask("step2").addTransition("step1", "step2");

        final List<User> listUsers = new ArrayList<User>();
        final List<String> listActors = new ArrayList<String>();
        listUsers.add(john);
        listActors.add(ACTOR_NAME);
        listUsers.add(jack);
        listActors.add(ACTOR_NAME);
        listUsers.add(jenny);
        listActors.add(ACTOR_NAME);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), listActors, listUsers);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // Execute task of multi-instance for John
        checkNbPendingTaskOf(1, john);
        final List<HumanTaskInstance> pendingTasks1 = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask1 = pendingTasks1.get(0);
        assignAndExecuteStep(pendingTask1, john.getId());

        // Execute task of multi-instance for Jack
        checkNbPendingTaskOf(1, jack);
        final List<HumanTaskInstance> pendingTasks2 = getProcessAPI().getPendingHumanTaskInstances(jack.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask2 = pendingTasks2.get(0);
        assignAndExecuteStep(pendingTask2, jack.getId());

        // Execute task of multi-instance for Jenny
        checkNbPendingTaskOf(1, jenny);
        final List<HumanTaskInstance> pendingTasks3 = getProcessAPI().getPendingHumanTaskInstances(jenny.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask3 = pendingTasks3.get(0);
        assignAndExecuteStep(pendingTask3, jenny.getId());

        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Test of sequential multi-instance with sub-process.
     *
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void multiInstanceSequentialWithSubProcess() throws Exception {
        final int loopMax = 3;

        // Sub-process definition
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("SubProcessInAMultiInstance", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME).addAutomaticTask("step1").addAutomaticTask("step2").addTransition("step1", "step2");

        final ProcessDefinition subProcess = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);

        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression(subProcess.getName());
        final Expression targetProcessVersionExpr = new ExpressionBuilder().createConstantStringExpression(subProcess.getVersion());

        final ProcessDefinitionBuilder builderProc = new ProcessDefinitionBuilder().createNewInstance("executeMultiInstanceSequentialWithSubProcess", "1.1");
        builderProc.addActor(ACTOR_NAME).addStartEvent("start")
                .addCallActivity("callActivity", targetProcessNameExpr, targetProcessVersionExpr)
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        builderProc.addAutomaticTask("step3").addEndEvent("end").addTransition("start", "callActivity").addTransition("callActivity", "step3")
                .addTransition("step3", "end");

        final ProcessDefinition mainProcess = deployAndEnableProcessWithActor(builderProc.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(mainProcess.getId());

        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(mainProcess);
        disableAndDeleteProcess(subProcess);
    }

    private void withMultiInstanceAttribute(final String condition, final ExpressionConstants expressionConstant, final int numberOfExecutedActivities)
            throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAStandardLoopUserTask" + condition, PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addIntegerData("a", new ExpressionBuilder().createConstantIntegerExpression(1));
        builder.addIntegerData("b", new ExpressionBuilder().createConstantIntegerExpression(2));
        builder.addUserTask("step1", ACTOR_NAME)
                .addMultiInstance(
                        true,
                        new ExpressionBuilder().createGroovyScriptExpression("testWithMultiInstanceAttribute1", "a + b", Integer.class.getName(),
                                new ExpressionBuilder().createDataExpression("a", Integer.class.getName()),
                                new ExpressionBuilder().createDataExpression("b", Integer.class.getName())))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("testWithMultiInstanceAttribute2", expressionConstant.getEngineConstantName()
                                + condition, Boolean.class.getName(), new ExpressionBuilder().createEngineConstant(expressionConstant)));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskSequentially(numberOfExecutedActivities, processInstance, true);
        disableAndDeleteProcess(processDefinition);
    }

    private void withParallelMultiInstanceAttribute(final String condition, final ExpressionConstants expressionConstant, final int numberOfExecutedActivities,
            final int numberOfTaskToComplete) throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeParallelUserTask" + condition, PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addIntegerData("a", new ExpressionBuilder().createConstantIntegerExpression(1));
        builder.addIntegerData("b", new ExpressionBuilder().createConstantIntegerExpression(2));
        builder.addUserTask("step1", ACTOR_NAME)
                .addMultiInstance(
                        false,
                        new ExpressionBuilder().createGroovyScriptExpression("testWithMultiInstanceAttribute1", "a + b", Integer.class.getName(),
                                new ExpressionBuilder().createDataExpression("a", Integer.class.getName()),
                                new ExpressionBuilder().createDataExpression("b", Integer.class.getName())))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("testWithMultiInstanceAttribute2", expressionConstant.getEngineConstantName()
                                + condition, Boolean.class.getName(), new ExpressionBuilder().createEngineConstant(expressionConstant)));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskInParallel(numberOfExecutedActivities, numberOfTaskToComplete, processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    private void checkPendingTaskSequentially(final int loopMax, final ProcessInstance processInstance, final boolean mustBeFinished) throws Exception {
        for (int i = 0; i < loopMax; i++) {
            waitForPendingTasks(john.getId(), 1);
            final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
            final HumanTaskInstance pendingTask = pendingTasks.get(0);

            assignAndExecuteStep(pendingTask, john.getId());
        }
        if (mustBeFinished) {
            waitForProcessToFinish(processInstance);
        }
    }

    private void checkPendingTaskInParallel(final int numberOfTask, final int numberOfTaskToCompleteMI, final ProcessInstance processInstance) throws Exception {
        final List<HumanTaskInstance> pendingTasks = waitForPendingTasks(john.getId(), numberOfTask);

        for (int i = 0; i < numberOfTaskToCompleteMI; i++) {
            assignAndExecuteStep(pendingTasks.get(i), john.getId());
        }
        waitForProcessToFinish(processInstance);
        final int nbAbortedActivities = numberOfTask - numberOfTaskToCompleteMI;
        checkNbOfArchivedActivities(processInstance, nbAbortedActivities);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.MULTIINSTANCE, jira = "ENGINE-1967", keywords = { "MultiInstance", "Several actors",
            "User filter", "Comments" })
    @Test
    public void multiInstanceVoteUseCase() throws Exception {
        // Build process definition
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeMultiInstanceWithSeveralActors", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME).addDescription("Survey");
        final String loopDataInputName = "listOfUserId";
        final String exprListUserIds = "[" + john.getId() + "l," + jack.getId() + "l," + jenny.getId() + "l]";
        builder.addData(loopDataInputName, List.class.getName(),
                new ExpressionBuilder().createGroovyScriptExpression("createListUserId", exprListUserIds, List.class.getName()));
        final UserTaskDefinitionBuilder userTaskBuilder = new UserTaskDefinitionBuilder(builder, (FlowElementContainerDefinitionImpl) builder.getProcess()
                .getProcessContainer(), "step1", ACTOR_NAME);
        userTaskBuilder.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilterWithAutoAssign", PROCESS_VERSION).addInput("userId",
                new ExpressionBuilder().createDataExpression("userIdValue", Long.class.getName()));
        userTaskBuilder.addData("userIdValue", Long.class.getName(), null);
        userTaskBuilder.addMultiInstance(false, loopDataInputName).addDataInputItemRef("userIdValue");
        userTaskBuilder.addDisplayName(new ExpressionBuilder().createConstantStringExpression("displayName"));
        builder.addAutomaticTask("step2").addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployProcessWithTestFilter(ACTOR_NAME, builder);

        // Start process, and wait all multiinstancied user tasks
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step1");
        waitForUserTask(processInstance, "step1");
        waitForUserTask(processInstance, "step1");

        // Get comments and check it
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, processInstance.getId());
        searchOptionsBuilder.sort(SearchCommentsDescriptor.CONTENT, Order.ASC);
        final List<Comment> comments = getProcessAPI().searchComments(searchOptionsBuilder.done()).getResult();
        assertEquals("The task \"displayName\" is now assigned to " + jack.getUserName(), comments.get(0).getContent());
        assertEquals("The task \"displayName\" is now assigned to " + jenny.getUserName(), comments.get(1).getContent());
        assertEquals("The task \"displayName\" is now assigned to " + john.getUserName(), comments.get(2).getContent());

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployProcessWithTestFilter(final String actorName, final ProcessDefinitionBuilder designProcessDefinition)
            throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                designProcessDefinition.done());
        final List<BarResource> impl = generateFilterImplementations("TestFilterWithAutoAssign");
        for (final BarResource barResource : impl) {
            businessArchiveBuilder.addUserFilters(barResource);
        }
        final List<BarResource> generateFilterDependencies = new ArrayList<BarResource>(1);
        final byte[] data = IOUtil.generateJar(TestFilterWithAutoAssign.class);
        generateFilterDependencies.add(new BarResource("TestFilterWithAutoAssign.jar", data));

        for (final BarResource barResource : generateFilterDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        final ProcessDefinition processDefinition = deployProcess(businessArchiveBuilder.done());
        getProcessAPI().addUserToActor(actorName, processDefinition, john.getId());
        getProcessAPI().addUserToActor(actorName, processDefinition, jack.getId());
        getProcessAPI().addUserToActor(actorName, processDefinition, jenny.getId());
        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    private List<BarResource> generateFilterImplementations(final String filterName) throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(1);
        final InputStream inputStream = TestConnector.class.getClassLoader().getResourceAsStream("org/bonitasoft/engine/filter/user/" + filterName + ".impl");
        final byte[] data = IOUtil.getAllContentFrom(inputStream);
        inputStream.close();
        resources.add(new BarResource(filterName + ".impl", data));
        return resources;
    }

    @Test
    public void multiInstanceWithAnEmptyList_should_instantiate_once_the_task() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("multiInstanceWithAnEmptyList", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addData("list", List.class.getName(), new ExpressionBuilder().createGroovyScriptExpression("emptyList", "[]", List.class.getName()));
        final UserTaskDefinitionBuilder taskDefinitionBuilder = builder.addUserTask("step1", ACTOR_NAME);
        taskDefinitionBuilder.addData("listValue", String.class.getName(), null);
        taskDefinitionBuilder.addMultiInstance(true, "list").addDataInputItemRef("listValue");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step1");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void multiInstanceWithANullList_should_put_the_task_in_failed_state() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("multiInstanceWithAnEmptyList", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addData("list", List.class.getName(), null);
        final UserTaskDefinitionBuilder taskDefinitionBuilder = builder.addUserTask("step1", ACTOR_NAME);
        taskDefinitionBuilder.addData("listValue", String.class.getName(), null);
        taskDefinitionBuilder.addMultiInstance(true, "list").addDataInputItemRef("listValue");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForFlowNodeInFailedState(processInstance);

        disableAndDeleteProcess(processDefinition);
    }

}
