package org.bonitasoft.engine.execution.work;


import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SBPMEventType;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SMessageInstanceImpl;
import org.bonitasoft.engine.core.process.instance.model.event.handling.impl.SWaitingMessageEventImpl;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class ExecuteMessageCoupleWorkTest {

    private static final long MESSAGE_INSTANCE_ID = 543L;
    private static final long WAITING_MESSAGE_ID = 2342L;
    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private EventsHandler eventsHandler;
    @Mock
    private TenantServiceAccessor tenantServiceAccessor;
    @Mock
    private EventInstanceService eventInstanceService;
    @Mock
    private DataInstanceService dataInstanceService;
    @Mock
    private UserTransactionService userTransactionService;
    @Mock
    private TechnicalLoggerService technicalLoggerService;
    @Captor
    private ArgumentCaptor<Callable<?>> callableArgumentCaptor;
    private Map<String, Object> context = new HashMap<>();

    private ExecuteMessageCoupleWork executeMessageCoupleWork = new ExecuteMessageCoupleWork(MESSAGE_INSTANCE_ID, WAITING_MESSAGE_ID);
    private SWaitingMessageEventImpl waitingMessageEvent = new SWaitingMessageEventImpl(SBPMEventType.BOUNDARY_EVENT, 4243252L, "Process", 5435312, "flownode", "message");
    private SMessageInstance messageInstance = new SMessageInstanceImpl("message", "Process", "flowNode", 4243252L, "flownode");

    @Before
    public void before() {
        waitingMessageEvent.setId(WAITING_MESSAGE_ID);
        messageInstance.setId(MESSAGE_INSTANCE_ID);
        context.put(TenantAwareBonitaWork.TENANT_ACCESSOR, tenantServiceAccessor);
        doReturn(eventsHandler).when(tenantServiceAccessor).getEventsHandler();
        doReturn(eventInstanceService).when(tenantServiceAccessor).getEventInstanceService();
        doReturn(dataInstanceService).when(tenantServiceAccessor).getDataInstanceService();
        doReturn(userTransactionService).when(tenantServiceAccessor).getUserTransactionService();
        doReturn(technicalLoggerService).when(tenantServiceAccessor).getTechnicalLoggerService();
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

        verify(dataInstanceService).deleteLocalDataInstances(MESSAGE_INSTANCE_ID, DataInstanceContainer.MESSAGE_INSTANCE.name(), true);
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
        verify(eventInstanceService, never()).updateWaitingMessage(eq(waitingMessageEvent), argThat(arg ->
                arg.getFields().size() == 1 &&
                        arg.getFields().get("progress").equals(0)));
    }

}