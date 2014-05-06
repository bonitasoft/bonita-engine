package org.bonitasoft.engine.connector.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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

    private ConnectorExecutorImpl connectorExecutorImpl;

    @Before
    public void before() {
        doReturn(true).when(loggerService).isLoggable(any(Class.class), any(TechnicalLogSeverity.class));
        final TimeTracker timeTracker = mock(TimeTracker.class);
        connectorExecutorImpl = new ConnectorExecutorImpl(0, 0, loggerService, 0, 0, sessionAccessor, sessionService, timeTracker);
        connectorExecutorImpl.useExecutor(executorService);
    }

    @Test
    public void should_execute_submit_callable() throws Exception {
        // given
        Future<?> future = mock(Future.class);
        doReturn(future).when(executorService).submit(any(ExecuteConnectorCallable.class));
        doReturn(Collections.singletonMap("result", "resultValue")).when(future).get();
        // when
        Map<String, Object> result = connectorExecutorImpl.execute(connector, Collections.<String, Object> singletonMap("key", "value"));

        // then
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get("result")).isEqualTo("resultValue");
    }

    @Test(expected = SConnectorException.class)
    public void should_execute_throw_exception_when_not_started() throws Exception {
        // given
        connectorExecutorImpl.stop();
        // when
        connectorExecutorImpl.execute(connector, Collections.<String, Object> singletonMap("key", "value"));
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
        SConnectorException exception = new SConnectorException("myException");
        doThrow(exception).when(connector).disconnect();
        // when
        try {
            connectorExecutorImpl.disconnect(connector);
            fail("should have thrown the exception");
        } catch (SConnectorException e) {
            // then
            assertThat(e).isEqualTo(exception);
        }
    }

    @Test
    public void should_disconnectSilently_only_logException() throws Exception {
        // given
        SConnectorException exception = new SConnectorException("myException");
        doThrow(exception).when(connector).disconnect();
        // when
        connectorExecutorImpl.disconnectSilently(connector);
        // then
        verify(loggerService).log(ConnectorExecutorImpl.class, TechnicalLogSeverity.WARNING,
                "An error occured while disconnecting the connector: " + connector, exception);
    }

    @Test
    public void should_stop_await_termination_of_thread_ppol() throws Exception {
        // when
        connectorExecutorImpl.stop();
        // then
        verify(executorService).shutdown();
        verify(executorService).awaitTermination(anyLong(), any(TimeUnit.class));
    }

}
