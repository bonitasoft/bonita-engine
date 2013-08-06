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

import java.io.Serializable;

import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceSingleton;
import org.bonitasoft.engine.work.TxBonitaWork;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class ExecuteMessageCoupleWork extends TxBonitaWork implements Serializable {

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

            // markMessageAsHandled
            final SMessageInstance messageInstanceToUpdate = eventInstanceService.getMessageInstance(messageInstanceId);
            final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
            descriptor.addField(tenantAccessor.getBPMInstanceBuilders().getSMessageInstanceBuilder().getHandledKey(), true);
            eventInstanceService.updateMessageInstance(messageInstanceToUpdate, descriptor);
        }
    }

    @Override
    protected String getDescription() {
        return getClass().getSimpleName() + ": messageInstanceId: " + messageInstanceId + ", waitingMessageId: " + waitingMessageId;
    }

    protected TenantServiceAccessor getTenantAccessor() {
        try {
            return TenantServiceSingleton.getInstance(getTenantId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

}
