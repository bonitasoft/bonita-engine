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
package org.bonitasoft.engine.process.instance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor;
import org.bonitasoft.engine.bpm.data.DataInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserSearchDescriptor;
import org.bonitasoft.engine.operation.Operation;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class ProcessInstanceIT extends AbstractProcessInstanceIT {

    @Test
    public void checkProcessInstanceDescriptionNotNull() throws Exception {
        checkProcessInstanceDescription("My description");
    }

    @Test
    public void checkProcessInstanceDescriptionNull() throws Exception {
        checkProcessInstanceDescription(null);
    }

    private void checkProcessInstanceDescription(final String description) throws Exception {
        // Create process definition with description;
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION)
                .addDescription(description).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);

        // Start ProcessInstance
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(description, processInstance.getDescription());

        waitForProcessToFinish(processInstance);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void checkArchivedProcessInstanceDescriptionNotNull() throws Exception {
        checkArchivedProcessInstanceDescription("My description");
    }

    @Test
    public void checkArchivedProcessInstanceDescriptionNull() throws Exception {
        checkArchivedProcessInstanceDescription(null);
    }

    private void checkArchivedProcessInstanceDescription(final String description) throws Exception {
        // Create process definition with description;
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION)
                .addDescription(description).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);

        // Start ProcessInstance
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(description, processInstance.getDescription());

        checkProcessInstanceIsArchived(processInstance);
        final List<ArchivedProcessInstance> processInstances = getProcessAPI().getArchivedProcessInstances(0, 1, ProcessInstanceCriterion.CREATION_DATE_DESC);
        assertEquals(1, processInstances.size());

        // We check that the retrieved processes are the good ones:
        final ArchivedProcessInstance archivedProcessInstance = processInstances.get(0);
        assertEquals(processInstance.getId(), archivedProcessInstance.getSourceObjectId());
        assertEquals(description, archivedProcessInstance.getDescription());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void deleteProcessInstance() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        final ProcessInstance process1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance process2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance process3 = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(5, getProcessAPI().getNumberOfProcessInstances());

        getProcessAPI().deleteProcessInstance(process1.getId());
        getProcessAPI().deleteProcessInstance(process3.getId());
        assertEquals(3, getProcessAPI().getNumberOfProcessInstances());

        getProcessAPI().getProcessInstance(process2.getId());
        try {
            getProcessAPI().getProcessInstance(process1.getId());
            Assert.fail("this instance should be deleted");
        } catch (final ProcessInstanceNotFoundException e) {
            // ok
        }
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = DeletionException.class)
    public void deleteUnknownProcessInstance() throws Exception {
        getProcessAPI().deleteProcessInstance(123456789123L);
    }

    @Test
    public void deleteProcessInstances() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance4 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance5 = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(5, getProcessAPI().getNumberOfProcessInstances());

        waitForUserTask(processInstance1, "step1");
        waitForUserTask(processInstance2, "step1");
        waitForUserTask(processInstance3, "step1");
        waitForUserTask(processInstance4, "step1");
        waitForUserTask(processInstance5, "step1");
        getProcessAPI().deleteProcessInstances(processDefinition.getId(), 0, 4);
        assertEquals(1, getProcessAPI().getNumberOfProcessInstances());

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Deprecated
    public void oldDeleteProcessInstances() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance4 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance5 = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(5, getProcessAPI().getNumberOfProcessInstances());

        waitForUserTask(processInstance1, "step1");
        waitForUserTask(processInstance2, "step1");
        waitForUserTask(processInstance3, "step1");
        waitForUserTask(processInstance4, "step1");
        waitForUserTask(processInstance5, "step1");
        getProcessAPI().deleteProcessInstances(processDefinition.getId());
        assertEquals(0, getProcessAPI().getNumberOfProcessInstances());

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Ignore("ENGINE-915 - ArchivedProcessInstance.getStartDate() returns null")
    public void getArchivedProcessInstance() throws Exception {
        getArchivedProcessInstances();
    }

    @Test
    public void getArchivedProcessInstanceOrderByLastUpdate() throws Exception {
        getArchivedProcessInstances(ProcessInstanceCriterion.LAST_UPDATE_ASC, 0, 1, 2, ProcessInstanceCriterion.LAST_UPDATE_DESC, 2, 1, 0);
    }

    private void getArchivedProcessInstances() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(false, false));
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);

        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance pi1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance pi2 = getProcessAPI().startProcess(processDefinition.getId());
        // We asked for creation date descending order:
        final List<ArchivedProcessInstance> processInstances;
        checkProcessInstanceIsArchived(pi2);
        processInstances = getProcessAPI().getArchivedProcessInstances(0, 10, ProcessInstanceCriterion.CREATION_DATE_ASC);
        assertEquals(3, processInstances.size());
        //
        final ArchivedProcessInstance returnedPI0 = processInstances.get(0);
        final ArchivedProcessInstance returnedPI1 = processInstances.get(1);
        final ArchivedProcessInstance returnedPI2 = processInstances.get(2);

        System.out.println("process instances : " + processInstances);

        // We check that the retrieved processes are the good ones:
        assertEquals(pi0.getId(), returnedPI0.getId());
        assertEquals(pi1.getId(), returnedPI1.getId());
        assertEquals(pi2.getId(), returnedPI2.getId());

        // First creation date must be after second creation date:
        assertTrue(returnedPI0.getStartDate().before(returnedPI1.getStartDate()));
        // Second creation date must be after third creation date:
        assertTrue(returnedPI1.getStartDate().before(returnedPI2.getStartDate()));

        disableAndDeleteProcess(processDefinition);
        assertEquals(0, getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT).size());
    }

    private void getArchivedProcessInstances(final ProcessInstanceCriterion asc, final int asc1, final int asc2, final int asc3,
            final ProcessInstanceCriterion desc, final int desc1, final int desc2, final int desc3) throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(false, false));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        getProcessAPI().enableProcess(processDefinition.getId());

        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        checkProcessInstanceIsArchived(pi0);

        final ProcessInstance pi1 = getProcessAPI().startProcess(processDefinition.getId());
        checkProcessInstanceIsArchived(pi1);

        final ProcessInstance pi2 = getProcessAPI().startProcess(processDefinition.getId());
        checkProcessInstanceIsArchived(pi2);

        // We asked for creation date descending order:
        List<ArchivedProcessInstance> processInstances = getProcessAPI().getArchivedProcessInstances(0, 10, asc);
        assertEquals(3, processInstances.size());
        assertEquals("completed", processInstances.get(asc1).getState());
        assertEquals(pi0.getId(), processInstances.get(asc1).getSourceObjectId());
        assertEquals("completed", processInstances.get(asc2).getState());
        assertEquals(pi1.getId(), processInstances.get(asc2).getSourceObjectId());
        assertEquals("completed", processInstances.get(asc3).getState());
        assertEquals(pi2.getId(), processInstances.get(asc3).getSourceObjectId());

        processInstances = getProcessAPI().getArchivedProcessInstances(0, 10, desc);
        assertEquals(3, processInstances.size());
        assertEquals(pi0.getId(), processInstances.get(desc1).getSourceObjectId());
        assertEquals(pi1.getId(), processInstances.get(desc2).getSourceObjectId());
        assertEquals(pi2.getId(), processInstances.get(desc3).getSourceObjectId());

        disableAndDeleteProcess(processDefinition);
        assertEquals(0, getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT).size());
    }

    @Test
    public void getNumberOfArchiveProcessInstance() throws Exception {
        getNumberOfArchivedProcessInstances();
    }

    private void getNumberOfArchivedProcessInstances() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("step1"),
                Arrays.asList(false));
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);
        long numberOfProcessInstancesBefore;
        numberOfProcessInstancesBefore = getProcessAPI().getNumberOfArchivedProcessInstances();
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition.getId());

        checkProcessInstanceIsArchived(processInstance1);
        checkProcessInstanceIsArchived(processInstance2);
        checkProcessInstanceIsArchived(processInstance3);
        assertEquals(numberOfProcessInstancesBefore + 3, getProcessAPI().getNumberOfArchivedProcessInstances());

        waitForProcessToFinish(processInstance1);
        waitForProcessToFinish(processInstance2);
        waitForProcessToFinish(processInstance3);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getNoChildrenInstanceIdsFromProcessInstance() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        final List<Long> ids = getProcessAPI().getChildrenInstanceIdsOfProcessInstance(pi0.getId(), 0, 10, ProcessInstanceCriterion.DEFAULT);
        assertEquals(0, ids.size());
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getSingleChildInstanceOfProcessInstance() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("SubProcessInAInstance", PROCESS_VERSION);
        builder.addActor(ACTOR_NAME).addAutomaticTask("step1").addAutomaticTask("step2")
                .addUserTask("userSubTask", ACTOR_NAME).addTransition("step1", "userSubTask").addTransition("userSubTask", "step2");

        final ProcessDefinition subProcess = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);

        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression(subProcess.getName());
        final Expression targetProcessVersionExpr = new ExpressionBuilder().createConstantStringExpression(subProcess.getVersion());

        final ProcessDefinitionBuilder builderProc = new ProcessDefinitionBuilder().createNewInstance("executeInstanceSequentialWithSubProcess", "1.1");
        builderProc.addActor(ACTOR_NAME).addStartEvent("start")
                .addCallActivity("callActivity", targetProcessNameExpr, targetProcessVersionExpr);
        builderProc.addAutomaticTask("step3").addEndEvent("end").addTransition("start", "callActivity").addTransition("callActivity", "step3")
                .addUserTask("userTask", ACTOR_NAME).addTransition("step3", "userTask").addTransition("userTask", "end");

        final DesignProcessDefinition processDefinition = builderProc.done();
        final ProcessDefinition mainProcess = deployAndEnableProcessWithActor(processDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(mainProcess.getId());
        final HumanTaskInstance userTask = waitForUserTaskAndGetIt("userSubTask");

        final List<Long> ids = getProcessAPI().getChildrenInstanceIdsOfProcessInstance(processInstance.getId(), 0, 10, ProcessInstanceCriterion.DEFAULT);
        assertThat(ids).isNotEmpty().hasSize(1).containsExactly(userTask.getParentProcessInstanceId());
        disableAndDeleteProcess(mainProcess, subProcess);
    }

    @Test
    public void getChildrenInstanceIdsOfUnknownProcessInstance() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final List<Long> childrenInstanceIds = getProcessAPI().getChildrenInstanceIdsOfProcessInstance(processInstance.getId() + 1, 0, 10,
                ProcessInstanceCriterion.DEFAULT);
        assertTrue(childrenInstanceIds.isEmpty());
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getProcessInstanceIdFromActivityInstanceId() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());

        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(pi0.getId(), 0, 10);
        for (final ActivityInstance activityInstance : activityInstances) {
            final long processInstanceId = getProcessAPI().getProcessInstanceIdFromActivityInstanceId(activityInstance.getId());
            assertEquals(pi0.getId(), processInstanceId);
        }
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getNumberOfProcessInstances() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        final long initialProcessInstanceNb = getProcessAPI().getNumberOfProcessInstances();

        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition.getId());

        final long finalProcessInstanceNb = getProcessAPI().getNumberOfProcessInstances();
        assertEquals(initialProcessInstanceNb + 3, finalProcessInstanceNb);

        // Clean up
        waitForUserTask(processInstance1, "step1");
        waitForUserTask(processInstance2, "step1");
        waitForUserTask(processInstance3, "step1");

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void getProcessInstances() throws Exception {
        final List<ProcessDefinition> processDefinitions = createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(3, user);
        final List<ProcessInstance> processInstances = startNbProcess(processDefinitions);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.CREATION_DATE_ASC, processInstances);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.CREATION_DATE_DESC, processInstances);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.LAST_UPDATE_DESC, processInstances);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.LAST_UPDATE_ASC, processInstances);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.NAME_ASC, processInstances);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.NAME_DESC, processInstances);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.DEFAULT, processInstances);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.STATE_DESC, processInstances);
        getProcessInstancesOrderByProcessInstanceCriterion(ProcessInstanceCriterion.STATE_ASC, processInstances);
        // Clean up
        disableAndDeleteProcess(processDefinitions);

        // We check that there are no resident process instances in DB:
        assertEquals(0, getProcessAPI().getProcessInstances(0, 10, ProcessInstanceCriterion.DEFAULT).size());
    }

    private void getProcessInstancesOrderByProcessInstanceCriterion(final ProcessInstanceCriterion processInstanceCriterion,
            final List<ProcessInstance> processInstances) {
        // We asked for creation date descending order:
        final List<ProcessInstance> resultProcessInstances = getProcessAPI().getProcessInstances(0, 10, processInstanceCriterion);
        assertEquals(3, resultProcessInstances.size());

        final ProcessInstance returnedPI0 = resultProcessInstances.get(0);
        final ProcessInstance returnedPI1 = resultProcessInstances.get(1);
        final ProcessInstance returnedPI2 = resultProcessInstances.get(2);

        final ProcessInstance pi0 = processInstances.get(0);
        final ProcessInstance pi1 = processInstances.get(1);
        final ProcessInstance pi2 = processInstances.get(2);

        switch (processInstanceCriterion) {
            case STATE_ASC:
                // First state must be before second state :
                assertTrue(returnedPI0.getState().compareToIgnoreCase(returnedPI1.getState()) <= 0);
                // Second state must be before third state :
                assertTrue(returnedPI1.getState().compareToIgnoreCase(returnedPI2.getState()) <= 0);
                break;
            case STATE_DESC:
                // First state must be after second state :
                assertTrue(returnedPI0.getState().compareToIgnoreCase(returnedPI1.getState()) >= 0);
                // Second state must be after third state :
                assertTrue(returnedPI1.getState().compareToIgnoreCase(returnedPI2.getState()) >= 0);
                break;
            case LAST_UPDATE_ASC:
                // First last update must be before second last update :
                assertTrue(returnedPI0.getLastUpdate().before(returnedPI1.getLastUpdate()));
                // Second last update must be before third last update :
                assertTrue(returnedPI1.getLastUpdate().before(returnedPI2.getLastUpdate()));
                break;
            case LAST_UPDATE_DESC:
                // First last update must be after second last update :
                assertTrue(returnedPI0.getLastUpdate().after(returnedPI1.getLastUpdate()));
                // Second last update must be after third last update :
                assertTrue(returnedPI1.getLastUpdate().after(returnedPI2.getLastUpdate()));
                break;
            case NAME_ASC:
                // We check that the retrieved processes are the good ones:
                assertEquals(pi0.getId(), returnedPI0.getId());
                assertEquals(pi1.getId(), returnedPI1.getId());
                assertEquals(pi2.getId(), returnedPI2.getId());

                // First name must be before second name :
                assertTrue(returnedPI0.getName().compareToIgnoreCase(returnedPI1.getName()) <= 0);
                // Second name must be before third name :
                assertTrue(returnedPI1.getName().compareToIgnoreCase(returnedPI2.getName()) <= 0);
                break;
            case NAME_DESC:
                // We check that the retrieved processes are the good ones:
                assertEquals(pi0.getId(), returnedPI2.getId());
                assertEquals(pi1.getId(), returnedPI1.getId());
                assertEquals(pi2.getId(), returnedPI0.getId());

                // First name must be after second name :
                assertTrue(returnedPI0.getName().compareToIgnoreCase(returnedPI1.getName()) >= 0);
                // Second name must be after third name :
                assertTrue(returnedPI1.getName().compareToIgnoreCase(returnedPI2.getName()) >= 0);
                break;
            case CREATION_DATE_ASC:
                // We check that the retrieved processes are the good ones:
                assertEquals(pi0.getId(), returnedPI0.getId());
                assertEquals(pi1.getId(), returnedPI1.getId());
                assertEquals(pi2.getId(), returnedPI2.getId());

                // First creation date must be before second creation date:
                assertTrue(returnedPI0.getStartDate().before(returnedPI1.getStartDate()));
                // Second creation date must be before third creation date:
                assertTrue(returnedPI1.getStartDate().before(returnedPI2.getStartDate()));
                break;
            case CREATION_DATE_DESC:
            case DEFAULT:
            default:
                // We check that the retrieved processes are the good ones:
                assertEquals(pi0.getId(), returnedPI2.getId());
                assertEquals(pi1.getId(), returnedPI1.getId());
                assertEquals(pi2.getId(), returnedPI0.getId());

                // First creation date must be after second creation date:
                assertTrue(returnedPI0.getStartDate().after(returnedPI1.getStartDate()));
                // Second creation date must be after third creation date:
                assertTrue(returnedPI1.getStartDate().after(returnedPI2.getStartDate()));
                break;
        }
    }

    @Test
    public void setProcessInstanceState() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step1");

        final long processInstanceId = processInstance.getId();
        processInstance = getProcessAPI().getProcessInstance(processInstanceId);
        assertEquals("started", processInstance.getState());

        getProcessAPI().setProcessInstanceState(processInstance, "initializing");
        processInstance = getProcessAPI().getProcessInstance(processInstanceId);
        assertEquals("initializing", processInstance.getState());

        getProcessAPI().setProcessInstanceState(processInstance, "started");
        processInstance = getProcessAPI().getProcessInstance(processInstanceId);
        assertEquals("started", processInstance.getState());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class, ProcessInstance.class }, concept = BPMNConcept.PROCESS, keywords = { "Process Instance", "start", "2 times" }, jira = "ENGINE-1094")
    @Test
    public void startProcess2Times() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("initTask1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, APITestUtil.ACTOR_NAME, user);

        // Start process instance first time, and complete it
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "initTask1", user);
        waitForProcessToFinish(processInstance);

        // Start process instance second time
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance2, "initTask1", user);
        waitForProcessToFinish(processInstance2);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "BS-8397", classes = { DataInstance.class, ProcessInstance.class }, concept = BPMNConcept.DATA, keywords = { "initilize process data behalf" })
    @Test
    public void startProcessUsingInitialVariableValuesFor() throws Exception {
        final String otherUserName = "other";
        final User otherUser = createUser(otherUserName, "user");

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("cantResolveDataInExpressionInDataDefaultValue", "1");
        processBuilder.addActor(ACTOR_NAME).addDescription("Process to test archiving mechanism");
        processBuilder.addDoubleData("D", new ExpressionBuilder().createConstantDoubleExpression(3.14));
        processBuilder.addData("bigD", BigDecimal.class.getName(), null);
        processBuilder.addUserTask("step1", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);

        try {
            final Map<String, Serializable> variables = new HashMap<String, Serializable>();
            variables.put("bigD", new BigDecimal("3.141592653589793"));
            final ProcessInstance instance = getProcessAPI().startProcess(otherUser.getId(), processDefinition.getId(), variables);
            final ProcessInstance processInstance2 = getProcessAPI().getProcessInstance(instance.getId());

            DataInstance dataInstance = getProcessAPI().getProcessDataInstance("bigD", instance.getId());
            assertEquals(new BigDecimal("3.141592653589793"), dataInstance.getValue());
            dataInstance = getProcessAPI().getProcessDataInstance("D", instance.getId());
            assertEquals(Double.valueOf(3.14), dataInstance.getValue());

            assertEquals(otherUser.getId(), processInstance2.getStartedBy());
            assertEquals(user.getId(), processInstance2.getStartedBySubstitute());

            // Check system comment
            final SearchOptions searchOptions = new SearchOptionsBuilder(0, 100).filter(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, processInstance2.getId())
                    .done();
            final List<Comment> comments = getProcessAPI().searchComments(searchOptions).getResult();
            boolean haveCommentForDelegate = false;
            for (final Comment comment : comments) {
                haveCommentForDelegate = haveCommentForDelegate
                        || comment.getContent()
                                .contains("The user " + USERNAME + " acting as delegate of the user " + otherUserName + " has started the case.");
            }
            assertTrue(haveCommentForDelegate);
        } finally {
            // Clean up
            disableAndDeleteProcess(processDefinition);
            deleteUsers(otherUser);
        }
    }

    @Test
    public void startProcessInstanceFor() throws Exception {
        final String otherUserName = "other";
        final User otherUser = createUser(otherUserName, "user");

        // create process definition with integer data;
        final String dataName = "var1";
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION).addActor(ACTOR_NAME)
                .addIntegerData(dataName, new ExpressionBuilder().createConstantIntegerExpression(1)).addUserTask("step1", ACTOR_NAME)
                .addAutomaticTask("step2").addTransition("step1", "step2").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, ACTOR_NAME, user);

        try {
            // create Operation keyed map
            final Operation integerOperation = BuildTestUtil.buildIntegerOperation(dataName, 2);
            final List<Operation> operations = new ArrayList<Operation>();
            final Map<String, Serializable> contexts = new HashMap<String, Serializable>();
            contexts.put("page", "1");
            operations.add(integerOperation);
            final long processDefinitionId = processDefinition.getId();
            final ProcessInstance processInstance = getProcessAPI().startProcess(otherUser.getId(), processDefinitionId, operations, contexts);
            final ProcessInstance processInstance2 = getProcessAPI().getProcessInstance(processInstance.getId());
            assertEquals(otherUser.getId(), processInstance2.getStartedBy());
            assertEquals(user.getId(), processInstance2.getStartedBySubstitute());

            // Check system comment
            final SearchOptions searchOptions = new SearchOptionsBuilder(0, 100).filter(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, processInstance.getId()).
                    done();
            final List<Comment> comments = getProcessAPI().searchComments(searchOptions).getResult();
            boolean haveCommentForDelegate = false;
            for (final Comment comment : comments) {
                haveCommentForDelegate = haveCommentForDelegate
                        || comment.getContent()
                                .contains("The user " + USERNAME + " acting as delegate of the user " + otherUserName + " has started the case.");
            }
            assertTrue(haveCommentForDelegate);
        } finally {
            // Clean up
            disableAndDeleteProcess(processDefinition);
            deleteUser(otherUser);
        }
    }

    private List<ProcessInstance> startNbProcess(final List<ProcessDefinition> processDefinitions) throws Exception {
        final List<ProcessInstance> process = new ArrayList<ProcessInstance>();
        for (final ProcessDefinition processDefinition : processDefinitions) {
            process.add(getProcessAPI().startProcess(processDefinition.getId()));
            Thread.sleep(5);// avoid two instances with the same date
        }
        return process;
    }

    private List<ProcessDefinition> createNbProcessDefinitionWithTwoHumanStepsAndDeployWithActor(final int nbProcess, final User user) throws BonitaException {
        return createNbProcessDefinitionWithHumanAndAutomaticAndDeployWithActor(nbProcess, user, Arrays.asList("step1", "step2"), Arrays.asList(true, true));
    }

    @Test
    public void getNumberOfUsersCanExecutePendingHumanTaskDeploymentInfo() throws Exception {
        final User otherUser = createUser("other", "user");

        // create process definition with integer data;
        final String dataName = "var1";
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION).addActor(ACTOR_NAME)
                .addIntegerData(dataName, new ExpressionBuilder().createConstantIntegerExpression(1)).addUserTask("step1", ACTOR_NAME)
                .addAutomaticTask("step2").addTransition("step1", "step2").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, Lists.newArrayList(ACTOR_NAME, ACTOR_NAME),
                Lists.newArrayList(user, otherUser));

        // create Operation keyed map
        final Operation integerOperation = BuildTestUtil.buildIntegerOperation(dataName, 2);

        final ProcessInstance processInstance = getProcessAPI().startProcess(otherUser.getId(), processDefinition.getId(),
                Collections.singletonList(integerOperation), Collections.<String, Serializable> singletonMap("page", "1"));
        final long step1Id = waitForUserTask(processInstance, "step1");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(UserSearchDescriptor.LAST_NAME, Order.DESC);
        final SearchResult<User> results = getProcessAPI().searchUsersWhoCanExecutePendingHumanTask(step1Id, builder.done());
        assertThat(results.getCount()).isSameAs(2L);
        assertThat(results.getResult()).isNotEmpty();
        assertThat(results.getResult().get(0).getId()).isEqualTo(user.getId());
        assertThat(results.getResult().get(1).getId()).isEqualTo(otherUser.getId());

        // Clean up
        disableAndDeleteProcess(processDefinition);
        deleteUser(otherUser);
    }

    @Test
    public void runProcessInstanceWithDefaultFlownode_should_pass_all_human_task() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION).addActor(ACTOR_NAME)
                .addUserTask("step1", ACTOR_NAME).addUserTask("step2", ACTOR_NAME).addDefaultTransition("step1", "step2").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, Lists.newArrayList(ACTOR_NAME, ACTOR_NAME),
                Lists.newArrayList(user));
        final ProcessInstance pi = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(pi, "step1", user);
        waitForUserTaskAndExecuteIt(pi, "step2", user);

        waitForProcessToFinish(pi);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void runProcessInstanceWithDefaultFlownode_and_another_evaluated_to_false_transition_should_passto_step2() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION).addActor(ACTOR_NAME)
                .addUserTask("step1", ACTOR_NAME).addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME).
                addTransition("step1", "step3", new ExpressionBuilder().createConstantBooleanExpression(false)).addDefaultTransition("step1", "step2")
                .getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, Lists.newArrayList(ACTOR_NAME, ACTOR_NAME),
                Lists.newArrayList(user));
        final ProcessInstance pi = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(pi, "step1", user);
        waitForUserTaskAndExecuteIt(pi, "step2", user);

        waitForProcessToFinish(pi);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void runProcessInstanceWithDefaultFlownode_and_another_evaluated_to_true_transition_should_passto_step3() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION).addActor(ACTOR_NAME)
                .addUserTask("step1", ACTOR_NAME).addUserTask("step2", ACTOR_NAME).addUserTask("step3", ACTOR_NAME)
                .addTransition("step1", "step3", new ExpressionBuilder().createConstantBooleanExpression(true)).addDefaultTransition("step1", "step2")
                .getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, Lists.newArrayList(ACTOR_NAME, ACTOR_NAME),
                Lists.newArrayList(user));
        final ProcessInstance pi = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(pi, "step1", user);
        waitForUserTaskAndExecuteIt(pi, "step3", user);

        waitForProcessToFinish(pi);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void runDeleteParentArchivedProcessInstanceAndElements_should_not_delete_process_instance_not_yet_archived() throws Exception {
        final DesignProcessDefinition processDef = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION).addActor(ACTOR_NAME)
                .addUserTask("step1", ACTOR_NAME).addUserTask("step2", ACTOR_NAME).addTransition("step1", "step2").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processDef, Lists.newArrayList(ACTOR_NAME, ACTOR_NAME),
                Lists.newArrayList(user));
        final ProcessInstance pi = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(pi, "step1", user);
        final long nbDeleted = getProcessAPI().deleteArchivedProcessInstances(processDefinition.getId(), 0, 10);

        // there is one archived process instance deleted because the former process_instance state has been archived
        assertThat(nbDeleted).isEqualTo(1);
        waitForUserTaskAndExecuteIt(pi, "step2", user);
        waitForProcessToFinish(pi);
        disableAndDeleteProcess(processDefinition);
    }

}
