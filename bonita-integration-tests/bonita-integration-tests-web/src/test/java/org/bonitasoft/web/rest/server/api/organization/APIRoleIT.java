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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.test.toolkit.organization.TestUserFactory.getJohnCarpenter;
import static org.bonitasoft.test.toolkit.organization.TestUserFactory.getMrSpechar;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import org.bonitasoft.engine.api.PlatformAPIAccessor;
import org.bonitasoft.engine.exception.BonitaHomeNotSetException;
import org.bonitasoft.engine.exception.ServerAPIException;
import org.bonitasoft.engine.exception.UnknownAPITypeException;
import org.bonitasoft.engine.identity.Role;
import org.bonitasoft.engine.io.FileContent;
import org.bonitasoft.test.toolkit.organization.*;
import org.bonitasoft.web.rest.model.identity.RoleItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Séverin Moussel
 */
public class APIRoleIT extends AbstractConsoleTest {

    @Override
    public void consoleTestSetUp() throws Exception {
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    /**
     * @return
     */
    private APIRole getAPIRole() {
        final APIRole apiRole = new APIRole();
        apiRole.setCaller(getAPICaller(getInitiator().getSession(), "API/identity/role"));
        return apiRole;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // GET / ADD
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void assertItemEquals(final String message, final RoleItem expected, final RoleItem actual) {
        Assert.assertEquals(message, expected.getAttributes(), actual.getAttributes());
    }

    @Test
    public void testAddAndGet() {
        // Add
        RoleItem input = new RoleItem();
        input.setName("Developper");
        input.setDescription("The guys who drink a lot of coffee");
        input = getAPIRole().runAdd(input);

        Assert.assertNotNull("Failed to add a new role", input);

        // Get
        final RoleItem output = getAPIRole().runGet(input.getId(), new ArrayList<>(), new ArrayList<>());

        Assert.assertNotNull("Role not found", output);
        assertItemEquals("Wrong role found", input, output);
        getAPIRole().runDelete(Arrays.asList(input.getId()));
    }

    @Test
    public void testDeploys() {
        // Add
        RoleItem input = new RoleItem();
        input.setName("Developper");
        input.setDescription("The guys who drink a lot of coffee");
        input = getAPIRole().runAdd(input);

        Assert.assertNotNull("Failed to add a new role", input);

        // Get
        final RoleItem output = getAPIRole().runGet(
                input.getId(),
                Arrays.asList(RoleItem.ATTRIBUTE_CREATED_BY_USER_ID),
                new ArrayList<>());

        Assert.assertNotNull("Role not found", output);
        assertItemEquals("Wrong role found", input, output);

        Assert.assertNotNull("Failed to deploy initiator user", output.getCreatedByUserId());
        Assert.assertEquals("Wrong process deployed", getInitiator().getUserName(),
                output.getCreatedByUser().getUserName());

        getAPIRole().runDelete(Arrays.asList(input.getId()));

    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // SEARCH
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testSearch() throws Exception {
        TestRoleFactory.getInstance().createRandomRoles(13);

        final ItemSearchResult<RoleItem> roleItems = getAPIRole().runSearch(0, 10, null, null, null, null, null);

        checkSearchResults(roleItems, 10, 13);
    }

    /**
     * @param roleItems
     */
    private void checkSearchResults(final ItemSearchResult<RoleItem> roleItems, final int nbResultsByPageExpected,
            final int nbTotalResultsExpected) {
        Assert.assertTrue("Empty search results", roleItems.getLength() > 0);
        Assert.assertTrue("Wrong page size", roleItems.getLength() == nbResultsByPageExpected);
        Assert.assertTrue("Wrong Total size", roleItems.getTotal() == nbTotalResultsExpected);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DELETE
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testDeleteOne() throws Exception {
        TestRoleFactory.getInstance().createRandomRoles(13);

        final ItemSearchResult<RoleItem> roleItems = getAPIRole().runSearch(0, 10, null, null, null, null, null);
        getAPIRole().runDelete(Arrays.asList(roleItems.getResults().get(0).getId()));

        final ItemSearchResult<RoleItem> roleItemsAfter = getAPIRole().runSearch(0, 10, null, null, null, null, null);
        Assert.assertEquals("Failed to delete one role", 12, roleItemsAfter.getTotal());
    }

    @Test
    public void testDeleteMultiple() throws Exception {
        TestRoleFactory.getInstance().createRandomRoles(13);

        final ItemSearchResult<RoleItem> roleItems = getAPIRole().runSearch(0, 10, null, null, null, null, null);

        getAPIRole().runDelete(Arrays.asList(
                roleItems.getResults().get(1).getId(),
                roleItems.getResults().get(0).getId()));

        final ItemSearchResult<RoleItem> roleItemsAfter = getAPIRole().runSearch(0, 10, null, null, null, null, null);
        Assert.assertEquals("Failed to delete multiple roles", 11, roleItemsAfter.getTotal());
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UPDATE
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testUpdate() throws Exception {
        final String newDescription = "Lorem ipsum dolor sit amet";

        // Add
        RoleItem input = new RoleItem();
        input.setName("Developper");
        input.setDescription("The guys who drink a lot of coffee");
        input = getAPIRole().runAdd(input);

        Assert.assertNotNull("Failed to add a new role", input);

        // Update
        final Map<String, String> updates = new HashMap<>();
        updates.put(RoleItem.ATTRIBUTE_DESCRIPTION, newDescription);
        getAPIRole().runUpdate(input.getId(), updates);

        // Get
        final RoleItem output = getAPIRole().runGet(input.getId(), new ArrayList<>(), new ArrayList<>());

        Assert.assertNotNull("Role not found", output);
        Assert.assertEquals("Update of role failed", newDescription, output.getDescription());

        getAPIRole().runDelete(Arrays.asList(input.getId()));
    }

    @Test
    public void should_update_role_icon()
            throws IOException, ServerAPIException, BonitaHomeNotSetException, UnknownAPITypeException {
        // Add
        RoleItem input = new RoleItem();
        final APIRole spyApiRole = spy(getAPIRole());
        input.setName("Developper");
        input.setDescription("The guys who drink a lot of coffee");
        input = spyApiRole.runAdd(input);
        final APIID id = input.getId();
        assertThat(input).isNotNull();
        input = new RoleItem();

        //store icon into database
        File file = File.createTempFile("tmp", ".png");
        Files.writeString(file.toPath(), "content");
        String iconFileKey = PlatformAPIAccessor.getTemporaryContentAPI()
                .storeTempFile(new FileContent("icon.png", new FileInputStream(file), "img/png"));

        input.setIcon(iconFileKey);

        try {
            input = spyApiRole.runUpdate(id, input.getAttributes());
            assertThat(input).isNotNull();
        } finally {
            spyApiRole.runDelete(Arrays.asList(id));
        }
    }

    @Test
    public void weCanCountAllUsersInAGroup() throws Exception {
        final Role roleWith2Users = createRoleWithAssignedUsers(getJohnCarpenter(), getMrSpechar());
        final List<String> counters = asList(RoleItem.COUNTER_NUMBER_OF_USERS);

        final RoleItem roleItem = getAPIRole().runGet(APIID.makeAPIID(roleWith2Users.getId()), null, counters);

        assertEquals(2L, (long) roleItem.getNumberOfUsers());
    }

    private Role createRoleWithAssignedUsers(final TestUser... users) {
        final TestGroup aGroup = TestGroupFactory.getRAndD();
        final TestRole aRole = TestRoleFactory.getDeveloper();
        for (final TestUser user : users) {
            TestMembershipFactory.assignMembership(user, aGroup, aRole);
        }
        return aRole.getRole();
    }
}
