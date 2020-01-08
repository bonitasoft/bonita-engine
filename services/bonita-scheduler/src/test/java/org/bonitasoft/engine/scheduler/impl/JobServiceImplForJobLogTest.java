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
package org.bonitasoft.engine.scheduler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogCreationException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogUpdatingException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class JobServiceImplForJobLogTest {

    @Mock
    private EventService eventService;
    @Mock
    private QueriableLoggerService queriableLoggerService;
    @Mock
    private ReadPersistenceService readPersistenceService;
    @Mock
    private Recorder recorder;
    @Mock
    private TechnicalLoggerService technicalLoggerService;
    @Spy
    @InjectMocks
    private JobServiceImpl jobServiceImpl;

    @Test
    public final void createJobLog() throws Exception {
        // Given
        final SJobLog sJobLog = mock(SJobLog.class);

        doNothing().when(recorder).recordInsert(any(InsertRecord.class), nullable(String.class));

        // When
        final SJobLog result = jobServiceImpl.createJobLog(sJobLog);

        // Then
        assertNotNull(result);
        assertEquals(sJobLog, result);
        verify(recorder).recordInsert(any(InsertRecord.class), nullable(String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void createNullJobLog() throws Exception {
        jobServiceImpl.createJobLog(null);
    }

    @Test(expected = SJobLogCreationException.class)
    public final void createJobLog_should_throw_exception_when_recorder_failed()
            throws SJobLogCreationException, SRecorderException {
        // Given
        final SJobLog sJobLog = mock(SJobLog.class);

        doThrow(new SRecorderException("plop")).when(recorder).recordInsert(any(InsertRecord.class),
                nullable(String.class));

        // When
        jobServiceImpl.createJobLog(sJobLog);
    }

    @Test
    public final void deleteJobLog_by_id() throws Exception {
        // Given
        final SJobLog sJobLog = mock(SJobLog.class);

        doReturn(sJobLog).when(readPersistenceService)
                .selectById(ArgumentMatchers.<SelectByIdDescriptor<SJobLog>> any());
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), nullable(String.class));

        // When
        jobServiceImpl.deleteJobLog(3);

        // Then
        verify(recorder).recordDelete(any(DeleteRecord.class), nullable(String.class));
    }

    @Test
    public final void deleteJobLog_by_id_should_do_nothing_when_job_log_doesnt_exist()
            throws SBonitaReadException, SJobLogDeletionException {
        // Given
        when(readPersistenceService.selectById(ArgumentMatchers.<SelectByIdDescriptor<SJobLog>> any()))
                .thenReturn(null);

        // When
        jobServiceImpl.deleteJobLog(1);

        // Then
        verify(jobServiceImpl, never()).deleteJobLog(any(SJobLog.class));
    }

    @Test(expected = SJobLogDeletionException.class)
    public void deleteJobLog_by_id_should_throw_exception_when_recorder_failed() throws Exception {
        final SJobLog sJobLog = mock(SJobLog.class);

        doReturn(sJobLog).when(readPersistenceService)
                .selectById(ArgumentMatchers.<SelectByIdDescriptor<SJobLog>> any());
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class),
                nullable(String.class));

        jobServiceImpl.deleteJobLog(3);
    }

    @Test
    public final void deleteJobLog_by_object() throws SRecorderException, SJobLogDeletionException {
        // Given
        final SJobLog sJobLog = mock(SJobLog.class);

        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), nullable(String.class));

        // When
        jobServiceImpl.deleteJobLog(sJobLog);

        // Then
        verify(recorder).recordDelete(any(DeleteRecord.class), nullable(String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void deleteJobLog_by_object_should_throw_exception_when_parameter_is_null()
            throws SJobLogDeletionException {
        jobServiceImpl.deleteJobLog(null);
    }

    @Test(expected = SJobLogDeletionException.class)
    public void deleteJobLog_by_object_should_throw_exception_when_recorder_failed() throws Exception {
        // Given
        final SJobLog sJobLog = mock(SJobLog.class);

        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class),
                nullable(String.class));

        // When
        jobServiceImpl.deleteJobLog(sJobLog);
    }

    @Test
    public void deleteJobLogs() throws Exception {
        // Given
        final long jobDescriptorId = 9L;
        final SJobLog sJobLog = mock(SJobLog.class);
        final List<SJobLog> jobLogs = Collections.singletonList(sJobLog);

        doReturn(jobLogs).doReturn(Collections.EMPTY_LIST).when(jobServiceImpl).getJobLogs(jobDescriptorId, 0, 100);
        doNothing().when(jobServiceImpl).deleteJobLog(any(SJobLog.class));

        // When
        jobServiceImpl.deleteJobLogs(jobDescriptorId);
    }

    @Test(expected = SJobLogDeletionException.class)
    public void deleteJobLogs_should_throw_exception_when_deleteJobLogs_failed() throws Exception {
        // Given
        final long jobDescriptorId = 9L;
        final SJobLog sJobLog = mock(SJobLog.class);
        final List<SJobLog> jobLogs = Collections.singletonList(sJobLog);

        doReturn(jobLogs).when(jobServiceImpl).getJobLogs(jobDescriptorId, 0, 100);
        doThrow(SJobLogDeletionException.class).when(jobServiceImpl).deleteJobLog(any(SJobLog.class));

        // When
        jobServiceImpl.deleteJobLogs(jobDescriptorId);
    }

    @Test(expected = SBonitaReadException.class)
    public void deleteJobLogs_should_throw_exception_when_getJobLogs_failed() throws Exception {
        // Given
        final long jobDescriptorId = 9L;
        doThrow(SBonitaReadException.class).when(jobServiceImpl).getJobLogs(jobDescriptorId, 0, 100);

        // When
        jobServiceImpl.deleteJobLogs(jobDescriptorId);
    }

    @Test
    public void getJobLog() throws SBonitaReadException {
        // Given
        final long jobLogId = 1;
        final SJobLog sJobLog = mock(SJobLog.class);
        when(readPersistenceService
                .selectById(SelectDescriptorBuilder.getElementById(SJobLog.class, "SJobLog", jobLogId)))
                        .thenReturn(sJobLog);

        // When
        final SJobLog result = jobServiceImpl.getJobLog(jobLogId);

        // Then
        Assert.assertEquals(sJobLog, result);
    }

    @Test
    public void getJobLog_should_throw_exception_when_not_exist() throws SBonitaReadException {
        // Given
        final long jobLogId = 455;
        doReturn(null).when(readPersistenceService)
                .selectById(SelectDescriptorBuilder.getElementById(SJobLog.class, "SJobLog", jobLogId));

        // When
        final SJobLog jobLog = jobServiceImpl.getJobLog(jobLogId);

        // Then
        assertNull(jobLog);
    }

    @Test(expected = SBonitaReadException.class)
    public void getJobLog_should_throw_exception_when_persistenceService_failed() throws SBonitaReadException {
        // Given
        final long jobLogId = 1;
        doThrow(new SBonitaReadException("")).when(readPersistenceService).selectById(
                SelectDescriptorBuilder.getElementById(SJobLog.class, "SJobLog", jobLogId));

        // When
        jobServiceImpl.getJobLog(jobLogId);
    }

    @Test
    public void getJobLogs_should_call_searchJobLogs() throws Exception {
        // Given
        final long jobDescriptorId = 9L;
        final int fromIndex = 0;
        final int maxResults = 10;

        // When
        jobServiceImpl.getJobLogs(jobDescriptorId, fromIndex, maxResults);

        // Then
        verify(jobServiceImpl).searchJobLogs(any(QueryOptions.class));
    }

    @Test
    public void getNumberOfJobLogs() throws SBonitaReadException, SBonitaReadException {
        // Given
        final QueryOptions options = new QueryOptions(0, 10);

        when(readPersistenceService.getNumberOfEntities(SJobLog.class, options, null)).thenReturn(1L);

        // When
        final long numberOfJobLogs = jobServiceImpl.getNumberOfJobLogs(options);

        // Then
        Assert.assertEquals(1L, numberOfJobLogs);
        verifyZeroInteractions(recorder);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfJobLog_should_throw_exception_when_persistenceService_failed() throws Exception {
        // Given
        final QueryOptions options = new QueryOptions(0, 10);

        when(readPersistenceService.getNumberOfEntities(SJobLog.class, options, null))
                .thenThrow(new SBonitaReadException(""));

        // When
        jobServiceImpl.getNumberOfJobLogs(options);
    }

    @Test
    public void searchJobLogs() throws SBonitaReadException, SBonitaReadException {
        // Given
        final QueryOptions options = new QueryOptions(0, 10);
        final SJobLog sJobLog = mock(SJobLog.class);
        when(readPersistenceService.searchEntity(SJobLog.class, options, null))
                .thenReturn(Collections.singletonList(sJobLog));

        // When
        final SJobLog result = jobServiceImpl.searchJobLogs(options).get(0);

        // Then
        assertEquals(sJobLog, result);
    }

    @Test(expected = SBonitaReadException.class)
    public void searchJobLog_should_throw_exception_when_persistenceService_failed()
            throws SBonitaReadException, SBonitaReadException {
        // Given
        final QueryOptions options = new QueryOptions(0, 10);
        doThrow(new SBonitaReadException("")).when(readPersistenceService).searchEntity(SJobLog.class, options, null);

        // When
        jobServiceImpl.searchJobLogs(options).get(0);
    }

    @Test
    public void updateJobLog_should_update_job_log() throws Exception {
        // Given
        final SJobLog jobLog = new SJobLog();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();

        // When
        jobServiceImpl.updateJobLog(jobLog, descriptor);

        // Then
        verify(recorder).recordUpdate(any(UpdateRecord.class), anyString());
    }

    @Test(expected = SJobLogUpdatingException.class)
    public void updateJobLog_should_throw_exception_when_recorder_failed() throws Exception {
        // Given
        final SJobLog jobLog = new SJobLog();
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        doThrow(new SRecorderException("plop")).when(recorder).recordUpdate(any(UpdateRecord.class),
                nullable(String.class));

        // When
        jobServiceImpl.updateJobLog(jobLog, descriptor);
    }

    @Test
    public void jobWasExecuted_should_update_jobLog_on_exception_if_previous_joblog() throws Exception {
        // Given
        final SJobLog jobLog = mock(SJobLog.class);
        doReturn(1L).when(jobLog).getRetryNumber();
        final List<SJobLog> jobLogs = Collections.singletonList((SJobLog) jobLog);

        doReturn(jobLogs).when(jobServiceImpl).getJobLogs(5L, 0, 1);

        long before = System.currentTimeMillis();

        // When
        Exception jobException = new Exception("theException");
        jobServiceImpl.logJobError(jobException, 5L);

        // Then
        ArgumentCaptor<EntityUpdateDescriptor> captor = ArgumentCaptor.forClass(EntityUpdateDescriptor.class);
        verify(jobServiceImpl).updateJobLog(eq(jobLogs.get(0)), captor.capture());

        EntityUpdateDescriptor updateDescriptor = captor.getValue();
        assertThat((String) updateDescriptor.getFields().get("lastMessage")).contains(jobException.getMessage());
        assertThat((Long) updateDescriptor.getFields().get("lastUpdateDate")).isGreaterThanOrEqualTo(before);
        assertThat(updateDescriptor.getFields().get("retryNumber")).isEqualTo(2L);
    }

    @Test
    public void createJobLog_should_call_job_service_createJobLog_when_related_job_descriptor_exists()
            throws Exception {
        //given
        Exception exception = new Exception("Missing mandatory parameter");
        long jobDescriptorId = 4L;
        long before = System.currentTimeMillis();
        given(jobServiceImpl.getJobDescriptor(jobDescriptorId)).willReturn(mock(SJobDescriptor.class));

        //when
        jobServiceImpl.createJobLog(exception, jobDescriptorId);

        //then
        ArgumentCaptor<SJobLog> captor = ArgumentCaptor.forClass(SJobLog.class);
        verify(jobServiceImpl).createJobLog(captor.capture());
        SJobLog jobLog = captor.getValue();
        assertThat(jobLog.getRetryNumber()).isEqualTo(0);
        assertThat(jobLog.getJobDescriptorId()).isEqualTo(jobDescriptorId);
        assertThat(jobLog.getLastMessage()).contains(exception.getMessage());
        assertThat(jobLog.getLastUpdateDate()).isGreaterThanOrEqualTo(before);
    }

    @Test
    public void createJobLog_should_not_create_job_log_when_related_job_descriptor_does_not_exist() throws Exception {
        //given
        Exception exception = new Exception("Missing mandatory parameter");
        long jobDescriptorId = 4L;
        given(jobServiceImpl.getJobDescriptor(jobDescriptorId)).willReturn(null);

        //when
        jobServiceImpl.createJobLog(exception, jobDescriptorId);

        //then
        verify(jobServiceImpl, never()).createJobLog(any(SJobLog.class));
    }

}
