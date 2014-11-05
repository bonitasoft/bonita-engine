package org.bonitasoft.engine.jobs;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExecuteAgainJobSynchronizationTest {

    @Mock
    private JobService jobService;

    @Mock
    private SchedulerService schedulerService;

    @Mock
    private TechnicalLoggerService loggerService;

    @InjectMocks
    private ExecuteAgainJobSynchronization synchronization;

    @Test
    public void afterComplemtionShouldDoNothingBecauseOfItIsOutOfAnActiceTransaction() {
        synchronization.afterCompletion(null);

        verifyZeroInteractions(jobService, schedulerService, loggerService);
    }

    @Test
    public void beforeCommitShouldExecuteAgainTheJob() throws Exception {
        final List<SJobDescriptor> jobDescriptors = new ArrayList<SJobDescriptor>();
        jobDescriptors.add(mock(SJobDescriptor.class));
        when(jobService.searchJobDescriptors(any(QueryOptions.class))).thenReturn(jobDescriptors);

        synchronization.beforeCommit();

        verify(schedulerService).executeAgain(anyLong());
    }

    @Test
    public void beforeCommitShouldLogWhenAnExceptionOccurs() throws Exception {
        final SBonitaReadException exception = new SBonitaReadException("ouch! ");
        when(jobService.searchJobDescriptors(any(QueryOptions.class))).thenThrow(exception);
        when(loggerService.isLoggable(ExecuteAgainJobSynchronization.class, TechnicalLogSeverity.WARNING)).thenReturn(true);

        synchronization.beforeCommit();

        verify(loggerService).log(ExecuteAgainJobSynchronization.class, TechnicalLogSeverity.WARNING, "Unable to reschedule the job: null", exception);
    }

}
