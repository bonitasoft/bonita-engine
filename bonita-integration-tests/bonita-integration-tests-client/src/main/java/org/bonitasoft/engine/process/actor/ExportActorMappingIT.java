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
package org.bonitasoft.engine.process.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.TestWithUser;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class ExportActorMappingIT extends TestWithUser {

    @Test
    public void exportSimpleActorMapping() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(ACTOR_NAME).addDescription("Delivery all day and night long").addUserTask("userTask1", ACTOR_NAME);
        final ProcessDefinition definition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);

        final String xmlContent = getProcessAPI().exportActorMapping(definition.getId());
        final InputStream xmlStream = ImportActorMappingIT.class.getResourceAsStream("simpleActorMapping.xml");
        try {
            final byte[] actormapping = IOUtils.toByteArray(xmlStream);
            assertEquals(removewhitespaces(new String(actormapping)), removewhitespaces(xmlContent));
        } finally {
            xmlStream.close();
        }
        disableAndDeleteProcess(definition);
    }

    @Test
    public void exportActorMappingWithDeletedGroup() throws Exception {
        final Group sales = createGroup("sales");

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(ACTOR_NAME).addDescription("Delivery all day and night long").addUserTask("userTask1", ACTOR_NAME);
        final ProcessDefinition definition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, sales);

        deleteGroups(sales);
        // should work
        final String xmlContent = getProcessAPI().exportActorMapping(definition.getId());
        disableAndDeleteProcess(definition);
        assertFalse(xmlContent.contains("group") || xmlContent.contains("sale"));
    }

    @Test
    public void exportActorMappingWithDeletedUser() throws Exception {
        final User john = createUser("john", "bpm");

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(ACTOR_NAME).addDescription("Delivery all day and night long").addUserTask("userTask1", ACTOR_NAME);
        final ProcessDefinition definition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, john);

        deleteUsers(john);
        // should work
        final String xmlContent = getProcessAPI().exportActorMapping(definition.getId());
        disableAndDeleteProcess(definition);
        assertFalse(xmlContent.contains("user") || xmlContent.contains("john"));
    }

    @Test
    public void exportActorMappingWithDeletedMembership() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(ACTOR_NAME).addDescription("Delivery all day and night long").addUserTask("userTask1", ACTOR_NAME);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        loginOnDefaultTenantWithDefaultTechnicalUser();
        final Group group = createGroup("group");
        final Role role = createRole("role");
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);

        getProcessAPI().addRoleAndGroupToActor(actor.getId(), role.getId(), group.getId());
        deleteRoles(role);
        // should work
        final String xmlContent = getProcessAPI().exportActorMapping(definition.getId());
        deleteProcess(definition);
        assertFalse(xmlContent.contains("group") || xmlContent.contains("role"));
        deleteGroups(group);
    }

    @Test
    public void exportActorMappingWithDeletedSubGroup() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(ACTOR_NAME).addDescription("Delivery all day and night long").addUserTask("userTask1", ACTOR_NAME);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        loginOnDefaultTenantWithDefaultTechnicalUser();
        final Group sales = createGroup("sales");
        final Group subSales = createGroup("sub", "/sales");
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);

        getProcessAPI().addGroupToActor(actor.getId(), subSales.getId());
        deleteGroups(sales);
        // should work
        final String xmlContent = getProcessAPI().exportActorMapping(definition.getId());
        deleteProcess(definition);
        assertFalse(xmlContent, xmlContent.contains("group") || xmlContent.contains("sale"));
    }

    @Test
    public void exportActorMappingWithDeletedRole() throws Exception {
        final Role sales = createRole("sales");

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(ACTOR_NAME).addDescription("Delivery all day and night long").addUserTask("userTask1", ACTOR_NAME);
        final ProcessDefinition definition = deployAndEnableProcessWithActor(processBuilder.done(), ACTOR_NAME, user);

        deleteRoles(sales);
        // should work
        final String xmlContent = getProcessAPI().exportActorMapping(definition.getId());
        disableAndDeleteProcess(definition);
        assertFalse(xmlContent.contains("role") || xmlContent.contains("sale"));
    }

    @Test
    public void exportcomplexActorMapping() throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(ACTOR_NAME).addDescription("Delivery all day and night long").addUserTask("userTask1", ACTOR_NAME);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        loginOnDefaultTenantWithDefaultTechnicalUser();
        final User john = createUser("john", "bpm");
        final Group rd = createGroup("RD");
        final Role role = createRole("dev");
        // final UserMembership createUserMembership = createUserMembership(johnName, "RD", "dev");
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);
        assertEquals(ACTOR_NAME, actor.getName());

        getProcessAPI().addUserToActor(actor.getId(), john.getId());
        getProcessAPI().addGroupToActor(actor.getId(), rd.getId());
        getProcessAPI().addRoleToActor(actor.getId(), role.getId());
        getProcessAPI().addRoleAndGroupToActor(actor.getId(), role.getId(), rd.getId());

        final String xmlContent = getProcessAPI().exportActorMapping(definition.getId());
        final InputStream xmlStream = ImportActorMappingIT.class.getResourceAsStream("complexActorMapping.xml");
        try {
            final byte[] actormapping = IOUtils.toByteArray(xmlStream);
            assertEquals(removewhitespaces(new String(actormapping)), removewhitespaces(xmlContent));
        } finally {
            xmlStream.close();
        }
        // getIdentityAPI().deleteUserMembership(createUserMembership.getId());
        getIdentityAPI().deleteGroup(rd.getId());
        getIdentityAPI().deleteRole(role.getId());
        deleteUser(john);
        deleteProcess(definition);
    }

    private String removewhitespaces(final String content) {
        String replace = content.replaceAll("\\s+", "");
        return replace = replace.replaceAll("\n", "");
    }

}
