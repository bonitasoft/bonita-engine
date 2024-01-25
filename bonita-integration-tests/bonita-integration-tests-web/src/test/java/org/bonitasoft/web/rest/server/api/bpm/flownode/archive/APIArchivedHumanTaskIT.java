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
package org.bonitasoft.web.rest.server.api.bpm.flownode.archive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.web.toolkit.client.data.APIID.makeAPIID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstance;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedHumanTaskItem;
import org.bonitasoft.web.rest.model.bpm.flownode.HumanTaskItem;
import org.bonitasoft.web.rest.server.WaitUntil;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Assert;
import org.junit.Test;

/**
 * FIXME to be refactored using much more test-toolkit
 */
public class APIArchivedHumanTaskIT extends AbstractConsoleTest {

    private APIArchivedHumanTask apiArchivedHumanTask;

    @Override
    public void consoleTestSetUp() throws Exception {
        apiArchivedHumanTask = new APIArchivedHumanTask();
        apiArchivedHumanTask.setCaller(getAPICaller(getInitiator().getSession(), "API/bpm/archivedHumanTask"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    private HumanTaskInstance initArchivedHumanTaskInstance() throws Exception {
        final TestProcess defaultHumanTaskProcess = TestProcessFactory.getDefaultHumanTaskProcess();
        defaultHumanTaskProcess.addActor(getInitiator());
        final ProcessInstance processInstance = defaultHumanTaskProcess.startCase(getInitiator()).getProcessInstance();

        waitPendingHumanTask(processInstance.getId());

        // Retrieve a humanTaskInstance
        final HumanTaskInstance humanTaskInstance = getProcessAPI()
                .getPendingHumanTaskInstances(getInitiator().getId(), 0, 10, null).get(0);
        getProcessAPI().assignUserTask(humanTaskInstance.getId(), getInitiator().getId());

        waitAssignedHumanTask();

        getProcessAPI().executeFlowNode(humanTaskInstance.getId());

        waitArchivedActivityInstance(processInstance.getId());

        return humanTaskInstance;
    }

    private ProcessAPI getProcessAPI() throws Exception {
        return TenantAPIAccessor.getProcessAPI(getInitiator().getSession());
    }

    private ArrayList<String> getProcessIdDeploy() {
        final ArrayList<String> deploys = new ArrayList<>();
        deploys.add(HumanTaskItem.ATTRIBUTE_PROCESS_ID);
        return deploys;
    }

    private HashMap<String, String> getNameFilter(final HumanTaskInstance humanTaskInstance) {
        final HashMap<String, String> filters = new HashMap<>();
        filters.put(ArchivedHumanTaskItem.ATTRIBUTE_NAME, humanTaskInstance.getName());
        return filters;
    }

    /**
     * Wait the process contain PendingHumanTaskInstance
     */
    private void waitPendingHumanTask(final long processInstanceId) throws Exception {

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(HumanTaskInstanceSearchDescriptor.PROCESS_INSTANCE_ID, processInstanceId);

        Assert.assertTrue("no pending task instances are found", new WaitUntil(50, 3000) {

            @Override
            protected boolean check() throws Exception {
                return getProcessAPI().searchPendingTasksForUser(getInitiator().getId(), searchOptionsBuilder.done())
                        .getCount() >= 1;
            }
        }.waitUntil());
    }

    private void waitAssignedHumanTask() throws Exception {
        Assert.assertTrue("Human task hasnt been assign", new WaitUntil(50, 3000) {

            @Override
            protected boolean check() throws Exception {
                return getProcessAPI().getAssignedHumanTaskInstances(getInitiator().getId(), 0, 10,
                        ActivityInstanceCriterion.DEFAULT).size() >= 1;
            }
        }.waitUntil());
    }

    /**
     * Wait the process contain ArchivedHumanTaskInstance
     */
    private void waitArchivedActivityInstance(final long processInstanceId) throws Exception {
        Assert.assertTrue("no archived task instances are found", new WaitUntil(50, 3000) {

            @Override
            protected boolean check() throws Exception {
                final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
                searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID,
                        processInstanceId);
                return getProcessAPI().searchArchivedActivities(searchOptionsBuilder.done()).getCount() >= 1L;
            }
        }.waitUntil());
    }

    @Test
    public void testGetArchivedHumanTask() throws Exception {
        final HumanTaskInstance humanTaskInstance = initArchivedHumanTaskInstance();
        final ArrayList<String> deploys = getProcessIdDeploy();

        final SearchOptionsBuilder searchOptionsBuilder = new SearchOptionsBuilder(0, 10);
        searchOptionsBuilder.filter(ArchivedActivityInstanceSearchDescriptor.PARENT_PROCESS_INSTANCE_ID,
                humanTaskInstance.getRootContainerId());
        final ArchivedActivityInstance archivedActivityInstance = getProcessAPI()
                .searchArchivedActivities(searchOptionsBuilder.done()).getResult().get(0);

        final ArchivedHumanTaskItem archivedHumanTaskItem = apiArchivedHumanTask
                .runGet(makeAPIID(archivedActivityInstance.getId()), deploys, new ArrayList<>());

        assertEquals("Can't get the good archivedTaskItem", archivedHumanTaskItem.getName(),
                humanTaskInstance.getName());
    }

    @Test
    public void testSearchArchivedHumanTask() throws Exception {
        final HumanTaskInstance humanTaskInstance = initArchivedHumanTaskInstance();
        final ArrayList<String> deploys = getProcessIdDeploy();
        final HashMap<String, String> filters = getNameFilter(humanTaskInstance);

        final ArchivedHumanTaskItem archivedHumanTaskItem = apiArchivedHumanTask.runSearch(0, 1, null, null,
                filters, deploys, new ArrayList<>()).getResults().get(0);

        assertNotNull("Can't find the good archivedTaskItem", archivedHumanTaskItem);
    }

    @Test
    public void testGetDatastore() {
        assertNotNull("Can't get the Datastore", apiArchivedHumanTask.getDefaultDatastore());
    }

    @Test
    public void archivedHumanTasksCanBeSortedByReachedStateDate() throws Exception {
        shouldSearchArchivedHumaTaskWithOrder(ArchivedHumanTaskItem.ATTRIBUTE_REACHED_STATE_DATE + " DESC");
    }

    @Test
    public void testSearchWithDefaultOrder() throws Exception {
        shouldSearchArchivedHumaTaskWithOrder(apiArchivedHumanTask.defineDefaultSearchOrder());

    }

    private void shouldSearchArchivedHumaTaskWithOrder(final String orders) throws Exception {
        //given
        final HumanTaskInstance humanTaskInstance = initArchivedHumanTaskInstance();
        final HashMap<String, String> filters = getNameFilter(humanTaskInstance);

        //when
        final ItemSearchResult<ArchivedHumanTaskItem> search = apiArchivedHumanTask.runSearch(0, 1, null, orders,
                filters, null, null);

        //then
        assertThat(search.getResults()).as("should get results").isNotEmpty();
    }

}
