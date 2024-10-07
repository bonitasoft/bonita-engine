/**
 * Copyright (C) 2017-2019 Bonitasoft S.A.
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

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.commons.time.FixedEngineClock;
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
public class DefaultBonitaExecutorServiceTest {

    public static final long TENANT_ID = 13L;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private WorkExecutionAuditor workExecutionAuditor;
    private final MyWorkExecutionCallback workExecutionCallback = new MyWorkExecutionCallback();
    private DefaultBonitaExecutorService bonitaExecutorService;
    private final FixedEngineClock engineClock = new FixedEngineClock(Instant.now());
    private final WorkFactory workFactory = new LocalWorkFactory(2);
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry(
            // So that micrometer updates its counters every 1 ms:
            k -> k.equals("simple.step") ? Duration.ofMillis(1).toString() : null,
            Clock.SYSTEM);

    @Before
    public void before() {
        var threadPoolExecutor = new WorkSingleThreadPoolExecutorFactory.SingleThreadPoolExecutor(
                new LinkedBlockingQueue<>(10),
                new WorkerThreadFactory("test-worker", 1, 1));
        bonitaExecutorService = new DefaultBonitaExecutorService(threadPoolExecutor, workFactory, engineClock,
                workExecutionCallback, workExecutionAuditor,
                meterRegistry, TENANT_ID);
    }

    @Test
    public void should_call_on_success_callback_when_work_executed_properly() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("NORMAL");

        bonitaExecutorService.submit(workDescriptor);

        await().until(workExecutionCallback::isOnSuccessCalled);
        assertThat(workExecutionCallback.isOnFailureCalled()).isFalse();
    }

    @Test
    public void should_call_on_failure_callback_when_work_executed_properly() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("EXCEPTION");

        bonitaExecutorService.submit(workDescriptor);

        await().until(workExecutionCallback::isOnFailureCalled);
        assertThat(workExecutionCallback.isOnSuccessCalled()).isFalse();
    }

    @Test
    public void should_execute_work_after_specified_date() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("NORMAL");
        workDescriptor.mustBeExecutedAfter(Instant.now().plus(5, SECONDS));

        bonitaExecutorService.submit(workDescriptor);

        //should still not be executed
        engineClock.addTime(1, SECONDS);
        await().atLeast(50, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertThat(workExecutionCallback.isOnSuccessCalled()).isFalse());

        //add time, work should be executed
        engineClock.addTime(5, SECONDS);
        await().until(workExecutionCallback::isOnSuccessCalled);
    }

    @Test
    public void should_update_meter_when_work_executes() {
        Gauge currentWorkQueue = meterRegistry.find(DefaultBonitaExecutorService.NUMBER_OF_WORKS_PENDING).gauge();
        for (int i = 0; i <= 4; i++) {
            WorkDescriptor workDescriptor = WorkDescriptor.create("SLEEP");
            bonitaExecutorService.submit(workDescriptor);
        }
        await().untilAsserted(() -> assertThat(currentWorkQueue.value()).isPositive());
    }

    @Test
    public void should_update_works_counters_when_enqueuing_workDescriptor_with_long_processing_time()
            throws Exception {

        //when:
        bonitaExecutorService.submit(WorkDescriptor.create("NORMAL"));
        bonitaExecutorService.submit(WorkDescriptor.create("SLEEP"));
        bonitaExecutorService.submit(WorkDescriptor.create("SLEEP"));
        bonitaExecutorService.submit(WorkDescriptor.create("SLEEP"));
        bonitaExecutorService.submit(WorkDescriptor.create("SLEEP"));
        bonitaExecutorService.submit(WorkDescriptor.create("NORMAL"));
        TimeUnit.MILLISECONDS.sleep(50); // give some time to the executor to process the work

        //then:
        // 1 executed because normal work is submitted first.
        assertThat(meterRegistry.find(DefaultBonitaExecutorService.NUMBER_OF_WORKS_EXECUTED).counter().count())
                .as("Executed works number").isEqualTo(1);
        // 1 running works because we have 1 threads in the pool and sleeping works wait for 2s to execute:
        assertThat(meterRegistry.find(DefaultBonitaExecutorService.NUMBER_OF_WORKS_RUNNING).gauge().value())
                .as("Running works number").isEqualTo(1);
        // 4 pending works because the single thread is busy, so the last 4 works are in the queue:
        assertThat(meterRegistry.find(DefaultBonitaExecutorService.NUMBER_OF_WORKS_PENDING).gauge().value())
                .as("Pending works number").isEqualTo(4);
    }

    @Test
    public void should_have_no_meters_after_shutdown() {
        var work = bonitaExecutorService.submit(WorkDescriptor.create("NORMAL"));
        await().until(work::isDone);

        bonitaExecutorService.shutdownAndEmptyQueue();

        assertThat(meterRegistry.getMeters()).isEmpty();
    }

    @Test
    public void should_have_tenant_id_in_all_meters() {
        assertThat(meterRegistry.find(DefaultBonitaExecutorService.NUMBER_OF_WORKS_EXECUTED)
                .tag("tenant", String.valueOf(TENANT_ID)).counter()).isNotNull();
        assertThat(meterRegistry.find(DefaultBonitaExecutorService.NUMBER_OF_WORKS_RUNNING)
                .tag("tenant", String.valueOf(TENANT_ID)).gauge()).isNotNull();
        assertThat(meterRegistry.find(DefaultBonitaExecutorService.NUMBER_OF_WORKS_PENDING)
                .tag("tenant", String.valueOf(TENANT_ID)).gauge()).isNotNull();
    }

    @Test
    public void should_call_on_success_callback_only_when_async_work_executed_properly() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("ASYNC");

        var work = bonitaExecutorService.submit(workDescriptor);
        await().until(work::isDone);

        assertThat(workExecutionCallback.isOnSuccessCalled()).isFalse();
        assertThat(workExecutionCallback.isOnFailureCalled()).isFalse();

        await().until(workExecutionCallback::isOnSuccessCalled);
        assertThat(workExecutionCallback.isOnFailureCalled()).isFalse();
    }

    @Test
    public void should_call_on_failure_callback_ony_when_async_work_executed_properly() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("ASYNC_EXCEPTION");

        var work = bonitaExecutorService.submit(workDescriptor);
        await().until(work::isDone);

        assertThat(workExecutionCallback.isOnSuccessCalled()).isFalse();
        assertThat(workExecutionCallback.isOnFailureCalled()).isFalse();

        await().until(workExecutionCallback::isOnFailureCalled);
        assertThat(workExecutionCallback.isOnSuccessCalled()).isFalse();
        assertThat(workExecutionCallback.getThrown()).hasMessage("my exception").isInstanceOf(SWorkException.class);
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private static class MyWorkExecutionCallback implements WorkExecutionCallback {

        private final AtomicBoolean onSuccessCalled = new AtomicBoolean(false);
        private final AtomicBoolean onFailureCalled = new AtomicBoolean(false);
        private Throwable thrown;

        @Override
        public void onSuccess(WorkDescriptor workDescriptor) {
            onSuccessCalled.set(true);
        }

        @Override
        public void onFailure(WorkDescriptor work, BonitaWork bonitaWork, Map<String, Object> context,
                Throwable thrown) {
            this.thrown = thrown;
            onFailureCalled.set(true);
        }

        public boolean isOnSuccessCalled() {
            return onSuccessCalled.get();
        }

        public boolean isOnFailureCalled() {
            return onFailureCalled.get();
        }

        public Throwable getThrown() {
            return thrown;
        }
    }

    private record LocalWorkFactory(long workSleepPeriodInSeconds) implements WorkFactory {

    @Override
    public BonitaWork create(WorkDescriptor workDescriptor) {
        return new BonitaWork() {

            @Override
            public String getDescription() {
                return workDescriptor.toString();
            }

            @Override
            public CompletableFuture<Void> work(Map<String, Object> context) throws Exception {
                switch (workDescriptor.getType()) {
                    case "EXCEPTION":
                        throw new Exception("classic exception");
                    case "SLEEP":
                        TimeUnit.SECONDS.sleep(workSleepPeriodInSeconds);
                        break;
                    case "ASYNC":
                        return CompletableFuture.supplyAsync(() -> {
                            try {
                                TimeUnit.MILLISECONDS.sleep(200);
                            } catch (InterruptedException ignored) {
                            }
                            return null;
                        }, Executors.newSingleThreadExecutor());
                    case "ASYNC_EXCEPTION":
                        return CompletableFuture.supplyAsync(() -> {
                            try {
                                TimeUnit.MILLISECONDS.sleep(200);
                            } catch (InterruptedException ignored) {
                            }
                            throw new CompletionException(new SWorkException("my exception"));
                        }, Executors.newSingleThreadExecutor());
                    case "NORMAL":
                    default:
                }
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void handleFailure(Throwable e, Map<String, Object> context) {
                // do nothing
            }
        };
    }

}

}
