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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.connector.AbstractSConnector;
import org.bonitasoft.engine.connector.ConnectorExecutionResult;
import org.bonitasoft.engine.monitoring.DefaultExecutorServiceMetricsProvider;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for ConnectorExecutorImpl with pool size 1 (single thread).
 * Documents that the pool correctly replaces its worker thread after connector exceptions,
 * so that subsequent connectors are not stuck forever.
 */
@ExtendWith(MockitoExtension.class)
class ConnectorExecutorSingleThreadTest {

    private static final long TENANT_ID = 12L;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private SessionService sessionService;

    @Mock
    private TimeTracker timeTracker;

    private ConnectorExecutorImpl connectorExecutorImpl;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry(
                // So that micrometer updates its counters every 1 ms:
                k -> k.equals("simple.step") ? Duration.ofMillis(1).toString() : null,
                Clock.SYSTEM);
        connectorExecutorImpl = new ConnectorExecutorImpl(sessionAccessor, sessionService,
                timeTracker, meterRegistry, new DefaultExecutorServiceMetricsProvider(),
                new ConnectorSingleThreadExecutorFactory(1));
        connectorExecutorImpl.start();
    }

    @AfterEach
    void tearDown() {
        connectorExecutorImpl.stop();
    }

    @Test
    void execute_should_continue_processing_after_connector_exception() throws Exception {
        // Execute a failing connector
        assertThatThrownBy(() -> connectorExecutorImpl
                .execute(new FailingSConnector(), new HashMap<>(),
                        Thread.currentThread().getContextClassLoader())
                .get(5, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class);

        // Execute a successful connector — must succeed even with pool size 1
        ConnectorExecutionResult result = connectorExecutorImpl
                .execute(new LocalSConnector(), new HashMap<>(),
                        Thread.currentThread().getContextClassLoader())
                .get(5, TimeUnit.SECONDS);

        assertThat(result.getOutputs()).containsEntry("result", "success");
    }

    @Test
    void execute_should_complete_future_exceptionally_on_connector_failure() {
        assertThatThrownBy(() -> connectorExecutorImpl
                .execute(new FailingSConnector(), new HashMap<>(),
                        Thread.currentThread().getContextClassLoader())
                .get(5, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("connector failure");
    }

    @Test
    void execute_should_maintain_accurate_metrics_after_exception() throws Exception {
        // Execute a failing connector
        assertThatThrownBy(() -> connectorExecutorImpl
                .execute(new FailingSConnector(), new HashMap<>(),
                        Thread.currentThread().getContextClassLoader())
                .get(5, TimeUnit.SECONDS))
                .isInstanceOf(ExecutionException.class);

        // Execute a successful connector
        connectorExecutorImpl
                .execute(new LocalSConnector(), new HashMap<>(),
                        Thread.currentThread().getContextClassLoader())
                .get(5, TimeUnit.SECONDS);

        TimeUnit.MILLISECONDS.sleep(50); // give micrometer time to update counters

        // Note: unlike the worker pool, the connector executor only counts successful executions
        var counter = meterRegistry.find(ConnectorExecutorImpl.NUMBER_OF_CONNECTORS_EXECUTED).counter();
        assertThat(counter)
                .as("Counter for number of connectors executed should be present")
                .isNotNull();
        assertThat(counter.count())
                .as("Only the successful connector should be counted as executed")
                .isEqualTo(1);
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private static class LocalSConnector extends AbstractSConnector {

        @Override
        public void validate() {
        }

        @Override
        public Map<String, Object> execute() {
            return Collections.singletonMap("result", "success");
        }

        @Override
        public void connect() {
        }

        @Override
        public void disconnect() {
        }
    }

    private static class FailingSConnector extends AbstractSConnector {

        @Override
        public void validate() {
        }

        @Override
        public Map<String, Object> execute() {
            throw new RuntimeException("connector failure");
        }

        @Override
        public void connect() {
        }

        @Override
        public void disconnect() {
        }
    }
}
