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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.archive.ArchiveService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceCreationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageModificationException;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SMessageEventCoupleImpl;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SMessageInstanceImpl;
import org.bonitasoft.engine.core.process.instance.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.SUpdateEvent;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
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
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class EventInstanceServiceImplForMessageTest {

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

    @InjectMocks
    private EventInstanceServiceImpl eventInstanceServiceImpl;

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#createMessageInstance(org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance)}
     * .
     */
    @Test
    public final void createMessageInstance_should_create_message_instance() throws Exception {
        // Given
        final SMessageInstanceImpl sMessageInstanceImpl = new SMessageInstanceImpl();
        final InsertRecord insertRecord = new InsertRecord(sMessageInstanceImpl);

        // When
        eventInstanceServiceImpl.createMessageInstance(sMessageInstanceImpl);

        // Then
        verify(recorder).recordInsert(eq(insertRecord), any(SInsertEvent.class));
    }

    @Test(expected = SMessageInstanceCreationException.class)
    public final void createMessageInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SMessageInstanceImpl sMessageInstanceImpl = new SMessageInstanceImpl();
        doThrow(new SRecorderException("")).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

        // When
        eventInstanceServiceImpl.createMessageInstance(sMessageInstanceImpl);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#deleteMessageInstance(org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance)}
     * .
     */
    @Test
    public final void deleteMessageInstance_should_delete_message_instance() throws Exception {
        // Given
        final SMessageInstanceImpl sMessageInstanceImpl = new SMessageInstanceImpl();
        final DeleteRecord insertRecord = new DeleteRecord(sMessageInstanceImpl);

        // When
        eventInstanceServiceImpl.deleteMessageInstance(sMessageInstanceImpl);

        // Then
        verify(recorder).recordDelete(eq(insertRecord), any(SDeleteEvent.class));
    }

    @Test(expected = SMessageModificationException.class)
    public final void deleteMessageInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SMessageInstanceImpl sMessageInstanceImpl = new SMessageInstanceImpl();
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        // When
        eventInstanceServiceImpl.deleteMessageInstance(sMessageInstanceImpl);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#resetProgressMessageInstances()}.
     */
    @Test
    public final void resetProgressMessageInstances_should_update_message() throws Exception {
        // Given
        doReturn(3).when(persistenceService).update("resetProgressMessageInstances");

        // When
        final int result = eventInstanceServiceImpl.resetProgressMessageInstances();

        // Then
        assertEquals("Should return the result of the mock.", 3, result);
    }

    @Test(expected = SMessageModificationException.class)
    public final void resetProgressMessageInstances_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        doThrow(new SPersistenceException("")).when(persistenceService).update("resetProgressMessageInstances");

        // When
        eventInstanceServiceImpl.resetProgressMessageInstances();
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#getMessageEventCouples(int, int)}.
     */
    @Test
    public final void getMessageEventCouples_should_return_message_event_couples() throws Exception {
        // Given
        final List<SMessageEventCoupleImpl> sMessageEventCoupleImpls = Arrays.asList(new SMessageEventCoupleImpl());
        final SelectListDescriptor<SMessageEventCouple> selectDescriptor = SelectDescriptorBuilder.getMessageEventCouples(0, 100);
        doReturn(sMessageEventCoupleImpls).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SMessageEventCouple> result = eventInstanceServiceImpl.getMessageEventCouples(0, 100);

        // Then
        assertEquals("Should return the result of the mock.", sMessageEventCoupleImpls, result);
    }

    @Test
    public final void getMessageEventCouples_should_return_empty_list_if_doesnt_exist() throws Exception {
        // Given
        final SelectListDescriptor<SMessageEventCouple> selectDescriptor = SelectDescriptorBuilder.getMessageEventCouples(0, 100);
        doReturn(Arrays.asList()).when(persistenceService).selectList(selectDescriptor);

        // When
        final List<SMessageEventCouple> result = eventInstanceServiceImpl.getMessageEventCouples(0, 100);

        // Then
        assertTrue("The result must be empty.", result.isEmpty());
    }

    @Test(expected = SEventTriggerInstanceReadException.class)
    public final void getMessageEventCouples_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SelectListDescriptor<SMessageEventCouple> selectDescriptor = SelectDescriptorBuilder.getMessageEventCouples(0, 100);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectList(selectDescriptor);

        // When
        eventInstanceServiceImpl.getMessageEventCouples(0, 100);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#getMessageInstance(long)}.
     */
    @Test
    public final void getMessageInstance_should_return_message_instance() throws Exception {
        // Given
        final long messageInstanceId = 63L;
        final SMessageInstanceImpl sMessageInstanceImpl = new SMessageInstanceImpl();
        final SelectByIdDescriptor<SMessageInstance> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(SMessageInstance.class,
                "MessageInstance", messageInstanceId);
        doReturn(sMessageInstanceImpl).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        final SMessageInstance result = eventInstanceServiceImpl.getMessageInstance(messageInstanceId);

        // Then
        assertEquals("Should return the result of the mock.", sMessageInstanceImpl, result);
    }

    @Test
    public final void getMessageInstance_should_return_null_if_doesnt_exist() throws Exception {
        // Given
        final long messageInstanceId = 63L;
        final SelectByIdDescriptor<SMessageInstance> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(SMessageInstance.class,
                "MessageInstance", messageInstanceId);
        doReturn(null).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        final SMessageInstance result = eventInstanceServiceImpl.getMessageInstance(messageInstanceId);

        // Then
        assertNull("Should return the result of the mock.", result);
    }

    @Test(expected = SMessageInstanceReadException.class)
    public final void getMessageInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final long messageInstanceId = 63L;
        final SelectByIdDescriptor<SMessageInstance> selectByIdDescriptor = SelectDescriptorBuilder.getElementById(SMessageInstance.class,
                "MessageInstance", messageInstanceId);
        doThrow(new SBonitaReadException("")).when(persistenceService).selectById(selectByIdDescriptor);

        // When
        eventInstanceServiceImpl.getMessageInstance(messageInstanceId);
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.core.process.instance.event.impl.EventInstanceServiceImpl#updateMessageInstance(org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance, org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor)}
     * .
     */
    @Test
    public final void updateMessageInstance_should_update_message_instance() throws Exception {
        // Given
        final SMessageInstanceImpl sMessageInstanceImpl = new SMessageInstanceImpl();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        final UpdateRecord updateRecord = UpdateRecord.buildSetFields(sMessageInstanceImpl, descriptor);

        // When
        eventInstanceServiceImpl.updateMessageInstance(sMessageInstanceImpl, descriptor);

        // Then
        verify(recorder).recordUpdate(eq(updateRecord), any(SUpdateEvent.class));
    }

    @Test(expected = SMessageModificationException.class)
    public final void updateMessageInstance_should_throw_exception_when_there_is_error() throws Exception {
        // Given
        final SMessageInstanceImpl sMessageInstanceImpl = new SMessageInstanceImpl();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        doThrow(new SRecorderException("")).when(recorder).recordUpdate(any(UpdateRecord.class), any(SUpdateEvent.class));

        // When
        eventInstanceServiceImpl.updateMessageInstance(sMessageInstanceImpl, descriptor);
    }

}
