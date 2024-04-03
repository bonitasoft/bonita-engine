/**
 * Copyright (C) 2022 Bonitasoft S.A.
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
package org.bonitasoft.web.rest.server.api.organization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.test.toolkit.organization.TestGroup;
import org.bonitasoft.test.toolkit.organization.TestGroupFactory;
import org.bonitasoft.test.toolkit.organization.TestMembershipFactory;
import org.bonitasoft.test.toolkit.organization.TestRole;
import org.bonitasoft.test.toolkit.organization.TestRoleFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.identity.MembershipItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIForbiddenException;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Séverin Moussel
 * @author Colin PUY
 */
public class APIMembershipIT extends AbstractConsoleTest {

    private APIMembership apiMembership;

    @Override
    public void consoleTestSetUp() throws Exception {
        apiMembership = new APIMembership();
        apiMembership.setCaller(getAPICaller(getInitiator().getSession(), "API/identity/membership"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    private void checkSearchResults(final ItemSearchResult<MembershipItem> membershipItems,
            final int nbResultsByPageExpected, final int nbTotalResultsExpected) {
        assertTrue("Empty search results", membershipItems.getLength() > 0);
        assertEquals("Wrong page size", membershipItems.getLength(), nbResultsByPageExpected);
        assertEquals("Wrong Total size", membershipItems.getTotal(), nbTotalResultsExpected);
    }

    @Test
    public void testAdd() {
        // Add
        final MembershipItem input = new MembershipItem();
        input.setUserId(getInitiator().getId());
        input.setGroupId(TestGroupFactory.getWeb().getId());
        input.setRoleId(TestRoleFactory.getManager().getId());

        final MembershipItem output = apiMembership.runAdd(input);

        Assert.assertNotNull("Failed to add a new membership", input);
        assertEquals("Wrong membership inserted", input.getUserId(), output.getUserId());
        assertEquals("Wrong membership inserted", input.getGroupId(), output.getGroupId());
        assertEquals("Wrong membership inserted", input.getRoleId(), output.getRoleId());
    }

    private void beforeSearch() {
        final List<TestRole> roles = TestRoleFactory.getInstance().createRandomRoles(5);
        final List<TestGroup> groups = TestGroupFactory.createRandomGroups(5);

        final TestUser user1 = getInitiator();
        final TestUser user2 = TestUserFactory.getRidleyScott();

        for (final TestRole role : roles) {
            for (final TestGroup group : groups) {
                TestMembershipFactory.assignMembership(user1, group, role);
                TestMembershipFactory.assignMembership(user2, group, role);
            }
        }
    }

    @Test
    public void testSearch() {
        beforeSearch();

        final Map<String, String> filters = new HashMap<>();
        filters.put(MembershipItem.ATTRIBUTE_USER_ID, String.valueOf(getInitiator().getId()));

        final ItemSearchResult<MembershipItem> searchResults = apiMembership.runSearch(0, 12, null, null, filters, null,
                null);

        checkSearchResults(searchResults, 12, 25);
    }

    @Test
    public void testDeploys() {
        beforeSearch();

        final Map<String, String> filters = new HashMap<>();
        filters.put(MembershipItem.ATTRIBUTE_USER_ID, String.valueOf(getInitiator().getId()));

        final ItemSearchResult<MembershipItem> searchResults = apiMembership.runSearch(
                0,
                11,
                null,
                null,
                filters,
                Arrays.asList(
                        MembershipItem.ATTRIBUTE_USER_ID,
                        MembershipItem.ATTRIBUTE_ROLE_ID,
                        MembershipItem.ATTRIBUTE_GROUP_ID,
                        MembershipItem.ATTRIBUTE_ASSIGNED_BY_USER_ID),
                null);

        checkSearchResults(searchResults, 11, 25);

        final MembershipItem firstMembership = searchResults.getResults().get(0);

        Assert.assertNotNull("Failed to deploy user_id", firstMembership.getUser());
        assertEquals("Wrong user deployed", getInitiator().getUser().getUserName(),
                firstMembership.getUser().getUserName());
        Assert.assertNotNull("Failed to deploy role_id", firstMembership.getRole());
        Assert.assertNotNull("Failed to deploy group_id", firstMembership.getGroup());
        Assert.assertNotNull("Failed to deploy assigned_by_user_id", firstMembership.getAssignedByUser());
    }

    @Test
    public void testDelete() {

        // INIT
        final TestRole roleManager = TestRoleFactory.getManager();
        final TestRole roleDeveloper = TestRoleFactory.getDeveloper();
        final TestGroup groupWeb = TestGroupFactory.createRandomGroups(1).get(0);

        final TestUser user = getInitiator();

        TestMembershipFactory.assignMembership(user, groupWeb, roleManager);
        TestMembershipFactory.assignMembership(user, groupWeb, roleDeveloper);

        // ACTION
        apiMembership.runDelete(
                List.of(APIID.makeAPIID(
                        getInitiator().getId(),
                        groupWeb.getId(),
                        roleManager.getId())));

        // CHECK RESULT

        final Map<String, String> filters = new HashMap<>();
        filters.put(MembershipItem.ATTRIBUTE_USER_ID, String.valueOf(getInitiator().getId()));

        final ItemSearchResult<MembershipItem> searchResults = apiMembership.runSearch(0, 12, null, null, filters, null,
                null);

        checkSearchResults(searchResults, 12, 1);
    }

    @Test(expected = APIForbiddenException.class)
    public void addingTwiceSameMembershipIsForbidden() {
        MembershipItem input = new MembershipItem();
        input.setUserId(getInitiator().getId());
        input.setGroupId(TestGroupFactory.getWeb().getId());
        input.setRoleId(TestRoleFactory.getManager().getId());

        apiMembership.runAdd(input);
        apiMembership.runAdd(input);
    }
}
