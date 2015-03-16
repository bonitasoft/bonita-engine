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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bonitasoft.engine.connector.SConnector;
import org.bonitasoft.engine.connector.exception.SConnectorException;
import org.bonitasoft.engine.connector.impl.ConnectorExecutorImpl.ExecuteConnectorCallable;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.session.SessionService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.tracking.TimeTracker;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConnectorExecutorImplTest {

    @Mock
    private TechnicalLoggerService loggerService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private SessionService sessionService;

    @Mock
    private ExecutorService executorService;

    @Mock
    private SConnector connector;

    @Mock
    private TimeTracker timeTracker;

    private ConnectorExecutorImpl connectorExecutorImpl;

    @Before
    public void before() {
        connectorExecutorImpl = new ConnectorExecutorImpl(1, 1, loggerService, 1, 1, sessionAccessor, sessionService, timeTracker);
        doReturn(true).when(loggerService).isLoggable(any(Class.class), any(TechnicalLogSeverity.class));
    }

    @Test
    public void should_execute_submit_callable() throws Exception {
        // given
        connectorExecutorImpl.setExecutor(executorService);
        final Future<?> future = mock(Future.class);
        doReturn(future).when(executorService).submit(any(ExecuteConnectorCallable.class));
        doReturn(Collections.singletonMap("result", "resultValue")).when(future).get();
        // when
        final Map<String, Object> result = connectorExecutorImpl.execute(connector, Collections.<String, Object> singletonMap("key", "value"), Thread
                .currentThread().getContextClassLoader());

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get("result")).isEqualTo("resultValue");
    }

    @Test(expected = SConnectorException.class)
    public void should_execute_throw_exception_when_not_started() throws Exception {
        // given
        connectorExecutorImpl.setExecutor(executorService);
        connectorExecutorImpl.stop();
        // when
        connectorExecutorImpl.execute(connector, Collections.<String, Object> singletonMap("key", "value"), Thread.currentThread().getContextClassLoader());
    }

    @Test
    public void should_disconnect_call_disconnect_on_connector() throws Exception {
        // when
        connectorExecutorImpl.setExecutor(executorService);
        connectorExecutorImpl.disconnect(connector);
        // then
        verify(connector).disconnect();
    }

    @Test
    public void should_disconnect_rethrow_connector_exceptions() throws Exception {
        // given
        connectorExecutorImpl.setExecutor(executorService);
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
        connectorExecutorImpl.setExecutor(executorService);
        final SConnectorException exception = new SConnectorException("myException");
        doThrow(exception).when(connector).disconnect();
        // when
        connectorExecutorImpl.disconnectSilently(connector);
        // then
        verify(loggerService).log(ConnectorExecutorImpl.class, TechnicalLogSeverity.WARNING,
                "An error occured while disconnecting the connector: " + connector, exception);
    }

    @Test
    public void should_stop_await_termination_of_thread_pool() throws Exception {
        // Given
        connectorExecutorImpl.setExecutor(executorService);
        // when
        connectorExecutorImpl.stop();
        // then
        verify(executorService).shutdown();
        verify(executorService).awaitTermination(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void pause_should_await_termination_of_thread_pool() throws Exception {
        // Given
        connectorExecutorImpl.setExecutor(executorService);
        // when
        connectorExecutorImpl.stop();
        // then
        verify(executorService).shutdown();
        verify(executorService).awaitTermination(anyLong(), any(TimeUnit.class));
    }

    @Test
    public void start_should_await_termination_of_thread_pool() {
        // when
        connectorExecutorImpl.start();

        // then
        assertThat(connectorExecutorImpl.getExecutorService()).as("The executor service must be not null.").isNotNull();
    }

    @Test
    public void resume_should_await_termination_of_thread_pool() {
        // when
        connectorExecutorImpl.resume();

        // then
        assertThat(connectorExecutorImpl.getExecutorService()).as("The executor service must be not null.").isNotNull();
    }

}
