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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.commons.time.FixedEngineClock;
import org.bonitasoft.engine.work.audit.WorkExecutionAuditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for ThreadPoolExecutor with pool size 1 (single thread).
 * Documents that the pool correctly replaces its worker thread after exceptions,
 * so that subsequent work is not stuck forever.
 */
@ExtendWith(MockitoExtension.class)
class SingleThreadPoolExecutorTest {

    private static final long TENANT_ID = 13L;

    @Mock
    private WorkExecutionAuditor workExecutionAuditor;
    private final ResettableWorkExecutionCallback workExecutionCallback = new ResettableWorkExecutionCallback();
    private DefaultBonitaExecutorService bonitaExecutorService;
    private final FixedEngineClock engineClock = new FixedEngineClock(Instant.now());
    private final WorkFactory workFactory = new LocalWorkFactory(2);
    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry(
            // So that micrometer updates its counters every 1 ms:
            k -> k.equals("simple.step") ? Duration.ofMillis(1).toString() : null,
            Clock.SYSTEM);

    @BeforeEach
    void setUp() {
        var threadPoolExecutor = new WorkSingleThreadPoolExecutorFactory.SingleThreadPoolExecutor(
                new LinkedBlockingQueue<>(10),
                new WorkerThreadFactory("test-worker", 1, 1));
        bonitaExecutorService = new DefaultBonitaExecutorService(threadPoolExecutor, workFactory, engineClock,
                workExecutionCallback, workExecutionAuditor,
                meterRegistry, TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        bonitaExecutorService.shutdownAndEmptyQueue();
    }

    @Test
    void submit_should_continue_processing_after_sync_exception() {
        // Submit failing work
        bonitaExecutorService.submit(WorkDescriptor.create("EXCEPTION"));
        await().until(workExecutionCallback::isOnFailureCalled);

        // Reset and submit normal work — must succeed even with pool size 1
        workExecutionCallback.reset();
        bonitaExecutorService.submit(WorkDescriptor.create("NORMAL"));
        await().until(workExecutionCallback::isOnSuccessCalled);
    }

    @Test
    void submit_should_preserve_work_failure_handling() {
        bonitaExecutorService.submit(WorkDescriptor.create("EXCEPTION"));
        await().until(workExecutionCallback::isOnFailureCalled);

        assertThat(workExecutionCallback.getThrown())
                .isInstanceOf(Exception.class)
                .hasMessageContaining("classic exception");

        // Verify pool still works
        workExecutionCallback.reset();
        bonitaExecutorService.submit(WorkDescriptor.create("NORMAL"));
        await().until(workExecutionCallback::isOnSuccessCalled);
    }

    @Test
    void submit_should_maintain_accurate_metrics_after_exception() {
        // Submit failing work
        bonitaExecutorService.submit(WorkDescriptor.create("EXCEPTION"));
        await().until(workExecutionCallback::isOnFailureCalled);

        // Submit normal work
        workExecutionCallback.reset();
        bonitaExecutorService.submit(WorkDescriptor.create("NORMAL"));
        await().until(workExecutionCallback::isOnSuccessCalled);

        assertThat(meterRegistry.find(DefaultBonitaExecutorService.NUMBER_OF_WORKS_EXECUTED).counter().count())
                .as("Both works (failed + successful) should be counted as executed")
                .isEqualTo(2);
    }

    @Test
    void submit_should_continue_processing_after_async_exception() {
        // Submit async failing work
        bonitaExecutorService.submit(WorkDescriptor.create("ASYNC_EXCEPTION"));
        await().until(workExecutionCallback::isOnFailureCalled);

        // Reset and submit normal work — must succeed even with pool size 1
        workExecutionCallback.reset();
        bonitaExecutorService.submit(WorkDescriptor.create("NORMAL"));
        await().until(workExecutionCallback::isOnSuccessCalled);
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private static class ResettableWorkExecutionCallback implements WorkExecutionCallback {

        private final AtomicBoolean onSuccessCalled = new AtomicBoolean(false);
        private final AtomicBoolean onFailureCalled = new AtomicBoolean(false);
        private volatile Throwable thrown;

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

        boolean isOnSuccessCalled() {
            return onSuccessCalled.get();
        }

        boolean isOnFailureCalled() {
            return onFailureCalled.get();
        }

        Throwable getThrown() {
            return thrown;
        }

        void reset() {
            onSuccessCalled.set(false);
            onFailureCalled.set(false);
            thrown = null;
        }
    }

    private static class LocalWorkFactory implements WorkFactory {

        private final long workSleepPeriodInSeconds;

        private LocalWorkFactory(long workSleepPeriodInSeconds) {
            this.workSleepPeriodInSeconds = workSleepPeriodInSeconds;
        }

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
