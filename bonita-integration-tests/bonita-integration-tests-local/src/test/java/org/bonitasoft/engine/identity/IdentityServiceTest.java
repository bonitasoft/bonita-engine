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
package org.bonitasoft.engine.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
import org.bonitasoft.engine.identity.model.builder.SContactInfoBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SContactInfoUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SCustomUserInfoDefinitionUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SGroupBuilder;
import org.bonitasoft.engine.identity.model.builder.SGroupBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SGroupUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SRoleBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SRoleUpdateBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserBuilder;
import org.bonitasoft.engine.identity.model.builder.SUserBuilderFactory;
import org.bonitasoft.engine.identity.model.builder.SUserMembershipBuilderFactory;
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
import org.junit.Test;

/**
 * @author Baptiste Mesta
 * @author Elias Ricken de Medeiros
 */
public class IdentityServiceTest extends CommonBPMServicesTest {

    private static IdentityService identityService;

    public IdentityServiceTest() {
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
        final SUser user = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("john").setPassword("bpm").done();
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
        final SUser user = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("testAddUserWithId").setPassword("bpm").done();
        assertEquals(0, user.getId());
        final SUser updatedUser = identityService.createUser(user);
        assertNotSame("The identifier must be set after adding a user", 0, updatedUser.getId());
        getTransactionService().complete();
    }

    @Test
    public void testAddUsersWithoutIds() throws Exception {
        getTransactionService().begin();
        final SUser user1 = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("testAddUsersWithoutIds7").setPassword("bpm").done();
        identityService.createUser(user1);
        getTransactionService().complete();

        getTransactionService().begin();
        final SUser user2 = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("testAddUsersWithoutIds2").setPassword("bpm").done();
        final SUser user3 = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("testAddUsersWithoutIds3").setPassword("bpm").done();
        final SUser user4 = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("testAddUsersWithoutIds4").setPassword("bpm").done();
        final SUser user5 = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("testAddUsersWithoutIds5").setPassword("bpm").done();
        final SUser user6 = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("testAddUsersWithoutIds6").setPassword("bpm").done();
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
        final SUser user = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName(username).setPassword(username).done();
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
        final SRole role = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName(roleName).done();
        identityService.createRole(role);
        final SRole role2 = identityService.getRoleByName(roleName);
        getTransactionService().complete();
        assertNotNull("can't find the role after adding it", role2);
        assertEquals("Does not retrieved the good role", roleName, role2.getName());
    }

    @Test
    public void testGetProfileMetadataByName() throws Exception {
        getTransactionService().begin();
        final String name = "MyProfileMetadata";
        final SCustomUserInfoDefinition metadata = BuilderFactory.get(SCustomUserInfoDefinitionBuilderFactory.class).createNewInstance().setName(name).done();
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
        final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("Seppo").setPassword("kikoo");
        final SUser seppo = identityService.createUser(userBuilder.done());
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
        final SRole testGetRole = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetRole").setId(id).done();
        identityService.createRole(testGetRole);
        final SRole role2 = identityService.getRole(id);
        getTransactionService().complete();

        assertNotNull("can't find the role after adding it", role2);
        assertEquals("Does not retrieved the good role", id, role2.getId());
    }

    @Test
    public void testGetGroup() throws Exception {
        getTransactionService().begin();
        final long id = new Date().getTime();
        final SGroup testGetGroup = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setName("testGetGroup").setId(id).done();
        identityService.createGroup(testGetGroup);
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
        final SGroup group = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setName("R&D").done();
        identityService.createGroup(group);
        final SGroup subGroup = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setParentPath(group.getPath()).setName("R&D").done();
        identityService.createGroup(subGroup);
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
        final SCustomUserInfoDefinition testGetProfileMetadata = BuilderFactory.get(SCustomUserInfoDefinitionBuilderFactory.class).createNewInstance()
                .setName("testGetProfileMetadata").setId(id).done();
        identityService.createCustomUserInfoDefinition(testGetProfileMetadata);
        final SCustomUserInfoDefinition metadata2 = identityService.getCustomUserInfoDefinition(id);
        getTransactionService().complete();

        assertNotNull("can't find the metadata after adding it", metadata2);
        assertEquals("Does not retrieved the good metadata", id, metadata2.getId());
    }

    @Test
    public void testGetUsers() throws Exception {
        getTransactionService().begin();
        final SUser user1 = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("Akseli").setPassword("kikoo").done();
        final long id1 = identityService.createUser(user1).getId();
        final SUser user2 = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("Anja").setPassword("kikoo").done();
        final long id2 = identityService.createUser(user2).getId();

        final List<SUser> retrievedUsers = identityService.getUsers(Arrays.asList(id1, id2));
        getTransactionService().complete();

        assertNotNull("can't find the users after adding them", retrievedUsers);
        assertEquals("bad number of retrieved users", 2, retrievedUsers.size());
        assertTrue("does not contains user 1", retrievedUsers.get(0).getId() == id1 || retrievedUsers.get(1).getId() == id1);
        assertTrue("does not contains user 2", retrievedUsers.get(1).getId() == id2 || retrievedUsers.get(0).getId() == id2);
    }

    @Test
    public void testGetUsersFromNullListIds() throws Exception {
        final SUser eetu = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("Eetu").setPassword("kikoo").done();
        final SUser inkeri = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("Inkeri").setPassword("kikoo").done();
        getTransactionService().begin();
        identityService.createUser(eetu);
        identityService.createUser(inkeri);
        final List<SUser> retrievedUsers = identityService.getUsers(null);
        getTransactionService().complete();

        assertEquals(0, retrievedUsers.size());
    }

    @Test
    public void testGetUsersFromEmptyListIds() throws Exception {
        final SUser lauri = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("lauri").setPassword("kikoo").done();
        final SUser mika = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("mika").setPassword("kikoo").done();
        getTransactionService().begin();
        identityService.createUser(lauri);
        identityService.createUser(mika);
        final List<SUser> retrievedUsers = identityService.getUsers(Collections.<Long> emptyList());
        getTransactionService().complete();

        assertEquals(0, retrievedUsers.size());
    }

    @Test
    public void testGetRoles() throws Exception {
        getTransactionService().begin();
        final long id1 = new Date().getTime();
        final SRole role1 = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetRoles1").setId(id1).done();
        identityService.createRole(role1);
        final long id2 = id1 + 1L;
        final SRole role2 = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetRoles2").setId(id2).done();
        identityService.createRole(role2);

        final List<SRole> retrievedUsers = identityService.getRoles(Arrays.asList(new Long[] { id1, id2 }));
        getTransactionService().complete();

        assertNotNull("can't find the roles after adding them", retrievedUsers);
        assertEquals("bad number of retrieved roles", 2, retrievedUsers.size());
        assertTrue("does not contains role 1", retrievedUsers.get(0).getId() == id1 || retrievedUsers.get(1).getId() == id1);
        assertTrue("does not contains role 2", retrievedUsers.get(1).getId() == id2 || retrievedUsers.get(0).getId() == id2);
    }

    @Test
    public void testGetRolesFromNullListids() throws Exception {
        getTransactionService().begin();
        final long id1 = new Date().getTime();
        final SRole role1 = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetRoles1").setId(id1).done();
        identityService.createRole(role1);
        final long id2 = id1 + 1L;
        final SRole role2 = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetRoles2").setId(id2).done();
        identityService.createRole(role2);

        final List<SRole> retrievedUsers = identityService.getRoles(null);
        getTransactionService().complete();

        assertEquals(0, retrievedUsers.size());
    }

    @Test
    public void testGetRolesFromEmptyListIds() throws Exception {
        getTransactionService().begin();
        final long id1 = new Date().getTime();
        final SRole role1 = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetRoles1").setId(id1).done();
        identityService.createRole(role1);
        final long id2 = id1 + 1L;
        final SRole role2 = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetRoles2").setId(id2).done();
        identityService.createRole(role2);

        final List<SRole> retrievedUsers = identityService.getRoles(Collections.<Long> emptyList());
        getTransactionService().complete();

        assertEquals(0, retrievedUsers.size());
    }

    @Test
    public void testGetGroups() throws Exception {
        getTransactionService().begin();
        final long id1 = new Date().getTime();
        final SGroup group1 = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setName("testGetGroups1").setId(id1).done();
        identityService.createGroup(group1);
        final long id2 = id1 + 1L;
        final SGroup group2 = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setName("testGetGroups2").setId(id2).done();
        identityService.createGroup(group2);

        final List<SGroup> retrievedGroups = identityService.getGroups(Arrays.asList(new Long[] { id1, id2 }));
        getTransactionService().complete();

        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved groups", 2, retrievedGroups.size());
        assertTrue("does not contains group 1", retrievedGroups.get(0).getId() == id1 || retrievedGroups.get(1).getId() == id1);
        assertTrue("does not contains group 2", retrievedGroups.get(1).getId() == id2 || retrievedGroups.get(0).getId() == id2);
    }

    @Test
    public void testGetGroupsFromNullListIds() throws Exception {
        getTransactionService().begin();
        final long id1 = new Date().getTime();
        final SGroup group1 = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setName("testGetGroups1").setId(id1).done();
        identityService.createGroup(group1);
        final long id2 = id1 + 1L;
        final SGroup group2 = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setName("testGetGroups2").setId(id2).done();
        identityService.createGroup(group2);

        final List<SGroup> retrievedGroups = identityService.getGroups(null);
        getTransactionService().complete();

        assertEquals(0, retrievedGroups.size());
    }

    @Test
    public void testGetGroupsFromEmptyListIds() throws Exception {
        getTransactionService().begin();
        final long id1 = new Date().getTime();
        final SGroup group1 = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setName("testGetGroups1").setId(id1).done();
        identityService.createGroup(group1);
        final long id2 = id1 + 1L;
        final SGroup group2 = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setName("testGetGroups2").setId(id2).done();
        identityService.createGroup(group2);

        final List<SGroup> retrievedGroups = identityService.getGroups(Collections.<Long> emptyList());
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
            role = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetRolesPaginated" + i).setId(id).done();
            identityService.createRole(role);
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
        List<SRole> retrievedRoles = identityService.getRoles(5, 5, BuilderFactory.get(SRoleBuilderFactory.class).getNameKey(), OrderByType.DESC);
        assertNotNull("can't find the roles after adding them", retrievedRoles);
        assertEquals("bad number of retrieved roles", 5, retrievedRoles.size());
        assertTrue("not in descending order", retrievedRoles.get(1).getName().compareTo(retrievedRoles.get(2).getName()) > 0);

        retrievedRoles = identityService.getRoles(5, 5, BuilderFactory.get(SRoleBuilderFactory.class).getNameKey(), OrderByType.ASC);
        getTransactionService().complete();
        assertNotNull("can't find the roles after adding them", retrievedRoles);
        assertEquals("bad number of retrieved roles", 5, retrievedRoles.size());
        assertTrue("not in asc order: first= " + retrievedRoles.get(0).getName() + "  second = " + retrievedRoles.get(3).getName(), retrievedRoles.get(0)
                .getName().compareTo(retrievedRoles.get(3).getName()) < 0);
    }

    @Test
    public void testGetRolesOrderByLabel() throws Exception {
        getTransactionService().begin();
        deleteUsers();
        deleteRoles();
        createRoles(10, "testGetRolesOrderByLabel_name", "testGetRolesOrderByLabel_label");
        List<SRole> retrievedRoles = identityService.getRoles(5, 5, BuilderFactory.get(SRoleBuilderFactory.class).getDisplayNameKey(), OrderByType.DESC);
        assertNotNull("can't find the roles after adding them", retrievedRoles);
        assertEquals("bad number of retrieved roles", 5, retrievedRoles.size());
        assertTrue("not in descending order", retrievedRoles.get(1).getDisplayName().compareTo(retrievedRoles.get(2).getDisplayName()) > 0);

        retrievedRoles = identityService.getRoles(5, 5, BuilderFactory.get(SRoleBuilderFactory.class).getDisplayNameKey(), OrderByType.ASC);
        getTransactionService().complete();
        assertNotNull("can't find the roles after adding them", retrievedRoles);
        assertEquals("bad number of retrieved roles", 5, retrievedRoles.size());
        assertTrue("not in asc order: first= ", retrievedRoles.get(0).getDisplayName().compareTo(retrievedRoles.get(3).getDisplayName()) < 0);
    }

    private List<SRole> createRoles(final int i, final String baseName, final String baseLabel) throws SIdentityException {
        final ArrayList<SRole> results = new ArrayList<SRole>();
        for (int j = 0; j < i; j++) {
            final SRole role = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName(baseName + j).setDisplayName(baseLabel + j).done();
            identityService.createRole(role);
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
            role = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetNumberOfRoles" + i).setId(id).done();
            identityService.createRole(role);
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
            group = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setName("testGetGroupsPaginated" + i).setId(id).done();
            identityService.createGroup(group);
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
        List<SGroup> retrievedGroups = identityService.getGroups(5, 5, BuilderFactory.get(SGroupBuilderFactory.class).getNameKey(), OrderByType.DESC);
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved roles", 5, retrievedGroups.size());
        assertTrue("not in descending order", retrievedGroups.get(1).getName().compareTo(retrievedGroups.get(2).getName()) > 0);

        retrievedGroups = identityService.getGroups(5, 5, BuilderFactory.get(SGroupBuilderFactory.class).getNameKey(), OrderByType.ASC);
        getTransactionService().complete();
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved groups", 5, retrievedGroups.size());
        assertTrue("not in descending order", retrievedGroups.get(0).getName().compareTo(retrievedGroups.get(3).getName()) < 0);
    }

    @Test
    public void testGetGroupsOrderByLabel() throws Exception {
        getTransactionService().begin();
        deleteGroups();
        createGroups(10, "testGetGroupsOrderByLabel_name", "testGetGroupsOrderByLabel_label", null);
        List<SGroup> retrievedGroups = identityService.getGroups(5, 5, BuilderFactory.get(SGroupBuilderFactory.class).getDisplayNameKey(), OrderByType.DESC);
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved roles", 5, retrievedGroups.size());
        assertTrue("not in descending order", retrievedGroups.get(1).getName().compareTo(retrievedGroups.get(2).getName()) > 0);

        retrievedGroups = identityService.getGroups(5, 5, BuilderFactory.get(SGroupBuilderFactory.class).getDisplayNameKey(), OrderByType.ASC);
        getTransactionService().complete();
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved groups", 5, retrievedGroups.size());
        assertTrue("not in descending order", retrievedGroups.get(0).getDisplayName().compareTo(retrievedGroups.get(3).getDisplayName()) < 0);
    }

    private List<SGroup> createGroups(final int i, final String basename, final String baseLabel, final SGroup g) throws SIdentityException {
        final List<SGroup> groups = new ArrayList<SGroup>();
        for (int j = 0; j < i; j++) {
            final SGroupBuilder inst = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setName(basename + j).setDisplayName(baseLabel + j);
            if (g != null) {
                inst.setParentPath(g.getPath());
            }
            final SGroup group = inst.done();
            identityService.createGroup(group);
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
            group = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setName("testGetNumberOfGroups" + i).setId(id).done();
            identityService.createGroup(group);
        }
        assertEquals("bad count of groups", numberOfGroups + 5, identityService.getNumberOfGroups());
        getTransactionService().complete();
    }

    @Test
    public void testGetGroupChildrenPaginated() throws Exception {
        getTransactionService().begin();
        deleteGroups();
        final List<SGroup> groups = createGroups(1, "testGetGroupChildrenPaginated_name", "testGetGroupChildrenPaginated_label", null);
        identityService.getGroup(groups.get(0).getId());

        final SGroup parentGroup = groups.iterator().next();
        createGroups(5, "testGetGroupChildrenPaginatedChildren_name", "testGetGroupChildrenPaginatedChildren_label", parentGroup);
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
        final List<SGroup> groups = createGroups(1, "testGetGroupChildrenWithCriterion_name", "testGetGroupChildrenWithCriterion_label", null);
        final SGroup parentGroup = groups.iterator().next();
        createGroups(5, "testGetGroupChildrenWithCriterionChildren_name", "testGetGroupChildrenWithCriterionChildren_label", parentGroup);
        getTransactionService().complete();

        getTransactionService().begin();
        List<SGroup> retrievedGroups = identityService.getGroupChildren(parentGroup.getId(), 0, 3, BuilderFactory.get(SGroupBuilderFactory.class).getNameKey(),
                OrderByType.DESC);
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved groups", 3, retrievedGroups.size());
        assertTrue("not in descending order", retrievedGroups.get(1).getName().compareTo(retrievedGroups.get(2).getName()) > 0);

        retrievedGroups = identityService.getGroupChildren(parentGroup.getId(), 0, 3, BuilderFactory.get(SGroupBuilderFactory.class).getNameKey(),
                OrderByType.ASC);
        getTransactionService().complete();
        assertNotNull("can't find the groups after adding them", retrievedGroups);
        assertEquals("bad number of retrieved groups", 3, retrievedGroups.size());
        assertTrue("not in descending order", retrievedGroups.get(0).getName().compareTo(retrievedGroups.get(1).getName()) < 0);

    }

    @Test
    public void testGetNumberOfGroupChildren() throws Exception {
        getTransactionService().begin();
        deleteGroups();
        final List<SGroup> groups = createGroups(1, "testGetNumberOfGroupChildren_name", "testGetNumberOfGroupChildren_label", null);
        final SGroup parentGroup = groups.iterator().next();
        createGroups(5, "testGetNumberOfGroupChildrenChildren_name", "testGetNumberOfGroupChildrenChildren_label", parentGroup);
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
            metadata = BuilderFactory.get(SCustomUserInfoDefinitionBuilderFactory.class).createNewInstance().setName("testGetProfileMetadataPaginated" + i)
                    .setId(id).done();
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
            info = BuilderFactory.get(SCustomUserInfoDefinitionBuilderFactory.class).createNewInstance().setName("testGetNumberOfCustomUserInfoDefinition" + i)
                    .setId(id).done();
            identityService.createCustomUserInfoDefinition(info);
        }
        assertEquals("bad count of custom user info definition", numberOfMetadata + 30, identityService.getNumberOfCustomUserInfoDefinition());
        getTransactionService().complete();
    }

    /*
     * Method that helps to retrieve users
     */
    @Test
    public void testGetUsersByRolePaginated() throws Exception {
        getTransactionService().begin();
        final SRole role = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetUsersByRole").done();
        identityService.createRole(role);
        final List<SGroup> groups = createGroups(2, "testGetUsersByRoleGroup", "testGetUsersByRoleGroup", null);
        for (int i = 0; i < 10; i++) {
            final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("user" + i).setPassword("kikoo");
            final SUser user = identityService.createUser(userBuilder.done());
            final SUserMembership userMembership1 = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                    .createNewInstance(user.getId(), groups.get(0).getId(), role.getId())
                    .done();
            identityService.createUserMembership(userMembership1);
        }
        for (int i = 10; i < 20; i++) {
            final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("user" + i).setPassword("kikoo");
            final SUser user = identityService.createUser(userBuilder.done());
            final SUserMembership userMembership2 = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                    .createNewInstance(user.getId(), groups.get(1).getId(), role.getId())
                    .done();
            identityService.createUserMembership(userMembership2);
        }

        final List<SUser> usersByRole = identityService.getUsersByRole(role.getId(), 10, 10);
        getTransactionService().complete();
        assertEquals("not all user were retrieved", 10, usersByRole.size());
    }

    @Test
    public void testGetUsersByRoleWithCriterion() throws Exception {
        getTransactionService().begin();

        final SRole role = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetUsersByRole").done();
        identityService.createRole(role);
        final List<SGroup> groups = createGroups(2, "testGetUsersByRoleGroup", "testGetUsersByRoleGroup", null);
        SUser user;
        for (int i = 0; i < 10; i++) {
            user = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("user" + i).setPassword("kikoo").done();
            final SUser user2 = identityService.createUser(user);
            final SUserMembership userMembership1 = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                    .createNewInstance(user2.getId(), groups.get(0).getId(), role.getId())
                    .done();
            identityService.createUserMembership(userMembership1);
        }
        for (int i = 10; i < 20; i++) {
            user = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("user" + i).setPassword("kikoo").done();
            final SUser user2 = identityService.createUser(user);
            final SUserMembership userMembership2 = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                    .createNewInstance(user2.getId(), groups.get(1).getId(), role.getId())
                    .done();
            identityService.createUserMembership(userMembership2);
        }

        List<SUser> usersByRole = identityService.getUsersByRole(role.getId(), 10, 10, BuilderFactory.get(SUserBuilderFactory.class).getUserNameKey(),
                OrderByType.DESC);
        assertEquals("not all user were retrieved", 10, usersByRole.size());
        assertTrue("not in descending order", usersByRole.get(1).getUserName().compareTo(usersByRole.get(2).getUserName()) > 0);
        usersByRole = identityService.getUsersByRole(role.getId(), 10, 10, BuilderFactory.get(SUserBuilderFactory.class).getUserNameKey(), OrderByType.ASC);
        getTransactionService().complete();
        assertEquals("not all user were retrieved", 10, usersByRole.size());
        assertTrue("not in asc order", usersByRole.get(1).getUserName().compareTo(usersByRole.get(2).getUserName()) < 0);
    }

    @Test
    public void testGetNumberOfUsersByRole() throws Exception {
        getTransactionService().begin();
        final SRole role = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetNumberOfUsersByRole").done();
        identityService.createRole(role);
        final long numberOfUsersByRole = identityService.getNumberOfUsersByRole(role.getId());
        final SGroup group = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setName("testGetUsersByGroup").done();
        identityService.createGroup(group);
        SUser user;
        for (int i = 0; i < 5; i++) {
            user = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("testGetNumberOfUsersByRole" + i).setPassword("kikoo").done();
            final SUser user2 = identityService.createUser(user);
            final SUserMembership userMembership = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                    .createNewInstance(user2.getId(), group.getId(), role.getId()).done();
            identityService.createUserMembership(userMembership);
        }
        assertEquals("not the good number of users by role", numberOfUsersByRole + 5, identityService.getNumberOfUsersByRole(role.getId()));
        getTransactionService().complete();
    }

    @Test
    public void testGetUsersByGroupPaginated() throws Exception {
        getTransactionService().begin();
        final SRole role = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetUsersByRole").done();
        identityService.createRole(role);
        final List<SGroup> groups = createGroups(2, "testGetUsersByRoleGroup", "testGetUsersByRoleGroup", null);
        SUser user;
        for (int i = 0; i < 5; i++) {
            final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("user" + i).setPassword("kikoo");
            user = identityService.createUser(userBuilder.done());
            final SUserMembership userMembership1 = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                    .createNewInstance(user.getId(), groups.get(0).getId(), role.getId())
                    .done();
            identityService.createUserMembership(userMembership1);
        }
        for (int i = 5; i < 10; i++) {
            final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("user" + i).setPassword("kikoo");
            user = identityService.createUser(userBuilder.done());
            final SUserMembership userMembership1 = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                    .createNewInstance(user.getId(), groups.get(1).getId(), role.getId())
                    .done();
            identityService.createUserMembership(userMembership1);
        }
        final List<SUser> usersByGroup = identityService.getUsersByGroup(groups.get(0).getId(), 1, 2);
        getTransactionService().complete();
        assertEquals("not the good number of user with the role", 2, usersByGroup.size());
    }

    @Test
    public void testGetUsersByGroupwithCriterion() throws Exception {
        getTransactionService().begin();

        final SRole role = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetUsersByRole").done();
        identityService.createRole(role);
        final List<SGroup> groups = createGroups(2, "testGetUsersByRoleGroup", "testGetUsersByRoleGroup", null);
        SUser user;
        for (int i = 0; i < 5; i++) {
            final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("user" + i).setPassword("kikoo");
            user = identityService.createUser(userBuilder.done());
            final SUserMembership userMembership1 = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                    .createNewInstance(user.getId(), groups.get(0).getId(), role.getId())
                    .done();
            identityService.createUserMembership(userMembership1);
        }
        for (int i = 5; i < 10; i++) {
            final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("user" + i).setPassword("kikoo");
            user = identityService.createUser(userBuilder.done());
            final SUserMembership userMembership1 = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                    .createNewInstance(user.getId(), groups.get(1).getId(), role.getId())
                    .done();
            identityService.createUserMembership(userMembership1);
        }

        List<SUser> usersByGroup = identityService.getUsersByGroup(groups.get(0).getId(), 1, 2, BuilderFactory.get(SUserBuilderFactory.class).getUserNameKey(),
                OrderByType.DESC);
        assertEquals("not the good number of user with the role", 2, usersByGroup.size());
        assertTrue("not in descending order", usersByGroup.get(0).getUserName().compareTo(usersByGroup.get(1).getUserName()) > 0);
        usersByGroup = identityService.getUsersByGroup(groups.get(0).getId(), 1, 2, BuilderFactory.get(SUserBuilderFactory.class).getUserNameKey(),
                OrderByType.ASC);
        getTransactionService().complete();
        assertEquals("not all user were retrieved", 2, usersByGroup.size());
        assertTrue("not in asc order", usersByGroup.get(0).getUserName().compareTo(usersByGroup.get(1).getUserName()) < 0);
    }

    @Test
    public void testGetNumberOfUsersByGroup() throws Exception {
        getTransactionService().begin();

        final SRole role = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setName("testGetUsersByRole").done();
        identityService.createRole(role);
        final List<SGroup> groups = createGroups(2, "testGetUsersByRoleGroup", "testGetUsersByRoleGroup", null);
        SUser user;
        for (int i = 0; i < 5; i++) {
            final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("user" + i).setPassword("kikoo");
            user = identityService.createUser(userBuilder.done());
            final SUserMembership userMembership1 = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                    .createNewInstance(user.getId(), groups.get(0).getId(), role.getId())
                    .done();
            identityService.createUserMembership(userMembership1);
        }
        for (int i = 5; i < 10; i++) {
            final SUserBuilder userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("user" + i).setPassword("kikoo");
            user = identityService.createUser(userBuilder.done());
            final SUserMembership userMembership1 = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                    .createNewInstance(user.getId(), groups.get(1).getId(), role.getId())
                    .done();
            identityService.createUserMembership(userMembership1);
        }
        final long id = groups.get(0).getId();

        assertEquals("not the good number of users by group", 5, identityService.getNumberOfUsersByGroup(id));
        getTransactionService().complete();
    }

    @Test
    public void testGetNumberOfUsersByMembership() throws Exception {
        getTransactionService().begin();
        final SGroup group = createGroups(1, "testGetUserByMembership", "testGetUserByMembership", null).iterator().next();
        final SRole role = createRoles(1, "testGetUserByMembership", "testGetUserByMembership").iterator().next();
        final List<SUser> users = createUsers(5, "getMembershipUsers");
        for (final SUser sUser : users) {
            final SUserMembership userMembership = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                    .createNewInstance(sUser.getId(), group.getId(), role.getId()).done();
            identityService.createUserMembership(userMembership);
        }
        assertEquals("not the good number of user by membership", 5, identityService.getNumberOfUsersByMembership(group.getId(), role.getId()));
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
        List<SUser> users = identityService.getUsers(5, 10, BuilderFactory.get(SUserBuilderFactory.class).getUserNameKey(), OrderByType.DESC);
        assertTrue("not in desc order", users.get(0).getUserName().compareTo(users.get(users.size() - 1).getUserName()) > 0);
        users = identityService.getUsers(5, 10, BuilderFactory.get(SUserBuilderFactory.class).getUserNameKey(), OrderByType.ASC);
        getTransactionService().complete();
        assertTrue("not in asc order", users.get(0).getUserName().compareTo(users.get(users.size() - 1).getUserName()) < 0);
    }

    @Test
    public void testGetUsersOrderByFirstName() throws Exception {
        getTransactionService().begin();
        deleteUsers();
        createUsers(10, "testGetUsersOrderByFirstName");
        List<SUser> users = identityService.getUsers(5, 10, BuilderFactory.get(SUserBuilderFactory.class).getFirstNameKey(), OrderByType.DESC);
        assertTrue("not in desc order", users.get(0).getFirstName().compareTo(users.get(users.size() - 1).getFirstName()) > 0);
        users = identityService.getUsers(5, 10, BuilderFactory.get(SUserBuilderFactory.class).getFirstNameKey(), OrderByType.ASC);
        getTransactionService().complete();
        assertTrue("not in asc order", users.get(0).getFirstName().compareTo(users.get(users.size() - 1).getFirstName()) < 0);
    }

    @Test
    public void testGetUsersOrderByLastName() throws Exception {
        getTransactionService().begin();
        deleteUsers();
        createUsers(10, "testGetUsersOrderByLastName");
        List<SUser> users = identityService.getUsers(5, 10, BuilderFactory.get(SUserBuilderFactory.class).getLastNameKey(), OrderByType.DESC);
        assertTrue("not in desc order", users.get(0).getLastName().compareTo(users.get(users.size() - 1).getLastName()) > 0);
        users = identityService.getUsers(5, 10, BuilderFactory.get(SUserBuilderFactory.class).getLastNameKey(), OrderByType.ASC);
        getTransactionService().complete();
        assertTrue("not in asc order", users.get(0).getLastName().compareTo(users.get(users.size() - 1).getLastName()) < 0);
    }

    private List<SUser> createUsers(final int i, final String baseUsername) throws SIdentityException {
        final List<SUser> ids = new ArrayList<SUser>();
        for (int j = 0; j < i; j++) {
            final SUser user = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName(baseUsername + j).setFirstName("firstName" + j)
                    .setLastName("lastName" + j).setPassword("password" + j).done();
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
        final SUser user = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("testGetNumberOfUsers").setPassword("kikoo").done();
        identityService.createUser(user);
        assertEquals(numberOfUsers + 1, identityService.getNumberOfUsers());
        getTransactionService().complete();
    }

    @Test
    public void testGetUsersByManager() throws Exception {
        getTransactionService().begin();
        createUsers(11, "testGetUsersByManager");
        final SUser manager = identityService.getUsers(0, 1).get(0);
        final List<SUser> users = identityService.getUsers(1, 10);
        for (final SUser user : users) {
            final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class).createNewInstance()
                    .updateManagerUserId(manager.getId()).done();
            identityService.updateUser(user, changeDescriptor);
        }
        final long id = manager.getId();
        final List<SUser> usersByManager = identityService.getUsersByManager(id, 0, 20);
        getTransactionService().complete();
        assertEquals("did not retrieved all user having the manager", 10, usersByManager.size());
        for (final SUser sUser : usersByManager) {
            assertEquals("One of the user have not the good manager", manager.getId(), sUser.getManagerUserId());
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
        EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class).createNewInstance().updateManagerUserId(manager.getId())
                .done();
        identityService.updateUser(user, changeDescriptor);
        final long id = manager.getId();
        final List<SUser> usersByManager = identityService.getUsersByManager(id, 0, 20);
        assertEquals("did not retrieved all user having the manager", 1, usersByManager.size());
        for (final SUser sUser : usersByManager) {
            assertEquals("One of the user have not the good manager", manager.getId(), sUser.getManagerUserId());
        }
        changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class).createNewInstance().updateFirstName("kevin").done();
        identityService.updateUser(user, changeDescriptor);
        user = identityService.getUser(user.getId());
        assertEquals("kevin", user.getFirstName());
        assertEquals(manager.getId(), user.getManagerUserId());

        changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class).createNewInstance().updateManagerUserId(newManager.getId()).done();
        identityService.updateUser(user, changeDescriptor);
        user = identityService.getUser(user.getId());
        assertEquals(newManager.getId(), user.getManagerUserId());
        getTransactionService().complete();
    }

    @Test
    public void testUpdateUser() throws Exception {
        getTransactionService().begin();
        SUserBuilder userBuilder = null;
        userBuilder = BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("testUpdateUser").setPassword("kikoo")
                .setFirstName("Update").setLastName("User");
        final SUser user = identityService.createUser(userBuilder.done());
        final String password = user.getPassword();
        final SContactInfo contactInfo = BuilderFactory.get(SContactInfoBuilderFactory.class).createNewInstance(user.getId(), true).setAddress("Somewhere")
                .setBuilding("AA11")
                .setCity("Taiwan").done();
        identityService.createUserContactInfo(contactInfo);
        final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SUserUpdateBuilderFactory.class).createNewInstance()
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
        final EntityUpdateDescriptor updateContactInfo = BuilderFactory.get(SContactInfoUpdateBuilderFactory.class).createNewInstance()
                .updateAddress(newAddress).updateCity(newCity)
                .updateBuilding(newBuilding).updateCountry(country).updateEmail(email).updateFaxNumber(faxNumber).updateMobileNumber(mobileNumber)
                .updatePhoneNumber(phoneNumber).updateRoom(room).updateState(state).updateWebsite(website).updateZipCode(zipCode).done();
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
        final SCustomUserInfoDefinition metadata = BuilderFactory.get(SCustomUserInfoDefinitionBuilderFactory.class).createNewInstance().setId(metadataId)
                .setName("testAddProfileMetadata").done();
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
        final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SCustomUserInfoDefinitionUpdateBuilderFactory.class).createNewInstance()
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
        final SRole role = BuilderFactory.get(SRoleBuilderFactory.class).createNewInstance().setId(id).setName("testAddRole").done();
        identityService.createRole(role);
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
        final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SRoleUpdateBuilderFactory.class).createNewInstance().updateName(newName).done();
        identityService.updateRole(role, changeDescriptor);
        final SRole role2 = identityService.getRole(id);
        getTransactionService().complete();
        assertNotNull("can't find the updated role", role2);
        assertEquals("not udpated", newName, role2.getName());
    }

    @Test
    public void testAddGroup() throws Exception {
        getTransactionService().begin();
        final long id = new Date().getTime();
        final SGroup group = BuilderFactory.get(SGroupBuilderFactory.class).createNewInstance().setId(id).setName("testAddGroup").done();
        identityService.createGroup(group);
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
        final EntityUpdateDescriptor changeDescriptor = BuilderFactory.get(SGroupUpdateBuilderFactory.class).createNewInstance().updateName(newName).done();
        identityService.updateGroup(group, changeDescriptor);
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
        final SCustomUserInfoDefinition metadataDefinition = BuilderFactory.get(SCustomUserInfoDefinitionBuilderFactory.class).createNewInstance()
                .setName("kikooMetadata").done();
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
        final List<SGroup> groups = createGroups(1, "testAddMembershipToUser_name", "testAddMembershipToUser_label", null);
        createRoles(1, "testAddMembershipToUser_name", "testAddMembershipToUser_label");
        final SGroup group = groups.iterator().next();

        final SRole role = identityService.getRoleByName("testAddMembershipToUser_name0");
        createUsers(1, "testAddMembershipToUser");
        SUser user = identityService.getUserByUserName("testAddMembershipToUser0");
        assertNotNull(user);
        getTransactionService().complete();

        getTransactionService().begin();
        user = identityService.getUserByUserName("testAddMembershipToUser0");
        final int size = identityService.getUserMembershipsOfUser(user.getId(), 0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS).size();
        final SUserMembership userMembership = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                .createNewInstance(user.getId(), group.getId(), role.getId()).done();
        identityService.createUserMembership(userMembership);
        getTransactionService().complete();

        getTransactionService().begin();
        final SUser user2 = identityService.getUser(user.getId());
        assertEquals("membership not added", size + 1, identityService.getUserMembershipsOfUser(user2.getId(), 0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS)
                .size());
        getTransactionService().complete();
    }

    @Test
    public void testRemoveMembershipFromUser() throws Exception {
        getTransactionService().begin();
        deleteGroups();
        final List<SGroup> groups = createGroups(1, "testRemoveMembershipFromUser_name", "testRemoveMembershipFromUser_label", null);
        createRoles(1, "testRemoveMembershipFromUser_name", "testRemoveMembershipFromUser_label");
        final SGroup group = groups.iterator().next();
        final SRole role = identityService.getRoleByName("testRemoveMembershipFromUser_name0");
        createUsers(1, "testRemoveMembershipFromUser");
        SUser user = identityService.getUserByUserName("testRemoveMembershipFromUser0");
        assertNotNull(user);
        getTransactionService().complete();

        getTransactionService().begin();

        user = identityService.getUserByUserName("testRemoveMembershipFromUser0");
        final SUserMembership userMembership = BuilderFactory.get(SUserMembershipBuilderFactory.class)
                .createNewInstance(user.getId(), group.getId(), role.getId()).done();
        identityService.createUserMembership(userMembership);
        getTransactionService().complete();

        getTransactionService().begin();
        user = identityService.getUserByUserName("testRemoveMembershipFromUser0");
        final int size = identityService.getUserMembershipsOfUser(user.getId(), 0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS).size();
        assertTrue("no membership on user", size >= 1);

        final SUserMembership userMembership2 = identityService.getUserMembership(user.getId(), group.getId(), role.getId());
        identityService.deleteUserMembership(userMembership2);

        final SUser user2 = identityService.getUser(user.getId());

        assertEquals("no membership on user", size - 1, identityService.getUserMembershipsOfUser(user2.getId(), 0, QueryOptions.UNLIMITED_NUMBER_OF_RESULTS)
                .size());
        getTransactionService().complete();
    }

    @Test
    public void searchUsersWithWildcards() throws Exception {
        getTransactionService().begin();
        final SUser user1 = identityService.createUser(BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("user1")
                .setFirstName("firstname1")
                .setLastName("lastname1").setPassword("lkh").done());
        final SUser user2 = identityService.createUser(BuilderFactory.get(SUserBuilderFactory.class).createNewInstance().setUserName("user2")
                .setFirstName("firstname2")
                .setLastName("lastname2").setPassword("mlbxcvjmsdkljf").done());
        getTransactionService().complete();

        final Map<Class<? extends PersistentObject>, Set<String>> userAllFields = new HashMap<Class<? extends PersistentObject>, Set<String>>();
        final Set<String> fields = new HashSet<String>(4);
        fields.add("userName");
        fields.add("firstName");
        fields.add("lastName");
        fields.add("jobTitle");
        userAllFields.put(SUser.class, fields);
        final QueryOptions queryOptions = new QueryOptions(0, 10, Arrays.asList(new OrderByOption(SUser.class, "userName", OrderByType.ASC)),
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
