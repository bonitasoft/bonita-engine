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
import static java.util.stream.Collectors.toList;
import static java.util.stream.LongStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.bpm.process.ProcessInstanceState.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
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
import org.bonitasoft.engine.core.process.instance.api.states.State;
import org.bonitasoft.engine.core.process.instance.model.SCallActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.state.FailedActivityStateImpl;
import org.bonitasoft.engine.execution.state.FlowNodeStateManager;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerSLF4JImpl;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ExecuteProcessesTest {

    public static final int BATCH_RESTART_SIZE = 2;
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
    private ExecuteProcesses executeProcesses;

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Before
    public void setUp() throws Exception {
        executeProcesses = new ExecuteProcesses(workService, new TechnicalLoggerSLF4JImpl(), activityInstanceService,
                processDefinitionService, processInstanceService, processExecutor, flowNodeStateManager,
                new BPMWorkFactory(), userTransactionService,
                BATCH_RESTART_SIZE);
        doAnswer(args -> ((Callable) args.getArgument(0)).call()).when(userTransactionService)
                .executeInTransaction(any());
    }

    @Test
    public void should_not_execute_process_with_no_caller_id() throws Exception {
        havingProcessInstance(COMPLETED, -1);

        executeProcesses.execute(singletonList(PROCESS_INSTANCE_ID));

        // as it can never happen, because when a process instance goes into COMPLETED state, it is archived
        // in the same transaction
        verifyNoMoreInteractions(workService);
    }

    @Test
    public void should_not_execute_a_process_called_by_a_failed_call_activity() throws Exception {
        havingCallActivity(State.ID_ACTIVITY_FAILED, CALLER_ID);
        havingProcessInstance(COMPLETED, CALLER_ID);
        when(flowNodeStateManager.getFailedState()).thenReturn(new FailedActivityStateImpl());

        executeProcesses.execute(singletonList(PROCESS_INSTANCE_ID));

        verifyNoMoreInteractions(workService);
        assertThat(systemOutRule.getLog().toLowerCase()).doesNotContain("error");
    }

    @Test
    public void should_execute_process_called_by_a_call_activity() throws Exception {
        havingCallActivity(State.ID_ACTIVITY_EXECUTING, CALLER_ID);
        havingProcessInstance(COMPLETED, CALLER_ID);
        when(flowNodeStateManager.getFailedState()).thenReturn(new FailedActivityStateImpl());

        executeProcesses.execute(singletonList(PROCESS_INSTANCE_ID));

        verify(workService)
                .registerWork(argThat(workDescriptor -> workDescriptor.getType().equals("EXECUTE_FLOWNODE") &&
                        workDescriptor.getParameter("flowNodeInstanceId").equals(CALLER_ID)));
    }

    @Test
    public void should_execute_on_enter_connector_of_process_instance() throws Exception {
        SProcessInstance processInstance = havingProcessInstance(INITIALIZING, -1);

        executeProcesses.execute(singletonList(PROCESS_INSTANCE_ID));

        verify(processExecutor).registerConnectorsToExecute(any(), eq(processInstance), eq(ConnectorEvent.ON_ENTER),
                any());
    }

    @Test
    public void should_execute_on_finish_connector_of_process_instance() throws Exception {
        SProcessInstance processInstance = havingProcessInstance(COMPLETING, -1);

        executeProcesses.execute(singletonList(PROCESS_INSTANCE_ID));

        verify(processExecutor).registerConnectorsToExecute(any(), eq(processInstance), eq(ConnectorEvent.ON_FINISH),
                any());
    }

    @Test
    public void should_a_batch_fail_to_commit_continue_with_subsequent_batches() throws Exception {
        SProcessInstance processInstance = havingProcessInstance(COMPLETING, -1);
        // given: a transaction commit fails but subsequent commits are ok
        doThrow(new Exception("Error during commit"))
                .doAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call())
                .when(userTransactionService).executeInTransaction(any());
        // we have more than 2*BATCH_RESTART_SIZE ids
        List<Long> ids = range(0, BATCH_RESTART_SIZE * 2 + 1).map((t) -> processInstance.getId()).boxed()
                .collect(toList());

        // when
        executeProcesses.execute(ids);

        // then
        verify(userTransactionService, times(3)).executeInTransaction(any());
        assertThat(systemOutRule.getLog())
                .containsOnlyOnce("Exception")
                .containsOnlyOnce("Some processes failed to recover");
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
