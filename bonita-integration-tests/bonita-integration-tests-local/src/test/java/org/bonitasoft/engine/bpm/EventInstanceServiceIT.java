package org.bonitasoft.engine.bpm;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.transaction.TransactionService;
import org.junit.Before;
import org.junit.Test;

public class EventInstanceServiceIT extends CommonBPMServicesTest {

    private EventInstanceService eventInstanceService;
    private TransactionService transactionService;

    @Before
    public void before() {
        eventInstanceService = getTenantAccessor().getEventInstanceService();
        transactionService = getTransactionService();
    }

    @Test
    public void should_create_a_message_with_a_creation_date() throws Exception {
        SMessageInstance sMessageInstance = new SMessageInstance("myMessage", "targetProcess", "targetFlowNode", 12345L, "fnName");

        SMessageInstance createdMessage = transactionService.executeInTransaction(() -> {
            eventInstanceService.createMessageInstance(sMessageInstance);
            return eventInstanceService.getMessageInstance(sMessageInstance.getId());
        });

        assertThat(createdMessage.getId()).isGreaterThan(0);
        assertThat(createdMessage.getCreationDate()).isGreaterThan(0);
    }
}
