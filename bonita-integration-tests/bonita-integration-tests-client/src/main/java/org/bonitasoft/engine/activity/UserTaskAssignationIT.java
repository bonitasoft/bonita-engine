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
package org.bonitasoft.engine.activity;

import net.jodah.concurrentunit.Waiter;
import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.flownode.FlowNodeExecutionException;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.check.CheckNbAssignedTaskOf;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


public class UserTaskAssignationIT extends TestWithTechnicalUser {


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static final String JOHN = "john";

    private static final String JACK = "jack";

    private User john;

    private User jack;

    private ProcessDefinition processDefinition;

    private ProcessInstance processInstance;

    private HumanTaskInstance step2;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        john = createUser(JOHN, "bpm");
        jack = createUser(JACK, "bpm");
        loginOnDefaultTenantWith(JOHN, "bpm");

        processDefinition = deployAndEnableSimpleProcess();
        processInstance = getProcessAPI().startProcess(processDefinition.getId());
        step2 = waitForUserTaskAndGetIt(processInstance, "step2");
    }

    @Override
    @After
    public void after() throws Exception {
        disableAndDeleteProcess(processDefinition);

        deleteUser(JOHN);
        deleteUser(JACK);
        VariableStorage.clearAll();
        super.after();
    }

    @Test
    public void getAssignedHumanTasksWithStartedState() throws Exception {
        assignAndExecuteStep(step2, john);

        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);

        // Task is in STARTED state so should not be retrieved:
        assertEquals(0, toDoTasks.size());
        waitForProcessToFinish(processInstance);
    }

    @Test(expected = FlowNodeExecutionException.class)
    public void cannotExecuteAnUnassignedTask() throws Exception {
        // execute activity without assign it before, an exception is expected
        getProcessAPI().executeFlowNode(step2.getId());
    }

    @Test
    public void assignUserTask() throws Exception {
        getProcessAPI().assignUserTask(step2.getId(), john.getId());

        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(john.getId(), toDoTasks.get(0).getAssigneeId());
    }

    @Test
    public void canAssignTask2Times() throws Exception {
        getProcessAPI().assignUserTask(step2.getId(), john.getId());

        // No exception expected
        getProcessAPI().assignUserTask(step2.getId(), john.getId());
    }

    @Test
    public void assignUserTaskSeveralTimes() throws Exception {
        // after assign
        getProcessAPI().assignUserTask(step2.getId(), john.getId());
        List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(john.getId(), toDoTasks.get(0).getAssigneeId());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
        // after release
        getProcessAPI().releaseUserTask(toDoTasks.get(0).getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, toDoTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        assertEquals(0, pendingTasks.get(0).getAssigneeId());
        // re assign
        getProcessAPI().assignUserTask(step2.getId(), jack.getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(jack.getId(), toDoTasks.get(0).getAssigneeId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());

        getProcessAPI().releaseUserTask(toDoTasks.get(0).getId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        assertEquals(0, pendingTasks.get(0).getAssigneeId());
        // re assign
        getProcessAPI().assignUserTask(step2.getId(), john.getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(john.getId(), toDoTasks.get(0).getAssigneeId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
    }

    @Test
    public void releaseUserTask() throws Exception {
        // after assign
        getProcessAPI().assignUserTask(step2.getId(), john.getId());
        List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(john.getId(), toDoTasks.get(0).getAssigneeId());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
        // after release
        getProcessAPI().releaseUserTask(toDoTasks.get(0).getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, toDoTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
    }

    @Test
    public void assignUserTaskSeveralTimesByChangingLogin() throws Exception {
        // login as jack
        logoutOnTenant();
        loginOnDefaultTenantWith(JACK, "bpm");

        // assign
        getProcessAPI().assignUserTask(step2.getId(), jack.getId());
        List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(jack.getId(), toDoTasks.get(0).getAssigneeId());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
        // release
        getProcessAPI().releaseUserTask(toDoTasks.get(0).getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(0, toDoTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        // re assign
        getProcessAPI().assignUserTask(pendingTasks.get(0).getId(), jack.getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(jack.getId(), toDoTasks.get(0).getAssigneeId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
    }

    @Test
    public void assignedDateUpdate() throws Exception {
        final Long taskId = step2.getId();

        // First assign
        getProcessAPI().assignUserTask(taskId, john.getId());
        assertTrue("Fail to claim task", new CheckNbAssignedTaskOf(getProcessAPI(), 30, 2000,
                false, 1, john).waitUntil());
        final Date firstClaimedDate = getProcessAPI().getHumanTaskInstance(taskId).getClaimedDate();
        assertNotNull("Claimed date not set during first assignment", firstClaimedDate);

        // Release
        getProcessAPI().releaseUserTask(taskId);
        assertTrue("Fail to release task", new CheckNbPendingTaskOf(getProcessAPI(), 30, 2000,
                false, 1, john).waitUntil());
        assertNull("Claimed date not unset during release", getProcessAPI().getHumanTaskInstance(taskId).getClaimedDate());

        // Second assign
        getProcessAPI().assignUserTask(taskId, john.getId());
        assertTrue("Fail to claim task for the second time", new CheckNbAssignedTaskOf(getProcessAPI(), 30,
                2000, false, 1, john).waitUntil());
        final HumanTaskInstance task = getProcessAPI().getHumanTaskInstance(taskId);
        assertNotNull("Claimed date not set during first assignment", task.getClaimedDate());
        assertNotEquals("Claimed date not updated", firstClaimedDate,task.getClaimedDate());

    }

    @Test
    @Ignore("lastUpdateDate should be removed (not used)")
    public void lastUpdateDateUpdate() throws Exception {
        final Long taskId = step2.getId();
        Date previousUpdateDate = step2.getLastUpdateDate();

        // First assign
        getProcessAPI().assignUserTask(taskId, john.getId());
        if (!new CheckNbAssignedTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil()) {
            fail("Fail to claim task");
        }
        HumanTaskInstance task = getProcessAPI().getHumanTaskInstance(taskId);
        assertNotNull("Last update date not set during first assignment", task.getLastUpdateDate());
        assertFalse("Last update date not updated during first assignment", task.getLastUpdateDate().equals(previousUpdateDate));
        previousUpdateDate = task.getLastUpdateDate();

        // Release
        getProcessAPI().releaseUserTask(taskId);
        task = waitForUserTaskAndGetIt(processInstance, "step2");
        assertFalse("Last update date not updated during release", previousUpdateDate.equals(task.getLastUpdateDate()));
        previousUpdateDate = task.getLastUpdateDate();

        // Second assign
        getProcessAPI().assignUserTask(taskId, john.getId());
        if (!new CheckNbAssignedTaskOf(getProcessAPI(), 50, 5000, false, 1, john).waitUntil()) {
            fail("Fail to claim task for the second time");
        }
        task = getProcessAPI().getHumanTaskInstance(taskId);
        assertFalse("Last update date not updated during second assignment", previousUpdateDate.equals(task.getLastUpdateDate()));
    }

    private ProcessDefinition deployAndEnableSimpleProcess()
            throws BonitaException, InvalidProcessDefinitionException {
        final DesignProcessDefinition designProcessDefinition = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps(
                Arrays.asList("step1", "step2"), Arrays.asList(false, true));
        return deployAndEnableProcessWithActor(designProcessDefinition, ACTOR_NAME, john);
    }
    @Test
    public void assignUserTaskIfNotAssigned() throws Exception {
        getProcessAPI().assignUserTaskIfNotAssigned(step2.getId(), john.getId());

        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(),
                0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(john.getId(), toDoTasks.get(0).getAssigneeId());
    }

    @Test
    public void shouldAssignAndUnassign() throws Exception {
        getProcessAPI().assignUserTaskIfNotAssigned(step2.getId(), john.getId());

        getProcessAPI().assignUserTaskIfNotAssigned(step2.getId(), 0L);

    }

    @Test
    public void shouldAssignAndAssignAgainToSameUser() throws Exception {
        getProcessAPI().assignUserTaskIfNotAssigned(step2.getId(), john.getId());

        getProcessAPI().assignUserTaskIfNotAssigned(step2.getId(), john.getId());

    }

    @Test
    public void canNotAssignTask2TimestoDifferentUserIfAssigned() throws Exception {
        getProcessAPI().assignUserTaskIfNotAssigned(step2.getId(), john.getId());
        thrown.expect(UpdateException.class);
        getProcessAPI().assignUserTaskIfNotAssigned(step2.getId(), jack.getId());

    }

    @Test
    public void shouldAssignTaskSeveralTimesToDifferentUsers() throws Exception {

        getProcessAPI().assignUserTaskIfNotAssigned(step2.getId(), john.getId());
        List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(john.getId(), toDoTasks.get(0).getAssigneeId());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());

        getProcessAPI().releaseUserTask(toDoTasks.get(0).getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, toDoTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        assertEquals(0, pendingTasks.get(0).getAssigneeId());

        getProcessAPI().assignUserTaskIfNotAssigned(step2.getId(), jack.getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(jack.getId(), toDoTasks.get(0).getAssigneeId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());

        getProcessAPI().releaseUserTask(toDoTasks.get(0).getId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());
        assertEquals(0, pendingTasks.get(0).getAssigneeId());

        getProcessAPI().assignUserTaskIfNotAssigned(step2.getId(), john.getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(john.getId(), toDoTasks.get(0).getAssigneeId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
    }

    @Test
    public void shouldAssignUserTaskSeveralTimesByChangingLogin() throws Exception {
        logoutOnTenant();
        loginOnDefaultTenantWith(JACK, "bpm");

        getProcessAPI().assignUserTaskIfNotAssigned(step2.getId(), jack.getId());
        List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(jack.getId(), toDoTasks.get(0).getAssigneeId());
        List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());

        getProcessAPI().releaseUserTask(toDoTasks.get(0).getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(0, toDoTasks.size());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, pendingTasks.size());

        getProcessAPI().assignUserTaskIfNotAssigned(pendingTasks.get(0).getId(), jack.getId());
        toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(jack.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        assertEquals(jack.getId(), toDoTasks.get(0).getAssigneeId());
        pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, pendingTasks.size());
    }

    @Test
    public void assignedDateUpdateIfNotAssigned() throws Exception {
        final Long taskId = step2.getId();

        getProcessAPI().assignUserTaskIfNotAssigned(taskId, john.getId());
        assertTrue("Fail to claim task", new CheckNbAssignedTaskOf(getProcessAPI(), 30, 2000,
                false, 1, john).waitUntil());
        final Date firstClaimedDate = getProcessAPI().getHumanTaskInstance(taskId).getClaimedDate();
        assertNotNull("Claimed date not set during first assignment", firstClaimedDate);

        getProcessAPI().releaseUserTask(taskId);
        assertTrue("Fail to release task", new CheckNbPendingTaskOf(getProcessAPI(), 30, 2000,
                false, 1, john).waitUntil());
        assertNull("Claimed date not unset during release", getProcessAPI().getHumanTaskInstance(taskId).getClaimedDate());

        getProcessAPI().assignUserTaskIfNotAssigned(taskId, john.getId());
        assertTrue("Fail to claim task for the second time", new CheckNbAssignedTaskOf(getProcessAPI(), 30, 2000, false, 1, john).waitUntil());
        final HumanTaskInstance task = getProcessAPI().getHumanTaskInstance(taskId);
        assertNotNull("Claimed date not set during first assignment", task.getClaimedDate());
        assertNotEquals("Claimed date not updated", firstClaimedDate, task.getClaimedDate());
    }

    @Test
    public void assignUserTaskSeveralThreads() throws Throwable {
        final Long taskId = step2.getId();
        loginOnDefaultTenantWith(JACK, "bpm");
        ProcessAPI pAPI = getProcessAPI();
        Waiter waiter = new Waiter();
        Boolean[] exceptionThreads = new Boolean[]{false,false};
        Thread thread1 = new Thread () {
            public void run () {
                try {
                    pAPI.assignUserTaskIfNotAssigned(taskId, john.getId());
                }catch (Exception e){
                    exceptionThreads[0] = true;
                }finally{
                    waiter.resume();
                }
            }
        };
        Thread thread2 = new Thread () {
            public void run () {
                try {
                    pAPI.assignUserTaskIfNotAssigned(taskId, jack.getId());
                }catch (Exception e){
                    exceptionThreads[0] = true;
                }finally{
                    waiter.resume();
                }
            }
        };

        thread1.start();
        thread2.start();
        waiter.await(1, TimeUnit.SECONDS, 2);
        //Assert only one failed;
        assertNotEquals("Both assigments failed", exceptionThreads[0]||exceptionThreads[1] , false);
        assertNotEquals("Both assigments took place",exceptionThreads[0]&&exceptionThreads[1], true);


    }

    @Test
    public void assignUserTaskSeveralThreadsWithoutCheck() throws Throwable {
        final Long taskId = step2.getId();
        loginOnDefaultTenantWith(JACK, "bpm");
        ProcessAPI pAPI = getProcessAPI();
        Waiter waiter = new Waiter();
        Boolean[] exceptionThreads = new Boolean[]{false,false};
        Thread thread1 = new Thread () {
            public void run () {
                try {
                    pAPI.assignUserTask(taskId, john.getId());
                }catch (Exception e){
                    exceptionThreads[0] = true;
                }finally{
                    waiter.resume();
                }
            }
        };
        Thread thread2 = new Thread () {
            public void run () {
                try {
                    pAPI.assignUserTask(taskId, jack.getId());
                }catch (Exception e){
                    exceptionThreads[0] = true;
                }finally{
                    waiter.resume();
                }
            }
        };

        thread1.start();
        thread2.start();
        waiter.await(1, TimeUnit.SECONDS, 2);
        //Assert only one failed;
        assertEquals("Both assigments took place",exceptionThreads[0]||exceptionThreads[1], false);

    }
}
