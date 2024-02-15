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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.bonitasoft.engine.scheduler.impl.JobUtils.createJobDetails;
import static org.bonitasoft.engine.scheduler.impl.JobUtils.jobThatFails;
import static org.bonitasoft.engine.scheduler.impl.JobUtils.jobThatThrowASRetryableException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.quartz.JobExecutionException;

/**
 * @author Baptiste Mesta
 */
public class AbstractQuartzJobTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private SchedulerServiceImpl schedulerService;

    private ConcurrentQuartzJob abstractQuartzJob = new ConcurrentQuartzJob();

    @Before
    public void before() {
        abstractQuartzJob.setSchedulerService(schedulerService);
        abstractQuartzJob.setJobDetails(createJobDetails(1, 2));
    }

    @Test
    public void should_not_unschedule_job_on_exception() throws Exception {
        // job should never throw an exception and be handled by the JobWrapper
        // we do not unschedule the job in that case. we don't want to loose the job
        doReturn(jobThatFails()).when(schedulerService).getPersistedJob(any());

        try {
            abstractQuartzJob.execute(null);
            fail("should throw exception");
        } catch (JobExecutionException e) {
            assertThat(e.unscheduleFiringTrigger()).isFalse();
            assertThat(e.refireImmediately()).isFalse();
        }
    }

    @Test
    public void should_retry_job_that_failed_with_SRetryable() throws Exception {
        doReturn(jobThatThrowASRetryableException()).when(schedulerService).getPersistedJob(any());
        abstractQuartzJob.setSchedulerService(schedulerService);
        abstractQuartzJob.setJobDetails(createJobDetails(1, 2));

        abstractQuartzJob.execute(null);

        verify(schedulerService).executeAgain(2, 5000);
    }

}
