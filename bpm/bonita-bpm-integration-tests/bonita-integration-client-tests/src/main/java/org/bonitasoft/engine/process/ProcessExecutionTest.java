package org.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.comment.Comment;
import org.bonitasoft.engine.bpm.comment.SearchCommentsDescriptor;
import org.bonitasoft.engine.bpm.data.ArchivedDataInstance;
import org.bonitasoft.engine.bpm.data.ArchivedDataNotFoundException;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.UserTaskDefinitionBuilder;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.APITestUtil;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbOfActivities;
import org.bonitasoft.engine.test.wait.WaitForFinalArchivedActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessExecutionTest extends CommonAPITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessExecutionTest.class);

    @After
    public void afterTest() throws BonitaException {
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();

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
        deleteUser(user);
    }

    @Test
    public void startProcessWithCurrentUser() throws Exception {
        final User user = createUser("john", "bpm");
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        // Check that the current getSession() user name is the one used to start the process:
        LOGGER.debug("current getSession() user name used to start the process: " + processInstance.getStartedBy());
        assertEquals(getSession().getUserId(), processInstance.getStartedBy());

        // Clean up
        waitForUserTask("step1", processInstance);
        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void startProcessFor() throws Exception {
        final User user = createUser("john", "bpm");
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(user.getId(), processDefinition.getId());

        try {
            waitForUserTask("step1", processInstance);
            // Check that the given user name is the one used to start the process:
            assertEquals(user.getId(), processInstance.getStartedBy());
            assertEquals(-1, processInstance.getStartedBySubstitute());

            // Check system comment
            final SearchOptions searchOptions = new SearchOptionsBuilder(0, 100).filter(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, processInstance.getId()).
                    done();
            final List<Comment> comments = getProcessAPI().searchComments(searchOptions).getResult();
            boolean haveCommentForDelegate = false;
            for (final Comment comment : comments) {
                haveCommentForDelegate = haveCommentForDelegate
                        || comment.getContent().contains("The user install acting as delegate of the user john has started the case.");
            }
            assertTrue(haveCommentForDelegate);
        } finally {
            // Clean up
            disableAndDeleteProcess(processDefinition);
            deleteUser(user);
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

    private User createUserAndloginOnDefaultTenantWith(final String userName) throws BonitaException {
        final User user = createUser(userName, "bpm");
        logoutOnTenant();
        loginOnDefaultTenantWith(userName, "bpm");
        return user;
    }

    @Test
    public void createAndExecuteProcessWithAutomaticSteps() throws Exception {
        final User user = createUser("john", "bpm");
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.1",
                Arrays.asList("step1", "step2"), Arrays.asList(false, false));

        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(user.getId(), processDefinition.getId());
        waitForProcessToFinish(processInstance);

        disableAndDeleteProcess(processDefinition);
        deleteUser(user.getId());
    }

    @Test
    public void deleteUnknownProcess() throws Exception {
        getProcessAPI().deleteProcessDefinition(123456789);
    }

    @Test
    public void executeProcessWithNoActivities() throws Exception {
        final User user = createUser("john", "bpm");
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.3",
                Collections.<String> emptyList(), Collections.<Boolean> emptyList());
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processDefinition.getId(), 0, 200);
        assertEquals(0, activities.size());

        assertTrue("Process instance should be completed",
                containsState(getProcessAPI().getArchivedProcessInstances(processInstance.getId(), 0, 10), TestStates.getNormalFinalState()));// FIXME

        disableAndDeleteProcess(processDefinition);
        deleteUser(user.getId());
    }

    @Test
    public void checkStartAndEndDate() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_ProcessToCheckDate",
                "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        final User user = createUserAndloginOnDefaultTenantWith("john");
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
        final ActivityInstance step1 = waitForUserTask("step1");
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
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_ProcessToCheckDate",
                "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        final User user = createUserAndloginOnDefaultTenantWith("john");
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
        assertTrue("expected 1 activity", new CheckNbOfActivities(getProcessAPI(), 20, 5000, true, processInstance, 1, TestStates.getReadyState()).waitUntil());

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
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("ProcessToArchive", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        final User user = createUserAndloginOnDefaultTenantWith("john");
        assignFirstActorToMe(processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance);

        final List<ArchivedProcessInstance> archs = getProcessAPI().getArchivedProcessInstances(processInstance.getId(), 0, 100);
        assertEquals(1, archs.size());
        assertEquals(TestStates.getInitialState(), archs.get(0).getState());

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
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("ProcessToArchive", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, APITestUtil.ACTOR_NAME, john);
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
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("ProcessToArchive", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        final User user = createUserAndloginOnDefaultTenantWith("john");
        assignFirstActorToMe(processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertTrue("Expected an activity", new CheckNbOfActivities(getProcessAPI(), 50, 1000, true, processInstance, 1, TestStates.getReadyState()).waitUntil());
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
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("ProcessToArchive", "1.0",
                Arrays.asList("step1"), Arrays.asList(true));
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        final User user = createUserAndloginOnDefaultTenantWith("john");
        assignFirstActorToMe(processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        assertEquals(user.getId(), processInstance.getStartedBy());
        assertTrue("Expected an activity", new CheckNbOfActivities(getProcessAPI(), 50, 1000, true, processInstance, 1, TestStates.getReadyState()).waitUntil());
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
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, actorName, user);
        final long processDefId = processDefinition.getId();
        final ProcessInstance pi = getProcessAPI().startProcess(processDefId);

        final WaitForFinalArchivedActivity wait = waitForFinalArchivedActivity("auto1", pi);
        final ArchivedActivityInstance archivedActivityInstance = wait.getResult();

        assertEquals(null, archivedActivityInstance.getDisplayDescription());

        final ActivityInstance activityInstance = waitForTaskInState(pi, "task1", TestStates.getReadyState());

        assertEquals(null, activityInstance.getDisplayDescription());

        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Test
    public void updateDueDateOfTask() throws Exception {
        final String johnName = "john";
        final User user = createUserAndloginOnDefaultTenantWith(johnName);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("processToUpdateDueDate", "1.0");
        builder.addActor("johnny");
        builder.addUserTask("step1", "johnny").addExpectedDuration(10000000l);
        final DesignProcessDefinition done = builder.done();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(done, "johnny", user);

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
    public void executeTaskFor() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final String johnName = "john";
        final User john = createUserAndloginOnDefaultTenantWith(johnName);
        final User jack = createUser("jack", PASSWORD);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, APITestUtil.ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        try {
            // execute step 1 using john
            final ActivityInstance step1 = waitForUserTask("step1", processInstance);
            assertEquals(0, step1.getExecutedBy());

            getProcessAPI().assignUserTask(step1.getId(), jack.getId());
            getProcessAPI().executeFlowNode(jack.getId(), step1.getId());
            waitForUserTask("step2", processInstance);

            // check that the step1 was executed by john
            final ArchivedActivityInstance step1Archived = getProcessAPI().getArchivedActivityInstance(step1.getId());
            assertEquals(jack.getId(), step1Archived.getExecutedBy());
            assertEquals(john.getId(), step1Archived.getExecutedBySubstitute());

            // Check system comment
            final SearchOptions searchOptions = new SearchOptionsBuilder(0, 100).filter(SearchCommentsDescriptor.PROCESS_INSTANCE_ID, processInstance.getId()).
                    done();
            final List<Comment> comments = getProcessAPI().searchComments(searchOptions).getResult();
            boolean haveCommentForDelegate = false;
            for (final Comment comment : comments) {
                haveCommentForDelegate = haveCommentForDelegate
                        || comment.getContent().contains("The user " + johnName + " acting as delegate of the user jack has done the task \"step1\".");
            }
            assertTrue(haveCommentForDelegate);
        } finally {
            // clean
            disableAndDeleteProcess(processDefinition);
            deleteUsers(john, jack);
        }
    }

    @Test
    public void systemCommentsShouldBeAutoAddedAtTaskAssignment() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor("actor");
        builder.addUserTask("step1", "actor").addDisplayName(new ExpressionBuilder().createConstantStringExpression("Step1 display name"));
        builder.addUserTask("step2", "actor");
        builder.addTransition("step1", "step2");
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(builder.done()).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        final User user = createUserAndloginOnDefaultTenantWith("Tom");
        assignFirstActorToMe(processDefinition);
        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("step1", processInstance);
        assignAndExecuteStep(step1, user.getId());
        waitForUserTask("step2", processInstance);

        final SearchResult<Comment> searchResult = getProcessAPI().searchComments(new SearchOptionsBuilder(0, 5).done());
        final List<Comment> commentList = searchResult.getResult();
        assertEquals(1, commentList.size());
        assertEquals("The task \"Step1 display name\" is now assigned to Tom", commentList.get(0).getContent());

        assertEquals(1, getProcessAPI().countComments(new SearchOptionsBuilder(0, 5).done()));

        disableAndDeleteProcess(processDefinition);
        deleteUser("Tom");
    }

    @Cover(jira = "ENGINE-1820", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
    "process instance" })
    @Test
    public void getArchivedProcessDataInstance() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));
        builder.addActor("actor");
        builder.addUserTask("step", "actor");

        final User matti = createUser("matti", "bpm");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), "actor", matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().updateProcessDataInstance(dataName, processInstance.getId(), "2");

        final ArchivedDataInstance archivedData = getProcessAPI().getArchivedProcessDataInstance(dataName, processInstance.getId());
        assertEquals("2", archivedData.getValue());
        disableAndDeleteProcess(processDefinition);
        deleteUser(matti);
    }

    @Cover(jira = "ENGINE-1820", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
    "process instance" })
    @Test
    public void getArchivedProcessDataInstanceFromAnArchivedProcess() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));
        builder.addAutomaticTask("system");

        final ProcessDefinition processDefinition = deployAndEnableProcess(builder.getProcess());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(processInstance);

        final ArchivedDataInstance archivedData = getProcessAPI().getArchivedProcessDataInstance(dataName, processInstance.getId());
        assertEquals("1", archivedData.getValue());
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(jira = "ENGINE-1820, ENGINE-1946", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = {
            "Not archived", "transient data", "process instance" })
    @Test
    public void dontArchivedTransientProcessDataInstance() throws Exception {
        final String dataName = "test";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1")).isTransient();
        builder.addActor("actor");
        builder.addUserTask("step", "actor");

        final User matti = createUser("matti", "bpm");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), "actor", matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().updateProcessDataInstance(dataName, processInstance.getId(), "2");
        waitForUserTaskAndExecuteIt("step", processInstance, matti);
        waitForProcessToFinish(processInstance);

        try {
            getProcessAPI().getArchivedProcessDataInstance(dataName, processInstance.getId());
        } catch (final ArchivedDataNotFoundException e) {
            disableAndDeleteProcess(processDefinition);
            deleteUser(matti);
        }
    }

    @Cover(jira = "ENGINE-1820", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
    "process instance" })
    @Test
    public void getUnknownArchivedProcessDataInstance() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addAutomaticTask("system");

        final ProcessDefinition processDefinition = deployAndEnableProcess(builder.getProcess());
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(processInstance);

        try {
            getProcessAPI().getArchivedProcessDataInstance("o", processInstance.getId());
            fail("The data named 'o' does not exists");
        } catch (final ArchivedDataNotFoundException dnfe) {
            // Do nothing
        } finally {
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Cover(jira = "ENGINE-1821", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
    "activity instance" })
    @Test
    public void getArchivedActivityDataInstance() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor("actor");
        builder.addUserTask("step", "actor").addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));

        final User matti = createUser("matti", "bpm");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), "actor", matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("step", processInstance);
        getProcessAPI().updateActivityDataInstance(dataName, userTask.getId(), "2");

        final ArchivedDataInstance archivedData = getProcessAPI().getArchivedActivityDataInstance(dataName, userTask.getId());
        assertEquals("2", archivedData.getValue());
        disableAndDeleteProcess(processDefinition);
        deleteUser(matti);
    }

    @Cover(jira = "ENGINE-1821", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
    "activity instance" })
    @Test
    public void getArchivedActivityDataInstanceFromAnArchivedProcess() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor("actor");
        builder.addUserTask("step", "actor").addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));

        final User matti = createUser("matti", "bpm");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), "actor", matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("step", processInstance);
        getProcessAPI().updateActivityDataInstance(dataName, userTask.getId(), "2");
        assignAndExecuteStep(userTask, matti.getId());
        waitForProcessToFinish(processInstance.getId());

        final ArchivedDataInstance archivedData = getProcessAPI().getArchivedActivityDataInstance(dataName, userTask.getId());
        assertEquals("2", archivedData.getValue());
        disableAndDeleteProcess(processDefinition);
        deleteUser(matti);
    }

    @Cover(jira = "ENGINE-1821", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
    "activity instance" })
    @Test
    public void getUnknownArchivedActivityDataInstance() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor("actor");
        builder.addUserTask("step", "actor");

        final User matti = createUser("matti", "bpm");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), "actor", matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("step", processInstance);

        try {
            getProcessAPI().getArchivedProcessDataInstance("o", userTask.getId());
            fail("The data named 'o' does not exists");
        } catch (final ArchivedDataNotFoundException dnfe) {
            // Do nothing
        } finally {
            disableAndDeleteProcess(processDefinition);
            deleteUser(matti);
        }
    }

    @Cover(jira = "ENGINE-1822", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
    "process instance" })
    @Test
    public void getArchivedProcessDataInstances() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));
        builder.addShortTextData("job", new ExpressionBuilder().createConstantStringExpression("job"));
        builder.addShortTextData("desc", new ExpressionBuilder().createConstantStringExpression("desc"));
        builder.addActor("actor");
        builder.addUserTask("step", "actor");

        final User matti = createUser("matti", "bpm");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), "actor", matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        getProcessAPI().updateProcessDataInstance(dataName, processInstance.getId(), "2");

        List<ArchivedDataInstance> archivedDataInstances = getProcessAPI().getArchivedProcessDataInstances(processInstance.getId(), 0, 10);
        assertEquals(3, archivedDataInstances.size());
        ArchivedDataInstance archivedDataInstance = getArchivedDataInstance(archivedDataInstances, dataName);
        assertEquals("2", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "job");
        assertEquals("job", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "desc");
        assertEquals("desc", archivedDataInstance.getValue());

        archivedDataInstances = getProcessAPI().getArchivedProcessDataInstances(processInstance.getId(), 0, 1);
        assertEquals(1, archivedDataInstances.size());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, dataName);
        assertEquals("2", archivedDataInstance.getValue());

        archivedDataInstances = getProcessAPI().getArchivedProcessDataInstances(processInstance.getId(), 1, 10);
        assertEquals(2, archivedDataInstances.size());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "job");
        assertEquals("job", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "desc");
        assertEquals("desc", archivedDataInstance.getValue());

        final HumanTaskInstance userTask = waitForUserTask("step", processInstance);
        assignAndExecuteStep(userTask, matti.getId());
        waitForProcessToFinish(processInstance.getId());

        archivedDataInstances = getProcessAPI().getArchivedProcessDataInstances(processInstance.getId(), 0, 10);
        assertEquals(3, archivedDataInstances.size());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, dataName);
        assertEquals("2", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "job");
        assertEquals("job", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "desc");
        assertEquals("desc", archivedDataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
        deleteUser(matti);
    }

    private ArchivedDataInstance getArchivedDataInstance(final List<ArchivedDataInstance> archivedDataInstances, final String dataName) {
        ArchivedDataInstance archivedDataInstance = null;
        final Iterator<ArchivedDataInstance> iterator = archivedDataInstances.iterator();
        while (archivedDataInstance == null || iterator.hasNext()) {
            final ArchivedDataInstance next = iterator.next();
            if (next.getName().equals(dataName)) {
                archivedDataInstance = next;
            }
        }
        assertNotNull(archivedDataInstance);
        return archivedDataInstance;
    }

    @Cover(jira = "ENGINE-1822", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
    "process instance" })
    @Test
    public void getEmptyArchivedProcessDataInstances() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addActor("actor");
        builder.addUserTask("step", "actor");

        final User matti = createUser("matti", "bpm");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), "actor", matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final List<ArchivedDataInstance> archivedDataInstances = getProcessAPI().getArchivedProcessDataInstances(processInstance.getId(), 0, 10);
        assertEquals(0, archivedDataInstances.size());

        disableAndDeleteProcess(processDefinition);
        deleteUser(matti);
    }

    @Cover(jira = "ENGINE-1822", classes = { ArchivedDataInstance.class, ProcessAPI.class }, concept = BPMNConcept.DATA, keywords = { "last archived data",
    "activity instance" })
    @Test
    public void getArchivedActivityDataInstances() throws Exception {
        final String dataName = "title";
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("ProcessToArchive", "1.0");
        builder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("0"));
        builder.addActor("actor");
        final UserTaskDefinitionBuilder taskDefinitionBuilder = builder.addUserTask("step", "actor");
        taskDefinitionBuilder.addShortTextData(dataName, new ExpressionBuilder().createConstantStringExpression("1"));
        taskDefinitionBuilder.addShortTextData("job", new ExpressionBuilder().createConstantStringExpression("job"));
        taskDefinitionBuilder.addShortTextData("desc", new ExpressionBuilder().createConstantStringExpression("desc"));

        final User matti = createUser("matti", "bpm");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(builder.getProcess(), "actor", matti);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final HumanTaskInstance userTask = waitForUserTask("step", processInstance);
        getProcessAPI().updateActivityDataInstance(dataName, userTask.getId(), "2");
        assignAndExecuteStep(userTask, matti.getId());
        waitForProcessToFinish(processInstance.getId());

        List<ArchivedDataInstance> archivedDataInstances = getProcessAPI().getArchivedActivityDataInstances(userTask.getId(), 0, 10);
        assertEquals(3, archivedDataInstances.size());
        ArchivedDataInstance archivedDataInstance = getArchivedDataInstance(archivedDataInstances, dataName);
        assertEquals("2", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "job");
        assertEquals("job", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "desc");
        assertEquals("desc", archivedDataInstance.getValue());

        archivedDataInstances = getProcessAPI().getArchivedActivityDataInstances(userTask.getId(), 0, 1);
        assertEquals(1, archivedDataInstances.size());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, dataName);
        assertEquals("2", archivedDataInstance.getValue());

        archivedDataInstances = getProcessAPI().getArchivedActivityDataInstances(userTask.getId(), 1, 10);
        assertEquals(2, archivedDataInstances.size());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "job");
        assertEquals("job", archivedDataInstance.getValue());
        archivedDataInstance = getArchivedDataInstance(archivedDataInstances, "desc");
        assertEquals("desc", archivedDataInstance.getValue());

        disableAndDeleteProcess(processDefinition);
        deleteUser(matti);
    }

}
