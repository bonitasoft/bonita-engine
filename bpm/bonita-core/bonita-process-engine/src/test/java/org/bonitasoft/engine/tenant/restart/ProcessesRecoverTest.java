/**
 * Copyright (C) 2020 Bonitasoft S.A.
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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bpm.process.ProcessInstanceState.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.bpm.connector.ConnectorEvent;
import org.bonitasoft.engine.bpm.process.ProcessInstanceState;
import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SActivityReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.SProcessInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.states.FlowNodeState;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.state.FailedActivityState;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ProcessesRecoverTest {

    public static final long CALLER_ID = 55L;
    public static final long PROCESS_INSTANCE_ID = 42L;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private FlowNodeStateManager flowNodeStateManager;
    @Mock
    private WorkService workService;
    @Mock
    private ActivityInstanceService activityInstanceService;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private ProcessInstanceService processInstanceService;
    @Mock
    private ProcessExecutor processExecutor;
    @Mock
    private UserTransactionService userTransactionService;
    private ProcessesRecover processesRecover;

    private RecoveryMonitor recoveryMonitor;
    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Before
    public void setUp() throws Exception {
        recoveryMonitor = new RecoveryMonitor();
        recoveryMonitor.startNow(100);
        processesRecover = new ProcessesRecover(workService, activityInstanceService,
                processDefinitionService, processInstanceService, processExecutor, new BPMWorkFactory());
        doAnswer(args -> ((Callable) args.getArgument(0)).call()).when(userTransactionService)
                .executeInTransaction(any());
    }

    @Test
    public void should_not_execute_process_with_no_caller_id() throws Exception {
        havingProcessInstance(COMPLETED, -1);

        processesRecover.execute(recoveryMonitor, singletonList(PROCESS_INSTANCE_ID));

        // as it can never happen, because when a process instance goes into COMPLETED state, it is archived
        // in the same transaction
        verifyNoMoreInteractions(workService);
    }

    @Test
    public void should_not_execute_a_process_called_by_a_failed_call_activity() throws Exception {
        havingCallActivity(FlowNodeState.ID_ACTIVITY_FAILED, CALLER_ID);
        havingProcessInstance(COMPLETED, CALLER_ID);
        when(flowNodeStateManager.getState(FlowNodeState.ID_ACTIVITY_FAILED)).thenReturn(new FailedActivityState());

        processesRecover.execute(recoveryMonitor, singletonList(PROCESS_INSTANCE_ID));

        verifyNoMoreInteractions(workService);
        assertThat(systemOutRule.getLog().toLowerCase()).doesNotContain("error");
    }

    @Test
    public void should_execute_process_called_by_a_call_activity() throws Exception {
        havingCallActivity(FlowNodeState.ID_ACTIVITY_EXECUTING, CALLER_ID);
        havingProcessInstance(COMPLETED, CALLER_ID);
        when(flowNodeStateManager.getState(FlowNodeState.ID_ACTIVITY_FAILED)).thenReturn(new FailedActivityState());

        processesRecover.execute(recoveryMonitor, singletonList(PROCESS_INSTANCE_ID));

        verify(workService)
                .registerWork(argThat(workDescriptor -> workDescriptor.getType().equals("EXECUTE_FLOWNODE") &&
                        workDescriptor.getParameter("flowNodeInstanceId").equals(CALLER_ID)));
    }

    @Test
    public void should_execute_on_enter_connector_of_process_instance() throws Exception {
        SProcessInstance processInstance = havingProcessInstance(INITIALIZING, -1);

        processesRecover.execute(recoveryMonitor, singletonList(PROCESS_INSTANCE_ID));

        verify(processExecutor).registerConnectorsToExecute(any(), eq(processInstance), eq(ConnectorEvent.ON_ENTER),
                any());
    }

    @Test
    public void should_execute_on_finish_connector_of_process_instance() throws Exception {
        SProcessInstance processInstance = havingProcessInstance(COMPLETING, -1);

        processesRecover.execute(recoveryMonitor, singletonList(PROCESS_INSTANCE_ID));

        verify(processExecutor).registerConnectorsToExecute(any(), eq(processInstance), eq(ConnectorEvent.ON_FINISH),
                any());
    }

    protected void havingCallActivity(int stateId, long id)
            throws SActivityInstanceNotFoundException, SActivityReadException {
        SCallActivityInstance caller = new SCallActivityInstance();
        caller.setStateId(stateId);
        caller.setId(id);
        doReturn(caller).when(activityInstanceService).getActivityInstance(id);
    }

    protected SProcessInstance havingProcessInstance(ProcessInstanceState state, long callerId)
            throws SProcessInstanceNotFoundException, SProcessInstanceReadException {
        SProcessInstance processInstance = new SProcessInstance();
        processInstance.setStateId(state.getId());
        processInstance.setCallerId(callerId);
        processInstance.setId(PROCESS_INSTANCE_ID);
        doReturn(processInstance).when(processInstanceService).getProcessInstance(PROCESS_INSTANCE_ID);
        return processInstance;
    }
}
