package com.bonitasoft.engine.supervisor;

import static org.junit.Assert.*;

import java.util.List;

import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
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
import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.ProcessAPI;

public class ProcessSupervisedTest extends CommonAPISPTest {

    private User user;

    private ProcessDefinition processDefinition;

    private ProcessSupervisor processSupervisor;

    protected ProcessDefinition failingProcessDefinition;

    String failingTaskName = "failedStep";

    @Before
    public void before() throws Exception {
        login();

        user = createUser(USERNAME, PASSWORD);
        final Expression targetProcessExpression = new ExpressionBuilder().createConstantStringExpression("receiveMessageProcess");
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription(DESCRIPTION).addUserTask("userTask1", ACTOR_NAME).addSendTask("sendTask", "messageName",
                targetProcessExpression);
        processBuilder.addShortTextData("Application", null);
        processBuilder.addTransition("userTask1", "sendTask");
        processDefinition = deployAndEnableWithActor(processBuilder.done(), ACTOR_NAME, user);

        processSupervisor = getProcessAPI().createProcessSupervisorForUser(processDefinition.getId(), user.getId());

        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("processwithIntegerData", "1.0");
        processDefinitionBuilder.addAutomaticTask(failingTaskName).addIntegerData("intdata",
                new ExpressionBuilder().createExpression("d", "d", Integer.class.getName(), ExpressionType.TYPE_CONSTANT));
        failingProcessDefinition = deployAndEnableProcess(processDefinitionBuilder.done());
    }

    @After
    public void after() throws BonitaException, BonitaHomeNotSetException {
        getProcessAPI().deleteSupervisor(processSupervisor.getSupervisorId());
        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(failingProcessDefinition);
        getIdentityAPI().deleteUser(user.getId());
        logout();
    }

    @Cover(classes = { ProcessAPI.class, ArchivedFlowNodeInstance.class }, concept = BPMNConcept.SUPERVISOR, jira = "BS-8295", keywords = { "" })
    @Test
    @Ignore
    public void searchArchivedFlowNodeInstancesSupervisedBy() throws Exception {
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("userTask1", processInstance, user);
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

    @Cover(classes = { ProcessAPI.class, FlowNodeInstance.class }, concept = BPMNConcept.SUPERVISOR, jira = "BS-8295", keywords = { "" })
    @Test
    @Ignore
    public void searchFlowNodeInstancesSupervisedByShouldFind1PendingFlowNodeAndThenFinishesItAndFindNoFlowNode() throws Exception {
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ActivityInstanceSearchDescriptor.NAME, Order.ASC);
        SearchResult<FlowNodeInstance> searchFlowNodeInstancesSupervisedBy = getProcessAPI().searchFlowNodeInstancesSupervisedBy(
                user.getId(), builder.done());
        assertEquals(1, searchFlowNodeInstancesSupervisedBy.getCount());

        final List<FlowNodeInstance> result = searchFlowNodeInstancesSupervisedBy.getResult();
        assertEquals("userTask1", result.get(0).getName());

        waitForUserTaskAndExecuteIt("userTask1", processInstance, user);
        waitForProcessToFinish(processInstance);

        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ActivityInstanceSearchDescriptor.NAME, Order.ASC);
        searchFlowNodeInstancesSupervisedBy = getProcessAPI().searchFlowNodeInstancesSupervisedBy(
                user.getId(), builder.done());
        assertEquals(0, searchFlowNodeInstancesSupervisedBy.getCount());
    }

    @Cover(classes = { ProcessAPI.class, FlowNodeInstance.class }, concept = BPMNConcept.SUPERVISOR, jira = "BS-8295", keywords = { "" })
    @Test
    @Ignore
    public void searchFlowNodeInstancesSupervisedByShouldFindOneFailedFlowNode() throws Exception {

        final ProcessInstance processInstance = getProcessAPI().startProcess(failingProcessDefinition.getId());
        final ActivityInstance failedTask = waitForTaskToFail(processInstance);
        assertEquals(failingTaskName, failedTask.getName());

        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        // builder.filter("status", "failed");
        builder.sort(ActivityInstanceSearchDescriptor.NAME, Order.ASC);
        SearchResult<FlowNodeInstance> searchFlowNodeInstancesSupervisedBy = getProcessAPI().searchFlowNodeInstancesSupervisedBy(
                user.getId(), builder.done());
        assertEquals(0, searchFlowNodeInstancesSupervisedBy.getCount());

        ProcessSupervisor failedProcessSupervisor = getProcessAPI().createProcessSupervisorForUser(failingProcessDefinition.getId(), user.getId());

        searchFlowNodeInstancesSupervisedBy = getProcessAPI().searchFlowNodeInstancesSupervisedBy(
                user.getId(), builder.done());
        assertEquals(1, searchFlowNodeInstancesSupervisedBy.getCount());
        final List<FlowNodeInstance> result = searchFlowNodeInstancesSupervisedBy.getResult();
        assertEquals(failingTaskName, result.get(0).getName());

        getProcessAPI().deleteSupervisor(failedProcessSupervisor.getSupervisorId());
    }

    @Cover(classes = { ProcessAPI.class, ArchivedFlowNodeInstance.class }, concept = BPMNConcept.SUPERVISOR, jira = "BS-8295", keywords = { "" })
    @Test
    @Ignore
    public void searchArchivedActivityInstancesSupervisedByShouldFind2Activities() throws Exception {
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("userTask1", processInstance, user);
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
    public void searchArchivedProcessInstancesSupervisedByShouldFind2Activities() throws Exception {
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("userTask1", processInstance, user);
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
