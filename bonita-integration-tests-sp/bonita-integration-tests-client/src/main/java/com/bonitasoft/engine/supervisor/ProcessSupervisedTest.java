package com.bonitasoft.engine.supervisor;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.expression.ExpressionType;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPIT;
import com.bonitasoft.engine.api.ProcessAPI;

public class ProcessSupervisedTest extends CommonAPISPIT {

    private List<User> users;

    private List<ProcessSupervisor> processSupervisors;

    private List<ProcessDefinition> processDefinitions;

    private final String failingTaskName = "failedStep";

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();

        users = new ArrayList<User>();
        users.add(createUser(USERNAME, PASSWORD));

        processDefinitions = new ArrayList<ProcessDefinition>();
        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression("receiveMessageProcess");
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addUserTask("userTask1", ACTOR_NAME).addSendTask("sendTask", "messageName",
                targetProcessExpression);
        processBuilder.addShortTextData("Application", null);
        processBuilder.addTransition("userTask1", "sendTask");
        processDefinitions.add(deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, users.get(0)));

        processSupervisors = new ArrayList<ProcessSupervisor>();
        processSupervisors.add(getProcessAPI().createProcessSupervisorForUser(processDefinitions.get(0).getId(), users.get(0).getId()));

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("processwithIntegerData", "1.0");
        processDefinitionBuilder.addAutomaticTask(failingTaskName).addIntegerData("intdata",
                new ExpressionBuilder().createExpression("d", "d", Integer.class.getName(), ExpressionType.TYPE_CONSTANT));
        processDefinitions.add(deployAndEnableProcess(processDefinitionBuilder.done()));
    }

    @After
    public void after() throws BonitaException, BonitaHomeNotSetException {
        deleteSupervisors(processSupervisors);
        disableAndDeleteProcess(processDefinitions);
        deleteUsers(users);
        logoutOnTenant();
    }

    @Cover(classes = { ProcessAPI.class, ArchivedFlowNodeInstance.class }, concept = BPMNConcept.SUPERVISOR, jira = "BS-8295", keywords = { "" })
    @Test
    public void searchArchivedFlowNodeInstancesSupervisedBy() throws Exception {
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(0).getId());
        final User user = users.get(0);
        waitForUserTaskAndExecuteIt(processInstance, "userTask1", user);
        waitForProcessToFinish(processInstance);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ArchivedActivityInstanceSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ArchivedFlowNodeInstance> searchArchivedFlowNodeInstancesSupervisedBy = getProcessAPI().searchArchivedFlowNodeInstancesSupervisedBy(
                user.getId(), builder.done());
        assertEquals(2, searchArchivedFlowNodeInstancesSupervisedBy.getCount());

        final List<ArchivedFlowNodeInstance> result = searchArchivedFlowNodeInstancesSupervisedBy.getResult();
        assertEquals("sendTask", result.get(0).getName());
        assertEquals("userTask1", result.get(1).getName());
    }

    @Cover(classes = { ProcessAPI.class, ArchivedFlowNodeInstance.class }, concept = BPMNConcept.SUPERVISOR, jira = "BS-8694", keywords = { "Call activity",
            "Sub-Process", "Manager", "searchArchivedFlowNodeInstancesSupervisedBy" })
    @Test
    public void searchArchivedFlowNodeInstancesSupervisedBy_should_return_1_flownode_when_is_on_subprocess_and_searchs_by_subprocess_manager() throws Exception {
        final User subProcessPM = users.get(0);
        final long subProcessPMId = subProcessPM.getId();
        final User parentProcessPM = createUser(USERNAME + 2, PASSWORD);
        final long parentProcessPMId = parentProcessPM.getId();
        users.add(parentProcessPM);

        // Create Sub-process and its supervisor
        final ProcessDefinitionBuilder targetProcessDefBuilder = new ProcessDefinitionBuilder().createNewInstance("Target process", PROCESS_VERSION);
        targetProcessDefBuilder.addActor(ACTOR_NAME);
        targetProcessDefBuilder.addUserTask("stepOfSubProcess", ACTOR_NAME);
        processDefinitions.add(deployAndEnableProcessWithActor(targetProcessDefBuilder.done(), ACTOR_NAME, subProcessPM));
        processSupervisors.add(getProcessAPI().createProcessSupervisorForUser(processDefinitions.get(2).getId(), subProcessPMId));

        // Create parent process and its supervisor
        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression("Target process");
        final ProcessDefinitionBuilder parentProcessDefBuilder = new ProcessDefinitionBuilder().createNewInstance("Process parent", PROCESS_VERSION);
        parentProcessDefBuilder.addActor(ACTOR_NAME);
        parentProcessDefBuilder.addStartEvent("start").addCallActivity("callActivity", targetProcessNameExpr, null).addUserTask("step1", ACTOR_NAME)
                .addEndEvent("end");
        parentProcessDefBuilder.addTransition("start", "callActivity").addTransition("callActivity", "step1").addTransition("step1", "end");
        processDefinitions.add(deployAndEnableProcessWithActor(parentProcessDefBuilder.done(), ACTOR_NAME, parentProcessPM));
        processSupervisors.add(getProcessAPI().createProcessSupervisorForUser(processDefinitions.get(3).getId(), parentProcessPMId));

        // Start the parent process and execute the task of the sub process
        getProcessAPI().startProcess(processDefinitions.get(3).getId());
        final HumanTaskInstance stepOfSubProcess = waitForUserTaskAndExecuteAndGetIt("stepOfSubProcess", parentProcessPM);
        waitForProcessToFinish(stepOfSubProcess.getParentProcessInstanceId());

        // Then
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ArchivedActivityInstanceSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ArchivedFlowNodeInstance> searchArchivedFlowNodeInstancesSupervisedByParentProcessPM = getProcessAPI()
                .searchArchivedFlowNodeInstancesSupervisedBy(parentProcessPMId, builder.done());
        assertEquals(0, searchArchivedFlowNodeInstancesSupervisedByParentProcessPM.getCount());

        final SearchResult<ArchivedFlowNodeInstance> searchArchivedFlowNodeInstancesSupervisedBySubProcessPM = getProcessAPI()
                .searchArchivedFlowNodeInstancesSupervisedBy(subProcessPMId, builder.done());
        assertEquals(1, searchArchivedFlowNodeInstancesSupervisedBySubProcessPM.getCount());
        final List<ArchivedFlowNodeInstance> result = searchArchivedFlowNodeInstancesSupervisedBySubProcessPM.getResult();
        assertEquals("stepOfSubProcess", result.get(0).getName());
    }

    @Cover(classes = { ProcessAPI.class, FlowNodeInstance.class }, concept = BPMNConcept.SUPERVISOR, jira = "BS-8295", keywords = { "" })
    @Test
    public void searchFlowNodeInstancesSupervisedByShouldFind1PendingFlowNodeAndThenFinishesItAndFindNoFlowNode() throws Exception {
        final User user = users.get(0);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(0).getId());

        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ActivityInstanceSearchDescriptor.NAME, Order.ASC);
        SearchResult<FlowNodeInstance> searchFlowNodeInstancesSupervisedBy = getProcessAPI().searchFlowNodeInstancesSupervisedBy(
                user.getId(), builder.done());
        assertEquals(1, searchFlowNodeInstancesSupervisedBy.getCount());

        final List<FlowNodeInstance> result = searchFlowNodeInstancesSupervisedBy.getResult();
        assertEquals("userTask1", result.get(0).getName());

        waitForUserTaskAndExecuteIt(processInstance, "userTask1", user);
        waitForProcessToFinish(processInstance);

        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ActivityInstanceSearchDescriptor.NAME, Order.ASC);
        searchFlowNodeInstancesSupervisedBy = getProcessAPI().searchFlowNodeInstancesSupervisedBy(
                user.getId(), builder.done());
        assertEquals(0, searchFlowNodeInstancesSupervisedBy.getCount());
    }

    @Cover(classes = { ProcessAPI.class, FlowNodeInstance.class }, concept = BPMNConcept.SUPERVISOR, jira = "BS-8295", keywords = { "" })
    @Test
    public void searchFlowNodeInstancesSupervisedByShouldFindOneFailedFlowNode() throws Exception {
        final User user = users.get(0);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(1).getId());
        final ActivityInstance failedTask = waitForTaskToFail(processInstance);
        assertEquals(failingTaskName, failedTask.getName());

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        // builder.filter("status", "failed");
        builder.sort(ActivityInstanceSearchDescriptor.NAME, Order.ASC);
        SearchResult<FlowNodeInstance> searchFlowNodeInstancesSupervisedBy = getProcessAPI().searchFlowNodeInstancesSupervisedBy(
                user.getId(), builder.done());
        assertEquals(0, searchFlowNodeInstancesSupervisedBy.getCount());

        final ProcessSupervisor failedProcessSupervisor = getProcessAPI().createProcessSupervisorForUser(processDefinitions.get(1).getId(), user.getId());

        searchFlowNodeInstancesSupervisedBy = getProcessAPI().searchFlowNodeInstancesSupervisedBy(
                user.getId(), builder.done());
        assertEquals(1, searchFlowNodeInstancesSupervisedBy.getCount());
        final List<FlowNodeInstance> result = searchFlowNodeInstancesSupervisedBy.getResult();
        assertEquals(failingTaskName, result.get(0).getName());

        getProcessAPI().deleteSupervisor(failedProcessSupervisor.getSupervisorId());
    }

    @Cover(classes = { ProcessAPI.class, ArchivedFlowNodeInstance.class }, concept = BPMNConcept.SUPERVISOR, jira = "BS-8295", keywords = { "" })
    @Test
    public void searchArchivedActivityInstancesSupervisedByShouldFind2Activities() throws Exception {
        final User user = users.get(0);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinitions.get(0).getId());
        waitForUserTaskAndExecuteIt(processInstance, "userTask1", user);
        waitForProcessToFinish(processInstance);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ArchivedActivityInstanceSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ArchivedActivityInstance> searchArchivedActivityInstancesSupervisedBy = getProcessAPI().searchArchivedActivityInstancesSupervisedBy(
                user.getId(), builder.done());
        assertEquals(2, searchArchivedActivityInstancesSupervisedBy.getCount());

        final List<ArchivedActivityInstance> result = searchArchivedActivityInstancesSupervisedBy.getResult();
        assertEquals("sendTask", result.get(0).getName());
        assertEquals("userTask1", result.get(1).getName());
    }

    @Cover(classes = { ProcessAPI.class, ArchivedFlowNodeInstance.class }, concept = BPMNConcept.SUPERVISOR, jira = "BS-8295", keywords = { "" })
    @Test
    public void searchArchivedProcessInstancesSupervisedByShouldFind1Process() throws Exception {
        final User user = users.get(0);
        final ProcessDefinition processDefinition = processDefinitions.get(0);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "userTask1", user);
        waitForProcessToFinish(processInstance);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ArchivedActivityInstanceSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ArchivedProcessInstance> searchArchivedProcessInstancesSupervisedBy = getProcessAPI().searchArchivedProcessInstancesSupervisedBy(
                user.getId(), builder.done());
        assertEquals(1, searchArchivedProcessInstancesSupervisedBy.getCount());

        final List<ArchivedProcessInstance> result = searchArchivedProcessInstancesSupervisedBy.getResult();
        assertEquals(processDefinition.getName(), result.get(0).getName());
    }
}
