package org.bonitasoft.engine.search;

import static org.bonitasoft.engine.matchers.BonitaMatcher.match;
import static org.bonitasoft.engine.matchers.ListContainsMatcher.namesContain;
import static org.bonitasoft.engine.matchers.ListElementMatcher.nameAre;
import static org.bonitasoft.engine.matchers.ListElementMatcher.stateAre;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ActivityStates;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedAutomaticTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedFlowNodeInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedHumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.ArchivedManualTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedUserTaskInstance;
import org.bonitasoft.engine.bpm.flownode.FlowNodeType;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.SendTaskInstance;
import org.bonitasoft.engine.bpm.flownode.TaskPriority;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.TestConnectorLongToExecute;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbOfActivities;
import org.bonitasoft.engine.test.wait.WaitForStep;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class SearchActivityInstanceTest extends CommonAPITest {

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
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
    }

    /**
     * Tests that the archiving mechanism works:
     * <ul>
     * <li>archived activities must be found in the archive</li>
     * <li>archived activities must be deleted from the journal</li>
     * </ul>
     * 
     * @throws Exception
     * @since 6.0
     */
    @Test
    public void activityArchivingMechanism() throws Exception {
        // create user and process
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription("Process to test archiving mechanism").addUserTask("userTask", ACTOR_NAME)
                .addUserTask("secondTask", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);

        final ProcessInstance processInstance = getProcessAPI().startProcess(user.getId(), processDefinition.getId());

        // Wait for 2 activities in READY state:
        final CheckNbOfActivities checkNbReadyActivities = new CheckNbOfActivities(getProcessAPI(), 200, 3000, true, processInstance, 2,
                TestStates.getReadyState());
        assertTrue("Expected 2 open activities for process instance " + processInstance.getId(), checkNbReadyActivities.waitUntil());

        // Check that no tasks are archived yet:
        SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 12);
        searchBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, processInstance.getId());
        SearchResult<ArchivedActivityInstance> archActivitResult = getProcessAPI().searchArchivedActivities(searchBuilder.done());
        assertEquals(0, archActivitResult.getCount());

        // Skip the 2 tasks one by one:
        final Set<ActivityInstance> activities = checkNbReadyActivities.getResult();
        int nbTasksArchived = 0;
        for (final ActivityInstance activityInstance : activities) {
            skipTask(activityInstance.getId());
            nbTasksArchived++;

            final int nbActivities = 2 - nbTasksArchived;
            // Check nb of remaining tasks in the journal:
            final CheckNbOfActivities checkNbActivities = new CheckNbOfActivities(getProcessAPI(), 200, 3000, true, processInstance, nbActivities);
            final boolean waitUntil = checkNbActivities.waitUntil();
            assertTrue("Expected " + nbActivities + " open activities for process instance " + processInstance.getId() + " but got "
                    + checkNbActivities.getResult().size(), waitUntil);

            // Check that both tasks are found in the archive:
            searchBuilder = new SearchOptionsBuilder(0, 12);
            searchBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, processInstance.getId());
            archActivitResult = getProcessAPI().searchArchivedActivities(searchBuilder.done());
            assertEquals(nbTasksArchived, archActivitResult.getCount());
        }

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchArchivedTasksManagedBy() throws Exception {
        // Create tasks, assign them, some to some users managed by "manager", some to other users with different manager.
        // execute / skip them so that the tasks (or only some) are archived.
        // Retrieve the tasks. check the count, the retrieved list, the order.
        final User jack = createUser("jack", "bpm");
        // Jules is not subordinates of jack:
        final User jules = createUser("jules", "bpm");
        final User john = createUser("john", "bpm", jack.getId());

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription("Famous French actor");
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).addUserTask("userTask2", ACTOR_NAME)
                .addUserTask("userTask3", ACTOR_NAME).addUserTask("task4", ACTOR_NAME).addUserTask("userTask5", ACTOR_NAME)
                .addUserTask("userTask6", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, jack);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        final long stepId1 = waitForUserTask("userTask1", pi0).getId();
        final long stepId2 = waitForUserTask("userTask2", pi0).getId();
        final long stepId3 = waitForUserTask("userTask3", pi0).getId();
        final long stepId4 = waitForUserTask("task4", pi0).getId();
        final long stepId5 = waitForUserTask("userTask5", pi0).getId();
        final long stepId6 = waitForUserTask("userTask6", pi0).getId();
        getProcessAPI().assignUserTask(stepId1, john.getId());
        getProcessAPI().assignUserTask(stepId2, john.getId());
        getProcessAPI().assignUserTask(stepId3, jack.getId());
        getProcessAPI().assignUserTask(stepId4, john.getId());
        getProcessAPI().assignUserTask(stepId5, jules.getId());
        // don't assign userTask6 to anyone.
        skipTask(stepId1);
        skipTask(stepId2);
        skipTask(stepId3);
        skipTask(stepId4);
        skipTask(stepId5);
        skipTask(stepId6);
        waitForProcessToFinish(pi0);
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        // filter only userTask1:
        builder.filter(ArchivedHumanTaskInstanceSearchDescriptor.NAME, "userTask1");
        builder.sort(ArchivedHumanTaskInstanceSearchDescriptor.NAME, Order.ASC);

        SearchResult<ArchivedHumanTaskInstance> aHumanTasksRes = getProcessAPI().searchArchivedHumanTasksManagedBy(jack.getId(), builder.done());
        assertEquals(1, aHumanTasksRes.getCount());
        List<ArchivedHumanTaskInstance> tasks = aHumanTasksRes.getResult();
        ArchivedHumanTaskInstance aArchivedHumanTaskInstance = tasks.get(0);
        assertEquals("userTask1", aArchivedHumanTaskInstance.getName());

        builder = new SearchOptionsBuilder(0, 10);
        // filter all userTask*:
        builder.searchTerm("userTask");
        builder.sort(ArchivedHumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        aHumanTasksRes = getProcessAPI().searchArchivedHumanTasksManagedBy(jack.getId(), builder.done());
        assertEquals(2, aHumanTasksRes.getCount());
        tasks = aHumanTasksRes.getResult();
        aArchivedHumanTaskInstance = tasks.get(0);
        assertEquals("userTask1", aArchivedHumanTaskInstance.getName());
        aArchivedHumanTaskInstance = tasks.get(1);
        assertEquals("userTask2", aArchivedHumanTaskInstance.getName());

        builder = new SearchOptionsBuilder(0, 10);
        // filter all ask*:
        builder.searchTerm("user");
        builder.sort(ArchivedHumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        aHumanTasksRes = getProcessAPI().searchArchivedHumanTasksManagedBy(jack.getId(), builder.done());
        assertEquals(2, aHumanTasksRes.getCount());
        tasks = aHumanTasksRes.getResult();
        aArchivedHumanTaskInstance = tasks.get(0);
        assertEquals("userTask1", aArchivedHumanTaskInstance.getName());
        aArchivedHumanTaskInstance = tasks.get(1);
        assertEquals("userTask2", aArchivedHumanTaskInstance.getName());

        builder = new SearchOptionsBuilder(0, 10);
        // filter all task of this process definition:
        builder.filter(ArchivedHumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
        builder.sort(ArchivedHumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        aHumanTasksRes = getProcessAPI().searchArchivedHumanTasksManagedBy(jack.getId(), builder.done());
        assertEquals(3, aHumanTasksRes.getCount());
        tasks = aHumanTasksRes.getResult();
        aArchivedHumanTaskInstance = tasks.get(0);
        assertEquals("task4", aArchivedHumanTaskInstance.getName());
        aArchivedHumanTaskInstance = tasks.get(1);
        assertEquals("userTask1", aArchivedHumanTaskInstance.getName());
        aArchivedHumanTaskInstance = tasks.get(2);
        assertEquals("userTask2", aArchivedHumanTaskInstance.getName());

        disableAndDeleteProcess(processDefinition);
        deleteUsers(john, jack, jules);
    }

    @Test
    public void searchAssignedTasksManagedBy() throws Exception {
        final User jack = createUser("jack", "bpm");
        final User john = createUser("john", "bpm", jack.getId());

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).addUserTask("userTask2", ACTOR_NAME)
                .addUserTask("userTask3", ACTOR_NAME).addUserTask("task4", ACTOR_NAME).getProcess();

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long stepId1 = waitForUserTask("userTask1", processInstance).getId();
        final long stepId2 = waitForUserTask("userTask2", processInstance).getId();
        final long stepId3 = waitForUserTask("userTask3", processInstance).getId();
        final long stepId4 = waitForUserTask("task4", processInstance).getId();
        getProcessAPI().assignUserTask(stepId1, john.getId());
        getProcessAPI().assignUserTask(stepId2, john.getId());
        getProcessAPI().assignUserTask(stepId3, jack.getId());
        getProcessAPI().assignUserTask(stepId4, john.getId());

        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(HumanTaskInstanceSearchDescriptor.NAME, "userTask1");
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        SearchResult<HumanTaskInstance> searchAssignedTasksManagedBy = getProcessAPI().searchAssignedTasksManagedBy(jack.getId(), builder.done());
        assertEquals(1, searchAssignedTasksManagedBy.getCount());
        List<HumanTaskInstance> tasks = searchAssignedTasksManagedBy.getResult();
        HumanTaskInstance humanTaskInstance = tasks.get(0);
        assertEquals("userTask1", humanTaskInstance.getName());

        builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm("userTask");
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchAssignedTasksManagedBy = getProcessAPI().searchAssignedTasksManagedBy(jack.getId(), builder.done());
        assertEquals(2, searchAssignedTasksManagedBy.getCount());
        tasks = searchAssignedTasksManagedBy.getResult();
        humanTaskInstance = tasks.get(0);
        assertEquals("userTask1", humanTaskInstance.getName());
        humanTaskInstance = tasks.get(1);
        assertEquals("userTask2", humanTaskInstance.getName());

        builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm("user");
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchAssignedTasksManagedBy = getProcessAPI().searchAssignedTasksManagedBy(jack.getId(), builder.done());
        assertEquals(2, searchAssignedTasksManagedBy.getCount());
        tasks = searchAssignedTasksManagedBy.getResult();
        humanTaskInstance = tasks.get(0);
        assertEquals("userTask1", humanTaskInstance.getName());
        humanTaskInstance = tasks.get(1);
        assertEquals("userTask2", humanTaskInstance.getName());

        disableAndDeleteProcess(processDefinition);
        deleteUsers(john, jack);
    }

    @Test
    public void searchHumanTaskInstances() throws Exception {
        // First process def with 2 instances:
        final DesignProcessDefinition designProcessDef1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("initTask1"),
                Arrays.asList(true));
        final ProcessDefinition processDef1 = deployAndEnableProcessWithActor(designProcessDef1, ACTOR_NAME, user);
        // final ProcessInstance processInstance =
        final ProcessInstance pi1 = getProcessAPI().startProcess(processDef1.getId());
        final ProcessInstance pi2 = getProcessAPI().startProcess(processDef1.getId());

        final ProcessDefinitionBuilder definitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME + 2, PROCESS_VERSION);
        definitionBuilder.addStartEvent("start");
        definitionBuilder.addActor(ACTOR_NAME);
        definitionBuilder.addUserTask("initTask2", ACTOR_NAME);
        definitionBuilder.addEndEvent("end");
        definitionBuilder.addTransition("start", "initTask2");
        definitionBuilder.addTransition("initTask2", "end");
        final ProcessDefinition processDef2 = deployAndEnableProcessWithActor(definitionBuilder.done(), ACTOR_NAME, user);
        final ProcessInstance pi3 = getProcessAPI().startProcess(processDef2.getId());
        final ProcessInstance pi4 = getProcessAPI().startProcess(processDef2.getId());
        final ProcessInstance pi5 = getProcessAPI().startProcess(processDef2.getId());
        waitForUserTask("initTask1", pi1);
        waitForUserTask("initTask1", pi2);
        waitForUserTask("initTask2", pi3);
        waitForUserTask("initTask2", pi4);
        waitForUserTask("initTask2", pi5);

        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(5, humanTasksSearch.getCount());

        // There should be 1 task which priority is above_normal:
        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(pi2.getId(), 0, 10);
        getProcessAPI().setTaskPriority(activityInstances.get(0).getId(), TaskPriority.ABOVE_NORMAL);
        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.PRIORITY, TaskPriority.ABOVE_NORMAL);
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(1, humanTasksSearch.getCount());
        assertEquals(TaskPriority.ABOVE_NORMAL, humanTasksSearch.getResult().get(0).getPriority());

        // There should be no assigned tasks to 'user':
        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, user.getId());
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(0, humanTasksSearch.getCount());

        // There should be 5 non-assigned tasks:
        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, 0L);
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(5, humanTasksSearch.getCount());

        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, pi2.getId());
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(1, humanTasksSearch.getCount());

        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDef2.getId());
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(3, humanTasksSearch.getCount());

        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.searchTerm("initTask2");
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(3, humanTasksSearch.getCount());
        for (final HumanTaskInstance task : humanTasksSearch.getResult()) {
            assertEquals("initTask2", task.getName());
        }

        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.searchTerm("initTask");
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(5, humanTasksSearch.getCount());
        for (final HumanTaskInstance task : humanTasksSearch.getResult()) {
            assertTrue("keyword search sould return only tasks with name containing 'initTask'", task.getName().contains("initTask"));
        }

        final long taskId = humanTasksSearch.getResult().get(0).getId();
        getProcessAPI().assignUserTask(taskId, user.getId());
        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE);
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(5, humanTasksSearch.getCount());
        for (final HumanTaskInstance task : humanTasksSearch.getResult()) {
            assertEquals(ActivityStates.READY_STATE, task.getState());
        }

        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, user.getId());
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(1, humanTasksSearch.getCount());

        disableAndDeleteProcess(processDef1, processDef2);
    }

    @Test
    public void searchHumanTaskInstancesOrderByPriorityAndDueDate() throws Exception {
        final ProcessDefinitionBuilder definitionBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        definitionBuilder.addStartEvent("start");
        definitionBuilder.addActor(ACTOR_NAME);
        definitionBuilder.addUserTask("initTask1", ACTOR_NAME).addPriority(TaskPriority.HIGHEST.name()).addExpectedDuration(1000);
        definitionBuilder.addUserTask("initTask2", ACTOR_NAME).addPriority(TaskPriority.HIGHEST.name()).addExpectedDuration(2000);
        definitionBuilder.addUserTask("initTask3", ACTOR_NAME).addPriority(TaskPriority.LOWEST.name()).addExpectedDuration(3000);
        definitionBuilder.addUserTask("initTask4", ACTOR_NAME).addPriority(TaskPriority.HIGHEST.name()).addExpectedDuration(500);
        definitionBuilder.addUserTask("initTask5", ACTOR_NAME).addPriority(TaskPriority.NORMAL.name()).addExpectedDuration(1500);
        definitionBuilder.addUserTask("initTask6", ACTOR_NAME).addPriority(TaskPriority.NORMAL.name()).addExpectedDuration(1000);
        definitionBuilder.addEndEvent("end");
        definitionBuilder.addTransition("start", "initTask1");
        definitionBuilder.addTransition("start", "initTask2");
        definitionBuilder.addTransition("start", "initTask3");
        definitionBuilder.addTransition("start", "initTask4");
        definitionBuilder.addTransition("start", "initTask5");
        definitionBuilder.addTransition("start", "initTask6");
        final ProcessDefinition processDef = deployAndEnableProcessWithActor(definitionBuilder.done(), ACTOR_NAME, user);
        getProcessAPI().startProcess(processDef.getId());
        waitForUserTask("initTask1");
        waitForUserTask("initTask2");
        waitForUserTask("initTask3");
        waitForUserTask("initTask4");
        waitForUserTask("initTask5");
        waitForUserTask("initTask6");

        // There should be 6 tasks
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(6, humanTasksSearch.getCount());

        List<HumanTaskInstance> humanTaskInstances;
        // There should be 6 tasks when order by ascending priority. The first task is "initTask3".
        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDef.getId());
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.PRIORITY, Order.ASC);
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(6, humanTasksSearch.getCount());
        humanTaskInstances = humanTasksSearch.getResult();
        assertEquals("initTask3", humanTaskInstances.get(0).getName());

        // There should be 6 tasks when order by descending priority. The last task is "initTask3".
        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDef.getId());
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.PRIORITY, Order.DESC);
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(6, humanTasksSearch.getCount());
        humanTaskInstances = humanTasksSearch.getResult();
        assertEquals("initTask3", humanTaskInstances.get(5).getName());

        // There should be 6 tasks when order by ascending due date in this order : "initTask4", ["initTask1" "initTask6"], "initTask5", "initTask2",
        // "initTask3"
        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDef.getId());
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.DUE_DATE, Order.ASC);
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(6, humanTasksSearch.getCount());
        humanTaskInstances = humanTasksSearch.getResult();
        assertEquals("initTask4", humanTaskInstances.get(0).getName());
        assertEquals("initTask5", humanTaskInstances.get(3).getName());
        assertEquals("initTask2", humanTaskInstances.get(4).getName());
        assertEquals("initTask3", humanTaskInstances.get(5).getName());

        // There should be 6 tasks when order by descending due date in this order : "initTask3", "initTask2", "initTask5", ["initTask1" "initTask6"],
        // "initTask4"
        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDef.getId());
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.DUE_DATE, Order.DESC);
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(6, humanTasksSearch.getCount());
        humanTaskInstances = humanTasksSearch.getResult();
        assertEquals("initTask3", humanTaskInstances.get(0).getName());
        assertEquals("initTask2", humanTaskInstances.get(1).getName());
        assertEquals("initTask5", humanTaskInstances.get(2).getName());
        assertEquals("initTask4", humanTaskInstances.get(5).getName());

        // There should be 6 tasks when order by due date and priority in this order : "initTask4", "initTask1", "initTask2", "initTask5", "initTask6",
        // "initTask3"
        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDef.getId());
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.PRIORITY, Order.DESC);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.DUE_DATE, Order.ASC);
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(6, humanTasksSearch.getCount());
        humanTaskInstances = humanTasksSearch.getResult();
        assertEquals("initTask4", humanTaskInstances.get(0).getName());
        assertEquals("initTask1", humanTaskInstances.get(1).getName());
        assertEquals("initTask2", humanTaskInstances.get(2).getName());
        assertEquals("initTask6", humanTaskInstances.get(3).getName());
        assertEquals("initTask5", humanTaskInstances.get(4).getName());
        assertEquals("initTask3", humanTaskInstances.get(5).getName());

        disableAndDeleteProcess(processDef);
    }

    @Test(expected = SearchException.class)
    public void searchHumanTaskInstancesWithSearchException() throws Exception {
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        final SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.sort("tyefv", Order.ASC).done());
        assertEquals(0, humanTasksSearch.getCount());
    }

    /**
     * @throws Exception
     */
    private void searchPendingTasks(final String taskName, final String actorName) throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(actorName);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask(taskName, actorName).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, actorName, user);
        // -------- start process and wait for tasks
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(taskName, processInstance);

        // -------- test pending task search methods
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, 0);
        builder.filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE);
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        builder.searchTerm("'");
        final SearchResult<HumanTaskInstance> searchHumanTaskInstances = getProcessAPI().searchHumanTaskInstances(builder.done());
        assertEquals(1, searchHumanTaskInstances.getCount());
        final List<HumanTaskInstance> tasks = searchHumanTaskInstances.getResult();
        assertEquals(taskName, tasks.get(0).getName());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchArchivedActivities() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).addUserTask("userTask2", ACTOR_NAME)
                .addAutomaticTask("automaticTask").addManualTask("manualTask", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance pi2 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("userTask1", pi1);
        waitForUserTaskAndAssigneIt("userTask2", pi1, user);
        final WaitForStep waitForStep4 = waitForStep("manualTask", pi1);
        assertTrue(waitForStep4.waitUntil());

        waitForUserTask("userTask1", pi2);
        waitForUserTask("userTask2", pi2);
        final WaitForStep waitForStep44 = waitForStep("manualTask", pi2);
        assertTrue(waitForStep44.waitUntil());

        // finish the tasks
        final List<ActivityInstance> openedActivityInstances1 = getProcessAPI().getOpenActivityInstances(pi1.getId(), 0, 20, ActivityInstanceCriterion.DEFAULT);
        assertEquals(3, openedActivityInstances1.size());
        for (final ActivityInstance activityInstance : openedActivityInstances1) {
            final long activityInstanceId = activityInstance.getId();
            getProcessAPI().setActivityStateById(activityInstanceId, 12);
        }
        waitForProcessToFinish(pi1);

        final List<ActivityInstance> openedActivityInstances2 = getProcessAPI().getOpenActivityInstances(pi2.getId(), 0, 20, ActivityInstanceCriterion.DEFAULT);
        assertEquals(3, openedActivityInstances2.size());
        for (final ActivityInstance activityInstance : openedActivityInstances2) {
            final long activityInstanceId = activityInstance.getId();
            getProcessAPI().setActivityStateById(activityInstanceId, 12);
        }
        waitForProcessToFinish(pi2);
        // each automatic task will have four-state archived instance
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.AUTOMATIC_TASK);
        SearchResult<ArchivedActivityInstance> archivedActivityInstancesSearch = getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done());
        assertEquals(2, archivedActivityInstancesSearch.getCount());
        for (final ArchivedActivityInstance activity : archivedActivityInstancesSearch.getResult()) {
            assertTrue(activity instanceof ArchivedAutomaticTaskInstance);
        }
        // test activity type
        searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.USER_TASK);
        archivedActivityInstancesSearch = getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done());
        assertEquals(4, archivedActivityInstancesSearch.getCount());
        for (final ArchivedActivityInstance activity : archivedActivityInstancesSearch.getResult()) {
            assertTrue(activity instanceof ArchivedUserTaskInstance);
        }

        searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.MANUAL_TASK);
        archivedActivityInstancesSearch = getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done());
        assertEquals(2, archivedActivityInstancesSearch.getCount());
        for (final ArchivedActivityInstance activity : archivedActivityInstancesSearch.getResult()) {
            assertTrue(activity instanceof ArchivedManualTaskInstance);
        }

        searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.HUMAN_TASK);
        archivedActivityInstancesSearch = getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done());
        assertEquals(6, archivedActivityInstancesSearch.getCount());
        for (final ArchivedActivityInstance activity : archivedActivityInstancesSearch.getResult()) {
            assertTrue(activity instanceof ArchivedHumanTaskInstance);
            assertTrue(activity instanceof ArchivedManualTaskInstance || activity instanceof ArchivedUserTaskInstance);
        }

        searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, pi1.getId());
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.AUTOMATIC_TASK);
        archivedActivityInstancesSearch = getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done());
        assertEquals(1, archivedActivityInstancesSearch.getCount());
        for (final ArchivedActivityInstance activity : archivedActivityInstancesSearch.getResult()) {
            assertTrue(activity instanceof ArchivedAutomaticTaskInstance);
        }

        searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, pi1.getId());
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ACTIVITY_TYPE, FlowNodeType.HUMAN_TASK);
        archivedActivityInstancesSearch = getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done());
        assertEquals(3, archivedActivityInstancesSearch.getCount());
        for (final ArchivedActivityInstance activity : archivedActivityInstancesSearch.getResult()) {
            assertTrue(activity instanceof ArchivedHumanTaskInstance);
            assertTrue(activity instanceof ArchivedManualTaskInstance || activity instanceof ArchivedUserTaskInstance);
        }

        searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
        archivedActivityInstancesSearch = getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done());
        assertEquals(8, archivedActivityInstancesSearch.getCount());
        for (final ArchivedActivityInstance activity : archivedActivityInstancesSearch.getResult()) {
            assertTrue(activity instanceof ArchivedManualTaskInstance || activity instanceof ArchivedUserTaskInstance
                    || activity instanceof ArchivedAutomaticTaskInstance);
        }

        searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.searchTerm("userTask");
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
        searchOptionsBuilder.sort(ArchivedActivityInstanceSearchDescriptor.NAME, Order.DESC);
        final SearchResult<ArchivedActivityInstance> taskInstanceSearchResult = getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done());
        assertEquals(4, taskInstanceSearchResult.getCount());
        final List<ArchivedActivityInstance> archivedActivities = taskInstanceSearchResult.getResult();
        final ArchivedActivityInstance aut1 = archivedActivities.get(0);
        final ArchivedActivityInstance aut2 = archivedActivities.get(1);
        final ArchivedActivityInstance aut3 = archivedActivities.get(2);
        final ArchivedActivityInstance aut4 = archivedActivities.get(3);
        assertEquals("userTask2", aut1.getName());
        assertEquals("userTask2", aut2.getName());
        assertEquals("userTask1", aut3.getName());
        assertEquals("userTask1", aut4.getName());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchArchivedTasks() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).addUserTask("userTask2", ACTOR_NAME)
                .getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("userTask1", pi0);
        Thread.sleep(1000);

        final ActivityInstance step2 = waitForUserTask("userTask2", pi0);

        SearchResult<ArchivedHumanTaskInstance> taskInstanceSearchResult;
        SearchOptionsBuilder builder;

        getProcessAPI().assignUserTask(step2.getId(), user.getId());

        getProcessAPI().setActivityStateById(step1.getId(), 12);
        getProcessAPI().setActivityStateById(step2.getId(), 12);
        waitForProcessToFinish(pi0);
        builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ArchivedHumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
        builder.sort(ArchivedHumanTaskInstanceSearchDescriptor.NAME, Order.DESC);
        taskInstanceSearchResult = getProcessAPI().searchArchivedHumanTasks(builder.done());
        assertEquals(2, taskInstanceSearchResult.getCount());
        List<ArchivedHumanTaskInstance> archivedTasks = taskInstanceSearchResult.getResult();
        final ArchivedHumanTaskInstance aut1 = archivedTasks.get(0);
        final ArchivedHumanTaskInstance aut2 = archivedTasks.get(1);
        assertEquals("userTask2", aut1.getName());
        assertEquals("userTask1", aut2.getName());
        final String archivedTaskActorName = getProcessAPI().getActor(aut1.getActorId()).getName();
        assertEquals(ACTOR_NAME, archivedTaskActorName);

        // filter task assigned by user
        builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ArchivedHumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, user.getId());
        builder.filter(ArchivedHumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
        builder.sort(ArchivedHumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        taskInstanceSearchResult = getProcessAPI().searchArchivedHumanTasks(builder.done());
        assertEquals(1, taskInstanceSearchResult.getCount());
        archivedTasks = taskInstanceSearchResult.getResult();
        assertEquals("userTask2", archivedTasks.get(0).getName());

        // search with sort on reached state date
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ArchivedHumanTaskInstanceSearchDescriptor.REACHED_STATE_DATE, Order.DESC);
        taskInstanceSearchResult = getProcessAPI().searchArchivedHumanTasks(builder.done());
        assertThat(taskInstanceSearchResult.getResult(), match(stateAre("skipped", "skipped")).and(nameAre("userTask2", "userTask1")));
        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ArchivedHumanTaskInstanceSearchDescriptor.REACHED_STATE_DATE, Order.ASC);
        taskInstanceSearchResult = getProcessAPI().searchArchivedHumanTasks(builder.done());
        assertThat(taskInstanceSearchResult.getResult(), match(stateAre("skipped", "skipped")).and(nameAre("userTask1", "userTask2")));

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void testSearchArchivedActivitiesInTerminalState() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).addUserTask("userTask2", ACTOR_NAME)
                .addUserTask("userTask3", ACTOR_NAME).addTransition("userTask2", "userTask3")
                .getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        final ActivityInstance step1 = waitForUserTask("userTask1", pi0);
        getProcessAPI().assignUserTask(step1.getId(), user.getId());
        final ActivityInstance step2 = waitForUserTask("userTask2", pi0);
        assignAndExecuteStep(step2, user.getId());
        waitForUserTask("userTask3", pi0);
        SearchOptionsBuilder builder;
        builder = new SearchOptionsBuilder(0, 10).filter(ArchivedFlowNodeInstanceSearchDescriptor.TERMINAL, true);
        SearchResult<ArchivedFlowNodeInstance> searchFlowNodeInstances = getProcessAPI().searchArchivedFlowNodeInstances(builder.done());
        assertEquals(1, searchFlowNodeInstances.getResult().size());
        assertEquals("userTask2", searchFlowNodeInstances.getResult().get(0).getName());
        builder = new SearchOptionsBuilder(0, 10).filter(ArchivedFlowNodeInstanceSearchDescriptor.TERMINAL, false);
        searchFlowNodeInstances = getProcessAPI().searchArchivedFlowNodeInstances(builder.done());
        assertTrue(searchFlowNodeInstances.getResult().size() > 1);

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { SearchOptionsBuilder.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "SearchArchivedTasks", "Apostrophe" }, jira = "ENGINE-366")
    @Test
    public void searchArchivedTasksWithApostrophe() throws Exception {
        final String taskName = "'Task";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask(taskName, ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(taskName, pi0);

        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(pi0.getId(), 0, 10);
        assertEquals(1, activityInstances.size());
        for (final ActivityInstance activityInstance : activityInstances) {
            final long activityInstanceId = activityInstance.getId();
            getProcessAPI().setActivityStateById(activityInstanceId, 12);
        }
        waitForProcessToFinish(pi0);
        final SearchResult<ArchivedHumanTaskInstance> taskInstanceSearchResult = getProcessAPI().searchArchivedHumanTasks(
                new SearchOptionsBuilder(0, 10).searchTerm("'").done());
        assertEquals(1, taskInstanceSearchResult.getCount());
        final List<ArchivedHumanTaskInstance> archivedTasks = taskInstanceSearchResult.getResult();
        final ArchivedHumanTaskInstance archivedHumanTaskInstance = archivedTasks.get(0);
        assertEquals(taskName, archivedHumanTaskInstance.getName());
        final String archivedTaskActorName = getProcessAPI().getActor(archivedHumanTaskInstance.getActorId()).getName();
        assertEquals(ACTOR_NAME, archivedTaskActorName);

        disableAndDeleteProcess(processDefinition);
    }

    /**
     * if you remove a process between the two calls, re-deploy it and re-instantiate it: it should get right results.
     * 
     * @throws Exception
     */
    @Test
    public void searchPendingUserTaskInstances() throws Exception {
        searchPendingUserTaskInstancesByActivityInstanceCriterion(ActivityInstanceCriterion.NAME_ASC);
        searchPendingUserTaskInstancesByActivityInstanceCriterion(ActivityInstanceCriterion.NAME_DESC);
        searchPendingUserTaskInstancesByActivityInstanceCriterion(ActivityInstanceCriterion.REACHED_STATE_DATE_ASC);
        searchPendingUserTaskInstancesByActivityInstanceCriterion(ActivityInstanceCriterion.REACHED_STATE_DATE_DESC);
        searchPendingUserTaskInstancesByActivityInstanceCriterion(ActivityInstanceCriterion.LAST_UPDATE_ASC);
        searchPendingUserTaskInstancesByActivityInstanceCriterion(ActivityInstanceCriterion.LAST_UPDATE_DESC);
        searchPendingUserTaskInstancesByActivityInstanceCriterion(ActivityInstanceCriterion.PRIORITY_ASC);
        searchPendingUserTaskInstancesByActivityInstanceCriterion(ActivityInstanceCriterion.PRIORITY_DESC);
        searchPendingUserTaskInstancesByActivityInstanceCriterion(ActivityInstanceCriterion.DEFAULT);
    }

    private void searchPendingUserTaskInstancesByActivityInstanceCriterion(final ActivityInstanceCriterion activityInstanceCriterion) throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addUserTask("Request", ACTOR_NAME).addPriority(TaskPriority.LOWEST.name());
        processBuilder.addUserTask("Request2", ACTOR_NAME);
        processBuilder.addUserTask("Approval", ACTOR_NAME);
        processBuilder.addUserTask("Approval2", ACTOR_NAME).addPriority(TaskPriority.HIGHEST.name());
        processBuilder.addTransition("Request", "Approval");
        processBuilder.addTransition("Request2", "Approval2");
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("Request", processInstance);
        waitForUserTask("Request2", processInstance);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.NAME, "Request2");
        final SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchPendingTasksForUser(user.getId(), searchOptionsBuilder.done());
        assertEquals(1, humanTasksSearch.getCount());
        final HumanTaskInstance userTaskId = humanTasksSearch.getResult().get(0);
        assignAndExecuteStep(userTaskId, user.getId());
        waitForUserTask("Approval2", processInstance);

        final List<HumanTaskInstance> userTaskInstances = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, activityInstanceCriterion);
        assertNotNull(userTaskInstances);
        assertEquals(2, userTaskInstances.size());
        switch (activityInstanceCriterion) {
            case NAME_ASC:
                assertEquals("Approval2", userTaskInstances.get(0).getName());
                break;
            case NAME_DESC:
                assertEquals("Request", userTaskInstances.get(0).getName());
                break;
            case REACHED_STATE_DATE_ASC:
                assertEquals("Request", userTaskInstances.get(0).getName());
                break;
            case REACHED_STATE_DATE_DESC:
                assertEquals("Approval2", userTaskInstances.get(0).getName());
                break;
            case LAST_UPDATE_ASC:
                assertEquals("Request", userTaskInstances.get(0).getName());
                break;
            case LAST_UPDATE_DESC:
                assertEquals("Approval2", userTaskInstances.get(0).getName());
                break;
            case PRIORITY_ASC:
                assertEquals("Request", userTaskInstances.get(0).getName());
                break;
            case PRIORITY_DESC:
            case DEFAULT:
            default:
                assertEquals("Approval2", userTaskInstances.get(0).getName());
                break;
        }

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchPendingTasks() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).addUserTask("userTask2", ACTOR_NAME)
                .addUserTask("userTask3", ACTOR_NAME).addUserTask("task4", ACTOR_NAME).addUserTask("userTask5", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        // -------- start process and wait for tasks
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("userTask1", pi0);
        waitForUserTaskAndAssigneIt("userTask2", pi0, user);
        waitForUserTaskAndAssigneIt("userTask3", pi0, user);
        waitForUserTask("task4", pi0);
        waitForUserTask("userTask5", pi0);

        // -------- test pending task search methods
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, 0);
        builder.filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE);
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        SearchResult<HumanTaskInstance> searchHumanTaskInstances = getProcessAPI().searchHumanTaskInstances(builder.done());
        assertEquals(3, searchHumanTaskInstances.getCount());
        List<HumanTaskInstance> tasks = searchHumanTaskInstances.getResult();
        HumanTaskInstance humanTaskInstance = tasks.get(0);
        assertEquals("task4", humanTaskInstance.getName());
        humanTaskInstance = tasks.get(1);
        assertEquals("userTask1", humanTaskInstance.getName());
        humanTaskInstance = tasks.get(2);
        assertEquals("userTask5", humanTaskInstance.getName());
        // -------- test assign task search methods
        builder = new SearchOptionsBuilder(0, 10);
        builder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, user.getId());
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchHumanTaskInstances = getProcessAPI().searchHumanTaskInstances(builder.done());
        assertEquals(2, searchHumanTaskInstances.getCount());
        tasks = searchHumanTaskInstances.getResult();
        humanTaskInstance = tasks.get(0);
        assertEquals("userTask2", humanTaskInstance.getName());
        humanTaskInstance = tasks.get(1);
        assertEquals("userTask3", humanTaskInstance.getName());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchAssignedAndPendingHumanTasks() throws Exception {
        final User john = createUser("John", PASSWORD);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).addUserTask("userTask2", ACTOR_NAME)
                .addUserTask("userTask3", ACTOR_NAME).addUserTask("task4", ACTOR_NAME).addUserTask("userTask5", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, Arrays.asList(user, john));
        // -------- start process and wait for tasks
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("userTask1", pi0);
        waitForUserTaskAndAssigneIt("userTask2", pi0, user);
        waitForUserTaskAndAssigneIt("userTask3", pi0, user);
        waitForUserTaskAndAssigneIt("task4", pi0, john);
        waitForUserTask("userTask5", pi0);

        // -------- test assigned & pending task search methods
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        SearchResult<HumanTaskInstance> searchHumanTaskInstances = getProcessAPI()
                .searchAssignedAndPendingHumanTasks(processDefinition.getId(), builder.done());
        assertEquals(5, searchHumanTaskInstances.getCount());
        List<HumanTaskInstance> tasks = searchHumanTaskInstances.getResult();
        assertEquals("task4", tasks.get(0).getName());
        assertEquals("userTask1", tasks.get(1).getName());
        assertEquals("userTask2", tasks.get(2).getName());

        // -------- test assign task search methods
        builder = new SearchOptionsBuilder(0, 10);
        builder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, user.getId());
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchHumanTaskInstances = getProcessAPI().searchAssignedAndPendingHumanTasks(processDefinition.getId(), builder.done());
        assertEquals(2, searchHumanTaskInstances.getCount());
        tasks = searchHumanTaskInstances.getResult();
        assertEquals("userTask2", tasks.get(0).getName());
        assertEquals("userTask3", tasks.get(1).getName());

        disableAndDeleteProcess(processDefinition);
        deleteUser(john);
    }

    @Test
    public void searchAssignedAndPendingHumanTasksFor() throws Exception {
        final User john = createUser("John", PASSWORD);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).addUserTask("userTask2", ACTOR_NAME)
                .addUserTask("userTask3", ACTOR_NAME).addUserTask("task4", ACTOR_NAME).addUserTask("userTask5", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, Arrays.asList(user, john));
        // -------- start process and wait for tasks
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("userTask1", pi0);
        waitForUserTaskAndAssigneIt("userTask2", pi0, user);
        waitForUserTaskAndAssigneIt("userTask3", pi0, user);
        waitForUserTaskAndAssigneIt("task4", pi0, john);
        waitForUserTask("userTask5", pi0);

        // -------- test pending task search methods
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        SearchResult<HumanTaskInstance> searchHumanTaskInstances = getProcessAPI()
                .searchAssignedAndPendingHumanTasksFor(processDefinition.getId(), john.getId(), builder.done());
        assertEquals(3, searchHumanTaskInstances.getCount());
        List<HumanTaskInstance> tasks = searchHumanTaskInstances.getResult();
        assertEquals("task4", tasks.get(0).getName());
        assertEquals("userTask1", tasks.get(1).getName());
        assertEquals("userTask5", tasks.get(2).getName());

        // -------- test assign task search methods
        builder = new SearchOptionsBuilder(0, 10);
        builder.filter(HumanTaskInstanceSearchDescriptor.NAME, "userTask1");
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchHumanTaskInstances = getProcessAPI().searchAssignedAndPendingHumanTasksFor(processDefinition.getId(), john.getId(), builder.done());
        assertEquals(1, searchHumanTaskInstances.getCount());
        tasks = searchHumanTaskInstances.getResult();
        assertEquals("userTask1", tasks.get(0).getName());

        disableAndDeleteProcess(processDefinition);
        deleteUser(john);
    }

    @Test
    public void searchPendingTasksManagedBy() throws Exception {
        // Create tasks, some to some users managed by "manager", some to other users with different manager.
        final User jack = createUser("jack", "bpm");
        // Jules is not subordinates of jack:
        final User jules = createUser("jules", "bpm");
        final User john = createUser("john", "bpm", jack.getId());

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription("Famous French actor");
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).addUserTask("userTask2", ACTOR_NAME)
                .addUserTask("userTask3", ACTOR_NAME).addUserTask("task4", ACTOR_NAME).addUserTask("userTask5", ACTOR_NAME)
                .addUserTask("userTask6", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, john);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("userTask1", pi0);
        waitForUserTask("userTask2", pi0);
        waitForUserTask("userTask3", pi0);
        waitForUserTask("task4", pi0);
        waitForUserTask("userTask5", pi0);
        waitForUserTask("userTask6", pi0);

        // filter all *userTask*, managedBy jack:
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm("userTask");
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        SearchResult<HumanTaskInstance> aHumanTasksRes = getProcessAPI().searchPendingTasksManagedBy(jack.getId(), builder.done());
        assertEquals(5, aHumanTasksRes.getCount());
        List<HumanTaskInstance> tasks = aHumanTasksRes.getResult();
        HumanTaskInstance humanTaskInstance = tasks.get(0);
        assertEquals("userTask1", humanTaskInstance.getName());
        humanTaskInstance = tasks.get(1);
        assertEquals("userTask2", humanTaskInstance.getName());
        humanTaskInstance = tasks.get(2);
        assertEquals("userTask3", humanTaskInstance.getName());
        humanTaskInstance = tasks.get(3);
        assertEquals("userTask5", humanTaskInstance.getName());
        humanTaskInstance = tasks.get(4);
        assertEquals("userTask6", humanTaskInstance.getName());

        // filter all *userTask*, managedBy jules:
        builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm("userTask");
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        aHumanTasksRes = getProcessAPI().searchPendingTasksManagedBy(jules.getId(), builder.done());
        assertEquals(0, aHumanTasksRes.getCount());
        assertTrue(aHumanTasksRes.getResult().isEmpty());

        // filter task4, managedBy jack:
        builder = new SearchOptionsBuilder(0, 10);
        builder.searchTerm("task");
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.DESC);
        aHumanTasksRes = getProcessAPI().searchPendingTasksManagedBy(jack.getId(), builder.done());
        assertEquals(1, aHumanTasksRes.getCount());
        tasks = aHumanTasksRes.getResult();
        humanTaskInstance = tasks.get(0);
        assertEquals("task4", humanTaskInstance.getName());

        disableAndDeleteProcess(processDefinition);
        deleteUser(john.getId());
        deleteUser(jack.getId());
        deleteUser(jules.getId());
    }

    @Cover(classes = { SearchOptionsBuilder.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "SearchPendingTasks", "Apostrophe" }, jira = "ENGINE-366")
    public void searchPendingTasksWithApostrophe() throws Exception {
        searchPendingTasks("userTask'1", ACTOR_NAME);
        searchPendingTasks("userTask1", "ACTOR'NAME");
    }

    @Test
    public void searchPendingTasksWithMultipleWords() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask", ACTOR_NAME).addUserTask("step1", ACTOR_NAME)
                .addUserTask("etape1", ACTOR_NAME).addUserTask("tache", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        // -------- start process and wait for tasks
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("userTask", processInstance);
        waitForUserTask("step1", processInstance);
        waitForUserTask("etape1", processInstance);
        waitForUserTask("tache", processInstance);

        // -------- test pending task search methods
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, 0);
        builder.filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE);
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        builder.searchTerm("eta userTask step");
        final SearchResult<HumanTaskInstance> searchHumanTaskInstances = getProcessAPI().searchHumanTaskInstances(builder.done());
        assertEquals(3, searchHumanTaskInstances.getCount());
        final List<HumanTaskInstance> tasks = searchHumanTaskInstances.getResult();
        assertThat(tasks, nameAre("etape1", "step1", "userTask"));

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchActivityTaskInstancesAdvancedFilters() throws Exception {
        // define a process containing one userTask.
        final String taskName = "ActivityForUser";
        final DesignProcessDefinition designProcessDef = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList(taskName),
                Arrays.asList(true));
        final ProcessDefinition processDef = deployAndEnableProcessWithActor(designProcessDef, ACTOR_NAME, user);
        // start twice and get 2 processInstances for processDef
        final ProcessInstance pi1 = getProcessAPI().startProcess(processDef.getId());
        waitForUserTask(taskName, pi1);
        Thread.sleep(5);
        final long afterCreationTask1 = System.currentTimeMillis();
        final ProcessInstance pi2 = getProcessAPI().startProcess(processDef.getId());
        waitForUserTask(taskName, pi2);
        final long afterCreationTask2 = System.currentTimeMillis();
        Thread.sleep(5);
        final ProcessInstance pi3 = getProcessAPI().startProcess(processDef.getId());
        waitForUserTask(taskName, pi3);

        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDef.getId());
        SearchResult<ActivityInstance> activityInstancesSearch = getProcessAPI().searchActivities(searchOptionsBuilder.done());
        assertEquals(3, activityInstancesSearch.getCount());

        // ********* LESS_THAN operator *********
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDef.getId());
        searchOptionsBuilder.lessThan(ActivityInstanceSearchDescriptor.LAST_MODIFICATION_DATE, afterCreationTask1);
        activityInstancesSearch = getProcessAPI().searchActivities(searchOptionsBuilder.done());
        assertEquals(1, activityInstancesSearch.getCount());
        assertEquals(pi1.getId(), activityInstancesSearch.getResult().get(0).getParentProcessInstanceId());

        // ********* BETWEEN operator *********
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDef.getId());
        searchOptionsBuilder.between(ActivityInstanceSearchDescriptor.LAST_MODIFICATION_DATE, afterCreationTask1, afterCreationTask2);
        activityInstancesSearch = getProcessAPI().searchActivities(searchOptionsBuilder.done());
        assertEquals(1, activityInstancesSearch.getCount());
        final ActivityInstance activityInstance2 = activityInstancesSearch.getResult().get(0);
        assertEquals(pi2.getId(), activityInstance2.getParentProcessInstanceId());

        // ********* DIFFERENT FROM operator *********
        SearchOptionsBuilder sob = new SearchOptionsBuilder(0, 10);
        sob.filter(HumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDef.getId());
        sob.differentFrom(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, 0);
        final SearchOptions searchOpts = sob.done();
        SearchResult<HumanTaskInstance> humanTasksSR = getProcessAPI().searchHumanTaskInstances(searchOpts);
        assertEquals(0, humanTasksSR.getCount());

        // assign one task
        getProcessAPI().assignUserTask(activityInstance2.getId(), user.getId());

        // Should then be 1 task assigned (ASSIGNEE_ID different from 0):
        humanTasksSR = getProcessAPI().searchHumanTaskInstances(searchOpts);
        assertEquals(1, humanTasksSR.getCount());

        // ********* OR operator *********
        // PROCESS_INSTANCE_ID = pi1 OR ASSIGNEE_ID different from 0 (from pi2):
        sob = new SearchOptionsBuilder(0, 10);
        sob.filter(HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, pi1.getId());
        sob.or();
        sob.differentFrom(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, 0);
        humanTasksSR = getProcessAPI().searchHumanTaskInstances(sob.done());
        assertEquals(2, humanTasksSR.getCount());

        disableAndDeleteProcess(processDef);
    }

    @Cover(classes = { SearchOptionsBuilder.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "SearchArchivedActivities", "Apostrophe" }, jira = "ENGINE-366")
    @Test
    public void searchActivityTaskInstancesWithApostrophe() throws Exception {
        // define a process containing one userTask.
        final String taskName = "Activity'ForUser";
        final DesignProcessDefinition designProcessDef = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList(taskName),
                Arrays.asList(true));
        final ProcessDefinition processDef = deployAndEnableProcessWithActor(designProcessDef, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDef.getId());
        waitForUserTask(taskName, processInstance);

        // Search apostrophe
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.searchTerm("Activity'");
        final SearchResult<ActivityInstance> activityInstancesSearch = getProcessAPI().searchActivities(searchOptionsBuilder.done());
        assertEquals(1, activityInstancesSearch.getCount());
        assertEquals(processInstance.getId(), activityInstancesSearch.getResult().get(0).getParentProcessInstanceId());

        disableAndDeleteProcess(processDef);
    }

    @Test
    public void searchPendingTasksWithLikeWildcardsCharacters() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("step#1a", ACTOR_NAME).addUserTask("step#1_b", ACTOR_NAME)
                .addUserTask("step#1_c", ACTOR_NAME).addUserTask("%step#2", ACTOR_NAME).addUserTask("mystep3", ACTOR_NAME).addUserTask("%step#4_a", ACTOR_NAME)
                .getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        // -------- start process and wait for tasks
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("step#1a", processInstance);
        waitForUserTask("step#1_b", processInstance);
        waitForUserTask("step#1_c", processInstance);
        waitForUserTask("%step#2", processInstance);
        waitForUserTask("mystep3", processInstance);
        waitForUserTask("%step#4_a", processInstance);

        // -------- test pending task search methods
        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        builder.searchTerm("step#");
        final SearchResult<HumanTaskInstance> searchHumanTaskInstancesWithEscapeCharacter = getProcessAPI().searchHumanTaskInstances(builder.done());
        assertEquals(3, searchHumanTaskInstancesWithEscapeCharacter.getCount());
        List<HumanTaskInstance> tasks = searchHumanTaskInstancesWithEscapeCharacter.getResult();
        assertThat(tasks, namesContain("step#1_b", "step#1_c", "step#1a"));

        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        builder.searchTerm("step#1_");
        final SearchResult<HumanTaskInstance> searchHumanTaskInstancesWithUnderscoreCharacter = getProcessAPI().searchHumanTaskInstances(builder.done());
        assertEquals(2, searchHumanTaskInstancesWithUnderscoreCharacter.getCount());
        tasks = searchHumanTaskInstancesWithUnderscoreCharacter.getResult();
        assertThat(tasks, nameAre("step#1_b", "step#1_c"));

        builder = new SearchOptionsBuilder(0, 10);
        builder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        builder.searchTerm("%step#");
        final SearchResult<HumanTaskInstance> searchHumanTaskInstancesWithPercentageCharacter = getProcessAPI().searchHumanTaskInstances(builder.done());
        assertEquals(2, searchHumanTaskInstancesWithPercentageCharacter.getCount());
        tasks = searchHumanTaskInstancesWithPercentageCharacter.getResult();
        assertThat(tasks, nameAre("%step#2", "%step#4_a"));

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { SendTaskInstance.class }, concept = BPMNConcept.ACTIVITIES, jira = "ENGINE-1404", keywords = { "send task", "search" })
    @Test
    public void searchSendTask() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        processBuilder.addSendTask("sendTask", "myMessage", new ExpressionBuilder().createConstantStringExpression("p1"))
                .addConnector("wait900ms", "testConnectorLongToExecute", "1.0.0", ConnectorEvent.ON_ENTER)
                .addInput("timeout", new ExpressionBuilder().createConstantLongExpression(900));
        processBuilder.addAutomaticTask("autoTask");
        processBuilder.addUserTask("userTask", ACTOR_NAME);
        processBuilder.addTransition("autoTask", "sendTask");
        processBuilder.addTransition("sendTask", "userTask");
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorLongToExecute(processBuilder, ACTOR_NAME, user);
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ArchivedActivityInstanceSearchDescriptor.NAME, Order.ASC);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForActivity("sendTask", processInstance);

        final SearchResult<ActivityInstance> searchActivities = getProcessAPI().searchActivities(builder.done());
        assertEquals(1, searchActivities.getCount());
        final List<ActivityInstance> activities = searchActivities.getResult();
        final ActivityInstance activity = activities.get(0);
        assertEquals("sendTask", activity.getName());
        waitForUserTask("userTask", processInstance);

        disableAndDeleteProcess(processDefinition);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorLongToExecute(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user, null, "TestConnectorLongToExecute.impl",
                TestConnectorLongToExecute.class, "TestConnectorLongToExecute.jar");
    }

}
