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
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.bonitasoft.engine.commons.time.EngineClock;
import org.bonitasoft.engine.work.audit.WorkExecutionAuditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Julien Reboul
 * @author Baptiste Mesta
 */

public class BonitaThreadPoolExecutor extends ThreadPoolExecutor implements BonitaExecutorService {

    private Logger log = LoggerFactory.getLogger(BonitaThreadPoolExecutor.class);
    public static final String NUMBER_OF_WORKS_PENDING = "bonita.bpmengine.work.pending";
    public static final String NUMBER_OF_WORKS_RUNNING = "bonita.bpmengine.work.running";
    public static final String NUMBER_OF_WORKS_EXECUTED = "bonita.bpmengine.work.executed";

    private final BlockingQueue<Runnable> workQueue;
    private final WorkFactory workFactory;
    private final EngineClock engineClock;
    private final WorkExecutionCallback workExecutionCallback;
    private WorkExecutionAuditor workExecutionAuditor;
    private MeterRegistry meterRegistry;

    private final AtomicLong runningWorks = new AtomicLong();
    private final Counter executedWorkCounter;
    private final Gauge numberOfWorksPending;
    private final Gauge numberOfWorksRunning;

    public BonitaThreadPoolExecutor(final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue,
            final ThreadFactory threadFactory,
            final RejectedExecutionHandler handler, WorkFactory workFactory, EngineClock engineClock,
            WorkExecutionCallback workExecutionCallback,
            WorkExecutionAuditor workExecutionAuditor, MeterRegistry meterRegistry, long tenantId) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.workQueue = workQueue;
        this.workFactory = workFactory;
        this.engineClock = engineClock;
        this.workExecutionCallback = workExecutionCallback;
        this.workExecutionAuditor = workExecutionAuditor;
        this.meterRegistry = meterRegistry;

        Tags tags = Tags.of("tenant", String.valueOf(tenantId));
        numberOfWorksPending = Gauge.builder(NUMBER_OF_WORKS_PENDING, workQueue, Collection::size)
                .tags(tags).baseUnit("works").description("Works pending in the execution queue")
                .register(meterRegistry);
        numberOfWorksRunning = Gauge.builder(NUMBER_OF_WORKS_RUNNING, runningWorks, AtomicLong::get)
                .tags(tags).baseUnit("works").description("Works currently executing")
                .register(meterRegistry);
        executedWorkCounter = Counter.builder(NUMBER_OF_WORKS_EXECUTED)
                .tags(tags).baseUnit("works").description("total works executed since last server start")
                .register(meterRegistry);
    }

    @Override
    public void clearAllQueues() {
        workQueue.clear();
    }

    @Override
    public Future<?> submit(final Runnable task) {
        // only submit if not shutdown
        if (!isShutdown()) {
            execute(task);
        }
        return null;
    }

    @Override
    public void shutdownAndEmptyQueue() {
        super.shutdown();
        log.info("Clearing queue of work, had {} elements", workQueue.size());
        workQueue.clear();
        meterRegistry.remove(numberOfWorksPending);
        meterRegistry.remove(numberOfWorksRunning);
        meterRegistry.remove(executedWorkCounter);
    }

    @Override
    public void submit(WorkDescriptor work) {
        submit(() -> {
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

    private boolean isRequiringDelayedExecution(WorkDescriptor work) {
        return work.getExecutionThreshold() != null && work.getExecutionThreshold().isAfter(engineClock.now());
    }
}
