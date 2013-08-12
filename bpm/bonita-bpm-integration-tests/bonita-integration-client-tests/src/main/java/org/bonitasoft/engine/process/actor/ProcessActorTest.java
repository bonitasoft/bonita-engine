package org.bonitasoft.engine.process.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.actor.ActorCriterion;
import org.bonitasoft.engine.bpm.actor.ActorInstance;
import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.engine.bpm.actor.ActorUpdater;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveFactory;
import org.bonitasoft.engine.bpm.flownode.ActivityInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.Problem;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoCriterion;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.connectors.VariableStorage;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.test.WaitUntil;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.bonitasoft.engine.test.check.CheckNbPendingTaskOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProcessActorTest extends CommonAPITest {

    private static final String JOHN = "john";

    private User john;

    @After
    public void afterTest() throws BonitaException {
        deleteUser(JOHN);
        VariableStorage.clearAll();
        logout();
    }

    @Before
    public void beforeTest() throws BonitaException {
        login();
        john = createUser(JOHN, "bpm");
    }

    @Test(expected = AlreadyExistsException.class)
    public void mapTwiceSameActorToAGroup() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition definition = deployAndEnableProcessWithHumanTask("DeliveryProcess", delivery, "deliver");
        final ActorInstance actor = checkActors(delivery, definition);
        final Group ergo = createGroup("Ergonomists");
        try {
            getProcessAPI().addGroupToActor(actor.getId(), ergo.getId());
            getProcessAPI().addGroupToActor(actor.getId(), ergo.getId());
            fail("This statement should never be reached");
        } finally {
            deleteGroups(ergo);
            disableAndDeleteProcess(definition);
        }
    }

    @Test
    @Cover(classes = { Group.class, ActorMember.class, ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "ActorMappingService" }, jira = "ENGINE-952")
    public void mapActorToAlreadyMappedParentGroupShouldBeHandledSilently() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition definition = deployAndEnableProcessWithHumanTask("DeliveryProcess", delivery, "deliver");
        final ActorInstance actor = checkActors(delivery, definition);
        final Group all = createGroup("Everyone");
        final Group ergo = createGroup("Ergonomists", all.getPath());
        try {
            getProcessAPI().addGroupToActor(actor.getId(), ergo.getId());
            getProcessAPI().addGroupToActor(actor.getId(), all.getId());
            final List<ActorMember> actorMembers = getProcessAPI().getActorMembers(actor.getId(), 0, 50);
            int nbGroupActorMembers = 0;
            for (final ActorMember actorMember : actorMembers) {
                if (actorMember.getGroupId() != -1 && actorMember.getRoleId() == -1) {
                    nbGroupActorMembers++;
                }
            }
            assertEquals("All group / actor mapping should have been retrieved", 2, nbGroupActorMembers);
        } finally {
            deleteGroups(ergo);
            deleteGroups(all);
            disableAndDeleteProcess(definition);
        }
    }

    @Test
    public void mapActorToAGroupAndGroupPlusRoleIsValid() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition definition = deployAndEnableProcessWithHumanTask("DeliveryProcess", delivery, "deliver");
        final ActorInstance actor = checkActors(delivery, definition);
        final Role role = createRole("Quality Manager");
        final Group ergo = createGroup("Ergonomists");
        try {
            final ActorMember addGroupToActor = getProcessAPI().addGroupToActor(actor.getId(), ergo.getId());
            final ActorMember addRoleAndGroupToActor = getProcessAPI().addRoleAndGroupToActor(actor.getId(), role.getId(), ergo.getId());
            final List<ActorMember> actorMembers = getProcessAPI().getActorMembers(actor.getId(), 0, 50);
            assertTrue("All group / actor mapping should have been retrieved", actorMembers.containsAll(Arrays.asList(addGroupToActor, addRoleAndGroupToActor)));
        } finally {
            deleteGroups(ergo);
            deleteRoles(role);
            disableAndDeleteProcess(definition);
        }
    }

    @Test
    public void johnHasGotAPendingTask() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("DeliveryProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("deliver", delivery);
        final ProcessDefinition processDefinition = deployAndEnableWithActor(processBuilder.done(), delivery, john);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final List<ActorInstance> actors = getProcessAPI().getActors(processDefinition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);
        assertEquals(delivery, actor.getName());
        assertEquals("Delivery all day and night long", actor.getDescription());

        getProcessAPI().startProcess(processDefinition.getId());
        assertTrue("no new activity found", new WaitUntil(20, 1000) {

            @Override
            protected boolean check() throws Exception {
                return getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null).size() == 1;
            }
        }.waitUntil());

        final List<HumanTaskInstance> tasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, tasks.size());

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void deployProcessWithActorMappingNotMatchingOrganization() throws Exception {
        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("DeliveryProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("deliver", delivery);
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();
        final BusinessArchiveBuilder barBuilder = new BusinessArchiveBuilder().createNewBusinessArchive();
        barBuilder.setProcessDefinition(designProcessDefinition);
        final StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        builder.append("<actormapping:actorMappings xmlns:actormapping=\"http://www.bonitasoft.org/ns/actormapping/6.0\">");
        builder.append("\t<actorMapping name=\"Delivery men\">");
        builder.append("\t\t<groups>");
        builder.append("\t\t\t<group>/unknown</group>");
        builder.append("\t\t</groups>");
        builder.append("\t\t<users>");
        builder.append("\t\t\t<user>toto</user>");
        builder.append("\t\t</users>");
        builder.append("\t</actorMapping>");
        builder.append("</actormapping:actorMappings>");
        barBuilder.setActorMapping(builder.toString().getBytes());
        final BusinessArchive businessArchive = barBuilder.done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);

        // process deployed but not activated
        // trying to add an actor on the process now
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());
        final List<Problem> processResolutionProblems = getProcessAPI().getProcessResolutionProblems(processDefinition.getId());
        assertEquals(1, processResolutionProblems.size());
        final Problem problem = processResolutionProblems.get(0);
        assertEquals("actor", problem.getResource());
        final List<ActorInstance> actors = getProcessAPI().getActors(processDeploymentInfo.getProcessId(), 0, 50, ActorCriterion.NAME_ASC);
        final ActorInstance deliveryActor = actors.get(0);
        getProcessAPI().addUserToActor(deliveryActor.getId(), john.getId());
        getProcessAPI().enableProcess(processDeploymentInfo.getProcessId());
        assertEquals(ActivationState.ENABLED, getProcessAPI().getProcessDeploymentInfo(processDeploymentInfo.getProcessId()).getActivationState());

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void butJamesHasNoPendingTask() throws Exception {
        final String username = "james";
        final String password = "suomenlinna";
        final User user = createUser(username, password);

        final String delivery = "Delivery men";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("userTask1", delivery);
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(designProcessDefinition);

        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, delivery, john);
        getProcessAPI().startProcess(processDefinition.getId());
        Thread.sleep(1000);

        final List<HumanTaskInstance> tasks = getProcessAPI().getPendingHumanTaskInstances(user.getId(), 0, 10, null);
        assertEquals(0, tasks.size());

        // Clean up
        deleteUser(user);
        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void eliasHasAssignedAndPendingUserTasks() throws Exception {
        final String username = "elias";
        final String password = "suomenlinna";
        final User user = createUser(username, password);

        final String coders = "Coding men";

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(coders).addDescription("Coding all scrum-sprint-long").addUserTask("userTask1", coders).addUserTask("userTask2", coders)
                .addUserTask("userTask3", coders);
        final DesignProcessDefinition processDefinition = processBuilder.done();

        final ProcessDefinition definition = deployAndEnableWithActor(processDefinition, coders, user);

        final ProcessInstance startedProcess = getProcessAPI().startProcess(definition.getId());
        checkNbOfActivitiesInReadyState(startedProcess, 3);

        final List<ActivityInstance> activities = getProcessAPI().getActivities(startedProcess.getId(), 0, 10);
        // assign first 2 user tasks to user:
        getProcessAPI().assignUserTask(activities.get(0).getId(), user.getId());
        getProcessAPI().assignUserTask(activities.get(1).getId(), user.getId());

        final long myAssignedTasksNb = getProcessAPI().getNumberOfAssignedHumanTaskInstances(user.getId());
        assertEquals(2L, myAssignedTasksNb);
        // there should remain 1 usertask not assigned:
        final long actorPendingTasksNb = getProcessAPI().getNumberOfPendingHumanTaskInstances(user.getId());
        assertEquals(1L, actorPendingTasksNb);

        disableAndDeleteProcess(definition);
        deleteUser(user);
    }

    private ProcessDefinition preparationBeforeTest(final String delivery) throws Exception {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").setActorInitiator(delivery).addUserTask("userTask1", delivery);
        final DesignProcessDefinition processDefinition = processBuilder.done();
        final BusinessArchiveBuilder businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive();
        businessArchive.setProcessDefinition(processDefinition);
        final File file = File.createTempFile("Actor", ".bar");
        try {
            file.delete();
            BusinessArchiveFactory.writeBusinessArchiveToFile(businessArchive.done(), file);
            final BusinessArchive archive = BusinessArchiveFactory.readBusinessArchive(file);
            return deployAndEnableWithActor(archive.getProcessDefinition(), delivery, john);
        } finally {
            file.delete();
        }
    }

    @Test
    @Cover(classes = { ActorInstance.class, ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "Actor", "Description" }, jira = "ENGINE-1065")
    public void getActor() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition definition = preparationBeforeTest(delivery);

        // Get the existing process def actor list:
        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        final ActorInstance actor = actors.get(0);
        assertEquals(delivery, actor.getName());
        assertEquals(definition.getId(), actor.getProcessDefinitionId());
        assertEquals("Delivery all day and night long", actor.getDescription());

        disableAndDeleteProcess(definition);
    }

    @Test
    public void getActors() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);

        final List<String> actorNameList = initActorAndDescription(processBuilder, 5);

        final ProcessDefinition processDefinition = deployAndEnableWithActor(processBuilder.getProcess(), actorNameList,
                Arrays.asList(user, user, user, user, user));

        // test ASC
        List<ActorInstance> actors = getProcessAPI().getActors(processDefinition.getId(), 0, 2, ActorCriterion.NAME_ASC);
        assertNotNull(actors);
        assertEquals(2, actors.size());
        assertEquals(actorNameList.get(0), actors.get(0).getName());
        assertEquals(actorNameList.get(1), actors.get(1).getName());
        actors = getProcessAPI().getActors(processDefinition.getId(), 2, 2, ActorCriterion.NAME_ASC);
        assertNotNull(actors);
        assertEquals(2, actors.size());
        assertEquals(actorNameList.get(2), actors.get(0).getName());
        assertEquals(actorNameList.get(3), actors.get(1).getName());
        actors = getProcessAPI().getActors(processDefinition.getId(), 4, 2, ActorCriterion.NAME_ASC);
        assertNotNull(actors);
        assertEquals(1, actors.size());
        assertEquals(actorNameList.get(4), actors.get(0).getName());

        // test PageOutOfRangeException
        actors = getProcessAPI().getActors(processDefinition.getId(), 6, 2, ActorCriterion.NAME_ASC);
        assertEquals(0, actors.size());

        // test DESC
        actors = getProcessAPI().getActors(processDefinition.getId(), 0, 2, ActorCriterion.NAME_DESC);
        assertNotNull(actors);
        assertEquals(2, actors.size());
        assertEquals(actorNameList.get(4), actors.get(0).getName());
        assertEquals(actorNameList.get(3), actors.get(1).getName());

        disableAndDeleteProcess(processDefinition);
        deleteUser(user.getId());
    }

    @Test
    public void getActorsByIds() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription("actor description1");
        processBuilder.addActor("actor2").addDescription("actor description2");
        final ProcessDefinition processDefinition = deployAndEnableWithActor(processBuilder.getProcess(), Arrays.asList(ACTOR_NAME, "actor2"),
                Arrays.asList(user, user));

        final List<ActorInstance> actors = getProcessAPI().getActors(processDefinition.getId(), 0, 5, null);
        assertNotNull(actors);
        assertEquals(2, actors.size());
        final List<Long> actorIds = new ArrayList<Long>();
        for (final ActorInstance actorInstance : actors) {
            actorIds.add(actorInstance.getId());
        }

        final Map<Long, ActorInstance> actorInstanceRes = getProcessAPI().getActorsFromActorIds(actorIds);
        assertNotNull(actorInstanceRes);
        assertEquals(2, actorInstanceRes.size());

        boolean isHas1 = false;
        boolean isHas2 = false;
        for (final Entry<Long, ActorInstance> et : actorInstanceRes.entrySet()) {
            if (ACTOR_NAME.equals(et.getValue().getName())) {
                isHas1 = true;
            } else {
                if ("actor2".equals(et.getValue().getName())) {
                    isHas2 = true;
                }
            }
        }
        assertTrue(isHas1 && isHas2);
        disableAndDeleteProcess(processDefinition);
        deleteUser(user.getId());
    }

    @Test
    public void updateActor() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition definition = preparationBeforeTest(delivery);

        // Get the existing process def actor list:
        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        final ActorInstance actor = actors.get(0);
        final long actorId = actor.getId();
        final ActorInstance actorRes = getProcessAPI().getActor(actorId);
        assertEquals(actorId, actorRes.getId());
        assertEquals(delivery, actorRes.getName());
        assertEquals(definition.getId(), actorRes.getProcessDefinitionId());

        final String changedDescription = "It's okay!";
        final String changedName = "Delivery women";
        final ActorUpdater descriptor = new ActorUpdater();
        descriptor.setDescription(changedDescription);
        descriptor.setDisplayName(changedName);
        final ActorInstance actorUpdated = getProcessAPI().updateActor(actorId, descriptor);

        assertEquals(actorId, actorUpdated.getId());
        assertEquals(delivery, actorUpdated.getName());
        assertEquals(changedDescription, actorUpdated.getDescription());
        assertEquals(changedName, actorUpdated.getDisplayName());
        assertEquals(definition.getId(), actorUpdated.getProcessDefinitionId());

        disableAndDeleteProcess(definition);
    }

    @Test(expected = UpdateException.class)
    public void updateActorWithEmptyDescriptor() throws Exception {
        final ActorUpdater descriptor = new ActorUpdater();
        getProcessAPI().updateActor(1l, descriptor);
    }

    @Test(expected = UpdateException.class)
    public void updateActorWithNoDescriptor() throws Exception {
        getProcessAPI().updateActor(1l, null);
    }

    @Test
    public void addActorMembers() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("userTask1", delivery);
        final DesignProcessDefinition processDefinition = processBuilder.done();

        final ProcessDefinition definition = deployAndEnableWithActor(processDefinition, delivery, john);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);
        assertEquals(delivery, actor.getName());

        assertEquals(1l, getProcessAPI().getNumberOfActorMembers(actor.getId()));

        final List<ActorMember> actorMembers = getProcessAPI().getActorMembers(actor.getId(), 0, 10);
        assertEquals(1, actorMembers.size());
        final ActorMember actorMember = actorMembers.get(0);
        assertEquals(john.getId(), actorMember.getUserId());
        assertEquals(-1, actorMember.getGroupId());
        assertEquals(-1, actorMember.getRoleId());

        getProcessAPI().removeActorMember(actorMember.getId());
        disableAndDeleteProcess(definition);
    }

    @Cover(classes = { User.class, ActorInstance.class, ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "Number", "Users", "Actor" }, jira = "ENGINE-681")
    @Test
    public void getNumberOfUsersOfActor() throws Exception {
        final String delivery = "Delivery men";
        final User user1 = createUser("user1", "bpm");
        final User user2 = createUser("user2", "bpm");
        final User user3 = createUser("user3", "bpm");

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("userTask1", delivery);
        final ProcessDefinition definition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processBuilder.done()).done());
        addMappingOfActorsForUser(delivery, user1.getId(), definition);
        addMappingOfActorsForUser(delivery, user2.getId(), definition);
        getProcessAPI().enableProcess(definition.getId());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());

        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // Retrieve actor
        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);
        assertEquals(delivery, actor.getName());

        // Check number of users mapped to actor
        final long result = getProcessAPI().getNumberOfUsersOfActor(actor.getId());
        assertEquals(2, result);

        // Retrieve actor members to clean database
        final List<ActorMember> actorMembers = getProcessAPI().getActorMembers(actor.getId(), 0, 10);
        getProcessAPI().removeActorMember(actorMembers.get(0).getId());
        getProcessAPI().removeActorMember(actorMembers.get(1).getId());
        disableAndDeleteProcess(definition);
        deleteUser(user1);
        deleteUser(user2);
        deleteUser(user3);
    }

    @Cover(classes = { Role.class, ActorInstance.class, ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "Number", "Roles", "Actor" }, jira = "ENGINE-683")
    @Test
    public void getNumberOfRolesOfActor() throws Exception {
        final String delivery = "Delivery men";
        final Role role1 = createRole("role1");
        final Role role2 = createRole("role2");
        final Role role3 = createRole("role3");

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("roleTask1", delivery);
        final ProcessDefinition definition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processBuilder.done()).done());
        addMappingOfActorsForRole(delivery, role1.getId(), definition);
        addMappingOfActorsForRole(delivery, role2.getId(), definition);
        getProcessAPI().enableProcess(definition.getId());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());

        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // Retrieve actor
        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);
        assertEquals(delivery, actor.getName());

        // Check number of roles mapped to actor
        final long result = getProcessAPI().getNumberOfRolesOfActor(actor.getId());
        assertEquals(2, result);

        // Retrieve actor members to clean database
        final List<ActorMember> actorMembers = getProcessAPI().getActorMembers(actor.getId(), 0, 10);
        getProcessAPI().removeActorMember(actorMembers.get(0).getId());
        getProcessAPI().removeActorMember(actorMembers.get(1).getId());
        disableAndDeleteProcess(definition);
        deleteRoles(role1, role2, role3);
    }

    @Cover(classes = { Group.class, ActorInstance.class, ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = { "Number", "Groups", "Actor" }, jira = "ENGINE-682")
    @Test
    public void getNumberOfGroupsOfActor() throws Exception {
        final String delivery = "Delivery men";
        final Group group1 = createGroup("group1");
        final Group group2 = createGroup("group2");
        final Group group3 = createGroup("group3");

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("groupTask1", delivery);
        final ProcessDefinition definition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processBuilder.done()).done());
        addMappingOfActorsForGroup(delivery, group1.getId(), definition);
        addMappingOfActorsForGroup(delivery, group2.getId(), definition);
        getProcessAPI().enableProcess(definition.getId());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());

        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // Retrieve actor
        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);
        assertEquals(delivery, actor.getName());

        // Check number of groups mapped to actor
        final long result = getProcessAPI().getNumberOfGroupsOfActor(actor.getId());
        assertEquals(2, result);

        // Retrieve actor members to clean database
        final List<ActorMember> actorMembers = getProcessAPI().getActorMembers(actor.getId(), 0, 10);
        getProcessAPI().removeActorMember(actorMembers.get(0).getId());
        getProcessAPI().removeActorMember(actorMembers.get(1).getId());
        disableAndDeleteProcess(definition);
        deleteGroups(group1, group2, group3);
    }

    @Cover(classes = { UserMembership.class, User.class, Group.class, Role.class, ActorInstance.class, ProcessAPI.class }, concept = BPMNConcept.ACTOR, keywords = {
            "Number", "Group", "Role", "Actor" }, jira = "ENGINE-684")
    @Test
    public void getNumberOfMembershipsOfActor() throws Exception {
        final String delivery = "Delivery men";
        final User user1 = createUser("user1", "bpm");
        final User user2 = createUser("user2", "bpm");
        final Group group1 = createGroup("group1");
        final Group group2 = createGroup("group2");
        final Role role1 = createRole("role1");
        final Role role2 = createRole("role2");

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(delivery).addDescription("Delivery all day and night long").addUserTask("userMembershipTask1", delivery);
        final ProcessDefinition definition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(processBuilder.done()).done());
        addMappingOfActorsForUser(delivery, user1.getId(), definition);
        addMappingOfActorsForRoleAndGroup(delivery, user1.getId(), group1.getId(), definition);
        addMappingOfActorsForRoleAndGroup(delivery, user1.getId(), group2.getId(), definition);
        getProcessAPI().enableProcess(definition.getId());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());

        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        // Retrieve actor
        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);
        assertEquals(delivery, actor.getName());

        // Check number of userMemberships mapped to actor
        final long result = getProcessAPI().getNumberOfMembershipsOfActor(actor.getId());
        assertEquals(2, result);

        // Retrieve actor members to clean database
        final List<ActorMember> actorMembers = getProcessAPI().getActorMembers(actor.getId(), 0, 10);
        getProcessAPI().removeActorMember(actorMembers.get(0).getId());
        getProcessAPI().removeActorMember(actorMembers.get(1).getId());
        disableAndDeleteProcess(definition);
        deleteUsers(user1, user2);
        deleteRoles(role1, role2);
        deleteGroups(group1, group2);
    }

    @Test
    public void getStartableProcessesForActors() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(delivery);
        processBuilder.setActorInitiator(delivery).addDescription("Delivery all day and night long").addUserTask("userTask1", delivery);
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, delivery, john);

        final ActorInstance actorInstance = getProcessAPI().getActorInitiator(processDefinition.getId());
        final Set<Long> actorIds = new HashSet<Long>();
        actorIds.add(actorInstance.getId());

        final List<ProcessDeploymentInfo> processDeploymentInfos = getProcessAPI().getStartableProcessDeploymentInfosForActors(actorIds, 0, 10,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, processDeploymentInfos.size());
        assertEquals(processDefinition.getId(), processDeploymentInfos.get(0).getProcessId());

        disableAndDeleteProcess(processDefinition);
    }

    @Test
    public void testIsAllowedToStartProcess() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance("firstProcess", "1.0");
        processBuilder.addActor(delivery);
        processBuilder.setActorInitiator(delivery).addDescription("Delivery all day and night long").addUserTask("userTask1", delivery);
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();
        final ProcessDefinition processDefinition = deployAndEnableWithActor(designProcessDefinition, delivery, john);

        final ActorInstance actorInstance = getProcessAPI().getActorInitiator(processDefinition.getId());
        final Set<Long> actorIds = new HashSet<Long>();
        actorIds.add(actorInstance.getId());

        final boolean isAllowedToStartProcess = getProcessAPI().isAllowedToStartProcess(processDefinition.getId(), actorIds);
        assertEquals(true, isAllowedToStartProcess);

        disableAndDeleteProcess(processDefinition);
    }

    private ProcessDefinition deployAndEnableProcessWithHumanTask(final String processName, final String actorName, final String userTaskName)
            throws BonitaException {
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance(processName, "1.0");
        processBuilder.addActor(actorName).addDescription("Delivery all day and night long");
        processBuilder.addStartEvent("startEvent");
        processBuilder.addUserTask(userTaskName, actorName);
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition("startEvent", userTaskName);
        processBuilder.addTransition(userTaskName, "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final ProcessDefinition processDef = deployAndEnableWithActor(designProcessDefinition, actorName, john);
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDef.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        return processDef;
    }

    @Test
    public void userHasGotAPendingTaskFromGroup() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition definition = deployAndEnableProcessWithHumanTask("DeliveryProcess", delivery, "deliver");
        final ActorInstance actor = checkActors(delivery, definition);

        final Group group = getIdentityAPI().createGroup("group1", null);
        final Role role = getIdentityAPI().createRole("role1");
        getIdentityAPI().addUserMembership(john.getId(), group.getId(), role.getId());

        getProcessAPI().addGroupToActor(actor.getId(), group.getId());

        getProcessAPI().startProcess(definition.getId());
        assertTrue("no new activity found", new CheckNbPendingTaskOf(getProcessAPI(), 20, 1500, false, 1, john).waitUntil());

        final List<HumanTaskInstance> tasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, tasks.size());

        cleanUserGroupAndRole(group, role);
        disableAndDeleteProcess(definition);
    }

    @Test
    public void mapActorToASubGroup() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition definition = deployAndEnableProcessWithHumanTask("DeliveryProcess", delivery, "deliver");
        final ActorInstance actor = checkActors(delivery, definition);

        final Group parent = createGroup("parent");
        final Group sub = createGroup("sub", "/parent");
        final Role role = getIdentityAPI().createRole("role1");
        getIdentityAPI().addUserMembership(john.getId(), sub.getId(), role.getId());

        getProcessAPI().addGroupToActor(actor.getId(), parent.getId());

        getProcessAPI().startProcess(definition.getId());
        waitForPendingTasks(john.getId(), 1);// should have 1 task because john is in the parent
        deleteGroups(parent);
        deleteRoles(role);
        disableAndDeleteProcess(definition);
    }

    @Test
    public void userHasGotAPendingTaskFromRole() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition definition = deployAndEnableProcessWithHumanTask("DeliveryProcess", delivery, "deliver");

        final ActorInstance actor = checkActors(delivery, definition);

        final Group group = getIdentityAPI().createGroup("group1", null);
        final Role role = getIdentityAPI().createRole("role1");
        getIdentityAPI().addUserMembership(john.getId(), group.getId(), role.getId());

        getProcessAPI().addRoleToActor(actor.getId(), role.getId());

        final long processInstanceId = getProcessAPI().startProcess(definition.getId()).getId();
        assertNotNull(waitForUserTask("deliver", processInstanceId));
        assertTrue("no new activity found", new CheckNbPendingTaskOf(getProcessAPI(), 20, 1500, false, 1, john).waitUntil());

        final List<HumanTaskInstance> tasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, tasks.size());

        cleanUserGroupAndRole(group, role);
        disableAndDeleteProcess(definition);
    }

    @Test
    public void userHasGotAPendingTaskFromRoleAndGroup() throws Exception {
        final String delivery = "Delivery men";
        final ProcessDefinition definition = deployAndEnableProcessWithHumanTask("DeliveryProcess", delivery, "deliver");

        final ActorInstance actor = checkActors(delivery, definition);

        final Group group = getIdentityAPI().createGroup("group1", null);
        final Role role = getIdentityAPI().createRole("role1");
        getIdentityAPI().addUserMembership(john.getId(), group.getId(), role.getId());

        getProcessAPI().addRoleAndGroupToActor(actor.getId(), role.getId(), group.getId());

        getProcessAPI().startProcess(definition.getId());
        assertTrue("no new activity found", new CheckNbPendingTaskOf(getProcessAPI(), 20, 1500, false, 1, john).waitUntil());

        final List<HumanTaskInstance> tasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(1, tasks.size());

        cleanUserGroupAndRole(group, role);
        disableAndDeleteProcess(definition);
    }

    @Test
    public void userDontGetAPendingTaskFromRoleOrGroupIfRoleAndGroupNeeded() throws Exception {
        final String actorName = "Delivery men";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder();
        processBuilder.createNewInstance("DeliveryProcess", "1.0");
        processBuilder.addActor(actorName).addDescription("Delivery all day and night long");
        processBuilder.addStartEvent("startEvent");
        processBuilder.addUserTask("deliver", actorName);
        processBuilder.addEndEvent("endEvent");
        processBuilder.addTransition("startEvent", "deliver");
        processBuilder.addTransition("deliver", "endEvent");
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();

        final ProcessDefinition definition = getProcessAPI().deploy(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
        final ActorInstance actor = checkActors(actorName, definition);

        final Group group1 = getIdentityAPI().createGroup("group1", null);
        final Group group2 = getIdentityAPI().createGroup("group2", null);
        final Role role1 = getIdentityAPI().createRole("role1");
        final Role role2 = getIdentityAPI().createRole("role2");
        getIdentityAPI().addUserMembership(john.getId(), group1.getId(), role1.getId());

        getProcessAPI().addRoleAndGroupToActor(actor.getId(), role1.getId(), group2.getId());
        getProcessAPI().addRoleAndGroupToActor(actor.getId(), role2.getId(), group1.getId());

        getProcessAPI().enableProcess(definition.getId());
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(definition.getId());
        assertEquals(ActivationState.ENABLED, processDeploymentInfo.getActivationState());

        final ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTask("deliver", processInstance);

        final List<HumanTaskInstance> tasks = getProcessAPI().getPendingHumanTaskInstances(john.getId(), 0, 10, null);
        assertEquals(0, tasks.size());

        cleanUserGroupAndRole(group1, role1);
        getIdentityAPI().deleteGroup(group2.getId());
        getIdentityAPI().deleteRole(role2.getId());
        disableAndDeleteProcess(definition);
    }

    @Test
    public void getNumberOfActors() throws Exception {
        final User user = createUser(USERNAME, PASSWORD);

        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(PROCESS_NAME, PROCESS_VERSION);
        processBuilder.addActor(ACTOR_NAME).addDescription(DESCRIPTION);
        processBuilder.addActor("Actor2").addDescription("working all-day-long");
        processBuilder.addActor("initiator", true);

        // Process def : one starting automatic activity that fires 3 human activities:
        final DesignProcessDefinition designProcessDefinition = processBuilder.addAutomaticTask("step1").addUserTask("step2", ACTOR_NAME)
                .addUserTask("step3", ACTOR_NAME).addUserTask("step4", ACTOR_NAME).addTransition("step1", "step2").addTransition("step1", "step3")
                .addTransition("step1", "step4").getProcess();

        final BusinessArchive businessArchive = new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done();
        final ProcessDefinition processDefinition = getProcessAPI().deploy(businessArchive);
        int nbOfActors = getProcessAPI().getNumberOfActors(processDefinition.getId());
        assertEquals(3, nbOfActors);

        final List<ActorInstance> actors = getProcessAPI().getActors(processDefinition.getId(), 0, 5, ActorCriterion.NAME_ASC);
        assertEquals(3, actors.size());
        for (final ActorInstance actor : actors) {
            getProcessAPI().addUserToActor(actor.getId(), user.getId());
        }

        getProcessAPI().enableProcess(processDefinition.getId());
        final ProcessInstance procInstance = getProcessAPI().startProcess(processDefinition.getId());
        nbOfActors = getProcessAPI().getNumberOfActors(processDefinition.getId());
        assertEquals(3, nbOfActors);
        checkNbOfOpenActivities(procInstance, 3);

        nbOfActors = getProcessAPI().getNumberOfActors(processDefinition.getId());
        assertEquals(3, nbOfActors);

        // clean all data for test
        disableAndDeleteProcess(processDefinition);
        deleteUser(user.getId());
    }

    private ActorInstance checkActors(final String delivery, final ProcessDefinition definition) {
        final List<ActorInstance> actors = getProcessAPI().getActors(definition.getId(), 0, 1, ActorCriterion.NAME_ASC);
        assertEquals(1, actors.size());
        final ActorInstance actor = actors.get(0);
        assertEquals(delivery, actor.getName());
        return actor;
    }

    private void cleanUserGroupAndRole(final Group group, final Role role) throws DeletionException {
        getIdentityAPI().deleteGroup(group.getId());
        getIdentityAPI().deleteRole(role.getId());
    }

    @Cover(classes = { ProcessDeploymentInfo.class }, concept = BPMNConcept.PROCESS, keywords = { "Pagination", "process definition" }, jira = "ENGINE-1375")
    @Test
    public void getPaginatedStartableProcessesForActors() throws Exception {
        final ProcessDefinition firstDefinition = getProcessDefinition("firstProcess");
        final ProcessDefinition secondDefinition = getProcessDefinition("secondProcess");

        final ActorInstance firstActorInstance = getProcessAPI().getActorInitiator(firstDefinition.getId());
        final ActorInstance secondActorInstance = getProcessAPI().getActorInitiator(secondDefinition.getId());
        final Set<Long> actorIds = new HashSet<Long>();
        actorIds.add(firstActorInstance.getId());
        actorIds.add(secondActorInstance.getId());

        final List<ProcessDeploymentInfo> processDeploymentInfos = getProcessAPI().getStartableProcessDeploymentInfosForActors(actorIds, 0, 1,
                ProcessDeploymentInfoCriterion.NAME_ASC);
        assertEquals(1, processDeploymentInfos.size());
        assertEquals(firstDefinition.getId(), processDeploymentInfos.get(0).getProcessId());

        disableAndDeleteProcess(firstDefinition);
        disableAndDeleteProcess(secondDefinition);
    }

    private ProcessDefinition getProcessDefinition(final String processName) throws BonitaException {
        final String delivery = "Delivery men";
        final ProcessDefinitionBuilder processBuilder = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0");
        processBuilder.addActor(delivery);
        processBuilder.setActorInitiator(delivery).addDescription("Delivery all day and night long").addUserTask("userTask1", delivery);
        final DesignProcessDefinition designProcessDefinition = processBuilder.done();
        return deployAndEnableWithActor(designProcessDefinition, delivery, john);
    }

}
