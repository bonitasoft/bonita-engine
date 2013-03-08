/*******************************************************************************
 * Copyright (C) 2009, 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.connector.impl;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.connector.SConnector;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.connector.exception.SConnectorValidationException;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;

/**
 * Execute connectors in parallel thread with a timeout
 * 
 * @author Baptiste Mesta
 */
public class ConnectorExecutorTimedOut implements ConnectorExecutor {

    private final ThreadPoolExecutor threadPoolExecutor;

    private long timeout;

    private final SessionAccessor sessionAccessor;

    /**
     * The handling of threads relies on the JVM
     * The rules to create new thread are:
     * - If the number of threads is less than the corePoolSize, create a new Thread to run a new task.
     * - If the number of threads is equal (or greater than) the corePoolSize, put the task into the queue.
     * - If the queue is full, and the number of threads is less than the maxPoolSize, create a new thread to run tasks in.
     * - If the queue is full, and the number of threads is greater than or equal to maxPoolSize, reject the task.
     * 
     * @param queueCapacity
     *            The maximum number of execution of connector to queue for each thread
     * @param corePoolSize
     *            the number of threads to keep in the pool, even
     *            if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param loggerService
     * @param timeout
     *            if the execution of the connector is above this time in milliseconds the execution will fail
     * @param maximumPoolSize
     *            the maximum number of threads to allow in the
     *            pool
     * @param keepAliveTime
     *            when the number of threads is greater than
     *            the core, this is the maximum time that excess idle threads
     *            will wait for new tasks before terminating. (in seconds)
     */
    public ConnectorExecutorTimedOut(final int queueCapacity, final int corePoolSize, final TechnicalLoggerService loggerService, final long timeout,
            final int maximumPoolSize, final long keepAliveTimeSeconds, final SessionAccessor sessionAccessor) {
        setTimeout(timeout);
        final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(queueCapacity);
        final RejectedExecutionHandler handler = new QueueRejectedExecutionHandler(loggerService);
        final ConnectorExecutorThreadFactory threadFactory = new ConnectorExecutorThreadFactory("ConnectorExecutor");
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTimeSeconds, TimeUnit.SECONDS, workQueue, threadFactory, handler);
        this.sessionAccessor = sessionAccessor;
    }

    @Override
    public Map<String, Object> execute(final SConnector sConnector, final Map<String, Object> inputParameters) throws SConnectorException {
        Callable<Map<String, Object>> callable = null;
        try {
            callable = new ExecuteConnectorCallable(inputParameters, sConnector, sessionAccessor.getSessionId(), sessionAccessor.getTenantId());
        } catch (final SBonitaException e) {
            disconnect(sConnector);
            throw new SConnectorException(e);
        }
        final Future<Map<String, Object>> submit = threadPoolExecutor.submit(callable);
        try {
            return submit.get(getTimeout(), TimeUnit.MILLISECONDS);
        } catch (final InterruptedException e) {
            disconnect(sConnector);
            throw new SConnectorException(e);
        } catch (final ExecutionException e) {
            disconnect(sConnector);
            throw new SConnectorException(e);
        } catch (final TimeoutException e) {
            submit.cancel(true);
            disconnect(sConnector);
            throw new SConnectorException("The connector timed out " + sConnector);
        }
    }

    @Override
    public void disconnect(final SConnector sConnector) throws SConnectorException {
        sConnector.disconnect();
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }

    /**
     * @author Baptiste Mesta
     */
    private final class ExecuteConnectorCallable implements Callable<Map<String, Object>> {

        private final Map<String, Object> inputParameters;

        private final SConnector sConnector;

        private final long sessionId;

        private final long tenantId;

        private ExecuteConnectorCallable(final Map<String, Object> inputParameters, final SConnector sConnector, final long sessionId, final long tenantId) {
            this.inputParameters = inputParameters;
            this.sConnector = sConnector;
            this.sessionId = sessionId;
            this.tenantId = tenantId;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            sessionAccessor.setSessionInfo(sessionId, tenantId);
            sConnector.setInputParameters(inputParameters);
            try {
                sConnector.validate();
            } catch (final SConnectorValidationException e) {
                throw new SConnectorException(e);
            }
            sConnector.connect();
            return sConnector.execute();
        }
    }

    private final class QueueRejectedExecutionHandler implements RejectedExecutionHandler {

        private final TechnicalLoggerService logger;

        public QueueRejectedExecutionHandler(final TechnicalLoggerService logger) {
            this.logger = logger;
        }

        @Override
        public void rejectedExecution(final Runnable task, final ThreadPoolExecutor executor) {
            logger.log(ThreadPoolExecutor.class, TechnicalLogSeverity.WARNING, "The work was rejected, requeue work: " + task.toString());
            try {
                executor.getQueue().put(task);
            } catch (final InterruptedException e) {
                throw new RejectedExecutionException("queuing " + task + " got interrupted", e);
            }
        }

    }
}
