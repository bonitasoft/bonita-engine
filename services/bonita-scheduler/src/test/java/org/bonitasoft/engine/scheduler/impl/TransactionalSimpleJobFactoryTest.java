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
import static org.bonitasoft.engine.scheduler.impl.JobUtils.createJobDetails;
import static org.mockito.BDDMockito.given;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.impl.JobDetailImpl;
import org.quartz.spi.TriggerFiredBundle;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalSimpleJobFactoryTest {

    @Mock
    private SchedulerServiceImpl schedulerServiceImpl;
    @Mock
    private TriggerFiredBundle bundle;
    @Mock
    private Scheduler scheduler;

    @InjectMocks
    TransactionalSimpleJobFactory factory;

    @Test
    public void should_inject_scheduler_and_job_details_on_new_job() throws Exception {
        JobDetailImpl jobDetails = createJobDetails(1, 2);
        jobDetails.setJobClass(ConcurrentQuartzJob.class);
        given(bundle.getJobDetail()).willReturn(jobDetails);

        Job newJob = factory.newJob(bundle, scheduler);

        assertThat(newJob).isInstanceOf(AbstractQuartzJob.class);
        assertThat(((AbstractQuartzJob) newJob).getSchedulerService()).isEqualTo(schedulerServiceImpl);
        assertThat(((AbstractQuartzJob) newJob).getJobDetail()).isEqualTo(jobDetails);
    }

}
