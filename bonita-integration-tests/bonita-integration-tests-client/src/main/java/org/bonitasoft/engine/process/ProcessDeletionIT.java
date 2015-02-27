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
package org.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.TestWithUser;
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
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.impl.DocumentDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
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
public class ProcessDeletionIT extends TestWithUser {

    private List<ProcessDefinition> processDefinitions;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        processDefinitions = new ArrayList<ProcessDefinition>();
    }

    @Override
    @After
    public void after() throws Exception {
        disableAndDeleteProcess(processDefinitions);
        super.after();
    }

    private ProcessDefinition deployProcessWithSeveralOutGoingTransitions() throws BonitaException {
        final ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("process To Delete", "2.5");
        processDefinitionBuilder.addActor(ACTOR_NAME);
        processDefinitionBuilder.addUserTask("step1", ACTOR_NAME);
        for (int i = 0; i < 10; i++) {
            final String activityName = "step2" + i;
            processDefinitionBuilder.addUserTask(activityName, ACTOR_NAME);
            processDefinitionBuilder.addTransition("step1", activityName);
        }
        return deployAndEnableProcessWithActor(processDefinitionBuilder.done(), ACTOR_NAME, user);
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete process instance", "Delete", "Process definition" }, jira = "")
    public void deleteProcessInstanceStopsCreatingNewActivities() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithSeveralOutGoingTransitions();
        processDefinitions.add(processDefinition); // To clean in the end
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        getProcessAPI().deleteProcessInstance(processInstance.getId());

        Thread.sleep(1500);

        final List<HumanTaskInstance> taskInstances = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 100, ActivityInstanceCriterion.DEFAULT);
        assertEquals(0, taskInstances.size());
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete process instance", "Delete", "Process definition" }, jira = "")
    public void deleteProcessInstanceAlsoDeleteArchivedElements() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithSeveralOutGoingTransitions();
        processDefinitions.add(processDefinition); // To clean in the end
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        for (int i = 0; i < 10; i++) {
            waitForUserTask(processInstance, "step2" + i);
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
        final ProcessInstance processInstance = startAndFinishProcess(processDefinition);
        getProcessAPI().deleteArchivedProcessInstances(processDefinition.getId(), 0, 50);

        final List<ArchivedActivityInstance> taskInstances = getProcessAPI().getArchivedActivityInstances(processInstance.getId(), 0, 100,
                ActivityInstanceCriterion.DEFAULT);
        assertEquals(0, taskInstances.size());
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete", "Process instance" }, jira = "BS-10374")
    public void deleteArchivedProcessInstancesInAllStatesByIdsAlsoDeleteArchivedElements() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithSeveralOutGoingTransitions();
        processDefinitions.add(processDefinition); // To clean in the end
        final ProcessInstance processInstance1 = startAndFinishProcess(processDefinition);
        startAndFinishProcess(processDefinition);
        startAndFinishProcess(processDefinition);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ProcessInstanceSearchDescriptor.NAME, Order.ASC);
        final List<ArchivedProcessInstance> archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done())
                .getResult();
        getProcessAPI().deleteArchivedProcessInstancesInAllStates(
                Arrays.asList(archivedProcessInstances.get(0).getSourceObjectId(), archivedProcessInstances.get(2).getSourceObjectId()));

        final List<ArchivedActivityInstance> taskInstances = getProcessAPI().getArchivedActivityInstances(processInstance1.getId(), 0, 100,
                ActivityInstanceCriterion.DEFAULT);
        assertEquals(0, taskInstances.size());
        final long numberOfArchivedProcessInstancesAfterDelete = getProcessAPI().searchArchivedProcessInstancesInAllStates(searchOptionsBuilder.done())
                .getCount();
        assertEquals(3, numberOfArchivedProcessInstancesAfterDelete);
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete", "Process instance" }, jira = "BS-10375")
    public void deleteArchivedProcessInstancesInAllStatesByIdAlsoDeleteArchivedElements() throws Exception {
        final ProcessDefinition processDefinition = deployProcessWithSeveralOutGoingTransitions();
        processDefinitions.add(processDefinition); // To clean in the end
        final ProcessInstance processInstance = startAndFinishProcess(processDefinition);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ProcessInstanceSearchDescriptor.NAME, Order.ASC);
        final List<ArchivedProcessInstance> archivedProcessInstances = getProcessAPI().searchArchivedProcessInstancesInAllStates(searchOptionsBuilder.done())
                .getResult();
        getProcessAPI().deleteArchivedProcessInstancesInAllStates(archivedProcessInstances.get(0).getSourceObjectId());

        final List<ArchivedActivityInstance> taskInstances = getProcessAPI().getArchivedActivityInstances(processInstance.getId(), 0, 100,
                ActivityInstanceCriterion.DEFAULT);
        assertEquals(0, taskInstances.size());
        final long numberOfArchivedProcessInstancesAfterDelete = getProcessAPI().searchArchivedProcessInstancesInAllStates(searchOptionsBuilder.done())
                .getCount();
        assertEquals(archivedProcessInstances.size() - 3, numberOfArchivedProcessInstancesAfterDelete);
    }

    private ProcessInstance startAndFinishProcess(final ProcessDefinition processDefinition) throws ProcessDefinitionNotFoundException,
            ProcessActivationException, ProcessExecutionException, Exception {
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        for (int i = 0; i < 10; i++) {
            waitForUserTaskAndExecuteIt(processInstance, "step2" + i, user);
        }
        waitForProcessToFinish(processInstance);
        return processInstance;
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
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDefinitionBuilder.done(), actorName, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
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
        waitForUserTask(rootProcessInstance, simpleStepName);

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
        final ActivityInstance simpleTask = waitForUserTaskAndExecuteAndGetIt(rootProcessInstance, simpleStepName, user);

        // execute intermediate task: p2 will finish
        final ActivityInstance intermediateTask = waitForUserTaskAndExecuteAndGetIt(rootProcessInstance, intermediateStepName, user);

        // execute root task: p3 will finish
        waitForUserTaskAndExecuteIt(rootProcessInstance, rootStepName, user);
        waitForProcessToFinish(rootProcessInstance);

        // delete the processDefinition: all archived processes must be deleted
        getProcessAPI().disableAndDelete(rootProcess.getId());

        // check that archived flow nodes were deleted.
        checkAllArchivedElementsWereDeleted(rootProcessInstance.getId());
        checkAllArchivedElementsWereDeleted(intermediateTask.getParentProcessInstanceId());
        checkAllArchivedElementsWereDeleted(simpleTask.getParentProcessInstanceId());
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
        return deployAndEnableProcessWithActor(processDefBuilder.done(), ACTOR_NAME, user);
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
        return deployAndEnableProcessWithActor(processDefBuilder.done(), ACTOR_NAME, user);
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
        final ActivityInstance simpleTask = waitForUserTaskAndGetIt(rootProcessInstance, simpleStepName);
        final ProcessInstance simpleProcessInstance = getProcessAPI().getProcessInstance(simpleTask.getParentProcessInstanceId());
        assignAndExecuteStep(simpleTask, user.getId());
        waitForProcessToFinish(simpleProcessInstance);
        waitForUserTask(rootProcessInstance, rootStepName);

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
        final ActivityInstance simpleTask = waitForUserTaskAndAssigneIt(rootProcessInstance, simpleStepName, user);
        final ProcessInstance simpleProcessInstance = getProcessAPI().getProcessInstance(simpleTask.getParentProcessInstanceId());
        getProcessAPI().executeFlowNode(simpleTask.getId());

        waitForUserTask(rootProcessInstance, rootStepName);
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
        waitForUserTask(rootProcessInstance, parentTaskName);
        getProcessAPI().sendSignal(signalName);

        // wait for first step in the event sub-process
        waitForUserTask(rootProcessInstance, childTaskName);

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
        return deployAndEnableProcessWithActor(processDefinition, "mainActor", user);
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
        waitForUserTaskAndExecuteIt(processInstanceToArchive, userTaskName, user);
        waitForProcessToFinish(processInstanceToArchive);

        // start a process non completed process
        final ProcessInstance activeProcessInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(activeProcessInstance, userTaskName);

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
        waitForUserTaskAndExecuteIt(processInstanceToArchive, userTaskName, user);
        waitForProcessToFinish(processInstanceToArchive);

        // start a process non completed process
        final ProcessInstance activeProcessInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(activeProcessInstance, userTaskName);

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
        waitForUserTask(processInstance, userTaskName);

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
        waitForUserTask(processInstance, userTaskName);

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
        waitForUserTask(processInstance, userTaskName);

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
        return deployAndEnableProcessWithActor(processDefBuilder.done(), ACTOR_NAME, user);
    }

    @Test
    @Cover(classes = { ProcessManagementAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Delete", "Process definition", "comments" }, jira = "ENGINE-257")
    public void deleteProcessInstanceAlsoDeleteComments() throws Exception {
        // deploy and start a simple process
        final String userTaskName = "step1";
        final ProcessDefinition processDefinition = deployAndEnableSimpleProcess("myProcess", userTaskName);
        processDefinitions.add(processDefinition); // To clean in the end
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, userTaskName);

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
        final long userTaskId = waitForUserTask(processInstance, userTaskName);

        // add a comment
        getProcessAPI().addProcessComment(processInstance.getId(), "just do it.");
        assignAndExecuteStep(userTaskId, user);
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
        final long userTaskId = waitForUserTask(processInstance, userTaskName);

        // add a comment
        getProcessAPI().addProcessComment(processInstance.getId(), "just do it2.");
        assignAndExecuteStep(userTaskId, user);
        waitForProcessToFinish(processInstance);

        SearchResult<ArchivedComment> searchResult = getProcessAPI().searchArchivedComments(new SearchOptionsBuilder(0, 10).done());
        assertTrue(searchResult.getCount() > 0);

        getProcessAPI().deleteArchivedProcessInstances(processDefinition.getId(), 0, 10);

        // check all archived comments were deleted along with process instance:
        searchResult = getProcessAPI().searchArchivedComments(new SearchOptionsBuilder(0, 10).done());
        assertEquals(0, searchResult.getCount());
    }

}
