/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.events.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bonitasoft.engine.commons.exceptions.SBonitaRuntimeException;
import org.bonitasoft.engine.events.impl.AbstractEventServiceImpl;
import org.bonitasoft.engine.events.model.HandlerRegistrationException;
import org.bonitasoft.engine.events.model.HandlerUnregistrationException;
import org.bonitasoft.engine.events.model.SEvent;
import org.bonitasoft.engine.events.model.SHandler;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
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
 * @author Laurent Vaills
 */
public class ClusteredEventServiceImpl extends AbstractEventServiceImpl {

    private final IMap<String, List<SHandler<SEvent>>> eventHandlers;

    // Local copy of the eventHandlers Map keys to speed up performance. It has to be manipulated only by the distributed tasks.
    private final Set<String> localEventTypes = new HashSet<String>();

    private final ReadWriteLock localEventTypesLock = new ReentrantReadWriteLock();

    private final HazelcastInstance hazelcastInstance;

    private final String mapName;

    private final String clusteredServicelocalUserContext;

    public ClusteredEventServiceImpl(final Map<String, SHandler<SEvent>> handlers, final TechnicalLoggerService logger,
            final HazelcastInstance hazelcastInstance) throws HandlerRegistrationException {
        this("PLATFORM", handlers, logger, hazelcastInstance, Manager.getInstance());
    }

    public ClusteredEventServiceImpl(final Map<String, SHandler<SEvent>> handlers, final TechnicalLoggerService logger,
            final HazelcastInstance hazelcastInstance, final long tenantId) throws HandlerRegistrationException {
        this("TENANT@" + tenantId, handlers, logger, hazelcastInstance, Manager.getInstance());
    }

    ClusteredEventServiceImpl(final String eventServiceHandlerMapNameSuffix,
            final Map<String, SHandler<SEvent>> handlers, final TechnicalLoggerService logger, final HazelcastInstance hazelcastInstance, final Manager manager)
                    throws HandlerRegistrationException {
        super(logger);
        if (!manager.isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        mapName = "EVENT_SERVICE_HANDLERS-" + eventServiceHandlerMapNameSuffix;

        logger.log(getClass(), TechnicalLogSeverity.TRACE, "instanciating Clustered event service " + mapName + " on " + this);

        // --- Hard coded configuration for Hazelcast
        final Config config = hazelcastInstance.getConfig();

        final NearCacheConfig nearCacheConfig = new NearCacheConfig();
        nearCacheConfig.setInMemoryFormat(InMemoryFormat.CACHED);

        config.addMapConfig(new MapConfig(mapName));
        // ---
        this.hazelcastInstance = hazelcastInstance;

        clusteredServicelocalUserContext = "ClusteredEventService" + eventServiceHandlerMapNameSuffix;
        hazelcastInstance.getUserContext().put(clusteredServicelocalUserContext, this);

        eventHandlers = hazelcastInstance.getMap(mapName);
        if (eventHandlers.isEmpty()) {
            // it seems we are the first one in the cluster so let's add the default handlers
            // There is another way, maybe cleaner, to know if we are the first one in the cluster :
            // hazelcastInstance.getCluster().getMembers().size() == 1
            // but that's not as easy in the tests to setup 2 hazelcast instances.
            // Furthermore, we have other constraints that prevents from starting 2 nodes at the same time
            // so the isEmpty() should be enough to guess we are the first one
            for (Map.Entry<String, SHandler<SEvent>> entry : handlers.entrySet()) {
                addHandler(entry.getKey(), entry.getValue());
            }
        } else {
            // If we are not the first one we have to initialize a local copy of the already registered events.
            localEventTypes.addAll(eventHandlers.keySet());
        }
    }

    @Override
    protected boolean containsHandlerFor(final String eventType) {
        boolean result = false;
        Lock readLock = localEventTypesLock.readLock();
        readLock.lock();
        try {
            return result = localEventTypes.contains(eventType);
        } finally {
            if (logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(getClass(), TechnicalLogSeverity.TRACE, "containsHandlerFor : " + eventType + " " + result);
            }
            readLock.unlock();
        }
    }

    @Override
    protected Collection<SHandler<SEvent>> getHandlersFor(final String eventType) {
        List<SHandler<SEvent>> result = eventHandlers.get(eventType);

        if (result == null && logger.isLoggable(getClass(), TechnicalLogSeverity.TRACE)) {
            logger.log(getClass(), TechnicalLogSeverity.TRACE, "WARNING, no handler found for type " + eventType);
        }

        return result;
    }

    @Override
    protected void addHandlerFor(final String eventType, final SHandler<SEvent> handler) throws HandlerRegistrationException {
        eventHandlers.lock(eventType);
        try {
            // check if the given event type is already registered in the Event Service:
            if (containsHandlerFor(eventType)) {
                // if the handler already exists for the same eventType, an Exception is thrown:
                final List<SHandler<SEvent>> handlers = eventHandlers.get(eventType);

                // Check if another handler of the same class is already registered:
                for (SHandler<SEvent> tmpHandler : handlers) {
                    if (tmpHandler.getIdentifier().equals(handler.getIdentifier())) {
                        throw new HandlerRegistrationException("The handler with identifier " + tmpHandler.getIdentifier()
                                + " is already registered for the event " + eventType);
                    }
                }
                handlers.add(handler);

                eventHandlers.replace(eventType, handlers);
            } else {
                // if the given type doesn't already exist in the eventFilters list, we create it:
                final List<SHandler<SEvent>> newHandlerList = new ArrayList<SHandler<SEvent>>(1);
                newHandlerList.add(handler);
                eventHandlers.put(eventType, newHandlerList);
                EventTypeCallable task = new AddEventTypeCallable(eventType, clusteredServicelocalUserContext);
                informAllMembers(task);
            }
        } finally {
            eventHandlers.unlock(eventType);
        }
    }

    /**
     * @param task
     */
    private void informAllMembers(final LocalClusteredServiceTask task) {
        IExecutorService executorService = hazelcastInstance.getExecutorService("ClusteredEventServiceHandlerManagement");
        Map<Member, Future<Void>> results = executorService.submitToAllMembers(task);
        for (Map.Entry<Member, Future<Void>> entry : results.entrySet()) {
            try {
                entry.getValue().get();
            } catch (final InterruptedException e) {
                throw new SBonitaRuntimeException("There was an error on the member " + entry.getKey() + " for the task " + task.getClass().getName(), e);
            } catch (final ExecutionException e) {
                throw new SBonitaRuntimeException("There was an error on the member " + entry.getKey() + " for the task " + task.getClass().getName(), e);
            }
        }
    }

    @Override
    protected void removeAllHandlersFor(final SHandler<SEvent> handler) {
        for (String eventType : eventHandlers.keySet()) {
            List<SHandler<SEvent>> handlers = eventHandlers.get(eventType);

            handlers.remove(handler);
            if (handlers.isEmpty()) {
                EventTypeCallable task = new RemoveEventTypeCallable(eventType, clusteredServicelocalUserContext);
                informAllMembers(task);
                eventHandlers.remove(eventType);
            } else {
                eventHandlers.replace(eventType, handlers);
            }
        }
    }

    // It should be used only for the tests
    /* package */
    synchronized void removeAllHandlers() {
        informAllMembers(new ClearLocalEventType(clusteredServicelocalUserContext));
        eventHandlers.clear();
    }

    private static abstract class LocalClusteredServiceTask implements Serializable, Callable<Void>, HazelcastInstanceAware {

        private static final long serialVersionUID = 1L;

        private transient HazelcastInstance hazelcastInstance;

        private final String clusteredServicelocalUserContext;

        protected LocalClusteredServiceTask(final String clusteredServicelocalUserContext) {
            this.clusteredServicelocalUserContext = clusteredServicelocalUserContext;
        }

        @Override
        public void setHazelcastInstance(final HazelcastInstance hazelcastInstance) {
            this.hazelcastInstance = hazelcastInstance;
        }

        @Override
        public Void call() {
            ClusteredEventServiceImpl clusteredEventService = (ClusteredEventServiceImpl) hazelcastInstance.getUserContext().get(
                    clusteredServicelocalUserContext);
            // If Service is not initialized yet on one node, ignore it:
            if (clusteredEventService != null) {
                doWithClusteredEventService(clusteredEventService);
            }
            return null;
        }

        /**
         * @param clusteredEventService
         */
        protected abstract void doWithClusteredEventService(final ClusteredEventServiceImpl clusteredEventService);

    }

    private static abstract class WriteLockLocalClusteredServiceTask extends LocalClusteredServiceTask {

        private static final long serialVersionUID = 1L;

        WriteLockLocalClusteredServiceTask(final String clusteredServicelocalUserContext) {
            super(clusteredServicelocalUserContext);
        }

        @Override
        final protected void doWithClusteredEventService(final ClusteredEventServiceImpl clusteredEventService) {
            Lock writeLock = clusteredEventService.localEventTypesLock.writeLock();
            writeLock.lock();
            try {
                doInsideWriteLock(clusteredEventService);
            } finally {
                writeLock.unlock();
            }
        }

        /**
         * @param clusteredEventService
         */
        protected abstract void doInsideWriteLock(final ClusteredEventServiceImpl clusteredEventService);

    }

    private static class ClearLocalEventType extends WriteLockLocalClusteredServiceTask {

        private static final long serialVersionUID = 1L;

        ClearLocalEventType(final String clusteredServicelocalUserContext) {
            super(clusteredServicelocalUserContext);
        }

        @Override
        protected void doInsideWriteLock(final ClusteredEventServiceImpl clusteredEventService) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Informing all nodes to clear the registered event types.");
            }
            clusteredEventService.localEventTypes.clear();
        }

    }

    private static abstract class EventTypeCallable extends WriteLockLocalClusteredServiceTask {

        private static final long serialVersionUID = 1L;

        protected final String eventType;

        public EventTypeCallable(final String eventType, final String clusteredServicelocalUserContext) {
            super(clusteredServicelocalUserContext);
            this.eventType = eventType;
        }

    }

    private static class AddEventTypeCallable extends EventTypeCallable {

        private static final long serialVersionUID = 1L;

        public AddEventTypeCallable(final String eventType, final String clusteredServicelocalUserContext) {
            super(eventType, clusteredServicelocalUserContext);
        }

        @Override
        protected void doInsideWriteLock(final ClusteredEventServiceImpl clusteredEventService) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Informing all nodes to register new event type " + eventType);
            }
            clusteredEventService.localEventTypes.add(eventType);
        }

    }

    private static class RemoveEventTypeCallable extends EventTypeCallable {

        private static final long serialVersionUID = 1L;

        public RemoveEventTypeCallable(final String eventType, final String clusteredServicelocalUserContext) {
            super(eventType, clusteredServicelocalUserContext);
        }

        @Override
        protected void doInsideWriteLock(final ClusteredEventServiceImpl clusteredEventService) {
            if (logger.isLoggable(this.getClass(), TechnicalLogSeverity.TRACE)) {
                logger.log(this.getClass(), TechnicalLogSeverity.TRACE, "Informing all nodes to UNregister event type " + eventType);
            }
            clusteredEventService.localEventTypes.remove(eventType);
        }

    }

    @Override
    protected void removeHandlerFor(final String eventType, final SHandler<SEvent> h) throws HandlerUnregistrationException {
        boolean removed = false;
        eventHandlers.lock(eventType);
        try {
            List<SHandler<SEvent>> handlers = eventHandlers.get(eventType);
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
            eventHandlers.replace(eventType, handlers);
        } finally {
            eventHandlers.unlock(eventType);
        }
    }

}
