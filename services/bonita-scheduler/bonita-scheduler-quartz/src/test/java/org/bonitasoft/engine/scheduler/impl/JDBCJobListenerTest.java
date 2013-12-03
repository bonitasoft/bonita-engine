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
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobLog;
import org.bonitasoft.engine.scheduler.model.impl.SJobLogImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


/**
 * @author Elias Ricken de Medeiros
 *
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
    
    private JobExecutionException exeption1 = new JobExecutionException(new Exception("generic exception"));

    private static final long JOB_DESCRIPTOR_ID = 50L; 
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doReturn(jobDetail).when(context).getJobDetail();
        doReturn(jobDataMap).when(jobDetail).getJobDataMap();
        doReturn(wrappedMap).when(jobDataMap).getWrappedMap();
        doReturn(JOB_DESCRIPTOR_ID).when(wrappedMap).get("jobId");
    }
    

    @Test
    public void create_jobLog_on_exception_if_no_privious_joblog() throws Exception {
        
        JobService jobService = mock(JobService.class);
        List<SJobLog> jobLogs = Collections.emptyList();
        
        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));

        JDBCJobListener jobListener = new JDBCJobListener(jobService , null);
        jobListener.jobWasExecuted(context, exeption1);
        
        verify(jobService).createJobLog(argThat(new SJobLogMatcher(JOB_DESCRIPTOR_ID, Exception.class.getName(), 0)));
    }

    @Test
    public void update_jobLog_on_exception_if_privious_joblog() throws Exception {
        JobService jobService = mock(JobService.class);
        SJobLogImpl jobLog = mock(SJobLogImpl.class);
        doReturn(1L).when(jobLog).getRetryNumber();
        List<SJobLog> jobLogs = Collections.singletonList((SJobLog)jobLog);
        
        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));
        
        JDBCJobListener jobListener = new JDBCJobListener(jobService , null);
        jobListener.jobWasExecuted(context, exeption1);
        
        verify(jobLog).setLastUpdateDate(any(Long.class));
        verify(jobLog).setRetryNumber(2L);
        verify(jobLog).setLastMessage(contains(exeption1.getClass().getName()));
        
    }
    
    @Test
    public void clean_jobLog_if_no_exception_and_has_privious_jobLog() throws Exception {
        JobService jobService = mock(JobService.class);
        SJobLogImpl jobLog = mock(SJobLogImpl.class);
        List<SJobLog> jobLogs = Collections.singletonList((SJobLog)jobLog);
        SJobDescriptor jobDesc = mock(SJobDescriptor.class);
        SchedulerServiceImpl schedulerService = mock(SchedulerServiceImpl.class);
        
        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));
        doReturn(jobDesc).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(true).when(schedulerService).isStillScheduled(jobDesc);
        
        JDBCJobListener jobListener = new JDBCJobListener(jobService , null);
        jobListener.setBOSSchedulerService(schedulerService);
        jobListener.jobWasExecuted(context, null);
        
        verify(jobService).deleteJobLog(jobLog);
    }

    @Test
    public void deleteJobDescriptor_if_no_exception_and_Job_is_no_more_scheduled() throws Exception {
        JobService jobService = mock(JobService.class);
        List<SJobLog> jobLogs = Collections.emptyList();
        SJobDescriptor jobDesc = mock(SJobDescriptor.class);
        doReturn("myJob").when(jobDesc).getJobName();
        SchedulerServiceImpl schedulerService = mock(SchedulerServiceImpl.class);
        
        doReturn(jobLogs).when(jobService).searchJobLogs(any(QueryOptions.class));
        doReturn(jobDesc).when(jobService).getJobDescriptor(JOB_DESCRIPTOR_ID);
        doReturn(false).when(schedulerService).isStillScheduled(jobDesc);
        
        JDBCJobListener jobListener = new JDBCJobListener(jobService , null);
        jobListener.setBOSSchedulerService(schedulerService);
        jobListener.jobWasExecuted(context, null);
        
        verify(schedulerService).delete("myJob");
    }

    
}
