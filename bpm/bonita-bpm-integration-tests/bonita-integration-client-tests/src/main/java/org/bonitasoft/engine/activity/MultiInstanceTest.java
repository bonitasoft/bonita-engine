package org.bonitasoft.engine.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedAutomaticTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedMultiInstanceActivityInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.MultiInstanceActivityInstance;
import org.bonitasoft.engine.bpm.flownode.impl.FlowElementContainerDefinitionImpl;
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
import org.bonitasoft.engine.filter.user.TestFilter;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
@SuppressWarnings("javadoc")
public class MultiInstanceTest extends CommonAPITest {

    private static final String JACK = "jack";

    private static final String JOHN = "john";

    private static final String JENNY = "jenny";

    private User john;

    private User jack;

    private User jenny;

    @Before
    public void beforeTest() throws BonitaException {
        login();
        john = createUser(JOHN, "bpm");
        jack = createUser(JACK, "bpm");
        jenny = createUser(JENNY, "bpm");
        logout();
        loginWith(JOHN, "bpm");
    }

    @After
    public void afterTest() throws BonitaException {
        deleteUser(JOHN);
        deleteUser(JACK);
        deleteUser(JENNY);
        VariableStorage.clearAll();
        logout();
    }

    // @Ignore("no archive yet")
    @Test
    public void executeAMultiInstanceUserTaskWhichCreate0Task() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceUserTaskWhichCreate0Task", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addAutomaticTask("autostep").addUserTask("step1", delivery).addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(0));

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance instance = getProcessAPI().startProcess(processDefinition.getId());
        assertTrue(waitProcessToFinishAndBeArchived(instance));
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
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceUserTask", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addUserTask("step1", delivery).addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(2));

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        HumanTaskInstance pendingTask = pendingTasks.get(0);

        assignAndExecuteStep(pendingTask, john.getId());

        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 500, false, 1, john).waitUntil());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        pendingTask = pendingTasks.get(0);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchMultiInstance() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceUserTask", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addUserTask("step1", delivery).addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(1));

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final ActivityInstance userTask = waitForUserTask("step1", processInstance);
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

        assignAndExecuteStep(userTask, john.getId());
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
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceWithMaxIteration", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addIntegerData("a", new ExpressionBuilder().createConstantIntegerExpression(1));
        builder.addIntegerData("b", new ExpressionBuilder().createConstantIntegerExpression(2));
        final int loopMax = 3;
        builder.addUserTask("step1", delivery).addMultiInstance(
                true,
                new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceWithMaxIteration", "a + b", Integer.class.getName(),
                        new ExpressionBuilder().createDataExpression("a", Integer.class.getName()),
                        new ExpressionBuilder().createDataExpression("b", Integer.class.getName())));

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        checkPendingTaskSequentially(loopMax, processDefinition, processInstance, true);
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
        final String delivery = "Delivery men";
        final String anotherActor = "anotherActor";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceWithMaxIteration", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addActor(anotherActor).addDescription("Delivery all day and night long");

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
        final UserTaskDefinitionBuilder userTask = builder.addUserTask("step1", delivery);
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
        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), Arrays.asList(delivery, anotherActor), Arrays.asList(john, jack));
        final ProcessInstance process = getProcessAPI().startProcess(processDefinition.getId());
        final DataInstance processDataInstance2 = getProcessAPI().getProcessDataInstance(loopDataInputName, process.getId());
        final List<?> value = (List<?>) processDataInstance2.getValue();
        final int loopMax = value.size();
        checkPendingTaskSequentially(loopMax, processDefinition, process, false);
        final DataInstance processDataInstance = getProcessAPI().getProcessDataInstance(loopDataOutputName, process.getId());
        assertNotNull("unable to find the loop data output on the process", processDataInstance);
        final List<?> list = (List<?>) processDataInstance.getValue();
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
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceParallelWithMaxIteration", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addIntegerData("a", new ExpressionBuilder().createConstantIntegerExpression(1));
        builder.addIntegerData("b", new ExpressionBuilder().createConstantIntegerExpression(2));
        final int loopMax = 3;
        builder.addUserTask("step1", delivery).addMultiInstance(
                false,
                new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceParallelWithLoopCardinalityUsingGroovyAndData", "a + b",
                        Integer.class.getName(), new ExpressionBuilder().createDataExpression("a", Integer.class.getName()),
                        new ExpressionBuilder().createDataExpression("b", Integer.class.getName())));

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        checkPendingTaskInParallel(loopMax, loopMax, processDefinition, processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeAMultiInstanceParallelWithLoopCardinalityMoreThan20() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceParallelWithMaxIteration", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addIntegerData("a", new ExpressionBuilder().createConstantIntegerExpression(16));
        builder.addIntegerData("b", new ExpressionBuilder().createConstantIntegerExpression(14));
        final int loopMax = 30;
        builder.addUserTask("step1", delivery).addMultiInstance(
                false,
                new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceParallelWithLoopCardinalityUsingGroovyAndData", "a + b",
                        Integer.class.getName(), new ExpressionBuilder().createDataExpression("a", Integer.class.getName()),
                        new ExpressionBuilder().createDataExpression("b", Integer.class.getName())));

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        checkPendingTaskInParallel(loopMax, loopMax, processDefinition, processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeAMultiInstanceParallelWithCompletionCondition() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder()
                .createNewInstance("executeAMultiInstanceParallelWithCompletionCondition", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        final int loopMax = 3;
        builder.addUserTask("step1", delivery)
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(3))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceParallelWithCompletionCondition",
                                ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES.getEngineConstantName() + " >= 2 ", Boolean.class.getName(),
                                new ExpressionBuilder().createEngineConstant(ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES)));

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskInParallel(loopMax, 2, processDefinition, processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void remainingInstancesAreAbortedAfterCompletionCondition() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder()
                .createNewInstance("remainingInstancesAreAbortedAfterCompletionCondition", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        final int loopMax = 3;
        builder.addUserTask("step1", delivery)
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(loopMax))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("remainingInstancesAreAbortedAfterCompletionCondition",
                                ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES.getEngineConstantName() + " == 1 ", Boolean.class.getName(),
                                new ExpressionBuilder().createEngineConstant(ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES)));

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskInParallel(loopMax, 1, processDefinition, processInstance);
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
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceSequentialWithCompletionCondition",
                "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addUserTask("step1", delivery)
                .addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(20))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceSequentialWithCompletionCondition",
                                ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES.getEngineConstantName() + " >= 15 ", Boolean.class.getName(),
                                new ExpressionBuilder().createEngineConstant(ExpressionConstants.NUMBER_OF_COMPLETED_INSTANCES)));

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskSequentially(15, processDefinition, processInstance, true);
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
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceSequentialWithCompletionConditionTrue",
                "1.0");
        builder.addActor(delivery).addDescription("Deliver all day and night long");
        builder.addUserTask("step1", delivery)
                .addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(4))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceSequentialWithCompletionConditionTrue", "true",
                                Boolean.class.getName()));
        builder.addUserTask("step2", delivery).addUserTask("step3", delivery).addTransition("step1", "step2").addTransition("step2", "step3");

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskSequentially(3, processDefinition, processInstance, true);
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
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAMultiInstanceSequentialWithCompletionConditionTrue",
                "1.0");
        builder.addActor(delivery).addDescription("Deliver all day and night long");
        builder.addUserTask("step1", delivery)
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(4))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("executeAMultiInstanceSequentialWithCompletionConditionTrue", "true",
                                Boolean.class.getName()));
        builder.addAutomaticTask("step2").addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 4, john).waitUntil());
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask1 = pendingTasks.get(0);
        assignAndExecuteStep(pendingTask1, john.getId());

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
        final String delivery = "Delivery men";
        final int loopMax = 3;
        final int numberOfExecutedActivities = loopMax + 1;

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeTaskAfterMultiInstanceSequential", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addUserTask("step1", delivery).addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        builder.addAutomaticTask("step2").addUserTask("step3", delivery).addTransition("step1", "step2").addTransition("step2", "step3");

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskSequentially(numberOfExecutedActivities, processDefinition, processInstance, true);
        assertTrue(waitProcessToFinishAndBeArchived(processInstance));
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
        final String delivery = "Delivery men";
        final int loopMax = 3;

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeTaskAfterMultiInstanceSequentialAuto", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addAutomaticTask("step1").addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        builder.addAutomaticTask("step2").addUserTask("step3", delivery).addTransition("step1", "step2").addTransition("step2", "step3");

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil());
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask = pendingTasks.get(0);

        assignAndExecuteStep(pendingTask, john.getId());
        assertTrue(waitProcessToFinishAndBeArchived(processInstance));
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
        final String delivery = "Delivery men";
        final int loopMax = 3;
        final int numberOfTask = loopMax;
        final int numberOfTaskToComplete = 3;

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeTaskAfterMultiInstanceSequential", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addUserTask("step1", delivery).addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        builder.addAutomaticTask("step2").addAutomaticTask("step3").addTransition("step1", "step2").addTransition("step2", "step3");

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskInParallel(numberOfTask, numberOfTaskToComplete, processDefinition, processInstance);
        assertTrue(waitProcessToFinishAndBeArchived(processInstance));
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
        final String delivery = "Delivery men";
        final int loopMax = 3;

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeTaskAfterMultiInstanceSequentialAuto", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addAutomaticTask("step1").addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        builder.addAutomaticTask("step2").addUserTask("step3", delivery).addTransition("step1", "step2").addTransition("step2", "step3");

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil());
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask = pendingTasks.get(0);

        assignAndExecuteStep(pendingTask, john.getId());
        assertTrue(waitProcessToFinishAndBeArchived(processInstance));
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
        final String panel = "Panel";
        final int loopMax = 3;

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeMultiInstanceWithActors", "1.0");
        builder.addActor(panel).addDescription("Survey");
        builder.addUserTask("step1", panel).addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        builder.addAutomaticTask("step2").addTransition("step1", "step2");

        final List<User> listUsers = new ArrayList<User>();
        final List<String> listActors = new ArrayList<String>();
        listUsers.add(john);
        listActors.add(panel);
        listUsers.add(jack);
        listActors.add(panel);
        listUsers.add(jenny);
        listActors.add(panel);

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), listActors, listUsers);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 3, john).waitUntil());
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 3, jack).waitUntil());
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 3, jenny).waitUntil());

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

        assertTrue(waitProcessToFinishAndBeArchived(processInstance));
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
        final String panel = "Panel";
        final int loopMax = 3;

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeMultiInstanceWithActors", "1.0");
        builder.addActor(panel).addDescription("Survey");
        builder.addUserTask("step1", panel).addMultiInstance(true, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        builder.addAutomaticTask("step2").addTransition("step1", "step2");

        final List<User> listUsers = new ArrayList<User>();
        final List<String> listActors = new ArrayList<String>();
        listUsers.add(john);
        listActors.add(panel);
        listUsers.add(jack);
        listActors.add(panel);
        listUsers.add(jenny);
        listActors.add(panel);

        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), listActors, listUsers);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // Execute task of multi-instance for John
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil());
        final List<HumanTaskInstance> pendingTasks1 = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask1 = pendingTasks1.get(0);
        assignAndExecuteStep(pendingTask1, john.getId());

        // Execute task of multi-instance for Jack
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, jack).waitUntil());
        final List<HumanTaskInstance> pendingTasks2 = getProcessAPI().getPendingHumanTaskInstances(jack.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask2 = pendingTasks2.get(0);
        assignAndExecuteStep(pendingTask2, jack.getId());

        // Execute task of multi-instance for Jenny
        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 1, jenny).waitUntil());
        final List<HumanTaskInstance> pendingTasks3 = getProcessAPI().getPendingHumanTaskInstances(jenny.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask3 = pendingTasks3.get(0);
        assignAndExecuteStep(pendingTask3, jenny.getId());

        assertTrue(waitProcessToFinishAndBeArchived(processInstance));
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
        final String delivery = "Delivery men";
        final int loopMax = 3;

        // Sub-process definition
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("SubProcessInAMultiInstance", "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long").addAutomaticTask("step1").addAutomaticTask("step2")
                .addTransition("step1", "step2");

        final ProcessDefinition subProcess = deployAndEnableWithActor(builder.done(), delivery, john);

        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression(subProcess.getName());
        final Expression targetProcessVersionExpr = new ExpressionBuilder().createConstantStringExpression(subProcess.getVersion());

        final ProcessDefinitionBuilder builderProc = new ProcessDefinitionBuilder().createNewInstance("executeMultiInstanceSequentialWithSubProcess", "1.1");
        builderProc.addActor(delivery).addDescription("Delivery all day and night long").addStartEvent("start")
                .addCallActivity("callActivity", targetProcessNameExpr, targetProcessVersionExpr)
                .addMultiInstance(false, new ExpressionBuilder().createConstantIntegerExpression(loopMax));
        builderProc.addAutomaticTask("step3").addEndEvent("end").addTransition("start", "callActivity").addTransition("callActivity", "step3")
                .addTransition("step3", "end");

        final ProcessDefinition mainProcess = deployAndEnableWithActor(builderProc.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(mainProcess.getId());

        assertTrue(waitProcessToFinishAndBeArchived(processInstance));
        disableAndDeleteProcess(mainProcess);
        disableAndDeleteProcess(subProcess);
    }

    private void withMultiInstanceAttribute(final String condition, final ExpressionConstants expressionConstant, final int numberOfExecutedActivities)
            throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeAStandardLoopUserTask" + condition, "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addIntegerData("a", new ExpressionBuilder().createConstantIntegerExpression(1));
        builder.addIntegerData("b", new ExpressionBuilder().createConstantIntegerExpression(2));
        builder.addUserTask("step1", delivery)
                .addMultiInstance(
                        true,
                        new ExpressionBuilder().createGroovyScriptExpression("testWithMultiInstanceAttribute1", "a + b", Integer.class.getName(),
                                new ExpressionBuilder().createDataExpression("a", Integer.class.getName()),
                                new ExpressionBuilder().createDataExpression("b", Integer.class.getName())))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("testWithMultiInstanceAttribute2", expressionConstant.getEngineConstantName()
                                + condition, Boolean.class.getName(), new ExpressionBuilder().createEngineConstant(expressionConstant)));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskSequentially(numberOfExecutedActivities, processDefinition, processInstance, true);
        disableAndDeleteProcess(processDefinition);
    }

    private void withParallelMultiInstanceAttribute(final String condition, final ExpressionConstants expressionConstant, final int numberOfExecutedActivities,
            final int numberOfTaskToComplete) throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeParallelUserTask" + condition, "1.0");
        builder.addActor(delivery).addDescription("Delivery all day and night long");
        builder.addIntegerData("a", new ExpressionBuilder().createConstantIntegerExpression(1));
        builder.addIntegerData("b", new ExpressionBuilder().createConstantIntegerExpression(2));
        builder.addUserTask("step1", delivery)
                .addMultiInstance(
                        false,
                        new ExpressionBuilder().createGroovyScriptExpression("testWithMultiInstanceAttribute1", "a + b", Integer.class.getName(),
                                new ExpressionBuilder().createDataExpression("a", Integer.class.getName()),
                                new ExpressionBuilder().createDataExpression("b", Integer.class.getName())))
                .addCompletionCondition(
                        new ExpressionBuilder().createGroovyScriptExpression("testWithMultiInstanceAttribute2", expressionConstant.getEngineConstantName()
                                + condition, Boolean.class.getName(), new ExpressionBuilder().createEngineConstant(expressionConstant)));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), delivery, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        checkPendingTaskInParallel(numberOfExecutedActivities, numberOfTaskToComplete, processDefinition, processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    private void checkPendingTaskSequentially(final int loopMax, final ProcessDefinition processDefinition, final ProcessInstance processInstance,
            final boolean mustBeFinished) throws Exception {
        for (int i = 0; i < loopMax; i++) {
            final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 50, 10000, false, 1, john);
            if (!checkNbPendingTaskOf.waitUntil()) {
                fail("failed on iteration " + i + " expected " + 1 + " pending task but was " + checkNbPendingTaskOf.getPendingHumanTaskInstances().size());
            }
            final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
            final HumanTaskInstance pendingTask = pendingTasks.get(0);

            assignAndExecuteStep(pendingTask, john.getId());
        }
        Thread.sleep(200);
        if (mustBeFinished) {
            assertTrue("There was a new pending task but no more was expected", new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 0, john).waitUntil());
            waitForProcessToFinish(processInstance);
        }
    }

    private void checkPendingTaskInParallel(final int numberOfTask, final int numberOfTaskToCompleteMI, final ProcessDefinition processDefinition,
            final ProcessInstance processInstance) throws Exception {
        for (int i = 0; i < numberOfTaskToCompleteMI; i++) {
            final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, numberOfTask - i, john);
            if (!checkNbPendingTaskOf.waitUntil()) {
                fail("failed on iteration " + i + " expected " + (numberOfTask - i) + " pending task but was "
                        + checkNbPendingTaskOf.getPendingHumanTaskInstances().size());
            }
            final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
            final HumanTaskInstance pendingTask = pendingTasks.get(0);

            assignAndExecuteStep(pendingTask, john.getId());
        }
        Thread.sleep(200);
        assertTrue("There was still pending task but no more was expected", new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, false, 0, john).waitUntil());
        waitForProcessToFinish(processInstance);
        final int nbAbortedActivities = numberOfTask - numberOfTaskToCompleteMI;
        checkNbOfArchivedActivities(processInstance, nbAbortedActivities);
    }

    @Test
    public void multiInstanceVoteUseCase() throws Exception {
        final String panel = "Panel";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("executeMultiInstanceWithSeveralActors", "1.0");
        builder.addActor(panel).addDescription("Survey");
        final String loopDataInputName = "listOfUserId";
        final String exprListUserIds = "[" + john.getId() + "l," + jack.getId() + "l," + jenny.getId() + "l]";
        builder.addData(loopDataInputName, List.class.getName(),
                new ExpressionBuilder().createGroovyScriptExpression("createListUserId", exprListUserIds, List.class.getName()));
        final UserTaskDefinitionBuilder userTaskBuilder = new UserTaskDefinitionBuilder(builder, (FlowElementContainerDefinitionImpl) builder.getProcess()
                .getProcessContainer(), "step1", panel);
        userTaskBuilder.addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0").addInput("userId",
                new ExpressionBuilder().createDataExpression("userIdValue", Long.class.getName()));
        userTaskBuilder.addData("userIdValue", Long.class.getName(), null);

        userTaskBuilder.addMultiInstance(false, loopDataInputName).addDataInputItemRef("userIdValue");

        builder.addAutomaticTask("step2").addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployProcessWithTestFilter(panel, builder, "TestFilter");

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        waitForUserTask("step1", processInstance);
        waitForUserTask("step1", processInstance);
        waitForUserTask("step1", processInstance);
        assertEquals(1, getProcessAPI().getNumberOfPendingHumanTaskInstances(jack.getId()));
        assertEquals(1, getProcessAPI().getNumberOfPendingHumanTaskInstances(john.getId()));
        assertEquals(1, getProcessAPI().getNumberOfPendingHumanTaskInstances(jenny.getId()));

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployProcessWithTestFilter(final String actorName, final ProcessDefinitionBuilder designProcessDefinition,
            final String filterName) throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                designProcessDefinition.done());
        final List<BarResource> impl = generateFilterImplementations(filterName);
        for (final BarResource barResource : impl) {
            businessArchiveBuilder.addUserFilters(barResource);
        }
        final List<BarResource> generateFilterDependencies = new ArrayList<BarResource>(1);
        final byte[] data = IOUtil.generateJar(TestFilter.class);
        generateFilterDependencies.add(new BarResource("TestFilter.jar", data));

        for (final BarResource barResource : generateFilterDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchiveBuilder.done());
        addMappingOfActorsForUser(actorName, john.getId(), processDefinition);
        addMappingOfActorsForUser(actorName, jack.getId(), processDefinition);
        addMappingOfActorsForUser(actorName, jenny.getId(), processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    private List<BarResource> generateFilterImplementations(final String filterName) throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(1);
        final InputStream inputStream = TestConnector.class.getClassLoader().getResourceAsStream("org/bonitasoft/engine/filter/user/" + filterName + ".impl");
        final byte[] data = IOUtil.getAllContentFrom(inputStream);
        inputStream.close();
        resources.add(new BarResource("TestFilter.impl", data));
        return resources;
    }

}
