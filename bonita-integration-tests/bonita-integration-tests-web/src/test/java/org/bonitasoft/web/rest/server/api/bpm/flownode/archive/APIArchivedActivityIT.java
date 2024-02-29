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

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.api.TenantAPIAccessor;
import org.bonitasoft.engine.bpm.flownode.ActivityInstanceCriterion;
import org.bonitasoft.engine.bpm.flownode.ArchivedActivityInstanceSearchDescriptor;
import org.bonitasoft.engine.bpm.flownode.HumanTaskInstance;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.search.SearchOptionsBuilder;
import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.flownode.ArchivedActivityItem;
import org.bonitasoft.web.rest.server.WaitUntil;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Assert;
import org.junit.Test;

public class APIArchivedActivityIT extends AbstractConsoleTest {

    private APIArchivedActivity apiArchivedActivity;

    @Override
    public void consoleTestSetUp() throws Exception {
        apiArchivedActivity = new APIArchivedActivity();
        apiArchivedActivity.setCaller(getAPICaller(getInitiator().getSession(), "API/bpm/archivedActivity"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    private HumanTaskInstance initArchivedHumanTaskInstance() throws Exception {
        final TestProcess defaultHumanTaskProcess = TestProcessFactory.getDefaultHumanTaskProcess();
        defaultHumanTaskProcess.addActor(getInitiator());
        final ProcessInstance processInstance = defaultHumanTaskProcess.startCase(getInitiator()).getProcessInstance();

        waitPendingHumanTask();

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

    /**
     * Wait the process contain PendingHumanTaskInstance
     */
    private void waitPendingHumanTask() throws Exception {
        Assert.assertTrue("no pending task instances are found", new WaitUntil(50, 3000) {

            @Override
            protected boolean check() throws Exception {
                return getProcessAPI().getPendingHumanTaskInstances(getInitiator().getId(), 0, 10, null).size() >= 1;
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
    public void testSearchWithDefaultOrder() throws Exception {
        verifySearhWithOrder(apiArchivedActivity.defineDefaultSearchOrder());
    }

    @Test
    public void testSearchWithNoOrder() throws Exception {
        verifySearhWithOrder(null);

    }

    private void verifySearhWithOrder(final String order) throws Exception {
        //given
        initArchivedHumanTaskInstance();

        //when
        final ItemSearchResult<ArchivedActivityItem> search = apiArchivedActivity.runSearch(0, 1, null, order, null,
                null, null);

        //then
        assertThat(search.getResults()).as("should get results").isNotEmpty();
    }

}
