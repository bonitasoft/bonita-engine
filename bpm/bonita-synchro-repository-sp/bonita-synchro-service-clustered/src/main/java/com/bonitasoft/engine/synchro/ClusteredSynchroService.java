/*******************************************************************************
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.synchro;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

import org.bonitasoft.engine.cache.CommonCacheService;
import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.synchro.AbstractSynchroService;

import com.bonitasoft.manager.Features;
import com.bonitasoft.manager.Manager;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISemaphore;

/**
 * @author Emmanuel Duchastenier
 * @author Baptiste Mesta
 */
public class ClusteredSynchroService extends AbstractSynchroService {

    private final static String CLUSTERED_SYNCHRO_SERVICE_WAITERS = "CLUSTERED_SYNCHRO_SERVICE_WAITERS";

    private final static String CLUSTERED_SYNCHRO_SERVICE_LOCK = "CLUSTERED_SYNCHRO_SERVICE_LOCK";

    private final HazelcastInstance hazelcastInstance;

    public ClusteredSynchroService(final TechnicalLoggerService logger, final HazelcastInstance hazelcastInstance, final CommonCacheService cacheService) {
        super(logger, cacheService);
        if (!Manager.getInstance().isFeatureActive(Features.ENGINE_CLUSTERING)) {
            throw new IllegalStateException("The clustering is not an active feature.");
        }
        this.hazelcastInstance = hazelcastInstance;
    }

    /**
     * String value is an identifier of the sempaphore for the current event (event.hashCode())
     */
    @Override
    protected Map<Map<String, Serializable>, String> getWaitersMap() {
        return hazelcastInstance.getMap(CLUSTERED_SYNCHRO_SERVICE_WAITERS);
    }

    @Override
    protected Map<String, Serializable> getEventKeyAndIdMap() {
        return hazelcastInstance.getMap("CLUSTERED_SYNCHRO_SERVICE_EVENT");
    }

    @Override
    protected Lock getServiceLock() {
        return hazelcastInstance.getLock(CLUSTERED_SYNCHRO_SERVICE_LOCK);
    }

    /**
     * @param semaphoreKey
     */
    @Override
    protected void releaseWaiter(final String semaphoreKey) {
        ISemaphore semaphore = hazelcastInstance.getSemaphore(semaphoreKey);
        semaphore.release();
    }

    @Override
    public Serializable waitForEvent(final Map<String, Serializable> event, final long timeout) throws InterruptedException, TimeoutException {
        Serializable id = null;
        String semaphoreKey = null;
        ISemaphore semaphore = null;
        getServiceLock().lock();
        try {
            id = getFiredAndRemoveIt(event);
            if (id != null) {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "waiting for event " + event + " already fired, returning directly");
                // Event has already been fired
            } else {
                logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "waiting for event " + event + " NOT fired yet, waiting for " + timeout + " ms max.");
                semaphoreKey = String.valueOf(event.hashCode());
                semaphore = hazelcastInstance.getSemaphore(semaphoreKey);
                semaphore.init(1);
                semaphore.acquire(1);
                getWaitersMap().put(event, semaphoreKey);
            }
        } finally {
            getServiceLock().unlock();
        }
        if (semaphore != null) {
            try {
                if (!semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                    throwTimeout(event, timeout);
                }
            } catch (final InterruptedException e) {
                throwTimeout(event, timeout);
            } finally {
                getWaitersMap().remove(event);
            }
            return getEventKeyAndIdMap().get(semaphoreKey);
        }
        logger.log(this.getClass(), TechnicalLogSeverity.DEBUG, "Event arrived. Returning ID " + id);
        return id;
    }

}
