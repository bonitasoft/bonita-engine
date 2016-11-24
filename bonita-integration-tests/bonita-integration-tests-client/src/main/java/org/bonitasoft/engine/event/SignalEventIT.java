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
package org.bonitasoft.engine.event;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.test.TestStates;
import org.junit.Test;

public class SignalEventIT extends AbstractEventIT {

    @Test
    public void sendSignal() throws Exception {
        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder();

        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("SayGO", "1.0").addActor(ACTOR_NAME).addStartEvent("Start").addUserTask("step1", ACTOR_NAME).addEndEvent("End")
                .addSignalEventTrigger("GO").addTransition("Start", "step1").addTransition("step1", "End");
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(builder.done());
        final BusinessArchive endSignalArchive = archiveBuilder.done();

        builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("GetGO", "1.0").addActor(ACTOR_NAME).addStartEvent("StartOnSignal").addSignalEventTrigger("GO")
                .addUserTask("Task1", ACTOR_NAME).addTransition("StartOnSignal", "Task1");
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(builder.done());
        final BusinessArchive startSignalArchive = archiveBuilder.done();

        final ProcessDefinition processDefinitionWithStartSignal = deployAndEnableProcessWithActor(startSignalArchive, ACTOR_NAME, user);
        final ProcessDefinition processDefinitionWithEndSignal = deployAndEnableProcessWithActor(endSignalArchive, ACTOR_NAME, user);

        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        // Check that the process with trigger signal on start is not started, before send signal
        final ProcessInstance processInstanceWithEndSignal = getProcessAPI().startProcess(processDefinitionWithEndSignal.getId());
        waitForUserTask(processInstanceWithEndSignal, "step1");
        checkNbOfProcessInstances(1);

        List<HumanTaskInstance> taskInstances = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(1, taskInstances.size());
        assertEquals("step1", taskInstances.get(0).getName());

        // Send signal
        assignAndExecuteStep(taskInstances.get(0), user);
        waitForProcessToFinish(processInstanceWithEndSignal.getId());

        // Check that the process with trigger signal on start is started, after send signal
        waitForUserTask("Task1");
        taskInstances = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(1, taskInstances.size());
        assertEquals("Task1", taskInstances.get(0).getName());

        disableAndDeleteProcess(processDefinitionWithStartSignal, processDefinitionWithEndSignal);
    }

    @Test
    public void sendIntermediateCatchSignal() throws Exception {
        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder();

        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("SayGO", "1.0").addStartEvent("Start").addEndEvent("End").addSignalEventTrigger("GO").addTransition("Start", "End");
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(builder.done());
        final BusinessArchive endSignalArchive = archiveBuilder.done();

        builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("GetGO", "1.0").addActor(ACTOR_NAME).addStartEvent("Start").addIntermediateCatchEvent("OnSignal").addSignalEventTrigger("GO")
                .addUserTask("Task1", ACTOR_NAME).addTransition("Start", "OnSignal").addTransition("OnSignal", "Task1");
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(builder.done());
        final BusinessArchive startSignalArchive = archiveBuilder.done();

        final ProcessDefinition startSignal = deployAndEnableProcessWithActor(startSignalArchive, ACTOR_NAME, user);
        final ProcessDefinition endSignal = deployAndEnableProcess(endSignalArchive);

        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final ProcessInstance instance = getProcessAPI().startProcess(startSignal.getId());
        waitForEvent(instance, "OnSignal", TestStates.WAITING);

        getProcessAPI().startProcess(endSignal.getId());
        waitForUserTask("Task1");

        disableAndDeleteProcess(startSignal, endSignal);
    }

    @Test
    public void sendIntermadiateThrowSignal() throws Exception {
        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder();

        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        final String intermediate = "Intermediate";
        builder.createNewInstance("SayGO", "1.0").addStartEvent("Start").addIntermediateThrowEvent(intermediate).addSignalEventTrigger("GO").addEndEvent("End")
                .addTransition("Start", intermediate).addTransition(intermediate, "End");
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(builder.done());
        final BusinessArchive endSignalArchive = archiveBuilder.done();

        builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("GetGO", "1.0").addActor(ACTOR_NAME).addStartEvent("StartOnSignal").addSignalEventTrigger("GO")
                .addUserTask("Task1", ACTOR_NAME).addTransition("StartOnSignal", "Task1");
        archiveBuilder.createNewBusinessArchive().setProcessDefinition(builder.done());
        final BusinessArchive startSignalArchive = archiveBuilder.done();

        final ProcessDefinition startSignal = deployAndEnableProcessWithActor(startSignalArchive, ACTOR_NAME, user);
        final ProcessDefinition endSignal = deployAndEnableProcess(endSignalArchive);

        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        getProcessAPI().startProcess(endSignal.getId());
        waitForUserTask("Task1");

        disableAndDeleteProcess(startSignal, endSignal);
    }

    @Test
    public void sendSignalViaAPIToStartSignalEvent() throws Exception {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("GetGO", "1.0").addActor(ACTOR_NAME).addStartEvent("StartOnSignal").addSignalEventTrigger("GO")
                .addUserTask("Task1", ACTOR_NAME).addTransition("StartOnSignal", "Task1");
        final DesignProcessDefinition startSignalDef = builder.done();

        final ProcessDefinition startSignal = deployAndEnableProcessWithActor(startSignalDef, ACTOR_NAME, user);
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        getProcessAPI().sendSignal("GO");
        waitForUserTask("Task1");

        disableAndDeleteProcess(startSignal);
    }

    @Test
    public void sendSignalViaAPIToIntermediateSignalEvent() throws Exception {

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("GetGO", "1.0").addActor(ACTOR_NAME).addStartEvent("Start").addIntermediateCatchEvent("OnSignal").addSignalEventTrigger("GO")
                .addUserTask("Task1", ACTOR_NAME).addTransition("Start", "OnSignal").addTransition("OnSignal", "Task1");
        final ProcessDefinition intermediateSignal = deployAndEnableProcessWithActor(builder.done(), ACTOR_NAME, user);
        logoutOnTenant();
        loginOnDefaultTenantWith(USERNAME, PASSWORD);

        final ProcessInstance instance = getProcessAPI().startProcess(intermediateSignal.getId());
        waitForEvent(instance, "OnSignal", TestStates.WAITING);

        getProcessAPI().sendSignal("GO");
        waitForUserTask("Task1");

        disableAndDeleteProcess(intermediateSignal);
    }

}
