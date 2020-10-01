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
package org.bonitasoft.engine.work;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.work.audit.WorkExecutionAuditor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Baptiste Mesta.
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkExecutorServiceImplTest {

    public static final int WORK_TERMINATION_TIMEOUT = 5000;
    @Mock
    private BonitaExecutorServiceFactory bonitaExecutorServiceFactory;
    @Mock
    private BonitaExecutorService bonitaExecutorService;
    @Mock
    private TechnicalLoggerService loggerService;
    @Mock
    private TechnicalLogger logger;
    private WorkDescriptor workDescriptor = WorkDescriptor.create("myWork");
    @Mock
    private BonitaWork bonitaWork;
    @Mock
    private WorkExecutionAuditor workExecutionAuditor;

    private WorkExecutorServiceImpl workExecutorService;

    @Before
    public void before() throws Exception {
        doReturn(bonitaExecutorService).when(bonitaExecutorServiceFactory).createExecutorService(any());
        doReturn(logger).when(loggerService).asLogger(any());
        workExecutorService = new WorkExecutorServiceImpl(bonitaExecutorServiceFactory, loggerService,
                WORK_TERMINATION_TIMEOUT, workExecutionAuditor);
        workExecutorService.start();
        doReturn(true).when(bonitaExecutorService).awaitTermination(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void should_submit_work_on_the_executor() throws Exception {

        workExecutorService.execute(workDescriptor);

        verify(bonitaExecutorService).submit(eq(workDescriptor));
    }

    @Test
    public void should_pause_shutdown_ThreadPool_and_clear_queue() throws InterruptedException, SBonitaException {
        final InOrder inOrder = inOrder(bonitaExecutorService);
        // given
        workExecutorService.start();

        // when
        workExecutorService.pause();

        // then
        inOrder.verify(bonitaExecutorService).shutdownAndEmptyQueue();
        inOrder.verify(bonitaExecutorService).clearAllQueues();
        inOrder.verify(bonitaExecutorService).awaitTermination(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void should_stop_shutdown_ThreadPool_and_not_clear_queue() throws InterruptedException {
        final InOrder inOrder = inOrder(bonitaExecutorService);
        // given
        workExecutorService.start();

        // when
        workExecutorService.stop();

        // then
        inOrder.verify(bonitaExecutorService).shutdownAndEmptyQueue();
        inOrder.verify(bonitaExecutorService).awaitTermination(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void pauseShouldStopWorkservice() throws SBonitaException {
        // given
        workExecutorService.start();

        // when
        workExecutorService.pause();

        // then
        assertThat(workExecutorService.isStopped()).as("WorkService should be deactivated").isTrue();
    }

    @Test
    public void resumeShouldAllowToExecuteWork() throws SBonitaException {
        // given
        workExecutorService.start();
        workExecutorService.pause();

        // when
        workExecutorService.resume();
        workExecutorService.execute(workDescriptor);

        // then
        verify(bonitaExecutorService, times(1)).submit(eq(workDescriptor));
    }

    @Test
    public void should_start_do_nothing_when_already_started() {
        // given
        workExecutorService.start();

        // when
        workExecutorService.start();

        // then: will only be started one time
        verify(bonitaExecutorServiceFactory, times(1)).createExecutorService(workExecutorService);
    }

    @Test
    public void should_stop_do_nothing_when_already_stopped() {
        // given
        workExecutorService.start();
        workExecutorService.stop();

        // when
        workExecutorService.stop();

        // then: will only be started one time
        verify(bonitaExecutorService, times(1)).shutdownAndEmptyQueue();
    }

    @Test(expected = SWorkException.class)
    public void should_pause_throw_exception_on_timeout() throws SWorkException, InterruptedException {
        // given
        workExecutorService.start();
        doReturn(false).when(bonitaExecutorService).awaitTermination(anyLong(), any(TimeUnit.class));

        // when
        workExecutorService.pause();

        // then: exception
    }

    @Test
    public void should_stop_do_not_throw_exception_on_timeout() throws InterruptedException {
        // given
        workExecutorService.start();
        doReturn(false).when(bonitaExecutorService).awaitTermination(anyLong(), any(TimeUnit.class));

        // when
        workExecutorService.stop();

        // then: will only be started one time
        verify(logger).warn(contains("Waited"));
    }

    @Test(expected = SWorkException.class)
    public void should_pause_throw_exception_on_interrupted() throws InterruptedException, SBonitaException {
        // given
        workExecutorService.start();
        doThrow(InterruptedException.class).when(bonitaExecutorService).awaitTermination(anyLong(),
                any(TimeUnit.class));

        // when
        workExecutorService.pause();

        // then: exception
    }

    @Test
    public void should_stop_do_not_throw_exception_on_interrupted() throws InterruptedException {
        // given
        workExecutorService.start();
        doThrow(InterruptedException.class).when(bonitaExecutorService).awaitTermination(anyLong(),
                any(TimeUnit.class));

        // when
        workExecutorService.stop();

        // then: will only be started one time

        verify(logger).warn(contains("Interrupted"), any(InterruptedException.class));
    }

    @Test
    public void checkStartStatus() {
        // when
        workExecutorService.start();

        // then
        assertThat(workExecutorService.isStopped()).isFalse();
    }

    @Test
    public void checkStopStatus() {
        // given
        workExecutorService.start();

        // when
        workExecutorService.stop();

        // then
        assertThat(workExecutorService.isStopped()).isTrue();

    }

    @Test
    public void should_reexecute_work_when_it_fails_because_of_lock_timeout() {
        workExecutorService.onFailure(workDescriptor, bonitaWork, Collections.emptyMap(),
                new LockTimeoutException("lock timeout"));

        verify(bonitaExecutorService).submit(eq(workDescriptor));
    }

    @Test
    public void should_warn_and_reexecute_work_when_it_fails_because_of_lock_exception() {
        workExecutorService.onFailure(workDescriptor, bonitaWork, Collections.emptyMap(),
                new LockException("lock timeout", new Exception()));

        verify(bonitaExecutorService).submit(eq(workDescriptor));
        verify(logger).warn(any(), any(Exception.class));
    }

    @Test
    public void should_log_on_success() {
        workExecutorService.onSuccess(workDescriptor);

        verify(logger).debug(eq("Completed work {}"), any(Object.class));
    }

    @Test
    public void should_await_specified_time_when_stopping_the_executor() throws Exception {

        workExecutorService.stop();

        verify(bonitaExecutorService).awaitTermination(WORK_TERMINATION_TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void should_ignored_work_because_of_precondition_not_verified() {
        workExecutorService.onFailure(workDescriptor, bonitaWork, Collections.emptyMap(),
                new SWorkPreconditionException("My precondition"));

        verifyNoMoreInteractions(bonitaExecutorService);
        verifyNoMoreInteractions(bonitaWork);
        verify(logger).warn(contains("Work was not executed because preconditions were not met,"), (Object) any(),
                (Object) any());
    }

}
