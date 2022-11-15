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
package org.bonitasoft.web.rest.server.datastore.bpm.process;

import static java.util.Arrays.*;
import static junit.framework.Assert.*;
import static org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.connector.ConnectorImplementationDescriptor;
import org.bonitasoft.engine.bpm.connector.ConnectorNotFoundException;
import org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorDependencyItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.framework.search.ItemSearchResult;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Colin PUY
 */
public class ProcessConnectorDependencyDatastoreTest extends APITestWithMock {

    @Mock
    private ProcessAPI processAPI;

    private ProcessConnectorDependencyDatastore datastore;

    @Before
    public void initializeMocks() {
        initMocks(this);

        this.datastore = spy(new ProcessConnectorDependencyDatastore(null));

        doReturn(this.processAPI).when(this.datastore).getProcessAPI();
    }

    private Map<String, String> buildFilters(final Long processId, final String connectorName,
            final String connectorVersion) {
        final Map<String, String> filters = new HashMap<>();
        filters.put(ATTRIBUTE_PROCESS_ID, String.valueOf(processId));
        filters.put(ATTRIBUTE_CONNECTOR_NAME, connectorName);
        filters.put(ATTRIBUTE_CONNECTOR_VERSION, connectorVersion);
        return filters;
    }

    @Test
    public void searchReturnAnEmptyResultIfNoConnectorImplementationIsFound() throws Exception {
        when(this.processAPI.getConnectorImplementation(anyLong(), anyString(), anyString())).thenReturn(null);
        final Map<String, String> filters = buildFilters(1L, "aConnectorName", "1");

        final ItemSearchResult<ProcessConnectorDependencyItem> searchResult = this.datastore.search(0, 10, null, null,
                filters);

        assertTrue(searchResult.getResults().isEmpty());
        assertEquals(0L, searchResult.getTotal());
    }

    @Test(expected = APIException.class)
    @SuppressWarnings("unchecked")
    public void searchThrowExceptionIfProcessIdIsUnknown() throws Exception {
        when(this.processAPI.getConnectorImplementation(anyLong(), anyString(), anyString())).thenThrow(
                new ConnectorNotFoundException(new NullPointerException()));
        final Map<String, String> filters = buildFilters(1L, "aConnectorName", "1");

        this.datastore.search(0, 10, null, null, filters);
    }

    @Test
    public void searchCanBePaginated() throws Exception {
        final ConnectorImplementationDescriptor connectorWith3Dependencies = new ConnectorImplementationDescriptor(
                "implementationClassName", "connectorId", "connectorVersion", "1", "definitionVersion",
                asList("dependency1", "dependency2", "dependency3"));
        when(this.processAPI.getConnectorImplementation(1L, "connectorId", "connectorVersion"))
                .thenReturn(connectorWith3Dependencies);
        final Map<String, String> filters = buildFilters(1L, "connectorId", "connectorVersion");

        final ItemSearchResult<ProcessConnectorDependencyItem> searchResult = this.datastore.search(0, 2, null, null,
                filters);

        assertEquals(2L, searchResult.getResults().size());
        assertEquals(3L, searchResult.getTotal());
        assertEquals(searchResult.getResults().get(0).getFilename(), "dependency1");
        assertEquals(searchResult.getResults().get(1).getFilename(), "dependency2");
    }
}
