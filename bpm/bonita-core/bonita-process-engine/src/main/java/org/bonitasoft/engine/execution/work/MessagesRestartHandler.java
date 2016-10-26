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

import java.util.concurrent.Callable;

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Resets all "In Progress" BPMN Message couples so that they can be triggered again on next cron.
 * Restart work {@link ExecuteMessageCoupleWork}
 *
 * @author Emmanuel Duchastenier
 */
public class MessagesRestartHandler implements TenantRestartHandler {

    @Override
    public void beforeServicesStart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor)
            throws RestartException {
        final EventInstanceService eventInstanceService = tenantServiceAccessor.getEventInstanceService();
        final TechnicalLoggerService technicalLoggerService = tenantServiceAccessor.getTechnicalLoggerService();

        try {
            // Reset of all SMessageInstance:
            logInfo(technicalLoggerService, "Reinitializing message instances in non-stable state to make them reworked by MessagesHandlingService");
            final int nbMessagesReset = eventInstanceService.resetProgressMessageInstances();
            logInfo(technicalLoggerService, nbMessagesReset + " message instances found and reset.");

            // Reset of all SWaitingMessageEvent:
            logInfo(technicalLoggerService, "Reinitializing waiting message events in non-stable state to make them reworked by MessagesHandlingService");
            final int nbWaitingEventsReset = eventInstanceService.resetInProgressWaitingEvents();
            logInfo(technicalLoggerService, nbWaitingEventsReset + " waiting message events found and reset.");

        } catch (final SBonitaException e) {
            handleException("Unable to reset MessageInstances / WaitingMessageEvents that were 'In Progress' when the node stopped", e);
        }
    }

    void logInfo(final TechnicalLoggerService technicalLoggerService, final String msg) {
        technicalLoggerService.log(MessagesRestartHandler.class, TechnicalLogSeverity.INFO, msg);
    }

    private void handleException(final String message, final Exception e) throws RestartException {
        throw new RestartException(message, e);
    }

    @Override
    public void afterServicesStart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor) {
        try {
            tenantServiceAccessor.getUserTransactionService().executeInTransaction(new Callable<Object>() {

                @Override
                public Object call() throws Exception {
                    tenantServiceAccessor.getMessagesHandlingService().triggerMatchingOfMessages();
                    return null;
                }
            });
        } catch (Exception e) {
            tenantServiceAccessor.getTechnicalLoggerService().log(MessagesRestartHandler.class, TechnicalLogSeverity.ERROR,
                    "Unable to register work to handle message events on startup, work will be triggered on next message event update", e);
        }
    }
}
