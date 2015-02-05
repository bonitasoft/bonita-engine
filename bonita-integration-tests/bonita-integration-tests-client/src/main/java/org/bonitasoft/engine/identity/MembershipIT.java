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

import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.CreationException;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bole Zhang
 * @author Baptiste Mesta
 */
public class MembershipIT extends TestWithTechnicalUser {

    private static User user1;

    private static User user2;

    private static User user3;

    private static User user4;

    private static Role role1;

    private static Role role2;

    private static Role role3;

    private static Role role4;

    private static Group group1;

    private static Group group2;

    private static Group group3;

    private static Group group4;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        createTestedUserMemberships();
    }

    @Override
    @After
    public void after() throws Exception {
        deleteTestedUserMemberships();
        super.after();
    }

    @Test
    public void getUserMembershipsWithPageOutOfRangeException() throws BonitaException {
        final User u = getIdentityAPI().createUser("u", "engine");
        final List<UserMembership> userMemberships = getIdentityAPI().getUserMemberships(u.getId(), 0, 3, UserMembershipCriterion.ROLE_NAME_ASC);
        assertTrue(userMemberships.isEmpty());
        getIdentityAPI().deleteUser(u.getId());
    }

    @Test(expected = CreationException.class)
    public void createUserMembershipWithoutUser() throws BonitaException {
        final RoleCreator roleCreator = new RoleCreator("roleM");
        final Role roleM = getIdentityAPI().createRole(roleCreator);
        final Group groupM = getIdentityAPI().createGroup("groupM", null);
        try {
            getIdentityAPI().addUserMembership(-2, groupM.getId(), roleM.getId());
        } finally {
            getIdentityAPI().deleteRole(roleM.getId());
            getIdentityAPI().deleteGroup(groupM.getId());
        }
    }

    @Test(expected = CreationException.class)
    public void createUserMembershipWithoutRole() throws BonitaException {
        final User userM = getIdentityAPI().createUser("userM", "engine");
        final Group groupM = getIdentityAPI().createGroup("groupM", null);
        try {
            getIdentityAPI().addUserMembership(userM.getId(), groupM.getId(), -4);
        } finally {
            getIdentityAPI().deleteUser(userM.getId());
            getIdentityAPI().deleteGroup(groupM.getId());
        }
    }

    @Test(expected = CreationException.class)
    public void createUserMembershipWithoutGroup() throws BonitaException {
        final User userM = getIdentityAPI().createUser("userM", "engine");
        final RoleCreator roleCreator = new RoleCreator("roleM");
        final Role roleM = getIdentityAPI().createRole(roleCreator);
        try {
            getIdentityAPI().addUserMembership(userM.getId(), -83, roleM.getId());
        } finally {
            getIdentityAPI().deleteUser(userM.getId());
            getIdentityAPI().deleteRole(roleM.getId());
        }
    }

    @Test
    public void addUserMembership() throws BonitaException {
        final User userM = getIdentityAPI().createUser("userM", "engine");
        final Role roleM = getIdentityAPI().createRole("roleM");
        final Group groupM = getIdentityAPI().createGroup("groupM", null);
        final UserMembership userMembership = getIdentityAPI().addUserMembership(userM.getId(), groupM.getId(), roleM.getId());
        try {
            assertNotNull(userMembership);
            assertEquals(getSession().getUserId(), userMembership.getAssignedBy());
            assertEquals(userM.getId(), userMembership.getUserId());
            assertEquals(roleM.getId(), userMembership.getRoleId());
            assertEquals(groupM.getId(), userMembership.getGroupId());
            // check assignedBy for membership
            assertEquals(getSession().getUserId(), userMembership.getAssignedBy());

        } finally {
            getIdentityAPI().deleteUserMembership(userM.getId(), groupM.getId(), roleM.getId());
            getIdentityAPI().deleteUser(userM.getId());
            getIdentityAPI().deleteRole(roleM.getId());
            getIdentityAPI().deleteGroup(groupM.getId());
        }
    }

    @Cover(classes = { IdentityAPI.class, UserMembership.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "Create", "User membership", "Twice" }, jira = "ENGINE-920")
    @Test(expected = AlreadyExistsException.class)
    public void addTwiceSameUserMembership() throws BonitaException {
        final User userM = getIdentityAPI().createUser("userM", "engine");
        final Role roleM = getIdentityAPI().createRole("roleM");
        final Group groupM = getIdentityAPI().createGroup("groupM", null);

        // Add first time the userMembership
        getIdentityAPI().addUserMembership(userM.getId(), groupM.getId(), roleM.getId());
        try {
            // Add twice the same userMembership
            getIdentityAPI().addUserMembership(userM.getId(), groupM.getId(), roleM.getId());
        } finally {
            getIdentityAPI().deleteUserMembership(userM.getId(), groupM.getId(), roleM.getId());
            getIdentityAPI().deleteUser(userM.getId());
            getIdentityAPI().deleteRole(roleM.getId());
            getIdentityAPI().deleteGroup(groupM.getId());
        }
    }

    @Test
    public void getRoleIdAndGroupIdFromMembership() throws BonitaException {
        final User userM = getIdentityAPI().createUser("userM", "engine");
        final Role roleM = getIdentityAPI().createRole("roleM");
        final Group groupM = getIdentityAPI().createGroup("groupM", null);
        final UserMembership userMembership = getIdentityAPI().addUserMembership(userM.getId(), groupM.getId(), roleM.getId());

        final Group group = getIdentityAPI().getGroup(userMembership.getGroupId());
        final Role role = getIdentityAPI().getRole(userMembership.getRoleId());
        try {
            assertEquals("groupM", group.getName());
            assertEquals("roleM", role.getName());
        } finally {
            getIdentityAPI().deleteUserMembership(userM.getId(), groupM.getId(), roleM.getId());
            getIdentityAPI().deleteUser(userM.getId());
            getIdentityAPI().deleteRole(roleM.getId());
            getIdentityAPI().deleteGroup(groupM.getId());
        }
    }

    @Test
    public void getMembershipByMembershipId() throws BonitaException {
        final User userM = getIdentityAPI().createUser("userM", "engine");
        final Role roleM = getIdentityAPI().createRole("roleM");
        final Group groupM = getIdentityAPI().createGroup("groupM", null);
        final UserMembership userMembership = getIdentityAPI().addUserMembership(userM.getId(), groupM.getId(), roleM.getId());
        try {
            final UserMembership membership2 = getIdentityAPI().getUserMembership(userMembership.getId());
            assertNotNull(membership2);
            assertEquals(userM.getId(), membership2.getUserId());
            assertEquals(roleM.getId(), membership2.getRoleId());
            assertEquals(groupM.getId(), membership2.getGroupId());
            assertEquals(userM.getUserName(), membership2.getUsername());
            assertEquals(roleM.getName(), membership2.getRoleName());
            assertEquals(groupM.getName(), membership2.getGroupName());
        } finally {
            getIdentityAPI().deleteUserMembership(userM.getId(), groupM.getId(), roleM.getId());
            getIdentityAPI().deleteUser(userM.getId());
            getIdentityAPI().deleteRole(roleM.getId());
            getIdentityAPI().deleteGroup(groupM.getId());
        }
    }

    @Test
    public void getAssignedByOnUserMembership() throws BonitaException, InterruptedException {
        final User userM = createUser("aTest", "engine");
        final User plop = createUser("plop", "bpm");
        final Role roleM = getIdentityAPI().createRole("roleM");
        final Group groupM = getIdentityAPI().createGroup("groupM", null);
        logoutOnTenant();
        loginOnDefaultTenantWith(plop.getUserName(), "bpm");
        final Date beforeDate = new Date();
        Thread.sleep(10);
        final UserMembership userMembership = getIdentityAPI().addUserMembership(userM.getId(), groupM.getId(), roleM.getId());
        Thread.sleep(10);
        final Date afterDate = new Date();
        try {
            final UserMembership membership2 = getIdentityAPI().getUserMembership(userMembership.getId());
            assertNotNull(membership2);
            assertEquals(plop.getId(), membership2.getAssignedBy());
            assertTrue("exprected date between <" + beforeDate + "> and <" + afterDate + "> but was <" + membership2.getAssignedDate() + ">", membership2
                    .getAssignedDate().compareTo(beforeDate) > 0 && membership2.getAssignedDate().compareTo(afterDate) < 0);
            assertEquals(userM.getId(), membership2.getUserId());
            assertEquals(roleM.getId(), membership2.getRoleId());
            assertEquals(groupM.getId(), membership2.getGroupId());
        } finally {
            getIdentityAPI().deleteUserMembership(userM.getId(), groupM.getId(), roleM.getId());
            getIdentityAPI().deleteUser(userM.getId());
            getIdentityAPI().deleteUser(plop.getId());
            getIdentityAPI().deleteRole(roleM.getId());
            getIdentityAPI().deleteGroup(groupM.getId());
        }
    }

    @Test
    public void getNumberOfUserMembershipsByUserId() {
        assertEquals(4, getIdentityAPI().getNumberOfUserMemberships(user1.getId()));
        assertEquals(1, getIdentityAPI().getNumberOfUserMemberships(user4.getId()));
    }

    @Test
    public void updateUserMemberships() throws BonitaException {
        final User userM = getIdentityAPI().createUser("userM", "engine");
        final Role roleM = getIdentityAPI().createRole("roleM");
        final Group groupM = getIdentityAPI().createGroup("groupM", null);
        final UserMembership userMembership = getIdentityAPI().addUserMembership(userM.getId(), groupM.getId(), roleM.getId());
        assertEquals(groupM.getId(), userMembership.getGroupId());
        assertEquals(roleM.getId(), userMembership.getRoleId());

        final UserMembership newMembership = getIdentityAPI().updateUserMembership(userMembership.getId(), group4.getId(), role3.getId());
        assertEquals(group4.getId(), newMembership.getGroupId());
        assertEquals(role3.getId(), newMembership.getRoleId());
        getIdentityAPI().deleteUserMembership(newMembership.getId());
        getIdentityAPI().deleteRole(roleM.getId());
        getIdentityAPI().deleteGroup(groupM.getId());
        getIdentityAPI().deleteUser(userM.getId());
    }

    @Test
    public void getUserMembershipsByRole() {
        List<UserMembership> userMemberships = getIdentityAPI().getUserMembershipsByRole(role1.getId(), 0, 500);
        assertEquals(2, userMemberships.size());

        userMemberships = getIdentityAPI().getUserMembershipsByRole(role1.getId(), 0, 1);
        assertEquals(1, userMemberships.size());

        userMemberships = getIdentityAPI().getUserMembershipsByRole(role1.getId(), 1, 1);
        assertEquals(1, userMemberships.size());

        userMemberships = getIdentityAPI().getUserMembershipsByRole(role1.getId(), 20, 500);
        assertEquals(0, userMemberships.size());
    }

    @Test
    public void getUserMembershipsByGroup() {
        List<UserMembership> userMemberships = getIdentityAPI().getUserMembershipsByGroup(group1.getId(), 0, 500);
        assertEquals(1, userMemberships.size());
        userMemberships = getIdentityAPI().getUserMembershipsByGroup(group3.getId(), 0, 500);
        assertEquals(3, userMemberships.size());
        userMemberships = getIdentityAPI().getUserMembershipsByGroup(group3.getId(), 0, 2);
        assertEquals(2, userMemberships.size());
        userMemberships = getIdentityAPI().getUserMembershipsByGroup(group3.getId(), 1, 2);
        assertEquals(2, userMemberships.size());
        userMemberships = getIdentityAPI().getUserMembershipsByGroup(group3.getId(), 20, 2);
        assertEquals(0, userMemberships.size());
    }

    @Test
    public void getUserMembershipsOrderByRoleNameAsc() {
        final List<UserMembership> userMemberships = getIdentityAPI().getUserMemberships(user1.getId(), 0, 3, UserMembershipCriterion.ROLE_NAME_ASC);
        assertEquals(3, userMemberships.size());
        assertEquals(role1.getName(), userMemberships.get(0).getRoleName());
        assertEquals(role2.getName(), userMemberships.get(1).getRoleName());
        assertEquals(role3.getName(), userMemberships.get(2).getRoleName());
    }

    @Test
    public void getUserMembershipsOrderByRoleNameDesc() {
        final List<UserMembership> userMemberships = getIdentityAPI().getUserMemberships(user1.getId(), 0, 3, UserMembershipCriterion.ROLE_NAME_DESC);
        assertEquals(3, userMemberships.size());
        assertEquals(role4.getName(), userMemberships.get(0).getRoleName());
        assertEquals(role3.getName(), userMemberships.get(1).getRoleName());
        assertEquals(role2.getName(), userMemberships.get(2).getRoleName());
    }

    @Test
    public void getUserMembershipsByGroupNameAsc() {
        final List<UserMembership> userMemberships = getIdentityAPI().getUserMemberships(user1.getId(), 0, 3, UserMembershipCriterion.GROUP_NAME_ASC);
        assertEquals(3, userMemberships.size());
        assertEquals(group1.getName(), userMemberships.get(0).getGroupName());
        assertEquals(group2.getName(), userMemberships.get(1).getGroupName());
        assertEquals(group3.getName(), userMemberships.get(2).getGroupName());
    }

    @Test
    public void getUserMembershipsByGroupNameDesc() {
        final List<UserMembership> userMemberships = getIdentityAPI().getUserMemberships(user1.getId(), 0, 3, UserMembershipCriterion.GROUP_NAME_DESC);
        assertEquals(3, userMemberships.size());
        assertEquals(group4.getName(), userMemberships.get(0).getGroupName());
        assertEquals(group3.getName(), userMemberships.get(1).getGroupName());
        assertEquals(group2.getName(), userMemberships.get(2).getGroupName());
    }

    @Test
    public void getAssignedBy() {
        final List<UserMembership> userMemberships = getIdentityAPI().getUserMemberships(user1.getId(), 0, 3, UserMembershipCriterion.ASSIGNED_DATE_DESC);
        assertEquals(user1.getId(), userMemberships.get(0).getAssignedBy());
    }

    @Test
    public void getUserMembershipsByAssignedDateAsc() {
        final List<UserMembership> userMemberships = getIdentityAPI().getUserMemberships(user1.getId(), 0, 3, UserMembershipCriterion.ASSIGNED_DATE_ASC);
        assertEquals(3, userMemberships.size());
        assertEquals(role1.getName(), userMemberships.get(0).getRoleName());
        assertEquals(role2.getName(), userMemberships.get(1).getRoleName());
        assertEquals(role3.getName(), userMemberships.get(2).getRoleName());
    }

    @Test
    public void getUserMembershipsByAssignedDateDesc() {
        final List<UserMembership> userMemberships = getIdentityAPI().getUserMemberships(user1.getId(), 0, 3, UserMembershipCriterion.ASSIGNED_DATE_DESC);
        assertEquals(3, userMemberships.size());
        assertEquals(role4.getName(), userMemberships.get(0).getRoleName());
        assertEquals(role3.getName(), userMemberships.get(1).getRoleName());
        assertEquals(role2.getName(), userMemberships.get(2).getRoleName());
    }

    @Test
    public void deleteUserMemberships() throws BonitaException {
        final User userM = getIdentityAPI().createUser("userM", "engine");
        final Role roleM = getIdentityAPI().createRole("roleM");
        final Group groupM = getIdentityAPI().createGroup("groupM", null);
        getIdentityAPI().addUserMembership(userM.getId(), groupM.getId(), roleM.getId());

        assertEquals(1, getIdentityAPI().getNumberOfUserMemberships(userM.getId()));
        getIdentityAPI().deleteUserMembership(userM.getId(), groupM.getId(), roleM.getId());
        assertEquals(0, getIdentityAPI().getNumberOfUserMemberships(userM.getId()));
        getIdentityAPI().deleteUser(userM.getId());
        getIdentityAPI().deleteRole(roleM.getId());
        getIdentityAPI().deleteGroup(groupM.getId());
    }

    @Test
    public void deleteUserMembership() throws BonitaException {
        final User userT = getIdentityAPI().createUser("userT", "engine");
        final Role roleT = getIdentityAPI().createRole("roleT");
        final Group groupT = getIdentityAPI().createGroup("groupT", null);
        final UserMembership membership1 = getIdentityAPI().addUserMembership(userT.getId(), groupT.getId(), roleT.getId());
        assertEquals(1, getIdentityAPI().getNumberOfUserMemberships(userT.getId()));

        final UserMembership membership2 = getIdentityAPI().getUserMembership(membership1.getId());
        getIdentityAPI().deleteUserMembership(membership2.getId());
        assertEquals(0, getIdentityAPI().getNumberOfUserMemberships(userT.getId()));
        getIdentityAPI().deleteUser(userT.getId());
        getIdentityAPI().deleteRole(roleT.getId());
        getIdentityAPI().deleteGroup(groupT.getId());
    }

    @Test
    public void deleteUserMembershipsByDeleteRole() throws BonitaException {
        final User userM = getIdentityAPI().createUser("userR", "engine");
        final Role roleM = getIdentityAPI().createRole("roleR");
        final Group groupM1 = getIdentityAPI().createGroup("groupR1", null);
        final Group groupM2 = getIdentityAPI().createGroup("groupR2", null);
        getIdentityAPI().addUserMembership(userM.getId(), groupM1.getId(), roleM.getId());
        getIdentityAPI().addUserMembership(userM.getId(), groupM2.getId(), roleM.getId());

        assertEquals(2, getIdentityAPI().getNumberOfUserMemberships(userM.getId()));
        getIdentityAPI().deleteRole(roleM.getId());
        assertEquals(0, getIdentityAPI().getNumberOfUserMemberships(userM.getId()));

        getIdentityAPI().deleteUser(userM.getId());
        getIdentityAPI().deleteGroup(groupM1.getId());
        getIdentityAPI().deleteGroup(groupM2.getId());
    }

    @Test
    public void deleteUserMembershipsByDeleteGroup() throws BonitaException {
        final User userM = getIdentityAPI().createUser("userR", "engine");
        final Role roleM1 = getIdentityAPI().createRole("roleR1");
        final Role roleM2 = getIdentityAPI().createRole("roleR2");
        final Group groupM = getIdentityAPI().createGroup("groupR", null);
        getIdentityAPI().addUserMembership(userM.getId(), groupM.getId(), roleM1.getId());
        getIdentityAPI().addUserMembership(userM.getId(), groupM.getId(), roleM2.getId());

        assertEquals(2, getIdentityAPI().getNumberOfUserMemberships(userM.getId()));
        getIdentityAPI().deleteGroup(groupM.getId());
        assertEquals(0, getIdentityAPI().getNumberOfUserMemberships(userM.getId()));

        getIdentityAPI().deleteUser(userM.getId());
        getIdentityAPI().deleteRole(roleM1.getId());
        getIdentityAPI().deleteRole(roleM2.getId());
    }

    private void createTestedUserMemberships() throws BonitaException, InterruptedException {
        user1 = createUser("userM1", "engine1");
        logoutOnTenant();
        loginOnDefaultTenantWith(user1.getUserName(), "engine1");
        user2 = createUser("userM2", "engine2");
        user3 = createUser("userM3", "engine3");
        user4 = createUser("userM4", "engine4");
        final RoleCreator roleCreator1 = new RoleCreator("roleM1");
        roleCreator1.setDisplayName("roleLabel").setDescription("create role for userMembership");
        role1 = getIdentityAPI().createRole(roleCreator1);
        final RoleCreator roleCreator2 = new RoleCreator("roleM2");
        roleCreator2.setDisplayName("roleLabel").setDescription("create role for userMembership");
        role2 = getIdentityAPI().createRole(roleCreator2);
        final RoleCreator roleCreator3 = new RoleCreator("roleM3");
        roleCreator3.setDisplayName("roleLabel").setDescription("create role for userMembership");
        role3 = getIdentityAPI().createRole(roleCreator3);
        final RoleCreator roleCreator4 = new RoleCreator("roleM4");
        roleCreator4.setDisplayName("roleLabel").setDescription("create role for userMembership");
        role4 = getIdentityAPI().createRole(roleCreator4);
        group1 = createGroup("groupM1", "grouplabel", "create group for userMembership");
        group2 = createGroup("groupM2", "grouplabel", "create group for userMembership");
        group3 = createGroup("groupM3", "grouplabel", "create group for userMembership");
        group4 = createGroup("groupM4", "grouplabel", "create group for userMembership");
        getIdentityAPI().addUserMembership(user1.getId(), group1.getId(), role1.getId());
        Thread.sleep(10);
        getIdentityAPI().addUserMembership(user1.getId(), group2.getId(), role2.getId());
        Thread.sleep(10);
        getIdentityAPI().addUserMembership(user1.getId(), group3.getId(), role3.getId());
        Thread.sleep(10);
        getIdentityAPI().addUserMembership(user1.getId(), group4.getId(), role4.getId());
        Thread.sleep(10);
        getIdentityAPI().addUserMembership(user2.getId(), group3.getId(), role4.getId());
        getIdentityAPI().addUserMembership(user3.getId(), group4.getId(), role2.getId());
        getIdentityAPI().addUserMembership(user4.getId(), group3.getId(), role1.getId());
    }

    private void deleteTestedUserMemberships() throws BonitaException {
        getIdentityAPI().deleteUserMembership(user1.getId(), group1.getId(), role1.getId());
        getIdentityAPI().deleteUserMembership(user1.getId(), group2.getId(), role2.getId());
        getIdentityAPI().deleteUserMembership(user1.getId(), group3.getId(), role3.getId());
        getIdentityAPI().deleteUserMembership(user1.getId(), group4.getId(), role4.getId());
        getIdentityAPI().deleteUserMembership(user2.getId(), group3.getId(), role4.getId());
        getIdentityAPI().deleteUserMembership(user3.getId(), group4.getId(), role2.getId());
        getIdentityAPI().deleteUserMembership(user4.getId(), group3.getId(), role1.getId());
        getIdentityAPI().deleteUser(user1.getId());
        getIdentityAPI().deleteUser(user2.getId());
        getIdentityAPI().deleteUser(user3.getId());
        getIdentityAPI().deleteUser(user4.getId());
        getIdentityAPI().deleteRole(role1.getId());
        getIdentityAPI().deleteRole(role2.getId());
        getIdentityAPI().deleteRole(role3.getId());
        getIdentityAPI().deleteRole(role4.getId());
        getIdentityAPI().deleteGroup(group1.getId());
        getIdentityAPI().deleteGroup(group2.getId());
        getIdentityAPI().deleteGroup(group3.getId());
        getIdentityAPI().deleteGroup(group4.getId());
    }

}
