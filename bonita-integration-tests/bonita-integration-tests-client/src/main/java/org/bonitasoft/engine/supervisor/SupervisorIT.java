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
package org.bonitasoft.engine.supervisor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisor;
import org.bonitasoft.engine.bpm.supervisor.ProcessSupervisorSearchDescriptor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SupervisorIT extends TestWithTechnicalUser {

    private List<User> users;

    private List<Group> groups;

    private List<Role> roles;

    private List<ProcessDefinition> processDefinitions;

    private List<ProcessSupervisor> supervisors;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        createUsers();
        createGroups();
        createRoles();
        createProcessDefinitions();
        supervisors = new ArrayList<ProcessSupervisor>();
        createUserSupervisors();
        createGroupSupervisors();
        createRoleSupervisors();
        createMembershipSupervisors();
    }

    @Override
    @After
    public void after() throws Exception {
        deleteSupervisors(supervisors);
        disableAndDeleteProcess(processDefinitions);
        deleteUsers(users);
        deleteRoles(roles);
        deleteGroups(groups);
        super.after();
    }

    private void createProcessDefinitions() throws BonitaException {
        processDefinitions = new ArrayList<ProcessDefinition>();
        processDefinitions.add(createProcessDefinition("myProcess1"));
        processDefinitions.add(createProcessDefinition("myProcess2"));
    }

    private ProcessDefinition createProcessDefinition(final String processName) throws BonitaException {
        // test process definition with no supervisor
        final DesignProcessDefinition designProcessDefinition = new ProcessDefinitionBuilder().createNewInstance(processName, "1.0").done();
        return deployAndEnableProcess(designProcessDefinition);
    }

    private void createUsers() throws BonitaException {
        users = new ArrayList<User>();
        users.add(getIdentityAPI().createUser(USERNAME, PASSWORD));
        users.add(createUser("user2", "bpm", "FirstName2", "LastName2"));
        users.add(createUser("user3", "bpm", "FirstName3", "LastName3"));
        users.add(createUser("user4", "bpm", "FirstName4", "LastName4"));
        users.add(createUser("user5", "bpm", "FirstName5", "LastName5"));
    }

    private void createGroups() throws BonitaException {
        groups = new ArrayList<Group>();
        groups.add(getIdentityAPI().createGroup("Engine", null));
        groups.add(createGroup("group2", "level2"));
    }

    private void createRoles() throws BonitaException {
        roles = new ArrayList<Role>();
        roles.add(getIdentityAPI().createRole("Developer"));
        roles.add(createRole("role2"));
    }

    private void createUserSupervisors() throws BonitaException {
        final ProcessDefinition processDefinition1 = processDefinitions.get(0);
        final ProcessDefinition processDefinition2 = processDefinitions.get(1);

        supervisors.add(getProcessAPI().createProcessSupervisorForUser(processDefinition1.getId(), users.get(0).getId()));
        supervisors.add(getProcessAPI().createProcessSupervisorForUser(processDefinition1.getId(), users.get(1).getId()));
        supervisors.add(getProcessAPI().createProcessSupervisorForUser(processDefinition1.getId(), users.get(2).getId()));
        supervisors.add(getProcessAPI().createProcessSupervisorForUser(processDefinition2.getId(), users.get(3).getId()));
        supervisors.add(getProcessAPI().createProcessSupervisorForUser(processDefinition2.getId(), users.get(4).getId()));
    }

    private void createGroupSupervisors() throws BonitaException {
        supervisors.add(getProcessAPI().createProcessSupervisorForGroup(processDefinitions.get(0).getId(), groups.get(0).getId()));
        supervisors.add(getProcessAPI().createProcessSupervisorForGroup(processDefinitions.get(1).getId(), groups.get(1).getId()));
    }

    private void createRoleSupervisors() throws BonitaException {
        supervisors.add(getProcessAPI().createProcessSupervisorForRole(processDefinitions.get(0).getId(), roles.get(0).getId()));
        supervisors.add(getProcessAPI().createProcessSupervisorForRole(processDefinitions.get(1).getId(), roles.get(1).getId()));
    }

    private void createMembershipSupervisors() throws BonitaException {
        final ProcessDefinition processDefinition1 = processDefinitions.get(0);
        final Role role1 = roles.get(0);
        final Role role2 = roles.get(1);
        final Group group1 = groups.get(0);
        final Group group2 = groups.get(1);
        supervisors.add(getProcessAPI().createProcessSupervisorForMembership(processDefinition1.getId(), group1.getId(), role1.getId()));
        supervisors.add(getProcessAPI().createProcessSupervisorForMembership(processDefinition1.getId(), group2.getId(), role2.getId()));
        supervisors.add(getProcessAPI().createProcessSupervisorForMembership(processDefinitions.get(1).getId(), group2.getId(), role1.getId()));
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Process" }, story = "Test if a user is supervisor of a process.", jira = "")
    @Test
    public void isUserProcessSupervisor() throws Exception {
        final long processDefinitionId = processDefinitions.get(0).getId();
        final User user = createUser("user546", "bpm", "FirstName564", "LastName2");
        final long userId = user.getId();
        ProcessSupervisor createdSupervisor = null;

        try {
            // before create supervisor
            assertFalse(getProcessAPI().isUserProcessSupervisor(processDefinitionId, userId));
            // create supervisor
            createdSupervisor = getProcessAPI().createProcessSupervisorForUser(processDefinitionId, userId);
            // after created supervisor
            assertTrue(getProcessAPI().isUserProcessSupervisor(processDefinitionId, userId));
        } finally {
            // clean-up
            if (createdSupervisor != null) {
                deleteSupervisor(createdSupervisor.getSupervisorId());
            }
            deleteUser(user);
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Get", "Delete" }, story = "Get and delete supervisor.", jira = "")
    @Test
    public void getAndDeleteSupervisor() throws BonitaException {
        final long userId = users.get(0).getId();

        // Count to assert
        final SearchOptionsBuilder builder = buildSearchOptions(null, 7, 3, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        final SearchResult<ProcessSupervisor> result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(12, result.getCount());

        final ProcessSupervisor getSupervisorResult = result.getResult().get(0);
        assertEquals(supervisors.get(0).getSupervisorId(), getSupervisorResult.getSupervisorId());
        assertEquals(userId, getSupervisorResult.getUserId());
        assertEquals(supervisors.get(0).getProcessDefinitionId(), getSupervisorResult.getProcessDefinitionId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Create" }, story = "Can't create twice the same process supervisor.", jira = "")
    @Test(expected = AlreadyExistsException.class)
    public void cantCreateTwiceSameUserSupervisor() throws BonitaException {
        final long processDefinitionId = processDefinitions.get(0).getId();
        final long userId = users.get(0).getId();

        // Add Supervisor
        final ProcessSupervisor createdSupervisor = getProcessAPI().createProcessSupervisorForUser(processDefinitionId, userId);

        try {
            getProcessAPI().createProcessSupervisorForUser(processDefinitionId, userId);
        } finally {
            // clean-up
            deleteSupervisor(createdSupervisor.getSupervisorId());
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Group", "Create" }, story = "Can't create twice the same process supervisor.", jira = "")
    @Test(expected = AlreadyExistsException.class)
    public void cantCreateTwiceSameGroupSupervisor() throws BonitaException {
        final long processDefinitionId = processDefinitions.get(0).getId();
        final long groupId = groups.get(0).getId();

        // Add Supervisor
        final ProcessSupervisor createdSupervisor = getProcessAPI().createProcessSupervisorForGroup(processDefinitionId, groupId);

        try {
            getProcessAPI().createProcessSupervisorForGroup(processDefinitionId, groupId);
        } finally {
            // clean-up
            deleteSupervisor(createdSupervisor.getSupervisorId());
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "Create" }, story = "Can't create twice the same process supervisor.", jira = "")
    @Test(expected = AlreadyExistsException.class)
    public void cantCreateTwiceSameRoleSupervisor() throws BonitaException {
        final long processDefinitionId = processDefinitions.get(0).getId();
        final long roleId = roles.get(0).getId();

        // Add Supervisor
        final ProcessSupervisor createdSupervisor = getProcessAPI().createProcessSupervisorForRole(processDefinitionId, roleId);

        try {
            getProcessAPI().createProcessSupervisorForRole(processDefinitionId, roleId);
        } finally {
            // clean-up
            deleteSupervisor(createdSupervisor.getSupervisorId());
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Membership", "Create" }, story = "Can't create twice the same process supervisor.", jira = "")
    @Test(expected = AlreadyExistsException.class)
    public void cantCreateTwiceSameMembershipSupervisor() throws BonitaException {
        final long processDefinitionId = processDefinitions.get(0).getId();
        final Group group1 = groups.get(0);
        final Role role1 = roles.get(0);
        // Add Supervisor
        final ProcessSupervisor createdSupervisor = getProcessAPI().createProcessSupervisorForMembership(processDefinitionId, group1.getId(),
                role1.getId());

        try {
            getProcessAPI().createProcessSupervisorForMembership(processDefinitionId, group1.getId(), role1.getId());
        } finally {
            // clean-up
            deleteSupervisor(createdSupervisor.getSupervisorId());
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Group", "Role", "Delete" }, story = "Delete supervisors corresponding to criteria", jira = "ENGINE-766")
    @Test
    public void deleteSupervisors() throws BonitaException {
        final ProcessDefinition processDefinition1 = processDefinitions.get(0);
        final Role role1 = roles.get(0);
        final Role role2 = roles.get(1);
        final Group group2 = groups.get(1);
        final long userId = users.get(0).getId();

        // Delete supervisor using ids
        // Unexisted Supervisor
        try {
            getProcessAPI().deleteSupervisor(processDefinition1.getId(), userId, role2.getId(), group2.getId());
            fail("no exception was thrown when deleting an unknown supervisor");
        } catch (final DeletionException e) {

        }
        final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 10, ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);
        SearchResult<ProcessSupervisor> result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(12, result.getCount());

        try {
            getProcessAPI().deleteSupervisor(null, null, role1.getId(), null);
            fail("no exception was thrown when deleting an unknown supervisor");
        } catch (final DeletionException e) {

        }
        result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(12, result.getCount());

        // Existed Supervisor
        getProcessAPI().deleteSupervisor(processDefinition1.getId(), null, role2.getId(), group2.getId());
        supervisors.remove(10);
        result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(11, result.getCount());

        getProcessAPI().deleteSupervisor(processDefinition1.getId(), userId, null, null);
        supervisors.remove(0);
        result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(10, result.getCount());

        getProcessAPI().deleteSupervisor(processDefinition1.getId(), null, role1.getId(), null);
        supervisors.remove(6);
        result = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(9, result.getCount());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Group" }, story = "Add group to supervisor.", jira = "")
    @Test
    public void addGroupToSupervisor() throws Exception {
        final long processDefinitionId = processDefinitions.get(0).getId();
        final Group group = getIdentityAPI().createGroup("Engine789489", null);

        // Add Supervisor
        ProcessSupervisor createdSupervisor = null;
        try {
            createdSupervisor = getProcessAPI().createProcessSupervisorForGroup(processDefinitionId, group.getId());
            assertEquals(processDefinitionId, createdSupervisor.getProcessDefinitionId());
            // Search supervisor
            final SearchOptionsBuilder builder = buildSearchOptions(null, 12, 1, ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);
            final SearchResult<ProcessSupervisor> result = getProcessAPI().searchProcessSupervisors(builder.done());
            assertEquals(13, result.getCount());
            final ProcessSupervisor getSupervisorResult = result.getResult().get(0);
            assertEquals(createdSupervisor.getSupervisorId(), getSupervisorResult.getSupervisorId());
            assertEquals(group.getId(), getSupervisorResult.getGroupId());
            assertEquals(createdSupervisor.getProcessDefinitionId(), getSupervisorResult.getProcessDefinitionId());
        } finally {
            // clean-up
            if (createdSupervisor != null) {
                deleteSupervisor(createdSupervisor.getSupervisorId());
            }
            deleteGroups(group);
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Membership" }, story = "Add membership to supervisor.", jira = "")
    @Test
    public void addMembershipToSupervisor() throws Exception {
        final long processDefinitionId = processDefinitions.get(0).getId();
        final Role role = getIdentityAPI().createRole("Developer5646");
        final Group group1 = groups.get(0);

        // Add Supervisor
        ProcessSupervisor createdSupervisor = null;
        try {
            createdSupervisor = getProcessAPI()
                    .createProcessSupervisorForMembership(processDefinitionId, group1.getId(), role.getId());
            assertEquals(processDefinitionId, createdSupervisor.getProcessDefinitionId());
            // Search supervisor
            final SearchOptionsBuilder builder = buildSearchOptions(null, 12, 12, ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);
            final SearchResult<ProcessSupervisor> result = getProcessAPI().searchProcessSupervisors(builder.done());
            assertEquals(13, result.getCount());
            final ProcessSupervisor getSupervisorResult = result.getResult().get(0);
            assertEquals(createdSupervisor.getSupervisorId(), getSupervisorResult.getSupervisorId());
            assertEquals(group1.getId(), getSupervisorResult.getGroupId());
            assertEquals(role.getId(), getSupervisorResult.getRoleId());
            assertEquals(createdSupervisor.getProcessDefinitionId(), getSupervisorResult.getProcessDefinitionId());
            // Check is user supervisor
            assertTrue(getProcessAPI().isUserProcessSupervisor(processDefinitionId, users.get(0).getId()));
        } finally {
            // clean-up
            if (createdSupervisor != null) {
                deleteSupervisor(createdSupervisor.getSupervisorId());
            }
            deleteRoles(role);
        }
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Search", "Process" }, story = "Search process supervisors for user.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUser() throws Exception {
        final ProcessSupervisor supervisor4 = supervisors.get(3);
        final ProcessSupervisor supervisor5 = supervisors.get(4);

        // test ASC
        SearchOptionsBuilder builder = buildSearchOptions(null, 7, 3, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(12, result1.getCount());
        List<ProcessSupervisor> supervisorsResult = result1.getResult();
        assertNotNull(supervisorsResult);
        assertEquals(3, supervisorsResult.size());
        assertEquals(supervisors.get(0).getUserId(), supervisorsResult.get(0).getUserId());
        assertEquals(supervisors.get(1).getUserId(), supervisorsResult.get(1).getUserId());
        assertEquals(supervisors.get(2).getUserId(), supervisorsResult.get(2).getUserId());

        builder = buildSearchOptions(null, 10, 2, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        final SearchResult<ProcessSupervisor> result2 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(12, result2.getCount());
        supervisorsResult = result2.getResult();
        assertNotNull(supervisorsResult);
        assertEquals(2, supervisorsResult.size());
        assertEquals(supervisor4.getUserId(), supervisorsResult.get(0).getUserId());
        assertEquals(supervisor5.getUserId(), supervisorsResult.get(1).getUserId());

        // test DESC
        builder = buildSearchOptions(null, 0, 2, ProcessSupervisorSearchDescriptor.USER_ID, Order.DESC);
        final SearchResult<ProcessSupervisor> result4 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(12, result4.getCount());
        supervisorsResult = result4.getResult();
        assertNotNull(supervisorsResult);
        assertEquals(2, supervisorsResult.size());
        assertEquals(supervisor5.getUserId(), supervisorsResult.get(0).getUserId());
        assertEquals(supervisor4.getUserId(), supervisorsResult.get(1).getUserId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Search", "Process", "Filter" }, story = "Search process supervisors for user with filter on process def id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserWithFilter() throws Exception {
        // filter on process
        Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.PROCESS_DEFINITION_ID,
                (Serializable) processDefinitions.get(1).getId());
        SearchOptionsBuilder builder = buildSearchOptions(filters, 3, 5, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(5, result1.getCount());
        List<ProcessSupervisor> supervisorsResult = result1.getResult();
        assertEquals(supervisors.get(3).getUserId(), supervisorsResult.get(0).getUserId());
        assertEquals(supervisors.get(4).getUserId(), supervisorsResult.get(1).getUserId());

        // filter on firstname
        filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.USER_ID, (Serializable) users.get(0).getId());
        builder = buildSearchOptions(filters, 0, 3, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        final SearchResult<ProcessSupervisor> result2 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(1, result2.getCount());
        supervisorsResult = result2.getResult();
        assertEquals(supervisors.get(0).getUserId(), supervisorsResult.get(0).getUserId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Group", "Search", "Process" }, story = "Search process supervisors for group.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForGroup() throws Exception {
        final SearchOptionsBuilder builder = buildSearchOptions(null, 8, 2, ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(12, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(groups.get(0).getId(), supervisors.get(0).getGroupId());
        assertEquals(groups.get(1).getId(), supervisors.get(1).getGroupId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Group", "Search", "Process", "Filter" }, story = "Search process supervisors for group with filter on group id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForGroupWithFilter() throws Exception {
        final Group group1 = groups.get(0);

        final Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.GROUP_ID, (Serializable) group1.getId());
        final SearchOptionsBuilder builder = buildSearchOptions(filters, 0, 5, ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(2, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(group1.getId(), supervisors.get(0).getGroupId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "Search", "Process" }, story = "Search process supervisors for role.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForRole() throws Exception {
        final SearchOptionsBuilder builder = buildSearchOptions(null, 9, 11, ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);
        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(12, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(roles.get(0).getId(), supervisors.get(0).getRoleId());
        assertEquals(roles.get(1).getId(), supervisors.get(1).getRoleId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "Search", "Process", "Filter" }, story = "Search process supervisors for role with filter on role id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForRoleWithFilter() throws Exception {
        final Role role1 = roles.get(0);

        final Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.ROLE_ID, (Serializable) role1.getId());
        final SearchOptionsBuilder builder = buildSearchOptions(filters, 0, 5, ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);
        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(3, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(role1.getId(), supervisors.get(0).getRoleId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "Group", "Search", "Process" }, story = "Search process supervisors for role and group.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForRoleAndGroup() throws Exception {
        final Role role1 = roles.get(0);
        final Role role2 = roles.get(1);
        final Group group1 = groups.get(0);
        final Group group2 = groups.get(1);

        final SearchOptionsBuilder builder = buildSearchOptions(null, 8, 4, ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);
        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(12, result1.getCount());
        final List<ProcessSupervisor> supervisorsResult = result1.getResult();
        assertEquals(role1.getId(), supervisorsResult.get(0).getRoleId());
        assertEquals(group1.getId(), supervisorsResult.get(0).getGroupId());
        assertEquals(role1.getId(), supervisorsResult.get(1).getRoleId());
        assertEquals(group2.getId(), supervisorsResult.get(1).getGroupId());
        assertEquals(role2.getId(), supervisorsResult.get(3).getRoleId());
        assertEquals(group2.getId(), supervisorsResult.get(3).getGroupId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "Group", "Search", "Process", "Filter" }, story = "Search process supervisors for role and group with filter on role id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForRoleAndGroupWithFilter() throws Exception {
        final Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.ROLE_ID, (Serializable) roles.get(0).getId());
        final SearchOptionsBuilder builder = buildSearchOptions(filters, 1, 5, ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(3, result1.getCount());
        final List<ProcessSupervisor> supervisorsResult = result1.getResult();
        assertEquals(supervisors.get(9).getSupervisorId(), supervisorsResult.get(0).getSupervisorId());
        assertEquals(supervisors.get(11).getSupervisorId(), supervisorsResult.get(1).getSupervisorId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "User", "Search", "Process" }, story = "Search process supervisors for role and user.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserAndRole() throws Exception {
        final Role role1 = roles.get(0);

        final SearchOptionsBuilder builder = buildSearchOptions(null, 3, 6, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(12, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(role1.getId(), supervisors.get(0).getRoleId());
        assertEquals(role1.getId(), supervisors.get(1).getRoleId());
        assertEquals(roles.get(1).getId(), supervisors.get(2).getRoleId());
        assertEquals(users.get(0).getId(), supervisors.get(4).getUserId());
        assertEquals(users.get(1).getId(), supervisors.get(5).getUserId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "Role", "User", "Search", "Process", "Filter" }, story = "Search process supervisors for role and user with filter on role id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserAndRoleWithFilter() throws Exception {
        final Role role1 = roles.get(0);

        final Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.ROLE_ID, (Serializable) role1.getId());
        final SearchOptionsBuilder builder = buildSearchOptions(filters, 0, 5, ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(3, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(role1.getId(), supervisors.get(0).getRoleId());
        assertEquals(role1.getId(), supervisors.get(1).getRoleId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Group", "Search", "Process" }, story = "Search process supervisors for user and group.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserAndGroup() throws Exception {
        final Group group2 = groups.get(1);

        final SearchOptionsBuilder builder = buildSearchOptions(null, 3, 8, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(12, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(groups.get(0).getId(), supervisors.get(0).getGroupId());
        assertEquals(group2.getId(), supervisors.get(1).getGroupId());
        assertEquals(group2.getId(), supervisors.get(2).getGroupId());
        assertEquals(group2.getId(), supervisors.get(3).getGroupId());
        assertEquals(users.get(0).getId(), supervisors.get(4).getUserId());
        assertEquals(users.get(1).getId(), supervisors.get(5).getUserId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Group", "Search", "Process", "Filter" }, story = "Search process supervisors for user and group with filter on user id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserAndGroupWithFilter() throws Exception {
        final Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.USER_ID, (Serializable) users.get(0).getId());
        final SearchOptionsBuilder builder = buildSearchOptions(filters, 0, 5, ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(1, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(supervisors.get(0).getSupervisorId(), supervisors.get(0).getSupervisorId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Group", "Role", "Search", "Process" }, story = "Search process supervisors for user, group and role.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserAndGroupAndRole() throws Exception {
        final Role role1 = roles.get(0);
        final Role role2 = roles.get(1);
        final Group group1 = groups.get(0);
        final Group group2 = groups.get(1);

        final SearchOptionsBuilder builder = buildSearchOptions(null, 3, 6, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(12, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(group1.getId(), supervisors.get(0).getGroupId());
        assertEquals(role1.getId(), supervisors.get(0).getRoleId());
        assertEquals(group2.getId(), supervisors.get(2).getGroupId());
        assertEquals(role1.getId(), supervisors.get(2).getRoleId());
        assertEquals(group2.getId(), supervisors.get(3).getGroupId());
        assertEquals(role2.getId(), supervisors.get(3).getRoleId());
        assertEquals(users.get(0).getId(), supervisors.get(4).getUserId());
        assertEquals(users.get(1).getId(), supervisors.get(5).getUserId());
    }

    @Cover(classes = ProcessSupervisor.class, concept = BPMNConcept.SUPERVISOR, keywords = { "Supervisor", "User", "Group", "Role", "Search", "Process",
            "Filter" }, story = "Search process supervisors for user and group with filter on user id.", jira = "ENGINE-766")
    @Test
    public void searchProcessSupervisorsForUserAndGroupAndRoleWithFilter() throws Exception {
        final Map<String, Serializable> filters = Collections.singletonMap(ProcessSupervisorSearchDescriptor.USER_ID, (Serializable) users.get(0).getId());
        final SearchOptionsBuilder builder = buildSearchOptions(filters, 0, 5, ProcessSupervisorSearchDescriptor.USER_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.GROUP_ID, Order.ASC);
        builder.sort(ProcessSupervisorSearchDescriptor.ROLE_ID, Order.ASC);

        final SearchResult<ProcessSupervisor> result1 = getProcessAPI().searchProcessSupervisors(builder.done());
        assertEquals(1, result1.getCount());
        final List<ProcessSupervisor> supervisors = result1.getResult();
        assertEquals(supervisors.get(0).getSupervisorId(), supervisors.get(0).getSupervisorId());
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
