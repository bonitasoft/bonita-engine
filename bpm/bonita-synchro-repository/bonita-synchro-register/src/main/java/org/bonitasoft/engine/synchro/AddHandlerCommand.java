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
package org.bonitasoft.engine.synchro;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.commons.exceptions.SBonitaException;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.service.TenantServiceAccessor;

public class AddHandlerCommand extends TenantCommand {

    private static final String PROCESSINSTANCE_STATE_UPDATED = "PROCESSINSTANCE_STATE_UPDATED";

    private static final String ACTIVITYINSTANCE_CREATED = "ACTIVITYINSTANCE_CREATED";

    private static final String ACTIVITYINSTANCE_STATE_UPDATED = "ACTIVITYINSTANCE_STATE_UPDATED";

    private static final String EVENT_INSTANCE_CREATED = "EVENT_INSTANCE_CREATED";

    private static final String GATEWAYINSTANCE_CREATED = "GATEWAYINSTANCE_CREATED";

    private static final String GATEWAYINSTANCE_STATE_UPDATED = "GATEWAYINSTANCE_STATE_UPDATED";

    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandExecutionException {
        final EventService eventService = serviceAccessor.getEventService();
        try {
            final long tenantId = serviceAccessor.getTenantId();
            if (!containsHandler(eventService, PROCESSINSTANCE_STATE_UPDATED, ProcessInstanceHandler.class)) {
                eventService.addHandler(PROCESSINSTANCE_STATE_UPDATED, new ProcessInstanceHandler(tenantId));
            }
            if (!containsHandler(eventService, ACTIVITYINSTANCE_STATE_UPDATED, FlowNodeHandler.class)) {
                eventService.addHandler(ACTIVITYINSTANCE_STATE_UPDATED, new FlowNodeHandler(tenantId));
                eventService.addHandler(ACTIVITYINSTANCE_CREATED, new FlowNodeHandler(tenantId));
                eventService.addHandler(EVENT_INSTANCE_CREATED, new FlowNodeHandler(tenantId));
            }
            if (!containsHandler(eventService, GATEWAYINSTANCE_CREATED, GatewayHandler.class)) {
                eventService.addHandler(GATEWAYINSTANCE_CREATED, new GatewayHandler(tenantId));
                eventService.addHandler(GATEWAYINSTANCE_STATE_UPDATED, new GatewayHandler(tenantId));
            }
        } catch (final SBonitaException e) {
            throw new SCommandExecutionException(e);
        }
        return null;
    }

    private boolean containsHandler(final EventService eventService, final String eventType, final Class<?> clazz) {
        final Set<SHandler<SEvent>> handlers = eventService.getHandlers(eventType);
        if (handlers != null) {
            for (final SHandler<SEvent> sHandler : handlers) {
                if (clazz.isInstance(sHandler)) {
                    return true;
                }
            }
        }
        return false;
    }

}
