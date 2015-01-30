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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.ManualTaskInstance;
import org.bonitasoft.engine.bpm.flownode.UserTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.junit.After;
import org.junit.Test;

public class ManualTasksIT extends TestWithUser {

    @Override
    @After
    public void after() throws Exception {
        VariableStorage.clearAll();
        super.after();
    }

    @Test
    public void executeProcessWithManualTask() throws Exception {
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                "executeConnectorOnStartOfAnAutomaticActivity", "1.0");
        designProcessDefinition.addActor(ACTOR_NAME).addDescription("Delivery all day and night long");
        designProcessDefinition.addAutomaticTask("step1");
        designProcessDefinition.addManualTask("step2", ACTOR_NAME);
        designProcessDefinition.addTransition("step1", "step2");

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndAssigneIt(processInstance, "step2", user);
        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 10, null);
        assertEquals(1, toDoTasks.size());
        final HumanTaskInstance humanTaskInstance = toDoTasks.get(0);
        assertEquals(user.getId(), humanTaskInstance.getAssigneeId());

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

        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTaskAndAssigneIt(processInstance, "step2", user);
        waitForUserTaskAndAssigneIt(processInstance, "step3", user);

        final List<HumanTaskInstance> toDoTasks = getProcessAPI().getAssignedHumanTaskInstances(user.getId(), 0, 10, null);
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
        assertEquals(user.getId(), humanTaskInstance1.getAssigneeId());
        assertEquals(user.getId(), humanTaskInstance2.getAssigneeId());
        assertTrue(step2 instanceof ManualTaskInstance);
        assertTrue(step3 instanceof UserTaskInstance);

        disableAndDeleteProcess(processDefinition);
    }

}
