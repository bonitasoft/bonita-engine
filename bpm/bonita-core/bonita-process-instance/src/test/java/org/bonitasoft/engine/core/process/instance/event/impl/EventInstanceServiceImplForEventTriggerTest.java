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

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceDeletionException;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.impl.STimerEventTriggerInstanceImpl;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
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
import org.mockito.junit.MockitoJUnitRunner;

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
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#createTimerEventTriggerInstance(STimerEventTriggerInstance)}
     * .
     */
    @Test
    public final void createEventTriggerInstance_should_create_event_trigger_instance() throws Exception {
        // Given
        final STimerEventTriggerInstanceImpl eventTriggerInstance = new STimerEventTriggerInstanceImpl();
        final InsertRecord insertRecord = new InsertRecord(eventTriggerInstance);

        // When
        eventInstanceServiceImpl.createTimerEventTriggerInstance(eventTriggerInstance);

        // Then
        verify(recorder).recordInsert(eq(insertRecord), nullable(String.class));
    }

    @Test(expected = SEventTriggerInstanceCreationException.class)
    public final void createEventTriggerInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final STimerEventTriggerInstanceImpl eventTriggerInstance = new STimerEventTriggerInstanceImpl();
        doThrow(new SRecorderException("")).when(recorder).recordInsert(any(InsertRecord.class), nullable(String.class));

        // When
        eventInstanceServiceImpl.createTimerEventTriggerInstance(eventTriggerInstance);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#deleteEventTriggerInstance(STimerEventTriggerInstance)}
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
        verify(recorder).recordDelete(eq(insertRecord), nullable(String.class));
    }

    @Test(expected = SEventTriggerInstanceDeletionException.class)
    public final void deleteEventTriggerInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final STimerEventTriggerInstanceImpl eventTriggerInstance = new STimerEventTriggerInstanceImpl();
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), nullable(String.class));

        // When
        eventInstanceServiceImpl.deleteEventTriggerInstance(eventTriggerInstance);
    }

    @Test
    public final void getEventTriggerInstance_should_return_event_trigger_instance() throws Exception {
        // Given
        final long eventTriggerInstanceId = 63L;
        final STimerEventTriggerInstanceImpl eventTriggerInstance = new STimerEventTriggerInstanceImpl();
        final SelectByIdDescriptor<STimerEventTriggerInstance> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(STimerEventTriggerInstance.class,
                STimerEventTriggerInstance.class.getSimpleName(), eventTriggerInstanceId);
        doReturn(eventTriggerInstance).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        final STimerEventTriggerInstance result = eventInstanceServiceImpl.getEventTriggerInstance(STimerEventTriggerInstance.class, eventTriggerInstanceId);

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
        final STimerEventTriggerInstance result = eventInstanceServiceImpl.getEventTriggerInstance(STimerEventTriggerInstance.class, eventTriggerInstanceId);

        // Then
        assertNull("Should return the result of the mock.", result);
    }

    @Test
    public final void searchEventTriggerInstances_should_return_the_list() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, STimerEventTriggerInstance.class, "id", OrderByType.ASC);
        final List<STimerEventTriggerInstanceImpl> triggerInstanceImpls = Arrays.asList(new STimerEventTriggerInstanceImpl());
        doReturn(triggerInstanceImpls).when(persistenceService).searchEntity(STimerEventTriggerInstance.class, queryOptions, null);

        // When
        final List<STimerEventTriggerInstance> result = eventInstanceServiceImpl.searchTimerEventTriggerInstances(queryOptions);

        // Then
        assertEquals("Should be equals to the result of the mock.", triggerInstanceImpls, result);
    }

    @Test(expected = SBonitaReadException.class)
    public final void searchEventTriggerInstances_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final QueryOptions queryOptions = new QueryOptions(0, 100, STimerEventTriggerInstance.class, "id", OrderByType.ASC);
        doThrow(new SBonitaReadException("")).when(persistenceService).searchEntity(STimerEventTriggerInstance.class, queryOptions, null);

        // When
        eventInstanceServiceImpl.searchTimerEventTriggerInstances(queryOptions);
    }

    @Test
    public final void getNumberOfEventTriggerInstancesByProcessInstance_should_return_the_number() throws Exception {
        // Given
        final int processInstanceId = 2;
        final QueryOptions queryOptions = new QueryOptions(0, 100, STimerEventTriggerInstance.class, "id", OrderByType.ASC);
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
        final QueryOptions queryOptions = new QueryOptions(0, 100, STimerEventTriggerInstance.class, "id", OrderByType.ASC);
        doThrow(new SBonitaReadException("")).when(persistenceService).getNumberOfEntities(eq(STimerEventTriggerInstance.class), eq("ByProcessInstance"),
                eq(queryOptions),
                any());

        // When
        eventInstanceServiceImpl.getNumberOfTimerEventTriggerInstances(processInstanceId, queryOptions);
    }

    @Test
    public final void searchEventTriggerInstancesByProcessInstance_should_return_the_list() throws Exception {
        // Given
        final int processInstanceId = 2;
        final QueryOptions queryOptions = new QueryOptions(0, 100, STimerEventTriggerInstance.class, "id", OrderByType.ASC);
        final List<STimerEventTriggerInstanceImpl> triggerInstanceImpls = Arrays.asList(new STimerEventTriggerInstanceImpl());
        doReturn(triggerInstanceImpls).when(persistenceService).searchEntity(eq(STimerEventTriggerInstance.class), eq("ByProcessInstance"), eq(queryOptions),
               any());

        // When
        final List<STimerEventTriggerInstance> result = eventInstanceServiceImpl.searchTimerEventTriggerInstances(processInstanceId, queryOptions);

        // Then
        assertEquals("Should be equals to the result of the mock.", triggerInstanceImpls, result);
    }

    @Test(expected = SBonitaReadException.class)
    public final void searchEventTriggerInstancesByProcessInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final int processInstanceId = 2;
        final QueryOptions queryOptions = new QueryOptions(0, 100, STimerEventTriggerInstance.class, "id", OrderByType.ASC);
        doThrow(new SBonitaReadException("")).when(persistenceService).searchEntity(eq(STimerEventTriggerInstance.class), eq("ByProcessInstance"),
                eq(queryOptions), any());

        // When
        eventInstanceServiceImpl.searchTimerEventTriggerInstances(processInstanceId, queryOptions);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#updateEventTriggerInstance(STimerEventTriggerInstance, EntityUpdateDescriptor)}
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
        verify(recorder).recordUpdate(eq(updateRecord), nullable(String.class));
    }
}
