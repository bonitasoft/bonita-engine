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
package org.bonitasoft.engine.scheduler.impl;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorCreationException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorReadException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class JobServiceImplForJobDescriptorTest {

    private static final int TENANT_ID = 46845;

    @Mock
    private EventService eventService;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private ReadPersistenceService readPersistenceService;

    @Mock
    private Recorder recorder;

    @Mock
    private TechnicalLoggerService logger;

    @Spy
    @InjectMocks
    private JobServiceImpl jobServiceImpl;

    @Test
    public final void createJobDescriptor_should_return_jobDescriptor() throws SJobDescriptorCreationException, SRecorderException {
        // Given
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        doReturn("plop").when(sJobDescriptor).getJobName();
        doReturn(true).when(logger).isLoggable(JobServiceImpl.class, TechnicalLogSeverity.TRACE);
        doReturn(true).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doReturn(mock(SInsertEvent.class)).when(jobServiceImpl).createInsertEvent(any(PersistentObject.class), anyString());
        doNothing().when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        // When
        final SJobDescriptor result = jobServiceImpl.createJobDescriptor(sJobDescriptor, TENANT_ID);

        // Then
        assertNotNull(result);
        assertEquals(sJobDescriptor.getJobName(), result.getJobName());
        assertEquals(sJobDescriptor.getDescription(), result.getDescription());
        assertEquals(sJobDescriptor.getJobClassName(), result.getJobClassName());
        verify(recorder, times(1)).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createJobDescriptor_should_throw_an_exception_if_the_descriptor_is_null() throws Exception {
        jobServiceImpl.createJobDescriptor(null, TENANT_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createJobDescriptor_should_throw_an_exception_if_the_descriptor_name_is_null() throws Exception {
        // Given
        final SJobDescriptor jobDescriptor = mock(SJobDescriptor.class);

        // When
        jobServiceImpl.createJobDescriptor(jobDescriptor, TENANT_ID);
    }

    @Test(expected = SJobDescriptorCreationException.class)
    public void createJobDescriptor_should_throw_exception_when_recorder_failed() throws Exception {
        //given
        doThrow(new SRecorderException("plop")).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        when(sJobDescriptor.getJobName()).thenReturn("jobName");

        //when
        jobServiceImpl.createJobDescriptor(sJobDescriptor, TENANT_ID);

        //then exception

    }

    @Test
    public final void deleteJobDescriptor_by_id_should_delete_job_descriptor() throws Exception {
        //Given
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        doReturn(3L).when(sJobDescriptor).getId();

        doReturn(sJobDescriptor).when(readPersistenceService).selectById(Matchers.<SelectByIdDescriptor<SJobDescriptor>> any());
        doReturn(true).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doReturn(mock(SDeleteEvent.class)).when(jobServiceImpl).createDeleteEvent(any(PersistentObject.class), anyString());
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        // When
        jobServiceImpl.deleteJobDescriptor(3);

        //Then
        verify(jobServiceImpl, times(1)).deleteJobDescriptor(sJobDescriptor);
    }

    @Test
    public final void deleteJobDescriptor_by_id_should_do_nothing_when_job_descriptor_doesnt_exist() throws Exception {
        // Given
        when(readPersistenceService.selectById(Matchers.<SelectByIdDescriptor<SJobDescriptor>> any())).thenReturn(null);
        when(logger.isLoggable(any(Class.class), eq(TechnicalLogSeverity.TRACE))).thenReturn(true);

        // When
        jobServiceImpl.deleteJobDescriptor(1);

        // Then
        verify(jobServiceImpl, never()).deleteJobDescriptor(any(SJobDescriptor.class));
    }

    @Test
    public final void deleteJobDescriptor_by_id_should_do_nothing_when_job_descriptor_doesnt_exist_without_log() throws Exception {
        // Given
        when(readPersistenceService.selectById(Matchers.<SelectByIdDescriptor<SJobDescriptor>> any())).thenReturn(null);

        // When
        jobServiceImpl.deleteJobDescriptor(1);

        // Then
        verify(jobServiceImpl, never()).deleteJobDescriptor(any(SJobDescriptor.class));
    }

    @Test(expected = SJobDescriptorDeletionException.class)
    public void deleteJobDescriptor_by_id_should_throw_exception_when_recorder_failed() throws Exception {
        // Given
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        doReturn(3L).when(sJobDescriptor).getId();

        doReturn(sJobDescriptor).when(readPersistenceService).selectById(Matchers.<SelectByIdDescriptor<SJobDescriptor>> any());
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        // When
        jobServiceImpl.deleteJobDescriptor(3);
    }

    @Test
    public void deleteJobDescriptor_by_name_should_delete_job_descriptor() throws Exception {
        //Given
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        final List<SJobDescriptor> jobDescriptors = Collections.singletonList(sJobDescriptor);
        doReturn(jobDescriptors).when(jobServiceImpl).searchJobDescriptors(any(QueryOptions.class));

        //when
        jobServiceImpl.deleteJobDescriptorByJobName("jobName");

        //then
        verify(jobServiceImpl, times(1)).deleteJobDescriptor(any(SJobDescriptor.class));
    }

    @Test
    public void deleteJobDescriptor_by_name_should_do_nothing_when_no_job_descriptor() throws Exception {
        //Given
        doReturn(Collections.EMPTY_LIST).when(jobServiceImpl).searchJobDescriptors(any(QueryOptions.class));

        //when
        jobServiceImpl.deleteJobDescriptorByJobName("jobName");

        //then
        verify(jobServiceImpl, never()).deleteJobDescriptor(any(SJobDescriptor.class));
    }

    @Test(expected = SJobDescriptorDeletionException.class)
    public void deleteJobDescriptor_by_name_should_throw_exception_when_searchJobDescriptors_failed() throws Exception {
        //Given
        doThrow(new SBonitaReadException("toto")).when(jobServiceImpl).searchJobDescriptors(any(QueryOptions.class));

        //when
        jobServiceImpl.deleteJobDescriptorByJobName("jobName");
    }

    @Test(expected = SJobDescriptorDeletionException.class)
    public void deleteAllJobDescriptors_should_throw_exception_when_searchEntity_failed() throws Exception {
        //Given
        when(readPersistenceService.searchEntity(eq(SJobDescriptor.class), any(QueryOptions.class), anyMapOf(String.class, Object.class))).thenThrow(
                new SBonitaReadException("error"));

        //When
        jobServiceImpl.deleteAllJobDescriptors();
    }

    @Test
    public void deleteAllJobDescriptors_should_delete_all_job_descriptors() throws Exception {
        //Given
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        doReturn(3L).when(sJobDescriptor).getId();
        final List<SJobDescriptor> descriptors = asList(sJobDescriptor, sJobDescriptor);
        when(readPersistenceService.searchEntity(eq(SJobDescriptor.class), any(QueryOptions.class), anyMapOf(String.class, Object.class))).thenReturn(
                descriptors);

        //When
        jobServiceImpl.deleteAllJobDescriptors();

        //Then
        verify(jobServiceImpl, times(2)).deleteJobDescriptor(sJobDescriptor);
    }

    @Test
    public final void deleteJobDescriptor_by_object_should_delete_job_descriptor() throws SRecorderException, SJobDescriptorDeletionException {
        // Given
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        doReturn(3L).when(sJobDescriptor).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        // When
        jobServiceImpl.deleteJobDescriptor(sJobDescriptor);

        //Then
        verify(recorder, times(1)).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void deleteJobDescriptor_by_object_should_throw_exception_when_job_descriptor_is_null() throws SJobDescriptorDeletionException {
        jobServiceImpl.deleteJobDescriptor(null);
    }

    @Test
    public void getJobDescriptor_by_id_should_return_jobDescriptor() throws SBonitaReadException, SJobDescriptorReadException {
        // Given
        final long jobDescriptorId = 1;
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        when(readPersistenceService.selectById(SelectDescriptorBuilder.getElementById(SJobDescriptor.class, "SJobDescriptor", jobDescriptorId))).thenReturn(
                sJobDescriptor);

        // When
        final SJobDescriptor jobDescriptor = jobServiceImpl.getJobDescriptor(jobDescriptorId);

        // Then
        Assert.assertEquals(sJobDescriptor, jobDescriptor);
    }

    @Test
    public void getJobDescriptor_by_id__should_return_null_when_no_job_descriptor() throws SBonitaReadException, SJobDescriptorReadException {
        // Given
        final long jobDescriptorId = 455;
        doReturn(null).when(readPersistenceService).selectById(SelectDescriptorBuilder.getElementById(SJobDescriptor.class, "SJobDescriptor", jobDescriptorId));

        // When
        final SJobDescriptor result = jobServiceImpl.getJobDescriptor(jobDescriptorId);

        // Then
        assertNull(result);
    }

    @Test(expected = SJobDescriptorReadException.class)
    public void getJobDescriptor_by_id_should_throw_exception_when_selectById_failed() throws SBonitaReadException, SJobDescriptorReadException {
        final long jobDescriptorId = 1;
        doThrow(new SBonitaReadException("")).when(readPersistenceService).selectById(
                SelectDescriptorBuilder.getElementById(SJobDescriptor.class, "SJobDescriptor", jobDescriptorId));

        jobServiceImpl.getJobDescriptor(jobDescriptorId);
    }

    @Test
    public void getNumberOfJobDescriptors() throws SBonitaReadException, SBonitaReadException {
        // Given
        final QueryOptions options = new QueryOptions(0, 10);
        when(readPersistenceService.getNumberOfEntities(SJobDescriptor.class, options, null)).thenReturn(1L);

        // When
        final long numberOfJobDescriptors = jobServiceImpl.getNumberOfJobDescriptors(options);

        // Then
        Assert.assertEquals(1L, numberOfJobDescriptors);
        verifyZeroInteractions(recorder);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfJobDescriptors_should_throw_exception() throws Exception {
        // Given
        final QueryOptions options = new QueryOptions(0, 10);
        when(readPersistenceService.getNumberOfEntities(SJobDescriptor.class, options, null)).thenThrow(new SBonitaReadException(""));

        // When
        jobServiceImpl.getNumberOfJobDescriptors(options);
    }

    @Test
    public void searchJobDescriptors() throws SBonitaReadException, SBonitaReadException {
        // Given
        final QueryOptions options = new QueryOptions(0, 10);
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        when(readPersistenceService.searchEntity(SJobDescriptor.class, options, null)).thenReturn(Collections.singletonList(sJobDescriptor));

        // When
        final SJobDescriptor result = jobServiceImpl.searchJobDescriptors(options).get(0);

        // Then
        assertEquals(sJobDescriptor, result);
        verify(readPersistenceService).searchEntity(SJobDescriptor.class, options, null);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchJobDescriptors_should_throw_exception() throws SBonitaReadException, SBonitaReadException {
        // Given
        final QueryOptions options = new QueryOptions(0, 10);
        doThrow(new SBonitaReadException("")).when(readPersistenceService).searchEntity(SJobDescriptor.class, options, null);

        // When
        jobServiceImpl.searchJobDescriptors(options).get(0);
    }

}
