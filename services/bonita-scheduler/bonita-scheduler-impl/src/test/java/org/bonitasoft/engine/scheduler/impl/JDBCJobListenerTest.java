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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.incident.Incident;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.scheduler.AbstractBonitaJobListener;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerExecutor;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorNotFoundException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.impl.SJobDescriptorImpl;
import org.bonitasoft.engine.scheduler.model.impl.SJobLogImpl;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Elias Ricken de Medeiros
 * @author Celine Souchet
 */
@RunWith(MockitoJUnitRunner.class)
public class JDBCJobListenerTest {

    private static final Long JOB_DESCRIPTOR_ID = 50L;

    private static final Long TENANT_ID = 987L;

    private static final String JOB_NAME = "jobName";

    private final SSchedulerException exeption1 = new SSchedulerException(new Exception("generic exception"));

    private final Map<String, Serializable> context = new HashMap<String, Serializable>();

    @Mock
    private JobService jobService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private TransactionService transactionService;

    @Mock
    private SchedulerExecutor schedulerExecutor;

    @Mock
    private IncidentService incidentService;

    @Mock
    private JobLogCreator jobLogCreator;

    @InjectMocks
    private JDBCJobListener jdbcJobListener;

    @Before
    public void setUp() {
        doReturn(true).when(logger).isLoggable(JDBCJobListener.class, TechnicalLogSeverity.TRACE);
        doReturn(true).when(logger).isLoggable(JDBCJobListener.class, TechnicalLogSeverity.WARNING);
        context.put(AbstractBonitaJobListener.JOB_DESCRIPTOR_ID, JOB_DESCRIPTOR_ID);
        context.put(AbstractBonitaJobListener.TENANT_ID, TENANT_ID);
        context.put(AbstractBonitaJobListener.JOB_NAME, JOB_NAME);
        context.put(AbstractBonitaJobListener.BOS_JOB, mock(StatelessJob.class));
    }

    @Test
    public void jobWasExecuted_should_call_incidentServiceIfExceptionOccurs() throws Exception {
        // Given
        doThrow(SBonitaReadException.class).when(jobService).getJobLogs(JOB_DESCRIPTOR_ID, 0, 1);

        // When
        jdbcJobListener.jobWasExecuted(context, exeption1);

        // Then
        verify(incidentService).report(anyLong(), any(Incident.class));
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobWasExecuted_should_create_jobLog_on_exception_if_no_previous_joblog() throws Exception {
        // Given
        final List<SJobLog> jobLogs = Collections.emptyList();
        doReturn(jobLogs).when(jobService).getJobLogs(JOB_DESCRIPTOR_ID, 0, 1);

        // When
        jdbcJobListener.jobWasExecuted(context, exeption1);

        // Then
        verify(jobLogCreator).createJobLog(exeption1, JOB_DESCRIPTOR_ID);
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobWasExecuted_should_update_jobLog_on_exception_if_previous_joblog() throws Exception {
        // Given
        final SJobLogImpl jobLog = mock(SJobLogImpl.class);
        doReturn(1L).when(jobLog).getRetryNumber();
        final List<SJobLog> jobLogs = Collections.singletonList((SJobLog) jobLog);

        doReturn(jobLogs).when(jobService).getJobLogs(JOB_DESCRIPTOR_ID, 0, 1);

        long before = System.currentTimeMillis();

        // When
        jdbcJobListener.jobWasExecuted(context, exeption1);

        // Then
        ArgumentCaptor<EntityUpdateDescriptor> captor = ArgumentCaptor.forClass(EntityUpdateDescriptor.class);
        verify(jobService).updateJobLog(eq(jobLog), captor.capture());
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));

        EntityUpdateDescriptor updateDescriptor = captor.getValue();
        assertThat((String) updateDescriptor.getFields().get("lastMessage")).contains(exeption1.getMessage());
        assertThat((Long) updateDescriptor.getFields().get("lastUpdateDate")).isGreaterThanOrEqualTo(before);
        assertThat(updateDescriptor.getFields().get("retryNumber")).isEqualTo(2L);
    }

    @Test
    public void jobWasExecuted_should_log_if_can_log_when_no_job_descriptor_id() throws Exception {
        // Given
        context.put(AbstractBonitaJobListener.JOB_DESCRIPTOR_ID, null);

        // When
        jdbcJobListener.jobWasExecuted(context, exeption1);

        // Then
        verify(logger).log(any(Class.class), eq(TechnicalLogSeverity.WARNING), eq("An exception occurs during the job execution."), eq(exeption1));
        verify(transactionService, never()).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(jobService, never()).deleteJobLogs(anyLong());
        verify(jobService, never()).getJobLogs(anyLong(), eq(0), eq(1));
    }

    @Test
    public void jobWasExecuted_should_log_if_can_log_when_job_descriptor_id_equals_0() throws Exception {
        // Given
        context.put(AbstractBonitaJobListener.JOB_DESCRIPTOR_ID, 0L);

        // When
        jdbcJobListener.jobWasExecuted(context, exeption1);

        // Then
        verify(logger).log(any(Class.class), eq(TechnicalLogSeverity.WARNING), eq("An exception occurs during the job execution."), eq(exeption1));
        verify(transactionService, never()).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(jobService, never()).deleteJobLogs(anyLong());
        verify(jobService, never()).getJobLogs(anyLong(), eq(0), eq(1));
    }

    @Test
    public void jobWasExecuted_should_do_nothing_if_cant_log_when_no_job_descriptor_id() throws Exception {
        // Given
        context.put(AbstractBonitaJobListener.JOB_DESCRIPTOR_ID, null);
        when(logger.isLoggable(any(Class.class), eq(TechnicalLogSeverity.WARNING))).thenReturn(false);

        // When
        jdbcJobListener.jobWasExecuted(context, exeption1);

        // Then
        verify(logger, never()).log(any(Class.class), eq(TechnicalLogSeverity.WARNING), anyString(), eq(exeption1));
        verify(transactionService, never()).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(jobService, never()).deleteJobLogs(anyLong());
        verify(jobService, never()).getJobLogs(anyLong(), eq(0), eq(1));
    }

    @Test
    public void jobWasExecuted_should_do_nothing_if_cant_log_when_no_tenant_id() throws Exception {
        // Given
        context.put(AbstractBonitaJobListener.TENANT_ID, null);
        when(logger.isLoggable(any(Class.class), eq(TechnicalLogSeverity.WARNING))).thenReturn(false);

        // When
        jdbcJobListener.jobWasExecuted(context, exeption1);

        // Then
        verify(logger, never()).log(any(Class.class), eq(TechnicalLogSeverity.WARNING), anyString(), eq(exeption1));
        verify(transactionService, never()).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(jobService, never()).deleteJobLogs(anyLong());
        verify(jobService, never()).getJobLogs(anyLong(), eq(0), eq(1));
    }

    @Test
    public void jobWasExecuted_should_do_nothing_if_cant_log_when_tenant_id_equals_0() throws Exception {
        // Given
        context.put(AbstractBonitaJobListener.TENANT_ID, 0L);
        when(logger.isLoggable(any(Class.class), eq(TechnicalLogSeverity.WARNING))).thenReturn(false);

        // When
        jdbcJobListener.jobWasExecuted(context, exeption1);

        // Then
        verify(logger, never()).log(any(Class.class), eq(TechnicalLogSeverity.WARNING), anyString(), eq(exeption1));
        verify(transactionService, never()).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(jobService, never()).deleteJobLogs(anyLong());
        verify(jobService, never()).getJobLogs(anyLong(), eq(0), eq(1));
    }

    @Test
    public void jobWasExecuted_should_deleteJobLogs_when_no_job_exception() throws Exception {
        // Given
        final SJobDescriptor jobDesc = mock(SJobDescriptor.class);
        doReturn(jobDesc).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(true).when(schedulerService).isStillScheduled(jobDesc);

        // When
        jdbcJobListener.jobWasExecuted(context, null);

        // Then
        verify(jobService).deleteJobLogs(JOB_DESCRIPTOR_ID);
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobWasExecuted_should_deleteJob_and_log_if_no_job_exception_and_job_is_no_more_triggered() throws Exception {
        // Given
        final SJobDescriptor jobDesc = mock(SJobDescriptor.class);
        doReturn("myJob").when(jobDesc).getJobName();
        doReturn(jobDesc).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(false).when(schedulerService).isStillScheduled(jobDesc);

        // When
        jdbcJobListener.jobWasExecuted(context, null);

        // Then
        verify(schedulerService, times(1)).delete("myJob");
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobWasExecuted_should_do_nothing_if_delete_job_failed_and_cant_log_and_Job_is_no_more_triggered_in_the_future() throws Exception {
        // Given
        doReturn(new SJobDescriptorImpl("jobClassName", "jobName", true)).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(false).when(schedulerService).isStillScheduled(any(SJobDescriptor.class));
        doThrow(SJobDescriptorNotFoundException.class).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(false).when(logger).isLoggable(JDBCJobListener.class, TechnicalLogSeverity.TRACE);

        // When
        jdbcJobListener.jobWasExecuted(context, null);

        // Then
        verify(schedulerService, never()).delete("myJob");
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(logger, never()).log(any(Class.class), any(TechnicalLogSeverity.class), anyString());
    }

    @Test
    public void jobWasExecuted_should_log_if_delete_job_failed_and_log_and_Job_is_no_more_triggered_in_the_future() throws Exception {
        // Given
        doThrow(SJobDescriptorNotFoundException.class).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(false).when(schedulerService).isStillScheduled(new SJobDescriptorImpl("jobClassName", "jobName", true));

        // When
        jdbcJobListener.jobWasExecuted(context, null);

        // Then
        verify(schedulerService, never()).delete("myJob");
        verify(sessionAccessor).setTenantId(TENANT_ID);
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobWasExecuted_should_not_call_deleteJob_if_no_job_exception_and_job_is_still_triggered() throws Exception {
        // Given
        final SJobDescriptor jobDesc = mock(SJobDescriptor.class);
        doReturn("myJob").when(jobDesc).getJobName();
        doReturn(jobDesc).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(true).when(schedulerService).isStillScheduled(jobDesc);

        // When
        jdbcJobListener.jobWasExecuted(context, null);

        // Then
        verify(schedulerService, times(0)).delete("myJob");
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobWasExecuted_should_log_if_can_log_when_job_descriptor_doesnt_exist() throws Exception {
        // Given
        doReturn(null).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);

        // When
        jdbcJobListener.jobWasExecuted(context, null);

        // Then
        verify(schedulerService, never()).delete(anyString());
        verify(logger).log(any(Class.class), eq(TechnicalLogSeverity.TRACE), anyString());
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobWasExecuted_should_do_nothing_if_cant_log_when_job_descriptor_doesnt_exist() throws Exception {
        // Given
        doReturn(null).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(false).when(logger).isLoggable(JDBCJobListener.class, TechnicalLogSeverity.TRACE);

        // When
        jdbcJobListener.jobWasExecuted(context, null);

        // Then
        verify(schedulerService, never()).delete(anyString());
        verify(logger, never()).log(any(Class.class), eq(TechnicalLogSeverity.TRACE), anyString());
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobWasExecuted_should_do_nothing_when_no_bos_job() throws Exception {
        // Given
        context.put(AbstractBonitaJobListener.BOS_JOB, null);

        // When
        jdbcJobListener.jobWasExecuted(context, null);

        // Then
        verify(schedulerExecutor, never()).delete(JOB_NAME, String.valueOf(TENANT_ID));
        verify(logger, never()).log(any(Class.class), any(TechnicalLogSeverity.class), anyString(), any(Exception.class));
        verify(jobService, never()).deleteJobLogs(anyLong());
        verify(schedulerService, never()).delete(anyString());
        verify(jobService, never()).updateJobLog(any(SJobLog.class), any(EntityUpdateDescriptor.class));
        verify(jobService, never()).createJobLog(any(SJobLog.class));
        verify(transactionService, never()).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobToBeExecuted_should_delete_quartz_job_if_the_job_descriptor_doesnt_exist() throws Exception {
        // When
        jdbcJobListener.jobToBeExecuted(context);

        // Then
        verify(schedulerExecutor).delete(JOB_NAME, String.valueOf(TENANT_ID));
        verify(sessionAccessor).setTenantId(TENANT_ID);
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobToBeExecuted_should_log_when_registerBonitaSynchronization_failed_if_can_log() throws Exception {
        // Given
        final STransactionNotFoundException e = new STransactionNotFoundException();
        doThrow(e).when(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));

        // When
        jdbcJobListener.jobToBeExecuted(context);

        // Then
        verify(schedulerExecutor).delete(JOB_NAME, String.valueOf(TENANT_ID));
        verify(sessionAccessor).setTenantId(TENANT_ID);
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(logger).log(any(Class.class), eq(TechnicalLogSeverity.WARNING), eq(e));
    }

    @Test
    public void jobToBeExecuted_should_do_nothing_when_registerBonitaSynchronization_failed_if_cant_log() throws Exception {
        // Given
        final STransactionNotFoundException e = new STransactionNotFoundException();
        doThrow(e).when(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        when(logger.isLoggable(any(Class.class), eq(TechnicalLogSeverity.WARNING))).thenReturn(false);

        // When
        jdbcJobListener.jobToBeExecuted(context);

        // Then
        verify(schedulerExecutor).delete(JOB_NAME, String.valueOf(TENANT_ID));
        verify(sessionAccessor).setTenantId(TENANT_ID);
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(logger, never()).log(any(Class.class), any(TechnicalLogSeverity.class), anyString(), any(Exception.class));
    }

    @Test
    public void jobToBeExecuted_should_log_if_can_log_when_delete_quartz_job_failed() throws Exception {
        // Given
        final SSchedulerException exception = new SSchedulerException("plop");
        doThrow(exception).when(schedulerExecutor).delete(JOB_NAME, String.valueOf(TENANT_ID));

        // When
        jdbcJobListener.jobToBeExecuted(context);

        // Then
        verify(logger).log(any(Class.class), eq(TechnicalLogSeverity.WARNING),
                eq("An exception occurs during the check of the existence of the job descriptor '" + JOB_DESCRIPTOR_ID + "'."), eq(exception));
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobToBeExecuted_should_do_nothing_if_cant_log_when_delete_quartz_job_failed() throws Exception {
        // Given
        when(logger.isLoggable(any(Class.class), eq(TechnicalLogSeverity.WARNING))).thenReturn(false);
        final SSchedulerException exception = new SSchedulerException("plop");
        doThrow(exception).when(schedulerExecutor).delete(JOB_NAME, String.valueOf(TENANT_ID));

        // When
        jdbcJobListener.jobToBeExecuted(context);

        // Then
        verify(logger, never()).log(any(Class.class), eq(TechnicalLogSeverity.WARNING),
                eq("An exception occurs during the check of the existence of the job descriptor '" + JOB_DESCRIPTOR_ID + "'."), eq(exception));
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobToBeExecuted_should_do_nothing_when_job_descriptor_exists() throws Exception {
        // Given
        final SJobDescriptor sJobDescriptor = new SJobDescriptorImpl();
        doReturn(sJobDescriptor).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);

        // When
        jdbcJobListener.jobToBeExecuted(context);

        // Then
        verify(schedulerExecutor, never()).delete(JOB_NAME, String.valueOf(TENANT_ID));
        verify(logger, never()).log(any(Class.class), eq(TechnicalLogSeverity.WARNING),
                eq("An exception occurs during the check of the existence of the job descriptor '" + JOB_DESCRIPTOR_ID + "'."), any(SSchedulerException.class));
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobToBeExecuted_should_do_nothing_when_no_job_descriptor_id() throws Exception {
        // Given
        context.put(AbstractBonitaJobListener.JOB_DESCRIPTOR_ID, null);

        // When
        jdbcJobListener.jobToBeExecuted(context);

        // Then
        verify(schedulerExecutor, never()).delete(JOB_NAME, String.valueOf(TENANT_ID));
        verify(logger, never()).log(any(Class.class), eq(TechnicalLogSeverity.WARNING),
                eq("An exception occurs during the check of the existence of the job descriptor '" + JOB_DESCRIPTOR_ID + "'."), any(SSchedulerException.class));
        verify(transactionService, never()).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobToBeExecuted_should_do_nothing_when_job_descriptor_id_equals_0() throws Exception {
        // Given
        context.put(AbstractBonitaJobListener.JOB_DESCRIPTOR_ID, 0L);

        // When
        jdbcJobListener.jobToBeExecuted(context);

        // Then
        verify(schedulerExecutor, never()).delete(JOB_NAME, String.valueOf(TENANT_ID));
        verify(logger, never()).log(any(Class.class), eq(TechnicalLogSeverity.WARNING),
                eq("An exception occurs during the check of the existence of the job descriptor '" + JOB_DESCRIPTOR_ID + "'."), any(SSchedulerException.class));
        verify(transactionService, never()).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobToBeExecuted_should_do_nothing_when_no_tenant_id() throws Exception {
        // Given
        context.put(AbstractBonitaJobListener.TENANT_ID, null);

        // When
        jdbcJobListener.jobToBeExecuted(context);

        // Then
        verify(schedulerExecutor, never()).delete(JOB_NAME, String.valueOf(TENANT_ID));
        verify(logger, never()).log(any(Class.class), eq(TechnicalLogSeverity.WARNING),
                eq("An exception occurs during the check of the existence of the job descriptor '" + JOB_DESCRIPTOR_ID + "'."), any(SSchedulerException.class));
        verify(transactionService, never()).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void jobToBeExecuted_should_do_nothing_when_tenant_id_equals_0() throws Exception {
        // Given
        context.put(AbstractBonitaJobListener.TENANT_ID, 0L);

        // When
        jdbcJobListener.jobToBeExecuted(context);

        // Then
        verify(schedulerExecutor, never()).delete(JOB_NAME, String.valueOf(TENANT_ID));
        verify(logger, never()).log(any(Class.class), eq(TechnicalLogSeverity.WARNING),
                eq("An exception occurs during the check of the existence of the job descriptor '" + JOB_DESCRIPTOR_ID + "'."), any(SSchedulerException.class));
        verify(transactionService, never()).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

    @Test
    public void getName() {
        // When
        final String name = jdbcJobListener.getName();

        // Then
        assertThat("JDBCJobListener").isEqualTo(name);
    }

    @Test
    public void jobExecutionVetoed_should_do_nothing() throws Exception {
        // When
        jdbcJobListener.jobExecutionVetoed(context);

        // Then
        verify(schedulerExecutor, never()).delete(anyString(), anyString());
        verify(logger, never()).log(any(Class.class), eq(TechnicalLogSeverity.WARNING), anyString(), any(SSchedulerException.class));
        verify(transactionService, never()).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
    }

}
