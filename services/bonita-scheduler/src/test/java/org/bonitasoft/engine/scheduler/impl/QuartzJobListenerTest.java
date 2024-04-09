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

import static java.util.Collections.singletonList;
import static org.bonitasoft.engine.scheduler.impl.JobUtils.createJobDetails;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.nullable;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.bonitasoft.engine.scheduler.BonitaJobListener;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.quartz.DateBuilder.IntervalUnit;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.impl.JobExecutionContextImpl;
import org.quartz.impl.RemoteScheduler;
import org.quartz.impl.calendar.WeeklyCalendar;
import org.quartz.impl.triggers.CalendarIntervalTriggerImpl;
import org.quartz.spi.OperableTrigger;
import org.quartz.spi.TriggerFiredBundle;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class QuartzJobListenerTest {

    public static final long TENANT_ID = 86L;
    @Mock
    private SessionAccessor sessionAccessor;

    private QuartzJobListener quartzJobListener;

    @Mock
    private BonitaJobListener bonitaJobListener;

    private JobExecutionContext context;
    @Mock
    private SchedulerServiceImpl schedulerService;

    @Before
    public void setUp() {
        quartzJobListener = new QuartzJobListener(singletonList(bonitaJobListener), sessionAccessor);

        final Scheduler scheduler = new RemoteScheduler("schedId", "host", 1589);
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.getWrappedMap().put("jobId", "96");
        jobDataMap.getWrappedMap().put("tenantId", String.valueOf(TENANT_ID));
        final JobDetail jobDetail = JobBuilder.newJob().withIdentity("jobName", "jobGroup").setJobData(jobDataMap)
                .ofType(LogJob.class).build();

        final OperableTrigger trigger = new CalendarIntervalTriggerImpl("name", "group", "jobName", "jobGroup",
                new Date(), new Date(), IntervalUnit.DAY, 6);
        final TriggerFiredBundle firedBundle = new TriggerFiredBundle(jobDetail, trigger, new WeeklyCalendar(), true,
                new Date(), new Date(),
                new Date(), new Date());
        final Job job = new LogJob();
        context = new JobExecutionContextImpl(scheduler, firedBundle, job);
    }

    /**
     * Test method for {@link QuartzJobListener#jobToBeExecuted(org.quartz.JobExecutionContext)}.
     */
    @Test
    public final void jobToBeExecuted() {

        // When
        quartzJobListener.jobToBeExecuted(context);

        // then
        verify(bonitaJobListener).jobToBeExecuted(anyMap());
    }

    @Test
    public final void jobExecutionVetoed() throws Exception {
        // Given
        final Scheduler scheduler = new RemoteScheduler("schedId", "host", 1589);
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.getWrappedMap().put("jobId", "96");
        jobDataMap.getWrappedMap().put("tenantId", "86");
        final JobDetail jobDetail = JobBuilder.newJob().withIdentity("jobName", "jobGroup").setJobData(jobDataMap)
                .ofType(LogJob.class).build();

        final OperableTrigger trigger = new CalendarIntervalTriggerImpl("name", "group", "jobName", "jobGroup",
                new Date(), new Date(), IntervalUnit.DAY, 6);
        final TriggerFiredBundle firedBundle = new TriggerFiredBundle(jobDetail, trigger, new WeeklyCalendar(), true,
                new Date(), new Date(),
                new Date(), new Date());
        final ConcurrentQuartzJob job = new ConcurrentQuartzJob();
        job.setSchedulerService(schedulerService);
        job.setJobDetails(createJobDetails(1, 2));
        final JobExecutionContext context = new JobExecutionContextImpl(scheduler, firedBundle, job);

        // When
        quartzJobListener.jobExecutionVetoed(context);

        // then
        verify(bonitaJobListener).jobExecutionVetoed(anyMap());
    }

    @Test
    public final void jobWasExecuted() {
        // When
        quartzJobListener.jobWasExecuted(context, null);

        // then
        verify(bonitaJobListener).jobWasExecuted(anyMap(), nullable(SSchedulerException.class));
    }

}
