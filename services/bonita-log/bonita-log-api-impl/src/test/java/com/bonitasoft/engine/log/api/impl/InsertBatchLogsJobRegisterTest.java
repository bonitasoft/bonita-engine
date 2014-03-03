package com.bonitasoft.engine.log.api.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.UnixCronTrigger;
import org.bonitasoft.engine.services.PersistenceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;



@RunWith(MockitoJUnitRunner.class)
public class InsertBatchLogsJobRegisterTest {
    
    /**
     * 
     */
    private static final String INSERT_BATCH_LOGS_JOB = "InsertBatchLogsJob";

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private TechnicalLoggerService loggerService;
    
    @Mock
    private PersistenceService persistenceService;

    private final String cronExpression = "* */2 * * * ?";
    
    
    @Test
    public void registerJobIfNotRegistered_should_schedule_job_if_not_yet_registered() throws Exception {
        //given
        InsertBatchLogsJobRegister jobRegister = new InsertBatchLogsJobRegister(persistenceService, schedulerService, loggerService, cronExpression);
        when(schedulerService.getAllJobs()).thenReturn(Collections.<String>emptyList());

        //when
        jobRegister.registerJobIfNotRegistered();

        //then
        JobDescriptorMatcher jobDescriptorMatcher = new JobDescriptorMatcher(InsertBatchLogsJob.class.getName(), INSERT_BATCH_LOGS_JOB);
        UnixCronTriggerMatcher cronMatcher = new UnixCronTriggerMatcher(cronExpression);
        verify(schedulerService, times(1)).schedule(argThat(jobDescriptorMatcher), Matchers.<List<SJobParameter>>any(), argThat(cronMatcher));
    }

    @Test
    public void registerJobIfNotRegistered_should_not_schedule_job_if_already_registered() throws Exception {
        //given
        InsertBatchLogsJobRegister jobRegister = new InsertBatchLogsJobRegister(persistenceService, schedulerService, loggerService, cronExpression);
        when(schedulerService.getAllJobs()).thenReturn(Collections.<String>singletonList(INSERT_BATCH_LOGS_JOB)); //job already scheduled
        
        //when
        jobRegister.registerJobIfNotRegistered();
        
        //then
        verify(schedulerService, never()).schedule(any(SJobDescriptor.class), Matchers.<List<SJobParameter>>any(), any(UnixCronTrigger.class));
    }

}
