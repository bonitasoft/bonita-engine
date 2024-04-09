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

import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_NAME;
import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_CONNECTOR_VERSION;
import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.ATTRIBUTE_PROCESS_ID;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessConnectorDependencyDatastore;
import org.bonitasoft.web.rest.server.framework.exception.APIFilterMandatoryException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

/**
 * @author Colin PUY
 */
public class APIProcessConnectorDependencyTest extends APITestWithMock {

    @Mock
    private ProcessConnectorDependencyDatastore datastore;

    @Spy
    private APIProcessConnectorDependency apiProcessConnectorDependency;

    @Before
    public void initializeMocks() {
        initMocks(this);

        doReturn(datastore).when(apiProcessConnectorDependency).defineDefaultDatastore();
    }

    private Map<String, String> buildFilters(String processId, String connectorName, String connectorVersion) {
        Map<String, String> filters = new HashMap<>();
        filters.put(ATTRIBUTE_PROCESS_ID, processId);
        filters.put(ATTRIBUTE_CONNECTOR_NAME, connectorName);
        filters.put(ATTRIBUTE_CONNECTOR_VERSION, connectorVersion);
        return filters;
    }

    @Test(expected = APIFilterMandatoryException.class)
    public void checkProcessIdIsMandatory() throws Exception {
        Map<String, String> filters = buildFilters(null, "aConnectorName", "aConnectorVersion");

        apiProcessConnectorDependency.checkMandatoryAttributes(filters);
    }

    @Test(expected = APIFilterMandatoryException.class)
    public void checkConnectorNameIsMandatory() throws Exception {
        Map<String, String> filters = buildFilters("1", "", "aConnectorVersion");

        apiProcessConnectorDependency.checkMandatoryAttributes(filters);
    }

    @Test(expected = APIFilterMandatoryException.class)
    public void checkConnectorVersionIsMandatory() throws Exception {
        Map<String, String> filters = buildFilters("1", "aConnectorName", "");

        apiProcessConnectorDependency.checkMandatoryAttributes(filters);
    }

    @Test
    public void checkMandatoryAttributesDontThrowExceptionIfAllAtributesAreSet() throws Exception {
        Map<String, String> filters = buildFilters("1", "aConnectorName", "aConnectorVersion");

        apiProcessConnectorDependency.checkMandatoryAttributes(filters);

        assertTrue("no exception thrown", true);
    }

    @Test(expected = APIFilterMandatoryException.class)
    public void searchCheckMandatoryAttributes() throws Exception {
        Map<String, String> filtersWithoutProcessId = buildFilters(null, "aConnectorName", "aConnectorVersion");

        apiProcessConnectorDependency.search(0, 10, null, null, filtersWithoutProcessId);
    }
}
