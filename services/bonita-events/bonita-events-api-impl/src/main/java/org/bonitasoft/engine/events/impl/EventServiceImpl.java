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
package org.bonitasoft.engine.events.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.HandlerUnregistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

/**
 * @author Christophe Havard
 * @author Matthieu Chaffotte
 * @author Laurent Vaills
 */
public class EventServiceImpl extends AbstractEventServiceImpl {

    /**
     * Contains a list of all events type and their registered handlers
     */
    protected Map<String, List<SHandler<SEvent>>> registeredHandlers;

    public EventServiceImpl(final TechnicalLoggerService logger) {
        super(logger);
        registeredHandlers = new HashMap<String, List<SHandler<SEvent>>>();
    }

    @Override
    protected boolean containsHandlerFor(final String key) {
        return registeredHandlers.containsKey(key);
    }

    @Override
    protected Collection<SHandler<SEvent>> getHandlersFor(final String eventType) {
        return registeredHandlers.get(eventType);
    }

    @Override
    protected void addHandlerFor(final String eventType, final SHandler<SEvent> handler) throws HandlerRegistrationException {
        // check if the given event type is already registered in the Event Service
        if (containsHandlerFor(eventType)) {
            // if the handler already exists for the same eventType, an Exception is thrown
            final List<SHandler<SEvent>> handlers = registeredHandlers.get(eventType);

            // Check if another handler of the same class is already registered
            for (SHandler<SEvent> tmpHandler : handlers) {
                if (tmpHandler.getIdentifier().equals(handler.getIdentifier())) {
                    throw new HandlerRegistrationException("The handler with identifier " + tmpHandler.getIdentifier() + " is already registered for the event " + eventType);
                }
            }

            handlers.add(handler);
        } else {
            // if the given type doesn't already exist in the eventFilters list, we create it
            final List<SHandler<SEvent>> newHandlerList = new ArrayList<SHandler<SEvent>>(3);
            newHandlerList.add(handler);
            registeredHandlers.put(eventType, newHandlerList);
        }
    }

    @Override
    protected void removeAllHandlersFor(final SHandler<SEvent> handler) {
        for (final String eventType : registeredHandlers.keySet()) {
            try {
                removeHandler(eventType, handler);
            } catch (HandlerUnregistrationException e) {
                // Nothing to do.
            }
        }
    }

    @Override
    protected void removeHandlerFor(final String eventType, final SHandler<SEvent> h) throws HandlerUnregistrationException {
        boolean removed = false;
        Collection<SHandler<SEvent>> handlers = getHandlersFor(eventType);
        if (handlers != null) {
            Iterator<SHandler<SEvent>> it = handlers.iterator();
            while (!removed && it.hasNext()) {
                SHandler<SEvent> handler = it.next();
                if (h.getIdentifier().equals(handler.getIdentifier())) {
                    it.remove();
                    removed = true;
                }
            }
        }
        if (!removed) {
            throw new HandlerUnregistrationException();
        }
    }
}
