package org.bonitasoft.engine.command.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Zhao Na
 */
public class ActorPermissionCommandTest extends CommonAPITest {

    private final String IS_ALLOWED_TO_START_PROCESS_CMD = "isAllowedToStartProcess";

    private final String IS_ALLOWED_TO_START_PROCESSES_CMD = "isAllowedToStartProcesses";

    private final String IS_ALLOWED_TO_SEE_OVERVIEW_FROM_CMD = "isAllowedToSeeOverviewForm";

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalUser();
    }

    @After
    public void after() throws BonitaException {
        logoutOnTenant();
    }

    private void cleanup(final long userId, final List<Long> processDefinitionIds) throws BonitaException {
        // clean up:
        deleteUser(userId);
        disableAndDeleteProcessById(processDefinitionIds);
    }

    private Map<String, Serializable> prepareParametersWithUserId(final long userId, final List<Long> processDefinitionIds) {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("USER_ID_KEY", userId);
        parameters.put("PROCESSDEFINITION_IDS_KEY", (Serializable) processDefinitionIds);
        return parameters;
    }

    private Map<String, Serializable> prepareParameters(final long processDefId, final Set<Long> actorIds) {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("PROCESS_DEFINITION_ID_KEY", processDefId);
        parameters.put("ACTOR_IDS_KEY", (Serializable) actorIds);
        return parameters;
    }

    private Map<String, Serializable> prepareParametersWithArchivedDescriptor(final long userId, final long processInstanceId) {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("USER_ID_KEY", userId);
        parameters.put("PROCESSINSTANCE_ID_KEY", processInstanceId);
        return parameters;
    }

    private ProcessDefinitionBuilder setterActors(final ProcessDefinitionBuilder processDefinitionBuilder, final List<String> actorNames,
            final List<Boolean> isInitiators) {
        final ProcessDefinitionBuilder processDefBuilder = processDefinitionBuilder;
        if (actorNames != null && !actorNames.isEmpty() && isInitiators != null && !isInitiators.isEmpty()) {
            if (actorNames.size() == isInitiators.size()) {
                for (int i = 0; i < actorNames.size(); i++) {
                    final String name = actorNames.get(i);
                    if (isInitiators.get(i)) {
                        processDefBuilder.setActorInitiator(name);
                    } else {
                        processDefBuilder.addActor(name);
                    }
                }
            }
        }
        return processDefBuilder;
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTOR, keywords = { "Command", "Actor permission", "Start process" }, story = "Test if an actor is allowed to start a process.", jira = "")
    @Test
    public void testIsAllowedToStartProcesses() throws Exception {
        final String userName = "Manu";
        final User manu = createUser(userName, "bpm");
        loginOnDefaultTenantWith(userName, "bpm");
        final String ACTOR_NAME1 = "ActorMenu";
        final String ACTOR_NAME2 = "ActorElias";
        final String ACTOR_NAME3 = "ActorBap";

        final List<Long> pIds = new ArrayList<Long>();
        final int num = 5;
        for (int i = 0; i < num; i++) {
            ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process", String.valueOf(i));
            if (i % 2 == 0) {
                processDefinitionBuilder = setterActors(processDefinitionBuilder, Arrays.asList(ACTOR_NAME1, ACTOR_NAME1, ACTOR_NAME2, ACTOR_NAME3),
                        Arrays.asList(true, false, false, false));
            } else {
                processDefinitionBuilder = setterActors(processDefinitionBuilder, Arrays.asList(ACTOR_NAME1, ACTOR_NAME1, ACTOR_NAME2, ACTOR_NAME3),
                        Arrays.asList(false, false, false, false));
            }

            // one process has only one actorInitiator.
            final DesignProcessDefinition processDef = processDefinitionBuilder.done();
            final ProcessDefinition processDefinition = getProcessAPI().deploy(
                    new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDef).done());
            getProcessAPI().addUserToActor(ACTOR_NAME1, processDefinition, manu.getId());
            getProcessAPI().addUserToActor(ACTOR_NAME2, processDefinition, manu.getId());
            getProcessAPI().addUserToActor(ACTOR_NAME3, processDefinition, manu.getId());
            final long processDefinitionId = processDefinition.getId();
            getProcessAPI().enableProcess(processDefinitionId);
            final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
            assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

            getProcessAPI().startProcess(processDefinition.getId());
            pIds.add(processDefinitionId);
        }

        final Map<String, Serializable> paras = prepareParametersWithUserId(manu.getId(), pIds);
        final Map<Long, Boolean> res = (Map<Long, Boolean>) getCommandAPI().execute(IS_ALLOWED_TO_START_PROCESSES_CMD, paras);

        assertEquals(pIds.size(), res.size());
        assertTrue(res.get(pIds.get(0)));
        assertFalse(res.get(pIds.get(1)));
        assertTrue(res.get(pIds.get(2)));
        assertFalse(res.get(pIds.get(3)));
        assertTrue(res.get(pIds.get(4)));

        cleanup(manu.getId(), pIds);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTOR, keywords = { "Command", "Actor permission", "Start process" }, story = "Test if an actor is allowed to start a process.", jira = "")
    @Test
    public void testIsAllowedToStartProcess() throws Exception {
        final String userName = "Manu";
        final User manu = createUser(userName, "bpm");
        loginOnDefaultTenantWith(userName, "bpm");
        final String ACTOR_NAME1 = "ActorMenu";
        final String ACTOR_NAME2 = "ActorElias";
        final String ACTOR_NAME3 = "ActorBap";

        DesignProcessDefinition processDef = null;
        ProcessDefinitionBuilder processDefinitionBuilder = new ProcessDefinitionBuilder().createNewInstance("My_Process", "1.0");
        processDefinitionBuilder = setterActors(processDefinitionBuilder, Arrays.asList(ACTOR_NAME1, ACTOR_NAME1, ACTOR_NAME2, ACTOR_NAME3),
                Arrays.asList(true, false, false, false));
        // one process has only one actorInitiator.
        processDef = processDefinitionBuilder.done();

        final ProcessDefinition processDefinition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDef).done());
        getProcessAPI().addUserToActor(ACTOR_NAME1, processDefinition, manu.getId());
        getProcessAPI().addUserToActor(ACTOR_NAME2, processDefinition, manu.getId());
        getProcessAPI().addUserToActor(ACTOR_NAME3, processDefinition, manu.getId());
        final long processDefinitionId = processDefinition.getId();
        getProcessAPI().enableProcess(processDefinitionId);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        getProcessAPI().startProcess(processDefinition.getId());
        final List<ActorInstance> actors = getProcessAPI().getActors(processDefinitionId, 0, 5, ActorCriterion.NAME_ASC);
        final ActorInstance actorInitiator = getProcessAPI().getActorInitiator(processDefinitionId);
        actors.add(actorInitiator);

        // generate ids
        final Set<Long> actorInstanceIds = new HashSet<Long>();
        for (final ActorInstance actor : actors) {
            actorInstanceIds.add(actor.getId());
        }

        final Map<String, Serializable> paras = prepareParameters(processDefinition.getId(), actorInstanceIds);
        final boolean res = (Boolean) getCommandAPI().execute(IS_ALLOWED_TO_START_PROCESS_CMD, paras);
        assertTrue(res);

        cleanup(manu.getId(), Arrays.asList(processDefinition.getId()));
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTOR, keywords = { "Command", "Actor permission", "Initiator actor", "Overview form" }, story = "Test if initiator actor is allowed to see overview form.", jira = "")
    @Test
    public void testIsAllowedToSeeOverviewFormForInitiatorActor() throws Exception {
        final String userName = "Manu";
        final User manu = createUser(userName, "bpm");
        loginOnDefaultTenantWith(userName, "bpm");

        final List<Long> processDefinitionIds = new ArrayList<Long>(2);
        final List<Long> processInstanceIds = new ArrayList<Long>(2);
        for (int i = 0; i < 2; i++) {
            final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(
                    "SearchOpenProcessInstancesInvolvingUser", "14." + i);
            if (i == 1) {
                designProcessDefinition.setActorInitiator(ACTOR_NAME);
            }
            designProcessDefinition.addActor(ACTOR_NAME);
            designProcessDefinition.addAutomaticTask("step1");
            designProcessDefinition.addUserTask("step2", ACTOR_NAME);
            designProcessDefinition.addTransition("step1", "step2");
            // assign pending task to jack
            final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, manu);
            final long processDefinitionId = processDefinition.getId();
            final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinitionId);
            assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

            final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

            processDefinitionIds.add(processDefinitionId);
            processInstanceIds.add(processInstance.getId());
            waitForUserTask("step2", processInstance.getId());
        }

        final Map<String, Serializable> paras1 = prepareParametersWithArchivedDescriptor(manu.getId(), processInstanceIds.get(0));
        assertTrue((Boolean) getCommandAPI().execute(IS_ALLOWED_TO_SEE_OVERVIEW_FROM_CMD, paras1));

        final Map<String, Serializable> paras2 = prepareParametersWithArchivedDescriptor(manu.getId(), processInstanceIds.get(1));
        assertTrue((Boolean) getCommandAPI().execute(IS_ALLOWED_TO_SEE_OVERVIEW_FROM_CMD, paras2));

        cleanup(manu.getId(), processDefinitionIds);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTOR, keywords = { "Command", "Actor permission", "Archived process instance", "Overview form",
            "User" }, story = "Test if user is allowed to see overview form for archived process instances.", jira = "")
    @Ignore("test was bad (does not test archived things)")
    @Test
    public void testIsAllowedToSeeOverviewFormForArchivedProcessInstancesInvolvingUser() throws Exception {
        final User user = createUser("jack", "bpm");
        // create process
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("SearchOpenProcessInstancesInvolvingUser",
                "14.3");
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addAutomaticTask("step1");
        designProcessDefinition.addUserTask("step2", ACTOR_NAME);
        designProcessDefinition.addTransition("step1", "step2");
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        final HumanTaskInstance pendingTask = waitForUserTask("step2", processInstance);
        final Map<String, Serializable> paras1 = prepareParametersWithArchivedDescriptor(user.getId(), processInstance.getId());
        // before execute
        assertFalse((Boolean) getCommandAPI().execute(IS_ALLOWED_TO_SEE_OVERVIEW_FROM_CMD, paras1));
        assignAndExecuteStep(pendingTask, user.getId());
        waitForProcessToFinish(processInstance);
        // after execute
        assertTrue((Boolean) getCommandAPI().execute(IS_ALLOWED_TO_SEE_OVERVIEW_FROM_CMD, paras1));

        skipTask(pendingTask.getId());
        cleanup(user.getId(), Arrays.asList(processDefinition.getId()));
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTOR, keywords = { "Command", "Actor permission", "Archived process instance", "Overview form",
            "User" }, story = "Test if user is allowed to see overview form for archived process instances.", jira = "")
    @Test
    public void testIsAllowedToSeeOverviewFormForProcessInstancesInvolvingUser() throws Exception {
        final User user = createUser("jack", "bpm");
        // create process
        final ProcessDefinitionBuilder designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("SearchOpenProcessInstancesInvolvingUser",
                "14.3");
        designProcessDefinition.addActor(ACTOR_NAME);
        designProcessDefinition.addAutomaticTask("step1");
        designProcessDefinition.addUserTask("step2", ACTOR_NAME);
        designProcessDefinition.addUserTask("step3", ACTOR_NAME);
        designProcessDefinition.addTransition("step1", "step2");
        designProcessDefinition.addTransition("step2", "step3");
        // assign pending task to jack
        final ProcessDefinition processDefinition = deployAndEnableProcessWithActor(designProcessDefinition.done(), ACTOR_NAME, user);
        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());

        logoutOnTenant();
        loginOnDefaultTenantWith("jack", "bpm");

        final HumanTaskInstance pendingTask = waitForUserTask("step2", processInstance);
        final Map<String, Serializable> paras1 = prepareParametersWithArchivedDescriptor(user.getId(), processInstance.getId());
        // before execute
        assertFalse((Boolean) getCommandAPI().execute(IS_ALLOWED_TO_SEE_OVERVIEW_FROM_CMD, paras1));
        assignAndExecuteStep(pendingTask, user.getId());
        waitForUserTask("step3", processInstance);

        // after execute
        assertTrue((Boolean) getCommandAPI().execute(IS_ALLOWED_TO_SEE_OVERVIEW_FROM_CMD, paras1));

        cleanup(user.getId(), Arrays.asList(processDefinition.getId()));
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Command", "Actor permission", "Wrong parameter" }, story = "Execute actor permission command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = CommandParameterizationException.class)
    public void testIsAllowedToStartProcessCommandWithWrongParameter() throws Exception {

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_KEY", "bad_value");

        getCommandAPI().execute(IS_ALLOWED_TO_START_PROCESS_CMD, parameters);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Command", "Actor permission", "Wrong parameter" }, story = "Execute actor permission command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = CommandParameterizationException.class)
    public void testIsAllowedToStartProcessesCommandWithWrongParameter() throws Exception {

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_KEY", "bad_value");

        getCommandAPI().execute(IS_ALLOWED_TO_START_PROCESSES_CMD, parameters);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.PROFILE, keywords = { "Command", "Actor permission", "Wrong parameter" }, story = "Execute actor permission command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = CommandParameterizationException.class)
    public void testIsAllowedToSeeOverviewFormCommandWithWrongParameter() throws Exception {

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_KEY", "bad_value");

        getCommandAPI().execute(IS_ALLOWED_TO_SEE_OVERVIEW_FROM_CMD, parameters);
    }

}
