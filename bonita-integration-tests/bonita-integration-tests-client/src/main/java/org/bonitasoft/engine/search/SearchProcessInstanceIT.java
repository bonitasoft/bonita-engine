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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstancesSearchDescriptor;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.process.impl.SubProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.TestConnectorThatThrowException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.SearchException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.identity.UserSearchDescriptor;
import org.bonitasoft.engine.identity.UserUpdater;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 * @author Celine Souchet
 */
public class SearchProcessInstanceIT extends TestWithUser {

    @Test
    public void searchOpenProcessInstances() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process",
                "1.0", Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance instance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance4 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance5 = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(instance1, "step1");
        waitForUserTask(instance2, "step1");
        waitForUserTask(instance3, "step1");
        waitForUserTask(instance4, "step1");
        waitForUserTask(instance5, "step1");

        // search and check result ASC
        final SearchOptionsBuilder searchOptions1 = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 0, 2, ProcessInstanceSearchDescriptor.ID,
                Order.ASC);
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstances(searchOptions1.done());
        assertNotNull(result);
        assertEquals(5, result.getCount());
        final List<ProcessInstance> processInstanceList1 = result.getResult();
        assertNotNull(processInstanceList1);
        assertEquals(2, processInstanceList1.size());
        assertEquals(instance1.getId(), processInstanceList1.get(0).getId());
        assertEquals(instance2.getId(), processInstanceList1.get(1).getId());

        final SearchOptionsBuilder searchOptions2 = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 4, 2, ProcessInstanceSearchDescriptor.ID,
                Order.ASC);
        result = getProcessAPI().searchOpenProcessInstances(searchOptions2.done());
        assertNotNull(result);
        assertEquals(5, result.getCount());
        final List<ProcessInstance> processInstanceList2 = result.getResult();
        assertNotNull(processInstanceList2);
        assertEquals(1, processInstanceList2.size());
        assertEquals(instance5.getId(), processInstanceList2.get(0).getId());

        final SearchOptionsBuilder searchOptions3 = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 0, 3, ProcessInstanceSearchDescriptor.ID,
                Order.DESC);
        // search and check result DESC
        result = getProcessAPI().searchOpenProcessInstances(searchOptions3.done());
        assertNotNull(result);
        assertEquals(5, result.getCount());
        final List<ProcessInstance> processInstanceList3 = result.getResult();
        assertNotNull(processInstanceList3);
        assertEquals(3, processInstanceList3.size());
        assertEquals(instance5.getId(), processInstanceList3.get(0).getId());
        assertEquals(instance4.getId(), processInstanceList3.get(1).getId());
        assertEquals(instance3.getId(), processInstanceList3.get(2).getId());

        final SearchOptionsBuilder searchOptions4 = BuildTestUtil.buildSearchOptions(processDefinition.getId() + 1, 0, 3, ProcessInstanceSearchDescriptor.ID,
                Order.DESC);
        result = getProcessAPI().searchOpenProcessInstances(searchOptions4.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessInstance.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, jira = "BS-11031", keywords = { "Failed task",
            "Failed process" })
    @Test
    public void searchFailedProcessInstances() throws Exception {
        // Build a process with a failed connector on enter
        final ProcessDefinition processDefinitionWithFailedConnector = deployAndEnableProcessWithConnector(BuildTestUtil
                .buildProcessDefinitionWithFailedConnector("Process with failed connector in enter"), "TestConnectorThatThrowException.impl",
                TestConnectorThatThrowException.class, "TestConnectorThatThrowException.jar");

        // Build a process with a failed task
        final ProcessDefinition processDefinitionWithFailedTask = deployAndEnableProcessWithConnector(BuildTestUtil
                .buildProcessDefinitionWithAutomaticTaskAndFailedConnector("Process with failed task"), "TestConnectorThatThrowException.impl",
                TestConnectorThatThrowException.class, "TestConnectorThatThrowException.jar");

        final ProcessInstance instance1 = getProcessAPI().startProcess(processDefinitionWithFailedConnector.getId());
        waitForProcessToBeInState(instance1, ProcessInstanceState.ERROR);

        final ProcessInstance instance2 = getProcessAPI().startProcess(processDefinitionWithFailedTask.getId());
        waitForFlowNodeInFailedState(instance2);

        final ProcessInstance instance3 = getProcessAPI().startProcess(processDefinitionWithFailedConnector.getId());
        waitForProcessToBeInState(instance3, ProcessInstanceState.ERROR);

        // search and check result ASC
        final SearchOptionsBuilder searchOptions1 = BuildTestUtil.buildSearchOptions(0, 2, ProcessInstanceSearchDescriptor.START_DATE, Order.ASC);
        SearchResult<ProcessInstance> result = getProcessAPI().searchFailedProcessInstances(searchOptions1.done());
        assertNotNull(result);
        assertEquals(3, result.getCount());
        final List<ProcessInstance> processInstanceList1 = result.getResult();
        assertNotNull(processInstanceList1);
        assertEquals(2, processInstanceList1.size());
        assertEquals(instance1.getId(), processInstanceList1.get(0).getId());
        assertEquals(instance2.getId(), processInstanceList1.get(1).getId());

        // search and check result DESC
        final SearchOptionsBuilder searchOptions2 = BuildTestUtil.buildSearchOptions(0, 3, ProcessInstanceSearchDescriptor.START_DATE, Order.DESC);
        result = getProcessAPI().searchFailedProcessInstances(searchOptions2.done());
        assertNotNull(result);
        assertEquals(3, result.getCount());
        final List<ProcessInstance> processInstanceList2 = result.getResult();
        assertEquals(3, processInstanceList2.size());
        assertEquals(instance3.getId(), processInstanceList2.get(0).getId());
        assertEquals(instance2.getId(), processInstanceList2.get(1).getId());
        assertEquals(instance1.getId(), processInstanceList2.get(2).getId());

        // Search only process instance with state ERROR
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 3);
        builder.filter(ProcessInstanceSearchDescriptor.STATE_NAME, ProcessInstanceState.ERROR);
        builder.sort(ProcessInstanceSearchDescriptor.START_DATE, Order.ASC);
        result = getProcessAPI().searchFailedProcessInstances(builder.done());
        assertNotNull(result);
        assertEquals(2, result.getCount());
        final List<ProcessInstance> processInstances = result.getResult();
        assertNotNull(processInstances);
        assertEquals(2, processInstances.size());
        assertEquals(instance1.getId(), processInstances.get(0).getId());
        assertEquals(instance3.getId(), processInstances.get(1).getId());

        disableAndDeleteProcess(processDefinitionWithFailedConnector, processDefinitionWithFailedTask);
    }

    /*
     * Start process not with jack
     * execute a task with jack
     * check archived process instances worked on are 0
     * finish process
     * check there is one archived process instance worked on
     */
    @Test
    public void searchArchivedProcessInstanceWorkedOnWithUserPerformedTask() throws Exception {
        // create process
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));

        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndExecuteIt(processInstance, "step1", user);

        final SearchOptionsBuilder searchOptions = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 0, 5, ProcessInstanceSearchDescriptor.ID,
                Order.ASC);
        // executed but not archived
        SearchResult<ArchivedProcessInstance> result = getProcessAPI().searchArchivedProcessInstancesInvolvingUser(user.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        waitForUserTaskAndExecuteIt(processInstance, "step2", user);

        // process finished: no more in worked on
        waitForProcessToFinish(processInstance);
        result = getProcessAPI().searchArchivedProcessInstancesInvolvingUser(user.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        final ArchivedProcessInstance processInstance2 = result.getResult().get(0);
        assertEquals(processInstance.getId(), processInstance2.getSourceObjectId());

        disableAndDeleteProcess(processDefinition);
    }

    /*
     * Start process with jack
     * execute a task with john
     * check archived process instances worked on are 0
     * finish process
     * check there is one archived process instance worked on
     */
    @Test
    public void searchArchivedProcessInstanceWorkedOnWithUserStartedProcess() throws Exception {
        // create user
        final User jack = createUser("jack", PASSWORD);
        final User john = createUser("john", PASSWORD);
        logoutOnTenant();
        loginOnDefaultTenantWith("john", PASSWORD);
        // create process
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1"), Arrays.asList(true));
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, jack);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask("step1");

        final SearchOptionsBuilder searchOptions = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 0, 5, ProcessInstanceSearchDescriptor.ID,
                Order.ASC);
        // the process is not started by jack but not finished: not in "workedOn"
        SearchResult<ArchivedProcessInstance> result = getProcessAPI().searchArchivedProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        // assign
        assignAndExecuteStep(step1Id, jack);

        // process finished: in worked on
        waitForProcessToFinish(processInstance);
        result = getProcessAPI().searchArchivedProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());

        disableAndDeleteProcess(processDefinition);
        deleteUsers(john, jack);
    }

    /*
     * Start process not with jack
     * assign task to jack
     * check worked on = 0
     * execute task with jack
     * check worked on = 1
     * finish process
     * check worked on = 0
     */
    @Test
    public void searchProcessInstanceWorkedOnWithUserPerformedTask() throws Exception {
        // create user
        final String username = "jack";
        final User jack = createUser(username, PASSWORD);
        final User john = createUser("john", PASSWORD);
        // create process
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, jack);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        logoutOnTenant();
        loginOnDefaultTenantWith(username, PASSWORD);
        final long step1Id = waitForUserTask(processInstance, "step1");

        final SearchOptionsBuilder searchOptions = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 0, 5, ProcessInstanceSearchDescriptor.ID,
                Order.ASC);
        // the process is not started by jack and jack has not performed tasks: not in "workedOn"
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        // assign
        getProcessAPI().assignUserTask(step1Id, jack.getId());

        // after assigned: still not worked on
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        getProcessAPI().executeFlowNode(step1Id);
        final long step2Id = waitForUserTask(processInstance, "step2");

        // one task was performed: the process is in "WorkedOn"
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        final ProcessInstance processInstance2 = result.getResult().get(0);
        assertEquals(processInstance.getId(), processInstance2.getId());
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        assignAndExecuteStep(step2Id, jack);

        // process finished: no more in worked on
        waitForProcessToFinish(processInstance);
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        disableAndDeleteProcess(processDefinition);
        deleteUsers(john, jack);
    }

    @Test
    public void searchProcessInstanceWorkedOnWithUserPerformedTaskOnMultipleInstances() throws Exception {
        // create user
        final String username = "jack";
        final User jack = createUser(username, PASSWORD);
        final User john = createUser("john", PASSWORD);
        // create process
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, jack);
        final ProcessInstance p1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p4 = getProcessAPI().startProcess(processDefinition.getId());

        logoutOnTenant();
        loginOnDefaultTenantWith(username, PASSWORD);
        waitForUserTaskAndExecuteIt(p1, "step1", jack);
        waitForUserTaskAndExecuteIt(p2, "step1", jack);
        waitForUserTaskAndExecuteIt(p3, "step1", jack);
        logoutOnTenant();
        loginOnDefaultTenantWith("john", PASSWORD);
        waitForUserTaskAndExecuteIt(p4, "step1", jack);

        waitForUserTask(p1, "step2");
        waitForUserTask(p2, "step2");
        waitForUserTask(p3, "step2");
        waitForUserTask(p4, "step2");

        final SearchOptionsBuilder searchOptions = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 0, 5, ProcessInstanceSearchDescriptor.ID,
                Order.ASC);
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(3, result.getCount());
        assertEquals(3, result.getResult().size());

        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        assertEquals(1, result.getResult().size());

        disableAndDeleteProcess(processDefinition);
        deleteUsers(john, jack);
    }

    @Test
    public void searchProcessInstanceWorkedOnWithUserStartedItOnMultipleInstances() throws Exception {
        // create user
        final String username = "jack";
        final User jack = createUser(username, PASSWORD);
        final User john = createUser("john", PASSWORD);
        // create process
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, jack);
        logoutOnTenant();
        loginOnDefaultTenantWith(username, PASSWORD);
        final ProcessInstance p1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p3 = getProcessAPI().startProcess(processDefinition.getId());
        logoutOnTenant();
        loginOnDefaultTenantWith("john", PASSWORD);
        final ProcessInstance p4 = getProcessAPI().startProcess(processDefinition.getId());

        waitForUserTask(p1, "step1");
        waitForUserTask(p2, "step1");
        waitForUserTask(p3, "step1");
        waitForUserTask(p4, "step1");

        final SearchOptionsBuilder searchOptions = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 0, 5, ProcessInstanceSearchDescriptor.ID,
                Order.ASC);
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(jack.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(3, result.getCount());
        assertEquals(3, result.getResult().size());

        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(john.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        assertEquals(1, result.getResult().size());

        disableAndDeleteProcess(processDefinition);
        deleteUsers(john, jack);
    }

    /*
     * Start process with jack
     * assign/execute task with john
     * check worked on = 1 for jack
     * finish process
     * check worked on = 0
     */
    @Test
    public void searchProcessInstanceWorkedOnWithUserStartedProcess() throws Exception {
        // create user
        final User jack = createUser("jack", PASSWORD);
        // create process
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1"), Arrays.asList(true));

        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, jack);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask("step1");

        final SearchOptionsBuilder searchOptions = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 0, 5, ProcessInstanceSearchDescriptor.ID,
                Order.ASC);
        // the process is started by jack and jack has not performed tasks: In "workedOn"
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(user.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        assignAndExecuteStep(step1Id, jack);

        // process finished: no more in worked on
        waitForProcessToFinish(processInstance);
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUser(user.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        deleteUser(jack);
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Managed By", "Process instance", "Open", "Involving User",
            "User Performed Task" }, jira = "ENGINE-715")
    @Test
    public void searchOpenProcessInstancesInvolvingUsersManagedByWithUserPerformedTask() throws Exception {
        // create user
        final User paul = createUser("paul", "bpm");
        final User jack = createUser("jack", paul.getId());
        final User john = createUser("john", "bpm");

        // create process
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));

        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, jack);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        logoutOnTenant();
        loginOnDefaultTenantWith("jack", "bpm");
        final long step1Id = waitForUserTask("step1");

        final SearchOptionsBuilder searchOptions = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 0, 5, ProcessInstanceSearchDescriptor.ID,
                Order.ASC);
        // the process is not started by jack and jack has not performed tasks: not in "workedOn"
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        // assign
        getProcessAPI().assignUserTask(step1Id, jack.getId());

        // after assigned: still not worked on
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        getProcessAPI().executeFlowNode(step1Id);
        final long step2Id = waitForUserTask("step2");

        // one task was performed: the process is in "WorkedOn"
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        final ProcessInstance processInstance2 = result.getResult().get(0);
        assertEquals(processInstance.getId(), processInstance2.getId());

        assignAndExecuteStep(step2Id, jack);

        // process finished: no more in worked on
        waitForProcessToFinish(processInstance);
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        disableAndDeleteProcess(processDefinition);
        deleteUsers(john, jack, paul);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Managed By", "Process instance", "Open", "Involving User",
            "User Performed Task", "Multiple Instances" }, jira = "ENGINE-715")
    @Test
    public void searchOpenProcessInstancesInvolvingUsersManagedByWithUserPerformedTaskOnMultipleInstances() throws Exception {
        // create user
        final User paul = createUser("paul", "bpm");
        final User jack = createUser("jack", paul.getId());
        final User john = createUser("john", paul.getId());

        // create process
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, jack);
        final ProcessInstance p1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p4 = getProcessAPI().startProcess(processDefinition.getId());

        logoutOnTenant();
        loginOnDefaultTenantWith("jack", "bpm");
        waitForUserTaskAndExecuteIt(p1, "step1", jack);
        waitForUserTaskAndExecuteIt(p2, "step1", jack);
        waitForUserTaskAndExecuteIt(p3, "step1", jack);
        logoutOnTenant();
        loginOnDefaultTenantWith("john", "bpm");
        waitForUserTaskAndExecuteIt(p4, "step1", jack);

        waitForUserTask(p1, "step2");
        waitForUserTask(p2, "step2");
        waitForUserTask(p3, "step2");
        waitForUserTask(p4, "step2");

        final SearchOptionsBuilder searchOptions = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 0, 5, ProcessInstanceSearchDescriptor.ID,
                Order.ASC);
        final SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(4, result.getCount());
        assertEquals(4, result.getResult().size());

        disableAndDeleteProcess(processDefinition);
        deleteUsers(john, jack, paul);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Managed By", "Process instance", "Open", "Involving User",
            "User Started It", "Multiple Instances" }, jira = "ENGINE-715")
    @Test
    public void searchOpenProcessInstancesInvolvingUsersManagedByWithUserStartedItOnMultipleInstances() throws Exception {
        // create user
        final User paul = createUser("paul", "bpm");
        final User jack = createUser("jack", paul.getId());
        final User john = createUser("john", paul.getId());
        final User pierre = createUser("pierre", "bpm");
        // create process
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(true, true));

        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, jack);
        logoutOnTenant();
        loginOnDefaultTenantWith("jack", "bpm");
        final ProcessInstance p1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance p3 = getProcessAPI().startProcess(processDefinition.getId());
        logoutOnTenant();
        loginOnDefaultTenantWith("john", "bpm");
        final ProcessInstance p4 = getProcessAPI().startProcess(processDefinition.getId());
        logoutOnTenant();
        loginOnDefaultTenantWith("pierre", "bpm");
        final ProcessInstance p5 = getProcessAPI().startProcess(processDefinition.getId());

        waitForUserTask(p1, "step1");
        waitForUserTask(p2, "step1");
        waitForUserTask(p3, "step1");
        waitForUserTask(p4, "step1");
        waitForUserTask(p5, "step1");

        final SearchOptionsBuilder searchOptions = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 0, 5, ProcessInstanceSearchDescriptor.ID,
                Order.ASC);
        final SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(4, result.getCount());
        assertEquals(4, result.getResult().size());

        disableAndDeleteProcess(processDefinition);
        deleteUsers(john, jack, paul, pierre);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Managed By", "Process instance", "Open", "Involving User",
            "User Started Process" }, jira = "ENGINE-715")
    @Test
    public void searchOpenProcessInstancesInvolvingUsersManagedByWithUserStartedProcess() throws Exception {
        // create user
        final User paul = createUser("paul", "bpm");
        final User jack = createUser("jack", paul.getId());
        final User john = createUser("john", paul.getId());
        logoutOnTenant();
        loginOnDefaultTenantWith("john", "bpm");

        // create process
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1"), Arrays.asList(true));

        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, jack);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        final long step1Id = waitForUserTask("step1");

        final SearchOptionsBuilder searchOptions = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 0, 5, ProcessInstanceSearchDescriptor.ID,
                Order.ASC);
        // the process is started by jack and jack has not performed tasks: In "workedOn"
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(1, result.getCount());
        assignAndExecuteStep(step1Id, jack);

        // process finished: no more in worked on
        waitForProcessToFinish(processInstance);
        result = getProcessAPI().searchOpenProcessInstancesInvolvingUsersManagedBy(paul.getId(), searchOptions.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        disableAndDeleteProcess(processDefinition);
        deleteUsers(john, jack, paul);
    }

    @Test
    public void searchOpenProcessInstancesStartedBy() throws Exception {
        // create process
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process",
                "1.0", Arrays.asList("step1", "step2"), Arrays.asList(true, true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance instance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance4 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance5 = getProcessAPI().startProcess(processDefinition.getId());

        waitForUserTask(instance1, "step1");
        waitForUserTask(instance2, "step1");
        waitForUserTask(instance3, "step1");
        waitForUserTask(instance4, "step1");
        waitForUserTask(instance5, "step1");

        // test started by correct user
        final SearchOptionsBuilder searchOptions1 = BuildTestUtil.buildSearchOptions(processDefinition.getId(), 0, 10, ProcessInstanceSearchDescriptor.ID,
                Order.ASC);
        searchOptions1.filter(ProcessInstanceSearchDescriptor.STARTED_BY, user.getId());
        SearchResult<ProcessInstance> result = getProcessAPI().searchOpenProcessInstances(searchOptions1.done());
        assertNotNull(result);
        assertEquals(5, result.getCount());
        final List<ProcessInstance> processInstanceList1 = result.getResult();
        assertNotNull(processInstanceList1);
        assertEquals(5, processInstanceList1.size());
        assertEquals(instance1.getId(), processInstanceList1.get(0).getId());
        assertEquals(instance2.getId(), processInstanceList1.get(1).getId());
        assertEquals(instance3.getId(), processInstanceList1.get(2).getId());
        assertEquals(instance4.getId(), processInstanceList1.get(3).getId());
        assertEquals(instance5.getId(), processInstanceList1.get(4).getId());

        // test started by not existed user
        searchOptions1.filter(ProcessInstanceSearchDescriptor.STARTED_BY, user.getId() + 1500);
        result = getProcessAPI().searchOpenProcessInstances(searchOptions1.done());
        assertNotNull(result);
        assertEquals(0, result.getCount());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void searchArchivedProcessInstancesInvolvingUser() throws Exception {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1"), Arrays.asList(false));
        final ProcessDefinition processDefinition = deployAndEnableProcess(designProcessDefinition);

        final ProcessInstance instance1 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance2 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance3 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance4 = getProcessAPI().startProcess(processDefinition.getId());
        final ProcessInstance instance5 = getProcessAPI().startProcess(processDefinition.getId());
        waitForProcessToFinish(instance1);
        waitForProcessToFinish(instance2);
        waitForProcessToFinish(instance3);
        waitForProcessToFinish(instance4);
        waitForProcessToFinish(instance5);

        // test started by correct user
        final SearchOptionsBuilder opts = new SearchOptionsBuilder(0, 10);
        opts.filter(ArchivedProcessInstancesSearchDescriptor.PROCESS_DEFINITION_ID, processDefinition.getId());
        opts.sort(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, Order.ASC);
        final SearchResult<ArchivedProcessInstance> result = getProcessAPI().searchArchivedProcessInstancesInvolvingUser(user.getId(), opts.done());
        assertEquals(5, result.getCount());
        final List<ArchivedProcessInstance> processInstances = result.getResult();
        assertNotNull(processInstances);
        assertEquals(5, processInstances.size());
        assertEquals(instance1.getId(), processInstances.get(0).getSourceObjectId());
        assertEquals(instance2.getId(), processInstances.get(1).getSourceObjectId());
        assertEquals(instance3.getId(), processInstances.get(2).getSourceObjectId());
        assertEquals(instance4.getId(), processInstances.get(3).getSourceObjectId());
        assertEquals(instance5.getId(), processInstances.get(4).getSourceObjectId());

        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class, ArchivedProcessInstancesSearchDescriptor.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Archived",
            "Process Instances" }, jira = "ENGINE-998")
    @Test
    public void searchArchivedProcessInstances() throws Exception {
        final User user1 = createUser("john1", "bpm");
        final User user2 = createUser("john2", "bpm");
        final User user3 = createUser("john3", "bpm");
        final User user4 = createUser("john4", "bpm");

        final DesignProcessDefinition designProcessDefinition1 = createProcessDefinition("3", false).done();
        final ProcessDefinition processDefinition1 = deployAndEnableProcess(designProcessDefinition1);
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefinition1.getId());
        waitForProcessToFinish(processInstance1);
        logoutOnTenant();

        loginOnDefaultTenantWith("john1", "bpm");
        final DesignProcessDefinition designProcessDefinition2 = createProcessDefinition("2", false).done();
        final ProcessDefinition processDefinition2 = deployAndEnableProcess(designProcessDefinition2);
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefinition2.getId());
        waitForProcessToFinish(processInstance2);
        logoutOnTenant();

        loginOnDefaultTenantWith("john3", "bpm");
        final DesignProcessDefinition designProcessDefinition3 = createProcessDefinition("5", false).done();
        final ProcessDefinition processDefinition3 = deployAndEnableProcess(designProcessDefinition3);
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefinition3.getId());
        waitForProcessToFinish(processInstance3);
        logoutOnTenant();

        loginOnDefaultTenantWith("john2", "bpm");
        final DesignProcessDefinition designProcessDefinition4 = createProcessDefinition("4", false).done();
        final ProcessDefinition processDefinition4 = deployAndEnableProcess(designProcessDefinition4);
        final ProcessInstance processInstance4 = getProcessAPI().startProcess(processDefinition4.getId());
        waitForProcessToFinish(processInstance4);
        logoutOnTenant();

        loginOnDefaultTenantWith("john4", "bpm");
        final DesignProcessDefinition designProcessDefinition5 = createProcessDefinition("1", false).done();
        final ProcessDefinition processDefinition5 = deployAndEnableProcess(designProcessDefinition5);
        final ProcessInstance processInstance5 = getProcessAPI().startProcess(processDefinition5.getId());
        waitForProcessToFinish(processInstance5);

        // Order by ARCHIVE_DATE
        SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.ARCHIVE_DATE, Order.ASC);
        final SearchResult<ArchivedProcessInstance> result = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done());
        assertEquals(5, result.getCount());
        List<ArchivedProcessInstance> archivedProcessInstances = result.getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by END_DATE
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.END_DATE, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by ID
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.ID, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by LAST_UPDATE
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.LAST_UPDATE, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by NAME
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.NAME, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by SOURCE_OBJECT_ID
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by START_DATE
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.START_DATE, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by STARTED_BY
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.STARTED_BY, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(processInstance1.getId(), archivedProcessInstances.get(0).getSourceObjectId());
        assertEquals(processInstance2.getId(), archivedProcessInstances.get(1).getSourceObjectId());
        assertEquals(processInstance4.getId(), archivedProcessInstances.get(2).getSourceObjectId());
        assertEquals(processInstance3.getId(), archivedProcessInstances.get(3).getSourceObjectId());
        assertEquals(processInstance5.getId(), archivedProcessInstances.get(4).getSourceObjectId());

        // Order by STATE_ID
        searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.STATE_ID, Order.ASC);
        archivedProcessInstances = getProcessAPI().searchArchivedProcessInstances(searchOptionsBuilder.done()).getResult();
        assertNotNull(archivedProcessInstances);
        assertEquals(5, archivedProcessInstances.size());
        assertEquals(6, archivedProcessInstances.get(0).getStateId());
        assertEquals(6, archivedProcessInstances.get(1).getStateId());
        assertEquals(6, archivedProcessInstances.get(2).getStateId());
        assertEquals(6, archivedProcessInstances.get(3).getStateId());
        assertEquals(6, archivedProcessInstances.get(4).getStateId());

        disableAndDeleteProcess(processDefinition1, processDefinition2, processDefinition3, processDefinition4, processDefinition5);
        deleteUsers(user1, user2, user3, user4);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "API, Search" }, story = "Search process instance should return 1 when there's a subprocess and parent process active", jira = "ENGINE-964")
    @Test
    public void twoPoolsWithOneWithACallActivityCaseTest() throws Exception {
        final ProcessDefinitionBuilder process2DefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("process2", "1.0");
        process2DefinitionBuilder.addActor(ACTOR_NAME);
        process2DefinitionBuilder.addUserTask("User task", ACTOR_NAME);
        final DesignProcessDefinition designProcess2Definition = process2DefinitionBuilder.done();
        final ProcessDefinition process2Definition = deployAndEnableProcessWithActor(designProcess2Definition, ACTOR_NAME, user);

        final ProcessDefinitionBuilder process1DefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("process1", "1.0");
        process1DefinitionBuilder.addActor(ACTOR_NAME);

        final Expression process2Name = new ExpressionBuilder().createConstantStringExpression("process2");
        final Expression process2Version = new ExpressionBuilder().createConstantStringExpression("1.0");
        process1DefinitionBuilder.addCallActivity("call process2", process2Name, process2Version);
        final DesignProcessDefinition designProcess1Definition = process1DefinitionBuilder.done();
        final ProcessDefinition process1Definition = deployAndEnableProcessWithActor(designProcess1Definition, ACTOR_NAME, user);

        final ProcessInstance instance1 = getProcessAPI().startProcess(process1Definition.getId());
        waitForUserTask(instance1, "User task");

        final SearchOptions opts = new SearchOptionsBuilder(0, 10).done();
        final SearchResult<ProcessInstance> processInstanceSearchResult = getProcessAPI().searchOpenProcessInstances(opts);
        assertThat(processInstanceSearchResult.getCount(), is(1L));

        disableAndDeleteProcess(process1Definition);
        disableAndDeleteProcess(process2Definition);
    }

    private ProcessDefinitionBuilder createProcessDefinition(final String processName, final boolean withUserTask) {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(processName, "17.3");
        if (withUserTask) {
            designProcessDefinition.addActor(ACTOR_NAME);
            designProcessDefinition.addUserTask("step1", ACTOR_NAME);
        } else {
            designProcessDefinition.addAutomaticTask("step1");
        }

        return designProcessDefinition;
    }

    @Cover(classes = { SearchOptionsBuilder.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "SearchArchivedProcessInstances",
            "Apostrophe" }, jira = "ENGINE-366, ENGINE-589")
    @Test
    public void searchArchivedProcessInstancesWithApostrophe() throws Exception {
        // Create process
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("Na'me", PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME);
        final DesignProcessDefinition designProcessDefinition = processBuilder.addUserTask("userTask1", ACTOR_NAME).getProcess();
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask("userTask1");

        final List<ActivityInstance> activityInstances = getProcessAPI().getActivities(processInstance.getId(), 0, 10);
        assertEquals(1, activityInstances.size());
        for (final ActivityInstance activityInstance : activityInstances) {
            final long activityInstanceId = activityInstance.getId();
            getProcessAPI().setActivityStateById(activityInstanceId, 12);
        }
        waitForProcessToFinish(processInstance);
        // Search Archived process
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10).searchTerm("Na'");
        final SearchResult<ArchivedProcessInstance> searchProcessInstanceResult = getProcessAPI().searchArchivedProcessInstances(builder.done());
        assertEquals(1, searchProcessInstanceResult.getCount());
        final List<ArchivedProcessInstance> archivedProcessInstances = searchProcessInstanceResult.getResult();
        final ArchivedProcessInstance archivedProcessInstance = archivedProcessInstances.get(0);
        assertEquals(processInstance.getId(), archivedProcessInstance.getSourceObjectId());
        assertEquals("Na'me", archivedProcessInstance.getName());

        // Clean
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Process instance", "terminal state" }, story = "Search archived process instances retrieve only instances in terminal state", jira = "ENGINE-1084")
    @Test
    public void searchArchivedProcessInstancesRetrieveOnlyTerminalStates() throws Exception {
        // create a process instance in state completed and a process instance in the state canceled
        final ProcessDefinition simpleProcess = createArchivedProcessInstanceInStateCompletedAndCanceled();

        // create process instance in state aborted
        final ProcessDefinition procWithEventSubProcess = createArchivedProcInstInAbortedState();

        // search
        final SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 10);
        searchBuilder.sort(ArchivedProcessInstancesSearchDescriptor.STATE_ID, Order.ASC);

        final SearchResult<ArchivedProcessInstance> searchResult = getProcessAPI().searchArchivedProcessInstances(searchBuilder.done());
        // check the result: 3 instances are expected, one in the state completed, one in the state canceled, and one in the state aborted
        assertEquals(3, searchResult.getCount());
        final List<ArchivedProcessInstance> processes = searchResult.getResult();
        // canceled
        assertEquals(simpleProcess.getName(), processes.get(0).getName());
        assertEquals(TestStates.CANCELLED.getStateName(), processes.get(0).getState());
        // aborted
        assertEquals(procWithEventSubProcess.getName(), processes.get(1).getName());
        assertEquals(TestStates.ABORTED.getStateName(), processes.get(1).getState());
        // completed
        assertEquals(simpleProcess.getName(), processes.get(2).getName());
        assertEquals(TestStates.NORMAL_FINAL.getStateName(), processes.get(2).getState());

        // clean up
        disableAndDeleteProcess(simpleProcess.getId());
        disableAndDeleteProcess(procWithEventSubProcess.getId());
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "User", "Enabled", "Disabled", "Who can start process" }, story = "Search enabled/disabled users who can start process", jira = "ENGINE-821")
    @Test
    public void searchUsersWhoCanStartProcess() throws Exception {
        final User jack = createUser("jack", PASSWORD);

        // Disabled jack
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setEnabled(false);
        getIdentityAPI().updateUser(jack.getId(), updateDescriptor);

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME,
                PROCESS_VERSION, Arrays.asList("step1"), Arrays.asList(true), ACTOR_NAME, true);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, Arrays.asList(ACTOR_NAME, ACTOR_NAME),
                Arrays.asList(user, jack));

        // Search
        final SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 10);
        searchBuilder.sort(UserSearchDescriptor.USER_NAME, Order.ASC);
        final SearchResult<User> searchResult = getProcessAPI().searchUsersWhoCanStartProcessDefinition(processDefinition.getId(), searchBuilder.done());
        assertEquals(1, searchResult.getCount());
        final List<User> users = searchResult.getResult();
        assertEquals(user.getId(), users.get(0).getId());

        // clean up
        deleteUsers(jack);
        disableAndDeleteProcess(processDefinition.getId());
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "User", "Enabled", "Group", "Who can start process" }, story = "Search enabled/disabled users who can start process", jira = "ENGINE-821")
    @Test
    public void searchUsersWhoCanStartProcessInAGroup() throws Exception {
        final Group group = createGroup(GROUP_NAME);
        final Role role = createRole(ROLE_NAME);
        final UserMembership userMembership = getIdentityAPI().addUserMembership(user.getId(), group.getId(), role.getId());

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME,
                PROCESS_VERSION, Arrays.asList("step1"), Arrays.asList(true), ACTOR_NAME, true);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, group);

        // Search
        final SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 10);
        searchBuilder.sort(UserSearchDescriptor.USER_NAME, Order.ASC);
        final SearchResult<User> searchResult = getProcessAPI().searchUsersWhoCanStartProcessDefinition(processDefinition.getId(), searchBuilder.done());
        assertEquals(1, searchResult.getCount());
        final List<User> users = searchResult.getResult();
        assertEquals(user.getId(), users.get(0).getId());

        // clean up
        disableAndDeleteProcess(processDefinition.getId());
        deleteUserMemberships(userMembership);
        deleteGroups(group);
        deleteRoles(role);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "User", "Enabled", "Group", "Who can start process" }, story = "Search enabled/disabled users who can start process", jira = "ENGINE-821")
    @Test
    public void searchUsersWhoCanStartProcessInARole() throws Exception {
        final Group group = createGroup(GROUP_NAME);
        final Role role = createRole(ROLE_NAME);
        final UserMembership userMembership = getIdentityAPI().addUserMembership(user.getId(), group.getId(), role.getId());

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME,
                PROCESS_VERSION, Arrays.asList("step1"), Arrays.asList(true), ACTOR_NAME, true);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, role);

        // Search
        final SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 10);
        searchBuilder.sort(UserSearchDescriptor.USER_NAME, Order.ASC);
        final SearchResult<User> searchResult = getProcessAPI().searchUsersWhoCanStartProcessDefinition(processDefinition.getId(), searchBuilder.done());
        assertEquals(1, searchResult.getCount());
        final List<User> users = searchResult.getResult();
        assertEquals(user.getId(), users.get(0).getId());

        // clean up
        disableAndDeleteProcess(processDefinition.getId());
        deleteUserMemberships(userMembership);
        deleteGroups(group);
        deleteRoles(role);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "User", "Enabled", "Group", "Who can start process" }, story = "Search enabled/disabled users who can start process", jira = "ENGINE-821")
    @Test
    public void searchUsersWhoCanStartProcessInARoleAndAGroup() throws Exception {
        final Group group = createGroup(GROUP_NAME);
        final Role role = createRole(ROLE_NAME);
        final UserMembership userMembership = createUserMembership(USERNAME, ROLE_NAME, GROUP_NAME);

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(PROCESS_NAME,
                PROCESS_VERSION, Arrays.asList("step1"), Arrays.asList(true), ACTOR_NAME, true);
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, role, group);

        // Search
        final SearchOptionsBuilder searchBuilder = new SearchOptionsBuilder(0, 10);
        searchBuilder.sort(UserSearchDescriptor.USER_NAME, Order.ASC);
        final SearchResult<User> searchResult = getProcessAPI().searchUsersWhoCanStartProcessDefinition(processDefinition.getId(), searchBuilder.done());
        assertEquals(1, searchResult.getCount());
        final List<User> users = searchResult.getResult();
        assertEquals(user.getId(), users.get(0).getId());

        // clean up
        disableAndDeleteProcess(processDefinition.getId());
        deleteUserMemberships(userMembership);
        deleteGroups(group);
        deleteRoles(role);
    }

    private ProcessDefinition createArchivedProcInstInAbortedState() throws Exception {
        final String userTaskName = "step1";
        final String subProcTaskName = "subStep";
        final String signalName = "go";
        // deploy and start a process with event subprocess
        final ProcessDefinition procWithEventSubProcess = deployProcessWithEventSubProcess(userTaskName, subProcTaskName, signalName);
        final ProcessInstance procInstWithEventSubProc = getProcessAPI().startProcess(procWithEventSubProcess.getId());

        // wait for first step of parent process and send a signal that will launch the event sub-process
        waitForUserTask(procInstWithEventSubProc, userTaskName);
        getProcessAPI().sendSignal(signalName);

        // execute user task and wait the parent process to finish (state aborted)
        waitForUserTaskAndExecuteIt(procInstWithEventSubProc, subProcTaskName, user);
        waitForProcessToBeInState(procInstWithEventSubProc, ProcessInstanceState.ABORTED);
        return procWithEventSubProcess;
    }

    private ProcessDefinition createArchivedProcessInstanceInStateCompletedAndCanceled() throws Exception {
        final String userTaskName = "step1";
        // deploy and start simple process
        final ProcessDefinition simpleProcess = deployProcessWithHumanTask(userTaskName);
        final ProcessInstance processInstanceToComplete = getProcessAPI().startProcess(simpleProcess.getId());

        // execute user task and wait process to finish: the process will be in the state completed
        waitForUserTaskAndExecuteIt(processInstanceToComplete, userTaskName, user);
        waitForProcessToFinish(processInstanceToComplete);

        // start another instance and cancel it: the process will be in the state canceled
        final ProcessInstance processInstanceToCancel = getProcessAPI().startProcess(simpleProcess.getId());
        waitForUserTask(processInstanceToCancel, userTaskName);
        getProcessAPI().cancelProcessInstance(processInstanceToCancel.getId());
        waitForProcessToBeInState(processInstanceToCancel, ProcessInstanceState.CANCELLED);
        return simpleProcess;
    }

    private ProcessDefinition deployProcessWithHumanTask(final String userTaskName) throws BonitaException, InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("myProc", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask(userTaskName, ACTOR_NAME);
        return deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

    private ProcessDefinition deployProcessWithEventSubProcess(final String parentUserTaskName, final String suProcTaskName, final String signalName)
            throws BonitaException, InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder().createNewInstance("eventProc", "1.0");
        builder.addActor(ACTOR_NAME);
        builder.addUserTask(parentUserTaskName, ACTOR_NAME);
        final SubProcessDefinitionBuilder subProcessBuilder = builder.addSubProcess("sub", true).getSubProcessBuilder();
        subProcessBuilder.addStartEvent("start").addSignalEventTrigger(signalName);
        subProcessBuilder.addUserTask(suProcTaskName, ACTOR_NAME);
        subProcessBuilder.addTransition("start", suProcTaskName);
        return deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
    }

    @Test
    public void search_process_instances_in_all_states_retrieves_all_process_states() throws Exception {
        // deploy and start a process
        final ProcessDefinition simpleProcess = deployProcessWithHumanTask("step1");
        final ProcessInstance procInst = getProcessAPI().startProcess(simpleProcess.getId());

        // execute it until the end
        waitForUserTaskAndExecuteIt(procInst, "step1", user);
        waitForProcessToFinish(procInst);

        // search archived process instances: all states must be retrieved
        final SearchResult<ArchivedProcessInstance> searchResult = searchAchivedProcessInstancesInAllStates(procInst.getId());

        assertEquals(3, searchResult.getCount());
        final List<ArchivedProcessInstance> archivedProcesses = searchResult.getResult();
        assertEquals(ProcessInstanceState.INITIALIZING.getId(), archivedProcesses.get(0).getStateId());
        assertEquals(ProcessInstanceState.STARTED.getId(), archivedProcesses.get(1).getStateId());
        assertEquals(ProcessInstanceState.COMPLETED.getId(), archivedProcesses.get(2).getStateId());

        deleteProcessInstanceAndArchived(simpleProcess);
        disableAndDeleteProcess(simpleProcess);
    }

    @Test
    public void searchProcessInstancesWithAssigneeShouldReturnProcessWithAtLeastOneTaskAssignedToUser() throws Exception {
        final User otherUser = getIdentityAPI().createUser("newUser", "pepito");

        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1"), Arrays.asList(true));
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, user);

        final long processDefId = processDefinition.getId();
        final ProcessInstance processInstance1 = getProcessAPI().startProcess(processDefId);
        final ProcessInstance processInstance2 = getProcessAPI().startProcess(processDefId);
        final ProcessInstance processInstance3 = getProcessAPI().startProcess(processDefId);
        waitForUserTaskAndAssigneIt(processInstance1, "step1", user);
        waitForUserTaskAndAssigneIt(processInstance2, "step1", user);
        waitForUserTaskAndAssigneIt(processInstance3, "step1", otherUser);

        final SearchOptions searchOptions = new SearchOptionsBuilder(0, 10)
                .filter(ProcessInstanceSearchDescriptor.PROCESS_DEFINITION_ID, processDefId)
                .filter(ProcessInstanceSearchDescriptor.ASSIGNEE_ID, user.getId())
                .sort(ProcessInstanceSearchDescriptor.ID, Order.ASC).done();
        final SearchResult<ProcessInstance> processInstancesSearched = getProcessAPI().searchProcessInstances(searchOptions);
        assertEquals(2, processInstancesSearched.getCount());

        final List<ProcessInstance> processInstances = processInstancesSearched.getResult();
        assertEquals(processInstance1, processInstances.get(0));
        assertEquals(processInstance2, processInstances.get(1));

        disableAndDeleteProcess(processDefinition);
        deleteUser(otherUser);
    }

    private SearchResult<ArchivedProcessInstance> searchAchivedProcessInstancesInAllStates(final long processInstanceId) throws SearchException {
        final SearchOptionsBuilder optionsBuilder = new SearchOptionsBuilder(0, 10);
        optionsBuilder.filter(ArchivedProcessInstancesSearchDescriptor.SOURCE_OBJECT_ID, processInstanceId);
        optionsBuilder.filter(ArchivedProcessInstancesSearchDescriptor.CALLER_ID, -1L);
        optionsBuilder.sort(ArchivedProcessInstancesSearchDescriptor.ID, Order.ASC);
        return getProcessAPI().searchArchivedProcessInstancesInAllStates(optionsBuilder.done());
    }

}
