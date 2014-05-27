package org.bonitasoft.engine.command.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
public class ActorCommandTest extends CommonAPITest {

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
    }

    @After
    public void after() throws BonitaException {
        logoutOnTenant();
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTOR, keywords = { "Actor", "Command", "User id", "Team" }, story = "Get actors ids for user id including team.", jira = "")
    @Test
    public void getActorIdsForUserIdIncludingTeam() throws Exception {
        final String userName = "Richard";
        final String managerName = "Jean-Michel";
        final User manager = createUser(managerName, "bpm");
        final User d1 = createUser(userName, manager.getId());
        final User d2 = createUser("coder2", manager.getId());
        final User x3 = createUser("useless", "bpm");
        loginOnDefaultTenantWith(userName, "bpm");

        final String actor11 = "killer";
        final String actor12 = "Slayer";
        final String actor13 = "Drawer";
        // First process definition:
        final ProcessDefinitionBuilder processBuilder1 = new ProcessDefinitionBuilder().createNewInstance("FirstProcess", "1.0");
        processBuilder1.addActor(actor11).addDescription("Killing daily").addUserTask("userTask1", actor11);
        processBuilder1.addActor(actor12).addDescription("Slaying vampires").addUserTask("userTask2", actor12);
        processBuilder1.addActor(actor13).addDescription("Classify files").addUserTask("userTask3", actor13);
        final DesignProcessDefinition designProcessDefinition1 = processBuilder1.done();
        final BusinessArchive businessArchive1 = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition1).done();
        final ProcessDefinition processDef1 = getProcessAPI().deploy(businessArchive1);
        final List<ActorInstance> actors1 = getProcessAPI().getActors(processDef1.getId(), 0, 3, ActorCriterion.NAME_ASC);
        assertEquals(3, actors1.size());
        final ActorInstance _actor11 = actors1.get(0);
        getProcessAPI().addUserToActor(_actor11.getId(), d1.getId());
        getProcessAPI().addUserToActor(_actor11.getId(), d2.getId());
        final ActorInstance _actor12 = actors1.get(1);
        getProcessAPI().addUserToActor(_actor12.getId(), d2.getId());
        final ActorInstance _actor13 = actors1.get(2);
        getProcessAPI().addUserToActor(_actor13.getId(), d1.getId());
        getProcessAPI().enableProcess(processDef1.getId());

        final String actor21 = "UPS";
        final String actor22 = "FedEx";
        // Second process definition:
        final ProcessDefinitionBuilder processBuilder2 = new ProcessDefinitionBuilder().createNewInstance("SecondProcess", "1.0");
        processBuilder2.addActor(actor21).addDescription("deliveryService1").addUserTask("userTask21", actor21);
        processBuilder2.addActor(actor22).addDescription("deliveryService2").addUserTask("userTask22", actor22);
        final DesignProcessDefinition designProcessDefinition2 = processBuilder2.done();
        final BusinessArchive businessArchive2 = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition2).done();
        final ProcessDefinition processDef2 = getProcessAPI().deploy(businessArchive2);
        final List<ActorInstance> actors2 = getProcessAPI().getActors(processDef2.getId(), 0, 2, ActorCriterion.NAME_ASC);
        assertEquals(2, actors2.size());
        final ActorInstance _actor21 = actors2.get(0);
        getProcessAPI().addUserToActor(_actor21.getId(), manager.getId());
        final ActorInstance _actor22 = actors2.get(1);
        getProcessAPI().addUserToActor(_actor22.getId(), x3.getId());
        getProcessAPI().enableProcess(processDef2.getId());

        final String commandName = "getActorIdsForUserIdIncludingTeam";
        // No need to register anymore because it is done at tenant level on startup:
        // commandAPI.register(commandName, "get Map of ...", "org.bonitasoft.engine.external.actor.GetActorIdsForUserIdIncludingTeam");
        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("USER_ID_KEY", manager.getId());

        @SuppressWarnings("unchecked")
        final Map<Long, Set<Long>> processDefActorIdMappings = (Map<Long, Set<Long>>) getCommandAPI().execute(commandName, parameters);
        assertEquals(2, processDefActorIdMappings.size());
        assertTrue("Expected Process Definition Id " + processDef1.getId() + " not found", processDefActorIdMappings.containsKey(processDef1.getId()));
        assertTrue("Expected Process Definition Id " + processDef2.getId() + " not found", processDefActorIdMappings.containsKey(processDef2.getId()));
        final Set<Long> actorSet1 = processDefActorIdMappings.get(processDef1.getId());
        assertEquals(3, actorSet1.size());
        assertTrue(actorSet1.contains(_actor11.getId()));
        assertTrue(actorSet1.contains(_actor12.getId()));
        assertTrue(actorSet1.contains(_actor13.getId()));
        final Set<Long> actorSet2 = processDefActorIdMappings.get(processDef2.getId());
        assertEquals(1, actorSet2.size());
        assertTrue(actorSet2.contains(_actor21.getId()));

        // clean up:
        // commandAPI.unregister(commandName);
        deleteUser(d1.getId());
        deleteUser(d2.getId());
        deleteUser(x3.getId());
        deleteUser(manager.getId());
        disableAndDeleteProcess(processDef1);
        disableAndDeleteProcess(processDef2);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTOR, keywords = { "Actor", "Command", "Wrong parameter" }, story = "Execute actor command with wrong parameter.", jira = "ENGINE-548")
    @Test(expected = CommandParameterizationException.class)
    public void testExecuteActorCommandWithWrongParameter() throws Exception {

        final String commandName = "getActorIdsForUserIdIncludingTeam";

        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_PARAMETER", "aa");
        getCommandAPI().execute(commandName, parameters);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTOR, keywords = { "Actor", "Command", "Wrong parameter", "User Id" }, story = "Execute actor command with non existing id.", jira = "ENGINE-548")
    @Test(expected = CommandParameterizationException.class)
    public void testExecuteActorCommandWithNonExistingUserId() throws Exception {

        final String commandName = "getActorIdsForUserIdIncludingTeam";

        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("USER_ID_KEY", -1);
        getCommandAPI().execute(commandName, parameters);
    }
}
