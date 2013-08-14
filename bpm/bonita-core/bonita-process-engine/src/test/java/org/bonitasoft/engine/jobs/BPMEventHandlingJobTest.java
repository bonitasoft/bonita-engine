/**
 * Copyright (C) 2013 BonitaSoft S.A.
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.junit.Test;

/**
 * @author Emmanuel Duchastenier
 */
@SuppressWarnings("javadoc")
public class BPMEventHandlingJobTest {

    @Test
    public void makeMessageUniqueCouplesWithDuplicateMessage() {
        List<SMessageEventCouple> messageCouples = new ArrayList<SMessageEventCouple>(3);

        final SMessageEventCouple couple1 = mock(SMessageEventCouple.class);
        final SMessageInstance mi1 = mock(SMessageInstance.class);
        when(couple1.getMessageInstance()).thenReturn(mi1);
        when(mi1.getId()).thenReturn(1L);
        final SWaitingMessageEvent wm1 = mock(SWaitingMessageEvent.class);
        when(couple1.getWaitingMessage()).thenReturn(wm1);
        when(wm1.getId()).thenReturn(10L);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        final SMessageInstance mi2 = mock(SMessageInstance.class);
        when(couple2.getMessageInstance()).thenReturn(mi2);
        when(mi2.getId()).thenReturn(2L);
        final SWaitingMessageEvent wm2 = mock(SWaitingMessageEvent.class);
        when(couple2.getWaitingMessage()).thenReturn(wm2);
        when(wm2.getId()).thenReturn(20L);

        final SMessageEventCouple couple3 = mock(SMessageEventCouple.class);
        final SMessageInstance mi3 = mock(SMessageInstance.class);
        when(couple3.getMessageInstance()).thenReturn(mi3);
        when(mi3.getId()).thenReturn(1L);
        final SWaitingMessageEvent wm3 = mock(SWaitingMessageEvent.class);
        when(couple3.getWaitingMessage()).thenReturn(wm3);
        when(wm3.getId()).thenReturn(30L);

        messageCouples.addAll(Arrays.asList(couple1, couple2, couple3));
        final List<SMessageEventCouple> uniqueCouples = new BPMEventHandlingJob().makeMessageUniqueCouples(messageCouples);
        assertEquals(2, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstance().getId());
        assertEquals(10L, first.getWaitingMessage().getId());
        final SMessageEventCouple second = uniqueCouples.get(1);
        assertEquals(2L, second.getMessageInstance().getId());
        assertEquals(20L, second.getWaitingMessage().getId());
    }

    @Test
    public void makeMessageUniqueCouplesWithDuplicateWaitingEvent() {
        List<SMessageEventCouple> messageCouples = new ArrayList<SMessageEventCouple>(3);

        final SMessageEventCouple couple1 = mock(SMessageEventCouple.class);
        final SMessageInstance mi1 = mock(SMessageInstance.class);
        when(couple1.getMessageInstance()).thenReturn(mi1);
        when(mi1.getId()).thenReturn(1L);
        final SWaitingMessageEvent wm1 = mock(SWaitingMessageEvent.class);
        when(couple1.getWaitingMessage()).thenReturn(wm1);
        when(wm1.getId()).thenReturn(10L);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        final SMessageInstance mi2 = mock(SMessageInstance.class);
        when(couple2.getMessageInstance()).thenReturn(mi2);
        when(mi2.getId()).thenReturn(2L);
        final SWaitingMessageEvent wm2 = mock(SWaitingMessageEvent.class);
        when(couple2.getWaitingMessage()).thenReturn(wm2);
        when(wm2.getId()).thenReturn(10L);

        final SMessageEventCouple couple3 = mock(SMessageEventCouple.class);
        final SMessageInstance mi3 = mock(SMessageInstance.class);
        when(couple3.getMessageInstance()).thenReturn(mi3);
        when(mi3.getId()).thenReturn(3L);
        final SWaitingMessageEvent wm3 = mock(SWaitingMessageEvent.class);
        when(couple3.getWaitingMessage()).thenReturn(wm3);
        when(wm3.getId()).thenReturn(30L);

        messageCouples.addAll(Arrays.asList(couple1, couple2, couple3));
        final List<SMessageEventCouple> uniqueCouples = new BPMEventHandlingJob().makeMessageUniqueCouples(messageCouples);
        assertEquals(2, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstance().getId());
        assertEquals(10L, first.getWaitingMessage().getId());
        final SMessageEventCouple second = uniqueCouples.get(1);
        assertEquals(3L, second.getMessageInstance().getId());
        assertEquals(30L, second.getWaitingMessage().getId());
    }

    @Test
    public void couplesWithDuplicateStartWaitingEventsAreConsideredTwice() {
        List<SMessageEventCouple> messageCouples = new ArrayList<SMessageEventCouple>(3);

        final SMessageEventCouple couple1 = mock(SMessageEventCouple.class);
        final SMessageInstance mi1 = mock(SMessageInstance.class);
        when(couple1.getMessageInstance()).thenReturn(mi1);
        when(mi1.getId()).thenReturn(1L);
        final SWaitingMessageEvent wm1 = mock(SWaitingMessageEvent.class);
        when(couple1.getWaitingMessage()).thenReturn(wm1);
        when(wm1.getEventType()).thenReturn(SBPMEventType.START_EVENT);
        when(wm1.getId()).thenReturn(10L);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        final SMessageInstance mi2 = mock(SMessageInstance.class);
        when(couple2.getMessageInstance()).thenReturn(mi2);
        when(mi2.getId()).thenReturn(2L);
        final SWaitingMessageEvent wm2 = mock(SWaitingMessageEvent.class);
        when(couple2.getWaitingMessage()).thenReturn(wm2);
        when(wm2.getId()).thenReturn(10L);
        when(wm2.getEventType()).thenReturn(SBPMEventType.START_EVENT);

        messageCouples.addAll(Arrays.asList(couple1, couple2));
        final List<SMessageEventCouple> uniqueCouples = new BPMEventHandlingJob().makeMessageUniqueCouples(messageCouples);
        assertEquals(2, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstance().getId());
        assertEquals(10L, first.getWaitingMessage().getId());
        final SMessageEventCouple second = uniqueCouples.get(1);
        assertEquals(2L, second.getMessageInstance().getId());
        assertEquals(10L, second.getWaitingMessage().getId());
    }

    @Test
    public void couplesWithDuplicateEventSubProcessesAreConsideredOnlyOnce() {
        List<SMessageEventCouple> messageCouples = new ArrayList<SMessageEventCouple>(3);

        final SMessageEventCouple couple1 = mock(SMessageEventCouple.class);
        final SMessageInstance mi1 = mock(SMessageInstance.class);
        when(couple1.getMessageInstance()).thenReturn(mi1);
        when(mi1.getId()).thenReturn(1L);
        final SWaitingMessageEvent wm1 = mock(SWaitingMessageEvent.class);
        when(couple1.getWaitingMessage()).thenReturn(wm1);
        when(wm1.getEventType()).thenReturn(SBPMEventType.EVENT_SUB_PROCESS);
        when(wm1.getId()).thenReturn(10L);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        final SMessageInstance mi2 = mock(SMessageInstance.class);
        when(couple2.getMessageInstance()).thenReturn(mi2);
        when(mi2.getId()).thenReturn(2L);
        final SWaitingMessageEvent wm2 = mock(SWaitingMessageEvent.class);
        when(couple2.getWaitingMessage()).thenReturn(wm2);
        when(wm2.getId()).thenReturn(10L);
        when(wm2.getEventType()).thenReturn(SBPMEventType.EVENT_SUB_PROCESS);

        messageCouples.addAll(Arrays.asList(couple1, couple2));
        final List<SMessageEventCouple> uniqueCouples = new BPMEventHandlingJob().makeMessageUniqueCouples(messageCouples);
        assertEquals(1, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstance().getId());
        assertEquals(10L, first.getWaitingMessage().getId());
    }

    @Test
    public void makeMessageUniqueCouplesWithDuplicateMessagesAndWaitingEvent() {
        List<SMessageEventCouple> messageCouples = new ArrayList<SMessageEventCouple>(4);

        final SMessageEventCouple couple1 = mock(SMessageEventCouple.class);
        final SMessageInstance mi1 = mock(SMessageInstance.class);
        when(couple1.getMessageInstance()).thenReturn(mi1);
        when(mi1.getId()).thenReturn(1L);
        final SWaitingMessageEvent wm1 = mock(SWaitingMessageEvent.class);
        when(couple1.getWaitingMessage()).thenReturn(wm1);
        when(wm1.getId()).thenReturn(10L);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        final SMessageInstance mi2 = mock(SMessageInstance.class);
        when(couple2.getMessageInstance()).thenReturn(mi2);
        when(mi2.getId()).thenReturn(2L);
        final SWaitingMessageEvent wm2 = mock(SWaitingMessageEvent.class);
        when(couple2.getWaitingMessage()).thenReturn(wm2);
        when(wm2.getId()).thenReturn(20L);

        final SMessageEventCouple couple3 = mock(SMessageEventCouple.class);
        final SMessageInstance mi3 = mock(SMessageInstance.class);
        when(couple3.getMessageInstance()).thenReturn(mi3);
        when(mi3.getId()).thenReturn(2L);
        final SWaitingMessageEvent wm3 = mock(SWaitingMessageEvent.class);
        when(couple3.getWaitingMessage()).thenReturn(wm3);
        when(wm3.getId()).thenReturn(10L);

        final SMessageEventCouple couple4 = mock(SMessageEventCouple.class);
        final SMessageInstance mi4 = mock(SMessageInstance.class);
        when(couple4.getMessageInstance()).thenReturn(mi4);
        when(mi4.getId()).thenReturn(2L);
        final SWaitingMessageEvent wm4 = mock(SWaitingMessageEvent.class);
        when(couple4.getWaitingMessage()).thenReturn(wm4);
        when(wm4.getId()).thenReturn(20L);

        messageCouples.addAll(Arrays.asList(couple1, couple2, couple3, couple4));
        final List<SMessageEventCouple> uniqueCouples = new BPMEventHandlingJob().makeMessageUniqueCouples(messageCouples);
        assertEquals(2, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstance().getId());
        assertEquals(10L, first.getWaitingMessage().getId());
        final SMessageEventCouple second = uniqueCouples.get(1);
        assertEquals(2L, second.getMessageInstance().getId());
        assertEquals(20L, second.getWaitingMessage().getId());
    }

}
