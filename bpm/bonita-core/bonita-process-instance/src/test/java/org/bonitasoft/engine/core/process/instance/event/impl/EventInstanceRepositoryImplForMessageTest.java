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
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceRepository;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageModificationException;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
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
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class EventInstanceRepositoryImplForMessageTest {

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
     * {@link EventInstanceRepository#createMessageInstance(org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance)}
     * .
     */
    @Test
    public final void createMessageInstance_should_create_message_instance() throws Exception {
        // Given
        final SMessageInstance sMessageInstance = new SMessageInstance();
        final InsertRecord insertRecord = new InsertRecord(sMessageInstance);

        // When
        eventInstanceRepository.createMessageInstance(sMessageInstance);

        // Then
        verify(recorder).recordInsert(eq(insertRecord), nullable(String.class));
    }

    @Test(expected = SMessageInstanceCreationException.class)
    public final void createMessageInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SMessageInstance sMessageInstance = new SMessageInstance();
        doThrow(new SRecorderException("")).when(recorder).recordInsert(any(InsertRecord.class),
                nullable(String.class));

        // When
        eventInstanceRepository.createMessageInstance(sMessageInstance);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#deleteMessageInstance(org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance)}
     * .
     */
    @Test
    public final void deleteMessageInstance_should_delete_message_instance() throws Exception {
        // Given
        final SMessageInstance sMessageInstance = new SMessageInstance();
        final DeleteRecord insertRecord = new DeleteRecord(sMessageInstance);

        // When
        eventInstanceRepository.deleteMessageInstance(sMessageInstance);

        // Then
        verify(recorder).recordDelete(eq(insertRecord), nullable(String.class));
    }

    @Test(expected = SMessageModificationException.class)
    public final void deleteMessageInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SMessageInstance sMessageInstance = new SMessageInstance();
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class),
                nullable(String.class));

        // When
        eventInstanceRepository.deleteMessageInstance(sMessageInstance);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#resetProgressMessageInstances()}.
     */
    @Test
    public final void resetProgressMessageInstances_should_update_message() throws Exception {
        // Given
        doReturn(3).when(persistenceService).update("resetProgressMessageInstances");

        // When
        final int result = eventInstanceRepository.resetProgressMessageInstances();

        // Then
        assertEquals("Should return the result of the mock.", 3, result);
    }

    @Test(expected = SMessageModificationException.class)
    public final void resetProgressMessageInstances_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        doThrow(new SPersistenceException("")).when(persistenceService).update("resetProgressMessageInstances");

        // When
        eventInstanceRepository.resetProgressMessageInstances();
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#getMessageEventCouples(int, int)}.
     */
    @Test
    public final void getMessageEventCouples_should_return_message_event_couples() throws Exception {
        // Given
        final List<SMessageEventCouple> sMessageEventCoupleImpls = Arrays.asList(new SMessageEventCouple());
        final SelectListDescriptor<SMessageEventCouple> selectDescriptor = SelectDescriptorBuilder
                .getMessageEventCouples(0, 100);
        doReturn(sMessageEventCoupleImpls).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SMessageEventCouple> result = eventInstanceRepository.getMessageEventCouples(0, 100);

        // Then
        assertEquals("Should return the result of the mock.", sMessageEventCoupleImpls, result);
    }

    @Test
    public final void getMessageEventCouples_should_return_empty_list_if_doesnt_exist() throws Exception {
        // Given
        final SelectListDescriptor<SMessageEventCouple> selectDescriptor = SelectDescriptorBuilder
                .getMessageEventCouples(0, 100);
        doReturn(Arrays.asList()).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SMessageEventCouple> result = eventInstanceRepository.getMessageEventCouples(0, 100);

        // Then
        assertTrue("The result must be empty.", result.isEmpty());
    }

    @Test(expected = SEventTriggerInstanceReadException.class)
    public final void getMessageEventCouples_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SelectListDescriptor<SMessageEventCouple> selectDescriptor = SelectDescriptorBuilder
                .getMessageEventCouples(0, 100);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(selectDescriptor);

        // When
        eventInstanceRepository.getMessageEventCouples(0, 100);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#getMessageInstance(long)}.
     */
    @Test
    public final void getMessageInstance_should_return_message_instance() throws Exception {
        // Given
        final long messageInstanceId = 63L;
        final SMessageInstance sMessageInstance = new SMessageInstance();
        final SelectByIdDescriptor<SMessageInstance> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(
                SMessageInstance.class,
                "MessageInstance", messageInstanceId);
        doReturn(sMessageInstance).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        final SMessageInstance result = eventInstanceRepository.getMessageInstance(messageInstanceId);

        // Then
        assertEquals("Should return the result of the mock.", sMessageInstance, result);
    }

    @Test
    public final void deleteMessage_should_process_ids_in_batch() throws Exception {
        // Given
        List<Long> ids = new Random().longs(0, 350).limit(350).boxed().collect(Collectors.toList());

        // When
        eventInstanceRepository.deleteMessageInstanceByIds(ids);
        // Then
        verify(persistenceService, times(4)).update(anyString(), anyMap());
    }

    @Test
    public final void getMessageInstance_should_return_null_if_doesnt_exist() throws Exception {
        // Given
        final long messageInstanceId = 63L;
        final SelectByIdDescriptor<SMessageInstance> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(
                SMessageInstance.class,
                "MessageInstance", messageInstanceId);
        doReturn(null).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        final SMessageInstance result = eventInstanceRepository.getMessageInstance(messageInstanceId);

        // Then
        assertNull("Should return the result of the mock.", result);
    }

    @Test(expected = SMessageInstanceReadException.class)
    public final void getMessageInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final long messageInstanceId = 63L;
        final SelectByIdDescriptor<SMessageInstance> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(
                SMessageInstance.class,
                "MessageInstance", messageInstanceId);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        eventInstanceRepository.getMessageInstance(messageInstanceId);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceRepositoryImpl#updateMessageInstance(org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updateMessageInstance_should_update_message_instance() throws Exception {
        // Given
        final SMessageInstance sMessageInstance = new SMessageInstance();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sMessageInstance, descriptor);

        // When
        eventInstanceRepository.updateMessageInstance(sMessageInstance, descriptor);

        // Then
        verify(recorder).recordUpdate(eq(updateRecord), nullable(String.class));
    }

    @Test(expected = SMessageModificationException.class)
    public final void updateMessageInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SMessageInstance sMessageInstance = new SMessageInstance();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        doThrow(new SRecorderException("")).when(recorder).recordUpdate(any(UpdateRecord.class),
                nullable(String.class));

        // When
        eventInstanceRepository.updateMessageInstance(sMessageInstance, descriptor);
    }

    @Test(expected = SMessageModificationException.class)
    public final void deleteMessageInstanceByIds_throw_exception_when_there_is_error() throws Exception {
        // Given
        doThrow(new SPersistenceException("")).when(persistenceService).update(anyString(), anyMap());
        // When
        eventInstanceRepository.deleteMessageInstanceByIds(Collections.singletonList(50L));
    }

    @Test(expected = SMessageInstanceReadException.class)
    public final void getMessageInstanceIdOlderThanCreationDateInstanceByIds_throw_exception_when_there_is_error()
            throws Exception {
        // Given
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(any(SelectListDescriptor.class));
        // When
        eventInstanceRepository.getMessageInstanceIdOlderThanCreationDate(14500L, new QueryOptions(0, 100));

    }

    @Test(expected = IllegalArgumentException.class)
    public final void should_throw_exception_when_query_option_have_bad_filters()
            throws Exception {
        eventInstanceRepository.getMessageInstanceIdOlderThanCreationDate(14500L, new QueryOptions(
                Collections.singletonList(new FilterOption(SMessageInstance.class, "toto", "testt")), null));

    }
}
