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
package org.bonitasoft.web.rest.server.api.bpm.flownode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bonitasoft.test.toolkit.bpm.TestHumanTask;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.flownode.HumanTaskItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Test;

public class APIHumanTaskIT extends AbstractConsoleTest {

    public APIHumanTask apiHumanTask;

    private TestHumanTask testHumanTask;

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.console.server.AbstractJUnitWebTest#webTestSetUp()
     */
    @Override
    public void consoleTestSetUp() throws Exception {
        testHumanTask = TestProcessFactory.getDefaultHumanTaskProcess()
                .addActor(TestUserFactory.getJohnCarpenter())
                .startCase()
                .getNextHumanTask();
        createAPIHumanTask();
    }

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.test.toolkit.AbstractJUnitTest#getInitiator()
     */
    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    @Test
    public void testGetDatastore() {
        assertNotNull("Is not possible to retrieve the dataStore", apiHumanTask.getDefaultDatastore());
    }

    @Test
    public void testGetHumanTaskItem() {
        final ArrayList<String> deploys = new ArrayList<>();
        deploys.add(HumanTaskItem.ATTRIBUTE_PROCESS_ID);
        final ArrayList<String> counters = new ArrayList<>();
        final APIID apiId = APIID.makeAPIID(testHumanTask.getId());
        final HumanTaskItem humanTaskItem = apiHumanTask.runGet(apiId, deploys, counters);
        assertEquals("Not possible to get the APIHUmanTaskItem ", humanTaskItem.getName(), testHumanTask.getName());
        assertEquals("Not possible to get the APIHUmanTaskItem ", humanTaskItem.getDescription(),
                testHumanTask.getDescription());
    }

    @Test
    public void testUpdateHumanTaskItem() {

        final APIID apiId = APIID.makeAPIID(testHumanTask.getId());

        // Update the humanTaskItem attributes
        final HashMap<String, String> attributes = new HashMap<>();
        attributes.put(HumanTaskItem.ATTRIBUTE_ASSIGNED_USER_ID,
                String.valueOf(TestUserFactory.getJohnCarpenter().getId()));
        apiHumanTask.update(apiId, attributes);
        final HumanTaskItem updateHumanTaskItem = apiHumanTask.get(apiId);
        assertNotSame("Attributes are not updated", updateHumanTaskItem.getAssignedId(),
                TestUserFactory.getJohnCarpenter().getId());

    }

    @Test
    public void testSearch() {
        // Set the filters
        final HashMap<String, String> filters = new HashMap<>();
        filters.put(HumanTaskItem.ATTRIBUTE_ID, String.valueOf(testHumanTask.getId()));

        // Search the humanTaskItem
        final ArrayList<String> deploys = new ArrayList<>();
        deploys.add(HumanTaskItem.ATTRIBUTE_PROCESS_ID);
        final ArrayList<String> counters = new ArrayList<>();
        final HumanTaskItem foundHumanTaskItem = apiHumanTask.runSearch(0, 1, null, null, filters, deploys, counters)
                .getResults().get(0);
        assertEquals("Can't search the humanTaskItem", testHumanTask.getName(), foundHumanTaskItem.getName());
    }

    @Test
    /**
     * Check that the paging system works fine
     */
    public void testHumanTaskItemSearchPaging() {

        final long before = apiHumanTask.runSearch(0, 10, null,
                apiHumanTask.defineDefaultSearchOrder(),
                new HashMap<>(),
                new ArrayList<>(), new ArrayList<>()).getTotal();

        // Setup : insert enough tasks to have 2 pages
        for (int i = 0; i < 15; i++) {
            try {
                TestProcessFactory.getDefaultHumanTaskProcess().startCase();
            } catch (final Exception e) {
                fail("Can't start process [" + e.getLocalizedMessage() + "]");
            }
        }

        // Setup: retrieve the needed APIs
        // this.apiHumanTask = new APIHumanTask();
        // final APIServletCall caller = new APIServletCall(mockHttpServletRequest, mockHttpServletResponse);
        // this.apiHumanTask.setCaller(caller);

        // Search for page 2 (1 in zero based)
        final ItemSearchResult<HumanTaskItem> search = apiHumanTask.runSearch(1, 10, null,
                apiHumanTask.defineDefaultSearchOrder(),
                new HashMap<>(),
                new ArrayList<>(), new ArrayList<>());

        assertThat(search.getResults().size()).isGreaterThan(2);
        assertThat(search.getTotal()).isGreaterThan(before);
    }

    @Test
    /**
     * Check when assigned a task to me this task is in available list
     *
     * @throws Exception
     */
    public void testAssignedTaskInAvailable() {
        testHumanTask.assignTo(TestUserFactory.getJohnCarpenter());

        final ArrayList<String> deploys = new ArrayList<>();
        deploys.add(HumanTaskItem.ATTRIBUTE_PROCESS_ID);
        final ArrayList<String> counters = new ArrayList<>();
        final HashMap<String, String> filters = new HashMap<>();
        filters.put(HumanTaskItem.FILTER_USER_ID,
                String.valueOf(TestUserFactory.getJohnCarpenter().getId()));

        final List<HumanTaskItem> listHumanTaskItem = apiHumanTask
                .runSearch(0, 1, null, null, filters, deploys, counters).getResults();
        assertEquals("HumanTask assigned to me not in available list", 1, listHumanTaskItem.size());
    }

    /**
     * Initialize APIHumanTask
     */
    private void createAPIHumanTask() {
        apiHumanTask = new APIHumanTask();
        apiHumanTask.setCaller(getAPICaller(TestUserFactory.getJohnCarpenter().getSession(),
                "API/bpm/humanTask"));
    }

}
