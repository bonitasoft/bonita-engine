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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.Test;

public class RoleIT extends TestWithTechnicalUser {

    @Test
    public void createRoleUsingTheRoleBuilder() throws BonitaException {
        final String manager = "manager";
        final Role role = getIdentityAPI().createRole(manager);
        assertNotNull(role);
        assertEquals(manager, role.getName());
        getIdentityAPI().deleteRole(role.getId());
    }

    @Test(expected = AlreadyExistsException.class)
    public void cannotCreateARoleWhichAlreadyExists() throws BonitaException {
        final String manager = "manager";
        final Role role = getIdentityAPI().createRole(manager);
        final long roleId = role.getId();
        try {
            getIdentityAPI().createRole(manager);
        } finally {
            getIdentityAPI().deleteRole(roleId);
        }
    }

    @Test(expected = CreationException.class)
    public void cannotCreateANullRole() throws BonitaException {
        getIdentityAPI().createRole((RoleCreator) null);
    }

    @Test(expected = CreationException.class)
    public void createARoleFail() throws BonitaException {
        getIdentityAPI().createRole((String) null);
    }

    @Test
    public void getRoleByName() throws BonitaException {
        final String manager = "manager";
        final Role createdRole = getIdentityAPI().createRole(manager);
        final Role searchedRole = getIdentityAPI().getRoleByName(manager);
        assertEquals(manager, searchedRole.getName());
        assertEquals(createdRole.getId(), searchedRole.getId());

        getIdentityAPI().deleteRole(searchedRole.getId());
    }

    @Test(expected = RoleNotFoundException.class)
    public void cannotGetARoleByAnUnexistingName() throws BonitaException {
        getIdentityAPI().getRoleByName("manager");
    }

    @Test
    public void getAnEmptyListWhenNoRolesAreDefined() {
        final List<Role> roles = getIdentityAPI().getRoles(0, 10, RoleCriterion.NAME_ASC);
        assertEquals(0, roles.size());
    }

    @Test
    public void getARole() throws Exception {
        final String manager = "manager";
        final Role role = getIdentityAPI().createRole(manager);
        final List<Role> roles = getIdentityAPI().getRoles(0, 10, RoleCriterion.NAME_ASC);
        assertEquals(1, roles.size());

        getIdentityAPI().deleteRole(role.getId());
    }

    @Test
    public void getRole() throws BonitaException {
        final String manager = "manager";
        final Role createdRole = getIdentityAPI().createRole(manager);

        final Role role = getIdentityAPI().getRole(createdRole.getId());
        assertNotNull(role);
        assertEquals(manager, role.getName());

        getIdentityAPI().deleteRole(role.getId());
    }

    @Test
    public void roleNameAndDisplayNameShouldAccept255Chars() throws BonitaException {
        final String stringIndex_255_chars = "abcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxy12345";
        final Role role = getIdentityAPI().createRole(new RoleCreator(stringIndex_255_chars).setDisplayName(stringIndex_255_chars));

        // Should be no exception:

        getIdentityAPI().deleteRole(role.getId());
    }

    @Test(expected = Exception.class)
    public void roleNameShouldNotAccept256Chars() throws BonitaException {
        final String stringIndex_256_chars = "_abcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxy12345";
        getIdentityAPI().createRole(stringIndex_256_chars);
    }

    @Test(expected = Exception.class)
    public void roleDisplayNameShouldNotAccept256Chars() throws BonitaException {
        final String stringIndex_256_chars = "_abcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxyabcdefghijklmnopqrstuvwxy12345";
        getIdentityAPI().createRole(new RoleCreator("someName").setDisplayName(stringIndex_256_chars));
    }

    @Test
    public void getRolesByIDs() throws BonitaException {
        final String manager = "manager";
        final Role roleCreated1 = getIdentityAPI().createRole(manager);
        final String teamManager = "teamManager";
        final Role roleCreated2 = getIdentityAPI().createRole(teamManager);

        final List<Long> roleIds = new ArrayList<Long>();
        roleIds.add(roleCreated1.getId());
        roleIds.add(roleCreated2.getId());

        final Map<Long, Role> roles = getIdentityAPI().getRoles(roleIds);
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertEquals(manager, roles.get(roleCreated1.getId()).getName());
        assertEquals(teamManager, roles.get(roleCreated2.getId()).getName());

        getIdentityAPI().deleteRole(roles.get(roleCreated1.getId()).getId());
        getIdentityAPI().deleteRole(roles.get(roleCreated2.getId()).getId());
    }

    public void getRolesByIDsWithoutRoleNotFoundException() throws BonitaException {
        final String manager = "manager";
        final Role roleCreated1 = getIdentityAPI().createRole(manager);
        final String teamManager = "teamManager";
        final Role roleCreated2 = getIdentityAPI().createRole(teamManager);

        final List<Long> roleIds = new ArrayList<Long>();
        roleIds.add(roleCreated1.getId());
        roleIds.add(roleCreated2.getId() + 100);

        final Map<Long, Role> roles = getIdentityAPI().getRoles(roleIds);
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals(teamManager, roles.get(roleCreated1.getId()).getName());

        getIdentityAPI().deleteRole(roleCreated1.getId());
        getIdentityAPI().deleteRole(roleCreated2.getId());
    }

    @Test(expected = RoleNotFoundException.class)
    public void cannotGetAnUnexistingRole() throws BonitaException {
        getIdentityAPI().getRole(45);
    }

    @Test
    public void getNumberOfRoles() throws BonitaException {
        final String manager = "manager";
        final String developer = "developer";
        final Role role1 = getIdentityAPI().createRole(manager);
        final Role role2 = getIdentityAPI().createRole(developer);
        assertEquals(manager, role1.getName());
        assertEquals(developer, role2.getName());

        final long numberOfRoles = getIdentityAPI().getNumberOfRoles();
        assertEquals(2, numberOfRoles);

        getIdentityAPI().deleteRole(role1.getId());
        getIdentityAPI().deleteRole(role2.getId());
    }

    @Test
    public void noRolesWhenThePageIndexIsOutOfRange() {
        final List<Role> roles = getIdentityAPI().getRoles(5, 10, RoleCriterion.NAME_ASC);
        assertTrue(roles.isEmpty());
    }

    @Test
    public void deleteARole() throws BonitaException {
        final String manager = "manager";
        final Role role = getIdentityAPI().createRole(manager);
        getIdentityAPI().deleteRole(role.getId());
    }

    @Test
    public void deleteRoles() throws BonitaException {
        final String manager = "manager";
        final String developer = "developer";
        final Role role1 = getIdentityAPI().createRole(manager);
        final Role role2 = getIdentityAPI().createRole(developer);
        final List<Long> roleIds = new ArrayList<Long>();
        roleIds.add(role2.getId());
        roleIds.add(role1.getId());
        getIdentityAPI().deleteRoles(roleIds);

        final long numberOfRoles = getIdentityAPI().getNumberOfRoles();
        assertEquals(0, numberOfRoles);
    }

    @Test
    public void cannotDeleteARoleTwice() throws BonitaException {
        final String developer = "developer";
        final Role role = getIdentityAPI().createRole(developer);
        getIdentityAPI().deleteRole(role.getId());
        getIdentityAPI().deleteRole(role.getId());
    }

    @Test
    public void canDeleteNoRole() throws BonitaException {
        final List<Long> roleIds = new ArrayList<Long>();
        getIdentityAPI().deleteRoles(roleIds);
    }

    @Test(expected = DeletionException.class)
    public void cannotDeleteNullRoles() throws BonitaException {
        getIdentityAPI().deleteRoles(null);
    }

    @Test
    public void updateARole() throws Exception {
        final String developer = "developer";
        final Role role = getIdentityAPI().createRole(developer);
        assertEquals(developer, role.getName());

        final String manager = "manager";
        final RoleUpdater updateDescriptor = new RoleUpdater();
        updateDescriptor.setName(manager);
        final Role updatedRole = getIdentityAPI().updateRole(role.getId(), updateDescriptor);
        assertNotNull(updatedRole);
        assertEquals(manager, updatedRole.getName());

        getIdentityAPI().deleteRole(updatedRole.getId());
    }

    @Test(expected = UpdateException.class)
    public void canontUpdateARoleWithANullUpdateDescriptor() throws Exception {
        final String developer = "developer";
        final Role role = getIdentityAPI().createRole(developer);
        try {
            getIdentityAPI().updateRole(role.getId(), null);
        } finally {
            getIdentityAPI().deleteRole(role.getId());
        }
    }

    @Test(expected = RoleNotFoundException.class)
    public void cannotUpdateARoleWithAnUnexistingRoleIdentifier() throws Exception {
        final String manager = "manager";
        final RoleUpdater updateDescriptor = new RoleUpdater();
        updateDescriptor.setName(manager);
        getIdentityAPI().updateRole(83, updateDescriptor);
    }

    @Test
    public void getUsersOfARole() throws BonitaException {
        final String developer = "developer";
        final Role role = getIdentityAPI().createRole(developer);

        final User user1 = getIdentityAPI().createUser("user1", "bpm");
        final User user2 = getIdentityAPI().createUser("user2", "bpm");
        final User user3 = getIdentityAPI().createUser("user3", "bpm");
        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(user1.getId());
        userIds.add(user2.getId());

        final Group group = getIdentityAPI().createGroup("R&D", null);
        getIdentityAPI().addUserMemberships(userIds, group.getId(), role.getId());

        final List<User> users = getIdentityAPI().getUsersInRole(role.getId(), 0, 10, UserCriterion.USER_NAME_ASC);
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals(user1, users.get(0));
        assertEquals(user2, users.get(1));

        getIdentityAPI().deleteUserMemberships(userIds, group.getId(), role.getId());
        getIdentityAPI().deleteRole(role.getId());
        getIdentityAPI().deleteGroup(group.getId());

        getIdentityAPI().deleteUsers(userIds);
        getIdentityAPI().deleteUser(user3.getId());
    }

    @Test
    public void getNumberOfUsersInRole() throws BonitaException {
        final String developer = "developer";
        final Role role = getIdentityAPI().createRole(developer);

        final User user1 = getIdentityAPI().createUser("user1", "bpm");
        final User user2 = getIdentityAPI().createUser("user2", "bpm");
        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(user1.getId());
        userIds.add(user2.getId());

        final Group group = getIdentityAPI().createGroup("R&D", null);
        getIdentityAPI().addUserMemberships(userIds, group.getId(), role.getId());

        final long count = getIdentityAPI().getNumberOfUsersInRole(role.getId());
        assertEquals(2, count);

        getIdentityAPI().deleteUserMemberships(userIds, group.getId(), role.getId());
        getIdentityAPI().deleteRole(role.getId());
        getIdentityAPI().deleteGroup(group.getId());
        getIdentityAPI().deleteUser(user1.getId());
        getIdentityAPI().deleteUser(user2.getId());
    }

    @Test
    public void getPaginatedRolesWithRoleCriterion() throws BonitaException {
        final String developer = "developer";
        final String manager = "manager";
        final String cto = "chief technology officer";

        final RoleCreator roleCreator1 = new RoleCreator(developer);
        roleCreator1.setDisplayName(developer);
        final Role aRole = getIdentityAPI().createRole(roleCreator1);
        final RoleCreator roleCreator2 = new RoleCreator(manager);
        roleCreator2.setDisplayName(manager);
        final Role bRole = getIdentityAPI().createRole(roleCreator2);
        final RoleCreator roleCreator3 = new RoleCreator(cto);
        roleCreator3.setDisplayName(cto);
        final Role cRole = getIdentityAPI().createRole(roleCreator3);

        final List<Role> rolesNameAsc = getIdentityAPI().getRoles(0, 3, RoleCriterion.NAME_ASC);
        assertEquals(3, rolesNameAsc.size());
        assertEquals(cto, rolesNameAsc.get(0).getName());
        assertEquals(developer, rolesNameAsc.get(1).getName());
        assertEquals(manager, rolesNameAsc.get(2).getName());

        final List<Role> rolesNameDesc = getIdentityAPI().getRoles(0, 3, RoleCriterion.NAME_DESC);
        assertEquals(3, rolesNameDesc.size());
        assertEquals(manager, rolesNameDesc.get(0).getName());
        assertEquals(developer, rolesNameDesc.get(1).getName());
        assertEquals(cto, rolesNameDesc.get(2).getName());

        final List<Role> rolesLabelAsc = getIdentityAPI().getRoles(0, 3, RoleCriterion.DISPLAY_NAME_ASC);
        assertEquals(3, rolesLabelAsc.size());
        assertEquals(cto, rolesLabelAsc.get(0).getDisplayName());
        assertEquals(developer, rolesLabelAsc.get(1).getDisplayName());
        assertEquals(manager, rolesLabelAsc.get(2).getDisplayName());

        final List<Role> rolesLabelDesc = getIdentityAPI().getRoles(0, 3, RoleCriterion.DISPLAY_NAME_DESC);
        assertEquals(3, rolesLabelDesc.size());
        assertEquals(manager, rolesLabelDesc.get(0).getDisplayName());
        assertEquals(developer, rolesLabelDesc.get(1).getDisplayName());
        assertEquals(cto, rolesLabelDesc.get(2).getDisplayName());

        getIdentityAPI().deleteRole(aRole.getId());
        getIdentityAPI().deleteRole(bRole.getId());
        getIdentityAPI().deleteRole(cRole.getId());
    }

    @Test
    public void getRolesOnMultiPages() throws BonitaException {
        final String roleName1 = "role1";
        final String roleName2 = "role2";
        final String roleName3 = "role3";
        final String roleName4 = "role4";
        final String roleName5 = "role5";
        final Role role1 = getIdentityAPI().createRole(roleName1);
        final Role role2 = getIdentityAPI().createRole(roleName2);
        final Role role3 = getIdentityAPI().createRole(roleName3);
        final Role role4 = getIdentityAPI().createRole(roleName4);
        final Role role5 = getIdentityAPI().createRole(roleName5);

        List<Role> rolesNameAsc = getIdentityAPI().getRoles(0, 2, RoleCriterion.NAME_ASC);
        assertEquals(2, rolesNameAsc.size());
        assertEquals(roleName1, rolesNameAsc.get(0).getName());
        assertEquals(roleName2, rolesNameAsc.get(1).getName());

        rolesNameAsc = getIdentityAPI().getRoles(2, 2, RoleCriterion.NAME_ASC);
        assertEquals(2, rolesNameAsc.size());
        assertEquals(roleName3, rolesNameAsc.get(0).getName());
        assertEquals(roleName4, rolesNameAsc.get(1).getName());

        rolesNameAsc = getIdentityAPI().getRoles(4, 2, RoleCriterion.NAME_ASC);
        assertEquals(1, rolesNameAsc.size());
        assertEquals(roleName5, rolesNameAsc.get(0).getName());
        final List<Role> roles = getIdentityAPI().getRoles(5, 2, RoleCriterion.NAME_ASC);
        assertTrue(roles.isEmpty());

        getIdentityAPI().deleteRole(role1.getId());
        getIdentityAPI().deleteRole(role2.getId());
        getIdentityAPI().deleteRole(role3.getId());
        getIdentityAPI().deleteRole(role4.getId());
        getIdentityAPI().deleteRole(role5.getId());
    }

    @Test(expected = AlreadyExistsException.class)
    public void cannotCreateTwoRoleWithTheSameName() throws BonitaException {
        final String role = "role";
        final Role role1 = getIdentityAPI().createRole(role);
        try {
            getIdentityAPI().createRole(role);
        } finally {
            getIdentityAPI().deleteRole(role1.getId());
        }
    }

    @Test
    public void searchRoleUsingFilter() throws BonitaException {
        final RoleCreator roleCreator1 = new RoleCreator("manager");
        roleCreator1.setDisplayName("Man");
        final Role mananger = getIdentityAPI().createRole(roleCreator1);
        final RoleCreator roleCreator2 = new RoleCreator("developer");
        roleCreator2.setDisplayName("Dev");
        final Role dev = getIdentityAPI().createRole(roleCreator2);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(RoleSearchDescriptor.DISPLAY_NAME, "Dev");
        final SearchResult<Role> searchRoles = getIdentityAPI().searchRoles(builder.done());
        assertNotNull(searchRoles);
        assertEquals(1, searchRoles.getCount());
        final List<Role> roles = searchRoles.getResult();
        assertEquals(dev, roles.get(0));

        getIdentityAPI().deleteRole(mananger.getId());
        getIdentityAPI().deleteRole(dev.getId());
    }

    @Cover(classes = { SearchOptionsBuilder.class, IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "SearchRole", "Apostrophe" }, jira = "ENGINE-366")
    @Test
    public void searchRoleWithApostrophe() throws BonitaException {
        final RoleCreator roleCreator1 = new RoleCreator("mana'ger");
        roleCreator1.setDisplayName("A");
        final Role mananger = getIdentityAPI().createRole(roleCreator1);
        final RoleCreator roleCreator2 = new RoleCreator("developer");
        roleCreator2.setDisplayName("mana'B");
        final Role dev = getIdentityAPI().createRole(roleCreator2);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(RoleSearchDescriptor.DISPLAY_NAME, Order.ASC);
        builder.searchTerm("mana'");
        final SearchResult<Role> searchRoles = getIdentityAPI().searchRoles(builder.done());
        assertNotNull(searchRoles);
        assertEquals(2, searchRoles.getCount());
        final List<Role> roles = searchRoles.getResult();
        assertEquals(mananger, roles.get(0));
        assertEquals(dev, roles.get(1));

        getIdentityAPI().deleteRole(mananger.getId());
        getIdentityAPI().deleteRole(dev.getId());
    }

    @Test
    public void getRolesFromIds() throws BonitaException {
        final RoleCreator roleCreator1 = new RoleCreator("managerA");
        roleCreator1.setDisplayName("managerA");
        final Role RoleA = getIdentityAPI().createRole(roleCreator1);
        final RoleCreator roleCreator2 = new RoleCreator("managerB");
        roleCreator2.setDisplayName("managerB");
        final Role RoleB = getIdentityAPI().createRole(roleCreator2);
        final RoleCreator roleCreator3 = new RoleCreator("managerC");
        roleCreator3.setDisplayName("managerC");
        final Role RoleC = getIdentityAPI().createRole(roleCreator3);
        final RoleCreator roleCreator4 = new RoleCreator("managerD");
        roleCreator4.setDisplayName("managerD");
        final Role RoleD = getIdentityAPI().createRole(roleCreator4);

        final long roleAId = RoleA.getId();
        final long roleBId = RoleB.getId();
        final long roleCId = RoleC.getId();
        final long roleDId = RoleD.getId();

        final List<Long> roleIds = new ArrayList<Long>();
        roleIds.add(roleAId);
        roleIds.add(roleBId);
        roleIds.add(roleCId);
        roleIds.add(roleDId);

        final Map<Long, Role> roles = getIdentityAPI().getRoles(roleIds);
        assertNotNull(roles);
        assertEquals(4, roles.size());
        assertEquals(RoleA, roles.get(roleAId));
        assertEquals(RoleB, roles.get(roleBId));
        assertEquals(RoleC, roles.get(roleCId));
        assertEquals(RoleD, roles.get(roleDId));

        getIdentityAPI().deleteRoles(roleIds);
    }

    @Test
    public void checkCreatedByForRole() throws BonitaException {
        final String manager = "manager";
        final Role createdRole = getIdentityAPI().createRole(manager);
        // check createdBy for role
        assertNotNull(createdRole.getCreatedBy());
        assertEquals(getSession().getUserId(), createdRole.getCreatedBy());
        getIdentityAPI().deleteRole(createdRole.getId());
    }

}
