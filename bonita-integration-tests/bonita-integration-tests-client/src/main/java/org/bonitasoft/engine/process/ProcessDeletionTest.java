package org.bonitasoft.engine.process;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessManagementAPI;
import org.bonitasoft.engine.bpm.comment.ArchivedComment;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.document.Document;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.impl.DocumentDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class ProcessDeletionTest extends CommonAPITest {

    protected User pedro;

    private List<ProcessDefinition> processDefinitions;

    @Before
    public void before() throws Exception {
         loginOnDefaultTenantWithDefaultTechnicalUser();
        pedro = getIdentityAPI().createUser(USERNAME, PASSWORD);
        processDefinitions = new ArrayList<ProcessDefinition>();
    }

    @After
    public void after() throws Exception {
        disableAndDeleteProcess(processDefinitions);
        deleteUser(pedro);
        logoutOnTenant();
    }

    private ProcessDefinition deployProcessWithSeveralOutGoingTransitions() throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("process To Delete", "2.5");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        for (int i = 0; i < 30; i++) {
            final String activityName = "step2" + i;
            processDefinitionBuilder.addUserTask(activityName, ACTOR_NAME);
            processDefinitionBuilder.addTransition("step1", activityName);
        }
        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, pedro);
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete process instance", "Delete", "Process definition" }, jira = "")
    public void deleteProcessInstanceStopsCreatingNewActivities() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithSeveralOutGoingTransitions();
        processDefinitions.add(processDefinition); // To clean in the end
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", processInstance, pedro);
        getProcessAPI().deleteProcessInstance(processInstance.getId());

        Thread.sleep(1500);

        final List<HumanTaskInstance> taskInstances = getProcessAPI().getPendingHumanTaskInstances(pedro.getId(), 0, 100, ActivityInstanceCriterion.DEFAULT);
        assertEquals(0, taskInstances.size());
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete process instance", "Delete", "Process definition" }, jira = "")
    public void deleteProcessInstanceAlsoDeleteArchivedElements() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithSeveralOutGoingTransitions();
        processDefinitions.add(processDefinition); // To clean in the end
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", processInstance, pedro);
        for (int i = 0; i < 30; i++) {
            waitForUserTask("step2" + i, processInstance);
        }

        getProcessAPI().deleteProcessInstance(processInstance.getId());

        final List<ArchivedActivityInstance> taskInstances = getProcessAPI().getArchivedActivityInstances(processInstance.getId(), 0, 100,
                ActivityInstanceCriterion.DEFAULT);
        assertEquals(0, taskInstances.size());
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete process instance", "Delete", "Process definition" }, jira = "")
    public void deleteArchivedProcessInstanceAlsoDeleteArchivedElements() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithSeveralOutGoingTransitions();
        processDefinitions.add(processDefinition); // To clean in the end
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", processInstance, pedro);
        for (int i = 0; i < 30; i++) {
            waitForUserTaskAndExecuteIt("step2" + i, processInstance, pedro);
        }
        waitForProcessToFinish(processInstance);
        getProcessAPI().deleteArchivedProcessInstances(processDefinition.getId(), 0, 50);

        final List<ArchivedActivityInstance> taskInstances = getProcessAPI().getArchivedActivityInstances(processInstance.getId(), 0, 100,
                ActivityInstanceCriterion.DEFAULT);
        assertEquals(0, taskInstances.size());
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete process instance", "Delete", "Process definition" }, jira = "")
    public void deleteProcessDefinitionStopsCreatingNewActivities() throws Exception {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("process To Delete", "2.5");
        final String actorName = "delivery";
        processDefinitionBuilder.addActor(actorName);
        processDefinitionBuilder.addUserTask("step1", actorName);
        for (int i = 0; i < 30; i++) {
            final String activityName = "step2" + i;
            processDefinitionBuilder.addUserTask(activityName, actorName);
            processDefinitionBuilder.addTransition("step1", activityName);

        }
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), actorName, pedro);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt("step1", processInstance, pedro);
        disableAndDeleteProcess(processDefinition.getId()); // will fail in CommonAPITest.succeeded if activities are created after delete
        Thread.sleep(1500);
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete process instance", "call activities" }, jira = "ENGINE-257")
    public void deleteProcessInstanceAlsoDeleteChildrenProcesses() throws Exception {
        // deploy a simple process P1
        final String simpleStepName = "simpleStep";
        final ProcessDefinition simpleProcess = deployAndEnableSimpleProcess("simpleProcess", simpleStepName);
        processDefinitions.add(simpleProcess); // To clean in the end

        // deploy a process P2 containing a call activity calling P1
        final String intermediateStepName = "intermediateStep1";
        final String intermediateCallActivityName = "intermediateCall";
        final ProcessDefinition intermediateProcess = deployAndEnableProcessWithCallActivity("intermediateProcess", simpleProcess.getName(),
                intermediateStepName, intermediateCallActivityName);
        processDefinitions.add(intermediateProcess); // To clean in the end

        // deploy a process P3 containing a call activity calling P2
        final String rootStepName = "rootStep1";
        final String rootCallActivityName = "rootCall";
        final ProcessDefinition rootProcess = deployAndEnableProcessWithCallActivity("rootProcess", intermediateProcess.getName(), rootStepName,
                rootCallActivityName);
        processDefinitions.add(rootProcess); // To clean in the end

        // start P3, the call activities will start instances of P2 a and P1
        final ProcessInstance rootProcessInstance = getProcessAPI().startProcess(rootProcess.getId());
        waitForUserTask(simpleStepName, rootProcessInstance);

        // check that the instances of p1, p2 and p3 were created
        List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.NAME_ASC);
        assertEquals(3, processInstances.size());

        // check that archived flow nodes
        List<ArchivedActivityInstance> taskInstances = getProcessAPI().getArchivedActivityInstances(rootProcessInstance.getId(), 0, 100,
                ActivityInstanceCriterion.DEFAULT);
        assertTrue(taskInstances.size() > 0);

        // delete the root process instance
        getProcessAPI().deleteProcessInstance(rootProcessInstance.getId());

        // check that the instances of p1 and p2 were deleted
        processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.NAME_ASC);
        assertEquals(0, processInstances.size());

        // check that archived flow nodes were not deleted.
        taskInstances = getProcessAPI().getArchivedActivityInstances(rootProcessInstance.getId(), 0, 100, ActivityInstanceCriterion.DEFAULT);
        assertEquals(0, taskInstances.size());
    }

    @Deprecated
    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete process instance", "call activities" }, jira = "ENGINE-257")
    public void deleteProcessDefinitionAlsoArchivedChildrenProcesses() throws Exception {
        // deploy a simple process P1
        final String simpleStepName = "simpleStep";
        final ProcessDefinition simpleProcess = deployAndEnableSimpleProcess("simpleProcess", simpleStepName);
        processDefinitions.add(simpleProcess); // To clean in the end

        // deploy a process P2 containing a call activity calling P1
        final String intermediateStepName = "intermediateStep1";
        final String intermediateCallActivityName = "intermediateCall";
        final ProcessDefinition intermediateProcess = deployAndEnableProcessWithCallActivity("intermediateProcess", simpleProcess.getName(),
                intermediateStepName, intermediateCallActivityName);
        processDefinitions.add(intermediateProcess); // To clean in the end

        // deploy a process P3 containing a call activity calling P2
        final String rootStepName = "rootStep1";
        final String rootCallActivityName = "rootCall";
        final ProcessDefinition rootProcess = deployAndEnableProcessWithCallActivity("rootProcess", intermediateProcess.getName(), rootStepName,
                rootCallActivityName);

        // start P3, the call activities will start instances of P2 a and P1
        final ProcessInstance rootProcessInstance = getProcessAPI().startProcess(rootProcess.getId());
        final ActivityInstance simpleTask = waitForUserTask(simpleStepName, rootProcessInstance);
        final long simpleProcessInstanceId = simpleTask.getParentProcessInstanceId();

        // execute simple task: p1 will finish
        assignAndExecuteStep(simpleTask, pedro.getId());

        // execute intermediate task: p2 will finish
        final ActivityInstance intermediateTask = waitForUserTask(intermediateStepName, rootProcessInstance);
        final long intermediateProcessInstanceId = intermediateTask.getParentProcessInstanceId();
        assignAndExecuteStep(intermediateTask, pedro.getId());

        // execute root task: p3 will finish
        waitForUserTaskAndExecuteIt(rootStepName, rootProcessInstance, pedro);
        waitForProcessToFinish(rootProcessInstance);

        // delete the processDefinition: all archived processes must be deleted
        getProcessAPI().disableAndDelete(rootProcess.getId());

        // check that archived flow nodes were deleted.
        checkAllArchivedElementsWereDeleted(rootProcessInstance.getId());
        checkAllArchivedElementsWereDeleted(intermediateProcessInstanceId);
        checkAllArchivedElementsWereDeleted(simpleProcessInstanceId);
    }

    private void checkAllArchivedElementsWereDeleted(final long processInstanceId) {
        // check that archived flow nodes were deleted.
        final List<ArchivedActivityInstance> taskInstances = getProcessAPI().getArchivedActivityInstances(processInstanceId, 0, 100,
                ActivityInstanceCriterion.DEFAULT);
        assertTrue(taskInstances.isEmpty());

        // check archived processIntances were deleted
        final List<ArchivedProcessInstance> archivedProcessInstanceList = getProcessAPI().getArchivedProcessInstances(processInstanceId, 0, 10);
        assertTrue(archivedProcessInstanceList.isEmpty());
    }

    private ProcessDefinition deployAndEnableSimpleProcess(final String processName, final String userTaskName) throws BonitaException {
        final ProcessDefinitionBuilder processDefBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        processDefBuilder.addActor(ACTOR_NAME);
        processDefBuilder.addStartEvent("tStart");
        processDefBuilder.addUserTask(userTaskName, ACTOR_NAME);
        processDefBuilder.addEndEvent("tEnd");
        processDefBuilder.addTransition("tStart", userTaskName);
        processDefBuilder.addTransition(userTaskName, "tEnd");
        return deployAndEnableProcessWithActor(processDefBuilder.done(), ACTOR_NAME, pedro);
    }

    private ProcessDefinition deployAndEnableProcessWithCallActivity(final String processName, final String targetProcessName, final String userTaskName,
            final String callActivityName) throws BonitaException {
        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression(targetProcessName);

        final ProcessDefinitionBuilder processDefBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        processDefBuilder.addActor(ACTOR_NAME);
        processDefBuilder.addStartEvent("start");
        processDefBuilder.addCallActivity(callActivityName, targetProcessNameExpr, null);
        processDefBuilder.addUserTask(userTaskName, ACTOR_NAME);
        processDefBuilder.addEndEvent("end");
        processDefBuilder.addTransition("start", callActivityName);
        processDefBuilder.addTransition(callActivityName, userTaskName);
        processDefBuilder.addTransition(userTaskName, "end");
        return deployAndEnableProcessWithActor(processDefBuilder.done(), ACTOR_NAME, pedro);
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete process instance", "call activities" }, jira = "ENGINE-257")
    public void deleteProcessInstanceAlsoDeleteArchivedChildrenProcesses() throws Exception {
        // deploy a simple process P1
        final String simpleStepName = "simpleStep";
        final ProcessDefinition simpleProcess = deployAndEnableSimpleProcess("simpleProcess", simpleStepName);
        processDefinitions.add(simpleProcess); // To clean in the end

        // deploy a process P2 containing a call activity calling P1
        final String rootStepName = "rootStep1";
        final String rootCallActivityName = "rootCall";
        final ProcessDefinition rootProcess = deployAndEnableProcessWithCallActivity("rootProcess", simpleProcess.getName(), rootStepName, rootCallActivityName);
        processDefinitions.add(rootProcess); // To clean in the end

        // start P2, the call activities will start an instance of P1
        final ProcessInstance rootProcessInstance = getProcessAPI().startProcess(rootProcess.getId());
        final ActivityInstance simpleTask = waitForUserTask(simpleStepName, rootProcessInstance);
        final ProcessInstance simpleProcessInstance = getProcessAPI().getProcessInstance(simpleTask.getParentProcessInstanceId());
        assignAndExecuteStep(simpleTask, pedro.getId());
        waitForProcessToFinish(simpleProcessInstance);
        waitForUserTask(rootStepName, rootProcessInstance);

        // check that only one instance (p2) is in the journal: p1 is supposed to be archived
        List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.NAME_ASC);
        assertEquals(1, processInstances.size());

        // check that there are archived instances of p1
        List<ArchivedProcessInstance> archivedProcessInstanceList = getProcessAPI().getArchivedProcessInstances(simpleProcessInstance.getId(), 0, 10);
        assertTrue(archivedProcessInstanceList.size() > 0);

        // delete the root process instance
        getProcessAPI().deleteProcessInstance(rootProcessInstance.getId());

        // check that the instance of p2 was deleted
        processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.NAME_ASC);
        assertEquals(0, processInstances.size());

        // check that the archived instances of p1 were not deleted
        archivedProcessInstanceList = getProcessAPI().getArchivedProcessInstances(simpleProcessInstance.getId(), 0, 10);
        assertEquals(0, archivedProcessInstanceList.size());

        // check that archived flow node were not deleted.
        final List<ArchivedActivityInstance> taskInstances = getProcessAPI().getArchivedActivityInstances(rootProcessInstance.getId(), 0, 10,
                ActivityInstanceCriterion.DEFAULT);
        assertEquals(0, taskInstances.size());
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete archived process instance", "call activities" }, jira = "ENGINE-1641")
    public void deleteArchivedProcessInstanceAndChildrenProcesses() throws Exception {
        // deploy a simple process P1
        final String simpleStepName = "simpleStep";
        final ProcessDefinition simpleProcess = deployAndEnableSimpleProcess("simpleProcess", simpleStepName);
        processDefinitions.add(simpleProcess); // To clean in the end

        // deploy a process P2 containing a call activity calling P1
        final String rootStepName = "rootStep1";
        final String rootCallActivityName = "rootCall";
        final ProcessDefinition rootProcess = deployAndEnableProcessWithCallActivity("rootProcess", simpleProcess.getName(), rootStepName, rootCallActivityName);
        processDefinitions.add(rootProcess); // To clean in the end

        // start P2, the call activities will start an instance of P1
        final ProcessInstance rootProcessInstance = getProcessAPI().startProcess(rootProcess.getId());
        final ActivityInstance simpleTask = waitForUserTask(simpleStepName, rootProcessInstance);
        assignAndExecuteStep(simpleTask, pedro.getId());
        final ProcessInstance simpleProcessInstance = getProcessAPI().getProcessInstance(simpleTask.getParentProcessInstanceId());
        waitForUserTask(rootStepName, rootProcessInstance);
        waitForProcessToFinish(simpleProcessInstance);

        // check that only one instance (p2) is in the journal: p1 is supposed to be archived
        List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.NAME_ASC);
        assertEquals(1, processInstances.size());

        // check that there are archived instances of p1
        List<ArchivedProcessInstance> archivedProcessInstanceList = getProcessAPI().getArchivedProcessInstances(simpleProcessInstance.getId(), 0, 10);
        assertTrue(archivedProcessInstanceList.size() > 0);

        // delete archived root process instances
        getProcessAPI().deleteArchivedProcessInstances(rootProcess.getId(), 0, 30);

        // check that the instance of p2 was not deleted
        processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.NAME_ASC);
        assertEquals(1, processInstances.size());

        // check that the archived instances of p1 were deleted
        archivedProcessInstanceList = getProcessAPI().getArchivedProcessInstances(simpleProcessInstance.getId(), 0, 1000);
        assertEquals(0, archivedProcessInstanceList.size());

        // check that archived flow node were deleted.
        final List<ArchivedActivityInstance> taskInstances = getProcessAPI().getArchivedActivityInstances(rootProcessInstance.getId(), 0, 10,
                ActivityInstanceCriterion.DEFAULT);
        assertEquals(0, taskInstances.size());
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete process instance", "event sub-process" }, jira = "ENGINE-257")
    public void deleteProcessInstanceAlsoDeleteEventSubProcesses() throws Exception {
        final String parentTaskName = "step1";
        final String childTaskName = "subStep";
        final String signalName = "go";
        // deploy and create a instance of a process containing an event sub-process
        final ProcessDefinition processDefinition = deployAndEnableProcessWithSignalEventSubProcess(parentTaskName, childTaskName, signalName);
        processDefinitions.add(processDefinition); // To clean in the end
        final ProcessInstance rootProcessInstance = getProcessAPI().startProcess(processDefinition.getId());

        // wait for the first step in the parent process before sending signal the launch the event sub-process
        waitForUserTask(parentTaskName, rootProcessInstance);
        getProcessAPI().sendSignal(signalName);

        // wait for first step in the event sub-process
        waitForUserTask(childTaskName, rootProcessInstance);

        // check the number of process instances: 2 expected the root process instance and the event subprocess
        List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT);
        assertEquals(2, processInstances.size());

        // delete the root process instance: the event subprocess must be deleted at same time
        getProcessAPI().deleteProcessInstance(rootProcessInstance.getId());

        // check the number of proces instances
        processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT);
        assertEquals(0, processInstances.size());
    }

    private ProcessDefinition deployAndEnableProcessWithSignalEventSubProcess(final String parentTaskName, final String childTaskName, final String signalName)
            throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessWithEventSubProcess", "1.0");
        builder.addActor("mainActor");
        builder.addStartEvent("start");
        builder.addUserTask(parentTaskName, "mainActor");
        builder.addEndEvent("end");
        builder.addTransition("start", parentTaskName);
        builder.addTransition(parentTaskName, "end");
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("eventSubProcess", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("startSub").addSignalEventTrigger(signalName);
        subProcessBuilder.addUserTask(childTaskName, "mainActor");
        subProcessBuilder.addEndEvent("endSubProcess");
        subProcessBuilder.addTransition("startSub", childTaskName);
        subProcessBuilder.addTransition(childTaskName, "endSubProcess");
        final DesignProcessDefinition processDefinition = builder.done();
        return deployAndEnableProcessWithActor(processDefinition, "mainActor", pedro);
    }

    @Deprecated
    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete", "Process definition", "Archived", "Process instance" }, jira = "ENGINE-257")
    public void deleteProcessDefinitionAlsoArchivedProcessIntances() throws Exception {
        // deploy a simple process
        final String userTaskName = "step1";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("myProcess", userTaskName);

        // start a process and execute it until end
        final ProcessInstance processInstanceToArchive = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(userTaskName, processInstanceToArchive, pedro);
        waitForProcessToFinish(processInstanceToArchive);

        // start a process non completed process
        final ProcessInstance activeProcessInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(userTaskName, activeProcessInstance);

        // check number of process instances and archived process instances
        List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT);
        final List<ArchivedProcessInstance> archivedProcessInstances = getProcessAPI().getArchivedProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT);
        assertEquals(1, processInstances.size());
        assertEquals(1, archivedProcessInstances.size());

        // delete definition
        getProcessAPI().disableAndDelete(processDefinition.getId());

        // check number of process instances and archived process instances
        processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT);
        assertTrue(processInstances.isEmpty());

        List<ArchivedProcessInstance> archivedProcessInstanceList = getProcessAPI().getArchivedProcessInstances(processInstanceToArchive.getId(), 0, 10);
        assertTrue(archivedProcessInstanceList.isEmpty());

        archivedProcessInstanceList = getProcessAPI().getArchivedProcessInstances(activeProcessInstance.getId(), 0, 10);
        assertTrue(archivedProcessInstanceList.isEmpty());
    }

    @Test(expected = DeletionException.class)
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Can't", "Delete", "Process definition", "With",
            "Process instances" }, jira = "ENGINE-1636")
    public void deleteJustProcessDefinition() throws Exception {
        // deploy a simple process
        final String userTaskName = "step1";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("myProcess", userTaskName);
        processDefinitions.add(processDefinition); // To clean in the end

        // start a process and execute it until end
        final ProcessInstance processInstanceToArchive = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(userTaskName, processInstanceToArchive, pedro);
        waitForProcessToFinish(processInstanceToArchive);

        // start a process non completed process
        final ProcessInstance activeProcessInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(userTaskName, activeProcessInstance);

        // check number of process instances and archived process instances
        List<ProcessInstance> processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT);
        final List<ArchivedProcessInstance> archivedProcessInstances = getProcessAPI().getArchivedProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT);
        assertEquals(1, processInstances.size());
        assertEquals(1, archivedProcessInstances.size());

        // delete definition
        try {
            getProcessAPI().disableAndDeleteProcessDefinition(processDefinition.getId());
        } finally {
            // check number of process instances and archived process instances
            processInstances = getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT);
            assertFalse(processInstances.isEmpty());

            List<ArchivedProcessInstance> archivedProcessInstanceList = getProcessAPI().getArchivedProcessInstances(processInstanceToArchive.getId(), 0, 10);
            assertFalse(archivedProcessInstanceList.isEmpty());

            archivedProcessInstanceList = getProcessAPI().getArchivedProcessInstances(activeProcessInstance.getId(), 0, 10);
            assertFalse(archivedProcessInstanceList.isEmpty());
        }
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete", "Process definition" }, jira = "ENGINE-1636")
    public void deleteProcessDefinition() throws Exception {
        // deploy a simple process
        final String userTaskName = "step1";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("myProcess", userTaskName);

        // delete definition
        getProcessAPI().disableAndDeleteProcessDefinition(processDefinition.getId());

        final SearchOptionsBuilder optsBuilder = new SearchOptionsBuilder(0, 5);
        optsBuilder.sort(ProcessDeploymentInfoSearchDescriptor.DEPLOYMENT_DATE, Order.DESC);
        final SearchResult<ProcessDeploymentInfo> searchResult = getProcessAPI().searchProcessDeploymentInfos(optsBuilder.done());
        assertEquals(0, searchResult.getCount());
        assertTrue(searchResult.getResult().isEmpty());
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete", "Process definition", "Archived", "Process instance" }, jira = "ENGINE-257")
    public void deleteProcessInstanceAndNotArchivedProcessIntances() throws Exception {
        // deploy a simple process
        final String userTaskName = "step1";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("myProcess", userTaskName);
        processDefinitions.add(processDefinition); // To clean in the end

        // start a process non completed process
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(userTaskName, processInstance);

        // delete the process instance
        getProcessAPI().deleteProcessInstance(processInstance.getId());

        // check that all archived process instance related to this process were deleted
        final List<ArchivedProcessInstance> archivedProcessInstanceList = getProcessAPI().getArchivedProcessInstances(processInstance.getId(), 0, 10);
        assertEquals(1, archivedProcessInstanceList.size());
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete", "Process definition", "Archived", "Process instance" }, jira = "ENGINE-1641")
    public void deleteArchivedProcessInstances() throws Exception {
        // deploy a simple process
        final String userTaskName = "step1";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("myProcess", userTaskName);
        processDefinitions.add(processDefinition); // To clean in the end

        // start a process non completed process
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(userTaskName, processInstance);

        // delete archived process instances
        getProcessAPI().deleteArchivedProcessInstances(processDefinition.getId(), 0, 1000);

        // check that all archived process instance related to this process were deleted
        final List<ArchivedProcessInstance> archivedProcessInstanceList = getProcessAPI().getArchivedProcessInstances(processInstance.getId(), 0, 10);
        assertEquals(0, archivedProcessInstanceList.size());
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete", "Process definition", "Documents" }, jira = "ENGINE-257")
    public void deleteProcessInstanceAlsoDeleteDocuments() throws Exception {
        // deploy and instantiate a process containing data and documents
        final String userTaskName = "step1";
        final String url = "http://intranet.bonitasoft.com/private/docStorage/anyValue";
        final ProcessDefinition processDefinition = deployAndEnableProcessWithDocument("myProcess", userTaskName, "Doc", url);
        processDefinitions.add(processDefinition); // To clean in the end
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(userTaskName, processInstance);

        // check the number of data and documents
        SearchResult<Document> documentsSearchResult = getProcessAPI().searchDocuments(new SearchOptionsBuilder(0, 10).done());
        assertEquals(1, documentsSearchResult.getCount());

        // delete process instance
        getProcessAPI().deleteProcessInstance(processInstance.getId());

        // check the number of data and documents
        documentsSearchResult = getProcessAPI().searchDocuments(new SearchOptionsBuilder(0, 10).done());
        assertEquals(0, documentsSearchResult.getCount());
    }

    private ProcessDefinition deployAndEnableProcessWithDocument(final String processName, final String userTaskName, final String docName, final String url)
            throws BonitaException {
        final ProcessDefinitionBuilder processDefBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        processDefBuilder.addActor(ACTOR_NAME);
        final DocumentDefinitionBuilder documentDefinitionBuilder = processDefBuilder.addDocumentDefinition(docName);
        documentDefinitionBuilder.addUrl(url);
        processDefBuilder.addStartEvent("tStart");
        processDefBuilder.addUserTask(userTaskName, ACTOR_NAME);
        processDefBuilder.addEndEvent("tEnd");
        processDefBuilder.addTransition("tStart", userTaskName);
        processDefBuilder.addTransition(userTaskName, "tEnd");
        return deployAndEnableProcessWithActor(processDefBuilder.done(), ACTOR_NAME, pedro);
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete", "Process definition", "comments" }, jira = "ENGINE-257")
    public void deleteProcessInstanceAlsoDeleteComments() throws Exception {
        // deploy and start a simple process
        final String userTaskName = "step1";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("myProcess", userTaskName);
        processDefinitions.add(processDefinition); // To clean in the end
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(userTaskName, processInstance);

        // add a comment
        getProcessAPI().addProcessComment(processInstance.getId(), "just do it.");

        SearchResult<Comment> searchResult = getProcessAPI().searchComments(new SearchOptionsBuilder(0, 10).done());
        assertTrue(searchResult.getCount() > 0);

        // delete process instance
        getProcessAPI().deleteProcessInstance(processInstance.getId());

        // check all comments were deleted
        searchResult = getProcessAPI().searchComments(new SearchOptionsBuilder(0, 10).done());
        assertEquals(0, searchResult.getCount());
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete", "Process definition", "archived comments" }, jira = "")
    public void deleteProcessInstanceAndComments() throws Exception {
        // deploy and start a simple process
        final String userTaskName = "etapa1";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("ArchivedCommentsDeletion", userTaskName);
        processDefinitions.add(processDefinition); // To clean in the end
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance userTask = waitForUserTask(userTaskName, processInstance);

        // add a comment
        getProcessAPI().addProcessComment(processInstance.getId(), "just do it.");
        assignAndExecuteStep(userTask, pedro.getId());
        waitForProcessToFinish(processInstance);

        SearchResult<ArchivedComment> searchResult = getProcessAPI().searchArchivedComments(new SearchOptionsBuilder(0, 10).done());
        assertTrue(searchResult.getCount() > 0);

        getProcessAPI().deleteProcessInstances(processDefinition.getId(), 0, 10);

        // check all archived comments were deleted along with process instance:
        searchResult = getProcessAPI().searchArchivedComments(new SearchOptionsBuilder(0, 10).done());
        assertEquals(2, searchResult.getCount()); // "just do it." && technical comment
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete", "Process definition", "archived comments" }, jira = "")
    public void deleteArchivedProcessInstanceAndComments() throws Exception {
        // deploy and start a simple process
        final String userTaskName = "etapa1";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("ArchivedCommentsDeletion", userTaskName);
        processDefinitions.add(processDefinition); // To clean in the end
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance userTask = waitForUserTask(userTaskName, processInstance);

        // add a comment
        getProcessAPI().addProcessComment(processInstance.getId(), "just do it2.");
        assignAndExecuteStep(userTask, pedro.getId());
        waitForProcessToFinish(processInstance);

        SearchResult<ArchivedComment> searchResult = getProcessAPI().searchArchivedComments(new SearchOptionsBuilder(0, 10).done());
        assertTrue(searchResult.getCount() > 0);

        getProcessAPI().deleteArchivedProcessInstances(processDefinition.getId(), 0, 10);

        // check all archived comments were deleted along with process instance:
        searchResult = getProcessAPI().searchArchivedComments(new SearchOptionsBuilder(0, 10).done());
        assertEquals(0, searchResult.getCount());
    }

}
