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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.bar.BarResource;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.category.Category;
import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.TestConnector;
import org.bonitasoft.engine.connectors.TestConnectorThatThrowException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.filter.user.TestFilter;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.io.IOUtil;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.test.BuildTestUtil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class GetProcessDefinitionIT extends TestWithTechnicalUser {

    private List<ProcessDefinition> enabledProcessDefinitions;

    private ProcessDefinition processDefinition4;

    private List<User> users = null;

    private List<Category> categories = null;

    private List<Group> groups = null;

    private List<Role> roles = null;

    private List<UserMembership> userMemberships = null;

    @Cover(classes = { SearchOptionsBuilder.class, ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Search", "Category" }, jira = "")
    @Test
    public void getProcessDefinitionsUnrelatedToCategoryUserCanStart() throws Exception {
        beforeSearchProcessDefinitionsUserCanStart();

        // test differentFrom process with one category
        List<ProcessDeploymentInfo> result = getProcessAPI().getProcessDeploymentInfosUnrelatedToCategory(categories.get(2).getId(), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(7, result.size());
        assertEquals(enabledProcessDefinitions.get(0).getId(), result.get(0).getProcessId());
        assertEquals(enabledProcessDefinitions.get(1).getId(), result.get(1).getProcessId());
        assertEquals(enabledProcessDefinitions.get(2).getId(), result.get(2).getProcessId());
        assertEquals(enabledProcessDefinitions.get(3).getId(), result.get(3).getProcessId());
        assertEquals(enabledProcessDefinitions.get(4).getId(), result.get(4).getProcessId());
        assertEquals(enabledProcessDefinitions.get(5).getId(), result.get(5).getProcessId());
        assertEquals(enabledProcessDefinitions.get(6).getId(), result.get(6).getProcessId());

        // test differentFrom process with 2 categories, one of which is to extract
        result = getProcessAPI().getProcessDeploymentInfosUnrelatedToCategory(categories.get(0).getId(), 0, 10, ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(7, result.size());
        assertEquals(enabledProcessDefinitions.get(0).getId(), result.get(0).getProcessId());
        assertEquals(enabledProcessDefinitions.get(1).getId(), result.get(1).getProcessId());
        assertEquals(processDefinition4.getId(), result.get(2).getProcessId());
        assertEquals(enabledProcessDefinitions.get(3).getId(), result.get(3).getProcessId());
        assertEquals(enabledProcessDefinitions.get(4).getId(), result.get(4).getProcessId());
        assertEquals(enabledProcessDefinitions.get(5).getId(), result.get(5).getProcessId());
        assertEquals(enabledProcessDefinitions.get(6).getId(), result.get(6).getProcessId());

        afterSearchProcessDefinitionsUserCanStart();
    }

    private void beforeSearchProcessDefinitionsUserCanStart() throws BonitaException {
        // create users
        users = new ArrayList<User>(2);
        final User chico = createUser("chicobento", "bpm");
        final User cebolinha = createUser("cebolinha", "bpm");
        final User cascao = createUser("cascao", "bpm");
        final User magali = createUser("magali", "bpm");
        final User monica = createUser("monica", "bpm");
        final User dorinha = createUser("dorinha", "bpm");
        users.add(chico);
        users.add(cebolinha);
        users.add(cascao);
        users.add(magali);
        users.add(monica);
        users.add(dorinha);

        // create groups
        groups = new ArrayList<Group>(2);
        final Group group1 = createGroup("group1");
        groups.add(group1);
        final Group group2 = createGroup("group2");
        groups.add(group2);

        // create roles
        roles = new ArrayList<Role>(2);
        final Role role1 = createRole("role1");
        final Role role2 = createRole("role2");
        roles.add(role1);
        roles.add(role2);

        // create user memberships
        userMemberships = new ArrayList<UserMembership>(3);
        userMemberships.add(getIdentityAPI().addUserMembership(magali.getId(), group1.getId(), role1.getId()));
        userMemberships.add(getIdentityAPI().addUserMembership(monica.getId(), group1.getId(), role2.getId()));
        userMemberships.add(getIdentityAPI().addUserMembership(dorinha.getId(), group2.getId(), role1.getId()));

        // create processes
        createProcessesDefForSearchProcessUserCanStart();

        categories = new ArrayList<Category>(3);
        final Category category1 = getProcessAPI().createCategory("category1", "the first known category");
        final Category category2 = getProcessAPI().createCategory("category2", "the second known category");
        final Category category3 = getProcessAPI().createCategory("category3", "the third known category");
        categories.add(category1);
        categories.add(category2);
        categories.add(category3);
        getProcessAPI().addProcessDefinitionToCategory(category1.getId(), enabledProcessDefinitions.get(2).getId());
        getProcessAPI().addProcessDefinitionToCategory(category2.getId(), enabledProcessDefinitions.get(2).getId());
        getProcessAPI().addProcessDefinitionToCategory(category2.getId(), enabledProcessDefinitions.get(1).getId());
        getProcessAPI().addProcessDefinitionToCategory(category3.getId(), processDefinition4.getId());
    }

    private void createProcessesDefForSearchProcessUserCanStart() throws BonitaException {
        enabledProcessDefinitions = new ArrayList<ProcessDefinition>(4);
        final String actor1 = ACTOR_NAME;
        final DesignProcessDefinition designProcessDefinition1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process1", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor1, true);
        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(designProcessDefinition1, actor1, users.get(0));
        enabledProcessDefinitions.add(processDefinition1);

        // create process2
        final String actor2 = "Actor2";
        final DesignProcessDefinition designProcessDefinition2 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process2", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActor(designProcessDefinition2, actor2, users.get(1));
        enabledProcessDefinitions.add(processDefinition2);

        final DesignProcessDefinition designProcessDefinition3 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process3", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition3 = deployAndEnableProcessWithActor(designProcessDefinition3, actor2, users.get(1));
        enabledProcessDefinitions.add(processDefinition3);

        // process not enabled
        final DesignProcessDefinition designProcessDefinition4 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process4", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        processDefinition4 = getProcessAPI().deploy(designProcessDefinition4);
        getProcessAPI().addUserToActor(actor2, processDefinition4, users.get(1).getId());

        // process without actor initiator
        final DesignProcessDefinition designProcessDefinition5 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process5", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, false);
        final ProcessDefinition processDefinition5 = deployAndEnableProcessWithActor(designProcessDefinition5, actor2, users.get(2));
        enabledProcessDefinitions.add(processDefinition5);

        // actor initiator is a group
        final DesignProcessDefinition designProcessDefinition6 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process6", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition6 = deployAndEnableProcessWithActor(designProcessDefinition6, actor2, groups.get(0));
        enabledProcessDefinitions.add(processDefinition6);

        // actor initiator is a role
        final DesignProcessDefinition designProcessDefinition7 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process7", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition7 = deployAndEnableProcessWithActor(designProcessDefinition7, actor2, roles.get(0));
        enabledProcessDefinitions.add(processDefinition7);

        // actor initiator is a membership
        final DesignProcessDefinition designProcessDefinition8 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("My_Process8", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actor2, true);
        final ProcessDefinition processDefinition8 = deployAndEnableProcessWithActor(designProcessDefinition8, actor2, roles.get(0), groups.get(0));
        enabledProcessDefinitions.add(processDefinition8);
    }

    private void afterSearchProcessDefinitionsUserCanStart() throws BonitaException {
        disableAndDeleteProcess(enabledProcessDefinitions);
        deleteProcess(processDefinition4);
        deleteUserMemberships(userMemberships);
        deleteUsers(users);
        deleteCategories(categories);
        deleteGroups(groups);
        deleteRoles(roles);
    }

    @Test
    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "Actor", "User", "Process" }, jira = "ENGINE-575", story = "Tests the method returning the list of processes that a removal of the user passed in parameters would cause to pass in unresolved.")
    public void getProcessesWithActorOnlyForUser() throws Exception {
        final String actorName1 = "ITAccountCreator";
        final String actorName2 = "ITAccountValidator";
        final User user1 = createUser("any", "contrasena");
        final User user2 = createUser("bob", "smith");
        final DesignProcessDefinition design1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("MyProcess1", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actorName1, false);
        final ProcessDefinition pDef1 = deployAndEnableProcessWithActor(design1, actorName1, user1);
        final DesignProcessDefinition design2 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("MyProcess2", "1.1",
                Arrays.asList("mi_etapa"),
                Arrays.asList(true), actorName2, false);
        final ProcessDefinition pDef2 = deployAndEnableProcessWithActor(design2, actorName2, user1);

        final int startIndex = 0;
        final int maxResults = 10;
        List<ProcessDeploymentInfo> processes = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUser(user1.getId(), startIndex, maxResults,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(2, processes.size());
        assertEquals(processes.get(0).getProcessId(), pDef1.getId());
        assertEquals(processes.get(1).getProcessId(), pDef2.getId());
        // assertThat("Process definition Ids are not the expected ones", processes, idAre(pDef1.getId(), pDef2.getId()));

        final ActorInstance actorInstance = getActor(actorName2, pDef2);
        if (actorInstance != null) {
            // So that we have more than one user member of actorName2 in pDef2:
            getProcessAPI().addUserToActor(actorInstance.getId(), user2.getId());
        }
        processes = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUser(user1.getId(), startIndex, maxResults,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, processes.size());
        assertEquals(processes.get(0).getProcessId(), pDef1.getId());

        disableAndDeleteProcess(pDef1);
        disableAndDeleteProcess(pDef2);

        deleteUser(user1);
        deleteUser(user2);
    }

    @Test
    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "Actor", "User", "Process" }, jira = "ENGINE-576", story = "Tests the method returning the list of processes that a removal of the users passed in parameters would cause to pass in unresolved.")
    public void getProcessesWithActorOnlyForUsers() throws Exception {
        final String actorName1 = "ITAccountCreator";
        final String actorName2 = "ITAccountValidator";
        final User user1 = createUser("any", "contrasena");
        final User user2 = createUser("bob", "smith");
        final User user3 = createUser("mark", "sampaio");
        final DesignProcessDefinition design1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("MyProcess1", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actorName1, false);
        final ProcessDefinition pDef1 = deployAndEnableProcessWithActor(design1, actorName1, user1);
        final DesignProcessDefinition design2 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("MyProcess2", "1.1",
                Arrays.asList("mi_etapa"), Arrays.asList(true), actorName2, false);
        final ProcessDefinition pDef2 = deployAndEnableProcessWithActor(design2, actorName2, user2);

        List<ProcessDeploymentInfo> processes = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUsers(Arrays.asList(user1.getId(), user2.getId()), 0,
                10, ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(2, processes.size());
        assertEquals(processes.get(0).getProcessId(), pDef1.getId());
        assertEquals(processes.get(1).getProcessId(), pDef2.getId());
        // assertThat("Process definition Ids are not the expected ones", processes, idAre(pDef1.getId(), pDef2.getId()));

        final ActorInstance actorInstance = getActor(actorName2, pDef2);
        if (actorInstance != null) {
            // So that we have more than one user member of actorName2 in pDef2:
            getProcessAPI().addUserToActor(actorInstance.getId(), user3.getId());
        }
        final ActorInstance actorInstance2 = getActor(actorName1, pDef1);
        if (actorInstance2 != null) {
            // So that we have more than one user member of actorName1 in pDef1:
            getProcessAPI().addUserToActor(actorInstance2.getId(), user3.getId());
        }
        processes = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUsers(Arrays.asList(user1.getId(), user2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(0, processes.size());

        processes = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUsers(Arrays.asList(user1.getId(), user3.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, processes.size());
        assertEquals(pDef1.getId(), processes.get(0).getProcessId());

        processes = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUsers(Arrays.asList(user2.getId(), user3.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, processes.size());
        assertEquals(pDef2.getId(), processes.get(0).getProcessId());

        processes = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUser(user3.getId(), 0, 10, ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(0, processes.size());

        disableAndDeleteProcess(pDef1);
        disableAndDeleteProcess(pDef2);

        deleteUser(user1);
        deleteUser(user2);
        deleteUser(user3);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "Actor", "Role", "Process" }, jira = "ENGINE-566", story = "Tests the method returning the list of processes that a removal of the role passed in parameters would cause to pass in unresolved.")
    @Test
    public void getProcessesWithActorOnlyRole() throws Exception {
        final String actorName = "actor";

        // create user
        final User manu = createUser(USERNAME, PASSWORD);
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final Role roleToDelete = createRole("roleToDelete");
        final Role roleToKeep = createRole("roleToKeep");

        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("MyProcess1", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();

        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(designProcessDefinition, actorName, roleToDelete);

        final DesignProcessDefinition designProcessDefinition2 = new ProcessDefinitionBuilder().createNewInstance("MyProcess2", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();

        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActor(designProcessDefinition2, actorName, roleToDelete, roleToKeep);

        List<ProcessDeploymentInfo> procDepInfos = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForRole(roleToDelete.getId(), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, procDepInfos.size());
        final ProcessDeploymentInfo firstPDInfos = procDepInfos.get(0);
        assertEquals(processDefinition1.getName(), firstPDInfos.getName());
        assertEquals(processDefinition1.getId(), firstPDInfos.getProcessId());

        final ActorInstance actorInstance = getActor(actorName, processDefinition1);
        if (actorInstance != null) {
            // So that we have more than one user member of actorName in processDefinition1:
            getProcessAPI().addRoleToActor(actorInstance.getId(), roleToKeep.getId());
        }

        procDepInfos = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForRole(roleToDelete.getId(), 0, 10, ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(0, procDepInfos.size());

        deleteRoles(roleToKeep);
        deleteRoles(roleToDelete);
        deleteUser(manu);
        disableAndDeleteProcess(processDefinition1);
        disableAndDeleteProcess(processDefinition2);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "Actor", "Role", "Process" }, jira = "ENGINE-574", story = "Tests the method returning the list of processes that a removal of the roles passed in parameters would cause to pass in unresolved.")
    @Test
    public void getProcessesWithActorOnlyRoles() throws Exception {
        final String actorName = "actor";

        // create user
        final User manu = createUser(USERNAME, PASSWORD);
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final Role roleToDelete1 = createRole("roleToDelete1");
        final Role roleToKeep1 = createRole("roleToKeep1");
        final Role roleToDelete2 = createRole("roleToDelete2");
        final Role roleToKeep2 = createRole("roleToKeep2");

        final DesignProcessDefinition designProcessDefinition11 = new ProcessDefinitionBuilder().createNewInstance("MyProcess11", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();

        final ProcessDefinition processDefinition11 = deployAndEnableProcessWithActor(designProcessDefinition11, actorName, roleToDelete1);

        final DesignProcessDefinition designProcessDefinition12 = new ProcessDefinitionBuilder().createNewInstance("MyProcess12", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();

        final ProcessDefinition processDefinition12 = deployAndEnableProcessWithActor(designProcessDefinition12, actorName, roleToDelete1, roleToKeep1);

        final DesignProcessDefinition designProcessDefinition21 = new ProcessDefinitionBuilder().createNewInstance("MyProcess21", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();

        final ProcessDefinition processDefinition21 = deployAndEnableProcessWithActor(designProcessDefinition21, actorName, roleToDelete2);

        final DesignProcessDefinition designProcessDefinition22 = new ProcessDefinitionBuilder().createNewInstance("MyProcess22", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();

        final ProcessDefinition processDefinition22 = deployAndEnableProcessWithActor(designProcessDefinition22, actorName, roleToDelete2, roleToKeep2);

        List<ProcessDeploymentInfo> pDepInfos = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForRoles(
                Arrays.asList(roleToDelete1.getId(), roleToDelete2.getId()), 0, 10, ProcessDeploymentInfoCriterion.NAME_ASC);

        assertEquals(2, pDepInfos.size());
        ProcessDeploymentInfo pDepInfo1 = pDepInfos.get(0);
        final ProcessDeploymentInfo pDepInfo2 = pDepInfos.get(1);

        assertEquals(processDefinition11.getId(), pDepInfo1.getProcessId());
        assertEquals(processDefinition11.getName(), pDepInfo1.getName());
        assertEquals(processDefinition21.getId(), pDepInfo2.getProcessId());
        assertEquals(processDefinition21.getName(), pDepInfo2.getName());

        final ActorInstance actorInstance = getActor(actorName, processDefinition11);
        if (actorInstance != null) {
            // So that we have more than one user member of actorName in processDefinition11:
            getProcessAPI().addRoleToActor(actorInstance.getId(), roleToKeep1.getId());
        }

        pDepInfos = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForRoles(Arrays.asList(roleToDelete1.getId(), roleToDelete2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);

        assertEquals(1, pDepInfos.size());
        pDepInfo1 = pDepInfos.get(0);

        assertEquals(processDefinition21.getId(), pDepInfo1.getProcessId());
        assertEquals(processDefinition21.getName(), pDepInfo1.getName());

        deleteRoles(roleToKeep1, roleToDelete1, roleToKeep2, roleToDelete2);
        deleteUser(manu);
        disableAndDeleteProcess(processDefinition11, processDefinition12, processDefinition21, processDefinition22);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "Actor", "Group", "Process" }, jira = "ENGINE-565", story = "Tests the method returning the list of processes that a removal of the group passed in parameters would cause to pass in unresolved.")
    @Test
    public void getProcessesWithActorOnlyGroup() throws Exception {
        final String actorName = "actor";

        // create user
        final User manu = createUser(USERNAME, PASSWORD);
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final Group groupToDelete = createGroup("groupToDelete");
        final Group groupSonToDelete = createGroup("sonToDelete", "/groupToDelete");
        final Group groupToKeep = createGroup("groupToDeleteNot");

        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("MyProcess1", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();
        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(designProcessDefinition, actorName, groupToDelete);

        final DesignProcessDefinition designProcessDefinition2 = new ProcessDefinitionBuilder().createNewInstance("MyProcess2", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();
        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActor(designProcessDefinition2, actorName, groupToDelete, groupToKeep);

        final DesignProcessDefinition designProcessDefinition3 = new ProcessDefinitionBuilder().createNewInstance("MyProcess3", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();
        final ProcessDefinition processDefinition3 = deployAndEnableProcessWithActor(designProcessDefinition3, actorName, groupSonToDelete);

        List<ProcessDeploymentInfo> procDepInfos = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForGroup(groupToDelete.getId(), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(2, procDepInfos.size());

        ProcessDeploymentInfo firstPDInfos = procDepInfos.get(0);
        final ProcessDeploymentInfo secondPDInfos = procDepInfos.get(1);

        assertEquals(processDefinition1.getName(), firstPDInfos.getName());
        assertEquals(processDefinition1.getId(), firstPDInfos.getProcessId());

        assertEquals(processDefinition3.getName(), secondPDInfos.getName());
        assertEquals(processDefinition3.getId(), secondPDInfos.getProcessId());

        final ActorInstance actorInstance = getActor(actorName, processDefinition3);
        if (actorInstance != null) {
            // So that we have more than one user member of actorName in processDefinition3:
            getProcessAPI().addGroupToActor(actorInstance.getId(), groupToKeep.getId());
        }

        procDepInfos = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForGroup(groupToDelete.getId(), 0, 10, ProcessDeploymentInfoCriterion.NAME_ASC);

        assertEquals(1, procDepInfos.size());

        firstPDInfos = procDepInfos.get(0);
        assertEquals(processDefinition1.getName(), firstPDInfos.getName());
        assertEquals(processDefinition1.getId(), firstPDInfos.getProcessId());

        deleteGroups(groupToKeep);
        deleteGroups(groupToDelete);
        deleteUser(manu);
        disableAndDeleteProcess(processDefinition1);
        disableAndDeleteProcess(processDefinition2);
        disableAndDeleteProcess(processDefinition3);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "Actor", "Group", "Process" }, jira = "ENGINE-564", story = "Tests the method returning the list of processes that a removal of the groups passed in parameters would cause to pass in unresolved.")
    @Test
    public void getProcessesWithActorOnlyForGroups() throws Exception {
        final String actorName = "actor";

        // create user
        final User manu = createUser(USERNAME, PASSWORD);
        loginOnDefaultTenantWith(USERNAME, PASSWORD);
        final Group groupToDelete = createGroup("groupToDelete");
        final Group groupSonToDelete = createGroup("sonToDelete", "/groupToDeleteNot");
        final Group groupToKeep = createGroup("groupToDeleteNot");

        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance("MyProcess1", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();

        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(designProcessDefinition, actorName, groupToDelete);

        final DesignProcessDefinition designProcessDefinition2 = new ProcessDefinitionBuilder().createNewInstance("MyProcess2", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();
        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActor(designProcessDefinition2, actorName, groupToDelete, groupToKeep);

        final DesignProcessDefinition designProcessDefinition3 = new ProcessDefinitionBuilder().createNewInstance("MyProcess3", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();
        final ProcessDefinition processDefinition3 = deployAndEnableProcessWithActor(designProcessDefinition3, actorName, groupSonToDelete);

        List<ProcessDeploymentInfo> procDepInfos = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForGroups(
                Arrays.asList(groupSonToDelete.getId(), groupToDelete.getId()), 0, 10, ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(2, procDepInfos.size());

        final ProcessDeploymentInfo firstPDInfos = procDepInfos.get(0);
        final ProcessDeploymentInfo secondPDInfos = procDepInfos.get(1);

        assertEquals(processDefinition1.getName(), firstPDInfos.getName());
        assertEquals(processDefinition1.getId(), firstPDInfos.getProcessId());

        assertEquals(processDefinition3.getName(), secondPDInfos.getName());
        assertEquals(processDefinition3.getId(), secondPDInfos.getProcessId());

        final ActorInstance actorInstance = getActor(actorName, processDefinition1);
        if (actorInstance != null) {
            // So that we have more than one user member of actorName in processDefinition1:
            getProcessAPI().addGroupToActor(actorInstance.getId(), groupSonToDelete.getId());
        }

        procDepInfos = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForGroups(Arrays.asList(groupToKeep.getId(), groupToDelete.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);

        assertEquals(3, procDepInfos.size());

        deleteGroups(groupToKeep, groupToDelete);
        deleteUser(manu);
        disableAndDeleteProcess(processDefinition1, processDefinition2, processDefinition3);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "Actor", "Group", "Process" }, story = "Tests the cases were the 2roles-2users-2groups removed separately would'nt cause the process to pass in unresolved but would altoger. The list of prrocesses returned must reflect that.", jira = "")
    @Test
    public void getProcessesWithActorWithParticularCases() throws Exception {
        final String actorName = "actor";

        final Role roleToDelete1 = createRole("roleToDelete1");
        final Role roleToDelete2 = createRole("roleToDelete2");
        final Group groupToDelete1 = createGroup("groupToDelete1");
        final Group groupToDelete2 = createGroup("groupToDelete2");
        final User userToDelete1 = createUser("userToDelete1", "pwd");
        final User userToDelete2 = createUser("userToDelete2", "pwd");

        final DesignProcessDefinition designProcessDefinition1 = new ProcessDefinitionBuilder().createNewInstance("MyProcess1", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();
        final ProcessDefinition processDefinition1 = deployAndEnableProcessWithActor(designProcessDefinition1, actorName, roleToDelete1);

        final DesignProcessDefinition designProcessDefinition2 = new ProcessDefinitionBuilder().createNewInstance("MyProcess2", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();
        final ProcessDefinition processDefinition2 = deployAndEnableProcessWithActor(designProcessDefinition2, actorName, groupToDelete1);

        final DesignProcessDefinition designProcessDefinition3 = new ProcessDefinitionBuilder().createNewInstance("MyProcess3", "1.0").addActor(actorName)
                .addUserTask("userTask", actorName).getProcess();
        final ProcessDefinition processDefinition3 = deployAndEnableProcessWithActor(designProcessDefinition3, actorName, userToDelete1);

        List<ProcessDeploymentInfo> pDepInfos1 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForRoles(Arrays.asList(roleToDelete1.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, pDepInfos1.size());
        assertEquals(processDefinition1.getName(), pDepInfos1.get(0).getName());
        assertEquals(processDefinition1.getId(), pDepInfos1.get(0).getProcessId());
        pDepInfos1 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForRoles(Arrays.asList(roleToDelete2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(0, pDepInfos1.size());
        pDepInfos1 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForRoles(Arrays.asList(roleToDelete1.getId(), roleToDelete2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, pDepInfos1.size());

        List<ProcessDeploymentInfo> pDepInfos2 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForGroups(Arrays.asList(groupToDelete1.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, pDepInfos2.size());
        assertEquals(processDefinition2.getName(), pDepInfos2.get(0).getName());
        assertEquals(processDefinition2.getId(), pDepInfos2.get(0).getProcessId());
        pDepInfos2 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForGroups(Arrays.asList(groupToDelete2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(0, pDepInfos2.size());
        pDepInfos2 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForGroups(Arrays.asList(groupToDelete1.getId(), groupToDelete2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, pDepInfos2.size());

        List<ProcessDeploymentInfo> pDepInfos3 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUsers(Arrays.asList(userToDelete1.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, pDepInfos3.size());
        assertEquals(processDefinition3.getName(), pDepInfos3.get(0).getName());
        assertEquals(processDefinition3.getId(), pDepInfos3.get(0).getProcessId());
        pDepInfos3 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUsers(Arrays.asList(userToDelete2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(0, pDepInfos3.size());
        pDepInfos3 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUsers(Arrays.asList(userToDelete1.getId(), userToDelete2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, pDepInfos3.size());

        ActorInstance actorInstance = getActor(actorName, processDefinition1);
        if (actorInstance != null) {
            // So that we have more than one user member of actorName in processDefinition1:
            getProcessAPI().addRoleToActor(actorInstance.getId(), roleToDelete2.getId());
        }
        actorInstance = getActor(actorName, processDefinition2);
        if (actorInstance != null) {
            // So that we have more than one user member of actorName in processDefinition2:
            getProcessAPI().addGroupToActor(actorInstance.getId(), groupToDelete2.getId());
        }
        actorInstance = getActor(actorName, processDefinition3);
        if (actorInstance != null) {
            // So that we have more than one user member of actorName in processDefinition3:
            getProcessAPI().addUserToActor(actorInstance.getId(), userToDelete2.getId());
        }

        pDepInfos1 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForRoles(Arrays.asList(roleToDelete1.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(0, pDepInfos1.size());
        pDepInfos1 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForRoles(Arrays.asList(roleToDelete2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(0, pDepInfos1.size());
        pDepInfos1 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForRoles(Arrays.asList(roleToDelete1.getId(), roleToDelete2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, pDepInfos1.size());
        assertEquals(processDefinition1.getId(), pDepInfos1.get(0).getProcessId());

        pDepInfos1 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForGroups(Arrays.asList(groupToDelete1.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(0, pDepInfos1.size());
        pDepInfos1 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForGroups(Arrays.asList(groupToDelete2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(0, pDepInfos1.size());
        pDepInfos1 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForGroups(Arrays.asList(groupToDelete1.getId(), groupToDelete2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, pDepInfos1.size());
        assertEquals(processDefinition2.getId(), pDepInfos1.get(0).getProcessId());

        pDepInfos1 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUsers(Arrays.asList(userToDelete1.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(0, pDepInfos1.size());
        pDepInfos1 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUsers(Arrays.asList(userToDelete2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(0, pDepInfos1.size());
        pDepInfos1 = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUsers(Arrays.asList(userToDelete1.getId(), userToDelete2.getId()), 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, pDepInfos1.size());
        assertEquals(processDefinition3.getId(), pDepInfos1.get(0).getProcessId());

        deleteRoles(roleToDelete1, roleToDelete2);
        deleteGroups(groupToDelete1, groupToDelete2);
        deleteUsers(userToDelete1, userToDelete2);

        disableAndDeleteProcess(processDefinition1);
        disableAndDeleteProcess(processDefinition2);
        disableAndDeleteProcess(processDefinition3);
    }

    @Test
    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "Actor", "User", "Process" }, jira = "ENGINE-1375")
    public void getPaginatedProcessesWithActorOnlyForUser() throws Exception {
        final String actorName1 = "ITAccountCreator";
        final String actorName2 = "ITAccountValidator";
        final User user1 = createUser("any", "contrasena");
        final User user2 = createUser("bob", "smith");
        final DesignProcessDefinition design1 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("MyProcess1", "1.0",
                Arrays.asList("step1", "step2"), Arrays.asList(true, true), actorName1, false);
        final ProcessDefinition pDef1 = deployAndEnableProcessWithActor(design1, actorName1, user1);
        final DesignProcessDefinition design2 = BuildTestUtil.buildProcessDefinitionWithHumanAndAutomaticSteps("MyProcess2", "1.1",
                Arrays.asList("mi_etapa"), Arrays.asList(true), actorName2, false);
        final ProcessDefinition pDef2 = deployAndEnableProcessWithActor(design2, actorName2, user1);

        final int startIndex = 0;
        final int maxResults = 1;
        final List<ProcessDeploymentInfo> processes = getProcessAPI().getProcessDeploymentInfosWithActorOnlyForUser(user1.getId(), startIndex, maxResults,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, processes.size());
        assertEquals(processes.get(0).getProcessId(), pDef1.getId());

        disableAndDeleteProcess(pDef1);
        disableAndDeleteProcess(pDef2);

        deleteUser(user1);
        deleteUser(user2);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Get", "DesignProcessDefinition", "Existing" }, jira = "ENGINE-1817")
    @Test
    public void getExistingDesignProcessDefinition() throws Exception {
        final User user = createUser("any", "contrasena");

        final Expression targetProcessNameExpr = new ExpressionBuilder().createConstantStringExpression(PROCESS_NAME);
        final Expression targetProcessVersionExpr = new ExpressionBuilder().createConstantStringExpression(PROCESS_VERSION);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME, true);
        processBuilder.addActor("actor2");
        processBuilder.addAutomaticTask("AutomaticTask").addCallActivity("CallActivity", targetProcessNameExpr, targetProcessVersionExpr)
                .addManualTask("ManualTask", ACTOR_NAME)
                .addBoundaryEvent("BoundaryEvent").addSignalEventTrigger("signalName");
        processBuilder.addUserTask("UserTask", ACTOR_NAME).addUserFilter("test", "org.bonitasoft.engine.filter.user.testFilter", "1.0")
                .addInput("userId", new ExpressionBuilder().createConstantLongExpression(3));
        processBuilder.addConnector("testConnectorThatThrowException", "testConnectorThatThrowException", "1.0",
                ConnectorEvent.ON_ENTER);
        processBuilder.addDocumentDefinition("Doc").addUrl("plop");
        processBuilder.addGateway("Gateway", GatewayType.PARALLEL);
        processBuilder.addBlobData("BlobData", null).addDescription("blolbDescription").addBooleanData("BooleanData", null);
        processBuilder.addDisplayName("plop").addDisplayDescription("plop2").addEndEvent("EndEvent");
        processBuilder.addIntermediateCatchEvent("IntermediateCatchEvent").addIntermediateThrowEvent("IntermediateThrowEvent");
        processBuilder.addReceiveTask("ReceiveTask", "messageName");
        processBuilder.addSendTask("SendTask", "messageName", targetProcessNameExpr);
        processBuilder.addTransition("BoundaryEvent", "ManualTask");

        final DesignProcessDefinition designProcessDefinition = processBuilder.done();
        final ProcessDefinition processDefinition = deployProcessWithTestFilter(designProcessDefinition, user);
        assertNotNull(processDefinition);

        final DesignProcessDefinition resultDesignProcessDefinition = getProcessAPI().getDesignProcessDefinition(processDefinition.getId());
        assertEquals(designProcessDefinition, resultDesignProcessDefinition);

        disableAndDeleteProcess(processDefinition);
        deleteUser(user);
    }

    @Cover(classes = { ProcessAPI.class }, concept = BPMNConcept.PROCESS, keywords = { "Get", "DesignProcessDefinition", "Not Existing" }, jira = "ENGINE-1817")
    @Test(expected = ProcessDefinitionNotFoundException.class)
    public void getNotExistingDesignProcessDefinition() throws Exception {
        getProcessAPI().getDesignProcessDefinition(16548654L);
    }

    private ProcessDefinition deployProcessWithTestFilter(final DesignProcessDefinition designProcessDefinition, final User user)
            throws BonitaException, IOException {
        final BusinessArchiveBuilder businessArchiveBuilder = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(
                designProcessDefinition);
        final List<BarResource> impl = generateFilterImplementations("TestFilter");
        for (final BarResource barResource : impl) {
            businessArchiveBuilder.addUserFilters(barResource);
        }
        final List<BarResource> generateFilterDependencies = new ArrayList<BarResource>(1);
        final byte[] data = IOUtil.generateJar(TestFilter.class);
        generateFilterDependencies.add(new BarResource("TestFilter.jar", data));
        for (final BarResource barResource : generateFilterDependencies) {
            businessArchiveBuilder.addClasspathResource(barResource);
        }

        businessArchiveBuilder.addConnectorImplementation(getResource("/org/bonitasoft/engine/connectors/TestConnectorThatThrowException.impl",
                "TestConnectorThatThrowException.impl"));
        businessArchiveBuilder
                .addClasspathResource(BuildTestUtil
                        .generateJarAndBuildBarResource(TestConnectorThatThrowException.class, "TestConnectorThatThrowException.jar"));

        final ProcessDefinition processDefinition = deployProcess(businessArchiveBuilder.done());
        getProcessAPI().addUserToActor(ACTOR_NAME, processDefinition, user.getId());
        getProcessAPI().addUserToActor("actor2", processDefinition, user.getId());

        getProcessAPI().enableProcess(processDefinition.getId());
        return processDefinition;
    }

    private List<BarResource> generateFilterImplementations(final String filterName) throws IOException {
        final List<BarResource> resources = new ArrayList<BarResource>(1);
        final InputStream inputStream = TestConnector.class.getClassLoader().getResourceAsStream("org/bonitasoft/engine/filter/user/" + filterName + ".impl");
        final byte[] data = IOUtil.getAllContentFrom(inputStream);
        inputStream.close();
        resources.add(new BarResource("TestFilter.impl", data));
        return resources;
    }

}
