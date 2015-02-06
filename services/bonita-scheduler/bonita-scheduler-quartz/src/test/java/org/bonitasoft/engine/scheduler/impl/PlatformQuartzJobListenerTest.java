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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.AbstractBonitaPlatformJobListener;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
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
public class PlatformQuartzJobListenerTest {

    private List<AbstractBonitaPlatformJobListener> bonitaJobListeners;

    private PlatformQuartzJobListener platformQuartzJobListener;

    @Before
    public void setUp() {
        bonitaJobListeners = Collections.singletonList(mock(AbstractBonitaPlatformJobListener.class));
        platformQuartzJobListener = new PlatformQuartzJobListener(bonitaJobListeners);
        MockitoAnnotations.initMocks(platformQuartzJobListener);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.PlatformQuartzJobListener#getName()}.
     */
    @Test
    public final void getName() {
        // When
        final String name = platformQuartzJobListener.getName();

        // Then
        assertTrue(name.startsWith("PlatformQuartzJobListener"));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.PlatformQuartzJobListener#jobToBeExecuted(org.quartz.JobExecutionContext)}.
     */
    @Test
    public final void jobToBeExecuted() {
        // Given
        final Scheduler scheduler = new RemoteScheduler("schedId", "host", 1589);
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.getWrappedMap().put("jobId", "96");
        jobDataMap.getWrappedMap().put("tenantId", "86");
        final JobDetail jobDetail = JobBuilder.newJob().withIdentity("jobName", "jobGroup").setJobData(jobDataMap).ofType(LogJob.class).build();

        final OperableTrigger trigger = new CalendarIntervalTriggerImpl("name", "group", "jobName", "jobGroup", new Date(), new Date(), IntervalUnit.DAY, 6);
        final TriggerFiredBundle firedBundle = new TriggerFiredBundle(jobDetail, trigger, new WeeklyCalendar(), true, new Date(), new Date(),
                new Date(), new Date());
        final Job job = new LogJob();
        final JobExecutionContext context = new JobExecutionContextImpl(scheduler, firedBundle, job);

        // When
        platformQuartzJobListener.jobToBeExecuted(context);

        // then
        verify(bonitaJobListeners.get(0)).jobToBeExecuted(anyMapOf(String.class, Serializable.class));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.PlatformQuartzJobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)}.
     */
    @Test
    public final void jobExecutionVetoed() {
        // Given
        final Scheduler scheduler = new RemoteScheduler("schedId", "host", 1589);
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.getWrappedMap().put("jobId", "96");
        jobDataMap.getWrappedMap().put("tenantId", "86");
        final JobDetail jobDetail = JobBuilder.newJob().withIdentity("jobName", "jobGroup").setJobData(jobDataMap).ofType(ConcurrentQuartzJob.class).build();

        final OperableTrigger trigger = new CalendarIntervalTriggerImpl("name", "group", "jobName", "jobGroup", new Date(), new Date(), IntervalUnit.DAY, 6);
        final TriggerFiredBundle firedBundle = new TriggerFiredBundle(jobDetail, trigger, new WeeklyCalendar(), true, new Date(), new Date(),
                new Date(), new Date());
        final ConcurrentQuartzJob job = new ConcurrentQuartzJob();
        job.setBosJob(new JobWrapper("name", mock(StatelessJob.class), mock(TechnicalLoggerService.class), 9, mock(EventService.class),
                mock(SessionAccessor.class), mock(TransactionService.class)));
        final JobExecutionContext context = new JobExecutionContextImpl(scheduler, firedBundle, job);

        // When
        platformQuartzJobListener.jobExecutionVetoed(context);

        // then
        verify(bonitaJobListeners.get(0)).jobExecutionVetoed(anyMapOf(String.class, Serializable.class));
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.scheduler.impl.PlatformQuartzJobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)}.
     */
    @Test
    public final void jobWasExecuted() {
        // Given
        final Scheduler scheduler = new RemoteScheduler("schedId", "host", 1589);
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.getWrappedMap().put("jobId", "96");
        jobDataMap.getWrappedMap().put("tenantId", "86");
        final JobDetail jobDetail = JobBuilder.newJob().withIdentity("jobName", "jobGroup").setJobData(jobDataMap).ofType(ConcurrentQuartzJob.class).build();

        final OperableTrigger trigger = new CalendarIntervalTriggerImpl("name", "group", "jobName", "jobGroup", new Date(), new Date(), IntervalUnit.DAY, 6);
        final TriggerFiredBundle firedBundle = new TriggerFiredBundle(jobDetail, trigger, new WeeklyCalendar(), true, new Date(), new Date(),
                new Date(), new Date());
        final Job job = new ConcurrentQuartzJob();
        final JobExecutionContext context = new JobExecutionContextImpl(scheduler, firedBundle, job);

        // When
        platformQuartzJobListener.jobWasExecuted(context, null);

        // then
        verify(bonitaJobListeners.get(0)).jobWasExecuted(anyMapOf(String.class, Serializable.class), any(SSchedulerException.class));
    }

}
