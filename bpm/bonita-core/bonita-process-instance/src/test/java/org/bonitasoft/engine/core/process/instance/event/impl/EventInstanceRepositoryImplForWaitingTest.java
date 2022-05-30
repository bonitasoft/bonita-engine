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
package org.bonitasoft.engine.core.process.instance.event.impl;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceRepository;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.*;
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
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class EventInstanceRepositoryImplForWaitingTest {

    @Mock
    private ArchiveService archiveService;

    @Mock
    private EventService eventService;
    @Mock
    private PersistenceService persistenceService;

    @Mock
    private Recorder recorder;

    @Spy
    @InjectMocks
    private EventInstanceRepositoryImpl eventInstanceRepository;

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#createWaitingEvent(org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent)}
     * .
     */
    @Test
    public final void createWaitingEvent_should_create_waiting_instance() throws Exception {
        // Given
        final SWaitingSignalEvent sWaitingEventImpl = new SWaitingSignalEvent();
        final InsertRecord insertRecord = new InsertRecord(sWaitingEventImpl);

        // When
        eventInstanceRepository.createWaitingEvent(sWaitingEventImpl);

        // Then
        verify(recorder).recordInsert(eq(insertRecord), nullable(String.class));
    }

    @Test(expected = SWaitingEventCreationException.class)
    public final void createWaitingEvent_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SWaitingMessageEvent sWaitingEventImpl = new SWaitingMessageEvent();
        doThrow(new SRecorderException("")).when(recorder).recordInsert(any(InsertRecord.class),
                nullable(String.class));

        // When
        eventInstanceRepository.createWaitingEvent(sWaitingEventImpl);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#deleteWaitingEvent(org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingEvent)}
     * .
     */
    @Test
    public final void deleteWaitingEvent_should_delete_waiting_event() throws Exception {
        // Given
        final SWaitingMessageEvent sWaitingEventImpl = new SWaitingMessageEvent();
        final DeleteRecord insertRecord = new DeleteRecord(sWaitingEventImpl);

        // When
        eventInstanceRepository.deleteWaitingEvent(sWaitingEventImpl);

        // Then
        verify(recorder).recordDelete(eq(insertRecord), nullable(String.class));
    }

    @Test(expected = SWaitingEventModificationException.class)
    public final void deleteWaitingEvent_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SWaitingSignalEvent sWaitingEventImpl = new SWaitingSignalEvent();
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class),
                nullable(String.class));

        // When
        eventInstanceRepository.deleteWaitingEvent(sWaitingEventImpl);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#resetInProgressWaitingEvents()}.
     */
    @Test
    public final void resetInProgressWaitingEvents_should_update_waiting_events() throws Exception {
        // Given
        doReturn(3).when(persistenceService).update("resetInProgressWaitingEvents");

        // When
        final int result = eventInstanceRepository.resetInProgressWaitingEvents();

        // Then
        assertEquals("Should return the result of the mock.", 3, result);
    }

    @Test(expected = SWaitingEventModificationException.class)
    public final void resetInProgressWaitingEvents_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        doThrow(new SPersistenceException("")).when(persistenceService).update("resetInProgressWaitingEvents");

        // When
        eventInstanceRepository.resetInProgressWaitingEvents();
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#getNumberOfWaitingEvents(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfWaitingEvents_should_return_the_number() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, SWaitingEvent.class, "id", OrderByType.ASC);
        doReturn(2L).when(persistenceService).getNumberOfEntities(SWaitingEvent.class, queryOptions, null);

        // When
        final long result = eventInstanceRepository.getNumberOfWaitingEvents(SWaitingEvent.class, queryOptions);

        // Then
        assertEquals("Should be equals to the result of the mock.", 2L, result);
    }

    @Test(expected = SBonitaReadException.class)
    public final void getNumberOfWaitingEvents_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, SWaitingEvent.class, "id", OrderByType.ASC);
        doThrow(new SBonitaReadException("")).when(persistenceService).getNumberOfEntities(SWaitingEvent.class,
                queryOptions, null);

        // When
        eventInstanceRepository.getNumberOfWaitingEvents(SWaitingEvent.class, queryOptions);
    }

    /**
     * Test method for
     * {@link EventInstanceRepository#getStartWaitingEventsOfProcessDefinition(long)}
     * .
     */
    @Test
    public final void searchStartWaitingEvents_should_return_the_list() throws Exception {
        // Given
        final List<SWaitingSignalEvent> sWaitingSignalEvents = Arrays.asList(new SWaitingSignalEvent());
        // event sub-processes are not returned:
        doReturn(sWaitingSignalEvents).when(persistenceService).selectList(any());

        // When
        final List<SWaitingEvent> result = eventInstanceRepository
                .getStartWaitingEventsOfProcessDefinition(9L);

        // Then
        assertEquals("Should be equals to the result of the mock.", sWaitingSignalEvents, result);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#getWaitingMessage(long)}.
     */
    @Test
    public final void getWaitingMessage_should_return_waiting_message() throws Exception {
        // Given
        final long waitingMessageId = 63L;
        final SWaitingMessageEvent sWaitingMessageEvent = new SWaitingMessageEvent();
        final SelectByIdDescriptor<SWaitingMessageEvent> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(
                SWaitingMessageEvent.class,
                "WaitingMessageEvent", waitingMessageId);
        doReturn(sWaitingMessageEvent).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        final SWaitingMessageEvent result = eventInstanceRepository.getWaitingMessage(waitingMessageId);

        // Then
        assertEquals("Should return the result of the mock.", sWaitingMessageEvent, result);
    }

    @Test
    public final void getWaitingMessage_should_return_null_if_doesnt_exist() throws Exception {
        // Given
        final long waitingMessageId = 63L;
        final SelectByIdDescriptor<SWaitingMessageEvent> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(
                SWaitingMessageEvent.class,
                "WaitingMessageEvent", waitingMessageId);
        doReturn(null).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        final SWaitingMessageEvent result = eventInstanceRepository.getWaitingMessage(waitingMessageId);

        // Then
        assertNull("Should return the result of the mock.", result);
    }

    @Test(expected = SWaitingEventReadException.class)
    public final void getWaitingMessage_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final long waitingMessageId = 63L;
        final SelectByIdDescriptor<SWaitingMessageEvent> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(
                SWaitingMessageEvent.class,
                "WaitingMessageEvent", waitingMessageId);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        eventInstanceRepository.getWaitingMessage(waitingMessageId);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#getWaitingSignalEvents(java.lang.String, int, int)}.
     */
    @Test
    public final void getWaitingSignalEvents_should_return_waiting_signal_event() throws Exception {
        // Given
        final String signalName = "name";
        final List<SWaitingSignalEvent> waitingSignalEventImpls = Arrays.asList(new SWaitingSignalEvent());
        final SelectListDescriptor<SWaitingSignalEvent> selectDescriptor = SelectDescriptorBuilder
                .getListeningSignals(signalName, 0, 100);
        doReturn(waitingSignalEventImpls).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SWaitingSignalEvent> result = eventInstanceRepository.getWaitingSignalEvents(signalName, 0, 100);

        // Then
        assertEquals("Should return the result of the mock.", waitingSignalEventImpls, result);
    }

    @Test
    public final void getWaitingSignalEvents_should_return_empty_list_if_doesnt_exist() throws Exception {
        // Given
        final String signalName = "name";
        final SelectListDescriptor<SWaitingSignalEvent> selectDescriptor = SelectDescriptorBuilder
                .getListeningSignals(signalName, 0, 100);
        doReturn(Arrays.asList()).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SWaitingSignalEvent> result = eventInstanceRepository.getWaitingSignalEvents(signalName, 0, 100);

        // Then
        assertTrue("The result must be empty.", result.isEmpty());
    }

    @Test(expected = SEventTriggerInstanceReadException.class)
    public final void getEventTriggerInstances_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final String signalName = "name";
        final SelectListDescriptor<SWaitingSignalEvent> selectDescriptor = SelectDescriptorBuilder
                .getListeningSignals(signalName, 0, 100);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(selectDescriptor);

        // When
        eventInstanceRepository.getWaitingSignalEvents(signalName, 0, 100);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#searchWaitingEvents(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchWaitingEvents_should_return_the_list() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, SWaitingEvent.class, "id", OrderByType.ASC);
        final List<SWaitingSignalEvent> sWaitingSignalEvents = Arrays.asList(new SWaitingSignalEvent());
        doReturn(sWaitingSignalEvents).when(persistenceService).searchEntity(SWaitingEvent.class, queryOptions, null);

        // When
        final List<SWaitingEvent> result = eventInstanceRepository.searchWaitingEvents(SWaitingEvent.class,
                queryOptions);

        // Then
        assertEquals("Should be equals to the result of the mock.", sWaitingSignalEvents, result);
    }

    @Test(expected = SBonitaReadException.class)
    public final void searchWaitingEvents_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, SWaitingEvent.class, "id", OrderByType.ASC);
        doThrow(new SBonitaReadException("")).when(persistenceService).searchEntity(SWaitingEvent.class, queryOptions,
                null);

        // When
        eventInstanceRepository.searchWaitingEvents(SWaitingEvent.class, queryOptions);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#updateWaitingMessage(org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updateWaitingMessage_should_update_waiting_message() throws Exception {
        // Given
        final SWaitingMessageEvent waitingMessageEventImpl = new SWaitingMessageEvent();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(waitingMessageEventImpl, descriptor);

        // When
        eventInstanceRepository.updateWaitingMessage(waitingMessageEventImpl, descriptor);

        // Then
        verify(recorder).recordUpdate(eq(updateRecord), nullable(String.class));
    }

    @Test(expected = SWaitingEventModificationException.class)
    public final void updateWaitingMessage_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SWaitingMessageEvent waitingMessageEventImpl = new SWaitingMessageEvent();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        doThrow(new SRecorderException("")).when(recorder).recordUpdate(any(UpdateRecord.class),
                nullable(String.class));

        // When
        eventInstanceRepository.updateWaitingMessage(waitingMessageEventImpl, descriptor);
    }

}
