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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.bonitasoft.engine.api.ProcessAPI;
import org.bonitasoft.engine.bpm.process.ProcessActivationException;
import org.bonitasoft.engine.bpm.process.ProcessDefinitionNotFoundException;
import org.bonitasoft.engine.bpm.process.ProcessExecutionException;
import org.bonitasoft.web.rest.server.APITestWithMock;
import org.bonitasoft.web.toolkit.client.common.exception.api.APIException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class CaseEngineClientTest extends APITestWithMock {

    @Mock
    private ProcessAPI processAPI;

    private CaseEngineClient caseEngineClient;

    @Before
    public void setUp() {
        initMocks(this);
        caseEngineClient = new CaseEngineClient(processAPI);
    }

    private Map<String, Serializable> someVariables() {
        final Map<String, Serializable> map = new HashMap<>();
        map.put("variable", 1L);
        return map;
    }

    @Test
    public void a_process_can_be_started_without_variables() throws Exception {
        final long expectedProcessId = 1L;
        final long userId = 1L;

        caseEngineClient.start(userId, expectedProcessId);

        verify(processAPI).startProcess(userId, expectedProcessId);
    }

    @Test
    public void a_process_can_be_started_with_variables() throws Exception {
        final long expectedProcessId = 1L;
        final long userId = 1L;
        final Map<String, Serializable> variables = someVariables();

        caseEngineClient.start(userId, expectedProcessId, variables);

        verify(processAPI).startProcess(userId, expectedProcessId, variables);
    }

    @Test(expected = APIException.class)
    public void cant_create_case_if_process_definition_is_not_found() throws Exception {
        when(processAPI.startProcess(anyLong())).thenThrow(new ProcessDefinitionNotFoundException(""));

        caseEngineClient.start(-1L, 1L);
    }

    @Test(expected = APIException.class)
    public void cant_create_case_if_process_is_not_activated() throws Exception {
        when(processAPI.startProcess(anyLong())).thenThrow(new ProcessActivationException(""));

        caseEngineClient.start(-1L, 1L);
    }

    @Test(expected = APIException.class)
    public void we_get_an_exception_if_process_fail_to_start() throws Exception {
        when(processAPI.startProcess(anyLong())).thenThrow(new ProcessExecutionException(""));

        caseEngineClient.start(-1L, 1L);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = APIException.class)
    public void cant_create_case_with_variables_if_process_definition_is_not_found() throws Exception {
        when(processAPI.startProcess(anyLong(), anyMap())).thenThrow(new ProcessDefinitionNotFoundException(""));

        caseEngineClient.start(-1L, 1L, someVariables());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = APIException.class)
    public void cant_create_case_with_variables_if_process_is_not_activated() throws Exception {
        when(processAPI.startProcess(anyLong(), anyMap())).thenThrow(new ProcessActivationException(""));

        caseEngineClient.start(-1L, 1L, someVariables());
    }

    @SuppressWarnings("unchecked")
    @Test(expected = APIException.class)
    public void we_get_an_exception_if_process_fail_to_start_with_variables() throws Exception {
        when(processAPI.startProcess(anyLong(), anyMap())).thenThrow(new ProcessExecutionException(""));

        caseEngineClient.start(-1L, 1L, someVariables());
    }

}
