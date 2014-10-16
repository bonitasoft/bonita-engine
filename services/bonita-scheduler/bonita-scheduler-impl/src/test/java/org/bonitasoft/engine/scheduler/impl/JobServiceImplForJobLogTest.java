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
package org.bonitasoft.engine.scheduler.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.queriablelogger.model.SQueriableLogSeverity;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogCreationException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogReadException;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

    @InjectMocks
    private JobServiceImpl jobServiceImpl;

    @Test
    public final void createJobLog() throws SRecorderException, SJobLogCreationException {
        final SJobLog sJobLog = mock(SJobLog.class);
        doReturn(1L).when(sJobLog).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        final SJobLog result = jobServiceImpl.createJobLog(sJobLog);
        assertNotNull(result);
        assertEquals(sJobLog, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void createNullJobLog() throws Exception {
        jobServiceImpl.createJobLog(null);
    }

    @Test(expected = SJobLogCreationException.class)
    public final void createJobLogThrowException() throws SJobLogCreationException, SRecorderException {
        final SJobLog sJobLog = mock(SJobLog.class);
        doReturn(1L).when(sJobLog).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("plop")).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        jobServiceImpl.createJobLog(sJobLog);
    }

    @Test
    public final void deleteJobLogById() throws SBonitaReadException, SRecorderException, SJobLogNotFoundException, SJobLogReadException,
            SJobLogDeletionException {
        final SJobLog sJobLog = mock(SJobLog.class);
        doReturn(3L).when(sJobLog).getId();

        doReturn(sJobLog).when(readPersistenceService).selectById(Matchers.<SelectByIdDescriptor<SJobLog>> any());
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        jobServiceImpl.deleteJobLog(3);
    }

    @Test(expected = SJobLogNotFoundException.class)
    public final void deleteNotExistingJobLogById() throws SBonitaReadException, SJobLogDeletionException, SJobLogNotFoundException, SJobLogReadException {
        when(readPersistenceService.selectById(Matchers.<SelectByIdDescriptor<SJobLog>> any())).thenReturn(null);

        jobServiceImpl.deleteJobLog(1);
    }

    @Test(expected = SJobLogDeletionException.class)
    public void deleteJobLogByIdThrowException() throws Exception {
        final SJobLog sJobLog = mock(SJobLog.class);
        doReturn(3L).when(sJobLog).getId();

        doReturn(sJobLog).when(readPersistenceService).selectById(Matchers.<SelectByIdDescriptor<SJobLog>> any());
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        jobServiceImpl.deleteJobLog(3);
    }

    @Test
    public final void deleteJobLogByObject() throws SRecorderException, SJobLogDeletionException {
        final SJobLog sJobLog = mock(SJobLog.class);
        doReturn(3L).when(sJobLog).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        jobServiceImpl.deleteJobLog(sJobLog);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void deleteNullJobLogByObject() throws SJobLogDeletionException {
        jobServiceImpl.deleteJobLog(null);
    }

    @Test(expected = SJobLogDeletionException.class)
    public void deleteJobLogByObjectThrowException() throws Exception {
        final SJobLog sJobLog = mock(SJobLog.class);
        doReturn(3L).when(sJobLog).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        jobServiceImpl.deleteJobLog(sJobLog);
    }

    @Test
    public void getJobLogById() throws SBonitaReadException, SJobLogNotFoundException, SJobLogReadException {
        final long jobLogId = 1;
        final SJobLog sJobLog = mock(SJobLog.class);
        when(readPersistenceService.selectById(SelectDescriptorBuilder.getElementById(SJobLog.class, "SJobLog", jobLogId))).thenReturn(sJobLog);

        Assert.assertEquals(sJobLog, jobServiceImpl.getJobLog(jobLogId));
    }

    @Test(expected = SJobLogNotFoundException.class)
    public void getJobLogByIdNotExist() throws SBonitaReadException, SJobLogNotFoundException, SJobLogReadException {
        final long jobLogId = 455;
        doReturn(null).when(readPersistenceService).selectById(SelectDescriptorBuilder.getElementById(SJobLog.class, "SJobLog", jobLogId));

        jobServiceImpl.getJobLog(jobLogId);
    }

    @Test(expected = SJobLogReadException.class)
    public void getJobLogByIdThrowException() throws SBonitaReadException, SJobLogNotFoundException, SJobLogReadException {
        final long jobLogId = 1;
        doThrow(new SBonitaReadException("")).when(readPersistenceService).selectById(
                SelectDescriptorBuilder.getElementById(SJobLog.class, "SJobLog", jobLogId));

        jobServiceImpl.getJobLog(jobLogId);
    }

    @Test
    public void getNumberOfJobLogs() throws SBonitaReadException, SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);

        when(readPersistenceService.getNumberOfEntities(SJobLog.class, options, null)).thenReturn(1L);
        Assert.assertEquals(1L, jobServiceImpl.getNumberOfJobLogs(options));

        verifyZeroInteractions(recorder);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfJobLogsThrowException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);

        when(readPersistenceService.getNumberOfEntities(SJobLog.class, options, null)).thenThrow(new SBonitaReadException(""));
        jobServiceImpl.getNumberOfJobLogs(options);
    }

    @Test
    public void searchJobLogs() throws SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);
        final SJobLog sJobLog = mock(SJobLog.class);
        when(readPersistenceService.searchEntity(SJobLog.class, options, null)).thenReturn(Collections.singletonList(sJobLog));

        assertEquals(sJobLog, jobServiceImpl.searchJobLogs(options).get(0));
    }

    @Test(expected = SBonitaReadException.class)
    public void searchJobLogsThrowException() throws SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);
        doThrow(new SBonitaReadException("")).when(readPersistenceService).searchEntity(SJobLog.class, options, null);

        jobServiceImpl.searchJobLogs(options).get(0);
    }

}
