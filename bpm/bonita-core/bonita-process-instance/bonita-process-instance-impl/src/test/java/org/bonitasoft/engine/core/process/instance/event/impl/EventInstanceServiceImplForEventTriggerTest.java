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
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceDeletionException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.SThrowErrorEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowErrorEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowMessageEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.SThrowSignalEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.STimerEventTriggerInstanceImpl;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class EventInstanceServiceImplForEventTriggerTest {

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
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#createEventTriggerInstance(org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance)}
     * .
     */
    @Test
    public final void createEventTriggerInstance_should_create_event_trigger_instance() throws Exception {
        // Given
        final STimerEventTriggerInstanceImpl eventTriggerInstance = new STimerEventTriggerInstanceImpl();
        final InsertRecord insertRecord = new InsertRecord(eventTriggerInstance);

        // When
        eventInstanceServiceImpl.createEventTriggerInstance(eventTriggerInstance);

        // Then
        verify(recorder).recordInsert(eq(insertRecord), any(SInsertEvent.class));
    }

    @Test(expected = SEventTriggerInstanceCreationException.class)
    public final void createEventTriggerInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final STimerEventTriggerInstanceImpl eventTriggerInstance = new STimerEventTriggerInstanceImpl();
        doThrow(new SRecorderException("")).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

        // When
        eventInstanceServiceImpl.createEventTriggerInstance(eventTriggerInstance);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#deleteEventTriggerInstance(org.bonitasoft.engine.core.process.instance.model.event.trigger.SEventTriggerInstance)}
     * .
     */
    @Test
    public final void deleteEventTriggerInstance_should_delete_event_trigger_instance() throws Exception {
        // Given
        final STimerEventTriggerInstanceImpl eventTriggerInstance = new STimerEventTriggerInstanceImpl();
        final DeleteRecord insertRecord = new DeleteRecord(eventTriggerInstance);

        // When
        eventInstanceServiceImpl.deleteEventTriggerInstance(eventTriggerInstance);

        // Then
        verify(recorder).recordDelete(eq(insertRecord), any(SDeleteEvent.class));
    }

    @Test(expected = SEventTriggerInstanceDeletionException.class)
    public final void deleteEventTriggerInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final STimerEventTriggerInstanceImpl eventTriggerInstance = new STimerEventTriggerInstanceImpl();
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        // When
        eventInstanceServiceImpl.deleteEventTriggerInstance(eventTriggerInstance);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#deleteEventTriggerInstances(long)}.
     */
    @Test
    public final void deleteEventTriggerInstances_should_delete_event_trigger_instances() throws Exception {
        // Given
        final long eventInstanceId = 63L;
        final STimerEventTriggerInstanceImpl eventTriggerInstance = new STimerEventTriggerInstanceImpl();
        doReturn(Arrays.asList(eventTriggerInstance)).doReturn(Arrays.asList()).when(eventInstanceServiceImpl)
                .getEventTriggerInstances(eq(eventInstanceId), any(QueryOptions.class));
        doNothing().when(eventInstanceServiceImpl).deleteEventTriggerInstance(eventTriggerInstance);

        // When
        eventInstanceServiceImpl.deleteEventTriggerInstances(eventInstanceId);

        // Then
        verify(eventInstanceServiceImpl, times(2)).getEventTriggerInstances(eq(eventInstanceId), any(QueryOptions.class));
        verify(eventInstanceServiceImpl).deleteEventTriggerInstance(eventTriggerInstance);
    }

    @Test(expected = SEventTriggerInstanceDeletionException.class)
    public final void deleteEventTriggerInstances_should_throw_exception_when_cant_delete_event_trigger_instance() throws Exception {
        // Given
        final long eventInstanceId = 63L;
        final STimerEventTriggerInstanceImpl eventTriggerInstance = new STimerEventTriggerInstanceImpl();
        doReturn(Arrays.asList(eventTriggerInstance)).when(eventInstanceServiceImpl).getEventTriggerInstances(eq(eventInstanceId), any(QueryOptions.class));
        doThrow(new SEventTriggerInstanceDeletionException(new Exception(""))).when(eventInstanceServiceImpl).deleteEventTriggerInstance(eventTriggerInstance);

        // When
        eventInstanceServiceImpl.deleteEventTriggerInstances(eventInstanceId);
    }

    @Test(expected = SEventTriggerInstanceReadException.class)
    public final void deleteEventTriggerInstances_should_throw_exception_when_cant_get_event_trigger_instance() throws Exception {
        // Given
        final long eventInstanceId = 63L;
        doThrow(new SEventTriggerInstanceReadException(new Exception(""))).when(eventInstanceServiceImpl).getEventTriggerInstances(eq(eventInstanceId),
                any(QueryOptions.class));

        // When
        eventInstanceServiceImpl.deleteEventTriggerInstances(eventInstanceId);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#getEventTriggerInstance(long)}.
     */
    @Test
    public final void getEventTriggerInstance_should_return_event_trigger_instance() throws Exception {
        // Given
        final long eventTriggerInstanceId = 63L;
        final STimerEventTriggerInstanceImpl eventTriggerInstance = new STimerEventTriggerInstanceImpl();
        final SelectByIdDescriptor<SEventTriggerInstance> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(SEventTriggerInstance.class,
                SEventTriggerInstance.class.getSimpleName(), eventTriggerInstanceId);
        doReturn(eventTriggerInstance).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        final SEventTriggerInstance result = eventInstanceServiceImpl.getEventTriggerInstance(SEventTriggerInstance.class, eventTriggerInstanceId);

        // Then
        assertEquals("Should return the result of the mock.", eventTriggerInstance, result);
    }

    @Test
    public final void getEventTriggerInstance_should_return_null_if_doesnt_exist() throws Exception {
        // Given
        final long eventTriggerInstanceId = 63L;
        final SelectByIdDescriptor<STimerEventTriggerInstance> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(STimerEventTriggerInstance.class,
                STimerEventTriggerInstance.class.getSimpleName(), eventTriggerInstanceId);
        doReturn(null).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        final SEventTriggerInstance result = eventInstanceServiceImpl.getEventTriggerInstance(STimerEventTriggerInstance.class, eventTriggerInstanceId);

        // Then
        assertNull("Should return the result of the mock.", result);
    }

    @Test(expected = SEventTriggerInstanceReadException.class)
    public final void getEventTriggerInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final long eventTriggerInstanceId = 63L;
        final SelectByIdDescriptor<SThrowErrorEventTriggerInstance> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(
                SThrowErrorEventTriggerInstance.class,
                SThrowErrorEventTriggerInstance.class.getSimpleName(), eventTriggerInstanceId);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        eventInstanceServiceImpl.getEventTriggerInstance(SThrowErrorEventTriggerInstance.class, eventTriggerInstanceId);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#getEventTriggerInstances(long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getEventTriggerInstances_should_return_event_trigger_instances() throws Exception {
        // Given
        final long eventInstanceId = 63L;
        final QueryOptions queryOptions = new QueryOptions(0, 100, SEventTriggerInstance.class, "id", OrderByType.ASC);
        final List<STimerEventTriggerInstanceImpl> triggerInstanceImpls = Arrays.asList(new STimerEventTriggerInstanceImpl());
        final SelectListDescriptor<SEventTriggerInstance> selectDescriptor = SelectDescriptorBuilder.getEventTriggers(eventInstanceId, queryOptions);
        doReturn(triggerInstanceImpls).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SEventTriggerInstance> result = eventInstanceServiceImpl.getEventTriggerInstances(eventInstanceId, queryOptions);

        // Then
        assertEquals("Should return the result of the mock.", triggerInstanceImpls, result);
    }

    @Test
    public final void getEventTriggerInstances_should_return_empty_list_if_doesnt_exist() throws Exception {
        // Given
        final long eventInstanceId = 63L;
        final QueryOptions queryOptions = new QueryOptions(0, 100, SEventTriggerInstance.class, "id", OrderByType.ASC);
        final SelectListDescriptor<SEventTriggerInstance> selectDescriptor = SelectDescriptorBuilder.getEventTriggers(eventInstanceId, queryOptions);
        doReturn(Arrays.asList()).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SEventTriggerInstance> result = eventInstanceServiceImpl.getEventTriggerInstances(eventInstanceId, queryOptions);

        // Then
        assertTrue("The result must be empty.", result.isEmpty());
    }

    @Test(expected = SEventTriggerInstanceReadException.class)
    public final void getEventTriggerInstances_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final long eventInstanceId = 63L;
        final QueryOptions queryOptions = new QueryOptions(0, 100, SEventTriggerInstance.class, "id", OrderByType.ASC);
        final SelectListDescriptor<SEventTriggerInstance> selectDescriptor = SelectDescriptorBuilder.getEventTriggers(eventInstanceId, queryOptions);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(selectDescriptor);

        // When
        eventInstanceServiceImpl.getEventTriggerInstances(eventInstanceId, queryOptions);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#getNumberOfEventTriggerInstances(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfEventTriggerInstances_should_return_the_number() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, SEventTriggerInstance.class, "id", OrderByType.ASC);
        doReturn(2L).when(persistenceService).getNumberOfEntities(STimerEventTriggerInstance.class, queryOptions, null);

        // When
        final long result = eventInstanceServiceImpl.getNumberOfEventTriggerInstances(STimerEventTriggerInstance.class, queryOptions);

        // Then
        assertEquals("Should be equals to the result of the mock.", 2L, result);
    }

    @Test(expected = SBonitaReadException.class)
    public final void getNumberOfEventTriggerInstances_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, SEventTriggerInstance.class, "id", OrderByType.ASC);
        doThrow(new SBonitaReadException("")).when(persistenceService).getNumberOfEntities(STimerEventTriggerInstance.class, queryOptions, null);

        // When
        eventInstanceServiceImpl.getNumberOfEventTriggerInstances(STimerEventTriggerInstance.class, queryOptions);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#searchEventTriggerInstances(java.lang.Class, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchEventTriggerInstances_should_return_the_list() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, SEventTriggerInstance.class, "id", OrderByType.ASC);
        final List<STimerEventTriggerInstanceImpl> triggerInstanceImpls = Arrays.asList(new STimerEventTriggerInstanceImpl());
        doReturn(triggerInstanceImpls).when(persistenceService).searchEntity(STimerEventTriggerInstance.class, queryOptions, null);

        // When
        final List<STimerEventTriggerInstance> result = eventInstanceServiceImpl.searchEventTriggerInstances(STimerEventTriggerInstance.class, queryOptions);

        // Then
        assertEquals("Should be equals to the result of the mock.", triggerInstanceImpls, result);
    }

    @Test(expected = SBonitaReadException.class)
    public final void searchEventTriggerInstances_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, SEventTriggerInstance.class, "id", OrderByType.ASC);
        doThrow(new SBonitaReadException("")).when(persistenceService).searchEntity(STimerEventTriggerInstance.class, queryOptions, null);

        // When
        eventInstanceServiceImpl.searchEventTriggerInstances(STimerEventTriggerInstance.class, queryOptions);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#getNumberOfEventTriggerInstances(java.lang.Class, long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void getNumberOfEventTriggerInstancesByProcessInstance_should_return_the_number() throws Exception {
        // Given
        final int processInstanceId = 2;
        final QueryOptions queryOptions = new QueryOptions(0, 100, SEventTriggerInstance.class, "id", OrderByType.ASC);
        doReturn(3L).when(persistenceService).getNumberOfEntities(eq(STimerEventTriggerInstance.class), eq("ByProcessInstance"), eq(queryOptions),
                Matchers.<Map<String, Object>> any());

        // When
        final long result = eventInstanceServiceImpl.getNumberOfTimerEventTriggerInstances(processInstanceId, queryOptions);

        // Then
        assertEquals("Should be equals to the result of the mock.", 3L, result);
    }

    @Test(expected = SBonitaReadException.class)
    public final void getNumberOfEventTriggerInstancesByProcessInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final int processInstanceId = 2;
        final QueryOptions queryOptions = new QueryOptions(0, 100, SEventTriggerInstance.class, "id", OrderByType.ASC);
        doThrow(new SBonitaReadException("")).when(persistenceService).getNumberOfEntities(eq(STimerEventTriggerInstance.class), eq("ByProcessInstance"),
                eq(queryOptions),
                Matchers.<Map<String, Object>> any());

        // When
        eventInstanceServiceImpl.getNumberOfTimerEventTriggerInstances(processInstanceId, queryOptions);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#searchEventTriggerInstances(java.lang.Class, long, org.bonitasoft.engine.persistence.QueryOptions)}
     * .
     */
    @Test
    public final void searchEventTriggerInstancesByProcessInstance_should_return_the_list() throws Exception {
        // Given
        final int processInstanceId = 2;
        final QueryOptions queryOptions = new QueryOptions(0, 100, SEventTriggerInstance.class, "id", OrderByType.ASC);
        final List<STimerEventTriggerInstanceImpl> triggerInstanceImpls = Arrays.asList(new STimerEventTriggerInstanceImpl());
        doReturn(triggerInstanceImpls).when(persistenceService).searchEntity(eq(STimerEventTriggerInstance.class), eq("ByProcessInstance"), eq(queryOptions),
                Matchers.<Map<String, Object>> any());

        // When
        final List<STimerEventTriggerInstance> result = eventInstanceServiceImpl.searchTimerEventTriggerInstances(processInstanceId, queryOptions);

        // Then
        assertEquals("Should be equals to the result of the mock.", triggerInstanceImpls, result);
    }

    @Test(expected = SBonitaReadException.class)
    public final void searchEventTriggerInstancesByProcessInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final int processInstanceId = 2;
        final QueryOptions queryOptions = new QueryOptions(0, 100, SEventTriggerInstance.class, "id", OrderByType.ASC);
        doThrow(new SBonitaReadException("")).when(persistenceService).searchEntity(eq(STimerEventTriggerInstance.class), eq("ByProcessInstance"),
                eq(queryOptions), Matchers.<Map<String, Object>> any());

        // When
        eventInstanceServiceImpl.searchTimerEventTriggerInstances(processInstanceId, queryOptions);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#updateEventTriggerInstance(SEventTriggerInstance, EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updateEventTriggerInstance_should_update_timer_event_trigger_instance() throws Exception {
        // Given
        final STimerEventTriggerInstanceImpl sTimerEventTriggerInstanceImpl = new STimerEventTriggerInstanceImpl();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sTimerEventTriggerInstanceImpl, descriptor);

        // When
        eventInstanceServiceImpl.updateEventTriggerInstance(sTimerEventTriggerInstanceImpl, descriptor);

        // Then
        verify(recorder).recordUpdate(eq(updateRecord), any(SUpdateEvent.class));
    }

    @Test
    public final void updateEventTriggerInstance_should_update_throw_message_event_trigger_instance() throws Exception {
        // Given
        final SThrowMessageEventTriggerInstanceImpl sThrowMessageEventTriggerInstanceImpl = new SThrowMessageEventTriggerInstanceImpl();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sThrowMessageEventTriggerInstanceImpl, descriptor);

        // When
        eventInstanceServiceImpl.updateEventTriggerInstance(sThrowMessageEventTriggerInstanceImpl, descriptor);

        // Then
        verify(recorder).recordUpdate(eq(updateRecord), any(SUpdateEvent.class));
    }

    @Test
    public final void updateEventTriggerInstance_should_update_throw_error_event_trigger_instance() throws Exception {
        // Given
        final SThrowErrorEventTriggerInstanceImpl sThrowErrorEventTriggerInstanceImpl = new SThrowErrorEventTriggerInstanceImpl();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sThrowErrorEventTriggerInstanceImpl, descriptor);

        // When
        eventInstanceServiceImpl.updateEventTriggerInstance(sThrowErrorEventTriggerInstanceImpl, descriptor);

        // Then
        verify(recorder).recordUpdate(eq(updateRecord), any(SUpdateEvent.class));
    }

    @Test(expected = SEventTriggerInstanceModificationException.class)
    public final void updateEventTriggerInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SThrowSignalEventTriggerInstanceImpl sThrowSignalEventTriggerInstanceImpl = new SThrowSignalEventTriggerInstanceImpl();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        doThrow(new SRecorderException("")).when(recorder).recordUpdate(any(UpdateRecord.class), any(SUpdateEvent.class));

        // When
        eventInstanceServiceImpl.updateEventTriggerInstance(sThrowSignalEventTriggerInstanceImpl, descriptor);
    }
}
