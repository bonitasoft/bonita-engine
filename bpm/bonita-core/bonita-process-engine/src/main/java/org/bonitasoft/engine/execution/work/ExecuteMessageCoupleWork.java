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

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.service.ServiceAccessor;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
@Slf4j
public class ExecuteMessageCoupleWork extends TenantAwareBonitaWork {

    private final long messageInstanceId;

    private final long waitingMessageId;

    ExecuteMessageCoupleWork(final long messageInstanceId, final long waitingMessageId) {
        this.messageInstanceId = messageInstanceId;
        this.waitingMessageId = waitingMessageId;
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": messageInstanceId: " + messageInstanceId + ", waitingMessageId: "
                + waitingMessageId;
    }

    private void resetWaitingMessage(final long waitingMessageId, final EventInstanceService eventInstanceService)
            throws SWaitingEventModificationException,
            SWaitingEventReadException {
        final SWaitingMessageEvent waitingMsg = eventInstanceService.getWaitingMessage(waitingMessageId);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor
                .addField(BuilderFactory.get(SWaitingMessageEventBuilderFactory.class).getProgressKey(),
                        SWaitingMessageEventBuilderFactory.PROGRESS_FREE_KEY);
        eventInstanceService.updateWaitingMessage(waitingMsg, descriptor);
    }

    @Override
    public String getRecoveryProcedure() {
        return "call processApi.executeMessageCouple(" + messageInstanceId + ", " + waitingMessageId
                + "); to re-launch the execution of the message.";
    }

    @Override
    public CompletableFuture<Void> work(final Map<String, Object> context) throws Exception {
        final ServiceAccessor serviceAccessor = getServiceAccessor(context);
        final EventInstanceService eventInstanceService = serviceAccessor.getEventInstanceService();
        final DataInstanceService dataInstanceService = serviceAccessor.getDataInstanceService();
        final SWaitingMessageEvent waitingMessage = eventInstanceService.getWaitingMessage(waitingMessageId);
        final SMessageInstance messageInstance = eventInstanceService.getMessageInstance(messageInstanceId);
        if (waitingMessage != null) {
            serviceAccessor.getEventsHandler().triggerCatchEvent(waitingMessage, messageInstanceId);
            eventInstanceService.deleteMessageInstance(messageInstance);
            dataInstanceService.deleteLocalDataInstances(messageInstanceId,
                    DataInstanceContainer.MESSAGE_INSTANCE.name(), true);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void handleFailure(final Throwable e, final Map<String, Object> context) throws Exception {
        final ServiceAccessor serviceAccessor = getServiceAccessor(context);
        serviceAccessor.getUserTransactionService().executeInTransaction(() -> {
            resetWaitingMessage(waitingMessageId, serviceAccessor.getEventInstanceService());
            return null;
        });
        log.warn(
                String.format(
                        "Unable to execute message couple with sent message %s and waiting message %s, the waiting message was reset"
                                +
                                " to allow other message to trigger it. This failure might come from a design issue, cause is: %s",
                        messageInstanceId, waitingMessageId, getRootCause(e)));
        log.debug("Cause of the issue while executing message couple: sent message {} and waiting message {} error {}",
                messageInstanceId, waitingMessageId, e);
    }

    private String getRootCause(Throwable e) {
        String message = null;
        while (e != null) {
            message = e.getMessage();
            e = e.getCause();
        }
        return message;
    }

}
