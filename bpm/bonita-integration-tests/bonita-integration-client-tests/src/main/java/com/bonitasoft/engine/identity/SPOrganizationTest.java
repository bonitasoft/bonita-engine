/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.identity;

import java.util.List;

import org.bonitasoft.engine.identity.Group;
import org.bonitasoft.engine.identity.GroupCreator;
import org.bonitasoft.engine.identity.GroupCriterion;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.identity.RoleCreator;
import org.bonitasoft.engine.identity.RoleCriterion;
import org.bonitasoft.engine.identity.User;
import org.bonitasoft.engine.identity.UserCreator;
import org.bonitasoft.engine.identity.UserCriterion;
import org.bonitasoft.engine.identity.UserMembership;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.CommonAPISPTest;
import com.bonitasoft.engine.api.IdentityAPI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Celine Souchet
 * 
 */
public class SPOrganizationTest extends CommonAPISPTest {

    @After
    public void afterTest() throws Exception {
        logout();
    }

    @Before
    public void beforeTest() throws Exception {
        login();
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

    @Test
    public void exportAndImportOrganization() throws Exception {
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
