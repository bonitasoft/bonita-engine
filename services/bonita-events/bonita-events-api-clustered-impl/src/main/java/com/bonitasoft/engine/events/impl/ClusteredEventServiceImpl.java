/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
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
package com.bonitasoft.engine.events.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.builders.SEventBuilders;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

import com.hazelcast.core.HazelcastInstance;

public class ClusteredEventServiceImpl extends ConfigurableEventServiceImpl {

    private final ConcurrentMap<String, List<SHandler<SEvent>>> eventHandlers;

    public ClusteredEventServiceImpl(final SEventBuilders eventBuilders, final Map<String, SHandler<SEvent>> handlers, final TechnicalLoggerService logger,
            final HazelcastInstance hazelcastInstance) throws HandlerRegistrationException {
        this("PLATFORM", eventBuilders, handlers, logger, hazelcastInstance);
    }

    public ClusteredEventServiceImpl(final SEventBuilders eventBuilders, final Map<String, SHandler<SEvent>> handlers, final TechnicalLoggerService logger,
            final HazelcastInstance hazelcastInstance, final long tenantId) throws HandlerRegistrationException {
        this("TENANT@" + tenantId, eventBuilders, handlers, logger, hazelcastInstance);
    }

    private ClusteredEventServiceImpl(final String eventServiceHandlerMapNameSuffix, final SEventBuilders eventBuilders,
            final Map<String, SHandler<SEvent>> handlers, final TechnicalLoggerService logger, final HazelcastInstance hazelcastInstance)
            throws HandlerRegistrationException {
        super(eventBuilders, handlers, logger);
        String mapName = "EVENT_SERVICE_HANDLERS-" + eventServiceHandlerMapNameSuffix;
        // --- Hard coded configuration for Hazelcast
//        Config config = hazelcastInstance.getConfig();
//
//        NearCacheConfig nearCacheConfig = new NearCacheConfig();
//        nearCacheConfig.setInMemoryFormat(InMemoryFormat.OBJECT);
//
//        config.addMapConfig(new MapConfig(mapName));
        // ---
        eventHandlers = hazelcastInstance.getMap(mapName);

        // Create a Map that is shared across the cluster.
        registeredHandlers = null;
    }

    @Override
    protected boolean containsHandlerFor(final String eventType) {
        return eventHandlers.containsKey(eventType);
    }

    @Override
    protected Collection<SHandler<SEvent>> getHandlersFor(final String eventType) {
        return eventHandlers.get(eventType);
    }

    @Override
    protected void addHandlerFor(final String eventType, final SHandler<SEvent> handler) throws HandlerRegistrationException {
        // check if the given event type is already registered in the Event Service
        if (containsHandlerFor(eventType)) {
            // if the handler already exists for the same eventType, an Exception is thrown
            final List<SHandler<SEvent>> handlers = eventHandlers.get(eventType);
            // the add method returns false if the given element already exists in the Set, and does nothing.
            if (!handlers.add(handler)) {
                throw new HandlerRegistrationException("This handler is already registered for this event type");
            }

            eventHandlers.replace(eventType, handlers);
        } else {
            // if the given type doesn't already exist in the eventFilters list, we create it
            final List<SHandler<SEvent>> newHandlerSet = new ArrayList<SHandler<SEvent>>(3);
            newHandlerSet.add(handler);
            eventHandlers.put(eventType, newHandlerSet);
        }
    }

    @Override
    protected void removeAllHandlersFor(final SHandler<SEvent> handler) {
        for(String eventType : eventHandlers.keySet()) {
            List<SHandler<SEvent>> handlers = eventHandlers.get(eventType);

            handlers.remove(handler);
            if (handlers.isEmpty()) {
                eventHandlers.remove(eventType);
            }
        }
    }

    // It should be used only for the tests
    /*package*/ void removeAllHandlers() {
        eventHandlers.clear();
    }

}
