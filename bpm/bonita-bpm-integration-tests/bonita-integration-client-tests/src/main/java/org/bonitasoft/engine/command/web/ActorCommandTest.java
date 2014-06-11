package org.bonitasoft.engine.command.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class ActorCommandTest extends CommonAPITest {

    private User manager;
    private User d1;
    private User d2;
    private User x3;

    @Before
    public void before() throws Exception {
        loginOnDefaultTenantWithDefaultTechnicalLogger();
        final String userName = "Richard";
        manager = createUser("Jean-Michel", "bpm");
        d1 = createUser(userName, manager.getId());
        d2 = createUser("coder2", manager.getId());
        x3 = createUser("useless", "bpm");
        loginOnDefaultTenantWith(userName, "bpm");
    }

    @After
    public void after() throws BonitaException {
        deleteUsers(d1, d2, x3, manager);
        logoutOnTenant();
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTOR, keywords = { "Actor", "Command", "User id", "Team" }, jira = "BS-8961")
    @Test
    public void isActorMemberOrTeamManagerOfActorMember() throws Exception {
        final String actor11 = "killer";
        final String actor12 = "Slayer";
        final String actor13 = "Drawer";
        final Map<String, List<User>> actorUsers1 = new HashMap<String, List<User>>();
        actorUsers1.put(actor11, Arrays.asList(d1, d2));
        actorUsers1.put(actor12, Arrays.asList(d2));
        actorUsers1.put(actor13, Arrays.asList(d1));

        // First process definition:
        final ProcessDefinitionBuilder processBuilder1 = new ProcessDefinitionBuilder().createNewInstance("FirstProcess", "1.0");
        processBuilder1.addActor(actor11).addDescription("Killing daily").addUserTask("userTask1", actor11);
        processBuilder1.addActor(actor12).addDescription("Slaying vampires").addUserTask("userTask2", actor12);
        processBuilder1.addActor(actor13).addDescription("Classify files").addUserTask("userTask3", actor13);
        final ProcessDefinition processDef1 = deployAndEnableProcessWithActor(processBuilder1.done(), actorUsers1);
        final ProcessInstance processInstance1 = startProcessAndWaitForTask(processDef1.getId(), "userTask1").getProcessInstance();

        final String actor21 = "UPS";
        final String actor22 = "FedEx";
        final Map<String, List<User>> actorUsers2 = new HashMap<String, List<User>>();
        actorUsers2.put(actor21, Arrays.asList(manager));
        actorUsers2.put(actor22, Arrays.asList(x3));

        // Second process definition:
        final ProcessDefinitionBuilder processBuilder2 = new ProcessDefinitionBuilder().createNewInstance("SecondProcess", "1.0");
        processBuilder2.addActor(actor21).addDescription("deliveryService1").addUserTask("userTask21", actor21);
        processBuilder2.addActor(actor22).addDescription("deliveryService2").addUserTask("userTask22", actor22);
        final ProcessDefinition processDef2 = deployAndEnableProcessWithActor(processBuilder2.done(), actorUsers2);
        final ProcessInstance processInstance2 = startProcessAndWaitForTask(processDef2.getId(), "userTask21").getProcessInstance();

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("USER_ID_KEY", manager.getId());
        parameters.put("PROCESS_INSTANCE_ID_KEY", processInstance1.getId());
        Boolean processDefActorIdMappings = (Boolean) getCommandAPI().execute("isActorMemberOrTeamManagerOfActorMember", parameters);
        assertTrue(processDefActorIdMappings);

        parameters.put("USER_ID_KEY", manager.getId());
        parameters.put("PROCESS_INSTANCE_ID_KEY", processInstance2.getId());
        processDefActorIdMappings = (Boolean) getCommandAPI().execute("isActorMemberOrTeamManagerOfActorMember", parameters);
        assertTrue(processDefActorIdMappings);

        parameters.put("USER_ID_KEY", d2.getId());
        parameters.put("PROCESS_INSTANCE_ID_KEY", processInstance2.getId());
        processDefActorIdMappings = (Boolean) getCommandAPI().execute("isActorMemberOrTeamManagerOfActorMember", parameters);
        assertFalse(processDefActorIdMappings);

        // clean up:
        disableAndDeleteProcess(processDef1, processDef2);
    }
}
