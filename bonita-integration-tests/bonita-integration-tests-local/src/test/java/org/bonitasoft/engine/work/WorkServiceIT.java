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

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.bonitasoft.engine.bpm.CommonBPMServicesTest;
import org.bonitasoft.engine.commons.exceptions.SRetryableException;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Baptiste Mesta.
 */
public class WorkServiceIT extends CommonBPMServicesTest {

    private RetryingWorkExecutorService workExecutorService;
    private BPMWorkFactory workFactory;
    private int originalDelay;

    @Before
    public void before() {
        this.workExecutorService = (RetryingWorkExecutorService) getServiceAccessor().getWorkExecutorService();
        this.workFactory = getServiceAccessor().getBPMWorkFactory();
        originalDelay = workExecutorService.getDelay();
        workExecutorService.setDelay(1);
    }

    @Override
    public void after() throws Exception {
        workExecutorService.setDelay(originalDelay);
        super.after();
    }

    @Test
    public void should_retry_work_that_fails_2_times() throws Exception {
        AtomicInteger failureCounter = new AtomicInteger(0);
        AtomicBoolean workDone = new AtomicBoolean(false);
        BonitaWork myFailingWork = new BonitaWork() {

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public CompletableFuture<Void> work(Map<String, Object> context) throws Exception {
                if (failureCounter.get() < 2) {
                    failureCounter.incrementAndGet();
                    throw new SRetryableException(new Exception(""));
                }
                workDone.set(true);
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public void handleFailure(Throwable e, Map<String, Object> context) throws Exception {

            }
        };
        workFactory.addExtension("MyFailingWork", workDescriptor -> myFailingWork);

        workExecutorService.execute(WorkDescriptor.create("MyFailingWork"));

        //work should complete
        await().until(workDone::get);
        //work should have failed 2 times
        assertThat(failureCounter.get()).isEqualTo(2);
    }

    @Test
    public void should_retry_work_that_always_fails() throws Exception {
        AtomicInteger failureCounter = new AtomicInteger(0);
        AtomicBoolean handleFailureCalled = new AtomicBoolean(false);
        BonitaWork myFailingWork = new BonitaWork() {

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public CompletableFuture<Void> work(Map<String, Object> context) throws Exception {
                failureCounter.incrementAndGet();
                throw new SRetryableException(new Exception(""));
            }

            @Override
            public void handleFailure(Throwable e, Map<String, Object> context) throws Exception {
                handleFailureCalled.set(true);
            }
        };
        workFactory.addExtension("MyFailingWork", workDescriptor -> myFailingWork);

        workExecutorService.execute(WorkDescriptor.create("MyFailingWork"));

        //work should complete
        await().until(handleFailureCalled::get);
        //work should be retried 11 times before failing completely
        assertThat(failureCounter.get()).isEqualTo(11);
    }

}
