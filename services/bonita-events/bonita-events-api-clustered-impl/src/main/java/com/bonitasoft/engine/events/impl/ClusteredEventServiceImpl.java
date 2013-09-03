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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.builders.SEventBuilders;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;

public class ClusteredEventServiceImpl extends ConfigurableEventServiceImpl {

    private final MultiMap<String, SHandler<SEvent>> multimapHandlers;

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
        multimapHandlers = hazelcastInstance.getMultiMap("EVENT_SERVICE_HANDLERS-" + eventServiceHandlerMapNameSuffix);

        // Create a Map that is shared across the cluster.
        registeredHandlers = null;

        // register the handlers with their associated event type
        for (final Map.Entry<String, SHandler<SEvent>> entry : handlers.entrySet()) {
            addHandler(entry.getKey(), entry.getValue());
        }
    }

    @Override
    protected boolean containsHandlerFor(String key) {
        return multimapHandlers.containsKey(key);
    }

    @Override
    protected Collection<SHandler<SEvent>> getHandlersFor(final String eventType) {
        return multimapHandlers.get(eventType);
    }

    @Override
    public Map<String, Set<SHandler<SEvent>>> getRegisteredHandlers() {
        Set<Entry<String, SHandler<SEvent>>> entrySet = multimapHandlers.entrySet();
        HashMap<String, Set<SHandler<SEvent>>> hashMap = new HashMap<String, Set<SHandler<SEvent>>>();
        for (Entry<String, SHandler<SEvent>> entry : entrySet) {
            if (!hashMap.containsKey(entry.getKey())) {
                hashMap.put(entry.getKey(), new HashSet<SHandler<SEvent>>());
            }
            hashMap.get(entry.getKey()).add(entry.getValue());
        }
        return hashMap;
    }

    @Override
    protected void addHandlerFor(String eventType, SHandler<SEvent> handler) {
        if (!multimapHandlers.containsEntry(eventType, handler)) {
            multimapHandlers.put(eventType, handler);
        }
    }

    @Override
    protected void removeHandlerInAllType(SHandler<SEvent> handler) {
        Set<Entry<String, SHandler<SEvent>>> entrySet = multimapHandlers.entrySet();
        for (Entry<String, SHandler<SEvent>> entry : entrySet) {
            if (handler.equals(entry.getValue())) {
                multimapHandlers.remove(entry.getKey(), entry.getValue());
            }
        }
    }

}
