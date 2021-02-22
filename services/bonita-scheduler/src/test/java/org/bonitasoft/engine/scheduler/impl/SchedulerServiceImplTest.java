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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerExecutor;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorCreationException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.service.ServicesResolver;
import org.bonitasoft.engine.services.PersistenceService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SchedulerServiceImplTest {

    private static final long TENANT_ID = 1L;
    private static final long JOB_DESCRIPTOR_ID = 32187L;

    private SchedulerServiceImpl schedulerService;
    @Mock
    private SchedulerExecutor schedulerExecutor;
    @Mock
    private JobService jobService;
    @Mock
    private EventService eventService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private PersistenceService persistenceService;
    @Mock
    private ServicesResolver servicesResolver;
    @Mock
    private TransactionService transactionService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        transactionService = mock(TransactionService.class);

        given(sessionAccessor.getTenantId()).willReturn(TENANT_ID);

        schedulerService = new SchedulerServiceImpl(schedulerExecutor, jobService, logger, eventService,
                transactionService, sessionAccessor, servicesResolver,
                persistenceService);

    }

    @Test
    public void isStarted_return_true_if_schedulorExecutor_is_started() throws Exception {
        when(schedulerExecutor.isStarted()).thenReturn(true);

        final boolean started = schedulerService.isStarted();

        assertThat(started).isTrue();
    }

    @Test
    public void isStarted_return_false_if_schedulorExecutor_is_not_started() throws Exception {
        when(schedulerExecutor.isStarted()).thenReturn(false);

        final boolean started = schedulerService.isStarted();

        assertThat(started).isFalse();
    }

    @Test
    public void isStopped_return_true_if_executor_is_shutodown() throws Exception {
        when(schedulerExecutor.isShutdown()).thenReturn(true);

        final boolean stopped = schedulerService.isStopped();

        assertThat(stopped).isTrue();
    }

    @Test
    public void isStopped_return_false_if_executor_is_not_shutodown() throws Exception {
        when(schedulerExecutor.isShutdown()).thenReturn(false);

        final boolean stopped = schedulerService.isStopped();

        assertThat(stopped).isFalse();
    }

    @Test
    public void start_start_schedulerexecutor_and_fire_a_start_event() throws Exception {
        schedulerService.start();

        verify(schedulerExecutor).start();
        verify(eventService).fireEvent(any(SEvent.class));
    }

    @Test
    public void stop_shutdown_schedulerexecutor_and_fire_a_stop_event() throws Exception {
        schedulerService.stop();

        verify(schedulerExecutor).shutdown();
        verify(eventService).fireEvent(any(SEvent.class));
    }

    @Test
    public void delete_delete_job_and_jobDescription() throws Exception {
        final String jobName = "aJobName";

        schedulerService.delete(jobName);

        verify(schedulerExecutor).delete(jobName, String.valueOf(TENANT_ID));
        verify(jobService).deleteJobDescriptorByJobName(jobName);
    }

    @Test
    public void delete_return_schedulerexecutor_deletion_status() throws Exception {
        final boolean expectedDeletionStatus = new Random().nextBoolean();
        final String jobName = "jobName";
        when(schedulerExecutor.delete(jobName, String.valueOf(TENANT_ID))).thenReturn(expectedDeletionStatus);

        final boolean deletionStatus = schedulerService.delete(jobName);

        assertThat(deletionStatus).isEqualTo(expectedDeletionStatus);
    }

    @Test(expected = SSchedulerException.class)
    public void cannot_execute_a_job_with_a_null_trigger() throws Exception {
        final SJobDescriptor jobDescriptor = mock(SJobDescriptor.class);
        final List<SJobParameter> parameters = new ArrayList<>();

        schedulerService.schedule(jobDescriptor, parameters, null);
    }

    @Test(expected = SSchedulerException.class)
    public void cannot_schedule_a_null_job() throws Exception {
        final Trigger trigger = mock(Trigger.class);
        when(jobService.createJobDescriptor(nullable(SJobDescriptor.class), any(Long.class)))
                .thenThrow(new SJobDescriptorCreationException(""));

        schedulerService.schedule(null, trigger);
    }

    @Test
    public void rescheduleErroneousTriggers_call_same_method_in_schedulerexecutor() throws Exception {
        schedulerService.rescheduleErroneousTriggers();

        verify(schedulerExecutor).rescheduleErroneousTriggers();
    }

    @Test
    public void should_pauseJobs_of_tenant_call_schedulerExecutor() throws Exception {
        schedulerService.resumeJobs(123L);
        verify(schedulerExecutor).resumeJobs("123");
    }

    @Test
    public void should_pauseJobs_of_tenant_call_schedulerExecutor_rethrow_exception() throws Exception {
        final SSchedulerException theException = new SSchedulerException("My exception");
        doThrow(theException).when(schedulerExecutor).resumeJobs("123");
        try {
            schedulerService.resumeJobs(123L);
            fail("should have rethrown the exception");
        } catch (final SSchedulerException e) {
            assertEquals(theException, e);
        }
    }

    @Test
    public void schedule_should_store_jobDescriptor_store_parameters_and_call_executor_schedule_using_tenantId()
            throws Exception {
        // given
        final long jogDescriptorId = 7L;
        final String jobName = "myJob";
        final SJobDescriptor jobDescriptor = mock(SJobDescriptor.class);
        given(jobDescriptor.getId()).willReturn(jogDescriptorId);
        given(jobDescriptor.getJobName()).willReturn(jobName);
        given(jobService.createJobDescriptor(jobDescriptor, TENANT_ID)).willReturn(jobDescriptor);
        final Trigger trigger = mock(Trigger.class);
        final List<SJobParameter> parameters = Collections.singletonList(mock(SJobParameter.class));

        // when
        schedulerService.schedule(jobDescriptor, parameters, trigger);

        // then
        verify(jobService, times(1)).createJobDescriptor(jobDescriptor, TENANT_ID);
        verify(jobService, times(1)).createJobParameters(parameters, TENANT_ID, jogDescriptorId);
        verify(schedulerExecutor, times(1)).schedule(jogDescriptorId, String.valueOf(TENANT_ID), jobName, trigger,
                false);
    }

    @Test
    public void should_delete_all_jobs_for_a_given_tenant() throws Exception {
        schedulerService.deleteJobs();

        verify(schedulerExecutor).deleteJobs(String.valueOf(TENANT_ID));
        verify(jobService).deleteAllJobDescriptors();
    }

    @Test
    public void rescheduleJob_should_call_rescheduleJob() throws Exception {
        // Given
        final String triggerName = "triggerName";
        final String groupName = "groupName";
        final Date triggerStartTime = new Date();

        // When
        schedulerService.rescheduleJob(triggerName, groupName, triggerStartTime);

        // Then
        verify(schedulerExecutor).rescheduleJob(triggerName, groupName, triggerStartTime);
    }

    @Test
    public void should_execute_again_an_existing_job() throws Exception {
        SJobDescriptor jobDescriptor = SJobDescriptor.builder().jobClassName("jobClassName")
                .jobName("jobName")
                .id(JOB_DESCRIPTOR_ID).build();
        doReturn(jobDescriptor)
                .when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);

        schedulerService.executeAgain(JOB_DESCRIPTOR_ID, 5000);

        verify(jobService, never()).setJobParameters(anyLong(), anyLong(), any());
        verify(schedulerExecutor).executeAgain(JOB_DESCRIPTOR_ID, String.valueOf(TENANT_ID), "jobName", false, 5000);
        verify(jobService, never()).deleteJobLogs(JOB_DESCRIPTOR_ID);
    }

    @Test
    public void should_retry_a_job_that_failed() throws Exception {
        SJobDescriptor jobDescriptor = SJobDescriptor.builder().jobClassName("jobClassName")
                .jobName("jobName")
                .id(JOB_DESCRIPTOR_ID).build();
        doReturn(jobDescriptor)
                .when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);

        schedulerService.retryJobThatFailed(JOB_DESCRIPTOR_ID);

        verify(jobService, never()).setJobParameters(anyLong(), anyLong(), any());
        verify(schedulerExecutor).executeAgain(JOB_DESCRIPTOR_ID, String.valueOf(TENANT_ID), "jobName", false, 0);
        verify(jobService).deleteJobLogs(JOB_DESCRIPTOR_ID);
    }

    @Test
    public void should_retry_a_job_that_failed_while_changing_parameters() throws Exception {
        SJobDescriptor jobDescriptor = SJobDescriptor.builder().jobClassName("jobClassName")
                .jobName("jobName")
                .id(JOB_DESCRIPTOR_ID).build();
        doReturn(jobDescriptor)
                .when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        List<SJobParameter> parameters = asList(SJobParameter.builder().key("a").value(1).build(),
                SJobParameter.builder().key("b").value(2).build());

        schedulerService.retryJobThatFailed(JOB_DESCRIPTOR_ID, parameters);

        verify(jobService).setJobParameters(TENANT_ID, JOB_DESCRIPTOR_ID, parameters);
        verify(schedulerExecutor).executeAgain(JOB_DESCRIPTOR_ID, String.valueOf(TENANT_ID), "jobName", false, 0);
        verify(jobService).deleteJobLogs(JOB_DESCRIPTOR_ID);
    }
}
