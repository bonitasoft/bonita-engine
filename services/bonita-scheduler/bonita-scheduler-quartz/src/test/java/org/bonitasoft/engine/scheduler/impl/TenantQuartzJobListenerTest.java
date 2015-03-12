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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.STransactionNotFoundException;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
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
public class TenantQuartzJobListenerTest {

    private final static String GROUP_NAME = "2";

    @Mock
    private SessionAccessor sessionAccessor;

    @Mock
    private TransactionService transactionService;

    @Mock
    private TechnicalLoggerService logger;

    private TenantQuartzJobListener tenantQuartzJobListener;

    private List<AbstractBonitaTenantJobListener> bonitaJobListeners;

    private JobExecutionContext context;

    @Before
    public void setUp() {
        bonitaJobListeners = Collections.singletonList(mock(AbstractBonitaTenantJobListener.class));
        tenantQuartzJobListener = new TenantQuartzJobListener(bonitaJobListeners, GROUP_NAME, sessionAccessor, transactionService, logger);
        when(logger.isLoggable(any(Class.class), eq(TechnicalLogSeverity.WARNING))).thenReturn(true);
        MockitoAnnotations.initMocks(tenantQuartzJobListener);

        final Scheduler scheduler = new RemoteScheduler("schedId", "host", 1589);
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.getWrappedMap().put("jobId", "96");
        jobDataMap.getWrappedMap().put("tenantId", "86");
        final JobDetail jobDetail = JobBuilder.newJob().withIdentity("jobName", "jobGroup").setJobData(jobDataMap).ofType(LogJob.class).build();

        final OperableTrigger trigger = new CalendarIntervalTriggerImpl("name", "group", "jobName", "jobGroup", new Date(), new Date(), IntervalUnit.DAY, 6);
        final TriggerFiredBundle firedBundle = new TriggerFiredBundle(jobDetail, trigger, new WeeklyCalendar(), true, new Date(), new Date(),
                new Date(), new Date());
        final Job job = new LogJob();
        context = new JobExecutionContextImpl(scheduler, firedBundle, job);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.TenantQuartzJobListener#getName()}.
     */
    @Test
    public final void getName() {
        // When
        final String name = tenantQuartzJobListener.getName();

        // Then
        assertTrue(name.startsWith("TenantQuartzJobListener_" + GROUP_NAME));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.TenantQuartzJobListener#jobToBeExecuted(org.quartz.JobExecutionContext)}.
     */
    @Test
    public final void jobToBeExecuted() {

        // When
        tenantQuartzJobListener.jobToBeExecuted(context);

        // then
        verify(bonitaJobListeners.get(0)).jobToBeExecuted(anyMapOf(String.class, Serializable.class));
    }

    @Test
    public void jobToBeExecuted_should_log_when_registerBonitaSynchronization_failed_if_can_log() throws Exception {
        // Given
        final STransactionNotFoundException e = new STransactionNotFoundException();
        doThrow(e).when(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));

        // When
        tenantQuartzJobListener.jobToBeExecuted(context);

        // Then
        verify(sessionAccessor).setTenantId(Long.valueOf(GROUP_NAME));
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(logger).log(any(Class.class), eq(TechnicalLogSeverity.WARNING), eq(e));
    }

    @Test
    public void jobToBeExecuted_should_do_nothing_when_registerBonitaSynchronization_failed_if_cant_log() throws Exception {
        // Given
        final STransactionNotFoundException e = new STransactionNotFoundException();
        doThrow(e).when(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        when(logger.isLoggable(any(Class.class), eq(TechnicalLogSeverity.WARNING))).thenReturn(false);

        // When
        tenantQuartzJobListener.jobToBeExecuted(context);

        // Then
        verify(sessionAccessor).setTenantId(Long.valueOf(GROUP_NAME));
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(logger, never()).log(any(Class.class), any(TechnicalLogSeverity.class), any(Exception.class));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.scheduler.impl.TenantQuartzJobListener#jobExecutionVetoed(org.quartz.JobExecutionContext)}.
     */
    @Test
    public final void jobExecutionVetoed() {
        // Given
        final Scheduler scheduler = new RemoteScheduler("schedId", "host", 1589);
        final JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.getWrappedMap().put("jobId", "96");
        jobDataMap.getWrappedMap().put("tenantId", "86");
        final JobDetail jobDetail = JobBuilder.newJob().withIdentity("jobName", "jobGroup").setJobData(jobDataMap).ofType(LogJob.class).build();

        final OperableTrigger trigger = new CalendarIntervalTriggerImpl("name", "group", "jobName", "jobGroup", new Date(), new Date(), IntervalUnit.DAY, 6);
        final TriggerFiredBundle firedBundle = new TriggerFiredBundle(jobDetail, trigger, new WeeklyCalendar(), true, new Date(), new Date(),
                new Date(), new Date());
        final ConcurrentQuartzJob job = new ConcurrentQuartzJob();
        job.setBosJob(new StatelessJob() {

            private static final long serialVersionUID = -3387888226053088779L;

            @Override
            public String getName() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getDescription() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void execute() {
                // TODO Auto-generated method stub

            }

            @Override
            public void setAttributes(final Map<String, Serializable> attributes) {
                // TODO Auto-generated method stub

            }
        });
        final JobExecutionContext context = new JobExecutionContextImpl(scheduler, firedBundle, job);

        // When
        tenantQuartzJobListener.jobExecutionVetoed(context);

        // then
        verify(bonitaJobListeners.get(0)).jobExecutionVetoed(anyMapOf(String.class, Serializable.class));
    }

    @Test
    public void jobExecutionVetoed_should_log_when_registerBonitaSynchronization_failed_if_can_log() throws Exception {
        // Given
        final STransactionNotFoundException e = new STransactionNotFoundException();
        doThrow(e).when(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));

        // When
        tenantQuartzJobListener.jobExecutionVetoed(context);

        // Then
        verify(sessionAccessor).setTenantId(Long.valueOf(GROUP_NAME));
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(logger).log(any(Class.class), eq(TechnicalLogSeverity.WARNING), eq(e));
    }

    @Test
    public void jobExecutionVetoed_should_do_nothing_when_registerBonitaSynchronization_failed_if_cant_log() throws Exception {
        // Given
        final STransactionNotFoundException e = new STransactionNotFoundException();
        doThrow(e).when(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        when(logger.isLoggable(any(Class.class), eq(TechnicalLogSeverity.WARNING))).thenReturn(false);

        // When
        tenantQuartzJobListener.jobExecutionVetoed(context);

        // Then
        verify(sessionAccessor).setTenantId(Long.valueOf(GROUP_NAME));
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(logger, never()).log(any(Class.class), any(TechnicalLogSeverity.class), any(Exception.class));
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.scheduler.impl.TenantQuartzJobListener#jobWasExecuted(org.quartz.JobExecutionContext, org.quartz.JobExecutionException)}.
     */
    @Test
    public final void jobWasExecuted() {
        // When
        tenantQuartzJobListener.jobWasExecuted(context, null);

        // then
        verify(bonitaJobListeners.get(0)).jobWasExecuted(anyMapOf(String.class, Serializable.class), any(SSchedulerException.class));
    }

    @Test
    public void jobWasExecuted_should_log_when_registerBonitaSynchronization_failed_if_can_log() throws Exception {
        // Given
        final STransactionNotFoundException e = new STransactionNotFoundException();
        doThrow(e).when(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));

        // When
        tenantQuartzJobListener.jobWasExecuted(context, null);

        // Then
        verify(sessionAccessor).setTenantId(Long.valueOf(GROUP_NAME));
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(logger).log(any(Class.class), eq(TechnicalLogSeverity.WARNING), eq(e));
    }

    @Test
    public void jobWasExecuted_should_do_nothing_when_registerBonitaSynchronization_failed_if_cant_log() throws Exception {
        // Given
        final STransactionNotFoundException e = new STransactionNotFoundException();
        doThrow(e).when(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        when(logger.isLoggable(any(Class.class), eq(TechnicalLogSeverity.WARNING))).thenReturn(false);

        // When
        tenantQuartzJobListener.jobWasExecuted(context, null);

        // Then
        verify(sessionAccessor).setTenantId(Long.valueOf(GROUP_NAME));
        verify(transactionService).registerBonitaSynchronization(any(BonitaTransactionSynchronizationImpl.class));
        verify(logger, never()).log(any(Class.class), any(TechnicalLogSeverity.class), any(Exception.class));
    }

}
