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
package org.bonitasoft.engine.core.connector.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectorInstanceServiceImplTest {

    private static final String STACK_TRACE = "stackTrace";

    private static final String EXCEPTION_MESSAGE = "exceptionMessage";

    private final String message = "An exception occurred during execution.";

    @Mock
    private ReadPersistenceService readPersistenceService;

    @Mock
    private Recorder recorder;

    @Mock
    private EventService eventService;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private SConnectorInstanceWithFailureInfo connectorInstanceWithFailureMock;

    @InjectMocks
    private ConnectorInstanceServiceImpl connectorInstanceServiceImpl;

    @Test
    public void should_persist_stack_trace_on_failure() throws Exception {
        SConnectorInstanceWithFailureInfo connectorInstanceWithFailure = new SConnectorInstanceWithFailureInfo();
        connectorInstanceServiceImpl.setConnectorInstanceFailureException(connectorInstanceWithFailure,
                new Exception("Root cause"));
        verify(recorder).recordUpdate(argThat(r -> r.getFields().get("exceptionMessage").equals("Root cause") &&
                ((String) r.getFields().get("stackTrace")).startsWith("java.lang.Exception: Root cause\n" +
                        "\tat org.bonitasoft.engine.core.connector.impl.ConnectorInstanceServiceImplTest.should_persist_stack_trace_on_failure(ConnectorInstanceServiceImplTest.java:")),
                any());
    }

    @Test
    public void should_not_fail_when_persisting_stack_trace_on_failure() throws Exception {
        SConnectorInstanceWithFailureInfo connectorInstanceWithFailure = new SConnectorInstanceWithFailureInfo();

        Exception mockedException = mock(Exception.class);
        when(mockedException.getMessage()).thenReturn("the message of the exception");
        when(mockedException.toString()).thenThrow(new AbstractMethodError());
        connectorInstanceServiceImpl.setConnectorInstanceFailureException(connectorInstanceWithFailure,
                new Exception("wrapping exception", mockedException));

        String stacktraceStart = "Unable to retrieve stacktrace, exceptions were:\n" +
                " * java.lang.Exception: wrapping exception\n" +
                " * ";
        String stacktraceEnd = ": the message of the exception\n";

        verify(recorder).recordUpdate(
                argThat(r -> r.getFields().get("exceptionMessage").equals("the message of the exception") &&
                        ((String) r.getFields().get("stackTrace")).startsWith(stacktraceStart) &&
                        ((String) r.getFields().get("stackTrace")).endsWith(stacktraceEnd)),
                any());
    }

    @Test
    public void setConnectorInstanceFailureExceptionWithNullMessage() throws Exception {
        final Exception exception = new Exception();

        //call method
        connectorInstanceServiceImpl.setConnectorInstanceFailureException(connectorInstanceWithFailureMock, exception);

        //verify
        final ArgumentCaptor<UpdateRecord> updateRecordCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder, times(1)).recordUpdate(updateRecordCaptor.capture(), nullable(String.class));
        final UpdateRecord updateRecord = updateRecordCaptor.getValue();
        final String stackTrace = (String) updateRecord.getFields().get(STACK_TRACE);

        assertNull(updateRecord.getFields().get(EXCEPTION_MESSAGE));
        assertTrue(stackTrace.startsWith(Exception.class.getName()));
        assertTrue(stackTrace.contains(getClass().getName() + ".setConnectorInstanceFailureExceptionWithNullMessage"));
    }

    @Test
    public void setConnectorInstanceFailureExceptionWithCausedBy() throws Exception {
        String causedByMessage = "This is the caused by message.";
        final Exception causedByException = new Exception(causedByMessage);
        final Exception exception = new Exception(message, causedByException);

        //call method
        connectorInstanceServiceImpl.setConnectorInstanceFailureException(connectorInstanceWithFailureMock, exception);

        //verify
        final ArgumentCaptor<UpdateRecord> updateRecordCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder, times(1)).recordUpdate(updateRecordCaptor.capture(), nullable(String.class));
        final UpdateRecord updateRecord = updateRecordCaptor.getValue();
        final String stackTrace = (String) updateRecord.getFields().get(STACK_TRACE);

        assertEquals(causedByMessage, updateRecord.getFields().get(EXCEPTION_MESSAGE));
        assertTrue(stackTrace.startsWith(Exception.class.getName() + ": " + message));
        assertTrue(stackTrace.contains(getClass().getName() + ".setConnectorInstanceFailureExceptionWithCausedBy"));
        assertTrue(stackTrace.contains("Caused by: " + Exception.class.getName() + ": " + causedByMessage));
    }

    @Test
    public void cleanConnectorInstanceFailureException() throws Exception {
        //call method
        connectorInstanceServiceImpl.setConnectorInstanceFailureException(connectorInstanceWithFailureMock, null);

        //verify
        final ArgumentCaptor<UpdateRecord> updateRecordCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder, times(1)).recordUpdate(updateRecordCaptor.capture(), nullable(String.class));
        final UpdateRecord updateRecord = updateRecordCaptor.getValue();
        final String stackTrace = (String) updateRecord.getFields().get(STACK_TRACE);

        assertNull(updateRecord.getFields().get(EXCEPTION_MESSAGE));
        assertNull(stackTrace);
    }

    @Test
    public void setConnectorInstanceFailureExceptionMessageGreaterThen255() throws Exception {
        final String messageToRepeat = "This is a message repeated many times. ";
        final StringBuilder stb = new StringBuilder();
        int currentLength = 0;
        while (currentLength < 256) {
            stb.append(messageToRepeat);
            currentLength += messageToRepeat.length();
        }
        final String longMessage = stb.toString();

        final Exception exception = new Exception(longMessage);

        //call method
        connectorInstanceServiceImpl.setConnectorInstanceFailureException(connectorInstanceWithFailureMock, exception);

        //verify
        final ArgumentCaptor<UpdateRecord> updateRecordCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder, times(1)).recordUpdate(updateRecordCaptor.capture(), nullable(String.class));
        final UpdateRecord updateRecord = updateRecordCaptor.getValue();
        final String stackTrace = (String) updateRecord.getFields().get(STACK_TRACE);

        final String retrievedMessage = (String) updateRecord.getFields().get(EXCEPTION_MESSAGE);
        assertEquals(longMessage.substring(0, 255), retrievedMessage);
        assertTrue(stackTrace.startsWith(Exception.class.getName() + ": " + longMessage));
        assertTrue(stackTrace
                .contains(getClass().getName() + ".setConnectorInstanceFailureExceptionMessageGreaterThen255"));
    }

    @Test
    public void getConnectorInstanceWithFailureInfo_should_return_the_result_of_select_list() throws Exception {
        //given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", 1L);
        parameters.put("containerType", "flowNode");
        parameters.put("state", "failed");

        List<SConnectorInstanceWithFailureInfo> connectors = Arrays.asList(
                mock(SConnectorInstanceWithFailureInfo.class),
                mock(SConnectorInstanceWithFailureInfo.class));
        given(
                readPersistenceService.selectList(new SelectListDescriptor<SConnectorInstanceWithFailureInfo>(
                        "getConnectorInstancesWithFailureInfoInState",
                        parameters,
                        SConnectorInstanceWithFailureInfo.class,
                        new QueryOptions(0, 100, SConnectorInstanceWithFailureInfo.class, "id", OrderByType.ASC))))
                                .willReturn(connectors);

        //when
        List<SConnectorInstanceWithFailureInfo> retrievedConnectors = connectorInstanceServiceImpl
                .getConnectorInstancesWithFailureInfo(1L, "flowNode",
                        "failed", 0, 100);

        //then
        assertThat(retrievedConnectors).isEqualTo(connectors);
    }

}
