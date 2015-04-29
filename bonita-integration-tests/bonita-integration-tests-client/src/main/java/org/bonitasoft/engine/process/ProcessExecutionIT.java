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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessExecutionIT extends TestWithUser {

    @Rule
    public ExpectedException expectdException = ExpectedException.none();

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExecutionIT.class);

    /**
     * there was an issue on deploy when the classloader needed to be refreshed
     * (because of Schemafactory was loading parser not in transaction)
     *
     * @throws Exception
     */
    @Test
    public void ensureADeployWorksAfterAChangeInDependencies() throws Exception {
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process123", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(designProcessDefinition1, ACTOR_NAME, user);
        getCommandAPI().addDependency("kikoo", new byte[] { 0, 2, 3 });
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(processDefinition1);
        getCommandAPI().removeDependency("kikoo");
    }

    @Test
    public void startProcessWithCurrentUser() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        // Check that the current getSession() user name is the one used to start the process:
        LOGGER.debug("current getSession() user name used to start the process: " + processInstance.getStartedBy());
        assertEquals(getSession().getUserId(), processInstance.getStartedBy());

        // Clean up
        waitForUserTask(processInstance, "step1");
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void startProcessFor() throws Exception {
        final User jack = createUser("jack", PASSWORD);

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(jack.getId(), processDefinition.getId());

        try {
            waitForUserTask(processInstance, "step1");
            // Check that the given user name is the one used to start the process:
            assertEquals(jack.getId(), processInstance.getStartedBy());
            assertEquals(user.getId(), processInstance.getStartedBySubstitute());

            // Check system comment
            final SearchOptions searchOptions = new SearchOptionsBuilder(0, 100).filter(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, processInstance.getId()).
                    done();
            final List<Comment> comments = getProcessAPI().searchComments(searchOptions).getResult();
            boolean haveCommentForDelegate = false;
            for (final Comment comment : comments) {
                haveCommentForDelegate = haveCommentForDelegate
                        || comment.getContent().contains("The user " + USERNAME + " acting as delegate of the user jack has started the case.");
            }
            assertTrue(haveCommentForDelegate);
        } finally {
            // Clean up
            disableAndDeleteProcess(processDefinition);
            deleteUsers(jack);
        }
    }

    @Test
    public void createAndExecuteProcessWithAutomaticSteps() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.1",
                Arrays.asList("step1", "step2"), Arrays.asList(false, false));

        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(user.getId(), processDefinition.getId());
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void deleteUnknownProcess() throws Exception {
        expectdException.expect(DeletionException.class);
        getProcessAPI().deleteProcessDefinition(123456789);
    }

    @Test
    public void executeProcessWithNoActivities() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.3",
                Collections.<String> emptyList(), Collections.<Boolean> emptyList());
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final List<ActivityInstance> activities = getProcessAPI().getActivities(processDefinition.getId(), 0, 200);
        assertEquals(0, activities.size());
        assertTrue("Process instance should be completed",
                containsState(getProcessAPI().getArchivedProcessInstances(processInstance.getId(), 0, 10), TestStates.NORMAL_FINAL));// FIXME

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void checkStartAndEndDate() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_ProcessToCheckDate",
                "1.0", Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        long before = new Date().getTime();
        Thread.sleep(10);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        Thread.sleep(10);
        long after = new Date().getTime();
        final long startDate = processInstance.getStartDate().getTime();
        assertTrue("The process instance must start between " + before + " and " + after + ", but was " + startDate, after >= startDate && startDate >= before);
        assertEquals(getSession().getUserId(), processInstance.getStartedBy());

        final Long step1Id = waitForUserTask("step1");
        before = new Date().getTime();
        assignAndExecuteStep(step1Id, user);
        waitForProcessToFinish(processInstance);
        after = new Date().getTime();
        final long endDate = getProcessAPI().getFinalArchivedProcessInstance(processInstance.getId()).getEndDate().getTime();
        assertTrue("The process instance must finish between " + before + " and " + after + ", but was " + endDate, after >= endDate && endDate >= before);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void checkLastUpdateDateOfAnArchivedProcess() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_ProcessToCheckDate",
                "1.0", Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        long before = new Date().getTime();
        Thread.sleep(10);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        Thread.sleep(10);
        long after = new Date().getTime();
        final long processStartDate = processInstance.getStartDate().getTime();
        assertTrue("The process instance " + processInstance.getName() + " must start between <" + before + "> and <" + after + ">, but was <"
                + processStartDate + ">", after >= processStartDate && processStartDate >= before);
        assertEquals(getSession().getUserId(), processInstance.getStartedBy());

        final Long step1Id = waitForUserTask("step1");
        before = new Date().getTime();
        assignAndExecuteStep(step1Id, user);
        waitForProcessToFinish(processInstance);
        after = new Date().getTime();
        final long lastUpdate = getProcessAPI().getFinalArchivedProcessInstance(processInstance.getId()).getLastUpdate().getTime();
        assertTrue("The process instance " + processInstance.getName() + " must update in last between <" + before + "> and <" + after + ">, but was <"
                + lastUpdate + ">", after >= lastUpdate && lastUpdate >= before);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, jira = "BS-11970", keywords = { "Archive", "Process instance", "Start event",
            "End event" })
    @Test
    public void checkProcessIsArchived() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        builder.addActor(ACTOR_NAME);
        builder.addStartEvent("start");
        builder.addUserTask("step1", ACTOR_NAME);
        builder.addEndEvent("end");
        builder.addTransition("start", "step1").addTransition("step1", "end");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final Long step1Id = waitForUserTask(processInstance, "step1");

        final List<ArchivedProcessInstance> archs = getProcessAPI().getArchivedProcessInstances(processInstance.getId(), 0, 100);
        assertEquals(1, archs.size());
        assertEquals(TestStates.INITIALIZING.getStateName(), archs.get(0).getState());

        assignAndExecuteStep(step1Id, user);
        waitForProcessToFinish(processInstance);

        // Verify if the process instance is archived
        final ArchivedProcessInstance archivedProcessInstance = getProcessAPI().getFinalArchivedProcessInstance(processInstance.getId());
        assertNotNull(archivedProcessInstance);
        try {
            getProcessAPI().getProcessInstance(processInstance.getId());
            fail("A ProcessInstanceNotFoundException should have been raised");
        } catch (final ProcessInstanceNotFoundException e) {
            // ok
        }

        // Verify if the flow node instances are archived
        final SearchOptionsBuilder optionsBuilder = new SearchOptionsBuilder(0, 20);
        optionsBuilder.sort(ArchivedFlowNodeInstanceSearchDescriptor.NAME, Order.ASC);
        optionsBuilder.filter(ArchivedFlowNodeInstanceSearchDescriptor.STATE_NAME, "completed");
        final List<ArchivedFlowNodeInstance> archivedFlowNodeInstances = getProcessAPI().searchArchivedFlowNodeInstances(optionsBuilder.done()).getResult();
        // To uncomment if need to fix BS-11970
        //        assertEquals(3, archivedFlowNodeInstances.size());
        //        assertEquals("end", archivedFlowNodeInstances.get(0).getName());
        //        assertEquals("start", archivedFlowNodeInstances.get(1).getName());
        //        assertEquals("step1", archivedFlowNodeInstances.get(2).getName());
        // To remove if need to fix BS-11970
        assertEquals(1, archivedFlowNodeInstances.size());
        assertEquals("step1", archivedFlowNodeInstances.get(0).getName());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    @Cover(classes = Connector.class, concept = BPMNConcept.PROCESS, keywords = { "archive", "process" }, jira = "ENGINE-507", story = "get a archived process instance by id")
    public void getArchivedProcessInstanceById() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("ProcessToArchive", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, APITestUtil.ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "step1");
        final List<ArchivedProcessInstance> archs = getProcessAPI().getArchivedProcessInstances(processInstance.getId(), 0, 100);
        assertEquals(1, archs.size());
        final ArchivedProcessInstance archivedProcessInstance = archs.get(0);
        assertEquals(archivedProcessInstance, getProcessAPI().getArchivedProcessInstance(archivedProcessInstance.getId()));
        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = ArchivedProcessInstanceNotFoundException.class)
    @Cover(classes = Connector.class, concept = BPMNConcept.PROCESS, keywords = { "archive", "process" }, jira = "ENGINE-507", story = "get a archived process instance by an unknown id throw a not found exception")
    public void getArchivedProcessInstanceByIdNotFound() throws Exception {
        getProcessAPI().getArchivedProcessInstance(123456789L);
    }

    @Test
    public void checkArchiveDate() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_ProcessToCheckDate",
                "1.0", Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask(processInstance, "step1");
        final long before = new Date().getTime();
        assignAndExecuteStep(step1Id, user.getId());
        waitForProcessToFinish(processInstance);
        final long after = new Date().getTime();
        final ArchivedProcessInstance archivedProcessInstance = getProcessAPI().getFinalArchivedProcessInstance(processInstance.getId());
        assertNotNull(archivedProcessInstance);
        long archiveDate = archivedProcessInstance.getArchiveDate().getTime();
        assertTrue("The process must be archived between " + before + " and " + after + ", but was " + archiveDate, after >= archiveDate
                && archiveDate >= before);

        final ArchivedActivityInstance archivedActivityInstance = getProcessAPI().getArchivedActivityInstance(step1Id);
        assertNotNull(archivedActivityInstance);
        assertEquals(step1Id, archivedActivityInstance.getSourceObjectId());
        archiveDate = archivedActivityInstance.getArchiveDate().getTime();
        assertTrue("The step1 must be archived between " + before + " and " + after + ", but was " + archiveDate, after >= archiveDate && archiveDate >= before);

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void checkArchiveStartedBy() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_ProcessToCheckDate",
                "1.0", Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(user.getId(), processInstance.getStartedBy());

        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        waitForProcessToFinish(processInstance);
        final ArchivedProcessInstance archivedProcessInstance = getProcessAPI().getFinalArchivedProcessInstance(processInstance.getId());
        assertNotNull(archivedProcessInstance);
        assertEquals(user.getId(), archivedProcessInstance.getStartedBy());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void activityDisplayDescriptionUndefined() throws Exception {
        // create process definition;
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION)
                .addActor(ACTOR_NAME).addAutomaticTask("auto1").addUserTask("task1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi = getProcessAPI().startProcess(processDefinition.getId());

        final ArchivedActivityInstance auto1 = waitForActivityInCompletedState(pi, "auto1", true);
        assertEquals(null, auto1.getDisplayDescription());

        final ActivityInstance activityInstance = waitForTaskInState(pi, "task1", TestStates.READY);
        assertEquals(null, activityInstance.getDisplayDescription());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void updateDueDateOfTask() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processToUpdateDueDate", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME).addExpectedDuration(10000000L);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTaskAndGetIt(processInstance, "step1");
        final Date expectedEndDate = ((UserTaskInstance) step1).getExpectedEndDate();
        final Date now = new Date();
        assertNotSame(now, expectedEndDate);
        getProcessAPI().updateDueDateOfTask(step1.getId(), now);
        final ActivityInstance activityInstance = getProcessAPI().getActivityInstance(step1.getId());
        final Date expectedEndDate2 = ((UserTaskInstance) activityInstance).getExpectedEndDate();
        assertEquals(now, expectedEndDate2);

        disableAndDeleteProcess(processDefinition);
    }

    @Test(expected = UpdateException.class)
    public void updateDueDateOfUnknownTask() throws Exception {
        getProcessAPI().updateDueDateOfTask(123456789L, new Date());
    }

    @Test
    public void executeTaskFor() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final User jack = createUser("jack", PASSWORD);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, APITestUtil.ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        try {
            // execute step 1 using john
            final ActivityInstance step1 = waitForUserTaskAndGetIt(processInstance, "step1");
            assertEquals(0, step1.getExecutedBy());

            getProcessAPI().assignUserTask(step1.getId(), jack.getId());
            getProcessAPI().executeFlowNode(jack.getId(), step1.getId());
            waitForUserTask(processInstance, "step2");

            // check that the step1 was executed by john
            final ArchivedActivityInstance step1Archived = getProcessAPI().getArchivedActivityInstance(step1.getId());
            assertEquals(jack.getId(), step1Archived.getExecutedBy());
            assertEquals(user.getId(), step1Archived.getExecutedBySubstitute());

            // Check system comment
            final SearchOptions searchOptions = new SearchOptionsBuilder(0, 100).filter(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, processInstance.getId()).
                    done();
            final List<Comment> comments = getProcessAPI().searchComments(searchOptions).getResult();
            boolean haveCommentForDelegate = false;
            for (final Comment comment : comments) {
                haveCommentForDelegate = haveCommentForDelegate
                        || comment.getContent().contains("The user " + USERNAME + " acting as delegate of the user jack has done the task \"step1\".");
            }
            assertTrue(haveCommentForDelegate);
        } finally {
            // clean
            disableAndDeleteProcess(processDefinition);
            deleteUsers(jack);
        }
    }

    @Test
    public void systemCommentsShouldBeAutoAddedAtTaskAssignment() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask("step1", ACTOR_NAME).addDisplayName(new ExpressionBuilder().createConstantStringExpression("Step1 display name"));
        builder.addUserTask("step2", ACTOR_NAME);
        builder.addTransition("step1", "step2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.done(), APITestUtil.ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);
        waitForUserTask(processInstance, "step2");

        final SearchResult<Comment> searchResult = getProcessAPI().searchComments(new SearchOptionsBuilder(0, 5).done());
        final List<Comment> commentList = searchResult.getResult();
        assertEquals(1, commentList.size());
        assertEquals("The task \"Step1 display name\" is now assigned to " + USERNAME, commentList.get(0).getContent());

        assertEquals(1, getProcessAPI().countComments(new SearchOptionsBuilder(0, 5).done()));

        disableAndDeleteProcess(processDefinition);
    }

}
