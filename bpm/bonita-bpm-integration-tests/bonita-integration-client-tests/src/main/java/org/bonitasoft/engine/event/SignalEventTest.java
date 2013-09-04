package org.bonitasoft.engine.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessRuntimeAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.EventInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.IntermediateCatchEventInstance;
import org.bonitasoft.engine.bpm.flownode.IntermediateThrowEventInstance;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.TestStates;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SignalEventTest extends CommonAPITest {

    private static final String ACTOR_NAME = "User";

    private User john = null;

    @Before
    public void setUp() throws Exception {
        login();
        john = createUser("john", "bpm");
    }

    @After
    public void tearDown() throws Exception {
        logout();
        login();
        if (john != null) {
            getIdentityAPI().deleteUser(john.getId());
        }
        logout();
    }

    @Cover(classes = { EventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal Event", "Start event", "End event", "Send", "Receive" }, story = "Send a signal from an end event of a process to a start event of an other process.")
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

        final ProcessDefinition processDefinitionWithStartSignal = deployAndEnableWithActor(startSignalArchive, ACTOR_NAME, john);
        final ProcessDefinition processDefinitionWithEndSignal = deployAndEnableWithActor(endSignalArchive, ACTOR_NAME, john);

        logout();
        loginWith("john", "bpm");

        // Check that the process with trigger signal on start is not started, before send signal
        final ProcessInstance processInstanceWithEndSignal = getProcessAPI().startProcess(processDefinitionWithEndSignal.getId());
        waitForUserTask("step1", processInstanceWithEndSignal.getId());
        checkNbOfProcessInstances(1);

        List<HumanTaskInstance> taskInstances = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(1, taskInstances.size());
        assertEquals("step1", taskInstances.get(0).getName());

        // Send signal
        assignAndExecuteStep(taskInstances.get(0), john.getId());
        waitForProcessToFinish(processInstanceWithEndSignal.getId());

        // Check that the process with trigger signal on start is started, after send signal
        waitForUserTask("Task1");
        taskInstances = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(1, taskInstances.size());
        assertEquals("Task1", taskInstances.get(0).getName());

        disableAndDeleteProcess(processDefinitionWithStartSignal);
        disableAndDeleteProcess(processDefinitionWithEndSignal);
    }

    @Cover(classes = { EventInstance.class, IntermediateCatchEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal event",
            "Intermediate catch event", "End event", "Send", "Receive" }, story = "Send a signal from an end event of a process to an intermediate catch event of an other process.")
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

        final ProcessDefinition startSignal = deployAndEnableWithActor(startSignalArchive, ACTOR_NAME, john);
        final ProcessDefinition endSignal = deployAndEnableProcess(endSignalArchive);

        logout();
        loginWith("john", "bpm");
        final ProcessInstance instance = getProcessAPI().startProcess(startSignal.getId());

        waitForEvent(50, 25000, instance, "OnSignal", TestStates.getWaitingState());

        getProcessAPI().startProcess(endSignal.getId());

        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 100, 10000, true, 1, john);
        assertTrue("there was no pending task", checkNbPendingTaskOf.waitUntil());

        final List<HumanTaskInstance> taskInstances = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(1, taskInstances.size());
        final HumanTaskInstance taskInstance = taskInstances.get(0);
        assertEquals("Task1", taskInstance.getName());

        disableAndDeleteProcess(startSignal);
        disableAndDeleteProcess(endSignal);
    }

    @Cover(classes = { EventInstance.class, IntermediateThrowEventInstance.class }, concept = BPMNConcept.EVENTS, keywords = { "Event", "Signal event",
            "Intermediate catch event", "Start event", "Send", "Receive" }, story = "Send a sigal from an intermediate throw event of a process to a start event of an other process.")
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

        final ProcessDefinition startSignal = deployAndEnableWithActor(startSignalArchive, ACTOR_NAME, john);
        final ProcessDefinition endSignal = deployAndEnableProcess(endSignalArchive);

        logout();
        loginWith("john", "bpm");

        getProcessAPI().startProcess(endSignal.getId());

        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 100, 5000, true, 1, john);
        assertTrue("there was no pending task", checkNbPendingTaskOf.waitUntil());

        final List<HumanTaskInstance> taskInstances = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(1, taskInstances.size());
        final HumanTaskInstance taskInstance = taskInstances.get(0);
        assertEquals("Task1", taskInstance.getName());

        disableAndDeleteProcess(startSignal);
        disableAndDeleteProcess(endSignal);
    }

    @Cover(classes = { ProcessRuntimeAPI.class }, concept = BPMNConcept.EVENTS, keywords = { "signal", "throw event", "send signal", "start event" }, jira = "ENGINE-455")
    @Test
    public void sendSignalViaAPIToStartSignalEvent() throws Exception {

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("GetGO", "1.0").addActor(ACTOR_NAME).addStartEvent("StartOnSignal").addSignalEventTrigger("GO")
                .addUserTask("Task1", ACTOR_NAME).addTransition("StartOnSignal", "Task1");
        final DesignProcessDefinition startSignalDef = builder.done();

        final ProcessDefinition startSignal = deployAndEnableWithActor(startSignalDef, ACTOR_NAME, john);
        logout();
        loginWith("john", "bpm");

        getProcessAPI().sendSignal("GO");

        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 100, 5000, true, 1, john);
        assertTrue("there was no pending task", checkNbPendingTaskOf.waitUntil());

        final List<HumanTaskInstance> taskInstances = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(1, taskInstances.size());
        final HumanTaskInstance taskInstance = taskInstances.get(0);
        assertEquals("Task1", taskInstance.getName());

        disableAndDeleteProcess(startSignal);
    }

    @Cover(classes = { ProcessRuntimeAPI.class }, concept = BPMNConcept.EVENTS, keywords = { "signal", "throw event", "send signal", "intermediate catch event" }, jira = "ENGINE-455")
    @Test
    public void sendSignalViaAPIToIntermediateSignalEvent() throws Exception {

        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("GetGO", "1.0").addActor(ACTOR_NAME).addStartEvent("Start").addIntermediateCatchEvent("OnSignal").addSignalEventTrigger("GO")
                .addUserTask("Task1", ACTOR_NAME).addTransition("Start", "OnSignal").addTransition("OnSignal", "Task1");
        final ProcessDefinition intermediateSignal = deployAndEnableWithActor(builder.done(), ACTOR_NAME, john);
        logout();
        loginWith("john", "bpm");

        final ProcessInstance instance = getProcessAPI().startProcess(intermediateSignal.getId());
        waitForEvent(50, 25000, instance, "OnSignal", TestStates.getWaitingState());

        getProcessAPI().sendSignal("GO");

        final CheckNbPendingTaskOf checkNbPendingTaskOf = new CheckNbPendingTaskOf(getProcessAPI(), 100, 5000, true, 1, john);
        assertTrue("there was no pending task", checkNbPendingTaskOf.waitUntil());

        final List<HumanTaskInstance> taskInstances = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, ActivityInstanceCriterion.NAME_ASC);
        assertEquals(1, taskInstances.size());
        final HumanTaskInstance taskInstance = taskInstances.get(0);
        assertEquals("Task1", taskInstance.getName());

        disableAndDeleteProcess(intermediateSignal);
    }

}
