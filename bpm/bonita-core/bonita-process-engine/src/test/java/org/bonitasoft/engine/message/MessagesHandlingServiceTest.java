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
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.iterate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType.EVENT_SUB_PROCESS;
import static org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType.INTERMEDIATE_CATCH_EVENT;
import static org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType.START_EVENT;
import static org.bonitasoft.engine.message.MessagesHandlingService.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
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
        final List<SMessageEventCouple> messageCouples = asList( //
                msgEventCouple(1L, 10L), //
                msgEventCouple(2L, 20L), //
                msgEventCouple(1L, 30L));

        // When
        final List<SMessageEventCouple> uniqueCouples = messagesHandlingService.getMessageUniqueCouples(messageCouples);

        // Then
        assertThat(uniqueCouples).containsExactly(
                msgEventCouple(1L, 10L), //
                msgEventCouple(2L, 20L));
    }

    @Test
    public void getMessageUniqueCouplesWithDuplicateWaitingEvent() {
        // Given
        final List<SMessageEventCouple> messageCouples = asList( //
                msgEventCouple(1L, 10L), //
                msgEventCouple(2L, 10L), //
                msgEventCouple(3L, 30L));

        // When
        final List<SMessageEventCouple> uniqueCouples = messagesHandlingService.getMessageUniqueCouples(messageCouples);

        // Then
        assertThat(uniqueCouples).containsExactly(
                msgEventCouple(1L, 10L), //
                msgEventCouple(3L, 30L));
    }

    @Test
    public void couplesWithDuplicateStartWaitingEventsAreConsideredTwice() {
        // Given
        final List<SMessageEventCouple> messageCouples = asList(
                msgEventCouple(1L, 10L, START_EVENT), //
                msgEventCouple(2L, 10L, START_EVENT));

        // When
        final List<SMessageEventCouple> uniqueCouples = messagesHandlingService.getMessageUniqueCouples(messageCouples);

        // Then
        assertThat(uniqueCouples).containsExactly(
                msgEventCouple(1L, 10L, START_EVENT), //
                msgEventCouple(2L, 10L, START_EVENT));
    }

    @Test
    public void couplesWithDuplicateEventSubProcessesAreConsideredOnlyOnce() {
        // Given
        final List<SMessageEventCouple> messageCouples = asList(
                msgEventCouple(1L, 10L, EVENT_SUB_PROCESS), //
                msgEventCouple(2L, 10L, EVENT_SUB_PROCESS));

        // When
        final List<SMessageEventCouple> uniqueCouples = messagesHandlingService.getMessageUniqueCouples(messageCouples);

        // Then
        assertThat(uniqueCouples).containsExactly(msgEventCouple(1L, 10L, EVENT_SUB_PROCESS));
    }

    @Test
    public void getMessageUniqueCouplesWithDuplicateMessagesAndWaitingEvent() {
        // Given
        final List<SMessageEventCouple> messageCouples = asList( //
                msgEventCouple(1L, 10L), //
                msgEventCouple(2L, 20L), //
                msgEventCouple(2L, 10L), //
                msgEventCouple(2L, 20L));

        // When
        final List<SMessageEventCouple> uniqueCouples = messagesHandlingService.getMessageUniqueCouples(messageCouples);

        // Then
        assertThat(uniqueCouples).containsExactly(
                msgEventCouple(1L, 10L), //
                msgEventCouple(2L, 20L));
    }

    @Test
    public void executeMessageCouple_should_increment_executed_message_counter() throws Exception {
        // given:
        doReturn(new SWaitingMessageEvent()).when(eventInstanceService).getWaitingMessage(2L);
        doReturn(new SMessageInstance()).when(eventInstanceService).getMessageInstance(1L);

        // when:
        messagesHandlingService.executeMessageCouple(1L, 2L);

        // then:
        assertThat(counterValue(NUMBER_OF_MESSAGES_EXECUTED)).isEqualTo(1);
    }

    @Test
    public void should_have_tenant_id_in_all_meters() {
        assertThat(counterForDefaultTenant(NUMBER_OF_MESSAGES_EXECUTED)).isNotNull();
        assertThat(counterForDefaultTenant(NUMBER_OF_MESSAGES_POTENTIAL_MATCHED)).isNotNull();
        assertThat(counterForDefaultTenant(NUMBER_OF_MESSAGES_MATCHING_RETRIGGERED_TASKS)).isNotNull();
    }

    @Test
    public void should_increment_metrics_on_executed_and_potential_couples_when_matching_messages() throws Exception {
        doReturn(new SWaitingMessageEvent()).when(eventInstanceService).getWaitingMessage(anyLong());
        doReturn(new SMessageInstance()).when(eventInstanceService).getMessageInstance(anyLong());
        doReturn(asList(
                new SMessageEventCouple(51, INTERMEDIATE_CATCH_EVENT, 61),
                new SMessageEventCouple(52, INTERMEDIATE_CATCH_EVENT, 62),
                new SMessageEventCouple(53, INTERMEDIATE_CATCH_EVENT, 63),
                new SMessageEventCouple(53, INTERMEDIATE_CATCH_EVENT, 64)// waiting event already matched
        )).when(eventInstanceService).getMessageEventCouples(anyInt(), anyInt());

        messagesHandlingService.matchEventCoupleAndTriggerExecution();

        assertThat(counterValue(NUMBER_OF_MESSAGES_EXECUTED)).isEqualTo(3);
        assertThat(counterValue(NUMBER_OF_MESSAGES_POTENTIAL_MATCHED)).isEqualTo(4);
        assertThat(counterValue(NUMBER_OF_MESSAGES_MATCHING_RETRIGGERED_TASKS)).isEqualTo(0);
    }

    @Test
    public void should_increment_metric_on_retriggered_taskls_when_matching_more_couples_than_the_maximum()
            throws Exception {
        doReturn(new SWaitingMessageEvent()).when(eventInstanceService).getWaitingMessage(anyLong());
        doReturn(new SMessageInstance()).when(eventInstanceService).getMessageInstance(anyLong());
        List<SMessageEventCouple> couples = iterate(1, i -> i + 1).limit(100) // 100 == MAX_COUPLES
                .map(i -> msgEventCouple(i, i))
                .collect(toList());
        doReturn(couples).when(eventInstanceService).getMessageEventCouples(anyInt(), anyInt());

        messagesHandlingService.matchEventCoupleAndTriggerExecution();

        assertThat(counterValue(NUMBER_OF_MESSAGES_MATCHING_RETRIGGERED_TASKS)).isEqualTo(1);
    }

    // =================================================================================================================
    // UTILS
    // =================================================================================================================

    private Counter counterForDefaultTenant(String counterName) {
        return meterRegistry.find(counterName).tag("tenant", String.valueOf(TENANT_ID)).counter();
    }

    private double counterValue(String counterName) {
        return meterRegistry.find(counterName).counter().count();
    }

    private static SMessageEventCouple msgEventCouple(long msgId, long eventId) {
        return new SMessageEventCouple(eventId, null, msgId);
    }

    private static SMessageEventCouple msgEventCouple(long msgId, long eventId, SBPMEventType eventType) {
        return new SMessageEventCouple(eventId, eventType, msgId);
    }

}
