package org.bonitasoft.engine.identity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.CommonAPITest;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.exception.UpdateException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RoleTest extends CommonAPITest {

    private static IdentityAPI identityAPI;

    private static APISession session;

    @Before
    public void before() throws BonitaException {
        session = loginDefaultTenant();
        identityAPI = TenantAPIAccessor.getIdentityAPI(session);
    }

    @After
    public void after() throws BonitaException {
        logoutTenant(session);
        identityAPI = null;
    }

    @Test
    public void testCreateRoleUsingTheRoleBuilder() throws BonitaException {
        final String manager = "manager";
        final Role role = identityAPI.createRole(manager);
        assertNotNull(role);
        assertEquals(manager, role.getName());
        identityAPI.deleteRole(role.getId());
    }

    @Test(expected = AlreadyExistsException.class)
    public void cannotCreateARoleWhichAlreadyExists() throws BonitaException {
        final String manager = "manager";
        final Role role = identityAPI.createRole(manager);
        final long roleId = role.getId();
        try {
            identityAPI.createRole(manager);
        } finally {
            identityAPI.deleteRole(roleId);
        }
    }

    @Test(expected = CreationException.class)
    public void cannotCreateANullRole() throws BonitaException {
        identityAPI.createRole((RoleCreator) null);
    }

    @Test(expected = CreationException.class)
    public void createARoleFail() throws BonitaException {
        identityAPI.createRole((String) null);
    }

    @Test
    public void getRoleByName() throws BonitaException {
        final String manager = "manager";
        final Role createdRole = identityAPI.createRole(manager);
        final Role searchedRole = identityAPI.getRoleByName(manager);
        assertEquals(manager, searchedRole.getName());
        assertEquals(createdRole.getId(), searchedRole.getId());

        identityAPI.deleteRole(searchedRole.getId());
    }

    @Test(expected = RoleNotFoundException.class)
    public void cannotGetARoleByAnUnexistingName() throws BonitaException {
        identityAPI.getRoleByName("manager");
    }

    @Test
    public void getAnEmptyListWhenNoRolesAreDefined() {
        final List<Role> roles = identityAPI.getRoles(0, 10, RoleCriterion.NAME_ASC);
        assertEquals(0, roles.size());
    }

    @Test
    public void getARole() throws Exception {
        final String manager = "manager";
        final Role role = identityAPI.createRole(manager);
        final List<Role> roles = identityAPI.getRoles(0, 10, RoleCriterion.NAME_ASC);
        assertEquals(1, roles.size());

        identityAPI.deleteRole(role.getId());
    }

    @Test
    public void getRole() throws BonitaException {
        final String manager = "manager";
        final Role createdRole = identityAPI.createRole(manager);

        final Role role = identityAPI.getRole(createdRole.getId());
        assertNotNull(role);
        assertEquals(manager, role.getName());

        identityAPI.deleteRole(role.getId());
    }

    @Test
    public void getRolesByIDs() throws BonitaException {
        final APISession session = loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final String manager = "manager";
        final Role roleCreated1 = identityAPI.createRole(manager);
        final String teamManager = "teamManager";
        final Role roleCreated2 = identityAPI.createRole(teamManager);

        final List<Long> roleIds = new ArrayList<Long>();
        roleIds.add(roleCreated1.getId());
        roleIds.add(roleCreated2.getId());

        final Map<Long, Role> roles = identityAPI.getRoles(roleIds);
        assertNotNull(roles);
        assertEquals(2, roles.size());
        assertEquals(manager, roles.get(roleCreated1.getId()).getName());
        assertEquals(teamManager, roles.get(roleCreated2.getId()).getName());

        identityAPI.deleteRole(roles.get(roleCreated1.getId()).getId());
        identityAPI.deleteRole(roles.get(roleCreated2.getId()).getId());
        logoutTenant(session);
    }

    public void getRolesByIDsWithoutRoleNotFoundException() throws BonitaException {
        final APISession session = loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final String manager = "manager";
        final Role roleCreated1 = identityAPI.createRole(manager);
        final String teamManager = "teamManager";
        final Role roleCreated2 = identityAPI.createRole(teamManager);

        final List<Long> roleIds = new ArrayList<Long>();
        roleIds.add(roleCreated1.getId());
        roleIds.add(roleCreated2.getId() + 100);

        final Map<Long, Role> roles = identityAPI.getRoles(roleIds);
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals(teamManager, roles.get(roleCreated1.getId()).getName());

        identityAPI.deleteRole(roleCreated1.getId());
        identityAPI.deleteRole(roleCreated2.getId());
        logoutTenant(session);
    }

    @Test(expected = RoleNotFoundException.class)
    public void cannotGetAnUnexistingRole() throws BonitaException {
        identityAPI.getRole(45);
    }

    @Test
    public void getNumberOfRoles() throws BonitaException {
        final String manager = "manager";
        final String developer = "developer";
        final Role role1 = identityAPI.createRole(manager);
        final Role role2 = identityAPI.createRole(developer);
        assertEquals(manager, role1.getName());
        assertEquals(developer, role2.getName());

        final long numberOfRoles = identityAPI.getNumberOfRoles();
        assertEquals(2, numberOfRoles);

        identityAPI.deleteRole(role1.getId());
        identityAPI.deleteRole(role2.getId());
    }

    @Test
    public void noRolesWhenThePageIndexIsOutOfRange() {
        final List<Role> roles = identityAPI.getRoles(5, 10, RoleCriterion.NAME_ASC);
        assertTrue(roles.isEmpty());
    }

    @Test
    public void deleteARole() throws BonitaException {
        final String manager = "manager";
        final Role role = identityAPI.createRole(manager);
        identityAPI.deleteRole(role.getId());
    }

    @Test
    public void deleteRoles() throws BonitaException {
        final String manager = "manager";
        final String developer = "developer";
        final Role role1 = identityAPI.createRole(manager);
        final Role role2 = identityAPI.createRole(developer);
        final List<Long> roleIds = new ArrayList<Long>();
        roleIds.add(role2.getId());
        roleIds.add(role1.getId());
        identityAPI.deleteRoles(roleIds);

        final long numberOfRoles = identityAPI.getNumberOfRoles();
        assertEquals(0, numberOfRoles);
    }

    @Test
    public void cannotDeleteARoleTwice() throws BonitaException {
        final String developer = "developer";
        final Role role = identityAPI.createRole(developer);
        identityAPI.deleteRole(role.getId());
        identityAPI.deleteRole(role.getId());
    }

    @Test
    public void canDeleteNoRole() throws BonitaException {
        final List<Long> roleIds = new ArrayList<Long>();
        identityAPI.deleteRoles(roleIds);
    }

    @Test(expected = DeletionException.class)
    public void cannotDeleteNullRoles() throws BonitaException {
        identityAPI.deleteRoles(null);
    }

    @Test
    public void updateARole() throws Exception {
        final String developer = "developer";
        final Role role = identityAPI.createRole(developer);
        assertEquals(developer, role.getName());

        final String manager = "manager";
        final RoleUpdater updateDescriptor = new RoleUpdater();
        updateDescriptor.setName(manager);
        final Role updatedRole = identityAPI.updateRole(role.getId(), updateDescriptor);
        assertNotNull(updatedRole);
        assertEquals(manager, updatedRole.getName());

        identityAPI.deleteRole(updatedRole.getId());
    }

    @Test(expected = UpdateException.class)
    public void canontUpdateARoleWithANullUpdateDescriptor() throws Exception {
        final String developer = "developer";
        final Role role = identityAPI.createRole(developer);
        try {
            identityAPI.updateRole(role.getId(), null);
        } finally {
            identityAPI.deleteRole(role.getId());
        }
    }

    @Test(expected = RoleNotFoundException.class)
    public void cannotUpdateARoleWithAnUnexistingRoleIdentifier() throws Exception {
        final String manager = "manager";
        final RoleUpdater updateDescriptor = new RoleUpdater();
        updateDescriptor.setName(manager);
        identityAPI.updateRole(83, updateDescriptor);
    }

    @Test
    public void getUsersOfARole() throws BonitaException {
        final String developer = "developer";
        final Role role = identityAPI.createRole(developer);

        final User user1 = identityAPI.createUser("user1", "bpm");
        final User user2 = identityAPI.createUser("user2", "bpm");
        final User user3 = identityAPI.createUser("user3", "bpm");
        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(user1.getId());
        userIds.add(user2.getId());

        final Group group = identityAPI.createGroup("R&D", null);
        identityAPI.addUserMemberships(userIds, group.getId(), role.getId());

        final List<User> users = identityAPI.getUsersInRole(role.getId(), 0, 10, UserCriterion.USER_NAME_ASC);
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals(user1, users.get(0));
        assertEquals(user2, users.get(1));

        identityAPI.deleteUserMemberships(userIds, group.getId(), role.getId());
        identityAPI.deleteRole(role.getId());
        identityAPI.deleteGroup(group.getId());

        identityAPI.deleteUsers(userIds);
        identityAPI.deleteUser(user3.getId());
    }

    @Test
    public void getNumberOfUsersInRole() throws BonitaException {
        final String developer = "developer";
        final Role role = identityAPI.createRole(developer);

        final User user1 = identityAPI.createUser("user1", "bpm");
        final User user2 = identityAPI.createUser("user2", "bpm");
        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(user1.getId());
        userIds.add(user2.getId());

        final Group group = identityAPI.createGroup("R&D", null);
        identityAPI.addUserMemberships(userIds, group.getId(), role.getId());

        final long count = identityAPI.getNumberOfUsersInRole(role.getId());
        assertEquals(2, count);

        identityAPI.deleteUserMemberships(userIds, group.getId(), role.getId());
        identityAPI.deleteRole(role.getId());
        identityAPI.deleteGroup(group.getId());
        identityAPI.deleteUser(user1.getId());
        identityAPI.deleteUser(user2.getId());
    }

    @Test
    public void testGetPaginatedRolesWithRoleCriterion() throws BonitaException {
        final String developer = "developer";
        final String manager = "manager";
        final String cto = "chief technology officer";

        final RoleCreator roleCreator1 = new RoleCreator(developer);
        roleCreator1.setDisplayName(developer);
        final Role aRole = identityAPI.createRole(roleCreator1);
        final RoleCreator roleCreator2 = new RoleCreator(manager);
        roleCreator2.setDisplayName(manager);
        final Role bRole = identityAPI.createRole(roleCreator2);
        final RoleCreator roleCreator3 = new RoleCreator(cto);
        roleCreator3.setDisplayName(cto);
        final Role cRole = identityAPI.createRole(roleCreator3);

        final List<Role> rolesNameAsc = identityAPI.getRoles(0, 3, RoleCriterion.NAME_ASC);
        assertEquals(3, rolesNameAsc.size());
        assertEquals(cto, rolesNameAsc.get(0).getName());
        assertEquals(developer, rolesNameAsc.get(1).getName());
        assertEquals(manager, rolesNameAsc.get(2).getName());

        final List<Role> rolesNameDesc = identityAPI.getRoles(0, 3, RoleCriterion.NAME_DESC);
        assertEquals(3, rolesNameDesc.size());
        assertEquals(manager, rolesNameDesc.get(0).getName());
        assertEquals(developer, rolesNameDesc.get(1).getName());
        assertEquals(cto, rolesNameDesc.get(2).getName());

        final List<Role> rolesLabelAsc = identityAPI.getRoles(0, 3, RoleCriterion.DISPLAY_NAME_ASC);
        assertEquals(3, rolesLabelAsc.size());
        assertEquals(cto, rolesLabelAsc.get(0).getDisplayName());
        assertEquals(developer, rolesLabelAsc.get(1).getDisplayName());
        assertEquals(manager, rolesLabelAsc.get(2).getDisplayName());

        final List<Role> rolesLabelDesc = identityAPI.getRoles(0, 3, RoleCriterion.DISPLAY_NAME_DESC);
        assertEquals(3, rolesLabelDesc.size());
        assertEquals(manager, rolesLabelDesc.get(0).getDisplayName());
        assertEquals(developer, rolesLabelDesc.get(1).getDisplayName());
        assertEquals(cto, rolesLabelDesc.get(2).getDisplayName());

        identityAPI.deleteRole(aRole.getId());
        identityAPI.deleteRole(bRole.getId());
        identityAPI.deleteRole(cRole.getId());
    }

    @Test
    public void getRolesOnMultiPages() throws BonitaException {
        final String roleName1 = "role1";
        final String roleName2 = "role2";
        final String roleName3 = "role3";
        final String roleName4 = "role4";
        final String roleName5 = "role5";
        final Role role1 = identityAPI.createRole(roleName1);
        final Role role2 = identityAPI.createRole(roleName2);
        final Role role3 = identityAPI.createRole(roleName3);
        final Role role4 = identityAPI.createRole(roleName4);
        final Role role5 = identityAPI.createRole(roleName5);

        List<Role> rolesNameAsc = identityAPI.getRoles(0, 2, RoleCriterion.NAME_ASC);
        assertEquals(2, rolesNameAsc.size());
        assertEquals(roleName1, rolesNameAsc.get(0).getName());
        assertEquals(roleName2, rolesNameAsc.get(1).getName());

        rolesNameAsc = identityAPI.getRoles(2, 2, RoleCriterion.NAME_ASC);
        assertEquals(2, rolesNameAsc.size());
        assertEquals(roleName3, rolesNameAsc.get(0).getName());
        assertEquals(roleName4, rolesNameAsc.get(1).getName());

        rolesNameAsc = identityAPI.getRoles(4, 2, RoleCriterion.NAME_ASC);
        assertEquals(1, rolesNameAsc.size());
        assertEquals(roleName5, rolesNameAsc.get(0).getName());
        final List<Role> roles = identityAPI.getRoles(5, 2, RoleCriterion.NAME_ASC);
        assertTrue(roles.isEmpty());

        identityAPI.deleteRole(role1.getId());
        identityAPI.deleteRole(role2.getId());
        identityAPI.deleteRole(role3.getId());
        identityAPI.deleteRole(role4.getId());
        identityAPI.deleteRole(role5.getId());
    }

    @Test(expected = AlreadyExistsException.class)
    public void cannotCreateTwoRoleWithTheSameName() throws BonitaException {
        final String role = "role";
        final APISession session = loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);
        final Role role1 = identityAPI.createRole(role);
        try {
            identityAPI.createRole(role);
        } finally {
            identityAPI.deleteRole(role1.getId());
        }
        logoutTenant(session);
    }

    @Test
    public void searchRoleUsingFilter() throws BonitaException {
        final APISession session = loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final RoleCreator roleCreator1 = new RoleCreator("manager");
        roleCreator1.setDisplayName("Man");
        final Role mananger = identityAPI.createRole(roleCreator1);
        final RoleCreator roleCreator2 = new RoleCreator("developer");
        roleCreator2.setDisplayName("Dev");
        final Role dev = identityAPI.createRole(roleCreator2);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(RoleSearchDescriptor.DISPLAY_NAME, "Dev");
        final SearchResult<Role> searchRoles = identityAPI.searchRoles(builder.done());
        assertNotNull(searchRoles);
        assertEquals(1, searchRoles.getCount());
        final List<Role> roles = searchRoles.getResult();
        assertEquals(dev, roles.get(0));

        identityAPI.deleteRole(mananger.getId());
        identityAPI.deleteRole(dev.getId());

        logoutTenant(session);
    }

    @Cover(classes = { SearchOptionsBuilder.class, IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "SearchRole", "Apostrophe" }, jira = "ENGINE-366")
    @Test
    public void searchRoleWithApostrophe() throws BonitaException {
        final APISession session = loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final RoleCreator roleCreator1 = new RoleCreator("mana'ger");
        roleCreator1.setDisplayName("A");
        final Role mananger = identityAPI.createRole(roleCreator1);
        final RoleCreator roleCreator2 = new RoleCreator("developer");
        roleCreator2.setDisplayName("mana'B");
        final Role dev = identityAPI.createRole(roleCreator2);

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(RoleSearchDescriptor.DISPLAY_NAME, Order.ASC);
        builder.searchTerm("mana'");
        final SearchResult<Role> searchRoles = identityAPI.searchRoles(builder.done());
        assertNotNull(searchRoles);
        assertEquals(2, searchRoles.getCount());
        final List<Role> roles = searchRoles.getResult();
        assertEquals(mananger, roles.get(0));
        assertEquals(dev, roles.get(1));

        identityAPI.deleteRole(mananger.getId());
        identityAPI.deleteRole(dev.getId());

        logoutTenant(session);
    }

    @Test
    public void testGetRolesFromIds() throws BonitaException {
        final APISession session = loginDefaultTenant();
        final IdentityAPI identityAPI = TenantAPIAccessor.getIdentityAPI(session);

        final RoleCreator roleCreator1 = new RoleCreator("managerA");
        roleCreator1.setDisplayName("managerA");
        final Role RoleA = identityAPI.createRole(roleCreator1);
        final RoleCreator roleCreator2 = new RoleCreator("managerB");
        roleCreator2.setDisplayName("managerB");
        final Role RoleB = identityAPI.createRole(roleCreator2);
        final RoleCreator roleCreator3 = new RoleCreator("managerC");
        roleCreator3.setDisplayName("managerC");
        final Role RoleC = identityAPI.createRole(roleCreator3);
        final RoleCreator roleCreator4 = new RoleCreator("managerD");
        roleCreator4.setDisplayName("managerD");
        final Role RoleD = identityAPI.createRole(roleCreator4);

        final long roleAId = RoleA.getId();
        final long roleBId = RoleB.getId();
        final long roleCId = RoleC.getId();
        final long roleDId = RoleD.getId();

        final List<Long> roleIds = new ArrayList<Long>();
        roleIds.add(roleAId);
        roleIds.add(roleBId);
        roleIds.add(roleCId);
        roleIds.add(roleDId);

        final Map<Long, Role> roles = identityAPI.getRoles(roleIds);
        assertNotNull(roles);
        assertEquals(4, roles.size());
        assertEquals(RoleA, roles.get(roleAId));
        assertEquals(RoleB, roles.get(roleBId));
        assertEquals(RoleC, roles.get(roleCId));
        assertEquals(RoleD, roles.get(roleDId));

        identityAPI.deleteRoles(roleIds);

        logoutTenant(session);
    }

    @Test
    public void checkCreatedByForRole() throws BonitaException {
        final String manager = "manager";
        final Role createdRole = identityAPI.createRole(manager);
        // check createdBy for role
        assertNotNull(createdRole.getCreatedBy());
        assertEquals(session.getUserId(), createdRole.getCreatedBy());
        identityAPI.deleteRole(createdRole.getId());
    }

}
