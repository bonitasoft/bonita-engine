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
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.commons.time.FixedEngineClock;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.transaction.STransactionCommitException;
import org.bonitasoft.engine.work.audit.WorkExecutionAuditor;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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
    private RetryingWorkExecutorService retryingWorkExecutorService;
    @Mock
    private BonitaExecutorService bonitaExecutorService;
    @Mock
    private TechnicalLoggerService technicalLoggerService;
    @Mock
    private TechnicalLogger logger;
    @Mock
    private BonitaWork bonitaWork;
    @Mock
    private WorkExecutionAuditor workExecutionAuditor;
    @Mock
    private ExceptionRetryabilityEvaluator retryabilityEvaluator;
    private FixedEngineClock engineClock = new FixedEngineClock(Instant.EPOCH);

    private MeterRegistry meterRegistry = new SimpleMeterRegistry(
            // So that micrometer updates its counters every 1 ms:
            k -> k.equals("simple.step") ? Duration.ofMillis(1).toString() : null,
            Clock.SYSTEM);

    @Before
    public void before() {
        doReturn(logger).when(technicalLoggerService).asLogger(any());
        doReturn(RETRYABLE).when(retryabilityEvaluator)
                .evaluateRetryability(argThat(t -> t instanceof SRetryableException));
        doReturn(NOT_RETRYABLE).when(retryabilityEvaluator)
                .evaluateRetryability(argThat(t -> !(t instanceof SRetryableException)));
        retryingWorkExecutorService = new RetryingWorkExecutorService(
                (WorkExecutionCallback workExecutionCallback) -> bonitaExecutorService,
                technicalLoggerService, engineClock, WORK_TERMINATION_TIMEOUT, MAX_RETRY, DELAY, DELAY_FACTOR,
                retryabilityEvaluator,
                workExecutionAuditor, meterRegistry, TENANT_ID);
        retryingWorkExecutorService.start();
    }

    @Test
    public void should_execute_work_normally() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");

        retryingWorkExecutorService.execute(workDescriptor);

        verify(bonitaExecutorService).submit(eq(workDescriptor));
    }

    @Test
    public void should_reattempt_work_on_accepted_failure() throws Exception {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");

        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
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

        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(), duringCommitException);

        verify(bonitaExecutorService).submit(eq(workDescriptor));
        verify(bonitaWork, never()).handleFailure(nullable(Exception.class), anyMap());
        assertThat(workDescriptor.getRetryCount()).isEqualTo(1);
    }

    @Test
    public void should_reset_retry_counter_on_non_retryable_failure() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("A_WORK");

        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
                new SRetryableException(new Exception("root_cause")));
        assertThat(meterRegistry.find(NUMBER_OF_WORKS_RETRIED).gauge().value()).isEqualTo(1);

        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
                new SRetryableException(new Exception("other_cause")));
        assertThat(meterRegistry.find(NUMBER_OF_WORKS_RETRIED).gauge().value())
                .as("Should not update counter for work that has already be counted").isEqualTo(1);

        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
                new Exception("not retry-able"));
        assertThat(meterRegistry.find(NUMBER_OF_WORKS_RETRIED).gauge().value()).isEqualTo(0);
    }

    @Test
    public void should_reset_retry_counter_on_success() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("A_WORK");

        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
                new SRetryableException(new Exception("root_cause")));
        assertThat(meterRegistry.find(NUMBER_OF_WORKS_RETRIED).gauge().value()).isEqualTo(1);

        retryingWorkExecutorService.onSuccess(workDescriptor);
        assertThat(meterRegistry.find(NUMBER_OF_WORKS_RETRIED).gauge().value()).isEqualTo(0);
    }

    @Test
    public void should_not_reattempt_work_on_other_failure() throws Exception {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");

        Exception rootCause = new Exception("rootCause");
        Map<String, Object> context = emptyMap();
        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, context,
                rootCause);

        verify(bonitaExecutorService, never()).submit(eq(workDescriptor));
        verify(bonitaWork).handleFailure(rootCause, context);
        assertThat(workDescriptor.getRetryCount()).isEqualTo(0);
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
        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, context,
                exception);

        verify(bonitaExecutorService, never()).submit(eq(workDescriptor));
        verify(bonitaWork).handleFailure(exception, context);
    }

    @Test
    public void should_await_specified_time_when_stopping_the_executor() throws Exception {

        retryingWorkExecutorService.stop();

        verify(bonitaExecutorService).awaitTermination(WORK_TERMINATION_TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void should_set_the_date_after_delay_on_work_when_retrying() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");
        SRetryableException exception = new SRetryableException(new Exception("rootCause"));
        Instant now = engineClock.now();

        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(),
                exception);

        verify(bonitaExecutorService).submit(eq(workDescriptor));
        assertThat(workDescriptor.getExecutionThreshold()).isEqualTo(now.plusMillis(DELAY));
    }

    @Test
    public void should_set_the_date_after_delay_and_factor_on_work_when_retrying_multiple_times() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");
        SRetryableException exception = new SRetryableException(new Exception("rootCause"));
        Instant now = engineClock.now();

        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(), exception);
        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(), exception);
        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(), exception);

        verify(bonitaExecutorService, times(3)).submit(eq(workDescriptor));
        //date after delay is: delay * factor * factor because there is 3 retry
        assertThat(workDescriptor.getExecutionThreshold())
                .isEqualTo(now.plusMillis(DELAY * DELAY_FACTOR * DELAY_FACTOR));
    }

    @Test
    public void should_just_requeue_work_when_the_failure_is_a_work_lock_issue() throws Exception {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");

        Exception rootCause = new LockTimeoutException("rootCause");
        Map<String, Object> context = emptyMap();
        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, context,
                rootCause);

        verify(bonitaExecutorService).submit(eq(workDescriptor));
        verify(bonitaWork, never()).handleFailure(rootCause, context);
        assertThat(workDescriptor.getRetryCount()).isEqualTo(0);
    }

    @Test
    public void should_abandon_execution_when_failure_is_a_precondition_issue() throws Exception {

        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");

        Exception rootCause = new SWorkPreconditionException("precondition failed");
        Map<String, Object> context = emptyMap();
        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, context,
                rootCause);

        verifyZeroInteractions(bonitaExecutorService);
        verify(bonitaWork, never()).handleFailure(rootCause, emptyMap());
        assertThat(workDescriptor.getRetryCount()).isEqualTo(0);
    }

    @Test
    public void should_retry_PreconditionException_when_retryability_evaluator_says_so() throws Exception {
        WorkDescriptor workDescriptor = WorkDescriptor.create("MY_WORK");
        Exception rootCause = new SWorkPreconditionException("precondition failed");
        doReturn(RETRYABLE).when(retryabilityEvaluator).evaluateRetryability(rootCause);

        retryingWorkExecutorService.onFailure(workDescriptor, bonitaWork, emptyMap(), rootCause);

        verify(bonitaExecutorService).submit(eq(workDescriptor));
        verify(bonitaWork, never()).handleFailure(rootCause, emptyMap());
        assertThat(workDescriptor.getRetryCount()).isEqualTo(1);
    }

    @Test
    public void should_have_tenant_id_in_all_meters() {
        assertThat(meterRegistry.find(NUMBER_OF_WORKS_RETRIED).tag("tenant", String.valueOf(TENANT_ID)).gauge())
                .isNotNull();
    }

}
