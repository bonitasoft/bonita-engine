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
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.connector.AbstractSConnector;
import org.bonitasoft.engine.connector.ConnectorExecutionResult;
import org.bonitasoft.engine.connector.SConnector;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.monitoring.DefaultExecutorServiceMetricsProvider;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectorExecutorImplTest {

    public static final long TENANT_ID = 12L;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private SessionService sessionService;

    @Mock
    private SConnector connector;

    @Mock
    private TimeTracker timeTracker;

    private ConnectorExecutorImpl connectorExecutorImpl;

    private SimpleMeterRegistry meterRegistry;

    @Rule
    public SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();

    @Before
    public void before() {
        meterRegistry = new SimpleMeterRegistry(
                // So that micrometer updates its counters every 1 ms:
                k -> k.equals("simple.step") ? Duration.ofMillis(1).toString() : null,
                Clock.SYSTEM);
        connectorExecutorImpl = new ConnectorExecutorImpl(sessionAccessor, sessionService,
                timeTracker,
                meterRegistry,
                TENANT_ID,
                new DefaultExecutorServiceMetricsProvider(),
                new ConnectorSingleThreadExecutorFactory(1));

        connectorExecutorImpl.start();
    }

    @Test
    public void should_execute_submit_callable() throws Exception {
        SConnector connector = new SConnector() {

            @Override
            public void setInputParameters(Map<String, Object> parameters) {

            }

            @Override
            public void validate() {

            }

            @Override
            public Map<String, Object> execute() {
                return Collections.singletonMap("result", "resultValue");
            }

            @Override
            public void connect() {

            }

            @Override
            public void disconnect() {

            }
        };
        final ConnectorExecutionResult result = connectorExecutorImpl.execute(connector,
                Collections.singletonMap("key", "value"), Thread.currentThread().getContextClassLoader())
                .get(100, TimeUnit.MILLISECONDS);

        assertThat(result.getOutputs().size()).isEqualTo(1);
        assertThat(result.getOutputs().get("result")).isEqualTo("resultValue");
    }

    @Test(expected = SConnectorException.class)
    public void should_execute_throw_exception_when_not_started() throws Exception {
        // given
        connectorExecutorImpl.stop();
        // when
        connectorExecutorImpl.execute(connector, Collections.singletonMap("key", "value"),
                Thread.currentThread().getContextClassLoader());
    }

    @Test
    public void should_disconnect_call_disconnect_on_connector() throws Exception {
        // when
        connectorExecutorImpl.disconnect(connector);
        // then
        verify(connector).disconnect();
    }

    @Test
    public void should_disconnect_rethrow_connector_exceptions() throws Exception {
        // given
        final SConnectorException exception = new SConnectorException("myException");
        doThrow(exception).when(connector).disconnect();
        // when
        try {
            connectorExecutorImpl.disconnect(connector);
            fail("should have thrown the exception");
        } catch (final SConnectorException e) {
            // then
            assertThat(e).isEqualTo(exception);
        }
    }

    @Test
    public void should_disconnectSilently_only_logException() throws Exception {
        // given
        final SConnectorException exception = new SConnectorException("myException");
        doThrow(exception).when(connector).disconnect();
        // when
        systemOutRule.clearLog();
        connectorExecutorImpl.disconnectSilently(connector);
        // then
        assertThat(systemOutRule.getLog())
                .contains("An error occurred while disconnecting the connector: " + connector);
    }

    @Test
    public void should_stop_await_termination_of_thread_pool() {
        ExecutorService executorService = connectorExecutorImpl.getExecutorService();

        connectorExecutorImpl.stop();

        assertThat(executorService.isShutdown()).isTrue();
    }

    @Test
    public void pause_should_await_termination_of_thread_pool() {
        ExecutorService executorService = connectorExecutorImpl.getExecutorService();

        connectorExecutorImpl.stop();

        assertThat(executorService.isShutdown()).isTrue();
    }

    @Test
    public void start_should_await_termination_of_thread_pool() {
        // when
        // start in before

        // then
        assertThat(connectorExecutorImpl.getExecutorService()).as("The executor service must be not null.").isNotNull();
    }

    @Test
    public void resume_should_await_termination_of_thread_pool() {
        // when
        connectorExecutorImpl.pause();
        connectorExecutorImpl.resume();

        // then
        assertThat(connectorExecutorImpl.getExecutorService()).as("The executor service must be not null.").isNotNull();
    }

    @Test
    public void should_update_connectors_counters_when_adding_a_connector_with_immediate_execution() throws Exception {
        //when:
        executeAConnector();
        TimeUnit.MILLISECONDS.sleep(50); // give some time to consider the connector to process

        //then:
        assertThat(meterRegistry.find(ConnectorExecutorImpl.NUMBER_OF_CONNECTORS_EXECUTED).counter().count())
                .as("Executed connectors number").isEqualTo(1);
        assertThat(meterRegistry.find(ConnectorExecutorImpl.NUMBER_OF_CONNECTORS_RUNNING).gauge().value())
                .as("Running connectors number").isEqualTo(0);
        assertThat(meterRegistry.find(ConnectorExecutorImpl.NUMBER_OF_CONNECTORS_PENDING).gauge().value())
                .as("Pending connectors number").isEqualTo(0);
    }

    @Test
    public void should_reset_and_have_counters_after_pause_and_resume() throws Exception {
        //when:
        executeAConnector();
        TimeUnit.MILLISECONDS.sleep(50); // give some time to consider the connector to process
        assertThat(meterRegistry.find(ConnectorExecutorImpl.NUMBER_OF_CONNECTORS_EXECUTED).counter().count())
                .as("Executed connectors number").isEqualTo(1);
        assertThat(meterRegistry.find("executor.completed").functionCounter().count()).isEqualTo(1);
        connectorExecutorImpl.pause();

        assertThat(meterRegistry.getMeters()).hasSize(0);

        connectorExecutorImpl.resume();
        executeAConnector();
        executeAConnector();
        TimeUnit.MILLISECONDS.sleep(50); // give some time to consider the connector to process

        //then:
        assertThat(meterRegistry.find(ConnectorExecutorImpl.NUMBER_OF_CONNECTORS_EXECUTED).counter().count())
                .as("Executed connectors number").isEqualTo(2);
        assertThat(meterRegistry.find("executor.completed").functionCounter().count()).isEqualTo(2);

    }

    private void executeAConnector()
            throws InterruptedException, java.util.concurrent.ExecutionException, SConnectorException {
        connectorExecutorImpl
                .execute(new LocalSConnector(-1), new HashMap<>(), Thread.currentThread().getContextClassLoader())
                .get();
    }

    @Test
    public void should_update_connectors_counters_when_enqueuing_connectors_with_long_processing_time()
            throws Exception {
        connectorExecutorImpl.execute(new LocalSConnector(2), new HashMap<>(),
                Thread.currentThread().getContextClassLoader());
        connectorExecutorImpl.execute(new LocalSConnector(2), new HashMap<>(),
                Thread.currentThread().getContextClassLoader());
        TimeUnit.MILLISECONDS.sleep(50); // give some time to consider the connector to process

        //then:  one is in queue (only one thread to execute connectors) and one is pending
        assertThat(meterRegistry.find(ConnectorExecutorImpl.NUMBER_OF_CONNECTORS_EXECUTED).counter().count())
                .as("Executed connectors number").isEqualTo(0);
        assertThat(meterRegistry.find(ConnectorExecutorImpl.NUMBER_OF_CONNECTORS_RUNNING).gauge().value())
                .as("Running connectors number").isEqualTo(1);
        assertThat(meterRegistry.find(ConnectorExecutorImpl.NUMBER_OF_CONNECTORS_PENDING).gauge().value())
                .as("Pending connectors number").isEqualTo(1);
    }

    @Test
    public void createExecutorService_should_register_ExecutorServiceMetrics() {
        assertThat(
                meterRegistry.find("executor.pool.size")
                        .tag("name", "bonita-connector-executor")
                        .tag("tenant", String.valueOf(TENANT_ID))
                        .gauge())
                .isNotNull();
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private static class LocalSConnector extends AbstractSConnector {

        private final long sleepPeriodInSeconds;

        private LocalSConnector(long sleepPeriodInSeconds) {
            this.sleepPeriodInSeconds = sleepPeriodInSeconds;
        }

        @Override
        public void validate() {
            // do nothing
        }

        @Override
        public Map<String, Object> execute() {
            if (sleepPeriodInSeconds > 0) {
                try {
                    TimeUnit.SECONDS.sleep(sleepPeriodInSeconds);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            return new HashMap<>();
        }

        @Override
        public void connect() {
            // do nothing
        }

        @Override
        public void disconnect() {
            // do nothing
        }
    }

    @Test
    public void should_have_tenant_id_in_all_meters() {
        assertThat(meterRegistry.find(ConnectorExecutorImpl.NUMBER_OF_CONNECTORS_EXECUTED)
                .tag("tenant", String.valueOf(TENANT_ID)).counter()).isNotNull();
        assertThat(meterRegistry.find(ConnectorExecutorImpl.NUMBER_OF_CONNECTORS_PENDING)
                .tag("tenant", String.valueOf(TENANT_ID)).gauge()).isNotNull();
        assertThat(meterRegistry.find(ConnectorExecutorImpl.NUMBER_OF_CONNECTORS_RUNNING)
                .tag("tenant", String.valueOf(TENANT_ID)).gauge()).isNotNull();
    }

}
