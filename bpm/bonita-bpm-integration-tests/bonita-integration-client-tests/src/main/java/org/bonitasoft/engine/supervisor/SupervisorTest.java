package org.bonitasoft.engine.supervisor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.bar.InvalidBusinessArchiveFormatException;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.InvalidProcessDefinitionException;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisorSearchDescriptor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SupervisorTest extends CommonAPITest {

    private User user1;

    private User user2;

    private User user3;

    private User user4;

    private User user5;

    private Group group1;

    private Group group2;

    private Role role1;

    private Role role2;

    private ProcessDefinition processDefinition1;

    private ProcessDefinition processDefinition2;

    private ProcessSupervisor supervisor1;

    private ProcessSupervisor supervisor2;

    private ProcessSupervisor supervisor3;

    private ProcessSupervisor supervisor4;

    private ProcessSupervisor supervisor5;

    @Before
    public void before() throws Exception {
        login();
        user1 = getIdentityAPI().createUser(USERNAME, PASSWORD);
        group1 = getIdentityAPI().createGroup("Engine", null);
        role1 = getIdentityAPI().createRole("Developer");
    }

    @After
    public void after() throws BonitaException, BonitaHomeNotSetException {
        deleteUser(user1);
        deleteRoles(role1);
        deleteGroups(group1);
        logout();
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Process" }, story = "Test if a user is supervisor of a process.", jira = "")
    @Test
    public void isUserProcessSupervisor() throws Exception {
        final ProcessDefinition processDefinition = createProcessDefinition("myProcess1");
        // before create supervisor
        assertFalse(getProcessAPI().isUserProcessSupervisor(processDefinition.getId(), user1.getId()));

        // create supervisor
        final ProcessSupervisor createdSupervisor = getProcessAPI().createProcessSupervisorForUser(processDefinition.getId(), user1.getId());
        // after created supervisor

        assertTrue(getProcessAPI().isUserProcessSupervisor(processDefinition.getId(), user1.getId()));

        // clean-up
        deleteProcess(processDefinition);
        deleteSupervisor(createdSupervisor.getSupervisorId());
    }

    private void deleteSupervisors(final ProcessSupervisor... processSupervisors) throws BonitaException {
        if (processSupervisors != null) {
            for (final ProcessSupervisor processSupervisor : processSupervisors) {
                deleteSupervisor(processSupervisor.getSupervisorId());
            }
        }
    }

    private ProcessDefinition createProcessDefinition(final String processName) throws InvalidProcessDefinitionException, ProcessDeployException,
            InvalidBusinessArchiveFormatException, AlreadyExistsException {
        // test process definition with no supervisor
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0").done();

        return getProcessAPI().deploy(new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(designProcessDefinition).done());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Get", "Delete" }, story = "Get and delete supervisor.", jira = "")
    @Test
    public void addGetAndDeleteSupervisor() throws BonitaException {
        final ProcessDefinition processDefinition = createProcessDefinition("myProcess1");
        // Add Supervisor
        final ProcessSupervisor createdSupervisor = getProcessAPI().createProcessSupervisorForUser(processDefinition.getId(), user1.getId());
        assertEquals(processDefinition.getId(), createdSupervisor.getProcessDefinitionId());

        // Count to assert
        final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 3, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        SearchResult<ProcessSupervisor> result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(1, result.getCount());

        final ProcessSupervisor getSupervisorResult = result.getResult().get(0);
        assertEquals(createdSupervisor.getSupervisorId(), getSupervisorResult.getSupervisorId());
        assertEquals(user1.getId(), getSupervisorResult.getUserId());
        assertEquals(createdSupervisor.getProcessDefinitionId(), getSupervisorResult.getProcessDefinitionId());

        // Delete supervisor using id
        deleteSupervisor(getSupervisorResult.getSupervisorId());

        result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(0, result.getCount());

        // clean-up
        deleteProcess(processDefinition);
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Create" }, story = "Can't create twice the same process supervisor.", jira = "")
    @Test(expected = AlreadyExistsException.class)
    public void cantCreateTwiceSameUserSupervisor() throws BonitaException {
        final ProcessDefinition processDefinition = createProcessDefinition("myProcess1");
        // Add Supervisor
        final ProcessSupervisor createdSupervisor = getProcessAPI().createProcessSupervisorForUser(processDefinition.getId(), user1.getId());

        try {
            getProcessAPI().createProcessSupervisorForUser(processDefinition.getId(), user1.getId());
        } finally {
            // clean-up
            deleteSupervisor(createdSupervisor.getSupervisorId());
            deleteProcess(processDefinition);
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Group", "Create" }, story = "Can't create twice the same process supervisor.", jira = "")
    @Test(expected = AlreadyExistsException.class)
    public void cantCreateTwiceSameGroupSupervisor() throws BonitaException {
        final ProcessDefinition processDefinition = createProcessDefinition("myProcess1");
        // Add Supervisor
        final ProcessSupervisor createdSupervisor = getProcessAPI().createProcessSupervisorForGroup(processDefinition.getId(), group1.getId());

        try {
            getProcessAPI().createProcessSupervisorForGroup(processDefinition.getId(), group1.getId());
        } finally {
            // clean-up
            deleteSupervisor(createdSupervisor.getSupervisorId());
            deleteProcess(processDefinition);
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "Create" }, story = "Can't create twice the same process supervisor.", jira = "")
    @Test(expected = AlreadyExistsException.class)
    public void cantCreateTwiceSameRoleSupervisor() throws BonitaException {
        final ProcessDefinition processDefinition = createProcessDefinition("myProcess1");
        // Add Supervisor
        final ProcessSupervisor createdSupervisor = getProcessAPI().createProcessSupervisorForRole(processDefinition.getId(), role1.getId());

        try {
            getProcessAPI().createProcessSupervisorForRole(processDefinition.getId(), role1.getId());
        } finally {
            // clean-up
            deleteSupervisor(createdSupervisor.getSupervisorId());
            deleteProcess(processDefinition);
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Membership", "Create" }, story = "Can't create twice the same process supervisor.", jira = "")
    @Test(expected = AlreadyExistsException.class)
    public void cantCreateTwiceSameMembershipSupervisor() throws BonitaException {
        final ProcessDefinition processDefinition = createProcessDefinition("myProcess1");
        // Add Supervisor
        final ProcessSupervisor createdSupervisor = getProcessAPI().createProcessSupervisorForMembership(processDefinition.getId(), group1.getId(),
                role1.getId());

        try {
            getProcessAPI().createProcessSupervisorForMembership(processDefinition.getId(), group1.getId(), role1.getId());
        } finally {
            // clean-up
            deleteSupervisor(createdSupervisor.getSupervisorId());
            deleteProcess(processDefinition);
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Group", "Role", "Delete" }, story = "Delete supervisors corresponding to criteria", jira = "ENGINE-766")
    @Test
    public void deleteSupervisors() throws BonitaException {
        // Create groups, roles, users
        createUsers();
        createGroups();
        createRoles();
        createProcessDefinitions();

        supervisor1 = getProcessAPI().createProcessSupervisorForGroup(processDefinition1.getId(), group1.getId());
        supervisor2 = getProcessAPI().createProcessSupervisorForGroup(processDefinition2.getId(), group2.getId());
        supervisor3 = getProcessAPI().createProcessSupervisorForRole(processDefinition2.getId(), role2.getId());
        supervisor4 = getProcessAPI().createProcessSupervisorForMembership(processDefinition1.getId(), group1.getId(), role1.getId());
        supervisor5 = getProcessAPI().createProcessSupervisorForMembership(processDefinition2.getId(), group2.getId(), role1.getId());

        getProcessAPI().createProcessSupervisorForMembership(processDefinition1.getId(), group2.getId(), role2.getId());
        getProcessAPI().createProcessSupervisorForRole(processDefinition1.getId(), role1.getId());
        getProcessAPI().createProcessSupervisorForUser(processDefinition1.getId(), user1.getId());

        // Delete supervisor using ids
        // Unexisted Supervisor
        try {
            getProcessAPI().deleteSupervisor(processDefinition1.getId(), user1.getId(), role2.getId(), group2.getId());
            fail("no exception was thrown when deleting an unknown supervisor");
        } catch (final DeletionException e) {

        }
        final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 10, ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);
        SearchResult<ProcessSupervisor> result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(8, result.getCount());

        try {
            getProcessAPI().deleteSupervisor(null, null, role1.getId(), null);
            fail("no exception was thrown when deleting an unknown supervisor");
        } catch (final DeletionException e) {

        }
        result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(8, result.getCount());

        // Existed Supervisor
        getProcessAPI().deleteSupervisor(processDefinition1.getId(), null, role2.getId(), group2.getId());
        result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(7, result.getCount());

        getProcessAPI().deleteSupervisor(processDefinition1.getId(), user1.getId(), null, null);
        result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(6, result.getCount());

        getProcessAPI().deleteSupervisor(processDefinition1.getId(), null, role1.getId(), null);
        result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(5, result.getCount());

        // clean-up
        deleteSupervisors(supervisor1, supervisor2, supervisor3, supervisor4, supervisor5);
        deleteRoles(role2);
        deleteGroups(group2);
        deleteUsers(user2, user3, user4, user5);
        deleteProcess(processDefinition1, processDefinition2);
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Group" }, story = "Add group to supervisor.", jira = "")
    @Test
    public void addGroupToSupervisor() throws Exception {
        final ProcessDefinition processDefinition = createProcessDefinition("myProcess1");

        // Add Superviso
        final ProcessSupervisor createdSupervisor = getProcessAPI().createProcessSupervisorForGroup(processDefinition.getId(), group1.getId());
        assertEquals(processDefinition.getId(), createdSupervisor.getProcessDefinitionId());

        // Search supervisor
        final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 3, ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);
        final SearchResult<ProcessSupervisor> result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(1, result.getCount());

        final ProcessSupervisor getSupervisorResult = result.getResult().get(0);
        assertEquals(createdSupervisor.getSupervisorId(), getSupervisorResult.getSupervisorId());
        assertEquals(group1.getId(), getSupervisorResult.getGroupId());
        assertEquals(createdSupervisor.getProcessDefinitionId(), getSupervisorResult.getProcessDefinitionId());

        // clean-up
        deleteSupervisor(createdSupervisor.getSupervisorId());
        deleteProcess(processDefinition);
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Membership" }, story = "Add membership to supervisor.", jira = "")
    @Test
    public void addMembershipToSupervisor() throws Exception {
        final UserMembership membership = getIdentityAPI().addUserMembership(user1.getId(), group1.getId(), role1.getId());
        final ProcessDefinition processDefinition = createProcessDefinition("myProcess1");
        // Add Supervisor
        final ProcessSupervisor createdSupervisor = getProcessAPI()
                .createProcessSupervisorForMembership(processDefinition.getId(), group1.getId(), role1.getId());
        assertEquals(processDefinition.getId(), createdSupervisor.getProcessDefinitionId());

        // Search supervisor
        final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 3, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        final SearchResult<ProcessSupervisor> result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(1, result.getCount());

        final ProcessSupervisor getSupervisorResult = result.getResult().get(0);
        assertEquals(createdSupervisor.getSupervisorId(), getSupervisorResult.getSupervisorId());
        assertEquals(group1.getId(), getSupervisorResult.getGroupId());
        assertEquals(role1.getId(), getSupervisorResult.getRoleId());
        assertEquals(createdSupervisor.getProcessDefinitionId(), getSupervisorResult.getProcessDefinitionId());

        // Check is user supervisor
        assertTrue(getProcessAPI().isUserProcessSupervisor(processDefinition.getId(), user1.getId()));
        // clean-up
        deleteSupervisor(createdSupervisor.getSupervisorId());
        // delete membership first
        getIdentityAPI().deleteUserMembership(membership.getId());
        deleteProcess(processDefinition);
    }

    private void afterSearchProcessSupervisorsForUser() throws BonitaException {
        deleteSupervisors(supervisor1, supervisor2, supervisor3, supervisor4, supervisor5);
        deleteUsers(user2, user3, user4, user5);
        deleteProcess(processDefinition1, processDefinition2);
    }

    private void afterSearchProcessSupervisorsForGroup() throws BonitaException {
        deleteSupervisors(supervisor1, supervisor2);
        deleteGroups(group2);
        deleteProcess(processDefinition1, processDefinition2);
    }

    private void afterSearchProcessSupervisorsForRole() throws BonitaException {
        deleteSupervisors(supervisor1, supervisor2);
        deleteRoles(role2);
        deleteProcess(processDefinition1, processDefinition2);
    }

    private void afterSearchProcessSupervisorsForRoleAndGroup() throws BonitaException {
        deleteSupervisors(supervisor1, supervisor2, supervisor3);
        deleteRoles(role2);
        deleteGroups(group2);
        deleteProcess(processDefinition1, processDefinition2);
    }

    private void afterSearchProcessSupervisorsForUserAndMembership() throws BonitaException {
        deleteSupervisors(supervisor1, supervisor2, supervisor3, supervisor4, supervisor5);
        deleteUsers(user2, user3, user4, user5);
        deleteGroups(group2);
        deleteRoles(role2);
        deleteProcess(processDefinition1, processDefinition2);
    }

    private void beforeSearchProcessSupervisorsForUser() throws BonitaException {
        // create users
        createUsers();
        // add user as supervisor for process definition
        createProcessDefinitions();
        createUserSupervisors();
    }

    private void beforeSearchProcessSupervisorsForGroup() throws BonitaException {
        createGroups();
        createProcessDefinitions();
        createGroupSupervisors();
    }

    private void beforeSearchProcessSupervisorsForRole() throws BonitaException {
        createRoles();
        createProcessDefinitions();
        createRoleSupervisors();
    }

    private void beforeSearchProcessSupervisorsForRoleAndGroup() throws BonitaException {
        createGroups();
        createRoles();
        createProcessDefinitions();
        createMembershipSupervisors();
    }

    private void beforeSearchProcessSupervisorsForUserAndMembership() throws BonitaException {
        createUsers();
        createGroups();
        createRoles();
        createProcessDefinitions();
        createUserAndMembershipSupervisors();
    }

    private void createProcessDefinitions() throws InvalidProcessDefinitionException, ProcessDeployException, InvalidBusinessArchiveFormatException,
            AlreadyExistsException {
        processDefinition1 = createProcessDefinition("myProcess1");
        processDefinition2 = createProcessDefinition("myProcess2");
    }

    private void createUsers() throws BonitaException {
        user2 = createUser("user2", "bpm", "FirstName2", "LastName2");
        user3 = createUser("user3", "bpm", "FirstName3", "LastName3");
        user4 = createUser("user4", "bpm", "FirstName4", "LastName4");
        user5 = createUser("user5", "bpm", "FirstName5", "LastName5");
    }

    private void createGroups() throws BonitaException {
        group2 = createGroup("group2", "level2");
    }

    private void createRoles() throws BonitaException {
        role2 = createRole("role2");
    }

    private void createUserSupervisors() throws BonitaException {
        supervisor1 = getProcessAPI().createProcessSupervisorForUser(processDefinition1.getId(), user1.getId());
        supervisor2 = getProcessAPI().createProcessSupervisorForUser(processDefinition1.getId(), user2.getId());
        supervisor3 = getProcessAPI().createProcessSupervisorForUser(processDefinition1.getId(), user3.getId());
        supervisor4 = getProcessAPI().createProcessSupervisorForUser(processDefinition2.getId(), user4.getId());
        supervisor5 = getProcessAPI().createProcessSupervisorForUser(processDefinition2.getId(), user5.getId());
    }

    private void createGroupSupervisors() throws BonitaException {
        supervisor1 = getProcessAPI().createProcessSupervisorForGroup(processDefinition1.getId(), group1.getId());
        supervisor2 = getProcessAPI().createProcessSupervisorForGroup(processDefinition2.getId(), group2.getId());
        supervisor3 = null;
        supervisor4 = null;
        supervisor5 = null;
    }

    private void createRoleSupervisors() throws BonitaException {
        supervisor1 = getProcessAPI().createProcessSupervisorForRole(processDefinition1.getId(), role1.getId());
        supervisor2 = getProcessAPI().createProcessSupervisorForRole(processDefinition2.getId(), role2.getId());
        supervisor3 = null;
        supervisor4 = null;
        supervisor5 = null;
    }

    private void createMembershipSupervisors() throws BonitaException {
        supervisor1 = getProcessAPI().createProcessSupervisorForMembership(processDefinition1.getId(), group1.getId(), role1.getId());
        supervisor2 = getProcessAPI().createProcessSupervisorForMembership(processDefinition1.getId(), group2.getId(), role2.getId());
        supervisor3 = getProcessAPI().createProcessSupervisorForMembership(processDefinition2.getId(), group2.getId(), role1.getId());
        supervisor4 = null;
        supervisor5 = null;
    }

    private void createUserAndMembershipSupervisors() throws BonitaException {
        supervisor1 = getProcessAPI().createProcessSupervisorForUser(processDefinition1.getId(), user1.getId());
        supervisor2 = getProcessAPI().createProcessSupervisorForMembership(processDefinition1.getId(), group2.getId(), role2.getId());
        supervisor3 = getProcessAPI().createProcessSupervisorForMembership(processDefinition2.getId(), group2.getId(), role1.getId());
        supervisor4 = getProcessAPI().createProcessSupervisorForMembership(processDefinition1.getId(), group1.getId(), role1.getId());
        supervisor5 = getProcessAPI().createProcessSupervisorForUser(processDefinition1.getId(), user2.getId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Search", "Process" }, story = "Search process supervisors for user.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUser() throws Exception {
        beforeSearchProcessSupervisorsForUser();

        // test ASC
        SearchOptionsBuilder builder = buildSearchOptions(null, 0, 3, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(5, result1.getCount());
        List<ProcessSupervisor> supervisors = result1.getResult();
        assertNotNull(supervisors);
        assertEquals(3, supervisors.size());
        assertEquals(supervisor1.getUserId(), supervisors.get(0).getUserId());
        assertEquals(supervisor2.getUserId(), supervisors.get(1).getUserId());
        assertEquals(supervisor3.getUserId(), supervisors.get(2).getUserId());

        builder = buildSearchOptions(null, 3, 3, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        final SearchResult<ProcessSupervisor> result2 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(5, result2.getCount());
        supervisors = result2.getResult();
        assertNotNull(supervisors);
        assertEquals(2, supervisors.size());
        assertEquals(supervisor4.getUserId(), supervisors.get(0).getUserId());
        assertEquals(supervisor5.getUserId(), supervisors.get(1).getUserId());

        // test DESC
        builder = buildSearchOptions(null, 0, 2, ProcessSupervisorSearchDescriptor.USER_ID, Order.DESC);
        final SearchResult<ProcessSupervisor> result4 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(5, result4.getCount());
        supervisors = result4.getResult();
        assertNotNull(supervisors);
        assertEquals(2, supervisors.size());
        assertEquals(supervisor5.getUserId(), supervisors.get(0).getUserId());
        assertEquals(supervisor4.getUserId(), supervisors.get(1).getUserId());

        // clean-up
        afterSearchProcessSupervisorsForUser();
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Search", "Process", "Filter" }, story = "Search process supervisors for user with filter on process def id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserWithFilter() throws Exception {
        beforeSearchProcessSupervisorsForUser();

        try {
            // filter on process
            Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.PROCESS_DEFINITION_ID,
                    (Serializable) processDefinition2.getId());
            SearchOptionsBuilder builder = buildSearchOptions(filters, 0, 3, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
            final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
            assertEquals(2, result1.getCount());
            List<ProcessSupervisor> supervisors = result1.getResult();
            assertEquals(supervisor4.getUserId(), supervisors.get(0).getUserId());
            assertEquals(supervisor5.getUserId(), supervisors.get(1).getUserId());
            // filter on firstname
            filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.USER_ID, (Serializable) user1.getId());
            builder = buildSearchOptions(filters, 0, 3, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
            final SearchResult<ProcessSupervisor> result2 = getProcessAPI().searchProcessSupervisors(builder.done());
            assertEquals(1, result2.getCount());
            supervisors = result2.getResult();
            assertEquals(supervisor1.getUserId(), supervisors.get(0).getUserId());
        } finally {
            // clean-up
            afterSearchProcessSupervisorsForUser();
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Group", "Search", "Process" }, story = "Search process supervisors for group.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForGroup() throws Exception {
        beforeSearchProcessSupervisorsForGroup();

        final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 5, ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(2, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(group1.getId(), supervisors.get(0).getGroupId());
        assertEquals(group2.getId(), supervisors.get(1).getGroupId());

        // clean-up
        afterSearchProcessSupervisorsForGroup();
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Group", "Search", "Process", "Filter" }, story = "Search process supervisors for group with filter on group id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForGroupWithFilter() throws Exception {
        beforeSearchProcessSupervisorsForGroup();

        final Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.GROUP_ID, (Serializable) group1.getId());
        final SearchOptionsBuilder builder = buildSearchOptions(filters, 0, 5, ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(1, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(group1.getId(), supervisors.get(0).getGroupId());

        // clean-up
        afterSearchProcessSupervisorsForGroup();
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "Search", "Process" }, story = "Search process supervisors for role.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForRole() throws Exception {
        beforeSearchProcessSupervisorsForRole();

        try {
            final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 5, ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);
            final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
            assertEquals(2, result1.getCount());
            final List<ProcessSupervisor> supervisors = result1.getResult();
            assertEquals(role1.getId(), supervisors.get(0).getRoleId());
            assertEquals(role2.getId(), supervisors.get(1).getRoleId());
        } finally {
            // clean-up
            afterSearchProcessSupervisorsForRole();
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "Search", "Process", "Filter" }, story = "Search process supervisors for role with filter on role id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForRoleWithFilter() throws Exception {
        beforeSearchProcessSupervisorsForRole();

        try {
            final Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.ROLE_ID, (Serializable) role1.getId());
            final SearchOptionsBuilder builder = buildSearchOptions(filters, 0, 5, ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);
            final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
            assertEquals(1, result1.getCount());
            final List<ProcessSupervisor> supervisors = result1.getResult();
            assertEquals(role1.getId(), supervisors.get(0).getRoleId());
        } finally {
            // clean-up
            afterSearchProcessSupervisorsForRole();
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "Group", "Search", "Process" }, story = "Search process supervisors for role and group.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForRoleAndGroup() throws Exception {
        beforeSearchProcessSupervisorsForRoleAndGroup();

        try {
            final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 5, ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);
            builder.sort(ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);
            final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
            assertEquals(3, result1.getCount());
            final List<ProcessSupervisor> supervisors = result1.getResult();
            assertEquals(role1.getId(), supervisors.get(0).getRoleId());
            assertEquals(group1.getId(), supervisors.get(0).getGroupId());
            assertEquals(role1.getId(), supervisors.get(1).getRoleId());
            assertEquals(group2.getId(), supervisors.get(1).getGroupId());
            assertEquals(role2.getId(), supervisors.get(2).getRoleId());
            assertEquals(group2.getId(), supervisors.get(2).getGroupId());
        } finally {
            // clean-up
            afterSearchProcessSupervisorsForRoleAndGroup();
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "Group", "Search", "Process", "Filter" }, story = "Search process supervisors for role and group with filter on role id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForRoleAndGroupWithFilter() throws Exception {
        beforeSearchProcessSupervisorsForRoleAndGroup();

        final Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.ROLE_ID, (Serializable) role1.getId());
        final SearchOptionsBuilder builder = buildSearchOptions(filters, 0, 5, ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(2, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(supervisor1.getSupervisorId(), supervisors.get(0).getSupervisorId());
        assertEquals(supervisor3.getSupervisorId(), supervisors.get(1).getSupervisorId());

        // clean-up
        afterSearchProcessSupervisorsForRoleAndGroup();
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "User", "Search", "Process" }, story = "Search process supervisors for role and user.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserAndRole() throws Exception {
        beforeSearchProcessSupervisorsForUserAndMembership();

        final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 5, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(5, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(role1.getId(), supervisors.get(0).getRoleId());
        assertEquals(role1.getId(), supervisors.get(1).getRoleId());
        assertEquals(role2.getId(), supervisors.get(2).getRoleId());
        assertEquals(user1.getId(), supervisors.get(3).getUserId());
        assertEquals(user2.getId(), supervisors.get(4).getUserId());

        // clean-up
        afterSearchProcessSupervisorsForUserAndMembership();
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "User", "Search", "Process", "Filter" }, story = "Search process supervisors for role and user with filter on role id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserAndRoleWithFilter() throws Exception {
        beforeSearchProcessSupervisorsForUserAndMembership();

        final Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.ROLE_ID, (Serializable) role1.getId());
        final SearchOptionsBuilder builder = buildSearchOptions(filters, 0, 5, ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(2, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(role1.getId(), supervisors.get(0).getRoleId());
        assertEquals(role1.getId(), supervisors.get(1).getRoleId());

        // clean-up
        afterSearchProcessSupervisorsForUserAndMembership();
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Group", "Search", "Process" }, story = "Search process supervisors for user and group.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserAndGroup() throws Exception {
        beforeSearchProcessSupervisorsForUserAndMembership();

        final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 5, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(5, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(group1.getId(), supervisors.get(0).getGroupId());
        assertEquals(group2.getId(), supervisors.get(1).getGroupId());
        assertEquals(group2.getId(), supervisors.get(2).getGroupId());
        assertEquals(user1.getId(), supervisors.get(3).getUserId());
        assertEquals(user2.getId(), supervisors.get(4).getUserId());

        // clean-up
        afterSearchProcessSupervisorsForUserAndMembership();
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Group", "Search", "Process", "Filter" }, story = "Search process supervisors for user and group with filter on user id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserAndGroupWithFilter() throws Exception {
        beforeSearchProcessSupervisorsForUserAndMembership();

        final Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.USER_ID, (Serializable) user1.getId());
        final SearchOptionsBuilder builder = buildSearchOptions(filters, 0, 5, ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(1, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(supervisor1.getSupervisorId(), supervisors.get(0).getSupervisorId());

        // clean-up
        afterSearchProcessSupervisorsForUserAndMembership();
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Group", "Role", "Search", "Process" }, story = "Search process supervisors for user, group and role.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserAndGroupAndRole() throws Exception {
        beforeSearchProcessSupervisorsForUserAndMembership();

        final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 5, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(5, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(group1.getId(), supervisors.get(0).getGroupId());
        assertEquals(role1.getId(), supervisors.get(0).getRoleId());
        assertEquals(group2.getId(), supervisors.get(1).getGroupId());
        assertEquals(role1.getId(), supervisors.get(1).getRoleId());
        assertEquals(group2.getId(), supervisors.get(2).getGroupId());
        assertEquals(role2.getId(), supervisors.get(2).getRoleId());
        assertEquals(user1.getId(), supervisors.get(3).getUserId());
        assertEquals(user2.getId(), supervisors.get(4).getUserId());

        // clean-up
        afterSearchProcessSupervisorsForUserAndMembership();
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Group", "Role", "Search", "Process",
            "Filter" }, story = "Search process supervisors for user and group with filter on user id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserAndGroupAndRoleWithFilter() throws Exception {
        beforeSearchProcessSupervisorsForUserAndMembership();

        final Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.USER_ID, (Serializable) user1.getId());
        final SearchOptionsBuilder builder = buildSearchOptions(filters, 0, 5, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(1, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(supervisor1.getSupervisorId(), supervisors.get(0).getSupervisorId());

        // clean-up
        afterSearchProcessSupervisorsForUserAndMembership();
    }

    private SearchOptionsBuilder buildSearchOptions(final Map<String, Serializable> filters, final int pageIndex, final int numberOfResults,
            final String orderByField, final Order order) {
        final SearchOptionsBuilder builder = new SearchOptionsBuilder(pageIndex, numberOfResults);
        if (filters != null) {
            for (final Entry<String, Serializable> filter : filters.entrySet()) {
                builder.filter(filter.getKey(), filter.getValue());
            }
        }
        builder.sort(orderByField, order);
        return builder;
    }

}
