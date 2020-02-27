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
package org.bonitasoft.engine.message;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SEventTriggerInstanceReadException;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageEventCouple;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.execution.work.BPMWorkFactory;
import org.bonitasoft.engine.lock.LockService;
import org.bonitasoft.engine.log.technical.TechnicalLogger;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.sessionaccessor.SessionAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.bonitasoft.engine.work.WorkService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Celine Souchet
 * @author Emmanuel Duchastenier
 */
@RunWith(MockitoJUnitRunner.class)
public class MessagesHandlingServiceTest {

    public static final long TENANT_ID = 1L;
    @Mock
    private EventInstanceService eventInstanceService;
    @Mock
    private WorkService workService;
    @Mock
    private TechnicalLoggerService loggerService;
    @Mock
    private LockService lockService;
    @Mock
    private UserTransactionService userTransactionService;
    @Mock
    private SessionAccessor sessionAccessor;
    @Mock
    private BPMWorkFactory workFactory;

    private MessagesHandlingService messagesHandlingService;
    private MeterRegistry meterRegistry;

    @Before
    public void setup() throws Exception {
        when(userTransactionService.executeInTransaction(any())).thenAnswer(a -> ((Callable) a.getArgument(0)).call());
        when(loggerService.asLogger(any())).thenReturn(mock(TechnicalLogger.class));

        meterRegistry = new SimpleMeterRegistry(
                // So that micrometer updates its counters every 1 ms:
                k -> k.equals("simple.step") ? Duration.ofMillis(1).toString() : null,
                Clock.SYSTEM);
        messagesHandlingService = spy(new MessagesHandlingService(eventInstanceService, workService, loggerService,
                lockService, TENANT_ID, userTransactionService, sessionAccessor, workFactory, meterRegistry));
    }

    @Test
    public void getMessageUniqueCouplesWithDuplicateMessage() {
        // Given
        final SMessageEventCouple couple1 = mock(SMessageEventCouple.class);
        when(couple1.getMessageInstanceId()).thenReturn(1L);
        when(couple1.getWaitingMessageId()).thenReturn(10L);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        when(couple2.getMessageInstanceId()).thenReturn(2L);
        when(couple2.getWaitingMessageId()).thenReturn(20L);

        final SMessageEventCouple couple3 = mock(SMessageEventCouple.class);
        when(couple3.getMessageInstanceId()).thenReturn(1L);
        when(couple3.getWaitingMessageId()).thenReturn(30L);

        final List<SMessageEventCouple> messageCouples = new ArrayList<>(3);
        messageCouples.addAll(asList(couple1, couple2, couple3));

        // When
        final List<SMessageEventCouple> uniqueCouples = messagesHandlingService.getMessageUniqueCouples(messageCouples);

        // Then
        assertEquals(2, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstanceId());
        assertEquals(10L, first.getWaitingMessageId());
        final SMessageEventCouple second = uniqueCouples.get(1);
        assertEquals(2L, second.getMessageInstanceId());
        assertEquals(20L, second.getWaitingMessageId());
    }

    @Test
    public void getMessageUniqueCouplesWithDuplicateWaitingEvent() {
        // Given
        final SMessageEventCouple couple1 = mock(SMessageEventCouple.class);
        when(couple1.getMessageInstanceId()).thenReturn(1L);
        when(couple1.getWaitingMessageId()).thenReturn(10L);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        when(couple2.getMessageInstanceId()).thenReturn(2L);
        when(couple2.getWaitingMessageId()).thenReturn(10L);

        final SMessageEventCouple couple3 = mock(SMessageEventCouple.class);
        when(couple3.getMessageInstanceId()).thenReturn(3L);
        when(couple3.getWaitingMessageId()).thenReturn(30L);

        final List<SMessageEventCouple> messageCouples = new ArrayList<>(3);
        messageCouples.addAll(asList(couple1, couple2, couple3));

        // When
        final List<SMessageEventCouple> uniqueCouples = messagesHandlingService.getMessageUniqueCouples(messageCouples);

        // Then
        assertEquals(2, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstanceId());
        assertEquals(10L, first.getWaitingMessageId());
        final SMessageEventCouple second = uniqueCouples.get(1);
        assertEquals(3L, second.getMessageInstanceId());
        assertEquals(30L, second.getWaitingMessageId());
    }

    @Test
    public void couplesWithDuplicateStartWaitingEventsAreConsideredTwice() throws SEventTriggerInstanceReadException {
        // Given
        final SMessageEventCouple couple1 = mock(SMessageEventCouple.class);
        when(couple1.getMessageInstanceId()).thenReturn(1L);
        when(couple1.getWaitingMessageId()).thenReturn(10L);
        when(couple1.getWaitingMessageEventType()).thenReturn(SBPMEventType.START_EVENT);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        when(couple2.getMessageInstanceId()).thenReturn(2L);
        when(couple2.getWaitingMessageId()).thenReturn(10L);
        when(couple2.getWaitingMessageEventType()).thenReturn(SBPMEventType.START_EVENT);

        final List<SMessageEventCouple> messageCouples = new ArrayList<>(3);
        messageCouples.addAll(asList(couple1, couple2));

        // When
        final List<SMessageEventCouple> uniqueCouples = messagesHandlingService.getMessageUniqueCouples(messageCouples);

        // Then
        assertEquals(2, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstanceId());
        assertEquals(10L, first.getWaitingMessageId());
        final SMessageEventCouple second = uniqueCouples.get(1);
        assertEquals(2L, second.getMessageInstanceId());
        assertEquals(10L, second.getWaitingMessageId());
    }

    @Test
    public void couplesWithDuplicateEventSubProcessesAreConsideredOnlyOnce() throws SEventTriggerInstanceReadException {
        // Given
        final SMessageEventCouple couple1 = mock(SMessageEventCouple.class);
        when(couple1.getMessageInstanceId()).thenReturn(1L);
        when(couple1.getWaitingMessageId()).thenReturn(10L);
        when(couple1.getWaitingMessageEventType()).thenReturn(SBPMEventType.EVENT_SUB_PROCESS);

        final SMessageEventCouple couple2 = mock(SMessageEventCouple.class);
        when(couple2.getMessageInstanceId()).thenReturn(2L);
        when(couple2.getWaitingMessageId()).thenReturn(10L);
        when(couple2.getWaitingMessageEventType()).thenReturn(SBPMEventType.EVENT_SUB_PROCESS);

        final List<SMessageEventCouple> messageCouples = new ArrayList<>(3);
        messageCouples.addAll(asList(couple1, couple2));

        // When
        final List<SMessageEventCouple> uniqueCouples = messagesHandlingService.getMessageUniqueCouples(messageCouples);

        // Then
        assertEquals(1, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstanceId());
        assertEquals(10L, first.getWaitingMessageId());
    }

    @Test
    public void getMessageUniqueCouplesWithDuplicateMessagesAndWaitingEvent()
            throws SEventTriggerInstanceReadException {
        // Given
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

        final List<SMessageEventCouple> messageCouples = new ArrayList<>(4);
        messageCouples.addAll(asList(couple1, couple2, couple3, couple4));

        // When
        final List<SMessageEventCouple> uniqueCouples = messagesHandlingService.getMessageUniqueCouples(messageCouples);

        // Then
        assertEquals(2, uniqueCouples.size());
        final SMessageEventCouple first = uniqueCouples.get(0);
        assertEquals(1L, first.getMessageInstanceId());
        assertEquals(10L, first.getWaitingMessageId());
        final SMessageEventCouple second = uniqueCouples.get(1);
        assertEquals(2L, second.getMessageInstanceId());
        assertEquals(20L, second.getWaitingMessageId());
    }

    @Test
    public void executeMessageCouple_should_increment_executed_message_counter() throws Exception {
        // given:
        doReturn(new SWaitingMessageEvent()).when(eventInstanceService).getWaitingMessage(2L);
        doReturn(new SMessageInstance()).when(eventInstanceService).getMessageInstance(1L);

        // when:
        messagesHandlingService.executeMessageCouple(1L, 2L);

        // then:
        assertThat(meterRegistry.find(MessagesHandlingService.NUMBER_OF_MESSAGES_EXECUTED).counter().count())
                .isEqualTo(1);
    }

    @Test
    public void should_have_tenant_id_in_all_meters() {
        assertThat(meterRegistry.find(MessagesHandlingService.NUMBER_OF_MESSAGES_EXECUTED)
                .tag("tenant", String.valueOf(TENANT_ID)).counter()).isNotNull();
        assertThat(meterRegistry.find(MessagesHandlingService.NUMBER_OF_MESSAGES_POTENTIAL_MATCHED)
                .tag("tenant", String.valueOf(TENANT_ID)).counter()).isNotNull();
        assertThat(meterRegistry.find(MessagesHandlingService.NUMBER_OF_MESSAGES_MATCHING_RETRIGGERED_TASKS)
                .tag("tenant", String.valueOf(TENANT_ID)).counter()).isNotNull();
    }

    @Test
    public void should_increment_metrics_on_executed_and_potential_couples_when_matching_messages() throws Exception {
        doReturn(new SWaitingMessageEvent()).when(eventInstanceService).getWaitingMessage(anyLong());
        doReturn(new SMessageInstance()).when(eventInstanceService).getMessageInstance(anyLong());
        doReturn(asList(
                new SMessageEventCouple(51, SBPMEventType.INTERMEDIATE_CATCH_EVENT, 61),
                new SMessageEventCouple(52, SBPMEventType.INTERMEDIATE_CATCH_EVENT, 62),
                new SMessageEventCouple(53, SBPMEventType.INTERMEDIATE_CATCH_EVENT, 63),
                new SMessageEventCouple(53, SBPMEventType.INTERMEDIATE_CATCH_EVENT, 64)// waiting event already matched
        )).when(eventInstanceService).getMessageEventCouples(anyInt(), anyInt());

        messagesHandlingService.matchEventCoupleAndTriggerExecution();

        assertThat(meterRegistry.find(MessagesHandlingService.NUMBER_OF_MESSAGES_EXECUTED).counter().count())
                .isEqualTo(3);
        assertThat(meterRegistry.find(MessagesHandlingService.NUMBER_OF_MESSAGES_POTENTIAL_MATCHED).counter().count())
                .isEqualTo(4);
        assertThat(meterRegistry.find(MessagesHandlingService.NUMBER_OF_MESSAGES_MATCHING_RETRIGGERED_TASKS).counter()
                .count())
                        .isEqualTo(0);

    }

    @Test
    public void should_increment_metric_on_retriggered_taskls_when_matching_more_couples_than_the_maximum()
            throws Exception {
        doReturn(new SWaitingMessageEvent()).when(eventInstanceService).getWaitingMessage(anyLong());
        doReturn(new SMessageInstance()).when(eventInstanceService).getMessageInstance(anyLong());
        List<SMessageEventCouple> couples = Stream.iterate(1, i -> i + 1).limit(100) // 100 == MAX_COUPLES
                .map(i -> new SMessageEventCouple(i, null, i))
                .collect(Collectors.toList());
        doReturn(couples).when(eventInstanceService).getMessageEventCouples(anyInt(), anyInt());

        messagesHandlingService.matchEventCoupleAndTriggerExecution();

        assertThat(meterRegistry.find(MessagesHandlingService.NUMBER_OF_MESSAGES_MATCHING_RETRIGGERED_TASKS).counter()
                .count())
                        .isEqualTo(1);
    }

}
