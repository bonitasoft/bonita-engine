/**
 * Copyright (C) 2012 BonitaSoft S.A.
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
package org.bonitasoft.engine.api.impl.transaction.event;

import java.util.Iterator;
import java.util.List;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.commons.transaction.TransactionContent;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageInstanceReadException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SMessageModificationException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventNotFoundException;
import org.bonitasoft.engine.core.process.instance.api.exceptions.event.trigger.SWaitingEventReadException;
import org.bonitasoft.engine.core.process.instance.model.builder.BPMInstanceBuilders;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SMessageInstance;
import org.bonitasoft.engine.core.process.instance.model.event.handling.SWaitingMessageEvent;
import org.bonitasoft.engine.execution.event.EventsHandler;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;

/**
 * @author Elias Ricken de Medeiros
 */
public class HandleMessageEventCouple implements TransactionContent {

    private final SMessageInstance messageInstance;

    private final List<SWaitingMessageEvent> waitingMessages;

    private final EventInstanceService eventInstanceService;

    private final BPMInstanceBuilders instanceBuilders;

    private final EventsHandler enventsHandler;

    private final TechnicalLoggerService logger;

    public HandleMessageEventCouple(final SMessageInstance messageInstance, final List<SWaitingMessageEvent> waitingMessages,
            final EventInstanceService eventInstanceService, final BPMInstanceBuilders instanceBuilders, final EventsHandler enventsHandler,
            final TechnicalLoggerService logger) {
        this.messageInstance = messageInstance;
        this.waitingMessages = waitingMessages;
        this.eventInstanceService = eventInstanceService;
        this.instanceBuilders = instanceBuilders;
        this.enventsHandler = enventsHandler;
        this.logger = logger;
    }

    @Override
    public void execute() throws SBonitaException {
        final SWaitingMessageEvent waitingMessage = findActiveWaitingMessage();
        if (waitingMessage != null) {
            enventsHandler.triggerCatchEvent(waitingMessage, messageInstance.getId());
            markMessageAsHandled();
        }
    }

    private SWaitingMessageEvent findActiveWaitingMessage() throws SWaitingEventNotFoundException, SWaitingEventReadException {
        final Iterator<SWaitingMessageEvent> iterator = waitingMessages.iterator();
        boolean found = false;
        SWaitingMessageEvent activeWaitingMessage = null;
        while (iterator.hasNext() && !found) {
            final SWaitingMessageEvent waitingMessageEvent = iterator.next();
            SWaitingMessageEvent updatedWaitingMessageEvent = null;
            try {
                updatedWaitingMessageEvent = eventInstanceService.getWaitingMessage(waitingMessageEvent.getId());
            } catch (final SWaitingEventNotFoundException e) {
                if (logger.isLoggable(HandleMessageEventCouple.class, TechnicalLogSeverity.DEBUG)) {
                    logger.log(HandleMessageEventCouple.class, TechnicalLogSeverity.DEBUG, "Waiting message already consumed", e);
                }
            }
            if (updatedWaitingMessageEvent != null && updatedWaitingMessageEvent.isActive()) {
                activeWaitingMessage = updatedWaitingMessageEvent;
                found = true;
            }
        }
        return activeWaitingMessage;
    }

    private void markMessageAsHandled() throws SMessageModificationException, SMessageInstanceNotFoundException, SMessageInstanceReadException {
        // the message instance received as parameter is not connected
        final SMessageInstance messageInstanceToUpdate = eventInstanceService.getMessageInstance(messageInstance.getId());
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(instanceBuilders.getSMessageInstanceBuilder().getHandledKey(), true);
        eventInstanceService.updateMessageInstance(messageInstanceToUpdate, descriptor);
    }

}
