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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.builders.SEventBuilder;
import org.bonitasoft.engine.events.model.builders.SEventBuilderFactory;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.scheduler.AbstractBonitaPlatformJobListener;
import org.bonitasoft.engine.scheduler.AbstractBonitaTenantJobListener;
import org.bonitasoft.engine.scheduler.InjectedService;
import org.bonitasoft.engine.scheduler.JobService;
import org.bonitasoft.engine.scheduler.SchedulerExecutor;
import org.bonitasoft.engine.scheduler.ServicesResolver;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.scheduler.exception.SSchedulerException;
import org.bonitasoft.engine.scheduler.exception.jobDescriptor.SJobDescriptorCreationException;
import org.bonitasoft.engine.scheduler.model.SJobDescriptor;
import org.bonitasoft.engine.scheduler.model.SJobParameter;
import org.bonitasoft.engine.scheduler.trigger.Trigger;
import org.bonitasoft.engine.sessionaccessor.STenantIdNotSetException;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(BuilderFactory.class)
public class SchedulerServiceImplTest {

    private static final long TENANT_ID = 1L;

    SchedulerServiceImpl schedulerService;

    @Mock
    private SchedulerExecutor schedulerExecutor;

    @Mock
    private JobService jobService;

    @Mock
    private EventService eventService;

    @Mock
    private SessionAccessor sessionAccessor;

    ServicesResolver servicesResolver;

    @Before
    public void setUp() throws STenantIdNotSetException {
        PowerMockito.mockStatic(BuilderFactory.class);
        initMocks(this);

        final TechnicalLoggerService logger = mock(TechnicalLoggerService.class);
        final TransactionService transactionService = mock(TransactionService.class);

        final SEventBuilder sEventBuilder = mock(SEventBuilder.class);
        final SEventBuilderFactory sEventBuilderFactory = mock(SEventBuilderFactory.class);
        when(BuilderFactory.get(SEventBuilderFactory.class)).thenReturn(sEventBuilderFactory);

        when(sEventBuilderFactory.createNewInstance(anyString())).thenReturn(sEventBuilder);
        when(sEventBuilderFactory.createInsertEvent(anyString())).thenReturn(sEventBuilder);
        when(sEventBuilder.setObject(any(Object.class))).thenReturn(sEventBuilder);

        given(sessionAccessor.getTenantId()).willReturn(TENANT_ID);

        servicesResolver = mock(ServicesResolver.class);
        schedulerService = new SchedulerServiceImpl(schedulerExecutor, jobService, logger, eventService, transactionService, sessionAccessor, servicesResolver);
    }

    @Test
    public void isStarted_return_true_if_schedulorExecutor_is_started() throws Exception {
        when(schedulerExecutor.isStarted()).thenReturn(true);

        final boolean started = schedulerService.isStarted();

        assertThat(started).isTrue();
    }

    @Test
    public void isStarted_return_false_if_schedulorExecutor_is_not_started() throws Exception {
        when(schedulerExecutor.isStarted()).thenReturn(false);

        final boolean started = schedulerService.isStarted();

        assertThat(started).isFalse();
    }

    @Test
    public void isStopped_return_true_if_executor_is_shutodown() throws Exception {
        when(schedulerExecutor.isShutdown()).thenReturn(true);

        final boolean stopped = schedulerService.isStopped();

        assertThat(stopped).isTrue();
    }

    @Test
    public void isStopped_return_false_if_executor_is_not_shutodown() throws Exception {
        when(schedulerExecutor.isShutdown()).thenReturn(false);

        final boolean stopped = schedulerService.isStopped();

        assertThat(stopped).isFalse();
    }

    @Test
    public void start_start_schedulerexecutor_and_fire_a_start_event() throws Exception {
        schedulerService.start();

        verify(schedulerExecutor).start();
        verify(eventService).fireEvent(any(SEvent.class));
    }

    @Test
    public void stop_shutdown_schedulerexecutor_and_fire_a_stop_event() throws Exception {
        schedulerService.stop();

        verify(schedulerExecutor).shutdown();
        verify(eventService).fireEvent(any(SEvent.class));
    }

    @Test
    public void delete_delete_job_and_jobDescription() throws Exception {
        final String jobName = "aJobName";

        schedulerService.delete(jobName);

        verify(schedulerExecutor).delete(jobName, String.valueOf(TENANT_ID));
        verify(jobService).deleteJobDescriptorByJobName(jobName);
    }

    @Test
    public void delete_return_schedulerexecutor_deletion_status() throws Exception {
        final boolean expectedDeletionStatus = new Random().nextBoolean();
        final String jobName = "jobName";
        when(schedulerExecutor.delete(jobName, String.valueOf(TENANT_ID))).thenReturn(expectedDeletionStatus);

        final boolean deletionStatus = schedulerService.delete(jobName);

        assertThat(deletionStatus).isEqualTo(expectedDeletionStatus);
    }

    @Test(expected = SSchedulerException.class)
    public void cannot_execute_a_job_with_a_null_trigger() throws Exception {
        final SJobDescriptor jobDescriptor = mock(SJobDescriptor.class);
        final List<SJobParameter> parameters = new ArrayList<SJobParameter>();

        schedulerService.schedule(jobDescriptor, parameters, null);
    }

    @Test(expected = SSchedulerException.class)
    public void cannot_schedule_a_null_job() throws Exception {
        final Trigger trigger = mock(Trigger.class);
        when(jobService.createJobDescriptor(any(SJobDescriptor.class), any(Long.class))).thenThrow(new SJobDescriptorCreationException(""));

        schedulerService.schedule(null, trigger);
    }

    @Test
    public void rescheduleErroneousTriggers_call_same_method_in_schedulerexecutor() throws Exception {
        schedulerService.rescheduleErroneousTriggers();

        verify(schedulerExecutor).rescheduleErroneousTriggers();
    }

    @Test
    public void should_pauseJobs_of_tenant_call_schedulerExecutor() throws Exception {
        schedulerService.resumeJobs(123l);

        verify(schedulerExecutor, times(1)).resumeJobs("123");
    }

    @Test
    public void should_pauseJobs_of_tenant_call_schedulerExecutor_rethrow_exception() throws Exception {
        final SSchedulerException theException = new SSchedulerException("My exception");
        doThrow(theException).when(schedulerExecutor).resumeJobs("123");
        try {
            schedulerService.resumeJobs(123l);
            fail("should have rethrown the exception");
        } catch (final SSchedulerException e) {
            assertEquals(theException, e);
        }
    }

    @Test
    public void should_injectService_inject_setter_hacing_the_annotation() throws Exception {
        final BeanThatNeedMyService beanThatNeedMyService = new BeanThatNeedMyService();

        final Long myService = new Long(1);
        when(servicesResolver.lookup("myService")).thenReturn(myService);

        schedulerService.injectServices(beanThatNeedMyService);

        assertEquals(myService, beanThatNeedMyService.getMyService());

    }

    @Test
    public void schedule_should_store_jobDescriptor_store_parameters_and_call_executor_schedule_using_tenantId()
            throws Exception {
        // given
        final long jogDescriptorId = 7L;
        final String jobName = "myJob";
        final boolean disallowConcurrency = true;
        final SJobDescriptor jobDescriptor = mock(SJobDescriptor.class);
        given(jobDescriptor.getId()).willReturn(jogDescriptorId);
        given(jobDescriptor.getJobName()).willReturn(jobName);
        given(jobDescriptor.disallowConcurrentExecution()).willReturn(disallowConcurrency);
        given(jobService.createJobDescriptor(jobDescriptor, TENANT_ID)).willReturn(jobDescriptor);
        final Trigger trigger = mock(Trigger.class);
        final List<SJobParameter> parameters = Collections.singletonList(mock(SJobParameter.class));

        // when
        schedulerService.schedule(jobDescriptor, parameters, trigger);

        // then
        verify(jobService, times(1)).createJobDescriptor(jobDescriptor, TENANT_ID);
        verify(jobService, times(1)).createJobParameters(parameters, TENANT_ID, jogDescriptorId);
        verify(schedulerExecutor, times(1)).schedule(jogDescriptorId, String.valueOf(TENANT_ID), jobName, trigger, disallowConcurrency);
    }

    @Test
    public void executeNow_should_use_tenantId_on_jobDescriptor_jobParameters_and_call_executor_executeNow()
            throws Exception {
        // given
        final SJobDescriptor jobDescriptor = mock(SJobDescriptor.class);
        given(jobService.createJobDescriptor(jobDescriptor, TENANT_ID)).willReturn(jobDescriptor);
        final List<SJobParameter> parameters = Collections.singletonList(mock(SJobParameter.class));

        // when
        schedulerService.executeNow(jobDescriptor, parameters);

        // then
        verify(jobService, times(1)).createJobDescriptor(jobDescriptor, TENANT_ID);
        verify(jobService, times(1)).createJobParameters(Matchers.<List<SJobParameter>> any(), eq(TENANT_ID), anyLong());
        verify(schedulerExecutor, times(1)).executeNow(anyLong(), eq(String.valueOf(TENANT_ID)), anyString(), anyBoolean());
    }

    @Test
    public void should_delete_all_jobs_for_a_given_tenant() throws Exception {
        schedulerService.deleteJobs();

        verify(schedulerExecutor).deleteJobs(String.valueOf(TENANT_ID));
        verify(jobService).deleteAllJobDescriptors();
    }

    private final class BeanThatNeedMyService implements StatelessJob {

        private static final long serialVersionUID = 1L;

        private Object myService;

        @InjectedService
        public void setMyService(final Object myService) {
            this.myService = myService;
        }

        public Object getMyService() {
            return myService;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public void execute() {
        }

        @Override
        public void setAttributes(final Map<String, Serializable> attributes) {
        }
    }

    @Test
    public void rescheduleJob_should_call_rescheduleJob() throws Exception {
        // Given
        final String triggerName = "triggerName";
        final String groupName = "groupName";
        final Date triggerStartTime = new Date();

        // When
        schedulerService.rescheduleJob(triggerName, groupName, triggerStartTime);

        // Then
        verify(schedulerExecutor).rescheduleJob(triggerName, groupName, triggerStartTime);
    }

    @Test
    public void addJobListener_for_TenantJobListener_should_call_addJobListener_for_TenantJobListener() throws SSchedulerException {
        // Given
        final String groupName = "groupName";
        final List<AbstractBonitaTenantJobListener> jobListeners = Collections.emptyList();

        // When
        schedulerService.addJobListener(jobListeners, groupName);

        // Then
        verify(schedulerExecutor).addJobListener(jobListeners, groupName);
    }

    @Test
    public void addJobListener_for_PlatormJobListener_should_call_addJobListener_for_PlatormJobListener() throws SSchedulerException {
        // Given
        final List<AbstractBonitaPlatformJobListener> jobListeners = Collections.emptyList();

        // When
        schedulerService.addJobListener(jobListeners);

        // Then
        verify(schedulerExecutor).addJobListener(jobListeners);
    }

    @Test
    public void initializeScheduler_should_call_initializeScheduler() throws Exception {
        // When
        schedulerService.initializeScheduler();

        // Then
        verify(schedulerExecutor).initializeScheduler();
    }
}
