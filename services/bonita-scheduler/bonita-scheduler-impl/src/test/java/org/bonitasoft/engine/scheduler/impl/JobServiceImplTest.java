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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorReadException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogReadException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterReadException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class JobServiceImplTest {

    @Mock
    private EventService eventService;

    @Mock
    private QueriableLoggerService queriableLogService;

    @Mock
    private ReadPersistenceService readPersistenceService;

    @Mock
    private Recorder recorder;

    @InjectMocks
    private JobServiceImpl jobServiceImpl;

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#getJobDescriptor(long)}.
     * 
     * @throws SBonitaReadException
     * @throws SJobDescriptorReadException
     * @throws SJobDescriptorNotFoundException
     */
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

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#getNumberOfJobDescriptors(org.bonitasoft.engine.persistence.QueryOptions)}.
     * 
     * @throws SBonitaReadException
     * @throws SBonitaSearchException
     */
    @Test
    public void getNumberOfJobDescriptors() throws SBonitaReadException, SBonitaSearchException {
        final QueryOptions options = new QueryOptions(0, 10);

        when(readPersistenceService.getNumberOfEntities(SJobDescriptor.class, options, null)).thenReturn(1L);
        Assert.assertEquals(1L, jobServiceImpl.getNumberOfJobDescriptors(options));

        verifyZeroInteractions(recorder);
    }

    @Test(expected = SBonitaSearchException.class)
    public void getNumberOfJobDescriptorsThrowException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);

        when(readPersistenceService.getNumberOfEntities(SJobDescriptor.class, options, null)).thenThrow(new SBonitaReadException(""));
        jobServiceImpl.getNumberOfJobDescriptors(options);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#searchJobDescriptors(org.bonitasoft.engine.persistence.QueryOptions)}.
     * 
     * @throws SBonitaReadException
     * @throws SBonitaSearchException
     */
    @Test
    public void searchJobDescriptors() throws SBonitaSearchException, SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);
        final SJobDescriptor sJobDescriptor = mock(SJobDescriptor.class);
        when(readPersistenceService.searchEntity(SJobDescriptor.class, options, null)).thenReturn(Collections.singletonList(sJobDescriptor));

        assertEquals(sJobDescriptor, jobServiceImpl.searchJobDescriptors(options).get(0));
    }

    @Test(expected = SBonitaSearchException.class)
    public void searchJobDescriptorsThrowException() throws SBonitaSearchException, SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);
        doThrow(new SBonitaReadException("")).when(readPersistenceService).searchEntity(SJobDescriptor.class, options, null);

        jobServiceImpl.searchJobDescriptors(options).get(0);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#getJobParameter(long)}.
     * 
     * @throws SJobParameterReadException
     * @throws SJobParameterNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public void getJobParameterById() throws SJobParameterNotFoundException, SJobParameterReadException, SBonitaReadException {
        final long jobParameterId = 1;
        final SJobParameter sJobParameter = mock(SJobParameter.class);
        when(readPersistenceService.selectById(SelectDescriptorBuilder.getElementById(SJobParameter.class, "SJobParameter", jobParameterId))).thenReturn(
                sJobParameter);

        Assert.assertEquals(sJobParameter, jobServiceImpl.getJobParameter(jobParameterId));
    }

    @Test(expected = SJobParameterNotFoundException.class)
    public void getJobParameterByIdNotExist() throws SBonitaReadException, SJobParameterNotFoundException, SJobParameterReadException {
        final long jobParameterId = 455;
        doReturn(null).when(readPersistenceService).selectById(SelectDescriptorBuilder.getElementById(SJobParameter.class, "SJobParameter", jobParameterId));

        jobServiceImpl.getJobParameter(jobParameterId);
    }

    @Test(expected = SJobParameterReadException.class)
    public void getJobParameterByIdThrowException() throws SJobParameterNotFoundException, SJobParameterReadException, SBonitaReadException {
        final long jobParameterId = 1;
        doThrow(new SBonitaReadException("")).when(readPersistenceService).selectById(
                SelectDescriptorBuilder.getElementById(SJobParameter.class, "SJobParameter", jobParameterId));

        jobServiceImpl.getJobParameter(jobParameterId);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#searchJobParameters(org.bonitasoft.engine.persistence.QueryOptions)}.
     * 
     * @throws SBonitaReadException
     * @throws SBonitaSearchException
     */
    @Test
    public void searchJobParameters() throws SBonitaSearchException, SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);
        final SJobParameter sJobDescriptor = mock(SJobParameter.class);
        when(readPersistenceService.searchEntity(SJobParameter.class, options, null)).thenReturn(Collections.singletonList(sJobDescriptor));

        assertEquals(sJobDescriptor, jobServiceImpl.searchJobParameters(options).get(0));
    }

    @Test(expected = SBonitaSearchException.class)
    public void searchJobParametersThrowException() throws SBonitaSearchException, SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);
        doThrow(new SBonitaReadException("")).when(readPersistenceService).searchEntity(SJobParameter.class, options, null);

        jobServiceImpl.searchJobParameters(options).get(0);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#getJobLog(long)}.
     * 
     * @throws SBonitaReadException
     * @throws SJobLogReadException
     * @throws SJobLogNotFoundException
     */
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

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#getNumberOfJobLogs(org.bonitasoft.engine.persistence.QueryOptions)}.
     * 
     * @throws SBonitaReadException
     * @throws SBonitaSearchException
     */
    @Test
    public void getNumberOfJobLogs() throws SBonitaReadException, SBonitaSearchException {
        final QueryOptions options = new QueryOptions(0, 10);

        when(readPersistenceService.getNumberOfEntities(SJobLog.class, options, null)).thenReturn(1L);
        Assert.assertEquals(1L, jobServiceImpl.getNumberOfJobLogs(options));

        verifyZeroInteractions(recorder);
    }

    @Test(expected = SBonitaSearchException.class)
    public void getNumberOfJobLogsThrowException() throws Exception {
        final QueryOptions options = new QueryOptions(0, 10);

        when(readPersistenceService.getNumberOfEntities(SJobLog.class, options, null)).thenThrow(new SBonitaReadException(""));
        jobServiceImpl.getNumberOfJobLogs(options);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#searchJobLogs(org.bonitasoft.engine.persistence.QueryOptions)}.
     * 
     * @throws SBonitaReadException
     * @throws SBonitaSearchException
     */
    @Test
    public void searchJobLogs() throws SBonitaSearchException, SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);
        final SJobLog sJobLog = mock(SJobLog.class);
        when(readPersistenceService.searchEntity(SJobLog.class, options, null)).thenReturn(Collections.singletonList(sJobLog));

        assertEquals(sJobLog, jobServiceImpl.searchJobLogs(options).get(0));
    }

    @Test(expected = SBonitaSearchException.class)
    public void searchJobLogsThrowException() throws SBonitaSearchException, SBonitaReadException {
        final QueryOptions options = new QueryOptions(0, 10);
        doThrow(new SBonitaReadException("")).when(readPersistenceService).searchEntity(SJobLog.class, options, null);

        jobServiceImpl.searchJobLogs(options).get(0);
    }

}
