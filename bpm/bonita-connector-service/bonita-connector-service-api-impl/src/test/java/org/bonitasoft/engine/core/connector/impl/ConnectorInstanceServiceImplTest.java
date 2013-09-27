/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
package org.bonitasoft.engine.core.connector.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.bonitasoft.engine.core.process.instance.model.SConnectorInstance;
import org.bonitasoft.engine.core.process.instance.model.SConnectorInstanceWithFailureInfo;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceLogBuilder;
import org.bonitasoft.engine.core.process.instance.model.builder.SConnectorInstanceWithFailureInfoBuilder;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.queriablelogger.model.builder.HasCRUDEAction.ActionType;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 */
@RunWith(MockitoJUnitRunner.class)
public class ConnectorInstanceServiceImplTest {

    private static final String STACK_TRACE = "stackTrace";

    private static final String EXCEPTION_MESSAGE = "exceptionMessage";

    private String message = "An exception occured during execution.";

    private String causedByMessage = "This is the caused by message.";

    @Mock
    private ReadPersistenceService readPersitenceService;

    @Mock
    private Recorder recorder;

    @Mock
    private BPMInstanceBuilders instanceBuilders;
    
    @Mock
    private SConnectorInstanceWithFailureInfoBuilder connectorWithFailureKeyProvider;

    @Mock(answer = Answers.RETURNS_MOCKS)
    private SConnectorInstanceLogBuilder connectorInstanceLogBuilder;

    @Mock
    private EventService eventService;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private SConnectorInstanceWithFailureInfo connectorInstanceWithFailureMock;

    @InjectMocks
    private ConnectorInstanceServiceImpl connectorInstanceServiceImpl;
    
    @Before
    public void setUp() {
        when(instanceBuilders.getSConnectorInstanceWithFailureInfoBuilder()).thenReturn(connectorWithFailureKeyProvider);
        when(connectorWithFailureKeyProvider.getExceptionMessageKey()).thenReturn(EXCEPTION_MESSAGE);
        when(connectorWithFailureKeyProvider.getStackTraceKey()).thenReturn(STACK_TRACE);
    }

    @Test
    public void setConnectorInstanceFailureException() throws Exception {
        Exception exception = new Exception(message);
        
        ConnectorInstanceServiceImpl mockedConnectorInstanceServiceImpl = mock(ConnectorInstanceServiceImpl.class,
                withSettings().spiedInstance(connectorInstanceServiceImpl));
        when(mockedConnectorInstanceServiceImpl.getQueriableLog(any(ActionType.class), anyString(), any(SConnectorInstance.class))).thenReturn(connectorInstanceLogBuilder);
        doCallRealMethod().when(mockedConnectorInstanceServiceImpl).setConnectorInstanceFailureException(connectorInstanceWithFailureMock, exception);
        
        //call method
        mockedConnectorInstanceServiceImpl.setConnectorInstanceFailureException(connectorInstanceWithFailureMock, exception);
        
        //verify
        ArgumentCaptor<UpdateRecord> updateRecordCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder, times(1)).recordUpdate(updateRecordCaptor.capture(), any(SUpdateEvent.class));
        UpdateRecord updateRecord = updateRecordCaptor.getValue();
        String stackTrace = (String)updateRecord.getFields().get(STACK_TRACE);

        assertEquals(message, updateRecord.getFields().get(EXCEPTION_MESSAGE));
        assertTrue(stackTrace.startsWith(Exception.class.getName() + ": " + message));
        assertTrue(stackTrace.contains(getClass().getName() + ".setConnectorInstanceFailureException"));
    }

    @Test
    public void setConnectorInstanceFailureExceptionWithCausedBy() throws Exception {
        Exception causedByException = new Exception(causedByMessage);
        Exception exception = new Exception(message, causedByException);
        
        ConnectorInstanceServiceImpl mockedConnectorInstanceServiceImpl = mock(ConnectorInstanceServiceImpl.class,
                withSettings().spiedInstance(connectorInstanceServiceImpl));
        when(mockedConnectorInstanceServiceImpl.getQueriableLog(any(ActionType.class), anyString(), any(SConnectorInstance.class))).thenReturn(connectorInstanceLogBuilder);
        doCallRealMethod().when(mockedConnectorInstanceServiceImpl).setConnectorInstanceFailureException(connectorInstanceWithFailureMock, exception);
        
        //call method
        mockedConnectorInstanceServiceImpl.setConnectorInstanceFailureException(connectorInstanceWithFailureMock, exception);
        
        //verify
        ArgumentCaptor<UpdateRecord> updateRecordCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder, times(1)).recordUpdate(updateRecordCaptor.capture(), any(SUpdateEvent.class));
        UpdateRecord updateRecord = updateRecordCaptor.getValue();
        String stackTrace = (String)updateRecord.getFields().get(STACK_TRACE);
        
        assertEquals(causedByMessage, updateRecord.getFields().get(EXCEPTION_MESSAGE));
        assertTrue(stackTrace.startsWith(Exception.class.getName() + ": " + message));
        assertTrue(stackTrace.contains(getClass().getName() + ".setConnectorInstanceFailureExceptionWithCausedBy"));
        assertTrue(stackTrace.contains("Caused by: " + Exception.class.getName() + ": " +  causedByMessage));
    }

    @Test
    public void cleanConnectorInstanceFailureException() throws Exception {
        ConnectorInstanceServiceImpl mockedConnectorInstanceServiceImpl = mock(ConnectorInstanceServiceImpl.class,
                withSettings().spiedInstance(connectorInstanceServiceImpl));
        when(mockedConnectorInstanceServiceImpl.getQueriableLog(any(ActionType.class), anyString(), any(SConnectorInstance.class))).thenReturn(connectorInstanceLogBuilder);
        doCallRealMethod().when(mockedConnectorInstanceServiceImpl).setConnectorInstanceFailureException(connectorInstanceWithFailureMock, null);
        
        //call method
        mockedConnectorInstanceServiceImpl.setConnectorInstanceFailureException(connectorInstanceWithFailureMock, null);
        
        //verify
        ArgumentCaptor<UpdateRecord> updateRecordCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder, times(1)).recordUpdate(updateRecordCaptor.capture(), any(SUpdateEvent.class));
        UpdateRecord updateRecord = updateRecordCaptor.getValue();
        String stackTrace = (String)updateRecord.getFields().get(STACK_TRACE);
        
        assertNull(updateRecord.getFields().get(EXCEPTION_MESSAGE));
        assertNull(stackTrace);
    }
    
    @Test
    public void setConnectorInstanceFailureExceptionMessageGreaterThen255() throws Exception {
        String messageToRepeat = "This is a message repeated many times. ";
        StringBuilder stb = new StringBuilder();
        int currentLength = 0;
        while (currentLength < 256) {
            stb.append(messageToRepeat);
            currentLength += messageToRepeat.length();
        }
        String longMessage = stb.toString();
        
        Exception exception = new Exception(longMessage);
        
        ConnectorInstanceServiceImpl mockedConnectorInstanceServiceImpl = mock(ConnectorInstanceServiceImpl.class,
                withSettings().spiedInstance(connectorInstanceServiceImpl));
        when(mockedConnectorInstanceServiceImpl.getQueriableLog(any(ActionType.class), anyString(), any(SConnectorInstance.class))).thenReturn(connectorInstanceLogBuilder);
        doCallRealMethod().when(mockedConnectorInstanceServiceImpl).setConnectorInstanceFailureException(connectorInstanceWithFailureMock, exception);
        
        //call method
        mockedConnectorInstanceServiceImpl.setConnectorInstanceFailureException(connectorInstanceWithFailureMock, exception);
        
        //verify
        ArgumentCaptor<UpdateRecord> updateRecordCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder, times(1)).recordUpdate(updateRecordCaptor.capture(), any(SUpdateEvent.class));
        UpdateRecord updateRecord = updateRecordCaptor.getValue();
        String stackTrace = (String)updateRecord.getFields().get(STACK_TRACE);

        String retrievedMessage = (String) updateRecord.getFields().get(EXCEPTION_MESSAGE);
        assertEquals(longMessage.substring(0, 255), retrievedMessage);
        assertTrue(stackTrace.startsWith(Exception.class.getName() + ": " + longMessage));
        assertTrue(stackTrace.contains(getClass().getName() + ".setConnectorInstanceFailureExceptionMessageGreaterThen255"));
    }

}
