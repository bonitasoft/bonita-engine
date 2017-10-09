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
import java.util.concurrent.ThreadFactory;
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
    private WorkFactory workFactory = workDescriptor -> new BonitaWork() {

        @Override
        public String getDescription() {
            return workDescriptor.toString();
        }

        @Override
        public void work(Map<String, Object> context) throws Exception {
            switch (workDescriptor.getType()) {
                case "EXCEPTION":
                    throw new Exception("classic exception");
                case "NORMAL":
            }
        }

        @Override
        public void handleFailure(Exception e, Map<String, Object> context) throws Exception {

        }
    };

    public BonitaThreadPoolExecutorTest() {
    }

    @Before
    public void before() throws Exception {
        ThreadFactory threadFactory = new WorkerThreadFactory("test-worker", 1, 3);
        bonitaThreadPoolExecutor = new BonitaThreadPoolExecutor(3, 3, 1000, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000), threadFactory,
                (r, executor) -> {
                }, workFactory, technicalLoggerService, engineClock, workExecutionCallback);
    }

    @Test
    public void should_call_on_success_callback_when_work_executed_properly() throws Exception {
        WorkDescriptor workDescriptor = WorkDescriptor.create("NORMAL");

        bonitaThreadPoolExecutor.submit(workDescriptor);

        await().until(() -> workExecutionCallback.isOnSuccessCalled());
        assertThat(workExecutionCallback.isOnFailureCalled()).isFalse();
    }

    @Test
    public void should_call_on_failure_callback_when_work_executed_properly() throws Exception {
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
}
