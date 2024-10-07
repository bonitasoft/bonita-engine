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

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.bonitasoft.engine.commons.time.EngineClock;
import org.bonitasoft.engine.work.audit.WorkExecutionAuditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultBonitaExecutorService implements BonitaExecutorService {

    private static final Logger log = LoggerFactory.getLogger(DefaultBonitaExecutorService.class);
    public static final String NUMBER_OF_WORKS_PENDING = "bonita.bpmengine.work.pending";
    public static final String NUMBER_OF_WORKS_RUNNING = "bonita.bpmengine.work.running";
    public static final String NUMBER_OF_WORKS_EXECUTED = "bonita.bpmengine.work.executed";
    public static final String WORKS_UNIT = "works";

    private final WorkFactory workFactory;
    private final EngineClock engineClock;
    private final WorkExecutionCallback workExecutionCallback;
    private final WorkExecutionAuditor workExecutionAuditor;
    private final MeterRegistry meterRegistry;

    private final AtomicLong runningWorks = new AtomicLong();
    private final Counter executedWorkCounter;
    private final Gauge numberOfWorksPending;
    private final Gauge numberOfWorksRunning;
    private final ThreadPoolExecutor executor;

    public DefaultBonitaExecutorService(final ThreadPoolExecutor executor,
            final WorkFactory workFactory,
            final EngineClock engineClock,
            final WorkExecutionCallback workExecutionCallback,
            final WorkExecutionAuditor workExecutionAuditor,
            final MeterRegistry meterRegistry,
            final long tenantId) {
        this.executor = executor;
        this.workFactory = workFactory;
        this.engineClock = engineClock;
        this.workExecutionCallback = workExecutionCallback;
        this.workExecutionAuditor = workExecutionAuditor;
        this.meterRegistry = meterRegistry;

        Tags tags = Tags.of("tenant", String.valueOf(tenantId));
        numberOfWorksPending = Gauge.builder(NUMBER_OF_WORKS_PENDING, executor.getQueue(), Collection::size)
                .tags(tags).baseUnit(WORKS_UNIT).description("Works pending in the execution queue")
                .register(meterRegistry);
        numberOfWorksRunning = Gauge.builder(NUMBER_OF_WORKS_RUNNING, runningWorks, AtomicLong::get)
                .tags(tags).baseUnit(WORKS_UNIT).description("Works currently executing")
                .register(meterRegistry);
        executedWorkCounter = Counter.builder(NUMBER_OF_WORKS_EXECUTED)
                .tags(tags).baseUnit(WORKS_UNIT).description("total works executed since last server start")
                .register(meterRegistry);
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    @Override
    public void clearAllQueues() {
        executor.getQueue().clear();
    }

    @Override
    public void shutdownAndEmptyQueue() {
        executor.shutdown();
        log.info("Clearing queue of work, had {} elements", executor.getQueue().size());
        executor.getQueue().clear();
        meterRegistry.remove(numberOfWorksPending);
        meterRegistry.remove(numberOfWorksRunning);
        meterRegistry.remove(executedWorkCounter);
    }

    @Override
    public Future<?> submit(WorkDescriptor work) {
        return executor.submit(() -> {
            if (isRequiringDelayedExecution(work)) {
                // Future implementation should use a real delay e.g. using a ScheduledThreadPoolExecutor
                // Will be executed later
                submit(work);
                return;
            }
            work.incrementExecutionCount();
            workExecutionAuditor.detectAbnormalExecutionAndNotify(work);

            BonitaWork bonitaWork = workFactory.create(work);
            HashMap<String, Object> context = new HashMap<>();
            CompletableFuture<Void> asyncResult;
            runningWorks.incrementAndGet();
            try {
                asyncResult = bonitaWork.work(context);
            } catch (Exception e) {
                executedWorkCounter.increment();
                runningWorks.decrementAndGet();
                workExecutionCallback.onFailure(work, bonitaWork, context, e);
                return;
            }

            asyncResult.handle((result, error) -> {
                executedWorkCounter.increment();
                runningWorks.decrementAndGet();
                if (error != null) {
                    if (error instanceof CompletionException) {
                        error = error.getCause();
                    }
                    workExecutionCallback.onFailure(work, bonitaWork, context, error);
                } else {
                    workExecutionCallback.onSuccess(work);
                }
                return null;
            });
        });
    }

    @Override
    public boolean awaitTermination(long workTerminationTimeout, TimeUnit seconds) throws InterruptedException {
        return executor.awaitTermination(workTerminationTimeout, seconds);
    }

    private boolean isRequiringDelayedExecution(WorkDescriptor work) {
        return work.getExecutionThreshold() != null && work.getExecutionThreshold().isAfter(engineClock.now());
    }
}
