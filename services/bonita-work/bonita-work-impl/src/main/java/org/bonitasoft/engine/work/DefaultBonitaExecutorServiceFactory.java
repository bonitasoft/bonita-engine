/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
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

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

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
public class DefaultBonitaExecutorServiceFactory implements BonitaExecutorServiceFactory {

    private final int corePoolSize;

    private final int queueCapacity;

    private final int maximumPoolSize;

    private final long keepAliveTimeSeconds;

    private final TechnicalLoggerService logger;

    private final long tenantId;

    public DefaultBonitaExecutorServiceFactory(final TechnicalLoggerService logger, final long tenantId, final int corePoolSize, final int queueCapacity,
            final int maximumPoolSize,
            final long keepAliveTimeSeconds) {
        this.logger = logger;
        this.tenantId = tenantId;
        this.corePoolSize = corePoolSize;
        this.queueCapacity = queueCapacity;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
    }

    @Override
    public BonitaExecutorService createExecutorService() {
        final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(queueCapacity);
        final RejectedExecutionHandler handler = new QueueRejectedExecutionHandler();
        final WorkerThreadFactory threadFactory = new WorkerThreadFactory("Bonita-Worker", tenantId, maximumPoolSize);
        return new BonitaThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTimeSeconds, TimeUnit.SECONDS, workQueue,
                threadFactory, handler, logger);
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
