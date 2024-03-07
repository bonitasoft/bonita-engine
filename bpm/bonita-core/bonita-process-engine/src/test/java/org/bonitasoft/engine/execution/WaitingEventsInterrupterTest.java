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
package org.bonitasoft.engine.execution;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.Optional;

import org.bonitasoft.engine.core.process.definition.model.event.impl.SIntermediateCatchEventDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.STimerType;
import org.bonitasoft.engine.core.process.definition.model.event.trigger.impl.STimerEventTriggerDefinitionImpl;
import org.bonitasoft.engine.core.process.definition.model.impl.SProcessDefinitionImpl;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.trigger.STimerEventTriggerInstance;
import org.bonitasoft.engine.scheduler.SchedulerService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class WaitingEventsInterrupterTest {

    public static final long CATCH_EVENT_INSTANCE_ID = 432987L;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private EventInstanceService eventInstanceService;
    @Mock
    private SchedulerService schedulerService;

    private WaitingEventsInterrupter waitingEventsInterrupter;
    private SProcessDefinitionImpl processDefinition;
    private SIntermediateCatchEventInstance catchEventInstance;
    private SIntermediateCatchEventDefinitionImpl catchEventDefinition;

    @Before
    public void before() {
        waitingEventsInterrupter = new WaitingEventsInterrupter(eventInstanceService, schedulerService);

        processDefinition = new SProcessDefinitionImpl("myPocess", "1.0");
        catchEventInstance = new SIntermediateCatchEventInstance();
        catchEventInstance.setId(CATCH_EVENT_INSTANCE_ID);
        catchEventDefinition = new SIntermediateCatchEventDefinitionImpl(1234L, "myCatchTimer");
    }

    @Test
    public void should_delete_timer_job_associated_with_the_catch_event() throws Exception {
        catchEventDefinition.addTimerEventTrigger(new STimerEventTriggerDefinitionImpl(STimerType.DURATION, null));

        waitingEventsInterrupter.interruptWaitingEvents(processDefinition, catchEventInstance, catchEventDefinition);

        verify(schedulerService).delete("Timer_Ev_" + CATCH_EVENT_INSTANCE_ID);
    }

    @Test
    public void should_delete_event_trigger_instance_associated_with_the_catch_timer() throws Exception {
        catchEventDefinition.addTimerEventTrigger(new STimerEventTriggerDefinitionImpl(STimerType.DURATION, null));
        STimerEventTriggerInstance eventTriggerInstance = new STimerEventTriggerInstance(123, "someEvent", 123, "job");
        doReturn(Optional.of(eventTriggerInstance)).when(eventInstanceService)
                .getTimerEventTriggerInstanceOfFlowNode(CATCH_EVENT_INSTANCE_ID);

        waitingEventsInterrupter.interruptWaitingEvents(processDefinition, catchEventInstance, catchEventDefinition);

        verify(eventInstanceService).deleteEventTriggerInstanceOfFlowNode(CATCH_EVENT_INSTANCE_ID);
    }

    @Test
    public void should_not_delete_timer_and_event_trigger_when_catch_is_not_a_timer() throws Exception {

        waitingEventsInterrupter.interruptWaitingEvents(processDefinition, catchEventInstance, catchEventDefinition);

        verifyZeroInteractions(eventInstanceService);
        verifyZeroInteractions(schedulerService);
    }

}
