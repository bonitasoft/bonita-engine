/*
 * Copyright (C) 2015 Bonitasoft S.A..
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.bonitasoft.engine.synchro;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.bonitasoft.engine.command.SCommandExecutionException;
import org.bonitasoft.engine.command.TenantCommand;
import org.bonitasoft.engine.events.EventService;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.service.TenantServiceAccessor;

/**
 * @author Guillaume Rosinosky <guillaume.rosinosky@bonitasoft.com>
 */
public class RemovePerfHandlerCommand extends TenantCommand {

    private static final String PROCESSINSTANCE_STATE_UPDATED = "PROCESSINSTANCE_STATE_UPDATED";

    private static final String ACTIVITYINSTANCE_STATE_UPDATED = "ACTIVITYINSTANCE_STATE_UPDATED";
    
    @Override
    public Serializable execute(final Map<String, Serializable> parameters, final TenantServiceAccessor serviceAccessor)
            throws SCommandExecutionException {
        JMSProducer.resetInstance();
        
        final EventService eventService = serviceAccessor.getEventService();
        
        // Default: remove all the concerned handlers 
        // TODO : remove specific handlers (those who have been created in addPerf and not others)
        removeAllHandlers(eventService, PROCESSINSTANCE_STATE_UPDATED, ProcessInstanceFinishedHandler.class);
        removeAllHandlers(eventService, ACTIVITYINSTANCE_STATE_UPDATED, TaskReadyHandler.class);
        removeAllHandlers(eventService, ACTIVITYINSTANCE_STATE_UPDATED, FlowNodeReachStateHandler.class);

        return null;
    }    

    private void removeAllHandlers(final EventService eventService, final String eventType, final Class<?> clazz) {
        final Set<SHandler<SEvent>> handlers = eventService.getHandlers(eventType);
        Collection<SHandler<SEvent>> removeCandidates = new LinkedList<SHandler<SEvent>>();
        
        if (handlers != null) {
            for (final Iterator<SHandler<SEvent>> iter = handlers.iterator(); iter.hasNext(); ) {
                final SHandler<SEvent> handler = iter.next();
                if (clazz.isInstance(handler)) {
                    removeCandidates.add(handler);
                }
            }
            handlers.removeAll(removeCandidates);
        }
    }    
    
}
