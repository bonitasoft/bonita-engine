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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.bonitasoft.test.toolkit.bpm.TestCase;
import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.cases.CaseItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.junit.Test;

/**
 * @author ROHART Bastien
 */
public class CaseDatastoreIT extends AbstractConsoleTest {

    private CaseDatastore caseDatastore;

    @Override
    public void consoleTestSetUp() throws Exception {
        caseDatastore = new CaseDatastore(getInitiator().getSession());
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    @Test
    public void twoPoolsWithOneWithACallActivityCaseTest() throws Exception {
        final long before = caseDatastore.search(0, 100, null, null, new HashMap<>()).getTotal();
        final TestProcess process2 = TestProcessFactory.getDefaultHumanTaskProcess();
        process2.addActor(getInitiator());
        process2.enable();

        // start process1 case via call activity
        final TestProcess process1 = TestProcessFactory.getCallActivityProcess(process2.getProcessDefinition());
        final TestCase parentCase = process1.addActor(getInitiator()).startCase();

        //wait for process instance to be in a "stable" state
        parentCase.getNextHumanTask();
        // Filters for Opened Cases
        final ItemSearchResult<CaseItem> itemSearchResult = caseDatastore.search(0, 100, null, null, new HashMap<>());

        assertEquals("2 cases started but one via call activity so only 1 should be opened", 1,
                itemSearchResult.getResults().size() - before);

        TestProcessFactory.getInstance().delete(process1);
        TestProcessFactory.getInstance().delete(process2);
    }

}
