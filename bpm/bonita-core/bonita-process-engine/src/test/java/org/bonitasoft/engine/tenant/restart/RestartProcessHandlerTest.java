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

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.LongStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.bonitasoft.engine.core.process.definition.ProcessDefinitionService;
import org.bonitasoft.engine.core.process.instance.api.ActivityInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.core.process.instance.model.SActivityInstance;
import org.bonitasoft.engine.core.process.instance.model.SProcessInstance;
import org.bonitasoft.engine.execution.FlowNodeStateManagerImpl;
import org.bonitasoft.engine.execution.ProcessExecutor;
import org.bonitasoft.engine.execution.state.FailedActivityStateImpl;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerSLF4JImpl;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.transaction.TransactionService;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class RestartProcessHandlerTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    TechnicalLoggerService logger;
    @Mock
    ActivityInstanceService activityInstanceService;
    @Mock
    WorkService workService;
    @Mock
    BPMWorkFactory workFactory;
    @Mock
    FlowNodeStateManagerImpl flowNodeStateManager;
    @Mock
    private TransactionService transactionService;
    @Mock
    private ProcessDefinitionService processDefinitionService;
    @Mock
    private ProcessInstanceService processInstanceService;
    @Mock
    private ProcessExecutor processExecutor;
    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog();
    private RestartProcessHandler restartProcessHandler;

    @Before
    public void before() throws Exception {
        restartProcessHandler = new RestartProcessHandler(7L, workService, activityInstanceService,
                processDefinitionService,
                processExecutor, workFactory, flowNodeStateManager, transactionService, new TechnicalLoggerSLF4JImpl(),
                processInstanceService);
    }

    @Test
    public void handleCompletionShouldDoNothingIfNoCallerId() throws Exception {

        restartProcessHandler.handleCompletion(mock(SProcessInstance.class), logger, activityInstanceService,
                workService, flowNodeStateManager, workFactory);
    }

    @Test
    public void handleCompletionShouldDoNothingIfCallActivityIsInFailedState() throws Exception {
        int failedStateId = 3;
        SActivityInstance callActivity = mock(SActivityInstance.class);
        when(callActivity.getStateId()).thenReturn(failedStateId);
        when(flowNodeStateManager.getFailedState()).thenReturn(new FailedActivityStateImpl());
        doReturn(callActivity).when(activityInstanceService).getActivityInstance(anyLong());
        SProcessInstance processInstance = mock(SProcessInstance.class);
        when(processInstance.getCallerId()).thenReturn(5L);
        restartProcessHandler.handleCompletion(processInstance, logger, activityInstanceService, workService,
                flowNodeStateManager, workFactory);
    }

    @Test
    public void handleCompletionShouldExecuteParentCallActivityIfItIsNOTInFailedState() throws Exception {
        int callActivityStateId = 8;
        SActivityInstance callActivity = mock(SActivityInstance.class);
        when(callActivity.getStateId()).thenReturn(callActivityStateId);
        when(flowNodeStateManager.getFailedState()).thenReturn(new FailedActivityStateImpl());
        doReturn(callActivity).when(activityInstanceService).getActivityInstance(anyLong());
        SProcessInstance processInstance = mock(SProcessInstance.class);
        when(processInstance.getCallerId()).thenReturn(5L);
        restartProcessHandler.handleCompletion(processInstance, logger, activityInstanceService, workService,
                flowNodeStateManager, workFactory);
    }

    @Test
    public void should_a_batch_fail_to_commit_continue_with_subsequent_batches() throws Exception {
        // given
        restartProcessHandler.setProcessInstancesByTenant(singletonMap(7L, range(1, 42).boxed().collect(toList())));
        when(transactionService.executeInTransaction(any()))
                .then(invocationOnMock -> {
                    // Necessary to call next on iterator
                    ((Callable) invocationOnMock.getArgument(0)).call();
                    throw new Exception("Error during commit");
                })
                .then(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call());
        systemOutRule.clearLog();

        // when
        restartProcessHandler.afterServicesStart();

        // then
        verify(transactionService, times(3)).executeInTransaction(any());
        assertThat(systemOutRule.getLog()).contains("Exception", "Some processes failed to recover");
    }
}
