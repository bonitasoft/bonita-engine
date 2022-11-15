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
package org.bonitasoft.web.rest.server.api.bpm.cases;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ArchivedProcessInstance;
import org.bonitasoft.engine.session.APISession;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.rest.server.datastore.bpm.cases.ArchivedCaseDatastore;
import org.bonitasoft.web.toolkit.client.data.APIID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Nicolas TITH
 */
@RunWith(MockitoJUnitRunner.class)
public class APIArchivedCaseTest extends APITestWithMock {

    @InjectMocks
    private APIArchivedCase apiArchivedCase;

    @Mock
    private APISession apiSession;

    @Spy
    private final ArchivedCaseDatastore archivedCaseDatastore = new ArchivedCaseDatastore(apiSession);

    @Mock
    private ProcessAPI processAPI;

    @Mock
    private ArchivedProcessInstance archivedProcessInstance;

    @Before
    public void initializeMocks() throws Exception {
        initMocks(this);
        apiArchivedCase = spy(new APIArchivedCase());
        doReturn(archivedCaseDatastore).when(apiArchivedCase).defineDefaultDatastore();
        doReturn(processAPI).when(archivedCaseDatastore).getProcessApi();
        doReturn(archivedProcessInstance).when(processAPI).getArchivedProcessInstance(anyLong());
    }

    @Test
    public void deleteShouldDeleteSeveralItem() throws Exception {
        //given
        final List<APIID> idList = Arrays.asList(APIID.makeAPIID(1L), APIID.makeAPIID(2L), APIID.makeAPIID(3L));
        //when
        apiArchivedCase.delete(idList);

        //then
        verify(archivedCaseDatastore).delete(idList);
    }

}
