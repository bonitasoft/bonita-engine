package org.bonitasoft.engine.scheduler.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventActionType;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SDeleteEvent;
import org.bonitasoft.engine.events.model.SInsertEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilder;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.persistence.PersistentObject;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.SRecorderException;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.scheduler.exception.failedJob.SFailedJobReadException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorCreationException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorReadException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogCreationException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobLog.SJobLogReadException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterCreationException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterDeletionException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterNotFoundException;
import org.bonitasoft.engine.scheduler.exception.jobParameter.SJobParameterReadException;
import org.bonitasoft.engine.scheduler.model.SFailedJob;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class JobServiceImplTest {

    private static final long NUMBER_OF_JOBS = 15l;

    private static final int NEW_SIZE = 15;

    private static final int OLD_SIZE = 7;

    private static final long JOB_DESCRIPTOR_ID = 2l;

    private static final long RETURNED = 5l;

    private static final long TENANT_ID = 1l;

    private JobServiceImpl jobService;

    @Mock
    private ReadPersistenceService readPersistenceService;

    @Mock
    private EventService eventService;

    @Mock
    private Recorder recorder;

    @Mock
    private SJobDescriptor sJobDescriptor;

    @Mock
    private SRecorderException sRecorderException;

    @Mock
    private SEventBuilderFactory sEventBuilderFactory;

    @Mock
    SEventBuilder sEventBuilder;

    @Mock
    private SInsertEvent sInsertEvent;

    @Mock
    private SDeleteEvent sDeleteEvent;

    @Mock
    private SBonitaReadException sBonitaReadException;

    @Mock
    private SBonitaException sBonitaException;

    @Mock
    private QueryOptions queryOptions;

    @Mock
    private SJobParameter sJobParameter;

    @Mock
    private SBonitaSearchException sBonitaSearchException;

    @Mock
    private SJobLog sJobLog;

    @Mock
    private SFailedJob sFailedJob;

    @Before
    public void before() throws Exception {
        doReturn("message").when(sRecorderException).getLocalizedMessage();

        when(sJobDescriptor.getJobName()).thenReturn("jobName");
        when(sJobDescriptor.getDescription()).thenReturn("job description");
        when(sJobDescriptor.getId()).thenReturn(1l);
        when(sJobDescriptor.getJobClassName()).thenReturn(this.getClass().getName());
        when(sJobDescriptor.disallowConcurrentExecution()).thenReturn(true);

        jobService = spy(new JobServiceImpl(eventService, recorder, readPersistenceService));
        doReturn(sEventBuilderFactory).when(jobService).getEventBuilderFactory();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createJobDescriptor_should_check_null_job_descriptor() throws Exception {

        //when
        jobService.createJobDescriptor(null, TENANT_ID);

        //then exception

    }

    @Test(expected = IllegalArgumentException.class)
    public void createJobDescriptor_should_check_bad_job_descriptor() throws Exception {
        //given
        when(sJobDescriptor.getJobName()).thenReturn(null);

        //when
        jobService.createJobDescriptor(sJobDescriptor, TENANT_ID);

        //then exception

    }

    @Test(expected = SJobDescriptorCreationException.class)
    public void createJobDescriptor_with_recorderexception() throws Exception {
        //given
        doThrow(sRecorderException).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));
        when(sJobDescriptor.getJobName()).thenReturn("jobName");

        //when
        jobService.createJobDescriptor(sJobDescriptor, TENANT_ID);

        //then exception

    }

    @Test
    public void createJobDescriptor_should_return_jobDescriptor() throws Exception {
        //when
        final SJobDescriptor createJobDescriptor = jobService.createJobDescriptor(sJobDescriptor, TENANT_ID);

        //then
        assertThat(createJobDescriptor.getJobName()).isEqualTo(sJobDescriptor.getJobName());
        assertThat(createJobDescriptor.getDescription()).isEqualTo(sJobDescriptor.getDescription());
        assertThat(createJobDescriptor.disallowConcurrentExecution()).isEqualTo(sJobDescriptor.disallowConcurrentExecution());
        verify(recorder, times(1)).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

    }

    @Test
    public void createJobDescriptor_with_eventHandlers() throws Exception {
        //given
        doReturn(true).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doReturn(sInsertEvent).when(jobService).createInsertEvent(any(PersistentObject.class), anyString());

        //when
        jobService.createJobDescriptor(sJobDescriptor, TENANT_ID);

        //then
        verify(jobService, times(1)).createInsertEvent(any(PersistentObject.class), anyString());

    }

    @Test
    public void deleteJobDescriptor_byId() throws Exception {
        doReturn(sJobDescriptor).when(jobService).getJobDescriptor(anyLong());

        //when
        jobService.deleteJobDescriptor(1l);

        //then
        verify(jobService, times(1)).deleteJobDescriptor(sJobDescriptor);

    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteJobDescriptor_with_null_Descriptor_should_throw_exception() throws Exception {

        //when
        jobService.deleteJobDescriptor(null);

        //then exception

    }

    @Test
    public void deleteJobDescriptor_byDescriptor() throws Exception {

        //when
        jobService.deleteJobDescriptor(sJobDescriptor);

        //then
        verify(recorder, times(1)).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

    }

    @Test
    public void deleteJobDescriptor_by_job_name() throws Exception {
        final List<SJobDescriptor> jobDescriptors = new ArrayList<SJobDescriptor>();
        for (int i = 0; i < NEW_SIZE; i++) {
            jobDescriptors.add(sJobDescriptor);
        }
        //given
        doReturn(jobDescriptors).when(jobService).searchJobDescriptors(any(QueryOptions.class));

        //when
        jobService.deleteJobDescriptorByJobName("jobName");

        //then
        verify(jobService, times(1)).deleteJobDescriptor(any(SJobDescriptor.class));

    }

    @Test(expected = SJobDescriptorNotFoundException.class)
    public void deleteJobDescriptor_should_throw_not_found_exception() throws Exception {
        //given
        doThrow(SRecorderException.class).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        //when
        jobService.deleteJobDescriptor(1l);

        //then exception
    }

    @Test(expected = SJobDescriptorDeletionException.class)
    public void deleteJobDescriptor_should_throw_not_found_exceptionz() throws Exception {
        //given
        doReturn(sJobDescriptor).when(jobService).getJobDescriptor(anyLong());
        doThrow(sRecorderException).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        //when
        jobService.deleteJobDescriptor(1l);

        //then exception
    }

    @Test
    public void deleteJobDescriptor_withHandlers() throws Exception {
        //given
        doReturn(true).when(eventService).hasHandlers(anyString(), any(EventActionType.class));
        doReturn(sDeleteEvent).when(jobService).createDeleteEvent(any(PersistentObject.class), anyString());

        //when
        jobService.deleteJobDescriptor(sJobDescriptor);

        //then
        verify(jobService, times(1)).createDeleteEvent(any(PersistentObject.class), anyString());

    }

    @SuppressWarnings("unchecked")
    @Test(expected = SJobDescriptorReadException.class)
    public void getJobDescriptor_should_throw_exception() throws Exception {
        //given
        doThrow(sBonitaReadException).when(readPersistenceService).selectById(any(SelectByIdDescriptor.class));

        //when
        jobService.getJobDescriptor(1l);

        //then

    }

    @SuppressWarnings("unchecked")
    @Test
    public void getJobDescriptor_should_return_jobDescriptor() throws Exception {
        //given
        doReturn(sJobDescriptor).when(readPersistenceService).selectById(any(SelectByIdDescriptor.class));

        //when
        final SJobDescriptor jobDescriptor = jobService.getJobDescriptor(1l);

        //then
        assertThat(jobDescriptor).isEqualTo(sJobDescriptor);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void getNumberOfJobDescriptors() throws Exception {
        //given
        doReturn(RETURNED).when(readPersistenceService).getNumberOfEntities(any(Class.class), any(QueryOptions.class), anyMapOf(String.class, Object.class));

        final long numberOfJobDescriptors = jobService.getNumberOfJobDescriptors(queryOptions);

        assertThat(numberOfJobDescriptors).isEqualTo(RETURNED);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SBonitaSearchException.class)
    public void getNumberOfJobDescriptors_should_throw_exception() throws Exception {
        //given
        doThrow(sBonitaReadException).when(readPersistenceService).getNumberOfEntities(any(Class.class), any(QueryOptions.class),
                anyMapOf(String.class, Object.class));

        //when
        jobService.getNumberOfJobDescriptors(queryOptions);

        //then
    }

    @SuppressWarnings("unchecked")
    @Test(expected = SBonitaSearchException.class)
    public void searchJobDescriptors_should_throw_exception() throws Exception {
        //given
        doThrow(sBonitaReadException).when(readPersistenceService).searchEntity(any(Class.class), any(QueryOptions.class),
                anyMapOf(String.class, Object.class));

        //when
        jobService.searchJobDescriptors(queryOptions);

        //then exception
    }

    @SuppressWarnings("unchecked")
    @Test
    public void searchJobDescriptors() throws Exception {
        //when
        jobService.searchJobDescriptors(queryOptions);

        //then
        verify(readPersistenceService).searchEntity(any(Class.class), any(QueryOptions.class),
                anyMapOf(String.class, Object.class));
    }

    @Test
    public void setJobParameters_should_remove_existing_parameters() throws Exception {
        //given
        final List<SJobParameter> newJobParameterList = new ArrayList<SJobParameter>();
        for (int i = 0; i < NEW_SIZE; i++) {
            newJobParameterList.add(sJobParameter);
        }
        final List<SJobParameter> oldJobParameterList = new ArrayList<SJobParameter>();
        for (int i = 0; i < OLD_SIZE; i++) {
            oldJobParameterList.add(sJobParameter);
        }
        doReturn(oldJobParameterList).when(jobService).searchJobParameters(any(QueryOptions.class));

        //when
        final List<SJobParameter> jobParameters = jobService.setJobParameters(TENANT_ID, JOB_DESCRIPTOR_ID, newJobParameterList);

        //then
        verify(jobService).deleteAllJobParameters(JOB_DESCRIPTOR_ID);
        verify(jobService, times(OLD_SIZE)).deleteJobParameter(sJobParameter);

        verify(jobService, times(NEW_SIZE)).createJobParameter(sJobParameter, TENANT_ID, JOB_DESCRIPTOR_ID);
        assertThat(jobParameters).hasSameSizeAs(newJobParameterList);

    }

    @Test(expected = SJobParameterCreationException.class)
    public void setJobParameters_should_throw_exception_when_existing_parameters_deletion_throws_exception() throws Exception {
        //given
        final List<SJobParameter> newJobParameterList = new ArrayList<SJobParameter>();
        for (int i = 0; i < NEW_SIZE; i++) {
            newJobParameterList.add(sJobParameter);
        }
        doThrow(sBonitaSearchException).when(jobService).searchJobParameters(any(QueryOptions.class));

        //when
        jobService.setJobParameters(TENANT_ID, JOB_DESCRIPTOR_ID, newJobParameterList);

        //then exception
    }

    @Test
    public void createJobParameters_with_null_parameters_should_return_emptylist() throws Exception {
        //when
        final List<SJobParameter> jobParameters = jobService.createJobParameters(null, TENANT_ID, JOB_DESCRIPTOR_ID);

        //then
        assertThat(jobParameters).isNotNull().isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createJobParameter_with_null_parameters_should_throw_exception() throws Exception {
        //when
        jobService.createJobParameter(null, TENANT_ID, JOB_DESCRIPTOR_ID);

        //then exception
    }

    @Test(expected = SJobParameterCreationException.class)
    public void createJobParameter_should_throw_exception() throws Exception {
        //given
        doThrow(sRecorderException).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

        //when
        jobService.createJobParameter(sJobParameter, TENANT_ID, JOB_DESCRIPTOR_ID);

        //then exception
    }

    @Test
    public void deleteJobParameter() throws Exception {
        //given
        doReturn(sJobParameter).when(jobService).getJobParameter(anyLong());

        //when
        jobService.deleteJobParameter(1l);

        //then exception
        verify(jobService).deleteJobParameter(sJobParameter);
        verify(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

    }

    @Test(expected = SJobParameterDeletionException.class)
    public void deleteJobParameter_should_throw_exception() throws Exception {
        //given
        doThrow(sRecorderException).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));
        doReturn(sJobParameter).when(jobService).getJobParameter(anyLong());

        //when
        jobService.deleteJobParameter(1l);

    }

    @Test
    public void getJobParameter() throws Exception {
        //given
        doReturn(sJobParameter).when(readPersistenceService).selectById(any(SelectByIdDescriptor.class));

        //when
        final SJobParameter parameter = jobService.getJobParameter(1l);

        //then
        assertThat(parameter).isEqualTo(sJobParameter);

    }

    @Test(expected = SJobParameterNotFoundException.class)
    public void getJobParameter_should_throw_exception_with_null_parameter() throws Exception {
        //given
        doReturn(null).when(readPersistenceService).selectById(any(SelectByIdDescriptor.class));

        //when
        jobService.getJobParameter(1l);

        //then exception

    }

    @Test(expected = SJobParameterReadException.class)
    public void getJobParameter_should_throw_exception() throws Exception {
        //given
        doThrow(sBonitaReadException).when(readPersistenceService).selectById(any(SelectByIdDescriptor.class));

        //when
        jobService.getJobParameter(1l);

        //then exception

    }

    @Test
    public void searchJobParameter() throws Exception {
        //given
        final List<SJobParameter> jobParameterList = new ArrayList<SJobParameter>();
        for (int i = 0; i < NEW_SIZE; i++) {
            jobParameterList.add(sJobParameter);
        }
        doReturn(jobParameterList).when(readPersistenceService).searchEntity(any(Class.class), any(QueryOptions.class), anyMap());

        //when
        final List<SJobParameter> searchJobParameters = jobService.searchJobParameters(queryOptions);

        //then
        verify(readPersistenceService).searchEntity(any(Class.class), any(QueryOptions.class), anyMap());
        assertThat(searchJobParameters).hasSameSizeAs(jobParameterList);

    }

    @Test(expected = SBonitaSearchException.class)
    public void searchJobParameter_should_throw_exception() throws Exception {
        //given
        doThrow(sBonitaReadException).when(readPersistenceService).searchEntity(any(Class.class), any(QueryOptions.class), anyMap());

        //when
        jobService.searchJobParameters(queryOptions);

        //then exception

    }

    @Test
    public void createJobLog() throws Exception {
        //when
        jobService.createJobLog(sJobLog);

        //then exception
        verify(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

    }

    @Test(expected = SJobLogCreationException.class)
    public void createJobLog_should_throw_exception() throws Exception {
        //given
        doThrow(sRecorderException).when(recorder).recordInsert(any(InsertRecord.class), any(SInsertEvent.class));

        //when
        jobService.createJobLog(sJobLog);

        //then exception
    }

    @Test
    public void deleteJobLog() throws Exception {
        //when
        jobService.deleteJobLog(sJobLog);

        //then exception
        verify(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

    }

    @Test
    public void deleteJobLogById() throws Exception {
        //given
        doReturn(sJobLog).when(jobService).getJobLog(anyLong());

        //when

        jobService.deleteJobLog(1l);

        //then exception
        verify(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

    }

    @Test(expected = SJobLogDeletionException.class)
    public void deleteJobLog_should_throw_exception() throws Exception {
        //given
        doThrow(sRecorderException).when(recorder).recordDelete(any(DeleteRecord.class), any(SDeleteEvent.class));

        //when
        jobService.deleteJobLog(sJobLog);

        //then exception
    }

    @Test
    public void getJobLog() throws Exception {
        //given
        doReturn(sJobLog).when(readPersistenceService).selectById(any(SelectByIdDescriptor.class));

        //when
        final SJobLog parameter = jobService.getJobLog(1l);

        //then
        assertThat(parameter).isEqualTo(sJobLog);

    }

    @Test(expected = SJobLogNotFoundException.class)
    public void getJobLog_should_throw_exception_with_null_parameter() throws Exception {
        //given
        doReturn(null).when(readPersistenceService).selectById(any(SelectByIdDescriptor.class));

        //when
        jobService.getJobLog(1l);

        //then exception

    }

    @Test(expected = SJobLogReadException.class)
    public void getJobLog_should_throw_exception() throws Exception {
        //given
        doThrow(sBonitaReadException).when(readPersistenceService).selectById(any(SelectByIdDescriptor.class));

        //when
        jobService.getJobLog(1l);

        //then exception

    }

    @Test
    public void getNumberOfJobLog() throws Exception {
        //given
        doReturn(NUMBER_OF_JOBS).when(readPersistenceService).getNumberOfEntities(any(Class.class), any(QueryOptions.class),
                anyMapOf(String.class, Object.class));

        //when
        final long numberOfJobLogs = jobService.getNumberOfJobLogs(queryOptions);

        //then
        assertThat(numberOfJobLogs).isEqualTo(15l);
    }

    @Test(expected = SBonitaSearchException.class)
    public void getNumberOfJobLog_should_throw_exception() throws Exception {
        //given
        doThrow(sBonitaReadException).when(readPersistenceService).getNumberOfEntities(any(Class.class), any(QueryOptions.class),
                anyMapOf(String.class, Object.class));

        //when
        jobService.getNumberOfJobLogs(queryOptions);

        //then exception
    }

    @Test
    public void searchJobLog() throws Exception {
        //given
        final List<SJobLog> JobLogList = new ArrayList<SJobLog>();
        for (int i = 0; i < NEW_SIZE; i++) {
            JobLogList.add(sJobLog);
        }
        doReturn(JobLogList).when(readPersistenceService).searchEntity(any(Class.class), any(QueryOptions.class), anyMap());

        //when
        final List<SJobLog> searchJobLogs = jobService.searchJobLogs(queryOptions);

        //then
        verify(readPersistenceService).searchEntity(any(Class.class), any(QueryOptions.class), anyMap());
        assertThat(searchJobLogs).hasSameSizeAs(JobLogList);

    }

    @Test(expected = SBonitaSearchException.class)
    public void searchJobLog_should_throw_exception() throws Exception {
        //given
        doThrow(sBonitaReadException).when(readPersistenceService).searchEntity(any(Class.class), any(QueryOptions.class), anyMap());

        //when
        jobService.searchJobLogs(queryOptions);

        //then exception

    }

    @Test
    public void getFailedJobs() throws Exception {
        //given
        final List<SFailedJob> failedJobList = new ArrayList<SFailedJob>();
        for (int i = 0; i < NEW_SIZE; i++) {
            failedJobList.add(sFailedJob);
        }
        doReturn(failedJobList).when(readPersistenceService).selectList(any(SelectListDescriptor.class));

        //when
        final List<SFailedJob> returnedList = jobService.getFailedJobs(1, 10);

        //then
        verify(readPersistenceService).selectList(any(SelectListDescriptor.class));
        assertThat(returnedList).hasSameSizeAs(failedJobList);

    }

    @Test(expected = SFailedJobReadException.class)
    public void getFailedJobs_should_throw_exception() throws Exception {
        //given
        doThrow(sBonitaReadException).when(readPersistenceService).selectList(any(SelectListDescriptor.class));

        //when
        jobService.getFailedJobs(1, 10);

        //then exception
    }

}
