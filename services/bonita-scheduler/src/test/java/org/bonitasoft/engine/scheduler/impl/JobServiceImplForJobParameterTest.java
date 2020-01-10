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

import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterCreationException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterReadException;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.recorder.SelectDescriptorBuilder;
import org.bonitasoft.engine.services.QueriableLoggerService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class JobServiceImplForJobParameterTest {

    @Mock
    private EventService eventService;

    @Mock
    private QueriableLoggerService queriableLoggerService;

    @Mock
    private ReadPersistenceService readPersistenceService;

    @Mock
    private Recorder recorder;

    @Spy
    @InjectMocks
    private JobServiceImpl jobServiceImpl;

    /**
     * method for
     * {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#createJobParameters(java.util.List, long, long)}.
     *
     * @throws SJobParameterCreationException
     * @throws SRecorderException
     */
    @Test
    public final void createJobParameters() throws SJobParameterCreationException, SRecorderException {
        final long tenantId = 2;
        final long jobDescriptorId = 9;
        final SJobParameter sJobParameter = mock(SJobParameter.class);

        final List<SJobParameter> result = jobServiceImpl.createJobParameters(Collections.singletonList(sJobParameter),
                tenantId, jobDescriptorId);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sJobParameter, result.get(0));
    }

    @Test
    public final void createJobParameters_with_null_parameters_should_return_empty_list() throws Exception {
        // Given
        final long tenantId = 2;
        final long jobDescriptorId = 9;

        // When
        final List<SJobParameter> result = jobServiceImpl.createJobParameters(null, tenantId, jobDescriptorId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public final void createJobParameter_with_empty_list_should_return_empty_list() throws Exception {
        // Given
        final long tenantId = 2;
        final long jobDescriptorId = 9;

        // When
        final List<SJobParameter> result = jobServiceImpl.createJobParameters(Collections.<SJobParameter> emptyList(),
                tenantId, jobDescriptorId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = SJobParameterCreationException.class)
    public final void createJobParameters_should_throw_exception_when_recorder_failed()
            throws SJobParameterCreationException, SRecorderException {
        // Given
        final long tenantId = 2;
        final long jobDescriptorId = 9;
        final SJobParameter sJobParameter = mock(SJobParameter.class);

        doThrow(new SRecorderException("plop")).when(recorder).recordInsert(any(InsertRecord.class),
                nullable(String.class));

        // When
        jobServiceImpl.createJobParameters(Collections.singletonList(sJobParameter), tenantId, jobDescriptorId);
    }

    /**
     * method for
     * {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#createJobParameter(org.bonitasoft.engine.scheduler.model.SJobParameter, long, long)}.
     *
     * @throws SJobParameterCreationException
     * @throws SRecorderException
     */
    @Test
    public final void createJobParameter() throws SJobParameterCreationException, SRecorderException {
        final long tenantId = 2;
        final long jobDescriptorId = 9;
        final SJobParameter sJobParameter = mock(SJobParameter.class);

        final SJobParameter result = jobServiceImpl.createJobParameter(sJobParameter, tenantId, jobDescriptorId);
        assertNotNull(result);
        assertEquals(sJobParameter, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void createJobParameter_with_null_parameters_should_throw_exception() throws Exception {
        // Given
        final long tenantId = 2;
        final long jobDescriptorId = 9;

        // When
        jobServiceImpl.createJobParameter(null, tenantId, jobDescriptorId);
    }

    @Test(expected = SJobParameterCreationException.class)
    public final void createJobParameter_should_throw_exception_when_recorder_failed() throws Exception {
        // Given
        final long tenantId = 2;
        final long jobDescriptorId = 9;
        final SJobParameter sJobParameter = mock(SJobParameter.class);

        doThrow(new SRecorderException("plop")).when(recorder).recordInsert(any(InsertRecord.class),
                nullable(String.class));

        // When
        jobServiceImpl.createJobParameter(sJobParameter, tenantId, jobDescriptorId);
    }

    /**
     * method for {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#deleteJobParameter(long)}.
     *
     * @throws SBonitaReadException
     * @throws SRecorderException
     * @throws SJobParameterDeletionException
     * @throws SJobParameterReadException
     * @throws SJobParameterNotFoundException
     */
    @Test
    public final void deleteJobParameter_by_id() throws Exception {
        // Given
        final SJobParameter sJobParameter = mock(SJobParameter.class);

        doReturn(sJobParameter).when(readPersistenceService)
                .selectById(ArgumentMatchers.<SelectByIdDescriptor<SJobParameter>> any());

        // When
        jobServiceImpl.deleteJobParameter(3);

        // Then
        verify(jobServiceImpl).deleteJobParameter(sJobParameter);
        verify(recorder).recordDelete(any(DeleteRecord.class), nullable(String.class));
    }

    @Test(expected = SJobParameterNotFoundException.class)
    public final void deleteNotExistingJobParameter_by_id() throws Exception {
        // Given
        when(readPersistenceService.selectById(ArgumentMatchers.<SelectByIdDescriptor<SJobParameter>> any()))
                .thenReturn(null);

        // When
        jobServiceImpl.deleteJobParameter(1);
    }

    @Test(expected = SJobParameterDeletionException.class)
    public void deleteJobParameter_by_id_should_throw_exception_when_recorder_failed() throws Exception {
        // Given
        final SJobParameter sJobParameter = mock(SJobParameter.class);

        doReturn(sJobParameter).when(readPersistenceService)
                .selectById(ArgumentMatchers.<SelectByIdDescriptor<SJobParameter>> any());
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class),
                nullable(String.class));

        // When
        jobServiceImpl.deleteJobParameter(3);
    }

    /**
     * method for
     * {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#deleteJobParameter(org.bonitasoft.engine.scheduler.model.SJobParameter)}.
     *
     * @throws SRecorderException
     * @throws SJobParameterDeletionException
     */
    @Test
    public final void deleteJobParameter_by_object() throws SRecorderException, SJobParameterDeletionException {
        final SJobParameter sJobParameter = mock(SJobParameter.class);

        jobServiceImpl.deleteJobParameter(sJobParameter);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void deleteNullJobParameter_by_object() throws SJobParameterDeletionException {
        jobServiceImpl.deleteJobParameter(null);
    }

    @Test(expected = SJobParameterDeletionException.class)
    public void deleteJobParameter_by_object_should_throw_exception_when_recorder_failed() throws Exception {
        final SJobParameter sJobParameter = mock(SJobParameter.class);

        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class),
                nullable(String.class));

        jobServiceImpl.deleteJobParameter(sJobParameter);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#getJobParameter(long)}.
     *
     * @throws SJobParameterReadException
     * @throws SJobParameterNotFoundException
     * @throws SBonitaReadException
     */
    @Test
    public void getJobParameter_by_id() throws Exception {
        // Given
        final long jobParameterId = 1;
        final SJobParameter sJobParameter = mock(SJobParameter.class);
        when(readPersistenceService.selectById(
                SelectDescriptorBuilder.getElementById(SJobParameter.class, "SJobParameter", jobParameterId)))
                        .thenReturn(
                                sJobParameter);

        // When
        final SJobParameter result = jobServiceImpl.getJobParameter(jobParameterId);

        // Then
        Assert.assertEquals(sJobParameter, result);
    }

    @Test(expected = SJobParameterNotFoundException.class)
    public void getJobParameter_by_id_should_throw_exception_when_not_exist()
            throws SBonitaReadException, SJobParameterNotFoundException,
            SJobParameterReadException {
        // Given
        final long jobParameterId = 455;
        doReturn(null).when(readPersistenceService).selectById(
                SelectDescriptorBuilder.getElementById(SJobParameter.class, "SJobParameter", jobParameterId));

        // When
        jobServiceImpl.getJobParameter(jobParameterId);
    }

    @Test(expected = SJobParameterReadException.class)
    public void getJobParameter_by_id_should_throw_exception_when_persistenceService_failed() throws Exception {
        // Given
        final long jobParameterId = 1;
        doThrow(new SBonitaReadException("")).when(readPersistenceService).selectById(
                SelectDescriptorBuilder.getElementById(SJobParameter.class, "SJobParameter", jobParameterId));

        // When
        jobServiceImpl.getJobParameter(jobParameterId);
    }

    @Test
    public void should_list_parameters() throws Exception {
        when(readPersistenceService.selectList(any()))
                .thenReturn(Collections.singletonList(SJobParameter.builder().key("key").value("value").build()));

        List<SJobParameter> result = jobServiceImpl.getJobParameters(123L);

        Assertions.assertThat(result.stream().collect(Collectors.toMap(SJobParameter::getKey, SJobParameter::getValue)))
                .containsOnly(entry("key", "value"));
    }

    /**
     * method for
     * {@link org.bonitasoft.engine.scheduler.impl.JobServiceImpl#setJobParameters(long, long, java.util.List)}.
     */
    @Test
    public final void setJobParameters() throws Exception {
        // Given
        final long tenantId = 12;
        final long jobDescriptorId = 8;
        final SJobParameter sJobParameter = mock(SJobParameter.class);

        final List<SJobParameter> sJobParameters = Collections.singletonList(sJobParameter);
        doReturn(sJobParameters).when(readPersistenceService).selectList(any());

        // When
        final List<SJobParameter> result = jobServiceImpl.setJobParameters(tenantId, jobDescriptorId, sJobParameters);

        // Then
        assertEquals(sJobParameters, result);
        verify(jobServiceImpl).deleteAllJobParameters(jobDescriptorId);
        verify(jobServiceImpl, times(sJobParameters.size())).deleteJobParameter(sJobParameter);
        verify(jobServiceImpl, times(sJobParameters.size())).createJobParameter(sJobParameter, tenantId,
                jobDescriptorId);
    }

    @Test
    public void setJobParametersWithEmptyList() throws Exception {
        // Given
        final long tenantId = 12;
        final long jobDescriptorId = 8;

        // When
        final List<SJobParameter> result = jobServiceImpl.setJobParameters(tenantId, jobDescriptorId,
                Collections.<SJobParameter> emptyList());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void setJobParametersWithNullList() throws Exception {
        // Given
        final long tenantId = 12;
        final long jobDescriptorId = 8;

        // When
        final List<SJobParameter> result = jobServiceImpl.setJobParameters(tenantId, jobDescriptorId, null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test(expected = SJobParameterCreationException.class)
    public void setJobParameters_should_throw_exception_when_search_failed()
            throws SBonitaReadException, SBonitaReadException,
            SJobParameterCreationException {
        // Given
        final long tenantId = 12;
        final long jobDescriptorId = 8;
        final SJobParameter sJobParameter = mock(SJobParameter.class);

        doThrow(new SBonitaReadException("")).when(readPersistenceService).selectList(any());

        // When
        jobServiceImpl.setJobParameters(tenantId, jobDescriptorId, Collections.singletonList(sJobParameter));
    }

    @Test(expected = SJobParameterCreationException.class)
    public void setJobParameters_should_throw_exception_when_delete_failed() throws Exception {
        // Given
        final long tenantId = 12;
        final long jobDescriptorId = 8;
        final SJobParameter sJobParameter = mock(SJobParameter.class);

        doReturn(Collections.singletonList(sJobParameter)).when(readPersistenceService).selectList(any());
        doThrow(new SRecorderException("")).when(recorder).recordDelete(any(DeleteRecord.class),
                nullable(String.class));

        // When
        jobServiceImpl.setJobParameters(tenantId, jobDescriptorId, Collections.singletonList(sJobParameter));
    }

    @Test(expected = SJobParameterCreationException.class)
    public final void setJobParameters_should_throw_exception_when_create_failed() throws Exception {
        // Given
        final long tenantId = 12;
        final long jobDescriptorId = 8;
        final SJobParameter sJobParameter = mock(SJobParameter.class);

        doThrow(new SRecorderException("plop")).when(recorder).recordInsert(any(InsertRecord.class),
                nullable(String.class));

        // When
        jobServiceImpl.setJobParameters(tenantId, jobDescriptorId, Collections.singletonList(sJobParameter));
    }

}
