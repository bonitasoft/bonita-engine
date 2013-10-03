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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.events.model.builders.SEventBuilders;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapConfig.InMemoryFormat;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;

/**
 *
 * @author Laurent Vaills
 *
 */
public class ClusteredEventServiceImpl extends ConfigurableEventServiceImpl {

    private final IMap<String, List<SHandler<SEvent>>> eventHandlers;
    // Local copy of the eventHandlers Map keys to speed up performance. It has to be manipulated
    // only by the distributed tasks.
    private final Set<String> localEventTypes = Collections.synchronizedSet(new HashSet<String>());
    private final HazelcastInstance hazelcastInstance;

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
        Config config = hazelcastInstance.getConfig();

        NearCacheConfig nearCacheConfig = new NearCacheConfig();
        nearCacheConfig.setInMemoryFormat(InMemoryFormat.CACHED);

        config.addMapConfig(new MapConfig(mapName));
        // ---
        this.hazelcastInstance = hazelcastInstance;
        hazelcastInstance.getUserContext().put("ClusteredEventService", this);
        eventHandlers = hazelcastInstance.getMap(mapName);

        // TODO
        // See to fill the Set localEventTypes from another cluster's member
        // Execute a Callable on a member that returns a copy of its own Set ?
        // What if another handler is added while was executed but not yet returned ?

        // We have created a Map that is shared across the cluster, so we do not need this one.
        registeredHandlers = null;
    }

    @Override
    protected boolean containsHandlerFor(final String eventType) {
        return localEventTypes.contains(eventType);
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
            EventTypeCallable task = new AddEventTypeCallable(eventType);
            informOtherMembers(task);
        }
    }

    /**
     * @param task
     */
    private void informOtherMembers(final LocalClusteredServiceTask task) {
        IExecutorService executorService = hazelcastInstance.getExecutorService("ClusteredEventServiceHandlerManagement");
        Map<Member, Future<Void>> results = executorService.submitToAllMembers(task);
        for(Map.Entry<Member, Future<Void>> entry : results.entrySet()) {
            try {
                entry.getValue().get();
            } catch (InterruptedException e) {
                throw new SBonitaRuntimeException(e);
            } catch (ExecutionException e) {
                throw new SBonitaRuntimeException(e);
            }
        }
    }

    @Override
    protected void removeAllHandlersFor(final SHandler<SEvent> handler) {
        for(String eventType : eventHandlers.keySet()) {
            List<SHandler<SEvent>> handlers = eventHandlers.get(eventType);

            handlers.remove(handler);
            if (handlers.isEmpty()) {
                eventHandlers.remove(eventType);
                EventTypeCallable task = new RemoveEventTypeCallable(eventType);
                informOtherMembers(task);
            } else {
                eventHandlers.replace(eventType, handlers);
            }
        }
    }

    // It should be used only for the tests
    /*package*/ synchronized void removeAllHandlers() {
        eventHandlers.clear();
        informOtherMembers(new ClearLocalEventType());
    }

    private static abstract class LocalClusteredServiceTask implements Callable<Void>, HazelcastInstanceAware {
        private transient HazelcastInstance hazelcastInstance;

        @Override
        public void setHazelcastInstance(final HazelcastInstance hazelcastInstance) {
            this.hazelcastInstance = hazelcastInstance;
        }

        @Override
        public Void call() throws Exception {
            ClusteredEventServiceImpl clusteredEventService = (ClusteredEventServiceImpl) hazelcastInstance.getUserContext().get("ClusteredEventService");
            doWithClusteredEventService(clusteredEventService);
            return null;
        }

        /**
         * @param clusteredEventService
         */
        protected abstract void doWithClusteredEventService(final ClusteredEventServiceImpl clusteredEventService);

    }

    private static class ClearLocalEventType extends LocalClusteredServiceTask {

        @Override
        protected void doWithClusteredEventService(final ClusteredEventServiceImpl clusteredEventService) {
            clusteredEventService.localEventTypes.clear();
        }

    }

    private static abstract class EventTypeCallable extends LocalClusteredServiceTask {

        protected final String eventType;

        public EventTypeCallable(final String eventType) {
            this.eventType = eventType;
        }

    }

    private static class AddEventTypeCallable extends EventTypeCallable {

        public AddEventTypeCallable(final String eventType) {
            super(eventType);
        }

        @Override
        protected void doWithClusteredEventService(final ClusteredEventServiceImpl clusteredEventService) {
            clusteredEventService.localEventTypes.add(eventType);
        }

    }

    private static class RemoveEventTypeCallable extends EventTypeCallable {

        public RemoveEventTypeCallable(final String eventType) {
            super(eventType);
        }

        @Override
        protected void doWithClusteredEventService(final ClusteredEventServiceImpl clusteredEventService) {
            clusteredEventService.localEventTypes.remove(eventType);
        }

    }

}
