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
package org.bonitasoft.engine.connector.impl;

import static org.bonitasoft.engine.connector.ConnectorExecutionResult.result;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.connector.BonitaConnectorExecutorFactory;
import org.bonitasoft.engine.connector.ConnectorExecutionResult;
import org.bonitasoft.engine.connector.ConnectorExecutor;
import org.bonitasoft.engine.connector.SConnector;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.monitoring.ExecutorServiceMetricsProvider;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.sessionaccessor.SessionIdNotSetException;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.bonitasoft.engine.tracking.TimeTrackerRecords;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;

/**
 * Execute connectors directly
 *
 * @author Baptiste Mesta
 * @author Celine Souchet
 * @author Matthieu Chaffotte
 */
@Slf4j
@Component
@ConditionalOnSingleCandidate(ConnectorExecutor.class)
public class ConnectorExecutorImpl implements ConnectorExecutor {

    public static final String NUMBER_OF_CONNECTORS_PENDING = "bonita.bpmengine.connector.pending";
    public static final String NUMBER_OF_CONNECTORS_RUNNING = "bonita.bpmengine.connector.running";
    public static final String NUMBER_OF_CONNECTORS_EXECUTED = "bonita.bpmengine.connector.executed";
    public static final String CONNECTORS_UNIT = "connectors";

    private ExecutorService executorService;
    private final BonitaConnectorExecutorFactory bonitaConnectorExecutorFactory;
    private final SessionAccessor sessionAccessor;
    private final SessionService sessionService;

    private final TimeTracker timeTracker;
    private final MeterRegistry meterRegistry;
    private final long tenantId;
    private final ExecutorServiceMetricsProvider executorServiceMetricsProvider;

    private final AtomicLong runningWorks = new AtomicLong();
    private Counter executedWorkCounter;
    private Gauge numberOfConnectorsPending;
    private Gauge numberOfConnectorsRunning;

    public ConnectorExecutorImpl(final SessionAccessor sessionAccessor,
            final SessionService sessionService,
            final TimeTracker timeTracker,
            final MeterRegistry meterRegistry,
            @Value("${tenantId}") long tenantId,
            ExecutorServiceMetricsProvider executorServiceMetricsProvider,
            BonitaConnectorExecutorFactory bonitaConnectorExecutorFactory) {
        this.sessionAccessor = sessionAccessor;
        this.sessionService = sessionService;
        this.timeTracker = timeTracker;
        this.meterRegistry = meterRegistry;
        this.tenantId = tenantId;
        this.executorServiceMetricsProvider = executorServiceMetricsProvider;
        this.bonitaConnectorExecutorFactory = bonitaConnectorExecutorFactory;
    }

    @Override
    public CompletableFuture<ConnectorExecutionResult> execute(final SConnector sConnector,
            final Map<String, Object> inputParameters, final ClassLoader classLoader) throws SConnectorException {
        if (executorService == null) {
            throw new SConnectorException("Unable to execute a connector, if the node is not started. Start it first");
        }

        long tenantId;
        try {
            tenantId = sessionAccessor.getTenantId();
        } catch (final STenantIdNotSetException tenantIdNotSetException) {
            throw new SConnectorException("Tenant id not set.", tenantIdNotSetException);
        }

        ExecuteConnectorCallable task = new ExecuteConnectorCallable(inputParameters, sConnector, tenantId,
                classLoader);
        return execute(sConnector, task);
    }

    protected CompletableFuture<ConnectorExecutionResult> execute(SConnector sConnector,
            InterruptibleCallable<Map<String, Object>> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return wrapForStats(task).call();
            } catch (Throwable e) {
                disconnectSilently(sConnector);
                throw new SBonitaRuntimeException(e);
            }
        }, executorService);
    }

    private Callable<ConnectorExecutionResult> wrapForStats(final Callable<Map<String, Object>> task) {
        return () -> {
            runningWorks.incrementAndGet();
            try {
                long startTime = System.currentTimeMillis();
                Map<String, Object> call = task.call();
                executedWorkCounter.increment();
                return result(call).tookMillis(System.currentTimeMillis() - startTime);
            } finally {
                runningWorks.decrementAndGet();
            }
        };
    }

    private void track(final TimeTrackerRecords recordName, final long startTime, final SConnector sConnector,
            final Map<String, Object> inputParameters) {
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

    void disconnectSilently(final SConnector sConnector) {
        try {
            sConnector.disconnect();
        } catch (final Exception t) {
            log.warn("An error occurred while disconnecting the connector: {}", sConnector, t);
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
    public final class ExecuteConnectorCallable implements InterruptibleCallable<Map<String, Object>> {

        private final Map<String, Object> inputParameters;

        private final SConnector sConnector;

        private final long tenantId;

        private final ClassLoader loader;
        private Thread thread;
        private boolean interrupted;
        private boolean completed;

        private ExecuteConnectorCallable(final Map<String, Object> inputParameters, final SConnector sConnector,
                final long tenantId,
                final ClassLoader loader) {
            this.inputParameters = inputParameters;
            this.sConnector = sConnector;
            this.tenantId = tenantId;
            this.loader = loader;
        }

        @Override
        public Map<String, Object> call() throws Exception {
            log.debug("Start execution of connector {}", sConnector.getClass());
            if (interrupted) {
                throw new InterruptedException();
            }
            final long startTime = System.currentTimeMillis();

            //Fix Classloading issue with ThreadLocal implementation of SessionAccessor
            sessionAccessor.setTenantId(tenantId);
            Thread.currentThread().setContextClassLoader(loader);

            sConnector.setInputParameters(inputParameters);
            try {
                thread = Thread.currentThread();
                sConnector.validate();
                sConnector.connect();
                return sConnector.execute();
            } finally {
                thread = null;
                completed = true;
                log.info("Finish execution of connector {}", sConnector.getClass());
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

        @Override
        public void interrupt() {
            interrupted = true;
            if (thread != null) {
                StackTraceElement[] stackTrace = thread.getStackTrace();
                String stack = Arrays.stream(stackTrace).map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n"));
                log.warn(
                        "Interrupt thread of connector {}, thread is {}, {}, connectors was doing :\n {}, activate debug logs to have the full execution stacktrace.",
                        sConnector.getClass(), thread.getName(), thread.getId(), stackTrace[0].toString());
                log.debug("Interrupt thread of connector {}, thread is {}, {}, stack is:\n {}",
                        sConnector.getClass(), thread.getName(), thread.getId(), stack);
                thread.interrupt();
            }
        }

        @Override
        public boolean isCompleted() {
            return completed;
        }
    }

    @Override
    public void start() {
        if (executorService == null) {
            var threadPoolExecutor = bonitaConnectorExecutorFactory.create();
            executorService = executorServiceMetricsProvider
                    .bind(meterRegistry,
                            threadPoolExecutor,
                            "bonita-connector-executor",
                            tenantId);
            Tags tags = Tags.of("tenant", String.valueOf(tenantId));
            numberOfConnectorsPending = Gauge
                    .builder(NUMBER_OF_CONNECTORS_PENDING, threadPoolExecutor.getQueue(), Collection::size)
                    .tags(tags).baseUnit(CONNECTORS_UNIT).description("Connectors pending in the execution queue")
                    .register(meterRegistry);
            numberOfConnectorsRunning = Gauge.builder(NUMBER_OF_CONNECTORS_RUNNING, runningWorks, AtomicLong::get)
                    .tags(tags).baseUnit(CONNECTORS_UNIT).description("Connectors currently executing")
                    .register(meterRegistry);
            executedWorkCounter = Counter.builder(NUMBER_OF_CONNECTORS_EXECUTED)
                    .tags(tags).baseUnit(CONNECTORS_UNIT)
                    .description("Total connectors executed since last server start")
                    .register(meterRegistry);
        }
    }

    // For unit tests
    ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void stop() {
        if (executorService != null) {
            meterRegistry.remove(executedWorkCounter);
            meterRegistry.remove(numberOfConnectorsRunning);
            meterRegistry.remove(numberOfConnectorsPending);
            executorServiceMetricsProvider.unbind(meterRegistry, "bonita-connector-executor", tenantId);

            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                    log.warn("Timeout (5s) trying to stop the connector executor thread pool.");
                }
            } catch (final InterruptedException e) {
                log.warn("Error while stopping the connector executor thread pool.", e);
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
