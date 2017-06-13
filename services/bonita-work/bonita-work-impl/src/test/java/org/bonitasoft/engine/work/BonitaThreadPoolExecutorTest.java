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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private BonitaThreadPoolExecutor bonitaThreadPoolExecutor;
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
                }, workFactory, technicalLoggerService);
    }

    @Test
    public void should_call_on_success_callback_when_work_executed_properly() throws Exception {
        AtomicBoolean onSuccessCalled = new AtomicBoolean(false);
        AtomicBoolean onFailureCalled = new AtomicBoolean(false);

        bonitaThreadPoolExecutor.submit(WorkDescriptor.create("NORMAL"),
                workDescriptor -> onSuccessCalled.set(true),
                (workDescriptor, bonitaWork, context, thrown) -> onFailureCalled.set(true));

        await().until((Runnable) onSuccessCalled::get);
        assertThat(onFailureCalled.get()).isFalse();
        assertThat(onSuccessCalled.get()).isTrue();
    }

    @Test
    public void should_call_on_failure_callback_when_work_executed_properly() throws Exception {
        AtomicBoolean onSuccessCalled = new AtomicBoolean(false);
        AtomicBoolean onFailureCalled = new AtomicBoolean(false);

        bonitaThreadPoolExecutor.submit(WorkDescriptor.create("EXCEPTION"),
                workDescriptor -> onSuccessCalled.set(true),
                (workDescriptor, bonitaWork, context, thrown) -> onFailureCalled.set(true));

        await().until((Runnable) onFailureCalled::get);
        assertThat(onFailureCalled.get()).isTrue();
        assertThat(onSuccessCalled.get()).isFalse();
    }

}
