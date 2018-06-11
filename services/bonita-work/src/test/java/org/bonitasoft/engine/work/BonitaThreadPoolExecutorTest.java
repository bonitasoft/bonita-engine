/**
 * Copyright (C) 2017 Bonitasoft S.A.
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

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bonitasoft.engine.commons.time.FixedEngineClock;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * @author Baptiste Mesta.
 */
public class BonitaThreadPoolExecutorTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private TechnicalLoggerService technicalLoggerService;
    private MyWorkExecutionCallback workExecutionCallback = new MyWorkExecutionCallback();
    private BonitaThreadPoolExecutor bonitaThreadPoolExecutor;
    private FixedEngineClock engineClock = new FixedEngineClock(Instant.now());
    private WorkFactory workFactory = new LocalWorkFactory(2);

    @Before
    public void before() throws Exception {
        bonitaThreadPoolExecutor = new BonitaThreadPoolExecutor(3, 3 //
                , 1_000, TimeUnit.SECONDS //
                , new ArrayBlockingQueue<>(1_000) //
                , new WorkerThreadFactory("test-worker", 1, 3) //
                , (r, executor) -> {
                } //
                , workFactory, technicalLoggerService, engineClock, workExecutionCallback);
    }

    @Test
    public void should_call_on_success_callback_when_work_executed_properly() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("NORMAL");

        bonitaThreadPoolExecutor.submit(workDescriptor);

        await().until(() -> workExecutionCallback.isOnSuccessCalled());
        assertThat(workExecutionCallback.isOnFailureCalled()).isFalse();
    }

    @Test
    public void should_call_on_failure_callback_when_work_executed_properly() {
        WorkDescriptor workDescriptor = WorkDescriptor.create("EXCEPTION");

        bonitaThreadPoolExecutor.submit(workDescriptor);

        await().until(() -> workExecutionCallback.isOnFailureCalled());
        assertThat(workExecutionCallback.isOnSuccessCalled()).isFalse();
    }

    @Test
    public void should_execute_work_after_specified_date() throws Exception {
        WorkDescriptor workDescriptor = WorkDescriptor.create("NORMAL");
        workDescriptor.mustBeExecutedAfter(Instant.now().plus(5, SECONDS));

        bonitaThreadPoolExecutor.submit(workDescriptor);

        //should still not be executed
        engineClock.addTime(1, SECONDS);
        Thread.sleep(50);
        assertThat(workExecutionCallback.isOnSuccessCalled()).isFalse();

        //add time, work should be executed
        engineClock.addTime(5, SECONDS);
        await().until(() -> workExecutionCallback.isOnSuccessCalled());
    }

    @Test
    public void should_update_works_counters_when_adding_a_workDescriptor_with_immediate_execution() throws Exception {
        //given:
        WorkDescriptor workDescriptor = WorkDescriptor.create("NORMAL");

        //when:
        bonitaThreadPoolExecutor.submit(workDescriptor);
        TimeUnit.MILLISECONDS.sleep(50); // give some time to the executor to process the work

        //then:
        assertThat(bonitaThreadPoolExecutor.getExecuted()).as("Executed works number").isEqualTo(1);
        assertThat(bonitaThreadPoolExecutor.getRunnings()).as("Running works number").isEqualTo(0);
        assertThat(bonitaThreadPoolExecutor.getPendings()).as("Pending works number").isEqualTo(0);
    }

    @Test
    public void should_update_works_counters_when_enqueuing_workDescriptor_with_long_processing_time()
            throws Exception {
        // We are using
        // an executor that can process only 1 element at a given time
        // works that take a lot of time to process to ensure to enqueue subsequent submitted works

        //given:
        bonitaThreadPoolExecutor = new BonitaThreadPoolExecutor(1, 1 //
                , 1_000, TimeUnit.SECONDS //
                , new ArrayBlockingQueue<>(1_000) //
                , new WorkerThreadFactory("test-worker", 1, 3) //
                , (r, executor) -> {
                } //
                , workFactory //
                , technicalLoggerService, engineClock, workExecutionCallback);

        //when:
        bonitaThreadPoolExecutor.submit(WorkDescriptor.create("SLEEP"));
        bonitaThreadPoolExecutor.submit(WorkDescriptor.create("NORMAL"));
        TimeUnit.MILLISECONDS.sleep(50); // give some time to the executor to process the work

        //then:
        assertThat(bonitaThreadPoolExecutor.getExecuted()).as("Executed works number").isEqualTo(0);
        assertThat(bonitaThreadPoolExecutor.getRunnings()).as("Running works number").isEqualTo(1);
        assertThat(bonitaThreadPoolExecutor.getPendings()).as("Pending works number").isEqualTo(1);
    }

    private static class MyWorkExecutionCallback implements WorkExecutionCallback {

        private final AtomicBoolean onSuccessCalled = new AtomicBoolean(false);
        private final AtomicBoolean onFailureCalled = new AtomicBoolean(false);

        @Override
        public void onSuccess(WorkDescriptor workDescriptor) {
            onSuccessCalled.set(true);
        }

        @Override
        public void onFailure(WorkDescriptor work, BonitaWork bonitaWork, Map<String, Object> context,
                Exception thrown) {
            onFailureCalled.set(true);
        }

        public boolean isOnSuccessCalled() {
            return onSuccessCalled.get();
        }

        public boolean isOnFailureCalled() {
            return onFailureCalled.get();
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
                public void work(Map<String, Object> context) throws Exception {
                    switch (workDescriptor.getType()) {
                        case "EXCEPTION":
                            throw new Exception("classic exception");
                        case "SLEEP":
                            TimeUnit.SECONDS.sleep(workSleepPeriodInSeconds);
                            break;
                        case "NORMAL":
                        default:
                    }
                }

                @Override
                public void handleFailure(Exception e, Map<String, Object> context) {
                    // do nothing
                }
            };
        }

    }

}
