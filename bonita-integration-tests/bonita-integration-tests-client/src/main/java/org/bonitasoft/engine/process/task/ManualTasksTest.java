package org.bonitasoft.engine.process.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ManualTasksTest extends CommonAPITest {

    private User john;

    @After
    public void afterTest() throws BonitaException {
        deleteUser(USERNAME);
        VariableStorage.clearAll();
        logoutOnTenant();
    }

    @Before
    public void beforeTest() throws BonitaException {
         loginOnDefaultTenantWithDefaultTechnicalUser();
        john = createUser(USERNAME, PASSWORD);
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
    }

    @Test
    public void executeProcessWithManualTask() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        designProcessDefinition.addManualTask("step2", ACTOR_NAME);
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndAssigneIt("step2", processInstance, john);
        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        final HumanTaskInstance humanTaskInstance = toDoTasks.get(0);
        assertEquals(john.getId(), humanTaskInstance.getAssigneeId());

        getProcessAPI().executeFlowNode(humanTaskInstance.getId());
        waitForProcessToFinish(processInstance.getId());
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void executeProcessWithManualTaskAndUserTask() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        designProcessDefinition.addManualTask("step2", ACTOR_NAME);
        designProcessDefinition.addUserTask("step3", ACTOR_NAME);
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step1", "step3");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, john);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndAssigneIt("step2", processInstance, john);
        waitForUserTaskAndAssigneIt("step3", processInstance, john);

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

}
