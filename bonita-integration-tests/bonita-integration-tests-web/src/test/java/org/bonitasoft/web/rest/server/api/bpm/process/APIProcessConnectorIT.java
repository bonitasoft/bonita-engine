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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.test.toolkit.bpm.TestProcess;
import org.bonitasoft.test.toolkit.bpm.TestProcessFactory;
import org.bonitasoft.test.toolkit.bpm.process.TestProcessConnector;
import org.bonitasoft.test.toolkit.bpm.process.TestProcessConnectorFactory;
import org.bonitasoft.test.toolkit.organization.TestUser;
import org.bonitasoft.test.toolkit.organization.TestUserFactory;
import org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDefinition;
import org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorItem;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.test.AbstractConsoleTest;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Test;

/**
 * @author Colin PUY
 */
public class APIProcessConnectorIT extends AbstractConsoleTest {

    private APIProcessConnector apiProcessConnector;

    @Override
    public void consoleTestSetUp() throws Exception {
        apiProcessConnector = new APIProcessConnector();
        apiProcessConnector.setCaller(getAPICaller(getInitiator().getSession(), "API/bpm/processConnector"));
    }

    @Override
    protected TestUser getInitiator() {
        return TestUserFactory.getJohnCarpenter();
    }

    @Test
    public void testSearch() {
        TestProcessConnector defaultConnector = TestProcessConnectorFactory.getDefaultConnector();
        TestProcess processWithConnector = TestProcessFactory.createProcessWithConnector(defaultConnector);
        Map<String, String> filters = new HashMap<>();
        filters.put(ProcessConnectorItem.ATTRIBUTE_PROCESS_ID, String.valueOf(processWithConnector.getId()));

        ItemSearchResult<ProcessConnectorItem> search = apiProcessConnector.runSearch(0, 10, "", null, filters, null,
                null);

        ProcessConnectorItem expectedItem = toProcessConnectorItem(defaultConnector, processWithConnector.getId());
        assertTrue(areEquals(expectedItem, search.getResults().get(0)));
        assertEquals(1L, search.getTotal());
    }

    @Test
    public void testGet() {
        TestProcessConnector defaultConnector = TestProcessConnectorFactory.getDefaultConnector();
        TestProcess processWithConnector = TestProcessFactory.createProcessWithConnector(defaultConnector);

        APIID apiid = anApiId(processWithConnector.getId(), defaultConnector.getId(), defaultConnector.getVersion());
        ProcessConnectorItem processConnectorItem = apiProcessConnector.runGet(apiid, null, null);

        ProcessConnectorItem expectedItem = toProcessConnectorItem(defaultConnector, processWithConnector.getId());
        assertTrue(areEquals(expectedItem, processConnectorItem));
    }

    private APIID anApiId(long processId, String connectorId, String connectorVersion) {
        APIID apiid = APIID.makeAPIID(String.valueOf(processId), connectorId, connectorVersion);
        apiid.setItemDefinition(ProcessConnectorDefinition.get());
        return apiid;
    }

    private ProcessConnectorItem toProcessConnectorItem(TestProcessConnector testProcessConnector, long processId) {
        ProcessConnectorItem item = new ProcessConnectorItem();
        item.setName(testProcessConnector.getId());
        item.setVersion(testProcessConnector.getVersion());
        item.setProcessId(processId);
        item.setImplementationName(testProcessConnector.getImplementationId());
        item.setImplementationVersion(testProcessConnector.getVersion());
        item.setClassname(testProcessConnector.getImplementationClassname());
        return item;
    }
}
