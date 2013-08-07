package org.bonitasoft.engine.process.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorMappingImportException;
import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.WaitUntil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class ImportActorMappingTest extends CommonAPITest {

    private static final String DELIVERY_MEN = "Delivery men";

    @After
    public void afterTest() throws BonitaException {
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
    }

    @Test
    public void importSimpleActorMapping() throws Exception {
        final String johnName = "john";

        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("simpleActorMapping.xml");
        final User john = createUser(johnName, "bpm");
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        getProcessAPI().enableProcess(definition.getId());

        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final long processInstanceId = getProcessAPI().startProcess(definition.getId()).getId();
        assertNotNull(waitForUserTask("userTask1", processInstanceId));
        assertTrue("no new activity found", new WaitUntil(20, 500) {

            @Override
            protected boolean check() throws Exception {
                return getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null).size() == 1;
            }
        }.waitUntil());

        final List<HumanTaskInstance> tasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, tasks.size());

        disableAndDeleteProcess(definition);
        deleteUser(johnName);
    }

    @Test
    public void importComplexActorMapping() throws Exception {
        final String johnName = "john";

        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("complexActorMapping.xml");
        final User john = createUser(johnName, "bpm");
        final Group rd = createGroup("RD");
        final Role role = createRole("dev");
        final ProcessDefinition definition = getProcessAPI().deploy(businessArchive.done());
        getProcessAPI().enableProcess(definition.getId());

        getAndCheckActors(john, rd, role, definition);

        disableAndDeleteProcess(definition);
        getIdentityAPI().deleteGroup(rd.getId());
        getIdentityAPI().deleteRole(role.getId());
        deleteUser(johnName);
    }

    @Test
    public void importActorMappingWithWrongXMLFile() throws Exception {
        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("actorMappingWithException.xml");

        checkProcessNotActivated(businessArchive);
    }

    @Test
    public void importActorMappingWithUnknownUser() throws Exception {
        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("simpleActorMapping.xml");

        checkProcessNotActivated(businessArchive);
    }

    @Test
    public void importActorMappingWithUnknownGroup() throws Exception {
        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("complexActorMapping.xml");

        // even if missing group the process is resolved
        checkProcessActivated(businessArchive);
    }

    @Test
    public void importActorMappingWithUnknownRole() throws Exception {
        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("complexActorMapping.xml");

        // even if missing role the process is resolved
        checkProcessActivated(businessArchive);
    }

    @Test
    public void importActorMappingWithUnknownMemberShip() throws Exception {
        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("complexActorMappingWithUnkownGroup.xml");

        // even if missing membership the process is resolved
        checkProcessActivated(businessArchive);
    }

    @Test
    public void testImportActorMapping() throws Exception {
        final User user = createUser("john", "bpm");
        final Group rd = createGroup("RD");
        final Role role = createRole("dev");

        final ProcessDefinitionBuilder processBuilder = createProcessDefinitionBuilder();
        final ProcessDefinition definition = deployAndEnableWithActor(processBuilder.getProcess(), DELIVERY_MEN, user);
        getProcessAPI().importActorMapping(definition.getId(), xmlToByteArray("complexActorMapping2.xml"));

        getAndCheckActors(user, rd, role, definition);

        // clean-up
        disableAndDeleteProcess(definition);
        getIdentityAPI().deleteGroup(rd.getId());
        getIdentityAPI().deleteRole(role.getId());
        deleteUser(user.getId());
    }

    @Test(expected = ActorMappingImportException.class)
    public void testImportActorMappingWithWrongXMLFile() throws Exception {
        final User user = createUser("john", "bpm");

        createProcessDefinitionAndCheckActorMappingImportException(user, null, null, "actorMappingWithException.xml");
    }

    @Test(expected = ActorMappingImportException.class)
    public void testImportActorMappingWithUnknownUser() throws Exception {
        final User user = createUser("paul", "bpm");

        createProcessDefinitionAndCheckActorMappingImportException(user, null, null, "simpleActorMapping.xml");
    }

    @Test(expected = ActorMappingImportException.class)
    public void testImportActorMappingWithUnknownGroup() throws Exception {
        final User user = createUser("john", "bpm");
        final Group rd = createGroup("RD1");
        final Role role = createRole("dev");

        createProcessDefinitionAndCheckActorMappingImportException(user, rd, role, "complexActorMapping.xml");
    }

    @Test(expected = ActorMappingImportException.class)
    public void testImportActorMappingWithUnknownRole() throws Exception {
        final User user = createUser("john", "bpm");
        final Group rd = createGroup("RD");
        final Role role = createRole("dev1");

        createProcessDefinitionAndCheckActorMappingImportException(user, rd, role, "complexActorMapping.xml");
    }

    @Test(expected = ActorMappingImportException.class)
    public void testImportActorMappingWithUnknownMemberShip() throws Exception {
        final User user = createUser("john", "bpm");
        final Group rd = createGroup("RD2");
        final Role role = createRole("dev");

        createProcessDefinitionAndCheckActorMappingImportException(user, rd, role, "complexActorMappingWithUnkownGroup.xml");
    }

    /**
     * @param xmlFileName
     * @return ProcessDefinition
     * @throws Exception
     * @since 6.0
     */
    private BusinessArchiveBuilder createAndDeployProcessDefinitionWithImportedActorMapping(final String xmlFileName) throws Exception {
        final DesignProcessDefinition processDefinition = createProcessDefinitionBuilder().done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);

        final byte[] actormapping = xmlToByteArray(xmlFileName);
        if (actormapping != null) {
            businessArchive.setActorMapping(actormapping);
        }

        return businessArchive;
    }

    /**
     * @param xmlFileName
     * @param businessArchive
     * @return
     * @throws IOException
     * @since 6.0
     */
    private byte[] xmlToByteArray(final String xmlFileName) throws IOException {
        final InputStream xmlStream = ImportActorMappingTest.class.getResourceAsStream(xmlFileName);
        byte[] actormapping = null;
        try {
            actormapping = IOUtils.toByteArray(xmlStream);
        } finally {
            xmlStream.close();
        }
        return actormapping;
    }

    /**
     * @return
     * @throws InvalidProcessDefinitionException
     * @since 6.0
     */
    private ProcessDefinitionBuilder createProcessDefinitionBuilder() throws InvalidProcessDefinitionException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(DELIVERY_MEN).addDescription("Delivery all day and night long").addUserTask("userTask1", DELIVERY_MEN);
        return processBuilder;
    }

    private void checkProcessNotActivated(final BusinessArchiveBuilder businessArchive) throws Exception {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive.done());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());
        final List<Problem> processResolutionProblems = getProcessAPI().getProcessResolutionProblems(processDefinition.getId());
        assertEquals(1, processResolutionProblems.size());
        final Problem problem = processResolutionProblems.get(0);
        assertEquals("actor", problem.getResource());
        deleteProcess(processDefinition);
        getIdentityAPI().deleteOrganization();
    }

    private void checkProcessActivated(final BusinessArchiveBuilder businessArchive) throws Exception {
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive.done());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());
        deleteProcess(processDefinition);
        getIdentityAPI().deleteOrganization();
    }

    /**
     * @param user
     * @param string
     * @throws Exception
     * @since 6.0
     */
    private void createProcessDefinitionAndCheckActorMappingImportException(final User user, final Group group, final Role role, final String xmlFileName)
            throws Exception {
        final ProcessDefinitionBuilder processBuilder = createProcessDefinitionBuilder();
        final ProcessDefinition definition = deployAndEnableWithActor(processBuilder.getProcess(), DELIVERY_MEN, user);

        try {
            getProcessAPI().importActorMapping(definition.getId(), xmlToByteArray(xmlFileName));
        } finally {
            // clean-up
            disableAndDeleteProcess(definition);
            if (group != null) {
                getIdentityAPI().deleteGroup(group.getId());
            }
            if (role != null) {
                getIdentityAPI().deleteRole(role.getId());
            }
            if (user != null) {
                deleteUser(user.getUserName());
            }
            getIdentityAPI().deleteOrganization();
        }
    }

    /**
     * @param user
     * @param group
     * @param role
     * @param definition
     * @throws Exception
     * @since 6.0
     */
    private void getAndCheckActors(final User user, final Group group, final Role role, final ProcessDefinition definition) throws Exception {
        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 15, ActorCriterion.NAME_DESC);
        final ActorInstance actorInstance = actors.get(0);
        final List<ActorMember> actorMembers = getProcessAPI().getActorMembers(actorInstance.getId(), 0, 15);
        assertEquals(4, actorMembers.size());
        for (final ActorMember actorMember : actorMembers) {
            assertTrue(checkRole(actorMember, role.getId()) || checkGroup(actorMember, group.getId()) || checkUser(actorMember, user.getId())
                    || checkMembership(actorMember, role.getId(), group.getId()));
        }
    }

    private boolean checkMembership(final ActorMember actorMember, final long roleId, final long groupId) {
        return actorMember.getRoleId() == roleId && actorMember.getGroupId() == groupId;
    }

    private boolean checkUser(final ActorMember actorMember, final long userId) {
        return actorMember.getUserId() == userId;
    }

    private boolean checkGroup(final ActorMember actorMember, final long groupId) {
        return actorMember.getGroupId() == groupId && actorMember.getRoleId() <= 0;
    }

    private boolean checkRole(final ActorMember actorMember, final long roleId) {
        return actorMember.getRoleId() == roleId && actorMember.getGroupId() <= 0;
    }

}
