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
package org.bonitasoft.web.rest.server.datastore.bpm.cases;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.test.toolkit.bpm.TestCase;
import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.cases.ArchivedCaseItem;
import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Test;

/**
 * @author ROHART Bastien
 */
public class ArchivedCaseDatastoreIT extends AbstractConsoleTest {

    private ArchivedCaseDatastore archivedCaseDatastore;

    /*
     * (non-Javadoc)
     * @see org.bonitasoft.console.server.AbstractConsoleTest#consoleTestSetUp()
     */
    @Override
    public void consoleTestSetUp() throws Exception {
        archivedCaseDatastore = new ArchivedCaseDatastore(getInitiator().getSession());
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
    public void twoPoolsWithOneWithACallActivityArchivedCaseTest() throws Exception {
        TestProcess subprocess = TestProcessFactory.getDefaultHumanTaskProcess();
        subprocess.addActor(getInitiator()).enable();

        // start subprocess case via call activity
        TestProcess rootProcess = TestProcessFactory.getCallActivityProcess(subprocess.getProcessDefinition());
        var rootInstance = rootProcess.addActor(getInitiator()).enable().startCase();
        await().atMost(5, TimeUnit.SECONDS).until(() -> !subprocess.listAllOpenCases().isEmpty());

        // archive process 1 case
        TestCase testCaseProcess = rootProcess.listOpenCases().get(0);
        testCaseProcess.getNextHumanTask().assignTo(getInitiator()).executeUserTask(getInitiator());
        await().atMost(5, TimeUnit.SECONDS).until(() -> rootInstance.getArchive() != null);

        // Filters for archived Cases
        ItemSearchResult<ArchivedCaseItem> itemSearchResult = archivedCaseDatastore.search(0, 100, null, null,
                new HashMap<>());

        assertEquals("2 cases started but one via call activity so only 1 should be retrieved", 1,
                itemSearchResult.getResults().size());

        TestProcessFactory.getInstance().delete(rootProcess);
        TestProcessFactory.getInstance().delete(subprocess);
    }

    @Test
    public void searchArchivedSubCases() throws Exception {
        TestProcess subprocess = TestProcessFactory.getDefaultHumanTaskProcess();
        subprocess.addActor(getInitiator()).enable();

        // start subprocess case via call activity
        TestProcess rootProcess = TestProcessFactory.getCallActivityProcess(subprocess.getProcessDefinition());
        var rootInstance = rootProcess.addActor(getInitiator()).enable().startCase();
        await().atMost(5, TimeUnit.SECONDS).until(() -> !subprocess.listAllOpenCases().isEmpty());

        // archive process 1 case
        TestCase testCaseRootProcess = rootProcess.listOpenCases().get(0);
        testCaseRootProcess.getNextHumanTask().assignTo(getInitiator()).executeUserTask(getInitiator());
        await().atMost(5, TimeUnit.SECONDS).until(() -> rootInstance.getArchive() != null);

        // Filters for archived Cases
        HashMap<String, String> filters = new HashMap<>();
        filters.put(ArchivedCaseItem.ATTRIBUTE_ROOT_CASE_ID, String.valueOf(testCaseRootProcess.getId()));
        filters.put(CaseItem.FILTER_CALLER, "any");
        ItemSearchResult<ArchivedCaseItem> itemSearchResult = archivedCaseDatastore.search(0, 100, null, null,
                filters);

        assertEquals("Filtering on root case ID, only the subcase should be retrieved", 1,
                itemSearchResult.getResults().size());
        assertEquals(subprocess.getProcessDefinition().getId(),
                itemSearchResult.getResults().get(0).getProcessId().toLong().longValue());

        TestProcessFactory.getInstance().delete(rootProcess);
        TestProcessFactory.getInstance().delete(subprocess);

    }

}
