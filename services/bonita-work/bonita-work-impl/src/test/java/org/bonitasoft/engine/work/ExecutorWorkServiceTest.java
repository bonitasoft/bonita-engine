/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

public class ExecutorWorkServiceTest {

    private ExecutorWorkService workService;

    private TransactionService transactionService;

    private WorkSynchronizationFactory workSynchronizationFactory;

    private TechnicalLoggerService loggerService;

    private SessionAccessor sessionAccessor;

    private BonitaExecutorServiceFactory bonitaExecutorServiceFactory;

    private AbstractWorkSynchronization abstractWorkSynchronization;

    private BonitaExecutorService executorService;

    private Queue<Runnable> queue;

    @SuppressWarnings("unchecked")
    @Before
    public void before() throws Exception {
        transactionService = mock(TransactionService.class);
        workSynchronizationFactory = mock(WorkSynchronizationFactory.class);
        loggerService = mock(TechnicalLoggerService.class);
        sessionAccessor = mock(SessionAccessor.class);
        bonitaExecutorServiceFactory = mock(BonitaExecutorServiceFactory.class);
        abstractWorkSynchronization = mock(AbstractWorkSynchronization.class);
        executorService = mock(BonitaExecutorService.class);
        queue = mock(Queue.class);

        doReturn(abstractWorkSynchronization).when(workSynchronizationFactory).getWorkSynchronization(any(BonitaExecutorService.class),
                any(TechnicalLoggerService.class), any(SessionAccessor.class), any(WorkService.class));
        doReturn(1L).when(sessionAccessor).getTenantId();
        doReturn(executorService).when(bonitaExecutorServiceFactory).createExecutorService();
        doReturn(false).when(executorService).isShutdown();
        doReturn(true).when(executorService).awaitTermination(anyLong(), any(TimeUnit.class));

        workService = spy(new ExecutorWorkService(transactionService, workSynchronizationFactory, loggerService, sessionAccessor, bonitaExecutorServiceFactory,
                30));
    }

    @Test
    public void pauseShouldStopWorkservice() throws SBonitaException {
        // given
        workService.start();

        // when
        workService.pause();

        // then
        assertThat(workService.isStopped()).as("WorkService should be deactivated").isTrue();

    }

    @Test
    public void should_pause_shutdown_ThreadPool_and_clear_queue() throws InterruptedException, SBonitaException {
        final InOrder inOrder = inOrder(executorService, workService, queue);
        // given
        workService.start();

        // when
        workService.pause();

        // then
        inOrder.verify(executorService).shutdownAndEmptyQueue();
        inOrder.verify(executorService).clearAllQueues();
        inOrder.verify(executorService).awaitTermination(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void should_stop_shutdown_ThreadPool_and_not_clear_queue() throws InterruptedException {
        final InOrder inOrder = inOrder(executorService, workService, queue);
        // given
        workService.start();

        // when
        workService.stop();

        // then
        inOrder.verify(executorService).shutdownAndEmptyQueue();
        inOrder.verify(queue, never()).clear();
        inOrder.verify(executorService).awaitTermination(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void pauseShouldNotAllowToRegisterWork() throws SBonitaException {
        // given
        workService.start();
        workService.pause();

        // when
        workService.registerWork(createBonitaWork());

        // then
        verify(abstractWorkSynchronization, times(0)).addWork(createBonitaWork());

    }

    @Test
    public void pauseShouldNotAllowToExecuteWork() throws SBonitaException {
        // given
        workService.start();

        // when
        workService.pause();
        workService.executeWork(createBonitaWork());

        // then
        verify(loggerService, times(1)).log((Class<?>) any(), eq(TechnicalLogSeverity.WARNING), anyString());
        verify(executorService, times(0)).submit(any(Runnable.class));
    }

    @Test
    public void resumeShouldDeactivateWorkservice() throws SBonitaException {
        // given
        workService.start();
        workService.pause();

        // when
        workService.resume();

        // then
        assertThat(workService.isStopped()).as("WorkService should not be stopped").isFalse();

    }

    @Test
    public void resumeShouldAllowToRegisterWork() throws SBonitaException {
        // given
        workService.start();
        workService.pause();
        workService.resume();

        // when
        workService.registerWork(createBonitaWork());

        // then
        verify(abstractWorkSynchronization, times(0)).addWork(createBonitaWork());
    }

    @Test
    public void resumeShouldAllowToExecuteWork() throws SBonitaException {
        // given
        workService.start();
        workService.pause();

        // when
        workService.resume();
        workService.executeWork(createBonitaWork());

        // then
        verify(executorService, times(1)).submit(any(Runnable.class));
    }

    @Test
    public void checkStartStatus() {
        // when
        workService.start();

        // then
        assertThat(workService.isStopped()).isFalse();
    }

    @Test
    public void checkStopStatus() {
        // given
        workService.start();

        // when
        workService.stop();

        // then
        assertThat(workService.isStopped()).isTrue();

    }

    @Test(expected = SWorkRegisterException.class)
    public void executeWorkShouldThrowExceptionWhenTenantIdNotSet() throws SWorkRegisterException, STenantIdNotSetException {
        // given
        workService.start();
        doThrow(STenantIdNotSetException.class).when(sessionAccessor).getTenantId();
        // when
        workService.executeWork(createBonitaWork());

        // then: exception

    }

    private BonitaWork createBonitaWork() {
        final BonitaWork work = new BonitaWork() {

            private static final long serialVersionUID = 1L;

            @Override
            public void work(final Map<String, Object> context) {
            }

            @Override
            public void handleFailure(final Exception e, final Map<String, Object> context) {

            }

            @Override
            public String getDescription() {
                return "fake bonita work";
            }
        };
        return work;
    }

    @Test
    public void should_start_do_nothing_when_already_started() {
        // given
        workService.start();

        // when
        workService.start();

        // then: will only be started one time
        verify(bonitaExecutorServiceFactory, times(1)).createExecutorService();
    }

    @Test
    public void should_stop_do_nothing_when_already_stopped() {
        // given
        workService.start();
        workService.stop();

        // when
        workService.stop();

        // then: will only be started one time
        verify(executorService, times(1)).shutdownAndEmptyQueue();
    }

    @Test
    public void should_isStopped_return_true_when_only_executor_is_shutdown() {
        // given
        doReturn(true).when(executorService).isShutdown();

        // then
        assertThat(workService.isStopped()).as("Should be stopped if only executor service is shutdown").isTrue();
    }

    @Test(expected = SWorkException.class)
    public void should_pause_throw_exception_on_timeout() throws SWorkException, InterruptedException {
        // given
        workService.start();
        doReturn(false).when(executorService).awaitTermination(anyLong(), any(TimeUnit.class));

        // when
        workService.pause();

        // then: exception
    }

    @Test
    public void should_stop_do_not_throw_exception_on_timeout() throws InterruptedException {
        // given
        workService.start();
        doReturn(false).when(executorService).awaitTermination(anyLong(), any(TimeUnit.class));

        // when
        workService.stop();

        // then: will only be started one time
        verify(loggerService, times(1)).log(any(Class.class), eq(TechnicalLogSeverity.WARNING), contains("Waited"));
    }

    @Test(expected = SWorkException.class)
    public void should_pause_throw_exception_on_interrupted() throws InterruptedException, SBonitaException {
        // given
        workService.start();
        doThrow(InterruptedException.class).when(executorService).awaitTermination(anyLong(), any(TimeUnit.class));

        // when
        workService.pause();

        // then: exception
    }

    @Test
    public void should_stop_do_not_throw_exception_on_interrupted() throws InterruptedException {
        // given
        workService.start();
        doThrow(InterruptedException.class).when(executorService).awaitTermination(anyLong(), any(TimeUnit.class));

        // when
        workService.stop();

        // then: will only be started one time
        verify(loggerService, times(1)).log(any(Class.class), eq(TechnicalLogSeverity.WARNING), contains("Interrupted"), any(InterruptedException.class));
    }

    @Test
    public void registerWorkAddTheWorkToTheTransactionSynchronization() throws SBonitaException {
        // given
        workService.start();

        // when
        final BonitaWork bonitaWork = createBonitaWork();
        workService.registerWork(bonitaWork);

        // then
        verify(abstractWorkSynchronization).addWork(bonitaWork);
    }

}
