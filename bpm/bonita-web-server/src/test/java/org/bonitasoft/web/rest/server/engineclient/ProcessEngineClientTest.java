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
package org.bonitasoft.web.rest.server.engineclient;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.bar.BusinessArchive;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessDeployException;
import org.bonitasoft.engine.bpm.process.V6FormDeployException;
import org.bonitasoft.engine.bpm.process.impl.internal.DesignProcessDefinitionImpl;
import org.bonitasoft.engine.exception.AlreadyExistsException;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

/**
 * @author Colin PUY
 */
public class ProcessEngineClientTest extends APITestWithMock {

    private static Integer BUNCH_SIZE = 10;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private ProcessAPI processAPI;
    private ProcessEngineClient processEngineClient;

    @Before
    public void setUp() {
        initMocks(this);
        processEngineClient = new ProcessEngineClient(processAPI);
    }

    @Test
    public void deleteArchivedProcessInstancesByBunch_delete_archived_processes_instances_by_bunch() throws Exception {
        when(processAPI.deleteArchivedProcessInstances(1L, 0, BUNCH_SIZE))
                .thenReturn(BUNCH_SIZE.longValue(), BUNCH_SIZE.longValue(), 0L);

        final List<Long> processList = new ArrayList<>();
        processList.add(1L);
        processEngineClient.deleteArchivedProcessInstancesByBunch(1L, 10, processList);

        verify(processAPI, times(3)).deleteArchivedProcessInstances(1L, 0, 10);
    }

    @Test
    public void deleteProcessInstancesByBunch_delete_archived_processes_instances_by_bunch() throws Exception {
        when(processAPI.deleteProcessInstances(1L, 0, BUNCH_SIZE))
                .thenReturn(BUNCH_SIZE.longValue(), BUNCH_SIZE.longValue(), 0L);

        final List<Long> processList = new ArrayList<>();
        processList.add(1L);
        processEngineClient.deleteProcessInstancesByBunch(1L, 10, processList);

        verify(processAPI, times(3)).deleteProcessInstances(1L, 0, 10);
    }

    @Test(expected = Exception.class)
    public void getProcessDataDefinitions_throw_exception_if_process_definition_is_not_found() throws Exception {
        when(processAPI.getProcessDataDefinitions(anyLong(), anyInt(), anyInt()))
                .thenThrow(new ProcessDefinitionNotFoundException(""));

        processEngineClient.getProcessDataDefinitions(1L);
    }

    @Test
    public void getProcessDataDefinitions_get_all_process_data_definition() throws Exception {
        final long expectedProcessId = 1L;

        processEngineClient.getProcessDataDefinitions(expectedProcessId);

        verify(processAPI).getProcessDataDefinitions(expectedProcessId, 0, Integer.MAX_VALUE);
    }

    @Test
    public void deploying_a_process_with_v6_forms_fails_with_the_right_message()
            throws AlreadyExistsException, ProcessDeployException {

        DesignProcessDefinitionImpl designProcessDefinition = new DesignProcessDefinitionImpl("processName", "1.0");
        BusinessArchive businessArchive = new BusinessArchive();
        businessArchive.setProcessDefinition(designProcessDefinition);

        when(processAPI.deploy((BusinessArchive) anyObject()))
                .thenThrow(new V6FormDeployException(new Exception("a cause")));

        expectedException.expect(APIException.class);
        processEngineClient.deploy(businessArchive);
    }

}
