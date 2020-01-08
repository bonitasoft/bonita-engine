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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
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
    private TechnicalLoggerService loggerService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private SessionService sessionService;

    @Mock
    private TimeTracker timeTracker;

    private ConnectorExecutorImpl connectorExecutor;

    @Before
    public void setUp() {
        connectorExecutor = new ConnectorExecutorImpl(10, 5, loggerService, 100, 100, sessionAccessor, sessionService,
                timeTracker, new SimpleMeterRegistry(), 12L, new DefaultExecutorServiceMetricsProvider());
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

    private Callable<Void> buildConnectorExecutionCallable(final String resourceName) {
        return () -> {
            connectorExecutor.execute(new ResourceConnector(resourceName), null, new ResourceClassLoader(resourceName));
            return null;
        };
    }

}
