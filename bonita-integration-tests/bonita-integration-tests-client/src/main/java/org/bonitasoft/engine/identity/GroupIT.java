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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.api.IdentityAPI;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.engine.exception.BonitaException;
import org.bonitasoft.engine.exception.DeletionException;
import org.bonitasoft.engine.search.Order;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.engine.search.SearchResult;
import org.bonitasoft.engine.test.annotation.Cover;
import org.bonitasoft.engine.test.annotation.Cover.BPMNConcept;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GroupIT extends TestWithTechnicalUser {

    private Group defaultGroup;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        final GroupCreator groupCreator = new GroupCreator("test");
        groupCreator.setDescription("description").setDisplayName("label");
        defaultGroup = getIdentityAPI().createGroup(groupCreator);
    }

    @Override
    @After
    public void after() throws Exception {
        getIdentityAPI().deleteGroup(defaultGroup.getId());
        defaultGroup = null;
        super.after();
    }

    @Test
    public void getGroup() throws BonitaException {
        final Group group = getIdentityAPI().getGroup(defaultGroup.getId());
        assertNotNull(group);
        assertEquals("test", group.getName());
        assertEquals("label", group.getDisplayName());
        assertEquals("description", group.getDescription());
    }

    @Test(expected = GroupNotFoundException.class)
    public void getGroupByGroupNotFound() throws BonitaException {
        getIdentityAPI().getGroup(0);
    }

    @Test
    public void getNumberOfGroups() throws BonitaException {
        assertEquals(1, getIdentityAPI().getNumberOfGroups());
        final Group newGroup = getIdentityAPI().createGroup("NewGroup", null);
        assertEquals(2, getIdentityAPI().getNumberOfGroups());
        getIdentityAPI().deleteGroup(newGroup.getId());
    }

    @Test(expected = AlreadyExistsException.class)
    public void createGroupBygroupWithGroupAlreadyExistException() throws BonitaException {
        Group group = getIdentityAPI().createGroup("NewGroup", null);
        try {
            group = getIdentityAPI().createGroup("NewGroup", null);
        } finally {
            getIdentityAPI().deleteGroup(group.getId());
        }
    }

    @Test
    public void getGroupByGroupName() throws BonitaException {
        final String groupName = "group111";
        final Group groupM = getIdentityAPI().createGroup(groupName, null);
        final Group group = getIdentityAPI().getGroupByPath(groupName);
        assertNotNull(group);
        assertEquals(groupName, group.getName());
        assertEquals(groupM.getId(), group.getId());
        getIdentityAPI().deleteGroup(group.getId());
    }

    @Test
    public void getGroups() throws BonitaException {
        final Group groupA = createGroup("testA", "labelA", "descrtptionA");
        final Group groupB = createGroup("testB", "labelB", "descrtptionB");
        final List<Group> listGroups = getIdentityAPI().getGroups(0, 5000, GroupCriterion.NAME_ASC);

        assertNotNull(listGroups);
        assertEquals(3, listGroups.size());
        assertEquals("testA", listGroups.get(1).getName());
        assertEquals("labelA", listGroups.get(1).getDisplayName());
        assertEquals("descrtptionA", listGroups.get(1).getDescription());
        assertEquals("testB", listGroups.get(2).getName());
        assertEquals("labelB", listGroups.get(2).getDisplayName());
        assertEquals("descrtptionB", listGroups.get(2).getDescription());
        getIdentityAPI().deleteGroup(groupA.getId());
        getIdentityAPI().deleteGroup(groupB.getId());
    }

    @Test
    public void getGroupsByIDs() throws BonitaException {
        final String group1 = "Group1";
        final Group groupCreated1 = getIdentityAPI().createGroup(group1, null);
        final String group2 = "Group2";
        final Group groupCreated2 = getIdentityAPI().createGroup(group2, null);

        final List<Long> groupIds = new ArrayList<Long>();
        groupIds.add(groupCreated1.getId());
        groupIds.add(groupCreated2.getId());

        final Map<Long, Group> groups = getIdentityAPI().getGroups(groupIds);
        assertNotNull(groups);
        assertEquals(2, groups.size());
        assertEquals(group1, groups.get(groupCreated1.getId()).getName());
        assertEquals(group2, groups.get(groupCreated2.getId()).getName());

        getIdentityAPI().deleteGroup(groups.get(groupCreated1.getId()).getId());
        getIdentityAPI().deleteGroup(groups.get(groupCreated2.getId()).getId());
    }

    public void getGroupsByIDsWithoutGroupNotFoundException() throws BonitaException {
        final String group1 = "Group1";
        final Group groupCreated1 = getIdentityAPI().createGroup(group1, null);
        final String group2 = "Group2";
        final Group groupCreated2 = getIdentityAPI().createGroup(group2, null);

        final List<Long> groupIds = new ArrayList<Long>();
        groupIds.add(groupCreated1.getId());
        groupIds.add(groupCreated2.getId() + 100);

        final Map<Long, Group> groups = getIdentityAPI().getGroups(groupIds);
        assertNotNull(groups);
        assertEquals(1, groups.size());
        assertEquals(group1, groups.get(0).getName());

        getIdentityAPI().deleteGroup(groupCreated1.getId());
        getIdentityAPI().deleteGroup(groupCreated2.getId());
    }

    @Test(expected = AlreadyExistsException.class)
    public void createGroupExistException() throws BonitaException {
        getIdentityAPI().createGroup("test", null);
    }

    @Test(expected = AlreadyExistsException.class)
    public void createSubGroupExistException() throws BonitaException {
        final Group group = getIdentityAPI().createGroup("r&d", "bonita");
        try {
            getIdentityAPI().createGroup("r&d", "bonita");
        } finally {
            deleteGroups(group);
        }
    }

    @Test
    public void deleteGroup() throws BonitaException {
        final long numberOfGroups = getIdentityAPI().getNumberOfGroups();
        final Group group = getIdentityAPI().createGroup("groupName", null);
        assertEquals(numberOfGroups + 1, getIdentityAPI().getNumberOfGroups());

        getIdentityAPI().deleteGroup(group.getId());
        assertEquals(numberOfGroups, getIdentityAPI().getNumberOfGroups());
    }

    @Test
    public void deleteGroupDeleteChildGroups() throws BonitaException {
        final long numberOfGroups = getIdentityAPI().getNumberOfGroups();
        final Group parentGroup = getIdentityAPI().createGroup("parentGroup", null);
        final Group notParentGroup = getIdentityAPI().createGroup("notParentGroup", null);
        final Group subGroup = getIdentityAPI().createGroup("subGroup", parentGroup.getPath());
        assertEquals(numberOfGroups + 3, getIdentityAPI().getNumberOfGroups());

        getIdentityAPI().deleteGroup(parentGroup.getId());
        try {
            getIdentityAPI().getGroup(subGroup.getId());
            fail("child group should not exists anymore");
        } catch (final GroupNotFoundException e) {
            // ok
        }
        getIdentityAPI().getGroup(notParentGroup.getId());
        assertEquals(numberOfGroups + 1, getIdentityAPI().getNumberOfGroups());
        getIdentityAPI().deleteGroup(notParentGroup.getId());

    }

    @Test
    public void deleteGroupDeleteChildGroupsRecursivly() throws BonitaException {
        final long numberOfGroups = getIdentityAPI().getNumberOfGroups();
        final Group parentGroup = getIdentityAPI().createGroup("parentGroup", null);
        for (int i = 0; i < 25; i++) {
            final Group sub = getIdentityAPI().createGroup("subGroup" + i, parentGroup.getPath());
            for (int j = 0; j < 25; j++) {
                getIdentityAPI().createGroup("subSubGroup" + j, sub.getPath());
            }
        }
        assertEquals(numberOfGroups + 1 + 25 + 25 * 25, getIdentityAPI().getNumberOfGroups());
        getIdentityAPI().deleteGroup(parentGroup.getId());
        assertEquals(numberOfGroups, getIdentityAPI().getNumberOfGroups());
    }

    @Test
    public void deleteGroupsChildrenAndParent() throws BonitaException {
        final long numberOfGroups = getIdentityAPI().getNumberOfGroups();
        final Group parentGroup = getIdentityAPI().createGroup("parentGroup", null);
        final Group sub0 = getIdentityAPI().createGroup("subGroup0", parentGroup.getPath());
        final Group sub01 = getIdentityAPI().createGroup("subSubGroup0", sub0.getPath());
        final Group sub1 = getIdentityAPI().createGroup("subGroup1", parentGroup.getPath());
        final Group sub11 = getIdentityAPI().createGroup("subSubGroup1", sub1.getPath());
        assertEquals(numberOfGroups + 5, getIdentityAPI().getNumberOfGroups());

        getIdentityAPI().deleteGroups(Arrays.asList(sub01.getId(), parentGroup.getId(), sub11.getId()));
        assertEquals(numberOfGroups, getIdentityAPI().getNumberOfGroups());
    }

    @Test(expected = DeletionException.class)
    public void deleteGroupNotFoundException() throws BonitaException {
        getIdentityAPI().deleteGroup(0);
    }

    @Test
    public void deleteGroups() throws BonitaException {
        assertNotNull(getIdentityAPI().getNumberOfGroups());
        assertEquals(1, getIdentityAPI().getNumberOfGroups());
        final List<Long> groupIdList = new ArrayList<Long>();

        final Group group1 = getIdentityAPI().createGroup("testName1", null);
        groupIdList.add(group1.getId());
        assertEquals(2, getIdentityAPI().getNumberOfGroups());

        final Group group2 = getIdentityAPI().createGroup("testName2", null);
        groupIdList.add(group2.getId());
        assertEquals(3, getIdentityAPI().getNumberOfGroups());

        assertEquals(2, groupIdList.size());
        getIdentityAPI().deleteGroups(groupIdList);
        assertEquals(1, getIdentityAPI().getNumberOfGroups());
    }

    @Test(expected = DeletionException.class)
    public void deleteGroupsWithNotExistId() throws BonitaException {
        assertNotNull(getIdentityAPI().getNumberOfGroups());
        assertEquals(1, getIdentityAPI().getNumberOfGroups());
        final List<Long> groupIdList = new ArrayList<Long>();

        final Group group1 = getIdentityAPI().createGroup("testName1", null);
        groupIdList.add(group1.getId());
        assertEquals(2, getIdentityAPI().getNumberOfGroups());

        groupIdList.add((long) 0);
        assertEquals(2, groupIdList.size());

        getIdentityAPI().deleteGroup(group1.getId());
        getIdentityAPI().deleteGroups(groupIdList);
    }

    @Test
    public void updateGroup() throws BonitaException {
        final Group group1 = getIdentityAPI().getGroup(defaultGroup.getId());
        assertEquals("test", group1.getName());

        final GroupUpdater updateDescriptor = new GroupUpdater();
        updateDescriptor.updateName("newtest");
        updateDescriptor.updateDisplayName("newlabel");
        updateDescriptor.updateDescription("newdescription");

        getIdentityAPI().updateGroup(group1.getId(), updateDescriptor);
        final Group group2 = getIdentityAPI().getGroup(group1.getId());
        assertNotNull(group2);
        assertEquals("newtest", group2.getName());
        assertEquals("newlabel", group2.getDisplayName());
        assertEquals("newdescription", group2.getDescription());
    }

    @Test
    public void updateParentGroupPath() throws BonitaException {
        final Group newRootGroup = createGroup("BonitaSoft", "BonitaSoft", "BonitaSoft company");

        final String groupL2Name = "France";
        Group groupL2 = getIdentityAPI().createGroup(groupL2Name, defaultGroup.getPath());
        assertEquals(defaultGroup.getPath(), groupL2.getParentPath());

        final String groupL3Name = "Grenoble";
        Group groupL3 = getIdentityAPI().createGroup(groupL3Name, groupL2.getPath());
        assertEquals(groupL2.getPath(), groupL3.getParentPath());

        final GroupUpdater updateDescriptor = new GroupUpdater();
        updateDescriptor.updateParentPath(newRootGroup.getPath());

        // update parent path
        getIdentityAPI().updateGroup(groupL2.getId(), updateDescriptor);
        groupL2 = getIdentityAPI().getGroup(groupL2.getId());
        assertEquals(newRootGroup.getPath(), groupL2.getParentPath());

        // assert children are also updated
        groupL3 = getIdentityAPI().getGroup(groupL3.getId());
        assertEquals(groupL2.getPath(), groupL3.getParentPath());

        getIdentityAPI().deleteGroup(groupL3.getId());
        getIdentityAPI().deleteGroup(groupL2.getId());
        getIdentityAPI().deleteGroup(newRootGroup.getId());
    }

    @Cover(classes = { IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, jira = "BS-7115", keywords = { "update", "group", "parent path", "empty",
            "null" })
    @Test
    public void when_update_group_with_empty_parent_path_it_is_set_to_null() throws BonitaException {
        final String parentGroupPath = "/parentPath";
        final Group group = createGroup("BonitaSoft", parentGroupPath);
        Group result = getIdentityAPI().getGroup(group.getId());
        assertEquals("The parent path must be equals to " + parentGroupPath + ".", parentGroupPath, result.getParentPath());

        // update parent path
        final GroupUpdater updateDescriptor = new GroupUpdater();
        updateDescriptor.updateParentPath("");
        getIdentityAPI().updateGroup(group.getId(), updateDescriptor);
        result = getIdentityAPI().getGroup(group.getId());
        assertNull("The parent path must be null.", result.getParentPath());

        getIdentityAPI().deleteGroup(group.getId());
    }

    @Cover(classes = { IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, jira = "BS-7115", keywords = { "create", "group", "parent path", "empty",
            "null" })
    @Test
    public void when_create_group_with_empty_parent_path_it_is_set_to_null() throws BonitaException {
        final Group group = createGroup("BonitaSoft", "");
        final Group result = getIdentityAPI().getGroup(group.getId());
        assertNull("The parent path must be null.", result.getParentPath());

        getIdentityAPI().deleteGroup(group.getId());
    }

    @Test
    public void updateGroupNameAlsoUpdateChildren() throws BonitaException {
        final Group newRootGroup = createGroup("BonitaSoft", "BonitaSoft", "BonitaSoft company");

        final String groupL2Name = "France";
        Group groupL2 = getIdentityAPI().createGroup(groupL2Name, defaultGroup.getPath());
        assertEquals(defaultGroup.getPath(), groupL2.getParentPath());

        final String groupL3Name = "Grenoble";
        Group groupL3 = getIdentityAPI().createGroup(groupL3Name, groupL2.getPath());
        assertEquals(groupL2.getPath(), groupL3.getParentPath());

        final GroupUpdater updateDescriptor = new GroupUpdater();
        updateDescriptor.updateName("Germany");

        // update parent path
        getIdentityAPI().updateGroup(groupL2.getId(), updateDescriptor);
        groupL2 = getIdentityAPI().getGroup(groupL2.getId());
        assertEquals("Germany", groupL2.getName());

        // assert children are also updated
        groupL3 = getIdentityAPI().getGroup(groupL3.getId());
        assertEquals("/" + defaultGroup.getName() + "/Germany", groupL3.getParentPath());

        getIdentityAPI().deleteGroup(groupL3.getId());
        getIdentityAPI().deleteGroup(groupL2.getId());
        getIdentityAPI().deleteGroup(newRootGroup.getId());
    }

    @Test
    public void updateGroupNameAndParenthAlsoUpdateAllChildrenInfos() throws BonitaException {
        // arrange
        Group parentGroup = getIdentityAPI().createGroup("France", defaultGroup.getPath());
        Group childGroup = getIdentityAPI().createGroup("Grenoble", parentGroup.getPath());

        // act
        final GroupUpdater group2Updater = new GroupUpdater();
        group2Updater.updateParentPath("/WorldCompany");
        group2Updater.updateName("Germany");
        getIdentityAPI().updateGroup(parentGroup.getId(), group2Updater);

        // assert
        parentGroup = getIdentityAPI().getGroup(parentGroup.getId());
        childGroup = getIdentityAPI().getGroup(childGroup.getId());
        assertEquals("/WorldCompany/Germany", childGroup.getParentPath());
        assertEquals("/WorldCompany/Germany/Grenoble", childGroup.getPath());

        // clean-up:
        getIdentityAPI().deleteGroup(childGroup.getId());
        getIdentityAPI().deleteGroup(parentGroup.getId());
    }

    @Test(expected = GroupNotFoundException.class)
    public void updateGroupsNotFoundException() throws BonitaException {
        final GroupUpdater updateDescriptor = new GroupUpdater();
        updateDescriptor.updateName("newtest");
        updateDescriptor.updateDisplayName("newlabel");
        updateDescriptor.updateDescription("newdescription");
        getIdentityAPI().updateGroup(0, updateDescriptor);
    }

    @Test
    public void getUsersInGroup() throws BonitaException {
        final User aUserInRoleA = getIdentityAPI().createUser("testnameA", "bpm");
        final User bUserInRoleA = getIdentityAPI().createUser("testnameB", "bpm");
        final User cUserInRoleB = getIdentityAPI().createUser("testnameC", "bpm");
        final User dUser = getIdentityAPI().createUser("testnameD", "bpm");

        final Group group = createGroup("group", "testLabel", "description");
        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(aUserInRoleA.getId());
        userIds.add(bUserInRoleA.getId());
        final RoleCreator roleCreatorA = new RoleCreator("RoleA");
        roleCreatorA.setDisplayName("LabelA").setDescription("DescriptionA");
        final Role testRoleA = getIdentityAPI().createRole(roleCreatorA);
        getIdentityAPI().addUserMemberships(userIds, defaultGroup.getId(), testRoleA.getId());

        final List<Long> testIds = new ArrayList<Long>();
        testIds.add(cUserInRoleB.getId());
        final RoleCreator roleCreatorB = new RoleCreator("RoleB");
        roleCreatorB.setDisplayName("LabelB").setDescription("DescriptionB");
        final Role testRoleB = getIdentityAPI().createRole(roleCreatorB);
        getIdentityAPI().addUserMemberships(testIds, group.getId(), testRoleB.getId());

        final List<User> users = getIdentityAPI().getUsersInGroup(defaultGroup.getId(), 0, 5000, UserCriterion.USER_NAME_ASC);
        assertNotNull(users);
        assertEquals(2, users.size());
        assertEquals("testnameA", users.get(0).getUserName());
        assertEquals("testnameB", users.get(1).getUserName());

        getIdentityAPI().deleteUserMemberships(userIds, defaultGroup.getId(), testRoleA.getId());
        getIdentityAPI().deleteUserMemberships(testIds, group.getId(), testRoleB.getId());
        getIdentityAPI().deleteUser(aUserInRoleA.getId());
        getIdentityAPI().deleteUser(bUserInRoleA.getId());
        getIdentityAPI().deleteUser(cUserInRoleB.getId());
        getIdentityAPI().deleteUser(dUser.getId());
        getIdentityAPI().deleteRole(testRoleA.getId());
        getIdentityAPI().deleteRole(testRoleB.getId());
        getIdentityAPI().deleteGroup(group.getId());
    }

    @Test
    public void getNumberOfUsersInGroup() throws BonitaException {
        final User aUser = getIdentityAPI().createUser("testnameA", "bpm");
        final User bUser = getIdentityAPI().createUser("testnameB", "bpm");
        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(aUser.getId());
        userIds.add(bUser.getId());
        final Role testRole = getIdentityAPI().createRole("testRole");

        getIdentityAPI().addUserMemberships(userIds, defaultGroup.getId(), testRole.getId());
        final List<User> users = getIdentityAPI().getUsersInGroup(defaultGroup.getId(), 0, 5000, UserCriterion.USER_NAME_ASC);
        final long count = getIdentityAPI().getNumberOfUsersInGroup(defaultGroup.getId());

        assertNotNull(users);
        assertNotNull(count);
        assertEquals(count, users.size());
        assertEquals("testnameA", users.get(0).getUserName());
        assertEquals("testnameB", users.get(1).getUserName());

        getIdentityAPI().deleteUserMemberships(userIds, defaultGroup.getId(), testRole.getId());
        getIdentityAPI().deleteUser(aUser.getId());
        getIdentityAPI().deleteUser(bUser.getId());
        getIdentityAPI().deleteRole(testRole.getId());
    }

    @Test
    public void getPaginatedGroupsWithGroupCriterion() throws BonitaException {
        final Group groupA = createGroup("testA", "labelA", "descrtptionA");
        final Group groupB = createGroup("testB", "labelB", "descrtptionB");
        final Group groupC = createGroup("testc", "labelC", "descrtptionC");
        final Group groupD = createGroup("testd", "labelD", "descrtptionD");
        final List<Group> groupNameASCPage1 = getIdentityAPI().getGroups(0, 3, GroupCriterion.NAME_ASC);
        assertEquals(3, groupNameASCPage1.size());
        assertEquals("testA", groupNameASCPage1.get(1).getName());
        assertEquals("testB", groupNameASCPage1.get(2).getName());

        final List<Group> groupNameASCPage2 = getIdentityAPI().getGroups(3, 3, GroupCriterion.NAME_ASC);
        assertEquals(2, groupNameASCPage2.size());
        assertEquals("testc", groupNameASCPage2.get(0).getName());
        assertEquals("testd", groupNameASCPage2.get(1).getName());

        final List<Group> groupNameDESC = getIdentityAPI().getGroups(0, 3, GroupCriterion.NAME_DESC);
        assertEquals(3, groupNameDESC.size());
        assertEquals("testd", groupNameDESC.get(0).getName());
        assertEquals("testc", groupNameDESC.get(1).getName());

        final List<Group> groupLabelASC = getIdentityAPI().getGroups(0, 3, GroupCriterion.LABEL_ASC);
        assertEquals(3, groupLabelASC.size());
        assertEquals("labelA", groupLabelASC.get(1).getDisplayName());
        assertEquals("labelB", groupLabelASC.get(2).getDisplayName());

        final List<Group> groupLabelDESC = getIdentityAPI().getGroups(0, 3, GroupCriterion.LABEL_DESC);
        assertEquals(3, groupLabelDESC.size());
        assertEquals("labelD", groupLabelDESC.get(0).getDisplayName());
        assertEquals("labelC", groupLabelDESC.get(1).getDisplayName());

        getIdentityAPI().deleteGroup(groupA.getId());
        getIdentityAPI().deleteGroup(groupB.getId());
        getIdentityAPI().deleteGroup(groupC.getId());
        getIdentityAPI().deleteGroup(groupD.getId());
    }

    @Test
    public void searchGroupUsingFilter() throws BonitaException {
        final Group groupA = createGroup("testA", "labelA", "desc");
        final Group groupB = createGroup("testB", "labelB", "Bbb");
        final Group groupC = createGroup("c", "labelC", "descrtptionC");
        final Group groupD = createGroup("d", "labelD", "descrtptionD");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.filter(GroupSearchDescriptor.ID, groupC.getId());
        final SearchResult<Group> searchGroups = getIdentityAPI().searchGroups(builder.done());
        assertNotNull(searchGroups);
        assertEquals(1, searchGroups.getCount());
        final List<Group> groups = searchGroups.getResult();
        assertEquals(groupC, groups.get(0));

        getIdentityAPI().deleteGroup(groupA.getId());
        getIdentityAPI().deleteGroup(groupB.getId());
        getIdentityAPI().deleteGroup(groupC.getId());
        getIdentityAPI().deleteGroup(groupD.getId());
    }

    @Cover(classes = { SearchOptionsBuilder.class, IdentityAPI.class }, concept = BPMNConcept.ORGANIZATION, keywords = { "SearchGroup", "Apostrophe" }, jira = "ENGINE-366")
    @Test
    public void searchGroupWithApostrophe() throws BonitaException {
        final Group groupA = createGroup("test'A", "labelA", "desc");
        final Group groupB = createGroup("testB", "test'B", "Bbb");
        final Group groupC = createGroup("testc", "labelC", "test'C");

        final SearchOptionsBuilder builder = new SearchOptionsBuilder(0, 10);
        builder.sort(GroupSearchDescriptor.NAME, Order.ASC);
        builder.searchTerm("test'");
        final SearchResult<Group> searchGroups = getIdentityAPI().searchGroups(builder.done());
        assertNotNull(searchGroups);
        assertEquals(3, searchGroups.getCount());
        final List<Group> groups = searchGroups.getResult();
        assertEquals(groupA, groups.get(0));
        assertEquals(groupB, groups.get(1));
        assertEquals(groupC, groups.get(2));

        getIdentityAPI().deleteGroup(groupA.getId());
        getIdentityAPI().deleteGroup(groupB.getId());
        getIdentityAPI().deleteGroup(groupC.getId());
    }

    @Test
    public void checkCreatedByForGroup() throws BonitaException {
        final Group group = createGroup("group1", "myGroup", "descrtption");
        assertNotNull(group);
        assertNotNull(group.getCreatedBy());
        assertEquals(getSession().getUserId(), group.getCreatedBy());
        getIdentityAPI().deleteGroup(group.getId());
    }

    @Test
    @Cover(classes = { Group.class }, concept = BPMNConcept.ORGANIZATION, jira = "BS-2423", keywords = { "group" }, story = "create a lot of groups make long parent path and it should work")
    public void should_be_able_to_create_big_groups_hierarchy() throws BonitaException {
        // this should work
        // acme -> Site -> Service -> Departement -> Back Office & Logistique
        createGroup("acme");
        createGroup("Site", "/acme");
        createGroup("Service", "/acme/Site");
        createGroup("Departement", "/acme/Site/Service");
        createGroup("Back Office & Logistique", "/acme/Site/Service/Departement");
        createGroup("Administration Titres", "/acme/Site/Service/Departement/Back Office & Logistique");

        assertEquals("Administration Titres", getIdentityAPI().getGroupByPath("/acme/Site/Service/Departement/Back Office & Logistique/Administration Titres")
                .getName());
        deleteGroups(getIdentityAPI().getGroupByPath("/acme"));

    }
}
