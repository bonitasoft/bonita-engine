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
package org.bonitasoft.engine.command.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Celine Souchet
 */
public class UserPermissionCommandIT extends TestWithTechnicalUser {

    private User manager;
    private User d1;
    private User d2;
    private User x3;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        final String userName = "Richard";
        manager = createUser("Jean-Michel", "bpm");
        d1 = createUser(userName, manager.getId());
        d2 = createUser("coder2", manager.getId());
        x3 = createUser("useless", "bpm");
        loginOnDefaultTenantWith(userName, "bpm");
    }

    @Override
    @After
    public void after() throws Exception {
        deleteUsers(d1, d2, x3, manager);
        super.after();
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.ACTOR, keywords = { "Actor", "Command", "User", "Team" }, jira = "BS-8961")
    @Test
    public void canStartProcessDefinitionCommand() throws Exception {
        final String actor11 = "killer";
        final String actor12 = "Slayer";
        final String actor13 = "Drawer";
        final Map<String, List<User>> actorUsers1 = new HashMap<String, List<User>>();
        actorUsers1.put(actor11, Arrays.asList(d1, d2));
        actorUsers1.put(actor12, Arrays.asList(d2));
        actorUsers1.put(actor13, Arrays.asList(d1));

        // First process definition:
        final ProcessDefinitionBuilder processBuilder1 = new ProcessDefinitionBuilder().createNewInstance("FirstProcess", "1.0");
        processBuilder1.addActor(actor11, true).addDescription("Killing daily").addUserTask("userTask1", actor11);
        processBuilder1.addActor(actor12).addDescription("Slaying vampires").addUserTask("userTask2", actor12);
        processBuilder1.addActor(actor13).addDescription("Classify files").addUserTask("userTask3", actor13);
        final ProcessDefinition processDef1 = deployAndEnableProcessWithActor(processBuilder1.done(), actorUsers1);

        final String actor21 = "UPS";
        final String actor22 = "FedEx";
        final Map<String, List<User>> actorUsers2 = new HashMap<String, List<User>>();
        actorUsers2.put(actor21, Arrays.asList(manager));
        actorUsers2.put(actor22, Arrays.asList(x3));

        // Second process definition:
        final ProcessDefinitionBuilder processBuilder2 = new ProcessDefinitionBuilder().createNewInstance("SecondProcess", "1.0");
        processBuilder2.addActor(actor21, true).addDescription("deliveryService1").addUserTask("userTask21", actor21);
        processBuilder2.addActor(actor22).addDescription("deliveryService2").addUserTask("userTask22", actor22);
        final ProcessDefinition processDef2 = deployAndEnableProcessWithActor(processBuilder2.done(), actorUsers2);

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("USER_ID_KEY", d1.getId());
        parameters.put("PROCESS_DEFINITION_ID_KEY", processDef1.getId());
        Boolean canStartProcessDefinition = (Boolean) getCommandAPI().execute("canStartProcessDefinition", parameters);
        assertTrue("User " + d1 + " should be allowed to start process " + processDef1, canStartProcessDefinition);

        parameters.put("USER_ID_KEY", manager.getId());
        parameters.put("PROCESS_DEFINITION_ID_KEY", processDef1.getId());
        canStartProcessDefinition = (Boolean) getCommandAPI().execute("canStartProcessDefinition", parameters);
        assertTrue(canStartProcessDefinition);

        parameters.put("USER_ID_KEY", manager.getId());
        parameters.put("PROCESS_DEFINITION_ID_KEY", processDef2.getId());
        canStartProcessDefinition = (Boolean) getCommandAPI().execute("canStartProcessDefinition", parameters);
        assertTrue("Manager should be allowed to start process " + processDef1, canStartProcessDefinition);

        parameters.put("USER_ID_KEY", d2.getId());
        parameters.put("PROCESS_DEFINITION_ID_KEY", processDef2.getId());
        canStartProcessDefinition = (Boolean) getCommandAPI().execute("canStartProcessDefinition", parameters);
        assertFalse(canStartProcessDefinition);

        // clean up:
        disableAndDeleteProcess(processDef1, processDef2);
    }

}
