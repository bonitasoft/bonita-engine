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
package org.bonitasoft.engine.command.web;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.CommandAPI;
import org.bonitasoft.engine.command.CommandExecutionException;
import org.bonitasoft.engine.command.CommandNotFoundException;
import org.bonitasoft.engine.command.CommandParameterizationException;
import org.bonitasoft.engine.entitymember.EntityMember;
import org.bonitasoft.engine.entitymember.EntityMemberSearchDescriptor;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.MemberType;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class EntityMemberCommandsIT extends TestWithTechnicalUser {

    private static final String DISCRIMINATOR_ID_KEY = "DISCRIMINATOR_ID_KEY";

    private static final String USER_REPORT_DISCRIMINATOR = "USER_REPORT";

    private static final String SUPERVISOR_REPORT_DISCRIMINATOR = "SUPERVISOR_REPORT";

    private static final String ENTITY_MEMBER_ID_KEY = "ENTITY_MEMBER_ID_KEY";

    private static final String EXTERNAL_ID_KEY = "EXTERNAL_ID_KEY";

    private static final String USER_ID_KEY = "userId";

    private static final String GROUP_ID_KEY = "groupId";

    private static final String ROLE_ID_KEY = "roleId";

    private static final String SEARCH_OPTIONS_KEY = "SEARCH_OPTIONS_KEY";

    private static final String CREATE_COMMAND_NAME = "addEntityMemberCommand";

    private static final String DELETE_COMMAND_NAME = "removeEntityMemberCommand";

    private static final String SEARCH_FOR_USER_COMMAND = "searchEntityMembersForUserCommand";

    private static final String SEARCH_COMMAND = "searchEntityMembersCommand";

    private static final String DELETE_SEVERAL_COMMAND = "deleteEntityMembersCommand";

    private static final String MEMBER_TYPE_KEY = "MEMBER_TYPE_KEY";

    private User user1;

    private User user2;

    private User user3;

    private User user4;

    private User user5;

    private Group group1;

    private Group group2;

    private Role role1;

    private Role role2;

    private final String externalId1 = "aaaaaaa11111111";

    private final String externalId2 = "bbbbbb222222222";

    private EntityMember entityMember1;

    private EntityMember entityMember2;

    private EntityMember entityMember3;

    private EntityMember entityMember4;

    private EntityMember entityMember5;

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Create", "Delete" }, story = "Create new entity member and delete it.", jira = "")
    @Test
    public void createNewEntityMemberAndDeleteIt() throws Exception {
        final User newUser = createUser("test1", "password");
        // execute command:
        final HashMap<String, Serializable> createCommandParameters = new HashMap<String, Serializable>(4);
        createCommandParameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        createCommandParameters.put(EXTERNAL_ID_KEY, externalId1);
        createCommandParameters.put(USER_ID_KEY, newUser.getId());
        // createCommandParameters.put(ROLE_ID_KEY, -1L);
        // createCommandParameters.put(GROUP_ID_KEY, -1L);
        final EntityMember entityMember = (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, createCommandParameters);
        assertEquals(newUser.getId(), entityMember.getUserId());
        assertEquals(-1, entityMember.getRoleId());
        assertEquals(-1, entityMember.getGroupId());
        assertEquals(externalId1, entityMember.getExternalId());
        assertTrue("entityMemberId should be valuated", -1 != entityMember.getEntityMemberId());

        // delete it:
        final HashMap<String, Serializable> deleteCommandParameters = new HashMap<String, Serializable>(1);
        deleteCommandParameters.put(ENTITY_MEMBER_ID_KEY, entityMember.getEntityMemberId());
        getCommandAPI().execute(DELETE_COMMAND_NAME, deleteCommandParameters);
        try {
            // try to delete it another time:
            getCommandAPI().execute(DELETE_COMMAND_NAME, deleteCommandParameters);
            fail("Should throw SExternalIdentityMappingException !");
        } catch (final CommandExecutionException e) {
            // We must pass here:
        }

        deleteUser(newUser);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Delete" }, story = "Delete all entity members for external id.", jira = "")
    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteAllEntityMembersForExternalId() throws Exception {
        // execute command:
        final HashMap<String, Serializable> createCommandParameters = new HashMap<String, Serializable>(4);
        createCommandParameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        createCommandParameters.put(EXTERNAL_ID_KEY, externalId1);
        final User user = createUser("Ducobu", "WhatIsAPassword?");
        final long userId = user.getId();
        createCommandParameters.put(USER_ID_KEY, userId);
        // createCommandParameters.put(ROLE_ID_KEY, -1L);
        // createCommandParameters.put(GROUP_ID_KEY, -1L);
        getCommandAPI().execute(CREATE_COMMAND_NAME, createCommandParameters);

        final Role role = createRole("whatever!");
        createCommandParameters.put(ROLE_ID_KEY, role.getId());
        getCommandAPI().execute(CREATE_COMMAND_NAME, createCommandParameters);

        createCommandParameters.put(DISCRIMINATOR_ID_KEY, SUPERVISOR_REPORT_DISCRIMINATOR);
        getCommandAPI().execute(CREATE_COMMAND_NAME, createCommandParameters);

        final HashMap<String, Serializable> searchParameters1 = new HashMap<String, Serializable>(4);
        searchParameters1.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        searchParameters1.put(USER_ID_KEY, userId);
        searchParameters1.put(EXTERNAL_ID_KEY, externalId1);
        searchParameters1.put(SEARCH_OPTIONS_KEY, new SearchOptionsBuilder(0, 10).done());
        SearchResult<EntityMember> entityMembers = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_FOR_USER_COMMAND, searchParameters1);
        assertEquals(2, entityMembers.getCount());

        final HashMap<String, Serializable> searchParameters2 = new HashMap<String, Serializable>(4);
        searchParameters2.put(DISCRIMINATOR_ID_KEY, SUPERVISOR_REPORT_DISCRIMINATOR);
        searchParameters2.put(USER_ID_KEY, userId);
        searchParameters2.put(EXTERNAL_ID_KEY, externalId1);
        searchParameters2.put(SEARCH_OPTIONS_KEY, new SearchOptionsBuilder(0, 10).done());
        SearchResult<EntityMember> entityMembers2 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_FOR_USER_COMMAND, searchParameters2);
        assertEquals(1, entityMembers2.getCount());

        // delete the one for discrim1:
        final HashMap<String, Serializable> deleteCommandParameters = new HashMap<String, Serializable>(2);
        deleteCommandParameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        deleteCommandParameters.put(EXTERNAL_ID_KEY, externalId1);
        getCommandAPI().execute(DELETE_SEVERAL_COMMAND, deleteCommandParameters);

        // Check we deleted only for discrim1:
        entityMembers = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_FOR_USER_COMMAND, searchParameters1);
        assertEquals(0, entityMembers.getCount());
        entityMembers2 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_FOR_USER_COMMAND, searchParameters2);
        assertEquals(1, entityMembers2.getCount());

        // delete the one for discrim2:
        deleteCommandParameters.put(DISCRIMINATOR_ID_KEY, SUPERVISOR_REPORT_DISCRIMINATOR);
        getCommandAPI().execute(DELETE_SEVERAL_COMMAND, deleteCommandParameters);

        // Check we deleted records for discrim2 now:
        entityMembers = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_FOR_USER_COMMAND, searchParameters1);
        assertEquals(0, entityMembers.getCount());
        entityMembers2 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_FOR_USER_COMMAND, searchParameters2);
        assertEquals(0, entityMembers2.getCount());

        deleteUser(user);
        deleteRoles(role);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Search" }, story = "Search entity members involving.", jira = "")
    @Test
    @SuppressWarnings("unchecked")
    public void testSearchEntityMembersInvolving() throws Exception {
        final User newUser = createUser("Barnabé", "Fukushima Daichi");
        final User newUser2 = createUser("test2", "password");
        final Role role = createRole("UnBeauRole");
        final Group group = createGroup("myGroup", "/HR/employee");

        final HashMap<String, Serializable> createCommandParameters = new HashMap<String, Serializable>(4);
        createCommandParameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        createCommandParameters.put(EXTERNAL_ID_KEY, externalId1);
        createCommandParameters.put(USER_ID_KEY, newUser.getId());
        // createCommandParameters.put(ROLE_ID_KEY, -1L);
        // createCommandParameters.put(GROUP_ID_KEY, -1L);
        final EntityMember entityMember = (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, createCommandParameters);

        createCommandParameters.put(USER_ID_KEY, newUser2.getId());
        final EntityMember entityMember2 = (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, createCommandParameters);

        createCommandParameters.put(EXTERNAL_ID_KEY, externalId2);
        final EntityMember entityMember3 = (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, createCommandParameters);

        createCommandParameters.put(EXTERNAL_ID_KEY, externalId1);
        // set for Role:
        createCommandParameters.remove(USER_ID_KEY);
        createCommandParameters.put(ROLE_ID_KEY, role.getId());
        final EntityMember entityMember4 = (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, createCommandParameters);

        HashMap<String, Serializable> searchParameters = new HashMap<String, Serializable>(3);
        searchParameters.put(DISCRIMINATOR_ID_KEY, "INNNNNEXISTENT");
        searchParameters.put(USER_ID_KEY, newUser.getId());
        searchParameters.put(EXTERNAL_ID_KEY, externalId1);
        searchParameters.put(SEARCH_OPTIONS_KEY, new SearchOptionsBuilder(0, 10).done());
        SearchResult<EntityMember> entityMembers = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_FOR_USER_COMMAND, searchParameters);
        // There should be no results returned:
        assertEquals(0, entityMembers.getCount());

        searchParameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        searchParameters.put(USER_ID_KEY, newUser.getId());
        searchParameters.put(EXTERNAL_ID_KEY, externalId1);
        searchParameters.put(SEARCH_OPTIONS_KEY, new SearchOptionsBuilder(0, 10).sort(EntityMemberSearchDescriptor.EXTERNAL_ID, Order.DESC).done());
        entityMembers = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_FOR_USER_COMMAND, searchParameters);
        assertEquals(1, entityMembers.getCount());
        assertEquals(entityMember.getEntityMemberId(), entityMembers.getResult().get(0).getEntityMemberId());

        // test sort by userName
        // final Map<String, Serializable> searchParameters = getSearchParameters(builder, MemberType.USER);
        searchParameters = new HashMap<String, Serializable>();
        searchParameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        searchParameters.put(EXTERNAL_ID_KEY, externalId1);
        searchParameters.put(USER_ID_KEY, newUser.getId());
        searchParameters.put(SEARCH_OPTIONS_KEY, new SearchOptionsBuilder(0, 10).sort(EntityMemberSearchDescriptor.USER_NAME, Order.DESC).done());
        entityMembers = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_FOR_USER_COMMAND, searchParameters);
        assertEquals(1, entityMembers.getCount());
        assertEquals(entityMember.getEntityMemberId(), entityMembers.getResult().get(0).getEntityMemberId());

        // delete the entity members:
        deleteEntityMember(entityMember.getEntityMemberId());
        deleteEntityMember(entityMember2.getEntityMemberId());
        deleteEntityMember(entityMember3.getEntityMemberId());
        deleteEntityMember(entityMember4.getEntityMemberId());

        deleteGroups(group);
        deleteRoles(role);
        deleteUser(newUser);
        deleteUser(newUser2);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Search", "User", "Role" }, story = "Search entity members involving user in role.", jira = "")
    @Test
    @SuppressWarnings("unchecked")
    public void testSearchEntityMembersInvolvingUserInRole() throws Exception {
        final User newUser = createUser("Giulio", "contrasenha");
        final Role role = createRole("OneRingToRoleThemAll");
        final Group group = createGroup("myBand");
        final UserMembership userMembership = createUserMembership(newUser.getUserName(), role.getName(), group.getName());

        final HashMap<String, Serializable> createCommandParameters = new HashMap<String, Serializable>(4);
        createCommandParameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        createCommandParameters.put(EXTERNAL_ID_KEY, externalId1);
        createCommandParameters.put(USER_ID_KEY, newUser.getId());
        // createCommandParameters.put(ROLE_ID_KEY, -1L);
        // createCommandParameters.put(GROUP_ID_KEY, -1L);
        final EntityMember entityMember = (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, createCommandParameters);

        // set for Role:
        createCommandParameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        createCommandParameters.put(EXTERNAL_ID_KEY, externalId2);
        // createCommandParameters.put(USER_ID_KEY, -1L);
        createCommandParameters.put(ROLE_ID_KEY, role.getId());
        // createCommandParameters.put(GROUP_ID_KEY, -1L);
        final EntityMember entityMember2 = (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, createCommandParameters);

        final HashMap<String, Serializable> searchParameters = new HashMap<String, Serializable>(3);
        searchParameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        searchParameters.put(USER_ID_KEY, newUser.getId());
        searchParameters.put(EXTERNAL_ID_KEY, externalId1);
        searchParameters.put(SEARCH_OPTIONS_KEY, new SearchOptionsBuilder(0, 10).sort(EntityMemberSearchDescriptor.EXTERNAL_ID, Order.DESC).done());
        final SearchResult<EntityMember> entityMembers = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_FOR_USER_COMMAND, searchParameters);
        assertEquals(1, entityMembers.getCount());
        assertEquals(entityMember.getEntityMemberId(), entityMembers.getResult().get(0).getEntityMemberId());

        // delete the entity members:
        deleteEntityMember(entityMember.getEntityMemberId());
        deleteEntityMember(entityMember2.getEntityMemberId());

        deleteUserMembership(userMembership.getId());
        deleteGroups(group);
        deleteRoles(role);
        deleteUser(newUser);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Search", "User" }, story = "Search entity members for user.", jira = "")
    @SuppressWarnings("unchecked")
    @Test
    public void testSearchEntityMembersForUser() throws Exception {
        beforeSearchEntityMembersForUser();

        // test ASC
        SearchOptionsBuilder builder = buildSearchOptions(null, 0, 3, EntityMemberSearchDescriptor.DISPLAY_NAME_PART3, Order.ASC);
        final Map<String, Serializable> searchParameters = getSearchParameters(builder, MemberType.USER);

        final SearchResult<EntityMember> result1 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_COMMAND, searchParameters);
        assertEquals(5, result1.getCount());
        List<EntityMember> entityMembers = result1.getResult();
        assertNotNull(entityMembers);
        assertEquals(3, entityMembers.size());
        assertEquals(entityMember1.getUserId(), entityMembers.get(0).getUserId());
        assertEquals("FirstName1", entityMembers.get(0).getDisplayNamePart1());
        assertEquals("LastName1", entityMembers.get(0).getDisplayNamePart2());
        assertEquals("user1", entityMembers.get(0).getDisplayNamePart3());
        assertEquals(entityMember2.getUserId(), entityMembers.get(1).getUserId());
        assertEquals(entityMember3.getUserId(), entityMembers.get(2).getUserId());

        builder = buildSearchOptions(null, 3, 3, EntityMemberSearchDescriptor.DISPLAY_NAME_PART3, Order.ASC);
        searchParameters.put(SEARCH_OPTIONS_KEY, builder.done());
        final SearchResult<EntityMember> result2 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_COMMAND, searchParameters);
        assertEquals(5, result2.getCount());
        entityMembers = result2.getResult();
        assertNotNull(entityMembers);
        assertEquals(2, entityMembers.size());
        assertEquals(entityMember4.getUserId(), entityMembers.get(0).getUserId());
        assertEquals(entityMember5.getUserId(), entityMembers.get(1).getUserId());

        // test DESC
        builder = buildSearchOptions(null, 0, 2, EntityMemberSearchDescriptor.DISPLAY_NAME_PART3, Order.DESC);
        searchParameters.put(SEARCH_OPTIONS_KEY, builder.done());
        final SearchResult<EntityMember> result4 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_COMMAND, searchParameters);
        assertEquals(5, result4.getCount());
        entityMembers = result4.getResult();
        assertNotNull(entityMembers);
        assertEquals(2, entityMembers.size());
        assertEquals(entityMember5.getUserId(), entityMembers.get(0).getUserId());
        assertEquals(entityMember4.getUserId(), entityMembers.get(1).getUserId());

        // clean-up
        afterSearchEntityMembersForUser();
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Search", "User", "Filter" }, story = "Search entity members for user with filter.", jira = "")
    @SuppressWarnings("unchecked")
    @Test
    public void testSearchEntityMembersForUserWithFilter() throws Exception {
        beforeSearchEntityMembersForUser();

        // filter on process
        Map<String, Serializable> filters = Collections.singletonMap(EntityMemberSearchDescriptor.EXTERNAL_ID, (Serializable) externalId2);
        SearchOptionsBuilder builder = buildSearchOptions(filters, 0, 3, EntityMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        final Map<String, Serializable> searchParameters = getSearchParameters(builder, MemberType.USER);

        final SearchResult<EntityMember> result1 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_COMMAND, searchParameters);
        assertEquals(2, result1.getCount());
        List<EntityMember> entityMembers = result1.getResult();
        assertEquals(entityMember4.getUserId(), entityMembers.get(0).getUserId());
        assertEquals(entityMember5.getUserId(), entityMembers.get(1).getUserId());

        // filter on display name 1
        filters = Collections.singletonMap(EntityMemberSearchDescriptor.DISPLAY_NAME_PART1, (Serializable) "FirstName1");
        builder = buildSearchOptions(filters, 0, 3, EntityMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        searchParameters.put(SEARCH_OPTIONS_KEY, builder.done());
        final SearchResult<EntityMember> result2 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_COMMAND, searchParameters);
        assertEquals(1, result2.getCount());
        entityMembers = result2.getResult();
        assertEquals(entityMember1.getUserId(), entityMembers.get(0).getUserId());

        // clean-up
        afterSearchEntityMembersForUser();
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Search", "User", "Search term" }, story = "Search entity members for user with search term.", jira = "")
    @SuppressWarnings("unchecked")
    @Test
    public void testSearchEntityMembersForUserWithSearchTerm() throws Exception {
        beforeSearchEntityMembersForUser();

        final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 3, EntityMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.searchTerm("FirstName1");
        final Map<String, Serializable> searchParameters = getSearchParameters(builder, MemberType.USER);

        final SearchResult<EntityMember> result1 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_COMMAND, searchParameters);
        assertEquals(1, result1.getCount());
        final List<EntityMember> entityMembers = result1.getResult();
        assertEquals(entityMember1.getUserId(), entityMembers.get(0).getUserId());

        // clean-up
        afterSearchEntityMembersForUser();
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Search", "Group" }, story = "Search entity members for group.", jira = "")
    @SuppressWarnings("unchecked")
    @Test
    public void testSearchEntityMembersForGroup() throws Exception {
        beforeSearchEntityMembersForGroup();
        final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 5, EntityMemberSearchDescriptor.EXTERNAL_ID, Order.ASC);
        final Map<String, Serializable> searchParameters = getSearchParameters(builder, MemberType.GROUP);

        final SearchResult<EntityMember> result1 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_COMMAND, searchParameters);
        assertEquals(2, result1.getCount());
        final List<EntityMember> entityMembers = result1.getResult();
        assertEquals(group1.getId(), entityMembers.get(0).getGroupId());
        assertEquals(group2.getId(), entityMembers.get(1).getGroupId());

        // clean-up
        afterSearchEntityMembersForGroup();
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Search", "Role" }, story = "Search entity members for role.", jira = "")
    @SuppressWarnings("unchecked")
    @Test
    public void testSearchEntityMembersForRole() throws Exception {
        beforeSearchEntityMembersForRole();

        final SearchOptionsBuilder builder = buildSearchOptions(null, 0, 5, EntityMemberSearchDescriptor.EXTERNAL_ID, Order.ASC);
        final Map<String, Serializable> searchParameters = getSearchParameters(builder, MemberType.ROLE);

        final SearchResult<EntityMember> result1 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_COMMAND, searchParameters);
        assertEquals(2, result1.getCount());
        final List<EntityMember> entityMembers = result1.getResult();
        assertEquals(role1.getId(), entityMembers.get(0).getRoleId());
        assertEquals(role2.getId(), entityMembers.get(1).getRoleId());

        // clean-up
        afterSearchEntityMembersForRole();
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Search", "Role", "Group" }, story = "Search entity members for role and group.", jira = "")
    @SuppressWarnings("unchecked")
    @Test
    public void testSearchEntityMembersForRoleAndGroup() throws Exception {
        beforeSearchEntityMembersForRoleAndGroup();

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 5);
        builder.sort(EntityMemberSearchDescriptor.EXTERNAL_ID, Order.ASC);
        builder.sort(EntityMemberSearchDescriptor.ROLE_ID, Order.ASC);
        final Map<String, Serializable> searchParameters = getSearchParameters(builder, MemberType.MEMBERSHIP);

        final SearchResult<EntityMember> result = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_COMMAND, searchParameters);
        assertEquals(3, result.getCount());
        final List<EntityMember> entityMembers = result.getResult();
        assertEquals(role1.getId(), entityMembers.get(0).getRoleId());
        assertEquals(group1.getId(), entityMembers.get(0).getGroupId());
        assertEquals(role2.getId(), entityMembers.get(1).getRoleId());
        assertEquals(group2.getId(), entityMembers.get(1).getGroupId());
        assertEquals(role1.getId(), entityMembers.get(2).getRoleId());
        assertEquals(group2.getId(), entityMembers.get(2).getGroupId());

        // clean-up
        afterSearchEntityMembersForRoleAndGroup();
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Search", "User" }, story = "Search entity members for user.", jira = "")
    @SuppressWarnings("unchecked")
    @Test
    public void searchEntityMembersForUser() throws Exception {
        // install two reports. add a supervisor member (user) to report1. check report2's supervisor member (user) list. it should be empty.
        // create a user
        final User user1 = createUser("John", "bpm", "FirstName1", "LastName1");
        final String report1 = "externalId1";
        final String report2 = "externalId2";

        // create a EntityMember1
        final HashMap<String, Serializable> createCommandParameters1 = new HashMap<String, Serializable>(3);
        createCommandParameters1.put(DISCRIMINATOR_ID_KEY, SUPERVISOR_REPORT_DISCRIMINATOR);
        createCommandParameters1.put(EXTERNAL_ID_KEY, report1);
        createCommandParameters1.put(USER_ID_KEY, user1.getId());
        final EntityMember member1 = (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, createCommandParameters1);

        SearchOptionsBuilder builder = buildSearchOptions(null, 0, 3, EntityMemberSearchDescriptor.DISPLAY_NAME_PART3, Order.ASC);
        builder.filter(EntityMemberSearchDescriptor.EXTERNAL_ID, report1);
        final Map<String, Serializable> searchParameters1 = new HashMap<String, Serializable>(3);
        searchParameters1.put(DISCRIMINATOR_ID_KEY, SUPERVISOR_REPORT_DISCRIMINATOR);
        searchParameters1.put(SEARCH_OPTIONS_KEY, builder.done());
        searchParameters1.put(MEMBER_TYPE_KEY, MemberType.USER);

        final SearchResult<EntityMember> memberResult1 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_COMMAND, searchParameters1);
        assertEquals(1, memberResult1.getCount());
        assertEquals(member1.getExternalId(), report1);
        assertEquals(member1.getUserId(), user1.getId());

        builder = buildSearchOptions(null, 0, 3, EntityMemberSearchDescriptor.DISPLAY_NAME_PART3, Order.ASC);
        builder.filter(EntityMemberSearchDescriptor.EXTERNAL_ID, report2);
        final Map<String, Serializable> searchParameters2 = new HashMap<String, Serializable>(3);
        searchParameters2.put(DISCRIMINATOR_ID_KEY, SUPERVISOR_REPORT_DISCRIMINATOR);
        searchParameters2.put(SEARCH_OPTIONS_KEY, builder.done());
        searchParameters2.put(MEMBER_TYPE_KEY, MemberType.USER);

        final SearchResult<EntityMember> memberResult2 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_COMMAND, searchParameters2);
        assertEquals(0, memberResult2.getCount());

        deleteUser(user1);
        deleteEntityMembers(member1);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Search" }, story = "Search entity members.", jira = "")
    @SuppressWarnings("unchecked")
    @Test
    public void searchEntityMembers() throws Exception {
        // install a report. add a role to report's supervisor. add a group to report's user. retrieve the corresponding group list and role list of report
        // supervisor and report user. No exceptions should be thrown.
        final Role role1 = createRole("role1");
        final Group group1 = createGroup("group1", "root");
        final User user1 = createUser("John", "bpm", "FirstName1", "LastName1");
        final String report1 = "externalId1";

        // add a role to report's supervisor
        final HashMap<String, Serializable> createCommandParameters1 = new HashMap<String, Serializable>(3);
        createCommandParameters1.put(DISCRIMINATOR_ID_KEY, SUPERVISOR_REPORT_DISCRIMINATOR);
        createCommandParameters1.put(EXTERNAL_ID_KEY, report1);
        createCommandParameters1.put(ROLE_ID_KEY, role1.getId());
        final EntityMember member1 = (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, createCommandParameters1);

        // add a group to report's user
        final HashMap<String, Serializable> createCommandParameters2 = new HashMap<String, Serializable>(3);
        createCommandParameters2.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        createCommandParameters2.put(EXTERNAL_ID_KEY, report1);
        createCommandParameters2.put(GROUP_ID_KEY, group1.getId());
        final EntityMember member2 = (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, createCommandParameters2);

        // it can not use DISPLAY_NAME_PART3 to be ORDER BY, refer class SearchEntityMemberGroupDescriptor
        SearchOptionsBuilder builder = buildSearchOptions(null, 0, 3, EntityMemberSearchDescriptor.DISPLAY_NAME_PART2, Order.ASC);
        builder.filter(EntityMemberSearchDescriptor.EXTERNAL_ID, report1);
        final Map<String, Serializable> searchParameters1 = new HashMap<String, Serializable>(3);
        searchParameters1.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        searchParameters1.put(SEARCH_OPTIONS_KEY, builder.done());
        searchParameters1.put(MEMBER_TYPE_KEY, MemberType.GROUP);

        final SearchResult<EntityMember> memberResult1 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_COMMAND, searchParameters1);
        assertEquals(1, memberResult1.getCount());
        assertEquals(member2.getExternalId(), report1);
        assertEquals(member2.getGroupId(), group1.getId());

        // it can not use DISPLAY_NAME_PART3 or DISPLAY_NAME_PART2 to be ORDER BY, refer class SearchEntityMemberRoleDescriptor
        builder = buildSearchOptions(null, 0, 3, EntityMemberSearchDescriptor.DISPLAY_NAME_PART1, Order.ASC);
        builder.filter(EntityMemberSearchDescriptor.EXTERNAL_ID, report1);
        final Map<String, Serializable> searchParameters2 = new HashMap<String, Serializable>(3);
        searchParameters2.put(DISCRIMINATOR_ID_KEY, SUPERVISOR_REPORT_DISCRIMINATOR);
        searchParameters2.put(SEARCH_OPTIONS_KEY, builder.done());
        searchParameters2.put(MEMBER_TYPE_KEY, MemberType.ROLE);

        final SearchResult<EntityMember> memberResult2 = (SearchResult<EntityMember>) getCommandAPI().execute(SEARCH_COMMAND, searchParameters2);
        assertEquals(1, memberResult2.getCount());
        assertEquals(member1.getExternalId(), report1);
        assertEquals(member1.getRoleId(), role1.getId());

        deleteRoles(role1);
        deleteGroups(group1);
        deleteUser(user1);
        deleteEntityMembers(member1, member2);
    }

    private Map<String, Serializable> getSearchParameters(final SearchOptionsBuilder builder, final MemberType memberType) {
        final Map<String, Serializable> searchParameters = new HashMap<String, Serializable>(3);
        searchParameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        searchParameters.put(SEARCH_OPTIONS_KEY, builder.done());
        searchParameters.put(MEMBER_TYPE_KEY, memberType);
        return searchParameters;
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

    private void deleteEntityMembers(final EntityMember... entityMembers) throws BonitaException {
        if (entityMembers != null) {
            for (final EntityMember entityMember : entityMembers) {
                deleteEntityMember(entityMember.getEntityMemberId());
            }
        }
    }

    private void deleteEntityMember(final Long id) throws CommandNotFoundException, CommandParameterizationException, CommandExecutionException {
        final Map<String, Serializable> parameters = new HashMap<String, Serializable>(2);
        parameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        parameters.put(ENTITY_MEMBER_ID_KEY, id);
        getCommandAPI().execute(DELETE_COMMAND_NAME, parameters);
    }

    private EntityMember createUserEntityMember(final String externalId, final long userId) throws BonitaException {
        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>(3);
        parameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        parameters.put(EXTERNAL_ID_KEY, externalId);
        parameters.put(USER_ID_KEY, userId);
        return (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, parameters);
    }

    private EntityMember createGroupEntityMember(final String externalId, final long groupId) throws BonitaException {
        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>(3);
        parameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        parameters.put(EXTERNAL_ID_KEY, externalId);
        parameters.put(GROUP_ID_KEY, groupId);
        return (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, parameters);
    }

    private EntityMember createRoleEntityMember(final String externalId, final long roleId) throws BonitaException {
        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>(3);
        parameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        parameters.put(EXTERNAL_ID_KEY, externalId);
        parameters.put(ROLE_ID_KEY, roleId);
        return (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, parameters);
    }

    private EntityMember createMembershipEntityMember(final String externalId, final long roleId, final long groupId) throws BonitaException {
        final HashMap<String, Serializable> parameters = new HashMap<String, Serializable>(4);
        parameters.put(DISCRIMINATOR_ID_KEY, USER_REPORT_DISCRIMINATOR);
        parameters.put(EXTERNAL_ID_KEY, externalId);
        parameters.put(ROLE_ID_KEY, roleId);
        parameters.put(GROUP_ID_KEY, groupId);
        return (EntityMember) getCommandAPI().execute(CREATE_COMMAND_NAME, parameters);
    }

    private void afterSearchEntityMembersForUser() throws BonitaException {
        deleteEntityMembers(entityMember1, entityMember2, entityMember3, entityMember4, entityMember5);
        deleteUsers(user1, user2, user3, user4, user5);
    }

    private void afterSearchEntityMembersForGroup() throws BonitaException {
        deleteEntityMembers(entityMember1, entityMember2);
        deleteGroups(group1, group2);
    }

    private void afterSearchEntityMembersForRole() throws BonitaException {
        deleteEntityMembers(entityMember1, entityMember2);
        deleteRoles(role1, role2);
    }

    private void afterSearchEntityMembersForRoleAndGroup() throws BonitaException {
        deleteEntityMembers(entityMember1, entityMember2, entityMember3);
        deleteRoles(role1, role2);
        deleteGroups(group1, group2);
    }

    private void beforeSearchEntityMembersForUser() throws BonitaException {
        // create users:
        createUsers();
        // add entity members:
        createUserEntityMembers();
    }

    private void beforeSearchEntityMembersForGroup() throws BonitaException {
        createGroups();
        createGroupEntityMembers();
    }

    private void beforeSearchEntityMembersForRole() throws BonitaException {
        createRoles();
        createRoleEntityMembers();
    }

    private void beforeSearchEntityMembersForRoleAndGroup() throws BonitaException {
        createGroups();
        createRoles();
        createMembershipEntityMembers();
    }

    private void createUserEntityMembers() throws BonitaException {
        entityMember1 = createUserEntityMember(externalId1, user1.getId());
        assertEquals("FirstName1", entityMember1.getDisplayNamePart1());
        assertEquals("LastName1", entityMember1.getDisplayNamePart2());
        assertEquals("user1", entityMember1.getDisplayNamePart3());
        entityMember2 = createUserEntityMember(externalId1, user2.getId());
        entityMember3 = createUserEntityMember(externalId1, user3.getId());
        entityMember4 = createUserEntityMember(externalId2, user4.getId());
        entityMember5 = createUserEntityMember(externalId2, user5.getId());

    }

    private void createUsers() throws BonitaException {
        user1 = createUser("user1", "bpm", "FirstName1", "LastName1");
        user2 = createUser("user2", "bpm", "FirstName2", "LastName2");
        user3 = createUser("user3", "bpm", "FirstName3", "LastName3");
        user4 = createUser("user4", "bpm", "FirstName4", "LastName4");
        user5 = createUser("user5", "bpm", "FirstName5", "LastName5");
    }

    private void createGroups() throws BonitaException {
        group1 = createGroup("group1", "root");
        group2 = createGroup("group2", "level2");
    }

    private void createRoles() throws BonitaException {
        role1 = createRole("role1");
        role2 = createRole("role2");
    }

    private void createGroupEntityMembers() throws BonitaException {
        entityMember1 = createGroupEntityMember(externalId1, group1.getId());
        assertEquals("group1", entityMember1.getDisplayNamePart1());
        assertEquals("root", entityMember1.getDisplayNamePart2());
        assertEquals(null, entityMember1.getDisplayNamePart3());
        entityMember2 = createGroupEntityMember(externalId2, group2.getId());
        entityMember3 = null;
        entityMember4 = null;
        entityMember5 = null;
    }

    private void createRoleEntityMembers() throws BonitaException {
        entityMember1 = createRoleEntityMember(externalId1, role1.getId());
        assertEquals("role1", entityMember1.getDisplayNamePart1());
        assertEquals(null, entityMember1.getDisplayNamePart2());
        assertEquals(null, entityMember1.getDisplayNamePart3());
        entityMember2 = createRoleEntityMember(externalId2, role2.getId());
        entityMember3 = null;
        entityMember4 = null;
        entityMember5 = null;
    }

    private void createMembershipEntityMembers() throws BonitaException {
        entityMember1 = createMembershipEntityMember(externalId1, role1.getId(), group1.getId());
        assertEquals("role1", entityMember1.getDisplayNamePart1());
        assertEquals("group1", entityMember1.getDisplayNamePart2());
        assertEquals("root", entityMember1.getDisplayNamePart3());
        entityMember2 = createMembershipEntityMember(externalId1, role2.getId(), group2.getId());
        entityMember3 = createMembershipEntityMember(externalId2, role1.getId(), group2.getId());
        entityMember4 = null;
        entityMember5 = null;
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Create", "Wrong parameter" }, story = "Execute entity member command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = CommandParameterizationException.class)
    public void testAddEntityMemberCommandWithWrongParameter() throws Exception {

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_KEY", "bad_value");

        getCommandAPI().execute(CREATE_COMMAND_NAME, parameters);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Delete", "Wrong parameter" }, story = "Execute entity member command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = CommandParameterizationException.class)
    public void testRemoveEntityMemberCommandWithWrongParameter() throws Exception {

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_KEY", "bad_value");

        getCommandAPI().execute(DELETE_COMMAND_NAME, parameters);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Search", "User", "Wrong parameter" }, story = "Execute entity member command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = CommandParameterizationException.class)
    public void testSearchEntityMembersForUserCommandWithWrongParameter() throws Exception {

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_KEY", "bad_value");

        getCommandAPI().execute(SEARCH_FOR_USER_COMMAND, parameters);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Search", "Wrong parameter" }, story = "Execute entity member command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = CommandParameterizationException.class)
    public void testSearchEntityMembersCommandWithWrongParameter() throws Exception {

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_KEY", "bad_value");

        getCommandAPI().execute(SEARCH_COMMAND, parameters);
    }

    @Cover(classes = CommandAPI.class, concept = BPMNConcept.MEMBER, keywords = { "Command", "Member", "Entity", "Delete", "Wrong parameter" }, story = "Execute entity member command with wrong parameter", jira = "ENGINE-586")
    @Test(expected = CommandParameterizationException.class)
    public void testDeleteEntityMembersCommandWithWrongParameter() throws Exception {

        final Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put("BAD_KEY", "bad_value");

        getCommandAPI().execute(DELETE_SEVERAL_COMMAND, parameters);
    }

}
