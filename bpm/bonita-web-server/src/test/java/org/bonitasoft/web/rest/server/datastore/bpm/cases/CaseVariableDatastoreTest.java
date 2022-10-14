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

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/**
 * @author Colin PUY
 */
public class CaseVariableDatastoreTest extends APITestWithMock {

    private CaseVariableDatastore datastore;

    @Mock
    private ProcessAPI processAPI;

    @Before
    public void initializeMocks() {
        initMocks(this);

        datastore = spy(new CaseVariableDatastore(null));
        doReturn(processAPI).when(datastore).getEngineProcessAPI();
    }

    @Test
    public void testUpdateVariableValue() throws Exception {
        long caseId = 1L;
        String name = "aName";
        String newValue = "newValue";

        datastore.updateVariableValue(caseId, name, String.class.getName(), newValue);

        verify(processAPI).updateProcessDataInstance(name, caseId, newValue);
    }

}
