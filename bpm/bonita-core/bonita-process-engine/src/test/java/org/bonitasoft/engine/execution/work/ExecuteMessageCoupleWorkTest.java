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
package org.bonitasoft.engine.execution.work;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class ExecuteMessageCoupleWorkTest {

    private static final long MESSAGE_INSTANCE_ID = 543L;
    private static final long WAITING_MESSAGE_ID = 2342L;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private EventsHandler eventsHandler;
    @Mock
    private ServiceAccessor serviceAccessor;
    @Mock
    private EventInstanceService eventInstanceService;
    @Mock
    private DataInstanceService dataInstanceService;
    @Mock
    private UserTransactionService userTransactionService;
    @Captor
    private ArgumentCaptor<Callable<?>> callableArgumentCaptor;
    private final Map<String, Object> context = new HashMap<>();

    private final ExecuteMessageCoupleWork executeMessageCoupleWork = new ExecuteMessageCoupleWork(MESSAGE_INSTANCE_ID,
            WAITING_MESSAGE_ID);
    private final SWaitingMessageEvent waitingMessageEvent = new SWaitingMessageEvent(SBPMEventType.BOUNDARY_EVENT,
            4243252L,
            "Process", 5435312, "flownode", "message");
    private final SMessageInstance messageInstance = new SMessageInstance("message", "Process", "flowNode", 4243252L,
            "flownode");

    @Before
    public void before() {
        waitingMessageEvent.setId(WAITING_MESSAGE_ID);
        messageInstance.setId(MESSAGE_INSTANCE_ID);
        context.put(TenantAwareBonitaWork.SERVICE_ACCESSOR, serviceAccessor);
        doReturn(eventsHandler).when(serviceAccessor).getEventsHandler();
        doReturn(eventInstanceService).when(serviceAccessor).getEventInstanceService();
        doReturn(dataInstanceService).when(serviceAccessor).getDataInstanceService();
        doReturn(userTransactionService).when(serviceAccessor).getUserTransactionService();
    }

    @Test
    public void should_execute_message_couple() throws Exception {
        doReturn(waitingMessageEvent).when(eventInstanceService).getWaitingMessage(WAITING_MESSAGE_ID);

        executeMessageCoupleWork.work(context);

        verify(eventsHandler).triggerCatchEvent(waitingMessageEvent, MESSAGE_INSTANCE_ID);
    }

    @Test
    public void should_delete_message_data_when_couple_is_handled() throws Exception {
        doReturn(waitingMessageEvent).when(eventInstanceService).getWaitingMessage(WAITING_MESSAGE_ID);

        executeMessageCoupleWork.work(context);

        verify(dataInstanceService).deleteLocalDataInstances(MESSAGE_INSTANCE_ID,
                DataInstanceContainer.MESSAGE_INSTANCE.name(), true);
    }

    @Test
    public void should_delete_message_instance_when_couple_is_handled() throws Exception {
        doReturn(waitingMessageEvent).when(eventInstanceService).getWaitingMessage(WAITING_MESSAGE_ID);
        doReturn(messageInstance).when(eventInstanceService).getMessageInstance(MESSAGE_INSTANCE_ID);

        executeMessageCoupleWork.work(context);

        verify(eventInstanceService).deleteMessageInstance(messageInstance);
    }

    @Test
    public void should_reset_message_instance_in_transaction_on_failure() throws Exception {
        doReturn(messageInstance).when(eventInstanceService).getMessageInstance(MESSAGE_INSTANCE_ID);
        doReturn(null).when(userTransactionService).executeInTransaction(callableArgumentCaptor.capture());

        executeMessageCoupleWork.handleFailure(new Exception("something happened during the coupling"), context);

        callableArgumentCaptor.getValue().call();
        verify(eventInstanceService, never()).updateMessageInstance(any(), any());
    }

    @Test
    public void should_not_reset_waiting_message_instance_on_failure() throws Exception {
        doReturn(messageInstance).when(eventInstanceService).getMessageInstance(MESSAGE_INSTANCE_ID);
        doReturn(null).when(userTransactionService).executeInTransaction(callableArgumentCaptor.capture());

        executeMessageCoupleWork.handleFailure(new Exception("something happened during the coupling"), context);

        callableArgumentCaptor.getValue().call();
        verify(eventInstanceService, never()).updateWaitingMessage(eq(waitingMessageEvent),
                argThat(arg -> arg.getFields().size() == 1 &&
                        arg.getFields().get("progress").equals(0)));
    }

}
