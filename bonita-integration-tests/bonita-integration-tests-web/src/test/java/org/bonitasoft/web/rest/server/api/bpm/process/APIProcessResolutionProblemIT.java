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
package org.bonitasoft.web.rest.server.api.bpm.process;

import junit.framework.Assert;
import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.process.ProcessResolutionProblemItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.bonitasoft.web.toolkit.client.common.texttemplate.Arg;
import org.bonitasoft.web.toolkit.client.common.util.MapUtil;
import org.junit.Test;

/**
 * @author Séverin Moussel
 */
public class APIProcessResolutionProblemIT extends AbstractConsoleTest {

    @Override
    public void consoleTestSetUp() throws Exception {
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // UTILS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private APIProcessResolutionProblem getAPI() {
        final APIProcessResolutionProblem api = new APIProcessResolutionProblem();
        api.setCaller(getAPICaller(getInitiator().getSession(), "API/bpm/processResolutionProblem"));
        return api;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // TESTS
    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    public void testSearchWithResults() {
        final TestProcess process = TestProcessFactory.getDefaultHumanTaskProcess();
        final ItemSearchResult<ProcessResolutionProblemItem> results = getAPI().runSearch(
                0, 100,
                null,
                null,
                MapUtil.asMap(new Arg(ProcessResolutionProblemItem.FILTER_PROCESS_ID, process.getId())),
                null,
                null);

        Assert.assertFalse("No resolution issues found", results.getResults().size() == 0);
        Assert.assertTrue("Wrong number of resolution issues found", results.getResults().size() == 1);
    }

    @Test
    public void testSearchWithoutResults() {
        final TestProcess process = TestProcessFactory.getDefaultHumanTaskProcess();
        process.addActor(getInitiator());

        final ItemSearchResult<ProcessResolutionProblemItem> results = getAPI().runSearch(
                0, 100,
                null,
                null,
                MapUtil.asMap(new Arg(ProcessResolutionProblemItem.FILTER_PROCESS_ID, process.getId())),
                null,
                null);

        Assert.assertTrue("Resolution issues found", results.getResults().size() == 0);
    }

}
