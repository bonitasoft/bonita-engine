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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.connector.AbstractSConnector;
import org.bonitasoft.engine.connector.ConnectorExecutionResult;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.connector.exception.SConnectorValidationException;
import org.bonitasoft.engine.monitoring.DefaultExecutorServiceMetricsProvider;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectorExecutorImplIT {

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private SessionService sessionService;

    @Mock
    private TimeTracker timeTracker;

    private ConnectorExecutorImpl connectorExecutor;

    @Before
    public void setUp() {
        connectorExecutor = new ConnectorExecutorImpl(sessionAccessor,
                sessionService,
                timeTracker,
                new SimpleMeterRegistry(),
                12L,
                new DefaultExecutorServiceMetricsProvider(),
                new ConnectorSingleThreadExecutorFactory(10));
        connectorExecutor.start();
    }

    @After
    public void tearDown() {
        connectorExecutor.stop();
    }

    @Test
    public void should_execute_a_simple_connector() throws Exception {
        connectorExecutor.execute(new ResourceConnector("test2"), null, new ResourceClassLoader("test2"));
    }

    @Test
    public void should_execute_connectors_concurrently() throws Exception {
        final Callable<Void> task = buildConnectorExecutionCallable("test");
        final Callable<Void> task2 = buildConnectorExecutionCallable("test2");

        final ExecutorService service = Executors.newFixedThreadPool(25);
        final List<Callable<Void>> tasks1 = Collections.nCopies(50, task);
        final List<Callable<Void>> tasks2 = Collections.nCopies(50, task2);

        final List<Callable<Void>> tasks = new ArrayList<>(tasks1);
        tasks.addAll(tasks2);

        final List<Future<Void>> all = service.invokeAll(tasks);
        service.shutdown();

        for (final Future<Void> future : all) {
            future.get();
        }
    }

    @Test
    public void should_return_execution_time_of_the_connector() throws Exception {
        ConnectorExecutionResult connectorExecutionResult = connectorExecutor
                .execute(new SleepConnector(50), Collections.emptyMap(), Thread.currentThread().getContextClassLoader())
                .get();

        assertThat(connectorExecutionResult.getExecutionTimeMillis()).isGreaterThanOrEqualTo(50).isLessThan(1000);
    }

    private Callable<Void> buildConnectorExecutionCallable(final String resourceName) {
        return () -> {
            connectorExecutor.execute(new ResourceConnector(resourceName), null, new ResourceClassLoader(resourceName));
            return null;
        };
    }

    private static class SleepConnector extends AbstractSConnector {

        private int sleepMillis;

        public SleepConnector(int sleepMillis) {
            this.sleepMillis = sleepMillis;
        }

        @Override
        public void validate() throws SConnectorValidationException {

        }

        @Override
        public Map<String, Object> execute() throws SConnectorException {
            try {
                sleepMillis = 100;
                Thread.sleep(sleepMillis);
            } catch (InterruptedException e) {
                throw new SConnectorException(e);
            }
            return null;
        }

        @Override
        public void connect() throws SConnectorException {

        }

        @Override
        public void disconnect() throws SConnectorException {

        }
    }
}
