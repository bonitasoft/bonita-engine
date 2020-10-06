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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.MeterRegistry;
import org.bonitasoft.engine.commons.time.EngineClock;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.monitoring.ExecutorServiceMetricsProvider;
import org.bonitasoft.engine.work.audit.WorkExecutionAuditor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Use ThreadPoolExecutor as ExecutorService
 * The handling of threads relies on the JVM
 * The rules to create new thread are:
 * - If the number of threads is less than the corePoolSize, create a new Thread to run a new task.
 * - If the number of threads is equal (or greater than) the corePoolSize, put the task into the queue.
 * - If the queue is full, and the number of threads is less than the maxPoolSize, create a new thread to run tasks in.
 * - If the queue is full, and the number of threads is greater than or equal to maxPoolSize, reject the task.
 * When the current number of threads are > than corePoolSize, they are kept idle during keepAliveTimeSeconds
 *
 * @author Baptiste Mesta
 */
@Component("bonitaExecutorServiceFactory")
public class DefaultBonitaExecutorServiceFactory implements BonitaExecutorServiceFactory {

    private final int corePoolSize;
    private final int queueCapacity;
    private final int maximumPoolSize;
    private final long keepAliveTimeSeconds;
    private final EngineClock engineClock;
    private final TechnicalLoggerService logger;
    private final WorkFactory workFactory;
    private final long tenantId;
    private final WorkExecutionAuditor workExecutionAuditor;
    private final MeterRegistry meterRegistry;
    private final ExecutorServiceMetricsProvider executorServiceMetricsProvider;

    public DefaultBonitaExecutorServiceFactory(
            @Qualifier("tenantTechnicalLoggerService") TechnicalLoggerService logger,
            WorkFactory workFactory,
            @Value("${tenantId}") long tenantId,
            @Value("${bonita.tenant.work.corePoolSize}") int corePoolSize,
            @Value("${bonita.tenant.work.queueCapacity}") int queueCapacity,
            @Value("${bonita.tenant.work.maximumPoolSize}") int maximumPoolSize,
            @Value("${bonita.tenant.work.keepAliveTimeSeconds}") long keepAliveTimeSeconds,
            EngineClock engineClock,
            WorkExecutionAuditor workExecutionAuditor,
            MeterRegistry meterRegistry,
            ExecutorServiceMetricsProvider executorServiceMetricsProvider) {
        this.logger = logger;
        this.workFactory = workFactory;
        this.tenantId = tenantId;
        this.corePoolSize = corePoolSize;
        this.queueCapacity = queueCapacity;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
        this.engineClock = engineClock;
        this.workExecutionAuditor = workExecutionAuditor;
        this.meterRegistry = meterRegistry;
        this.executorServiceMetricsProvider = executorServiceMetricsProvider;
    }

    @Override
    public BonitaExecutorService createExecutorService(WorkExecutionCallback workExecutionCallback) {
        final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(queueCapacity);
        final RejectedExecutionHandler handler = new QueueRejectedExecutionHandler();
        final WorkerThreadFactory threadFactory = new WorkerThreadFactory("Bonita-Worker", tenantId, maximumPoolSize);

        final BonitaThreadPoolExecutor bonitaThreadPoolExecutor = new BonitaThreadPoolExecutor(corePoolSize,
                maximumPoolSize, keepAliveTimeSeconds, TimeUnit.SECONDS,
                workQueue, threadFactory, handler, workFactory, logger, engineClock, workExecutionCallback,
                workExecutionAuditor, meterRegistry, tenantId);
        logger.log(this.getClass(), TechnicalLogSeverity.INFO,
                "Creating a new Thread pool to handle works: " + bonitaThreadPoolExecutor);

        //TODO this returns the timed executor service, this should be used instead of the BonitaExecutorService but we should change it everywhere
        executorServiceMetricsProvider
                .bindMetricsOnly(meterRegistry, bonitaThreadPoolExecutor, "bonita-work-executor", tenantId);
        return bonitaThreadPoolExecutor;
    }

    @Override
    public void unbind() {
        executorServiceMetricsProvider.unbind(meterRegistry, "bonita-work-executor", tenantId);
    }

    private final class QueueRejectedExecutionHandler implements RejectedExecutionHandler {

        public QueueRejectedExecutionHandler() {
        }

        @Override
        public void rejectedExecution(final Runnable task, final ThreadPoolExecutor executor) {
            if (executor.isShutdown()) {
                logger.log(getClass(), TechnicalLogSeverity.INFO, "Tried to run work " + task
                        + " but the work service is shutdown. work will be restarted with the node");
            } else {
                throw new RejectedExecutionException(
                        "Unable to run the task "
                                + task
                                + "\n your work queue is full you might consider changing your configuration to scale more. See parameter 'queueCapacity' in bonita.home configuration files.");
            }
        }

    }

}
