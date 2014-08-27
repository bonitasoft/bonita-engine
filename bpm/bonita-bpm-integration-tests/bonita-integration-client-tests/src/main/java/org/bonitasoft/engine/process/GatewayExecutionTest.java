package org.bonitasoft.engine.process;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.PlatformAPI;
import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.bpm.flownode.GatewayInstance;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TimerType;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.InvalidExpressionException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.PlatformSession;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.WaitUntil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GatewayExecutionTest extends CommonAPITest {

    private Expression trueExpression;

    private Expression falseExpression;

    private User user;

    @After
    public void afterTest() throws BonitaException {
        deleteUser(user);
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();
        user = createUser(USERNAME, PASSWORD);
    }

    @Test
    public void archiveGatewayInstance() throws Exception {
        createTrueAndFalseExpression();
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("My_Process", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME).addDescription("description");
        builder.addAutomaticTask("step1");
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addUserTask("step3", ACTOR_NAME);
        builder.addUserTask("step4", ACTOR_NAME);
        builder.addGateway("gatewayOne", GatewayType.INCLUSIVE)
        .addDisplayDescriptionAfterCompletion(new ExpressionBuilder().createConstantStringExpression("description after completion"))
        .addDisplayName(new ExpressionBuilder().createConstantStringExpression("display name"));
        builder.addTransition("step1", "gatewayOne");
        builder.addTransition("gatewayOne", "step2", falseExpression);
        builder.addTransition("gatewayOne", "step3", falseExpression)
        .addDefaultTransition("gatewayOne", "step4");
        final DesignProcessDefinition designProcessDefinition = builder.getProcess();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // create gateway instance and transition instance and archive them
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        waitForStep("step4", processInstance, TestStates.getReadyState());

        // test gateway instance, gateway instance has been deleted after archive
        final SearchOptionsBuilder builder0 = new SearchOptionsBuilder(0, 10);
        builder0.filter(FlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, processInstance.getId());
        builder0.filter(FlowNodeInstanceSearchDescriptor.STATE_NAME, "completed");
        builder0.filter(FlowNodeInstanceSearchDescriptor.NAME, "gatewayOne");
        final SearchResult<FlowNodeInstance> searchResult0 = getProcessAPI().searchFlowNodeInstances(builder0.done());
        assertEquals(0, searchResult0.getCount());

        // search archive gateway instances:
        SearchOptionsBuilder builder1 = new SearchOptionsBuilder(0, 10);
        builder1.filter(ArchivedFlowNodeInstanceSearchDescriptor.FLOW_NODE_TYPE, "gate");
        builder1.filter(ArchivedFlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, processInstance.getId());
        final SearchResult<ArchivedFlowNodeInstance> searchResult1 = getProcessAPI().searchArchivedFlowNodeInstances(builder1.done());
        // we expect all normal gateway states to be archived:
        assertEquals(getProcessAPI().getSupportedStates(FlowNodeType.GATEWAY).size(), searchResult1.getCount());

        // check display name/description
        builder1 = new SearchOptionsBuilder(0, 10);
        builder1.filter(ArchivedFlowNodeInstanceSearchDescriptor.FLOW_NODE_TYPE, "gate");
        builder1.filter(ArchivedFlowNodeInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, processInstance.getId());
        builder1.filter(ArchivedFlowNodeInstanceSearchDescriptor.NAME, "gatewayOne");
        builder1.filter(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME, "completed");
        final ArchivedFlowNodeInstance gatewayOne = getProcessAPI().searchArchivedFlowNodeInstances(builder1.done()).getResult().get(0);
        // we expect all normal gateway states to be archived:
        assertThat(gatewayOne.getDisplayName()).isEqualTo("display name");
        assertThat(gatewayOne.getDisplayDescription()).isEqualTo("description after completion");

        // clean
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Parallel gateway with
     * 1 automatic step in input
     * 2 user tasks in output
     * > 2 tasks for john expected
     */
    @Test
    public void processWithParallelGatewaySplit() throws Exception {
        // test initialization (will be extracted in other methods
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addGateway("gateway1", GatewayType.PARALLEL)
        .addTransition("step1", "gateway1").addTransition("gateway1", "step2").addTransition("gateway1", "step3").getProcess();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        // we should have 2 elements ready:
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, true, 2, user);
        assertTrue("there was no 2 pending task for john", checkNbPendingTaskOf.waitUntil());
        final List<HumanTaskInstance> pendingHumanTaskInstances = checkNbPendingTaskOf.getPendingHumanTaskInstances();
        for (final HumanTaskInstance humanTaskInstance : pendingHumanTaskInstances) {
            getProcessAPI().assignUserTask(humanTaskInstance.getId(), user.getId());
        }
        for (final HumanTaskInstance humanTaskInstance : pendingHumanTaskInstances) {
            getProcessAPI().executeFlowNode(humanTaskInstance.getId());
        }
        assertTrue(waitForProcessToFinishAndBeArchived(processInstance));
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Parallel gateway with
     * 2 automatic step in input
     * 1 user task in output
     * > 1 task for john expected
     */
    @Test
    public void processWithParallelGatewayMerge() throws Exception {
        //given
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addAutomaticTask("step2").addAutomaticTask("step3").addUserTask("step4", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.PARALLEL).addGateway("gateway2", GatewayType.PARALLEL).addTransition("step1", "gateway1")
        .addTransition("gateway1", "step2").addTransition("gateway1", "step3").addTransition("step2", "gateway2").addTransition("step3", "gateway2")
        .addTransition("gateway2", "step4").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());

        // when
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        // then
        waitForUserTaskAndExecuteIt("step4", processInstance, user);
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Parallel gateway with
     * 2 automatic step in input
     * 1 user task in output
     * > 1 task for john expected
     */
    @Test
    public void processMultiMerge() throws Exception {
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addAutomaticTask("step2").addAutomaticTask("step3").addUserTask("step4", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.PARALLEL).addTransition("step1", "gateway1").addTransition("gateway1", "step2")
        .addTransition("gateway1", "step3").addTransition("step2", "step4").addTransition("step3", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step4", "step4");
    }

    /**
     * Parallel gateway with
     * used as a XOR Gateway
     * Exception expected
     */
    @Test(expected = InvalidProcessDefinitionException.class)
    public void parallelWithConditionalTransitions() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME).addAutomaticTask("step1")
        .addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addGateway("gateway1", GatewayType.PARALLEL)
        .addTransition("step1", "gateway1").addTransition("gateway1", "step2", trueExpression).addTransition("gateway1", "step3", falseExpression)
        .getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step2", "step3");
    }

    /*
     * Exclusive gateway with
     * unconditionnal output transitions
     * Expected : step 2
     */
    @Test
    public void exclusiveWithUnconditionalTransitions() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_exclusive_gateway", PROCESS_VERSION).addActor(ACTOR_NAME).addAutomaticTask("step1")
        .addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addGateway("gateway1", GatewayType.EXCLUSIVE)
        .addTransition("step1", "gateway1").addTransition("gateway1", "step2").addTransition("gateway1", "step3").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step2");
    }

    /*
     * Inclusive gateway with
     * unconditionnal output transitions
     * Expected : step2, step3
     */
    @Test
    public void inclusiveWithUnconditionalTransitions() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_inclusive_gateway", PROCESS_VERSION).addActor(ACTOR_NAME).addAutomaticTask("step1")
        .addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addGateway("gateway1", GatewayType.INCLUSIVE)
        .addTransition("step1", "gateway1").addTransition("gateway1", "step2").addTransition("gateway1", "step3").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step2", "step3");
    }

    /**
     * Linear process
     * with auto task -> user task -> auto task -> user task
     * 1 task pending + 1 task pending for John
     */
    @Test
    public void linearProcessWith2UserTasks() throws Exception {
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME).addAutomaticTask("step1")
        .addUserTask("step2", ACTOR_NAME).addAutomaticTask("step3").addUserTask("step4", ACTOR_NAME).addTransition("step1", "step2")
        .addTransition("step2", "step3").addTransition("step3", "step4").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        // we should have 2 elements ready:
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 300, 5000, true, 1, user);
        assertTrue("there was no pending task for john (expected step2)", checkNbPendingTaskOf.waitUntil());
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        final HumanTaskInstance humanTaskInstance = pendingTasks.get(0);
        assertEquals("step2", humanTaskInstance.getName());
        assignAndExecuteStep(humanTaskInstance, user.getId());

        assertTrue("there was no pending task for john (expected step4)", checkNbPendingTaskOf.waitUntil());
        final List<HumanTaskInstance> pendingTasks2 = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, null);
        assertEquals(1, pendingTasks2.size());
        final HumanTaskInstance humanTaskInstance2 = pendingTasks2.get(0);
        assertEquals("step4", humanTaskInstance2.getName());
        assignAndExecuteStep(humanTaskInstance2, user.getId());

        assertTrue(waitForProcessToFinishAndBeArchived(processInstance));
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Exclusive gateway with
     * 1 automatic step in input
     * 1 user task in output
     * > 1 task for john expected
     */
    @Test
    public void processWithExclusiveGatewayWith1InputAnd1Output() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addGateway("gateway1", GatewayType.EXCLUSIVE).addTransition("step1", "gateway1")
        .addTransition("gateway1", "step2", trueExpression).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        // test execution
        getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        // we should have 2 elements ready:
        waitForPendingTasks(user, 1);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void processWithExclusiveAndInclusive() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME).addStartEvent("start")
        .addUserTask("step1", ACTOR_NAME).addGateway("para1", GatewayType.PARALLEL).addAutomaticTask("step2")
        .addGateway("inclu1", GatewayType.INCLUSIVE).addAutomaticTask("step3").addAutomaticTask("step4").addGateway("inclu2", GatewayType.INCLUSIVE)
        .addEndEvent("end").addTerminateEventTrigger().addGateway("para2", GatewayType.PARALLEL).addTransition("start", "step1")
        .addTransition("step1", "para1").addTransition("para1", "step2").addTransition("para1", "inclu1")
        .addTransition("inclu1", "step3", new ExpressionBuilder().createConstantBooleanExpression(true))
        .addTransition("inclu1", "step4", new ExpressionBuilder().createConstantBooleanExpression(false)).addTransition("step4", "inclu2")
        .addTransition("step3", "inclu2").addTransition("inclu2", "para2").addTransition("step2", "para2").addTransition("para2", "end").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance startProcess = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        waitForUserTaskAndExecuteIt("step1", startProcess, user);
        waitForProcessToFinish(startProcess);
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * exclusive gateway
     * 3 output:
     * step2 condition = true
     * step3 condition = true
     * step4 = default
     * expected step2
     */
    @Test
    public void exclusiveSplit1() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addUserTask("step4", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.EXCLUSIVE).addTransition("step1", "gateway1").addTransition("gateway1", "step2", trueExpression)
        .addTransition("gateway1", "step3", trueExpression).addDefaultTransition("gateway1", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step2");
    }

    /**
     * exclusive gateway
     * 3 output:
     * step3 condition = true
     * step2 condition = true
     * step4 = default
     * expected step3
     */
    @Test
    public void exclusiveSplit1bis() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addUserTask("step4", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.EXCLUSIVE).addTransition("step1", "gateway1").addTransition("gateway1", "step3", trueExpression)
        .addTransition("gateway1", "step2", trueExpression).addDefaultTransition("gateway1", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step3");
    }

    /**
     * exclusive gateway
     * 3 output:
     * step2 condition = false
     * step3 condition = true
     * step4 = default
     * expected step3
     */
    @Test
    public void exclusiveSplit2() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addUserTask("step4", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.EXCLUSIVE).addTransition("step1", "gateway1").addTransition("gateway1", "step2", falseExpression)
        .addTransition("gateway1", "step3", trueExpression).addDefaultTransition("gateway1", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step3");
    }

    /**
     * exclusive gateway
     * 3 output:
     * step2 condition = false
     * step3 condition = false
     * step4 = default
     * expected step4
     */
    @Test
    public void exclusiveSplit3() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addUserTask("step4", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.EXCLUSIVE).addTransition("step1", "gateway1").addTransition("gateway1", "step2", falseExpression)
        .addTransition("gateway1", "step3", falseExpression).addDefaultTransition("gateway1", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step4");
    }

    /**
     * exclusive gateway
     * 3 output:
     * step2 condition = false
     * step3 condition = false
     * expected exception: no default gateway is defined
     */
    @Test
    public void exclusiveSplitWithNoDefaultTransition() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addGateway("gateway1", GatewayType.EXCLUSIVE)
        .addTransition("step1", "gateway1").addTransition("gateway1", "step2", falseExpression).addTransition("gateway1", "step3", falseExpression)
        .getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "");
    }

    /**
     * exclusive gateway
     * 1 output:
     * step2 condition = false
     * gateway fail
     * fix and restart gateway
     * expected step2 ready
     */
    @Test
    public void exclusiveSplitWithNoDefaultTransitionFailThenRestart() throws Exception {
        createTrueAndFalseExpression();
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process_with_parallel_gateway",
                PROCESS_VERSION);
        processDefinitionBuilder.addActor("actor").addAutomaticTask("step1").addUserTask("step2", "actor").addGateway("gateway1", GatewayType.EXCLUSIVE)
        .addTransition("step1", "gateway1")
        .addTransition("gateway1", "step2", new ExpressionBuilder().createDataExpression("condition", Boolean.class.getName())).getProcess();
        processDefinitionBuilder.addData("condition", Boolean.class.getName(), falseExpression);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), "actor", user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        // the gateway failed
        final FlowNodeInstance gateway = waitForFlowNodeInFailedState(processInstance);
        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 1).filter(FlowNodeInstanceSearchDescriptor.NAME, "gateway1").done();
        final SearchResult<FlowNodeInstance> searchFlowNodeInstances = getProcessAPI().searchFlowNodeInstances(searchOptions);
        final FlowNodeInstance flowNodeInstance = searchFlowNodeInstances.getResult().get(0);
        assertTrue(flowNodeInstance instanceof GatewayInstance);
        // retry the gateway
        getProcessAPI().retryTask(gateway.getId());
        waitForFlowNodeInFailedState(processInstance);
        // should still be in failed
        final SearchResult<FlowNodeInstance> searchFlowNodeInstances2 = getProcessAPI().searchFlowNodeInstances(searchOptions);
        assertEquals("failed", searchFlowNodeInstances2.getResult().get(0).getState());

        // change value of condition to make it work
        getProcessAPI().updateProcessDataInstance("condition", processInstance.getId(), true);
        // retry the gateway
        getProcessAPI().retryTask(gateway.getId());
        // we should have step2 ready
        waitForUserTask("step2", processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * inclusive gateway
     * 3 output:
     * step2 condition = true
     * step3 condition = true
     * step4 = default
     * expected step2 + step3
     */
    @Test
    public void inclusiveSplit1() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME).addAutomaticTask("step1")
        .addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addUserTask("step4", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.INCLUSIVE).addTransition("step1", "gateway1").addTransition("gateway1", "step2", trueExpression)
        .addTransition("gateway1", "step3", trueExpression).addDefaultTransition("gateway1", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step2", "step3");
    }

    /**
     * inclusive gateway
     * 3 output:
     * step2 condition = false
     * step3 condition = true
     * step4 = default
     * expected step3
     */
    @Test
    public void inclusiveSplit2() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addUserTask("step4", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.INCLUSIVE).addTransition("step1", "gateway1").addTransition("gateway1", "step2", falseExpression)
        .addTransition("gateway1", "step3", trueExpression).addDefaultTransition("gateway1", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step3");
    }

    /**
     * inclusive gateway
     * 3 output:
     * step2 condition = false
     * step3 condition = true
     * step4 = default
     * expected step3
     */
    @Test
    public void inclusiveSplit2WithDataAsCondition() throws Exception {
        createTrueAndFalseExpression();
        final Expression trueData = new ExpressionBuilder().createDataExpression("trueData", Boolean.class.getName());
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process_with_parallel_gateway",
                PROCESS_VERSION);
        processDefinitionBuilder.addBooleanData("trueData", trueExpression);
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder
                .addActor(ACTOR_NAME)

                .addAutomaticTask("step1")
                .addUserTask("step2", ACTOR_NAME)
                .addUserTask("step3", ACTOR_NAME)
                .addUserTask("step4", ACTOR_NAME)
                .addGateway("gateway1", GatewayType.INCLUSIVE)
                .addTransition("step1", "gateway1")
                .addTransition(
                        "gateway1",
                        "step2",
                        new ExpressionBuilder().createGroovyScriptExpression("inclusiveSplit2WithDataAsCondition", "!trueData", Boolean.class.getName(),
                                Arrays.asList(trueData))).addTransition("gateway1", "step3", trueData).addDefaultTransition("gateway1", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step3");
    }

    /**
     * exclusive gateway
     * 3 output:
     * step2 condition = false
     * step3 condition = false
     * step4 = default
     * expected step4
     */
    @Test
    public void inclusiveSplit3() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addUserTask("step4", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.INCLUSIVE).addTransition("step1", "gateway1").addTransition("gateway1", "step2", falseExpression)
        .addTransition("gateway1", "step3", falseExpression).addDefaultTransition("gateway1", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step4");
    }

    /**
     * exclusive gateway
     * 3 output:
     * step2 condition = false
     * step3 condition = false
     * expected Exception: no default transition on gateway
     */
    @Test
    public void inclusiveSplitWithNoDefaultTransition() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addGateway("gateway1", GatewayType.INCLUSIVE)
        .addTransition("step1", "gateway1").addTransition("gateway1", "step2", falseExpression).addTransition("gateway1", "step3", falseExpression)
        .getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "");
    }

    /**
     * Inclusive then Inclusive Gateway
     * expected step5
     */
    @Test
    public void inclusiveSplitThenInclusiveMerge() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME).addAutomaticTask("step1")
        .addAutomaticTask("step2").addAutomaticTask("step3").addUserTask("step4", ACTOR_NAME).addUserTask("step5", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.INCLUSIVE).addGateway("gateway2", GatewayType.INCLUSIVE).addTransition("step1", "gateway1")
        .addTransition("gateway1", "step2", trueExpression).addTransition("gateway1", "step3", trueExpression)
        .addDefaultTransition("gateway1", "step4").addTransition("step2", "gateway2").addTransition("step3", "gateway2")
        .addTransition("step4", "gateway2").addTransition("gateway2", "step5").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step5");
    }

    /**
     * Parallel then Inclusive Gateway
     * step 1 -> parallel gateway1 -> ( step 2, step3 ) -> inclusive gateway2 -> step4
     * Dynamic expected : step2 & step3 pending, exec step2, step3 pending , exec step3, step4 pending , exec step4, end process.
     */
    @Test
    public void parallelSplitThenInclusiveMerge() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addUserTask("step4", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.PARALLEL).addGateway("gateway2", GatewayType.INCLUSIVE).addTransition("step1", "gateway1")
        .addTransition("gateway1", "step2").addTransition("gateway1", "step3").addTransition("step2", "gateway2").addTransition("step3", "gateway2")
        .addTransition("gateway2", "step4").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());
        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        // we should have 2 elements ready:
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 50, 5000, true, 2, user);
        assertTrue("there was no pending task for john (expected step2 and step3)", checkNbPendingTaskOf.waitUntil());
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(2, pendingTasks.size());
        final HumanTaskInstance step2 = pendingTasks.get(0);
        assertEquals("step2", step2.getName());
        final HumanTaskInstance step3 = pendingTasks.get(1);
        assertEquals("step3", step3.getName());
        assignAndExecuteStep(step2, user.getId());
        final CheckNbPendingTaskOf checkNbPendingTaskOf2 = new CheckNbPendingTaskOf(getProcessAPI(), 50, 2000, true, 2, user);
        assertFalse("there was no pending task for john (expected step3)", checkNbPendingTaskOf2.waitUntil());
        assignAndExecuteStep(step3, user.getId());
        waitForUserTask("step4", processInstance);
        final List<HumanTaskInstance> pendingTasks2 = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        final HumanTaskInstance step4 = pendingTasks2.get(0);
        assertEquals("step4", step4.getName());
        assignAndExecuteStep(step4, user.getId());
        assertTrue(waitForProcessToFinishAndBeArchived(processInstance));
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Notify inclusive gateway that a branch died
     * inclusive1 -> step1+step2 -> inclusive2 -> step3
     * complete step2
     * step1 is interrupted by a boundary
     * exception path of boundary is finished
     * expected: step3 is active because the inclusive2 was notified
     */
    @Test
    public void notifyInclusiveGatewayThatABranchDied() throws Exception {
        createTrueAndFalseExpression();
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("NotifiedBranchDeadProcess", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addGateway("inclusive1", GatewayType.INCLUSIVE);
        builder.addUserTask("step1", ACTOR_NAME).addBoundaryEvent("signal", true).addSignalEventTrigger("bip");
        builder.addUserTask("exceptionStep", ACTOR_NAME);
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addGateway("inclusive2", GatewayType.INCLUSIVE);
        builder.addUserTask("step3", ACTOR_NAME);
        builder.addTransition("start", "inclusive1");
        builder.addTransition("inclusive1", "step1");
        builder.addTransition("inclusive1", "step2");
        builder.addTransition("step1", "inclusive2");
        builder.addTransition("signal", "exceptionStep");
        builder.addTransition("step2", "inclusive2");
        builder.addTransition("inclusive2", "step3");
        final DesignProcessDefinition designProcessDefinition = builder.getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        // execute step2
        waitForUserTask("step1", processInstance);
        final ActivityInstance step2 = waitForUserTask("step2", processInstance);
        assignAndExecuteStep(step2, user.getId());

        // send signal to trigger boundary
        getProcessAPI().sendSignal("bip");
        // wait and execute exceptionStep
        waitForUserTaskAndExecuteIt("exceptionStep", processInstance, user);

        // step3 should be ready
        waitForUserTaskAndExecuteIt("step3", processInstance, user);
        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Notify inclusive gateway that a branch died
     * inclusive1 -> step1+step2 -> inclusive2 -> step3
     * complete step2
     * step1 is interrupted by a boundary
     * exception path of boundary is finished
     * expected: step3 is active because the inclusive2 was notified
     */
    @Test
    public void inclusiveGatewayWithNonInterruptingBoundary() throws Exception {
        createTrueAndFalseExpression();
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("NotifiedBranchDeadProcess", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addGateway("inclusive1", GatewayType.INCLUSIVE);
        builder.addUserTask("step1", ACTOR_NAME).addBoundaryEvent("timer", false)
        .addTimerEventTriggerDefinition(TimerType.DURATION, new ExpressionBuilder().createConstantLongExpression(1));
        builder.addUserTask("exceptionStep", ACTOR_NAME);
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addGateway("inclusive2", GatewayType.INCLUSIVE);
        builder.addUserTask("step3", ACTOR_NAME);
        builder.addTransition("start", "inclusive1");
        builder.addTransition("inclusive1", "step1");
        builder.addTransition("inclusive1", "step2");
        builder.addTransition("step1", "inclusive2");
        builder.addTransition("timer", "exceptionStep");
        builder.addTransition("step2", "inclusive2");
        builder.addTransition("inclusive2", "step3");
        final DesignProcessDefinition designProcessDefinition = builder.getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());

        // execute step2
        final ActivityInstance step1 = waitForUserTask("step1", processInstance);
        final ActivityInstance step2 = waitForUserTask("step2", processInstance);
        assignAndExecuteStep(step2, user.getId());

        waitForUserTask("exceptionStep", processInstance);
        // step1 should still be here
        assignAndExecuteStep(step1, user.getId());

        // step3 should be ready event if exceptionStep is not
        waitForUserTask("step3", processInstance);

        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Inclusive then Exclusive Gateway
     * Expected : step5
     */
    @Test
    public void inclusiveSplitThenExclusiveMerge() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME).addAutomaticTask("step1")
        .addAutomaticTask("step2").addAutomaticTask("step3").addAutomaticTask("step4").addUserTask("step5", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.INCLUSIVE).addGateway("gateway2", GatewayType.EXCLUSIVE).addTransition("step1", "gateway1")
        .addTransition("gateway1", "step2", trueExpression).addTransition("gateway1", "step3", trueExpression)
        .addDefaultTransition("gateway1", "step4").addTransition("step2", "gateway2").addTransition("step3", "gateway2")
        .addTransition("step4", "gateway2").addTransition("gateway2", "step5").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        // we should have 2 elements ready:
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 50, 10000, true, 2, user);
        checkNbPendingTaskOf.waitUntil();
        // assertTrue("# pending tasks does not match expected", expectedNbPendingTask);
        final List<HumanTaskInstance> pendingHumanTaskInstances = checkNbPendingTaskOf.getPendingHumanTaskInstances();
        assertEquals(2, pendingHumanTaskInstances.size());
        assertEquals("step5", pendingHumanTaskInstances.get(0).getName());
        assertEquals("step5", pendingHumanTaskInstances.get(1).getName());
        for (final HumanTaskInstance humanTaskInstance : pendingHumanTaskInstances) {
            getProcessAPI().assignUserTask(humanTaskInstance.getId(), user.getId());
        }
        for (final HumanTaskInstance humanTaskInstance : pendingHumanTaskInstances) {
            getProcessAPI().executeFlowNode(humanTaskInstance.getId());
        }
        assertTrue(waitForProcessToFinishAndBeArchived(processInstance));
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Exclusive then Exclusive Gateway
     * expected : step4
     */
    @Test
    public void exclusiveSplitThenExclusiveMerge() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME).addAutomaticTask("step1")
        .addAutomaticTask("step2").addAutomaticTask("step3").addAutomaticTask("step4").addUserTask("step5", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.EXCLUSIVE).addGateway("gateway2", GatewayType.EXCLUSIVE).addTransition("step1", "gateway1")
        .addTransition("gateway1", "step2", trueExpression).addTransition("gateway1", "step3", trueExpression)
        .addDefaultTransition("gateway1", "step4").addTransition("step2", "gateway2").addTransition("step3", "gateway2")
        .addTransition("step4", "gateway2").addTransition("gateway2", "step5").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        // we should have 2 elements ready:
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 50, 10000, true, 1, user);
        checkNbPendingTaskOf.waitUntil();
        // assertTrue("# pending tasks does not match expected", expectedNbPendingTask);
        final List<HumanTaskInstance> pendingHumanTaskInstances = checkNbPendingTaskOf.getPendingHumanTaskInstances();
        assertEquals(1, pendingHumanTaskInstances.size());
        assertEquals("step5", pendingHumanTaskInstances.get(0).getName());
        for (final HumanTaskInstance humanTaskInstance : pendingHumanTaskInstances) {
            getProcessAPI().assignUserTask(humanTaskInstance.getId(), user.getId());
        }
        for (final HumanTaskInstance humanTaskInstance : pendingHumanTaskInstances) {
            getProcessAPI().executeFlowNode(humanTaskInstance.getId());
        }
        assertTrue(waitForProcessToFinishAndBeArchived(processInstance));
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Parallel then Exclusive Gateway
     * expected : step5
     * Fails sometimes
     */
    @Test
    public void parallelSplitThenExclusiveMerge() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME).addAutomaticTask("step1")
        .addAutomaticTask("step2").addAutomaticTask("step3").addAutomaticTask("step4").addUserTask("step5", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.PARALLEL).addGateway("gateway2", GatewayType.EXCLUSIVE).addTransition("step1", "gateway1")
        .addTransition("gateway1", "step2").addTransition("gateway1", "step3").addTransition("gateway1", "step4").addTransition("step2", "gateway2")
        .addTransition("step3", "gateway2").addTransition("step4", "gateway2").addTransition("gateway2", "step5").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        // we should have 2 elements ready:
        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 50, 10000, true, 3, user);
        checkNbPendingTaskOf.waitUntil();
        final List<HumanTaskInstance> pendingHumanTaskInstances = checkNbPendingTaskOf.getPendingHumanTaskInstances();
        assertEquals(3, pendingHumanTaskInstances.size());
        assertEquals("step5", pendingHumanTaskInstances.get(0).getName());
        assertEquals("step5", pendingHumanTaskInstances.get(1).getName());
        assertEquals("step5", pendingHumanTaskInstances.get(2).getName());
        for (final HumanTaskInstance humanTaskInstance : pendingHumanTaskInstances) {
            getProcessAPI().assignUserTask(humanTaskInstance.getId(), user.getId());
        }
        for (final HumanTaskInstance humanTaskInstance : pendingHumanTaskInstances) {
            getProcessAPI().executeFlowNode(humanTaskInstance.getId());
        }
        assertTrue(waitForProcessToFinishAndBeArchived(processInstance));
        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Inclusive then Parallel Gateway
     * Expected : Process is blocked on gateway2 (parallel gateway waiting for inactive transition)
     */
    @Test
    public void inclusiveSplitThenParallelMerge() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME).addAutomaticTask("step1")
        .addAutomaticTask("step2").addAutomaticTask("step3").addUserTask("step4", ACTOR_NAME).addUserTask("step5", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.INCLUSIVE).addGateway("gateway2", GatewayType.PARALLEL).addTransition("step1", "gateway1")
        .addTransition("gateway1", "step2", trueExpression).addTransition("gateway1", "step3", trueExpression)
        .addDefaultTransition("gateway1", "step4").addTransition("step2", "gateway2").addTransition("step3", "gateway2")
        .addTransition("step4", "gateway2").addTransition("gateway2", "step5").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        waitForGateway("gateway2", processInstance, true);

        final List<HumanTaskInstance> pendingHumanTaskInstances = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10,
                ActivityInstanceCriterion.NAME_ASC);
        assertEquals(0, pendingHumanTaskInstances.size());

        disableAndDeleteProcess(processDefinition);
    }

    /**
     * Inclusive then Parallel Gateway
     * Expected : Process is blocked on gateway2 (parallel gateway waiting for inactive transition)
     */
    @Test
    public void exclusiveSplitThenParallelMerge() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Processy_exclusiveSplitThenParallelMerge", PROCESS_VERSION).addActor(ACTOR_NAME).addAutomaticTask("step1")
        .addAutomaticTask("step2").addUserTask("step3", ACTOR_NAME).addUserTask("step4", ACTOR_NAME).addUserTask("step5", ACTOR_NAME)
        .addGateway("gateway1", GatewayType.EXCLUSIVE).addGateway("gateway2", GatewayType.PARALLEL).addTransition("step1", "gateway1")
        .addTransition("gateway1", "step2", trueExpression).addTransition("gateway1", "step3", trueExpression)
        .addDefaultTransition("gateway1", "step4").addTransition("step2", "gateway2").addTransition("step3", "gateway2")
        .addTransition("step4", "gateway2").addTransition("gateway2", "step5").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // test execution
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        waitForGateway("gateway2", processInstance, true);

        final List<HumanTaskInstance> pendingHumanTaskInstances = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10,
                ActivityInstanceCriterion.NAME_ASC);
        assertEquals(0, pendingHumanTaskInstances.size());
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void activitySplitsToTrueTransitionEvenIfADefaultTransitionIsSet() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("splitActivity", PROCESS_VERSION)
                .addActor(ACTOR_NAME).addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME)
                .addUserTask("step4", ACTOR_NAME).addTransition("step1", "step2", trueExpression).addTransition("step1", "step3", falseExpression)
                .addDefaultTransition("step1", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step2");
    }

    @Test
    public void activitySplitsToDefaultTransitionWhenAllTransitionsAreConditionalAndFalse() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("splitActivity", PROCESS_VERSION)
                .addActor(ACTOR_NAME).addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME)
                .addUserTask("step4", ACTOR_NAME).addTransition("step1", "step2", falseExpression).addTransition("step1", "step3", falseExpression)
                .addDefaultTransition("step1", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step4");
    }

    @Test
    public void activitySplitsToDefaultTransitionAndNormalTransition() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("splitActivity", PROCESS_VERSION)
                .addActor(ACTOR_NAME).addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME)
                .addUserTask("step4", ACTOR_NAME).addTransition("step1", "step2").addTransition("step1", "step3", falseExpression)
                .addDefaultTransition("step1", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step2", "step4");
    }

    @Test
    public void activitySplitsToTrueConditionTransitionAndNormalTransition() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("splitActivity", PROCESS_VERSION)
                .addActor(ACTOR_NAME).addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME)
                .addUserTask("step4", ACTOR_NAME).addTransition("step1", "step2").addTransition("step1", "step3", trueExpression)
                .addDefaultTransition("step1", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step2", "step3");
    }

    @Test
    public void activitySplitsTo2NormalTransitions() throws Exception {
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("My_Process_with_parallel_gateway", PROCESS_VERSION).addActor(ACTOR_NAME)
        .addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).addTransition("step1", "step2")
        .addTransition("step1", "step3").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step2", "step3");
    }

    @Test
    public void activitySplitsTo2TrueTransitions() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("splitActivity", PROCESS_VERSION)
                .addActor(ACTOR_NAME).addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME)
                .addUserTask("step4", ACTOR_NAME).addTransition("step1", "step2", trueExpression).addTransition("step1", "step3", trueExpression)
                .addDefaultTransition("step1", "step4").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step2", "step3");
    }

    @Test
    public void activitySplitsWithNormalAndFalseConditionnal() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("splitActivity", PROCESS_VERSION)
                .addActor(ACTOR_NAME).addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME)
                .addTransition("step1", "step2").addTransition("step1", "step3", falseExpression).getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "");
    }

    @Test
    public void activitySplitsWith2FalseConditionnal() throws Exception {
        createTrueAndFalseExpression();
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("splitActivity", PROCESS_VERSION)
                .addActor(ACTOR_NAME).addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME)
                .addTransition("step1", "step2", falseExpression).addTransition("step1", "step3", falseExpression).getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "");
    }

    @Test
    public void inputAndOutputTransitionsOfInclusiveGatewayShouldBeEvaluatedToTrue() throws Exception {
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder()
        .createNewInstance("testInputTransitionOfInclusiveShouldBeEvaluatedToTrue", PROCESS_VERSION).addActor(ACTOR_NAME).addAutomaticTask("step1")
        .addGateway("gateway", GatewayType.INCLUSIVE).addUserTask("step2", ACTOR_NAME).addTransition("step1", "gateway")
        .addTransition("gateway", "step2").getProcess();
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, designProcessDefinition, "step2");
    }

    private void assertJohnHasGotTheExpectedTaskPending(final String actorName, final DesignProcessDefinition designProcessDefinition,
            final String... expected) throws Exception {
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, actorName, user);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDeploymentInfo.getProcessId());
        // we should have 2 elements ready:
        if (expected.length == 1 && expected[0].isEmpty()) {
            final WaitUntil waitUntil = new WaitUntil(DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT) {

                @Override
                protected boolean check() throws Exception {
                    final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
                    searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
                    searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.STATE_NAME, "failed");
                    final SearchResult<FlowNodeInstance> searchActivities = getProcessAPI().searchFlowNodeInstances(searchOptionsBuilder.done());
                    return searchActivities.getCount() == 1;
                }
            };
            assertTrue("Expected a task in fail state, there was none or more than one", waitUntil.waitUntil());
        } else {
            final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), DEFAULT_REPEAT_EACH, DEFAULT_TIMEOUT, true,
                    expected.length, user);
            // assertTrue("there was no pending task for john", checkNbPendingTaskOf.waitUntil());
            checkNbPendingTaskOf.waitUntil();
            assertEquals(expected.length, checkNbPendingTaskOf.getPendingHumanTaskInstances().size());
            final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, expected.length,
                    ActivityInstanceCriterion.NAME_ASC);
            for (int i = 0; i < expected.length; i++) {
                assertEquals(expected[i], pendingTasks.get(i).getName());// TODO check order
            }
            for (final HumanTaskInstance humanTaskInstance : pendingTasks) {
                getProcessAPI().assignUserTask(humanTaskInstance.getId(), user.getId());
            }
            for (final HumanTaskInstance humanTaskInstance : pendingTasks) {
                getProcessAPI().executeFlowNode(humanTaskInstance.getId());
            }
            assertTrue(waitForProcessToFinishAndBeArchived(processInstance));
        }
        disableAndDeleteProcess(processDefinition);
    }

    protected void createTrueAndFalseExpression() throws InvalidExpressionException {
        trueExpression = new ExpressionBuilder().createConstantBooleanExpression(true);
        falseExpression = new ExpressionBuilder().createConstantBooleanExpression(false);
    }

    @Cover(jira = "ENGINE-1520", classes = { GatewayInstance.class }, concept = BPMNConcept.GATEWAY, keywords = { "Gateway", "hitBy", "too many transitions" })
    @Test
    public void manyTransitionsToGateway() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("tooLong", "255");
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addGateway("sem", GatewayType.PARALLEL);
        for (int j = 0; j < 50; j++) {
            builder.addAutomaticTask("automatic-automatic-automatic-automatic" + j);
            builder.addTransition("start", "automatic-automatic-automatic-automatic" + j);
            builder.addTransition("automatic-automatic-automatic-automatic" + j, "sem");
        }
        builder.addUserTask("step", ACTOR_NAME);
        builder.addEndEvent("end");
        builder.addTransition("sem", "step");
        builder.addTransition("step", "end");
        final Set<TransitionDefinition> transitions = builder.done().getProcessContainer().getTransitions();
        for (final TransitionDefinition transitionDefinition : transitions) {
            System.out.println(transitionDefinition.getName());
        }
        assertJohnHasGotTheExpectedTaskPending(ACTOR_NAME, builder.getProcess(), "step");
    }

    @Test
    public void tooManyTokens() throws Exception {
        ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder();
        processDefinitionBuilder = processDefinitionBuilder.createNewInstance("tooManyTokens", PROCESS_VERSION);
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addStartEvent("Start");
        processDefinitionBuilder.addGateway("Gateway1", GatewayType.PARALLEL);
        processDefinitionBuilder.addUserTask("Step1", ACTOR_NAME);
        processDefinitionBuilder.addEndEvent("End1");
        processDefinitionBuilder.addUserTask("Step3", ACTOR_NAME);
        processDefinitionBuilder.addGateway("Gateway2", GatewayType.PARALLEL);
        processDefinitionBuilder.addUserTask("Step2", ACTOR_NAME);
        processDefinitionBuilder.addEndEvent("Terminate").addTerminateEventTrigger();
        processDefinitionBuilder.addTransition("Start", "Gateway1");
        processDefinitionBuilder.addTransition("Gateway1", "Step1");
        processDefinitionBuilder.addTransition("Gateway1", "Step3");
        processDefinitionBuilder.addTransition("Step3", "Gateway2");
        processDefinitionBuilder.addTransition("Step1", "Gateway2");
        processDefinitionBuilder.addTransition("Step1", "End1");
        processDefinitionBuilder.addTransition("Gateway2", "Step2");
        processDefinitionBuilder.addTransition("Step2", "Terminate");
        final DesignProcessDefinition designProcessDefinition = processDefinitionBuilder.getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("Step1", processInstance);
        final List<HumanTaskInstance> taskInstances = getProcessAPI().getHumanTaskInstances(processInstance.getId(), "Step1", 0, 1);
        assignAndExecuteStep(taskInstances.get(0).getId(), user.getId());
        waitForUserTaskAndExecuteIt("Step3", processInstance, user.getId());
        waitForTaskToFail(processInstance);
        // should also get the exception...not yet in the task
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Cover(classes = {}, concept = BPMNConcept.GATEWAY, keywords = { "restart", "Gateway", "Failed" }, jira = "BS-9367")
    public void restartNodeShouldNotRestartGatewaysWithNotFullfilledMergingCondition() throws Exception {
        final ProcessDefinitionBuilder processDesignBuilder = new ProcessDefinitionBuilder().createNewInstance("process_with_join_gateway", PROCESS_VERSION);
        processDesignBuilder.addStartEvent("goForIt");
        processDesignBuilder.addEndEvent("terminated");
        final DesignProcessDefinition designProcessDefinition = processDesignBuilder.addActor(ACTOR_NAME).addGateway("split", GatewayType.PARALLEL)
                .addAutomaticTask("autoTask").addUserTask("manualTask", ACTOR_NAME).addGateway("join", GatewayType.PARALLEL).addTransition("goForIt", "split")
                .addTransition("split", "autoTask").addTransition("split", "manualTask").addTransition("autoTask", "join").addTransition("manualTask", "join")
                .addTransition("join", "terminated").getProcess();

        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final long flowNodeInstanceId = waitForFlowNodeInState(processInstance, "join", TestStates.getExecutingState(), false);

        logoutOnTenant();
        final PlatformSession loginPlatform = loginOnPlatform();
        final PlatformAPI platformAPI = PlatformAPIAccessor.getPlatformAPI(loginPlatform);
        System.out.println("stopping node");
        platformAPI.stopNode();
        System.out.println("starting node");
        platformAPI.startNode();
        logoutOnPlatform(loginPlatform);
        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        // To be sure asynchronous restart works have been executed:
        Thread.sleep(200);

        final FlowNodeInstance joinGateway = getProcessAPI().getFlowNodeInstance(flowNodeInstanceId);
        assertEquals(TestStates.getExecutingState(), joinGateway.getState());

        disableAndDeleteProcess(processDefinition);
    }
}
