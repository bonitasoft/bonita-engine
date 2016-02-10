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
