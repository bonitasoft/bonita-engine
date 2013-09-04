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
package org.bonitasoft.engine.execution.work;

import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilder;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class ExecuteMessageCoupleWork extends TxBonitaWork {

    private static final long serialVersionUID = 2171765554098439091L;

    private final long messageInstanceId;

    private final long waitingMessageId;

    public ExecuteMessageCoupleWork(final long messageInstanceId, final long waitingMessageId) {
        this.messageInstanceId = messageInstanceId;
        this.waitingMessageId = waitingMessageId;
    }

    @Override
    protected void work() throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor();
        final EventInstanceService eventInstanceService = tenantAccessor.getEventInstanceService();
        final SWaitingMessageEvent waitingMessage = eventInstanceService.getWaitingMessage(waitingMessageId);
        if (waitingMessage != null) {
            tenantAccessor.getEventsHandler().triggerCatchEvent(waitingMessage, messageInstanceId);
        }
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": messageInstanceId: " + messageInstanceId + ", waitingMessageId: " + waitingMessageId;
    }

    @Override
    protected TenantServiceAccessor getTenantAccessor() {
        try {
            return TenantServiceSingleton.getInstance(getTenantId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void handleFailure(Exception e) throws Exception {
        TenantServiceAccessor tenantAccessor = getTenantAccessor();
        resetWaitingMessage(waitingMessageId, tenantAccessor.getEventInstanceService(), tenantAccessor.getBPMInstanceBuilders());
        resetMessageInstance(messageInstanceId, tenantAccessor.getEventInstanceService(), tenantAccessor.getBPMInstanceBuilders());
    }

    private void resetWaitingMessage(final long waitingMessageId, final EventInstanceService eventInstanceService,
            final BPMInstanceBuilders instanceBuilders) throws SWaitingEventModificationException, SWaitingEventNotFoundException, SWaitingEventReadException {
        final SWaitingMessageEvent waitingMsg = eventInstanceService.getWaitingMessage(waitingMessageId);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(instanceBuilders.getSWaitingMessageEventBuilder().getProgressKey(), SWaitingMessageEventBuilder.PROGRESS_FREE_KEY);
        eventInstanceService.updateWaitingMessage(waitingMsg, descriptor);
    }

    private void resetMessageInstance(final long messageInstanceId, final EventInstanceService eventInstanceService,
            final BPMInstanceBuilders instanceBuilders) throws SMessageModificationException, SMessageInstanceNotFoundException, SMessageInstanceReadException {
        final SMessageInstance messageInstance = eventInstanceService.getMessageInstance(messageInstanceId);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(instanceBuilders.getSMessageInstanceBuilder().getHandledKey(), false);
        eventInstanceService.updateMessageInstance(messageInstance, descriptor);
    }

    @Override
    protected String getRecoveryProcedure() {
        return "Change the 'progress' field of the waiting message having id " + waitingMessageId + " to " + SWaitingMessageEventBuilder.PROGRESS_FREE_KEY
                + " and "
                + "the 'handled' field of the message instance  having id " + messageInstanceId + " to false";
    }

}
