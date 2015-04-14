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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorMappingImportException;
import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Matthieu Chaffotte
 */
public class ImportActorMappingIT extends TestWithTechnicalUser {

    private ProcessDefinition processDefinition;

    @Override
    @After
    public void after() throws Exception {
        try {
            getProcessAPI().disableProcess(processDefinition.getId());
        } catch (final ProcessActivationException e) {
            // Do nothing. Process already disabled
        }
        deleteProcess(processDefinition);
        getIdentityAPI().deleteOrganization();
        super.after();
    }

    @Test
    public void importSimpleActorMapping() throws Exception {
        final User john = createUser(USERNAME, PASSWORD);
        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("simpleActorMapping.xml");
        processDefinition = deployProcess(businessArchive.done());
        getProcessAPI().enableProcess(processDefinition.getId());

        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final ProcessInstance processInstance = getProcessAPI().startProcess(processDefinition.getId());
        waitForUserTask(processInstance, "userTask1");
        waitForPendingTasks(john.getId(), 1);

        final List<HumanTaskInstance> tasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, tasks.size());

        deleteUser(john);
    }

    @Test
    public void importComplexActorMapping() throws Exception {
        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("complexActorMapping.xml");
        final User john = createUser("john", "bpm");
        final Group rd = createGroup("RD");
        final Role role = createRole("dev");
        processDefinition = deployProcess(businessArchive.done());
        getProcessAPI().enableProcess(processDefinition.getId());

        getAndCheckActors(john, rd, role, processDefinition);
    }

    @Test
    public void importActorMappingWithWrongXMLFileBeforeDeploy() throws Exception {
        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("actorMappingWithException.xml");
        checkProcessNotActivated(businessArchive);
    }

    @Test(expected = ActorMappingImportException.class)
    public void importActorMappingWithWrongXMLFileAfterDeploy() throws Exception {
        final User user = createUser("john", "bpm");

        createProcessDefinitionAndCheckActorMappingImportException(user, "actorMappingWithException.xml");
    }

    @Test
    public void importActorMappingWithUnknownUserBeforeDeploy() throws Exception {
        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("simpleActorMapping.xml");
        checkProcessNotActivated(businessArchive);
    }

    @Test(expected = ActorMappingImportException.class)
    public void importActorMappingWithUnknownUserAfterDeploy() throws Exception {
        final User user = createUser("paul", "bpm");

        createProcessDefinitionAndCheckActorMappingImportException(user, "actorMappingWithException.xml");
    }

    @Test
    public void importActorMappingWithUnknownGroupBeforeDeploy() throws Exception {
        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("complexActorMapping.xml");
        createUser("john", "bpm");
        createGroup("RD1");
        createRole("dev");

        // even if missing group the process is resolved
        checkProcessActivated(businessArchive);
    }

    @Test(expected = ActorMappingImportException.class)
    public void importActorMappingWithUnknownGroupAfterDeploy() throws Exception {
        final User user = createUser("john", "bpm");
        createGroup("RD1");
        createRole("dev");

        createProcessDefinitionAndCheckActorMappingImportException(user, "complexActorMapping.xml");
    }

    @Test
    public void importActorMappingWithUnknownRoleBeforeDeploy() throws Exception {
        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("complexActorMapping.xml");
        createUser("john", "bpm");
        createGroup("RD");
        createRole("dev1");

        // even if missing role the process is resolved
        checkProcessActivated(businessArchive);
    }

    @Test(expected = ActorMappingImportException.class)
    public void importActorMappingWithUnknownRoleAfterDeploy() throws Exception {
        final User user = createUser("john", "bpm");
        createGroup("RD");
        createRole("dev1");

        createProcessDefinitionAndCheckActorMappingImportException(user, "complexActorMapping.xml");
    }

    @Test
    public void importActorMappingWithUnknownMemberShipBeforeDeploy() throws Exception {
        createUser("john", "bpm");
        createGroup("RD2");
        createRole("dev");
        final BusinessArchiveBuilder businessArchive = createAndDeployProcessDefinitionWithImportedActorMapping("complexActorMappingWithUnkownGroup.xml");

        // even if missing membership the process is resolved
        checkProcessActivated(businessArchive);
    }

    @Test(expected = ActorMappingImportException.class)
    public void importActorMappingWithUnknownMemberShipAfterDeploy() throws Exception {
        final User user = createUser("john", "bpm");
        createGroup("RD2");
        createRole("dev");

        createProcessDefinitionAndCheckActorMappingImportException(user, "complexActorMappingWithUnkownGroup.xml");
    }

    @Test
    public void importActorMapping() throws Exception {
        final User user = createUser("john", "bpm");
        final Group rd = createGroup("RD");
        final Role role = createRole("dev");

        final ProcessDefinitionBuilder processBuilder = createProcessDefinitionBuilder();
        processDefinition = deployAndEnableProcessWithActor(processBuilder.getProcess(), ACTOR_NAME, user);
        getProcessAPI().importActorMapping(processDefinition.getId(), xmlToByteArray("complexActorMapping2.xml"));

        getAndCheckActors(user, rd, role, processDefinition);
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-1881", keywords = { "process resolution" })
    @Test
    public void importActorMappingComputesProcessResolution() throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("resolve", "1.0").addActor("Leader", true).addUserTask("step1", "Leader");
        final DesignProcessDefinition processDesignDefinition = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDesignDefinition).done();
        processDefinition = deployProcess(businessArchive);
        ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        Assert.assertEquals(ConfigurationState.UNRESOLVED, deploymentInfo.getConfigurationState());

        // Let's resolve actor mapping by importing a valid file:
        final User user = createUser("john", "bpm");
        getProcessAPI()
                .importActorMapping(
                        processDefinition.getId(),
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><actormappings:actorMappings xmlns:actormappings=\"http://www.bonitasoft.org/ns/actormapping/6.0\"><actorMapping name=\"Leader\"><users><user>john</user></users></actorMapping></actormappings:actorMappings>"
                                .getBytes());

        deploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        Assert.assertEquals(ConfigurationState.RESOLVED, deploymentInfo.getConfigurationState());

        deleteUser(user);
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
        final InputStream xmlStream = ImportActorMappingIT.class.getResourceAsStream(xmlFileName);
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
     * @since 6.0
     */
    private ProcessDefinitionBuilder createProcessDefinitionBuilder() {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(ACTOR_NAME).addDescription("Delivery all day and night long").addUserTask("userTask1", ACTOR_NAME);
        return processBuilder;
    }

    private void checkProcessNotActivated(final BusinessArchiveBuilder businessArchive) throws Exception {
        processDefinition = deployProcess(businessArchive.done());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());
        final List<Problem> processResolutionProblems = getProcessAPI().getProcessResolutionProblems(processDefinition.getId());
        assertEquals(1, processResolutionProblems.size());
        final Problem problem = processResolutionProblems.get(0);
        assertEquals("actor", problem.getResource());
    }

    private void checkProcessActivated(final BusinessArchiveBuilder businessArchive) throws Exception {
        processDefinition = deployProcess(businessArchive.done());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());
    }

    /**
     * @param user
     * @param string
     * @throws Exception
     * @since 6.0
     */
    private void createProcessDefinitionAndCheckActorMappingImportException(final User user, final String xmlFileName) throws Exception {
        final ProcessDefinitionBuilder processBuilder = createProcessDefinitionBuilder();
        processDefinition = deployAndEnableProcessWithActor(processBuilder.getProcess(), ACTOR_NAME, user);

        getProcessAPI().importActorMapping(processDefinition.getId(), xmlToByteArray(xmlFileName));
    }

    /**
     * @param user
     * @param group
     * @param role
     * @param definition
     * @since 6.0
     */
    private void getAndCheckActors(final User user, final Group group, final Role role, final ProcessDefinition definition) {
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
