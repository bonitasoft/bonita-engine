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
package org.bonitasoft.engine.tenant.restart;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceRepository;
import org.bonitasoft.engine.execution.work.ExecuteMessageCoupleWork;
import org.bonitasoft.engine.execution.work.RestartException;
import org.bonitasoft.engine.message.MessagesHandlingService;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.springframework.stereotype.Component;

/**
 * Resets all "In Progress" BPMN Message couples so that they can be triggered again on next cron.
 * Restart work {@link ExecuteMessageCoupleWork}
 *
 * @author Emmanuel Duchastenier
 */
@Slf4j
@Component
public class MessagesRestartHandler implements TenantRestartHandler {

    private EventInstanceRepository eventInstanceRepository;
    private UserTransactionService userTransactionService;
    private MessagesHandlingService messagesHandlingService;

    public MessagesRestartHandler(
            EventInstanceRepository eventInstanceRepository, UserTransactionService userTransactionService,
            MessagesHandlingService messagesHandlingService) {
        this.eventInstanceRepository = eventInstanceRepository;
        this.userTransactionService = userTransactionService;
        this.messagesHandlingService = messagesHandlingService;
    }

    @Override
    public void beforeServicesStart()
            throws RestartException {

        try {
            // Reset of all SMessageInstance:
            log.info(
                    "Reinitializing message instances in non-stable state to make them reworked by MessagesHandlingService");
            final int nbMessagesReset = eventInstanceRepository.resetProgressMessageInstances();
            log.info(nbMessagesReset + " message instances found and reset.");

            // Reset of all SWaitingMessageEvent:
            log.info(
                    "Reinitializing waiting message events in non-stable state to make them reworked by MessagesHandlingService");
            final int nbWaitingEventsReset = eventInstanceRepository.resetInProgressWaitingEvents();
            log.info(nbWaitingEventsReset + " waiting message events found and reset.");

        } catch (final SBonitaException e) {
            throw new RestartException(
                    "Unable to reset MessageInstances / WaitingMessageEvents that were 'In Progress' when the node stopped",
                    e);
        }
    }

    @Override
    public void afterServicesStart() {
        try {
            userTransactionService.executeInTransaction(() -> {
                messagesHandlingService.triggerMatchingOfMessages();
                return null;
            });
        } catch (Exception e) {
            log.error(
                    "Unable to register work to handle message events on startup, work will be triggered on next message event update",
                    e);
        }
    }
}
