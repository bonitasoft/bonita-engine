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
package org.bonitasoft.engine.work;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.work.ExceptionRetryabilityEvaluator.Retryability.NOT_RETRYABLE;
import static org.bonitasoft.engine.work.ExceptionRetryabilityEvaluator.Retryability.RETRYABLE;
import static org.bonitasoft.engine.work.ExceptionRetryabilityEvaluator.Retryability.UNCERTAIN_COMPLETION_OF_COMMIT;
import static org.bonitasoft.engine.work.RetryingWorkExecutorService.NUMBER_OF_WORKS_RETRIED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.commons.time.FixedEngineClock;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.work.audit.WorkExecutionAuditor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * @author Baptiste Mesta.
 */
public class RetryingWorkExecutorServiceTest {

    private static final int MAX_RETRY = 4;
    private static final int WORK_TERMINATION_TIMEOUT = 30;
    private static final int DELAY = 1000;
    private static final int DELAY_FACTOR = 2;
    public static final long TENANT_ID = 12L;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    private RetryingWorkExecutorService workExecutorService;
    @Mock
    private BonitaExecutorServiceFactory bonitaExecutorServiceFactory;
    @Mock(lenient = true)
    private BonitaExecutorService bonitaExecutorService;
    @Mock
    private TechnicalLoggerService technicalLoggerService;
    @Mock
    private TechnicalLogger logger;
    @Mock
    private BonitaWork bonitaWork;
    private final WorkDescriptor workDescriptor = WorkDescriptor.create("myWork");
    @Mock
    private WorkExecutionAuditor workExecutionAuditor;
    @Mock
    private IncidentService incidentService;
    @Mock(lenient = true)
    private ExceptionRetryabilityEvaluator retryabilityEvaluator;
    private final FixedEngineClock engineClock = new FixedEngineClock(Instant.EPOCH);

    private final MeterRegistry meterRegistry = new SimpleMeterRegistry(
            // So that micrometer updates its counters every 1 ms:
            k -> k.equals("simple.step") ? Duration.ofMillis(1).toString() : null,
            Clock.SYSTEM);

    @Before
    public void before() throws Exception {
        doReturn(logger).when(technicalLoggerService).asLogger(any());
        doReturn(bonitaExecutorService).when(bonitaExecutorServiceFactory).createExecutorService(any());
        doReturn(RETRYABLE).when(retryabilityEvaluator)
                .evaluateRetryability(argThat(t -> t instanceof SRetryableException));
        doReturn(NOT_RETRYABLE).when(retryabilityEvaluator)
                .evaluateRetryability(argThat(t -> !(t instanceof SRetryableException)));
        workExecutorService = new RetryingWorkExecutorService(
                bonitaExecutorServiceFactory,
                technicalLoggerService, engineClock, WORK_TERMINATION_TIMEOUT, MAX_RETRY, DELAY, DELAY_FACTOR,
                retryabilityEvaluator,
                workExecutionAuditor, meterRegistry, incidentService, TENANT_ID);
        doReturn(true).when(bonitaExecutorService).awaitTermination(anyLong(), any(TimeUnit.class));
        workExecutorService.start();
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
        verify(logger).warn(any(), any(), any(Exception.class));
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
        verify(logger).warn(contains("Work was not executed because preconditions were not met,"), (Object) any(),
                (Object) any());
    }

    @Test
    public void should_execute_work_normally() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");

        workExecutorService.execute(workDescriptor);

        verify(bonitaExecutorService).submit(eq(workDescriptor));
    }

    @Test
    public void should_reattempt_work_on_accepted_failure() throws Exception {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");

        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
                new SRetryableException(new Exception("rootCause")));

        verify(bonitaExecutorService).submit(eq(workDescriptor));
        verify(bonitaWork, never()).handleFailure(nullable(Exception.class), anyMap());
        assertThat(workDescriptor.getRetryCount()).isEqualTo(1);
    }

    @Test
    public void should_reattempt_work_on_when_issue_happened_during_commit() throws Exception {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");
        STransactionCommitException duringCommitException = new STransactionCommitException("exception during commit");
        doReturn(UNCERTAIN_COMPLETION_OF_COMMIT).when(retryabilityEvaluator)
                .evaluateRetryability(duringCommitException);

        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(), duringCommitException);

        verify(bonitaExecutorService).submit(eq(workDescriptor));
        verify(bonitaWork, never()).handleFailure(nullable(Exception.class), anyMap());
        assertThat(workDescriptor.getRetryCount()).isEqualTo(1);
    }

    @Test
    public void should_reset_retry_counter_on_non_retryable_failure() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("A_WORK");

        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
                new SRetryableException(new Exception("root_cause")));
        assertThat(getNumberOfRetries()).isEqualTo(1);

        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
                new SRetryableException(new Exception("other_cause")));
        assertThat(getNumberOfRetries())
                .as("Should not update counter for work that has already be counted").isEqualTo(1);

        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
                new Exception("not retry-able"));
        assertThat(getNumberOfRetries()).isEqualTo(0);
    }

    @Test
    public void should_reset_retry_counter_on_precondition_issue() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("A_WORK");

        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
                new SRetryableException(new Exception("root_cause")));
        assertThat(getNumberOfRetries()).isEqualTo(1);

        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
                new SWorkPreconditionException("precondition"));
        assertThat(getNumberOfRetries()).isEqualTo(0);
    }

    @Test
    public void should_reset_retry_counter_on_success() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("A_WORK");

        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
                new SRetryableException(new Exception("root_cause")));
        assertThat(getNumberOfRetries()).isEqualTo(1);

        workExecutorService.onSuccess(workDescriptor);
        assertThat(getNumberOfRetries()).isEqualTo(0);
    }

    @Test
    public void should_not_reattempt_work_on_other_failure() throws Exception {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");

        Exception rootCause = new Exception("rootCause");
        Map<String, Object> context = emptyMap();
        workExecutorService.onFailure(workDescriptor, bonitaWork, context,
                rootCause);

        verify(bonitaExecutorService, never()).submit(eq(workDescriptor));
        verify(bonitaWork).handleFailure(rootCause, context);
        assertThat(workDescriptor.getRetryCount()).isEqualTo(0);
    }

    @Test
    public void should_report_incident_when_we_are_unable_to_handle_the_failure_and_the_work_is_not_handled_by_the_recovery()
            throws Exception {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");
        doThrow(new Exception("HandleFailureError")).when(bonitaWork).handleFailure(any(), anyMap());

        Exception rootCause = new Exception("rootCause");
        Map<String, Object> context = emptyMap();
        workExecutorService.onFailure(workDescriptor, bonitaWork, context,
                rootCause);

        verify(bonitaWork).handleFailure(any(), anyMap());
        verify(incidentService).report(anyLong(), any());
    }

    @Test
    public void should_do_nothing_when_we_are_unable_to_handle_the_failure_and_the_work_is_handled_by_the_recovery()
            throws Exception {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");
        doThrow(new Exception("HandleFailureError")).when(bonitaWork).handleFailure(any(), anyMap());
        doReturn(true).when(bonitaWork).canBeRecoveredByTheRecoveryMechanism();

        Map<String, Object> context = emptyMap();
        workExecutorService.onFailure(workDescriptor, bonitaWork, context, new Exception("rootCause"));

        verify(bonitaWork).handleFailure(any(), anyMap());
        verify(incidentService, never()).report(anyLong(), any());
    }

    @Test
    public void should_not_reattempt_work_when_retry_count_is_exceeded() throws Exception {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");
        //max retry is 3
        workDescriptor.incrementRetryCount();
        workDescriptor.incrementRetryCount();
        workDescriptor.incrementRetryCount();
        workDescriptor.incrementRetryCount();

        Map<String, Object> context = emptyMap();
        SRetryableException exception = new SRetryableException(new Exception("rootCause"));
        workExecutorService.onFailure(workDescriptor, bonitaWork, context,
                exception);

        verify(bonitaExecutorService, never()).submit(eq(workDescriptor));
        verify(bonitaWork).handleFailure(exception, context);
    }

    @Test
    public void should_set_the_date_after_delay_on_work_when_retrying() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");
        SRetryableException exception = new SRetryableException(new Exception("rootCause"));
        Instant now = engineClock.now();

        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
                exception);

        verify(bonitaExecutorService).submit(eq(workDescriptor));
        assertThat(workDescriptor.getExecutionThreshold()).isAfterOrEqualTo(now.plusMillis(DELAY));
        assertThat(workDescriptor.getExecutionThreshold()).isBeforeOrEqualTo(now.plusMillis(DELAY + 1000));
    }

    @Test
    public void should_set_the_date_after_delay_and_factor_on_work_when_retrying_multiple_times() {
        SRetryableException exception = new SRetryableException(new Exception("rootCause"));
        Instant now = engineClock.now();

        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(), exception);
        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(), exception);
        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(), exception);

        verify(bonitaExecutorService, times(3)).submit(eq(workDescriptor));
        //date after delay is: delay * factor * factor because there is 3 retry
        assertThat(workDescriptor.getExecutionThreshold()).isAfterOrEqualTo(now.plusMillis(DELAY * DELAY_FACTOR * DELAY_FACTOR));
        assertThat(workDescriptor.getExecutionThreshold()).isBeforeOrEqualTo(now.plusMillis(DELAY * DELAY_FACTOR * DELAY_FACTOR + 1000));
    }

    @Test
    public void should_just_requeue_work_when_the_failure_is_a_work_lock_issue() throws Exception {
        Exception rootCause = new LockTimeoutException("rootCause");
        Map<String, Object> context = emptyMap();
        workExecutorService.onFailure(workDescriptor, bonitaWork, context,
                rootCause);

        verify(bonitaExecutorService).submit(eq(workDescriptor));
        verify(bonitaWork, never()).handleFailure(rootCause, context);
        assertThat(workDescriptor.getRetryCount()).isEqualTo(0);
    }

    @Test
    public void should_abandon_execution_when_failure_is_a_precondition_issue() throws Exception {
        Exception rootCause = new SWorkPreconditionException("precondition failed");
        Map<String, Object> context = emptyMap();
        workExecutorService.onFailure(workDescriptor, bonitaWork, context,
                rootCause);

        verifyZeroInteractions(bonitaExecutorService);
        verify(bonitaWork, never()).handleFailure(rootCause, emptyMap());
        assertThat(workDescriptor.getRetryCount()).isEqualTo(0);
    }

    @Test
    public void should_retry_PreconditionException_when_retryability_is_RETRYABLE() throws Exception {
        Exception rootCause = new SWorkPreconditionException("precondition failed");
        doReturn(RETRYABLE).when(retryabilityEvaluator).evaluateRetryability(rootCause);

        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(), rootCause);

        verify(bonitaExecutorService).submit(eq(workDescriptor));
        verify(bonitaWork, never()).handleFailure(rootCause, emptyMap());
        assertThat(workDescriptor.getRetryCount()).isEqualTo(1);
    }

    @Test
    public void should_retry_PreconditionException_when_retryability_is_UNCERTAIN_COMPLETION_OF_COMMIT()
            throws Exception {
        Exception rootCause = new SWorkPreconditionException("precondition failed");
        doReturn(UNCERTAIN_COMPLETION_OF_COMMIT).when(retryabilityEvaluator).evaluateRetryability(rootCause);

        workExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(), rootCause);

        verify(bonitaExecutorService).submit(eq(workDescriptor));
        verify(bonitaWork, never()).handleFailure(rootCause, emptyMap());
        assertThat(workDescriptor.getRetryCount()).isEqualTo(1);
    }

    @Test
    public void should_have_tenant_id_in_all_meters() {
        assertThat(meterRegistry.find(NUMBER_OF_WORKS_RETRIED).tag("tenant", String.valueOf(TENANT_ID)).gauge())
                .isNotNull();
    }

    protected double getNumberOfRetries() {
        return meterRegistry.find(NUMBER_OF_WORKS_RETRIED).gauge().value();
    }
}
