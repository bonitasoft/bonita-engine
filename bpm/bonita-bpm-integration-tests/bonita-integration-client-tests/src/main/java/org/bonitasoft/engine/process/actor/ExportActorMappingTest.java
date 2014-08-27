package org.bonitasoft.engine.process.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.CommonAPITest;
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
public class ExportActorMappingTest extends CommonAPITest {

    @Test
    public void exportSimpleActorMapping() throws Exception {
        final String delivery = "Delivery men";
        final String johnName = "john";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("userTask1", delivery);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

         loginOnDefaultTenantWithDefaultTechnicalUser();
        final User john = createUser(johnName, "bpm");
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);
        assertEquals(delivery, actor.getName());

        getProcessAPI().addUserToActor(actor.getId(), john.getId());

        final String xmlContent = getProcessAPI().exportActorMapping(definition.getId());
        final InputStream xmlStream = ImportActorMappingTest.class.getResourceAsStream("simpleActorMapping.xml");
        try {
            final byte[] actormapping = IOUtils.toByteArray(xmlStream);
            assertEquals(removewhitespaces(new String(actormapping)), removewhitespaces(xmlContent));
        } finally {
            xmlStream.close();
        }
        deleteUser(johnName);
        deleteProcess(definition);

        logoutOnTenant();
    }

    @Test
    public void exportActorMappingWithDeletedGroup() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("userTask1", delivery);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

         loginOnDefaultTenantWithDefaultTechnicalUser();
        final Group sales = createGroup("sales");
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);

        getProcessAPI().addGroupToActor(actor.getId(), sales.getId());
        deleteGroups(sales);
        // should work
        final String xmlContent = getProcessAPI().exportActorMapping(definition.getId());
        deleteProcess(definition);
        assertFalse(xmlContent.contains("group") || xmlContent.contains("sale"));

        logoutOnTenant();
    }

    @Test
    public void exportActorMappingWithDeletedUser() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("userTask1", delivery);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

         loginOnDefaultTenantWithDefaultTechnicalUser();
        final User john = createUser("john", "bpm");
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);

        getProcessAPI().addUserToActor(actor.getId(), john.getId());
        deleteUsers(john);
        // should work
        final String xmlContent = getProcessAPI().exportActorMapping(definition.getId());
        deleteProcess(definition);
        assertFalse(xmlContent.contains("user") || xmlContent.contains("john"));

        logoutOnTenant();
    }

    @Test
    public void exportActorMappingWithDeletedMembership() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("userTask1", delivery);
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
        logoutOnTenant();
    }

    @Test
    public void exportActorMappingWithDeletedSubGroup() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("userTask1", delivery);
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

        logoutOnTenant();
    }

    @Test
    public void exportActorMappingWithDeletedRole() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("userTask1", delivery);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

         loginOnDefaultTenantWithDefaultTechnicalUser();
        final Role sales = createRole("sales");
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);

        getProcessAPI().addRoleToActor(actor.getId(), sales.getId());
        deleteRoles(sales);
        // should work
        final String xmlContent = getProcessAPI().exportActorMapping(definition.getId());
        deleteProcess(definition);
        assertFalse(xmlContent.contains("role") || xmlContent.contains("sale"));

        logoutOnTenant();
    }

    @Test
    public void exportcomplexActorMapping() throws Exception {
        final String delivery = "Delivery men";
        final String johnName = "john";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("exportProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("userTask1", delivery);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

         loginOnDefaultTenantWithDefaultTechnicalUser();
        final User john = createUser(johnName, "bpm");
        final Group rd = createGroup("RD");
        final Role role = createRole("dev");
        // final UserMembership createUserMembership = createUserMembership(johnName, "RD", "dev");
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());

        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);
        assertEquals(delivery, actor.getName());

        getProcessAPI().addUserToActor(actor.getId(), john.getId());
        getProcessAPI().addGroupToActor(actor.getId(), rd.getId());
        getProcessAPI().addRoleToActor(actor.getId(), role.getId());
        getProcessAPI().addRoleAndGroupToActor(actor.getId(), role.getId(), rd.getId());

        final String xmlContent = getProcessAPI().exportActorMapping(definition.getId());
        final InputStream xmlStream = ImportActorMappingTest.class.getResourceAsStream("complexActorMapping.xml");
        try {
            final byte[] actormapping = IOUtils.toByteArray(xmlStream);
            assertEquals(removewhitespaces(new String(actormapping)), removewhitespaces(xmlContent));
        } finally {
            xmlStream.close();
        }
        // getIdentityAPI().deleteUserMembership(createUserMembership.getId());
        getIdentityAPI().deleteGroup(rd.getId());
        getIdentityAPI().deleteRole(role.getId());
        deleteUser(johnName);
        deleteProcess(definition);

        logoutOnTenant();
    }

    private String removewhitespaces(final String content) {
        String replace = content.replaceAll("\\s+", "");
        return replace = replace.replaceAll("\n", "");
    }

}
