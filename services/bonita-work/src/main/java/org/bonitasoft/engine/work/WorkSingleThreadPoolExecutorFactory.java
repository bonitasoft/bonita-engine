/**
 * Copyright (C) 2024 Bonitasoft S.A.
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

import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnSingleCandidate(BonitaWorkExecutorFactory.class)
public class WorkSingleThreadPoolExecutorFactory implements BonitaWorkExecutorFactory {

    private final int queueCapacity;
    private final long tenantId;

    public WorkSingleThreadPoolExecutorFactory(@Value("${tenantId}") long tenantId,
            @Value("${bonita.tenant.work.queueCapacity}") int queueCapacity) {
        this.queueCapacity = queueCapacity;
        this.tenantId = tenantId;
    }

    @Override
    public ThreadPoolExecutor create() {
        return new SingleThreadPoolExecutor(new ArrayBlockingQueue<>(queueCapacity),
                new WorkerThreadFactory("Bonita-Worker", tenantId));
    }

    public static class SingleThreadPoolExecutor extends ThreadPoolExecutor {

        public SingleThreadPoolExecutor(final BlockingQueue<Runnable> workQueue,
                final ThreadFactory threadFactory) {
            super(1, 1, 0, TimeUnit.MILLISECONDS, workQueue, threadFactory, new QueueRejectedExecutionHandler());
        }

        @Override
        public Future<?> submit(final Runnable task) {
            // only submit if not shutdown
            if (!isShutdown()) {
                return super.submit(task);

            }
            return null;
        }

    }

    public static class QueueRejectedExecutionHandler implements RejectedExecutionHandler {

        private static final Logger logger = LoggerFactory.getLogger(QueueRejectedExecutionHandler.class);

        @Override
        public void rejectedExecution(final Runnable task, final ThreadPoolExecutor executor) {
            if (executor.isShutdown()) {
                logger.info(
                        "Tried to run work {} but the work service is shutdown. work will be restarted with the node",
                        task);
            } else {
                throw new RejectedExecutionException(
                        "Unable to run the task "
                                + task
                                + "\n your work queue is full you might consider changing your configuration to scale more. See parameter 'bonita.tenant.work.queueCapacity' in bonita-tenant-community.properties configuration files.");
            }
        }

    }
}
