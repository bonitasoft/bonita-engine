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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.scheduler.AbstractBonitaJobListener;
import org.bonitasoft.engine.scheduler.StatelessJob;
import org.bonitasoft.engine.search.SSearchException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @version 6.4.0
 * @since 6.4.0
 */
@RunWith(MockitoJUnitRunner.class)
public class TimerEventTriggerJobListenerTest {

    private static final String TRIGGER_NAME = "TriggerName";

    private static final Long TENANT_ID = 987L;

    @Mock
    private EventInstanceService eventInstanceService;

    @Mock
    private TechnicalLoggerService logger;

    private TimerEventTriggerJobListener timerEventTriggerJobListener;

    private final Map<String, Serializable> context = new HashMap<String, Serializable>();

    @Before
    public void setUp() {
        timerEventTriggerJobListener = new TimerEventTriggerJobListener(eventInstanceService, TENANT_ID, logger);
        MockitoAnnotations.initMocks(timerEventTriggerJobListener);

        context.put(AbstractBonitaJobListener.TRIGGER_NAME, TRIGGER_NAME);
        context.put(AbstractBonitaJobListener.TENANT_ID, TENANT_ID);
        context.put(AbstractBonitaJobListener.BOS_JOB, mock(StatelessJob.class));
        context.put(AbstractBonitaJobListener.JOB_TYPE, TriggerTimerEventJob.class.getName());
    }

    /**
     * Test method for
     * {@link org.bonitasoft.engine.jobs.TimerEventTriggerJobListener#jobWasExecuted(java.util.Map, org.bonitasoft.engine.scheduler.exception.SSchedulerException)}
     * .
     */
    @Test
    public final void jobWasExecuted_should_delete_timer_event_trigger_if_exists() throws Exception {
        // Given
        final STimerEventTriggerInstance sTimerEventTriggerInstance = mock(STimerEventTriggerInstance.class);
        final List<STimerEventTriggerInstance> timerEventTriggerInstances = Collections.singletonList(sTimerEventTriggerInstance);
        doReturn(timerEventTriggerInstances).when(eventInstanceService).searchEventTriggerInstances(eq(STimerEventTriggerInstance.class),
                any(QueryOptions.class));

        // When
        timerEventTriggerJobListener.jobWasExecuted(context, null);

        // then
        verify(eventInstanceService).deleteEventTriggerInstance(sTimerEventTriggerInstance);
    }

    @Test
    public final void jobWasExecuted_should_not_search_for_event_triggers_when_executed_job_was_not_TimerEventJob() throws Exception {
        // Given
        final STimerEventTriggerInstance sTimerEventTriggerInstance = mock(STimerEventTriggerInstance.class);
        final List<STimerEventTriggerInstance> timerEventTriggerInstances = Collections.singletonList(sTimerEventTriggerInstance);
        doReturn(timerEventTriggerInstances).when(eventInstanceService).searchEventTriggerInstances(eq(STimerEventTriggerInstance.class),
                any(QueryOptions.class));

        context.put(AbstractBonitaJobListener.JOB_TYPE, "AnotherJob");

        // When
        timerEventTriggerJobListener.jobWasExecuted(context, null);

        // then
        verify(eventInstanceService, never()).searchEventTriggerInstances(same(STimerEventTriggerInstance.class), any(QueryOptions.class));
    }

    @Test
    public final void jobWasExecuted_should_do_nothing_if_timer_event_trigger_doesnt_exist() throws Exception {
        // Given
        doReturn(Collections.<STimerEventTriggerInstance> emptyList()).when(eventInstanceService).searchEventTriggerInstances(
                eq(STimerEventTriggerInstance.class), any(QueryOptions.class));

        // When
        timerEventTriggerJobListener.jobWasExecuted(context, null);

        // then
        verify(eventInstanceService, never()).deleteEventTriggerInstance(any(STimerEventTriggerInstance.class));
    }

    @Test
    public final void jobWasExecuted_should_not_delete_timer_event_trigger_if_no_engine_job() throws Exception {
        // Given
        context.put(AbstractBonitaJobListener.BOS_JOB, null);

        // When
        timerEventTriggerJobListener.jobWasExecuted(context, null);

        // then
        verify(eventInstanceService, never()).deleteEventTriggerInstance(any(STimerEventTriggerInstance.class));
    }

    @Test
    public void jobWasExecuted_should_do_nothing_when_no_tenant_id() throws Exception {
        // Given
        context.put(AbstractBonitaJobListener.TENANT_ID, null);

        // When
        timerEventTriggerJobListener.jobWasExecuted(context, null);

        // Then
        verify(eventInstanceService, never()).deleteEventTriggerInstance(any(STimerEventTriggerInstance.class));
        verify(logger, never()).log(any(Class.class), any(TechnicalLogSeverity.class), anyString(), any(Exception.class));
    }

    @Test
    public void jobWasExecuted_should_do_nothing_when_tenant_id_equals_0() throws Exception {
        // Given
        context.put(AbstractBonitaJobListener.TENANT_ID, 0L);

        // When
        timerEventTriggerJobListener.jobWasExecuted(context, null);

        // Then
        verify(eventInstanceService, never()).deleteEventTriggerInstance(any(STimerEventTriggerInstance.class));
        verify(logger, never()).log(any(Class.class), any(TechnicalLogSeverity.class), anyString(), any(Exception.class));
    }

    @Test
    public final void jobWasExecuted_should_do_nothing_when_deleteTimerEventTriggerIfJobNotScheduledAnyMore_throws_exception() throws Exception {
        // Given
        final TimerEventTriggerJobListener spiedTimerEventTriggerJobListener = spy(timerEventTriggerJobListener);
        doThrow(new SSearchException(new Exception())).when(spiedTimerEventTriggerJobListener).deleteTimerEventTrigger(TRIGGER_NAME);

        // When
        spiedTimerEventTriggerJobListener.jobWasExecuted(context, null);

        // then
        verify(eventInstanceService, never()).deleteEventTriggerInstance(any(STimerEventTriggerInstance.class));
    }

    @Test
    public final void jobWasExecuted_should_log_if_can_log_when_deleteTimerEventTriggerIfJobNotScheduledAnyMore_throws_exception() throws Exception {
        // Given
        doReturn(true).when(logger).isLoggable(any(Class.class), eq(TechnicalLogSeverity.WARNING));
        final TimerEventTriggerJobListener spiedTimerEventTriggerJobListener = spy(timerEventTriggerJobListener);
        final SSearchException e = new SSearchException(new Exception());
        doThrow(e).when(spiedTimerEventTriggerJobListener).deleteTimerEventTrigger(TRIGGER_NAME);

        // When
        spiedTimerEventTriggerJobListener.jobWasExecuted(context, null);

        // then
        verify(eventInstanceService, never()).deleteEventTriggerInstance(any(STimerEventTriggerInstance.class));
        verify(logger).log(any(Class.class), eq(TechnicalLogSeverity.WARNING),
                eq("An exception occurs during the deleting of the timer event trigger '" + TRIGGER_NAME + "'."), eq(e));
    }

    /**
     * Test method for {@link org.bonitasoft.engine.jobs.TimerEventTriggerJobListener#getName()
     * .
     */
    @Test
    public final void getName() {
        // When
        final String name = timerEventTriggerJobListener.getName();

        // then
        assertEquals("TimerEventTriggerJobListener_" + TENANT_ID, name);
    }

    /**
     * Test method for {@link org.bonitasoft.engine.jobs.TimerEventTriggerJobListener#jobExecutionVetoed(Map)
     * .
     */
    @Test
    public final void jobExecutionVetoed_should_do_nothing() {
        timerEventTriggerJobListener.jobExecutionVetoed(Collections.<String, Serializable> emptyMap());
    }

    /**
     * Test method for {@link org.bonitasoft.engine.jobs.TimerEventTriggerJobListener#jobToBeExecuted(Map)
     * .
     */
    @Test
    public final void jobToBeExecuted_should_do_nothing() {
        timerEventTriggerJobListener.jobToBeExecuted(Collections.<String, Serializable> emptyMap());
    }

}
