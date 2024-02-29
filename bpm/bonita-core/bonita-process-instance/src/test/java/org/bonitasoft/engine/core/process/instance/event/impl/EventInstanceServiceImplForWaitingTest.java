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
package org.bonitasoft.engine.core.process.instance.event.impl;

import static org.mockito.Mockito.*;

import java.util.Collections;

import io.micrometer.core.instrument.MeterRegistry;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceRepository;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.model.event.SIntermediateCatchEventInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingSignalEvent;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventInstanceServiceImplForWaitingTest {

    @Mock
    private EventInstanceRepository instanceRepository;

    @Mock
    private MeterRegistry meterRegistry;

    @Mock
    private DataInstanceService dataInstanceService;

    private EventInstanceServiceImpl eventInstanceServiceImpl;

    @Before
    public void setUp() {
        eventInstanceServiceImpl = spy(
                new EventInstanceServiceImpl(instanceRepository, dataInstanceService, meterRegistry, 1L));
    }

    @Test
    public final void deleteWaitingEvents_should_delete_waiting_events() throws Exception {
        // Given
        final SIntermediateCatchEventInstance sIntermediateCatchEventInstance = new SIntermediateCatchEventInstance();
        final SWaitingMessageEvent waitingMessageEventImpl = new SWaitingMessageEvent();
        doReturn(Collections.singletonList(waitingMessageEventImpl)).doReturn(Collections.emptyList())
                .when(instanceRepository)
                .getWaitingEventsForFlowNodeId(anyLong());
        doNothing().when(eventInstanceServiceImpl).deleteWaitingEvent(waitingMessageEventImpl);

        // When
        eventInstanceServiceImpl.deleteWaitingEvents(sIntermediateCatchEventInstance);

        // Then
        verify(eventInstanceServiceImpl).deleteWaitingEvent(waitingMessageEventImpl);
    }

    @Test(expected = SWaitingEventModificationException.class)
    public final void deleteWaitingEvents_should_throw_exception_when_cant_delete_waiting_event() throws Exception {
        // Given
        final SIntermediateCatchEventInstance sIntermediateCatchEventInstance = new SIntermediateCatchEventInstance();
        final SWaitingSignalEvent waitingMessageEventImpl = new SWaitingSignalEvent();
        doReturn(Collections.singletonList(waitingMessageEventImpl)).doReturn(Collections.emptyList())
                .when(instanceRepository)
                .getWaitingEventsForFlowNodeId(anyLong());
        doThrow(new SWaitingEventModificationException(new Exception(""))).when(instanceRepository)
                .deleteWaitingEvent(waitingMessageEventImpl);
        // When
        eventInstanceServiceImpl.deleteWaitingEvents(sIntermediateCatchEventInstance);
    }

    @Test(expected = SEventTriggerInstanceReadException.class)
    public final void deleteWaitingEvents_should_throw_exception_when_cant_search_waiting_event() throws Exception {
        // Given
        final SIntermediateCatchEventInstance sIntermediateCatchEventInstance = new SIntermediateCatchEventInstance();
        doThrow(new SEventTriggerInstanceReadException(new Exception(""))).when(instanceRepository)
                .getWaitingEventsForFlowNodeId(anyLong());
        // When
        eventInstanceServiceImpl.deleteWaitingEvents(sIntermediateCatchEventInstance);
    }

}
