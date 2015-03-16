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
package org.bonitasoft.engine.connector.impl;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.connector.SConnector;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.tracking.TimeTrackerRecords;

/**
 * Execute connectors directly
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
public class ConnectorExecutorImpl implements ConnectorExecutor {

    private ExecutorService executorService;

    private final SessionAccessor sessionAccessor;

    private final SessionService sessionService;

    private final int queueCapacity;

    private final int corePoolSize;

    private final int maximumPoolSize;

    private final long keepAliveTimeSeconds;

    private final TechnicalLoggerService loggerService;

    private final TimeTracker timeTracker;

    /**
     * The handling of threads relies on the JVM
     * The rules to create new thread are:
     * - If the number of threads is less than the corePoolSize, create a new Thread to run a new task.
     * - If the number of threads is equal (or greater than) the corePoolSize, put the task into the queue.
     * - If the queue is full, and the number of threads is less than the maxPoolSize, create a new thread to run tasks in.
     * - If the queue is full, and the number of threads is greater than or equal to maxPoolSize, reject the task.
     *
     * @param queueCapacity
     *        The maximum number of execution of connector to queue for each thread
     * @param corePoolSize
     *        the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param loggerService
     * @param timeout
     *        if the execution of the connector is above this time in milliseconds the execution will fail
     * @param maximumPoolSize
     *        the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime
     *        when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating. (in seconds)
     */
    public ConnectorExecutorImpl(final int queueCapacity, final int corePoolSize, final TechnicalLoggerService loggerService, final int maximumPoolSize,
            final long keepAliveTimeSeconds, final SessionAccessor sessionAccessor, final SessionService sessionService, final TimeTracker timeTracker) {

        this.queueCapacity = queueCapacity;
        this.corePoolSize = corePoolSize;
        this.loggerService = loggerService;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.timeTracker = timeTracker;
    }

    @Override
    public Map<String, Object> execute(final SConnector sConnector, final Map<String, Object> inputParameters, final ClassLoader classLoader)
            throws SConnectorException {
        final long startTime = System.currentTimeMillis();
        if (executorService == null) {
            throw new SConnectorException("Unable to execute a connector, if the node is not started. Start it first");
        }

        long tenantId;
        try {
            tenantId = sessionAccessor.getTenantId();
        } catch (final STenantIdNotSetException tenantIdNotSetException) {
            throw new SConnectorException("Tenant id not set.", tenantIdNotSetException);
        }
        final Callable<Map<String, Object>> callable = new ExecuteConnectorCallable(inputParameters, sConnector, tenantId, classLoader);
        final Future<Map<String, Object>> submit = executorService.submit(callable);
        try {
            return getValue(submit);
        } catch (final InterruptedException e) {
            disconnectSilently(sConnector);
            throw new SConnectorException(e);
        } catch (final ExecutionException e) {
            disconnectSilently(sConnector);
            throw new SConnectorException(e);
        } catch (final TimeoutException e) {
            submit.cancel(true);
            disconnectSilently(sConnector);
            throw new SConnectorException("The connector timed out " + sConnector);
        } finally {
            track(TimeTrackerRecords.EXECUTE_CONNECTOR_INCLUDING_POOL_SUBMIT, startTime, sConnector, inputParameters);
        }
    }

    private void track(final String recordName, final long startTime, final SConnector sConnector, final Map<String, Object> inputParameters) {
        if (timeTracker.isTrackable(recordName)) {
            final long endTime = System.currentTimeMillis();
            final StringBuilder desc = new StringBuilder();
            desc.append("Connector: ");
            desc.append(sConnector);
            desc.append(" - ");
            desc.append("inputParameters: ");
            desc.append(inputParameters);
            timeTracker.track(recordName, desc.toString(), endTime - startTime);
        }
    }

    protected Map<String, Object> getValue(final Future<Map<String, Object>> submit) throws InterruptedException, ExecutionException, TimeoutException {
        return submit.get();
    }

    void disconnectSilently(final SConnector sConnector) {
        try {
            sConnector.disconnect();
        } catch (final Exception t) {
            if (loggerService.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                loggerService.log(getClass(), TechnicalLogSeverity.WARNING, "An error occured while disconnecting the connector: " + sConnector, t);
            }
        }
    }

    @Override
    public void disconnect(final SConnector sConnector) throws SConnectorException {
        try {
            sConnector.disconnect();
        } catch (final SConnectorException e) {
            throw e;
        } catch (final Exception t) {
            throw new SConnectorException(t);
        }
    }

    /**
     * @author Baptiste Mesta
     */
    final class ExecuteConnectorCallable implements Callable<Map<String, Object>> {

        private final Map<String, Object> inputParameters;

        private final SConnector sConnector;

        private final long tenantId;

        private final ClassLoader loader;

        private ExecuteConnectorCallable(final Map<String, Object> inputParameters, final SConnector sConnector, final long tenantId, final ClassLoader loader) {
            this.inputParameters = inputParameters;
            this.sConnector = sConnector;
            this.tenantId = tenantId;
            this.loader = loader;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            final long startTime = System.currentTimeMillis();

            //Fix Classloading issue with ThreadLocal implementation of SessionAccessor
            sessionAccessor.setTenantId(tenantId);
            Thread.currentThread().setContextClassLoader(loader);

            sConnector.setInputParameters(inputParameters);
            try {
                sConnector.validate();
                sConnector.connect();
                return sConnector.execute();
            } finally {
                // in case a session has been created: see ConnectorAPIAccessorImpl
                try {
                    final long sessionId = sessionAccessor.getSessionId();
                    sessionAccessor.deleteSessionId();
                    sessionService.deleteSession(sessionId);
                } catch (final SessionIdNotSetException e) {
                    // nothing, no session has been created
                }
                track(TimeTrackerRecords.EXECUTE_CONNECTOR_CALLABLE, startTime, sConnector, inputParameters);
            }
        }

    }

    private final class QueueRejectedExecutionHandler implements RejectedExecutionHandler {

        private final TechnicalLoggerService logger;

        public QueueRejectedExecutionHandler(final TechnicalLoggerService logger) {
            this.logger = logger;
        }

        @Override
        public void rejectedExecution(final Runnable task, final ThreadPoolExecutor executor) {
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.WARNING)) {
                logger.log(this.getClass(), TechnicalLogSeverity.WARNING, "The work was rejected. Requeue work : " + task.toString());
            }
            try {
                executor.getQueue().put(task);
            } catch (final InterruptedException e) {
                throw new RejectedExecutionException("Queuing " + task + " got interrupted.", e);
            }
        }

    }

    @Override
    public void start() {
        if (executorService == null) {
            final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(queueCapacity);
            final RejectedExecutionHandler handler = new QueueRejectedExecutionHandler(loggerService);
            final ConnectorExecutorThreadFactory threadFactory = new ConnectorExecutorThreadFactory("ConnectorExecutor");
            setExecutor(new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTimeSeconds, TimeUnit.SECONDS, workQueue, threadFactory, handler));
        }
    }

    void setExecutor(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    // For unit tests
    ExecutorService getExecutorService() {
        return executorService;
    }

    public void setExecutorService(final ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void stop() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                    loggerService.log(getClass(), TechnicalLogSeverity.WARNING, "Timeout (5s) trying to stop the connector executor thread pool.");
                }
            } catch (final InterruptedException e) {
                loggerService.log(getClass(), TechnicalLogSeverity.WARNING, "Error while stopping the connector executor thread pool.", e);
            }
            executorService = null;
        }
    }

    @Override
    public void pause() {
        stop();
    }

    @Override
    public void resume() {
        start();
    }
}
