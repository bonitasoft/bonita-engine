/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.tenant.restart;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.log.technical.TechnicalLoggerSLF4JImpl;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class RestartProcessHandlerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private ProcessInstanceService processInstanceService;
    @Mock
    private ExecuteProcesses executeProcesses;
    private RestartProcessHandler restartProcessHandler;

    @Before
    public void before() throws Exception {
        restartProcessHandler = new RestartProcessHandler(new TechnicalLoggerSLF4JImpl(), processInstanceService,
                executeProcesses);
    }

    @Test
    public void should_execute_processes_retrieved_from_database() throws Exception {
        doReturn(asList(processWithId(1L), processWithId(2L), processWithId(3L)))
                .when(processInstanceService).getProcessInstancesInStates(any(), any());

        restartProcessHandler.beforeServicesStart();
        restartProcessHandler.afterServicesStart();

        verify(executeProcesses).execute(asList(1L, 2L, 3L));

    }

    protected SProcessInstance processWithId(long id) {
        SProcessInstance sProcessInstance = new SProcessInstance();
        sProcessInstance.setId(id);
        return sProcessInstance;
    }
}
