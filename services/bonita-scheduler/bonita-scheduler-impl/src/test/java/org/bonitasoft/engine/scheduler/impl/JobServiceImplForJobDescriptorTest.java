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
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
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
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorCreationException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorNotFoundException;
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
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class JobServiceImplForJobDescriptorTest {

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

    @InjectMocks
    private JobServiceImpl jobServiceImpl;

    @Test
    public final void createJobDescriptorByTenant() throws SJobDescriptorCreationException, SRecorderException {
        final long tenantId = 2;
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);

        doReturn(true).when(logger).isLoggable(JobServiceImpl.class, TechnicalLogSeverity.TRACE);

        doReturn("plop").when(sJobDescriptor).getJobName();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        final SJobDescriptor result = jobServiceImpl.createJobDescriptor(sJobDescriptor, tenantId);
        assertNotNull(result);
        assertEquals(sJobDescriptor.getJobName(), result.getJobName());
        assertEquals(sJobDescriptor.getDescription(), result.getDescription());
        assertEquals(sJobDescriptor.getJobClassName(), result.getJobClassName());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void createNullJobDescriptorByTenant() throws Exception {
        final long tenantId = 2;
        jobServiceImpl.createJobDescriptor(null, tenantId);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void createJobDescriptorByTenantWithNullName() throws Exception {
        final long tenantId = 2;
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);

        jobServiceImpl.createJobDescriptor(sJobDescriptor, tenantId);
    }

    @Test
    public final void deleteJobDescriptorById() throws SBonitaReadException, SRecorderException, SJobDescriptorNotFoundException, SJobDescriptorReadException,
    SJobDescriptorDeletionException {
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        doReturn(3L).when(sJobDescriptor).getId();

        doReturn(sJobDescriptor).when(readPersistenceService).selectById(Matchers.<SelectByIdDescriptor<SJobDescriptor>> any());
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        jobServiceImpl.deleteJobDescriptor(3);
    }

    @Test
    public final void deleteNotExistingJobDescriptorById() throws SBonitaReadException, SJobDescriptorDeletionException, SJobDescriptorNotFoundException,
    SJobDescriptorReadException {
        when(readPersistenceService.selectById(Matchers.<SelectByIdDescriptor<SJobDescriptor>> any())).thenReturn(null);

        jobServiceImpl.deleteJobDescriptor(1);
    }

    @Test(expected = SJobDescriptorDeletionException.class)
    public void deleteJobDescriptorByIdThrowException() throws Exception {
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        doReturn(3L).when(sJobDescriptor).getId();

        doReturn(sJobDescriptor).when(readPersistenceService).selectById(Matchers.<SelectByIdDescriptor<SJobDescriptor>> any());
        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        jobServiceImpl.deleteJobDescriptor(3);
    }

    @Test
    public final void deleteJobDescriptorByObject() throws SRecorderException, SJobDescriptorDeletionException {
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        doReturn(3L).when(sJobDescriptor).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doNothing().when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(false).when(queriableLoggerService).isLoggable(anyString(), any(SQueriableLogSeverity.class));

        jobServiceImpl.deleteJobDescriptor(sJobDescriptor);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void deleteNullJobDescriptorByObject() throws SJobDescriptorDeletionException {
        jobServiceImpl.deleteJobDescriptor(null);
    }

    @Test(expected = SJobDescriptorDeletionException.class)
    public void deleteJobParameterByObjectThrowException() throws Exception {
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        doReturn(3L).when(sJobDescriptor).getId();

        doReturn(false).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        jobServiceImpl.deleteJobDescriptor(sJobDescriptor);
    }

    @Test
    public void getJobDescriptorById() throws SBonitaReadException, SJobDescriptorNotFoundException, SJobDescriptorReadException {
        final long jobDescriptorId = 1;
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        when(readPersistenceService.selectById(SelectDescriptorBuilder.getElementById(SJobDescriptor.class, "SJobDescriptor", jobDescriptorId))).thenReturn(
                sJobDescriptor);

        Assert.assertEquals(sJobDescriptor, jobServiceImpl.getJobDescriptor(jobDescriptorId));
    }

    @Test(expected = SJobDescriptorNotFoundException.class)
    public void getJobDescriptorByIdNotExist() throws SBonitaReadException, SJobDescriptorNotFoundException, SJobDescriptorReadException {
        final long jobDescriptorId = 455;
        doReturn(null).when(readPersistenceService).selectById(SelectDescriptorBuilder.getElementById(SJobDescriptor.class, "SJobDescriptor", jobDescriptorId));

        jobServiceImpl.getJobDescriptor(jobDescriptorId);
    }

    @Test(expected = SJobDescriptorReadException.class)
    public void getJobDescriptorByIdThrowException() throws SBonitaReadException, SJobDescriptorNotFoundException, SJobDescriptorReadException {
        final long jobDescriptorId = 1;
        doThrow(new SBonitaReadException("")).when(readPersistenceService).selectById(
                SelectDescriptorBuilder.getElementById(SJobDescriptor.class, "SJobDescriptor", jobDescriptorId));

        jobServiceImpl.getJobDescriptor(jobDescriptorId);
    }

    @Test
    public void getNumberOfJobDescriptors() throws SBonitaReadException, SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);

        when(readPersistenceService.getNumberOfEntities(SJobDescriptor.class, options, null)).thenReturn(1L);
        Assert.assertEquals(1L, jobServiceImpl.getNumberOfJobDescriptors(options));

        verifyZeroInteractions(recorder);
    }

    @Test(expected = SBonitaReadException.class)
    public void getNumberOfJobDescriptorsThrowException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);

        when(readPersistenceService.getNumberOfEntities(SJobDescriptor.class, options, null)).thenThrow(new SBonitaReadException(""));
        jobServiceImpl.getNumberOfJobDescriptors(options);
    }

    @Test
    public void searchJobDescriptors() throws SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        when(readPersistenceService.searchEntity(SJobDescriptor.class, options, null)).thenReturn(Collections.singletonList(sJobDescriptor));

        assertEquals(sJobDescriptor, jobServiceImpl.searchJobDescriptors(options).get(0));
    }

    @Test(expected = SBonitaReadException.class)
    public void searchJobDescriptorsThrowException() throws SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);
        doThrow(new SBonitaReadException("")).when(readPersistenceService).searchEntity(SJobDescriptor.class, options, null);

        jobServiceImpl.searchJobDescriptors(options).get(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createJobDescriptor_should_throw_an_exception_if_the_descriptor_is_null() throws Exception {
        jobServiceImpl.createJobDescriptor(null, 46845);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createJobDescriptor_should_throw_an_exception_if_the_descriptor_name_is_null() throws Exception {
        final SJobDescriptor jobDescriptor = mock(SJobDescriptor.class);
        when(jobDescriptor.getJobName()).thenReturn(null);

        jobServiceImpl.createJobDescriptor(jobDescriptor, 46845);
    }

}
