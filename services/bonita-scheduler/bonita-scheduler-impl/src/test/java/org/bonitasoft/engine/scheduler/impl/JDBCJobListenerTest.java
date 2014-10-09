/**
 * Copyright (C) 2013-2014 BonitaSoft S.A.
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

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.incident.Incident;
import org.bonitasoft.engine.incident.IncidentService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaSearchException;
import org.bonitasoft.engine.scheduler.AbstractBonitaJobListener;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorNotFoundException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.impl.SJobLogImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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

    private final SSchedulerException exeption1 = new SSchedulerException(new Exception("generic exception"));

    private final Map<String, Serializable> context = new HashMap<String, Serializable>();

    @Mock
    private JobService jobService;

    @Mock
    private TechnicalLoggerService logger;

    @Mock
    private SchedulerService schedulerService;

    @InjectMocks
    private JDBCJobListener jdbcJobListener;

    @Before
    public void setUp() {
        doReturn(true).when(logger).isLoggable(JDBCJobListener.class, TechnicalLogSeverity.TRACE);
        context.put(AbstractBonitaJobListener.JOB_DESCRIPTOR_ID, JOB_DESCRIPTOR_ID);
        context.put(AbstractBonitaJobListener.BOS_JOB, mock(StatelessJob.class));
    }

    @Test
    public void jobWasExecuted_shouldCallIncidentServiceIfExceptionOccurs() throws Exception {
        final IncidentService incidentService = mock(IncidentService.class);
        doThrow(SBonitaSearchException.class).when(jobService).searchJobLogs(any(QueryOptions.class));

        context.put(AbstractBonitaJobListener.TENANT_ID, 13651444L);

        final JDBCJobListener jobListener = new JDBCJobListener(schedulerService, jobService, incidentService, logger);
        jobListener.jobWasExecuted(context, exeption1);

        verify(incidentService).report(anyLong(), any(Incident.class));
    }

    @Test
    public void create_jobLog_on_exception_if_no_previous_joblog() throws Exception {

        final List<SJobLog> jobLogs = Collections.emptyList();

        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));

        final JDBCJobListener jobListener = new JDBCJobListener(schedulerService, jobService, null, logger);
        jobListener.jobWasExecuted(context, exeption1);

        verify(jobService).createJobLog(argThat(new SJobLogMatcher(JOB_DESCRIPTOR_ID, Exception.class.getName(), 0)));
    }

    @Test
    public void update_jobLog_on_exception_if_previous_joblog() throws Exception {
        final SJobLogImpl jobLog = mock(SJobLogImpl.class);
        doReturn(1L).when(jobLog).getRetryNumber();
        final List<SJobLog> jobLogs = Collections.singletonList((SJobLog) jobLog);

        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));

        final JDBCJobListener jobListener = new JDBCJobListener(schedulerService, jobService, null, logger);
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

        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));
        doReturn(jobDesc).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(true).when(schedulerService).isStillScheduled(jobDesc);

        final JDBCJobListener jobListener = new JDBCJobListener(schedulerService, jobService, null, logger);
        jobListener.jobWasExecuted(context, null);

        verify(jobService).deleteJobLog(jobLog);
    }

    @Test
    public void should_call_deleteJob_itself_if_no_exception_and_Job_is_no_more_triggered_in_the_future() throws Exception {
        final List<SJobLog> jobLogs = Collections.emptyList();
        final SJobDescriptor jobDesc = mock(SJobDescriptor.class);
        doReturn("myJob").when(jobDesc).getJobName();

        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));
        doReturn(jobDesc).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(false).when(schedulerService).isStillScheduled(jobDesc);

        final JDBCJobListener jobListener = new JDBCJobListener(schedulerService, jobService, null, logger);
        jobListener.jobWasExecuted(context, null);

        verify(schedulerService, times(1)).delete("myJob");
    }

    @Test
    public void should_deleteJob_ignore_SJobDescriptorNotFoundException_if_job_already_deleted() throws Exception {
        final SJobDescriptor jobDesc = mock(SJobDescriptor.class);
        doReturn("myJob").when(jobDesc).getJobName();

        // when
        doThrow(SJobDescriptorNotFoundException.class).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(false).when(schedulerService).isStillScheduled(jobDesc);

        final JDBCJobListener jobListener = new JDBCJobListener(schedulerService, jobService, null, logger);
        jobListener.jobWasExecuted(context, null);

        verify(schedulerService, times(0)).delete("myJob");
    }

    @Test
    public void should_not_call_deleteJob_itself_if_no_exception_occurs_and_Job_is_still_triggered_in_the_future() throws Exception {
        final List<SJobLog> jobLogs = Collections.emptyList();
        final SJobDescriptor jobDesc = mock(SJobDescriptor.class);
        doReturn("myJob").when(jobDesc).getJobName();

        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));
        doReturn(jobDesc).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(true).when(schedulerService).isStillScheduled(jobDesc);

        final JDBCJobListener jobListener = new JDBCJobListener(schedulerService, jobService, null, logger);
        jobListener.jobWasExecuted(context, null);

        verify(schedulerService, times(0)).delete("myJob");
    }

}
