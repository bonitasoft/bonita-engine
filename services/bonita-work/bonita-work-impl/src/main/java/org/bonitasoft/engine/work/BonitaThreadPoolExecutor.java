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

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Julien Reboul
 * @author Baptiste Mesta
 * 
 */
public class BonitaThreadPoolExecutor extends ThreadPoolExecutor implements BonitaExecutorService {

    private final BlockingQueue<Runnable> workQueue;

    private final TechnicalLoggerService logger;

    public BonitaThreadPoolExecutor(final int corePoolSize,
            final int maximumPoolSize,
            final long keepAliveTime,
            final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue,
            final ThreadFactory threadFactory,
            final RejectedExecutionHandler handler, final TechnicalLoggerService logger) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.workQueue = workQueue;
        this.logger = logger;
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
    public <T> Future<T> submit(final Callable<T> task) {
        throw new UnsupportedOperationException("Use submit(Runnable)");
    }

    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        throw new UnsupportedOperationException("Use submit(Runnable)");
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException("Use stop instead");
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Use stop instead");
    }

    @Override
    public void shutdownAndEmptyQueue() {
        super.shutdown();
        logger.log(getClass(), TechnicalLogSeverity.INFO, "Clearing queue of work, had " + workQueue.size() + " elements");
        workQueue.clear();
    }

    @Override
    public void notifyNodeStopped(final String nodeName) {
        // nothing to do
    }
}
