package org.bonitasoft.engine.process.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ManualTasksTest extends CommonAPITest {

    private static final String JOHN = "john";

    private User john;

    @After
    public void afterTest() throws BonitaException {
        deleteUser(JOHN);
        VariableStorage.clearAll();
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
        john = createUser(JOHN, "bpm");
        logout();
        loginWith(JOHN, "bpm");
    }

    @Test
    public void executeProcessWithManualTask() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        designProcessDefinition.addManualTask("step2", delivery);
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition.done(), delivery, john);

        getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 2500, false, 1, john).waitUntil());
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask = pendingTasks.get(0);

        getProcessAPI().assignUserTask(pendingTask.getId(), john.getId());
        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        final HumanTaskInstance humanTaskInstance = toDoTasks.get(0);
        assertEquals(john.getId(), humanTaskInstance.getAssigneeId());

        getProcessAPI().executeFlowNode(humanTaskInstance.getId());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeProcessWithManualTaskAndUserTask() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(delivery).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        designProcessDefinition.addManualTask("step2", delivery);
        designProcessDefinition.addUserTask("step3", delivery);
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step1", "step3");

        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition.done(), delivery, john);

        getProcessAPI().startProcess(processDefinition.getId());

        assertTrue(new CheckNbPendingTaskOf(getProcessAPI(), 50, 2500, false, 2, john).waitUntil());
        final List<HumanTaskInstance> pendingTasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        final HumanTaskInstance pendingTask1 = pendingTasks.get(0);
        final HumanTaskInstance pendingTask2 = pendingTasks.get(1);

        getProcessAPI().assignUserTask(pendingTask1.getId(), john.getId());
        getProcessAPI().assignUserTask(pendingTask2.getId(), john.getId());
        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(2, toDoTasks.size());
        final HumanTaskInstance humanTaskInstance1 = toDoTasks.get(0);
        final HumanTaskInstance humanTaskInstance2 = toDoTasks.get(1);
        HumanTaskInstance step3;
        HumanTaskInstance step2;
        if (humanTaskInstance1.getName().equals("step3")) {
            step3 = humanTaskInstance1;
            step2 = humanTaskInstance2;
        } else {
            step3 = humanTaskInstance2;
            step2 = humanTaskInstance1;
        }
        assertEquals(john.getId(), humanTaskInstance1.getAssigneeId());
        assertEquals(john.getId(), humanTaskInstance2.getAssigneeId());
        assertTrue(step2 instanceof ManualTaskInstance);
        assertTrue(step3 instanceof UserTaskInstance);

        disableAndDeleteProcess(processDefinition);
    }

    // @Test(expected = UnhideableTaskException.class)
    // public void unableToHideManualTask() throws Exception {
    // final User user = createUser("login1", "password");
    // final ProcessDefinition processDefinition = deployProcessWithUserTask(user);
    // final ProcessInstance startProcess = getProcessAPI().startProcess(processDefinition.getId());
    // final ActivityInstance task = waitForUserTask("Request", startProcess);
    // final long taskId = task.getId();
    // login();
    // loginWith(user);
    // getProcessAPI().assignUserTask(taskId, user.getId());
    //
    // final ManualTaskInstance manualUserTask = getProcessAPI().addManualUserTask(taskId, "subtask", "MySubTask", user.getId(), "desk", new Date(),
    // TaskPriority.NORMAL);
    // try {
    // getProcessAPI().hideTasks(user.getId(), manualUserTask.getId());
    // } finally {
    // deleteUser(user);
    // disableAndDelete(processDefinition);
    // }
    // }

}
