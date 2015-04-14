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
package org.bonitasoft.engine.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connector.Connector;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Assert;
import org.junit.Test;

public class ProcessResolutionIT extends TestWithTechnicalUser {

    @Cover(classes = { Problem.class, ProcessDefinition.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-531", keywords = { "process resolution" }, exceptions = { ProcessDefinitionNotFoundException.class })
    @Test(expected = ProcessDefinitionNotFoundException.class)
    public void processNotFoundWhenGettingResolutionProblems() throws BonitaException {
        getProcessAPI().getProcessResolutionProblems(-458);
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class, ActorInstance.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-531", keywords = { "process resolution" })
    @Test
    public void noActorMapping() throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("resolve", "1.0").addActor("Leader").addUserTask("step1", "Leader");
        final DesignProcessDefinition processDefinition = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done();
        final ProcessDefinition definition = deployProcess(businessArchive);
        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        Assert.assertEquals(ConfigurationState.UNRESOLVED, deploymentInfo.getConfigurationState());

        final List<Problem> problems = getProcessAPI().getProcessResolutionProblems(definition.getId());
        Assert.assertEquals(1, problems.size());
        final Problem problem = problems.get(0);
        Assert.assertEquals(Problem.Level.ERROR, problem.getLevel());
        Assert.assertEquals("actor", problem.getResource());
        Assert.assertNotNull(problem.getDescription());

        deleteProcess(definition.getId());
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class, ActorInstance.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-531", keywords = { "process resolution" })
    @Test
    public void resolveActorMapping() throws BonitaException, InterruptedException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("resolve", "1.0").addActor("Leader", true).addUserTask("step1", "Leader");
        final DesignProcessDefinition processDefinition = builder.done();
        final BusinessArchiveBuilder archiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        archiveBuilder.addClasspathResource(new BarResource("aDependency", new byte[] { 1, 5, 2, 3, 6, 4, 6, 8 }));
        final BusinessArchive businessArchive = archiveBuilder.setProcessDefinition(processDefinition).done();
        final ProcessDefinition definition = deployProcess(businessArchive);

        ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        long previousUpdate = deploymentInfo.getLastUpdateDate().getTime();
        long lastUpdate = previousUpdate;
        Assert.assertEquals(ConfigurationState.UNRESOLVED, deploymentInfo.getConfigurationState());

        // add actor mapping on user
        Thread.sleep(10);
        final ActorInstance initiator = getProcessAPI().getActorInitiator(definition.getId());
        getProcessAPI().addUserToActor(initiator.getId(), getSession().getUserId());

        // check state is resolved
        deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        Assert.assertEquals(ConfigurationState.RESOLVED, deploymentInfo.getConfigurationState());
        lastUpdate = deploymentInfo.getLastUpdateDate().getTime();
        assertTrue("lastupdate date not changed", lastUpdate > previousUpdate);
        previousUpdate = lastUpdate;
        List<Problem> problems = getProcessAPI().getProcessResolutionProblems(definition.getId());
        Assert.assertEquals(0, problems.size());

        Thread.sleep(10);
        // remove actor member
        final ActorInstance actorInstance = getProcessAPI().getActors(definition.getId(), 0, 10, ActorCriterion.NAME_ASC).get(0);
        final ActorMember actorMember = getProcessAPI().getActorMembers(actorInstance.getId(), 0, 10).get(0);
        getProcessAPI().removeActorMember(actorMember.getId());

        // check unresolved
        deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        Assert.assertEquals(ConfigurationState.UNRESOLVED, deploymentInfo.getConfigurationState());
        lastUpdate = deploymentInfo.getLastUpdateDate().getTime();
        assertTrue("lastupdate date not changed", lastUpdate > previousUpdate);
        previousUpdate = lastUpdate;

        Thread.sleep(10);
        // add user again to actor and check resolved again
        getProcessAPI().addUserToActor(actorInstance.getId(), getSession().getUserId());
        deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        Assert.assertEquals(ConfigurationState.RESOLVED, deploymentInfo.getConfigurationState());
        lastUpdate = deploymentInfo.getLastUpdateDate().getTime();
        assertTrue("lastupdate date not changed", lastUpdate > previousUpdate);
        previousUpdate = lastUpdate;
        problems = getProcessAPI().getProcessResolutionProblems(definition.getId());
        Assert.assertEquals(0, problems.size());

        deleteProcess(definition.getId());
    }

    @Cover(classes = { ProcessDefinition.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-988", keywords = { "dependency" })
    @Test
    public void deploy2ProcessWithSameDependency() throws BonitaException {
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("process", "1.0").addAutomaticTask("step1");
        final DesignProcessDefinition processDefinition1 = builder.done();
        final BusinessArchiveBuilder archiveBuilder1 = new BusinessArchiveBuilder().createNewBusinessArchive();
        final BarResource resource = new BarResource("aDependency", new byte[] { 1, 5, 2, 3, 6, 4, 6, 8 });
        archiveBuilder1.addClasspathResource(resource);
        final BusinessArchive businessArchive1 = archiveBuilder1.setProcessDefinition(processDefinition1).done();
        final ProcessDefinition definition1 = getProcessAPI().deploy(businessArchive1);

        builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("processbis", "1.0").addAutomaticTask("step1");
        final DesignProcessDefinition processDefinition2 = builder.done();
        final BusinessArchiveBuilder archiveBuilder2 = new BusinessArchiveBuilder().createNewBusinessArchive();
        archiveBuilder2.addClasspathResource(resource);
        final BusinessArchive businessArchive2 = archiveBuilder2.setProcessDefinition(processDefinition2).done();
        final ProcessDefinition definition2 = getProcessAPI().deploy(businessArchive2);
        deleteProcess(definition1.getId());
        deleteProcess(definition2.getId());
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class, ActorInstance.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-791", keywords = { "process resolution update" })
    @Test
    public void removeLastUserFromActorUnresolvesProcess() throws BonitaException {
        final User piouPiou = getIdentityAPI().createUser("Piou-piou", "s3cR3t");
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        final String actor = "Dev Leader";
        builder.createNewInstance("update proc resolution", "1.0").addActor(actor, true).addUserTask("test_session", actor);
        final DesignProcessDefinition processDefinition = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done();
        final ProcessDefinition definition = deployProcess(businessArchive);
        final ActorInstance initiator = getProcessAPI().getActorInitiator(definition.getId());
        getProcessAPI().addUserToActor(initiator.getId(), piouPiou.getId());
        ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());

        final List<ActorMember> actorMembers = getProcessAPI().getActorMembers(initiator.getId(), 0, 10);
        getProcessAPI().removeActorMember(actorMembers.get(0).getId());
        // reload the process deploy info:
        processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());

        deleteProcess(definition.getId());
        deleteUser(piouPiou);
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class, ActorInstance.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-791", keywords = { "process resolution update" })
    @Test
    public void deleteUserFromActorUnresolvesProcess() throws BonitaException {
        final User piouPiou = getIdentityAPI().createUser("Piou-piou", "s3cR3t");
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        final String actor = "Dev Leader";
        builder.createNewInstance("update proc resolution", "1.1").addActor(actor, true).addUserTask("deleteUser", actor);
        final DesignProcessDefinition processDefinition = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done();
        final ProcessDefinition definition = deployProcess(businessArchive);
        final ActorInstance initiator = getProcessAPI().getActorInitiator(definition.getId());
        getProcessAPI().addUserToActor(initiator.getId(), piouPiou.getId());
        ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());

        getIdentityAPI().deleteUser(piouPiou.getId());
        // reload the process deploy info:
        processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());

        deleteProcess(definition.getId());
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class, ActorInstance.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-791", keywords = { "process resolution update" })
    @Test
    public void deleteRoleFromActorUnresolvesProcess() throws BonitaException {
        final Role role = getIdentityAPI().createRole("Tester");
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        final String actor = "Dev Leader";
        builder.createNewInstance("update proc resolution", "1.2").addActor(actor, true).addUserTask("deleteRole", actor);
        final DesignProcessDefinition processDefinition = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done();
        final ProcessDefinition definition = deployProcess(businessArchive);
        final ActorInstance initiator = getProcessAPI().getActorInitiator(definition.getId());
        getProcessAPI().addRoleToActor(initiator.getId(), role.getId());
        ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());

        getIdentityAPI().deleteRole(role.getId());
        // reload the process deploy info:
        processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());

        deleteProcess(definition.getId());
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class, ActorInstance.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-791", keywords = { "process resolution update" })
    @Test
    public void deleteGroupFromActorUnresolvesProcess() throws BonitaException {
        final Group group = getIdentityAPI().createGroup("Tester", null);
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        final String actor = "Dev Leader";
        builder.createNewInstance("update proc resolution", "1.2").addActor(actor, true).addUserTask("deleteGroup", actor);
        final DesignProcessDefinition processDefinition = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done();
        final ProcessDefinition definition = deployProcess(businessArchive);
        final ActorInstance initiator = getProcessAPI().getActorInitiator(definition.getId());
        getProcessAPI().addGroupToActor(initiator.getId(), group.getId());
        ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.RESOLVED, processDeploymentInfo.getConfigurationState());

        getIdentityAPI().deleteGroup(group.getId());
        // reload the process deploy info:
        processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());

        deleteProcess(definition.getId());
    }

    @Cover(classes = { Problem.class, ProcessDefinition.class, Connector.class }, concept = BPMNConcept.PROCESS, jira = "ENGINE-531", keywords = { "process resolution" })
    @Test
    public void noConnectorImplementation() throws BonitaException {
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("resolve", "1.0").addAutomaticTask("auto").addConnector("exec", "exec-1.0", "1.0", ConnectorEvent.ON_ENTER);
        final DesignProcessDefinition processDefinition = builder.done();
        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processDefinition).done();
        final ProcessDefinition definition = deployProcess(businessArchive);
        final ProcessDeploymentInfo deploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        Assert.assertEquals(ConfigurationState.UNRESOLVED, deploymentInfo.getConfigurationState());

        final List<Problem> problems = getProcessAPI().getProcessResolutionProblems(definition.getId());
        Assert.assertEquals(1, problems.size());
        final Problem problem = problems.get(0);
        Assert.assertEquals(Problem.Level.ERROR, problem.getLevel());
        Assert.assertEquals("connector", problem.getResource());
        Assert.assertNotNull(problem.getDescription());

        deleteProcess(definition.getId());
    }

}
