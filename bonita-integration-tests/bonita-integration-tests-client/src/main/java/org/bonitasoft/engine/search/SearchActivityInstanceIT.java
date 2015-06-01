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

import org.bonitasoft.engine.TestWithUser;
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
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.connectors.TestConnectorLongToExecute;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbOfActivities;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Celine Souchet
 */
public class SearchActivityInstanceIT extends TestWithUser {

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
        waitForUserTask(processInstance, "userTask");
        waitForUserTask(processInstance, "secondTask");

        // Check that no tasks are archived yet:
        SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 12);
        searchBuilder.filter(ArchivedActivityInstanceSearchDescriptor.ROOT_PROCESS_INSTANCE_ID, processInstance.getId());
        SearchResult<ArchivedActivityInstance> archActivitResult = getProcessAPI().searchArchivedActivities(searchBuilder.done());
        assertEquals(0, archActivitResult.getCount());

        // Skip the 2 tasks one by one:
        final List<ActivityInstance> activities = getProcessAPI().getActivities(processInstance.getId(), 0, 2);
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
        final long stepId1 = waitForUserTaskAndAssigneIt(pi0, "userTask1", john).getId();
        final long stepId2 = waitForUserTaskAndAssigneIt(pi0, "userTask2", john).getId();
        final long stepId3 = waitForUserTaskAndAssigneIt(pi0, "userTask3", jack).getId();
        final long stepId4 = waitForUserTaskAndAssigneIt(pi0, "task4", john).getId();
        final long stepId5 = waitForUserTaskAndAssigneIt(pi0, "userTask5", jules).getId();
        final long stepId6 = waitForUserTask(pi0, "userTask6");
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
        waitForUserTaskAndAssigneIt(processInstance, "userTask1", john).getId();
        waitForUserTaskAndAssigneIt(processInstance, "userTask2", john).getId();
        waitForUserTaskAndAssigneIt(processInstance, "userTask3", jack).getId();
        waitForUserTaskAndAssigneIt(processInstance, "task4", john).getId();

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
        final Group group = createGroup("groupName");
        final Role role = createRole("roleName");

        // First process def with 2 instances:
        final DesignProcessDefinition designProcessDef1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(Arrays.asList("initTask1"),
                Arrays.asList(true));
        final ProcessDefinition processDef1 = deployAndEnableProcessWithActor(designProcessDef1, ACTOR_NAME, user);
        final ProcessInstance pi1 = getProcessAPI().startProcess(processDef1.getId());
        final long step1Id = waitForUserTask(pi1, "initTask1");
        final ProcessInstance pi2 = getProcessAPI().startProcess(processDef1.getId());
        waitForUserTask(pi2, "initTask1");

        final DesignProcessDefinition designProcessDef2 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME + 2, PROCESS_VERSION,
                Arrays.asList("initTask2"), Arrays.asList(true));
        final ProcessDefinition processDef2 = deployAndEnableProcessWithActor(designProcessDef2, ACTOR_NAME, user);
        final ProcessInstance pi3 = getProcessAPI().startProcess(processDef2.getId());
        waitForUserTask(pi3, "initTask2");
        final ProcessInstance pi4 = getProcessAPI().startProcess(processDef2.getId());
        waitForUserTask(pi4, "initTask2");
        final ProcessInstance pi5 = getProcessAPI().startProcess(processDef2.getId());
        waitForUserTask(pi5, "initTask2");

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        final SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(5, humanTasksSearch.getCount());

        searchHumanTaskInstancesFilteredByPriority(pi2);
        searchHumanTaskInstancesFilteredByAssigneeId();
        searchHumanTaskInstancesFilteredByProcessInstance(pi2);
        searchHumanTaskInstancesFilteredByProcessDefinition(processDef2);
        searchHumanTaskInstancesFilteredByState(step1Id);
        searchHumanTaskInstancesFilteredByUser(user.getId(), processDef1.getId());
        searchHumanTaskInstancesFilteredByGroup(group.getId(), processDef2.getId());
        searchHumanTaskInstancesFilteredByRole(role.getId(), processDef1.getId());
        searchHumanTaskInstancesFilteredByMembership(group.getId(), role.getId(), processDef2.getId());
        searchHumanTaskInstancesByTerm();

        disableAndDeleteProcess(processDef1, processDef2);
        deleteGroups(group);
        deleteRoles(role);
    }

    private void searchHumanTaskInstancesFilteredByUser(final long userId, final long processDefinitionId) throws Exception {
        final ProcessSupervisor supervisor = getProcessAPI().createProcessSupervisorForUser(processDefinitionId, userId);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.USER_ID, userId);
        final SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(2, humanTasksSearch.getCount());
        for (final HumanTaskInstance task : humanTasksSearch.getResult()) {
            assertEquals("initTask1", task.getName());
        }

        getProcessAPI().deleteSupervisor(supervisor.getSupervisorId());
    }

    private void searchHumanTaskInstancesFilteredByGroup(final long groupId, final long processDefinitionId) throws Exception {
        final ProcessSupervisor supervisor = getProcessAPI().createProcessSupervisorForGroup(processDefinitionId, groupId);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.GROUP_ID, groupId);
        final SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(3, humanTasksSearch.getCount());
        for (final HumanTaskInstance task : humanTasksSearch.getResult()) {
            assertEquals("initTask2", task.getName());
        }

        getProcessAPI().deleteSupervisor(supervisor.getSupervisorId());
    }

    private void searchHumanTaskInstancesFilteredByRole(final long roleId, final long processDefinitionId) throws Exception {
        final ProcessSupervisor supervisor = getProcessAPI().createProcessSupervisorForRole(processDefinitionId, roleId);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.ROLE_ID, roleId);
        final SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(2, humanTasksSearch.getCount());
        for (final HumanTaskInstance task : humanTasksSearch.getResult()) {
            assertEquals("initTask1", task.getName());
        }

        getProcessAPI().deleteSupervisor(supervisor.getSupervisorId());
    }

    private void searchHumanTaskInstancesFilteredByMembership(final long groupId, final long roleId, final long processDefinitionId) throws Exception {
        final ProcessSupervisor supervisor = getProcessAPI().createProcessSupervisorForMembership(processDefinitionId, groupId, roleId);

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.ROLE_ID, roleId);
        final SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(3, humanTasksSearch.getCount());
        for (final HumanTaskInstance task : humanTasksSearch.getResult()) {
            assertEquals("initTask2", task.getName());
        }

        final SearchOptionsBuilder searchOptionsBuilder2 = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder2.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder2.filter(HumanTaskInstanceSearchDescriptor.GROUP_ID, groupId);
        final SearchResult<HumanTaskInstance> humanTasksSearch2 = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder2.done());
        assertEquals(humanTasksSearch, humanTasksSearch2);

        getProcessAPI().deleteSupervisor(supervisor.getSupervisorId());
    }

    private void searchHumanTaskInstancesFilteredByState(final long step1Id) throws UpdateException, SearchException {
        getProcessAPI().assignUserTask(step1Id, user.getId());
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.STATE_NAME, ActivityStates.READY_STATE);
        final SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(5, humanTasksSearch.getCount());
        for (final HumanTaskInstance task : humanTasksSearch.getResult()) {
            assertEquals(ActivityStates.READY_STATE, task.getState());
        }
    }

    private void searchHumanTaskInstancesByTerm() throws SearchException {
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.searchTerm("initTask2");
        SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(3, humanTasksSearch.getCount());
        for (final HumanTaskInstance task : humanTasksSearch.getResult()) {
            assertEquals("initTask2", task.getName());
        }

        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.searchTerm("initTask");
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(5, humanTasksSearch.getCount());
        for (final HumanTaskInstance task : humanTasksSearch.getResult()) {
            assertTrue("keyword search sould return only tasks with name containing 'initTask'", task.getName().contains("initTask"));
        }
    }

    private void searchHumanTaskInstancesFilteredByProcessDefinition(final ProcessDefinition processDef2) throws SearchException {
        SearchOptionsBuilder searchOptionsBuilder;
        SearchResult<HumanTaskInstance> humanTasksSearch;
        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDef2.getId());
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(3, humanTasksSearch.getCount());
    }

    private void searchHumanTaskInstancesFilteredByProcessInstance(final ProcessInstance pi2) throws SearchException {
        SearchOptionsBuilder searchOptionsBuilder;
        SearchResult<HumanTaskInstance> humanTasksSearch;
        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, pi2.getId());
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(1, humanTasksSearch.getCount());
    }

    private void searchHumanTaskInstancesFilteredByAssigneeId() throws SearchException {
        // There should be no assigned tasks to 'user':
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, user.getId());
        SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(0, humanTasksSearch.getCount());

        // There should be 5 non-assigned tasks:
        searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.sort(HumanTaskInstanceSearchDescriptor.NAME, Order.ASC);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.ASSIGNEE_ID, 0L);
        humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(5, humanTasksSearch.getCount());
    }

    private void searchHumanTaskInstancesFilteredByPriority(final ProcessInstance pi2) throws UpdateException, SearchException {
        // There should be 1 task which priority is above_normal:
        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(pi2.getId(), 0, 10);
        getProcessAPI().setTaskPriority(activityInstances.get(0).getId(), TaskPriority.ABOVE_NORMAL);
        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 45);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.PRIORITY, TaskPriority.ABOVE_NORMAL);
        final SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchHumanTaskInstances(searchOptionsBuilder.done());
        assertEquals(1, humanTasksSearch.getCount());
        assertEquals(TaskPriority.ABOVE_NORMAL, humanTasksSearch.getResult().get(0).getPriority());
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
        waitForUserTask(processInstance, taskName);

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
        waitForUserTask(pi1, "userTask1");
        waitForUserTaskAndAssigneIt(pi1, "userTask2", user);
        waitForUserTask(pi1, "manualTask");

        waitForUserTask(pi2, "userTask1");
        waitForUserTask(pi2, "userTask2");
        waitForUserTask(pi2, "manualTask");

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
        searchOptionsBuilder.sort(ArchivedActivityInstanceSearchDescriptor.ARCHIVE_DATE, Order.DESC);
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
        final long step1Id = waitForUserTask(pi0, "userTask1");
        final long step2Id = waitForUserTaskAndAssigneIt(pi0, "userTask2", user).getId();

        getProcessAPI().setActivityStateById(step1Id, 12);
        getProcessAPI().setActivityStateById(step2Id, 12);
        waitForProcessToFinish(pi0);

        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(ArchivedHumanTaskInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
        builder.sort(ArchivedHumanTaskInstanceSearchDescriptor.NAME, Order.DESC);
        SearchResult<ArchivedHumanTaskInstance> taskInstanceSearchResult = getProcessAPI().searchArchivedHumanTasks(builder.done());
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
    public void searchArchivedActivitiesInTerminalState() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).addUserTask("userTask2", ACTOR_NAME)
                .addUserTask("userTask3", ACTOR_NAME).addTransition("userTask2", "userTask3").getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance pi0 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndAssigneIt(pi0, "userTask1", user);
        waitForUserTaskAndExecuteIt(pi0, "userTask2", user);
        waitForUserTask(pi0, "userTask3");

        SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10).filter(ArchivedFlowNodeInstanceSearchDescriptor.TERMINAL, true);
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
        waitForUserTask(pi0, taskName);

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
        waitForUserTask(processInstance, "Request");
        waitForUserTask(processInstance, "Request2");

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 20);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.NAME, "Request2");
        final SearchResult<HumanTaskInstance> humanTasksSearch = getProcessAPI().searchPendingTasksForUser(user.getId(), searchOptionsBuilder.done());
        assertEquals(1, humanTasksSearch.getCount());
        final HumanTaskInstance userTaskId = humanTasksSearch.getResult().get(0);
        assignAndExecuteStep(userTaskId, user.getId());
        waitForUserTask(processInstance, "Approval2");

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
        waitForUserTask(pi0, "userTask1");
        waitForUserTaskAndAssigneIt(pi0, "userTask2", user);
        waitForUserTaskAndAssigneIt(pi0, "userTask3", user);
        waitForUserTask(pi0, "task4");
        waitForUserTask(pi0, "userTask5");

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
        waitForUserTask(pi0, "userTask1");
        waitForUserTaskAndAssigneIt(pi0, "userTask2", user);
        waitForUserTaskAndAssigneIt(pi0, "userTask3", user);
        waitForUserTaskAndAssigneIt(pi0, "task4", john);
        waitForUserTask(pi0, "userTask5");

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
        waitForUserTask(pi0, "userTask1");
        waitForUserTaskAndAssigneIt(pi0, "userTask2", user);
        waitForUserTaskAndAssigneIt(pi0, "userTask3", user);
        waitForUserTaskAndAssigneIt(pi0, "task4", john);
        waitForUserTask(pi0, "userTask5");

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
        waitForUserTask(pi0, "userTask1");
        waitForUserTask(pi0, "userTask2");
        waitForUserTask(pi0, "userTask3");
        waitForUserTask(pi0, "task4");
        waitForUserTask(pi0, "userTask5");
        waitForUserTask(pi0, "userTask6");

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
        deleteUsers(john, jack, jules);
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
        waitForUserTask(processInstance, "userTask");
        waitForUserTask(processInstance, "step1");
        waitForUserTask(processInstance, "etape1");
        waitForUserTask(processInstance, "tache");

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
        waitForUserTask(pi1, taskName);
        Thread.sleep(5);
        final long afterCreationTask1 = System.currentTimeMillis();
        final ProcessInstance pi2 = getProcessAPI().startProcess(processDef.getId());
        waitForUserTask(pi2, taskName);
        final long afterCreationTask2 = System.currentTimeMillis();
        Thread.sleep(5);
        final ProcessInstance pi3 = getProcessAPI().startProcess(processDef.getId());
        waitForUserTask(pi3, taskName);

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

        // ********** all fields **********
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.ACTIVITY_TYPE, activityInstance2.getType());
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.DISPLAY_NAME, activityInstance2.getDisplayName());
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.NAME, activityInstance2.getName());
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.PROCESS_DEFINITION_ID, activityInstance2.getProcessDefinitionId());
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.PROCESS_INSTANCE_ID, activityInstance2.getRootContainerId());
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID, activityInstance2.getParentProcessInstanceId());
        searchOptionsBuilder.filter(ActivityInstanceSearchDescriptor.LAST_MODIFICATION_DATE, activityInstance2.getLastUpdateDate().getTime());
        activityInstancesSearch = getProcessAPI().searchActivities(searchOptionsBuilder.done());

        assertEquals(1, activityInstancesSearch.getCount());
        final ActivityInstance activityInstance2Result = activityInstancesSearch.getResult().get(0);
        assertEquals(activityInstance2,activityInstance2Result);


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
        waitForUserTask(processInstance, taskName);

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
        waitForUserTask(processInstance, "step#1a");
        waitForUserTask(processInstance, "step#1_b");
        waitForUserTask(processInstance, "step#1_c");
        waitForUserTask(processInstance, "%step#2");
        waitForUserTask(processInstance, "mystep3");
        waitForUserTask(processInstance, "%step#4_a");

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
                .addConnector("wait2000ms", "testConnectorLongToExecute", "1.0.0", ConnectorEvent.ON_FINISH)
                .addInput("timeout", new ExpressionBuilder().createConstantLongExpression(2000));
        processBuilder.addAutomaticTask("autoTask");
        processBuilder.addUserTask("userTask", ACTOR_NAME);
        processBuilder.addTransition("autoTask", "sendTask");
        processBuilder.addTransition("sendTask", "userTask");
        final ProcessDefinition processDefinition = deployProcessWithActorAndTestConnectorLongToExecute(processBuilder, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        //        waitForFlowNodeInState(processInstance, "sendTask", TestStates.INITIALIZING, true);
        waitForFlowNodeInExecutingState(processInstance, "sendTask", true);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(ArchivedActivityInstanceSearchDescriptor.NAME, Order.ASC);
        final SearchResult<ActivityInstance> searchActivities = getProcessAPI().searchActivities(builder.done());
        assertEquals(1, searchActivities.getCount());
        final List<ActivityInstance> activities = searchActivities.getResult();
        final ActivityInstance activity = activities.get(0);
        assertEquals("sendTask", activity.getName());
        waitForUserTask(processInstance, "userTask");

        disableAndDeleteProcess(processDefinition);
    }

    public ProcessDefinition deployProcessWithActorAndTestConnectorLongToExecute(final ProcessDefinitionBuilder processDefinitionBuilder,
            final String actorName, final User user) throws BonitaException, IOException {
        return deployAndEnableProcessWithActorAndConnectorAndParameter(processDefinitionBuilder, actorName, user, null, "TestConnectorLongToExecute.impl",
                TestConnectorLongToExecute.class, "TestConnectorLongToExecute.jar");
    }

}
