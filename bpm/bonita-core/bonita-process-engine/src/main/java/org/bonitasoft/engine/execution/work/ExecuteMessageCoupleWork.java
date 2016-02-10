/**
 * Copyright (C) 2015 BonitaSoft S.A.
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

import java.util.Map;

import org.bonitasoft.engine.builder.BuilderFactory;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SMessageInstanceBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.builder.event.handling.SWaitingMessageEventBuilderFactory;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.data.instance.api.DataInstanceContainer;
import org.bonitasoft.engine.data.instance.api.DataInstanceService;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Emmanuel Duchastenier
 * @author Matthieu Chaffotte
 */
public class ExecuteMessageCoupleWork extends TenantAwareBonitaWork {

    private static final long serialVersionUID = 2171765554098439091L;

    private final long messageInstanceId;

    private final long waitingMessageId;

    ExecuteMessageCoupleWork(final long messageInstanceId, final long waitingMessageId) {
        this.messageInstanceId = messageInstanceId;
        this.waitingMessageId = waitingMessageId;
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName() + ": messageInstanceId: " + messageInstanceId + ", waitingMessageId: " + waitingMessageId;
    }

    private void resetWaitingMessage(final long waitingMessageId, final EventInstanceService eventInstanceService) throws SWaitingEventModificationException,
            SWaitingEventReadException {
        final SWaitingMessageEvent waitingMsg = eventInstanceService.getWaitingMessage(waitingMessageId);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor
                .addField(BuilderFactory.get(SWaitingMessageEventBuilderFactory.class).getProgressKey(), SWaitingMessageEventBuilderFactory.PROGRESS_FREE_KEY);
        eventInstanceService.updateWaitingMessage(waitingMsg, descriptor);
    }

    private void resetMessageInstance(final long messageInstanceId, final EventInstanceService eventInstanceService) throws SMessageModificationException,
            SMessageInstanceReadException {
        final SMessageInstance messageInstance = eventInstanceService.getMessageInstance(messageInstanceId);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(BuilderFactory.get(SMessageInstanceBuilderFactory.class).getHandledKey(), false);
        eventInstanceService.updateMessageInstance(messageInstance, descriptor);
    }

    @Override
    public String getRecoveryProcedure() {
        return "Change the 'progress' field of the waiting message having id " + waitingMessageId + " to "
                + SWaitingMessageEventBuilderFactory.PROGRESS_FREE_KEY + " and " + "the 'handled' field of the message instance  having id "
                + messageInstanceId + " to false";
    }

    @Override
    public void work(final Map<String, Object> context) throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        final EventInstanceService eventInstanceService = tenantAccessor.getEventInstanceService();
        final DataInstanceService dataInstanceService = tenantAccessor.getDataInstanceService();
        final SWaitingMessageEvent waitingMessage = eventInstanceService.getWaitingMessage(waitingMessageId);
        final SMessageInstance messageInstance = eventInstanceService.getMessageInstance(messageInstanceId);
        if (waitingMessage != null) {
            tenantAccessor.getEventsHandler().triggerCatchEvent(waitingMessage, messageInstanceId);
            eventInstanceService.deleteMessageInstance(messageInstance);
            dataInstanceService.deleteLocalDataInstances(messageInstanceId, DataInstanceContainer.MESSAGE_INSTANCE.name(), true);
        }
    }

    @Override
    public void handleFailure(final Exception e, final Map<String, Object> context) throws Exception {
        final TenantServiceAccessor tenantAccessor = getTenantAccessor(context);
        resetWaitingMessage(waitingMessageId, tenantAccessor.getEventInstanceService());
        resetMessageInstance(messageInstanceId, tenantAccessor.getEventInstanceService());
    }

}
