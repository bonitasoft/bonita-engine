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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.incident.Incident;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorNotFoundException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.impl.SJobLogImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author Elias Ricken de Medeiros
 */
public class JDBCJobListenerTest {

    @Mock
    private JobExecutionContext context;

    @Mock
    private JobDetail jobDetail;

    @Mock
    private JobDataMap jobDataMap;

    @Mock
    private Map<String, Object> wrappedMap;

    private final JobExecutionException exeption1 = new JobExecutionException(new Exception("generic exception"));

    private static final long JOB_DESCRIPTOR_ID = 50L;

    private JobService jobService;

    @Mock
    private TechnicalLoggerService logger;

    @InjectMocks
    private JDBCJobListener jdbcJobListener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        jobService = mock(JobService.class);
        doReturn(jobDetail).when(context).getJobDetail();
        doReturn(jobDataMap).when(jobDetail).getJobDataMap();
        doReturn(wrappedMap).when(jobDataMap).getWrappedMap();
        doReturn(String.valueOf(JOB_DESCRIPTOR_ID)).when(wrappedMap).get("jobId");

        doReturn(true).when(logger).isLoggable(JDBCJobListener.class, TechnicalLogSeverity.TRACE);

    }

    @Test
    public void jobWasExecuted_shouldCallIncidentServiceIfExceptionOccurs() throws Exception {
        final IncidentService incidentService = mock(IncidentService.class);
        doThrow(SBonitaSearchException.class).when(jobService).searchJobLogs(any(QueryOptions.class));
        doReturn("13651444").when(wrappedMap).get("tenantId");

        final JDBCJobListener jobListener = new JDBCJobListener(jobService, incidentService, logger);
        jobListener.jobWasExecuted(context, exeption1);

        verify(incidentService).report(anyLong(), any(Incident.class));
    }

    @Test
    public void create_jobLog_on_exception_if_no_previous_joblog() throws Exception {

        final List<SJobLog> jobLogs = Collections.emptyList();

        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));

        final JDBCJobListener jobListener = new JDBCJobListener(jobService, null, logger);
        jobListener.jobWasExecuted(context, exeption1);

        verify(jobService).createJobLog(argThat(new SJobLogMatcher(JOB_DESCRIPTOR_ID, Exception.class.getName(), 0)));
    }

    @Test
    public void update_jobLog_on_exception_if_previous_joblog() throws Exception {
        final SJobLogImpl jobLog = mock(SJobLogImpl.class);
        doReturn(1L).when(jobLog).getRetryNumber();
        final List<SJobLog> jobLogs = Collections.singletonList((SJobLog) jobLog);

        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));

        final JDBCJobListener jobListener = new JDBCJobListener(jobService, null, logger);
        jobListener.jobWasExecuted(context, exeption1);

        verify(jobLog).setLastUpdateDate(any(Long.class));
        verify(jobLog).setRetryNumber(2L);
        verify(jobLog).setLastMessage(contains(exeption1.getClass().getName()));

    }

    @Test
    public void clean_jobLog_if_no_exception_and_has_previous_jobLog() throws Exception {
        final SJobLogImpl jobLog = mock(SJobLogImpl.class);
        final List<SJobLog> jobLogs = Collections.singletonList((SJobLog) jobLog);
        final SJobDescriptor jobDesc = mock(SJobDescriptor.class);
        final SchedulerServiceImpl schedulerService = mock(SchedulerServiceImpl.class);

        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));
        doReturn(jobDesc).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(true).when(schedulerService).isStillScheduled(jobDesc);

        final JDBCJobListener jobListener = new JDBCJobListener(jobService, null, logger);
        jobListener.setBOSSchedulerService(schedulerService);
        jobListener.jobWasExecuted(context, null);

        verify(jobService).deleteJobLog(jobLog);
    }

    @Test
    public void should_call_deleteJob_itself_if_no_exception_and_Job_is_no_more_triggered_in_the_future() throws Exception {
        final List<SJobLog> jobLogs = Collections.emptyList();
        final SJobDescriptor jobDesc = mock(SJobDescriptor.class);
        doReturn("myJob").when(jobDesc).getJobName();
        final SchedulerServiceImpl schedulerService = mock(SchedulerServiceImpl.class);

        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));
        doReturn(jobDesc).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(false).when(schedulerService).isStillScheduled(jobDesc);

        final JDBCJobListener jobListener = new JDBCJobListener(jobService, null, logger);
        jobListener.setBOSSchedulerService(schedulerService);
        jobListener.jobWasExecuted(context, null);

        verify(schedulerService, times(1)).delete("myJob");
    }

    @Test
    public void should_deleteJob_ignore_SJobDescriptorNotFoundException_if_job_already_deleted() throws Exception {
        //        final List<SJobLog> jobLogs = Collections.emptyList();
        final SJobDescriptor jobDesc = mock(SJobDescriptor.class);
        doReturn("myJob").when(jobDesc).getJobName();
        final SchedulerServiceImpl schedulerService = mock(SchedulerServiceImpl.class);

        //when
        doThrow(SJobDescriptorNotFoundException.class).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(false).when(schedulerService).isStillScheduled(jobDesc);

        final JDBCJobListener jobListener = new JDBCJobListener(jobService, null, logger);
        jobListener.setBOSSchedulerService(schedulerService);
        jobListener.jobWasExecuted(context, null);

        verify(schedulerService, times(0)).delete("myJob");
    }

    @Test
    public void should_not_call_deleteJob_itself_if_no_exception_occurs_and_Job_is_still_triggered_in_the_future() throws Exception {
        final List<SJobLog> jobLogs = Collections.emptyList();
        final SJobDescriptor jobDesc = mock(SJobDescriptor.class);
        doReturn("myJob").when(jobDesc).getJobName();
        final SchedulerServiceImpl schedulerService = mock(SchedulerServiceImpl.class);

        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));
        doReturn(jobDesc).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(true).when(schedulerService).isStillScheduled(jobDesc);

        final JDBCJobListener jobListener = new JDBCJobListener(jobService, null, logger);
        jobListener.setBOSSchedulerService(schedulerService);
        jobListener.jobWasExecuted(context, null);

        verify(schedulerService, times(0)).delete("myJob");
    }

}
