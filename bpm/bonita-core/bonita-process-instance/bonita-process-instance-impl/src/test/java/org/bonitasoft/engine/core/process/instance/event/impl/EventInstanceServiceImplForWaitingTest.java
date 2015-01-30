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
package org.bonitasoft.engine.core.process.instance.event.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SWaitingMessageEventImpl;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SWaitingSignalEventImpl;
import org.bonitasoft.engine.core.process.instance.model.event.impl.SIntermediateCatchEventInstanceImpl;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.services.SPersistenceException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class EventInstanceServiceImplForWaitingTest {

    @Mock
    private ArchiveService archiveService;

    @Mock
    private EventService eventService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private Recorder recorder;

    @Spy
    @InjectMocks
    private EventInstanceServiceImpl eventInstanceServiceImpl;

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#createWaitingEvent(org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent)}
     * .
     */
    @Test
    public final void createWaitingEvent_should_create_waiting_instance() throws Exception {
        // Given
        final SWaitingSignalEventImpl sWaitingEventImpl = new SWaitingSignalEventImpl();
        final InsertRecord insertRecord = new InsertRecord(sWaitingEventImpl);

        // When
        eventInstanceServiceImpl.createWaitingEvent(sWaitingEventImpl);

        // Then
        verify(recorder).recordInsert(eq(insertRecord), any(SInsertEvent.class));
    }

    @Test(expected = SWaitingEventCreationException.class)
    public final void createWaitingEvent_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SWaitingMessageEventImpl sWaitingEventImpl = new SWaitingMessageEventImpl();
        doThrow(new SRecorderException("")).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

        // When
        eventInstanceServiceImpl.createWaitingEvent(sWaitingEventImpl);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#deleteWaitingEvent(org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent)}
     * .
     */
    @Test
    public final void deleteWaitingEvent_should_delete_waiting_event() throws Exception {
        // Given
        final SWaitingMessageEventImpl sWaitingEventImpl = new SWaitingMessageEventImpl();
        final DeleteRecord insertRecord = new DeleteRecord(sWaitingEventImpl);

        // When
        eventInstanceServiceImpl.deleteWaitingEvent(sWaitingEventImpl);

        // Then
        verify(recorder).recordDelete(eq(insertRecord), any(SDeleteEvent.class));
    }

    @Test(expected = SWaitingEventModificationException.class)
    public final void deleteWaitingEvent_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SWaitingSignalEventImpl sWaitingEventImpl = new SWaitingSignalEventImpl();
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        // When
        eventInstanceServiceImpl.deleteWaitingEvent(sWaitingEventImpl);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#deleteWaitingEvents(org.bonitasoft.engine.core.process.instance.model.SFlowNodeInstance)}
     * .
     */
    @Test
    public final void deleteWaitingEvents_should_delete_waiting_events() throws Exception {
        // Given
        final SIntermediateCatchEventInstanceImpl sIntermediateCatchEventInstanceImpl = new SIntermediateCatchEventInstanceImpl();
        final SWaitingMessageEventImpl waitingMessageEventImpl = new SWaitingMessageEventImpl();
        doReturn(Arrays.asList(waitingMessageEventImpl)).doReturn(Arrays.asList()).when(eventInstanceServiceImpl)
                .searchWaitingEvents(eq(SWaitingEvent.class), any(QueryOptions.class));
        doNothing().when(eventInstanceServiceImpl).deleteWaitingEvent(waitingMessageEventImpl);

        // When
        eventInstanceServiceImpl.deleteWaitingEvents(sIntermediateCatchEventInstanceImpl);

        // Then
        verify(eventInstanceServiceImpl, times(2)).searchWaitingEvents(eq(SWaitingEvent.class), any(QueryOptions.class));
        verify(eventInstanceServiceImpl).deleteWaitingEvent(waitingMessageEventImpl);
    }

    @Test(expected = SWaitingEventModificationException.class)
    public final void deleteWaitingEvents_should_throw_exception_when_cant_delete_waiting_event() throws Exception {
        // Given
        final SIntermediateCatchEventInstanceImpl sIntermediateCatchEventInstanceImpl = new SIntermediateCatchEventInstanceImpl();
        final SWaitingSignalEventImpl waitingMessageEventImpl = new SWaitingSignalEventImpl();
        doReturn(Arrays.asList(waitingMessageEventImpl)).when(eventInstanceServiceImpl).searchWaitingEvents(eq(SWaitingEvent.class), any(QueryOptions.class));
        doThrow(new SWaitingEventModificationException(new Exception(""))).when(eventInstanceServiceImpl).deleteWaitingEvent(waitingMessageEventImpl);

        // When
        eventInstanceServiceImpl.deleteWaitingEvents(sIntermediateCatchEventInstanceImpl);
    }

    @Test(expected = SBonitaReadException.class)
    public final void deleteWaitingEvents_should_throw_exception_when_cant_search_waiting_event() throws Exception {
        // Given
        final SIntermediateCatchEventInstanceImpl sIntermediateCatchEventInstanceImpl = new SIntermediateCatchEventInstanceImpl();
        doThrow(new SBonitaReadException(new Exception(""))).when(eventInstanceServiceImpl).searchWaitingEvents(eq(SWaitingEvent.class),
                any(QueryOptions.class));

        // When
        eventInstanceServiceImpl.deleteWaitingEvents(sIntermediateCatchEventInstanceImpl);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#resetInProgressWaitingEvents()}.
     */
    @Test
    public final void resetInProgressWaitingEvents_should_update_waiting_events() throws Exception {
        // Given
        doReturn(3).when(persistenceService).update("resetInProgressWaitingEvents");

        // When
        final int result = eventInstanceServiceImpl.resetInProgressWaitingEvents();

        // Then
        assertEquals("Should return the result of the mock.", 3, result);
    }

    @Test(expected = SWaitingEventModificationException.class)
    public final void resetInProgressWaitingEvents_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        doThrow(new SPersistenceException("")).when(persistenceService).update("resetInProgressWaitingEvents");

        // When
        eventInstanceServiceImpl.resetInProgressWaitingEvents();
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#getNumberOfWaitingEvents(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfWaitingEvents_should_return_the_number() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, SWaitingEvent.class, "id", OrderByType.ASC);
        doReturn(2L).when(persistenceService).getNumberOfEntities(SWaitingEvent.class, queryOptions, null);

        // When
        final long result = eventInstanceServiceImpl.getNumberOfWaitingEvents(SWaitingEvent.class, queryOptions);

        // Then
        assertEquals("Should be equals to the result of the mock.", 2L, result);
    }

    @Test(expected = SBonitaReadException.class)
    public final void getNumberOfWaitingEvents_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, SWaitingEvent.class, "id", OrderByType.ASC);
        doThrow(new SBonitaReadException("")).when(persistenceService).getNumberOfEntities(SWaitingEvent.class, queryOptions, null);

        // When
        eventInstanceServiceImpl.getNumberOfWaitingEvents(SWaitingEvent.class, queryOptions);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#searchStartWaitingEvents(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchStartWaitingEvents_should_return_the_list() throws Exception {
        // Given
        final long processDefinitionId = 9L;
        final QueryOptions queryOptions = new QueryOptions(0, 100, SWaitingEvent.class, "id", OrderByType.ASC);
        final List<SWaitingSignalEventImpl> sWaitingSignalEventImpls = Arrays.asList(new SWaitingSignalEventImpl());
        final SelectListDescriptor<SWaitingEvent> descriptor = SelectDescriptorBuilder.getStartWaitingEvents(processDefinitionId, queryOptions);
        doReturn(sWaitingSignalEventImpls).when(persistenceService).selectList(descriptor);

        // When
        final List<SWaitingEvent> result = eventInstanceServiceImpl.searchStartWaitingEvents(processDefinitionId, queryOptions);

        // Then
        assertEquals("Should be equals to the result of the mock.", sWaitingSignalEventImpls, result);
    }

    @Test(expected = SBonitaReadException.class)
    public final void searchStartWaitingEvents_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final long processDefinitionId = 9L;
        final QueryOptions queryOptions = new QueryOptions(0, 100, SWaitingEvent.class, "id", OrderByType.ASC);
        final SelectListDescriptor<SWaitingEvent> descriptor = SelectDescriptorBuilder.getStartWaitingEvents(processDefinitionId, queryOptions);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(descriptor);

        // When
        eventInstanceServiceImpl.searchStartWaitingEvents(processDefinitionId, queryOptions);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#getWaitingMessage(long)}.
     */
    @Test
    public final void getWaitingMessage_should_return_waiting_message() throws Exception {
        // Given
        final long waitingMessageId = 63L;
        final SWaitingMessageEventImpl sWaitingMessageEventImpl = new SWaitingMessageEventImpl();
        final SelectByIdDescriptor<SWaitingMessageEvent> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(SWaitingMessageEvent.class,
                "WaitingMessageEvent", waitingMessageId);
        doReturn(sWaitingMessageEventImpl).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        final SWaitingMessageEvent result = eventInstanceServiceImpl.getWaitingMessage(waitingMessageId);

        // Then
        assertEquals("Should return the result of the mock.", sWaitingMessageEventImpl, result);
    }

    @Test
    public final void getWaitingMessage_should_return_null_if_doesnt_exist() throws Exception {
        // Given
        final long waitingMessageId = 63L;
        final SelectByIdDescriptor<SWaitingMessageEvent> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(SWaitingMessageEvent.class,
                "WaitingMessageEvent", waitingMessageId);
        doReturn(null).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        final SWaitingMessageEvent result = eventInstanceServiceImpl.getWaitingMessage(waitingMessageId);

        // Then
        assertNull("Should return the result of the mock.", result);
    }

    @Test(expected = SWaitingEventReadException.class)
    public final void getWaitingMessage_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final long waitingMessageId = 63L;
        final SelectByIdDescriptor<SWaitingMessageEvent> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(SWaitingMessageEvent.class,
                "WaitingMessageEvent", waitingMessageId);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        eventInstanceServiceImpl.getWaitingMessage(waitingMessageId);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#getWaitingSignalEvents(java.lang.String, int, int)}.
     */
    @Test
    public final void getWaitingSignalEvents_should_return_waiting_signal_event() throws Exception {
        // Given
        final String signalName = "name";
        final List<SWaitingSignalEventImpl> waitingSignalEventImpls = Arrays.asList(new SWaitingSignalEventImpl());
        final SelectListDescriptor<SWaitingSignalEvent> selectDescriptor = SelectDescriptorBuilder.getListeningSignals(signalName, 0, 100);
        doReturn(waitingSignalEventImpls).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SWaitingSignalEvent> result = eventInstanceServiceImpl.getWaitingSignalEvents(signalName, 0, 100);

        // Then
        assertEquals("Should return the result of the mock.", waitingSignalEventImpls, result);
    }

    @Test
    public final void getWaitingSignalEvents_should_return_empty_list_if_doesnt_exist() throws Exception {
        // Given
        final String signalName = "name";
        final SelectListDescriptor<SWaitingSignalEvent> selectDescriptor = SelectDescriptorBuilder.getListeningSignals(signalName, 0, 100);
        doReturn(Arrays.asList()).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SWaitingSignalEvent> result = eventInstanceServiceImpl.getWaitingSignalEvents(signalName, 0, 100);

        // Then
        assertTrue("The result must be empty.", result.isEmpty());
    }

    @Test(expected = SEventTriggerInstanceReadException.class)
    public final void getEventTriggerInstances_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final String signalName = "name";
        final SelectListDescriptor<SWaitingSignalEvent> selectDescriptor = SelectDescriptorBuilder.getListeningSignals(signalName, 0, 100);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(selectDescriptor);

        // When
        eventInstanceServiceImpl.getWaitingSignalEvents(signalName, 0, 100);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#searchWaitingEvents(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchWaitingEvents_should_return_the_list() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, SWaitingEvent.class, "id", OrderByType.ASC);
        final List<SWaitingSignalEventImpl> sWaitingSignalEventImpls = Arrays.asList(new SWaitingSignalEventImpl());
        doReturn(sWaitingSignalEventImpls).when(persistenceService).searchEntity(SWaitingEvent.class, queryOptions, null);

        // When
        final List<SWaitingEvent> result = eventInstanceServiceImpl.searchWaitingEvents(SWaitingEvent.class, queryOptions);

        // Then
        assertEquals("Should be equals to the result of the mock.", sWaitingSignalEventImpls, result);
    }

    @Test(expected = SBonitaReadException.class)
    public final void searchWaitingEvents_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, SWaitingEvent.class, "id", OrderByType.ASC);
        doThrow(new SBonitaReadException("")).when(persistenceService).searchEntity(SWaitingEvent.class, queryOptions, null);

        // When
        eventInstanceServiceImpl.searchWaitingEvents(SWaitingEvent.class, queryOptions);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#updateWaitingMessage(org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updateWaitingMessage_should_update_waiting_message() throws Exception {
        // Given
        final SWaitingMessageEventImpl waitingMessageEventImpl = new SWaitingMessageEventImpl();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(waitingMessageEventImpl, descriptor);

        // When
        eventInstanceServiceImpl.updateWaitingMessage(waitingMessageEventImpl, descriptor);

        // Then
        verify(recorder).recordUpdate(eq(updateRecord), any(SUpdateEvent.class));
    }

    @Test(expected = SWaitingEventModificationException.class)
    public final void updateWaitingMessage_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SWaitingMessageEventImpl waitingMessageEventImpl = new SWaitingMessageEventImpl();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        doThrow(new SRecorderException("")).when(recorder).recordUpdate(any(UpdateRecord.class), any(SUpdateEvent.class));

        // When
        eventInstanceServiceImpl.updateWaitingMessage(waitingMessageEventImpl, descriptor);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#getBoundaryWaitingErrorEvent(long, java.lang.String)}.
     */
    // @Test
    // public final void testGetBoundaryWaitingErrorEvent() throws Exception {
    // // TODO
    // throw new RuntimeException("not yet implemented");
    // }
}
