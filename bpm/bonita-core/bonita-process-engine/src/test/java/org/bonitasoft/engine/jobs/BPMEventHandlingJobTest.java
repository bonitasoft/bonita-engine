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
        when(couple1.getMessageInstanceId()).thenReturn(1L);
        when(couple1.getWaitingMessageId()).thenReturn(10L);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        when(couple2.getMessageInstanceId()).thenReturn(2L);
        when(couple2.getWaitingMessageId()).thenReturn(20L);

        final SMessageEventCouple couple3 = mock(SMessageEventCouple.class);
        when(couple3.getMessageInstanceId()).thenReturn(1L);
        when(couple3.getWaitingMessageId()).thenReturn(30L);

        messageCouples.addAll(Arrays.asList(couple1, couple2, couple3));
        final List<SMessageEventCouple> uniqueCouples = new BPMEventHandlingJob().makeMessageUniqueCouples(messageCouples);
        assertEquals(2, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstanceId());
        assertEquals(10L, first.getWaitingMessageId());
        final SMessageEventCouple second = uniqueCouples.get(1);
        assertEquals(2L, second.getMessageInstanceId());
        assertEquals(20L, second.getWaitingMessageId());
    }

    @Test
    public void makeMessageUniqueCouplesWithDuplicateWaitingEvent() {
        List<SMessageEventCouple> messageCouples = new ArrayList<SMessageEventCouple>(3);

        final SMessageEventCouple couple1 = mock(SMessageEventCouple.class);
        when(couple1.getMessageInstanceId()).thenReturn(1L);
        when(couple1.getWaitingMessageId()).thenReturn(10L);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        when(couple2.getMessageInstanceId()).thenReturn(2L);
        when(couple2.getWaitingMessageId()).thenReturn(10L);

        final SMessageEventCouple couple3 = mock(SMessageEventCouple.class);
        when(couple3.getMessageInstanceId()).thenReturn(3L);
        when(couple3.getWaitingMessageId()).thenReturn(30L);

        messageCouples.addAll(Arrays.asList(couple1, couple2, couple3));
        final List<SMessageEventCouple> uniqueCouples = new BPMEventHandlingJob().makeMessageUniqueCouples(messageCouples);
        assertEquals(2, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstanceId());
        assertEquals(10L, first.getWaitingMessageId());
        final SMessageEventCouple second = uniqueCouples.get(1);
        assertEquals(3L, second.getMessageInstanceId());
        assertEquals(30L, second.getWaitingMessageId());
    }

    @Test
    public void couplesWithDuplicateStartWaitingEventsAreConsideredTwice() {
        List<SMessageEventCouple> messageCouples = new ArrayList<SMessageEventCouple>(3);

        final SMessageEventCouple couple1 = mock(SMessageEventCouple.class);
        when(couple1.getMessageInstanceId()).thenReturn(1L);
        when(couple1.getWaitingMessageId()).thenReturn(10L);
        when(couple1.getWaitingMessageEventType()).thenReturn(SBPMEventType.START_EVENT);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        when(couple2.getMessageInstanceId()).thenReturn(2L);
        when(couple2.getWaitingMessageId()).thenReturn(10L);
        when(couple2.getWaitingMessageEventType()).thenReturn(SBPMEventType.START_EVENT);

        messageCouples.addAll(Arrays.asList(couple1, couple2));
        final List<SMessageEventCouple> uniqueCouples = new BPMEventHandlingJob().makeMessageUniqueCouples(messageCouples);
        assertEquals(2, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstanceId());
        assertEquals(10L, first.getWaitingMessageId());
        final SMessageEventCouple second = uniqueCouples.get(1);
        assertEquals(2L, second.getMessageInstanceId());
        assertEquals(10L, second.getWaitingMessageId());
    }

    @Test
    public void couplesWithDuplicateEventSubProcessesAreConsideredOnlyOnce() {
        List<SMessageEventCouple> messageCouples = new ArrayList<SMessageEventCouple>(3);

        final SMessageEventCouple couple1 = mock(SMessageEventCouple.class);
        when(couple1.getMessageInstanceId()).thenReturn(1L);
        when(couple1.getWaitingMessageId()).thenReturn(10L);
        when(couple1.getWaitingMessageEventType()).thenReturn(SBPMEventType.EVENT_SUB_PROCESS);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        when(couple2.getMessageInstanceId()).thenReturn(2L);
        when(couple2.getWaitingMessageId()).thenReturn(10L);
        when(couple2.getWaitingMessageEventType()).thenReturn(SBPMEventType.EVENT_SUB_PROCESS);

        messageCouples.addAll(Arrays.asList(couple1, couple2));
        final List<SMessageEventCouple> uniqueCouples = new BPMEventHandlingJob().makeMessageUniqueCouples(messageCouples);
        assertEquals(1, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstanceId());
        assertEquals(10L, first.getWaitingMessageId());
    }

    @Test
    public void makeMessageUniqueCouplesWithDuplicateMessagesAndWaitingEvent() {
        List<SMessageEventCouple> messageCouples = new ArrayList<SMessageEventCouple>(4);

        final SMessageEventCouple couple1 = mock(SMessageEventCouple.class);
        when(couple1.getMessageInstanceId()).thenReturn(1L);
        when(couple1.getWaitingMessageId()).thenReturn(10L);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        when(couple2.getMessageInstanceId()).thenReturn(2L);
        when(couple2.getWaitingMessageId()).thenReturn(20L);

        final SMessageEventCouple couple3 = mock(SMessageEventCouple.class);
        when(couple3.getMessageInstanceId()).thenReturn(2L);
        when(couple3.getWaitingMessageId()).thenReturn(10L);

        final SMessageEventCouple couple4 = mock(SMessageEventCouple.class);
        when(couple4.getMessageInstanceId()).thenReturn(2L);
        when(couple4.getWaitingMessageId()).thenReturn(20L);

        messageCouples.addAll(Arrays.asList(couple1, couple2, couple3, couple4));
        final List<SMessageEventCouple> uniqueCouples = new BPMEventHandlingJob().makeMessageUniqueCouples(messageCouples);
        assertEquals(2, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstanceId());
        assertEquals(10L, first.getWaitingMessageId());
        final SMessageEventCouple second = uniqueCouples.get(1);
        assertEquals(2L, second.getMessageInstanceId());
        assertEquals(20L, second.getWaitingMessageId());
    }

}
