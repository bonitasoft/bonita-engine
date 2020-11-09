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

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.tenant.restart.ElementToRecover.Type.FLOWNODE;
import static org.bonitasoft.engine.tenant.restart.ElementToRecover.Type.PROCESS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.FlowNodeInstanceService;
import org.bonitasoft.engine.core.process.instance.api.ProcessInstanceService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecoveryServiceTest {

    @Mock
    private FlowNodeInstanceService flowNodeInstanceService;
    @Mock
    private ProcessInstanceService processInstanceService;
    @Mock(lenient = true)
    private UserTransactionService userTransactionService;
    @Mock
    private FlowNodesRecover flowNodesRecover;
    @Mock
    private ProcessesRecover processesRecover;
    @Mock
    private SessionAccessor sessionAccessor;

    @InjectMocks
    private RecoveryService recoveryService;

    @BeforeEach
    public void before() throws Exception {
        recoveryService.setReadBatchSize(2);
        recoveryService.setBatchRestartSize(1000);
        recoveryService.setConsiderElementsOlderThan(Duration.ofMillis(1000));
        when(userTransactionService.executeInTransaction(any()))
                .thenAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call());

    }

    @Test
    void should_return_list_of_elements_to_recover() throws Exception {
        doReturn(asList(1L, 2L)).doReturn(emptyList()).when(flowNodeInstanceService)
                .getFlowNodeInstanceIdsToRecover(eq(Duration.ZERO), any());
        doReturn(asList(1L, 2L)).doReturn(emptyList())
                .when(processInstanceService).getProcessInstanceIdsToRecover(eq(Duration.ZERO), any());

        List<ElementToRecover> allElementsToRecover = recoveryService
                .getAllElementsToRecover(Duration.ZERO);

        assertThat(allElementsToRecover).containsExactlyInAnyOrder(
                elementToRecover(1L, PROCESS), elementToRecover(2L, PROCESS),
                elementToRecover(1L, FLOWNODE), elementToRecover(2L, FLOWNODE));
    }

    @Test
    void should_return_all_elements_even_if_over_page_size() throws SBonitaException {
        doReturn(asList(1L, 2L))
                .doReturn(singletonList(4L))
                .when(processInstanceService).getProcessInstanceIdsToRecover(eq(Duration.ZERO), any());
        doReturn(asList(7L, 9L))
                .doReturn(singletonList(13L))
                .when(flowNodeInstanceService).getFlowNodeInstanceIdsToRecover(eq(Duration.ZERO), any());

        List<ElementToRecover> allElementsToRecover = recoveryService
                .getAllElementsToRecover(Duration.ZERO);

        assertThat(allElementsToRecover).containsExactlyInAnyOrder(
                elementToRecover(1L, PROCESS), elementToRecover(2L, PROCESS), elementToRecover(4L, PROCESS),
                elementToRecover(7L, FLOWNODE), elementToRecover(9L, FLOWNODE), elementToRecover(13L, FLOWNODE));
    }

    @Test
    void should_recover_elements_provided() throws Exception {
        recoveryService.recover(asList(
                elementToRecover(1L, PROCESS),
                elementToRecover(2L, PROCESS),
                elementToRecover(4L, PROCESS),
                elementToRecover(7L, FLOWNODE),
                elementToRecover(9L, FLOWNODE),
                elementToRecover(13L, FLOWNODE)));

        verify(flowNodesRecover).execute(any(), eq(asList(7L, 9L, 13L)));
        verify(processesRecover).execute(any(), eq(asList(1L, 2L, 4L)));
    }

    @Test
    void should_recover_all_elements_older_than() throws Exception {
        Duration considerElementsOlderThan = Duration.ofSeconds(10);
        recoveryService.setConsiderElementsOlderThan(considerElementsOlderThan);

        doReturn(asList(1L, 2L))
                .doReturn(singletonList(4L))
                .when(processInstanceService).getProcessInstanceIdsToRecover(eq(considerElementsOlderThan), any());
        doReturn(asList(7L, 9L))
                .doReturn(singletonList(13L))
                .when(flowNodeInstanceService).getFlowNodeInstanceIdsToRecover(eq(considerElementsOlderThan), any());

        recoveryService.recoverAllElements();

        verify(flowNodesRecover).execute(any(), eq(asList(7L, 9L, 13L)));
        verify(processesRecover).execute(any(), eq(asList(1L, 2L, 4L)));
    }

    private ElementToRecover elementToRecover(long l, ElementToRecover.Type process) {
        return ElementToRecover.builder().id(l).type(process).build();
    }

    @Test
    void should_restart_flownodes_in_batch() throws Exception {
        recoveryService.setBatchRestartSize(2);

        recoveryService.recover(asList(
                elementToRecover(1L, FLOWNODE),
                elementToRecover(2L, FLOWNODE),
                elementToRecover(3L, FLOWNODE),
                elementToRecover(4L, FLOWNODE),
                elementToRecover(5L, FLOWNODE)));

        verify(flowNodesRecover).execute(any(), eq(asList(1L, 2L)));
        verify(flowNodesRecover).execute(any(), eq(asList(3L, 4L)));
        verify(flowNodesRecover).execute(any(), eq(singletonList(5L)));
        verify(userTransactionService, times(3)).executeInTransaction(any());
    }

    @Test
    void should_restart_processes_in_batch() throws Exception {
        recoveryService.setBatchRestartSize(2);

        recoveryService.recover(asList(
                elementToRecover(1L, PROCESS),
                elementToRecover(2L, PROCESS),
                elementToRecover(3L, PROCESS),
                elementToRecover(4L, PROCESS),
                elementToRecover(5L, PROCESS)));

        verify(processesRecover).execute(any(), eq(asList(1L, 2L)));
        verify(processesRecover).execute(any(), eq(asList(3L, 4L)));
        verify(processesRecover).execute(any(), eq(singletonList(5L)));
        verify(userTransactionService, times(3)).executeInTransaction(any());
    }

    @Test
    void should_continue_to_restart_process_even_if_one_batch_failed() throws Exception {
        doThrow(new UnsupportedOperationException("current batch failed, sorry ¯\\_(ツ)_/¯"))
                .doAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call())
                .when(userTransactionService).executeInTransaction(any());
        recoveryService.setBatchRestartSize(2);

        recoveryService.recover(asList(
                elementToRecover(1L, PROCESS),
                elementToRecover(2L, PROCESS),
                elementToRecover(3L, PROCESS),
                elementToRecover(4L, PROCESS),
                elementToRecover(5L, PROCESS)));

        verify(processesRecover, never()).execute(any(), eq(asList(1L, 2L)));// transaction fail there
        verify(processesRecover).execute(any(), eq(asList(3L, 4L)));
        verify(processesRecover).execute(any(), eq(singletonList(5L)));
        verify(userTransactionService, times(3)).executeInTransaction(any());
    }

    @Test
    void should_continue_to_restart_flow_node_even_if_one_batch_failed() throws Exception {
        doThrow(new UnsupportedOperationException("current batch failed, sorry ¯\\_(ツ)_/¯"))
                .doAnswer(invocationOnMock -> ((Callable) invocationOnMock.getArgument(0)).call())
                .when(userTransactionService).executeInTransaction(any());
        recoveryService.setBatchRestartSize(2);

        recoveryService.recover(asList(
                elementToRecover(1L, FLOWNODE),
                elementToRecover(2L, FLOWNODE),
                elementToRecover(3L, FLOWNODE),
                elementToRecover(4L, FLOWNODE),
                elementToRecover(5L, FLOWNODE)));

        verify(flowNodesRecover, never()).execute(any(), eq(asList(1L, 2L)));// transaction fail there
        verify(flowNodesRecover).execute(any(), eq(asList(3L, 4L)));
        verify(flowNodesRecover).execute(any(), eq(singletonList(5L)));
        verify(userTransactionService, times(3)).executeInTransaction(any());
    }
}
