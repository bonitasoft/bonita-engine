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

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbOfActivities;
import org.bonitasoft.engine.test.wait.WaitForFinalArchivedActivity;
import org.bonitasoft.engine.test.wait.WaitForStep;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessExecutionTest extends CommonAPITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExecutionTest.class);

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();

    }

    /**
     * there was an issue on deploy when the classloader needed to be refreshed
     * (because of Schemafactory was loading parser not in transaction)
     * 
     * @throws Exception
     */
    @Test
    public void ensureADeployWorksAfterAChangeInDependencies() throws Exception {
        final User user = createUser("john", "bpm");
        final DesignProcessDefinition designProcessDefinition1 = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process123", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition1 = deployAndEnableWithActor(designProcessDefinition1, ACTOR_NAME, user);
        getCommandAPI().addDependency("kikoo", new byte[] { 0, 2, 3 });
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, user);
        disableAndDeleteProcess(processDefinition);
        disableAndDeleteProcess(processDefinition1);
        getCommandAPI().removeDependency("kikoo");
        deleteUser(user);
    }

    @Test
    public void startProcessWithCurrentUser() throws Exception {
        final User user = createUser("john", "bpm");
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        // Check that the current getSession() user name is the one used to start the process:
        LOGGER.debug("current getSession() user name used to start the process: " + processInstance.getStartedBy());
        assertEquals(getSession().getUserId(), processInstance.getStartedBy());

        // Clean up
        waitForUserTask("step1", processInstance);
        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    // @Ignore
    @Test
    public void startProcessOnBehalfOf() throws Exception {
        final User user = createUser("john", "bpm");
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(user.getId(), processDefinition.getId());

        // Check that the given user name is the one used to start the process:
        assertEquals(user.getId(), processInstance.getStartedBy());

        // Clean up
        waitForUserTask("step1", processInstance);
        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void createAndExecuteProcessActivity() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, false));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        final String johnName = "john";
        createUserAndLoginWith(johnName);
        assignFirstActorToMe(processDefinition);

        getProcessAPI().enableProcess(processDefinition.getId());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertTrue("expected an activity",
                new CheckNbOfActivities(getProcessAPI(), 20, 500, true, processInstance, 1, TestStates.getReadyState("step1")).waitUntil());

        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 200);
        final ActivityInstance step1 = activities.get(0);
        assertEquals("step1", step1.getName());
        assertEquals(TestStates.getReadyState(step1), step1.getState());
        assignAndExecuteStep(step1, getSession().getUserId());
        try {
            getProcessAPI().getActivityInstance(step1.getId());
            fail("should not be able to retrieve the step");
        } catch (final ActivityInstanceNotFoundException e) {
            // ok
        } finally {
            // Clean up
            waitForProcessToFinish(processInstance);
            disableAndDeleteProcess(processDefinition);
            deleteUser(johnName);
        }
    }

    private void assignFirstActorToMe(final ProcessDefinition processDefinition) throws BonitaException {
        final List<ActorInstance> actors = getProcessAPI().getActors(processDefinition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);
        final User user = getIdentityAPI().getUserByUserName(getSession().getUserName());
        getProcessAPI().addUserToActor(actor.getId(), user.getId());
    }

    @Test(expected = InvalidProcessDefinitionException.class)
    public void createInvalidProcessAttributes() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("Double-hyphen -- test", "any");
        builder.setActorInitiator("toto");
        builder.done();
    }

    private User createUserAndLoginWith(final String userName) throws BonitaException {
        final User user = createUser(userName, "bpm");
        logout();
        loginWith(userName, "bpm");
        return user;
    }

    // @Test
    // public void createAndExecuteProcessWith2Branches() throws Exception {
    // final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("My_Process_with_branches", "1.0")
    // .addAutomaticTask("step1")
    // .addUserTask("step2", "admin")
    // .addUserTask("step3", "admin")
    // .addTransition("step1", "step2")
    // .addTransition("step1", "step3")
    // .getProcess();
    //
    // final ProcessDefinition processDefinition = deployAndEnable(designProcessDefinition);
    // final ProcessInstance processInstance = processManagementAPI.start(-1, processDefinition.getId());
    //
    // assertTrue("expected 2 activities", new CheckNbOfActivities(20, 500, true, processInstance, 2).waitUntil());
    // final Set<ActivityInstance> activities = processRuntimeAPI.getActivities(processInstance.getId(), 0, 200);
    // assertEquals(2, activities.size());
    // final Iterator<ActivityInstance> iterator = activities.iterator();
    // final ActivityInstance step = iterator.next();
    // ActivityInstance step2;
    // ActivityInstance step3;
    // if (step.getName().equals("step2")) {
    // step2 = step;
    // step3 = iterator.next();
    // } else {
    // step2 = iterator.next();
    // step3 = step;
    // }
    //
    // assertEquals(TestStates.getStartedState(processInstance), processInstance.getStateId());
    //
    // assertEquals("step2", step2.getName());
    // assertEquals(TestStates.getReadyState(step2), step2.getStateId());
    // processRuntimeAPI.executeActivity(step2.getId());
    // step2 = processRuntimeAPI.getActivityInstance(step2.getId());
    // assertEquals(TestStates.getNormalFinalState(step2), step2.getStateId());
    //
    // assertEquals("step3", step3.getName());
    // assertEquals(TestStates.getReadyState(step3), step3.getStateId());
    // processRuntimeAPI.executeActivity(step3.getId());
    // final ArchivedActivityInstance archivedStep3 = processRuntimeAPI.getArchivedActivityInstance(step3.getId());
    // assertEquals(TestStates.getNormalFinalState(archivedStep3), archivedStep3.getStateId());
    //
    // assertEquals(TestStates.getNormalFinalState(processInstance), processRuntimeAPI.getArchivedProcessInstance(processInstance.getId()).getStateId());
    // disableAndDelete(processDefinition);
    // }

    @Test
    public void createAndExecuteProcessWithAutomaticSteps() throws Exception {
        final User user = createUser("john", "bpm");
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.1",
                Arrays.asList("step1", "step2"), Arrays.asList(false, false));

        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(user.getId(), processDefinition.getId());
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
        deleteUser(user.getId());
    }

    // @Test
    // public void createAndExecuteProcessWithAutomaticStepsAndUserTask() throws Exception {
    // final DesignProcessDefinition designProcessDefinition = BPMTestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.2",
    // Arrays.asList("step1", "step2"), Arrays.asList(false, true));
    //
    // final ProcessDefinition processDefinition = deployAndEnable(designProcessDefinition);
    // final ProcessInstance processInstance = processRuntimeAPI.start(processDefinition.getId());
    //
    // assertNotSame(TestStates.getNormalFinalState(processInstance), processInstance.getStateId());// FIXME
    //
    // assertTrue("expected 1 activities", new CheckNbOfActivities(20, 500, true, processInstance, 1).waitUntil());
    //
    // final Set<ActivityInstance> activities = processRuntimeAPI.getActivities(processInstance.getId(), 0, 200);
    // final ActivityInstance step2 = activities.iterator().next();
    //
    // assertEquals("step2", step2.getName());
    //
    // processRuntimeAPI.executeActivity(step2.getId());
    //
    // assertEquals(TestStates.getNormalFinalState(processInstance), processRuntimeAPI.getArchivedProcessInstance(processInstance.getId()).getStateId());//
    // FIXME
    //
    // disableAndDelete(processDefinition);
    // }

    @Test(expected = DeletionException.class)
    public void deleteUnknownProcess() throws Exception {
        getProcessAPI().deleteProcess(123456789);
    }

    @Test
    public void executeProcessWithNoActivities() throws Exception {
        final User user = createUser("john", "bpm");
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.3",
                Collections.<String> emptyList(), Collections.<Boolean> emptyList());
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(user.getId(), processDefinition.getId());
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processDefinition.getId(), 0, 200);
        assertEquals(0, activities.size());

        assertTrue("Process instance should be completed",
                containsState(getProcessAPI().getArchivedProcessInstances(processInstance.getId(), 0, 10), TestStates.getNormalFinalState(processInstance)));// FIXME

        disableAndDeleteProcess(processDefinition);
        deleteUser(user.getId());
    }

    @Test
    public void checkStartAndEndDate() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_ProcessToCheckDate", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        final User user = createUserAndLoginWith("john");
        assignFirstActorToMe(processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        long before = new Date().getTime();
        Thread.sleep(10);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        Thread.sleep(10);
        long after = new Date().getTime();
        final long startDate = processInstance.getStartDate().getTime();
        assertTrue("The process instance must start between " + before + " and " + after + ", but was " + startDate, after >= startDate && startDate >= before);
        assertEquals(getSession().getUserId(), processInstance.getStartedBy());
        assertTrue("expected 1 activity",
                new CheckNbOfActivities(getProcessAPI(), 20, 500, true, processInstance, 1, TestStates.getReadyState(null)).waitUntil());
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 200);
        final ActivityInstance step1 = activities.get(0);
        before = new Date().getTime();
        assignAndExecuteStep(step1, user.getId());
        waitForProcessToFinish(processInstance);
        after = new Date().getTime();
        final long endDate = getProcessAPI().getFinalArchivedProcessInstance(processInstance.getId()).getEndDate().getTime();
        assertTrue("The process instance must finish between " + before + " and " + after + ", but was " + endDate, after >= endDate && endDate >= before);

        disableAndDeleteProcess(processDefinition);
        deleteUser("john");
    }

    @Test
    public void checkLastUpdateDateOfAnArchivedProcess() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_ProcessToCheckDate", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        final User user = createUserAndLoginWith("john");
        assignFirstActorToMe(processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        long before = new Date().getTime();
        Thread.sleep(10);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        Thread.sleep(10);
        long after = new Date().getTime();
        final long processStartDate = processInstance.getStartDate().getTime();
        assertTrue("The process instance " + processInstance.getName() + " must start between <" + before + "> and <" + after + ">, but was <"
                + processStartDate + ">", after >= processStartDate && processStartDate >= before);
        assertEquals(getSession().getUserId(), processInstance.getStartedBy());
        assertTrue("expected 1 activity",
                new CheckNbOfActivities(getProcessAPI(), 20, 500, true, processInstance, 1, TestStates.getReadyState(null)).waitUntil());

        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 200);
        final ActivityInstance step1 = activities.get(0);
        before = new Date().getTime();
        assignAndExecuteStep(step1, user.getId());
        waitForProcessToFinish(processInstance);
        after = new Date().getTime();
        final long lastUpdate = getProcessAPI().getFinalArchivedProcessInstance(processInstance.getId()).getLastUpdate().getTime();
        assertTrue("The process instance " + processInstance.getName() + " must update in last between <" + before + "> and <" + after + ">, but was <"
                + lastUpdate + ">", after >= lastUpdate && lastUpdate >= before);
        disableAndDeleteProcess(processDefinition);
        deleteUser("john");
    }

    @Test
    public void checkProcessIsArchived() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("ProcessToArchive", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        final User user = createUserAndLoginWith("john");
        assignFirstActorToMe(processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance);

        final List<ArchivedProcessInstance> archs = getProcessAPI().getArchivedProcessInstances(processInstance.getId(), 0, 100);
        assertEquals(1, archs.size());
        assertEquals(TestStates.getInitialState(processInstance), archs.get(0).getState());

        assignAndExecuteStep(step1, user.getId());
        waitForProcessToFinish(processInstance);
        final ArchivedProcessInstance archivedProcessInstance = getProcessAPI().getFinalArchivedProcessInstance(processInstance.getId());
        assertNotNull(archivedProcessInstance);
        try {
            getProcessAPI().getProcessInstance(processInstance.getId());
            fail("A ProcessInstanceNotFoundException should have been raised");
        } catch (final ProcessInstanceNotFoundException e) {
            // ok
        }
        disableAndDeleteProcess(processDefinition);
        deleteUser("john");
    }

    @Test
    @Cover(classes = Connector.class, concept = BPMNConcept.PROCESS, keywords = { "archive", "process" }, jira = "ENGINE-507", story = "get a archived process instance by id")
    public void getArchivedProcessInstanceById() throws Exception {
        final User john = createUser("john", "bpm");
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("ProcessToArchive", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, APITestUtil.ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("step1", processInstance);
        final List<ArchivedProcessInstance> archs = getProcessAPI().getArchivedProcessInstances(processInstance.getId(), 0, 100);
        assertEquals(1, archs.size());
        final ArchivedProcessInstance archivedProcessInstance = archs.get(0);
        assertEquals(archivedProcessInstance, getProcessAPI().getArchivedProcessInstance(archivedProcessInstance.getId()));
        disableAndDeleteProcess(processDefinition);
        deleteUser(john);
    }

    @Test(expected = ArchivedProcessInstanceNotFoundException.class)
    @Cover(classes = Connector.class, concept = BPMNConcept.PROCESS, keywords = { "archive", "process" }, jira = "ENGINE-507", story = "get a archived process instance by an unknown id throw a not found exception")
    public void getArchivedProcessInstanceByIdNotFound() throws Exception {
        getProcessAPI().getArchivedProcessInstance(123456789l);
    }

    @Test
    public void checkArchiveDate() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("ProcessToArchive", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        final User user = createUserAndLoginWith("john");
        assignFirstActorToMe(processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertTrue("Expected an activity",
                new CheckNbOfActivities(getProcessAPI(), 50, 1000, true, processInstance, 1, TestStates.getReadyState(null)).waitUntil());
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 200);
        final ActivityInstance step1 = activities.get(0);
        final long before = new Date().getTime();
        assignAndExecuteStep(step1, user.getId());
        waitForProcessToFinish(processInstance);
        final long after = new Date().getTime();
        final ArchivedProcessInstance archivedProcessInstance = getProcessAPI().getFinalArchivedProcessInstance(processInstance.getId());
        assertNotNull(archivedProcessInstance);
        long archiveDate = archivedProcessInstance.getArchiveDate().getTime();
        assertTrue("The process must be archived between " + before + " and " + after + ", but was " + archiveDate, after >= archiveDate
                && archiveDate >= before);
        final ArchivedActivityInstance archivedActivityInstance = getProcessAPI().getArchivedActivityInstance(step1.getId());
        assertNotNull(archivedActivityInstance);
        assertEquals(step1.getId(), archivedActivityInstance.getSourceObjectId());
        archiveDate = archivedActivityInstance.getArchiveDate().getTime();
        assertTrue("The step1 must be archived between " + before + " and " + after + ", but was " + archiveDate, after >= archiveDate && archiveDate >= before);
        disableAndDeleteProcess(processDefinition);
        deleteUser("john");
    }

    @Test
    public void checkArchiveStartedBy() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("ProcessToArchive", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        final User user = createUserAndLoginWith("john");
        assignFirstActorToMe(processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(user.getId(), processInstance.getStartedBy());
        assertTrue("Expected an activity",
                new CheckNbOfActivities(getProcessAPI(), 50, 1000, true, processInstance, 1, TestStates.getReadyState(null)).waitUntil());
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 200);
        final ActivityInstance step1 = activities.get(0);
        assignAndExecuteStep(step1, user.getId());
        waitForProcessToFinish(processInstance);
        final ArchivedProcessInstance archivedProcessInstance = getProcessAPI().getFinalArchivedProcessInstance(processInstance.getId());
        assertNotNull(archivedProcessInstance);
        assertEquals(user.getId(), archivedProcessInstance.getStartedBy());
        disableAndDeleteProcess(processDefinition);
        deleteUser("john");
    }

    @Test
    public void activityDisplayDescriptionUndefined() throws Exception {
        // create process definition;
        final String processName = "a";
        final String version = "0.1beta";
        final User user = createUser("toto", "titi");
        final String actorName = "actor";
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(processName, version).addActor(actorName)
                .addAutomaticTask("auto1").addUserTask("task1", actorName).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, actorName, user);
        final long processDefId = processDefinition.getId();
        final ProcessInstance pi = getProcessAPI().startProcess(processDefId);

        final WaitForFinalArchivedActivity wait = waitForFinalArchivedActivity("auto1", pi);
        final ArchivedActivityInstance archivedActivityInstance = wait.getResult();

        assertEquals(null, archivedActivityInstance.getDisplayDescription());

        final WaitForStep waitForStep = waitForStep(200, 1000, "task1", pi);
        final ActivityInstance activityInstance = waitForStep.getResult();

        assertEquals(null, activityInstance.getDisplayDescription());

        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void updateDueDateOfTask() throws Exception {
        final String johnName = "john";
        final User user = createUserAndLoginWith(johnName);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processToUpdateDueDate", "1.0");
        builder.addActor("johnny");
        builder.addUserTask("step1", "johnny").addExpectedDuration(10000000l);
        final DesignProcessDefinition done = builder.done();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(done, "johnny", user);

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance waitForUserTask = waitForUserTask("step1", processInstance);
        final Date expectedEndDate = ((UserTaskInstance) waitForUserTask).getExpectedEndDate();
        final Date now = new Date();
        assertNotSame(now, expectedEndDate);
        getProcessAPI().updateDueDateOfTask(waitForUserTask.getId(), now);
        final ActivityInstance activityInstance = getProcessAPI().getActivityInstance(waitForUserTask.getId());
        final Date expectedEndDate2 = ((UserTaskInstance) activityInstance).getExpectedEndDate();
        assertEquals(now, expectedEndDate2);
        disableAndDeleteProcess(processDefinition);
        deleteUser(johnName);
    }

    @Test(expected = UpdateException.class)
    public void updateDueDateOfUnknownTask() throws Exception {
        getProcessAPI().updateDueDateOfTask(123456789l, new Date());
    }

    @Test(expected = UpdateException.class)
    public void updateDueDateOfTaskWithNullDate() throws Exception {
        getProcessAPI().updateDueDateOfTask(123456789l, null);
    }

    @Test
    public void executeTaskOnBehalf() throws Exception {
        final DesignProcessDefinition designProcessDefinition = APITestUtil.createProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final String johnName = "john";
        final User user = createUserAndLoginWith(johnName);
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, APITestUtil.ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        // execute step 1 using john
        final ActivityInstance step1 = waitForUserTask("step1", processInstance.getId());
        assertEquals(0, step1.getExecutedBy());
        assignAndExecuteStep(step1, user.getId());
        waitForUserTask("step2", processInstance);

        // check that the step1 was executed by john
        final ArchivedActivityInstance step1Archived = getProcessAPI().getArchivedActivityInstance(step1.getId());
        assertEquals(user.getId(), step1Archived.getExecutedBy());

        // clean
        disableAndDeleteProcess(processDefinition);
        deleteUser(johnName);
    }

    @Test
    public void systemComments() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor("actor");
        builder.addUserTask("step1", "actor").addDisplayName(new ExpressionBuilder().createConstantStringExpression("Step1 display name"));
        builder.addUserTask("step2", "actor");
        builder.addUserTask("step3", "actor");
        builder.addTransition("step1", "step2");
        builder.addTransition("step2", "step3");
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(builder.done()).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        final User user = createUserAndLoginWith("Tom");
        assignFirstActorToMe(processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance.getId());
        assignAndExecuteStep(step1, user.getId());
        waitForUserTaskAndExecuteIt("step2", processInstance, user.getId());
        waitForUserTask("step3", processInstance.getId());

        final SearchResult<Comment> searchResult0 = getProcessAPI().searchComments(new SearchOptionsBuilder(0, 5).done());
        final List<Comment> commentList0 = searchResult0.getResult();
        assertEquals(2, commentList0.size());
        assertEquals("The task \"Step1 display name\" is now assigned to Tom", commentList0.get(0).getContent());
        assertEquals("The task \"step2\" is now assigned to Tom", commentList0.get(1).getContent());

        // test number of comments using the method countComments
        final SearchOptionsBuilder builder1 = new SearchOptionsBuilder(0, 5);
        assertEquals(2, getProcessAPI().countComments(builder1.done()));

        disableAndDeleteProcess(processDefinition);
        deleteUser("Tom");
    }
}
