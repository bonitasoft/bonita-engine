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

import static java.util.Collections.EMPTY_MAP;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.web.rest.model.bpm.process.ProcessConnectorItem;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.datastore.bpm.process.ProcessConnectorDatastore;
import org.bonitasoft.web.rest.server.framework.exception.APIFilterMandatoryException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Colin PUY
 */
@SuppressWarnings("unchecked")
public class APIProcessConnectorTest extends APITestWithMock {

    @Mock
    private ProcessConnectorDatastore processConnectorDatastore;

    private APIProcessConnector apiProcessConnector;

    @Before
    public void initializeMocks() {
        initMocks(this);

        this.apiProcessConnector = spy(new APIProcessConnector());

        doReturn(this.processConnectorDatastore).when(this.apiProcessConnector).defineDefaultDatastore();
    }

    @Test(expected = APIFilterMandatoryException.class)
    public void whenSearchingFilterProcessIdIsMandatory() throws Exception {
        this.apiProcessConnector.search(0, 10, null, null, EMPTY_MAP);
    }

    @Test
    public void searchIsDoneWhenProcessIdFilterIsSet() throws Exception {
        final Map<String, String> filters = new HashMap<>();
        filters.put(ProcessConnectorItem.ATTRIBUTE_PROCESS_ID, "1");

        this.apiProcessConnector.search(0, 10, null, null, filters);

        verify(this.processConnectorDatastore).search(0, 10, null, null, filters);
    }
}
