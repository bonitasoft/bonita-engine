/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.identity.model.SContactInfo;
import org.bonitasoft.engine.identity.model.SCustomUserInfoDefinition;
import org.bonitasoft.engine.identity.model.SGroup;
import org.bonitasoft.engine.identity.model.SRole;
import org.bonitasoft.engine.identity.model.SUser;
import org.bonitasoft.engine.identity.model.SUserMembership;
import org.bonitasoft.engine.identity.model.builder.SContactInfoUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SGroupUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SRoleUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserUpdateBuilderFactory;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SearchFields;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.test.util.TestUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 */
public class IdentityServiceIT extends CommonBPMServicesTest {

    private static IdentityService identityService;

    @Before
    public void before() {
        identityService = getTenantAccessor().getIdentityService();
    }

    @After
    public void tearDown() throws Exception {
        TestUtil.closeTransactionIfOpen(getTransactionService());

        getTransactionService().begin();
        deleteUserMemberships();
        deleteRoles();
        deleteGroups();
        deleteUsers();
        getTransactionService().complete();
    }

    private void deleteUserMemberships() throws SIdentityException {
        final List<SUserMembership> memberships = identityService.getUserMemberships(0, 5000);
        for (final SUserMembership sMembership : memberships) {
            identityService.deleteUserMembership(sMembership);
        }
    }

    @Test
    public void testAddUser() throws Exception {
        getTransactionService().begin();
        final SUser user = SUser.builder().userName("john").password("bpm").build();
        identityService.createUser(user);
        getTransactionService().complete();

        getTransactionService().begin();
        final SUser user2 = identityService.getUserByUserName("john");
        assertNotNull("can't find the user after adding it", user2);
        assertEquals(user.getUserName(), user2.getUserName());
        assertNotSame(user.getPassword(), user2.getPassword());
        assertEquals(user.getCreationDate(), user2.getCreationDate());
        // all fields
        getTransactionService().complete();
    }

    @Test
    public void testAddUserWithId() throws Exception {
        getTransactionService().begin();
        final SUser user = SUser.builder().userName("testAddUserWithId").password("bpm").build();
        assertEquals(0, user.getId());
        final SUser updatedUser = identityService.createUser(user);
        assertNotSame("The identifier must be set after adding a user", 0, updatedUser.getId());
        getTransactionService().complete();
    }

    @Test
    public void testAddUsersWithoutIds() throws Exception {
        getTransactionService().begin();
        final SUser user1 = SUser.builder().userName("testAddUsersWithoutIds7").password("bpm").build();
        identityService.createUser(user1);
        getTransactionService().complete();

        getTransactionService().begin();
        final SUser user2 = SUser.builder().userName("testAddUsersWithoutIds2").password("bpm").build();
        final SUser user3 = SUser.builder().userName("testAddUsersWithoutIds3").password("bpm").build();
        final SUser user4 = SUser.builder().userName("testAddUsersWithoutIds4").password("bpm").build();
        final SUser user5 = SUser.builder().userName("testAddUsersWithoutIds5").password("bpm").build();
        final SUser user6 = SUser.builder().userName("testAddUsersWithoutIds6").password("bpm").build();
        identityService.createUser(user2);
        identityService.createUser(user3);
        identityService.createUser(user4);
        identityService.createUser(user5);
        identityService.createUser(user6);
        getTransactionService().complete();
    }

    @Test
    public void testGetUserByUserName() throws Exception {
        getTransactionService().begin();
        final String username = "myUser";
        final SUser user = SUser.builder().userName(username).password(username).build();
        identityService.createUser(user);
        final SUser user2 = identityService.getUserByUserName(username);
        getTransactionService().complete();
        assertNotNull("can't find the user after adding it", user2);
        assertEquals("Does not retrieved the good user", username, user2.getUserName());
    }

    @Test(expected = SIdentityException.class)
    public void testGetUserByUsernameNotExists() throws Exception {
        getTransactionService().begin();
        final String username = "unexistingError";
        identityService.getUserByUserName(username);
        getTransactionService().complete();
    }

    @Test
    public void testGetRoleByName() throws Exception {
        getTransactionService().begin();
        final String roleName = "myRole";
        final SRole role = SRole.builder().name(roleName).build();
        identityService.createRole(role, null, null);
        final SRole role2 = identityService.getRoleByName(roleName);
        getTransactionService().complete();
        assertNotNull("can't find the role after adding it", role2);
        assertEquals("Does not retrieved the good role", roleName, role2.getName());
    }

    @Test
    public void testGetProfileMetadataByName() throws Exception {
        getTransactionService().begin();
        final String name = "MyProfileMetadata";
        final SCustomUserInfoDefinition metadata = SCustomUserInfoDefinition.builder().name(name).build();
        identityService.createCustomUserInfoDefinition(metadata);
        final SCustomUserInfoDefinition metadata2 = identityService.getCustomUserInfoDefinitionByName(name);
        assertNotNull("can't find the profile metadata after adding it", metadata2);
        assertEquals("Does not retrieved the good profile metadata", name, metadata2.getName());
        getTransactionService().complete();
    }

    /*
     * Getters that use objects Ids
     */

    @Test
    public void testGetUser() throws Exception {
        getTransactionService().begin();
        final SUser.SUserBuilder userBuilder = SUser.builder().userName("Seppo").password("kikoo");
        final SUser seppo = identityService.createUser(userBuilder.build());
        final SUser user2 = identityService.getUser(seppo.getId());
        getTransactionService().complete();

        assertNotNull("can't find the user after adding it", user2);
        assertEquals("Does not retrieved the good user", seppo.getId(), user2.getId());
    }

    @Test(expected = SIdentityException.class)
    public void testGetUnexistingUser() throws Exception {
        getTransactionService().begin();
        identityService.getUser(1254863);
        getTransactionService().complete();
    }

    @Test
    public void testGetRole() throws Exception {
        getTransactionService().begin();
        final long id = new Date().getTime();
        final SRole testGetRole = SRole.builder().name("testGetRole").id(id).build();
        identityService.createRole(testGetRole, null, null);
        final SRole role2 = identityService.getRole(id);
        getTransactionService().complete();

        assertNotNull("can't find the role after adding it", role2);
        assertEquals("Does not retrieved the good role", id, role2.getId());
    }

    @Test
    public void testGetGroup() throws Exception {
        getTransactionService().begin();
        final long id = new Date().getTime();
        final SGroup testGetGroup = SGroup.builder().name("testGetGroup").id(id).build();
        identityService.createGroup(testGetGroup, null, null);
        getTransactionService().complete();

        getTransactionService().begin();
        final SGroup group2 = identityService.getGroup(id);
        getTransactionService().complete();

        assertNotNull("can't find the group after adding it", group2);
        assertEquals("Does not retrieved the good group", id, group2.getId());
    }

    @Test
    public void testGetGroupByPath() throws Exception {
        getTransactionService().begin();
        final SGroup group = SGroup.builder().name("R&D").build();
        identityService.createGroup(group, null, null);
        final SGroup subGroup = SGroup.builder().parentPath(group.getPath()).name("R&D").build();
        identityService.createGroup(subGroup, null, null);
        getTransactionService().complete();

        getTransactionService().begin();
        SGroup actual = identityService.getGroupByPath("R&D");
        assertEquals(group, actual);
        actual = identityService.getGroupByPath("/R&D");
        assertEquals(group, actual);
        actual = identityService.getGroupByPath("/R&D/R&D");
        assertEquals(subGroup, actual);
        getTransactionService().complete();
    }

    @Test
    public void testGetProfileMetadata() throws Exception {
        getTransactionService().begin();
        final long id = new Date().getTime();
        final SCustomUserInfoDefinition testGetProfileMetadata = SCustomUserInfoDefinition.builder()
                .name("testGetProfileMetadata").id(id).build();
        identityService.createCustomUserInfoDefinition(testGetProfileMetadata);
        final SCustomUserInfoDefinition metadata2 = identityService.getCustomUserInfoDefinition(id);
        getTransactionService().complete();

        assertNotNull("can't find the metadata after adding it", metadata2);
        assertEquals("Does not retrieved the good metadata", id, metadata2.getId());
    }

    @Test
    public void testGetUsers() throws Exception {
        getTransactionService().begin();
        final SUser user1 = SUser.builder().userName("Akseli").password("kikoo").build();
        final long id1 = identityService.createUser(user1).getId();
        final SUser user2 = SUser.builder().userName("Anja").password("kikoo").build();
        final long id2 = identityService.createUser(user2).getId();

        final List<SUser> retrievedUsers = identityService.getUsers(Arrays.asList(id1, id2));
        getTransactionService().complete();

        assertNotNull("can't find the users after adding them", retrievedUsers);
        assertEquals("bad number of retrieved users", 2, retrievedUsers.size());
        assertTrue("does not contains user 1",
                retrievedUsers.get(0).getId() == id1 || retrievedUsers.get(1).getId() == id1);
        assertTrue("does not contains user 2",
                retrievedUsers.get(1).getId() == id2 || retrievedUsers.get(0).getId() == id2);
    }

    @Test
    public void testGetUsersFromNullListIds() throws Exception {
        final SUser eetu = SUser.builder().userName("Eetu").password("kikoo").build();
        final SUser inkeri = SUser.builder().userName("Inkeri").password("kikoo").build();
        getTransactionService().begin();
        identityService.createUser(eetu);
        identityService.createUser(inkeri);
        final List<SUser> retrievedUsers = identityService.getUsers(null);
        getTransactionService().complete();

        assertEquals(0, retrievedUsers.size());
    }

    @Test
    public void testGetUsersFromEmptyListIds() throws Exception {
        final SUser lauri = SUser.builder().userName("lauri").password("kikoo").build();
        final SUser mika = SUser.builder().userName("mika").password("kikoo").build();
        getTransactionService().begin();
        identityService.createUser(lauri);
        identityService.createUser(mika);
        final List<SUser> retrievedUsers = identityService.getUsers(Collections.emptyList());
        getTransactionService().complete();

        assertEquals(0, retrievedUsers.size());
    }

    @Test
    public void testGetRoles() throws Exception {
        getTransactionService().begin();
        final long id1 = new Date().getTime();
        final SRole role1 = SRole.builder().name("testGetRoles1").id(id1).build();
        identityService.createRole(role1, null, null);
        final long id2 = id1 + 1L;
        final SRole role2 = SRole.builder().name("testGetRoles2").id(id2).build();
        identityService.createRole(role2, null, null);

        final List<SRole> retrievedUsers = identityService.getRoles(Arrays.asList(new Long[] { id1, id2 }));
        getTransactionService().complete();

        assertNotNull("can't find the roles after adding them", retrievedUsers);
        assertEquals("bad number of retrieved roles", 2, retrievedUsers.size());
        assertTrue("does not contains role 1",
                retrievedUsers.get(0).getId() == id1 || retrievedUsers.get(1).getId() == id1);
        assertTrue("does not contains role 2",
                retrievedUsers.get(1).getId() == id2 || retrievedUsers.get(0).getId() == id2);
    }

    @Test
    public void testGetRolesFromNullListids() throws Exception {
        getTransactionService().begin();
        final long id1 = new Date().getTime();
        final SRole role1 = SRole.builder().name("testGetRoles1").id(id1).build();
        identityService.createRole(role1, null, null);
        final long id2 = id1 + 1L;
        final SRole role2 = SRole.builder().name("testGetRoles2").id(id2).build();
        identityService.createRole(role2, null, null);

        final List<SRole> retrievedUsers = identityService.getRoles(null);
        getTransactionService().complete();

        assertEquals(0, retrievedUsers.size());
    }

    @Test
    public void testGetRolesFromEmptyListIds() throws Exception {
        getTransactionService().begin();
        final long id1 = new Date().getTime();
        final SRole role1 = SRole.builder().name("testGetRoles1").id(id1).build();
        identityService.createRole(role1, null, null);
        final long id2 = id1 + 1L;
        final SRole role2 = SRole.builder().name("testGetRoles2").id(id2).build();
        identityService.createRole(role2, null, null);

        final List<SRole> retrievedUsers = identityService.getRoles(Collections.emptyList());
        getTransactionService().complete();

        assertEquals(0, retrievedUsers.size());
    }

    @Test
    public void testGetGroups() throws Exception {
        getTransactionService().begin();
        final long id1 = new Date().getTime();
        final SGroup group1 = SGroup.builder().name("testGetGroups1").id(id1).build();
        identityService.createGroup(group1, null, null);
        final long id2 = id1 + 1L;
        final SGroup group2 = SGroup.builder().name("testGetGroups2").id(id2).build();
        identityService.createGroup(group2, null, null);

        final List<SGroup> retrievedGroups = identityService.getGroups(Arrays.asList(new Long[] { id1, id2 }));
        getTransactionService().complete();

        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved groups", 2, retrievedGroups.size());
        assertTrue("does not contains group 1",
                retrievedGroups.get(0).getId() == id1 || retrievedGroups.get(1).getId() == id1);
        assertTrue("does not contains group 2",
                retrievedGroups.get(1).getId() == id2 || retrievedGroups.get(0).getId() == id2);
    }

    @Test
    public void testGetGroupsFromNullListIds() throws Exception {
        getTransactionService().begin();
        final long id1 = new Date().getTime();
        final SGroup group1 = SGroup.builder().name("testGetGroups1").id(id1).build();
        identityService.createGroup(group1, null, null);
        final long id2 = id1 + 1L;
        final SGroup group2 = SGroup.builder().name("testGetGroups2").id(id2).build();
        identityService.createGroup(group2, null, null);

        final List<SGroup> retrievedGroups = identityService.getGroups(null);
        getTransactionService().complete();

        assertEquals(0, retrievedGroups.size());
    }

    @Test
    public void testGetGroupsFromEmptyListIds() throws Exception {
        getTransactionService().begin();
        final long id1 = new Date().getTime();
        final SGroup group1 = SGroup.builder().name("testGetGroups1").id(id1).build();
        identityService.createGroup(group1, null, null);
        final long id2 = id1 + 1L;
        final SGroup group2 = SGroup.builder().name("testGetGroups2").id(id2).build();
        identityService.createGroup(group2, null, null);

        final List<SGroup> retrievedGroups = identityService.getGroups(Collections.emptyList());
        getTransactionService().complete();

        assertEquals(0, retrievedGroups.size());
    }

    @Test
    public void testGetRolesPaginated() throws Exception {
        getTransactionService().begin();
        long id;
        SRole role;
        final long time = new Date().getTime();
        for (int i = 0; i < 30; i++) {
            id = time + i;
            role = SRole.builder().name("testGetRolesPaginated" + i).id(id).build();
            identityService.createRole(role, null, null);
        }

        List<SRole> retrievedRoles = identityService.getRoles(5, 5);
        assertNotNull("can't find the roles after adding them", retrievedRoles);
        assertEquals("bad number of retrieved roles", 5, retrievedRoles.size());

        retrievedRoles = identityService.getRoles(0, 20);
        getTransactionService().complete();
        assertNotNull("can't find the roles after adding them", retrievedRoles);
        assertEquals("bad number of retrieved roles", 20, retrievedRoles.size());
    }

    @Test
    public void testGetRolesOrderByName() throws Exception {
        getTransactionService().begin();
        createRoles(10, "testGetRolesOrderByName_name", "testGetRolesOrderByName_label");
        List<SRole> retrievedRoles = identityService.getRoles(5, 5, SRole.NAME, OrderByType.DESC);
        assertNotNull("can't find the roles after adding them", retrievedRoles);
        assertEquals("bad number of retrieved roles", 5, retrievedRoles.size());
        assertTrue("not in descending order",
                retrievedRoles.get(1).getName().compareTo(retrievedRoles.get(2).getName()) > 0);

        retrievedRoles = identityService.getRoles(5, 5, SRole.NAME, OrderByType.ASC);
        getTransactionService().complete();
        assertNotNull("can't find the roles after adding them", retrievedRoles);
        assertEquals("bad number of retrieved roles", 5, retrievedRoles.size());
        assertTrue(
                "not in asc order: first= " + retrievedRoles.get(0).getName() + "  second = "
                        + retrievedRoles.get(3).getName(),
                retrievedRoles.get(0)
                        .getName().compareTo(retrievedRoles.get(3).getName()) < 0);
    }

    @Test
    public void testGetRolesOrderByLabel() throws Exception {
        getTransactionService().begin();
        deleteUsers();
        deleteRoles();
        createRoles(10, "testGetRolesOrderByLabel_name", "testGetRolesOrderByLabel_label");
        List<SRole> retrievedRoles = identityService.getRoles(5, 5, SRole.DISPLAY_NAME, OrderByType.DESC);
        assertNotNull("can't find the roles after adding them", retrievedRoles);
        assertEquals("bad number of retrieved roles", 5, retrievedRoles.size());
        assertTrue("not in descending order",
                retrievedRoles.get(1).getDisplayName().compareTo(retrievedRoles.get(2).getDisplayName()) > 0);

        retrievedRoles = identityService.getRoles(5, 5, SRole.DISPLAY_NAME, OrderByType.ASC);
        getTransactionService().complete();
        assertNotNull("can't find the roles after adding them", retrievedRoles);
        assertEquals("bad number of retrieved roles", 5, retrievedRoles.size());
        assertTrue("not in asc order: first= ",
                retrievedRoles.get(0).getDisplayName().compareTo(retrievedRoles.get(3).getDisplayName()) < 0);
    }

    private List<SRole> createRoles(final int i, final String baseName, final String baseLabel)
            throws SIdentityException {
        final ArrayList<SRole> results = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            final SRole role = SRole.builder().name(baseName + j).displayName(baseLabel + j).build();
            identityService.createRole(role, null, null);
            results.add(role);
        }
        return results;
    }

    private void deleteRoles() throws SIdentityException {
        final List<SRole> roles = identityService.getRoles(0, 5000);
        for (final SRole sRole : roles) {
            identityService.deleteRole(sRole);
        }
    }

    @Test
    public void testGetNumberOfRoles() throws Exception {
        getTransactionService().begin();
        final long numberOfRoles = identityService.getNumberOfRoles();
        long id;
        SRole role;
        final long time = new Date().getTime();
        for (int i = 0; i < 5; i++) {
            id = time + i;
            role = SRole.builder().name("testGetNumberOfRoles" + i).id(id).build();
            identityService.createRole(role, null, null);
        }
        assertEquals("bad count of roles", numberOfRoles + 5, identityService.getNumberOfRoles());
        getTransactionService().complete();
    }

    /*
     * Method that helps to retrieve groups
     */
    @Test
    public void testGetGroupsPaginated() throws Exception {
        getTransactionService().begin();
        long id;
        SGroup group;
        final long time = new Date().getTime();
        for (int i = 0; i < 30; i++) {
            id = time + i;
            group = SGroup.builder().name("testGetGroupsPaginated" + i).id(id).build();
            identityService.createGroup(group, null, null);
        }

        List<SGroup> retrievedGroups = identityService.getGroups(5, 5);
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved groups", 5, retrievedGroups.size());

        retrievedGroups = identityService.getGroups(0, 20);
        getTransactionService().complete();
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved groups", 20, retrievedGroups.size());
    }

    @Test
    public void testGetGroupsOrderByName() throws Exception {
        getTransactionService().begin();
        deleteGroups();
        createGroups(10, "testGetGroupsOrderByName_name", "testGetGroupsOrderByName_label", null);
        getTransactionService().complete();

        getTransactionService().begin();
        List<SGroup> retrievedGroups = identityService.getGroups(5, 5, SGroup.NAME, OrderByType.DESC);
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved roles", 5, retrievedGroups.size());
        assertTrue("not in descending order",
                retrievedGroups.get(1).getName().compareTo(retrievedGroups.get(2).getName()) > 0);

        retrievedGroups = identityService.getGroups(5, 5, SGroup.NAME, OrderByType.ASC);
        getTransactionService().complete();
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved groups", 5, retrievedGroups.size());
        assertTrue("not in descending order",
                retrievedGroups.get(0).getName().compareTo(retrievedGroups.get(3).getName()) < 0);
    }

    @Test
    public void testGetGroupsOrderByLabel() throws Exception {
        getTransactionService().begin();
        deleteGroups();
        createGroups(10, "testGetGroupsOrderByLabel_name", "testGetGroupsOrderByLabel_label", null);
        List<SGroup> retrievedGroups = identityService.getGroups(5, 5, SGroup.DISPLAY_NAME, OrderByType.DESC);
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved roles", 5, retrievedGroups.size());
        assertTrue("not in descending order",
                retrievedGroups.get(1).getName().compareTo(retrievedGroups.get(2).getName()) > 0);

        retrievedGroups = identityService.getGroups(5, 5, SGroup.DISPLAY_NAME, OrderByType.ASC);
        getTransactionService().complete();
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved groups", 5, retrievedGroups.size());
        assertTrue("not in descending order",
                retrievedGroups.get(0).getDisplayName().compareTo(retrievedGroups.get(3).getDisplayName()) < 0);
    }

    private List<SGroup> createGroups(final int i, final String basename, final String baseLabel, final SGroup g)
            throws SIdentityException {
        final List<SGroup> groups = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            final SGroup.SGroupBuilder inst = SGroup.builder().name(basename + j).displayName(baseLabel + j);
            if (g != null) {
                inst.parentPath(g.getPath());
            }
            final SGroup group = inst.build();
            identityService.createGroup(group, null, null);
            groups.add(group);
        }
        return groups;
    }

    private void deleteGroups() throws SIdentityException {
        final List<SGroup> groups = identityService.getGroups(0, 5000);
        for (final SGroup sGroup : groups) {
            identityService.deleteGroup(sGroup);
        }
    }

    @Test
    public void testGetNumberOfGroups() throws Exception {
        getTransactionService().begin();
        final long numberOfGroups = identityService.getNumberOfGroups();
        long id;
        SGroup group;
        final long time = new Date().getTime();
        for (int i = 0; i < 5; i++) {
            id = time + i;
            group = SGroup.builder().name("testGetNumberOfGroups" + i).id(id).build();
            identityService.createGroup(group, null, null);
        }
        assertEquals("bad count of groups", numberOfGroups + 5, identityService.getNumberOfGroups());
        getTransactionService().complete();
    }

    @Test
    public void testGetGroupChildrenPaginated() throws Exception {
        getTransactionService().begin();
        deleteGroups();
        final List<SGroup> groups = createGroups(1, "testGetGroupChildrenPaginated_name",
                "testGetGroupChildrenPaginated_label", null);
        identityService.getGroup(groups.get(0).getId());

        final SGroup parentGroup = groups.iterator().next();
        createGroups(5, "testGetGroupChildrenPaginatedChildren_name", "testGetGroupChildrenPaginatedChildren_label",
                parentGroup);
        getTransactionService().complete();

        getTransactionService().begin();
        final List<SGroup> retrievedGroups = identityService.getGroupChildren(parentGroup.getId(), 0, 5);
        getTransactionService().complete();
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved groups", 5, retrievedGroups.size());
    }

    @Test
    public void testGetGroupChildrenWithCriterion() throws Exception {
        getTransactionService().begin();
        deleteGroups();
        final List<SGroup> groups = createGroups(1, "testGetGroupChildrenWithCriterion_name",
                "testGetGroupChildrenWithCriterion_label", null);
        final SGroup parentGroup = groups.iterator().next();
        createGroups(5, "testGetGroupChildrenWithCriterionChildren_name",
                "testGetGroupChildrenWithCriterionChildren_label", parentGroup);
        getTransactionService().complete();

        getTransactionService().begin();
        List<SGroup> retrievedGroups = identityService.getGroupChildren(parentGroup.getId(), 0, 3, SGroup.NAME,
                OrderByType.DESC);
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved groups", 3, retrievedGroups.size());
        assertTrue("not in descending order",
                retrievedGroups.get(1).getName().compareTo(retrievedGroups.get(2).getName()) > 0);

        retrievedGroups = identityService.getGroupChildren(parentGroup.getId(), 0, 3, SGroup.NAME,
                OrderByType.ASC);
        getTransactionService().complete();
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved groups", 3, retrievedGroups.size());
        assertTrue("not in descending order",
                retrievedGroups.get(0).getName().compareTo(retrievedGroups.get(1).getName()) < 0);

    }

    @Test
    public void testGetNumberOfGroupChildren() throws Exception {
        getTransactionService().begin();
        deleteGroups();
        final List<SGroup> groups = createGroups(1, "testGetNumberOfGroupChildren_name",
                "testGetNumberOfGroupChildren_label", null);
        final SGroup parentGroup = groups.iterator().next();
        createGroups(5, "testGetNumberOfGroupChildrenChildren_name", "testGetNumberOfGroupChildrenChildren_label",
                parentGroup);
        getTransactionService().complete();

        getTransactionService().begin();
        assertEquals("bad count of groups", 5, identityService.getNumberOfGroupChildren(parentGroup.getId()));
        getTransactionService().complete();
    }

    /*
     * Method that helps to retrieve profile metadata
     */

    @Test
    public void testGetProfileMetadataPaginated() throws Exception {
        getTransactionService().begin();
        long id;
        SCustomUserInfoDefinition metadata;
        final long time = new Date().getTime();
        for (int i = 0; i < 30; i++) {
            id = time + i;
            metadata = SCustomUserInfoDefinition.builder().name("testGetProfileMetadataPaginated" + i)
                    .id(id).build();
            identityService.createCustomUserInfoDefinition(metadata);
        }

        List<SCustomUserInfoDefinition> retrievedMetadata = identityService.getCustomUserInfoDefinitions(5, 5);
        assertNotNull("can't find the groups after adding them", retrievedMetadata);
        assertEquals("bad number of retrieved groups", 5, retrievedMetadata.size());

        retrievedMetadata = identityService.getCustomUserInfoDefinitions(0, 20);
        getTransactionService().complete();
        assertNotNull("can't find the groups after adding them", retrievedMetadata);
        assertEquals("bad number of retrieved groups", 20, retrievedMetadata.size());
    }

    @Test
    public void testGetNumberOfCustomUserInfoDefinition() throws Exception {
        getTransactionService().begin();
        final long numberOfMetadata = identityService.getNumberOfCustomUserInfoDefinition();
        long id;
        SCustomUserInfoDefinition info;
        final long time = new Date().getTime();
        for (int i = 0; i < 30; i++) {
            id = time + 50L + i;
            info = SCustomUserInfoDefinition.builder().name("testGetNumberOfCustomUserInfoDefinition" + i)
                    .id(id).build();
            identityService.createCustomUserInfoDefinition(info);
        }
        assertEquals("bad count of custom user info definition", numberOfMetadata + 30,
                identityService.getNumberOfCustomUserInfoDefinition());
        getTransactionService().complete();
    }

    /*
     * Method that helps to retrieve users
     */
    @Test
    public void testGetUsersWithRolePaginated() throws Exception {
        getTransactionService().begin();
        final SRole role = SRole.builder().name("testGetUsersWithRole").build();
        identityService.createRole(role, null, null);
        final List<SGroup> groups = createGroups(2, "testGetUsersWithRoleGroup", "testGetUsersWithRoleGroup", null);
        for (int i = 0; i < 10; i++) {
            final SUser.SUserBuilder userBuilder = SUser.builder().userName("user" + i).password("kikoo");
            final SUser user = identityService.createUser(userBuilder.build());
            final SUserMembership userMembership1 = SUserMembership.builder().userId(user.getId())
                    .groupId(groups.get(0).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership1);
        }
        for (int i = 10; i < 20; i++) {
            final SUser.SUserBuilder userBuilder = SUser.builder().userName("user" + i).password("kikoo");
            final SUser user = identityService.createUser(userBuilder.build());
            final SUserMembership userMembership2 = SUserMembership.builder().userId(user.getId())
                    .groupId(groups.get(1).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership2);
        }

        final List<SUser> usersWithRole = identityService.getUsersWithRole(role.getId(), 10, 10);
        getTransactionService().complete();
        assertEquals("not all user were retrieved", 10, usersWithRole.size());
    }

    @Test
    public void testGetUsersWithRoleWithCriterion() throws Exception {
        getTransactionService().begin();

        final SRole role = SRole.builder().name("testGetUsersWithRole").build();
        identityService.createRole(role, null, null);
        final List<SGroup> groups = createGroups(2, "testGetUsersWithRoleGroup", "testGetUsersWithRoleGroup", null);
        SUser user;
        for (int i = 0; i < 10; i++) {
            user = SUser.builder().userName("user" + i).password("kikoo").build();
            final SUser user2 = identityService.createUser(user);
            final SUserMembership userMembership1 = SUserMembership.builder().userId(user2.getId())
                    .groupId(groups.get(0).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership1);
        }
        for (int i = 10; i < 20; i++) {
            user = SUser.builder().userName("user" + i).password("kikoo").build();
            final SUser user2 = identityService.createUser(user);
            final SUserMembership userMembership2 = SUserMembership.builder().userId(user2.getId())
                    .groupId(groups.get(1).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership2);
        }

        List<SUser> usersWithRole = identityService.getUsersWithRole(role.getId(), 10, 10, SUser.USER_NAME,
                OrderByType.DESC);
        assertEquals("not all user were retrieved", 10, usersWithRole.size());
        assertTrue("not in descending order",
                usersWithRole.get(1).getUserName().compareTo(usersWithRole.get(2).getUserName()) > 0);
        usersWithRole = identityService.getUsersWithRole(role.getId(), 10, 10, SUser.USER_NAME, OrderByType.ASC);
        getTransactionService().complete();
        assertEquals("not all user were retrieved", 10, usersWithRole.size());
        assertTrue("not in asc order",
                usersWithRole.get(1).getUserName().compareTo(usersWithRole.get(2).getUserName()) < 0);
    }

    @Test
    public void testGetActiveAndInactiveUsersWithRole() throws Exception {
        getTransactionService().begin();
        final SRole role = SRole.builder().name("testGetActiveAndInactiveUsersWithRole").build();
        identityService.createRole(role, null, null);
        final List<SGroup> groups = createGroups(2, "testGetUsersWithRoleGroup", "testGetUsersWithRoleGroup", null);
        SUser user;
        for (int i = 0; i < 10; i++) {
            user = SUser.builder().userName("inactive user" + i).password("kikoo").enabled(false).build();
            final SUser user2 = identityService.createUser(user);
            final SUserMembership userMembership1 = SUserMembership.builder().userId(user2.getId())
                    .groupId(groups.get(0).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership1);
        }
        for (int i = 10; i < 20; i++) {
            user = SUser.builder().userName("active user" + i).enabled(true).password("kikoo").build();
            final SUser user2 = identityService.createUser(user);
            final SUserMembership userMembership2 = SUserMembership.builder().userId(user2.getId())
                    .groupId(groups.get(1).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership2);
        }

        List<SUser> activeUsersWithRole = identityService
                .getActiveUsersWithRole(role.getId(), 0, 20, SUser.USER_NAME,
                        OrderByType.DESC);
        List<SUser> inactiveUsersByRole = identityService
                .getInactiveUsersWithRole(role.getId(), 0, 20, SUser.USER_NAME,
                        OrderByType.DESC);
        assertThat(inactiveUsersByRole.size()).isEqualTo(10);
        assertThat(activeUsersWithRole.size()).isEqualTo(10);
        for (int i = 0; i < 10; i++) {
            assertThat(inactiveUsersByRole.get(i).getUserName()).contains("inactive user");
            assertThat(activeUsersWithRole.get(i).getUserName()).contains("active user");
        }

        getTransactionService().complete();
    }

    @Test
    public void testGetNumberOfUsersByRole() throws Exception {
        getTransactionService().begin();
        final SRole role = SRole.builder().name("testGetNumberOfUsersByRole").build();
        identityService.createRole(role, null, null);
        final long numberOfUsersByRole = identityService.getNumberOfUsersByRole(role.getId());
        final SGroup group = SGroup.builder().name("testGetUsersByGroup").build();
        identityService.createGroup(group, null, null);
        SUser user;
        for (int i = 0; i < 5; i++) {
            user = SUser.builder().userName("testGetNumberOfUsersByRole" + i).password("kikoo").build();
            final SUser user2 = identityService.createUser(user);
            final SUserMembership userMembership = SUserMembership.builder().userId(user2.getId())
                    .groupId(group.getId()).roleId(role.getId()).build();
            identityService.createUserMembership(userMembership);
        }
        assertEquals("not the good number of users by role", numberOfUsersByRole + 5,
                identityService.getNumberOfUsersByRole(role.getId()));
        getTransactionService().complete();
    }

    @Test
    public void testGetUsersInGroupPaginated() throws Exception {
        getTransactionService().begin();
        final SRole role = SRole.builder().name("testGetUsersByRole").build();
        identityService.createRole(role, null, null);
        final List<SGroup> groups = createGroups(2, "testGetUsersByRoleGroup", "testGetUsersByRoleGroup", null);
        SUser user;
        for (int i = 0; i < 5; i++) {
            final SUser.SUserBuilder userBuilder = SUser.builder().userName("user" + i).password("kikoo");
            user = identityService.createUser(userBuilder.build());
            final SUserMembership userMembership1 = SUserMembership.builder().userId(user.getId())
                    .groupId(groups.get(0).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership1);
        }
        for (int i = 5; i < 10; i++) {
            final SUser.SUserBuilder userBuilder = SUser.builder().userName("user" + i).password("kikoo");
            user = identityService.createUser(userBuilder.build());
            final SUserMembership userMembership1 = SUserMembership.builder().userId(user.getId())
                    .groupId(groups.get(1).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership1);
        }
        final List<SUser> usersByGroup = identityService.getUsersInGroup(groups.get(0).getId(), 1, 2, "id", null);
        getTransactionService().complete();
        assertEquals("not the good number of user with the role", 2, usersByGroup.size());
    }

    @Test
    public void testGetUsersInGroupwithCriterion() throws Exception {
        getTransactionService().begin();

        final SRole role = SRole.builder().name("testGetUsersByRole").build();
        identityService.createRole(role, null, null);
        final List<SGroup> groups = createGroups(2, "testGetUsersByRoleGroup", "testGetUsersByRoleGroup", null);
        SUser user;
        for (int i = 0; i < 5; i++) {
            final SUser.SUserBuilder userBuilder = SUser.builder().userName("user" + i).password("kikoo");
            user = identityService.createUser(userBuilder.build());
            final SUserMembership userMembership1 = SUserMembership.builder().userId(user.getId())
                    .groupId(groups.get(0).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership1);
        }
        for (int i = 5; i < 10; i++) {
            final SUser.SUserBuilder userBuilder = SUser.builder().userName("user" + i).password("kikoo");
            user = identityService.createUser(userBuilder.build());
            final SUserMembership userMembership1 = SUserMembership.builder().userId(user.getId())
                    .groupId(groups.get(1).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership1);
        }

        List<SUser> usersByGroup = identityService.getUsersInGroup(groups.get(0).getId(), 1, 2, SUser.USER_NAME,
                OrderByType.DESC);
        assertEquals("not the good number of user with the role", 2, usersByGroup.size());
        assertTrue("not in descending order",
                usersByGroup.get(0).getUserName().compareTo(usersByGroup.get(1).getUserName()) > 0);
        usersByGroup = identityService.getUsersInGroup(groups.get(0).getId(), 1, 2, SUser.USER_NAME,
                OrderByType.ASC);
        getTransactionService().complete();
        assertEquals("not all user were retrieved", 2, usersByGroup.size());
        assertTrue("not in asc order",
                usersByGroup.get(0).getUserName().compareTo(usersByGroup.get(1).getUserName()) < 0);
    }

    @Test
    public void testGetActiveAndInactiveUsersInGroup() throws Exception {
        getTransactionService().begin();
        final SRole role = SRole.builder().name("testGetActiveAndInactiveUsersByGroup").build();
        identityService.createRole(role, null, null);
        final List<SGroup> groups = createGroups(2, "testGetActiveAndInactiveUsersByGroup",
                "testGetActiveAndInactiveUsersByGroup", null);
        SUser user;
        for (int i = 0; i < 5; i++) {
            final SUser.SUserBuilder userBuilder = SUser.builder().userName("inactive user" + i).password("kikoo")
                    .enabled(false);
            user = identityService.createUser(userBuilder.build());
            final SUserMembership userMembership1 = SUserMembership.builder().userId(user.getId())
                    .groupId(groups.get(0).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership1);
        }
        for (int i = 5; i < 10; i++) {
            final SUser.SUserBuilder userBuilder = SUser.builder().userName("active user " + i).password("kikoo")
                    .enabled(true);
            user = identityService.createUser(userBuilder.build());
            final SUserMembership userMembership1 = SUserMembership.builder().userId(user.getId())
                    .groupId(groups.get(1).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership1);
        }
        for (int i = 10; i < 15; i++) {
            final SUser.SUserBuilder userBuilder = SUser.builder().userName("active user " + i).password("kikoo")
                    .enabled(true);
            user = identityService.createUser(userBuilder.build());
            final SUserMembership userMembership1 = SUserMembership.builder().userId(user.getId())
                    .groupId(groups.get(0).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership1);
        }
        final List<SUser> activeUsersByGroup1 = identityService.getActiveUsersInGroup(groups.get(0).getId(), 0, 2, null,
                null);
        final List<SUser> inactiveUsersByGroup1 = identityService.getInactiveUsersInGroup(groups.get(0).getId(), 0, 8,
                null, null);
        assertThat(activeUsersByGroup1.size()).isEqualTo(2);
        assertThat(inactiveUsersByGroup1.size()).isEqualTo(5);
        final List<SUser> activeUsersByGroup2 = identityService.getActiveUsersInGroup(groups.get(1).getId(), 0, 3, null,
                null);
        final List<SUser> inactiveUsersByGroup2 = identityService.getInactiveUsersInGroup(groups.get(1).getId(), 0, 2,
                null, null);
        assertThat(activeUsersByGroup2.size()).isEqualTo(3);
        assertThat(inactiveUsersByGroup2.size()).isEqualTo(0);
        final List<SUser> activeUsersByGroup3 = identityService.getActiveUsersInGroup(groups.get(0).getId(), 0, 500,
                null, null);
        final List<SUser> inactiveUsersByGroup3 = identityService.getInactiveUsersInGroup(groups.get(0).getId(), 0,
                1000, null, null);
        assertThat(activeUsersByGroup3.size()).isEqualTo(5);
        assertThat(inactiveUsersByGroup3.size()).isEqualTo(5);
        for (int i = 0; i < 5; i++) {
            assertThat(activeUsersByGroup3.get(i).getUserName()).contains("active user");
            assertThat(inactiveUsersByGroup3.get(i).getUserName()).contains("inactive user");
        }
        getTransactionService().complete();
    }

    @Test
    public void testGetNumberOfUsersByGroup() throws Exception {
        getTransactionService().begin();

        final SRole role = SRole.builder().name("testGetUsersByRole").build();
        identityService.createRole(role, null, null);
        final List<SGroup> groups = createGroups(2, "testGetUsersByRoleGroup", "testGetUsersByRoleGroup", null);
        SUser user;
        for (int i = 0; i < 5; i++) {
            final SUser.SUserBuilder userBuilder = SUser.builder().userName("user" + i).password("kikoo");
            user = identityService.createUser(userBuilder.build());
            final SUserMembership userMembership1 = SUserMembership.builder().userId(user.getId())
                    .groupId(groups.get(0).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership1);
        }
        for (int i = 5; i < 10; i++) {
            final SUser.SUserBuilder userBuilder = SUser.builder().userName("user" + i).password("kikoo");
            user = identityService.createUser(userBuilder.build());
            final SUserMembership userMembership1 = SUserMembership.builder().userId(user.getId())
                    .groupId(groups.get(1).getId()).roleId(role.getId())
                    .build();
            identityService.createUserMembership(userMembership1);
        }
        final long id = groups.get(0).getId();

        assertEquals("not the good number of users by group", 5, identityService.getNumberOfUsersByGroup(id));
        getTransactionService().complete();
    }

    @Test
    public void testGetNumberOfUsersByMembership() throws Exception {
        getTransactionService().begin();
        final SGroup group = createGroups(1, "testGetUserByMembership", "testGetUserByMembership", null).iterator()
                .next();
        final SRole role = createRoles(1, "testGetUserByMembership", "testGetUserByMembership").iterator().next();
        final List<SUser> users = createUsers(5, "getMembershipUsers");
        for (final SUser sUser : users) {
            final SUserMembership userMembership = SUserMembership.builder().userId(sUser.getId())
                    .groupId(group.getId()).roleId(role.getId()).build();
            identityService.createUserMembership(userMembership);
        }
        assertEquals("not the good number of user by membership", 5,
                identityService.getNumberOfUsersByMembership(group.getId(), role.getId()));
        getTransactionService().complete();
    }

    @Test
    public void testGetUsersPaginated() throws Exception {
        getTransactionService().begin();
        createUsers(20, "testGetUsersPaginated");
        List<SUser> users = identityService.getUsers(5, 10);
        final SUser user1 = users.get(0);
        assertEquals("returned list have not the correct size", 10, users.size());

        users = identityService.getUsers(6, 10);
        final SUser user2 = users.get(0);
        getTransactionService().complete();
        assertEquals("returned list have not the correct size", 10, users.size());
        assertNotSame("from index not working", user1.getId(), user2.getId());
    }

    @Test
    public void testGetUsersOrderByUserName() throws Exception {
        getTransactionService().begin();
        createUsers(10, "testGetUsersOrderByUserName");
        List<SUser> users = identityService.getUsers(5, 10, SUser.USER_NAME, OrderByType.DESC);
        assertTrue("not in desc order",
                users.get(0).getUserName().compareTo(users.get(users.size() - 1).getUserName()) > 0);
        users = identityService.getUsers(5, 10, SUser.USER_NAME, OrderByType.ASC);
        getTransactionService().complete();
        assertTrue("not in asc order",
                users.get(0).getUserName().compareTo(users.get(users.size() - 1).getUserName()) < 0);
    }

    @Test
    public void testGetUsersOrderByFirstName() throws Exception {
        getTransactionService().begin();
        deleteUsers();
        createUsers(10, "testGetUsersOrderByFirstName");
        List<SUser> users = identityService.getUsers(5, 10, SUser.FIRST_NAME, OrderByType.DESC);
        assertTrue("not in desc order",
                users.get(0).getFirstName().compareTo(users.get(users.size() - 1).getFirstName()) > 0);
        users = identityService.getUsers(5, 10, SUser.FIRST_NAME, OrderByType.ASC);
        getTransactionService().complete();
        assertTrue("not in asc order",
                users.get(0).getFirstName().compareTo(users.get(users.size() - 1).getFirstName()) < 0);
    }

    @Test
    public void testGetUsersOrderByLastName() throws Exception {
        getTransactionService().begin();
        deleteUsers();
        createUsers(10, "testGetUsersOrderByLastName");
        List<SUser> users = identityService.getUsers(5, 10, SUser.LAST_NAME, OrderByType.DESC);
        assertTrue("not in desc order",
                users.get(0).getLastName().compareTo(users.get(users.size() - 1).getLastName()) > 0);
        users = identityService.getUsers(5, 10, SUser.LAST_NAME, OrderByType.ASC);
        getTransactionService().complete();
        assertTrue("not in asc order",
                users.get(0).getLastName().compareTo(users.get(users.size() - 1).getLastName()) < 0);
    }

    private List<SUser> createUsers(final int i, final String baseUsername) throws SIdentityException {
        final List<SUser> ids = new ArrayList<>();
        for (int j = 0; j < i; j++) {
            final SUser user = SUser.builder().userName(baseUsername + j).firstName("firstName" + j)
                    .lastName("lastName" + j).password("password" + j).build();
            ids.add(identityService.createUser(user));
        }
        return ids;
    }

    private void deleteUsers() throws SIdentityException {
        final List<SUser> users = identityService.getUsers(0, 5000);
        for (final SUser sUser : users) {
            identityService.deleteUser(sUser);
        }
    }

    @Test
    public void testGetNumberOfUsers() throws Exception {
        getTransactionService().begin();
        final long numberOfUsers = identityService.getNumberOfUsers();
        final SUser user = SUser.builder().userName("testGetNumberOfUsers").password("kikoo").build();
        identityService.createUser(user);
        assertEquals(numberOfUsers + 1, identityService.getNumberOfUsers());
        getTransactionService().complete();
    }

    @Test
    public void testGetUsersWithManager() throws Exception {
        getTransactionService().begin();
        createUsers(11, "testGetUsersWithManager");
        final SUser manager = identityService.getUsers(0, 1).get(0);
        final List<SUser> users = identityService.getUsers(1, 10);
        for (final SUser user : users) {
            final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class)
                    .createNewInstance()
                    .updateManagerUserId(manager.getId()).done();
            identityService.updateUser(user, changeDescriptor);
        }
        final long id = manager.getId();
        final List<SUser> usersWithManager = identityService.getUsersWithManager(id, 0, 20, null, null);
        getTransactionService().complete();
        assertEquals("did not retrieved all user having the manager", 10, usersWithManager.size());
        for (final SUser sUser : usersWithManager) {
            assertEquals("One of the user have not the good manager", manager.getId(), sUser.getManagerUserId());
        }
    }

    @Test
    public void testGetActiveAndInactiveUsersWithManager() throws Exception {
        getTransactionService().begin();
        createUsers(31, "testGetActiveUsersWithManager");
        final SUser manager = identityService.getUsers(0, 1).get(0);
        final List<SUser> activeUsers = identityService.getUsers(0, 15);
        final List<SUser> inactiveUsers = identityService.getUsers(15, 5);
        for (final SUser user : activeUsers) {
            final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class)
                    .createNewInstance()
                    .updateManagerUserId(manager.getId()).updateEnabled(true).done();
            identityService.updateUser(user, changeDescriptor);
        }
        for (final SUser user : inactiveUsers) {
            final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class)
                    .createNewInstance()
                    .updateManagerUserId(manager.getId()).updateEnabled(false).done();
            identityService.updateUser(user, changeDescriptor);
        }
        final long id = manager.getId();
        final List<SUser> activeUsersWithManager = identityService.getActiveUsersWithManager(id, 0, 20, null, null);
        final List<SUser> inactiveUsersWithManager = identityService.getInactiveUsersWithManager(id, 0, 20, null, null);
        getTransactionService().complete();
        assertThat(activeUsersWithManager.size()).isEqualTo(15);
        assertThat(inactiveUsersWithManager.size()).isEqualTo(5);
        for (final SUser sUser : activeUsersWithManager) {
            assertThat(sUser.getManagerUserId()).isEqualTo(manager.getId());
        }
        for (final SUser sUser : inactiveUsersWithManager) {
            assertThat(sUser.getManagerUserId()).isEqualTo(manager.getId());
        }
    }

    @Test
    // FIXME change name
    public void testUpdateUserDoesNotChangeManagerId() throws Exception {
        getTransactionService().begin();
        createUsers(3, "testGetUpdateUserDoesNotChangeanagerId");
        final List<SUser> users = identityService.getUsers(0, 3);
        assertEquals(3, users.size());
        final SUser manager = users.get(0);
        SUser user = users.get(1);
        final SUser newManager = users.get(2);
        EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class)
                .createNewInstance().updateManagerUserId(manager.getId())
                .done();
        identityService.updateUser(user, changeDescriptor);
        final long id = manager.getId();
        final List<SUser> usersWithManager = identityService.getUsersWithManager(id, 0, 20, null, null);
        assertEquals("did not retrieved all user having the manager", 1, usersWithManager.size());
        for (final SUser sUser : usersWithManager) {
            assertEquals("One of the user have not the good manager", manager.getId(), sUser.getManagerUserId());
        }
        changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class).createNewInstance()
                .updateFirstName("kevin").done();
        identityService.updateUser(user, changeDescriptor);
        user = identityService.getUser(user.getId());
        assertEquals("kevin", user.getFirstName());
        assertEquals(manager.getId(), user.getManagerUserId());

        changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class).createNewInstance()
                .updateManagerUserId(newManager.getId()).done();
        identityService.updateUser(user, changeDescriptor);
        user = identityService.getUser(user.getId());
        assertEquals(newManager.getId(), user.getManagerUserId());
        getTransactionService().complete();
    }

    @Test
    public void testUpdateUser() throws Exception {
        getTransactionService().begin();
        SUser.SUserBuilder userBuilder = null;
        userBuilder = SUser.builder().userName("testUpdateUser").password("kikoo")
                .firstName("Update").lastName("User");
        final SUser user = identityService.createUser(userBuilder.build());
        final String password = user.getPassword();
        final SContactInfo contactInfo = SContactInfo.builder().userId(user.getId()).personal(true).address("Somewhere")
                .building("AA11")
                .city("Taiwan").build();
        identityService.createUserContactInfo(contactInfo);
        final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class)
                .createNewInstance()
                .updateUserName("testUpdateUser2").updatePassword("lol")
                .updateFirstName("updated").updateLastName("user2").updateEnabled(true).done();
        final String newAddress = "SomeWhereElse";
        final String newCity = "Ouarzazate";
        final String newBuilding = "BB22";
        final String country = "Marrocco";
        final String email = "other@fifi.org";
        final String faxNumber = "99999999";
        final String mobileNumber = "77777777";
        final String phoneNumber = "555555";
        final String room = "Room2";
        final String state = "State2";
        final String website = "website2";
        final String zipCode = "zipCode2";
        final EntityUpdateDescriptor updateContactInfo = BuilderFactory.get(SContactInfoUpdateBuilderFactory.class)
                .createNewInstance()
                .updateAddress(newAddress).updateCity(newCity)
                .updateBuilding(newBuilding).updateCountry(country).updateEmail(email).updateFaxNumber(faxNumber)
                .updateMobileNumber(mobileNumber)
                .updatePhoneNumber(phoneNumber).updateRoom(room).updateState(state).updateWebsite(website)
                .updateZipCode(zipCode).done();
        identityService.updateUser(user, changeDescriptor);
        identityService.updateUserContactInfo(contactInfo, updateContactInfo);
        final SUser user2 = identityService.getUser(user.getId());
        final SContactInfo contactInfo2 = identityService.getUserContactInfo(user2.getId(), true);
        getTransactionService().complete();
        assertEquals("user was not updated", user, user2);
        assertEquals("testUpdateUser2", user2.getUserName());
        assertNotSame(password, user2.getPassword()); // FIXME replace password by user.getPassword()
        assertEquals("updated", user2.getFirstName());
        assertEquals("user2", user2.getLastName());
        assertEquals(user2.getId(), (long) contactInfo2.getUserId());
        assertEquals(newAddress, contactInfo2.getAddress());
        assertEquals(newBuilding, contactInfo2.getBuilding());
        assertEquals(newCity, contactInfo2.getCity());
        assertEquals(country, contactInfo2.getCountry());
        assertEquals(email, contactInfo2.getEmail());
        assertEquals(faxNumber, contactInfo2.getFaxNumber());
        assertEquals(mobileNumber, contactInfo2.getMobileNumber());
        assertEquals(phoneNumber, contactInfo2.getPhoneNumber());
        assertEquals(room, contactInfo2.getRoom());
        assertEquals(state, contactInfo2.getState());
        assertEquals(website, contactInfo2.getWebsite());
        assertEquals(zipCode, contactInfo2.getZipCode());
    }

    @Test
    public void testAddProfileMetadata() throws Exception {
        getTransactionService().begin();
        final long metadataId = new Date().getTime();
        final SCustomUserInfoDefinition metadata = SCustomUserInfoDefinition.builder().id(metadataId)
                .name("testAddProfileMetadata").build();
        identityService.createCustomUserInfoDefinition(metadata);
        final SCustomUserInfoDefinition metadata2 = identityService.getCustomUserInfoDefinition(metadataId);
        getTransactionService().complete();
        assertNotNull("can't retrieve the metadata", metadata2);
        assertEquals("retrieved not the good metadata", metadata.getId(), metadata2.getId());
    }

    @Test
    public void testUpdateProfileMetadata() throws Exception {
        getTransactionService().begin();
        final SCustomUserInfoDefinition metadata = identityService.getCustomUserInfoDefinitions(0, 1).get(0);
        final long metadataId = metadata.getId();
        final String newName = "theNewName";
        final EntityUpdateDescriptor changeDescriptor = BuilderFactory
                .get(SCustomUserInfoDefinitionUpdateBuilderFactory.class).createNewInstance()
                .updateName(newName).done();
        identityService.updateCustomUserInfoDefinition(metadata, changeDescriptor);
        final SCustomUserInfoDefinition metadata2 = identityService.getCustomUserInfoDefinition(metadataId);
        getTransactionService().complete();
        assertNotNull("can't retrieve the metadata", metadata2);
        assertEquals("retrieved not the good metadata", metadata.getId(), metadata2.getId());
        assertEquals("metadata not updated", newName, metadata2.getName());
    }

    @Test
    public void testAddRole() throws Exception {
        getTransactionService().begin();
        final long id = new Date().getTime();
        final SRole role = SRole.builder().id(id).name("testAddRole").build();
        identityService.createRole(role, null, null);
        final SRole role2 = identityService.getRole(id);
        getTransactionService().complete();
        assertNotNull("can't find the added role", role2);
        assertEquals("not the good role was added", role.getName(), role2.getName());
    }

    @Test
    public void testUpdateRole() throws Exception {
        getTransactionService().begin();

        final SRole role = createRoles(1, "firstName", "firstLabel").get(0);
        final long id = role.getId();
        final String newName = "newRoleName";
        final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SRoleUpdateBuilderFactory.class)
                .createNewInstance().updateName(newName).done();
        identityService.updateRole(role, changeDescriptor, null);
        final SRole role2 = identityService.getRole(id);
        getTransactionService().complete();
        assertNotNull("can't find the updated role", role2);
        assertEquals("not udpated", newName, role2.getName());
    }

    @Test
    public void testAddGroup() throws Exception {
        getTransactionService().begin();
        final long id = new Date().getTime();
        final SGroup group = SGroup.builder().id(id).name("testAddGroup").build();
        identityService.createGroup(group, null, null);
        final SGroup group2 = identityService.getGroup(id);
        getTransactionService().complete();
        assertNotNull("can't find the added group", group2);
        assertEquals("not the good group was added", group.getName(), group2.getName());
    }

    @Test
    public void testUpdateGroup() throws Exception {
        getTransactionService().begin();
        final SGroup group = createGroups(1, "firstName", "firstLabel", null).get(0);
        final long id = group.getId();
        final String newName = "newGroupName";
        final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SGroupUpdateBuilderFactory.class)
                .createNewInstance().updateName(newName).done();
        identityService.updateGroup(group, changeDescriptor, null);
        final SGroup group2 = identityService.getGroup(id);
        getTransactionService().complete();
        assertNotNull("can't find the updated group", group2);
        assertEquals("not udpated", newName, group2.getName());
    }

    /*
     * Methods that delete objects from the identityService module
     */

    @Test(expected = SIdentityException.class)
    public void testDeleteUser() throws Exception {
        getTransactionService().begin();
        final SUser user = createUsers(1, "testDeleteUser").get(0);
        final long id = user.getId();
        identityService.deleteUser(user);

        assertNull("the user was not deleted", identityService.getUser(id));
        getTransactionService().complete();
    }

    @Test(expected = SIdentityException.class)
    public void testDeleteProfileMetadata() throws Exception {
        getTransactionService().begin();
        final SCustomUserInfoDefinition metadataDefinition = SCustomUserInfoDefinition.builder()
                .name("kikooMetadata").build();
        identityService.createCustomUserInfoDefinition(metadataDefinition);
        final long id = metadataDefinition.getId();
        identityService.deleteCustomUserInfoDefinition(metadataDefinition);
        assertNull("the profile metadata was not deleted", identityService.getCustomUserInfoDefinition(id));
        getTransactionService().complete();
    }

    @Test(expected = SIdentityException.class)
    public void testDeleteRole() throws Exception {
        getTransactionService().begin();
        final SRole role = createRoles(1, "testDeleteRole", "testDeleteRole").get(0);
        final long id = role.getId();
        identityService.deleteRole(role);
        assertNull("the role was not deleted", identityService.getRole(id));
        getTransactionService().complete();
    }

    @Test(expected = SIdentityException.class)
    public void testDeleteGroup() throws Exception {
        getTransactionService().begin();
        final List<SGroup> groups = createGroups(1, "testDeleteGroup_name", "testDeleteGroup_label", null);
        getTransactionService().complete();

        getTransactionService().begin();
        final SGroup group = groups.iterator().next();
        assertNotNull(group);
        final long id = group.getId();
        identityService.deleteGroup(group);
        assertNull("the group was not deleted", identityService.getGroup(id));
        getTransactionService().complete();
    }

    /*
     * Methods to add/remove/set memberships to user
     */

    @Test
    public void testAddMembershipToUser() throws Exception {
        getTransactionService().begin();
        final List<SGroup> groups = createGroups(1, "testAddMembershipToUser_name", "testAddMembershipToUser_label",
                null);
        createRoles(1, "testAddMembershipToUser_name", "testAddMembershipToUser_label");
        final SGroup group = groups.iterator().next();

        final SRole role = identityService.getRoleByName("testAddMembershipToUser_name0");
        createUsers(1, "testAddMembershipToUser");
        SUser user = identityService.getUserByUserName("testAddMembershipToUser0");
        assertNotNull(user);
        getTransactionService().complete();

        getTransactionService().begin();
        user = identityService.getUserByUserName("testAddMembershipToUser0");
        final int size = identityService
                .getUserMembershipsOfUser(user.getId(), 0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS).size();
        final SUserMembership userMembership = SUserMembership.builder().userId(user.getId()).groupId(group.getId())
                .roleId(role.getId()).build();
        identityService.createUserMembership(userMembership);
        getTransactionService().complete();

        getTransactionService().begin();
        final SUser user2 = identityService.getUser(user.getId());
        assertEquals("membership not added", size + 1,
                identityService.getUserMembershipsOfUser(user2.getId(), 0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS)
                        .size());
        getTransactionService().complete();
    }

    @Test
    public void testRemoveMembershipFromUser() throws Exception {
        getTransactionService().begin();
        deleteGroups();
        final List<SGroup> groups = createGroups(1, "testRemoveMembershipFromUser_name",
                "testRemoveMembershipFromUser_label", null);
        createRoles(1, "testRemoveMembershipFromUser_name", "testRemoveMembershipFromUser_label");
        final SGroup group = groups.iterator().next();
        final SRole role = identityService.getRoleByName("testRemoveMembershipFromUser_name0");
        createUsers(1, "testRemoveMembershipFromUser");
        SUser user = identityService.getUserByUserName("testRemoveMembershipFromUser0");
        assertNotNull(user);
        getTransactionService().complete();

        getTransactionService().begin();

        user = identityService.getUserByUserName("testRemoveMembershipFromUser0");
        final SUserMembership userMembership = SUserMembership.builder().userId(user.getId()).groupId(group.getId())
                .roleId(role.getId()).build();
        identityService.createUserMembership(userMembership);
        getTransactionService().complete();

        getTransactionService().begin();
        user = identityService.getUserByUserName("testRemoveMembershipFromUser0");
        final int size = identityService
                .getUserMembershipsOfUser(user.getId(), 0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS).size();
        assertTrue("no membership on user", size >= 1);

        final SUserMembership userMembership2 = identityService.getUserMembership(user.getId(), group.getId(),
                role.getId());
        identityService.deleteUserMembership(userMembership2);

        final SUser user2 = identityService.getUser(user.getId());

        assertEquals("no membership on user", size - 1,
                identityService.getUserMembershipsOfUser(user2.getId(), 0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS)
                        .size());
        getTransactionService().complete();
    }

    @Test
    public void searchUsersWithWildcards() throws Exception {
        getTransactionService().begin();
        final SUser user1 = identityService.createUser(SUser.builder().userName("user1")
                .firstName("firstname1")
                .lastName("lastname1").password("lkh").build());
        final SUser user2 = identityService.createUser(SUser.builder().userName("user2")
                .firstName("firstname2")
                .lastName("lastname2").password("mlbxcvjmsdkljf").build());
        getTransactionService().complete();

        final Map<Class<? extends PersistentObject>, Set<String>> userAllFields = new HashMap<>();
        final Set<String> fields = new HashSet<>(4);
        fields.add("userName");
        fields.add("firstName");
        fields.add("lastName");
        fields.add("jobTitle");
        userAllFields.put(SUser.class, fields);
        final QueryOptions queryOptions = new QueryOptions(0, 10,
                Arrays.asList(new OrderByOption(SUser.class, "userName", OrderByType.ASC)),
                new ArrayList<FilterOption>(0), new SearchFields(Arrays.asList("#"), userAllFields));
        getTransactionService().begin();
        final List<SUser> result = identityService.searchUsers(queryOptions);
        assertEquals(0, result.size());
        getTransactionService().complete();

        // clean-up
        getTransactionService().begin();
        identityService.deleteUser(user1);
        identityService.deleteUser(user2);
        getTransactionService().complete();
    }
}
