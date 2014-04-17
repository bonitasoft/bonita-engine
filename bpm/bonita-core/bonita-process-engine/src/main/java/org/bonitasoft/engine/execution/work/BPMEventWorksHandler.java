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

import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.core.process.instance.api.event.EventInstanceService;
import org.bonitasoft.engine.service.PlatformServiceAccessor;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * Resets all "In Progress" BPMN Message couples so that they can be triggered again on next cron.
 * Restart work {@link ExecuteMessageCoupleWork}
 * 
 * @author Emmanuel Duchastenier
 */
public class BPMEventWorksHandler implements TenantRestartHandler {

    @Override
    public void handleRestart(final PlatformServiceAccessor platformServiceAccessor, final TenantServiceAccessor tenantServiceAccessor) throws RestartException {
        final EventInstanceService eventInstanceService = tenantServiceAccessor.getEventInstanceService();
        try {
            // Reset of all SMessageInstance:
            eventInstanceService.resetProgressMessageInstances();

            // Reset of all SWaitingMessageEvent:
            eventInstanceService.resetInProgressWaitingEvents();

        } catch (final SBonitaException e) {
            handleException("Unable to reset MessageInstances / WaitingMessageEvents that were 'In Progress' when the node stopped", e);
        }
    }

    private void handleException(final String message, final Exception e) throws RestartException {
        throw new RestartException(message, e);
    }
}
