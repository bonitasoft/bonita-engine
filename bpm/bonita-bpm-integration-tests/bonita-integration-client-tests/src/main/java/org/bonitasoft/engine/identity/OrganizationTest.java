package org.bonitasoft.engine.identity;

import static org.bonitasoft.engine.matchers.BonitaMatcher.match;
import static org.bonitasoft.engine.matchers.ListElementMatcher.managersAre;
import static org.bonitasoft.engine.matchers.ListElementMatcher.usernamesAre;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.bpm.actor.ActorMember;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfo;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.identity.GroupCreator.GroupField;
import org.bonitasoft.engine.identity.RoleCreator.RoleField;
import org.bonitasoft.engine.test.StartProcessUntilStep;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OrganizationTest extends CommonAPITest {

    private static final String UTF_8 = "UTF-8";

    @Before
    public void before() throws BonitaException {
        login();
    }

    @After
    public void after() throws BonitaException {
        logout();
    }

    @Cover(classes = { IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Import", "Number of users" }, jira = "")
    @Test
    public void importOrganization() throws Exception {
        // create XML file
        final String userName = "anthony.birembault";
        final String jobTitle = "Web Team Manager";
        final String roleName = "Manager";
        final String roleDisplayName = "Bonita Manager";
        final String groupName = "Web";
        final String groupDisplayName = "web team";

        final String userName1 = "liuyanyan";
        final String roleName1 = "Developer";
        final String groupName1 = "Engine";

        importOrganization("simpleOrganization.xml");
        final User persistedUser = getIdentityAPI().getUserByUserName(userName);
        assertEquals(jobTitle, persistedUser.getJobTitle());
        // check createdBy for user
        assertEquals(getSession().getUserId(), persistedUser.getCreatedBy());
        final Group persistedGroup = getIdentityAPI().getGroupByPath(groupName);
        assertEquals(groupDisplayName, persistedGroup.getDisplayName());
        // check createdBy for group
        assertEquals(getSession().getUserId(), persistedGroup.getCreatedBy());
        final Role persistedRole = getIdentityAPI().getRoleByName(roleName);
        assertEquals(roleDisplayName, persistedRole.getDisplayName());
        // check createdBy for role
        assertEquals(getSession().getUserId(), persistedRole.getCreatedBy());
        final UserMembership persistedMembership = getIdentityAPI().getUserMemberships(persistedUser.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName, persistedMembership.getGroupName());
        assertEquals(roleName, persistedMembership.getRoleName());
        assertEquals(userName, persistedMembership.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership.getAssignedBy());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(userName1);
        assertNotNull(persistedUser1);
        final Role persistedRole1 = getIdentityAPI().getRoleByName(roleName1);
        assertNotNull(persistedRole1);
        final Group persistedGroup1 = getIdentityAPI().getGroupByPath(groupName1);
        assertNotNull(persistedGroup1);
        assertEquals(1, getIdentityAPI().getNumberOfUsersInGroup(persistedGroup1.getId()));
        assertEquals(persistedUser1, getIdentityAPI().getUsersInGroup(persistedGroup1.getId(), 0, 1, UserCriterion.FIRST_NAME_ASC).get(0));

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    @Cover(classes = { IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Import", "UserMembership", "Deleted" }, jira = "ENGINE-1363")
    @Test
    public void reImportUserMembershipDeleted() throws Exception {
        final String userName = "anthony.birembault";

        importOrganization("simpleOrganization.xml");
        final User persistedUser = getIdentityAPI().getUserByUserName(userName);
        final List<UserMembership> userMemberships = getIdentityAPI().getUserMemberships(persistedUser.getId(), 0, 10,
                UserMembershipCriterion.ASSIGNED_DATE_ASC);
        assertEquals(1, userMemberships.size());

        getIdentityAPI().deleteUserMembership(userMemberships.get(0).getId());

        // Re-import organization
        importOrganization("simpleOrganization.xml");
        final User persistedUser2 = getIdentityAPI().getUserByUserName(userName);
        final List<UserMembership> userMemberships2 = getIdentityAPI().getUserMemberships(persistedUser2.getId(), 0, 10,
                UserMembershipCriterion.ASSIGNED_DATE_ASC);
        assertEquals(1, userMemberships2.size());

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    private void importOrganization(final String fileName) throws IOException, OrganizationImportException {
        final InputStream xmlStream = OrganizationTest.class.getResourceAsStream(fileName);
        final InputStreamReader reader = new InputStreamReader(xmlStream);
        try {
            final byte[] organisationContent = IOUtils.toByteArray(reader);
            getIdentityAPI().importOrganization(new String(organisationContent, UTF_8));
        } finally {
            xmlStream.close();
            reader.close();
        }
    }

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Import", "Organization", "Enabled", "Disabled",
            "User" }, jira = "ENGINE-577")
    @Test
    public void importOrganizationWithEnabledAndDisabledUsers() throws Exception {
        // create XML file
        final String jobTitle = "Web Team Manager";
        final String userName = "anthony.birembault";
        final String userName1 = "liuyanyan";

        importOrganization("simpleOrganization.xml");

        final User persistedUser = getIdentityAPI().getUserByUserName(userName);
        assertEquals(jobTitle, persistedUser.getJobTitle());
        assertEquals(false, persistedUser.isEnabled());
        // check createdBy for user
        assertEquals(getSession().getUserId(), persistedUser.getCreatedBy());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(userName1);
        assertNotNull(persistedUser1);
        assertEquals(true, persistedUser1.isEnabled());

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
        getIdentityAPI().deleteUser(persistedUser1.getId());

        final Role persistedRole = getIdentityAPI().getRoleByName("Manager");
        assertNotNull(persistedRole);
        getIdentityAPI().deleteRole(persistedRole.getId());
        final Role persistedRole1 = getIdentityAPI().getRoleByName("Developer");
        assertNotNull(persistedRole1);
        getIdentityAPI().deleteRole(persistedRole1.getId());

        final Group persistedGroup = getIdentityAPI().getGroupByPath("Web");
        assertNotNull(persistedGroup);
        getIdentityAPI().deleteGroup(persistedGroup.getId());
        final Group persistedGroup1 = getIdentityAPI().getGroupByPath("Engine");
        assertNotNull(persistedGroup1);
        getIdentityAPI().deleteGroup(persistedGroup1.getId());
    }

    @Test
    public void importComplexOrganization() throws Exception {
        // create XML file
        final InputStream xmlStream = OrganizationTest.class.getResourceAsStream("complexOrganization.xml");
        try {
            final byte[] organisationContent = IOUtils.toByteArray(xmlStream);
            getIdentityAPI().importOrganization(new String(organisationContent));
        } finally {
            xmlStream.close();
        }
        final User jack = getIdentityAPI().getUserByUserName("jack");
        final User john = getIdentityAPI().getUserByUserName("john");
        assertNotSame("bpm", john.getPassword());
        assertEquals("John", john.getFirstName());
        assertEquals("Doe", john.getLastName());
        assertEquals("john.png", john.getIconName());
        assertEquals("/icons/", john.getIconPath());
        assertEquals("M", john.getTitle());
        assertEquals(jack.getId(), john.getManagerUserId());
        assertEquals("Web Team Manager", john.getJobTitle());
        final ContactData johnPersoContactData = getIdentityAPI().getUserContactData(john.getId(), true);
        assertEquals("emailValue", johnPersoContactData.getEmail());
        assertEquals("phoneNumberValue", johnPersoContactData.getPhoneNumber());
        assertEquals("mobileNumberValue", johnPersoContactData.getMobileNumber());
        assertEquals("faxNumberValue", johnPersoContactData.getFaxNumber());
        assertEquals("buildingValue", johnPersoContactData.getBuilding());
        assertEquals("roomValue", johnPersoContactData.getRoom());
        assertEquals("addressValue", johnPersoContactData.getAddress());
        assertEquals("zipCodeValue", johnPersoContactData.getZipCode());
        assertEquals("cityValue", johnPersoContactData.getCity());
        assertEquals("stateValue", johnPersoContactData.getState());
        assertEquals("countryValue", johnPersoContactData.getCountry());
        assertEquals("websiteValue", johnPersoContactData.getWebsite());
        final ContactData johnProfessionalContactData = getIdentityAPI().getUserContactData(john.getId(), false);
        assertEquals("emailProfessionalValue", johnProfessionalContactData.getEmail());
        assertEquals("phoneNumberProfessionalValue", johnProfessionalContactData.getPhoneNumber());
        assertEquals("mobileNumberProfessionalValue", johnProfessionalContactData.getMobileNumber());
        assertEquals("faxNumberProfessionalValue", johnProfessionalContactData.getFaxNumber());
        assertEquals("buildingProfessionalValue", johnProfessionalContactData.getBuilding());
        assertEquals("roomProfessionalValue", johnProfessionalContactData.getRoom());
        assertEquals("addressProfessionalValue", johnProfessionalContactData.getAddress());
        assertEquals("zipCodeProfessionalValue", johnProfessionalContactData.getZipCode());
        assertEquals("cityProfessionalValue", johnProfessionalContactData.getCity());
        assertEquals("stateProfessionalValue", johnProfessionalContactData.getState());
        assertEquals("countryProfessionalValue", johnProfessionalContactData.getCountry());
        assertEquals("websiteProfessionalValue", johnProfessionalContactData.getWebsite());

        getIdentityAPI().getUserByUserName("james");

        final Group bonitaRD = getIdentityAPI().getGroupByPath("/BonitaSoft/RD");
        final Group rd = getIdentityAPI().getGroupByPath("/RD");
        getIdentityAPI().getGroupByPath("/BonitaSoft/RD");

        List<UserMembership> userMemberships = getIdentityAPI().getUserMemberships(john.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC);
        assertEquals(1, userMemberships.size());
        UserMembership userMembership = userMemberships.get(0);
        assertEquals(bonitaRD.getId(), userMembership.getGroupId());
        assertEquals(jack.getId(), userMembership.getAssignedBy());
        assertEquals(new Date(1331142448365l), userMembership.getAssignedDate());
        userMemberships = getIdentityAPI().getUserMemberships(jack.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC);
        assertEquals(1, userMemberships.size());
        userMembership = userMemberships.get(0);
        assertEquals(rd.getId(), userMembership.getGroupId());

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    @Cover(classes = { IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Import", "Organization" }, story = "import same organization twice work", jira = "")
    @Test
    public void importACMEOrganizationTwiceWithDefaultProfile() throws Exception {
        // create XML file

        final InputStream xmlStream = OrganizationTest.class.getResourceAsStream("ACME.xml");
        try {
            final byte[] organisationContent = IOUtils.toByteArray(xmlStream);
            getIdentityAPI().importOrganization(new String(organisationContent));
            getIdentityAPI().importOrganization(new String(organisationContent));
        } finally {
            xmlStream.close();
        }

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    @Cover(classes = { IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Import", "Organization" }, story = "import same organization twice work even if some elements are removed", jira = "ENGINE-916")
    @Test
    public void importACMEOrganizationTwiceButRemoveGroupsAndRole() throws Exception {
        // create XML file

        final InputStream xmlStream = OrganizationTest.class.getResourceAsStream("ACME.xml");
        final byte[] organisationContent;
        try {
            organisationContent = IOUtils.toByteArray(xmlStream);
        } finally {
            xmlStream.close();
        }
        getIdentityAPI().importOrganization(new String(organisationContent));
        final long numberOfGroups = getIdentityAPI().getNumberOfGroups();
        final long numberOfRoles = getIdentityAPI().getNumberOfRoles();
        // remove some groups and roles
        final List<Group> groups = getIdentityAPI().getGroups(1, 3, GroupCriterion.NAME_ASC);
        final List<Long> groupIds = new ArrayList<Long>(groups.size());
        for (final Group group : groups) {
            groupIds.add(group.getId());
        }
        getIdentityAPI().deleteGroups(groupIds);
        final List<Role> roles = getIdentityAPI().getRoles(0, 2, RoleCriterion.NAME_ASC);
        final List<Long> roleIds = new ArrayList<Long>(roles.size());
        for (final Role role : roles) {
            roleIds.add(role.getId());
        }
        getIdentityAPI().deleteRoles(roleIds);

        getIdentityAPI().importOrganization(new String(organisationContent));
        assertEquals(numberOfGroups, getIdentityAPI().getNumberOfGroups());
        assertEquals(numberOfRoles, getIdentityAPI().getNumberOfRoles());

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    @Test(expected = OrganizationImportException.class)
    public void importOrganizationWithOrganizationImportException() throws Exception {
        final String xmlOrganization = "";
        final User createUser = getIdentityAPI().createUser("john", "bpm");
        try {
            getIdentityAPI().importOrganization(xmlOrganization);
        } catch (final OrganizationImportException e) {
            // check john was not deleted:
            assertNotNull("import organization with a bad file made the organization to be deleted!", getIdentityAPI().getUserByUserName("john"));
            deleteUser(createUser);
            throw e;
        }
    }

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Import", "Organization", "User" }, story = "check that import fail restore to previous state the organization", jira = "")
    @Test(expected = OrganizationImportException.class)
    public void importOrganizationFailRollBackToOldOrganization() throws Exception {
        final InputStream xmlStream = OrganizationTest.class.getResourceAsStream("organizationFailOnDuplicates.xml");
        try {
            final byte[] organisationContent = IOUtils.toByteArray(xmlStream);
            final String xmlOrganization = new String(organisationContent);
            getIdentityAPI().importOrganization(xmlOrganization);

            // check john is replaced before james is imported (in the xml)
            assertTrue(xmlOrganization, xmlOrganization.indexOf("james") < xmlOrganization.indexOf("john"));
        } finally {
            xmlStream.close();
        }

        // clear organization
        getIdentityAPI().deleteOrganization();

        // ensure james does not exist anymore
        try {
            getIdentityAPI().getUserByUserName("james");
            fail("james should not be found.");
        } catch (final UserNotFoundException unfe) {
            // ok
        }

        // we have only "john" as user
        final User createUser = getIdentityAPI().createUser("john", "bpm", "John", null);

        try {
            final byte[] organisationContent = IOUtils.toByteArray(xmlStream);
            getIdentityAPI().importOrganization(new String(organisationContent), ImportPolicy.FAIL_ON_DUPLICATES);
        } catch (final OrganizationImportException e) {
            // check john was not deleted and have old username
            try {
                getIdentityAPI().getUserByUserName("james");
                fail("old organisation should be restored");
            } catch (final UserNotFoundException unfe) {
                deleteUser(createUser);
                throw e;
            }
        } finally {
            xmlStream.close();
        }

    }

    @Test
    public void importOrganizationWithCycle() throws Exception {
        final InputStream xmlStream = OrganizationTest.class.getResourceAsStream("organizationWithCycle.xml");
        try {
            final byte[] bs = IOUtils.toByteArray(xmlStream);
            final String organizationContent = new String(bs);
            getIdentityAPI().importOrganization(organizationContent);

            // clean-up
            getIdentityAPI().deleteOrganization();
            getIdentityAPI().importOrganization(organizationContent);
        } finally {
            xmlStream.close();
        }

        final List<User> users = getIdentityAPI().getUsers(0, 10, UserCriterion.USER_NAME_ASC);
        assertThat(users, match(usernamesAre("user1", "user2", "user3")).and(managersAre(users.get(1).getId(), users.get(2).getId(), users.get(0).getId())));
        assertEquals(3, users.size());

        getIdentityAPI().deleteOrganization();
    }

    @Cover(classes = { ActorMember.class, Group.class, Role.class, User.class }, concept = BPMNConcept.ORGANIZATION, jira = "ENGINE-808, ENGINE-1635", keywords = {
            "Delete", "Organization", "Actor member", "User", "Group", "Role", "User membership", "Profile member", "Hidden activity", "Supervisor",
            "External identity mapping", "Pending mapping " })
    @Test
    public void deleteOrganization() throws Exception {
        // Create records for user role, group and membership
        final User persistedUser1 = getIdentityAPI().createUser("liuyanyan", "bpm");
        final User persistedUser2 = getIdentityAPI().createUser("anthony.birembault", "bpm");

        final RoleCreator rc1 = new RoleCreator("Developer");
        rc1.setDisplayName("roleDisplayName");
        final Role persistedRole1 = getIdentityAPI().createRole(rc1);
        final RoleCreator rc2 = new RoleCreator("Manager");
        rc2.setDisplayName("roleDisplayName");
        final Role persistedRole2 = getIdentityAPI().createRole(rc2);

        final GroupCreator groupCreator1 = new GroupCreator("Engine");
        groupCreator1.setDisplayName("engine team");
        final Group persistedGroup1 = getIdentityAPI().createGroup(groupCreator1);

        final GroupCreator groupCreator2 = new GroupCreator("Web");
        groupCreator2.setDisplayName("web team");
        final Group persistedGroup2 = getIdentityAPI().createGroup(groupCreator2);

        getIdentityAPI().addUserMembership(persistedUser1.getId(), persistedGroup1.getId(), persistedRole1.getId());
        getIdentityAPI().addUserMembership(persistedUser2.getId(), persistedGroup2.getId(), persistedRole2.getId());

        assertEquals(1, getIdentityAPI().getNumberOfUserMemberships(persistedUser1.getId()));
        assertEquals(2, getIdentityAPI().getNumberOfGroups());
        assertEquals(2, getIdentityAPI().getNumberOfUsers());
        assertEquals(2, getIdentityAPI().getNumberOfRoles());

        // Create process that is mapped to user of organization
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("deleteAllHuman", "1.1").addActor(ACTOR_NAME, true).addUserTask("human", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), ACTOR_NAME, persistedUser1);
        final StartProcessUntilStep startProcessUntilStep = startProcessAndWaitForTask(processDefinition.getId(), "human");
        assignAndExecuteStep(startProcessUntilStep.getActivityInstance(), persistedUser1.getId());
        waitForProcessToFinish(startProcessUntilStep.getProcessInstance());

        // delete organization and do check
        getIdentityAPI().deleteOrganization();
        assertEquals(0, getIdentityAPI().getNumberOfGroups());
        assertEquals(0, getIdentityAPI().getNumberOfUsers());
        assertEquals(0, getIdentityAPI().getNumberOfRoles());
        assertEquals(0, getIdentityAPI().getNumberOfUserMemberships(persistedUser1.getId()));

        // reload the process deploy info:
        final ProcessDeploymentInfo processDeploymentInfo = getProcessAPI().getProcessDeploymentInfo(processDefinition.getId());
        assertEquals(ConfigurationState.UNRESOLVED, processDeploymentInfo.getConfigurationState());

        // Clean up
        disableAndDeleteProcess(processDefinition);
    }

    @Cover(classes = { ActorMember.class, Group.class, Role.class, User.class }, concept = BPMNConcept.ORGANIZATION, jira = "ENGINE-1635", keywords = {
            "Delete", "Organization", "Actor member", "User", "Group", "Role", "User membership", "Profile member", "Hidden activity", "Supervisor",
            "External identity mapping", "Pending mapping " })
    @Test(expected = DeletionException.class)
    public void cantDeleteOrganizationWhenProcessInstanceIsActive() throws Exception {
        // Create records for user role, group and membership
        final User persistedUser1 = getIdentityAPI().createUser("liuyanyan", "bpm");
        final User persistedUser2 = getIdentityAPI().createUser("anthony.birembault", "bpm");

        final RoleCreator rc1 = new RoleCreator("Developer");
        rc1.setDisplayName("roleDisplayName");
        final Role persistedRole1 = getIdentityAPI().createRole(rc1);
        final RoleCreator rc2 = new RoleCreator("Manager");
        rc2.setDisplayName("roleDisplayName");
        final Role persistedRole2 = getIdentityAPI().createRole(rc2);

        final GroupCreator groupCreator1 = new GroupCreator("Engine");
        groupCreator1.setDisplayName("engine team");
        final Group persistedGroup1 = getIdentityAPI().createGroup(groupCreator1);

        final GroupCreator groupCreator2 = new GroupCreator("Web");
        groupCreator2.setDisplayName("web team");
        final Group persistedGroup2 = getIdentityAPI().createGroup(groupCreator2);

        getIdentityAPI().addUserMembership(persistedUser1.getId(), persistedGroup1.getId(), persistedRole1.getId());
        getIdentityAPI().addUserMembership(persistedUser2.getId(), persistedGroup2.getId(), persistedRole2.getId());

        assertEquals(1, getIdentityAPI().getNumberOfUserMemberships(persistedUser1.getId()));
        assertEquals(2, getIdentityAPI().getNumberOfGroups());
        assertEquals(2, getIdentityAPI().getNumberOfUsers());
        assertEquals(2, getIdentityAPI().getNumberOfRoles());

        // Create process that is mapped to user of organization
        final ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("deleteAllHuman", "1.1").addActor(ACTOR_NAME, true).addUserTask("human", ACTOR_NAME);
        final ProcessDefinition processDefinition = deployAndEnableWithActor(builder.done(), ACTOR_NAME, persistedUser1);
        final StartProcessUntilStep startProcessUntilStep = startProcessAndWaitForTask(processDefinition.getId(), "human");

        // delete organization and do check
        try {
            getIdentityAPI().deleteOrganization();
        } finally {
            // Clean up
            assignAndExecuteStep(startProcessUntilStep.getActivityInstance(), persistedUser1.getId());
            waitForProcessToFinish(startProcessUntilStep.getProcessInstance());
            getIdentityAPI().deleteOrganization();
            disableAndDeleteProcess(processDefinition);
        }
    }

    @Cover(classes = { IdentityAPI.class, ImportPolicy.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Organization", "Import", "Policy" }, story = "Import a new organization keep the old, if duplicates elements fail import", jira = "ENGINE-428")
    @Test(expected = OrganizationImportException.class)
    public void importOrganizationFailOnDuplicates() throws Exception {
        // create XML file
        final ImportPolicy policy = ImportPolicy.FAIL_ON_DUPLICATES;

        final String userName = "anthony.birembault";
        final String jobTitle = "Web Team Manager";
        final String roleName = "Manager";
        final String roleDisplayName = "Bonita Manager";
        final String groupName = "Web";
        final String groupDisplayName = "web team";

        final String userName1 = "liuyanyan";
        final String roleName1 = "Developer";
        final String roleDisplayName1 = "Bonita developer";
        final String groupName1 = "Engine";
        final String groupDisplayName1 = "engine team";

        importFirstSimpleOrganization();
        final User userToDisable = getIdentityAPI().getUserByUserName(userName);
        final UserUpdater updateDescriptor = new UserUpdater();
        updateDescriptor.setEnabled(false);
        getIdentityAPI().updateUser(userToDisable.getId(), updateDescriptor);
        assertEquals(2, getIdentityAPI().getNumberOfUsers());
        try {
            importOrganizationWithPolicy("simpleOrganizationDuplicates2.xml", policy);
        } catch (final OrganizationImportException e) {

            assertEquals(2, getIdentityAPI().getNumberOfUsers());
            assertEquals(2, getIdentityAPI().getNumberOfGroups());
            assertEquals(2, getIdentityAPI().getNumberOfRoles());

            final User persistedUser = getIdentityAPI().getUserByUserName(userName);
            assertNotNull(persistedUser);
            assertEquals(jobTitle, persistedUser.getJobTitle());
            assertFalse(persistedUser.isEnabled());
            final Role persistedRole = getIdentityAPI().getRoleByName(roleName);
            assertNotNull(persistedRole);
            assertEquals(roleDisplayName, persistedRole.getDisplayName());
            final Group persistedGroup = getIdentityAPI().getGroupByPath(groupName);
            assertNotNull(persistedGroup);
            assertEquals(groupDisplayName, persistedGroup.getDisplayName());
            final UserMembership persistedMembership = getIdentityAPI()
                    .getUserMemberships(persistedUser.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC).get(0);
            assertEquals(groupName, persistedMembership.getGroupName());
            assertEquals(roleName, persistedMembership.getRoleName());
            assertEquals(userName, persistedMembership.getUsername());
            // check assignedBy for membership
            assertEquals(getSession().getUserId(), persistedMembership.getAssignedBy());

            final User persistedUser1 = getIdentityAPI().getUserByUserName(userName1);
            assertNotNull(persistedUser1);
            assertTrue(persistedUser1.isEnabled());
            final Role persistedRole1 = getIdentityAPI().getRoleByName(roleName1);
            assertNotNull(persistedRole1);
            assertEquals(roleDisplayName1, persistedRole1.getDisplayName());
            final Group persistedGroup1 = getIdentityAPI().getGroupByPath(groupName1);
            assertNotNull(persistedGroup1);
            assertEquals(groupDisplayName1, persistedGroup1.getDisplayName());
            final UserMembership persistedMembership1 = getIdentityAPI().getUserMemberships(persistedUser1.getId(), 0, 10,
                    UserMembershipCriterion.GROUP_NAME_ASC).get(0);
            assertEquals(groupName1, persistedMembership1.getGroupName());
            assertEquals(roleName1, persistedMembership1.getRoleName());
            assertEquals(userName1, persistedMembership1.getUsername());
            // check assignedBy for membership
            assertEquals(getSession().getUserId(), persistedMembership1.getAssignedBy());

            // clean-up
            getIdentityAPI().deleteUser(persistedUser.getId());
            getIdentityAPI().deleteRole(persistedRole.getId());
            getIdentityAPI().deleteGroup(persistedGroup.getId());

            getIdentityAPI().deleteUser(persistedUser1.getId());
            getIdentityAPI().deleteRole(persistedRole1.getId());
            getIdentityAPI().deleteGroup(persistedGroup1.getId());
            throw e;
        }

        fail("This statement shouldn't be reached.");
    }

    @Cover(classes = { IdentityAPI.class, ImportPolicy.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Organization", "Import", "Policy" }, story = "Import a new organization keep the old, no duplicates elements", jira = "ENGINE-428")
    @Test
    public void importOrganizationFailOnDuplicatesNoDuplicates() throws Exception {
        // create XML file
        final ImportPolicy policy = ImportPolicy.FAIL_ON_DUPLICATES;

        final String userName = "anthony.birembault";
        final String jobTitle = "Web Team Manager";
        final String roleName = "Manager";
        final String roleDisplayName = "Bonita Manager";
        final String groupName = "Web";
        final String groupDisplayName = "web team";

        final String userName1 = "liuyanyan";
        final String roleName1 = "Developer";
        final String roleDisplayName1 = "Bonita developer";
        final String groupName1 = "Engine";
        final String groupDisplayName1 = "engine team";

        final String userName2 = "johnnyfootball";
        final String roleName2 = "Tester";
        final String groupName2 = "QA";

        importFirstSimpleOrganization();
        importOrganizationWithPolicy("simpleOrganizationNoDuplicates.xml", policy);

        assertEquals(3, getIdentityAPI().getNumberOfUsers());
        assertEquals(3, getIdentityAPI().getNumberOfGroups());
        assertEquals(3, getIdentityAPI().getNumberOfRoles());

        final User persistedUser = getIdentityAPI().getUserByUserName(userName);
        assertEquals(jobTitle, persistedUser.getJobTitle());
        // check createdBy for user
        assertEquals(getSession().getUserId(), persistedUser.getCreatedBy());
        final Group persistedGroup = getIdentityAPI().getGroupByPath(groupName);
        assertEquals(groupDisplayName, persistedGroup.getDisplayName());
        // check createdBy for group
        assertEquals(getSession().getUserId(), persistedGroup.getCreatedBy());
        final Role persistedRole = getIdentityAPI().getRoleByName(roleName);
        assertEquals(roleDisplayName, persistedRole.getDisplayName());
        // check createdBy for role
        assertEquals(getSession().getUserId(), persistedRole.getCreatedBy());
        final UserMembership persistedMembership = getIdentityAPI().getUserMemberships(persistedUser.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName, persistedMembership.getGroupName());
        assertEquals(roleName, persistedMembership.getRoleName());
        assertEquals(userName, persistedMembership.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership.getAssignedBy());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(userName1);
        assertNotNull(persistedUser1);
        final Role persistedRole1 = getIdentityAPI().getRoleByName(roleName1);
        assertNotNull(persistedRole1);
        assertEquals(roleDisplayName1, persistedRole1.getDisplayName());
        final Group persistedGroup1 = getIdentityAPI().getGroupByPath(groupName1);
        assertNotNull(persistedGroup1);
        assertEquals(groupDisplayName1, persistedGroup1.getDisplayName());
        final UserMembership persistedMembership1 = getIdentityAPI().getUserMemberships(persistedUser1.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName1, persistedMembership1.getGroupName());
        assertEquals(roleName1, persistedMembership1.getRoleName());
        assertEquals(userName1, persistedMembership1.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership1.getAssignedBy());

        final User persistedUser2 = getIdentityAPI().getUserByUserName(userName2);
        assertNotNull(persistedUser2);
        final Role persistedRole2 = getIdentityAPI().getRoleByName(roleName2);
        assertNotNull(persistedRole2);
        final Group persistedGroup2 = getIdentityAPI().getGroupByPath(groupName2);
        assertNotNull(persistedGroup2);
        final UserMembership persistedMembership2 = getIdentityAPI().getUserMemberships(persistedUser2.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName2, persistedMembership2.getGroupName());
        assertEquals(roleName2, persistedMembership2.getRoleName());
        assertEquals(userName2, persistedMembership2.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership2.getAssignedBy());

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
        getIdentityAPI().deleteRole(persistedRole.getId());
        getIdentityAPI().deleteGroup(persistedGroup.getId());

        getIdentityAPI().deleteUser(persistedUser1.getId());
        getIdentityAPI().deleteRole(persistedRole1.getId());
        getIdentityAPI().deleteGroup(persistedGroup1.getId());

        getIdentityAPI().deleteUser(persistedUser2.getId());
        getIdentityAPI().deleteRole(persistedRole2.getId());
        getIdentityAPI().deleteGroup(persistedGroup2.getId());

    }

    @Cover(classes = { IdentityAPI.class, ImportPolicy.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Organization", "Import", "Policy" }, story = "Import a new organization keep the old, if duplicates elements replace existing ones, add new elements", jira = "ENGINE-428")
    @Test
    public void importOrganizationMergeDuplicates() throws Exception {
        // create XML file
        final ImportPolicy policy = ImportPolicy.MERGE_DUPLICATES;

        final String userName = "anthony.birembault";
        final String jobTitle = "Web Team Manager";
        final String roleName = "Manager";
        final String roleDisplayName = "Bonita Manager";
        final String groupName = "Web";
        final String groupDisplayName = "web team";

        final String userName1 = "liuyanyan";
        final String roleName1 = "Developer";
        final String roleDisplayName1 = "Bonitasoft developer";
        final String groupName1 = "Engine";
        final String groupDisplayName1 = "RD engine team";

        final String userName2 = "johnnyfootball";
        final String roleName2 = "Tester";
        final String groupName2 = "QA";

        importFirstSimpleOrganization();
        importOrganizationWithPolicy("simpleOrganizationDuplicates2.xml", policy);

        assertEquals(3, getIdentityAPI().getNumberOfUsers());
        assertEquals(3, getIdentityAPI().getNumberOfGroups());
        assertEquals(3, getIdentityAPI().getNumberOfRoles());

        final User persistedUser = getIdentityAPI().getUserByUserName(userName);
        assertEquals(jobTitle, persistedUser.getJobTitle());
        // check createdBy for user
        assertEquals(getSession().getUserId(), persistedUser.getCreatedBy());
        final Group persistedGroup = getIdentityAPI().getGroupByPath(groupName);
        assertEquals(groupDisplayName, persistedGroup.getDisplayName());
        // check createdBy for group
        assertEquals(getSession().getUserId(), persistedGroup.getCreatedBy());
        final Role persistedRole = getIdentityAPI().getRoleByName(roleName);
        assertEquals(roleDisplayName, persistedRole.getDisplayName());
        // check createdBy for role
        assertEquals(getSession().getUserId(), persistedRole.getCreatedBy());
        final UserMembership persistedMembership = getIdentityAPI().getUserMemberships(persistedUser.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName, persistedMembership.getGroupName());
        assertEquals(roleName, persistedMembership.getRoleName());
        assertEquals(userName, persistedMembership.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership.getAssignedBy());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(userName1);
        assertNotNull(persistedUser1);
        final Role persistedRole1 = getIdentityAPI().getRoleByName(roleName1);
        assertNotNull(persistedRole1);
        assertEquals(roleDisplayName1, persistedRole1.getDisplayName());
        final Group persistedGroup1 = getIdentityAPI().getGroupByPath(groupName1);
        assertNotNull(persistedGroup1);
        assertEquals(groupDisplayName1, persistedGroup1.getDisplayName());
        final UserMembership persistedMembership1 = getIdentityAPI().getUserMemberships(persistedUser1.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName1, persistedMembership1.getGroupName());
        assertEquals(roleName1, persistedMembership1.getRoleName());
        assertEquals(userName1, persistedMembership1.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership.getAssignedBy());

        final User persistedUser2 = getIdentityAPI().getUserByUserName(userName2);
        assertNotNull(persistedUser2);
        final Role persistedRole2 = getIdentityAPI().getRoleByName(roleName2);
        assertNotNull(persistedRole2);
        final Group persistedGroup2 = getIdentityAPI().getGroupByPath(groupName2);
        assertNotNull(persistedGroup2);
        final UserMembership persistedMembership2 = getIdentityAPI().getUserMemberships(persistedUser2.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName2, persistedMembership2.getGroupName());
        assertEquals(roleName2, persistedMembership2.getRoleName());
        assertEquals(userName2, persistedMembership2.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership.getAssignedBy());

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
        getIdentityAPI().deleteRole(persistedRole.getId());
        getIdentityAPI().deleteGroup(persistedGroup.getId());

        getIdentityAPI().deleteUser(persistedUser1.getId());
        getIdentityAPI().deleteRole(persistedRole1.getId());
        getIdentityAPI().deleteGroup(persistedGroup1.getId());

        getIdentityAPI().deleteUser(persistedUser2.getId());
        getIdentityAPI().deleteRole(persistedRole2.getId());
        getIdentityAPI().deleteGroup(persistedGroup2.getId());
    }

    @Cover(classes = { IdentityAPI.class, ImportPolicy.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Import", "Organization",
            "Enabled", "Disabled", "User" }, jira = "ENGINE-577")
    @Test
    public void importOrganizationMergeDuplicatesWithEnabledAndDisabledUsers() throws Exception {
        // create XML file
        importFirstSimpleOrganization();
        importOrganizationWithPolicy("simpleOrganizationDuplicates2.xml", ImportPolicy.MERGE_DUPLICATES);

        assertEquals(3, getIdentityAPI().getNumberOfUsers());
        assertEquals(3, getIdentityAPI().getNumberOfGroups());
        assertEquals(3, getIdentityAPI().getNumberOfRoles());

        final User persistedUser = getIdentityAPI().getUserByUserName("johnnyfootball");
        assertNotNull(persistedUser);
        assertEquals(false, persistedUser.isEnabled());

        final User persistedUser1 = getIdentityAPI().getUserByUserName("liuyanyan");
        assertNotNull(persistedUser1);
        assertEquals(true, persistedUser1.isEnabled());

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
        getIdentityAPI().deleteUser(persistedUser1.getId());

        getIdentityAPI().deleteRole(getIdentityAPI().getRoleByName("Tester").getId());
        getIdentityAPI().deleteGroup(getIdentityAPI().getGroupByPath("QA").getId());

        getIdentityAPI().deleteRole(getIdentityAPI().getRoleByName("Developer").getId());
        getIdentityAPI().deleteGroup(getIdentityAPI().getGroupByPath("Engine").getId());

        getIdentityAPI().deleteUser(getIdentityAPI().getUserByUserName("anthony.birembault").getId());
        getIdentityAPI().deleteRole(getIdentityAPI().getRoleByName("Manager").getId());
        getIdentityAPI().deleteGroup(getIdentityAPI().getGroupByPath("Web").getId());
    }

    @Cover(classes = { IdentityAPI.class, ImportPolicy.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Organization", "Import", "Policy" }, story = "Import a new organization keep the old, if duplicates elements keep existing ones, add new elements", jira = "ENGINE-428")
    @Test
    public void importOrganizationIgnoreDuplicates() throws Exception {
        // create XML file
        final ImportPolicy policy = ImportPolicy.IGNORE_DUPLICATES;

        final String userName = "anthony.birembault";
        final String jobTitle = "Web Team Manager";
        final String roleName = "Manager";
        final String roleDisplayName = "Bonita Manager";
        final String groupName = "Web";
        final String groupDisplayName = "web team";

        final String userName1 = "liuyanyan";
        final String roleName1 = "Developer";
        final String roleDisplayName1 = "Bonita developer";
        final String groupName1 = "Engine";
        final String groupDisplayName1 = "engine team";

        final String userName2 = "johnnyfootball";
        final String roleName2 = "Tester";
        final String groupName2 = "QA";

        importFirstSimpleOrganization();
        importOrganizationWithPolicy("simpleOrganizationDuplicates2.xml", policy);

        assertEquals(3, getIdentityAPI().getNumberOfUsers());
        assertEquals(3, getIdentityAPI().getNumberOfGroups());
        assertEquals(3, getIdentityAPI().getNumberOfRoles());

        final User persistedUser = getIdentityAPI().getUserByUserName(userName);
        assertEquals(jobTitle, persistedUser.getJobTitle());
        // check createdBy for user
        assertEquals(getSession().getUserId(), persistedUser.getCreatedBy());
        final Group persistedGroup = getIdentityAPI().getGroupByPath(groupName);
        assertEquals(groupDisplayName, persistedGroup.getDisplayName());
        // check createdBy for group
        assertEquals(getSession().getUserId(), persistedGroup.getCreatedBy());
        final Role persistedRole = getIdentityAPI().getRoleByName(roleName);
        assertEquals(roleDisplayName, persistedRole.getDisplayName());
        // check createdBy for role
        assertEquals(getSession().getUserId(), persistedRole.getCreatedBy());
        final UserMembership persistedMembership = getIdentityAPI().getUserMemberships(persistedUser.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName, persistedMembership.getGroupName());
        assertEquals(roleName, persistedMembership.getRoleName());
        assertEquals(userName, persistedMembership.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership.getAssignedBy());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(userName1);
        assertNotNull(persistedUser1);
        final Role persistedRole1 = getIdentityAPI().getRoleByName(roleName1);
        assertNotNull(persistedRole1);
        assertEquals(roleDisplayName1, persistedRole1.getDisplayName());
        final Group persistedGroup1 = getIdentityAPI().getGroupByPath(groupName1);
        assertNotNull(persistedGroup1);
        assertEquals(groupDisplayName1, persistedGroup1.getDisplayName());
        final UserMembership persistedMembership1 = getIdentityAPI().getUserMemberships(persistedUser1.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName1, persistedMembership1.getGroupName());
        assertEquals(roleName1, persistedMembership1.getRoleName());
        assertEquals(userName1, persistedMembership1.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership1.getAssignedBy());

        final User persistedUser2 = getIdentityAPI().getUserByUserName(userName2);
        assertNotNull(persistedUser2);
        final Role persistedRole2 = getIdentityAPI().getRoleByName(roleName2);
        assertNotNull(persistedRole2);
        final Group persistedGroup2 = getIdentityAPI().getGroupByPath(groupName2);
        assertNotNull(persistedGroup2);
        final UserMembership persistedMembership2 = getIdentityAPI().getUserMemberships(persistedUser2.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName2, persistedMembership2.getGroupName());
        assertEquals(roleName2, persistedMembership2.getRoleName());
        assertEquals(userName2, persistedMembership2.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership2.getAssignedBy());

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
        getIdentityAPI().deleteRole(persistedRole.getId());
        getIdentityAPI().deleteGroup(persistedGroup.getId());

        getIdentityAPI().deleteUser(persistedUser1.getId());
        getIdentityAPI().deleteRole(persistedRole1.getId());
        getIdentityAPI().deleteGroup(persistedGroup1.getId());

        getIdentityAPI().deleteUser(persistedUser2.getId());
        getIdentityAPI().deleteRole(persistedRole2.getId());
        getIdentityAPI().deleteGroup(persistedGroup2.getId());
    }

    @Cover(classes = { IdentityAPI.class, ImportPolicy.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Import", "Organization",
            "Enabled", "Disabled", "User" }, jira = "ENGINE-577")
    @Test
    public void importOrganizationIgnoreDuplicatesWithEnabledAndDisabledUsers() throws Exception {
        // create XML file
        importFirstSimpleOrganization();
        importOrganizationWithPolicy("simpleOrganizationDuplicates2.xml", ImportPolicy.IGNORE_DUPLICATES);

        assertEquals(3, getIdentityAPI().getNumberOfUsers());
        assertEquals(3, getIdentityAPI().getNumberOfGroups());
        assertEquals(3, getIdentityAPI().getNumberOfRoles());

        final User persistedUser = getIdentityAPI().getUserByUserName("johnnyfootball");
        assertNotNull(persistedUser);
        assertFalse(persistedUser.isEnabled());

        final User persistedUser1 = getIdentityAPI().getUserByUserName("liuyanyan");
        assertNotNull(persistedUser1);
        assertTrue(persistedUser1.isEnabled());

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
        getIdentityAPI().deleteUser(persistedUser1.getId());

        getIdentityAPI().deleteRole(getIdentityAPI().getRoleByName("Tester").getId());
        getIdentityAPI().deleteGroup(getIdentityAPI().getGroupByPath("QA").getId());

        getIdentityAPI().deleteRole(getIdentityAPI().getRoleByName("Developer").getId());
        getIdentityAPI().deleteGroup(getIdentityAPI().getGroupByPath("Engine").getId());

        getIdentityAPI().deleteUser(getIdentityAPI().getUserByUserName("anthony.birembault").getId());
        getIdentityAPI().deleteRole(getIdentityAPI().getRoleByName("Manager").getId());
        getIdentityAPI().deleteGroup(getIdentityAPI().getGroupByPath("Web").getId());
    }

    private void importOrganizationWithPolicy(final String xmlFile, final ImportPolicy policy) throws Exception {
        final InputStream xmlStream = OrganizationTest.class.getResourceAsStream(xmlFile);
        try {
            final String organizationContent = IOUtils.toString(xmlStream);
            getIdentityAPI().importOrganization(organizationContent, policy);
        } finally {
            xmlStream.close();
        }
    }

    private void importFirstSimpleOrganization() throws Exception {
        final String userName = "anthony.birembault";
        final String jobTitle = "Web Team Manager";
        final String roleName = "Manager";
        final String roleDisplayName = "Bonita Manager";
        final String groupName = "Web";
        final String groupDisplayName = "web team";

        final String userName1 = "liuyanyan";
        final String roleName1 = "Developer";
        final String groupName1 = "Engine";

        importOrganization("simpleOrganizationDuplicates1.xml");
        final User persistedUser = getIdentityAPI().getUserByUserName(userName);
        assertEquals(jobTitle, persistedUser.getJobTitle());
        // check createdBy for user
        assertEquals(getSession().getUserId(), persistedUser.getCreatedBy());
        final Group persistedGroup = getIdentityAPI().getGroupByPath(groupName);
        assertEquals(groupDisplayName, persistedGroup.getDisplayName());
        // check createdBy for group
        assertEquals(getSession().getUserId(), persistedGroup.getCreatedBy());
        final Role persistedRole = getIdentityAPI().getRoleByName(roleName);
        assertEquals(roleDisplayName, persistedRole.getDisplayName());
        // check createdBy for role
        assertEquals(getSession().getUserId(), persistedRole.getCreatedBy());
        final UserMembership persistedMembership = getIdentityAPI().getUserMemberships(persistedUser.getId(), 0, 10, UserMembershipCriterion.GROUP_NAME_ASC)
                .get(0);
        assertEquals(groupName, persistedMembership.getGroupName());
        assertEquals(roleName, persistedMembership.getRoleName());
        assertEquals(userName, persistedMembership.getUsername());
        // check assignedBy for membership
        assertEquals(getSession().getUserId(), persistedMembership.getAssignedBy());

        final User persistedUser1 = getIdentityAPI().getUserByUserName(userName1);
        assertNotNull(persistedUser1);
        final Role persistedRole1 = getIdentityAPI().getRoleByName(roleName1);
        assertNotNull(persistedRole1);
        final Group persistedGroup1 = getIdentityAPI().getGroupByPath(groupName1);
        assertNotNull(persistedGroup1);
    }

    @Test
    public void exportOrganization() throws Exception {
        // create records for user role, group and membership
        final User persistedUser1 = getIdentityAPI().createUser("liuyanyan", "bpm");

        final UserCreator creator = new UserCreator("anthony.birembault", "bpm");
        creator.setJobTitle("Web Team Manager");
        final User persistedUser2 = getIdentityAPI().createUser(creator);

        final RoleCreator rc1 = new RoleCreator("Developer");
        rc1.setDisplayName("Bonita developer");
        final Role persistedRole1 = getIdentityAPI().createRole(rc1);
        final RoleCreator rc2 = new RoleCreator("Manager");
        rc2.setDisplayName("Bonita Manager");
        final Role persistedRole2 = getIdentityAPI().createRole(rc2);

        final GroupCreator groupCreator1 = new GroupCreator("Engine");
        groupCreator1.setDisplayName("engine team");
        final Group persistedGroup1 = getIdentityAPI().createGroup(groupCreator1);

        final GroupCreator groupCreator2 = new GroupCreator("Web");
        groupCreator2.setDisplayName("web team");
        final Group persistedGroup2 = getIdentityAPI().createGroup(groupCreator2);

        final UserMembership membership1 = getIdentityAPI().addUserMembership(persistedUser1.getId(), persistedGroup1.getId(), persistedRole1.getId());
        final UserMembership membership2 = getIdentityAPI().addUserMembership(persistedUser2.getId(), persistedGroup2.getId(), persistedRole2.getId());

        // export and check
        final String organizationContent = getIdentityAPI().exportOrganization();

        assertTrue(organizationContent.indexOf("Developer") != -1);
        assertTrue(organizationContent.indexOf("Bonita developer") != -1);
        assertTrue(organizationContent.indexOf("Engine") != -1);
        assertTrue(organizationContent.indexOf("engine team") != -1);
        assertTrue(organizationContent.indexOf(getIdentityAPI().getUserMembership(membership1.getId()).getGroupName()) != -1);
        assertTrue(organizationContent.indexOf(getIdentityAPI().getUserMembership(membership2.getId()).getGroupName()) != -1);

        // clean-up
        getIdentityAPI().deleteUser(persistedUser1.getId());
        getIdentityAPI().deleteUser(persistedUser2.getId());
        getIdentityAPI().deleteRole(persistedRole1.getId());
        getIdentityAPI().deleteRole(persistedRole2.getId());
        getIdentityAPI().deleteGroup(persistedGroup1.getId());
        getIdentityAPI().deleteGroup(persistedGroup2.getId());
    }

    @Cover(classes = { IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Export", "Organization", "Special characters" }, jira = "ENGINE-1517")
    @Test
    public void exportOrganizationWithSpecialCharacters() throws Exception {
        // create records for user role, group and membership
        final User user = getIdentityAPI().createUser("Céline*^$", "ploµ¨µ%§");
        final User user2 = getIdentityAPI().createUser("ééééééééééééééééééé", "éééééééééééééééé");

        final RoleCreator roleCreator = new RoleCreator("Développeur");
        roleCreator.setDisplayName("'(-è");
        roleCreator.setDescription("è-__ç_");
        roleCreator.setIconName("(-è_");
        roleCreator.setIconPath("^*_ç");
        final Role role = getIdentityAPI().createRole(roleCreator);

        final GroupCreator groupCreator = new GroupCreator("µ£¨µ");
        groupCreator.setDisplayName(".?/5434%¨%¨%");
        groupCreator.setDescription("è-__ç_2");
        groupCreator.setIconName("(-è_2");
        groupCreator.setIconPath("^*_ç2");
        groupCreator.setParentPath("$*ù$^ù");
        final Group group = getIdentityAPI().createGroup(groupCreator);

        final UserMembership membership = getIdentityAPI().addUserMembership(user.getId(), group.getId(), role.getId());

        // export and check
        final String organizationContent = getIdentityAPI().exportOrganization();

        // Role
        for (final Entry<RoleField, Serializable> entry : roleCreator.getFields().entrySet()) {
            assertTrue(organizationContent.indexOf((String) entry.getValue()) != -1);
        }

        // Group
        for (final Entry<GroupField, Serializable> entry : groupCreator.getFields().entrySet()) {
            assertTrue(organizationContent.indexOf((String) entry.getValue()) != -1);
        }

        // User
        assertTrue(organizationContent.indexOf("Céline*^$") != -1);
        assertTrue(organizationContent.indexOf("ééééééééééééééééééé") != -1);

        // UserMembership
        assertTrue(organizationContent.indexOf(getIdentityAPI().getUserMembership(membership.getId()).getGroupName()) != -1);

        // Verify all tags
        assertTrue(organizationContent.indexOf("<organization:Organization") != -1);
        assertTrue(organizationContent.indexOf("<users>") != -1);
        assertTrue(organizationContent.indexOf("<user") != -1);
        assertTrue(organizationContent.indexOf("</user>") != -1);
        assertTrue(organizationContent.indexOf("</users>") != -1);
        assertTrue(organizationContent.indexOf("<roles>") != -1);
        assertTrue(organizationContent.indexOf("<role") != -1);
        assertTrue(organizationContent.indexOf("</role>") != -1);
        assertTrue(organizationContent.indexOf("</roles>") != -1);
        assertTrue(organizationContent.indexOf("<groups>") != -1);
        assertTrue(organizationContent.indexOf("<group") != -1);
        assertTrue(organizationContent.indexOf("</group>") != -1);
        assertTrue(organizationContent.indexOf(" </groups>") != -1);
        assertTrue(organizationContent.indexOf("<memberships") != -1);
        assertTrue(organizationContent.indexOf("</memberships>") != -1);
        assertTrue(organizationContent.indexOf("</organization:Organization>") != -1);

        // clean-up
        deleteUsers(user, user2);
        getIdentityAPI().deleteRole(role.getId());
        getIdentityAPI().deleteGroup(group.getId());
    }

    @Cover(classes = { IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Import", "Export", "Organization", "Special characters" }, jira = "ENGINE-1517")
    @Test
    public void importAndExportOrganizationWithSpecialCharacters() throws Exception {
        importOrganization("OrganizationWithSpecialCharacters.xml");

        // export and check
        final String organizationContent = getIdentityAPI().exportOrganization();

        // Role
        assertTrue(organizationContent.indexOf("ééé") != -1);

        // Group
        assertTrue(organizationContent.indexOf("ééééééééé") != -1);

        // User
        assertTrue(organizationContent.indexOf("éé") != -1);

        // Verify all tags
        assertTrue(organizationContent.indexOf("<organization:Organization") != -1);
        assertTrue(organizationContent.indexOf("<users>") != -1);
        assertTrue(organizationContent.indexOf("<user") != -1);
        assertTrue(organizationContent.indexOf("</user>") != -1);
        assertTrue(organizationContent.indexOf("</users>") != -1);
        assertTrue(organizationContent.indexOf("<roles>") != -1);
        assertTrue(organizationContent.indexOf("<role") != -1);
        assertTrue(organizationContent.indexOf("</role>") != -1);
        assertTrue(organizationContent.indexOf("</roles>") != -1);
        assertTrue(organizationContent.indexOf("<groups>") != -1);
        assertTrue(organizationContent.indexOf("<group") != -1);
        assertTrue(organizationContent.indexOf("</group>") != -1);
        assertTrue(organizationContent.indexOf(" </groups>") != -1);
        assertTrue(organizationContent.indexOf("<memberships/>") != -1);
        assertTrue(organizationContent.indexOf("</organization:Organization>") != -1);

        // clean-up
        getIdentityAPI().deleteOrganization();
    }

    @Test
    public void exportAndImportOrganization() throws Exception {
        // create records for user role, group and membership
        final String username = "liuyanyan";
        final String password = "bpm";
        final User persistedUser1 = getIdentityAPI().createUser(username, password);

        final UserCreator creator = new UserCreator("anthony.birembault", password);
        creator.setJobTitle("Web Team Manager");
        final User persistedUser2 = getIdentityAPI().createUser(creator);

        final RoleCreator rc1 = new RoleCreator("Developer");
        rc1.setDisplayName("Bonita developer");
        final Role persistedRole1 = getIdentityAPI().createRole(rc1);
        final RoleCreator rc2 = new RoleCreator("Manager");
        rc2.setDisplayName("Bonita Manager");
        final Role persistedRole2 = getIdentityAPI().createRole(rc2);

        final GroupCreator groupCreator1 = new GroupCreator("Engine");
        groupCreator1.setDisplayName("engine team");
        final Group persistedGroup1 = getIdentityAPI().createGroup(groupCreator1);

        final GroupCreator groupCreator2 = new GroupCreator("Web");
        groupCreator2.setDisplayName("web team");
        final Group persistedGroup2 = getIdentityAPI().createGroup(groupCreator2);

        final UserMembership membership1 = getIdentityAPI().addUserMembership(persistedUser1.getId(), persistedGroup1.getId(), persistedRole1.getId());
        final UserMembership membership2 = getIdentityAPI().addUserMembership(persistedUser2.getId(), persistedGroup2.getId(), persistedRole2.getId());

        // export and check
        final String organizationContent = getIdentityAPI().exportOrganization();

        assertTrue(organizationContent.indexOf("Developer") != -1);
        assertTrue(organizationContent.indexOf("Bonita developer") != -1);
        assertTrue(organizationContent.indexOf("Engine") != -1);
        assertTrue(organizationContent.indexOf("engine team") != -1);
        assertTrue(organizationContent.indexOf(getIdentityAPI().getUserMembership(membership1.getId()).getGroupName()) != -1);
        assertTrue(organizationContent.indexOf(getIdentityAPI().getUserMembership(membership2.getId()).getGroupName()) != -1);

        // clean-up
        getIdentityAPI().deleteUser(persistedUser1.getId());
        getIdentityAPI().deleteUser(persistedUser2.getId());
        getIdentityAPI().deleteRole(persistedRole1.getId());
        getIdentityAPI().deleteRole(persistedRole2.getId());
        getIdentityAPI().deleteGroup(persistedGroup1.getId());
        getIdentityAPI().deleteGroup(persistedGroup2.getId());

        getIdentityAPI().importOrganization(organizationContent);
        final List<User> users = getIdentityAPI().getUsers(0, 10, UserCriterion.FIRST_NAME_ASC);
        for (final User user : users) {
            getIdentityAPI().deleteUser(user.getId());
        }
        assertEquals(2, users.size());
        final List<Role> roles = getIdentityAPI().getRoles(0, 10, RoleCriterion.NAME_ASC);
        for (final Role role : roles) {
            getIdentityAPI().deleteRole(role.getId());
        }
        final List<Group> groups = getIdentityAPI().getGroups(0, 10, GroupCriterion.NAME_ASC);
        for (final Group group : groups) {
            getIdentityAPI().deleteGroup(group.getId());
        }
    }

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Export", "Organization", "Disabled", "User" }, jira = "ENGINE-577")
    @Test
    public void exportOrganizationWithDisabledUsers() throws Exception {
        // create records for user
        final User persistedUser = getIdentityAPI().createUser("liuyanyan", "bpm");
        final UserUpdater updater = new UserUpdater();
        updater.setEnabled(false);
        getIdentityAPI().updateUser(persistedUser.getId(), updater);

        // export and check
        final String organizationContent = getIdentityAPI().exportOrganization();
        assertTrue(organizationContent.indexOf("false") != -1);

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
    }

    @Cover(classes = { IdentityAPI.class, User.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Export", "Organization", "Enabled", "User" }, jira = "ENGINE-577")
    @Test
    public void exportOrganizationWithEnabledUsers() throws Exception {
        // create records for user
        final UserCreator creator = new UserCreator("anthony.birembault", "bpm");
        creator.setEnabled(true);
        final User persistedUser = getIdentityAPI().createUser(creator);

        // export and check
        final String organizationContent = getIdentityAPI().exportOrganization();
        assertTrue(organizationContent.indexOf("true") != -1);

        // clean-up
        getIdentityAPI().deleteUser(persistedUser.getId());
    }

}
