package org.bonitasoft.engine.process.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.FlowNodeInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.SearchOptions;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.check.CheckNbPendingTasksForUserUsingSearch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class HiddenTaskTest extends CommonAPITest {

    private ProcessDefinition processDefinition;

    private User user;

    private SearchOptions searchOptions;

    private CheckNbPendingTasksForUserUsingSearch checkNbOPendingTasks;

    private User user2;

    @Before
    public void beforeTest() throws BonitaException {
        loginOnDefaultTenantWithDefaultTechnicalUser();

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                "ProcessContainingTasksToHide", "1.01beta", Arrays.asList("humanTask_1", "humanTask_2"), Arrays.asList(true, true), "actor", true, true);
        user = createUser("common_user", "abc");
        user2 = createUser("uncommon_user", "abc");
        processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, "actor", user);
        final long id = processDefinition.getId();
        getProcessAPI().startProcess(id);

        searchOptions = new SearchOptionsBuilder(0, 10).done();

        checkNbOPendingTasks = new CheckNbPendingTasksForUserUsingSearch(getProcessAPI(), 50, 3000, true, 2, user.getId(),
                new SearchOptionsBuilder(0, 100).done());
    }

    @After
    public void afterTest() throws BonitaException {
        disableAndDeleteProcess(processDefinition);
        deleteUser(user.getId());
        deleteUser(user2.getId());
        logoutOnTenant();
    }

    @Test
    public void hideTask() throws Exception {
        // get hidden tasks: there should not be humanTask_1
        // get pending tasks, hide humanTask_1
        // show pending tasks again: there should be no humanTask_1 anymore
        // show hidden tasks again: there should be humanTask_1

        assertTrue("There should be 2 pending tasks for " + user.getUserName(), checkNbOPendingTasks.waitUntil());

        // search Hidden tasks:
        SearchResult<HumanTaskInstance> humanTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(0, humanTasks.getCount());

        // Let's hide the first task:
        final List<HumanTaskInstance> pendingTasks = checkNbOPendingTasks.getPendingHumanTasks();
        final HumanTaskInstance task1 = pendingTasks.get(0);
        getProcessAPI().hideTasks(user.getId(), task1.getId());

        // There should be only 1 pending task for USER now:
        final List<HumanTaskInstance> pendingTasksForUser = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10,
                ActivityInstanceCriterion.NAME_ASC);
        assertEquals(1, pendingTasksForUser.size());

        // There should be 1 hidden task now:
        humanTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(1, humanTasks.getCount());
        // assertEquals("humanTask_1", humanTasks.getResult().get(0).getName());
    }

    @Test(expected = UpdateException.class)
    public void hideTask2Times() throws Exception {
        // get hidden tasks: there should not be humanTask_1
        // get pending tasks, hide humanTask_1
        // show pending tasks again: there should be no humanTask_1 anymore
        // show hidden tasks again: there should be humanTask_1

        assertTrue("There should be 2 pending tasks for " + user.getUserName(), checkNbOPendingTasks.waitUntil());

        // search Hidden tasks:
        final SearchResult<HumanTaskInstance> humanTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(0, humanTasks.getCount());

        // Let's hide the first task:
        final List<HumanTaskInstance> pendingTasks = checkNbOPendingTasks.getPendingHumanTasks();
        final HumanTaskInstance task1 = pendingTasks.get(0);
        getProcessAPI().hideTasks(user.getId(), task1.getId());
        getProcessAPI().hideTasks(user.getId(), task1.getId());
    }

    @Test
    public void hideTwoTasksInOneCall() throws Exception {
        final ProcessDefinitionBuilder definitionBuilder = new ProcessDefinitionBuilder().createNewInstance("hideTwoTasksInOneCall", "1.71");
        definitionBuilder.addStartEvent("start");
        final String actorName = "actor";
        definitionBuilder.addActor(actorName).addUserTask("humanTask_1", actorName).addUserTask("humanTask_2", actorName);
        final DesignProcessDefinition designProcessDef = definitionBuilder.done();
        final ProcessDefinition processDef = deployAndEnableProcessWithActor(designProcessDef, actorName, user);

        final long id = processDef.getId();
        final ProcessInstance procInstance = getProcessAPI().startProcess(id);

        final FlowNodeInstance task1 = waitForFlowNodeInReadyState(procInstance, "humanTask_1", false);
        final FlowNodeInstance task2 = waitForFlowNodeInReadyState(procInstance, "humanTask_2", false);
        getProcessAPI().hideTasks(user.getId(), task1.getId(), task2.getId());

        // search Hidden tasks:
        final SearchResult<HumanTaskInstance> humanTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(2, humanTasks.getCount());

        disableAndDeleteProcess(processDef);
    }

    @Test
    public void unhideTask() throws Exception {
        // get pending tasks, hide a task
        // show pending tasks again: there should be 1 pending task
        // show hidden tasks: there should be 1 hidden task
        // unhide it, there should be back 0 hidden task and 2 pending tasks

        // wait for 2 Pending tasks:
        assertTrue("There should be 2 pending tasks for " + user.getUserName(), checkNbOPendingTasks.waitUntil());

        // Let's hide the first task:
        final List<HumanTaskInstance> pendingTasks = checkNbOPendingTasks.getPendingHumanTasks();
        final HumanTaskInstance task1 = pendingTasks.get(0);
        getProcessAPI().hideTasks(user.getId(), task1.getId());

        // search Hidden tasks:
        SearchResult<HumanTaskInstance> hiddenTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(1, hiddenTasks.getCount());

        // un-hide it:
        getProcessAPI().unhideTasks(user.getId(), hiddenTasks.getResult().get(0).getId());

        // There should be back only 2 pending task for USER now:
        final List<HumanTaskInstance> pendingTasksForUser = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10,
                ActivityInstanceCriterion.NAME_ASC);
        assertEquals(2, pendingTasksForUser.size());

        // There should not be any hidden task now:
        hiddenTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(0, hiddenTasks.getCount());
    }

    @Test
    public void claimedTaskShouldBeUnhidden() throws Exception {
        // get pending tasks, hide a task
        // show hidden tasks: there there should be 1 hidden task
        // assign it to someone, show hidden tasks again: there should be 0 hidden task
        // unassign it, there should still be 0 hidden tasks and 2 pending tasks

        // wait for 2 Pending tasks:
        assertTrue("There should be 2 pending tasks for " + user.getUserName(), checkNbOPendingTasks.waitUntil());

        // Let's hide the first task:
        final List<HumanTaskInstance> pendingTasks = checkNbOPendingTasks.getPendingHumanTasks();
        final HumanTaskInstance task1 = pendingTasks.get(0);
        getProcessAPI().hideTasks(user.getId(), task1.getId());

        // search Hidden tasks:
        SearchResult<HumanTaskInstance> hiddenTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(1, hiddenTasks.getCount());

        getProcessAPI().assignUserTask(task1.getId(), user.getId());

        hiddenTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(0, hiddenTasks.getCount());

        getProcessAPI().releaseUserTask(task1.getId());

        hiddenTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(0, hiddenTasks.getCount());
    }

    @Test
    public void hiddenTasksNotShownIfNotActorAnymore() throws Exception {
        // get pending tasks, hide a task
        // show hidden tasks: there there should be 1 hidden task
        // admin removes me from the actor. Show hidden tasks again: there there should be 0 hidden task
        // admin adds me back in the actor. Show hidden tasks again: there should be back 1 hidden task

        // wait for 2 Pending tasks:
        assertTrue("There should be 2 pending tasks for " + user.getUserName(), checkNbOPendingTasks.waitUntil());

        // Let's hide the first task:
        final List<HumanTaskInstance> pendingTasks = checkNbOPendingTasks.getPendingHumanTasks();
        final HumanTaskInstance task1 = pendingTasks.get(0);
        getProcessAPI().hideTasks(user.getId(), task1.getId());

        // search Hidden tasks:
        SearchResult<HumanTaskInstance> hiddenTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(1, hiddenTasks.getCount());

        final List<ActorInstance> actors = getProcessAPI().getActors(processDefinition.getId(), 0, 10, ActorCriterion.NAME_ASC);
        final long actorId = actors.get(0).getId();
        final List<ActorMember> actorMembers = getProcessAPI().getActorMembers(actorId, 0, 10);
        getProcessAPI().removeActorMember(actorMembers.get(0).getId());

        hiddenTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(0, hiddenTasks.getCount());

        getProcessAPI().addUserToActor(actorId, user.getId());

        hiddenTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(1, hiddenTasks.getCount());
    }

    @Test
    public void isTaskHidden() throws Exception {
        logoutLogin(user);
        assertTrue("There should be 2 pending tasks for " + user.getUserName(), checkNbOPendingTasks.waitUntil());

        final SearchResult<HumanTaskInstance> humanTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(0, humanTasks.getCount());

        // Let's hide the first task:
        final List<HumanTaskInstance> pendingTasks = checkNbOPendingTasks.getPendingHumanTasks();
        final HumanTaskInstance task1 = pendingTasks.get(0);
        assertFalse(getProcessAPI().isTaskHidden(task1.getId(), user.getId()));
        getProcessAPI().hideTasks(user.getId(), task1.getId());
        assertTrue(getProcessAPI().isTaskHidden(task1.getId(), user.getId()));
        assertFalse(getProcessAPI().isTaskHidden(pendingTasks.get(1).getId(), user.getId()));
        logoutLogin(user2);
        assertFalse(getProcessAPI().isTaskHidden(task1.getId(), user2.getId()));
        logoutLogin(null);
    }

    @Test
    public void listTaskHidden() throws Exception {
        logoutLogin(user);
        assertTrue("There should be 2 pending tasks for " + user.getUserName(), checkNbOPendingTasks.waitUntil());

        // There should be no hidden tasks there
        final SearchResult<HumanTaskInstance> humanTasks = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(0, humanTasks.getCount());

        // Let's hide the first task:
        final List<HumanTaskInstance> pendingTasks = checkNbOPendingTasks.getPendingHumanTasks();
        final HumanTaskInstance task1 = pendingTasks.get(0);
        getProcessAPI().hideTasks(user.getId(), task1.getId());

        // There should be 1 hidden task there
        final SearchResult<HumanTaskInstance> humanTasks2 = getProcessAPI().searchPendingHiddenTasks(user.getId(), searchOptions);
        assertEquals(1, humanTasks2.getCount());

        // Change user
        logoutLogin(user2);

        // There should be no hidden task there
        final SearchResult<HumanTaskInstance> humanTasks3 = getProcessAPI().searchPendingHiddenTasks(user2.getId(), searchOptions);
        assertEquals(0, humanTasks3.getCount());
    }

    private void logoutLogin(final User user) throws BonitaException {
        logoutOnTenant();
        if (user != null) {
            loginOnDefaultTenantWith(user.getUserName(), "abc");
            checkNbOPendingTasks = new CheckNbPendingTasksForUserUsingSearch(getProcessAPI(), 50, 3000, true, 2, user.getId(),
                    new SearchOptionsBuilder(0, 100).done());
        } else {
            loginOnDefaultTenantWithDefaultTechnicalUser();
            checkNbOPendingTasks = new CheckNbPendingTasksForUserUsingSearch(getProcessAPI(), 50, 3000, true, 2, this.user.getId(), new SearchOptionsBuilder(0,
                    100).done());
        }
    }

}
