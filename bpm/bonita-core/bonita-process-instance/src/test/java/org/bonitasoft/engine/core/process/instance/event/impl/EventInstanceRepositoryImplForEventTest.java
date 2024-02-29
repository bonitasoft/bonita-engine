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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.SEventInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.event.SBoundaryEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.SStartEventInstance;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class EventInstanceRepositoryImplForEventTest {

    @Mock
    private ArchiveService archiveService;

    @Mock
    private EventService eventService;
    @Mock
    private PersistenceService persistenceService;

    @Mock
    private Recorder recorder;

    @InjectMocks
    private EventInstanceRepositoryImpl eventInstanceRepository;

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#createEventInstance(org.bonitasoft.engine.core.process.instance.model.event.SEventInstance)}
     * .
     */
    @Test
    public final void createEventInstance_should_create_event_instance() throws Exception {
        // Given
        final SIntermediateCatchEventInstance eventInstanceImpl = new SIntermediateCatchEventInstance();
        final InsertRecord insertRecord = new InsertRecord(eventInstanceImpl);

        // When
        eventInstanceRepository.createEventInstance(eventInstanceImpl);

        // Then
        verify(recorder).recordInsert(eq(insertRecord), nullable(String.class));
    }

    @Test(expected = SEventInstanceCreationException.class)
    public final void createEventInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SStartEventInstance eventInstanceImpl = new SStartEventInstance();
        doThrow(new SRecorderException("")).when(recorder).recordInsert(any(InsertRecord.class),
                nullable(String.class));

        // When
        eventInstanceRepository.createEventInstance(eventInstanceImpl);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#getActivityBoundaryEventInstances(long, int, int)}
     * .
     */
    @Test
    public final void getActivityBoundaryEventInstances_should_return_event_instances() throws Exception {
        // Given
        final long activityInstanceId = 56L;
        final int fromIndex = 1;
        final int maxResults = 6;
        final List<SBoundaryEventInstance> triggerInstanceImpls = Arrays.asList(new SBoundaryEventInstance());
        final SelectListDescriptor<SBoundaryEventInstance> selectDescriptor = SelectDescriptorBuilder
                .getActivityBoundaryEvents(activityInstanceId, fromIndex,
                        maxResults);
        doReturn(triggerInstanceImpls).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SBoundaryEventInstance> result = eventInstanceRepository
                .getActivityBoundaryEventInstances(activityInstanceId, fromIndex, maxResults);

        // Then
        assertEquals("Should return the result of the mock.", triggerInstanceImpls, result);
    }

    @Test
    public final void getActivityBoundaryEventInstances_should_return_empty_list_if_doesnt_exist() throws Exception {
        // Given
        final long activityInstanceId = 56L;
        final int fromIndex = 1;
        final int maxResults = 6;
        final SelectListDescriptor<SBoundaryEventInstance> selectDescriptor = SelectDescriptorBuilder
                .getActivityBoundaryEvents(activityInstanceId, fromIndex,
                        maxResults);
        doReturn(Arrays.asList()).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SBoundaryEventInstance> result = eventInstanceRepository
                .getActivityBoundaryEventInstances(activityInstanceId, fromIndex, maxResults);

        // Then
        assertTrue("The result must be empty.", result.isEmpty());
    }

    @Test(expected = SEventInstanceReadException.class)
    public final void getActivityBoundaryEventInstances_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final long activityInstanceId = 56L;
        final int fromIndex = 1;
        final int maxResults = 6;
        final SelectListDescriptor<SBoundaryEventInstance> selectDescriptor = SelectDescriptorBuilder
                .getActivityBoundaryEvents(activityInstanceId, fromIndex,
                        maxResults);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(selectDescriptor);

        // When
        eventInstanceRepository.getActivityBoundaryEventInstances(activityInstanceId, fromIndex, maxResults);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#getEventInstances(long, int, int, java.lang.String, org.bonitasoft.engine.persistence.OrderByType)}
     * .
     */
    @Test
    public final void getEventInstances_should_return_event_instances() throws Exception {
        // Given
        final long rootContainerId = 56L;
        final int fromIndex = 1;
        final int maxResults = 6;
        final String fieldName = "name";
        final OrderByType orderByType = OrderByType.ASC;
        final SelectListDescriptor<SEventInstance> selectDescriptor = SelectDescriptorBuilder
                .getEventsFromRootContainer(rootContainerId, fromIndex,
                        maxResults, fieldName, orderByType);
        final List<SIntermediateCatchEventInstance> eventInstanceImpls = Arrays
                .asList(new SIntermediateCatchEventInstance());
        doReturn(eventInstanceImpls).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SEventInstance> result = eventInstanceRepository.getEventInstances(rootContainerId, fromIndex,
                maxResults, fieldName, orderByType);

        // Then
        assertEquals("Should return the result of the mock.", eventInstanceImpls, result);
    }

    @Test
    public final void getEventInstances_should_return_empty_list_if_doesnt_exist() throws Exception {
        // Given
        final long rootContainerId = 56L;
        final int fromIndex = 1;
        final int maxResults = 6;
        final String fieldName = "name";
        final OrderByType orderByType = OrderByType.ASC;
        final SelectListDescriptor<SEventInstance> selectDescriptor = SelectDescriptorBuilder
                .getEventsFromRootContainer(rootContainerId, fromIndex,
                        maxResults, fieldName, orderByType);
        doReturn(Arrays.asList()).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SEventInstance> result = eventInstanceRepository.getEventInstances(rootContainerId, fromIndex,
                maxResults, fieldName, orderByType);

        // Then
        assertTrue("The result must be empty.", result.isEmpty());
    }

    @Test(expected = SEventInstanceReadException.class)
    public final void getEventInstances_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final long rootContainerId = 56L;
        final int fromIndex = 1;
        final int maxResults = 6;
        final String fieldName = "name";
        final OrderByType orderByType = OrderByType.ASC;
        final SelectListDescriptor<SEventInstance> selectDescriptor = SelectDescriptorBuilder
                .getEventsFromRootContainer(rootContainerId, fromIndex,
                        maxResults, fieldName, orderByType);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(selectDescriptor);

        // When
        eventInstanceRepository.getEventInstances(rootContainerId, fromIndex, maxResults, fieldName, orderByType);
    }

}
